# Plan part 2: a generic post-build diagnostics service, with unused-elements as its first provider

Status: **COMPLETE** — sessions 1 and 2 (2026-08-13) landed the mechanism swap and the hardening; session 3
(2026-08-14) measured the A/B against part 1 (§4.6) and found the mass-edit case cut in half.
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
   pass republishes the marker-bearing files once (207 functions + 54 other declarations on CDM — §4.5).
   **Correction (review, 2026-08-13): those republishes were being discarded.** The workspace's first build
   runs inside LSP `initialize`, before the client sends `initialized`, and
   `LanguageServerImpl#publishDiagnostics:510` holds every publish behind that future — whose dependents run
   in *reverse* registration order when it completes (verified). So a file published twice in that window —
   once by the build, once by the sweep amending it — reached the client oldest-last and kept the build's
   hint-less answer: **no markers at all after a cold start**, until each file was opened (a `didOpen` build
   re-attaches them, which is why this never showed up in use). No test could see it: every other test goes
   through `initializeContext`, which initializes against an empty directory and writes files afterwards.
   Fixed by `RosettaLanguageServerImpl#initialized` republishing the store's derived half once `super` has
   drained that queue; `UnusedElementColdStartTest` is the regression guard and fails without the fix.
2. **Non-model resources** (empty files, parse wrecks with no `RosettaModel` root): sweep skips them; if a
   URI previously had hints and now has no model, its new list is empty → diff publishes the removal.
3. **Builtins** never produce hints (`isInBuiltinResource`), so they never enter the store and are never
   republished — this preserves part 1 §4.3/§3.9 without any dedicated filter code.
4. **Cancellation self-heals.** Update a URI's store entry only *after* its publish succeeds. A pass
   cancelled mid-way leaves the remaining entries un-updated, so the next completed build's diff finds them
   unequal and republishes. Cancellation *before* the pass skips `afterBuild` entirely — the same exposure
   part 1 had, repaired the same way, and strictly better than part 1's permanently-lost triggers.
5. **The publish path must not throw** (part 1's phase-3 lesson: a throw there surfaces as *missing*
   diagnostics). The generic `markerFor` fallback already covers unknown kinds, and the null-code guard in
   `RosettaLanguageServerImpl#toDiagnostic` stays as is. **Corrected (review, 2026-08-13):** the SPI merely
   *documented* "must not throw" and the service did not enforce it, so a provider that threw took the whole
   build's write request down with it — a provider throwing on every resource makes `initialize` itself fail
   (verified). `republishIfChanged` now catches per resource, logs, and leaves that resource's store entry
   alone so the next build retries; with the catch in place the same throwing provider leaves ordinary
   diagnostics untouched (`ChangeDetectionTest` and `GenerationErrorHandlingTest` stay green).
6. **Clustering does not apply here.** `ProjectManager#doBuild:107` calls the two-argument
   `IncrementalBuilder#build`, which uses `DisabledClusteringPolicy` (`IncrementalBuilder:483`), and nothing
   rebinds `IncrementalBuilder`. `ClusteringStorageAwareResourceLoader#clearResourceSet` therefore never
   runs, so a build never evicts resources from the project resource set and the sweep always sees the whole
   workspace. Part 1's caveat is retired rather than carried forward; the service javadoc states the fact.
7. **`didOpen`-triggered builds** mark the opened file dirty (part 1 §2.3); it gets built and the wrap
   (§3.4) attaches current-store hints to its publish — no behaviour change from today.
8. **Single project, many source folders.** The sweep is per `ProjectManager` resource set. In the
   supported deployment that is one project holding every workspace folder as a source folder, so
   cross-folder references are in the same resource set and are seen. Record this in the service javadoc.

## 4. Verification

Capture test counts before and after in each module (CLAUDE.md rule). Part 1 recorded `rune-ide` 123 /
0 skipped and `rune-integration-tests` 1491 / 21 skipped; both are stale — commits landed on the branch
since. Measured after session 1 (2026-08-13): `rune-ide` 125 / 0 skipped, `rune-integration-tests`
1504 / 21 skipped. Measured after session 2 (2026-08-13): `rune-ide` 128 / 0 skipped,
`rune-integration-tests` 1504 / 21 skipped — unchanged, confirming session 2 touched nothing outside
`rune-ide` (the +3 are exactly the new tests below). **Measured after the review pass (2026-08-13):
`rune-ide` 129 / 0 skipped, `rune-integration-tests` 1504 / 21 skipped — full `mvn install` green with
checkstyle enforced.**

