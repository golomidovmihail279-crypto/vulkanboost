#!/usr/bin/env python3
"""Check RenderScale shaders inside the final VulkanBoost JAR.

Usage: python scripts/check_renderscale_shaders.py build/libs/vulkanboost-0.5.jar

The checker inspects outer JAR -> nested RenderScale JAR -> GLSL resources.
It treats unsupported shader-code characters, apostrophes, and unterminated
block comments as errors. Block comments elsewhere are only reported as warnings.
"""
import io
import json
import re
import sys
import zipfile

SHADER_RE = re.compile(r"^assets/renderscale/shaders/.*\.(glsl|fsh|vsh)$")
REQUIRED_SHADERS = {
    "assets/renderscale/shaders/include/ffx_a.glsl",
    "assets/renderscale/shaders/include/fsr_easu.glsl",
}


def scan_glsl(text):
    """Return [(level, line, message)] after scanning GLSL comments lexically."""
    findings = []
    i = 0
    line = 1
    state = "code"
    block_start = 0

    while i < len(text):
        c = text[i]
        nxt = text[i + 1] if i + 1 < len(text) else ""
        if c == "\n":
            line += 1

        if state == "code":
            if c == "/" and nxt == "/":
                state = "line"
                i += 2
                continue
            if c == "/" and nxt == "*":
                state = "block"
                block_start = line
                findings.append(("WARN", line, "block comment"))
                i += 2
                continue
            if c == "~":
                findings.append(("ERROR", line, "bitwise NOT '~' in code"))
            elif c == "'":
                findings.append(("ERROR", line, "apostrophe outside a comment"))
            elif ord(c) > 126:
                findings.append(("ERROR", line, "non-ASCII character U+%04X in code" % ord(c)))
        elif state == "line":
            if c == "\n" or c == "\r":
                state = "code"
            elif c == "'":
                findings.append(("ERROR", line, "apostrophe inside // comment"))
            elif ord(c) > 126:
                findings.append(("WARN", line, "non-ASCII character U+%04X in comment" % ord(c)))
        else:
            if c == "*" and nxt == "/":
                state = "code"
                i += 2
                continue
            if c == "'":
                findings.append(("ERROR", line, "apostrophe inside /* */ comment"))
            elif ord(c) > 126:
                findings.append(("WARN", line, "non-ASCII character U+%04X in comment" % ord(c)))
        i += 1

    if state == "block":
        findings.append(("ERROR", block_start, "unterminated block comment"))
    return findings


def code_only(text):
    """Return code while removing comments, preserving line boundaries."""
    out = []
    i = 0
    state = "code"
    while i < len(text):
        c = text[i]
        nxt = text[i + 1] if i + 1 < len(text) else ""
        if state == "code":
            if c == "/" and nxt == "/":
                state = "line"
                i += 2
                continue
            if c == "/" and nxt == "*":
                state = "block"
                i += 2
                continue
            out.append(c)
        elif state == "line":
            if c == "\n":
                state = "code"
                out.append(c)
        elif c == "*" and nxt == "/":
            state = "code"
            i += 2
            continue
        i += 1
    return "".join(out)


def sanitize_comment_apostrophes(text):
    """Remove apostrophes from comments only; reject apostrophes in code."""
    out = []
    i = 0
    state = "code"
    while i < len(text):
        c = text[i]
        nxt = text[i + 1] if i + 1 < len(text) else ""
        if state == "code":
            if c == "/" and nxt == "/":
                out.extend(("/", "/"))
                state = "line"
                i += 2
                continue
            if c == "/" and nxt == "*":
                out.extend(("/", "*"))
                state = "block"
                i += 2
                continue
            if c == "'":
                raise ValueError("apostrophe outside a comment")
            out.append(c)
        elif state == "line":
            if c == "\n" or c == "\r":
                state = "code"
                out.append(c)
            elif c != "'":
                out.append(c)
        else:
            if c == "*" and nxt == "/":
                out.extend(("*", "/"))
                state = "code"
                i += 2
                continue
            if c != "'":
                out.append(c)
        i += 1
    if state == "block":
        raise ValueError("unterminated block comment")
    return "".join(out)


