# Elect the model marker by ancestry, not classpath order

Builds on `rune-config-model-marker.md` (shipped: marker + convention config resolution + marker
consumption). That plan's first accepted risk — **"Two markers, wrong one wins"** — left the winning
marker to classpath order: a consumer pom that lists an ancestor model jar before its own (e.g. via
alphabetical dependency sorting, where `cdm-java` sorts before `rosetta-source`) silently resurrects
the inherited-config bug when plugin versions are equal. This plan closes that gap by making the
winner computable from the model graph itself: each marker records **who it is** and **who its
direct parents are**, and `rune-testing` elects the **leaf** — the marker no other marker claims as
a parent — independent of classpath order and with zero consumer configuration.

> **Revision note (v2).** An earlier revision of this plan wrote the *full recursive closure* of
> ancestors into each marker (`ancestorModels`), crawled at build time via Maven's
> `ProjectBuilder`. That was simplified to **direct parents only** (`parentModels`) once the
> version-sync invariant (below) was made explicit: the closure's only functional payoff was
> bridging a *markerless intermediate* — a state the ecosystem prohibits. The simplification
> deletes the `ProjectBuilder` effective-pom machinery (the most fragile, Maven-version-sensitive
> code), removes staleness (a closure bakes *other repos'* ancestry into your marker at your build
> time; direct parents come from the actual jars on the classpath at election time), and stops
> propagating an ancestor's rotted `rosetta.parent.*` declarations into every descendant's marker.
> Both halves shipped together; the key was renamed before anything was released.

## The version-sync invariant (load-bearing)

A model that declares a parent must be built with a Rune DSL/plugin version **in step with its
consumers** — its generated Java has to stay compatible with the runtime the chain runs on — so
intermediate models cannot lag behind on pre-marker plugin versions. Only **root** models
(rune-fpml, iso20022) may drift behind.