1. `mvn -o test -pl rune-ide -Dtest='UnusedElement*'` — **DONE.** All **70** green with the original 67's
   assertions unchanged (60 validation + 2 staleness + 5 marker), plus the 3 new tests below. The staleness
   pair is the acceptance gate for the whole plan.
2. New tests — **DONE**, all added to `UnusedElementStalenessTest.java` except the third:
   - deleting the file containing the only call site adds the marker to the declaring file; deleting the
     declaring file leaves no stale store entry. **Correction (review, 2026-08-13):** the second half was
     originally asserted as "a later unrelated build does not republish for the deleted URI", which cannot
     fail — `republishIfChanged` is only reached from the sweep over `resourceSet.getResources()`, and a
     deleted resource has left that set, so a leaked entry can never produce a publish. Verified by deleting
     the prune line and watching the test stay green. It now injects `DerivedDiagnosticsStore` and asserts
     `isPublished` directly — true before the delete (which also proves the URI is keyed the way the store
     keys it, so the second assertion is not looking up the wrong key) and false after. Fails without the
     prune.
     **Correction found while writing this test:** a watched-file deletion's own bookkeeping (the delta with
     `getNew() == null`) is not guaranteed to land in the build that reports the deletion — Xtext's clustering
     can carry it into the *next* build instead (§3.8.6's caveat, observed directly here rather than only in
     theory). The test accounts for this with one settling build after the delete before taking its baseline,
     then asserts a second, independent build adds nothing further. Also load-bearing: simulate the deletion
     with `didChangeWatchedFiles`/`FileChangeType.Deleted`, not `deleteFile` + `close()` — the latter's
     deletion detection depends on `WorkspaceManager#exists`, which was observed to still report the file
     present immediately after deletion in this harness, so nothing was noticed until a later, unrelated
     build stumbled onto it. The explicit watched-file event is unconditional and does not have this gap.
   - an edited file's single `publishDiagnostics` contains both its validation issues and its unused hints
     (guards the §3.4 merge). Realised with a file that has both an unused import (a genuine
     `RosettaSimpleValidator#checkImport` warning) and an unused function, asserted right after creation and
     again after an edit that leaves both issues unchanged — the case where the sweep republishes nothing and
     the merged build-time publish is the only one the client ever sees.
   - **`UnusedElementMultiSourceFolderTest`** — a declaration in one source folder referenced from another is
     not flagged, pinning §3.8.8. `SingleProjectWorkspaceConfigFactory` (the production one-project-many-
     source-folders factory) lives in `bsp-server`, outside this repository, so the test binds a small
     equivalent locally: one `FileProjectConfig` with two source folders, via a test-only
     `IMultiRootWorkspaceConfigFactory` bound through a `RosettaServerModule` subclass (the same pattern
     `GenerationErrorHandlingTest` uses to override a binding for one test). Verified the test is not vacuous
     by temporarily breaking the cross-folder call and confirming the marker then does appear.
     **Correction (review, 2026-08-13):** the fixture is not what makes the assertion hold — the harness
     supplies a single workspace folder, so Xtext's default factory produces one project spanning both
     directories too, and the test stays green with the binding removed (verified). Kept as a statement of
     the deployment shape; the javadoc no longer claims otherwise.
3. Full `rune-ide` and `rune-integration-tests` suites; then full `mvn install` (checkstyle enforced) —
   **DONE, green.** `rune-integration-tests` untouched, confirmed by the unchanged 1504/21 count above.
