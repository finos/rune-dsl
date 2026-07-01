#!/usr/bin/env python3
"""Fail the build when a read-only Rune namespace is changed.

The read-only namespaces are read from the ``namespaceConfig`` entries marked
``readOnly: true`` of a ``rune-config.yml`` file, so they are configured in a single
place. A change to a ``.rosetta`` file is a violation when the file's namespace is
read-only either:

* **before** the change -- you may not modify, delete or move a file *out of* a
  read-only namespace; or
* **after** the change -- you may not add a file to, or move a file *into*, a
  read-only namespace.

The build also fails if a configured pattern matches no namespace in the
repository, which catches stale or mistyped patterns.

The checker is self-contained: it only uses the Python standard library and git.
"""
import argparse
import fnmatch
import re
import subprocess
import sys
from pathlib import Path

# Matches `namespace foo.bar`, optionally preceded by the `override` keyword and optionally quoted.
NAMESPACE_RE = re.compile(r'^\s*(?:override\s+)?namespace\s+"?([A-Za-z0-9_.]+)"?', re.MULTILINE)


def git(*args):
    return subprocess.run(["git", *args], capture_output=True, text=True, check=True).stdout


def git_show(ref, path):
    """Content of `path` at `ref`, or None if it does not exist there."""
    result = subprocess.run(["git", "show", f"{ref}:{path}"], capture_output=True, text=True)
    return result.stdout if result.returncode == 0 else None


def namespace_of(text):
    if text is None:
        return None
    match = NAMESPACE_RE.search(text)
    return match.group(1) if match else None


def read_readonly_namespaces(config_path):
    """Read the read-only namespaces from the ``namespaceConfig`` list of a rune-config.yml file.

    A namespace is read-only when its ``namespaceConfig`` entry declares ``readOnly: true``.
    Only the standard library is used, so this walks the block-style YAML directly rather than
    parsing it fully: it collects the ``namespace`` of each list item that also sets
    ``readOnly: true``, ignoring any nested keys (e.g. those under ``schemaConfig``).
    """
    config = Path(config_path)
    if not config.is_file():
        return []

    patterns = []
    in_section = False
    section_indent = 0
    item_indent = None
    current = None  # {"namespace": str|None, "read_only": bool} for the entry being parsed

    def flush():
        if current and current["read_only"] and current["namespace"]:
            patterns.append(current["namespace"])

    for raw in config.read_text(encoding="utf-8").splitlines():
        line = re.sub(r'\s+#.*$', '', raw).rstrip()  # strip trailing comments
        if not line.strip():
            continue
        indent = len(line) - len(line.lstrip())

        if not in_section:
            if re.match(r'^(\s*)namespaceConfig\s*:\s*$', line):
                in_section = True
                section_indent = indent
                item_indent = None
                current = None
            continue

        # A key at or below the section indent that is not a list item ends the section.
        if indent <= section_indent and not line.lstrip().startswith("-"):
            flush()
            current = None
            in_section = False
            continue

        item = re.match(r'^(\s*)-\s*(.*)$', line)
        if item and (item_indent is None or len(item.group(1)) == item_indent):
            item_indent = len(item.group(1))
            flush()
            current = {"namespace": None, "read_only": False}
            _apply(current, item.group(2))
        elif current is not None:
            _apply(current, line.lstrip())
    flush()
    return patterns


def _apply(current, text):
    """Track the ``namespace`` and ``readOnly`` keys of the current ``namespaceConfig`` entry."""
    kv = re.match(r'^(\w+)\s*:\s*(.*)$', text)
    if not kv:
        return
    key, value = kv.group(1), _clean(kv.group(2))
    if key == "namespace":
        current["namespace"] = value
    elif key == "readOnly":
        current["read_only"] = value.lower() == "true"


def _clean(value):
    return value.strip().strip('"').strip("'")


def changed_rosetta_files(base):
    """Yield (status, old_path, new_path) for changed files between base and HEAD."""
    out = git("diff", "--name-status", "-z", f"{base}...HEAD")
    tokens = out.split("\0")
    i = 0
    while i < len(tokens):
        status = tokens[i]
        if not status:
            i += 1
            continue
        code = status[0]
        if code in ("R", "C"):
            yield code, tokens[i + 1], tokens[i + 2]
            i += 3
        else:
            yield code, tokens[i + 1], tokens[i + 1]
            i += 2


def matches_any(namespace, patterns):
    return namespace is not None and any(fnmatch.fnmatchcase(namespace, pattern) for pattern in patterns)


def repository_namespaces(root):
    namespaces = set()
    for file in Path(root).rglob("*.rosetta"):
        try:
            namespace = namespace_of(file.read_text(encoding="utf-8", errors="replace"))
        except OSError:
            continue
        if namespace:
            namespaces.add(namespace)
    return namespaces


def describe(code, old, new):
    verb = {"A": "added", "M": "modified", "D": "deleted", "R": "renamed", "C": "copied"}.get(code, "changed")
    return f"{new} ({verb})" if old == new else f"{old} -> {new} ({verb})"


def main():
    parser = argparse.ArgumentParser(description="Verify that read-only Rune namespaces are not changed.")
    parser.add_argument("--base", required=True, help="Git ref to diff HEAD against.")
    parser.add_argument("--config", required=True,
                        help="Path to the rune-config.yml whose namespaceConfig lists the read-only namespaces.")
    parser.add_argument("--root", default=".", help="Directory to scan for .rosetta files.")
    args = parser.parse_args()

    patterns = read_readonly_namespaces(args.config)
    if not patterns:
        print(f"No readOnlyNamespaces configured in '{args.config}'; nothing to check.")
        return 0

    failures = []

    # 1. Stale-pattern check: every configured pattern must match at least one namespace.
    all_namespaces = repository_namespaces(args.root)
    for pattern in patterns:
        if not any(fnmatch.fnmatchcase(ns, pattern) for ns in all_namespaces):
            failures.append(
                f"Read-only namespace pattern '{pattern}' does not match any .rosetta namespace "
                f"under '{args.root}' (stale or mistyped pattern?)."
            )

    # 2. Changed-file check: a file may not be read-only before OR after the change.
    for code, old, new in changed_rosetta_files(args.base):
        if not (old.endswith(".rosetta") or new.endswith(".rosetta")):
            continue
        before = None if code in ("A", "C") else namespace_of(git_show(args.base, old))
        after = None if code == "D" else namespace_of(read_worktree(new))
        if matches_any(before, patterns):
            failures.append(f"{describe(code, old, new)} changes a file in read-only namespace '{before}'.")
        elif matches_any(after, patterns):
            failures.append(f"{describe(code, old, new)} moves a file into read-only namespace '{after}'.")

    if failures:
        print("Read-only namespace check FAILED:")
        for failure in failures:
            print(f"  - {failure}")
        return 1

    print(f"Read-only namespace check passed ({len(patterns)} pattern(s) checked).")
    return 0


def read_worktree(path):
    file = Path(path)
    if not file.is_file():
        return None
    return file.read_text(encoding="utf-8", errors="replace")


if __name__ == "__main__":
    sys.exit(main())
