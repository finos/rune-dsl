package com.regnosys.rosetta.parsing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaExpressionPrioritisationTest {
	@Inject
    private RosettaTestModelService modelService;
	
	@Test
	void testPrioritisationOfOperations1() {
		var model = modelService.toTestModel("""
			type Foo:
				bar Bar (0..*)
			type Bar:
				a string (0..*)
			func F:
				inputs: a string (1..1)
				output: result Bar (0..*)
				add result -> a: a
		""");
		
		assertEcoreEquals(model, """
				foo -> bar only-element -> a
					join ", "
					then extract F(item) only-element -> a
					then filter item <> "foo"
			""", """
				(((((((foo -> bar) only-element) -> a)
					join ", ")
					then (extract F(item) only-element -> a)))
					then (filter item <> "foo"))
			""",
			"foo Foo (1..1)");
	}
	
	@Test
	void testPrioritisationOfOperations2() {
		var model = modelService.toTestModel("""
			type Foo:
				bar Bar (0..*)
			type Bar:
				a string (0..*)
			func F:
				inputs: bar Bar (1..1)
				output: result boolean (1..1)
				set result: bar -> a any = "foo"
		""");
		
		assertEcoreEquals(model, """
				foo
					extract if F(bar only-element) = True and bar only-element -> a first = "bar"
						then bar
				    then extract [ item -> a
				    	filter [<> "foo"]
				    	then only-element ]
				    then extract item + "bar"
			""", """
				((foo
					extract (if ((F(bar only-element) = True) and (((bar only-element) -> a) first = "bar"))
						then bar))
				    then (extract [(((item -> a)
				    	filter [<> "foo"])
				    	then only-element)]))
				    then (extract (item + "bar"))
			""",
			"foo Foo (1..1)");
	}
	
	private void assertEcoreEquals(RosettaTestModel context, String expr1, String expr2, String... attributes) {
		var parsedExpr1 = context.parseExpression(expr1, attributes);
		var parsedExpr2 = context.parseExpression(expr2, attributes);
		assertTrue(EcoreUtil2.equals(parsedExpr1, parsedExpr2));
	}
}
