#!/usr/bin/env python3
import os
import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
PLAN_PATH = REPO_ROOT/".junie"/"xtend-migration-plan.md"

# Top-level folders to scan per requirement (per top level folder)
# We consider only source Xtend, not generated artifacts (.xtendbin, xtend-gen outputs).
SCAN_DIRS = [
    "rune-ide",
    "rune-integration-tests",
    "rune-lang",
    "rune-runtime",
    "rune-testing",
    "rune-tools",
    "rune-xcore-plugin-dependencies",
]

EXCLUDE_DIR_NAMES = {"xtend-gen", "target", ".git", ".idea", ".junie"}


def list_xtend_basenames_by_top_folder():
    """Return mapping: top_folder -> set of simple class basenames for .xtend files.

    Only considers '*.xtend' files under each top-level folder in SCAN_DIRS, skipping
    generated/targets.
    """
    mapping = {}
    for top in SCAN_DIRS:
        top_path = REPO_ROOT / top
        names = set()
        if not top_path.exists():
            mapping[top] = names
            continue
        for root, dirs, files in os.walk(top_path):
            # prune excluded dirs in-place
            dirs[:] = [d for d in dirs if d not in EXCLUDE_DIR_NAMES]
            for f in files:
                if f.endswith(".xtend"):
                    names.add(Path(f).stem)
        mapping[top] = names
    return mapping


CHECKBOX_RE = re.compile(r"^(\s*- \[)( |x)(\] )([A-Za-z0-9_.$-]+)\s*$")
SECTION_HEADER_RE = re.compile(r"^###\s+([a-zA-Z0-9.-]+)\s*\(")
# Fallback header without count: e.g. "### rune-lang"
SECTION_HEADER_SIMPLE_RE = re.compile(r"^###\s+([a-zA-Z0-9.-]+)\s*$")


def update_plan(plan_text: str, xtend_names_by_top: dict) -> str:
    lines = plan_text.splitlines()
    current_top = None
    out_lines = []
    for line in lines:
        m = SECTION_HEADER_RE.match(line)
        if not m:
            m = SECTION_HEADER_SIMPLE_RE.match(line)
        if m:
            current_top = m.group(1)
            out_lines.append(line)
            continue

        m2 = CHECKBOX_RE.match(line)
        if m2 and current_top in xtend_names_by_top:
            checked_symbol = m2.group(2)
            item_name = m2.group(4)
            xtend_present = item_name in xtend_names_by_top[current_top]
            # If no xtend source exists anymore, mark as migrated [x]
            if not xtend_present and checked_symbol != 'x':
                line = f"- [x] {item_name}"
            # If xtend exists, keep as-is (even if already checked)
            out_lines.append(line)
            continue

        out_lines.append(line)
    return "\n".join(out_lines) + ("\n" if plan_text.endswith("\n") else "")


def main():
    with open(PLAN_PATH, "r", encoding="utf-8") as f:
        original = f.read()
    mapping = list_xtend_basenames_by_top_folder()
    updated = update_plan(original, mapping)
    if updated != original:
        with open(PLAN_PATH, "w", encoding="utf-8") as f:
            f.write(updated)
        print("[INFO] Migration plan updated based on current .xtend sources.")
    else:
        print("[INFO] Migration plan already up to date.")

    # Print a brief per-folder summary for visibility
    for top, names in mapping.items():
        print(f"[SUMMARY] {top}: {len(names)} .xtend files")

if __name__ == "__main__":
    main()
