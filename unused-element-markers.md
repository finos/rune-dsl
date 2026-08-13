# Plan: extend "unused" editor markers from functions to every named root element

Status: **all five phases done** (phases 3 and 4 on 2026-07-30, phase 5 on 2026-07-30), plus three
post-phase-5 hardening changes and one known limitation — see "Post-phase-5 changes" after the Phase 5
section. Branch: `unused-functions-editor-only`.

**The one open item from phase 4 is now closed.** `RosettaTypeAlias` can carry `[suppressUnused]` — see
"Phase 5" below for what shipped and the Xtext code-generator bug that the naive one-line version of this
change ran into.

Scope as shipped in phase 3: `Data` (incl. `Choice`), `RosettaEnumeration`, and `RosettaRule` — both
`reporting rule` and `eligibility rule`.

**Scope from phase 4 onwards (2026-07-30 decision):** *every* `RosettaRootElement` that carries a name,
i.e. every one whose EMF supertypes include `RosettaNamed`. That is a rule rather than a list, so a new
grammar root element is a candidate by default instead of a silent omission. Concretely it adds
`RosettaTypeAlias`, `Annotation`, `Schema`, `RosettaExternalRuleSource`, `RosettaBody`, `RosettaCorpus`,
`RosettaSegment`, `RosettaBasicType`, `RosettaRecordType` and `RosettaExternalFunction`
(`library function`) — see the table in phase 4. Two elements stay out, for reasons that are *not*
policy choices:

- `RosettaReport` — has no name at all, so the rule excludes it and there is nowhere to put the marker.
- `RosettaMetaType` — is named, but no cross-reference to one exists anywhere in the model; it is
  resolved by name through the index (3b.2), so flagging it would be a 100% false positive.

**No grammar changes were made in phases 3-4 to widen `[suppressUnused]`.** Phase 5 makes exactly one such
change — `RosettaTypeAlias` — to fix the noise measured in 4.6; kinds that still cannot be annotated stay
un-suppressable, and that remains accepted rather than an oversight (3b.3).

## 1. Where we are

The branch marks uncalled functions as faded in the editor only. **The file names in this table are the
pre-phase-3 ones**; phase 3 renamed `UnusedFunctionHelper` → `UnusedElementHelper`,
`UnusedFunctionResourceValidator` → `UnusedElementResourceValidator`, and the two test classes to match.
Sections 1 to 4 below are kept as written at the time, so read them as the record of the reasoning rather than
as a description of the current tree.

| File | Role |
|---|---|
| `rune-lang/.../validation/UnusedFunctionHelper.java` | detection — walks the live AST, caches referenced URIs per resource |
| `rune-ide/.../validation/UnusedFunctionResourceValidator.java` | emits `FeatureBasedDiagnostic`s, bound only in the IDE injector |
| `rune-ide/.../RosettaIdeModule.java` | `bindIResourceValidator()` — editor-only binding |
| `rune-ide/.../server/RosettaLanguageServerImpl.java` | `toDiagnostic` → `Hint` + `DiagnosticTag.Unnecessary` |
| `rune-lang/.../validation/RosettaIssueCodes.java` | `UNUSED_FUNCTION` |
| `rune-runtime/src/main/resources/model/annotations.rosetta` | `[suppressUnused]` opt-out |

Deliberately not a validator `@Check`, so batch builds and `ValidationTestHelper` are unaffected.
`assertNoIssues()` filters severity ≤ `Warning`, so Hints do not fail existing tests.

## 1a. Why `Hint` severity, not a validation `warning()`

