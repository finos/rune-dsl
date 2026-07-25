# Elect the model marker by ancestry, not classpath order

Builds on `rune-config-model-marker.md` (shipped: marker + convention config resolution + marker
consumption). That plan's first accepted risk — **"Two markers, wrong one wins"** — left the winning
marker to classpath order: a consumer pom that lists an ancestor model jar before its own (e.g. via
alphabetical dependency sorting, where `cdm-java` sorts before `rosetta-source`) silently resurrects
the inherited-config bug when plugin versions are equal. This plan closes that gap by making the
winner computable from the model graph itself: each marker records **who it is** and **who its
ancestors are**, and `rune-testing` elects the **leaf** — the marker no other marker claims as an
ancestor — independent of classpath order and with zero consumer configuration.

## Execution as one session

Unlike the v1 plan, both halves land in **one fresh session**. What made the v1 two-session split
necessary is gone here: the consumer's tests run against hand-built marker fixtures (no dependency
on real plugin output), and the compatibility gate (Design §2 step 2) means neither half breaks
without the other — a session that finishes one side and stalls on the other leaves nothing broken.
The plan is self-contained: a fresh session needs this document, not the conversation that produced
it.

Rules for the session:

- **Two repos, both checked out, separate git projects.** Work on the **currently checked-out
  `config-model-marker` branch in each repo** — do **not** create new branches; this ancestry work
  stacks on top of the v1 marker work already on those branches. Commits are still per-repo (one
  branch name, two independent git projects):
  - **rune-dsl** (`/Users/davidal-kanani/Developer/rune-dsl`) — Design §1 in `rune-maven-plugin`.
    Extends `ModelPropertiesWriter` and adds the ancestry crawl. Build:
    `mvn -pl rune-maven-plugin -am install` (Java 21).
  - **rune-testing** (`/Users/davidal-kanani/Developer/rune-testing`) — Design §2. Replaces the
    classpath-order winner with leaf election.
- **Strict ordering, never interleaved**: Design §1 to green **and locally installed** first, then
  Design §2. Only one repo is "hot" at a time — this also avoids the overlapping-class-name
  confusion between the repos (both have `serialisation`/`maven` packages and similarly named
  tests). Do not start §2 while §1 has failing tests.
- The marker format (key names `modelSourceGav`/`modelId`/`ancestorModels`, GA-only matching,
  comma-separated closure) is fixed by this document; both halves must use it byte-for-byte —
  do not "improve" a key name on one side only.
- **Sequencing steps 3–5 are out of scope**: downstream rebuilds (fpml, CDM, DRR), the DRR
  order-flip validation, and the iso20022 pom check are hand-off items for a human/CI after the
  pair is published — those repos are not part of the coding task.

Fallback: if the combined run gets heavy (context or build-time), split at the natural seam — ship
§1 complete and committed, and start a second session for §2 with this document; the halves are
independent at the code level, so the split costs nothing but the shared warm context.

## Context

### The residual failure

With v1, `getResource(MODEL_PROPERTIES_PATH)` returns the first marker in classpath order. The
child's marker wins today only because (a) `target/classes` precedes dependency jars in the
generation module, and (b) every surveyed `tests` module happens to declare its own model jar before
its ancestors'. Nothing *verifies* (b); a pom-tidying PR or a `sortpom` adoption flips the winner
silently. The marker GAV alone cannot fix this: the resolver has no independent notion of "the model
under test" — Maven module identity does not exist at test runtime (Surefire runs against an
unpackaged `target/test-classes` with no `META-INF/maven`, and even the packaged tests jar's GAV is
`org.finos.cdm:tests`, not `cdm-java`). Identity without an expectation decides nothing; the
expectation must be *computable*, and the model graph is what makes it computable.

### The existing `rosetta.parent.*` convention

Model repos already declare their model parents as properties in the **top-level pom**, one GAV per
parent (verified in DRR and CDM checkouts):

```xml
<!-- digital-regulatory-reporting/pom.xml:67-72 -->
<rosetta.parent.common-domain-model.groupId>org.finos.cdm</rosetta.parent.common-domain-model.groupId>
<rosetta.parent.common-domain-model.artifactId>cdm-parent</rosetta.parent.common-domain-model.artifactId>
<rosetta.parent.common-domain-model.version>${finos.cdm.version}</rosetta.parent.common-domain-model.version>
<rosetta.parent.iso-20022.groupId>org.iso20022</rosetta.parent.iso-20022.groupId>
<!-- ... -->
```

