# Xtend to Java Migration Plan

This document outlines the plan for migrating Xtend code to Java in the rune-dsl project.

## Migration Guidelines

When migrating Xtend code to Java, follow these principles:

1. The generated Java code from an Xtend file should only be used as inspiration. The resulting Java code should use best practices such as meaningful variable names and using triple quote multiline strings instead of hard-to-read string builder patterns.
2. Avoid using Xtend-specific libraries in the migrated Java code. Remove annotations such as `@Extension`.
3. Replace Xtend-specific libraries and extension methods with Java's standard operations such as streams and lambdas.
4. Pay special attention to string templates (triple quotes) as they require careful migration.
5. Test thoroughly after migration to ensure functionality is preserved.
6. When you fail to migrate a method/test, indicate this in a comment at the top of the file, and add a note in the migration plan to highlight this.
7. When failing to migrate a method/test, add the original Xtend source code of that method or test as a comment inside the migrated method.

## Step-by-Step Migration Guide for AI

### Prerequisites
1. Take into account all guidelines as written in the migration plan.
2. Read the Xtend documentation located in the xtend-docs folder.
3. Analyze existing Java code to find best practices and coding style.

### Migration Process
To migrate a test file, perform the following steps:

1. Run the generated Java test and note down the number of tests it ran in the migration plan.
2. Look at the Xtend source code of the test to migrate.
3. Look at the corresponding generated Java code under xtend-gen.
4. Create a Java version of the test (the resulting code should look as closely as possible to the original Xtend code).
5. Remove the generated Java file to make sure there is no duplicate classes.
6. Run the new Java test.
7. Try to fix failing tests.
8. Rerun and try to fix failing tests once more.
9. If you do not succeed, copy the original Xtend source code of the test as a comment in the migrated method, highlight this with a comment at the top of the file, and add a note to the migration plan.
10. Verify that the number of tests that ran is the same as noted down before. If not, make a note of it in the migration plan.
11. Delete the Xtend source file.
12. Tick off the file in the migration plan.

To migrate a non-test file, perform the following steps:

1. Look at the Xtend source code of the class to migrate.
2. Look at the corresponding generated Java code under xtend-gen.
3. Create a Java version of the class (the resulting code should look as closely as possible to the original Xtend code).
4. Remove the generated Java file to make sure there is no duplicate classes.
5. Review the resulting Java file and make sure that all methods of the original Xtend file are still present.
6. Delete the Xtend source file.
7. Tick off the file in the migration plan. If you failed to migrate anything, make a note of it.


## Status

The bulk of the migration is complete. Everything below has been converted to Java
and its `.xtend` source removed:

- **rune-ide** — all test classes (`ContentAssistTest`, `QuickFixTest`, `FormattingTest`,
  `RosettaDocumentationProviderTest`, `InlayHintTest`, `Issue785`, `SemanticTokenTest`,
  `HandleParseErrorGracefullyTest`, `AbstractRosettaLanguageServerTest`,
  `AbstractRosettaLanguageServerValidationTest`).
- **rune-integration-tests** — all test classes and helpers, including the large
  generator-regression tests (`FunctionGeneratorTest`, `FunctionOperationGeneratorTest`,
  `FunctionGeneratorHelper`, `ListOperationTest`, `RosettaBinaryOperationTest`,
  `TypeCoercionTest`, the `object` generator tests, the `qualify` tests,
  `DocReferenceTest`, `DocumentationSamples`, `RosettaExpressionsTest`,
  `ExpressionParserTest`, `RosettaTypeProviderXtendTest`, `ModelGeneratorUtilTest`, …).
- **rune-testing** — all helpers (`CodeGeneratorTestHelper`, `CustomConfigTestHelper`,
  `ExpressionJavaEvaluatorService`, `ExpressionValidationHelper`, `ExpressionParser`,
  `ModelHelper`).
- **rune-runtime** — `MapperMaths`.
- **rune-lang** — the formatters (`RosettaFormatter`, `RosettaExpressionFormatter`),
  `BindableType`, `TestResourceAwareFSAFactory`, and the previously-migrated
  infrastructure (`RosettaGenerator`, `RosettaInternalGenerator`,
  `ImportingStringConcatenation`, `DeepPathUtilGenerator`, `ValidatorsGenerator`,
  `RosettaAttributeExtensions`, `RosettaFunctionExtensions`, `RosettaQualifiedNameProvider`,
  …).
- **rune-tools** — `UnnecessaryElementsRemoverTest`.

## Remaining Xtend files

What remains are the **code generators** (and the xcore serializer fragment). These
are being migrated to the fluent `CodeRenderer`/`CodeWriter` API rather than
transcribed as plain Java — see the `migrate-xtend-generator` skill and the reference
migration `DeepPathUtilGenerator` (PR #1255). Removing these is the last step before
Xtend can be dropped as a dependency, which also lets `rune-lang` move off Java 17
(it is currently pinned there for Xtend interoperability).

### rune-lang — code generators
- [ ] generator/java/condition/ConditionGenerator
- [ ] generator/java/enums/EnumGenerator
- [ ] generator/java/expression/ExpressionGenerator
- [ ] generator/java/expression/TypeCoercionService
- [ ] generator/java/function/FunctionGenerator
- [ ] generator/java/function/LabelProviderGenerator
- [ ] generator/java/object/JavaPackageInfoGenerator
- [ ] generator/java/object/MetaFieldGenerator
- [ ] generator/java/object/ModelMetaGenerator
- [ ] generator/java/object/ModelObjectBoilerPlate
- [ ] generator/java/object/ModelObjectBuilderGenerator
- [ ] generator/java/object/ModelObjectGenerator
- [ ] generator/java/object/validators/CardinalityValidatorGenerator
- [ ] generator/java/object/validators/OnlyExistsValidatorGenerator
- [ ] generator/java/object/validators/TypeFormatValidatorGenerator
- [ ] generator/java/reports/ReportGenerator
- [ ] generator/java/reports/RuleGenerator

### rune-lang — generator utilities/helpers
- [ ] generator/java/enums/EnumHelper
- [ ] generator/java/util/ImportManagerExtension
- [ ] generator/java/util/ModelGeneratorUtil
- [ ] generator/java/util/RecordJavaUtil
- [ ] generator/java/util/RosettaGrammarUtil

### rune-xcore-plugin-dependencies
- [ ] xcore/generator/serializer/RosettaSerializerFragment