This is the founding decision of the whole branch — everything in section 1's file table follows from it —
made across three early commits, before any of the "unused element" generalisation existed:
`4fd18397` (first cut: a `warning()` `@Check` with `[suppressWarnings unused]` as the opt-out), `7db19004`
(severity dropped to `Hint`, opt-out still a warning-suppression flag), `b6a66543` (moved out of validation
entirely into the language-server layer, `[suppressUnused]` introduced as its own annotation). The PR this
branch produces (#1299) is explicitly framed as *the alternative* to #1298, which took the `warning()` path
and stopped there.

**The reasoning, as it stood at `b6a66543` and is still current:**

1. **A validation `warning()` is a build-time correctness signal; this marker is not one.** An unreferenced
   function/type/rule is not necessarily wrong — it may be public API for a downstream consumer, generated
   code, or a model still being written. Treating it as a warning conflates "the model is invalid" with "the
   editor has an opinion about this declaration," and the latter belongs in the editor layer, not
   `rune-lang`'s validator set that `mvn test`/CI/`ValidationTestHelper` all share.
2. **A warning is an `Issue`; a `Hint` produced outside `IResourceValidator` is not.** Keeping it off the
   `@Check` path means `assertIssues`/`assertNoIssues` never see it (assertNoIssues filters severity ≤
   `Warning` — see above), so the entire existing validator suite needed zero fixture edits. The warning-based
   first cut (`4fd18397`) is what the "3/3 pass … 263 run, 0 failures" contrast in the PR body's Tests section
   is measured against: the equivalent warning-based change failed 29 fixtures that asserted exact issue
   lists, purely from the new warning appearing.
3. **`Hint` + `DiagnosticTag.Unnecessary` is a standard LSP idiom for exactly this shape of feedback** — Monaco
   and VS Code render it as faded/greyed text, not a squiggle or a Problems-panel entry, with no client-side
   work. That rendering is the actual UX goal ("fade dead code"), and severity is the mechanism LSP exposes to
   ask for it — a warning would additionally show up in the Problems panel and in build logs, which is not
   part of the goal and actively unwanted for anything that isn't a real defect.
4. **The decision was load-bearing for everything phases 3-5 did later, not just for functions.** Scope grew
   from "functions only" to every named root element (phase 4), including several kinds that can never carry
   `[suppressUnused]` because the grammar has no annotation slot for them (`RosettaRule`,
   `RosettaExternalRuleSource`, `RosettaBody`/`Corpus`/`Segment`, `RosettaBasicType`, `RosettaRecordType`,
   `RosettaExternalFunction`, `Annotation` — see 3b.3), and the noise census in 4.6 found one kind
   (`RosettaTypeAlias`, before phase 5's grammar change) flagged at 82% in a real published model (CDM). Under
   a warning, that combination — un-suppressible kinds plus a genuinely noisy one — would have meant either a
   build-breaking warning on any library model that publishes a type-alias vocabulary, an annotation library,
   or documentation elements with no report, or a second suppression mechanism invented just to route around
   it. Under `Hint`, both are simply advisory and the branch could widen scope five times over without a single
   CI or fixture consequence. This is the connection between the founding decision and section 4.3's
   "excluding the builtins is required, not optional" — the builtins argument is about *false positives being
   permanent*; this section is about *why a permanent false positive is tolerable at all* only because the
   signal is a `Hint`.
5. **The trade-off accepted in exchange:** no CI "fail the build on dead code" gate exists, and none is
   provided by this branch. That is intentional — see item 1 of the PR body's "Why this shape" note. A
   CI-enforced dead-code gate is a different feature (opt-in, almost certainly warning- or error-severity, and
   probably wanting the transitivity and index-primary detection this plan explicitly declines in section 6)
   and should be designed separately rather than retrofitted onto this marker by raising its severity.

## 2. Findings from investigation (2026-07-28)

### 2.1 The Xtext index already contains these references

`IDefaultResourceDescriptionStrategy` has two independent methods:

- **`createEObjectDescriptions`** — exported *names*, feeding global scoping. The only one
  `RosettaResourceDescriptionStrategy` overrides. Its `RosettaExpression` / `Attribute` /
  `RosettaRule` → `return false` pruning lives here.
- **`createReferenceDescriptions`** — the cross-reference records, feeding the builder's dependency
  graph and find-references. **Not overridden.** `DefaultResourceDescription.computeReferenceDescriptions`
  uses its own traversal, pruned only by that method's return value (default: always `true`), and calls
  `EcoreUtil2.resolveLazyCrossReferences` first.

Verified by dumping `IResourceDescription.getReferenceDescriptions()` for a three-file model. For a file
using `a.Foo`, `a.Bar` and `a.F` from another namespace, the index contains `TypeCall.type → a.Foo`,
`TypeCall.type → a.Bar`, and `RosettaSymbolReference.symbol → a.F` — a **function call from inside an
expression body**, with `containerEObjectURI` set to the enclosing `Function`.

The javadoc on `UnusedFunctionHelper.getReferencedFunctionUris` therefore states an incorrect reason
("`RosettaResourceDescriptionStrategy` does not descend into expressions"). The conclusion — use the live
AST — is still defensible, but not for that reason. See 2.2.

**Never** un-prune `createEObjectDescriptions` for expressions: that would export expression internals as
globally-named objects and change name resolution, and would break the `AttributeDescription` /
`RuleDescription` user-data design that `ChangeDetectionTest` covers.

### 2.2 The index is cross-resource only

`DefaultResourceDescriptionStrategy.isResolvedAndExternal` ends with
`return from.eResource() != to.eResource();`. Same-file references are never indexed — confirmed: a file
declaring *and* using `LocalType`, `LocalEnum`, `LocalFunc` internally produced zero reference
descriptions for them.

So an index-based usage query must be a hybrid — index for other files, live-AST walk for the candidate's
own resource. This is exactly what `org.eclipse.xtext.findReferences.ReferenceFinder` does
(`findAllReferences` iterates `indexData.getAllResourceDescriptions()`; `findReferences` loads the real
resource when the target lives in it). Also note `IResourceDescriptions` / `ResourceDescriptionsData`
offer **no target→sources reverse lookup**.

### 2.3 The marker is stale across files (existing bug — fixed by phase 2)

Covered by `rune-ide/.../validation/UnusedFunctionStalenessTest.java` (`@Disabled`; remove the
annotations as the acceptance criteria). **Both tests now pass and are enabled — see phase 2.**

| Edit in `caller.rosetta` | `caller.rosetta` | `decl.rosetta` |
|---|---|---|
| add first call `1` → `decl.F()` | revalidated | **keeps** "Function 'F' is never used" |
| remove last call `decl.F()` → `1` | revalidated | **never gains** the marker |

The language server only revalidates *affected* resources (`IncrementalBuilder` builds `toBeBuilt` from
`Indexer.computeAndIndexAffected`). `decl.rosetta` neither changed nor references `caller.rosetta`, so
`DefaultResourceDescriptionManager.isAffected` returns false.

This is orthogonal to index-vs-live-AST — it is about which files get revalidated at all. It matters far
more for types and enums than for functions, because cross-file type references are the norm rather than
the exception: shipping 3 on top of a broken 2.3 means routinely greying out types that are in use.

**How much this is felt depends on the client, and the plan overstated it (noted 2026-07-29).** `didOpen` is
itself a build trigger: `LanguageServerImpl.didOpen` → `WorkspaceManager.didOpen` →
`didChangeFiles([uri], [])`, which marks the file **dirty** even though its content matches the index, and
`IncrementalBuilder.launch` unloads dirty files before building. So opening a file discards its caches and
revalidates it against current state — the same clean slate phase 2 arranges by hand. A client that sends
`didOpen` when a file becomes the active document therefore self-heals a stale marker the moment the user
looks at it, and the staleness window shrinks to "the marker is on screen while another file is edited".

Phase 2 still covers what `didOpen` cannot: a client that keeps Monaco models for the session and only opens
each file once (tab switch ≠ `didOpen`); workspace mutation with no `didOpen` at all (file deletion, model or
branch sync, bulk import — note `super.didChangeWatchedFiles` skips open documents, so affected-but-unopened
files are exactly the ones left stale); and any view that aggregates diagnostics across files. Whether the
Rosetta client behaves the first way is **not yet verified** — that is the single fact that decides how much
2.3 mattered. Until it is, read "hard prerequisite" below as "belt and braces".

**Verified 2026-08-01: the Rosetta client is the second kind, so "hard prerequisite" was correct.** It
creates a Monaco model for *every* workspace item once, at workspace load (`createWorkspaceItems` →
`createMonacoModels` in `ui/.../workspace/store/effects/workspace-utils.effects.ts`), which is when
`didOpen` is sent; a tab switch is `editor.setModel(existingModel)` (`ui/.../textual/services/editor.service.ts:81`)
and sends nothing. So there is no self-heal-on-view — but there is also nothing unopened: every file stays
an open document for the whole session, so whenever the build revalidates a file its diagnostics are pushed
to a live document immediately, and the phase-2 pass is the only thing that puts the *declaring* file into
that revalidated set. The UI never sends `didChangeWatchedFiles` (external mutations — file add/delete,
model sync — arrive as model create/dispose/`setValue`, i.e. `didOpen`/`didClose`/`didChange`), so the
"skips open documents" subtlety above never comes into play. See the Post-phase-5 section for the
companion finding that the workspace is single-project.

## 3. Reference shapes for types and enums

A function has exactly one usage shape (`RosettaSymbolReference.symbol`). Types and enums have at least
twelve, spread across `rune-lang/src/main/java/com/regnosys/rosetta/Rosetta.xtext`:

| Reference | Line |
|---|---|
| `TypeCall.type` — attribute types, function in/out, choice options, constructors, report/rule `from` | 206 |
| `Data.superType` (`extends`) | 67 |
| `RosettaEnumeration.parent` (`extends`) | 92 |
| `RosettaEnumValueReference.enumeration` (`Enum -> VALUE`) | 143 |
| `ToEnumOperation.enumeration` (`to-enum`) | 544, 582, 621 |
| `AsOperation.type` (`as Foo`) | 550, 588, 627 |
| `SwitchCaseGuard.referenceGuard` (`switch` case) | 515 |
| `RosettaReport.reportType` (`with type`) | 729 |
| `RosettaExternalClass.data` (rule source) | 693 |
| `RosettaQualifiableConfiguration.rosettaClass` (`isEvent root`) | 391 |
| `RosettaDataReference.data` (annotation paths) | 290 |
| `RosettaSymbolReference.symbol` — `RosettaEnumeration` is a `RosettaSymbol` | 336 |

Hand-enumerating these is a maintenance trap: any new grammar rule silently reintroduces false
positives. Hence step 3.2 below. (Section 3b applies the same argument to the *candidate* list, which phase 4
replaces with a rule for the same reason.)

## 3a. Reference shapes for rules (investigation 2026-07-28)

`RosettaRule` (`Rosetta.xtext:737`, `Rosetta.xcore:354`) covers both kinds — `eligibility` is just a
boolean flag on the same class, so both share one code path. Four usage shapes:

| Reference | Where | Notes |
|---|---|---|
| `RosettaReport.eligibilityRules` (`report … when R and S`) | `Rosetta.xtext:728` | the *only* shape that uses an eligibility rule |
| `RuleReferenceAnnotation.reportingRule` (`[ruleReference R]`) | `Rosetta.xtext:734` | contained in `Attribute.ruleReferences` (`Rosetta.xtext:82,88`) → rolls up to the enclosing `Data` |
| `RuleReferenceAnnotation.reportingRule` inside a rule source | via `RosettaExternalRegularAttribute.externalRuleReferences`, `Rosetta.xtext:702` | rolls up to the enclosing `RosettaExternalRuleSource` |
| `RosettaSymbolReference.symbol` | `Rosetta.xtext:336` | **`RosettaRule extends RosettaCallableWithArgs extends RosettaSymbol`** — a rule can be invoked from an expression like a function. Real usage: `RosettaRuleGeneratorTest.java:80` (`then extract BarQuxReport { bazQux1: QuxQux1, … }`) |

**All four fall out of 3.2 with no new code.** The last one is already handled by today's helper
mechanically (it matches `RosettaSymbolReference`), but today it filters on `instanceof Function`, so a
rule→rule call is currently invisible; the generic walk fixes that for free.

### 3a.1 The blocker: `RosettaRule` is not `Annotated`

> **Not done, and now decided against.** Phase 3 shipped rules without an opt-out, and the 2026-07-30 scope
> decision generalised that to a standing policy: no grammar change to widen `[suppressUnused]` to any kind.
> Read the rest of this section as the costing exercise it was — it remains accurate, and it is the recipe if
> the policy is ever reversed. 3b.3 is the current statement of which kinds are suppressible.

`[suppressUnused]` **cannot be written on a rule today.** `Data` and `RosettaEnumeration` get
`Annotated` via `RootElement` (`RosettaSimple.xcore:32`), but `RosettaRule extends RosettaRootElement,
RosettaCallableWithArgs, RosettaDefinable, References` — no `Annotated` — and the grammar rule has
`References*` but no `Annotations*`. This is the only piece of the rules extension that is not free.

Cost to add it (small, and precedented):

- `Rosetta.xcore:354` — add `Annotated` to `RosettaRule`'s supertypes.
- `Rosetta.xtext:737` — change `References*` to `=>(References|Annotations)*`, copying `Condition`
  verbatim (`Rosetta.xtext:120-121`). The `=>` predicate is required for the same reason as there: `[`
  starts both an annotation and a list literal, and a rule body is an expression. The keyword accessors
  the formatter uses (`getReportingKeyword_0_0`, `getRuleKeyword_1`, `getFromKeyword_3_0`,
  `getColonKeyword_4`) all sit *before* the changed group, so their generated indices do not shift.
- `RosettaFormatter.format(RosettaRule)` (`RosettaFormatter.java:637`) — add the four-line
  `ele.getAnnotations().forEach(...)` block from `format(Condition)` (`RosettaFormatter.java:393`).
- Regenerate parser/EMF (`mvn -pl rune-lang generate-sources`) and add a parsing + formatting test.

No validator restricts annotation placement, so nothing else gates it.

**Alternative if you want to avoid the grammar change in the first cut:** ship rules without an opt-out
and rely on `Hint` severity. Weak for library models — a DSL/DRR-style model publishes rules for
downstream consumption exactly like it publishes functions, and those are the ones that would grey out.
Recommend doing the grammar change.

### 3a.2 No other exemptions needed

Rules have no analogue of `superFunction`, `transform` or an empty body. `[rootType]` is irrelevant.
So the exemption list for a rule is just `[suppressUnused]`.

Also confirmed absent: any existing "rule is not used" check. `ReportValidator` validates eligibility
rule kind/input type (`ReportValidator.java:94-104`) and rule-reference type/cardinality, never usage.
`RosettaIssueCodes.MAPPING_RULE_NOT_USED` exists but is **dead** — declared and referenced nowhere. Leave
it alone rather than repurposing it; its name predates the current rule model.

### 3a.3 Derived state is not a factor

`RosettaDerivedStateComputer` only touches expressions (default input, default `else`, join separator,
implicit variable). It does **not** propagate rule references onto attributes, so the live-AST walk sees
exactly what is written in the source. The effective-rule computation that *does* resolve rule sources,
`extends`, `+`/`-` and `empty` (`rules/RuleReferenceService.java`) operates on the `RAttribute` layer, not
on EMF objects — invisible to the walk, and correctly so: detection stays purely syntactic. Consequence
worth stating: a rule referenced only from a rule source whose reference is later removed by a `-` in a
sub-source still counts as used.

### 3a.4 Invalidation risk is higher for rules than for types

Rule references are cross-file by construction in real models: rules in one file, `[ruleReference]`s on
the report type in another, rule sources in a third. Reporting rules are therefore the worst case for the
staleness bug in 2.3 — editing a `[ruleReference]` will not refresh the rule's file. Eligibility rules are
safer, since a report and its eligibility rules conventionally sit in the same file (see
`rune-integration-tests/src/test/resources/report-override-runtime-test/reg.rosetta`). **Phase 2 is a hard
prerequisite for reporting rules.**

The reference descriptions needed by phase 2 do exist for these shapes: they are ordinary cross-references
on non-expression objects, so `createReferenceDescriptions` records them (2.1). Note additionally that
`RosettaResourceDescriptionStrategy.createAttributeDescription` already serialises
`attr.getRuleReferences()` into `AttributeDescription` user data, so a changed `[ruleReference]` already
produces a description delta — which is the signal phase 2 keys off.

### 3a.5 Expected noise

Heuristic count over the repo's own rule-bearing test models (146 rule declarations across
`rune-integration-tests/src/test/java`, name-occurrence counting): **18 (~12%) would be flagged**, mostly
in `RosettaRuleGeneratorTest` where a rule is deliberately declared standalone with no report.
Comparable to the types case, and much less than the CDM public-API problem described in section 5.

## 3b. Reference shapes for the phase-4 kinds (investigation 2026-07-30)

### 3b.1 All but one fall out of 3.2 for free

Every new kind is reached by an ordinary EMF cross-reference from somewhere in the grammar, so the generic
walk already shipped in `UnusedElementHelper#computeOutgoingReferences` records it with no new code. Only
`isCandidate` has to change.

| Kind | Referenced by | `Rosetta.xtext` |
|---|---|---|
| `RosettaTypeAlias` | `TypeCall.type` — it is a `RosettaType` | 206 (decl. 268) |
| `RosettaBasicType` | `TypeCall.type` | 206 (decl. 235) |
| `RosettaRecordType` | `TypeCall.type`; its features roll up to it | 206 (decl. 247) |
| `RosettaExternalFunction` (`library function`) | `RosettaSymbolReference.symbol` — it is a `RosettaCallableWithArgs` | 336 (decl. 261) |
| `Annotation` | `AnnotationRef.annotation`; also `AnnotationRef.attribute` (e.g. `[metadata key]`), which rolls up to the declaring `Annotation` | 45-46, `RosettaSimple.xcore:52` (decl. 39) |
| `Schema` | `TransformAnnotation.ref` (`[ingest fixml]`) | 184-185 (decl. 173) |
| `RosettaExternalRuleSource` | `RosettaReport.ruleSource` (`with source`), `RosettaExternalRuleSource.superSource` (`extends`) | 730, 686 (decl. 685) |
| `RosettaBody` | `RegulatoryDocumentReference.body`, `RosettaCorpus.body` | 712, 323 (decl. 318) |
| `RosettaCorpus` | `RegulatoryDocumentReference.corpusList` | 712 (decl. 322) |
| `RosettaSegment` | `RosettaSegmentRef.segment` | 716 (decl. 326) |

Notes on individual rows:

- `TransformAnnotation.ref` is typed `[SchemaOrFormat|ValidID]`, so the same feature points either at a
  `Schema` or at a `RosettaEnumValue` of the built-in `SerializationFormat` enum (`[projection XML]`).
  Container rollup already handles the second case, and has since phase 3.
- `RosettaSegmentRef` is `segment=[RosettaSegment|QualifiedName] segmentRef=STRING` — the segment itself is
  a real cross-reference, only the *value* beside it is a string. So segments are visible to the walk.
- Bodies, corpuses and segments are reached through `RegulatoryDocumentReference`, which appears in a
  `RosettaReport` (line 724) and in every `[docReference]` (line 302). `[docReference]`s hang off the
  `References` fragment, which is on `Data`, `Attribute`, `Function`, `Condition`, `RosettaEnumeration`,
  `RosettaEnumValue` and `RosettaRule` — all of which roll up to a root element, so the reference is seen.
- Non-transitivity has a visible consequence here: a `body` referenced only by an unused `corpus` still
  counts as used, and so does a rule source referenced only by a report nobody generates.
- `RosettaResourceDescriptionStrategy` returning `false` for `RosettaExternalRuleSource` ("do not traverse
  down") only prunes *exported names*; `createReferenceDescriptions` uses its own unpruned traversal (2.1),
  so references made from inside a rule source are still indexed and 3.9's kind filter still works.

### 3b.2 The one genuine exclusion: `RosettaMetaType`

`metaType` (`Rosetta.xtext:276`, `Rosetta.xcore:132`) is named, so the "every named root element" rule would
include it — and it would be **wrong every single time**. A `metaType` is never the target of a
cross-reference. It is looked up by *name* against the index:
`RosettaEcoreUtil#getMetaDescriptions` → `RosettaConfigExtension#findMetaTypes`, which calls
`descriptions.getExportedObjects(ROSETTA_META_TYPE, QualifiedName.create(name), false)` and is itself marked
`@Deprecated` with a `//TODO: remove metaTypes from the model`. A model writes `[metadata reference]`, whose
`AnnotationRef.attribute` points at the `reference` attribute of the builtin `metadata` *annotation* — never
at the `metaType reference string` declaration.

So the AST walk cannot see a metaType as used, no `[suppressUnused]` is possible (not `Annotated`), and the
marker would be permanent. Exclude it explicitly, with that reason in a comment. This is the one place phase
4 departs from a literal reading of "every named root element"; it is a correctness exclusion, not a taste
one. It also disappears on its own if the deprecated metaType mechanism is ever removed.

Two smaller notes on metaTypes, for whoever revisits this: `RosettaQualifiedNameProvider#qualifiedName`
gives a metaType an *unqualified* name, so two metaTypes of the same name in different namespaces would
collapse to one `ElementId`; and no `.rosetta` file in the repo declares one — they exist only in inline test
models such as `GlobalKeyGeneratorTest`.

### 3b.3 Which kinds can carry `[suppressUnused]` — and which cannot

Per the 2026-07-30 decision, no grammar change is made to widen this **as of phase 4**; phase 5 later made
exactly one exception for `RosettaTypeAlias` (moved to the left column below — see the Phase 5 section for
why and the Xtext generator bug the change ran into). The determining factor is **not** the EMF `Annotated`
supertype but whether the grammar rule actually offers the `Annotations` fragment:

| Suppressible | Not suppressible |
|---|---|
| `Function`, `Data`/`Choice`, `RosettaEnumeration`, `Schema` (`Rosetta.xtext:173-175` has `Annotations*`), `RosettaTypeAlias` (phase 5) | `RosettaRule`, `RosettaExternalRuleSource`, `RosettaBody`, `RosettaCorpus`, `RosettaSegment`, `RosettaBasicType`, `RosettaRecordType`, `RosettaExternalFunction`, `Annotation` |

The `Annotation` row is the surprising one and worth recording so nobody "fixes" it by mistake:
`Annotation extends RootElement`, which *is* `Annotated`, so the EMF class has an `annotations` list — but
the grammar rule (`Rosetta.xtext:39-43`) is
`'annotation' RosettaNamed ':' RosettaDefinable? ('[' 'prefix' prefix=ValidID ']')? attributes+=Attribute*`
with no `Annotations*` fragment. So `[suppressUnused]` on an annotation does not parse, and the list is
always empty. Adding it would be a one-line grammar change with the same `=>` predicate caveat as 3a.1 (the
existing `[prefix …]` group already competes for `[`) — **not planned**, noted only so the option is on
record if annotation-library noise turns out to be a real problem.

Consequence to accept: a model that publishes an annotation library, a type-alias library, a bodies/corpuses
file, or a `rule source` for downstream consumption has no opt-out for those declarations and relies on
`Hint` severity alone. For the builtins specifically this is handled instead by excluding them outright
(4.3).

## 4. Recommended approach

Four phases. **Do phase 2 before phase 3**, or accept visibly wrong markers. Phase 4 depends on phase 3.

### Phase 1 — correct the record (small) — DONE (2026-07-29)

Fix the javadoc on `UnusedFunctionHelper` per 2.1. Cheap, and the wrong reasoning is what would send the
next person down the wrong path.

Fixed the javadoc on `getReferencedFunctionUris` in
`rune-lang/.../validation/UnusedFunctionHelper.java`: it now explains that the index *does* contain
function-call references (`createReferenceDescriptions` is unoverridden), but is unusable because
same-file references aren't indexed (2.2) and there's no target→source reverse lookup — not because
expressions aren't descended into.

### Phase 2 — fix invalidation — DONE (2026-07-29)

**Superseded (2026-08-13) by part 2's post-build service.** Everything below this point — the
`IncomingReferenceChanges` diff, the unload-before-revalidate step, the per-kind pruning of §3.9 — describes
the mechanism as it shipped in this PR. Part 2 replaces it outright with a language-agnostic
`WorkspaceDerivedDiagnosticsService` that recomputes markers for every resource after each build instead of
diffing which resources' incoming references changed; see `unused-element-markers-part2.md` §1 and §3.7 for
why and what moved. `IncomingReferenceChanges` and the revalidation methods on
`RosettaStatefulIncrementalBuilder` no longer exist. The measurements and remaining-caveats sections below are
kept as the record of why the replacement was worth doing (§3.6's mass-edit cost is part 2's §1 point 3).

Acceptance met: both `UnusedFunctionStalenessTest` cases pass with `@Disabled` removed. `rune-ide` went
from 66 tests / 2 skipped to 66 tests / 0 skipped, no other change.

**The `isAffected` approach this plan proposed does not work. Do not retry it.** It was implemented and
abandoned; what shipped instead is described below.

#### Why `IResourceDescription.Manager#isAffected` cannot do this

The plan assumed a delta exposes old and new reference descriptions. It does not, at the point
`isAffected` is asked:

- `Indexer#computeAndIndexAffected` builds deltas via `addToIndex`, which wraps each new description in
  `ResolvedResourceDescription`. That class's `getReferenceDescriptions()` **and**
  `getImportedNames()` deliberately log an `IllegalStateException` and return empty — it exists precisely
  to detach a description from its resource before the description goes into the long-lived index.
- Only the *old* side carries references (it is a `SerializableResourceDescription` read back from the
  index). That is enough to notice a reference **disappearing** but never one **appearing** — which is
  exactly the half-working behaviour observed: the "last call site removed" case passed while "first call
  site added" kept failing.
- The references cannot be made available there either. Both they and `importedNames` are derived by
  `EcoreUtil2.resolveLazyCrossReferences`, and `Indexer` computes descriptions under
  `CompilerPhases#setIndexing(true)` and *before* `newIndex.register(delta)`. Forcing resolution at that
  point links against an index that does not yet contain the model, fails, and caches bogus
  `Couldn't resolve reference to RosettaType 'int'` errors on the resources — this was reproduced, and
  poisoned every subsequent assertion in the test.

Corollary worth remembering: **a changed resource's new outgoing references are structurally unknowable
until after the whole index is up to date.** That is why Xtext's own `isAffected` is name-based rather
than reference-based.

#### What shipped

A post-build pass in the language server, entirely within `rune-ide` — `rune-lang` is untouched, so this
stays editor-only in the same sense as the rest of the feature:

| File | Role |
|---|---|
| `rune-ide/.../build/IncomingReferenceChanges.java` | new — pure diff: given the build's deltas, which *other* resources declare an element whose incoming-reference set changed |
| `rune-ide/.../build/RosettaStatefulIncrementalBuilder.java` | `launch()` now calls `revalidateResourcesWithChangedIncomingReferences(result)` after `doLaunch()` |

It runs *after* `super.launch()`, where `IncrementalBuilder` has resolved cross-references and stored
`SerializableResourceDescription.createCopy(description)` — and `createCopy` **does** copy reference
descriptions. So both sides of every delta are complete and the diff is exact in both directions.

Two details that are load-bearing:

- **Group targets by declaring resource, keep the element URIs.** Grouping answers "did the references
  into *that file* change"; keeping individual `targetEObjectUri`s means moving a call from one
  declaration to another in the same file still counts for both.
- **Unload before revalidating.** The resources are unchanged, so revalidating in place returns the
  previous answer: `CachingResourceValidator` memoises issues per resource and `UnusedFunctionHelper`
  memoises the called-function set per resource, and `ProjectManager#createFreshResourceSet` is a
  misnomer — it *reuses* the resource set across builds, so those caches survive. Unloading is how
  Xtext's own affected-resource path gets a clean slate too. No code is generated for these resources,
  since unchanged input produces identical output.

#### How this compares to the plan's stated risks

- **Rebuild cascade cost** — not a concern any more. `isAffected` is untouched, so the rebuild set is
  unchanged; the extra work is a revalidate-only (no generation) pass over exactly the resources whose
  incoming references changed.
- **Freshness within a batch** — actually *solved* rather than accepted. The pass runs once the whole
  batch has been built and every new description is installed, so a multi-file edit is evaluated against
  the final index state, not a half-updated one.

#### Measured cost (2026-07-29, CDM: 145 files / 3.0 MB)

A/B against the pre-phase-2 builder, medians of 3 rounds, driving the real language server via
`didChange` and counting `publishDiagnostics` (one per validated resource). Harness:
`CdmEditBenchmark` (not checked in — see 3.9).

| Edit | without pass | with pass | files validated |
|---|---|---|---|
| cold initial build | 5076 ms | 5049 ms | 148 → 148 |
| keystroke changing no cross-file reference | 26 ms | 27 ms | 1 → 1 |
| keystroke where a call **starts or stops** resolving | 24-27 ms | 44-48 ms | 1 → 2 |
| keystroke toggling a **type** or **enum** reference | 22-23 ms | 25-26 ms | 1 → 2 |
| blank out `event-common-func.rosetta` (mass edit) | 100 ms | 620 ms | 4 → 44 |
| restore it | 241 ms | 748 ms | 4 → 44 |

So: cold start and ordinary typing are free (the diff over 148 deltas costs ~1 ms, since every candidate
is already in `built`); a call-site toggle roughly doubles a 25 ms build; mass edits cost 5-6× and are
user-perceptible.

#### Remaining caveats

**Clustering.** `UnusedFunctionHelper` walks every resource in the resource set. If clustering unloads a
resource during the revalidation pass, that resource's call sites become invisible to the walk and a
marker could be wrong until the next edit. Not reachable at the scale of the tests.

**The pass is kind-agnostic.** It fires for *any* changed cross-resource reference, including references to
types and enums, which no marker consumes today — measured above at +3 ms, and cheap only because those
declaring files contain no functions and so trigger no AST walk. Harmless now, but it is the frequency
half of phase 3's cost. See 3.9.

**Build statistics under-report it.** `resetBuildStatistics` / `logBuildStatistics` both run inside
`doLaunch()`, i.e. before the revalidation pass, so the pass's validations increment `sourceFilesValidated`
after `Build FINISHED - validated N files` has been logged and are then reset by the next build. Observed
directly: an edit logging `validated 4 files` was followed by `Revalidated 40 files`. Move the log after the
pass, or count the pass separately.

### Phase 3 — widen detection to types, enums and rules — DONE (2026-07-30)

Shipped. `rune-ide` went from 66 tests / 0 skipped to 95 / 0 skipped; `rune-integration-tests` unchanged at
1489 / 21 skipped; full build green.

| File | Change |
|---|---|
| `rune-lang/.../validation/UnusedElementHelper.java` | new — replaces `UnusedFunctionHelper`; generic cross-reference walk, per-resource cache |
| `rune-lang/.../validation/RosettaIssueCodes.java` | `UNUSED_TYPE`, `UNUSED_ENUMERATION`, `UNUSED_REPORTING_RULE`, `UNUSED_ELIGIBILITY_RULE`, `UNUSED_CODES` |
| `rune-ide/.../validation/UnusedElementResourceValidator.java` | new — replaces `UnusedFunctionResourceValidator`; iterates `model.getElements()`, dispatches on kind |
| `rune-ide/.../RosettaIdeModule.java` | binds the new validator |
| `rune-ide/.../server/RosettaLanguageServerImpl.java` | `UNUSED_CODES.contains(...)`, **plus a null-code guard — see below** |
| `rune-ide/.../build/IncomingReferenceChanges.java` | 3.9 — prunes the revalidation pass by target kind |
| `rune-ide/.../validation/UnusedElementValidationTest.java` | 35 cases, one per reference shape |
| `rune-ide/.../performance/EditLatencyBenchmark.java` | new — the 3.10 harness, now checked in |
| `rune-runtime/.../model/annotations.rosetta` | `[suppressUnused]` description covers functions, types and enums |

#### Decisions taken (answers to section 5)

- **Rules are in scope, with no opt-out.** The 3a.1 grammar change was *not* made: `RosettaRule` is still not
  `Annotated`, so `[suppressUnused]` cannot be written on a rule. Detection covers rules regardless.
  Phase 4 generalises this from a one-off concession to the standing policy — see 3b.3.
- **`RosettaTypeAlias` excluded**, as originally scoped. ~~As shipped in phase 3.~~
  **Superseded by phase 4**, which includes it (and every other named root element).
- **Types are always on**, like functions — no client setting. Vindicated by the noise census below, and note
  `rune-ide` has no `didChangeConfiguration`/`initializationOptions` handling at all today, so a setting would
  have been new infrastructure rather than a flag.

#### The one significant deviation: cache on qualified names, not URIs

3.6 as written (`outgoingReferences(r).contains(candidateUri)`) is **unsound**, and the bug it introduces is
exactly the false positive this feature must not have. No `IFragmentProvider` is bound, so element URIs are
EMF's positional fragments (`//@elements.3`). Once the outgoing-reference sets are cached *per referencing
resource*, inserting a declaration at the top of file B renumbers everything below it — while file C, which
references those declarations, is not rebuilt, because a purely positional change produces no exported-name
delta. C's cached URIs then point at the wrong declarations and B's still-used types get greyed out.

So `UnusedElementHelper` caches an `ElementId(QualifiedName, eClassName)` per reference instead. Qualified
names only change on a rename, and a rename *does* produce a name delta and *does* rebuild the referencing
files. The `eClassName` discriminator keeps two declarations of different kinds that share a qualified name
from being conflated. The pre-phase-3 code was not exposed to this because it recomputed the whole
resource-set-wide set in one pass, so every URI in it was computed at the same instant.

#### Latent bug found and fixed: an immutable `Set` and a null issue code

Replacing `UNUSED_FUNCTION.equals(issue.getCode())` with `UNUSED_CODES.contains(issue.getCode())` looks like a
pure refactor and is not: `Set.of(...)` throws `NullPointerException` on `contains(null)`, and issues with no
code do exist. Because `toDiagnostic` runs while publishing, the NPE **discarded every diagnostic for the
resource** — an invalid model published as silently clean, with no log line, since the throw happens inside
the LSP's publish path. `RosettaLanguageServerImpl` now null-guards, and
`UnusedElementValidationTest#validationErrorsWithNoIssueCodeAreStillReported` covers it.

Worth remembering as a class of bug: anything that runs on the publish path must not throw, because the
failure mode is *fewer* diagnostics rather than a visible error.

#### 3.7 — one planned case could not be written as specified

"reporting rule referenced only by a `- attr` removal in a rule source → flagged" needs a rule source whose
only entry is a `- attr`, and that model is **invalid Rune**: it fails validation with "There is no rule
reference to remove". Rewritten as `ruleSourceRemovalCreatesNoRuleReference`, which asserts both halves of the
real behaviour on a valid model — a rule mentioned nowhere is flagged even with a rule source present, and a
rule whose `[ruleReference]` a source removes still counts as used (the 3a.3 consequence).

#### 3.9 — how the kind filter was implemented

The delta's reference descriptions carry only `targetEObjectUri`, so the target's `EClass` comes from the
declaring resource's `getExportedObjects()`, matched by URI. Nested targets (an attribute, an enum value) are
rolled up by URI-fragment prefix, because `UnusedElementHelper` attributes a reference to the declaration
containing its target — without the rollup, `foo -> bar` would be pruned even though it is a use of `bar`'s
type. A resource that exports no marker-capable declaration at all is skipped outright, which is what drops
`annotations.rosetta` from the fan-out.

#### 3.10 — measured (2026-07-30, CDM: 145 files / 3.0 MB)

Harness checked in as `EditLatencyBenchmark` (`rune-ide`, test scope). It skips unless given
`-Drune.benchmark.model.dir=...`, and its name does not match surefire's include patterns, so CI never runs
it. **Marked temporary — do not delete without asking.** A/B by running it on the branch and again with the
main-source changes stashed; medians of 3 rounds, apply/revert reported separately.

| Edit | phase 2 only | with phase 3 | files validated |
|---|---|---|---|
| cold initial build | 3464 ms | **2725 ms** | 145 → 145 |
| edit changing no cross-file reference | 114 / 110 ms | **107 / 108 ms** | 1 → 1 |
| edit toggling a function call | 50 / 51 ms | **28 / 30 ms** | 2 → 2 |
| edit toggling a type or enum reference | 281 / 286 ms | **187 / 191 ms** | 11 → 11 |
| blanking out a 40-dependent file | 795 / 941 ms | **564 / 647 ms** | 40 → 40 |

**Phase 3 is faster than phase 2 in every scenario, despite detecting four kinds instead of one.** 3.6 is the
reason: it replaces N full resource-set walks (one per candidate) with one walk per resource plus hash
lookups, and that outweighs the larger candidate set. So 3.6 was not the "partial mitigation" the plan billed
it as — it was a net win that paid for the widening outright. The fan-out counts are identical before and
after, i.e. 3.9 did not need to reduce the trigger; it now only drops annotation-only resources, since
functions, types, enums and rules can all carry a marker. **Phase 4 makes annotations marker-capable too, so
this last remaining effect is precisely what 4.3 has to preserve by hand.**

Note the absolute numbers are not comparable with the phase-2 table above it: that run measured a
ranged single-character `didChange` on a small file, this one replaces a whole document and picks its
bulk-edit target by fan-out (the file whose declarations the most other files reference), which is a
100 KB+ `ingest-fpml-*` file. The A/B columns are measured the same way as each other, which is what matters.

#### Noise census on CDM — section 5's main worry was misplaced

| Kind | markers | declarations | share |
|---|---|---|---|
| Function | 234 | 1303 | 18.0% |
| Type | 20 | 634 | 3.2% |
| Enumeration | 11 | 271 | 4.1% |

**Types are the least noisy of the three, not the worst.** Section 5 assumed a library model would grey out
"a large share" of its types; in CDM it is 3%, because a type with no in-model reference is genuinely rare
even in a published model — nearly everything is reachable from a root type. The function figure reproduces
the plan's earlier 235/1303, which cross-checks the harness against the original method. CDM declares no
reporting rules (those live in DRR), so the rule share is still only estimated at ~12% from this repo's own
test models (3a.5).

**3.1 Generalise the helper** — `UnusedFunctionHelper` → `UnusedElementHelper`.

**3.2 Replace the type-specific check with a generic cross-reference walk**, keyed on the containing
root element of each target rather than the target itself:

```java
for (EObject target : source.eCrossReferences()) {
    if (target.eIsProxy()) continue;
    RosettaRootElement targetRoot = rootElementOf(target);
    if (targetRoot == null || targetRoot == rootElementOf(source)) continue; // self-reference
    referenced.add(EcoreUtil.getURI(targetRoot));
}
```

Two properties this buys:

- **Container rollup.** A reference to an enum *value* (e.g. a `schema` format at `Rosetta.xtext:174`, or
  a `switch` case guard) counts as a use of its enum; a reference to an attribute counts as a use of its
  type. Without this, `schema` formats are false positives.
- **Self-reference exclusion.** `type Foo: child Foo (0..1)` stays flagged. This also *changes existing
  behaviour*: a self-recursive function with no external callers becomes flagged where today it does not.
  Intended, but needs its own test.

Covers every row of the table in section 3 **and all four rule shapes in 3a** with no per-feature code.

One detail the rules case adds: today's helper narrows `RosettaSymbolReference.symbol` to
`instanceof Function`. Drop that narrowing rather than extending it to a type list — the container rollup
plus self-reference exclusion is what makes it safe.

**3.3 Exemptions.** Only these need explicit handling:

- `Function` — unchanged (`superFunction`, non-empty `transform`, empty body, `[suppressUnused]`).
- `Data` — `[suppressUnused]`, `[rootType]`.
- `RosettaEnumeration` — `[suppressUnused]`.
- `RosettaRule` — ~~`[suppressUnused]` only, **after the grammar change in 3a.1**~~ → **as shipped: no
  exemption at all**, since the grammar change was not made. Phase 4 adds `Schema` (`[suppressUnused]`) and
  nothing else; see 4.4.

Report types, rule inputs, qualifiable roots, rule-source classes and supertypes need **no** special
case: 3.2 already sees them.

`[suppressUnused]` on a `type`/`enum` already parses and validates — `Data` and `RosettaEnumeration` are
both `Annotated` via `RootElement`, and no validator restricts annotation placement. On a rule it does
not (3a.1). Reword the annotation's description in
`rune-runtime/src/main/resources/model/annotations.rosetta` to cover all four kinds. *(Phase 4 revisits that
wording again — 4.9 — because the set of kinds that get the marker and the set that can suppress it diverge
once every named root element is a candidate.)*

**3.4 Issue codes.** *(Superseded 2026-08-13 — collapsed to a single `UNUSED_DECLARATION`; see "Review
changes" at the end of this plan.)* Add `UNUSED_TYPE`, `UNUSED_ENUMERATION`, `UNUSED_REPORTING_RULE` and
`UNUSED_ELIGIBILITY_RULE` next to `UNUSED_FUNCTION`, plus a `Set<String> UNUSED_CODES`. Separate codes
leave room for kind-specific quick fixes later.
`RosettaLanguageServerImpl.toDiagnostic` becomes `UNUSED_CODES.contains(issue.getCode())`.

Split the two rule codes even though detection is shared, because the messages differ usefully:
"Eligibility rule 'X' is not used by any report" vs "Reporting rule 'X' is never used". Switch on
`RosettaRule.isEligibility()`.

**3.5 Validator.** `UnusedFunctionResourceValidator` → `UnusedElementResourceValidator`. All candidates
are `RosettaRootElement`s, so replace the `getAllContents()` walk with `model.getElements()` and dispatch
on type. `RosettaPackage.Literals.ROSETTA_NAMED__NAME` already works for all three kinds.

**3.6 Caching.** Today the cache is keyed on the *validated* resource but its value spans the whole
resource set, so every open file re-walks every AST in the workspace. Restructure to cache **outgoing
references per resource** (invalidated only when that file changes) and query with hash lookups:

```java
boolean isReferenced(URI candidate, ResourceSet rs) {
    return rs.getResources().stream().anyMatch(r -> outgoingReferences(r).contains(candidate));
}
```

One edit then costs one file's walk plus N cheap `contains` calls instead of N full walks.

Measured on CDM with an instrumented helper (2026-07-29): **one walk = 148 resources / ~79,300 EObjects /
9-16 ms.** Where that lands today:

- Cold build: **78 walks, ~1.3 s of the 5.0 s total** — a quarter of cold start, and this is phase 1's
  cost, present before phase 2.
- Call-site toggle: 2 walks, ~19 ms of the +21 ms delta.
- Mass edit: 13-14 walks, ~200 ms of the +520 ms delta.

Two consequences for how this step is billed. First, **3.6 is a partial mitigation, not the mitigation**:
only ~38% of the pass's fan-out cost is the walk; the other ~62% is re-parse + link + the *full* validator
over each revalidated file. Second, phase 3 multiplies the walk count, not just its size — only 14 of the 44
files revalidated in the mass-edit case walk today, because the other 30 declare no functions; once types
and enums are candidates, all 44 walk. Extrapolating, that case goes from ~520 ms to roughly 0.9-1.1 s.

Ordering within phase 3 does not matter, since nothing is degraded while 3.6 is outstanding — but it must
land in the same release as the widened detection, and in the *first* PR if phase 3 is split per section 5.

**3.7 Tests.** Extend `UnusedFunctionValidationTest` → `UnusedElementValidationTest`: one case per row of
section 3 and of section 3a (each is a distinct regression risk), plus `[rootType]`, `[suppressUnused]`,
choice types, recursive type, enum-value-only references (`schema` format, switch guard). Rule-specific
cases:

- eligibility rule used by a report → not flagged; declared with no report → flagged.
- reporting rule used by `[ruleReference]` on an attribute → not flagged.
- reporting rule used by `[ruleReference]` inside a `rule source` → not flagged.
- reporting rule used only by another rule's expression (`extract Foo { a: OtherRule }`) → not flagged.
- reporting rule referenced only by a `- attr` removal in a rule source → flagged (no `reportingRule`
  cross-reference exists in that form).
- `[suppressUnused]` on a rule → not flagged (depends on 3a.1).

**3.8 Fix fallout.** Any test asserting an *exact* `getDiagnostics()` count breaks, since nearly every
test model declares an unreferenced type. Apply the `filterSevereDiagnostics` helper already added to
`ChangeDetectionTest` across `QuickFixTest`, `Issue785`, `GenerationErrorHandlingTest`,
`HandleParseErrorGracefullyTest` (~37 `getDiagnostics()` call sites in `rune-ide`). Per `CLAUDE.md`,
capture the test count before and after. Rules add little here beyond the types case (~18 additional
flagged declarations across `rune-integration-tests`, per 3a.5), and none of it is in `rune-ide`.

If the 3a.1 grammar change lands, additionally re-run `RosettaParsingTest`, `RosettaFormattingTest` and
`RosettaRuleGeneratorTest` — the `=>` predicate on `(References|Annotations)*` is the part most likely to
regress parsing of a rule whose expression begins with `[`.

**3.9 Prune the phase 2 pass by target kind.** New step, from the measurement. The revalidation pass
currently fires for every changed cross-resource reference regardless of what it points at, so widening
detection also widens the *trigger* — and type references are cross-file by default in a real model, where
function calls are the exception. Skip a candidate resource unless a changed target is a kind that can carry
a marker.

The delta's reference descriptions carry only `targetEObjectUri`, so the `EClass` has to come from the
declaring resource's exported objects: `IResourceDescription.getExportedObjects()` →
`IEObjectDescription.getEClass()`, matched by URI. Both descriptions are in hand at that point in
`RosettaStatefulIncrementalBuilder`, so this stays inside `rune-ide`.

Cheap and worth doing on its own merits: it also drops the builtins, which the pass revalidates today
(`basictypes.rosetta` and `annotations.rosetta` both appeared in the measured 40-file fan-out) even though
they are read-only and can never carry a marker.

**3.10 Measure.** Baseline taken 2026-07-29 — see the tables in phase 2 and 3.6 for the numbers and the
method (a `didChange`-driven harness against CDM, A/B by classpath-overriding the builder, plus an
instrumented `UnusedFunctionHelper` to attribute the walk). The harness lives outside the repo; recreate or
check it in before re-measuring, and re-run the same scenarios after phase 3. Targets to beat: no
regression on the ~26 ms no-reference-change edit, keep the reference-toggle edit under ~50 ms, and keep the
mass-edit case at or below today's ~620 ms rather than the ~1 s that 3.6-less widening would produce.

### Phase 4 — widen detection to every named root element — DONE (2026-07-30)

Shipped. `rune-ide` went from 95 tests / 0 skipped to 122 / 0; `rune-integration-tests` unchanged at
1489 / 21 skipped; full build green including checkstyle.

| File | Change |
|---|---|
| `rune-lang/.../validation/UnusedElementHelper.java` | `isCandidate` is now the naming rule plus two exclusions; `isExempt` handles `[suppressUnused]` generically for anything `Annotated` |
| `rune-lang/.../validation/RosettaIssueCodes.java` | `UNUSED_TYPE_ALIAS`, `UNUSED_ANNOTATION`, `UNUSED_SCHEMA`, `UNUSED_DECLARATION`, all added to `UNUSED_CODES` |
| `rune-lang/.../builtin/RosettaBuiltinsService.java` | new `isBuiltinResource(URI)` + file-name constants — the shared predicate 4.3 asked for |
| `rune-lang/.../validation/names/RosettaNamesAreUniqueValidationHelper.java` | its private `isBuiltin` now delegates to that predicate rather than duplicating the file names |
| `rune-ide/.../validation/UnusedElementResourceValidator.java` | `messageFor`/`issueCodeFor` if-chains replaced by the `KINDS` table + generic fallback; `markerFor` is static/package-private so it can be tested directly |
| `rune-ide/.../build/IncomingReferenceChanges.java` | `MARKER_CAPABLE_KINDS` list replaced by the same rule (`ROSETTA_ROOT_ELEMENT` **and** `ROSETTA_NAMED`, minus `ROSETTA_META_TYPE`); builtin resources short-circuit to empty |
| `rune-ide/.../validation/UnusedElementValidationTest.java` | 35 → 57 cases |
| `rune-ide/.../validation/UnusedElementMarkerTest.java` | new — the reflective guard over every named root element (4.7) |
| `rune-ide/.../performance/EditLatencyBenchmark.java` | census denominators widened to every root-element keyword; noun grouping fixed (see below) |
| `rune-runtime/.../model/annotations.rosetta` | `[suppressUnused]` description now distinguishes the kinds that get the marker from the kinds that can carry the annotation |

#### 4.3 was not hypothetical — measured

Confirmed by temporarily removing the builtin exclusion and running
`UnusedElementValidationTest#builtinDeclarationsAreNeverFlagged`: **19 markers appear on the two read-only
builtin files**, none of them suppressible —

```
Annotation 'calculation' / 'codeImplementation' / 'deprecated' / 'externalConfig' / 'metadata' /
'qualification' / 'suppressUnused' / 'suppressWarnings'   (8)
Basic type 'pattern', Type alias 'calculation', Enumeration 'SerializationFormat'   (3)
Library function 'Adjust' / 'DateRanges' / 'IsLeapYear' / 'Max' / 'Min' / 'Within'   (6)
Record type 'dateTime' / 'zonedDateTime'   (2)
```

`[suppressUnused]` marking *itself* as never used is the neatest illustration of why the exclusion is
required rather than merely tidy. That test is the regression guard.

#### The `Annotation` gap is real and now has a test

`suppressUnusedCannotBeWrittenOnAnAnnotation` asserts that `[suppressUnused]` on an `annotation` declaration
is a **syntax error**, not a no-op. 3b.3 predicted this from reading the grammar; it is worth having as a test
because the EMF model says the opposite (`Annotation extends RootElement`, which is `Annotated`), so anyone
checking the model rather than the grammar would conclude it works.

#### 4.5 measured (2026-07-30, CDM: 145 files / 3.0 MB)

A/B on the same checkout by stashing only the main-source changes and keeping the benchmark, medians of 3,
apply/revert separately.

| Edit | phase 3 | with phase 4 | files validated |
|---|---|---|---|
| cold initial build | 2742 ms | 2708 ms | 145 → 145 |
| edit changing no cross-file reference | 115 / 110 ms | 105 / 102 ms | 1 → 1 |
| edit toggling a function call | 27 / 27 ms | 26 / 26 ms | 2 → 2 |
| edit toggling a type or enum reference | 190 / 185 ms | 183 / 180 ms | 11 → 11 |
| blanking out a 40-dependent file | 589 / 649 ms | 573 / 642 ms | 40 → 40 |

**No regression on any row, and the fan-out counts are identical.** As 4.5 predicted, detection was already
walking every resource and recording every cross-reference regardless of target kind, so widening candidacy
only adds hash lookups. The fan-out staying at 1/2/11/40 is 4.3 doing its job: the builtins became
marker-capable in principle and are excluded in practice, so the phase-2 pass triggers on exactly what it did
before.

#### 4.6 noise census on CDM — one kind is genuinely noisy

| Kind | markers | declarations | share |
|---|---|---|---|
| Function | 234 | 1303 | 18.0% |
| Type | 20 | 759 (+16 `choice`) | 2.6% |
| Enumeration | 11 | 279 | 3.9% |
| **Type alias** | **14** | **17** | **82%** |
| Segment | 5 | 15 | 33% |
| Corpus | 4 | 32 | 12.5% |
| Body | 0 | 4 | 0% |
| Annotation | 0 | 1 | 0% |
| metaType | 0 | 6 | — excluded by 3b.2 |

CDM declares no schema, rule source, basic type, record type or library function, so those are unmeasured
here. `Type` reproduces phase 3's 20 exactly, which is the check that the `isExempt` restructure is
behaviour-preserving. The 6 metaTypes going unflagged is 3b.2 verified on a real model rather than a
constructed one.

**`typeAlias` at 82% was the one bad number in this phase, and it had no opt-out — fixed in phase 5.** All 14
are the `FpMLCodingScheme(domain: …)` family in `coding-scheme-type.rosetta` — a published scheme vocabulary,
i.e. precisely the "public API of a library model" case `[suppressUnused]` exists for. At the time this was
written the fix was described as "one line, not done, per the no-grammar-change decision"; phase 5 made that
change (it was not, in the end, quite one line — see 5.1 for the Xtext code-generator issue the naive version
ran into and the placement that avoids it).

The census denominators were widened to every root-element keyword, so the `type` figure is 759 rather than
phase 3's 634 — the old pattern required `type Name:` or `type Name(` and so missed `type Name extends …`.
The **marker** counts are directly comparable; the phase-3 *shares* were computed against too small a
denominator and were therefore slightly pessimistic.

One measurement bug found and fixed while doing this: the census grouped markers by
`message.split(" ")[0]`, which folds "Type alias" into "Type" — the first phase-4 run appeared to show types
jumping from 20 to 34 markers, which would have been an unexplained behaviour change. It now groups on the
whole noun.

#### 4.8 fallout — smaller than expected

Only two existing cases broke, both in `UnusedElementValidationTest`, and both for the same legitimate
reason: their models declare a `rule source` no report references, which is now correctly flagged. Since a
rule source has no opt-out, the expectations assert the extra marker with a comment saying it is scaffolding.
Nothing outside that class needed touching — `filterSevereDiagnostics` from 3.8 had already absorbed the
class of breakage that would otherwise have hit `QuickFixTest`, `ChangeDetectionTest` and friends.

Two grammar details worth knowing, both found by a test failing:

- **`label` is a keyword** (the builtin `[label …]` annotation), so it cannot name an attribute — a test
  annotation with a `label string (0..1)` attribute fails to parse with `missing EOF at 'label'`.
- `corpus` takes `corpusType` *before* the optional body reference and the name, so `corpus Auth Doc` means
  "corpus of type Auth named Doc with no body", not "corpus Doc of body Auth". Getting this wrong silently
  changes what a test asserts rather than failing it.

#### Design as planned below

Replaces the hand-maintained candidate list with a rule: a `RosettaRootElement` is a candidate iff it is a
`RosettaNamed` with a non-null name. The point is that the *next* grammar root element is then covered by
default; the list in `UnusedElementHelper#isCandidate` was already a maintenance trap of exactly the kind
section 3 warned about for reference shapes.

Detection itself needs no new machinery — 3b.1 confirms every new kind is reached by an ordinary
cross-reference that the phase-3 walk already records. The work is in candidacy, messages/codes, two
exclusions, and fallout.

#### 4.1 The full root-element census

There are 18 alternatives in `RosettaRootElement` (`Rosetta.xtext:153-172`):

| Grammar alternative | EMF class | Named? | Annotatable in grammar? | After phase 4 |
|---|---|---|---|---|
| `Function` | `Function` | yes | yes | in (phase 3) |
| `Data`, `Choice` | `Data`, `Choice` | yes | yes | in (phase 3) |
| `Enumeration` | `RosettaEnumeration` | yes | yes | in (phase 3) |
| `RosettaRule` | `RosettaRule` | yes (via `RosettaCallableWithArgs` → `RosettaSymbol`) | no | in (phase 3) |
| `RosettaTypeAlias` | `RosettaTypeAlias` | yes (via `RosettaType`) | no — **became yes in phase 5** | **new** |
| `Annotation` | `Annotation` | yes | no — see 3b.3 | **new** |
| `Schema` | `Schema` | yes | yes | **new** |
| `RosettaExternalRuleSource` | same | yes | no | **new** |
| `RosettaBody` | same | yes | no | **new** |
| `RosettaCorpus` | same | yes | no | **new** |
| `RosettaSegment` | same | yes | no | **new** |
| `RosettaBasicType` | same | yes (via `RosettaType`) | no | **new** — builtin-only in practice (4.3) |
| `RosettaRecordType` | same | yes (via `RosettaType`) | no | **new** — builtin-only in practice (4.3) |
| `RosettaLibraryFunction` | `RosettaExternalFunction` | yes (via `RosettaCallableWithArgs`) | no | **new** |
| `RosettaMetaType` | same | yes | no | **excluded** — 3b.2 |
| `RosettaReport` | same | **no** | n/a | out, by the rule itself |

`RosettaReport` needing no special case is the neatest part of the change: it was already excluded by hand,
and it is excluded by the naming rule for the same underlying reason (there is nothing to attach a marker to).

#### 4.2 Candidacy, messages and issue codes

`UnusedElementHelper#isCandidate` becomes, in substance:

```java
private boolean isCandidate(RosettaRootElement element) {
    // Excluded for a correctness reason, not a policy one: a metaType is resolved by name through the
    // index (RosettaConfigExtension#findMetaTypes), never by a cross-reference, so the walk can never
    // see one as used.
    if (element instanceof RosettaMetaType) {
        return false;
    }
    if (isBuiltin(element.eResource())) {   // 4.3
        return false;
    }
    return element instanceof RosettaNamed named && named.getName() != null;
}
```

**`UnusedElementResourceValidator#messageFor` and `#issueCodeFor` must be restructured before `isCandidate`
widens, not after.** Both currently end in an unchecked `return ((RosettaRule) element)…`, so the first
candidate of a new kind throws `ClassCastException` from inside `validate` — and per the phase-3 lesson about
the publish path, a throw in the diagnostic pipeline shows up as *missing* diagnostics rather than a visible
error. Replace the if-chain in each with a single table keyed on kind, carrying both the display noun and the
issue code, plus a **generic fallback** so that a future root element gets a sensible marker instead of a
crash. The fallback is what makes "every named root element" actually self-maintaining.

Display nouns: `Type alias`, `Annotation`, `Schema`, `Rule source`, `Body`, `Corpus`, `Segment`,
`Basic type`, `Record type`, `Library function` — messages stay in the existing
`"<Noun> 'X' is never used"` shape (the two rule messages keep their special wording).

Issue codes: *(superseded 2026-08-13 — every kind now shares `UNUSED_DECLARATION`; see "Review changes" at
the end of this plan.)* the phase-3 rationale for one code per kind was room for kind-specific quick fixes.
That is worth keeping only where a quick fix is plausible, so add `UNUSED_TYPE_ALIAS`, `UNUSED_ANNOTATION` and
`UNUSED_SCHEMA`, and give the documentation and builtin-ish kinds (rule source, body, corpus, segment, basic
type, record type, library function) plus the generic fallback a single shared `UNUSED_DECLARATION`. Add all
of them to `UNUSED_CODES` — that set, not the individual code, is what `RosettaLanguageServerImpl` gates on,
so splitting a code out later is a non-breaking change. Keep the null-guard in `toDiagnostic` exactly as it
is (see the phase-3 note on it).

#### 4.3 Exclude the builtins — required, not optional

This is the one part of phase 4 that is not free, and it must land in the same change.

`basictypes.rosetta` declares basic types, record types, `library function`s, the `int` and `calculation`
type aliases and the `SerializationFormat` enum; `annotations.rosetta` declares ten `Annotation`s. Before
phase 4 neither file declared anything marker-capable. After it, both do — and:

1. **Every unused builtin gets a permanent, unsuppressable marker.** In a typical model that is
   `basicType pattern`, `typeAlias calculation`, most of the `library function`s (`DateRanges`, `Adjust`,
   `Within`, `IsLeapYear`) and most annotations (`suppressWarnings`, `codeImplementation`, `externalConfig`,
   …). None of those kinds can carry `[suppressUnused]` (3b.3), the files are read-only, and the diagnostics
   are published — the phase-2 measurement observed both files inside the 40-file revalidation fan-out, so
   they are in the resource set and are validated.
2. **It undoes 3.9.** The kind filter's only remaining effect after phase 3 was dropping resources that
   declare nothing marker-capable, and the builtins were exactly that case. Make them marker-capable and the
   phase-2 revalidation pass fires for them again.

So skip candidates whose resource is a builtin. There are two existing precedents to copy rather than invent
a third spelling: `RosettaNamesAreUniqueValidationHelper#isBuiltin` (matches
`uri.trimFragment().lastSegment()` against `basictypes.rosetta` / `annotations.rosetta`) and
`RosettaGenerator#ignoredFiles`. Prefer factoring one predicate the helper and `IncomingReferenceChanges`
can share, since 3.9 needs the same answer — `markerCapableFragments` should return empty for a builtin
resource so the pass keeps skipping it. Filename matching is ugly; it is also what the codebase already
does, and `RosettaBuiltinsService` only exposes the canonical `classpath:` URIs, which the language server
does not actually use (see the workaround in `RosettaBuiltinsService#getModel`).

#### 4.4 Exemptions

Unchanged from phase 3 except for one addition: `Schema` honours `[suppressUnused]`. Everything else new is
un-suppressable by construction, so `isExempt` gains no other cases. `Data` keeps `[rootType]`; no new kind
has an analogue of it, of `superFunction`, or of an empty function body.

#### 4.5 Cost

Expected to be near zero for detection, for the reason 3.10 already demonstrated: `computeOutgoingReferences`
walks every resource in full and records *every* cross-reference regardless of target kind, so widening
candidacy adds no walks — only more `Set#contains` probes. The one term that does grow is `isReferenced`,
which loops the whole resource set per candidate: cost is `candidates × resources` hash lookups per validated
file, so a file with many root elements in a large workspace does more probing. At CDM scale (145 files) this
is thousands of O(1) lookups per file and should not be measurable; if it ever is, the fix is to union the
per-resource sets once per build rather than per candidate.

With 4.3 in place the phase-2 fan-out should be unchanged from phase 3. Without it, expect it to grow by the
builtins on most edits.

Re-run `EditLatencyBenchmark` (checked in, `rune-ide` test scope,
`-Drune.benchmark.model.dir=…`) on the same five scenarios and A/B against phase 3. Targets: no regression on
any row of the phase-3 table.

#### 4.6 Noise census

Re-run the phase-3 census on CDM and extend the table with a row per new kind. Two expectations worth
recording in advance so the measurement can contradict them: `Annotation` and `RosettaTypeAlias` are the
plausible noise sources in a library model (both un-suppressable), while `RosettaBody`/`Corpus`/`Segment` and
`RosettaExternalRuleSource` should be near-zero in a model that declares reports at all and 100% in one that
declares documentation elements without reports. CDM declares no reporting rules, so DRR is the model to
census for the documentation kinds.

#### 4.7 Tests

Extend `UnusedElementValidationTest`. One used-case and one unused-case per new kind, i.e. per row of 3b.1:

- type alias used as an attribute type → not flagged; declared and unused → flagged.
- annotation applied to a type → not flagged; applied only as `[metadata key]`, i.e. via
  `AnnotationRef.attribute` → not flagged (exercises the rollup); declared and unused → flagged.
- schema referenced by `[ingest MySchema]` → not flagged; `[suppressUnused]` on a schema → not flagged;
  declared and unused → flagged. Plus `[projection XML]` → the `SerializationFormat` enum not flagged.
- rule source referenced by `with source` on a report → not flagged; referenced only by another rule
  source's `extends` → not flagged; declared and unused → flagged.
- body referenced by a corpus → not flagged; body/corpus/segment referenced only from a `[docReference]`
  → not flagged; each declared and unused → flagged.
- `library function` called from an expression → not flagged; declared and unused → flagged.
- basic type / record type declared in a *non-builtin* test model and unused → flagged (the kinds are in
  scope; only the builtin *files* are excluded).

Plus the guards that encode the decisions rather than the mechanics:

- a `metaType`, used or not, is never flagged (3b.2).
- a `report` is never flagged, and no `NullPointerException` results from it having no name.
- nothing in `basictypes.rosetta` or `annotations.rosetta` is ever flagged (4.3). Easiest as a direct unit
  test over `UnusedElementHelper` against the builtin models in the resource set, since the LSP test harness
  asserts diagnostics for the test document only.
- a root element kind with no table entry falls back to the generic message and `UNUSED_DECLARATION` rather
  than throwing (4.2). Assert on whichever kind is left out of the table.

#### 4.8 Fallout

Wider than phase 3's, because documentation elements and annotations appear in far more test models than
unreferenced functions did. Every `rune-ide` test asserting an exact `getDiagnostics()` count is at risk
again; the `filterSevereDiagnostics` helper applied in 3.8 already covers the pattern, so this is a sweep
rather than a design problem. Two specific things to re-check:

- the 35 existing `UnusedElementValidationTest` cases — several of their models declare a `body`, `corpus`
  or `segment` and will now attract *additional* markers, so assertions that pin the full marker set need
  updating.
- `UnusedElementStalenessTest` — same, and it is the test that must not be weakened.

`rune-integration-tests` should again be untouched, since none of this is a validator `@Check`. Per
`CLAUDE.md`, capture the test count in both modules before and after.

#### 4.9 Documentation to update alongside the code

- `UnusedElementHelper`'s class javadoc and the `isCandidate` javadoc, which currently say "Reports, rule
  sources, annotations and type aliases are deliberately excluded" — the exact opposite of phase 4.
- `UnusedElementResourceValidator`'s class javadoc ("functions, types, enumerations and rules").
- `IncomingReferenceChanges#MARKER_CAPABLE_KINDS` and its javadoc, which mirrors `isCandidate` by hand.
  With candidacy now a rule, prefer testing `ROSETTA_NAMED.isSuperTypeOf(eClass)` minus the exclusions over
  extending the list to 15 entries.
- `annotations.rosetta` — the `[suppressUnused]` description says "for a function, type or enum"; it should
  name the kinds that can actually carry it (function, type, enum, schema, ~~and, after phase 5, type
  alias~~) rather than the kinds that get the marker, since those sets now differ.

### Phase 5 — make `RosettaTypeAlias` suppressible — DONE (2026-07-30)

Closes the one open item 4.6 left: 82% of CDM's type aliases were getting a permanent, un-suppressable
marker. Shipped. `rune-lang` compiles clean; `rune-ide` went from 122 tests / 0 skipped to 123 / 0;
`rune-integration-tests` went from 1489 / 21 skipped to 1491 / 21 skipped (two new tests, parsing +
formatting); full repo `mvn install` (all modules, checkstyle included) green.

| File | Change |
|---|---|
| `rune-lang/.../Rosetta.xtext` | `RosettaTypeAlias` rule gains `Annotations*`, placed **after** `RosettaTyped` and before `conditions += Condition*` — see below for why this placement, specifically, was required |
| `rune-lang/model/Rosetta.xcore` | `RosettaTypeAlias` supertypes gain `Annotated` (import already present, used by `Data`/`RosettaEnumeration`/etc.) |
| `rune-lang/.../formatting2/RosettaFormatter.java` | `format(RosettaTypeAlias)` formats `getAnnotations()` between the type call and the conditions, in both the inline and multiline branches |
| `rune-runtime/.../model/annotations.rosetta` | `[suppressUnused]` description now lists "function, type, enum, type alias or schema" |
| `rune-lang/.../validation/UnusedElementHelper.java` | **No behaviour change** — `isExempt` already tests `element instanceof Annotated` generically (this is what phase 4 built); its javadoc and `isInBuiltinResource`'s javadoc are updated to say so, since they previously listed type aliases as one of the kinds that cannot carry the annotation |
| `rune-integration-tests/.../parsing/RosettaParsingTest.java` | `testTypeAliasWithSuppressUnusedAnnotation` |
| `rune-integration-tests/.../formatting2/RosettaFormattingTest.java` | `testFormatTypeAliasWithAnnotation` |
| `rune-ide/.../validation/UnusedElementValidationTest.java` | `suppressUnusedOptsOutForTypeAlias`, modelled on the existing `suppressUnusedOptsOutForSchema`; class javadoc updated to list type alias among the annotatable kinds |

No change was needed in `UnusedElementResourceValidator`, `IncomingReferenceChanges`, or any issue-code
table: candidacy and marker-capability were never about whether a kind *can be annotated*, only about
whether it is named (4.2), so widening what `[suppressUnused]` accepts doesn't touch any of that — it only
changes what `isExempt` finds when it looks.

#### 5.1 The grammar change is not the one-line change 4.6 described, and the reason is worth recording

4.6 said the fix would be `Annotations*` on the `RosettaTypeAlias` rule, right after `RosettaDefinable?`,
matching where `Data`/`Choice`/`Enumeration` put it (immediately after the colon-and-definable, before the
rule's main content). That placement was tried first and does not compile:

```
[ERROR] .../RosettaSemanticSequencer.java:[4322,25] method
condition_Annotations_RosettaDefinable_RosettaNamed_RosettaTypeAlias_RosettaTyped_TypeParameters_RosettaTypeAlias(...)
is already defined in class com.regnosys.rosetta.serializer.RosettaSemanticSequencer
```

Confirmed by `git stash`-ing the grammar/xcore change and regenerating: the pre-phase-5 grammar produces no
such method at all, so this is newly introduced by adding `Annotations*`, not a pre-existing generator
quirk that happened to surface now.

Root cause: `RosettaTypeAlias` is reachable from two different "contexts" in Xtext's serializer-generator
sense — directly (`RosettaTypeAlias returns RosettaTypeAlias`) and through its containing alternative
(`RosettaRootElement returns RosettaTypeAlias`). For every other root element with `Annotations*`
(`Data`, `Choice`, `Enumeration`, `Schema`), both contexts compute the *same* cardinality for the
`annotations` feature (`*` in both), so the generator merges them into one `sequence_...` method — visible
directly in the generated code for `Schema`, whose doc comment lists both contexts against one shared
method. For `RosettaTypeAlias` with `Annotations*` placed before `RosettaTyped`, the two contexts instead
computed *different* cardinalities for the same feature — one context's constraint printed
`annotations+=AnnotationRef?` (0 or 1), the other `annotations+=AnnotationRef*` (0 or more) — genuinely
inconsistent, and documented as such by Xtext's own generator: the `sequence_` half of the duplicate gets
auto-commented out with `// This is probably a bug in Xtext's serializer, please report it here:
https://bugs.eclipse.org/bugs/enter_bug.cgi?product=TMF`, but the `condition_` half (the private boolean
dispatch predicate with the same computed name) is *not* auto-deduplicated, so two methods with identical
signatures and different bodies land in the same class and the compiler rejects it. Moving `Annotations*`
to after `RosettaTyped` (and before `conditions`) made both contexts agree on `*` again — confirmed by
regenerating and finding zero `[WARN] Skipped generating duplicate method]` output, then a clean compile.

Practical consequence: this is not a general rule ("annotations always go at the end") — it is specific to
whatever combination of optional elements precedes the annotation list in *this* rule (`TypeParameters?`
before the colon, `RosettaDefinable?` after it). Anyone adding `Annotations*` to a currently-un-annotated
root element rule should regenerate and grep for `Skipped generating duplicate method` before assuming the
`Data`-style placement (right after `RosettaDefinable?`) works; if it doesn't, moving the annotation list to
after the rule's mandatory content is the workaround used here, and is worth trying before anything more
invasive.

#### 5.2 Placement changes the formatted shape, and the test asserts the shape actually produced

Because `Annotations*` now sits structurally after `RosettaTyped`, and `format(RosettaTypeAlias)` goes
through `formattingUtil.formatInlineOrMultiline` (the same single-line-if-it-fits logic `Condition` uses),
adding an annotation always forces the multiline branch: `TrimmedMaxLineWidthDocument.createReplacements`
(`rune-lang/.../formatting2/TrimmedMaxLineWidthDocument.java:67`) throws `FormattingNotApplicableException`
the moment the trial single-line rendering contains a `\n`, and an annotation is formatted with a forced
newline before it. So `typeAlias max4String: string(...) [suppressUnused]` reformats to the type call and
the annotation each on their own indented line under `typeAlias max4String:`, not to the type call staying
on the declaration line with only the annotation dropping to the next line (the `Schema` shape, where there
is no inline/multiline choice to begin with). `testFormatTypeAliasWithAnnotation` asserts the shape actually
produced rather than the shape guessed in advance.

#### 5.3 Verification

- `mvn -pl rune-lang generate-sources` — clean, no duplicate-method warning.
- `mvn -pl rune-lang compile` — clean.
- Targeted: `RosettaParsingTest`, `RosettaFormattingTest` (`rune-integration-tests`) and
  `UnusedElementValidationTest`, `UnusedElementMarkerTest`, `UnusedElementStalenessTest` (`rune-ide`) all
  green.
- Full module runs: `rune-ide` 123/0 skipped, `rune-integration-tests` 1491/21 skipped (skip count
  unchanged from phase 4).
- Full repo `mvn install` (all modules, tests included, checkstyle enforced) — green.

### Post-phase-5 changes (2026-07-30/31, recorded 2026-08-01)

Three hardening changes shipped after the phase-5 record above was written (`e938edb9`), found by exercising
the feature rather than planned:

- **`FunctionDispatch` is exempt** (`UnusedElementHelper#isExemptFunction`). A dispatch case is not
  referenceable at all: `RosettaScopeProvider` filters `FunctionDispatch` out of the local-elements scope
  (`RosettaScopeProvider.java:396`), so nothing in the grammar can point at one and the walk could never see
  it as used — a dispatch case is used exactly when the main declaration of the same name is, and that is
  judged on the main declaration. Same correctness category as the `metaType` exclusion (3b.2), not a policy
  choice. Covered by `dispatchCasesOfACalledFunctionAreNotFlagged` and
  `dispatchCasesOfAnUncalledFunctionAreNotFlagged`.
- **The phase-2 pass respects `isIndexOnly()`** (`RosettaStatefulIncrementalBuilder`). An index-only project
  is deliberately never validated by the regular build, so the revalidation pass must not validate it either.
- **`isReferenced` iterates a copy of the resource list** (`UnusedElementHelper`). The walk resolves
  cross-references, and resolving a still-unresolved proxy demand-loads its resource into
  `resourceSet.getResources()` — a `ConcurrentModificationException` waiting to happen if the live list is
  iterated.

Plus one test-only rename (`9e5cebd5`, 2026-07-31): the test annotation attribute `tag` → `key` in
`UnusedElementValidationTest`, because `tag` is a hard keyword on the 9.x.x line (legacy
`RosettaSynonymRef`) and keeping the two branches' test content identical avoids a needless diff.

**Known limitation, discovered in review (2026-08-01): the phase-2 pass is single-project.** For a candidate
resource owned by another project, `result.getIndexState().getResourceDescriptions().getResourceDescription(uri)`
returns null, so `markerCapableFragments` comes back empty and the candidate is silently skipped —
cross-project incoming-reference changes leave stale markers. Whether this matters depends on whether the
deployed client ever configures multiple projects, which — like the `didOpen` question in 2.3 — is not yet
verified. The fix, if needed, has to live at the workspace level rather than in the per-project builder; see
the discussion in the PR/review notes.

**Resolved 2026-08-01: the limitation is not reachable in the Rosetta product — decision is
document-and-accept.** Investigated in rosetta-products (backend + UI); findings, with the evidence:

- **Production runs a single-project workspace.** The Monaco language client is created with exactly one
  workspace folder, `workspace.info.workingUri` = `<owner>/<workspace>/working`
  (`ui/src/app/core/services/language-server.service.ts:326` and `:334`); a captured production
  `initialize` message (`backend/execution-engine/src/test/resources/lsp/performance/init.json`) confirms
  it on the wire: `rootUri: file:///<user>/<ws>/working` plus that single `ws:` folder. The backend's
  `InitialiseParamsEnricher` only adds `initializationOptions` (a zip of that one directory plus
  rune-config files) and never adds folders; dependency models travel as *config files only*
  (`BspProvisionerFactory#getDependencyConfigPaths`), so there is nothing for a second project to be made
  of. `RosettaServerModule` binds no `IWorkspaceConfigFactory`/`IProjectDescriptionFactory`, so Xtext
  2.38's `ServerModule` defaults apply — `MultiRootWorkspaceConfigFactory` +
  `DefaultProjectDescriptionFactory`, one project per folder → one project. `indexOnly` appears nowhere in
  rosetta-products; the `isIndexOnly()` guard above is API compliance, not a configured case. *Caveat:* the
  `com.regnosys.bsp:bsp-server` jar that actually embeds the language server is built per model bundle and
  its source was not inspectable, but every input it receives is single-rooted.
- **Two editable, marker-bearing files cannot end up in different projects.** Dependency ("parent") model
  files are never materialized into the workspace — `WorkspaceFileServiceImpl#persistRuneFiles` copies only
  the model's own files; parents are served from the backend model cache, opened by the client as read-only
  documents, and overriding one creates an editable copy *inside* the workspace. Same single project either
  way.
- **The 2.3 `didOpen` question is resolved too** — see the note added to 2.3. Short version: the client
  opens every file once at workspace load and never re-opens on tab switch, so there is no self-heal-on-view
  — but every file is an open document for the whole session, so pushed diagnostics keep markers current
  the moment the phase-2 pass revalidates the declaring file. Phase 2 carried the whole load.

Revisit (hoist the `IncomingReferenceChanges` diff to the workspace level: run it once over all projects'
deltas against the full `ChunkedResourceDescriptions`, group candidates by owning project, and have each
owning `ProjectManager` unload + revalidate its own) only if a deployment ever runs multi-root workspaces or
materializes dependency models as separate folders. Pointing the per-project pass at the full index is *not*
a fix — it would detect cross-project candidates but cannot revalidate another project's resources, so it
costs the diff without curing the staleness.

### Review changes (2026-08-13): one issue code for every kind — DONE

PR review asked why each kind needs its own issue code rather than one `UNUSED_DECLARATION`. Taken, and
shipped: `UNUSED_FUNCTION`, `UNUSED_TYPE`, `UNUSED_ENUMERATION`, `UNUSED_REPORTING_RULE`,
`UNUSED_ELIGIBILITY_RULE`, `UNUSED_TYPE_ALIAS`, `UNUSED_ANNOTATION`, `UNUSED_SCHEMA` and the `UNUSED_CODES`
set are all deleted. Every unused marker now carries `UNUSED_DECLARATION`, and the kind of declaration is
named only in the message — which it already was.

Why the "room for a kind-specific quick fix later" rationale did not survive contact with the API:

- `AbstractDeclarativeIdeQuickfixProvider#getFixMethodPredicate` dispatches on the code string alone, and the
  `@QuickFix` method receives only the `DiagnosticResolutionAcceptor` — the diagnostic and the `EObject`
  arrive later, via `DiagnosticResolution#configure`. So one code still permits a kind-specific *fix*
  (`ISemanticModification`/`ITextModification` both get the `EObject`), but not a kind-specific *label*, and
  not withholding a fix from a kind.
- The one future fix that would want that is "add `[suppressUnused]`", which only applies to the kinds that
  can carry annotations (3b.3). If it is ever built, splitting a code back out then is a one-line change to
  the `toDiagnostic` gate — nothing outside this repo reads these strings, and they have never been
  released.

Fallout, all mechanical: `RosettaLanguageServerImpl#toDiagnostic` gates on
`UNUSED_DECLARATION.equals(issue.getCode())`, which puts the constant on the left and so subsumes the phase-3
null-code guard (the `UnusedElementValidationTest` case covering a null code stays as the regression test);
`KindDescriptor` loses its `issueCode` component; the one-field `Marker` record collapses, so
`markerFor` → `markerMessageFor` returning the message; `UnusedElementMarkerTest` loses its five issue-code
assertions. The end-to-end guarantee those assertions stood in for — that the published diagnostic is a
`Hint` with the `Unnecessary` tag — is asserted by `UnusedElementValidationTest` per kind, so nothing is
uncovered.

Verified: `mvn -o install -pl rune-ide` green, 125 tests / 0 skipped, including 67 `UnusedElement*`
(60 validation + 2 staleness + 5 marker), with the staleness pair unchanged. `rune-integration-tests`
test-compiles against the reduced `RosettaIssueCodes` (nothing there ever referenced the deleted constants).
*(The 123-test `rune-ide` baseline quoted in part 2 §4 predates this and is 2 short of what the module
actually runs; this change adds and removes no tests.)*

## 5. Open decisions — RESOLVED (2026-07-30)

All of these were settled before phase 3 was implemented; the answers and what they cost are recorded under
phase 3 above. In short: rules in scope with no opt-out and no grammar change; type aliases excluded; types
always on; the benchmark harness recreated and checked in. Transitivity stays non-transitive as recommended.
Rule sources remain out of scope.

**Two of them were then reopened and re-decided on 2026-07-30, when the scope became "every named root
element" (see the header and phase 4): type aliases are now *in*, and so are rule sources.** The bullets
below are left as written; the *Scope* and *Rule sources as candidates* bullets are superseded, and the
"grammar change appetite" question is now answered globally rather than per kind — no grammar change, and
kinds that cannot be annotated simply cannot be suppressed (3b.3).

The **noise** worry below turned out to be the wrong way round — types
attract markers on 3% of CDM declarations against 18% for functions — so read the bullet as the hypothesis it
was, not a finding.

- **Noise.** Unlike functions, types are a model's public API — in a library model such as CDM a large
  share of types have no in-model reference and would grey out. Data point for the *existing* function
  marker, from the CDM run in 3.10: **235 hints against 1303 declared functions (~18%)**. CDM also declares
  759 types and 279 enums, none of which are candidates yet. Treat 18% as an upper bound: 44 of CDM's files
  are `ingest-fpml-*` and fail to resolve `fpml.*` types in this workspace, so their call sites are partly
  invisible to the walk and inflate the count. `Hint` severity and `[rootType]` soften
  this, but types may warrant being opt-in via a client setting. Enums are much lower risk. Rules sit in
  between: ~12% flagged on the repo's own models (3a.5), and a rule with no report/`[ruleReference]` is
  genuinely dead code — this is arguably the *most* useful of the three new kinds.
- **Transitivity.** Current logic is non-transitive: a type used only by an unused type counts as used.
  Consistent with the function behaviour and cheaper, so keep it — but it means peeling unused code one
  layer per edit. Same for a reporting rule referenced only from a `rule source` no report uses.
- ~~**Scope.**~~ **Superseded by phase 4 — type aliases are in.** `Choice extends Data`, so choice types are
  included for free. `RosettaTypeAlias` (`Rosetta.xtext:268`) would also come free via 3.2 — decide whether
  to include it.
- ~~**Grammar change appetite.**~~ **Answered globally by phase 4: no grammar change, anywhere.** 3a.1 is the
  only structural change in this plan. If it is unwanted, the
  fallback is rules without an opt-out — acceptable for a `Hint`, but it makes published rule libraries
  noisy. Sequencing option: ship types + enums (which need no grammar change) first, then rules with the
  grammar change as a separate PR.
- ~~**Rule sources as candidates.**~~ **Superseded by phase 4 — rule sources are in, without an opt-out.**
  A `rule source` that no report references is also dead code and would be
  a one-line addition (`RosettaExternalRuleSource extends RosettaRootElement, RosettaNamed`, so the
  `ROSETTA_NAMED__NAME` marker location works), but like `RosettaRule` it is not `Annotated` and would need
  the same treatment as 3a.1. Out of scope for now.

## 6. Not recommended

- Overriding `IResourceDescription.Manager#isAffected` to spot changed *incoming* references. Tried and
  abandoned in phase 2 — the data is not available at that point in the build, and forcing it corrupts
  linking. See phase 2 for the full reasoning.
- Making detection index-primary. It buys little for detection itself (2.2 forces a hybrid anyway, and
  there is no reverse lookup), while adding two invalidation stories. Revisit only if find-references /
  rename / call-hierarchy for functions, types and enums is wanted in `rune-ide` — none exist today, and
  that, not this marker, is the payoff that would justify it.
- Touching `createEObjectDescriptions`. See 2.1.