```xml
<!-- common-domain-model/pom.xml:129-131 -->
<rosetta.parent.rune-fpml.groupId>com.regnosys.rune-fpml</rosetta.parent.rune-fpml.groupId>
<!-- ... -->
```

rune-fpml declares none — a parentless (root) model is legitimate and terminates the crawl
naturally.

### The identity alignment that makes matching work

`rosetta.parent.*` points at the ancestor repo's **root/parent pom** GA (`org.finos.cdm:cdm-parent`),
while the marker is written in the generation module, whose own GAV is the **source module**
(`org.finos.cdm:cdm-java`). These never string-match. But the generation module's direct Maven
`<parent>` **is** the repo root pom — verified: `cdm-java`'s `<parent>` is `org.finos.cdm:cdm-parent`
(`common-domain-model/rosetta-source/pom.xml:5-9`), DRR `rosetta-source`'s is `com.regnosys:drr` —
i.e. exactly the GA a child declares for that ancestor. So the marker records its repo identity as
`getProject().getParent()`'s GA, and both sides of the match use the same coordinate system.
Group-only matching was rejected: DRR's groupId is plain `com.regnosys`, shared with rosetta-common
and other non-model REGnosys artifacts.

## Design

### 1. `rune-maven-plugin` — write identity + ancestry into the marker

`ModelPropertiesWriter` gains three keys (all additive; v1 keys unchanged):

```properties
runeConfigPresentInModel=true
runeMavenPluginVersion=10.4.0
modelSourceGav=org.finos.cdm:cdm-java:6.23.0
modelId=org.finos.cdm:cdm-parent
ancestorModels=com.regnosys.rune-fpml:parent
```

- **`modelSourceGav`** — the generation (source) module's own `groupId:artifactId:version`, from
  `getProject()`. **Diagnostics only, never a matching key** (it is the source sub-module
  coordinate, hence the name): it appears in log/error text so failures name real artifacts instead
  of URLs.
- **`modelId`** — the repo identity: `getProject().getParent()`'s `groupId:artifactId`. **GA only,
  no version** — the local parent version is `0.0.0.master-SNAPSHOT` while a child declares the
  release number, so including version would break exactly the local-development builds used for
  validation. If the module has no parent pom, fall back to the module's own GA (children of such a
  model would declare that same GA, keeping the scheme consistent).
- **`ancestorModels`** — comma-separated GAs (no versions): the **full recursive closure** of
  ancestor model repo identities. Empty (or omitted) for a root model like rune-fpml.

**Direct parents cost nothing.** The `rosetta.parent.*` properties live in the top-level pom, but
pom inheritance flattens them into the effective model: `getProject().getProperties()` in the
`rosetta-source` execution already contains them, interpolated (`${finos.cdm.version}` → `6.23.0`).
Collecting direct parents is: filter keys by prefix `rosetta.parent.`, group by the middle segment
(`common-domain-model`, `iso-20022`), assemble `{groupId, artifactId, version}` triples. Keep this
as a pure static function over a `Properties` object so it is unit-testable without a Mojo harness
(same pattern as `findConventionalConfigFile`).

