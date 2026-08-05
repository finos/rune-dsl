# Plan part 2: move the unused-element check to a post-build pass

Status: **NOT STARTED**. Prerequisite: everything in `unused-element-markers.md` (part 1), complete on
branch `unused-functions-editor-only`. This plan is a follow-up refactor of that shipped feature — land
part 1 first; do this on a fresh branch on top of it.

**One sentence:** stop computing "unused" hints inside per-resource validation and instead compute them
once per build, after `doLaunch()`, from the settled resource set — publishing only the diffs — which
deletes `IncomingReferenceChanges` and the unload-and-revalidate machinery outright.

## 1. Why

Part 1's design is correct and measured, but computing the hint *inside* `validate()` forces everything
downstream of it:

1. **The trigger problem.** A marker on file A depends on the contents of every other file, so part 1 needs
   `IncomingReferenceChanges` (~204 lines: a before/after reference-description diff, URI-fragment
   containment rollup, and an `isMarkerCapable` that hand-mirrors `UnusedElementHelper#isCandidate`) to
   decide which *other* files to revalidate after each build.
2. **The cache-defeat problem.** The triggered files' contents didn't change, so they must be unloaded
   before revalidation or the caches hand back the previous answer (see the javadoc on
   `RosettaStatefulIncrementalBuilder#revalidateResourcesWithChangedIncomingReferences`, and the
   clustering-order subtlety below it).
3. **The cost problem.** Revalidation is a full re-parse + link + all validators per triggered file —
   measured in part 1 (§3.6) at ~62% of the fan-out cost. The mass-edit case costs ~620 ms, 5–6× the
   build itself, and is user-perceptible.
4. **The cancellation window.** If the build is cancelled between `doLaunch()` and the pass, or mid-pass,
   the deltas' triggers are consumed and lost — the stale marker persists until some unrelated future edit
   happens to change references into the same file. Nothing repairs it.

A post-build recompute replaces the *diff-of-inputs* mechanism (guess whose answers changed from index
deltas) with a *diff-of-outputs* mechanism (recompute all answers cheaply, publish the ones that changed).
The caches flip from hazard to asset: hints are no longer part of any cached validation answer, so nothing
needs unloading, and the cached base issues are exactly what gets reused at republish time.

## 2. Decisions already made — do not relitigate

- **Keep `UnusedElementHelper`'s live-AST walk. Do not use `IReferenceFinder` or the index.** The full
  reasoning is in the javadoc on `UnusedElementHelper#computeOutgoingReferences` (three objections; only
  the staleness one is about *when* the check runs, and it is the only one this plan's move neutralises).
  Recorded 2026-08-02 after a dedicated investigation; the walk survives this refactor byte-for-byte apart
  from §3.4.
- Hint severity, editor-only binding, `[suppressUnused]`/`[rootType]` exemptions, dispatch/metaType/builtin
  exclusions, non-transitivity, and the qualified-name `ElementId` keying are all unchanged (part 1 §1a,
  §3b.2, §4.3, post-phase-5 notes).
- The single-project limitation stands, unchanged in shape: the pass runs per `ProjectManager` resource
  set, same as part 1's pass. (Resolved as document-and-accept in part 1's post-phase-5 record.)
- `UnusedElementStalenessTest` is the acceptance gate and must not be weakened.

## 3. Target design

### 3.1 The pass

In `RosettaStatefulIncrementalBuilder#launch()`, replace the call to
`revalidateResourcesWithChangedIncomingReferences(result)` with a new pass that runs after `doLaunch()`:

1. Skip when `getRequest().isIndexOnly()` (keep the existing guard and its comment).
2. Sweep every resource in the build context's resource set whose content is a `RosettaModel`; for each,
   compute its hint list with the relocated marker logic (§3.5) against a usage snapshot built once for the
   whole sweep (§3.4). Check cancellation per resource, exactly as the current `revalidate()` does.
