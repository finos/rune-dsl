#!/usr/bin/env python3
"""Unit tests for the generated-namespace annotator's pure logic (no git needed).

Run with:  python3 -m unittest test_annotate_generated_namespaces
"""
import unittest
from unittest import mock

import annotate_generated_namespaces as ann
from namespace_patterns import GitError

CONFIG = """\
model:
  name: DEMO
namespaceConfig:
- namespace: demo.unavista.csv
  origin:
    modelImport: csv
- namespace: demo.locked
  readOnly: true
- namespace: demo.imported.*
  origin:
    modelImport: xsd
  schemaConfig:
    schema: demoImported
    configPath: xml-config/demo-imported.json
generators:
  namespaces:
  - demo.*
"""


class ParseGeneratedNamespacesTest(unittest.TestCase):
    def test_reads_only_the_entries_carrying_an_origin(self):
        self.assertEqual(
            ann.parse_generated_namespaces(CONFIG),
            [("demo.unavista.csv", {"modelImport": "csv"}),
             ("demo.imported.*", {"modelImport": "xsd"})],
        )

    def test_a_config_without_generated_namespaces_yields_nothing(self):
        self.assertEqual(ann.parse_generated_namespaces("model:\n  name: X\n"), [])

    def test_a_key_this_version_does_not_recognise_names_no_tool(self):
        # A config is only ever read by the version that wrote it or a newer one, and the checker
        # ships from the same repository as the DSL, so an unrecognised key is a mistake -- and is
        # ignored here exactly as the Java side ignores it.
        config = "namespaceConfig:\n- namespace: demo.future\n  origin:\n    someUnknownTool: v1\n"
        self.assertEqual(ann.parse_generated_namespaces(config), [])

    def test_an_empty_origin_says_nothing(self):
        config = "namespaceConfig:\n- namespace: demo.plain\n  origin: {}\n"
        self.assertEqual(ann.parse_generated_namespaces(config), [])

    # The shapes below all read the same way on the Java side. Each one used to be parsed
    # differently here, back when this walked the YAML by hand.
    def test_flow_style_is_read(self):
        config = "namespaceConfig:\n- namespace: demo.x\n  origin: {modelImport: csv}\n"
        self.assertEqual(ann.parse_generated_namespaces(config), [("demo.x", {"modelImport": "csv"})])

    def test_a_valueless_key_names_no_tool(self):
        config = "namespaceConfig:\n- namespace: demo.x\n  origin:\n    modelImport:\n"
        self.assertEqual(ann.parse_generated_namespaces(config), [])

    def test_a_valueless_key_does_not_swallow_the_next_line(self):
        config = ("namespaceConfig:\n- namespace: demo.x\n  origin:\n"
                  "    someTool:\n    modelImport: csv\n")
        self.assertEqual(ann.parse_generated_namespaces(config), [("demo.x", {"modelImport": "csv"})])

    def test_a_non_scalar_value_names_no_tool(self):
        config = "namespaceConfig:\n- namespace: demo.x\n  origin:\n    modelImport:\n      version: 2\n"
        self.assertEqual(ann.parse_generated_namespaces(config), [])


class WithoutDocumentationTest(unittest.TestCase):
    def test_strips_documentation_strings_and_comments(self):
        self.assertEqual(
            ann.without_documentation('    lei string (1..1) <"The LEI."> // a comment'),
            "lei string (1..1)",
        )

    def test_normalises_whitespace_so_reindentation_is_not_a_change(self):
        self.assertEqual(ann.without_documentation("  lei   string  (1..1)"), "lei string (1..1)")

    def test_a_double_slash_inside_a_string_is_not_a_comment(self):
        # A URL in a label would otherwise be truncated, so an edit to it would read as
        # documentation-only -- an all-clear over a changed column binding.
        self.assertEqual(
            ann.without_documentation('[label "http://old.example.com/a"]'),
            '[label "http://old.example.com/a"]',
        )
        self.assertNotEqual(
            ann.without_documentation('[label "http://old.example.com/a"]'),
            ann.without_documentation('[label "http://new.example.com/b"]'),
        )

    def test_an_escaped_quote_does_not_end_the_string(self):
        self.assertEqual(ann.without_documentation(r'[label "a\"b//c"] // gone'), r'[label "a\"b//c"]')


