#!/usr/bin/env python3
"""Fail the build when a read-only Rune namespace is changed by hand.

The read-only namespaces are read from the ``namespaceConfig`` entries marked
``readOnly: true`` of a ``rune-config.yml`` file, so they are configured in a single
place. Both the **base** and the **head** version of that config are consulted, so a
change that comes together with the matching config change -- e.g. a schema import that
adds a read-only namespace and its generated files in one pull request -- is allowed:

* an **added** file is a violation only if its namespace is already read-only in the
  *base* config (adding the files that first make a namespace read-only is allowed);
* a **deleted** file is a violation only if its namespace is still read-only in the
  *head* config (deleting a read-only namespace together with its config entry is allowed);
* a **modified** file is a violation only if its namespace is read-only in *both* the
  base and head config (a hand-edit of content that stays read-only). A modification that
  flips the read-only flag either way is treated as a deliberate lock/unlock and allowed.

The build also fails if a configured (head) pattern matches no namespace in the
repository, which catches stale or mistyped patterns, and if the config file does not
exist in the working tree, which catches a mistyped config path.

The checker needs only git, PyYAML and the shared ``namespace_patterns`` module beside it,
which also defines how a pattern is read and how the config is parsed.
"""
import argparse
import sys
from pathlib import Path

from namespace_patterns import changed_files, git_show, matches, matches_any, namespace_config, namespace_of


def parse_readonly_namespaces(text):
    """Read the read-only namespaces from the ``namespaceConfig`` list of rune-config.yml content.

    A namespace is read-only when its ``namespaceConfig`` entry declares ``readOnly: true``.
    """
    return [
        entry["namespace"]
        for entry in namespace_config(text)
        if entry.get("readOnly") is True and entry.get("namespace")
    ]


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


def classify(code, old, new, ns_before, ns_after, base_patterns, head_patterns):
    """Return a violation message for a changed file, or None when the change is allowed.

    The file's namespace before (``ns_before``) and after (``ns_after``) the change is compared
    against the read-only patterns of the base and head config:

    * a file entering a namespace (an add, or a move into a different namespace) is blocked when that
      namespace is already read-only in the *base* config;
    * a file leaving a namespace (a delete, or a move out of a different namespace) is blocked when
      that namespace is still read-only in the *head* config;
    * a file that stays in its namespace but is modified is blocked when that namespace is read-only in
      *both* configs.

    A rename between two namespaces is both an entry and a leave, so it reuses the first two checks.
    """
    entered = ns_after is not None and ns_after != ns_before
    left = ns_before is not None and ns_before != ns_after
    stayed = ns_before is not None and ns_before == ns_after
    if entered and matches_any(ns_after, base_patterns):
        return f"{describe(code, old, new)} adds a file to read-only namespace '{ns_after}'."
    if left and matches_any(ns_before, head_patterns):
        return f"{describe(code, old, new)} removes a file from read-only namespace '{ns_before}'."
    if stayed and matches_any(ns_before, base_patterns) and matches_any(ns_before, head_patterns):
        return f"{describe(code, old, new)} modifies a file in read-only namespace '{ns_before}'."
    return None


def main():
    parser = argparse.ArgumentParser(description="Verify that read-only Rune namespaces are not changed.")
    parser.add_argument("--base", required=True, help="Git ref to diff HEAD against.")
    parser.add_argument("--config", required=True,
                        help="Path to the rune-config.yml whose namespaceConfig lists the read-only namespaces.")
    parser.add_argument("--root", default=".", help="Directory to scan for .rosetta files.")
    args = parser.parse_args()

    # Read the read-only namespaces from both the head config (the checked-out worktree) and the base
    # config (read from the base ref), so added/deleted/modified files can be judged against the right one.
    head_config = read_worktree(args.config)
    if head_config is None:
        # A missing config would otherwise silently disable the check, hiding a mistyped config-path
        # (or a config that was deleted or renamed without updating the workflow).
        print("Read-only namespace check FAILED:")
        print(f"  - Config file '{args.config}' does not exist in the working tree. Is the config-path correct?")
        return 1
    head_patterns = parse_readonly_namespaces(head_config)
    base_patterns = parse_readonly_namespaces(git_show(args.base, args.config))
    if not head_patterns and not base_patterns:
        print(f"No read-only namespaces configured in '{args.config}' (base or head); nothing to check.")
        return 0

    failures = []

    # 1. Stale-pattern check: every configured head pattern must match at least one namespace.
    all_namespaces = repository_namespaces(args.root)
    for pattern in head_patterns:
        if not any(matches(ns, pattern) for ns in all_namespaces):
            failures.append(
                f"Read-only namespace pattern '{pattern}' does not match any .rosetta namespace "
                f"under '{args.root}' (stale or mistyped pattern?)."
            )

    # 2. Changed-file check.
    for code, old, new in changed_files(args.base):
        if not (old.endswith(".rosetta") or new.endswith(".rosetta")):
            continue
        ns_before = None if code in ("A", "C") else namespace_of(git_show(args.base, old))
        ns_after = None if code == "D" else namespace_of(read_worktree(new))
        violation = classify(code, old, new, ns_before, ns_after, base_patterns, head_patterns)
        if violation:
            failures.append(violation)

    if failures:
        print("Read-only namespace check FAILED:")
        for failure in failures:
            print(f"  - {failure}")
        return 1

    checked = sorted(set(base_patterns) | set(head_patterns))
    print(f"Read-only namespace check passed ({len(checked)} pattern(s) checked).")
    return 0


def read_worktree(path):
    file = Path(path)
    if not file.is_file():
        return None
    return file.read_text(encoding="utf-8", errors="replace")


if __name__ == "__main__":
    sys.exit(main())
