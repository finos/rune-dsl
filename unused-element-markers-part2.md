# Plan part 2: a generic post-build diagnostics service, with unused-elements as its first provider

Status: **SESSION 1 DONE** (2026-08-13) — the mechanism swap has landed; sessions 2 and 3 remain.
Prerequisite: everything in `unused-element-markers.md` (part 1), complete on
branch `unused-functions-editor-only`. Part 1 is not released — it is still open as PR #1299 — so this work
continues **on that same branch**, not a fresh one on top of it. That is also what §7 recommends: since this
plan deletes `IncomingReferenceChanges` outright, folding it into the PR before merge means the
infrastructure lands once, in its final generic form.

**One sentence:** stop computing "unused" hints inside per-resource validation and instead compute them
once per build, after the build completes, from the settled workspace — publishing only the diffs — behind a
language-agnostic provider SPI, which deletes `IncomingReferenceChanges` and the unload-and-revalidate
machinery outright.

Review feedback on PR #1299 (SimonCockx, 2026-08-04) endorsed the post-build direction and asked for the
lifecycle to be generic rather than Rune-specific. §3.1 is that ask. His suggestion to source references
from `IReferenceFinder` is declined — see §2.

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
4. **The cancellation window.** If the build is cancelled between the build and the pass, or mid-pass,
   the deltas' triggers are consumed and lost — the stale marker persists until some unrelated future edit
   happens to change references into the same file. Nothing repairs it.
5. **No reuse.** "A diagnostic that depends on more than the resource being validated" is a general
   language-server problem. Part 1 solves it once, privately, inside a Rune builder subclass.

A post-build recompute replaces the *diff-of-inputs* mechanism (guess whose answers changed from index
deltas) with a *diff-of-outputs* mechanism (recompute all answers cheaply, publish the ones that changed).
The caches flip from hazard to asset: hints are no longer part of any cached validation answer, so nothing
needs unloading, and the cached base issues are exactly what gets reused at republish time.

## 2. Decisions already made — do not relitigate

- **Keep `UnusedElementHelper`'s live-AST walk. Do not use `IReferenceFinder` or the index.** Three
  objections are recorded on `UnusedElementHelper#computeOutgoingReferences`. Moving the check post-build
  kills the second one and leaves the other two standing, so the conclusion is unchanged:
  - *Same-file references are never indexed.* `RosettaResourceDescriptionStrategy` does not override
    `createReferenceDescriptions`, so recording is gated by `DefaultResourceDescriptionStrategy`, whose
    `isResolvedAndExternal` returns `from.eResource() != to.eResource()` (Xtext 2.38, line 133). A function
    calling a function in its own file produces no reference description, so an index query reports it
    unused. All 60 cases in `UnusedElementValidationTest` are single-file, and 34 of them assert that a
    reference keeps a declaration unmarked; an index-based implementation fails all 34. Keeping the walk for
    the candidate's own resource and the index for the rest means two mechanisms with two notions of what a
    reference is, both needing container rollup and self-reference exclusion — more code than the walk.
  - *(Dead) The index is stale mid-build.* True while the check ran inside validation. Post-build the index
    is settled, so this objection no longer applies. The closing paragraph of that javadoc says as much and
    must be rewritten (§3.7).
  - *The index records positional URIs.* Nothing binds `IFragmentProvider` and Xtext's
    `DefaultFragmentProvider.getFragment` delegates to the EMF fallback, so `getTargetEObjectUri()` returns
    fragments like `//@elements.3/@attributes.1` that renumber on insertion. Matching those against
    candidates, and re-deriving `declaringRootElement` in fragment space so a reference to an enum value
    counts as a use of its enumeration, is strictly worse than the walk.

  The *batching* half of the review comment is accepted and is §3.5: build one snapshot per sweep rather
  than scanning per candidate.