3. Diff each resource's new hint list against the store (§3.2). For each URI whose hints changed:
   republish `baseIssues + newHints` through `getRequest().getAfterValidate().afterValidate(uri, merged)`,
   then — and only then — update that URI's store entry (§3.6 point 4 explains why this ordering).
   `baseIssues` come from the language's `IResourceValidator` (`CachingResourceValidator`), which is a
   cache hit because the resource didn't change or was just validated. Do **not** unload anything.

Why this publish channel is safe: `ProjectManager#newBuildRequest` wires `afterValidate` straight to the
`issueAcceptor` → `publishDiagnostics` (verified in Xtext 2.38 sources, `ProjectManager.java:128-129`), and
this very class already publishes generation errors through it (`generate(...)` catch blocks). LSP
`publishDiagnostics` **replaces** the diagnostics for a URI, which is why every republish must carry the
base issues merged in, never hints alone.

The pass runs inside `launch()`, i.e. inside the same `WriteRequest` as the build, so `RequestManager`
ordering guarantees hold: by the time the build future completes, every publish is out, and no read request
observes a half-published state. This is what keeps the existing LSP test harness working unchanged.

### 3.2 The marker store

The builder is instantiated **per build** — `RosettaServerModule#bindIncrementalBuilder$...` (line 85)
registers it as the class Xtext's `IncrementalBuilder` gets from a `Provider` — so state cannot live on the
builder. Add a small `@Singleton` store class (server injector; it reaches the builder by plain `@Inject`
since the builder comes from that injector), e.g. `ide/build/UnusedMarkerStore`:

- Keyed by resource `URI`. One language server serves one workspace, and URIs are absolute, so
  multi-project sharing of the singleton is harmless.
- The value must support equality for the diff. Xtext `Issue` implementations do not implement `equals`,
  so store an immutable record per marker — `(message, issueCode, offset, length)` is sufficient and
  stable — and compare lists. Keep the actual `Issue` objects only transiently for publishing.
- Entries for deleted resources are removed during the pass (a resource in the deltas with no new
  description / absent from the resource set). The client's diagnostics for deleted files are already
  cleared by `ProjectManager`, so no republish is needed — just drop the entry.

### 3.3 Merging hints into the build's own publishes (anti-flicker)

During `doLaunch()`, the regular loop validates and publishes each built resource *without* hints (the
validator no longer computes them). Left alone, an edited file would publish validation-only diagnostics
and get its hints back milliseconds later — a per-keystroke blink of the faded rendering.

Fix: at the start of `launch()`, wrap the request's `afterValidate` to append the store's *current* hints
for that URI to whatever the build publishes — the exact wrapping pattern `resetBuildStatistics()` already
uses on the same callback (`RosettaStatefulIncrementalBuilder.java:147-151`). The `BuildRequest` is
per-build, so wraps do not accumulate across builds. Consequences, both accepted:

- A built file's own hints are one pass stale *within* the build (e.g. deleting the last same-file call to
  a function in the same file), corrected by the §3.1 republish in the same write request, ms later. The
  diff criterion "newHints ≠ store entry" covers this automatically: for built files whose hints didn't
  change, the mid-build publish was already correct and no republish happens.
- If the statistics wrapper is also active, wrap order determines whether pass republishes are counted
  as validations. Wrap hints innermost (closest to the original callback) and note that
  `sourceFilesValidated` counting republishes is the same under-/over-count quirk part 1 already recorded
  for its pass — improve the log line if touched, don't redesign it.

### 3.4 `UnusedElementHelper` restructure (the one behaviour-preserving change)

`isReferenced` currently loops every resource per candidate (candidates × resources hash probes — ~360k
per full sweep on CDM, single-digit-to-low-double-digit ms). Restructure to the shape part 1 §4.5 already
earmarked: build the union of the per-resource `outgoingReferences` sets **once per sweep** and probe it
per candidate. Concretely: replace the public `isUnused(element)` with a two-step API, e.g.
`UsageSnapshot snapshot(ResourceSet)` (unions the cached sets; walks only uncached/changed resources) and
`boolean isUnused(RosettaRootElement, UsageSnapshot)`. The walk, the per-resource cache, `ElementId`,
`isCandidate`, and every exemption are untouched. The only production caller is the relocated marker logic
(§3.5); update any direct helper unit tests to the two-step call.

