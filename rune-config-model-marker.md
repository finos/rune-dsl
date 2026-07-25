# Disambiguate the model rune-config via a build-generated marker

## Execution as sessions

The code lands in **two fresh sessions**, one per repo. Note the section numbering here is by
*concern* (Design §1/§2/§3), not by execution order — do **not** treat "§1, then §2, then §3" as
three sessions, and do **not** drive sessions off the Sequencing numbers (those are ship/validate
order, and only steps 1–2 are code).

- **Session 1 — rune-dsl / `rune-maven-plugin`** = Design **§1 + §2 together** (= Sequencing step 1).
  Both sections touch the same three Mojo classes and share the `findConventionalConfigFile`
  helper (§2 introduces it, §1 consumes it), so they are one unit — don't split them. Includes the
  plugin-side test infrastructure and tests (see the "Tests" section's test-infra note). Build:
  `mvn -pl rune-maven-plugin -am install` (Java 21). Do not touch rune-testing.

  > **Status: done (2026-07-24).** Added `ModelPropertiesWriter` (writes
  > `META-INF/rune/model.properties` with `runeConfigPresentInModel`/`runeMavenPluginVersion`) and
  > `AbstractRuneGeneratorMojo.findConventionalConfigFile(File)` per Design §1/§2, both consumed by
  > the reworked `resolveConfig()` (transitional honour-then-warn on `runeConfig`/`rosettaConfig`,
  > convention fallback otherwise). Both parameters now carry identical `@Deprecated`/`@deprecated`
  > convention-based javadoc. `writesModelProperties()` added to `AbstractRuneGeneratorMojo`
  > (default `false`), overridden to `true` only in `RuneGenerateMojo`; `RuneTestGenerateMojo` left
  > at the default, per plan. The marker write is called from `internalExecute()` after the
  > `errorDetected`/`failOnValidationError` check, guarded by `writesModelProperties()`, so a failed
  > build emits no marker. **Deviation from the plan:** the Tests section's test-infra note said to
  > add `junit(-jupiter)` as an explicit test-scope dependency in `rune-maven-plugin/pom.xml`;
  > `mvn dependency:tree` showed junit-jupiter 6.0.1 already resolves there transitively (the root
  > POM declares it in its own `<dependencies>`, not `<dependencyManagement>`, so every module
  > inherits it — the same reason `rune-lang`/`rune-runtime` don't redeclare it either). Followed
  > that existing convention and only added the `src/test/java` root, skipping the redundant pom.xml
  > edit; behaviour is identical. `mvn -pl rune-maven-plugin -am install`: **15 tests, 0 failures, 0
  > errors** (5 in `ModelPropertiesWriterTest`, 10 in `AbstractRuneGeneratorMojoTest`), checkstyle
  > clean. Changes were left uncommitted for review.
  >
  > **Follow-up (2026-07-24).** Added `AbstractRuneGeneratorMojo.warnIfLegacyConfigFileName(File,
  > Consumer<String>)`: when the marker's `writeModelProperties()` finds the conventional config file
  > under the legacy name `rosetta-config.yml` rather than `rune-config.yml`, it now logs a warning
  > telling the model owner to rename it (separate from, and in addition to, the existing
  > `runeConfig`/`rosettaConfig` *parameter* deprecation warning — this one fires regardless of
  > whether a parameter is set, purely off the file name found on disk). Extracted as a pure
  > `Consumer<String>`-sink function, same pattern as `resolveConfig`, so it is unit-testable without
  > the Mojo harness. Three new tests in `AbstractRuneGeneratorMojoTest`. `mvn -pl rune-maven-plugin
  > test`: **18 tests, 0 failures, 0 errors**, checkstyle clean.

- **Session 2 — `rune-testing`** = Design **§3** (= Sequencing step 2). Separate repo. Independent at
  the code level (its marker-reading code depends only on unchanged `rune-runtime` classes, and its
  tests use hand-built marker fixtures), but must **ship as a pair** with Session 1; for end-to-end
  validation, install Session 1's plugin locally first.

  > **Status: done (2026-07-24).** `DefaultModelSerialisation.resolve()` now consults
  > `META-INF/rune/model.properties` per Design §3, including the plugin-version convention check;
  > `maven-artifact` was added as an explicit `rune-testing/pom.xml` dependency (pinned to `3.9.14`,
  > matching the version already pulled in transitively); the five new test cases from the Tests
  > section were added to `DefaultModelSerialisationTest`, existing fixtures were updated to carry a
  > marker (now mandatory), and a `src/test/resources/META-INF/rune/model.properties` fixture
  > (`runeConfigPresentInModel=false`) was added so `TransformTestExtensionDefaultSerialisationTest`
  > and `PipelineTestPackWriterDefaultSerialisationTest` — which resolve via the module's real test
  > classloader, not a synthetic one — keep passing. `rune.dsl.version` was left untouched at
  > `10.3.0` per instruction. `mvn clean install`: **82 tests, 0 failures, 0 errors** (19 in
  > `DefaultModelSerialisationTest`: 13 pre-existing + 6 new). `TransformTestExtension` and
  > `PipelineTestPackWriter` needed no code changes — they only call `resolve()`. Sequencing steps
  > 3–6 (downstream CDM/child-model/tokenovate builds) remain out of scope, as planned.

Each session prompt must state **which repo** it targets (class names overlap across repos — see
"Repositories and modules") and that **Sequencing steps 3–6 are out of scope for it**: those are
downstream builds in CDM, a child model, and tokenovate — repos that are **not checked out** — so a
coding session cannot run them. They are post-release validation for CI or a human once the
dsl/bundle pair is published, not tasks to execute now.

## Context

The default serialisation format lives in a model's `rune-config.yml` (or the legacy
`rosetta-config.yml`), which ships at the root of the model's jar. `rune-testing` locates it by
classpath lookup:

```java
// DefaultModelSerialisation.java:128
private static Optional<URL> findConfigUrl(ClassLoader classLoader) {
    return CONFIG_FILE_NAMES.stream()
            .map(classLoader::getResource)
            .filter(Objects::nonNull)
            .findFirst();
}
```

`getResource` returns the first match in classpath order. A **child model that ships no config of
its own silently picks up its parent's** and adopts the parent's `defaultSerialisationFormat` — so
e.g. a child of CDM inherits CDM's `RUNE_JSON` default without ever having opted in. There is
already a TODO at `DefaultModelSerialisation.java:124` describing the fix.

The fix: have `rune-maven-plugin` emit a per-model marker recording whether *that model* ships its
own config, and have `rune-testing` consult the marker instead of inferring from classpath order.

### Options considered and rejected

An earlier design let each model declare the config's location as a Maven property
(`<modelConfigFile>foo/rune-config.yml</modelConfigFile>`), threaded through the plugin, the marker
file, and rosetta-products' `RuneConfigResolver`. It was dropped: it required changes in every
model pom, in rosetta-products' config resolution and its three config write/copy sites, and in
`rune-ide`'s LSP watched-file registration — all to support relocating a file that no model
actually wants to relocate. A boolean marker solves the ambiguity on its own.

That same conclusion — that no model wants the config anywhere other than the conventional root —
is why this change goes one step further and treats the config's *location* as convention rather
than configuration. The plugin resolves it from `src/main/resources` by convention (Design §2), and
the existing `runeConfig`/`rosettaConfig` path parameters are deprecated rather than a new path key
being added. Every one of the 12 model repos surveyed already points these parameters at exactly the
conventional location, so there is nothing to relocate and no escape hatch to preserve.

## Repositories and modules

This change spans **two repositories**, both added to the session:

- **`rune-dsl`** (`/Users/david.al-kanani/Developer/REGnosys/rune-dsl`) — the DSL SDK. Two of its
  modules are touched/referenced:
  - `rune-maven-plugin` — the Mojo classes edited here. Package
    `com.regnosys.rosetta.maven`, source root
    `rune-maven-plugin/src/main/java/com/regnosys/rosetta/maven/`. Contains
    `AbstractRuneGeneratorMojo`, `RuneGenerateMojo`, `RuneTestGenerateMojo`,
    `RuneLanguageAccessFactory`, and where the new `ModelPropertiesWriter` will be added.
  - `rune-runtime` — referenced only, **not edited**: `RuneConfigurationFileProvider`
    (`com.regnosys.rosetta.config.file`) and `RuneConfigurationService`
    (`com.regnosys.rosetta.config`) both live here.
  - `rune-lang` — referenced only: `RosettaStandaloneSetup` (`com.regnosys.rosetta`).
- **`rune-testing`** (`/Users/david.al-kanani/Developer/REGnosys/rune-testing`) — a **separate
  repo/git project**. The marker-consumer change lives here: `DefaultModelSerialisation`,
  `TransformTestExtension`, `PipelineTestPackWriter` (all under
  `src/main/java/com/regnosys/testing/…`) and their tests under `src/test/java/com/regnosys/testing/…`.

Unless a class is prefixed below with its repo, resolve it via this map. `rosetta-products` and
`rune-ide` are named only to explain why they are out of scope; they are not in this session.

## Scope

**In scope:** `rune-maven-plugin` (in **rune-dsl**: emit the marker; resolve the config by
convention; deprecate the config-path parameters) and **rune-testing** (consume the marker).

**Out of scope, and unchanged:**

- **rosetta-products.** `RuneConfigResolverImpl` is already model-scoped — every lookup is anchored
  at `scCachePathProvider.getLocalPath(modelName, version)` or at
  `ModelInstanceParams.resourceClasspath()`, both of which are that model's own checkout, so a
  parent's config is never in scope. It has no equivalent bug and needs no change. It also has zero
  usages of `TransformTestExtension`, `PipelineTestPackWriter` or `DefaultModelSerialisation`, so
  its `rune-testing` dependency (`dropwizard-common`, `execution-engine`,
  `execution-engine-testing`, `notification-server`) is unaffected.
- **Model poms.** No pom edits are required in this change: convention resolution makes the
  `runeConfig`/`rosettaConfig` parameters redundant (every model already points them at the
  conventional `src/main/resources` location), so builds keep working untouched. Deleting the now-
  redundant parameters from model poms is a follow-up cleanup, not part of this change.
- **`rune-ide`.** `RosettaLanguageServerImpl:81-143` registers watchers by the canonical file names;
  those names do not change.

**Now in scope (previously listed as unchanged):**

- **`AbstractRuneGeneratorMojo.resolveConfig()`** (line 130) is reworked — see Design §2. The
  `runeConfig`/`rosettaConfig` precedence no longer matters because both parameters are deprecated
  together in favour of convention.

## Design

### 1. `rune-maven-plugin` — emit the marker

New `com.regnosys.rosetta.maven.ModelPropertiesWriter`, writing
`META-INF/rune/model.properties` under a given output directory:

```properties
runeConfigPresentInModel=false
runeMavenPluginVersion=10.4.0
```

**`runeConfigPresentInModel`** — does `${project.basedir}/src/main/resources/rune-config.yml` or
`…/rosetta-config.yml` exist **on disk**? A filesystem check scoped to this project, never a
classpath lookup; that is precisely what removes the ambiguity. This check calls the same
`findConventionalConfigFile(basedir)` helper introduced in §2 (`presence == non-null`), so the
marker and the generator's config resolution key off one shared definition of the convention and
can never diverge.

This is deliberately *not* "did `resolveConfig()` return something". The question the marker
answers is narrower and matches what `rune-testing` needs: **does this model's jar carry a rune
config at its root?** A model pointing `runeConfig` at a file outside `src/main/resources`
correctly yields `false`, because such a config would not be classpath-discoverable by
`rune-testing` anyway.

CDM gets `true` with no pom change — `rosetta-source/src/main/resources/rosetta-config.yml` is on
disk.

**`runeMavenPluginVersion`** — call `getVersion()` on the `MojoExecution` field already injected at
`AbstractRuneGeneratorMojo:110-111` (`@Parameter(defaultValue = "${mojoExecution}", readonly =
true) private MojoExecution mojoExecution;`). `MojoExecution.getVersion()` is a public Maven API
method returning the plugin's own version — **verified present** in maven-core. Note: the field is
currently only used for `mojoExecution.getGoal()` (at `AbstractRuneGeneratorMojo:183`); this change
*adds* the `getVersion()` call, it is not calling an existing usage. Used by the convention check in
§3.

**Wiring.** Add `protected boolean writesModelProperties() { return false; }` to
`AbstractRuneGeneratorMojo`, override to `true` in `RuneGenerateMojo` (leave `RuneTestGenerateMojo`
at the default `false`). Call the writer near the end of `internalExecute()` — the method spans
`AbstractRuneGeneratorMojo:156-194` (the plan's earlier "line 190" was the `builder.launch()` line,
not the method end). Place the write **after** generation completes successfully, i.e. after the
`errorDetected`/`failOnValidationError` check (currently ~line 190-194) so no marker is emitted for
a failed build; guard it with `if (writesModelProperties())`. Write against
`getProject().getBuild().getOutputDirectory()`.

Not `getClassOutputDirectory()` — that method is defined on the two concrete Mojos (not on
`AbstractRuneGeneratorMojo`), and in `RuneTestGenerateMojo` it returns
`getProject().getBuild().getTestOutputDirectory()`; a second marker in `test-classes` would compete
on the classpath. Expose the target directory as an overridable `@Parameter` (defaulting to the
build output directory) so it can be redirected in tests.

**Phase.** `generate` runs at `generate-sources`, before `process-resources` populates
`target/classes`. `maven-resources-plugin` does not clear that directory, so a file written at
`generate-sources` survives. This is also why the presence check reads `src/main/resources` rather
than the output directory. CDM invokes `generate` in four profiles
(`rosetta-source/pom.xml:169,234,274,521`); all four write the same value, idempotent.

### 2. `rune-maven-plugin` — resolve the config by convention, deprecate the parameters

Today every model passes its config location explicitly (`<runeConfig>` or the deprecated
`<rosettaConfig>`), and `resolveConfig()` (`AbstractRuneGeneratorMojo:130`, body `126-143`) just
picks between them. **The current method already contains two `getLog().warn(...)` calls** — one
when both parameters are set, one flagging `rosettaConfig` as deprecated in favour of `runeConfig`.
Both of those existing warnings are **replaced** by the single shared warning below (the new stance
is that *neither* parameter should be set). A survey of all 12 model repos found every one points
these parameters at exactly
`src/main/resources/{rune,rosetta}-config.yml` — the conventional root. So the parameter carries no
information the plugin cannot derive itself.

Introduce the shared helper `findConventionalConfigFile(File basedir)` returning the config `File`
or `null`: probe `basedir/src/main/resources/rune-config.yml`, then `…/rosetta-config.yml`, on disk.
This is the **single** definition of the convention, also consumed by the marker presence check in
§1.

Rework `resolveConfig()`:

1. If `runeConfig` or `rosettaConfig` is set, log one **shared** deprecation warning — identical text
   regardless of which parameter was used (the old "use `runeConfig` instead of `rosettaConfig`"
   precedence message is gone, since the recommended state is now *neither*):

   > The `runeConfig`/`rosettaConfig` parameter is deprecated and will be removed in the next major
   > Rune DSL release. The configuration file is now discovered by convention at
   > `src/main/resources/rune-config.yml` (or the legacy `rosetta-config.yml`); remove this
   > parameter and let the plugin locate it automatically.

2. **Transitional behaviour — honour, don't ignore.** If a parameter is set, still return its path
   (preferring `runeConfig` when both are set), so upgrading to this plugin breaks no build. This is
   standard deprecate-then-remove: the warning fires now, the value is still respected until removal.
3. If neither is set, return `findConventionalConfigFile(getProject().getBasedir())` as an absolute
   path (or `null` when no config exists, e.g. rune-fpml).

The resolved absolute path flows through the unchanged pipeline (`RuneLanguageAccessFactory` →
`RosettaStandaloneSetup.setConfigFile` → `RuneConfigurationFileProvider.createFromFile` → primary in
`getResources()`), so the dependency-config union is **byte-for-byte identical** to today for every
model whose parameter already points at the conventional location. The convention path is a
filesystem lookup, not a classpath one, because at `generate-sources` the model's own
`src/main/resources` is not yet copied onto `target/classes` (the same reason the explicit path was
originally required, documented at `RuneConfigurationFileProvider:88-94`).

Add `@Deprecated` + a matching `@deprecated` javadoc tag to **both** the `runeConfig` and
`rosettaConfig` fields. Note the current state (`AbstractRuneGeneratorMojo:62-75`): `rosettaConfig`
is *already* `@Deprecated` (with a "use `runeConfig` instead" javadoc), while `runeConfig` is not
annotated at all. So the concrete edits are: add `@Deprecated`/`@deprecated` to `runeConfig`, and
rewrite `rosettaConfig`'s javadoc so both fields carry the **same** convention-based message. This
makes `maven-plugin-plugin` record identical `<deprecated>` entries in the descriptor (surfaced by `mvn help:describe -Ddetail` and by Maven's build-time deprecated-parameter
warning), reinforcing the runtime log.

**Removal.** Scheduled for the next major dsl/bundle bump: delete both `@Parameter` fields, drop the
deprecation branch, and make convention the only resolution path. After removal a config living
outside `src/main/resources` no longer resolves — no model does this today (see Accepted risks).

### 3. `rune-testing` — consult the marker

Replace `DefaultModelSerialisation.findConfigUrl` (line 128) and its TODO:

1. `classLoader.getResource("META-INF/rune/model.properties")`.
2. **Not found → throw.** The message must name both `rune-maven-plugin` and `rune-testing` and
   state that they move as a pair. That is the diagnostic for the one skew case that survives —
   models bind them to different properties (in CDM, `${rosetta.dsl.version}` at `pom.xml:249` vs
   `${rosetta.bundle.version}` at `pom.xml:390`), so a new `rune-testing` against an old plugin is
   possible by mistake.
3. `runeConfigPresentInModel=false` → legacy `RosettaObjectMapper`, with **no config lookup at
   all**. This is the fix.
4. `=true` → resolve the config **relative to the marker's own container**: strip
   `META-INF/rune/model.properties` off the marker URL, then append `rune-config.yml`, then
   `rosetta-config.yml`. Handles both `jar:file:/…!/` and exploded `file:/…/target/classes/` forms.
   Classpath order therefore picks only the marker, and the marker fully determines everything
   after it — a "marker from the child, config from the parent" cross-wire becomes impossible.
5. Config found → read via `RuneConfigurationService` as today; `JSON`/`RUNE_JSON` mapped as today,
   `IllegalStateException` for any other format as today. Keep the existing lenient handling of a
   *malformed but present* config (lines 138-144) — that is orthogonal to this change.
6. `=true` but no config in that container → throw. That is a broken build, not an opt-out.

**No escape hatch.** The ambiguous fallback is exactly what this change deletes, and dsl and bundle
ship as a pair, so there is no supported configuration that needs one.

**Convention check.** Enumerate all markers with `getResources()` and fail if any *other* marker
declares a `runeMavenPluginVersion` **greater** than the winning one — that means an ancestor was
built with a newer plugin than the model under test, i.e. the team convention that a child's
dsl/bundle version is always ≥ its parent's has been violated. Compare with
`org.apache.maven.artifact.versioning.ComparableVersion` (`maven-artifact` is a small, pure
dependency); if a version is absent or unparseable, skip the check rather than fail, so a malformed
marker cannot block a build.

**Dependency note (rune-testing).** `maven-artifact` is currently on rune-testing's classpath only
**transitively** (`rune-maven-plugin` → `maven-plugin-api` → `maven-artifact`, compile scope), not
as a declared dependency in `rune-testing/pom.xml`. Add it as an **explicit** `<dependency>` in
`rune-testing/pom.xml` rather than relying on the transitive path (that pom already manages a
transitive Maven version — see its comment near line 255 — so pin it consistently there).

This turns the convention from trusted into verified, in a file being written anyway, and it is the
check that generalises beyond this one bug.

## Why the convention makes this sound

Marker presence is monotonic in plugin version. Let `P` be the plugin version that introduces the
marker. A jar carries a marker iff it was built with plugin ≥ `P`. The failure case needs child
< `P` **and** some ancestor ≥ `P`. Since `rune-maven-plugin` is versioned by
`${rosetta.dsl.version}` and the team convention is child dsl ≥ parent dsl, child plugin ≥ parent
plugin ≥ `P` — a contradiction. Under the convention the case is unreachable, which is why no
code-source anchoring off a model class is needed.

## Tests

The four consumer tests below all live in **rune-testing** under
`src/test/java/com/regnosys/testing/…` — `serialisation/DefaultModelSerialisationTest`,
`transform/TransformTestExtensionDefaultSerialisationTest`,
`pipeline/PipelineTestPackWriterDefaultSerialisationTest`,
`pipeline/PipelineFunctionRunnerImplTest` (all confirmed present). They build
synthetic classloaders and need a marker fixture. New cases:

- marker absent → throws, message names both artifacts;
- marker `false` **with a config on the classpath** → legacy mapper (direct regression test for the
  bug);
- marker `true` with configs in two containers → picks the marker's own container;
- an ancestor marker declaring a higher `runeMavenPluginVersion` → throws;
- marker with an absent/unparseable version → check skipped, no failure.

> **Test-infra note (rune-maven-plugin has none today).** `rune-maven-plugin` currently has **no
> `src/test` tree and no test dependencies** — its `pom.xml` declares only `maven-plugin-api`,
> `maven-plugin-annotations`, and `xtext-maven-plugin`, with no JUnit, no Surefire, and no
> `maven-plugin-testing-harness`. The plugin-side tests below therefore require **new test
> infrastructure**. Preferred approach: write the new logic so it is unit-testable as plain Java
> without the Mojo harness — i.e. put the marker-writing in `ModelPropertiesWriter` (takes an output
> `File`/dir + booleans + version string) and the convention lookup in a static
> `findConventionalConfigFile(File basedir)` helper, then test those directly with JUnit against a
> temp directory. This needs only adding `junit`(-jupiter) as a `test`-scope dependency and a
> `src/test/java` root; it avoids pulling in `maven-plugin-testing-harness`. A fresh session must
> add these to `rune-maven-plugin/pom.xml` before the plugin-side tests can compile. Match whatever
> JUnit version the rest of rune-dsl already uses.

Plugin-side (marker): `runeConfigPresentInModel` is `true` for each canonical name present, `false`
when neither is; the marker lands in `outputDirectory` and not in `test-classes`; repeated
`generate` executions are idempotent.

Plugin-side (convention resolution & deprecation): `resolveConfig()` returns the conventional
`src/main/resources/rune-config.yml` when no parameter is set; falls back to the legacy
`rosetta-config.yml`; returns `null` when neither exists; when a parameter *is* set it is still
honoured (with `runeConfig` winning over `rosettaConfig`) **and** logs the shared deprecation
warning. `findConventionalConfigFile` is exercised directly for the present / legacy-only / absent
cases, and is the single code path the marker presence check also relies on.

## Sequencing

The plugin change and the `rune-testing` change must ship in the same dsl/bundle pair —
`rune-testing` hard-fails without a marker, and only the new plugin produces one.

**What a fresh session can do in this session vs. not:** steps 1–2 are the code changes and are
fully actionable here (rune-dsl and rune-testing are both checked out). Steps 3–6 are **downstream
validation builds in other repos (CDM, a child model, tokenovate) that are NOT in this session** —
they are release-validation checkpoints for a human/CI to run after publishing the dsl/bundle pair,
not edits to make now. A fresh session should treat steps 3–6 as a hand-off checklist, not tasks to
execute.

1. `rune-maven-plugin` (so `rune-testing` has something to build against). Build with
   `mvn -pl rune-maven-plugin -am install` (Java 21, per CLAUDE.md).
2. `rune-testing` (separate repo; build/test it there).
3. CDM build on the new pair: confirm `true` + `RUNE_JSON` still resolves exactly as today, and that
   the deprecation warning now fires for CDM's four `<rosettaConfig>` executions while generation
   output is unchanged (each already points at the conventional location).
4. A child-model build: confirm `false` no longer inherits the parent's format.
5. Convention parity: for at least one model, delete the `<runeConfig>`/`<rosettaConfig>` parameter
   and confirm the build resolves the same config file and produces identical generated sources —
   proving the parameter is redundant ahead of its removal.
6. tokenovate `json-schema` profile: this is the one surveyed execution that passes *no* config
   parameter (it currently free-rides on a dependency's config via the classpath at
   `generate-sources`). Under convention it will now resolve tokenovate's own
   `src/main/resources/rosetta-config.yml`. Build that profile and confirm the JSON-schema output is
   correct — this is a genuine behaviour change for that profile (arguably a latent-bug fix), so it
   must be validated rather than assumed. The cheaper alternative is to simply add the missing
   parameter, but convention makes that unnecessary.

Downstream impact is small — CDM has three affected files
(`tests/.../ingest/IngestFpmlConfirmationToTradeStateTest.java`,
`tests/.../ingest/IngestFpmlConfirmationToWorkflowStepTest.java`,
`tests/.../testpack/CdmTestPackCreator.java`), none of which change; they need only a rebuild.
Every model repo already has a `rune-maven-plugin` generate execution, so no repo is left without a
marker.

## Accepted risks

- **Two markers, wrong one wins.** When both child and parent carry markers, classpath order
  decides which is read. Maven orders direct dependencies before transitives and a model's `tests`
  module declares its own jar first (`cdm-java` is first in CDM's `tests/pom.xml`), so the model's
  own marker wins. This is the same assumption the current code already makes for the config file
  itself, so the change does not increase the risk. Only anchoring the lookup to the code source of
  a known model class would close it fully, and that would require making the `defaultSerialisation`
  field lazy in both `TransformTestExtension:86` and `PipelineTestPackWriter:70`. Not worth it.
  **Superseded:** a follow-up design closes this gap without code-source anchoring or consumer
  configuration — see `rune-config-model-ancestry.md` (marker records `modelId`/`parentModels`
  from the `rosetta.parent.*` pom convention; `rune-testing` elects the leaf of the model graph
  instead of trusting classpath order).
- **New `rune-testing` against an old plugin.** No marker anywhere on the classpath → hard fail.
  Loud rather than silent, which is the desired direction; the error message carries the fix.
- **No escape hatch for a relocated config.** Convention is the only lookup, and after the
  parameters are removed (next major dsl bump) a config living outside `src/main/resources` will not
  resolve at all. This is accepted deliberately: the survey found all 12 model repos point at the
  conventional location, and the original "modelConfigFile" design was rejected precisely because no
  model wants to relocate the file. During the deprecation window a parameter is still honoured, so
  a hypothetical relocated config keeps working until removal, giving a full release cycle to
  migrate — after which relocation means moving the file, not re-adding a parameter.
