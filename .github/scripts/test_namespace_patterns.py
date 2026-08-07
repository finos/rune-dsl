#!/usr/bin/env python3
"""Unit tests for the shared namespace-pattern matching and rune-config.yml parsing.

Run with:  python3 -m unittest test_namespace_patterns
"""
import pathlib
import unittest

# The corpus is YAML so that it can carry a comment explaining each case. PyYAML is the checkers'
# only dependency -- see requirements.txt -- and is imported rather than guarded here: a corpus test
# that quietly skips is a divergence guard that is quietly off.
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


class NamespaceConfigTest(unittest.TestCase):
    def test_reads_the_entries_as_mappings(self):
        entries = ns.namespace_config(CONFIG)
        self.assertEqual([e["namespace"] for e in entries],
                         ["com.example.locked", "com.example.generated", "com.example.open"])
        self.assertEqual(entries[0]["readOnly"], True)
        self.assertEqual(entries[1]["origin"], {"modelImport": "csv"})
        self.assertEqual(entries[2]["schemaConfig"]["configPath"], "c.json")

    def test_flow_style_reads_the_same_as_block_style(self):
        # Both are valid YAML and the Java side accepts both, so this side must too.
        block = "namespaceConfig:\n- namespace: a\n  origin:\n    modelImport: csv\n"
        flow = "namespaceConfig:\n- {namespace: a, origin: {modelImport: csv}}\n"
        self.assertEqual(ns.namespace_config(block), ns.namespace_config(flow))

    def test_empty_missing_or_unparseable_config_yields_nothing(self):
        self.assertEqual(ns.namespace_config(""), [])
        self.assertEqual(ns.namespace_config(None), [])
        self.assertEqual(ns.namespace_config("model:\n  name: X\n"), [])
        self.assertEqual(ns.namespace_config("model: [unclosed\n"), [])


if __name__ == "__main__":
    unittest.main()
