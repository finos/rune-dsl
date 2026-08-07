#!/usr/bin/env python3
"""Unit tests for the shared namespace-pattern matching and rune-config.yml parsing.

Run with:  python3 -m unittest test_namespace_patterns
"""
import pathlib
import unittest

# The corpus is YAML so that it can carry a comment explaining each case. PyYAML is the only
# dependency anything here has, it is test-only, and it is deliberately imported rather than
# guarded: a corpus test that quietly skips is a divergence guard that is quietly off.
#   pip install pyyaml
# The checkers themselves stay standard-library-only -- they run in other repositories with
# nothing but `setup-python`.
import yaml

import namespace_patterns as ns

CONFIG = """\
model:
  name: X
namespaceConfig:
- id: a
  namespace: com.example.locked
  readOnly: true
- namespace: com.example.generated
  origin:
    modelImport: csv
- id: c
  namespace: com.example.open
  schemaConfig:
    schema: c
    configPath: c.json
generators:
  namespaces:
  - com.example.*
"""


class SharedCorpusTest(unittest.TestCase):
    """The corpus is also read by RuneNamespacePatternTest (Java).

    Matching is implemented twice -- here and in rune-runtime -- and a comment saying the two
    must be kept in step is not a mechanism. Running both over the same cases is.
    """

    CORPUS = yaml.safe_load((pathlib.Path(__file__).parent / "namespace-pattern-cases.yml").read_text())

    def test_every_case(self):
        for case in self.CORPUS["cases"]:
            pattern = case["pattern"]
            for namespace in case.get("matches") or []:
                with self.subTest(pattern=pattern, namespace=namespace, expected=True):
                    self.assertTrue(ns.matches(namespace, pattern),
                                    f"'{pattern}' should match '{namespace}'")
            for namespace in case.get("does-not-match") or []:
                with self.subTest(pattern=pattern, namespace=namespace, expected=False):
                    self.assertFalse(ns.matches(namespace, pattern),
                                     f"'{pattern}' should not match '{namespace}'")


class MatchesTest(unittest.TestCase):
    def test_missing_namespace_never_matches(self):
        self.assertFalse(ns.matches(None, "foo.bar"))
        self.assertFalse(ns.matches_any(None, ["foo.bar.*"]))

    def test_no_patterns_matches_nothing(self):
        self.assertFalse(ns.matches_any("foo.bar", []))

    def test_matches_any_is_the_union(self):
        self.assertTrue(ns.matches_any("abc.def", ["foo.*", "abc.def"]))
        self.assertFalse(ns.matches_any("abc.def.sub", ["foo.*", "abc.def"]))


class NamespaceOfTest(unittest.TestCase):
    def test_reads_the_namespace_declaration(self):
        self.assertEqual(ns.namespace_of("namespace foo.bar : <\"Doc\">\n"), "foo.bar")
        self.assertEqual(ns.namespace_of("override namespace foo.bar\n"), "foo.bar")
        self.assertIsNone(ns.namespace_of("type Foo:\n"))
        self.assertIsNone(ns.namespace_of(None))


class NamespaceConfigEntriesTest(unittest.TestCase):
    def test_splits_the_section_into_entries_keeping_nested_keys(self):
        entries = ns.namespace_config_entries(CONFIG)
        self.assertEqual(len(entries), 3)
        self.assertIn("modelImport: csv", entries[1])
        self.assertIn("configPath: c.json", entries[2])

    def test_stops_at_the_next_top_level_key(self):
        # `generators` follows the section, and must not be swallowed into the last entry
        self.assertNotIn("com.example.*", ns.namespace_config_entries(CONFIG)[2])

    def test_reads_top_level_entry_values(self):
        entries = ns.namespace_config_entries(CONFIG)
        self.assertEqual(ns.entry_value(entries[0], "namespace"), "com.example.locked")
        self.assertEqual(ns.entry_value(entries[0], "readOnly"), "true")
        self.assertIsNone(ns.entry_value(entries[1], "readOnly"))

    def test_empty_or_missing_config_yields_nothing(self):
        self.assertEqual(ns.namespace_config_entries(""), [])
        self.assertEqual(ns.namespace_config_entries(None), [])
        self.assertEqual(ns.namespace_config_entries("model:\n  name: X\n"), [])


if __name__ == "__main__":
    unittest.main()