class ClassifyTest(unittest.TestCase):
    def classify(self, diff, code="M", old="f.rosetta", new="f.rosetta"):
        with mock.patch.object(ann, "git", return_value=diff):
            return ann.classify("base", old, new, code)

    def test_documentation_only_change_is_a_notice(self):
        level, description = self.classify(
            '--- a/f.rosetta\n+++ b/f.rosetta\n@@ -3 +3 @@\n'
            '-    lei string (1..1)\n'
            '+    lei string (1..1) <"The legal entity identifier.">\n'
        )
        self.assertEqual(level, "notice")
        self.assertIn("documentation", description)

    def test_purely_additive_change_is_a_notice(self):
        level, description = self.classify(
            '--- a/f.rosetta\n+++ b/f.rosetta\n@@ -3 +3,2 @@\n'
            '+    venue string (0..1)\n'
        )
        self.assertEqual(level, "notice")
        self.assertIn("added", description)

    def test_changed_declaration_is_a_warning(self):
        level, _ = self.classify(
            '--- a/f.rosetta\n+++ b/f.rosetta\n@@ -3 +3 @@\n'
            '-        [label "Trading Date Time"]\n'
            '+        [label "TradingDateTime"]\n'
        )
        self.assertEqual(level, "warning")

    def test_removed_declaration_is_a_warning(self):
        level, _ = self.classify(
            '--- a/f.rosetta\n+++ b/f.rosetta\n@@ -3 +0,0 @@\n'
            '-    venue string (0..1)\n'
        )
        self.assertEqual(level, "warning")

    def test_an_unrecognised_construct_errs_towards_a_warning(self):
        # the classifier knows nothing about `condition`; replacing one still reads as a change
        level, _ = self.classify(
            '--- a/f.rosetta\n+++ b/f.rosetta\n@@ -3 +3 @@\n'
            '-    condition Foo: quantity exists\n'
            '+    condition Foo: quantity is absent\n'
        )
        self.assertEqual(level, "warning")

    def test_deleting_the_file_is_a_warning_without_needing_a_diff(self):
        # Deleting a generated type is the most consequential edit; it must not be the one that
        # is silent because the file is no longer on disk.
        with mock.patch.object(ann, "git", side_effect=AssertionError("must not diff a deletion")):
            level, description = ann.classify("base", "f.rosetta", "f.rosetta", "D")
        self.assertEqual(level, "warning")
        self.assertIn("deleted", description)

    def test_a_rename_diffs_both_paths(self):
        # Diffing only the destination renders a rename as a wholly new file, which reads as
        # purely additive -- an active all-clear over an edit that removed declarations.
        with mock.patch.object(ann, "git", return_value="") as git:
            ann.classify("base", "old.rosetta", "new.rosetta", "R")
        self.assertEqual(git.call_args.args[-2:], ("old.rosetta", "new.rosetta"))


class UnderRootTest(unittest.TestCase):
    def test_default_root_covers_everything(self):
        self.assertTrue(ann.under_root("a/b/c.rosetta", "."))

    def test_a_root_restricts_to_its_subtree(self):
        self.assertTrue(ann.under_root("src/rosetta/a.rosetta", "src/rosetta"))
        self.assertTrue(ann.under_root("src/rosetta/a.rosetta", "src/rosetta/"))
        self.assertFalse(ann.under_root("src/test/a.rosetta", "src/rosetta"))
        # a sibling directory sharing a prefix is not under the root
        self.assertFalse(ann.under_root("src/rosetta-test/a.rosetta", "src/rosetta"))


