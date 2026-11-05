---
status: accepted
date: 2026-11-05
decision-makers: [Simon Cockx]
---

# Record architecture decisions in docs/adr, as MADR 4.0

## Context and Problem Statement

Decisions about this repository's structure, dependencies and interfaces were made in PR threads,
Slack and Notion designs, where nobody finds them later. New joiners, reviewers and coding agents
cannot tell a deliberate choice from an accident, so deliberate choices get undone and settled
questions get argued again.

## Considered Options

* ADRs in `docs/adr/` in this repository, in MADR 4.0
* Decision pages in Notion
* No decision records

## Decision Outcome

Chosen option: "ADRs in `docs/adr/` in this repository, in MADR 4.0", because a record beside
the code is reviewed in the same PR as the code, and is read by coding agents working in the
repository without any extra set-up.

### Consequences

* Good, because a reviewer or an agent can check a change against the decisions it touches.
* Bad, because someone has to write each record, and supersede it when the decision changes.