- **The pass hooks `WorkspaceManager#afterBuild`, not the incremental builder.** Verified in Xtext 2.38:
  `WorkspaceManager#didChangeFiles` (`:297-304`) returns a `Buildable` whose body is
  `buildable.build(...)` then `afterBuild(deltas)`, and `LanguageServerImpl#runBuildable` (`:487-489`) runs
  that buildable inside `requestManager.runWrite`. So `afterBuild` executes in the build's own write request
  and part 1's ordering guarantee holds. Every document path funnels through `didChangeFiles`
  (`didChangeTextDocumentContent:406`, `didOpen:422`, `didClose:431/433`), and the cold build is covered by
  `refreshWorkspaceConfig:254`. The LSP test harness drives the same entry points
  (`AbstractLanguageServerTest#open:401` → `didOpen`; `makeChange` → `didChange`).
- Hint severity, editor-only binding, `[suppressUnused]`/`[rootType]` exemptions, dispatch/metaType/builtin
  exclusions, non-transitivity, and the qualified-name `ElementId` keying are all unchanged (part 1 §1a,
  §3b.2, §4.3, post-phase-5 notes).
- The single-project limitation stands. The review independently confirmed the deployment model:
  `SingleProjectWorkspaceConfigFactory` creates one `MultiRootProjectConfig` with all workspace folders as
  source folders of that one project. Document it and add a multi-source-folder test (§4.2); do not build
  multi-project support.
- `UnusedElementStalenessTest` is the acceptance gate and must not be weakened.

## 3. Target design

### 3.1 The generic service and its SPI

New package `rune-ide/.../server/diagnostics` (language-agnostic — nothing in it may import
`com.regnosys.rosetta.rosetta.*`):

As built (the `Object sweepState` this plan originally proposed was replaced by a closure, which is the
cleaner shape the section invited — no cast, no type parameter):

```java
public interface IWorkspaceDerivedDiagnosticsProvider {
    /** Prepares the whole-workspace state, once, and returns the per-resource step that reads it. */
    Sweep beginSweep(ResourceSet resourceSet);

    interface Sweep {
        List<Issue> computeDiagnostics(Resource resource);
    }
}
```

Two phases rather than one method because the useful shape of the state is provider-specific — Rune's is
`UsageSnapshot` (§3.5) — and computing it per resource is what makes the naive version quadratic.

The service owns the store (§3.3), the sweep, the diff, and the republish. Providers are contributed via a
Guice multibinder from the language module, so a second future consumer is an added binding and one class.
The service runs on the server injector and so cannot inject that set; it resolves it per resource through
`IResourceServiceProvider.get(Injector.class).getExistingBinding(...)`, because
`IResourceServiceProvider#get(Class)` cannot express a multibound `Set`. A language contributing nothing
yields an empty set rather than an error.

### 3.2 The pass

`RosettaWorkspaceManager` (already bound by `RosettaServerModule#bindWorkspaceManager`) overrides
`afterBuild(List<IResourceDescription.Delta>)`, which is `protected` on `WorkspaceManager` (`:282`), and
delegates to the service:

1. For each `ProjectManager` from `getProjectManagers()` (`:381`), skip those whose `getProjectConfig()`
   reports `isIndexOnly()` — that is where the flag comes from (`ProjectManager:133`), so part 1's
   `getRequest().isIndexOnly()` guard survives the move.
2. Call `beginSweep` once on that project's `getResourceSet()` (`:219`).
3. Sweep every resource in it whose content is a `RosettaModel`; compute each one's diagnostics. Check
   cancellation per resource, as the current `revalidate()` does.
4. Diff each resource's new list against the store (§3.3). For each URI whose list changed, republish
   `baseIssues + newDiagnostics`, then — and only then — update that URI's store entry (§3.8.4 explains the
   ordering). Do **not** unload anything.
   **Correction made during session 1:** `baseIssues` must be *what was last published* for the URI, not a
   fresh `IResourceValidator.validate` call. Not every diagnostic comes from the validator —
   `RosettaStatefulIncrementalBuilder#generate` reports a code-generation failure straight to
   `afterValidate` — so re-deriving the base from validation alone silently drops generation errors on the
   republish. `GenerationErrorHandlingTest` catches this immediately. The wrapper of §3.4 is the one place
   that sees every publish, so it records the base; see §3.3.
   A second consequence: only resources something has already published for are eligible for a republish.
   That is the right rule anyway (a dependency loaded only to resolve references is not ours to report on)
   and it subsumes the builtin exclusion of §3.8.3.
