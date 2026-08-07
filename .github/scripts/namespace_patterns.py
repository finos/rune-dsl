#!/usr/bin/env python3
"""Namespace-pattern matching, git plumbing and ``rune-config.yml`` parsing shared by the
namespace checks.

A namespace pattern appears in ``generators.namespaces`` and in the ``namespace`` of a
``namespaceConfig`` entry. Namespaces are dot-separated sequences of alphanumeric
segments, so only two forms need supporting:

* a bare namespace, e.g. ``abc.def``, matching that namespace exactly;
* a terminal ``.*``, e.g. ``abc.def.*``, matching ``abc.def`` **and** all of its
  subnamespaces. A bare ``*`` is the degenerate case of this and matches everything.

Matching is segment-aware, so ``abc.def.*`` never matches ``abc.defghi``. No other glob
syntax is recognised: ``abc*``, ``a?c`` and ``a[bc]`` are read as exact namespaces, which
is to say they match nothing.

This mirrors ``RuneNamespacePattern`` in rune-runtime, which is the definition every
consumer shares. Both read the same case corpus -- ``namespace-pattern-cases.yml``
beside this file -- so a divergence between them fails a test. It deliberately is *not*
``fnmatch``: a glob would read ``abc.def.*`` as excluding ``abc.def`` itself and as
matching ``abc.defghi``.

``rune-config.yml`` is parsed with PyYAML, so that this side and the Java side read the same
file the same way. It is the only dependency the checkers have, and the reusable workflow
installs it.
"""
import re
import subprocess

import yaml

# Matches `namespace foo.bar`, optionally preceded by the `override` keyword and optionally quoted.
NAMESPACE_RE = re.compile(r'^\s*(?:override\s+)?namespace\s+"?([A-Za-z0-9_.]+)"?', re.MULTILINE)


class GitError(RuntimeError):
    """A git invocation failed. Raised rather than returning empty output, so that a check cannot
    silently conclude "nothing changed" when it never managed to look."""


def matches(namespace, pattern):
    """Whether `namespace` is covered by `pattern`. See the module docstring for the rule."""
    if namespace is None:
        return False
    if pattern == "*":
        return True
    if pattern.endswith(".*"):
        prefix = pattern[:-2]
        return namespace == prefix or namespace.startswith(prefix + ".")
    return namespace == pattern


def matches_any(namespace, patterns):
    """Whether `namespace` is covered by any of `patterns`. No patterns matches nothing."""
    return any(matches(namespace, pattern) for pattern in patterns)


def namespace_of(text):
    """The namespace declared by the given .rosetta file content, or None."""
    if text is None:
        return None
    match = NAMESPACE_RE.search(text)
    return match.group(1) if match else None


def git(*args):
    """Run git, raising GitError if it fails. Never returns partial output as if it were success."""
    result = subprocess.run(["git", *args], capture_output=True, text=True)
    if result.returncode != 0:
        raise GitError(f"git {' '.join(args)} failed: {result.stderr.strip()}")
    return result.stdout


def git_show(ref, path):
    """Content of `path` at `ref`, or None if it does not exist there."""
    result = subprocess.run(["git", "show", f"{ref}:{path}"], capture_output=True, text=True)
    return result.stdout if result.returncode == 0 else None


def changed_files(base, *pathspecs):
    """Yield (status, old_path, new_path) for files changed between `base` and HEAD.

    Renames and copies keep both paths, so a caller can see what a file was as well as what it
    became -- which is what makes a rename distinguishable from an unrelated add plus delete.
    """
    out = git("diff", "--name-status", "-z", "--find-renames", f"{base}...HEAD", "--", *pathspecs)
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


def namespace_config(text):
    """The ``namespaceConfig`` entries of a rune-config.yml, as a list of mappings.

    Parsed with PyYAML rather than walked by hand, so that every shape the Java side accepts is
    read the same way here -- flow style, valueless keys, nested values. Hand-walking the block
    structure made this a second, subtly different implementation of one file format.
    """
    if not text:
        return []
    try:
        config = yaml.safe_load(text)
    except yaml.YAMLError:
        return []
    if not isinstance(config, dict):
        return []
    entries = config.get("namespaceConfig") or []
    return [entry for entry in entries if isinstance(entry, dict)]
