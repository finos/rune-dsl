Please include a summary of the change and the issue/story number.

## Type of change

Please delete options that are not relevant.

- Bug fix (non-breaking change which fixes an issue)
- New feature (non-breaking change which adds functionality)
- Breaking change (fix or feature that would cause existing functionality to not work as expected)
- This change requires a documentation update

## DSL Impact Summary

This section helps the CDM governance working groups (CRWG, TAWG, Steering WG) see at a glance whether this change needs their review, and lets you flag a change as scoped to extension models.

For each row, check the box that applies to your change..

| DSL Impact | Affects the CDM | Affects extension models |
|---|---|---|
| Validation changes (warnings/errors)? | Yes / No | Yes / No |
| Add new syntax? | Yes / No | Yes / No |
| Impact Java code generators? | Yes / No | Yes / No |
| Impact Python code generators? | Yes / No | Yes / No |
| Not backwards compatible? | Yes / No | Yes / No |

## CDM Working Group Approval

Working Group approval is required where a row above is checked "Affects the CDM":

- [ ] **Steering WG** review completed — required if this change is **not** backwards compatible
- [ ] **CRWG** review completed — required if this change requires model changes (due to validation changes) and/or adds new syntax
- [ ] **TAWG** review completed — required if this change adds new syntax, and/or impacts code generators