5. Prune store entries for URIs the deltas report deleted. `afterBuild` receives the deltas directly, so
   this needs no inference.
6. `afterBuild` is handed no `CancelIndicator`, and step 3 needs one. `RosettaWorkspaceManager` captures the
   current build's indicator in a field by overriding `didChangeFiles` (wrapping the returned `Buildable`)
   and `refreshWorkspaceConfig` — between them, every build path. Request handling is serialised, so a field
   is enough.

**The publish channel.** `WorkspaceManager` hands its `issueAcceptor` to each `ProjectManager` at
initialize; that acceptor is `LanguageServerImpl#publishDiagnostics` (`:510`), which routes through
`toDiagnostics:523` → `toDiagnostic:536` → `RosettaLanguageServerImpl#toDiagnostic:97`, where
`UNUSED_DECLARATION` becomes `Hint` + `DiagnosticTag.Unnecessary`. The service must publish through that
acceptor, not through
`getLanguageClient()` directly, or the tag mapping is lost.

Reaching it needs a workaround: `WorkspaceManager.issueAcceptor` is private (`:78`) and
`ProjectManager#getIssueAcceptor()` is protected (`:207`), so neither is visible from
`com.regnosys.rosetta.ide.server`. `RosettaWorkspaceManager` already overrides
`initialize(URI, issueAcceptor, CancelIndicator)`; override both public `initialize` overloads and capture
the acceptor in a field of its own. Install the service's wrapper (§3.4) at that point — before
`refreshWorkspaceConfig` runs, since that triggers the initial build.

LSP `publishDiagnostics` **replaces** the diagnostics for a URI, which is why every republish must carry the
base issues merged in, never hints alone.

### 3.3 The store

A `@Singleton` on the server injector, `server/diagnostics/DerivedDiagnosticsStore`. As built it records
both halves of what the client has been told about each resource, keyed by resource `URI`:

- the **base**: the issues last published for it by something other than a sweep, captured by the §3.4
  wrapper. This is what makes a republish faithful — see the correction in §3.2 step 4. An entry existing at
  all is also the "has anything been published for this resource" test the sweep gates on.
- the **derived** half, per provider.
- URIs are absolute, so one store across projects is harmless.
- The diff needs equality. Xtext `Issue` implementations do not implement `equals`, so comparison reduces
  each issue to an immutable `(message, issueCode, offset, length)` record — sufficient and stable — and
  compares the per-provider maps, with an empty contribution comparing equal to no entry. The actual `Issue`
  objects are *retained*, not discarded: republishing needs the line and column information they carry, which
  the value record deliberately omits. They hold no reference to the model.
- Cleared on `initialize`, so a test server reusing an injector across cases cannot leak entries.

### 3.4 Merging into the build's own publishes (anti-flicker)

During the build, the regular loop validates and publishes each built resource *without* hints (the
validator no longer computes them). Left alone, an edited file would publish validation-only diagnostics
and get its hints back milliseconds later — a per-keystroke blink of the faded rendering.

Fix: wrap the issue acceptor **once**, at `initialize` (§3.2), so every publish through it has the store's
*current* derived entries for that URI appended. One wrap for the life of the workspace manager — not part 1's
per-build wrap of `BuildRequest.afterValidate` — so there is no accumulation question and no interaction to
reason about with `resetBuildStatistics`, which wraps a different callback and is untouched by this plan.

**Double-attach, resolved:** the wrapper is not in the pass's path at all. `install` captures the *unwrapped*
acceptor and the pass publishes its merged list straight through that, so exactly one component owns each
publish and there is no "skip the URIs the pass is republishing" bookkeeping. The same wrapper is where the
base half of the store is recorded (§3.3), which is the only reason the pass can assemble a faithful merged
list without re-running validation.

Consequence, accepted: a built file's own hints are one pass stale *within* the build (e.g. deleting the
last same-file call to a function in the same file), corrected by the §3.2 republish in the same write
request, ms later. The diff criterion "new list ≠ store entry" covers this automatically: for built files
whose hints didn't change, the mid-build publish was already correct and no republish happens.

### 3.5 `UnusedElementHelper` restructure (the one behaviour-preserving change)

