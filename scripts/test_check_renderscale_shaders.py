"""Unit tests for the RenderScale shader checker and comment normalization."""
import io
import json
import os
import re
import tempfile
import unittest
import zipfile

import check_renderscale_shaders as chk


FFX_A = "// header\nuint a = ~mask;\n"
EASU_SRC = (
    "#version 150\n"
    "/*\n"
    " * Port of AMD's Vulkan example.\n"
    "   continuation line without a star, it's plain text\n"
    " */\n"
    "vec3 FsrEasuF(vec3 c){ return c * 0.5; }\n"
)


def old_patch(shader):
    """Emulate the original build.gradle block-comment regex."""
    marker = shader.index("AMD's Vulkan example")
    start = shader.rindex("/*", 0, marker)
    end = shader.index("*/", marker)
    body = shader[start + 2:end]
    line_comment = re.sub(r"(?m)^[ \t]*\*", "//", body)
    return shader[:start] + line_comment + shader[end + 2:]


def proposed_patch(shader):
    """Reference behavior for the line-comment conversion and apostrophe pass."""
    marker = shader.index("AMD's Vulkan example")
    start = shader.rindex("/*", 0, marker)
    end = shader.index("*/", marker)
    after = shader[end + 2:]
    if after.split("\n", 1)[0].strip():
        raise ValueError("code after block comment on same line")
    body = shader[start + 2:end]
    lines = []
    for line in body.split("\n"):
        clean = re.sub(r"^[ \t]*\*", "", line)
        lines.append("//" + clean if clean.strip() else "")
    converted = shader[:start] + "\n".join(lines) + after
    return chk.sanitize_comment_apostrophes(converted)


def build_jar(ffx_a, easu, extra_outer=None, extra_nested=None, rcas_shader=None):
    nested = io.BytesIO()
    with zipfile.ZipFile(nested, "w") as archive:
        archive.writestr("fabric.mod.json", json.dumps({"id": "renderscale"}))
        archive.writestr("assets/renderscale/shaders/include/ffx_a.glsl", ffx_a)
        archive.writestr("assets/renderscale/shaders/include/fsr_easu.glsl", easu)
        archive.writestr(
            "assets/renderscale/shaders/core/easu.fsh",
            "#moj_import <renderscale:ffx_fsr1_easu.glsl>\n"
        )
        archive.writestr(
            "assets/renderscale/shaders/core/rcas.fsh",
            rcas_shader or "#moj_import <renderscale:ffx_fsr1_rcas.glsl>\n"
        )
        archive.writestr(
            "assets/renderscale/shaders/include/ffx_fsr1_easu.glsl",
            "void FsrEasuF() {}\n"
        )
        archive.writestr(
            "assets/renderscale/shaders/include/ffx_fsr1_rcas.glsl",
            "void FsrRcasCon() {}\nvoid FsrRcasF() {}\n"
        )
        for path, content in (extra_nested or {}).items():
            archive.writestr(path, content)
    fd, path = tempfile.mkstemp(suffix=".jar")
    os.close(fd)
    with zipfile.ZipFile(path, "w") as archive:
        archive.writestr(
            "fabric.mod.json",
            json.dumps({"jars": [{"file": "META-INF/jars/renderscale.jar"}]})
        )
        archive.writestr("META-INF/jars/renderscale.jar", nested.getvalue())
        for item_path, content in (extra_outer or {}).items():
            archive.writestr(item_path, content)
    return path


def errors(path):
    return [item for item in chk.check_jar(path)[1] if item[0] == "ERROR"]


