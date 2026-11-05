"""Tests for the final nested-JAR bundle validator."""
import io
import json
import os
import tempfile
import unittest
import zipfile
from pathlib import Path

import check_bundle


def make_nested(mod_id, version, child_jars=None, jars_override=None):
    child_jars = child_jars or {}
    nested_entries = [{"file": path} for path in child_jars]
    if jars_override is not None:
        nested_entries = jars_override
    stream = io.BytesIO()
    with zipfile.ZipFile(stream, "w") as archive:
        archive.writestr(
            "fabric.mod.json",
            json.dumps({"schemaVersion": 1, "id": mod_id, "version": version, "jars": nested_entries}),
        )
        for path, payload in child_jars.items():
            archive.writestr(path, payload)
    return stream.getvalue()


def make_bundle(mod_ids=None, extra_path=None, version=check_bundle.EXPECTED_BUNDLE_VERSION,
                nested_overrides=None, root_overrides=None, depends=None):
    mod_ids = mod_ids or sorted(check_bundle.EXPECTED_COMPONENT_IDS)
    versions = dict(check_bundle.EXPECTED_VERSIONS)
    nested_jars = []
    payloads = {}
    for index, mod_id in enumerate(mod_ids):
        component_version = versions.get(mod_id, "test-version")
        path = f"META-INF/jars/{index:02d}-{mod_id}.jar"
        nested_jars.append({"file": path})
        payloads[path] = (nested_overrides or {}).get(mod_id, make_nested(mod_id, component_version))

    if extra_path:
        path, payload = extra_path
        nested_jars.append({"file": path})
        payloads[path] = payload

    root_metadata = {
        "schemaVersion": 1,
        "id": "vulkanboost",
        "version": version,
        "jars": nested_jars,
        "depends": check_bundle.REQUIRED_DEPENDENCIES.copy() if depends is None else depends,
    }
    if root_overrides:
        root_metadata.update(root_overrides)
    stream = io.BytesIO()
    with zipfile.ZipFile(stream, "w") as archive:
        archive.writestr("fabric.mod.json", json.dumps(root_metadata))
        for class_path in check_bundle.REQUIRED_PROJECT_CLASSES:
            archive.writestr(class_path, b"test-class")
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

    def test_wrong_root_version_is_rejected(self):
        path = make_bundle(version="0.7")
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("bundle version mismatch" in error for error in errors))
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

    def test_empty_root_jars_string_is_rejected(self):
        path = make_bundle(root_overrides={"jars": ""})
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("root metadata jars must be a JSON array" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_empty_nested_jars_string_is_rejected(self):
        component = make_nested("fabric-api", check_bundle.EXPECTED_VERSIONS["fabric-api"], jars_override="")
        path = make_bundle(nested_overrides={"fabric-api": component})
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("nested metadata jars must be a JSON array" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_deep_nested_metadata_must_use_an_array(self):
        deep_archive = make_nested("deep-library", "1.0", jars_override=17)
        component = make_nested("fabric-api", check_bundle.EXPECTED_VERSIONS["fabric-api"],
                                child_jars={"META-INF/jars/deep.jar": deep_archive})
        path = make_bundle(nested_overrides={"fabric-api": component})
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("nested metadata jars must be a JSON array" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_nested_jar_recursion_limit_is_enforced(self):
        def chain(remaining):
            if remaining == 0:
                return make_nested("deep-leaf", "1.0")
            return make_nested(f"deep-level-{remaining}", "1.0",
                               child_jars={"META-INF/jars/next.jar": chain(remaining - 1)})
        component = make_nested("fabric-api", check_bundle.EXPECTED_VERSIONS["fabric-api"],
                                child_jars={"META-INF/jars/level-1.jar": chain(9)})
        path = make_bundle(nested_overrides={"fabric-api": component})
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("nested-JAR recursion limit exceeded" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_empty_dependencies_string_is_rejected(self):
        path = make_bundle(depends="")
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("root metadata depends must be a JSON object" in error for error in errors))
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
