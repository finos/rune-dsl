package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class SwitchValidatorTest extends AbstractValidatorTest {

    @Inject
    private RosettaTestModelService modelService;
    @Inject
    private RosettaValidationTestHelper validationTestHelper;

    @Test
    void testSwitchDefaultMustComeAtTheEnd() {
        var expression = modelService.parseExpression("""
                42 switch
                	default False,
                	42 then True
                """);
        validationTestHelper.assertError(expression, SWITCH_CASE_OR_DEFAULT, null, "A default case is only allowed at the end");
    }

    @Test
    void testSwitchOnComplexTypeCannotHaveLiteralGuard() {
        var model = modelService.toTestModel("""
                type Foo:
                
                type Opt1 extends Foo:
                
                type Opt2 extends Foo:
                """);

        var expression = model.parseExpression("""
                foo switch
                	Opt1 then 1,
                	"Hello" then 2,
                	default 3
                """, "foo Foo (1..1)");
        validationTestHelper.assertIssues(expression, """
                ERROR (null) 'Case should be a subtype of type Foo' at 3:2, length 7, on SwitchCaseOrDefault
                """);
    }

    @Test
    void testSwitchOnComplexTypeMustBeSubtypeOfInput() {
        var model = modelService.toTestModel("""
                type Foo:
                
                type Opt1 extends Foo:
                
                type Opt2:
                """);

        var expression = model.parseExpression("""
                foo switch
                	Opt1 then 1,
                	Opt2 then 2,
                	default 3
                """, "foo Foo (1..1)");
        validationTestHelper.assertIssues(expression, """
                ERROR (null) 'Case should be a subtype of type Foo' at 3:2, length 4, on SwitchCaseOrDefault
                """);
    }

    @Test
    void testSwitchOnComplexTypeWithoutDefaultIsInvalid() {
        var model = modelService.toTestModel("""
                type Foo:
                
                type Opt1 extends Foo:
                
                type Opt2 extends Foo:
                """);

        var expression = model.parseExpression("""
                foo switch
                	Opt1 then 1,
                	Opt2 then 2
                """, "foo Foo (1..1)");
        validationTestHelper.assertIssues(expression, """
                ERROR (null) 'A switch on a complex type must have a default case' at 1:5, length 6, on SwitchOperation
                """);
    }

    @Test
    void testSwitchOnComplexTypeWithNonReachableCase() {
        var model = modelService.toTestModel("""
                type Foo:
                
                type Bar extends Foo:
                
                type Qux extends Bar:
                """);

        var expression = model.parseExpression("""
                foo switch
                	Qux then 1,
                	Foo then 2,
                	Bar then 3,
                	default 4
                """, "foo Foo (1..1)");
        validationTestHelper.assertIssues(expression, """
                ERROR (null) 'Case already covered by Foo' at 4:2, length 3, on SwitchCaseOrDefault
                """);
    }

    @Test
    void testSwitchOnChoiceCannotHaveLiteralGuard() {
        var model = modelService.toTestModel("""
                choice Foo:
                	Opt1
                	Opt2
                
                type Opt1:
                
                type Opt2:
                """);

        var expression = model.parseExpression("""
                foo switch
                	Opt1 then 1,
                	"Hello" then 2
                """, "foo Foo (1..1)");
        validationTestHelper.assertError(expression, SWITCH_CASE_OR_DEFAULT, null, "Case should match a choice option of type Foo");
    }

    @Test
    void testSwitchOnChoiceWithMissingCase() {
        var model = modelService.toTestModel("""
                choice Foo:
                	Opt1
                	Bar
                
                choice Bar:
                	Opt2
                	Opt3
                
                type Opt1:
                
                type Opt2:
                
                type Opt3:
                """);

        var expression = model.parseExpression("""
                foo switch
                	Opt1 then 1,
                	Opt2 then 2
                """, "foo Foo (1..1)");
        validationTestHelper.assertError(expression, SWITCH_OPERATION, null, "Missing the following cases: Opt3. Either provide all or add a default.");
    }

    @Test
    void testSwitchOnChoiceWithNonReachableCase() {
        var model = modelService.toTestModel("""
                choice Foo:
                	Opt1
                	Bar
                
                choice Bar:
                	Opt2
                	Opt3
                
                type Opt1:
                
                type Opt2:
                
                type Opt3:
                """);

        var expression = model.parseExpression("""
                foo switch
                	Opt1 then 1,
                	Bar then 2,
                	Opt2 then 3
                """, "foo Foo (1..1)");
        validationTestHelper.assertError(expression, SWITCH_CASE_OR_DEFAULT, null, "Case already covered by Bar");
    }

    @Test
    void testSwitchWithDuplicateCase() {
        var expression = modelService.parseExpression("""
                42 switch
                	0 then 1,
                	1 then 2,
                	0 then 3
                """);
        validationTestHelper.assertError(expression, SWITCH_CASE_OR_DEFAULT, null, "Duplicate case");
    }

    @Test
    void testSwitchInputRecordTypesAreNotValid() {
        var expression = modelService.parseExpression("someDate switch default \"someResult\"", "someDate date (1..1)");
        validationTestHelper.assertError(expression, SWITCH_OPERATION, null, "Operator `switch` is not supported for type `date`. Supported argument types are basic types, enumerations, complex types, and choice types");
    }

    @Test
    void testSwitchWithMultiCardinalityInputIsInvalid() {
        var expression = modelService.parseExpression("""
                inp switch
                	"A" then "aValue",
                	"B" then "bValue",
                	"C" then "cValue",
                	default "someOtherValue"
                """, "inp string (1..*)");
        validationTestHelper.assertWarning(expression, ROSETTA_EXPRESSION, null, "Expecting single cardinality. The `switch` operator requires a single cardinality input");
    }

    @Test
    void testValidSwitchSyntaxEnumFailsWhenMissingEnumValues() {
        var model = modelService.toTestModel("""
                enum SomeEnum:
                	A
                	B
                	C
                	D
                """);

        var expression = model.parseExpression("""
                inEnum switch
                	A then "aValue",
                	B then "bValue",
                	C then "cValue"
                """, "inEnum SomeEnum (1..1)");
        validationTestHelper.assertError(expression, SWITCH_OPERATION, null, "Missing the following cases: D. Either provide all or add a default.");
    }

    @Test
    void testSwitchArgumentMatchesCaseStatementTypes() {
        var model = modelService.toTestModel("""
                enum SomeEnum:
                	A
                	B
                	C
                	D
                """);

        var expression = model.parseExpression("""
                inEnum switch
                	A 	then "aValue",
                	10 	then "bValue",
                	C 	then "cValue",
                	default "defaultValue"
                """, "inEnum SomeEnum (1..1)");
        validationTestHelper.assertError(expression, SWITCH_CASE_OR_DEFAULT, null, "Case should match an enum value of SomeEnum");
    }

    @Test
    void testValidSwitchSyntaxWithDefault() {
        var model = modelService.toTestModel("""
                enum SomeEnum:
                	A
                	B
                	C
                	D
                """);

        var expression = model.parseExpression("""
                inEnum switch
                	A then "aValue",
                	B then "bValue",
                	C then "cValue",
                	default "defaultValue"
                """, "inEnum SomeEnum (1..1)");
        validationTestHelper.assertNoIssues(expression);
    }

    @Test
    void testValidSwitchSyntaxEnum() {
        var model = modelService.toTestModel("""
                enum SomeEnum:
                	A
                	B
                	C
                	D
                """);

        var expression = model.parseExpression("""
                inEnum switch
                	A then "aValue",
                	B then "bValue",
                	C then "cValue",
                	D then "dValue"
                """, "inEnum SomeEnum (1..1)");
        validationTestHelper.assertNoIssues(expression);
    }

    @Test
    void testValidSwitchSyntaxString() {
        var expression = modelService.parseExpression("""
                someInput switch
                	"A" then "aValue",
                	"B" then "bValue"
                """, "someInput string (1..1)");
        validationTestHelper.assertNoIssues(expression);
    }
}