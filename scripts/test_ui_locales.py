import json
import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
LANG_DIR = ROOT / "src" / "main" / "resources" / "assets" / "vulkanboost" / "lang"
JAVA_DIR = ROOT / "src" / "main" / "java" / "dev" / "vulkanboost" / "client"
REGISTRY_FILE = ROOT / "src" / "main" / "java" / "dev" / "vulkanboost" / "optimization" / "OptimizationRegistry.java"
import sys
sys.path.insert(0, str(ROOT / "scripts"))
import check_bundle


def reject_duplicate_keys(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate translation key: {key}")
        result[key] = value
    return result


class UiLocaleTests(unittest.TestCase):
    def load_locale(self, filename):
        path = LANG_DIR / filename
        with path.open("r", encoding="utf-8") as stream:
            return json.load(stream, object_pairs_hook=reject_duplicate_keys)

    def test_english_and_russian_keys_match(self):
        english = self.load_locale("en_us.json")
        russian = self.load_locale("ru_ru.json")
        self.assertEqual(
            set(english),
            set(russian),
            "en_us.json and ru_ru.json must contain exactly the same keys",
        )

    def test_registry_matches_the_complete_bundle_manifest(self):
        registry = REGISTRY_FILE.read_text(encoding="utf-8")
        registered_ids = set(re.findall(r'new Entry\(\s*"([^"]+)"', registry))
        self.assertEqual(check_bundle.EXPECTED_COMPONENT_IDS, registered_ids)

    def test_release_version_matches_gradle_and_validator(self):
        properties = (ROOT / "gradle.properties").read_text(encoding="utf-8")
        match = re.search(r"^mod_version=(\S+)$", properties, re.MULTILINE)
        self.assertIsNotNone(match)
        self.assertEqual(check_bundle.EXPECTED_BUNDLE_VERSION, match.group(1))

    def test_literal_screen_translation_keys_exist(self):
        english = self.load_locale("en_us.json")
        used_keys = set()
        for source in JAVA_DIR.glob("*.java"):
            text = source.read_text(encoding="utf-8")
            used_keys.update(re.findall(r'"(screen\.vulkanboost\.[A-Za-z0-9_.]+)"', text))

        missing = sorted(used_keys - set(english))
        self.assertEqual([], missing, f"Missing UI translation keys: {missing}")


if __name__ == "__main__":
    unittest.main()
