---
name: migrate-xtend-generator
description: Migrate an Xtend code generator (rune-lang) to the fluent Java CodeRenderer/CodeWriter API. Use when converting a *.xtend generator class to Java, when asked to "migrate a generator", or when reviewing such a migration. Covers the porting steps, Xtend-to-Java translation patterns, output conventions, verification via regression tests, and parity checking against the old generator.
---

# Migrating an Xtend code generator to the fluent API

The reference migration is `DeepPathUtilGenerator` (PR #1255): compare
`DeepPathUtilGenerator.java` on the current branch with
`DeepPathUtilGenerator.xtend` on the last commit before the migration to see
every pattern below applied to a real generator.

## Architecture

- Extend `FluentJavaClassGenerator<T, C>` (or `FluentRObjectJavaClassGenerator`
  when `T` is an `RObject`) instead of `XtendJavaClassGenerator` /
  `RObjectJavaClassGenerator`.
- `generateClass` returns a `CodeRenderer` instead of a `StringConcatenationClient`.
- The renderer is rendered **once** into a `RecordingCodeWriter`, which claims
  imports and identifiers in the file scope while recording; the recording is
  then replayed with all identifiers resolved. Identifiers may therefore be
  created while rendering (just like Xtend templates do mid-template).
- Even so, prefer separating *model building* (compute methods, scopes,
  identifiers; capture in small records) from *rendering* (pure writes) when it
  doesn't contort the code — it makes generators easier to unit-test and read.

## Porting steps

1. `streamObjects` / `createTypeRepresentation` / `getSource`: port 1:1.
2. Transcribe the `'''…'''` template into `render*` methods using
   `out.write` / `out.writeln` / `out.indented(() -> …)` / `out.join(items, ", ", item -> …)`.
3. **Do not hand-render expression logic** (getters, null checks, exists
   checks, coercions). Keep delegating to `ExpressionGenerator`,
   `TypeCoercionService`, and the `JavaStatementBuilder` API exactly as the
   Xtend code did — the resulting `JavaStatement`/`JavaExpression` objects
   implement `CodeRenderer` and can be written directly to the writer. A
   hand-rendered reimplementation silently changes the generated output style
   and duplicates coercion semantics.
4. Wrap ad-hoc expression fragments with `JavaExpression.from(CodeRenderer, JavaType)`
   (not the legacy `StringConcatenationClient` overload).
5. Delete the `.xtend` file in the same commit.
6. If a piece of Xtend infrastructure loses its last user, delete it; if it
   still has users, mark it `@Deprecated` pointing at the fluent replacement.

## Xtend → Java translation patterns

| Xtend | Java |
|---|---|
| `@Inject extension JavaTypeTranslator` + `attr.toMetaJavaType` | `@Inject private JavaTypeTranslator typeTranslator;` + `typeTranslator.toMetaJavaType(attr)` |
| `name.toFirstLower` / `name.toFirstUpper` | statically imported `StringUtils.uncapitalize(name)` / `capitalize(name)` (commons-lang3) |
| `«FOR x : xs SEPARATOR ', '»…«ENDFOR»` | `out.join(xs, ", ", x -> …)` |
| `«IF cond»…«ENDIF»` around lines | plain `if` around `out.writeln(…)` calls |
| template indentation | `out.indented(() -> …)` |
| `val t = if (x instanceof RChoiceType) x.asRDataType else x` | a small `normalizeChoiceType(RType)` helper |
| `new HashSet<>()` for collected output | `LinkedHashSet` / `LinkedHashMap` — deterministic iteration order |
| `xs.reverseView` | `com.google.common.collect.Lists.reverse(xs)` |

Expect roughly +35 lines of fixed boilerplate per generator (license header,
explicit imports, `@Inject` fields on two lines) plus 20–40% body growth from
Java verbosity. The algorithm itself should port mechanically — if it doesn't,
you are probably reimplementing instead of transcribing.

## Output conventions

- Newline is always `"\n"`; indentation is 4 spaces (`CodeWriterConfig`
  defaults). Legacy Xtend output used tabs and platform line separators, so
  whitespace-only diffs in downstream generated code are expected and accepted.
- Blank lines are truly empty (the writer never emits trailing whitespace);
  Xtend used to leak the template's indentation into blank lines.
- New files (main and test) carry the Apache license header.

## Verification

1. **Fixtures first, on the old generator**: before touching the `.xtend`, make
   sure a regression fixture under
   `rune-integration-tests/src/test/resources/generation-regression-tests/<name>/`
   exercises every branch of the generator's template (every `«IF»`, every
   `«FOR»` with zero, one and several items, optional values that are `null`).
   If one is missing, add the model and a test class extending
   `AbstractJavaGeneratorRegressionTest`, generate `expected/` with the
   **unmigrated** generator and commit it on its own. Every layout regression
   in earlier migrations (#1293, #1376, #1378) was in output no fixture
   covered at the time.
2. **Migrate, then regenerate**: run the regression tests with
   `-Drune.updateExpectations` to rewrite `expected/`, e.g.
   `mvnd -o verify -pl rune-integration-tests -am -Dtest='*RegressionTest' -Dsurefire.failIfNoSpecifiedTests=false -Drune.updateExpectations`.
3. **Parity check**: the diff of `expected/` must be whitespace only. Tabs
   become four spaces and trailing whitespace goes; nothing else may change,
   not even a blank line. This lists every file that differs in anything
   else:
   ```bash
   for f in $(git diff --name-only -- '*/expected/*'); do
     diff -q <(git show HEAD:"$f" | expand -t 4 | sed 's/[[:space:]]*$//') \
             <(sed 's/[[:space:]]*$//' "$f") > /dev/null || echo "$f"
   done
   ```
   Investigate every file it prints. A difference is acceptable only when the
   old output was itself wrong, and then it needs a sentence in the PR.
4. Run the functional tests that execute generated code for the feature (e.g.
   the relevant tests in `FunctionGeneratorTest`), plus all existing
   `*RegressionTest` classes — a migration must not change other generators'
   output.

## Pitfalls

- `GeneratorScope.getActualName` **closes the scope** on first call. Never
  resolve identifier names while the file is still being built — write
  `GeneratedIdentifier` objects to the writer and let replay resolve them.
- The line-ending normalization in `AbstractJavaGeneratorRegressionTest` is
  temporary scaffolding for the migration period (legacy generators emit
  platform separators on Windows); remove it when the last Xtend generator is
  gone.
- The migration-only bridges — `CodeWriterTargetStringConcatenation` and
  `TargetStringConcatenationCodeWriter` (both standalone classes under
  `generator/java/util/`, extracted out of `JavaExpression` in PR #1293),
  `JavaExpression`'s legacy `StringConcatenationClient` overload of `from`,
  and the legacy `com.regnosys.rosetta.generator.TargetLanguageRepresentation`
  interface — exist only to let not-yet-migrated Xtend templates interop with
  the fluent `CodeWriter` API. The final cleanup deletes them together with
  the Xtend machinery and adds a fluent debug writer for
  `JavaExpression.toString()`.
- Xtend and Java disagree on these, and each one has changed generated output
  in an earlier migration:
  - `«x»` with `x == null` renders nothing, but `StringBuilder.append(null)`
    and `"…" + null` render `null`. `CodeWriter.write(null)` renders
    nothing, so write to the `CodeWriter` instead of building strings.
  - A template line holding only `«IF»`, `«ELSE»`, `«ENDIF»`, `«FOR»` or
    `«ENDFOR»` produces no output line. Do not turn it into an
    `out.newline()`, and keep every blank line the template does emit.
  - `==` in Xtend is `equals`, `===` is identity. `?.` is null-safe. `head`
    and `last` return `null` on an empty list; `forall` is `true` on one.
- Render every Java string literal with `JavaLiteral.STRING(text)` (or
  `JavaLiteral.NULL`), never `"\"" + text + "\""` or a hand-picked escaper.
  Model text in javadoc goes through `ModelGeneratorUtil.escape`.
- Write multi-line text in one `out.write`: the writer indents every line.
  Do not split it into lines yourself.
- Reuse an existing representation instead of rebuilding it by hand, e.g.
  `out.write(javaClass.asClassDeclaration())` /
  `asInterfaceDeclaration()`.
- Port what is used; delete what is not. Before converting a method with no
  caller in rune-dsl, check rosetta-code-generators and rune-python-generator;
  if nothing calls it there either, delete it.
- Keep the design: transcribe with the fluent API rather than reworking how a
  generator is structured. A reviewer will ask why a pattern changed.
- Keep public and protected signatures stable where another class (or a
  downstream repo) calls them, unless you also migrate every caller.
- Style in review: Apache license header on new files; `java.*` imports in
  their own group; a comment wherever a type stays fully qualified because of
  a name clash; locals declared with a readable general type (`JavaType`, not
  `JavaParameterizedType<…>`).
