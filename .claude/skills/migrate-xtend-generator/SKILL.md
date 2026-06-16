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

1. **Regression test**: create
   `rune-integration-tests/src/test/resources/generation-regression-tests/<name>/model/*.rosetta`
   covering the generator's interesting cases, plus a test class extending
   `AbstractJavaGeneratorRegressionTest`. Generate the `expected/` files by
   temporarily flipping `UPDATE_EXPECTATIONS` to `true` in
   `AbstractJavaGeneratorRegressionTest`, running the test, and flipping it
   back. Review the generated expectations before committing them.
2. **Parity check against the old generator**: check out the commit that still
   has the `.xtend` version (e.g. in a second worktree), copy the same model
   and test class there, generate expectations with the old generator, and
   diff against the new output. Aim for token-identical output; only
   whitespace (tabs vs spaces, trailing whitespace, line endings) should
   differ. Investigate any non-whitespace diff before proceeding.
3. Run the functional tests that execute generated code for the feature (e.g.
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
- The bridges in `JavaExpression` (`CodeWriterTargetStringConcatenation`,
  `TargetStringConcatenationCodeWriter`, the `StringConcatenationClient`
  overload of `from`) and the legacy
  `com.regnosys.rosetta.generator.TargetLanguageRepresentation` interface are
  migration-only; the final cleanup deletes them together with the Xtend
  machinery and adds a fluent debug writer for `JavaExpression.toString()`.
