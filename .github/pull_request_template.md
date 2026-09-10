Please include a summary of the change and the issue/story number.

## Type of change

Please delete options that are not relevant.

- Bug fix (non-breaking change which fixes an issue)
- New feature (non-breaking change which adds functionality)
- Breaking change (fix or feature that would cause existing functionality to not work as expected)
- This change requires a documentation update

## DSL Impact Summary

This section helps the CDM governance working groups (CRWG, TAWG, Steering WG) see at a glance whether this change needs their review, and lets you flag a change as scoped to private models only.

For each row, delete the two options that don't apply in each column, leaving just `Yes`, `No`, or `N/A`.

| Does the change... | Affects the CDM | Affects private models only |
|---|---|---|
| ...require changes to the model (grammar / metamodel)? | Yes / No / N/A | Yes / No / N/A |
| ...add new syntax? | Yes / No / N/A | Yes / No / N/A |
| ...impact extensions? | Yes / No / N/A | Yes / No / N/A |
| ...impact code generators (Java/Python)? | Yes / No / N/A | Yes / No / N/A |
| ...remain backwards compatible? (original functionality is still available, or the change can be switched off) | Yes / No / N/A | Yes / No / N/A |

If every "Affects the CDM" answer above is `No` or `N/A`, the working group checkboxes below can be skipped.

## CDM Working Group Approval

Required only where a row above marks "Affects the CDM" as `Yes`.

- [ ] **CRWG** review completed — required if this change requires model changes and/or adds new syntax
- [ ] **TAWG** review completed — required if this change adds new syntax, impacts extensions, and/or impacts code generators
- [ ] **Steering WG** review completed — required if this change is **not** backwards compatible