This is what makes direct parents sufficient for election: every non-root model jar on a test
classpath carries a marker contributing its own parent edges, so the "not a leaf" edge that rules
each model out is supplied by the model *directly above* it (fpml is ruled out by CDM's marker, CDM
by DRR's). A markerless jar can only be a root, and a markerless root is invisible to election in a
way that never produces a wrong or ambiguous winner — it contributes no candidate identity, and the
only models it could have ruled out are *below* it, of which a root has none. Dangling
`parentModels` entries pointing at markerless roots simply never match anything.

The invariant is enforced by code compatibility, not by any explicit check (the marker
version-convention check can only see markers that exist) — which is why it is written down here.
If it is ever violated (an intermediate model jar without a marker: a fork built outside the
plugin, a shaded jar with `META-INF/rune` stripped), election degrades **loudly**: two apparent
leaves and an `IllegalStateException` whose message names the markerless-intermediate cause — never
a silently wrong winner.

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

rune-fpml declares none — a parentless (root) model is legitimate and writes an empty
`parentModels`.

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

### 1. `rune-maven-plugin` — write identity + direct parents into the marker

`ModelPropertiesWriter` gains three keys (all additive; v1 keys unchanged):

```properties
runeConfigPresentInModel=true
runeMavenPluginVersion=10.4.0
modelSourceGav=org.finos.cdm:cdm-java:6.23.0
modelId=org.finos.cdm:cdm-parent
parentModels=com.regnosys.rune-fpml:parent
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
- **`parentModels`** — comma-separated GAs (no versions): the model's **direct** model parents.
  Empty (or omitted) for a root model like rune-fpml. *Not* a transitive closure — see the revision
  note; transitive edges come from the intermediate models' own markers at election time.

**Direct parents cost nothing.** The `rosetta.parent.*` properties live in the top-level pom, but
pom inheritance flattens them into the effective model: `getProject().getProperties()` in the
`rosetta-source` execution already contains them, interpolated (`${finos.cdm.version}` → `6.23.0`).
Collecting them is: filter keys by prefix `rosetta.parent.`, group by the middle segment
(`common-domain-model`, `iso-20022`), assemble `{groupId, artifactId, version}` triples, keep the
GA. This is a pure static function over a `Properties` object (`ModelAncestry.parseDirectParents`),
unit-testable without a Mojo harness — no `ProjectBuilder`, no effective-pom building of other
artifacts, no recursion, no cycle guard, no offline-safety argument to maintain.

**Cross-check warning — declared vs. found.** `rosetta.parent.*` is hand-maintained and can rot.
The classpath is ground truth: `RuneGenerateMojo` runs with `requiresDependencyResolution = COMPILE`
and has the classpath elements injected. At marker-writing time, scan the classpath jars for *model
jars* (a jar containing `META-INF/rune/model.properties` or `*.rosetta` entries). Every model jar
whose marker declares a `modelId` must be **accounted for**: either declared as one of this model's
direct parents, or claimed as a parent by *some other classpath model jar's marker* (a transitive
ancestor — fpml on DRR's classpath — is accounted for by CDM's marker, not by DRR's declarations).
Warn otherwise. Markerless model jars are skipped silently: their repo identity is not knowable
from the jar (that would need the pom crawl this revision deleted), and by the invariant only roots
legitimately lack markers. The check stays **presence-driven, never absence-driven**: a
legitimately parentless model (rune-fpml) declares nothing, finds no marked model jars, and stays
silent — there is no "you declared no parents" warning. Warn only; never fail the build on it.

Unchanged from v1: written by `RuneGenerateMojo` only (`writesModelProperties()`), after the
`errorDetected` check, into the build output directory; idempotent across CDM's four profile
executions (all write identical values). Broken ancestry computation degrades the marker (no
`modelId`/`parentModels`, consumers fall back to classpath order), never fails the build.

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
   `parentModels`. Compare GAs exactly (case-sensitive string equality on `groupId:artifactId`).
   The election algorithm is identical to what a closure would need — with direct parents, each
   intermediate marker simply supplies its own edge.
   - **Exactly one leaf** → winner, regardless of classpath order. (Several markers agreeing on one
     `modelId` — the same model twice on the classpath — are one leaf, tie-broken by classpath
     order.)
   - **Multiple leaves** → throw `IllegalStateException` naming the leaves by `modelSourceGav` and
     both possible causes: two independent model graphs on one test classpath (genuinely
     ambiguous), or an intermediate model jar without a marker (an invariant violation — the fix is
     rebuilding that intermediate with a marker-writing plugin).
   - **Zero leaves** (an ancestry cycle — only possible via corrupted markers) → fall back to
     classpath order with a warning, mirroring the "malformed marker cannot block a build" stance
     of the version check.
4. Log the resolution decision at info: *"resolving default serialisation from marker of
   `<modelSourceGav>` (modelId `<modelId>`)"* — this is what turns a mis-ordered pom from invisible
   into spottable even in fallback mode.
5. The plugin-version convention check is kept as-is (orthogonal: it detects version skew, leaf
   election detects order skew). Its error message names markers by `modelSourceGav` when available
   instead of URLs.

**No new consumer API.** `resolve(ClassLoader)` keeps its signature; the expectation is computed
from the markers, not supplied. This is the property that kept the v1 design zero-configuration and
it is preserved here.

## Marker examples

```properties
# rune-fpml (root model)
modelSourceGav=com.regnosys.rune-fpml:rosetta-source:1.2.3
modelId=com.regnosys.rune-fpml:parent
parentModels=

# CDM
modelSourceGav=org.finos.cdm:cdm-java:6.23.0
modelId=org.finos.cdm:cdm-parent
parentModels=com.regnosys.rune-fpml:parent

# DRR (direct parents only - fpml's edge comes from CDM's marker)
modelSourceGav=com.regnosys.drr:rosetta-source:7.0.0
modelId=com.regnosys:drr
parentModels=org.finos.cdm:cdm-parent,org.iso20022:parent
```

Election over {fpml, CDM, DRR}: `rune-fpml:parent` ∈ CDM's list → not a leaf; `cdm-parent` ∈ DRR's
list → not a leaf; `com.regnosys:drr` ∈ nobody's → DRR wins, wherever `cdm-java` sits on the
classpath. If the fpml jar predates markers entirely (a drifted root), it is invisible and the
election result is unchanged.

## Tests

Plugin-side (rune-maven-plugin, extending the existing plain-JUnit infra):

- direct-parent parsing: `rosetta.parent.*` triples grouped correctly; interpolated values; no
  properties → empty list; malformed group (missing artifactId) → warn + skip.
- `modelId`: from module's Maven parent GA; no-parent module falls back to own GA; `modelSourceGav`
  is the module GAV.
- `readJarMarker`: reads identity + parents from a jar's marker; a pre-ancestry marker or a
  markerless jar reads as absent.
- cross-check: undeclared marked model jar → warns; declared → silent; transitive ancestor
  accounted for by its child's marker → silent (the DRR/fpml case); markerless model jar → silent
  (the drifted-root case); **parentless model with no model jars → silent** (the legitimate-root
  case); check never fails the build.
- marker output: new keys present alongside v1 keys; root model writes empty `parentModels`;
  idempotent on repeated executions.

Consumer-side (rune-testing, `DefaultModelSerialisationTest` synthetic-classloader pattern):

- **order-independence regression**: ancestor's marker *first* on the classpath, child's second →
  child still wins (the sortpom scenario — the test v1 could not have).
- **three-model chain**: root → middle → leaf, root's marker first; the leaf's `parentModels` name
  only the middle model, the root is ruled out by the middle's marker — the property direct-parents
  election relies on.
- two independent leaves → throws, names both `modelSourceGav`s.
- any v1 marker present (missing `modelId`) → classpath-order fallback, no throw.
- zero leaves (synthetic cycle) → classpath-order fallback with warning.
- single marker, root model (empty `parentModels`) → wins trivially.
- duplicate markers of the same model → one leaf, not an ambiguity.
- version-convention check still fires under leaf election (orthogonality).
- existing fixtures gain the new keys; the shared
  `src/test/resources/META-INF/rune/model.properties` fixture stays valid either way (single
  marker).

## Sequencing

1. `rune-maven-plugin`. `mvn -pl rune-maven-plugin -am install`.
2. `rune-testing`, against the locally installed plugin.
3. **Downstream (hand-off, not for a coding session):** rebuild the chain bottom-up — fpml, CDM,
   DRR — with the new plugin; confirm each marker's `modelId`/`parentModels` match this doc's
   examples, and that the cross-check warning is silent for all three.
4. DRR order-flip validation: on a scratch branch, move `cdm-java` above `drr:rosetta-source` in
   `tests/pom.xml` and confirm tests still resolve DRR's serialisation (v1 would silently resolve
   CDM's).
5. Verify the iso20022 assumption against its published pom: `org.iso20022:rosetta-source`'s Maven
   `<parent>` is `org.iso20022:parent` (same repo template as CDM/DRR, but not checked out here —
   30-second check, do it before calling the scheme universal).

## Accepted risks and conventions

- **The version-sync invariant is convention, not code.** Election correctness under direct
  parents rests on "a model that declares a parent is built with a marker-writing plugin; only
  roots drift". No check can see a markerless jar's identity, so a violation (fork, shaded jar)
  surfaces as the multi-leaf throw — loud, with both possible causes named in the message — rather
  than being prevented. Documented here because it is load-bearing.
- **`modelId` = the generation module's *direct* Maven parent GA.** Holds for every repo surveyed
  (generation module is a direct child of the repo root). A repo inserting an intermediate parent
  pom between them would record the intermediate and break matching with its children's
  declarations. Documented convention, not code: don't build a parent-chain walker until a repo
  actually needs one.
- **`rosetta.parent.*` is declared intent, hand-maintained.** Mitigated by the build-time
  cross-check warning (trusted → verified, at the desk of the person who can fix it). A repo that
  ignores the warning can still ship incomplete declarations; leaf election then throws on
  ambiguity rather than guessing — loud, with the fix in the message. Rot is contained: a wrong
  declaration only affects the repo that owns it, since nothing copies another repo's declarations
  into this marker any more.
- **GA-only matching, versions ignored.** Dependency mediation can change which version is on the
  classpath, and local SNAPSHOT parents never match declared release versions; version skew is the
  version-convention check's job, not election's.
- **Multiple independent leaves throw.** A deliberate hardening over v1's silent classpath-order
  pick for the same situation. If a legitimate two-leaf classpath ever appears, the escape hatch
  discussion reopens then — not pre-built now.
- **v1 fallback keeps order-dependence alive during transition.** Any v1 marker on the classpath
  disables election. Bounded by rollout: once fpml/CDM/DRR rebuild with the new plugin, election is
  active for that chain; the info-level resolution log makes the interim state observable.
- **Markerless model jars are invisible to the cross-check.** Without the pom crawl there is no
  way to learn a markerless jar's repo identity, so a rotted declaration *for a drifted root*
  goes unwarned until that root rebuilds with a marker. Accepted: the same invariant that makes
  direct parents sufficient makes this the only silent case, and it self-heals on the root's next
  release.
