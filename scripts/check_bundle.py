#!/usr/bin/env python3
"""Validate the distributable VulkanBoost bundle.

Usage: python3 scripts/check_bundle.py build/libs/vulkanboost-0.6.jar
"""
from __future__ import annotations

import io
import json
import sys
import zipfile
from pathlib import Path, PurePosixPath
from typing import Any

EXPECTED_COMPONENT_IDS = {
    "fabric-api", "vulkanmod", "lithium", "ferritecore", "alternate-current",
    "badoptimizations", "modernfix", "scalablelux", "zfastnoise", "krypton",
    "c2me", "bura", "epitaph-optimizer", "dynamic_fps", "fastquit",
    "smart_particles", "cloth-config", "betterhurtcam", "ukulib",
    "structure_layout_optimizer", "resourcefulconfig", "cullleaves", "midnightlib",
    "puzzleslib", "forgeconfigapiport",
}
EXPECTED_VERSIONS = {
    "fabric-api": "0.140.2+1.21.11",
    "vulkanmod": "0.6.8+1.21.11",
    "betterhurtcam": "1.12.0+mc1.21.11",
    "ukulib": "1.10.2+1.21.11",
    "structure_layout_optimizer": "1.1.4+1.21.11-fabric",
    "resourcefulconfig": "3.11.3",
    "cullleaves": "4.1.1.1",
    "midnightlib": "1.9.2",
    "puzzleslib": "21.11.11",
    "forgeconfigapiport": "21.11.0",
}
REQUIRED_DEPENDENCIES = {
    "fabricloader": ">=0.19.3", "minecraft": "1.21.11",
    "vulkanmod": ">=0.6.8", "fabric-api": "*",
    "betterhurtcam": ">=1.12.0", "ukulib": "^1.10.0",
}


def load_metadata(archive: zipfile.ZipFile, label: str) -> dict[str, Any]:
    try:
        value = json.loads(archive.read("fabric.mod.json"))
    except KeyError as exc:
        raise ValueError(f"{label}: missing fabric.mod.json") from exc
    except (json.JSONDecodeError, UnicodeDecodeError) as exc:
        raise ValueError(f"{label}: invalid fabric.mod.json: {exc}") from exc
    if not isinstance(value, dict):
        raise ValueError(f"{label}: fabric.mod.json must be an object")
    return value


def nested_paths(metadata: dict[str, Any], label: str, errors: list[str]) -> list[str]:
    """Return safe nested-JAR paths, reporting malformed Fabric metadata."""
    entries = metadata.get("jars", [])
    if not isinstance(entries, list):
        errors.append(f"{label}: 'jars' must be an array")
        return []

    paths: list[str] = []
    for index, item in enumerate(entries):
        path = item.get("file") if isinstance(item, dict) else None
        if not isinstance(path, str) or not path:
            errors.append(f"{label}: nested-JAR record {index} has no valid file path")
            continue
        normalized = PurePosixPath(path)
        if normalized.is_absolute() or ".." in normalized.parts or "\\" in path:
            errors.append(f"{label}: unsafe nested-JAR path {path!r}")
            continue
        paths.append(path)
    return paths


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
            for child_path in nested_paths(metadata, label, errors):
                if child_path not in names:
                    errors.append(f"{label}: missing nested JAR entry {child_path!r}")
                    continue
                inspect_nested_jar(
                    nested.read(child_path), f"{label}!/{child_path}", errors, depth + 1
                )
    except (zipfile.BadZipFile, OSError) as exc:
        errors.append(f"{label}: invalid nested JAR: {exc}")


