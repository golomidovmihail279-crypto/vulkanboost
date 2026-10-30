"""Tests for the final nested-JAR bundle validator."""
import io
import json
import os
import tempfile
import unittest
import zipfile
from pathlib import Path

import check_bundle


def make_nested(mod_id, version):
    stream = io.BytesIO()
    with zipfile.ZipFile(stream, "w") as archive:
        archive.writestr(
            "fabric.mod.json",
            json.dumps({"schemaVersion": 1, "id": mod_id, "version": version, "jars": []}),
        )
    return stream.getvalue()


def make_bundle(mod_ids=None, extra_path=None):
    mod_ids = mod_ids or sorted(check_bundle.EXPECTED_COMPONENT_IDS)
    versions = dict(check_bundle.EXPECTED_VERSIONS)
    nested_jars = []
    payloads = {}
    for index, mod_id in enumerate(mod_ids):
        version = versions.get(mod_id, "test-version")
        path = f"META-INF/jars/{index:02d}-{mod_id}.jar"
        nested_jars.append({"file": path})
        payloads[path] = make_nested(mod_id, version)

    if extra_path:
        path, payload = extra_path
        nested_jars.append({"file": path})
        payloads[path] = payload

    root_metadata = {
        "schemaVersion": 1,
        "id": "vulkanboost",
        "version": "0.7",
        "jars": nested_jars,
        "depends": check_bundle.REQUIRED_DEPENDENCIES.copy(),
    }
    stream = io.BytesIO()
    with zipfile.ZipFile(stream, "w") as archive:
        archive.writestr("fabric.mod.json", json.dumps(root_metadata))
        for path, payload in payloads.items():
            archive.writestr(path, payload)
    handle, path = tempfile.mkstemp(suffix=".jar")
    os.close(handle)
    Path(path).write_bytes(stream.getvalue())
    return path


class BundleCheckerTests(unittest.TestCase):
    def test_clean_25_component_bundle_passes(self):
        path = make_bundle()
        try:
            self.assertEqual(check_bundle.check_bundle(path), [])
        finally:
            Path(path).unlink(missing_ok=True)

    def test_unexpected_component_is_rejected(self):
        ids = sorted(check_bundle.EXPECTED_COMPONENT_IDS)
        ids[-1] = "unexpected-mod"
        path = make_bundle(ids)
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("unexpected components present" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_duplicate_top_level_mod_id_is_rejected(self):
        ids = sorted(check_bundle.EXPECTED_COMPONENT_IDS)
        ids[-1] = ids[0]
        path = make_bundle(ids)
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("duplicate top-level mod ids" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_invalid_nested_archive_is_rejected(self):
        path = make_bundle(extra_path=("META-INF/jars/extra.jar", b"not-a-zip"))
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("invalid nested JAR/metadata" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_source_tree_allows_vulkanboost_client_sources(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "src" / "main" / "java" / "dev" / "vulkanboost" / "client" / "VulkanBoostClient.java"
            source.parent.mkdir(parents=True)
            source.write_text("class VulkanBoostClient {}", encoding="utf-8")
            self.assertEqual(check_bundle.check_source_tree(root), [])

    def test_source_tree_rejects_java_outside_vulkanboost_namespace(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "src" / "main" / "java" / "example" / "CustomRenderer.java"
            source.parent.mkdir(parents=True)
            source.write_text("class CustomRenderer {}", encoding="utf-8")
            errors = check_bundle.check_source_tree(root)
            self.assertTrue(any("unexpected project-owned runtime file" in error for error in errors))

    def test_source_tree_still_rejects_custom_shaders(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            shader = root / "src" / "main" / "resources" / "assets" / "vulkanboost" / "shaders" / "custom.fsh"
            shader.parent.mkdir(parents=True)
            shader.write_text("// no project-owned renderer yet", encoding="utf-8")
            errors = check_bundle.check_source_tree(root)
            self.assertTrue(any("unexpected project-owned runtime file" in error for error in errors))


if __name__ == "__main__":
    unittest.main(verbosity=2)