def check_jar(path):
    """Return (RenderScale nested-JAR path, findings)."""
    findings = []
    with zipfile.ZipFile(path) as outer:
        outer_names = set(outer.namelist())
        if "fabric.mod.json" not in outer_names:
            raise SystemExit("ERROR: outer JAR is missing fabric.mod.json")
        try:
            root_metadata = json.loads(outer.read("fabric.mod.json"))
        except (json.JSONDecodeError, UnicodeDecodeError) as exc:
            raise SystemExit("ERROR: invalid outer fabric.mod.json: " + str(exc))

        render_scale_matches = []
        for nested in root_metadata.get("jars", []):
            nested_path = nested.get("file")
            if not nested_path or nested_path not in outer_names:
                findings.append(("ERROR", str(nested_path), 0, "missing nested JAR", ""))
                continue
            data = outer.read(nested_path)
            try:
                with zipfile.ZipFile(io.BytesIO(data)) as child:
                    child_names = child.namelist()
                    child_meta = {}
                    if "fabric.mod.json" in child_names:
                        child_meta = json.loads(child.read("fabric.mod.json"))
                    if child_meta.get("id") == "renderscale":
                        render_scale_matches.append((nested_path, data))
                    elif any(SHADER_RE.match(name) for name in child_names):
                        findings.append(("ERROR", nested_path, 0, "stale RenderScale shader path in another nested JAR", ""))
            except (zipfile.BadZipFile, json.JSONDecodeError, UnicodeDecodeError) as exc:
                findings.append(("ERROR", nested_path, 0, "invalid nested JAR/metadata: " + str(exc), ""))

        if len(render_scale_matches) == 0:
            raise SystemExit("ERROR: nested RenderScale JAR not found")
        if len(render_scale_matches) > 1:
            for nested_path, _ in render_scale_matches[1:]:
                findings.append(("ERROR", nested_path, 0, "duplicate nested JAR with id 'renderscale'", ""))
        render_scale_path, render_scale_bytes = render_scale_matches[0]

        for name in sorted(outer_names):
            if SHADER_RE.match(name):
                findings.append(("ERROR", name, 0, "stale RenderScale shader in OUTER JAR", ""))

        with zipfile.ZipFile(io.BytesIO(render_scale_bytes)) as render_scale:
            shader_names = {name for name in render_scale.namelist() if SHADER_RE.match(name)}
            for required in sorted(REQUIRED_SHADERS):
                if required not in shader_names:
                    findings.append(("ERROR", required, 0, "required shader missing from nested RenderScale JAR", ""))

            easu_path = "assets/renderscale/shaders/include/fsr_easu.glsl"
            if easu_path in shader_names:
                easu_text = render_scale.read(easu_path).decode("utf-8")
                if "/*" in easu_text or "*/" in easu_text:
                    findings.append(("ERROR", easu_path, 0, "EASU still contains block-comment markers", ""))
                if "// AMDs Vulkan example, which declares separate texture and sampler objects." not in easu_text:
                    findings.append(("ERROR", easu_path, 0, "normalized EASU comment marker missing", ""))

            for shader_path in sorted(shader_names):
                try:
                    text = render_scale.read(shader_path).decode("utf-8")
                except UnicodeDecodeError as exc:
                    findings.append(("ERROR", shader_path, 0, "shader is not valid UTF-8: " + str(exc), ""))
                    continue
                lines = text.splitlines()
                for level, line_no, message in scan_glsl(text):
                    content = lines[line_no - 1].strip() if 0 < line_no <= len(lines) else ""
                    findings.append((level, shader_path, line_no, message, content))

    return render_scale_path, findings


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit(__doc__)
    nested_path, findings = check_jar(sys.argv[1])
    print("nested RenderScale JAR:", nested_path)
    for level, shader, line_no, message, content in findings:
        print("%-5s %s:%s: %s%s" % (
            level, shader, line_no, message,
            ("  | " + content[:100]) if content else ""
        ))
    error_count = sum(1 for item in findings if item[0] == "ERROR")
    warning_count = len(findings) - error_count
    print("%d error(s), %d warning(s)" % (error_count, warning_count))
    sys.exit(1 if error_count else 0)