def check_source_tree(root: str | Path = ".") -> list[str]:
    """Ensure the base bundle declares no custom runtime hooks."""
    root = Path(root)
    errors: list[str] = []
    metadata_path = root / "src" / "main" / "resources" / "fabric.mod.json"
    if metadata_path.is_file():
        try:
            metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, UnicodeDecodeError, OSError) as exc:
            return [f"fabric.mod.json: invalid metadata: {exc}"]
        if not isinstance(metadata, dict):
            return ["fabric.mod.json: metadata must be an object"]
        if metadata.get("mixins"):
            errors.append("custom mixin registration is not expected in the bundle base")

    source_root = root / "src" / "main"
    if source_root.exists():
        for path in source_root.rglob("*"):
            if not path.is_file():
                continue
            if path.suffix.lower() in {".java", ".class", ".glsl", ".fsh", ".vsh"}:
                errors.append(
                    f"unexpected project-owned runtime file: {path.relative_to(root).as_posix()}"
                )
            if path.name.endswith(".mixins.json"):
                errors.append(
                    f"unexpected mixin configuration: {path.relative_to(root).as_posix()}"
                )
    return errors


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
            if not isinstance(dependencies, dict):
                errors.append("root metadata 'depends' must be an object")
                dependencies = {}
            for dependency, expected in REQUIRED_DEPENDENCIES.items():
                actual = dependencies.get(dependency)
                if actual != expected:
                    errors.append(
                        f"root dependency {dependency!r}: expected {expected!r}, found {actual!r}"
                    )

            if metadata.get("mixins"):
                errors.append("unexpected root mixin registration remains")
            if "vulkanboost.mixins.json" in names:
                errors.append("unexpected mixin config remains")
            custom_classes = sorted(
                name for name in names
                if name.startswith("dev/vulkanboost/") and name.endswith(".class")
            )
            if custom_classes:
                errors.append(
                    f"unexpected project-owned runtime classes remain: {custom_classes}"
                )

            nested_entries = metadata.get("jars", [])
            nested_paths_list = nested_paths(metadata, str(path), errors)
            nested_ids: list[str] = []
            nested_versions: dict[str, str] = {}
            seen_paths: set[str] = set()

            for nested_path in nested_paths_list:
                if nested_path in seen_paths:
                    errors.append(f"duplicate nested-JAR path: {nested_path}")
                    continue
                seen_paths.add(nested_path)
                if nested_path not in names:
                    errors.append(f"nested JAR listed in metadata is missing: {nested_path}")
                    continue

                try:
                    with zipfile.ZipFile(io.BytesIO(archive.read(nested_path))) as child:
                        child_metadata = load_metadata(child, nested_path)
                        mod_id = child_metadata.get("id")
                        version = child_metadata.get("version")
                        if not isinstance(mod_id, str) or not mod_id:
                            errors.append(f"{nested_path}: missing or invalid mod id")
                            continue
                        if not isinstance(version, (str, int, float)):
                            errors.append(f"{nested_path}: missing or invalid mod version")
                            continue

                        nested_ids.append(mod_id)
                        nested_versions[mod_id] = str(version)
                        child_names = set(child.namelist())
                        for child_path in nested_paths(child_metadata, nested_path, errors):
                            if child_path not in child_names:
                                errors.append(
                                    f"{nested_path}: missing nested JAR entry {child_path!r}"
                                )
                                continue
                            inspect_nested_jar(
                                child.read(child_path),
                                f"{nested_path}!/{child_path}",
                                errors,
                                depth=1,
                            )
                except (zipfile.BadZipFile, ValueError, OSError) as exc:
                    errors.append(f"{nested_path}: invalid nested JAR/metadata: {exc}")

            if isinstance(nested_entries, list) and len(nested_entries) != len(EXPECTED_COMPONENT_IDS):
                errors.append(
                    f"expected {len(EXPECTED_COMPONENT_IDS)} top-level components, "
                    f"found {len(nested_entries)}"
                )
            duplicates = sorted({item for item in nested_ids if nested_ids.count(item) > 1})
            if duplicates:
                errors.append(f"duplicate top-level mod ids: {duplicates}")
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
    except (zipfile.BadZipFile, OSError) as exc:
        return [f"{path}: cannot read bundle JAR: {exc}"]
    return errors


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__.strip(), file=sys.stderr)
        return 2
    findings = check_source_tree(Path.cwd()) + check_bundle(sys.argv[1])
    for finding in findings:
        print("ERROR:", finding)
    if findings:
        print(f"Validation failed: {len(findings)} error(s).")
        return 1
    print(
        "Validation passed: 25 unique top-level components, pinned component versions, "
        "valid nested-JAR metadata, and no unexpected project-owned runtime hooks."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