**Recursion via `ProjectBuilder`.** For each direct-parent GAV, build the *effective* pom of that
parent artifact (`org.apache.maven.project.ProjectBuilder`, injected `@Component`;
`session.getProjectBuildingRequest()` with `setResolveDependencies(false)` and
`setProcessPlugins(false)`), read *its* `rosetta.parent.*` properties, recurse. Effective-model
building is required — CDM's declared value is `${rune-fpml.version}`, which only interpolates in
the built model, not a raw XML parse. Guards: a visited set on GA (cycle guard); warn-and-skip on
an unresolvable pom (a broken crawl must degrade the marker, never fail the build). **No new remote
fetches**: each parent pom is guaranteed present in the local repo already, because it is the Maven
`<parent>` of a model jar Maven has just resolved (it downloaded `cdm-parent`'s pom to build
`cdm-java`'s effective pom). The crawl is offline-safe. Versions from the properties are used only
to *resolve* parent poms during the crawl; `ancestorModels` entries are written GA-only.

Keep the recursion behind a small seam (e.g. a `ParentPomLoader` functional interface the
`ProjectBuilder`-backed implementation satisfies) so closure/cycle/skip logic is unit-testable with
fixture loaders, without a Maven runtime.

**Why full closure rather than direct parents only.** Direct parents would suffice if every
intermediate ancestor on the classpath carried a marker (the resolver could chain them). The closure
buys robustness against a **markerless intermediate**: if `cdm-java` predates markers but the fpml
jar does not, direct-parents-only leaves both DRR and rune-fpml looking like leaves (ambiguous),
while DRR's closed list (`cdm-parent, parent(iso), rune-fpml:parent`) still elects DRR uniquely. The
crawl is cheap and offline, so write the closure.

**Cross-check warning — declared vs. found.** `rosetta.parent.*` is hand-maintained and can rot.
The classpath is ground truth: `RuneGenerateMojo` runs with `requiresDependencyResolution = COMPILE`
and has the classpath elements injected. At marker-writing time, scan the classpath jars for *model
jars* (a jar containing `META-INF/rune/model.properties` or `*.rosetta` entries); for each one
found, resolve its repo identity (its marker's `modelId` when present; otherwise
`ProjectBuilder`-build the dependency's pom and take its Maven parent GA — same loader as the
crawl) and **warn if that GA is not in the declared ancestor closure**. The check is
**presence-driven, never absence-driven**: a legitimately parentless model (rune-fpml) declares
nothing, finds no model jars, and stays silent — there is no "you declared no parents" warning.
Warn only; never fail the build on it.

Unchanged from v1: written by `RuneGenerateMojo` only (`writesModelProperties()`), after the
`errorDetected` check, into the build output directory; idempotent across CDM's four profile
executions (all write identical values).

### 2. `rune-testing` — leaf election

Rework the winner selection in `DefaultModelSerialisation.resolve()` (everything *after* the winner
is chosen — container-anchored config resolution, format mapping, lenient malformed-config handling
— is unchanged):

1. Enumerate **all** markers via `getResources(MODEL_PROPERTIES_PATH)` (already done for the
   version-convention check) and parse each.
2. **Compatibility gate**: if **any** marker lacks `modelId` (a v1 marker), fall back to today's
   behaviour — the first marker in classpath order wins — and log at debug that leaf election was
   skipped because a pre-ancestry marker is present. This keeps the new consumer working against
   models built with the v1 plugin, unlike the v1 hard-fail pairing.
3. Otherwise elect the **leaf**: the marker whose `modelId` appears in **no other** marker's
   `ancestorModels`. Compare GAs exactly (case-sensitive string equality on
   `groupId:artifactId`).
   - **Exactly one leaf** → winner, regardless of classpath order.
   - **Multiple leaves** → throw `IllegalStateException` naming the leaves by `modelSourceGav`:
     two independent model graphs on one test classpath is genuinely ambiguous, and today's
     classpath-order answer for that case is silent luck, not semantics.
   - **Zero leaves** (an ancestry cycle — only possible via corrupted markers) → fall back to
     classpath order with a warning, mirroring the "malformed marker cannot block a build" stance
     of the version check.
4. Log the resolution decision at info: *"resolving default serialisation from marker of
   `<modelSourceGav>` (modelId `<modelId>`)"* — this is what turns a mis-ordered pom from invisible
   into spottable even in fallback mode.
5. The plugin-version convention check is kept as-is (orthogonal: it detects version skew, leaf
   election detects order skew). Its error message should now name markers by `modelSourceGav`
   when available instead of URLs.

**No new consumer API.** `resolve(ClassLoader)` keeps its signature; the expectation is computed
from the markers, not supplied. This is the property that kept the v1 design zero-configuration and
it is preserved here.

## Marker examples

```properties
# rune-fpml (root model)
modelSourceGav=com.regnosys.rune-fpml:rosetta-source:1.2.3
modelId=com.regnosys.rune-fpml:parent
ancestorModels=

# CDM
modelSourceGav=org.finos.cdm:cdm-java:6.23.0
modelId=org.finos.cdm:cdm-parent
ancestorModels=com.regnosys.rune-fpml:parent

# DRR (closure, not just direct parents)
modelSourceGav=com.regnosys.drr:rosetta-source:7.0.0
modelId=com.regnosys:drr
ancestorModels=org.finos.cdm:cdm-parent,org.iso20022:parent,com.regnosys.rune-fpml:parent
```

Election over {CDM, DRR}: `cdm-parent` ∈ DRR's list → not a leaf; `com.regnosys:drr` ∈ nobody's →
DRR wins, wherever `cdm-java` sits on the classpath.

## Tests

Plugin-side (rune-maven-plugin, extending the existing plain-JUnit infra):

- direct-parent parsing: `rosetta.parent.*` triples grouped correctly; interpolated values; no
  properties → empty list; malformed group (missing artifactId) → warn + skip.
- closure via a fixture `ParentPomLoader`: DRR→CDM→fpml chain; markerless-intermediate scenario
  still yields full closure; cycle → terminates with visited-set; unresolvable parent → warn +
  partial closure, build succeeds.
- `modelId`: from module's Maven parent GA; no-parent module falls back to own GA; `modelSourceGav`
  is the module GAV.
- cross-check: undeclared model jar on classpath → warns; declared → silent; **parentless model
  with no model jars → silent** (the legitimate-root case); check never fails the build.
- marker output: new keys present alongside v1 keys; root model writes empty `ancestorModels`;
  idempotent on repeated executions.

Consumer-side (rune-testing, `DefaultModelSerialisationTest` synthetic-classloader pattern):

- **order-independence regression**: ancestor's marker *first* on the classpath, child's second →
  child still wins (the sortpom scenario — the test v1 could not have).
- two independent leaves → throws, names both `modelSourceGav`s.
- any v1 marker present (missing `modelId`) → classpath-order fallback, no throw.
- zero leaves (synthetic cycle) → classpath-order fallback with warning.
- single marker, root model (empty `ancestorModels`) → wins trivially.
- version-convention check still fires under leaf election (orthogonality).
- existing fixtures gain the new keys; the shared
  `src/test/resources/META-INF/rune/model.properties` fixture stays valid either way (single
  marker).

## Sequencing

1. `rune-maven-plugin` (Session 1). `mvn -pl rune-maven-plugin -am install`.
2. `rune-testing` (Session 2), against the locally installed plugin.
3. **Downstream (hand-off, not for a coding session):** rebuild the chain bottom-up — fpml, CDM,
   DRR — with the new plugin; confirm each marker's `modelId`/`ancestorModels` match this doc's
   examples, and that the cross-check warning is silent for all three.
4. DRR order-flip validation: on a scratch branch, move `cdm-java` above `drr:rosetta-source` in
   `tests/pom.xml` and confirm tests still resolve DRR's serialisation (v1 would silently resolve
   CDM's).
5. Verify the iso20022 assumption against its published pom: `org.iso20022:rosetta-source`'s Maven
   `<parent>` is `org.iso20022:parent` (same repo template as CDM/DRR, but not checked out here —
   30-second check, do it before calling the scheme universal).

## Accepted risks and conventions

- **`modelId` = the generation module's *direct* Maven parent GA.** Holds for every repo surveyed
  (generation module is a direct child of the repo root). A repo inserting an intermediate parent
  pom between them would record the intermediate and break matching with its children's
  declarations. Documented convention, not code: don't build a parent-chain walker until a repo
  actually needs one.
- **`rosetta.parent.*` is declared intent, hand-maintained.** Mitigated by the build-time
  cross-check warning (trusted → verified, at the desk of the person who can fix it). A repo that
  ignores the warning can still ship an incomplete closure; leaf election then throws on ambiguity
  rather than guessing — loud, with the fix in the message.
- **GA-only matching, versions ignored.** Dependency mediation can change which version is on the
  classpath, and local SNAPSHOT parents never match declared release versions; version skew is the
  version-convention check's job, not election's.
- **Multiple independent leaves throw.** A deliberate hardening over v1's silent classpath-order
  pick for the same situation. If a legitimate two-leaf classpath ever appears, the escape hatch
  discussion reopens then — not pre-built now.
- **v1 fallback keeps order-dependence alive during transition.** Any v1 marker on the classpath
  disables election. Bounded by rollout: once fpml/CDM/DRR rebuild with the new plugin, election is
  active for that chain; the info-level resolution log makes the interim state observable.
