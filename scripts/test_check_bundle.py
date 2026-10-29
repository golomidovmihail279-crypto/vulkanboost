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


def make_bundle(mod_ids=None, extra_path=None, root_overrides=None):
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
        "version": "0.6",
        "jars": nested_jars,
        "depends": check_bundle.REQUIRED_DEPENDENCIES.copy(),
    }
    if root_overrides:
        root_metadata.update(root_overrides)

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

    def test_non_array_jars_metadata_is_reported_not_crashed(self):
        path = make_bundle(root_overrides={"jars": "not-an-array"})
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("'jars' must be an array" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_non_object_depends_metadata_is_reported_not_crashed(self):
        path = make_bundle(root_overrides={"depends": ["not", "an", "object"]})
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("'depends' must be an object" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_unsafe_nested_jar_path_is_rejected(self):
        path = make_bundle(root_overrides={"jars": [{"file": "../outside.jar"}]})
        try:
            errors = check_bundle.check_bundle(path)
            self.assertTrue(any("unsafe nested-JAR path" in error for error in errors))
        finally:
            Path(path).unlink(missing_ok=True)

    def test_source_tree_rejects_custom_runtime_files(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "src" / "main" / "java" / "example" / "CustomRenderer.java"
            source.parent.mkdir(parents=True)
            source.write_text("class CustomRenderer {}", encoding="utf-8")
            errors = check_bundle.check_source_tree(root)
            self.assertTrue(any("unexpected project-owned runtime file" in error for error in errors))

    def test_source_tree_rejects_non_object_metadata(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            metadata = root / "src" / "main" / "resources" / "fabric.mod.json"
            metadata.parent.mkdir(parents=True)
            metadata.write_text("[]", encoding="utf-8")
            errors = check_bundle.check_source_tree(root)
            self.assertTrue(any("metadata must be an object" in error for error in errors))


    def test_internal_dependency_minimum_version_mismatch_is_rejected(self):
        self.assertFalse(check_bundle.predicate_matches("0.140.2+1.21.11", ">=0.141.2"))
        self.assertTrue(check_bundle.predicate_matches("0.141.2+1.21.11", ">=0.141.2"))

    def test_internal_dependency_caret_and_tilde_predicates(self):
        self.assertTrue(check_bundle.predicate_matches("1.10.2+1.21.11", "^1.10.0"))
        self.assertFalse(check_bundle.predicate_matches("2.0.0", "^1.10.0"))
        self.assertTrue(check_bundle.predicate_matches("1.10.2", "~1.10.0"))
        self.assertFalse(check_bundle.predicate_matches("1.11.0", "~1.10.0"))

    def test_internal_dependency_space_separated_range(self):
        self.assertTrue(check_bundle.predicate_matches("1.21.11", ">=1.21.11- <1.21.12-"))
        self.assertFalse(check_bundle.predicate_matches("1.21.12", ">=1.21.11- <1.21.12-"))


    def test_prerelease_predicates_match_release_versions(self):
        self.assertTrue(check_bundle.predicate_matches("1.21.11", ">=1.21.9-beta.1"))
        self.assertTrue(check_bundle.predicate_matches("1.21.11", ">=1.21.6-rc.1"))
        self.assertTrue(check_bundle.predicate_matches("1.21.11", ">1.19-alpha.22.11.a"))
        self.assertFalse(check_bundle.predicate_matches("1.21.8", ">=1.21.9-beta.1"))

    def test_minecraft_wildcard_predicate(self):
        self.assertTrue(check_bundle.predicate_matches("1.21.11", "1.21.x"))
        self.assertFalse(check_bundle.predicate_matches("1.22.0", "1.21.x"))
        self.assertFalse(check_bundle.predicate_matches("1.20.6", "1.21.x"))

    def test_prerelease_is_lower_than_corresponding_release(self):
        self.assertLess(check_bundle.compare_full_versions("1.21.11-beta.1", "1.21.11"), 0)
        self.assertGreater(check_bundle.compare_full_versions("1.21.11", "1.21.11-rc.1"), 0)

    def test_unsupported_internal_predicate_is_not_silently_accepted(self):
        self.assertIsNone(check_bundle.predicate_matches("1.2.3", "latest-stable"))


    def test_nested_duplicates_choose_highest_compatible_version(self):
        root = ("vulkanboost", {"id": "vulkanboost", "depends": {"shared-lib": ">=1.0 <2.0"}})
        candidates = {
            "shared-lib": [
                {"version": "2.0.0", "metadata": {"id": "shared-lib", "version": "2.0.0", "depends": {}}, "origin": "newer.jar"},
                {"version": "1.5.0", "metadata": {"id": "shared-lib", "version": "1.5.0", "depends": {}}, "origin": "compatible.jar"},
            ]
        }
        errors = []
        selected = check_bundle.resolve_and_validate_dependencies([root], candidates, errors)
        self.assertEqual(errors, [])
        self.assertEqual(selected["shared-lib"]["version"], "1.5.0")

    def test_nested_dependency_conflict_is_reported_when_no_candidate_matches(self):
        root = ("vulkanboost", {"id": "vulkanboost", "depends": {"shared-lib": "<1.0.0"}})
        candidates = {
            "shared-lib": [
                {"version": "1.2.0", "metadata": {"id": "shared-lib", "version": "1.2.0", "depends": {}}, "origin": "shared.jar"},
            ]
        }
        errors = []
        check_bundle.resolve_and_validate_dependencies([root], candidates, errors)
        self.assertTrue(any("no compatible bundled candidate" in error for error in errors))

    def test_system_java_dependency_uses_target_java_21(self):
        root_ok = ("vulkanboost", {"id": "vulkanboost", "depends": {"java": ">=21"}})
        errors = []
        check_bundle.resolve_and_validate_dependencies([root_ok], {}, errors)
        self.assertEqual(errors, [])

        root_bad = ("native-module", {"id": "native-module", "depends": {"java": ">=22"}})
        errors = []
        check_bundle.resolve_and_validate_dependencies([root_bad], {}, errors)
        self.assertTrue(any("target provides '21'" in error for error in errors))

    def test_unused_nested_candidate_is_not_forced_into_active_set(self):
        root = ("vulkanboost", {"id": "vulkanboost", "depends": {}})
        candidates = {
            "optional-native": [
                {"version": "1.0.0", "metadata": {"id": "optional-native", "version": "1.0.0", "depends": {"java": ">=22"}}, "origin": "optional-native.jar"},
            ]
        }
        errors = []
        warnings = []
        selected = check_bundle.resolve_and_validate_dependencies([root], candidates, errors, warnings)
        self.assertEqual(errors, [])
        self.assertNotIn("optional-native", selected)
        self.assertTrue(any("requires java" in warning for warning in warnings))


if __name__ == "__main__":
    unittest.main(verbosity=2)
