package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;
import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.List;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*;

/*
 * Do not write any more tests in here, because this approach of putting every test type in one place is messy
 * and hard to navigate, better to split tests concepts into suites.
 * The appropriate test suites can be found by looking for [TestType]ValidatorTest.java where test type could be Expression for example ExpressionValidatorTest.
 */
@Deprecated
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaValidatorTest extends AbstractValidatorTest {
    @Inject
    private RosettaValidationTestHelper validationTestHelper;
    @Inject
    private ModelHelper modelHelper;
    @Inject
    private ExpressionParser expressionParser;

    @Test
    void testProjectAnnotationHasValidTargetFormat() {
        assertNoIssues("""
				func Foo:
					[projection XML]
				""");
    }

    @Test
    void testProjectAnnotationHasInvalidTargetFormat() {
        assertIssues("""
				func Foo:
					[projection]
				""", """
                ERROR (null) 'The `projection` annotation must have a target format such as JSON, XML or CSV' at 5:3, length 10, on AnnotationRef
                """);
    }

    @Test
    void testIngestAnnotationHasValidSourceFormat() {
        assertNoIssues("""
				func Foo:
					[ingest JSON]
				""");
    }

    @Test
    void testIngestAnnotationHasSourceFormat() {
        assertIssues("""
				func Foo:
					[ingest]
				""", """
                ERROR (null) 'The `ingest` annotation must have a source format such as JSON, XML or CSV' at 5:3, length 6, on AnnotationRef
                """);
    }

    @Test
    void testMultipleTransformAnnotationsCanNotBeUsed() {
        assertIssues("""
				func Foo:
					[ingest JSON]
					[enrich]
				""", """
                ERROR (null) 'Only one transform annotation allowed.' at 6:2, length 8, on AnnotationRef
                """);
    }

    @Test
    void testTransformAnnotationShouldBeUsedOnFunction() {
        var model1 = modelHelper.parseRosetta("""
				type Foo:
					[ingest JSON]
				""");

        validationTestHelper.assertError(model1, ROSETTA_NAMED, null, "Transform annotations only allowed on a function.");

        var model2 = modelHelper.parseRosetta("""
				type Foo:
					[enrich]
				""");

        validationTestHelper.assertError(model2, ROSETTA_NAMED, null, "Transform annotations only allowed on a function.");

        var model3 = modelHelper.parseRosetta("""
				type Foo:
					[projection]
				""");

        validationTestHelper.assertError(model3, ROSETTA_NAMED, null, "Transform annotations only allowed on a function.");
    }

    @Test
    void testConditionShouldBeSingleCardinality() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					condition C:
						[True, False]
				""");

        validationTestHelper.assertWarning(model, CONDITION, null, "Expecting single cardinality. A condition should be single cardinality");
    }

    @Test
    void testOnlyExistsOnMetaIsNotValidOnSymbolReferences() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					a int (0..1)
					b int (0..1)
					c int (0..1)

				type Bar:
					b int (1..1)

				func MyFunc:
					inputs:
						foosWithMeta Foo (1..*)
							[metadata scheme]
					output:
						result Foo (1..*)
					add result:
						foosWithMeta filter (b, scheme) only exists
				""");

        validationTestHelper.assertError(model, ROSETTA_SYMBOL_REFERENCE, null,
                "Invalid use of `only exists` on meta feature scheme"
        );
    }

    @Test
    void testOnlyExistsOnMetaIsNotValidOnFeatureCalls() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					a int (0..1)
					b int (0..1)
					c int (0..1)

				type Bar:
					b int (1..1)

				func MyFunc:
					inputs:
						foo Foo (1..1)
							[metadata scheme]
					output:
						result boolean (1..1)
					set result:
						(foo -> b, foo -> scheme) only exists
				""");

        validationTestHelper.assertError(model, ROSETTA_FEATURE_CALL, null,
                "Invalid use of `only exists` on meta feature scheme"
        );
    }

    @Test
    void testCanUseMixOfImportAliasAndFullyQualified() {
        var model1 = """
				namespace foo.bar

				type A:
					id string (1..1)

				type D:
					id string (1..1)
				""";

        var model2 = """
				namespace test

				import foo.bar.* as someAlias

				type B:
					a someAlias.A (1..1)
					d foo.bar.D (1..1)
				""";

        modelHelper.parseRosettaWithNoIssues(model1, model2);
    }

    @Test
    void testCanUseMixOfImportAliasAndNoAlias() {
        var model1 = """
				namespace foo.bar

				type A:
					id string (1..1)
				""";

        var model2 = """
				namespace test

				import foo.bar.* as someAlias

				type D:
					id string (1..1)

				type B:
					a someAlias.A (1..1)
					d D (1..1)
				""";

        modelHelper.parseRosettaWithNoIssues(model1, model2);
    }

    @Test
    void testCanUseImportAliasesWhenWildcardPresent() {
        var model1 = """
				namespace foo.bar

				type A:
					id string (1..1)
				""";

        var model2 = """
				namespace test

				import foo.bar.* as someAlias

				type B:
					a someAlias.A (1..1)
				""";

        modelHelper.parseRosettaWithNoIssues(model1, model2);
    }

    @Test
    void testCannotUseImportAliasesWithoutWildcard() {
        var model = modelHelper.parseRosetta("""
				import foo.bar.Test as someAlias
				""");

        validationTestHelper.assertError(model, IMPORT, null,
                "\"as\" statement can only be used with wildcard imports"
        );
    }

    @Test
    void testCannotAccessUncommonMetaFeatureOfDeepFeatureCall() {
        var model = modelHelper.parseRosettaWithNoIssues("""
				type A:
					a string (1..1)
						[metadata scheme]

				type B:
					a string (1..1)

				type C:
					a string (1..1)
						[metadata scheme]

				choice ABC:
					A
					B
					C
				""");

        var expression = expressionParser.parseExpression("abc ->> a -> scheme", List.of(model), List.of("abc ABC (1..1)"));
        validationTestHelper.assertError(expression, ROSETTA_FEATURE_CALL, Diagnostic.LINKING_DIAGNOSTIC, "Couldn't resolve reference to RosettaFeature 'scheme'.");
    }

    @Test
    void testCanAccessMetaFeatureAfterDeepFeatureCall() {
        var context = modelHelper.parseRosettaWithNoIssues("""
				choice A:
					B
						[metadata reference]
					C
						[metadata reference]

				type B:
					[metadata key]
					id string (1..1)
						[metadata scheme]

				type C:
					[metadata key]
					id string (1..1)
						[metadata scheme]
				""");

        var expression1 = expressionParser.parseExpression("a ->> id", List.of(context), List.of("a A (1..1)"));
        validationTestHelper.assertNoIssues(expression1);

        var expression2 = expressionParser.parseExpression("a ->> id -> scheme", List.of(context), List.of("a A (1..1)"));
        validationTestHelper.assertNoIssues(expression2);
    }

    @Test
    void testDeepFeatureCall() {
        var context = modelHelper.parseRosettaWithNoIssues("""
				choice A:
					B
					C

				type B:
					opt1 Option1 (0..1)
					opt2 Option2 (0..1)
					attr Foo (0..1)

				condition Choice: one-of

				type C:
					opt1 Option1 (0..1)

				condition Choice: one-of

				type Option1:
					attr Foo (1..1)

				type Option2:
					attr Foo (1..1)
					otherAttr string (1..1)

				type Option3:
					attr Foo (1..1)

				type Foo:
				""");

        var expression1 = expressionParser.parseExpression("a ->> attr", List.of(context), List.of("a A (1..1)"));
        validationTestHelper.assertNoIssues(expression1);
        var expression2 = expressionParser.parseExpression("a ->> opt1", List.of(context), List.of("a A (1..1)"));
        validationTestHelper.assertNoIssues(expression2);
        var expression3 = expressionParser.parseExpression("b ->> attr", List.of(context), List.of("b B (1..1)"));
        validationTestHelper.assertNoIssues(expression3);

        var expression4 = expressionParser.parseExpression("a ->> B", List.of(context), List.of("a A (1..1)"));
        validationTestHelper.assertError(expression4, ROSETTA_DEEP_FEATURE_CALL, Diagnostic.LINKING_DIAGNOSTIC, "Couldn't resolve reference to Attribute 'B'.");
        var expression5 = expressionParser.parseExpression("a ->> opt2", List.of(context), List.of("a A (1..1)"));
        validationTestHelper.assertError(expression5, ROSETTA_DEEP_FEATURE_CALL, Diagnostic.LINKING_DIAGNOSTIC, "Couldn't resolve reference to Attribute 'opt2'.");
        var expression6 = expressionParser.parseExpression("a ->> otherAttr", List.of(context), List.of("a A (1..1)"));
        validationTestHelper.assertError(expression6, ROSETTA_DEEP_FEATURE_CALL, Diagnostic.LINKING_DIAGNOSTIC, "Couldn't resolve reference to Attribute 'otherAttr'.");
        var expression7 = expressionParser.parseExpression("b ->> opt1", List.of(context), List.of("b B (1..1)"));
        validationTestHelper.assertError(expression7, ROSETTA_DEEP_FEATURE_CALL, Diagnostic.LINKING_DIAGNOSTIC, "Couldn't resolve reference to Attribute 'opt1'.");
    }

    @Test
    void testCannotCallFuncWithoutInput() {
        var model = modelHelper.parseRosetta("""
				func Foo:
					inputs: a int (1..1)
					output: result int (1..1)
					set result:
						Foo
				""");

        validationTestHelper.assertError(model, ROSETTA_SYMBOL_REFERENCE, null,
                "Expected 1 argument, but got 0 instead"
        );
    }

    @Test
    void testOrderDoesNotMatter() {
        var model1 = """
				namespace test

				import a.b.c.*

				reporting rule B from string:
					A -> a
				""";
        var model2 = """
				namespace a.b.c

				type Foo:
					a int (1..1)

				reporting rule A from string:
					[ Foo { a: 1 }, Foo { a: 2 } ] then last
				""";
        modelHelper.parseRosettaWithNoIssues(model1, model2);
        modelHelper.parseRosettaWithNoIssues(model2, model1);
    }

    @Test
    void validConstructor() {
        modelHelper.parseRosettaWithNoIssues("""
				type A:
					value int (1..1)
					b string (0..*)
					c A (0..1)

				func CreateA:
					output: result A (1..1)
					set result:
						A {
							c: A { value: 0, ... },
							b: ["A", "B"],
							value: 2*21,
						}
				""");
    }

    @Test
    void missingFieldsInConstructor() {
        var model = modelHelper.parseRosetta("""
				type A:
					a int (1..1)
					b string (0..*)
					c A (0..1)

				func CreateA:
					output: result A (1..1)
					set result:
						A {
							a: 2*21
						}
				""");

        validationTestHelper.assertError(model, ROSETTA_CONSTRUCTOR_EXPRESSION, RosettaIssueCodes.MISSING_MANDATORY_CONSTRUCTOR_ARGUMENT,
                "Missing attributes `b`, `c`. Perhaps you forgot a `...` at the end of the constructor?"
        );
    }

    @Test
    void invalidUseOfDotsInConstructor() {
        var model = modelHelper.parseRosetta("""
				type A:
					a int (1..1)
					b string (1..*)
					c A (1..1)

				func CreateA:
					output: result A (1..1)
					set result:
						A {
							a: 2*21,
							...
						}
				""");

        validationTestHelper.assertError(model, ROSETTA_CONSTRUCTOR_EXPRESSION, null,
                "There are no optional attributes left"
        );
    }

    @Test
    void duplicateFieldInConstructor() {
        var model = modelHelper.parseRosetta("""
				type A:
					a int (1..1)
					b string (0..*)
					c A (0..1)

				func CreateA:
					output: result A (1..1)
					set result:
						A {
							a: 2*21,
							a: 0,
							...
						}
				""");

        validationTestHelper.assertError(model, CONSTRUCTOR_KEY_VALUE_PAIR, null,
                "Duplicate attribute `a`"
        );
    }

    @Test
    void wrongTypeInConstructor() {
        var model = modelHelper.parseRosetta("""
				type A:
					a int (1..1)
					b string (0..*)
					c A (0..1)

				func CreateA:
					output: result A (1..1)
					set result:
						A {
							a: "abc",
							...
						}
				""");

        validationTestHelper.assertError(model, CONSTRUCTOR_KEY_VALUE_PAIR, null,
                "Expected type `int`, but got `string` instead. Cannot assign `string` to attribute `a`"
        );
    }

    @Test
    void validRecordConstructor() {
        modelHelper.parseRosettaWithNoIssues("""
				func CreateDate:
					output: result date (1..1)
					set result:
						date {
							day: 4,
							month: 11,
							year: 1998
						}
				""");
    }

    @Test
    void missingFieldInRecordConstructor() {
        var model = modelHelper.parseRosetta("""
				func CreateDate:
					output: result date (1..1)
					set result:
						date {
							day: 4
						}
				""");

        validationTestHelper.assertError(model, ROSETTA_CONSTRUCTOR_EXPRESSION, RosettaIssueCodes.MISSING_MANDATORY_CONSTRUCTOR_ARGUMENT,
                "Missing attributes `month`, `year`"
        );
    }

    @Test
    void invalidUseOfDotsInRecordConstructor() {
        var model = modelHelper.parseRosetta("""
				func CreateDate:
					output: result date (1..1)
					set result:
						date {
							day: 4,
							...
						}
				""");

        validationTestHelper.assertError(model, ROSETTA_CONSTRUCTOR_EXPRESSION, null,
                "There are no optional attributes left"
        );
    }

    @Test
    void attributeOfImplicitItemWithMultiCardinalityShouldBeMulti() {
        var model = modelHelper.parseRosetta("""
				type A:
					val int (1..1)

				func Foo:
					inputs: ins A (0..*)
					output: result int (1..1)
					set result:
						ins then val
				""");

        validationTestHelper.assertWarning(model, OPERATION, null,
                "Expecting single cardinality. Cannot assign a list to a single value");
    }

    @Test
    void canCallARuleFromARule() {
        modelHelper.parseRosettaWithNoIssues("""
				reporting rule Bar from number:
					item
					then Bar2

				reporting rule Bar2 from number:
					item + item
				""");
    }

    @Test
    void testEligibilityRulesShouldHaveSameInputTypeAsReport() {
        var model = modelHelper.parseRosetta("""
				body Authority TEST_REG
				corpus TEST_REG FOO

				report TEST_REG FOO in T+1
					from number
					when Foo
					with type Report

				eligibility rule Foo from string:
					item = "42"

				reporting rule Bar from number:
					item + item

				type Report:
					attr number (1..1)
						[ruleReference Bar]
				""");

        validationTestHelper.assertError(model, ROSETTA_REPORT, null,
                "Eligibility rule Foo expects a `string` as input, but this report is generated from a `number`.");
    }

    @Test
    void testReportShouldHaveSameInputTypeAsReportType() {
        var model = modelHelper.parseRosetta("""
				body Authority TEST_REG
				corpus TEST_REG FOO

				report TEST_REG FOO in T+1
					from number
					when Bla
					with type Report

				eligibility rule Bla from number:
					item = 42

				reporting rule Foo from string:
					item + item

				reporting rule Bar from string:
					42

				type Report:
					attr1 string (1..1)
						[ruleReference Foo]
					attr2 number (1..1)
						[ruleReference Bar]
				""");

        validationTestHelper.assertError(model, ROSETTA_REPORT, null,
                "Report type Report expects a `string` as input, but this report is generated from a `number`.");
    }

    @Test
    void testReportShouldHaveSameInputTypeAsRuleSource() {
        var model = modelHelper.parseRosetta("""
				body Authority TEST_REG
				corpus TEST_REG FOO

				report TEST_REG FOO in T+1
					from number
					when Bla
					with type Report
					with source RuleSource

				eligibility rule Bla from number:
					item = 42

				reporting rule Foo from string:
					item + item

				reporting rule Bar from string:
					42

				type Report:
					attr1 string (1..1)
					attr2 number (1..1)

				rule source RuleSource {
					Report:
						+ attr1
							[ruleReference Foo]
						+ attr2
							[ruleReference Bar]
				}
				""");

        validationTestHelper.assertError(model, ROSETTA_REPORT, null,
                "Rule source RuleSource expects a `string` as input, but this report is generated from a `number`.");
    }

    @Test
    void testExternalRuleReferencesMustHaveSameInputType1() {
        var model = modelHelper.parseRosetta("""
				reporting rule Foo from string:
					item + item

				reporting rule Bar from number:
					item * 2

				type Report:
					attr1 string (1..1)
					attr2 number (1..1)

				rule source RuleSource {
					Report:
						+ attr1
							[ruleReference Foo]
						+ attr2
							[ruleReference Bar]
				}
				""");

        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null,
                "Rule `Bar` expects an input of type `number`, while previous rules expect an input of type `string`");
    }

    @Test
    void testExternalRuleReferencesMustHaveSameInputType2() {
        var model = modelHelper.parseRosetta("""
				reporting rule Foo1 from string:
					item + item

				reporting rule Foo2 from string:
					item + item

				reporting rule Bar from number:
					item * 2

				type Report:
					sub Subreport (1..1)
					attr number (1..1)

				type Subreport:
					attr1 string (1..1)
					attr2 string (1..1)

				rule source RuleSource {
					Report:
						+ attr
							[ruleReference Bar]
					Subreport:
						+ attr1
							[ruleReference Foo1]
						+ attr2
							[ruleReference Foo2]
				}
				""");

        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null,
                "Rule `Bar` expects an input of type `number`, while previous rules expect an input of type `string`");
    }

    @Test
    void testExternalRuleReferencesMustHaveSameInputTypeInInheritedReport() {
        var model = modelHelper.parseRosetta("""
				reporting rule Foo from string:
					item + item

				reporting rule Bar from number:
					item * 2

				type Report:
					attr1 string (1..1)
					attr2 number (1..1)

				rule source Source1 {
					Report:
						+ attr1
							[ruleReference Foo]
				}

				rule source Source2 extends Source1 {
					Report:
						+ attr2
							[ruleReference Bar]
				}
				""");

        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null,
                "Rule `Bar` expects an input of type `number`, while previous rules expect an input of type `string`");
    }

    @Test
    void testRuleReferencesMustHaveSameInputType1() {
        var model = modelHelper.parseRosetta("""
				reporting rule Foo from string:
					item + item

				reporting rule Bar from number:
					item * 2

				type Report:
					attr1 string (1..1)
						[ruleReference Foo]
					attr2 number (1..1)
						[ruleReference Bar]
				""");

        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null,
                "Rule `Bar` expects an input of type `number`, while previous rules expect an input of type `string`");
    }

    @Test
    void testRuleReferencesMustHaveSameInputType2() {
        var model = modelHelper.parseRosetta("""
				reporting rule Foo1 from string:
					item + item

				reporting rule Foo2 from string:
					item + item

				reporting rule Bar from number:
					item * 2

				type Report:
					sub Subreport (1..1)
					attr number (1..1)
						[ruleReference Bar]

				type Subreport:
					attr1 string (1..1)
						[ruleReference Foo1]
					attr2 string (1..1)
						[ruleReference Foo2]
				""");

        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null,
                "Rule `Bar` expects an input of type `number`, while previous rules expect an input of type `string`");
    }

    @Test
    void testRuleReferencesMustHaveSameInputType3() {
        var model = modelHelper.parseRosetta("""
				reporting rule Foo1 from string:
					item + item

				reporting rule Foo2 from string:
					item + item

				reporting rule Bar from number:
					item * 2

				type Report:
					attr number (1..1)
						[ruleReference Bar]
					sub Subreport (1..1)

				type Subreport:
					attr1 string (1..1)
						[ruleReference Foo1]
					attr2 string (1..1)
						[ruleReference Foo2]
				""");

        validationTestHelper.assertError(model, ATTRIBUTE, null,
                "Rule `Foo1` for sub -> attr1 expects an input of type `string`, while previous rules expect an input of type `number`");
    }

    @Test
    void testRuleReferencesMustHaveSameInputTypeInInheritedReport() {
        var model = modelHelper.parseRosetta("""
				reporting rule Foo from string:
					item + item

				reporting rule Bar from number:
					item * 2

				type ReportParent:
					attr1 string (1..1)
						[ruleReference Foo]

				type ReportChild extends ReportParent:
					attr2 number (1..1)
						[ruleReference Bar]
				""");

        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null,
                "Rule `Bar` expects an input of type `number`, while previous rules expect an input of type `string`");
    }

    @Test
    void testMandatoryThen1() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					attr Bar (0..*)
					someInt int (1..1)

				func Foo:
					inputs:
						input Bar (0..*)
					output:
						result int (0..*)

					add result:
						input -> attr only-element -> attr
						extract [ attr ]
						flatten
						filter [ someInt = 42 ]
						extract [ someInt ]
				""");

        validationTestHelper.assertError(model, FLATTEN_OPERATION, MANDATORY_THEN,
                "Usage of `then` is mandatory.");
        validationTestHelper.assertError(model, FILTER_OPERATION, MANDATORY_THEN,
                "Usage of `then` is mandatory.");
        validationTestHelper.assertError(model, MAP_OPERATION, MANDATORY_THEN,
                "Usage of `then` is mandatory.");
    }

    @Test
    void testMandatoryThen2() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					attr Bar (0..*)
					someInt int (1..1)

				func DoTheThing:
					inputs: bar Bar (1..1)
					output: result Bar (0..*)

				func Foo:
					inputs:
						input Bar (0..*)
					output:
						result Bar (0..*)

					add result:
						input
						extract [ DoTheThing(item) ] flatten
						distinct
						sort [someInt]
				""");

        validationTestHelper.assertError(model, FLATTEN_OPERATION, MANDATORY_THEN,
                "Usage of `then` is mandatory.");
        validationTestHelper.assertError(model, DISTINCT_OPERATION, MANDATORY_THEN,
                "Usage of `then` is mandatory.");
        validationTestHelper.assertError(model, SORT_OPERATION, MANDATORY_THEN,
                "Usage of `then` is mandatory.");
    }

    @Test
    void testMandatoryThen3() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					attr Bar (0..*)
					someInt int (1..1)

				func Foo:
					inputs:
						input Bar (0..*)
					output:
						result int (0..*)

					add result:
						input -> attr only-element -> attr
						extract attr
						then filter [ someInt = 42 ]
						extract someInt
				""");

        validationTestHelper.assertError(model, MAP_OPERATION, MANDATORY_THEN,
                "Usage of `then` is mandatory.");
    }

    @Test
    void testMandatorySquareBrackets() {
        var model = modelHelper.parseRosetta("""
				func Foo:
					inputs:
						input int (0..*)
					output:
						result int (0..*)

					add result:
						input
						then extract
						item
						extract item + 1
				""");

        validationTestHelper.assertError(model, MAP_OPERATION, null,
                "Ambiguous operation. Either use `then` or surround with square brackets to define a nested operation.");
    }

    @Test
    void testSuperfluousSquareBrackets() {
        var model = modelHelper.parseRosetta("""
				func Foo:
					inputs:
						input int (0..*)
					output:
						result int (0..*)

					add result:
						input
						then extract [ item + 1 ]
				""");

        validationTestHelper.assertWarning(model, INLINE_FUNCTION, REDUNDANT_SQUARE_BRACKETS,
                "Usage of brackets is unnecessary.");
    }

    @Test
    void testMandatoryThenSucceeds1() {
        modelHelper.parseRosettaWithNoIssues("""
				func Foo:
					inputs:
						input int (0..*)
					output:
						result int (0..*)

					add result:
						input
						then extract [
							extract item + 1
						]
				""");
    }

    @Test
    void testMandatoryThenSucceeds2() {
        modelHelper.parseRosettaWithNoIssues("""
				func Foo:
					inputs:
						input int (0..*)
					output:
						result int (0..*)

					add result:
						input
						extract Foo(extract item + 1)
						then flatten
				""");
    }

    @Test
    void testMandatoryThenSucceeds3() {
        modelHelper.parseRosettaWithNoIssues("""
				func Foo:
					inputs:
						input int (0..*)
					output:
						result int (0..*)

					add result:
						input
						extract (
							extract item + 1
						)
				""");
    }

    @Test
    void noDuplicateInheritanceForRuleSourceTest() {
        var model = modelHelper.parseRosetta("""
				rule source TestA {}
				rule source TestB {}
				rule source TestC extends TestA, TestB {}
				""");
        validationTestHelper.assertError(model, ROSETTA_EXTERNAL_RULE_SOURCE, null,
                "A rule source may not extend more than one other rule source.");
    }

    @Test
    void noDuplicateTypesInAnnotationSourceTest() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					foo string (0..1)

				rule source TestA {
					Foo:
						+ foo

					Foo:
						+ foo
				}
				""");
        validationTestHelper.assertWarning(model, ROSETTA_EXTERNAL_CLASS, null,
                "Duplicate type `Foo`.");
    }

    @Test
    void synonymNotAllowedInRuleSourceTest() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					foo string (0..1)

				rule source TestA {
					Foo:
						[meta "bar"]
						+ foo
							[value "bar" path "baz"]
				}
				""");
        validationTestHelper.assertError(model, ROSETTA_EXTERNAL_CLASS_SYNONYM, null,
                "You may not define synonyms in a rule source.");
        validationTestHelper.assertError(model, ROSETTA_EXTERNAL_SYNONYM, null,
                "You may not define synonyms in a rule source.");
    }

    @Test
    void enumNotAllowedInRuleReferenceSourceTest() {
        var model = modelHelper.parseRosetta("""
				enum Foo:
					BAR

				rule source TestA {
					enums

					Foo:
						+ BAR
				}
				""");
        validationTestHelper.assertError(model, ROSETTA_EXTERNAL_RULE_SOURCE, null,
                "A rule source cannot define annotations for enums.");
    }

    @Test
    void externalRuleSourceCannotExtendExternalSynonymSourceTest() {
        var model = modelHelper.parseRosetta("""
				synonym source SynSource {}

				rule source RuleSource extends SynSource {}
				""");
        validationTestHelper.assertError(model, ROSETTA_EXTERNAL_RULE_SOURCE, Diagnostic.LINKING_DIAGNOSTIC,
                "Couldn't resolve reference to ExternalAnnotationSource 'SynSource'.");
    }

    @Test
    void cannotRemoveNonExistingRuleReferenceFromExternalRuleSourceTest() {
        var model = modelHelper.parseRosetta("""
				body Authority TEST_REG
				corpus TEST_REG FOO

				report TEST_REG FOO in T+1
					from ReportableEvent
					when FooRule
					with type Foo
					with source TestA

				eligibility rule FooRule from Foo:
					filter foo exists

				type Foo:
					foo string (0..1)

				reporting rule RA from Foo:
					"A"

				reporting rule RB from Foo:
					"B"

				rule source TestA {
					Foo:
						- foo
				}
				""");
        validationTestHelper.assertError(model, ROSETTA_EXTERNAL_REGULAR_ATTRIBUTE, null,
                "There is no rule reference to remove");
    }

    @Test
    void mayNotUseAmbiguousOutputTest() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					result int (1..1)

				func F:
					inputs:
						foo Foo (1..1)
					output:
						result int (1..1)

					set result:
						foo extract [ result ]
				""");
        validationTestHelper.assertError(model, ROSETTA_SYMBOL_REFERENCE, null,
                "Ambiguous reference. `result` may either refer to `item -> result` or to the output variable.");
    }

    @Test
    void dateMemberHasRightTypeTest() {
        modelHelper.parseRosettaWithNoIssues("""
				func F:
					inputs:
						d date (1..1)
					output: result boolean (1..1)
					set result:
						d -> day > 15
				""");
    }

    @Test
    void nameShadowingNotAllowed1() {
        var model =
                modelHelper.parseRosetta("""
				func F:
					inputs:
						a int (1..1)
					output: result int (1..1)
					set result:
						42 extract a [ a ]
				""");
        validationTestHelper.assertError(model, CLOSURE_PARAMETER, null,
                "Duplicate name.");
    }

    @Test
    void nameShadowingNotAllowed2() {
        var model =
                modelHelper.parseRosetta("""
				func F:
					output: result int (1..1)
					alias a: 10
					set result:
						42 extract a [ a ]
				""");
        validationTestHelper.assertError(model, CLOSURE_PARAMETER, null,
                "Duplicate name.");
    }

    @Test
    void nameShadowingNotAllowed3() {
        var model =
                modelHelper.parseRosetta("""
				func F:
					output: a int (1..1)
					set a:
						42 extract a [ a ]
				""");
        validationTestHelper.assertError(model, CLOSURE_PARAMETER, null,
                "Duplicate name.");
    }

    @Test
    void nameShadowingNotAllowed4() {
        var model =
                modelHelper.parseRosetta("""
				func F:
					output: result int (1..1)
					set result:
						42 extract a [ 10 extract a [ a ] ]
				""");
        validationTestHelper.assertError(model, CLOSURE_PARAMETER, null,
                "Duplicate name.");
    }

    @Test
    void mayDoRecursiveCalls() {
        modelHelper.parseRosettaWithNoIssues("""
				func Rec:
					output: result int (1..1)
					alias test: Rec()
					set result: Rec()
				""");
    }

    @Test
    void testCannotOmitParametersOfBinaryFunction() {
        var model =
                modelHelper.parseRosetta("""
				func Add:
					inputs:
						a int (1..1)
						b int (1..1)
					output: result int (1..1)
					set result:
						a + b

				func Foo:
					inputs: a int (0..*)
					output: b int (0..*)
					add b:
						a extract Add
				""");
        validationTestHelper.assertError(model, ROSETTA_SYMBOL_REFERENCE, null,
                "Expected 2 arguments, but got 0 instead");
    }

    @Test
    void testCannotCallParameter() {
        var model =
                modelHelper.parseRosetta("""
				func Foo:
					inputs: a int (0..*)
					output: b int (0..*)
					add b:
						a()
				""");
        validationTestHelper.assertError(model, ROSETTA_SYMBOL_REFERENCE, null,
                "A variable may not be called");
    }

    @Test
    void testGeneratedInputWithoutImplicitVariable() {
        var model =
                modelHelper.parseRosetta("""
				func Foo:
					inputs: a int (0..*)
					output: b int (0..*)
					add b:
						extract [item+1]
				""");
        validationTestHelper.assertError(model, MAP_OPERATION, null,
                "There is no implicit variable in this context. This operator needs an explicit input in this context.");
    }

    @Test
    void testImplicitVariableWhenItDoesNotExist() {
        var model =
                modelHelper.parseRosetta("""
				func Foo:
					inputs: a int (0..*)
					output: b int (0..*)
					add b:
						item
				""");
        validationTestHelper.assertError(model, ROSETTA_IMPLICIT_VARIABLE, null,
                "There is no implicit variable in this context.");
    }

    @Test
    void testGeneratedInputValidationRedirection() {
        var model =
                modelHelper.parseRosetta("""
				type Foo:
					a int (1..1)
					condition A:
						*42
				""");
        validationTestHelper.assertError(model, ARITHMETIC_OPERATION, null,
                "Expected type `number`, but got `Foo` instead. Cannot use `Foo` with operator `*`");
    }

    @Test
    void testTypeExpectation() {
        var model =
                modelHelper.parseRosetta("""
				type Foo:
					id int (1..1)

				condition R:
					if id = True
					then id < 1
				""");
        validationTestHelper.assertError(model, EQUALITY_OPERATION, null,
                "Types `int` and `boolean` are not comparable");
    }

    @Test
    void testTypeExpectationNoError() {
        var model =
                modelHelper.parseRosettaWithNoErrors("""
				type Foo:
					id int (1..1)

				condition R:
					if id = 1
					then id < 1
				""");
        validationTestHelper.assertNoError(model, TYPE_ERROR);
    }

    @Test
    void testTypeExpectationError() {
        var model =
                modelHelper.parseRosetta("""
				type Foo:
					id boolean (1..1)
					condition R:
						if id = True
						then id < 1
				""");
        validationTestHelper.assertError(model, COMPARISON_OPERATION, null, "Operator `<` is not supported for type `boolean`. Supported types are `number`, `date` and `zonedDateTime`");
    }

    @Test
    void testTypeErrorAssignment_01() {
        var model = modelHelper.parseRosetta("""
				namespace "test"
				version "test"

				type Foo:
					id boolean (1..1)

				func Test:
					inputs: in0 Foo (0..1)
					output: out Foo (0..1)
					set out:
						"not a Foo"
				""");
        validationTestHelper.assertError(model, OPERATION, null, "Expected type `Foo`, but got `string` instead. Cannot assign `string` to output `out`");
    }


    @Test
    void testTypeErrorAssignment_02() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					id boolean (1..1)

				func Test:
					inputs: in0 Foo (0..1)
					output: out Foo (0..1)
					set out -> id:
						"not a boolean"
				""");
        validationTestHelper.assertError(model, OPERATION, null, "Expected type `boolean`, but got `string` instead. Cannot assign `string` to output `id`");
    }

    @Test
    void testTypeErrorAssignment_03() {
        var model =
                modelHelper.parseRosetta("""
				type WithKey:
					[metadata key]

				type TypeToUse:
					attr WithKey (0..1)
						[metadata reference]

				func Bar:
					inputs:
						in1 TypeToUse (1..1)
					output: result TypeToUse (1..1)
					set result -> attr:
						in1 as-key
				""");
        validationTestHelper.assertError(model, OPERATION, null, "Expected type `WithKey`, but got `TypeToUse` instead. Cannot assign `TypeToUse` to output `attr`");
    }

    @Test
    void testTypeErrorAssignment_04() {
        var model =
                modelHelper.parseRosetta("""
				enum Enumerate : X Y Z

				type Type:
					other Enumerate (0..1)

				func Funcy:
					inputs: in0 Type (0..1)
					output: out string (0..1)
					alias Ali : in0 -> other = Enumerate -> X
				""");
        validationTestHelper.assertNoErrors(model);
    }

    @Test
    void testTypeErrorAssignment_05() {
        var model =
                modelHelper.parseRosetta("""
				type Type:
					other int (0..1)

				func Funcy:
					inputs: in0 Type (0..1)
					output: out string (0..1)
					set out: in0->other
				""");
        validationTestHelper.assertError(model, OPERATION, null, "Expected type `string`, but got `int` instead. Cannot assign `int` to output `out`");
    }

    @Test
    void testAttributesWithLocationBadTarget() {
        var model = modelHelper.parseRosetta("""
				metaType scheme string
				metaType reference string

				type Bar:
					bar string (1..1)
						[metadata address "pointsTo"=Foo->foo]

				""");
        validationTestHelper.assertError(model, ROSETTA_DATA_REFERENCE, Diagnostic.LINKING_DIAGNOSTIC, "Couldn't resolve reference to Data 'Foo'.");
    }

    @Test
    void testAttributesWithLocationAndNoAddress() {
        var model = modelHelper.parseRosetta("""
				metaType scheme string
				metaType reference string

				type Foo:
					foo string (1..1)

				type Bar:
					bar string (1..1)
						[metadata address "pointsTo"=Foo->foo]

				""");
        validationTestHelper.assertError(model, ANNOTATION_QUALIFIER, null, "Target of address must be annotated with metadata location");
    }

    @Test
    void testAttributesWithLocationAndAddressWrongType() {
        var model = modelHelper.parseRosetta("""
				metaType scheme string
				metaType reference string

				type Foo:
					foo int (1..1)
						[metadata location]

				type Bar:
					bar string (1..1)
						[metadata address "pointsTo"=Foo->foo]

				""");
        validationTestHelper.assertError(model, ANNOTATION_QUALIFIER, TYPE_ERROR, "Expected address target type of 'string' but was 'int'");
    }


    @Test
    void testDuplicateAttribute() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					i int (1..1)

				type Bar extends Foo:
					i int (1..1)
				""");
        validationTestHelper.assertNoErrors(model);
    }

    @Test
    void testDuplicateAttributeNotAllowedWithDiffCard1() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					i int (1..1)

				type Bar extends Foo:
					i int (0..1)
				""");
        validationTestHelper.assertWarning(model, ATTRIBUTE, null, "Attribute 'i' already defined in super type. To override the type, cardinality or annotations of this attribute, use the keyword `override`");
    }

    @Test
    void testDuplicateAttributeNotAllowedWithDiffCard2() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					i int (1..1)

				type Bar extends Foo:
					i int (1..*)
				""");
        validationTestHelper.assertWarning(model, ATTRIBUTE, null, "Attribute 'i' already defined in super type. To override the type, cardinality or annotations of this attribute, use the keyword `override`");
    }

    @Test
    void testDuplicateAttributeNotAllowedWithDiffType() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					i int (1..1)

				type Bar extends Foo:
					i string (1..1)
				""");
        validationTestHelper.assertWarning(model, ATTRIBUTE, null, "Attribute 'i' already defined in super type. To override the type, cardinality or annotations of this attribute, use the keyword `override`");
    }

    @Test
    void testDuplicateChoiceRuleAttribute_thisOne() {
        var model = modelHelper.parseRosetta("""
                type Bar:
                	attribute1 string (0..1)
                	attribute2 string (0..1)
                	attribute3 string (0..1)
                
                	condition Foo:
                		required choice
                			attribute1, attribute1
                """);
        validationTestHelper.assertError(model, CHOICE_OPERATION, null, "Duplicate attribute.");
    }

    @Test
    void testDuplicateChoiceRuleAttribute_thatOne() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					attribute1 string (0..1)
					attribute2 string (0..1)
					attribute3 string (0..1)

					condition Foo:
						required choice attribute1, attribute2, attribute2
				""");
        validationTestHelper.assertError(model, CHOICE_OPERATION, null, "Duplicate attribute.");
    }

    @Test
    void shouldNoGenerateErrorsForConditionWithInheritedAttributeExists() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					x string (0..1)

				type Bar extends Foo:
					y string (0..1)

					condition XExists:
						x exists
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void checkMergeSynonymErrorOnSingleCardinality() {
        var model = modelHelper.parseRosetta("""
				synonym source FpML

				type Foo:
					attr int (0..1)
						[synonym FpML merge "bar"]
				""");
        validationTestHelper.assertError(model, ROSETTA_SYNONYM_BODY, null, "Merge synonym can only be specified on an attribute with multiple cardinality.");
    }

    @Test
    void checkMergeSynonymNoErrorOnMultiCardinality() {
        var model = modelHelper.parseRosetta("""
				synonym source FpML

				type Foo:
					attr int (0..*)
						[synonym FpML merge "bar"]
				""");
        validationTestHelper.assertNoErrors(model);
    }

    @Test
    void checkMappingMultipleSetToWithoutWhenCases() {
        var model = modelHelper.parseRosetta("""
				type Quote:
					attr int (1..1)
						[synonym FIX
							set to 1,
							set to 2]
				""");
        validationTestHelper.assertError(model, ROSETTA_MAPPING, null, "Only one set to with no when clause allowed.");
    }

    @Test
    void checkMappingMultipleSetToOrdering() {
        var model = modelHelper.parseRosetta("""
				type Quote:
					attr int (1..1)
						[synonym FIX
							set to 1,
							set to 2 when "a.b.c" exists]
				""");
        validationTestHelper.assertError(model, ROSETTA_MAPPING, null, "Set to without when case must be ordered last.");
    }

    @Test
    void checkMappingSetToTypeCheck() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					value0 string (1..1)

				type Quote:
					attr Foo (1..1)
						[synonym FIX
							set to "hello"]
				""");
        validationTestHelper.assertError(model, ROSETTA_MAPPING, null, "Set to constant type does not match type of field.");
    }

    @Test
    void checkMappingSetToEnumTypeCheck() {
        var model = modelHelper.parseRosetta("""
				enum Foo: ONE

				enum Bar: BAR

				type Quote:
					attr Foo (1..1)
						[synonym FIX
							set to Bar.BAR]
				""");
        validationTestHelper.assertError(model, ROSETTA_MAPPING, null, "Set to constant type does not match type of field.");
    }

    @Test
    void checkMappingSetToWhenTypeCheck() {
        var model = modelHelper.parseRosetta("""
				synonym source FpML
				type Foo:
					stringvar string (1..1)

				type Quote:
					attr Foo (1..1)
						[synonym FpML value "foo" set when "foo->bar" exists]
				""");
        validationTestHelper.assertNoErrors(model);
    }

    @Test
    void checkOperationTypes() {
        var model = modelHelper.parseRosetta("""
				type Clazz:
					test boolean (0..1)

					condition Condition:
						if test = True
							or False <> False
							or 1 > 0
							or 1 < 0
							or 1 >= 0
							or 1 <= 0
							or 1 <> 0
							or 1 = 0
						then 1.1 = .0
							and 0.2 <> 0.1
							and 0.2 > 0.1
							and 0.2 < 0.1
							and 0.2 <= 0.1
							and 0.2 >= 0.1
				""");
        validationTestHelper.assertNoErrors(model);
    }

    @Test
    void checkDateZonedDateTypes() {
        var model = modelHelper.parseRosetta("""
				recordType date{}
				recordType zonedDateTime{}

				func Foo:
					inputs:
						timestamp zonedDateTime (1..1)
					output: result date (1..1)

				func Bar:
					inputs:
						timestamp date (1..1)
					output: result boolean (1..1)
					set result:
						Foo(timestamp) = timestamp
				""");
        validationTestHelper.assertError(model, ROSETTA_SYMBOL_REFERENCE, null,
                "Expected type `zonedDateTime`, but got `date` instead. Cannot assign `date` to input `timestamp`");
    }

    @Test
    void checkAsKeyUsage_01() {
        var model = modelHelper.parseRosetta("""
				type WithKey:
					[metadata key]

				type TypeToUse:
					attr WithKey (0..1)
						[metadata reference]

				func Bar:
					inputs:
						in0 WithKey (1..1)
					output: result TypeToUse (1..1)
					set result -> attr:
						in0 as-key
				""");
        validationTestHelper.assertNoErrors(model);
    }

    @Test
    void checkAsKeyUsage_02() {
        var model = modelHelper.parseRosetta("""
				type WithKey:
					[metadata key]

				type TypeToUse:
					attr WithKey (0..1)
						[metadata reference]
					attr2 TypeToUse (0..1)

				func Bar:
					inputs:
						in0 WithKey (1..1)
						in1 TypeToUse (1..1)
					output: result TypeToUse (1..1)
					set result -> attr2:
						in1 as-key
				""");
        validationTestHelper.assertError(model, AS_KEY_OPERATION, null,
                "'as-key' can only be used with attributes annotated with [metadata reference] annotation.");
    }

    @Test
    void checkAsKeyUsage_03() {
        var model = modelHelper.parseRosetta("""
				type WithKey:
					[metadata key]

				type TypeToUse:
					attr WithKey (0..1)
						[metadata reference]
					attr2 TypeToUse (0..1)

				func Bar:
					inputs:
						in0 WithKey (1..1)
						in1 TypeToUse (1..1)
					output: result TypeToUse (1..1)
						[metadata scheme]
					set result -> scheme:
						in1 as-key
				""");
        validationTestHelper.assertError(model, AS_KEY_OPERATION, null,
                "'as-key' can only be used with attributes annotated with [metadata reference] annotation.");
    }

    @Test
    void checkAsKeyUsage_04() {
        var model = modelHelper.parseRosetta("""
				type WithKey:
					[metadata key]

				type TypeToUse:
					attr WithKey (0..1)
						[metadata reference]

				func Bar:
					inputs:
						in0 WithKey (1..1)
					output: result WithKey (1..1)
					set result:
						in0 as-key
				""");
        validationTestHelper.assertError(model, AS_KEY_OPERATION, null,
                "'as-key' can only be used when assigning an attribute. Example: \"set out -> attribute: value as-key\"");
    }

    @Test
    void checkSynonymPathSyntax_01() {
        var model = modelHelper.parseRosetta("""
				type TypeToUse:
					attr string (0..1)
						[synonym FpML value "adjustedDate" path "relative.date" meta id]
				""");
        validationTestHelper.assertError(model, ROSETTA_SYNONYM_VALUE_BASE, null,
                "Character '.' is not allowed in paths. Use '->' to separate path segments.");
    }

    @Test
    void checkSynonymPathSyntax_02() {
        var model = modelHelper.parseRosetta("""
				type TypeToUse:
					attr string (0..1)
						[synonym FpML set to "Custom" when "Pty+Src" = "D"]
				""");
        validationTestHelper.assertError(model, ROSETTA_MAP_PATH_VALUE, null,
                "Character '+' is not allowed in paths. Use '->' to separate path segments.");
    }

    @Test
    void checkChoiceConditionAttributes() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					attribute1 string (0..1)
					attribute2 string (0..1)
					attribute3 string (0..1)

				condition:
					required choice
					attribute1
				""");
        validationTestHelper.assertError(model, CHOICE_OPERATION, null,
                "At least two attributes must be passed to a choice rule");
    }


    @Test
    void externalSynonymWithFormatShouldOnlyOnDate() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					foo int (0..1)

				synonym source TEST_Base

				synonym source TEST extends TEST_Base {
					Foo:
						+ foo
							[value "bar" path "baz" dateFormat "MM/dd/yy"]
				}
				""");
        validationTestHelper.assertError(model, ROSETTA_SYNONYM_BODY, null,
                "Format can only be applied to date/time types");
    }

    @Test
    void externalSynonymWithFormatValid() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					foo time (0..1)

				synonym source TEST_Base

				synonym source TEST extends TEST_Base {
					Foo:
						+ foo
							[value "bar" path "baz" dateFormat "MMb/dd/yy"]
				}
				""");
        validationTestHelper.assertError(model, ROSETTA_SYNONYM_BODY, null,
                "Format must be a valid date/time format - Unknown pattern letter: b");
    }

    @Test
    void internalSynonymWithFormatShouldOnlyBeOnDate() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					foo int (0..1)
						[synonym TEST_Base value "bar" path "baz" dateFormat "MM/dd/yy"]
				synonym source TEST_Base
				""");
        validationTestHelper.assertError(model, ROSETTA_SYNONYM_BODY, null,
                "Format can only be applied to date/time types");
    }

    @Test
    void externalSynonymCanExtendMultipleParents() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					foo time (0..1)

				synonym source TEST_Base1
				synonym source TEST_Base2
				synonym source TEST_Base3

				synonym source TEST extends TEST_Base1, TEST_Base2, TEST_Base3 {
				}
				""");
        validationTestHelper.assertNoErrors(model);
    }

    @Test
    void internalSynonymWithPatternShouldBeValid() {
        var model = modelHelper.parseRosetta("""
				type Foo:
					foo int (0..1)
						[synonym TEST_Base value "bar" path "baz" pattern "([A-Z)" "$1"]
				synonym source TEST_Base
				""");
        validationTestHelper.assertError(model, ROSETTA_SYNONYM_BODY, null,
                """
                        Pattern to match must be a valid regular expression - Unclosed character class near index 5
                        ([A-Z)
                             ^""");
    }

    @Disabled
    @Test
    void testFishIsAShark() {//This test tests that when a check throws an exception it is translated into a validation error - see ExceptionValidator below
        var model = modelHelper.parseRosetta("""
				type MyFish:
					foo int (0..1)
						[synonym TEST_Base value "bar" path "baz" pattern "([A-Z)" "$1"]
				synonym source TEST_Base
				""");
        validationTestHelper.assertError(model, ROSETTA_TYPE, null,
                "checkForSharks");
    }

    @Test
    void enumSynonymWithPatternShouldBeValid() {
        var model = modelHelper.parseRosetta("""
				enum Enumerate : X Y Z

				synonym source TEST_Base
				synonym source TEST extends TEST_Base {
					enums

					Enumerate:
						+ X
							[value "bar" pattern "([A-Z)" "$1"]
				}
				""");
        validationTestHelper.assertError(model, ROSETTA_ENUM_SYNONYM, null,
                """
                        Pattern to match must be a valid regular expression - Unclosed character class near index 5
                        ([A-Z)
                             ^""");
    }

    @Test
    void shouldGenerateRuleCardinalityWarning() {
        var model = modelHelper.parseRosetta("""
				body Authority TEST_REG
				corpus TEST_REG MiFIR

				report TEST_REG MiFIR in T+1
				from Bar
				when FooRule
				with type BarReport

				eligibility rule FooRule from Bar:
				filter bar1 exists

				reporting rule Aa from Bar:
				extract bar1 as "A"

				type Bar:
				bar1 string (0..*)

				type BarReport:
				aa string (1..1)
					[ruleReference Aa]
				""");
        validationTestHelper.assertWarning(model, RULE_REFERENCE_ANNOTATION, null, "Expected single cardinality, but rule has multi cardinality");
    }

    @Test
    void shouldGenerateRuleTypeError() {
        var model = modelHelper.parseRosetta("""
				body Authority TEST_REG
				corpus TEST_REG MiFIR

				report TEST_REG MiFIR in T+1
				from Bar
				when FooRule
				with type BarReport

				eligibility rule FooRule from Bar:
				filter barA exists

				reporting rule Aa from Bar:
				extract barA as "A"

				reporting rule Bb from Bar:
				extract barB as "B"

				reporting rule Cc from Bar:
				extract barC as "C"

				reporting rule Dd from Bar:
				extract barD as "D"

				reporting rule Ee from Bar:
				extract barE as "E"

				reporting rule Ff from Bar:
				extract barF as "F"

				type Bar:
				barA date (0..1)
				barB time (0..1)
				barC zonedDateTime (0..1)
				barD int (0..1)
				barE number (0..1)
				barF BazEnum (0..1)

				enum BazEnum:
				X
				Y
				Z

				type BarReport:
				aa string (1..1)
					[ruleReference Aa]
				bb string (1..1)
					[ruleReference Bb]
				cc string (1..1)
					[ruleReference Cc]
				dd string (1..1)
					[ruleReference Dd]
				ee string (1..1)
					[ruleReference Ee]
				ff string (1..1)
					[ruleReference Ff]
				""");
        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null, "Expected type string, but rule has type date");
        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null, "Expected type string, but rule has type time");
        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null, "Expected type string, but rule has type zonedDateTime");
        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null, "Expected type string, but rule has type int");
        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null, "Expected type string, but rule has type number");
        validationTestHelper.assertError(model, RULE_REFERENCE_ANNOTATION, null, "Expected type string, but rule has type BazEnum");
    }

    @Test
    void shouldNotGenerateRuleTypeErrorUsingReturn() {
        var model = modelHelper.parseRosetta("""
				body Authority TEST_REG
				corpus TEST_REG MiFIR

				report TEST_REG MiFIR in T+1
				from Bar
				when FooRule
				with type BarReport

				eligibility rule FooRule from Bar:
				filter bar1 exists

				reporting rule A from Bar:
				"Not Modelled"
				as "A"

				type Bar:
				bar1 string (0..1)

				type BarReport:
				a string (1..1)
					[ruleReference A]
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldNotGenerateCountCardinalityErrorForExtract() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					foos Foo (0..*)

				type Foo:
					attr string (1..1)

				func FuncFoo:
					inputs:
						bars Bar (0..*)
					output:
						fooCounts int (0..*)

					add fooCounts:
						bars
						extract bar [ bar -> foos ]
						then extract foosItem [ foosItem count ]
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldNotGenerateCountCardinalityErrorDefaultParameterForExtract() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					foos Foo (0..*)

				type Foo:
					attr string (1..1)

				func FuncFoo:
					inputs:
						bars Bar (0..*)
					output:
						fooCounts int (0..*)

					add fooCounts:
						bars
						extract item -> foos
						then extract item count
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldNotGenerateCountCardinalityErrorForNestedExtract() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					foos Foo (0..*)

				type Foo:
					amount number (1..1)

				func FuncFoo:
					inputs:
						bars Bar (0..*)
					output:
						result boolean (1..1)

					alias results:
						bars -> foos
						extract item -> amount > 0

					set result:
						results all = True
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldNotGenerateCountCardinalityErrorDefaultParameterForNestedExtract() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					foos Foo (0..*)

				type Foo:
					amount number (1..1)

				func FuncFoo:
					inputs:
						bars Bar (0..*)
					output:
						result boolean (1..1)

					alias results:
						bars -> foos
						extract foo [ foo -> amount > 0 ]

					set result:
						results all = True
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldNotGenerateErrorForExtractListOperation() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					foo Foo (1..1)

				type Foo:
					amount number (1..1)

				func FuncFoo:
					inputs:
						bars Bar (0..*)
					output:
						result number (1..1)

					set result:
						bars
						extract item -> foo
						then extract item -> amount
						then distinct
						then only-element
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldGenerateNoErrorForFeatureCallAfterListOperation() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					foo Foo (1..1)

				type Foo:
					amount number (1..1)

				func FuncFoo:
					inputs:
						bars Bar (0..*)
					output:
						result number (1..1)

					set result:
						bars
						extract item -> foo
						then distinct only-element -> amount
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    @Disabled
    void shouldGenerateErrorForFeatureCallAfterListOperation2() {
        var model = modelHelper.parseRosetta("""
				type Bar:
					foo Foo (1..1)

				type Foo:
					amount number (1..1)

				func FuncFoo:
					inputs:
						bars Bar (0..*)
					output:
						result number (1..1)

					set result:
						if bars exists
						then bars extract [ item -> foo ] distinct only-element -> amount
				""");
        // then clause should generate syntax error (see test above shouldGenerateErrorForFeatureCallAfterListOperation)
        validationTestHelper.assertError(model, ROSETTA_MODEL, Diagnostic.SYNTAX_DIAGNOSTIC, "missing EOF at '->'");
    }

    @Test
    void shouldNotGenerateCardinalityWarning() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						n1 number (0..1)
						n2 number (0..1)
						n3 number (0..1)
					output:
						result boolean (0..1)

					set result:
						if n1 exists and n2 exists and n3 exists
						then n1 + n2 = n3
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldNotGenerateCardinalityWarning2() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						n1 number (0..1)
						n2 number (0..1)
						n3 number (0..1)
					output:
						result boolean (0..1)

					alias n3Alias:
						GetNumberList( n3 ) only-element

					set result:
						n1 + n2 = n3Alias

				func GetNumberList:
					[codeImplementation]
					inputs:
						x number (1..1)
					output:
						xs number (0..*)
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldGenerateListFilterNoExpressionError() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						filteredFoo Foo (0..*)

					add filteredFoo:
						foos
						filter

				type Foo:
					x string (1..1)
				""");
        validationTestHelper.assertError(model, FILTER_OPERATION, null, "Missing an expression.");
    }

    @Test
    void shouldGenerateListFilterParametersError() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						filteredFoo Foo (0..*)

					add filteredFoo:
						foos
						filter a, b [ a -> attr ]

				type Foo:
					attr boolean (1..1)
				""");
        validationTestHelper.assertError(model, INLINE_FUNCTION, null, "Function must have 1 named parameter.");
    }

    @Test
    void shouldGenerateListFilterExpressionTypeError() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						filteredFoo Foo (0..*)

					add filteredFoo:
						foos
						filter [ item -> x ]

				type Foo:
					x string (1..1)
				""");
        validationTestHelper.assertError(model, INLINE_FUNCTION, null, "Expression must evaluate to a boolean.");
    }

    @Test
    void shouldGenerateListExtractNoExpressionError() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						strings string (0..*)

					add strings:
						foos
						extract

				type Foo:
					x string (1..1)
				""");
        validationTestHelper.assertError(model, MAP_OPERATION, null, "Missing an expression.");
    }

    @Test
    void shouldGenerateListExtractParametersError() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						strings string (0..*)

					add strings:
						foos
						extract a, b [ a -> x ]

				type Foo:
					x string (1..1)
				""");
        validationTestHelper.assertError(model, INLINE_FUNCTION, null, "Function must have 1 named parameter.");
    }

    @Test
    void extractWithNamedFunctionReferenceShouldGenerateNoError() {
        var model = modelHelper.parseRosetta("""
				func DoSomething:
					inputs:
						a Foo (1..1)
					output:
						result string (1..1)

					set result:
						a -> x

				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						strings string (0..*)

					add strings:
						foos
						extract DoSomething

				type Foo:
					x string (1..1)
				""");
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldGenerateListExtractParametersErrorNamedFunctionReference() {
        var model = modelHelper.parseRosetta("""
				func DoSomething:
					inputs:
						a Foo (1..1)
						b boolean (1..1)
					output:
						result string (1..1)

					set result:
						a -> x

				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						strings string (0..*)

					add strings:
						foos
						extract DoSomething

				type Foo:
					x string (1..1)
				""");
        validationTestHelper.assertError(model, ROSETTA_SYMBOL_REFERENCE, null, "Expected 2 arguments, but got 0 instead");
    }

    @Test
    void shouldNotGenerateListExtractExpressionCardinalityError() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						strings string (0..*)

					add strings:
						foos
						extract a [ a -> xs ] // list of lists
						then flatten

				type Foo:
					xs string (0..*)
				""");
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldNotGenerateListExtractExpressionCardinalityError2() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						strings string (0..*)

					add strings:
						foos
						extract a [ a -> bars ] // list of list<bar>
						then extract bars [ bars -> x ] // list of list<string> (maintain same list cardinality)
						then flatten // list<string>

				type Foo:
					bars Bar (0..*)

				type Bar:
					x string (0..1)
				""");
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldNotGenerateListExtractExpressionCardinalityError3() {
        var model = modelHelper.parseRosetta("""
				func FuncFoo:
					inputs:
						foos Foo (0..*)
					output:
						strings string (0..*)

					add strings:
						foos
						extract a [ a -> bars ] // list of list<bar>
						then extract bars [ bars -> bazs ] // list of list<baz>
						then extract bazs [ bazs -> x ] // list of list<string>
						then flatten // list<string>

				type Foo:
					bars Bar (0..*)

				type Bar:
					bazs Baz (0..*)

				type Baz:
					x string (0..1)
				""");
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldGenerateListFlattenCardinalityError() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                strings string (0..*)

                add strings:
                foos
                extract a [ a -> x ] // not a list of lists
                flatten

                type Foo:
                x string (0..1) // single cardinality
                """);
        validationTestHelper.assertError(model, FLATTEN_OPERATION, null, "List flatten only allowed for list of lists.");
    }

    @Test
    void shouldGenerateListFlattenCardinalityError2() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                updatedFoos Foo (0..*)

                add updatedFoos:
                foos
                        flatten

                type Foo:
                x string (0..1)
                """);
        validationTestHelper.assertError(model, FLATTEN_OPERATION, null, "List flatten only allowed for list of lists.");
    }

    @Test
    void shouldNotGenerateListSingleCardinalityError() {
        modelHelper.parseRosettaWithNoIssues("""
                func FuncFoo:
                inputs:
                foo Foo (1..1)
                output:
                s string (1..1)

                set s:
                foo
                extract item -> x

                type Foo:
                x string (0..1)
                """);
    }

    @Test
    void shouldGenerateListSingleCardinalityError2() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foo Foo (1..1)
                output:
                onlyFoo Foo (1..1)

                set onlyFoo:
                foo
                only-element

                type Foo:
                x string (0..1)
                """);
        validationTestHelper.assertWarning(model, ROSETTA_ONLY_ELEMENT, null, "List only-element operation cannot be used for single cardinality expressions.");
    }

    @Test
    void shouldGenerateListSingleCardinalityError3() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foo Foo (1..1)
                output:
                s string (1..1)

                set s:
                foo -> x
                only-element

                type Foo:
                x string (0..1)
                """);
        validationTestHelper.assertWarning(model, ROSETTA_ONLY_ELEMENT, null, "List only-element operation cannot be used for single cardinality expressions.");
    }

    @Test
    void shouldNotGenerateListSingleCardinalityError4() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                s string (1..1)

                set s:
                foos
                only-element
                extract item -> x

                type Foo:
                x string (0..1)
                """);
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldGenerateListUnflattenedAssignOutputError() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                strings string (0..*)

                add strings:
                foos
                extract a [ a -> xs ] // list of lists

                type Foo:
                xs string (0..*)
                """);
        validationTestHelper.assertError(model, OPERATION, null, "Assign expression contains a list of lists, use flatten to create a list");
    }

    @Test
    void shouldGenerateListUnflattenedSetError() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                strings string (0..*)

                add strings:
                foos
                extract a [ a -> xs ] // list of lists

                type Foo:
                xs string (0..*)
                """);
        validationTestHelper.assertError(model, OPERATION, null, "Assign expression contains a list of lists, use flatten to create a list");
    }

    @Test
    void shouldGenerateListUnflattenedAliasError() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                strings string (0..*)

                alias stringsAlias:
                foos
                extract a [ a -> xs ] // list of lists

                add strings:
                stringsAlias

                type Foo:
                xs string (0..*)
                """);
        validationTestHelper.assertError(model, SHORTCUT_DECLARATION, null, "Alias expression contains a list of lists, use flatten to create a list.");
    }

    @Test
    void shouldGenerateListOnlyElementUnflattenedError() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                res string (0..1)

                set res:
                foos
                extract a [ a -> xs ] // list of lists
                only-element

                type Foo:
                xs string (0..*)
                """);
        validationTestHelper.assertError(model, ROSETTA_ONLY_ELEMENT, null, "List must be flattened before only-element operation.");
    }

    @Test
    void shouldGenerateListDistinctUnflattenedError() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                res string (0..*)

                add res:
                foos
                extract a [ a -> xs ] // list of lists
                distinct

                type Foo:
                xs string (0..*)
                """);
        validationTestHelper.assertError(model, DISTINCT_OPERATION, null, "List must be flattened before distinct operation.");
    }

    @Test
    void shouldNotGenerateTypeErrorForExpressionInBrackets() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foo Foo (1..1)
                output:
                result boolean (1..1)

                set result:
                ( foo -> x1 and foo -> x2 )
                and ( foo -> x4 < 5.0
                        and ( foo -> x3 is absent or foo -> x6 exists ) )

                type Foo:
                x1 boolean (1..1)
                x2 boolean (1..1)
                x3 number (0..1)
                x4 number (1..1)
                x5 int (1..1)
                x6 string (0..1)
                """);
        validationTestHelper.assertNoErrors(model);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldGenerateTypeErrorForExpressionInBrackets() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foo Foo (1..1)
                output:
                result boolean (1..1)

                set result:
                ( foo -> x1 and foo -> x2 )
                and ( foo -> x4 // number
                        and ( foo -> x3 is absent or foo -> x6 exists ) )

                type Foo:
                x1 boolean (1..1)
                x2 boolean (1..1)
                x3 number (0..1)
                x4 number (1..1)
                x5 int (1..1)
                x6 string (0..1)
                """);
        validationTestHelper.assertError(model, ROSETTA_BINARY_OPERATION, null, "Expected type `boolean`, but got `number` instead. Cannot use `number` with operator `and`");
    }

    @Test
    void shouldGenerateTypeErrorForExpressionInBrackets3() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foo Foo (1..1)
                output:
                result boolean (1..1)

                set result:
                ( foo -> x3 and foo -> x4 ) exists

                type Foo:
                x3 number (1..1)
                x4 number (1..1)
                """);
        validationTestHelper.assertError(model, LOGICAL_OPERATION, null, "Expected type `boolean`, but got `number` instead. Cannot use `number` with operator `and`");
    }

    @Test
    void shouldGenerateReduceParametersError() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                res string (1..1)

                set res:
                foos
                reduce a [ a -> x ]

                type Foo:
                x string (0..1)
                """);
        validationTestHelper.assertError(model, INLINE_FUNCTION, null, "Function must have 2 named parameters.");
    }

    @Test
    void shouldGenerateReduceTypeError() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                res string (1..1)

                set res:
                foos
                reduce a, b [ a -> x ]

                type Foo:
                x string (0..1)
                """);
        validationTestHelper.assertError(model, REDUCE_OPERATION, null, "List reduce expression must evaluate to the same type as the input. Found types Foo and string.");
    }

    @Test
    void shouldGenerateReduceCardinalityError() {
        var model = modelHelper.parseRosetta("""
                type Foo:
                x string (0..1)

                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                res Foo (1..1)

                set res:
                foos
                reduce a, b [ GetFooList( a, b ) ]

                func GetFooList:
                inputs:
                foo1 Foo (1..1)
                foo2 Foo (1..1)
                output:
                foos Foo (0..*)
                """);
        validationTestHelper.assertError(model, INLINE_FUNCTION, null, "Operation only supports single cardinality expressions.");
    }

    @Test
    void shouldGenerateListSortCardinalityError() {
        var model = modelHelper.parseRosetta("""
                type Foo:
                attrList string (1..*) // list

                func SortFooOnAttr:
                inputs:
                foos Foo (0..*)
                output:
                sortedFoos Foo (0..*)

                add sortedFoos:
                foos sort [item -> attrList] // sort based on multi-cardinality
                """);
        validationTestHelper.assertError(model, INLINE_FUNCTION, null, "Operation only supports single cardinality expressions.");
    }

    @Test
    void shouldGenerateListSortTypeError() {
        var model = modelHelper.parseRosetta("""
                type Foo:
                attrList string (1..*) // list

                func SortFooOnAttr:
                inputs:
                foos Foo (0..*)
                output:
                sortedFoos Foo (0..*)

                add sortedFoos:
                foos sort // sort based on Foo
                """);
        validationTestHelper.assertError(model, SORT_OPERATION, null, "Operation sort only supports comparable types (string, int, number, boolean, date). Found type Foo.");
    }

    @Test
    void shouldGenerateListSortTypeError2() {
        var model = modelHelper.parseRosetta("""
                type Bar:
                foo Foo (1..1)

                type Foo:
                attr string (1..1)

                func SortBarOnFoo:
                inputs:
                bars Bar (0..*)
                output:
                sortedBars Bar (0..*)

                add sortedBars:
                bars
                sort x [ x -> foo ] // sort based on Foo
                """);
        validationTestHelper.assertError(model, INLINE_FUNCTION, null, "Operation sort only supports comparable types (string, int, number, boolean, date). Found type Foo.");
    }

    @Test
    @Disabled
    void shouldGenerateListIndexNoItemExpression() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                indexFoo Foo (0..1)

                set indexFoo:
                foos
                get-item [ item -> attr ]

                type Foo:
                attr int (1..1)
                """);
        validationTestHelper.assertError(model, null, null, "List get-item does not allow expressions using an item or named parameter.");
    }

    @Test
    @Disabled
    void shouldGenerateListIndexNoNamedExpression() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                output:
                indexFoo Foo (0..1)

                set indexFoo:
                foos
                get-item x [ x -> attr ]

                type Foo:
                attr int (1..1)
                """);
        validationTestHelper.assertError(model, null, null, "List get-item does not allow expressions using an item or named parameter.");
    }

    @Test
    @Disabled
    void shouldGenerateListIndexNoItemExpression2() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                index int (1..1)
                output:
                indexFoo Foo (0..1)

                set indexFoo:
                foos
                get-item [ index ]

                type Foo:
                attr int (1..1)
                """);
        validationTestHelper.assertNoErrors(model);
    }

    @Test
    @Disabled
    void shouldGenerateListIndexNoItemExpression3() {
        var model = modelHelper.parseRosetta("""
                func FuncFoo:
                inputs:
                foos Foo (0..*)
                index int (1..1)
                output:
                removeLast Foo (0..1)

                set removeLast:
                foos
                remove-index [ foos count - 1 ]

                type Foo:
                attr int (1..1)
                """);
        validationTestHelper.assertNoErrors(model);
    }

    @Test
    void joinShouldAllowExpressions() {
        modelHelper.parseRosettaWithNoIssues("""
                func FuncFoo:
                inputs:
                stringList string (0..*)
                output:
                joined string (1..1)

                set joined:
                stringList
                join ("a" + "b")

                type Foo:
                attr int (1..1)
                """);
    }

    @Test
    void shouldWarnNonUsedImportsForData() {
        var model = modelHelper.parseRosetta("""

                import foo.bar.*


                             type Foo:
                     attr int (1..1)
                """);
        validationTestHelper.assertWarning(model, IMPORT, UNUSED_IMPORT, "Unused import foo.bar.*");
    }


    @Test
    void shouldNotWarnForValidDataImports() {
        modelHelper.parseRosettaWithNoIssues("""
                        namespace test.one

                        type Foo:
                        attr int (1..1)
                        """,
                """
                             namespace test.two
                        import test.one.*


                                     type Bar:
                             attr Foo (1..1)
                        """);
    }

    @Test
    void shouldNotWarnForValidEnumImports() {
        modelHelper.parseRosettaWithNoIssues("""
                        namespace test.one

                        enum Foo:
                        A B C
                        """,
                """
                             namespace test.two
                        import test.one.*


                                     type Bar:
                             attr Foo (1..1)
                        """);
    }

    @Test
    void shouldNotWarnForValidFuncImports() {
        modelHelper.parseRosettaWithNoIssues("""
                        namespace test.one

                        type Foo1:
                        attr int (1..1)
                        """,
                """
                        namespace test.two

                        type Foo2:
                        attr int (1..1)
                        """,
                """
                             namespace test.three
                        import test.one.*
                        import test.two.*

                                     func Bar:
                          [codeImplementation]
                             inputs:
                             foo1 Foo1 (1..1)
                             output:
                             foo2 Foo2 (1..1)
                        """);
    }

    @Test
    void shouldNotWarnForValidFuncAlias() {
        modelHelper.parseRosettaWithNoIssues("""
                        namespace test.one

                        type Foo1:
                        attr int (1..1)
                        """,
                """
                             namespace test.two
                        import test.one.*

                                     type Foo2:
                             attr Foo1 (1..1)
                        """,
                """
                             namespace test.three
                        import test.one.*

                                     func Bar:
                          [codeImplementation]
                             inputs:
                             foo1 Foo1 (1..1)
                             output:
                             foo1x Foo1 (1..1)

                             alias a: foo1 -> attr
                        """);
    }

    @Test
    void shouldNotWarnForUsedImports() {
        modelHelper.parseRosettaWithNoIssues("""
                             namespace dsl.test

                        import foo.bar.*

                                     type A:
                             a qux.MyType (1..1)
                        """,
                """
                        namespace foo.bar.qux


                        type MyType:
                        a int (0..1)
                        """);
    }

    @Test
    void shouldNotWarnForUsedImportsWithAlias() {
        modelHelper.parseRosettaWithNoIssues("""
                             namespace dsl.test

                        import foo.bar.* as bar

                             type A:
                             a bar.qux.MyType (1..1)
                        """,
                """
                        namespace foo.bar.qux


                        type MyType:
                        a int (0..1)
                        """);
    }
}
