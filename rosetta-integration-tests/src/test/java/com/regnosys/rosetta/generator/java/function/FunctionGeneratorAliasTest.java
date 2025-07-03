package com.regnosys.rosetta.generator.java.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.generator.java.RosettaJavaPackages;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionGeneratorAliasTest {

	@Inject
	FunctionGeneratorHelper functionGeneratorHelper;
	@Inject
	CodeGeneratorTestHelper generatorTestHelper;

	@Test
	void shouldAddComplexTypeListWithIfStatement() throws Exception {
		String model = """
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
				""";

		var code = generatorTestHelper.generateCode(model);
		var classes = generatorTestHelper.compileToClasses(code);
		var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

		var foo1 = generatorTestHelper.createInstanceUsingBuilder(classes,
				new RosettaJavaPackages.RootPackage("com.rosetta.test.model"), "Foo", Map.of("attr", "1"));
		var foo2 = generatorTestHelper.createInstanceUsingBuilder(classes,
				new RosettaJavaPackages.RootPackage("com.rosetta.test.model"), "Foo", Map.of("attr", "2"));

		var result = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, List.of(foo1, foo2), true);

		// reflective Bar.getFoos()
		Method getFoosMethod = result.getClass().getMethod("getFoos");
		@SuppressWarnings("unchecked")
		List<Object> foos = (List<Object>) getFoosMethod.invoke(result);

		assertEquals(2, foos.size());
		assertTrue(foos.containsAll(List.of(foo1, foo2))); // appends to existing list
	}

	@Test
	void shouldAddComplexTypeListAliasWithIfStatement() throws Exception {
		String model = """
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

						alias filteredFoos: newFoos

						add bar -> foos:
							if test
							then filteredFoos
				""";
		var code = generatorTestHelper.generateCode(model);
		var classes = generatorTestHelper.compileToClasses(code);
		var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

		var foo1 = generatorTestHelper.createInstanceUsingBuilder(classes,
				new RosettaJavaPackages.RootPackage("com.rosetta.test.model"), "Foo", Map.of("attr", "1"));
		var foo2 = generatorTestHelper.createInstanceUsingBuilder(classes,
				new RosettaJavaPackages.RootPackage("com.rosetta.test.model"), "Foo", Map.of("attr", "2"));

		var result = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, List.of(foo1, foo2), true);

		// reflective Bar.getFoos()
		Method getFoosMethod = result.getClass().getMethod("getFoos");
		@SuppressWarnings("unchecked")
		List<Object> foos = (List<Object>) getFoosMethod.invoke(result);

		assertEquals(2, foos.size());
		assertTrue(foos.containsAll(List.of(foo1, foo2))); // appends to existing list
	}
}