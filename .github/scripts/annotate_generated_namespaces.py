#!/usr/bin/env python3
"""Annotate a pull request with the generated Rune namespaces it modifies.

A namespace is *generated* when its ``namespaceConfig`` entry in rune-config.yml
carries an ``origin`` marker naming the tool that produced it, e.g.::

    namespaceConfig:
    - namespace: demo.unavista.csv
      origin:
        modelImport: csv

Unlike read-only namespaces, generated namespaces are meant to be refined by hand --
completing enumerations, narrowing type aliases, adding documentation. So this check
never fails on a model edit. It reports, so that the edit is visible in review. A
blocking check would attract a bypass label on nearly every pull request and the signal
would decay into a ritual.

Classification is deliberately **fail-safe** and carries almost no knowledge of Rune
syntax, so that it stays correct as the language evolves. Rather than recognising a list
of risky constructs -- which would silently under-report any construct it had not been
taught -- it recognises only what is demonstrably harmless and treats everything else as
worth a reviewer's attention:

* a change that leaves the same set of declarations is a **notice**, whether it moved
  documentation, comments or only the order;
* a change that only adds declarations is a **notice**, since data that parsed before
  still parses;
* anything that modifies or removes a declaration, including deleting the file, is a
  **warning**, because a renamed label, a narrowed alias or a changed type can break
  ingestion.

An unrecognised construct therefore lands in the warning bucket rather than being missed.
One annotation is emitted per file.
"""
import argparse
import os
import re
import sys

from namespace_patterns import GitError, changed_files, git, git_show, matches, namespace_config, namespace_of

DOC_STRING_RE = re.compile(r'<"(?:[^"\\]|\\.)*">')

# GitHub renders at most ~10 annotations per level per step, so the budget is per level: ten
# warnings must not crowd out a notice, and vice versa. Anything over the budget still appears in
# the run summary, and the count that did not fit is stated -- a cap nobody is told about reads as
# "nothing else changed".
MAX_INLINE_ANNOTATIONS_PER_LEVEL = 10

# Every annotation ends with this, so a reviewer who has not seen one before can find out
# what generated namespaces are and why the note is there.
DEFAULT_DOCS_URL = "https://rune.finos.org/docs/developers/generated-namespaces"

# The keys under `origin` that name a producing tool. This is the same set the Java
# `RuneOriginConfiguration` recognises, and both ship from this repository at the same version, so
# a configuration can never carry a key the checker has not heard of.
ORIGIN_TOOLS = ("modelImport",)


def parse_generated_namespaces(text):
    """Return [(namespace_pattern, origin)] for entries whose ``origin`` names a tool."""
    generated = []
    for entry in namespace_config(text):
        namespace = entry.get("namespace")
        origin = entry.get("origin")
        if not namespace or not isinstance(origin, dict):
            continue
        tools = {tool: origin[tool] for tool in ORIGIN_TOOLS
                 if isinstance(origin.get(tool), str) and origin[tool]}
        if tools:
            generated.append((str(namespace), tools))
    return generated


def describe_origin(tools):
    return ", ".join(f"{tool} ({value})" for tool, value in tools.items())


def read(path):
    try:
        with open(path, encoding="utf-8", errors="replace") as handle:
            return handle.read()
    except OSError:
        return None


def strip_comment(line):
    """Drop a `//` comment, leaving `//` that appears inside a string literal alone.

    A URL in a label -- `[label "http://example.com/a"]` -- would otherwise be read as a
    comment, so an edit to it would classify as documentation-only.
    """
    in_string = False
    index = 0
    while index < len(line):
        char = line[index]
        if in_string:
            if char == "\\":
                index += 2
                continue
            if char == '"':
                in_string = False
        elif char == '"':
            in_string = True
        elif char == "/" and line.startswith("//", index):
            return line[:index]
        index += 1
    return line


def without_documentation(body):
    """A line reduced to its declaration, with documentation and comments removed."""
    return " ".join(strip_comment(DOC_STRING_RE.sub("", body)).split())


def declarations(diff_lines, marker):
    return sorted(filter(None, (
        without_documentation(line[1:])
        for line in diff_lines
        if line.startswith(marker) and not line.startswith(marker * 3)
    )))


def classify(base, old_path, new_path, code):
    """Return (level, description) for the change made to a file.

    Errs towards `warning`: only changes that are provably documentation-only or purely
    additive are downgraded to a notice.
    """
    if code == "D":
        return "warning", "deleted the file"

    # Both pathspecs, so git can pair a rename with its content change. Diffing only the
    # destination renders a rename as a wholly new file, which reads as purely additive --
    # an active all-clear over an edit that may have removed declarations.
    pathspecs = [new_path] if old_path == new_path else [old_path, new_path]
    diff_lines = git("diff", "-U0", "--find-renames", f"{base}...HEAD", "--", *pathspecs).splitlines()

    added = declarations(diff_lines, "+")
    removed = declarations(diff_lines, "-")

    if added == removed:
        return "notice", "changed documentation, comments or ordering only"
    if not removed:
        return "notice", "added declarations, without changing or removing any"
    return "warning", "changed or removed existing declarations"


def first_generated(namespaces, generated):
    """The first of `namespaces` covered by a generated pattern, with the tools that produced it."""
    for namespace in namespaces:
        if namespace is None:
            continue
        for pattern, tools in generated:
            if matches(namespace, pattern):
                return namespace, tools
    return None, None


