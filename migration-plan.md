# Xtend to Java Migration Plan

This document outlines the plan for migrating Xtend code to Java in the rune-dsl project.

## Migration Guidelines

When migrating Xtend code to Java, follow these principles:

1. The generated Java code from an Xtend file can be used to understand the semantics, but the goal is to rewrite in idiomatic Java.
2. Avoid using Xtend-specific libraries in the migrated Java code.
3. Use modern Java features (streams, lambdas) instead of Xtend-specific constructs like extension methods.
4. Pay special attention to string templates (triple quotes) as they require careful migration.
5. Test thoroughly after migration to ensure functionality is preserved.

## Files to Migrate

### rosetta-ide (10 files)

#### Project Files (0 files)

No project files to migrate.

#### Test Files (10 files)

##### Files without usage of string template features (7 files)
- [ ] ContentAssistTest
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

### rosetta-integration-tests (37 files)

#### Project Files (0 files)

No project files to migrate.

#### Test Files (37 files)

##### Files without usage of string template features (32 files)
- [ ] DocReferenceTest
- [ ] ExpressionFormatterTestHelper
- [ ] RosettaExpressionFormattingTest
- [ ] RosettaFormattingTest
- [ ] ListOperationTest
- [ ] RosettaCountOperationTest
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
- [ ] Issue844
- [ ] Issue868
- [ ] RosettaFragmentProviderTest
- [ ] RosettaExpressionsTest
- [ ] ExpressionParserTest
- [ ] RosettaTypeProviderXtendTest
- [ ] SubtypeRelationTest
- [ ] ChoiceValidatorTest
- [ ] EnumValidatorTest
- [ ] RosettaValidatorTest
- [ ] TypeValidatorTest

##### Files with usage of string template features (5 files)
- [ ] DocumentationSamples
- [ ] ExpressionGeneratorTest
- [ ] RosettaBinaryOperationTest
- [ ] RosettaExistsExpressionTest
- [ ] ModelObjectGeneratorTest

### rosetta-lang (35 files)

#### Project Files (35 files)

##### Files without usage of string template features (14 files)
- [ ] RosettaFormatter
- [ ] RosettaGenerator
- [ ] RosettaInternalGenerator
- [ ] RosettaOutputConfigurationProvider
- [ ] EnumHelper
- [ ] JavaDependencyProvider
- [ ] RuleGenerator
- [ ] ImportingStringConcatenation
- [ ] RosettaGrammarUtil
- [ ] ExpandedAttribute
- [ ] TestResourceAwareFSAFactory
- [ ] RosettaAttributeExtensions
- [ ] RosettaFunctionExtensions
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
- [ ] ValidatorsGenerator
- [ ] ReportGenerator
- [ ] ImportManagerExtension
- [ ] ModelGeneratorUtil
- [ ] RecordJavaUtil
- [ ] IterableUtil
- [ ] RosettaSimpleValidator

### rosetta-runtime (1 file)

#### Project Files (1 file)

##### Files with usage of string template features (1 file)
- [ ] MapperMaths

### rosetta-testing (6 files)

#### Project Files (6 files)

##### Files without usage of string template features (3 files)
- [ ] CustomConfigTestHelper
- [ ] ExpressionParser
- [ ] ExpressionValidationHelper

##### Files with usage of string template features (3 files)
- [ ] CodeGeneratorTestHelper
- [ ] ExpressionJavaEvaluatorService
- [ ] ModelHelper

### rosetta-tools (1 file)

#### Project Files (0 files)

No project files to migrate.

#### Test Files (1 file)

##### Files with usage of string template features (1 file)
- [ ] UnnecessaryElementsRemoverTest

### rosetta-xcore-plugin-dependencies (1 file)

#### Project Files (1 file)

##### Files with usage of string template features (1 file)
- [ ] RosettaSerializerFragment