`isReferenced` currently loops every resource per candidate (candidates × resources hash probes — ~360k
per full sweep on CDM, single-digit-to-low-double-digit ms). Restructure to the shape part 1 §4.5 already
earmarked: build the union of the per-resource `outgoingReferences` sets **once per sweep** and probe it
per candidate. Concretely: replace the public `isUnused(element)` with a two-step API —
`UsageSnapshot snapshot(ResourceSet)` (unions the cached sets; walks only uncached/changed resources) and
`boolean isUnused(RosettaRootElement, UsageSnapshot)`. The walk, the per-resource cache, `ElementId`,
`isCandidate`, and every exemption are untouched. The two steps map onto `beginSweep`/`computeDiagnostics`.
The only production caller is the Rune provider (§3.6); update any direct helper unit tests.

### 3.6 The Rune provider

`UnusedElementResourceValidator`'s Rune-specific content — the `KINDS` table, `markerMessageFor`,
`fallbackNoun`, and the issue-construction loop — moves to a language-injector class implementing
`IWorkspaceDerivedDiagnosticsProvider`, e.g. `rune-ide/.../validation/UnusedElementDiagnosticsProvider`.
`markerMessageFor` stays static and package-private; `UnusedElementMarkerTest` asserts on it directly. This
class holds all the policy and none of the lifecycle.

Getting it from the service: the service runs on the server injector, the provider on the language
injector. Resolve per resource through the `IResourceServiceProvider` registry — the same lookup the
builder's inherited `validate(Resource)` uses — see §3.1 for how the multibound set is reached from there.
Every `.rosetta` resource is the one language, but the lookup is per resource anyway; `beginSweep` is
memoised per language for the duration of one sweep, so the snapshot is still built once.

### 3.7 Relocations and deletions

| File | Change |
|---|---|
| `rune-ide/.../build/IncomingReferenceChanges.java` | **delete** (all ~204 lines) |
| `rune-ide/.../build/RosettaStatefulIncrementalBuilder.java` | delete `revalidateResourcesWithChangedIncomingReferences` + `revalidate`; rewrite class javadoc. Nothing from this plan is added here — the class returns to its generation-error duties only |
| `rune-ide/.../server/RosettaWorkspaceManager.java` | override the `List<WorkspaceFolder>` `initialize` (install the wrapper — the `baseDir` overload funnels into it, so wrapping there too would double-wrap), `afterBuild`, and `didChangeFiles`/`refreshWorkspaceConfig` for the cancel indicator (§3.2) |
| `rune-ide/.../server/diagnostics/IWorkspaceDerivedDiagnosticsProvider.java` | new, §3.1 |
| `rune-ide/.../server/diagnostics/WorkspaceDerivedDiagnosticsService.java` | new, §3.1–§3.2 |
| `rune-ide/.../server/diagnostics/DerivedDiagnosticsStore.java` | new, §3.3 |
| `rune-ide/.../validation/UnusedElementResourceValidator.java` | **delete**; content moves to `UnusedElementDiagnosticsProvider` (§3.6) |
| `rune-ide/.../RosettaIdeModule.java` | remove `bindIResourceValidator()` — `RosettaRuntimeModule:124-125` already binds `CachingResourceValidator`, so removal falls back to exactly the right validator; add the provider multibinding |
| `rune-lang/.../validation/UnusedElementHelper.java` | §3.5 only; update the class javadoc sentence naming `UnusedElementResourceValidator` as its consumer, and the closing paragraph of the `computeOutgoingReferences` javadoc — it currently says "even a redesign that computed markers after the build *would* keep this walk"; after this plan it *does*, and the reason narrows to the same-file and positional-URI objections (§2) |

### 3.8 Edge cases and invariants

1. **Cold build / server restart.** Store is empty; every built file publishes hint-less first, then the
   pass republishes the marker-bearing files once (a few dozen on CDM). Accepted.
2. **Non-model resources** (empty files, parse wrecks with no `RosettaModel` root): sweep skips them; if a
   URI previously had hints and now has no model, its new list is empty → diff publishes the removal.
3. **Builtins** never produce hints (`isInBuiltinResource`), so they never enter the store and are never
   republished — this preserves part 1 §4.3/§3.9 without any dedicated filter code.
