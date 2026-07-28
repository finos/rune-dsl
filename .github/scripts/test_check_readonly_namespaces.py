#!/usr/bin/env python3
"""Minimal unit tests for the read-only-namespace checker's pure logic (no git needed).

Run with:  python3 -m unittest test_check_readonly_namespaces
"""
import unittest

import check_readonly_namespaces as chk

CONFIG = """\
model:
  name: X
namespaceConfig:
- id: a
  namespace: com.example.locked
  readOnly: true
- namespace: com.example.noid
  readOnly: true
- id: c
  namespace: com.example.open
  schemaConfig:
    schema: c
    configPath: c.json
"""


class ParseReadonlyNamespacesTest(unittest.TestCase):
    def test_extracts_readonly_entries_including_id_less_ones(self):
        # both the entry with an id and the one without are picked up; the non-read-only one is not
        self.assertEqual(
            chk.parse_readonly_namespaces(CONFIG),
            ["com.example.locked", "com.example.noid"],
        )

    def test_empty_or_missing_config_yields_nothing(self):
        self.assertEqual(chk.parse_readonly_namespaces(""), [])
        self.assertEqual(chk.parse_readonly_namespaces(None), [])


class ClassifyTest(unittest.TestCase):
    LOCKED = ["ns.locked"]
    EMPTY = []

    def classify(self, code, ns_before, ns_after, base, head):
        return chk.classify(code, "f.rosetta", "f.rosetta", ns_before, ns_after, base, head)

    # Case 1 -- adding files: judged against the base config.
    def test_add_is_allowed_when_namespace_becomes_readonly_only_in_head(self):
        self.assertIsNone(self.classify("A", None, "ns.locked", self.EMPTY, self.LOCKED))

    def test_add_is_blocked_when_namespace_already_readonly_in_base(self):
        self.assertIsNotNone(self.classify("A", None, "ns.locked", self.LOCKED, self.LOCKED))

    # Case 2 -- deleting files: judged against the head config.
    def test_delete_is_allowed_when_namespace_no_longer_readonly_in_head(self):
        self.assertIsNone(self.classify("D", "ns.locked", None, self.LOCKED, self.EMPTY))

    def test_delete_is_blocked_when_namespace_still_readonly_in_head(self):
        self.assertIsNotNone(self.classify("D", "ns.locked", None, self.LOCKED, self.LOCKED))

    # Case 3 -- modifying files: blocked only when read-only in both base and head.
    def test_modify_is_blocked_when_readonly_in_both(self):
        self.assertIsNotNone(self.classify("M", "ns.locked", "ns.locked", self.LOCKED, self.LOCKED))

    def test_modify_is_allowed_when_readonly_only_in_head(self):
        self.assertIsNone(self.classify("M", "ns.locked", "ns.locked", self.EMPTY, self.LOCKED))

    def test_modify_is_allowed_when_readonly_only_in_base(self):
        self.assertIsNone(self.classify("M", "ns.locked", "ns.locked", self.LOCKED, self.EMPTY))

    def test_modify_is_allowed_when_not_readonly(self):
        self.assertIsNone(self.classify("M", "ns.open", "ns.open", self.EMPTY, self.EMPTY))

    # Renames between namespaces reuse the entry (base) and leave (head) checks.
    def test_rename_into_base_readonly_namespace_is_blocked(self):
        self.assertIsNotNone(self.classify("R", "ns.open", "ns.locked", self.LOCKED, self.LOCKED))

    def test_rename_out_of_head_readonly_namespace_is_blocked(self):
        self.assertIsNotNone(self.classify("R", "ns.locked", "ns.open", self.LOCKED, self.LOCKED))


if __name__ == "__main__":
    unittest.main()
