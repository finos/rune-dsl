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


## Files to Migrate

### rune-ide (10 files)

#### Project Files (0 files)

No project files to migrate.

#### Test Files (10 files)

##### Files without usage of string template features (7 files)
- [x] ContentAssistTest
- [ ] FormattingTest
- [ ] RosettaDocumentationProviderTest
- [ ] InlayHintTest
- [ ] Issue785
- [ ] QuickFixTest
- [ ] SemanticTokenTest

##### Files with usage of string template features (3 files)
- [ ] HandleParseErrorGracefullyTest
- [ ] AbstractRosettaLanguageServerTest
- [ ] AbstractRosettaLanguageServerValidationTest

### rune-integration-tests (37 files)

#### Project Files (0 files)

No project files to migrate.

#### Test Files (37 files)

##### Files without usage of string template features (32 files)
- [ ] DocReferenceTest
- [ ] ExpressionFormatterTestHelper
- [x] RosettaExpressionFormattingTest
- [x] RosettaFormattingTest
- [ ] ListOperationTest
- [x] RosettaCountOperationTest
- [ ] CalculationFunctionGeneratorTest
- [ ] FunctionGeneratorHelper
- [ ] FunctionGeneratorTest
- [ ] EnumGeneratorTest
- [ ] ExternalHashcodeGeneratorTest
- [ ] GlobalKeyGeneratorTest
- [ ] ModelMetaGeneratorFilteredNamespaceTest
- [ ] ModelMetaGeneratorTest
- [ ] ModelObjectBoilerPlateTest
- [ ] ModelObjectBuilderGeneratorTest
- [ ] RosettaExtensionsTest
- [ ] RosettaModelTest
- [ ] RosettaObjectInheritanceGeneratorTest
- [ ] RosettaProcessorTest
- [ ] QualifyTestHelper
- [ ] RosettaQualifyEventTest
- [ ] RosettaQualifyProductTest
- [ ] ModelGeneratorUtilTest
- [x] Issue844
- [x] Issue868
- [x] RosettaFragmentProviderTest
- [ ] RosettaExpressionsTest
- [ ] ExpressionParserTest
- [ ] RosettaTypeProviderXtendTest
- [x] SubtypeRelationTest
- [x] ChoiceValidatorTest
- [x] EnumValidatorTest
- [x] RosettaValidatorTest
- [x] TypeValidatorTest

##### Files with usage of string template features (5 files)
- [ ] DocumentationSamples
- [x] ExpressionGeneratorTest
- [ ] RosettaBinaryOperationTest
- [x] RosettaExistsExpressionTest
- [ ] ModelObjectGeneratorTest

### rune-lang (35 files)

#### Project Files (35 files)

##### Files without usage of string template features (14 files)
- [ ] RosettaFormatter
- [x] RosettaGenerator
- [x] RosettaInternalGenerator
- [x] RosettaOutputConfigurationProvider
- [ ] EnumHelper
- [x] JavaDependencyProvider
- [ ] RuleGenerator
- [x] ImportingStringConcatenation
- [ ] RosettaGrammarUtil
- [ ] ExpandedAttribute
- [ ] TestResourceAwareFSAFactory
- [x] RosettaAttributeExtensions
- [x] RosettaFunctionExtensions
- [ ] RosettaQualifiedNameProvider

##### Files with usage of string template features (21 files)
- [ ] RosettaExpressionFormatter
- [ ] ConditionGenerator
- [ ] EnumGenerator
- [ ] DeepPathUtilGenerator
- [ ] ExpressionGenerator
- [ ] TypeCoercionService
- [ ] FunctionGenerator
- [ ] LabelProviderGenerator
- [ ] JavaPackageInfoGenerator
- [ ] MetaFieldGenerator
- [ ] ModelMetaGenerator
- [ ] ModelObjectBoilerPlate
- [ ] ModelObjectBuilderGenerator
- [ ] ModelObjectGenerator
- [x] ValidatorsGenerator
- [ ] ReportGenerator
- [ ] ImportManagerExtension
- [ ] ModelGeneratorUtil
- [ ] RecordJavaUtil
- [x] IterableUtil
- [x] RosettaSimpleValidator

### rune-runtime (1 file)

#### Project Files (1 file)

##### Files with usage of string template features (1 file)
- [ ] MapperMaths

### rune-testing (6 files)

#### Project Files (6 files)

##### Files without usage of string template features (3 files)
- [ ] CustomConfigTestHelper
- [x] ExpressionParser
- [ ] ExpressionValidationHelper

##### Files with usage of string template features (3 files)
- [ ] CodeGeneratorTestHelper
- [ ] ExpressionJavaEvaluatorService
- [x] ModelHelper

### rune-tools (1 file)

#### Project Files (0 files)

No project files to migrate.

#### Test Files (1 file)

##### Files with usage of string template features (1 file)
- [x] UnnecessaryElementsRemoverTest

### rune-xcore-plugin-dependencies (1 file)

#### Project Files (1 file)

##### Files with usage of string template features (1 file)
- [ ] RosettaSerializerFragment