### 3.5 Relocations and deletions

| File | Change |
|---|---|
| `rune-ide/.../build/IncomingReferenceChanges.java` | **delete** (all ~204 lines) |
| `rune-ide/.../build/RosettaStatefulIncrementalBuilder.java` | delete `revalidateResourcesWithChangedIncomingReferences` + `revalidate`; add the §3.1 pass, §3.3 wrap, store injection; rewrite class javadoc |
| `rune-ide/.../validation/UnusedElementResourceValidator.java` | **delete**; its `KINDS` table, `markerFor` (keep static + package-private — `UnusedElementMarkerTest` asserts on it directly), `fallbackNoun` and the issue-construction loop move to a new language-injector class in the same package, e.g. `UnusedElementDiagnostics`, with a method like `List<Issue> computeHints(Resource, UsageSnapshot)` |
| `rune-ide/.../RosettaIdeModule.java` | remove `bindIResourceValidator()` — `RosettaRuntimeModule:124-125` already binds `CachingResourceValidator`, so removal falls back to exactly the right validator |
| `rune-ide/.../build/UnusedMarkerStore.java` | new, §3.2 |
| `rune-lang/.../validation/UnusedElementHelper.java` | §3.4 only; update the class javadoc sentence that says it is consumed by `UnusedElementResourceValidator`, and the final paragraph of the `computeOutgoingReferences` javadoc (it currently says "even a redesign that computed markers after the build *would* keep this walk" — after this plan, it *does*) |

Getting language services from the builder: the builder lives on the server injector, not the language
injector. Resolve per resource through the same lookup the inherited `validate(Resource)` uses (the build
context / `IResourceServiceProvider` registry) to obtain `UnusedElementDiagnostics` and the
`IResourceValidator`. Every `.rosetta` resource is the one language, but do the lookup per resource anyway
rather than caching a single provider.

### 3.6 Edge cases and invariants

1. **Cold build / server restart.** Store is empty; every built file publishes hint-less first, then the
   pass republishes the marker-bearing files once (a few dozen on CDM). Accepted.
2. **Non-model resources** (empty files, parse wrecks with no `RosettaModel` root): sweep skips them; if a
   URI previously had hints and now has no model, its new hint list is empty → diff publishes the removal.
3. **Builtins** never produce hints (`isInBuiltinResource`), so they never enter the store and are never
   republished — this preserves part 1 §4.3/§3.9 without any dedicated filter code.
4. **Cancellation self-heals.** Update a URI's store entry only *after* its publish succeeds. A pass
   cancelled mid-way leaves the remaining entries un-updated, so the next completed build's diff finds them
   unequal and republishes. This is strictly better than part 1, where a cancelled pass loses the triggers
   permanently.
5. **The publish path must not throw** (part 1's phase-3 lesson: a throw there surfaces as *missing*
   diagnostics). The generic `markerFor` fallback already covers unknown kinds; keep the pass free of
   other throw sources and keep the null-code guard in `RosettaLanguageServerImpl#toDiagnostic` as is.
6. **Clustering caveat carries over unchanged** (part 1 phase 2 "Remaining caveats"): if clustering ever
   unloads resources mid-sweep, their call sites are invisible to the walk. Same exposure as part 1; not
   reachable at tested scale; keep the note in the builder javadoc.
7. **`didOpen`-triggered builds** mark the opened file dirty (part 1 §2.3); it gets built and the wrap
   (§3.3) attaches current-store hints to its publish — no behaviour change from today.

## 4. Verification

Capture test counts before and after in each module (CLAUDE.md rule). Current baselines from part 1:
`rune-ide` 123 tests / 0 skipped, `rune-integration-tests` 1491 / 21 skipped.

