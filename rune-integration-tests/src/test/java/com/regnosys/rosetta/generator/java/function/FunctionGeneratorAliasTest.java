package com.regnosys.rosetta.generator.java.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.util.ReflectiveInvoker;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.RosettaFunction;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionGeneratorAliasTest {

	@Inject
	private RosettaTestModelService modelService;

    @Test
    void shouldSupportDependencyInAlias() {
        var model = modelService.toJavaTestModel("""
				func Foo:
					output:
						result int (1..1)
				
					alias a: Bar() + 1
					set result: a + 1
				
				func Bar:
				    output:
				        result int (1..1)
				    set result: 40
				""").compile();

        var result = model.evaluateExpression(Integer.class, "Foo()");

        assertEquals(42, result);
    }

	@Test
	void shouldSupportQualifiedEnumValueInAlias() {
		var model = modelService.toJavaTestModel("""
				enum DayOfWeekEnum: MON TUE WED

				func WithArrow:
					output:
						result DayOfWeekEnum (0..*)
					alias days: [DayOfWeekEnum -> MON, DayOfWeekEnum -> TUE]
					set result: days

				func WithDot:
					output:
						result DayOfWeekEnum (0..*)
					alias days: [DayOfWeekEnum.MON, DayOfWeekEnum.TUE]
					set result: days

				func SingleWithDot:
					output:
						result DayOfWeekEnum (1..1)
					alias day: DayOfWeekEnum.WED
					set result: day
				""").compile();

		var mon = model.getEnumJavaValue("DayOfWeekEnum", "MON");
		var tue = model.getEnumJavaValue("DayOfWeekEnum", "TUE");
		var wed = model.getEnumJavaValue("DayOfWeekEnum", "WED");

		assertEquals(List.of(mon, tue), ReflectiveInvoker.from(model.getFunctionJavaInstance("WithArrow"), "evaluate", List.class).invoke());
		assertEquals(List.of(mon, tue), ReflectiveInvoker.from(model.getFunctionJavaInstance("WithDot"), "evaluate", List.class).invoke());
		assertEquals(wed, ReflectiveInvoker.from(model.getFunctionJavaInstance("SingleWithDot"), "evaluate", model.getEnumJavaClass("DayOfWeekEnum")).invoke());
	}

	@Test
	void featureCallOnAliasOfChoiceShouldNotThrow() {
		modelService.toJavaTestModel("""
				namespace test

				type OptionA:
				    attr string (0..1)

				type OptionB:
				    attr string (0..1)

				choice SomeChoice:
				    OptionA
				    OptionB

				typeAlias ChoiceAlias: SomeChoice

				func GetOptionA:
				    inputs: x ChoiceAlias (0..1)
				    output: result OptionA (0..1)
				    set result:
				        x -> OptionA
				""").compile();
	}

	@Test
	void shouldAddComplexTypeListWithIfStatement() {
		var model = modelService.toJavaTestModel("""
				type Bar:
					foos Foo (0..*)
				type Foo:
					attr string (1..1)

				func FuncFoo:
				 	inputs:
				 		newFoos Foo (0..*)
				 		test boolean (1..1)
					output:
						bar Bar (1..1)

					add bar -> foos:
						if test
						then newFoos
				""").compile();

		var fooClass = model.getTypeJavaType("Foo");
		var foo1 = model.evaluateExpression(fooClass, """
				Foo {
					attr: "1"
				}
				""");
		var foo2 = model.evaluateExpression(fooClass, """
				Foo {
					attr: "2"
				}
				""");

		Class<? extends RosettaModelObject> barClass = model.getTypeJavaClass("Bar");
		RosettaFunction func = model.getFunctionJavaInstance("FuncFoo");
		RosettaModelObject result = ReflectiveInvoker.from(func, "evaluate", barClass).invoke(List.of(foo1, foo2),
				true);

		@SuppressWarnings("unchecked")
		List<Object> foos = (List<Object>) ReflectiveInvoker.from(result, "getFoos", List.class).invoke();

		assertEquals(2, foos.size());
		assertTrue(foos.containsAll(List.of(foo1, foo2))); // appends to existing list
	}

	@Test
	void shouldAddComplexTypeListAliasWithIfStatement() {
		var model = modelService.toJavaTestModel("""
				type Bar:
					foos Foo (0..*)
				type Foo:
					attr string (1..1)

				func FuncFoo:
				 	inputs:
				 		newFoos Foo (0..*)
				 		test boolean (1..1)
					output:
						bar Bar (1..1)

					add bar -> foos:
						if test
						then newFoos
				""").compile();

		var fooClass = model.getTypeJavaType("Foo");
		var foo1 = model.evaluateExpression(fooClass, """
				Foo {
					attr: "1"
				}
				""");
		var foo2 = model.evaluateExpression(fooClass, """
				Foo {
					attr: "2"
				}
				""");

		Class<? extends RosettaModelObject> barClass = model.getTypeJavaClass("Bar");
		RosettaFunction func = model.getFunctionJavaInstance("FuncFoo");
		RosettaModelObject result = ReflectiveInvoker.from(func, "evaluate", barClass).invoke(List.of(foo1, foo2),
				true);

		@SuppressWarnings("unchecked")
		List<Object> foos = (List<Object>) ReflectiveInvoker.from(result, "getFoos", List.class).invoke();

		assertEquals(2, foos.size());
		assertTrue(foos.containsAll(List.of(foo1, foo2))); // appends to existing list
	}
}