def under_root(path, root):
    """Whether a repository-relative path lies under `root`. A root of `.` covers everything."""
    normalised = os.path.normpath(root)
    if normalised in (".", ""):
        return True
    return os.path.normpath(path).startswith(normalised + os.sep)


def findings_for(base, config_text, root):
    """Return [(level, path, namespace, origin, description)], warnings first."""
    findings = []
    generated = parse_generated_namespaces(config_text)
    if not generated:
        return findings

    for code, old_path, new_path in changed_files(base, "*.rosetta"):
        path = old_path if code == "D" else new_path
        if not (under_root(path, root) or under_root(old_path, root)):
            continue
        # Both sides, not the first one that answers: the question is whether the file was *ever* in
        # a generated namespace. A deleted file has only a base version, and a file whose `namespace`
        # declaration was edited has a head version that no longer matches -- moving one out of a
        # generated namespace must not be a way to leave without a word.
        head_namespace = namespace_of(read(new_path)) if code != "D" else None
        base_namespace = namespace_of(git_show(base, old_path))

        namespace, tools = first_generated([head_namespace, base_namespace], generated)
        if namespace is None:
            continue

        level, description = classify(base, old_path, new_path, code)
        if code in ("R", "C") and old_path != new_path:
            description = f"renamed the file and {description}"
        if base_namespace and head_namespace and base_namespace != head_namespace:
            description = (f"moved the file from '{base_namespace}' to '{head_namespace}' and "
                           f"{description}")
            level = "warning"
        findings.append((level, path, namespace, tools, description))

    # Warnings first, so that the most significant ones survive the annotation limit.
    findings.sort(key=lambda finding: (finding[0] != "warning", finding[1]))
    return findings


def within_annotation_budget(findings):
    """The findings that fit inline, budgeted per level rather than across the combined list."""
    shown, seen = [], {}
    for finding in findings:
        level = finding[0]
        seen[level] = seen.get(level, 0) + 1
        if seen[level] <= MAX_INLINE_ANNOTATIONS_PER_LEVEL:
            shown.append(finding)
    return shown


def annotate(findings, docs_url):
    shown = within_annotation_budget(findings)
    for level, path, namespace, tools, description in shown:
        if level == "warning":
            title = "Generated namespace: declarations changed"
            advice = ("If this touches a label, an attribute name or an attribute type, the column "
                      "binding changes with it - confirm that ingestion still resolves.")
        else:
            title = "Generated namespace modified"
            advice = "Hand edits are expected here; this note exists so the change is visible in review."
        print(
            f"::{level} file={path},line=1,title={title}::"
            f"Namespace '{namespace}' is maintained by {describe_origin(tools)}. "
            f"This pull request has {description}. {advice} "
            f"What is this? {docs_url}"
        )

    dropped = len(findings) - len(shown)
    if dropped:
        print(f"::notice::{dropped} further generated-namespace change(s) are not shown inline, "
              f"because GitHub renders at most {MAX_INLINE_ANNOTATIONS_PER_LEVEL} annotations per "
              f"level. All {len(findings)} are listed in the workflow run summary.")


def write_summary(findings, docs_url):
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if not summary_path:
        return
    with open(summary_path, "a", encoding="utf-8") as handle:
        handle.write("### Generated namespaces modified by this pull request\n\n")
        handle.write("| | File | Namespace | Origin | Change |\n")
        handle.write("|---|---|---|---|---|\n")
        for level, path, namespace, tools, description in findings:
            icon = "⚠️" if level == "warning" else "ℹ️"
            handle.write(f"| {icon} | `{path}` | `{namespace}` | {describe_origin(tools)} | {description} |\n")
        handle.write(
            "\nThese namespaces were produced by a tool. Editing them is expected - this check "
            "reports rather than blocks, so that the change is visible in review.\n"
            f"\n[What are generated namespaces?]({docs_url})\n"
        )


def main():
    parser = argparse.ArgumentParser(description="Report hand edits to generated Rune namespaces.")
    parser.add_argument("--base", required=True, help="Git ref to diff HEAD against.")
    parser.add_argument("--config", required=True,
                        help="Path to the rune-config.yml whose namespaceConfig lists the generated namespaces.")
    parser.add_argument("--root", default=".", help="Directory under which .rosetta files live.")
    parser.add_argument("--docs-url", default=DEFAULT_DOCS_URL,
                        help="Documentation link appended to every annotation.")
    args = parser.parse_args()

    config_text = read(args.config)
    if config_text is None:
        # Unlike the read-only check this one never fails on a model edit, so a missing config is
        # reported and skipped rather than treated as a mistyped path.
        print(f"::notice::No Rune configuration found at '{args.config}'; no generated namespaces to report.")
        return 0

    try:
        findings = findings_for(args.base, config_text, args.root)
    except GitError as error:
        # Silence would be indistinguishable from "nothing changed", so say so. The job still does
        # not fail: this check never blocks a pull request.
        print(f"::warning::The generated-namespace check could not run, so no edit was reported: {error}")
        return 0

    if not findings:
        return 0

    annotate(findings, args.docs_url)
    write_summary(findings, args.docs_url)
    return 0


if __name__ == "__main__":
    sys.exit(main())