1. `mvn -o test -pl rune-ide -Dtest='UnusedElement*'` — all **67** green with assertions unchanged
   (60 validation + 2 staleness + 5 marker). The staleness pair is the acceptance gate for the whole plan.
2. New tests (extend `UnusedElementValidationTest` / a small builder-level test):
   - deleting the file containing the only call site adds the marker to the declaring file; deleting the
     declaring file leaves no stale store entry (assert via a subsequent unrelated build not republishing).
   - an edited file's single `publishDiagnostics` contains both its validation issues and its unused hints
     (guards the §3.3 merge — this is the assertion that would catch hint-clobbering regressions).
3. Full `rune-ide` and `rune-integration-tests` suites; then full `mvn install` (checkstyle enforced).
   `rune-integration-tests` should be untouched — nothing here is a validator `@Check`.
4. `EditLatencyBenchmark` on CDM (`-Drune.benchmark.model.dir=...`), A/B against the part-1 branch, medians
   of 3, same five scenarios. Targets: keystroke ≤ 30 ms (the sweep must not be measurable after §3.4);
   reference-toggle at or below today's 44–48 ms (expect ~30); mass edit **materially** below 620 ms
   (expect ≤ ~300 ms — this case is the reason the plan exists); cold build within +100 ms.

## 5. Session plan

Assessed 2026-08-02. The core code change is moderate (~6 files, well-specified above), but the long pole
is verification: the 60-case LSP suite catches subtle publish-ordering mistakes, and the benchmark needs a
CDM checkout the agent may not have. **Do not attempt the whole plan in one Sonnet session.** One
strong-model session can plausibly do sessions 1+2 together if everything is green first pass; the split
below is the safe default and each session lands independently verifiable.

### Session 1 — the mechanism swap (Sonnet or Opus)

§3.4 helper restructure → `UnusedElementDiagnostics` relocation → store → pass + wrap → deletions +
unbinding, in that order (each step compiles). **Acceptance:** `rune-lang` compiles with checkstyle;
`mvn -o test -pl rune-ide -Dtest='UnusedElement*'` = 67/67 green, assertions unchanged. If the staleness
tests fail, the likely causes in order: republish missing the base-issue merge (§3.1), diff comparing
`Issue` objects instead of value records (§3.2), store updated before publish (§3.6.4).

### Session 2 — hardening and full verification (Sonnet or Opus)

The §4.2 new tests; full-suite runs and count capture; javadoc updates listed in §3.5; update this plan's
status header and part 1's phase-2 section with a pointer ("superseded by part 2's post-build pass").
**Acceptance:** full `mvn install` green; both modules' counts recorded; no `rune-integration-tests` change.

### Session 3 — benchmark (needs the user)

Requires a CDM checkout and `-Drune.benchmark.model.dir`; the agent should ask the user to provide the
path (or run it) rather than skipping silently. A/B per §4.4, record the table here next to part 1's
tables. If the keystroke row regresses past 30 ms, profile the sweep first — the union snapshot (§3.4) is
the knob, and per-URI diff short-circuits are the second.

## 6. Risks

- **Publish-path regressions are silent** — they manifest as missing or flickering diagnostics, not
  errors. The §4.2 merged-publish test and the staleness pair are the tripwires; run them after every
  change to the pass, not just at the end.
- **`Issue` equality**: the diff silently never firing (always-equal records) or always firing
  (identity comparison) both produce plausible-looking behaviour locally; the staleness tests catch the
  former, the merged-publish test plus a publish-count assertion catch the latter.
- **Wrap interactions**: `resetBuildStatistics` conditionally wraps the same callback; hint-merge must
  compose with it in either order without double-counting. Covered by running the suite with
  `ENABLE_INCREMENTAL_BUILDER_STATISTICS=true` once, manually.
- **Store lifetime**: server-injector singleton means test servers sharing an injector across tests could
  leak entries between cases; `AbstractRosettaLanguageServerValidationTest` creates a fresh server per
  test class setup — verify, and clear the store on `initialize` if not.