4. `EditLatencyBenchmark` on CDM (`-Drune.benchmark.model.dir=...`), A/B against the part-1 branch, medians
   of 3, same five scenarios. Targets: keystroke ≤ 30 ms (the sweep must not be measurable after §3.5);
   reference-toggle at or below today's 44–48 ms (expect ~30); mass edit **materially** below 620 ms
   (expect ≤ ~300 ms — this case is the reason the plan exists); cold build within +100 ms. **DONE —
   §4.6.** Three of the four targets met; the "not measurable" one is missed by the same 15–25 ms §4.5
   already accepted.

### 4.5 What the sweep costs (measured 2026-08-13, CDM `master` @ dcaa995ee, 145 files)

Not the §4.4 A/B — this is the branch measured against itself with the provider multibinding removed, which
isolates the sweep from every other difference. Medians of 3, `EditLatencyBenchmark`, apply/revert.

| scenario | sweep on | sweep off | sweep costs |
|---|---|---|---|
| cold initial build | 2757 ms | 2753 ms | ~0 |
| edit changing no cross-file reference | 125 / 121 ms | 95 / 109 ms | ~+25 ms |
| edit toggling a function call | 38 / 38 ms | 21 / 20 ms | ~+17 ms |
| edit toggling a type or enum reference | 198 / 201 ms | 185 / 183 ms | ~+15 ms |
| blanking out the widest-fan-out file | 258 / 329 ms | 238 / 295 ms | ~+25 ms |

**Verdict: acceptable, shipped unoptimised.** The sweep adds 15–25 ms to an edit. That is below what anyone
perceives, and typing is debounced before a build is triggered at all, so it is not paid per keystroke.
Against the §4.4 targets the mass edit (258–329 ms vs part 1's 620 ms) and the reference toggle (38 ms vs
44–48 ms) are met and the cold build is unchanged; only "the sweep must not be measurable" is missed, which
is a statement about measurability rather than about anything a user would notice.

Timing the sweep from the inside, per build:

```
total 18–37 ms   snapshot 15–34 ms (~87%)   per-resource compute + diff 2.4–3.4 ms (145 resources)
```

Within the sweep, the union snapshot of §3.5 is essentially the whole cost and the per-candidate half is
free: probing 2 100 declarations against the snapshot and diffing 145 store entries costs under 3.5 ms and
does not move between scenarios. The snapshot stays at 15–17 ms even on the cheapest edit, where only two
files were rebuilt and so only two of the 145 cached reference sets had to be recomputed — that residue is
the union itself, not the walk.

Two properties to keep in view rather than act on:

- It is a **fixed** toll. The snapshot rebuilds a whole-workspace `HashSet` whether one character changed or
  a whole file did, so it is ~10% of the mass edit but ~45% of the cheapest edit (17 ms of 38 ms). Cost that
  does not scale with the edit scales with the workspace instead.
- Only **one** workspace size was measured. O(total references) is what the code's shape implies, not
  something these numbers demonstrate; a second, larger model would be needed to claim a scaling factor.

If a materially larger workspace ever makes this visible, the knob is already known: maintain the reference
counts incrementally, keyed by `ElementId`, instead of rebuilding the set per build. Nothing else in the
sweep is worth touching.

Marker census on CDM, from the same run: `Function=207` (of 1304), `Type=20` (of 760), `Type alias=14` (of
17), `Enumeration=11` (of 279), `Segment=5` (of 15), `Corpus=4` (of 32).

### 4.6 The A/B against part 1 (measured 2026-08-14, CDM `master` @ 2c05b1931, 145 files)

The §4.4 comparison. The part-1 side is the branch's part-1 tip (`6c8a90bf`) with the same `main` merge the
part-2 side carries, built in a throwaway worktree, so the only difference between the two sides is this
plan's work — `6c8a90bf` alone would have carried two unrelated `main` commits as noise. Two runs per side
in alternating order; each cell is `apply / revert`, the harness's own median of 3, run 1 then run 2. The
CDM checkout is one commit past §4.5's `dcaa995ee`, differing by four lines in one of the 145 files.

| scenario | part 1 | part 2 | change |
|---|---|---|---|
| cold initial build | 2761 / 2700 ms | 2714 / 2870 ms | within run-to-run spread |
| edit changing no cross-file reference | 109/110 · 103/106 ms | 121/116 · 125/120 ms | +15 ms |
| edit toggling a function call | 27/30 · 26/29 ms | 36/36 · 38/39 ms | +9 ms |
| edit toggling a type or enum reference | 181/180 · 180/179 ms | 205/192 · 193/198 ms | +15 ms |
| blanking out the widest-fan-out file | 543/637 · 568/622 ms | 251/331 · 259/332 ms | **−290 ms (−48%)** |

