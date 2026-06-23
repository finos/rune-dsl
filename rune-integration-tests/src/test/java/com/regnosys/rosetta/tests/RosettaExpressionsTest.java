package com.regnosys.rosetta.tests;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.ARITHMETIC_OPERATION;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A set of tests for all instances of RosettaExpression i.e. RosettaAdditiveExpression
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaExpressionsTest {

    @Inject
    private CodeGeneratorTestHelper generatorTestHelper;
    @Inject
    private ModelHelper modelHelper;
    @Inject
    private ValidationTestHelper validationTestHelper;
    @Inject
    private EqualityHelper eqHelper;

    @Test
    void absentElseBranchShouldBeSyntacticSugarForEmptyListLiteral() {
        RosettaModel model = modelHelper.parseRosettaWithNoErrors("""
                func AbsentElseSyntacticSugar:
                    output: result int (0..1)
                    set result:
                        if True then 0
                """);

        List<? extends EObject> elements = model.getElements();
        Function function = (Function) elements.get(elements.size() - 1);
        RosettaConditionalExpression conditional =
                (RosettaConditionalExpression) function.getOperations().get(0).getExpression();

        assertFalse(conditional.isFull());
        assertNotNull(conditional.getElsethen());
        ListLiteral lit = ExpressionFactory.eINSTANCE.createListLiteral();
        lit.setGenerated(true);
        assertTrue(eqHelper.equals(conditional.getElsethen(), lit));
    }

    @Test
    void shouldParseQualifierWithAdditiveExpression() {
        modelHelper.parseRosettaWithNoErrors("""
                type Test:
                    one number (1..1)
                    two number (1..1)

                func TestQualifier:
                    inputs: test Test (1..1)
                    output: result boolean (1..1)
                    set result:
                        test -> one + test -> two = 42
                """);
    }

    @Test
    void shouldParseNoIssuesWhenDateSubtraction() {
        RosettaModel model = modelHelper.parseRosettaWithNoErrors("""
                type Test:
                    one date (1..1)
                    two date (1..1)

                func TestQualifier:
                    inputs: test Test (1..1)
                    output: result boolean (1..1)
                    set result:
                        test -> one - test -> two = 42
                """);
        validationTestHelper.assertNoIssues(model);
    }

    @Test
    void shouldParseWithErrorWhenAddingDates() {
        RosettaModel model = modelHelper.parseRosetta("""
                type Test:
                    one date (1..1)
                    two date (1..1)


                func TestQualifier:
                    inputs: test Test (1..1)
                    output: result boolean (1..1)
                    set result:
                        test -> one + test -> two = 42
                """);
        validationTestHelper.assertError(model, ARITHMETIC_OPERATION, null,
                "Expected type `time`, but got `date` instead. Cannot add `date` to a `date`");
    }

    /**
     * The openjdk 11 compiler requires extra generics information for compilation. Eclipse compiler doesn't need this
     * so you will get a nice surprise when you build your generated code using javac compiler (Maven and IntelliJ).
     */
    @Test
    void shouldCodeGenerateWithMoreGenericsInformation() {
        Map<String, String> code = generatorTestHelper.generateCode("""
                type Test:
                    one date (1..1)
                    two date (1..1)

                func TestQualifier:
                    inputs: test Test (1..1)
                    output: result boolean (1..1)
                    set result:
                        test -> one - test -> two = 42
                """);

        String qualifier = code.get("com.rosetta.test.model.functions.TestQualifier");
        assertThat(qualifier, containsString("MapperMaths.<Integer, Date, Date>subtract"));
    }

    @Test
    @Disabled
    void shoudCodeGenerateAndCompileWhenSubtractingDates() {
        Map<String, String> code = generatorTestHelper.generateCode("""
                type Test:
                    one date (1..1)
                    two date (1..1)

                func TestQualifier:
                    inputs: test Test (1..1)
                    output: result boolean (1..1)
                    set result:
                        test -> one - test -> two = 42
                """);

        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shoudCodeGenerateAndCompileWhenAddingNumbers() {
        Map<String, String> code = generatorTestHelper.generateCode("""
                type Test:
                    one number (1..1)
                    two int (1..1)


                func TestQualifier:
                    inputs: test Test (1..1)
                    output: result boolean (1..1)
                    set result:
                        test -> one + test -> two = 42
                """);

        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shoudCodeGenerateAndCompileAccessingMetaSimple() {
        Map<String, String> code = generatorTestHelper.generateCode("""
                type Test:
                    one string (1..1)
                        [metadata scheme]
                    two int (1..1)

                func TestQualifier:
                    inputs: test Test (1..1)
                    output: result boolean (1..1)
                    set result:
                        test -> one -> scheme = "scheme"
                """);
        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shoudCodeGenerateAndCompileAccessingMeta() {
        Map<String, String> code = generatorTestHelper.generateCode("""
                type Test:
                    one Foo (1..1)
                        [metadata scheme]
                    two int (1..1)

                type Foo:
                    one string (1..1)
                        [metadata scheme]
                    two int (1..1)

                func TestQualifier:
                    inputs: test Test(1..1)
                    output: is_product boolean (1..1)
                    set is_product:
                        test -> one -> scheme = "scheme"
                """);

        generatorTestHelper.compileToClasses(code);
    }

    @Test
    void shoudCodeGenerateAndCompileAccessPastMeta() {
        Map<String, String> code = generatorTestHelper.generateCode("""
                type Test:
                    one Foo (1..1)
                        [metadata scheme]
                    two int (1..1)

                type Foo:
                    one string (1..1)
                        [metadata scheme]
                    two int (1..1)

                func TestQualifier:
                    inputs: test Test(1..1)
                    output: is_product boolean (1..1)
                    set is_product:
                        test -> one -> one = "scheme"
                """);

        generatorTestHelper.compileToClasses(code);
    }
}