class CheckerTests(unittest.TestCase):
    def test_unpatched_jar_fails_on_tilde_and_apostrophes(self):
        result = errors(build_jar(FFX_A, EASU_SRC))
        messages = [item[3] for item in result]
        self.assertTrue(any("bitwise NOT" in message for message in messages))
        self.assertTrue(any("apostrophe" in message for message in messages))

    def test_old_regex_leaves_uncommented_continuation_text(self):
        patched = old_patch(EASU_SRC)
        self.assertIn("// Port of AMD's Vulkan example", patched)
        result = errors(build_jar("// header\nuint a = mask;\n", patched))
        self.assertTrue(any("apostrophe outside" in item[3] for item in result))

    def test_new_comment_patch_changes_comments_not_code(self):
        patched = proposed_patch(EASU_SRC)
        self.assertEqual(chk.code_only(EASU_SRC).split(), chk.code_only(patched).split())
        self.assertNotIn("'", patched)
        self.assertIn("FsrEasuF", chk.code_only(patched))
        self.assertFalse(any("apostrophe" in item[2] for item in chk.scan_glsl(patched)))

    def test_new_patch_rejects_code_after_comment_end(self):
        with self.assertRaises(ValueError):
            proposed_patch("/*\n * AMD's Vulkan example */ int x;\n")

    def test_stale_copy_in_outer_jar_is_detected(self):
        path = build_jar(
            "// ok\n",
            "// ok\n",
            extra_outer={"assets/renderscale/shaders/include/fsr_easu.glsl": "// stale\n"}
        )
        self.assertTrue(any("OUTER" in item[3] for item in errors(path)))

    def test_unterminated_block_comment_is_detected(self):
        path = build_jar("// ok\nint a;\n", "/* never closed\n")
        self.assertTrue(any("unterminated" in item[3] for item in errors(path)))

    def test_clean_nested_jar_passes(self):
        easu = (
            "// AMDs Vulkan example, which declares separate texture and sampler objects.\n"
            "vec4 FsrEasuF(vec4 c) { return c * 0.5; }\n"
        )
        path = build_jar("// ok\nuint a = mask;\n", easu)
        self.assertEqual(errors(path), [])

    def test_existing_line_comment_easu_is_normalized(self):
        source = "// AMD's Vulkan example, which declares separate texture and sampler objects.\n"
        patched = chk.sanitize_comment_apostrophes(source)
        self.assertIn("// AMDs Vulkan example, which declares separate texture and sampler objects.", patched)
        self.assertNotIn("'", patched)
        self.assertFalse(any("apostrophe" in item[2] for item in chk.scan_glsl(patched)))

    def test_mode_specific_headers_are_required_by_each_pipeline(self):
        path = build_jar("// ok\nuint a = mask;\n", (
            "// AMDs Vulkan example, which declares separate texture and sampler objects.\n"
            "vec4 FsrEasuF(vec4 c) { return c * 0.5; }\n"
        ))
        self.assertEqual(errors(path), [])
        with zipfile.ZipFile(path) as outer:
            nested = io.BytesIO(outer.read("META-INF/jars/renderscale.jar"))
            with zipfile.ZipFile(nested) as archive:
                easu = archive.read("assets/renderscale/shaders/core/easu.fsh").decode()
                rcas = archive.read("assets/renderscale/shaders/core/rcas.fsh").decode()
                self.assertIn("ffx_fsr1_easu.glsl", easu)
                self.assertIn("ffx_fsr1_rcas.glsl", rcas)
                self.assertNotIn("ffx_fsr1.glsl>", easu)
                self.assertNotIn("ffx_fsr1.glsl>", rcas)

    def test_easu_only_probe_copies_easu_output_without_calling_rcas(self):
        probe = (
            "#moj_import <renderscale:ffx_fsr1_rcas.glsl>\\n"
            "uniform sampler2D InSampler;\\n"
            "in vec2 texCoord;\\n"
            "out vec4 fragColor;\\n"
            "// VULKANBOOST_EASU_ONLY_TEST: RCAS bypassed; copy EASU output unchanged.\\n"
            "void main() { fragColor = texture(InSampler, texCoord); }\\n"
        )
        path = build_jar(
            "// ok\\nuint a = mask;\\n",
            "// AMDs Vulkan example, which declares separate texture and sampler objects.\\n"
            "vec4 FsrEasuF(vec4 c) { return c * 0.5; }\\n",
            rcas_shader=probe,
        )
        self.assertEqual(errors(path), [])

    def test_easu_only_probe_rejects_an_rcas_function_call(self):
        probe = (
            "#moj_import <renderscale:ffx_fsr1_rcas.glsl>\\n"
            "// VULKANBOOST_EASU_ONLY_TEST\\n"
            "void main() { fragColor = FsrRcasF(); }\\n"
        )
        path = build_jar(
            "// ok\\nuint a = mask;\\n",
            "// AMDs Vulkan example, which declares separate texture and sampler objects.\\n"
            "vec4 FsrEasuF(vec4 c) { return c * 0.5; }\\n",
            rcas_shader=probe,
        )
        self.assertTrue(any("must not execute RCAS" in item[3] for item in errors(path)))

    def test_apostrophe_in_code_is_not_silently_removed(self):
        with self.assertRaises(ValueError):
            chk.sanitize_comment_apostrophes("float x = value's;\n")


if __name__ == "__main__":
    unittest.main(verbosity=2)