**The mass edit, which the plan exists for, is halved** and lands inside the ≤ ~300 ms target; the cold
build is unchanged; the reference toggle at 36–39 ms is under the 44–48 ms target. Only "the sweep must not
be measurable" is missed, by the same fixed 15–25 ms §4.5 measured and accepted.

Two things the numbers say that §4.4 did not anticipate:

- **Part 1's reference toggle re-measures at 26–30 ms, not the 44–48 ms the target was written against.**
  So part 2 is ~9 ms *slower* on that scenario against a baseline measured on the same machine in the same
  session, even though it is comfortably inside the target as stated. Same conclusion as §4.5 — this is the
  snapshot toll, not a regression in the toggle path — but the target's headline figure was flattering.
- **The mass edit publishes to 30 files where part 1 published to 40.** Part 1 revalidated the files whose
  incoming references changed and republished each; the sweep amends only the files whose hints actually
  changed. The other scenarios' fan-out is identical on both sides (2 and 11).

The marker census is identical on both sides (`Function=207`, `Type=20`, `Type alias=14`, `Enumeration=11`,
`Segment=5`, `Corpus=4`) — a behaviour-preservation check on the whole of CDM that the A/B gets for free.

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

### Session 2 — hardening and full verification (**Sonnet**) — **DONE 2026-08-13**

The §4.2 new tests; the javadoc updates listed in §3.7; update part 1's phase-2 section with a pointer
("superseded by part 2's post-build service"). Mechanical work against a design that session 1 has already
proven.

**Acceptance met:** full `mvn install` green; `rune-ide` 128/0 skipped (+3 new tests), `rune-integration-tests`
1504/21 skipped (unchanged) — see §4.

**The §3.7 javadoc rewrites needed no further changes.** Re-checked every file in §3.7's table
(`RosettaStatefulIncrementalBuilder`, `RosettaWorkspaceManager`, `UnusedElementHelper`, `RosettaIdeModule`,
and the three new `server/diagnostics` classes) and grepped the tree for the deleted classes' names and for
phrases session 1's notes called out (`revalidateResourcesWithChangedIncomingReferences`, `isMarkerCapable`,
"would keep this walk") — none remain. Session 1's cleanup was already complete; this session's only
javadoc-adjacent change is part 1's phase-2 pointer (§ above).

**What this session found, not anticipated by the plan:** the deletion-mechanism and clustering-delay
findings recorded under §4.2's first new test. Both were surfaced by writing the test against the real
language server rather than assumed from reading the code.

### Review pass — 2026-08-13, after sessions 1 and 2

Three defects fixed, two documentation claims corrected. Each fix was checked by breaking it again and
confirming the guarding test fails:

- **Cold-start markers were discarded.** §3.8.1. New `UnusedElementColdStartTest`.
- **The deletion test's stale-entry assertion could not fail.** §4.2. Now asserts on the store.
- **A throwing provider took down the build's write request.** §3.8.5. Contained in `republishIfChanged`.
- **Clustering caveat retired** (§3.8.6) and the multi-source-folder fixture's javadoc corrected (§4.2).

`rune-ide` 128 → **129 / 0 skipped** (the cold-start test), `rune-integration-tests` unchanged.

### Session 3 — benchmark (**Sonnet**, needs the user) — **DONE 2026-08-14**

The §4.4 A/B against part 1, on a CDM checkout via `-Drune.benchmark.model.dir`. **Results and method in
§4.6.** The mass edit that motivated the plan drops from 543–637 ms to 251–332 ms; the other scenarios pay
the 15–25 ms sweep toll §4.5 already measured against this branch itself, and the two measurements agree.
No code changed this session, so no test counts moved.

Nothing here changes the "ship unoptimised" call. If a larger workspace ever makes the sweep visible, §4.5
names the one knob worth turning.

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
