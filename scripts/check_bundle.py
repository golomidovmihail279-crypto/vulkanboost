#!/usr/bin/env python3
"""Validate the distributable VulkanBoost nested-JAR bundle without renderer-specific assumptions.

Usage:
    python3 scripts/check_bundle.py build/libs/vulkanboost-0.5.jar
"""
from __future__ import annotations

import io
import json
import sys
import zipfile
from pathlib import Path
from typing import Any

EXPECTED_COMPONENT_IDS = {
    "fabric-api",
    "vulkanmod",
    "lithium",
    "ferritecore",
    "alternate-current",
    "badoptimizations",
    "modernfix",
    "scalablelux",
    "zfastnoise",
    "krypton",
    "c2me",
    "bura",
    "epitaph-optimizer",
    "dynamic_fps",
    "fastquit",
    "smart_particles",
    "cloth-config",
    "betterhurtcam",
    "ukulib",
}
EXPECTED_VERSIONS = {
    "fabric-api": "0.140.2+1.21.11",
    "vulkanmod": "0.6.8+1.21.11",
    "betterhurtcam": "1.12.0+mc1.21.11",
    "ukulib": "1.10.2+1.21.11",
}
REQUIRED_DEPENDENCIES = {
    "fabricloader": ">=0.19.3",
    "minecraft": "1.21.11",
    "vulkanmod": ">=0.6.8",
    "fabric-api": "*",
    "betterhurtcam": ">=1.12.0",
    "ukulib": "^1.10.0",
}
FORBIDDEN_MOD_IDS = {"renderscale"}
FORBIDDEN_RESOURCE_MARKERS = (
    "assets/renderscale/shaders/",
    "ffx_fsr1_easu.glsl",
    "ffx_fsr1_rcas.glsl",
)


def load_metadata(archive: zipfile.ZipFile, label: str) -> dict[str, Any]:
    try:
        raw = archive.read("fabric.mod.json")
        value = json.loads(raw)
    except KeyError as exc:
        raise ValueError(f"{label}: missing fabric.mod.json") from exc
    except (json.JSONDecodeError, UnicodeDecodeError) as exc:
        raise ValueError(f"{label}: invalid fabric.mod.json: {exc}") from exc
    if not isinstance(value, dict):
        raise ValueError(f"{label}: fabric.mod.json must be an object")
    return value


def inspect_nested_jar(data: bytes, label: str, errors: list[str], depth: int = 0) -> None:
    if depth > 8:
        errors.append(f"{label}: nested-JAR recursion limit exceeded")
        return
    try:
        with zipfile.ZipFile(io.BytesIO(data)) as nested:
            names = set(nested.namelist())
            try:
                metadata = load_metadata(nested, label)
            except ValueError as exc:
                errors.append(str(exc))
                return

            mod_id = metadata.get("id")
            if mod_id in FORBIDDEN_MOD_IDS:
                errors.append(f"{label}: removed mod id is still bundled: {mod_id}")

            for name in names:
                lowered = name.lower()
                if any(marker in lowered for marker in FORBIDDEN_RESOURCE_MARKERS):
                    errors.append(f"{label}: legacy FSR/RenderScale resource remains: {name}")

            for child in metadata.get("jars", []):
                child_path = child.get("file") if isinstance(child, dict) else None
                if not child_path or child_path not in names:
                    errors.append(f"{label}: missing nested JAR entry {child_path!r}")
                    continue
                inspect_nested_jar(nested.read(child_path), f"{label}!/{child_path}", errors, depth + 1)
    except (zipfile.BadZipFile, OSError) as exc:
        errors.append(f"{label}: invalid nested JAR: {exc}")