4. **Cancellation self-heals.** Update a URI's store entry only *after* its publish succeeds. A pass
   cancelled mid-way leaves the remaining entries un-updated, so the next completed build's diff finds them
   unequal and republishes. Cancellation *before* the pass skips `afterBuild` entirely — the same exposure
   part 1 had, repaired the same way, and strictly better than part 1's permanently-lost triggers.
5. **The publish path must not throw** (part 1's phase-3 lesson: a throw there surfaces as *missing*
   diagnostics). The generic `markerFor` fallback already covers unknown kinds; keep the pass free of
   other throw sources and keep the null-code guard in `RosettaLanguageServerImpl#toDiagnostic` as is.
6. **Clustering caveat carries over unchanged** (part 1 phase 2 "Remaining caveats"): if clustering ever
   unloads resources mid-sweep, their call sites are invisible to the walk. Same exposure as part 1; not
   reachable at tested scale; keep the note in the service javadoc.
7. **`didOpen`-triggered builds** mark the opened file dirty (part 1 §2.3); it gets built and the wrap
   (§3.4) attaches current-store hints to its publish — no behaviour change from today.
8. **Single project, many source folders.** The sweep is per `ProjectManager` resource set. In the
   supported deployment that is one project holding every workspace folder as a source folder, so
   cross-folder references are in the same resource set and are seen. Record this in the service javadoc.

## 4. Verification

Capture test counts before and after in each module (CLAUDE.md rule). Part 1 recorded `rune-ide` 123 /
0 skipped and `rune-integration-tests` 1491 / 21 skipped; both are stale — commits landed on the branch
since. **Measured after session 1 (2026-08-13): `rune-ide` 125 / 0 skipped, `rune-integration-tests`
1504 / 21 skipped.** Session 1 added and removed no tests: `rune-ide`'s `@Test` count is identical to the
commit it started from, and `rune-integration-tests` has no working-tree change at all.

1. `mvn -o test -pl rune-ide -Dtest='UnusedElement*'` — all **67** green with assertions unchanged
   (60 validation + 2 staleness + 5 marker). The staleness pair is the acceptance gate for the whole plan.
2. New tests:
   - deleting the file containing the only call site adds the marker to the declaring file; deleting the
     declaring file leaves no stale store entry (assert via a subsequent unrelated build not republishing).
   - an edited file's single `publishDiagnostics` contains both its validation issues and its unused hints
     (guards the §3.4 merge — this is the assertion that would catch hint-clobbering regressions).
   - **a declaration in one source folder referenced from another is not flagged** — the multi-source-folder
     case the review asked for, and the only test that pins §3.8.8.
3. Full `rune-ide` and `rune-integration-tests` suites; then full `mvn install` (checkstyle enforced).
   `rune-integration-tests` should be untouched — nothing here is a validator `@Check`.
4. `EditLatencyBenchmark` on CDM (`-Drune.benchmark.model.dir=...`), A/B against the part-1 branch, medians
   of 3, same five scenarios. Targets: keystroke ≤ 30 ms (the sweep must not be measurable after §3.5);
   reference-toggle at or below today's 44–48 ms (expect ~30); mass edit **materially** below 620 ms
   (expect ≤ ~300 ms — this case is the reason the plan exists); cold build within +100 ms.

## 5. Session plan

Reassessed 2026-08-13 after PR review. The generic SPI adds a layer to what was already a moderate change,
and the long pole is still verification: the 60-case LSP suite catches subtle publish-ordering mistakes.
**Do not attempt the whole plan in one session.** Each session lands independently verifiable.

### Session 1 — the mechanism swap (**Opus**) — **DONE 2026-08-13**

§3.5 helper restructure → §3.1 SPI + service + store → §3.6 provider relocation → §3.2 `afterBuild`
override and acceptor capture → §3.4 wrap → deletions and unbinding, in that order (each step compiles).
Opus rather than Sonnet because the publish-channel plumbing (protected/private acceptor, wrapper vs pass
ordering in §3.4) is the part most likely to produce plausible-but-wrong code.