class FindingsForTest(unittest.TestCase):
    def findings(self, changed, namespaces, classification=("warning", "changed things"), base_namespaces=None):
        base_namespaces = base_namespaces or {}
        with mock.patch.object(ann, "changed_files", return_value=changed), \
                mock.patch.object(ann, "read", side_effect=lambda path: namespaces.get(path)), \
                mock.patch.object(ann, "git_show", side_effect=lambda ref, path: base_namespaces.get(path)), \
                mock.patch.object(ann, "classify", return_value=classification):
            return ann.findings_for("base", CONFIG, ".")

    def test_reports_a_changed_generated_file_and_ignores_the_rest(self):
        findings = self.findings(
            [("M", "model/unavista-csv-type.rosetta", "model/unavista-csv-type.rosetta"),
             ("M", "model/other.rosetta", "model/other.rosetta")],
            {"model/unavista-csv-type.rosetta": "namespace demo.unavista.csv\n",
             "model/other.rosetta": "namespace demo.handwritten\n"},
        )
        self.assertEqual([(f[0], f[1], f[2], f[3]) for f in findings],
                         [("warning", "model/unavista-csv-type.rosetta", "demo.unavista.csv",
                           {"modelImport": "csv"})])

    def test_a_wildcard_entry_covers_the_namespace_itself(self):
        findings = self.findings([("M", "a.rosetta", "a.rosetta")], {"a.rosetta": "namespace demo.imported\n"})
        self.assertEqual([f[2] for f in findings], ["demo.imported"])

    def test_a_deleted_file_is_reported_from_the_base_ref(self):
        # It is not on disk to be read, so the namespace comes from the base version.
        findings = self.findings(
            [("D", "model/gone.rosetta", "model/gone.rosetta")],
            {},
            base_namespaces={"model/gone.rosetta": "namespace demo.unavista.csv\n"},
        )
        self.assertEqual([(f[1], f[2]) for f in findings], [("model/gone.rosetta", "demo.unavista.csv")])

    def test_a_rename_is_reported_and_says_so(self):
        findings = self.findings(
            [("R", "model/old.rosetta", "model/new.rosetta")],
            {"model/new.rosetta": "namespace demo.unavista.csv\n"},
        )
        self.assertEqual(len(findings), 1)
        self.assertEqual(findings[0][1], "model/new.rosetta")
        self.assertIn("renamed", findings[0][4])

    def test_a_file_moved_out_of_a_generated_namespace_is_still_reported(self):
        # The head namespace no longer matches, so selecting on it alone would let the change leave
        # without a word -- while removing declarations on the way out.
        findings = self.findings(
            [("M", "model/t.rosetta", "model/t.rosetta")],
            {"model/t.rosetta": "namespace demo.handwritten\n"},
            base_namespaces={"model/t.rosetta": "namespace demo.unavista.csv\n"},
        )
        self.assertEqual(len(findings), 1)
        self.assertEqual(findings[0][0], "warning")
        self.assertIn("moved the file from 'demo.unavista.csv' to 'demo.handwritten'", findings[0][4])

    def test_warnings_are_reported_before_notices(self):
        levels = {"a.rosetta": ("notice", "added"), "b.rosetta": ("warning", "changed")}
        with mock.patch.object(ann, "changed_files",
                               return_value=[("M", "a.rosetta", "a.rosetta"), ("M", "b.rosetta", "b.rosetta")]), \
                mock.patch.object(ann, "read", return_value="namespace demo.unavista.csv\n"), \
                mock.patch.object(ann, "classify", side_effect=lambda base, old, new, code: levels[new]):
            findings = ann.findings_for("base", CONFIG, ".")
        self.assertEqual([f[1] for f in findings], ["b.rosetta", "a.rosetta"])


class AnnotationBudgetTest(unittest.TestCase):
    def budget(self, warnings, notices):
        findings = [("warning", f"w{i}.rosetta", "n", {}, "d") for i in range(warnings)] \
                 + [("notice", f"n{i}.rosetta", "n", {}, "d") for i in range(notices)]
        shown = ann.within_annotation_budget(findings)
        return sum(1 for f in shown if f[0] == "warning"), sum(1 for f in shown if f[0] == "notice")

    def test_the_budget_is_per_level_so_warnings_do_not_crowd_out_notices(self):
        # GitHub's limit is per level, so twelve warnings must not suppress three notices.
        self.assertEqual(self.budget(12, 3), (10, 3))

    def test_each_level_is_capped(self):
        self.assertEqual(self.budget(14, 14), (10, 10))
        self.assertEqual(self.budget(2, 2), (2, 2))

    def test_the_count_that_did_not_fit_is_stated(self):
        findings = [("warning", f"w{i}.rosetta", "n", {}, "d") for i in range(12)]
        with mock.patch("builtins.print") as printed:
            ann.annotate(findings, "url")
        notices = [c.args[0] for c in printed.call_args_list if c.args[0].startswith("::notice::")]
        self.assertEqual(len(notices), 1)
        self.assertIn("2 further", notices[0])


class MainTest(unittest.TestCase):
    def test_a_git_failure_is_reported_rather_than_read_as_nothing_changed(self):
        # Silence would be indistinguishable from "no generated namespace was touched".
        with mock.patch.object(ann, "read", return_value=CONFIG), \
                mock.patch.object(ann, "findings_for", side_effect=GitError("bad revision")), \
                mock.patch("sys.argv", ["prog", "--base", "nope", "--config", "rune-config.yml"]), \
                mock.patch("builtins.print") as printed:
            self.assertEqual(ann.main(), 0)
        self.assertIn("::warning::", printed.call_args.args[0])
        self.assertIn("could not run", printed.call_args.args[0])


if __name__ == "__main__":
    unittest.main()