def check_bundle(path: str | Path) -> list[str]:
    errors: list[str] = []
    try:
        with zipfile.ZipFile(path) as archive:
            names = set(archive.namelist())
            try:
                metadata = load_metadata(archive, str(path))
            except ValueError as exc:
                return [str(exc)]

            if metadata.get("id") != "vulkanboost":
                errors.append("outer metadata id must be 'vulkanboost'")

            dependencies = metadata.get("depends", {})
            for dependency, expected in REQUIRED_DEPENDENCIES.items():
                actual = dependencies.get(dependency)
                if actual != expected:
                    errors.append(
                        f"root dependency {dependency!r}: expected {expected!r}, found {actual!r}"
                    )
            if "renderscale" in dependencies:
                errors.append("root metadata still declares RenderScale")

            if metadata.get("mixins"):
                errors.append("obsolete root mixin registration remains")
            if "vulkanboost.mixins.json" in names:
                errors.append("obsolete VulkanBoost mixin config remains")

            runtime_classes = sorted(
                name for name in names
                if name.startswith("dev/vulkanboost/") and name.endswith(".class")
            )
            if runtime_classes:
                errors.append(f"obsolete custom rendering runtime classes remain: {runtime_classes}")

            nested_entries = metadata.get("jars", [])
            nested_ids: list[str] = []
            nested_versions: dict[str, str] = {}
            seen_paths: set[str] = set()

            for item in nested_entries:
                nested_path = item.get("file") if isinstance(item, dict) else None
                if not nested_path:
                    errors.append("root metadata has a nested-JAR record without a file path")
                    continue
                if nested_path in seen_paths:
                    errors.append(f"duplicate nested-JAR path: {nested_path}")
                    continue
                seen_paths.add(nested_path)
                if nested_path not in names:
                    errors.append(f"nested JAR listed in metadata is missing: {nested_path}")
                    continue

                data = archive.read(nested_path)
                try:
                    with zipfile.ZipFile(io.BytesIO(data)) as child:
                        child_metadata = load_metadata(child, nested_path)
                        mod_id = child_metadata.get("id")
                        version = child_metadata.get("version")
                        if not isinstance(mod_id, str) or not mod_id:
                            errors.append(f"{nested_path}: missing or invalid mod id")
                            continue
                        nested_ids.append(mod_id)
                        nested_versions[mod_id] = str(version)
                        if mod_id in FORBIDDEN_MOD_IDS:
                            errors.append(f"removed mod id is still bundled: {mod_id}")
                        for resource_path in child.namelist():
                            lowered = resource_path.lower()
                            if any(marker in lowered for marker in FORBIDDEN_RESOURCE_MARKERS):
                                errors.append(
                                    f"{nested_path}: legacy FSR/RenderScale resource remains: {resource_path}"
                                )
                        for grandchild in child_metadata.get("jars", []):
                            grandchild_path = grandchild.get("file") if isinstance(grandchild, dict) else None
                            if not grandchild_path or grandchild_path not in child.namelist():
                                errors.append(
                                    f"{nested_path}: missing nested JAR entry {grandchild_path!r}"
                                )
                                continue
                            inspect_nested_jar(
                                child.read(grandchild_path),
                                f"{nested_path}!/{grandchild_path}",
                                errors,
                                depth=1,
                            )
                except (zipfile.BadZipFile, ValueError, OSError) as exc:
                    errors.append(f"{nested_path}: invalid nested JAR/metadata: {exc}")

            if len(nested_entries) != len(EXPECTED_COMPONENT_IDS):
                errors.append(
                    f"expected {len(EXPECTED_COMPONENT_IDS)} top-level components, found {len(nested_entries)}"
                )

            duplicate_ids = sorted({item for item in nested_ids if nested_ids.count(item) > 1})
            if duplicate_ids:
                errors.append(f"duplicate top-level mod ids: {duplicate_ids}")

            missing = sorted(EXPECTED_COMPONENT_IDS - set(nested_ids))
            unexpected = sorted(set(nested_ids) - EXPECTED_COMPONENT_IDS)
            if missing:
                errors.append(f"required components missing: {missing}")
            if unexpected:
                errors.append(f"unexpected components present: {unexpected}")

            for mod_id, expected in EXPECTED_VERSIONS.items():
                actual = nested_versions.get(mod_id)
                if actual != expected:
                    errors.append(
                        f"{mod_id} version mismatch: expected {expected!r}, found {actual!r}"
                    )

            for name in names:
                lowered = name.lower()
                if any(marker in lowered for marker in FORBIDDEN_RESOURCE_MARKERS):
                    errors.append(f"legacy FSR/RenderScale resource remains in outer JAR: {name}")

    except (zipfile.BadZipFile, OSError) as exc:
        return [f"{path}: cannot read bundle JAR: {exc}"]

    return errors


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__.strip(), file=sys.stderr)
        return 2

    findings = check_bundle(sys.argv[1])
    for finding in findings:
        print("ERROR:", finding)
    if findings:
        print(f"Bundle validation failed: {len(findings)} error(s).")
        return 1

    print(
        "Bundle validation passed: 19 unique top-level components, pinned core versions, "
        "valid nested JAR metadata, and no legacy RenderScale/FSR runtime resources."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