**Acceptance met:** `mvn -o test -pl rune-ide -Dtest='UnusedElement*'` = 67/67 green with assertions
unchanged (60 validation + 2 staleness + 5 marker), and `mvn -o install` green across the reactor with
checkstyle enforced — so session 2's full-suite acceptance is already satisfied too.

**What the plan got wrong, found by the tests:**

- **Base issues (§3.2 step 4).** Re-deriving them from `IResourceValidator` drops any diagnostic that does
  not come from the validator. `GenerationErrorHandlingTest` fails immediately: a republish over a file with
  a code-generation error replaces the error with the unused hint. Fixed by recording the base in the §3.4
  wrapper. See §3.2 and §3.3 for the corrected design.
- **A latent part-1 bug surfaced by that fix.** Previously the generation-error publish ran *after* the
  validate publish and clobbered the file's unused hints, so a file with a generation error silently lost its
  markers. It now shows both, which is correct. `GenerationErrorHandlingTest`'s two assertions were narrowed
  to filter by the generation-error issue code — the only test change in session 1 that was not a rename,
  and the subject of that test is unaffected.
- **Wrapper/pass double-attach (§3.4, §6).** Simpler than the plan assumed: the pass publishes through the
  unwrapped acceptor, so no skip-list is needed.
- **No cancel indicator in `afterBuild`.** Not noticed when writing §3.2; see step 6 there.

**Remaining for session 2**, unchanged: the §4.2 new tests, the javadoc rewrites in §3.7 that were not
forced by a deleted class reference, and part 1's phase-2 pointer. Note session 1 already fixed every
javadoc that named a now-deleted class (`IncomingReferenceChanges`, `UnusedElementResourceValidator`) plus
the `computeOutgoingReferences` staleness bullet and closing paragraph, since leaving those would have made
the code lie.

### Session 2 — hardening and full verification (**Sonnet**)

The §4.2 new tests; the javadoc updates listed in §3.7; update part 1's phase-2 section with a pointer
("superseded by part 2's post-build service"). Mechanical work against a design that session 1 has already
proven.

**Acceptance:** full `mvn install` green; both modules' counts recorded; no `rune-integration-tests` change.

### Session 3 — benchmark (**Sonnet**, needs the user)

Requires a CDM checkout and `-Drune.benchmark.model.dir`; ask the user for the path (or to run it) rather
than skipping silently. A/B per §4.4, record the table here next to part 1's tables. If the keystroke row
regresses past 30 ms, profile the sweep first — the union snapshot (§3.5) is the knob, and per-URI diff
short-circuits are the second.

## 6. Risks

- **Publish-path regressions are silent** — they manifest as missing or flickering diagnostics, not
  errors. The §4.2 merged-publish test and the staleness pair are the tripwires; run them after every
  change to the pass, not just at the end.
- ~~**Wrapper/pass double-attach.**~~ Resolved by construction in session 1 — the pass bypasses the wrapper
  (§3.4). The merged-publish test of §4.2 is still worth having as a regression guard.
- **`Issue` equality**: the diff silently never firing (always-equal records) or always firing
  (identity comparison) both produce plausible-looking behaviour locally; the staleness tests catch the
  former, the merged-publish test plus a publish-count assertion catch the latter.
- **Store lifetime**: server-injector singleton means test servers sharing an injector could leak entries
  between cases. §3.3 clears on `initialize`. In practice `AbstractLanguageServerTest#setup` builds a fresh
  injector per test method, so the store is per-test regardless.

## 7. Outstanding review items not covered here

- ~~**Per-kind issue codes.**~~ **DONE (2026-08-13), on the branch.** Taken: the eight per-kind codes and the
  `UNUSED_CODES` set are deleted and every marker carries `UNUSED_DECLARATION`. Part 1's "Review changes"
  section has the reasoning and the verification; the only thing this plan inherits is that §3.6's provider
  supplies one constant rather than a per-kind code, and the merged-publish test of §4.2 can identify unused
  hints by that single code.
- **PR split.** The review asks for #1299 to land as three pieces: generic infrastructure, Rune provider,
  grammar-level suppression. Since this plan deletes `IncomingReferenceChanges` outright, landing part 1's
  version of the infrastructure and then replacing it is wasted review effort — prefer folding this plan
  into the PR before merge so the infrastructure lands once, in its final generic form.
