Please include a summary of the change and the issue/story number.

## Type of change

Please delete options that are not relevant.

- Bug fix (non-breaking change which fixes an issue)
- New feature (non-breaking change which adds functionality)
- Breaking change (fix or feature that would cause existing functionality to not work as expected)
- This change requires a documentation update

## DSL Impact Summary

This section helps the CDM governance working groups (CRWG, TAWG, Steering WG) see at a glance whether this change needs their review, and lets you flag a change as scoped to private models only.

For each row, check the box that applies to your change..

| DSL Impact | Affects the CDM | Affects extension models only |
|---|---|---|
| Validation changes (warnings/errors)? | [ ] | [ ] |
| Add new syntax? | [ ] | [ ] |
| Impact Java code generators? | [ ] | [ ] |
| Impact Python code generators? | [ ] | [ ] |
| Backwards incompatible? | [ ] | [ ] |

If every "Affects the CDM" answer above is `No` or `N/A`, the working group checkboxes below can be skipped.

## CDM Working Group Approval

Required only where a row above marks "Affects the CDM" as `Yes`.

- [ ] **Steering WG** review completed — required if this change is **not** backwards compatible
- [ ] **CRWG** review completed — required if this change requires model changes (due to validation changes) and/or adds new syntax
- [ ] **TAWG** review completed — required if this change adds new syntax, and/or impacts code generators
