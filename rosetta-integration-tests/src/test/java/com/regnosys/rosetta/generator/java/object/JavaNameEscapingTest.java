package com.regnosys.rosetta.generator.java.object;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class JavaNameEscapingTest {
	@Inject
	private RosettaTestModelService modelService;
	
	// TODO: also check source code to make sure these tests keep testing what they should test.
	
	@Test
	void testAttributeGetterOverlapsWithInheritedMethod() {
		modelService.toJavaTestModel("""
				type Foo:
					class int (1..1)
				""").compile();
	}
	
	@Test
	void testAttributeGetterOverlapsWithHardcodedMethod() {
		modelService.toJavaTestModel("""
				type Foo:
					^type int (1..1)
				""").compile();
	}
	
	@Test
	void testTypeNameOverlapsWithJavaLangClass() {
		modelService.toJavaTestModel("""
				type Object:
					attr int (1..1)
				
				type Foo:
				""").compile();
	}
	
	@Test
	void testJavaKeywordInNamespace() {
		modelService.toJavaTestModel("""
				namespace foo.package
				
				type Foo:
				""").compile();
	}
	
	@Test
	void testFunctionAndTypeClassNameOverlap() {
		modelService.toJavaTestModel("""
				namespace foo
				
				func Foo:
					[codeImplementation]
					output:
						result boolean (1..1)
				""",
				"""
				namespace foo.functions
				
				type Foo:
					condition C:
						foo.Foo()
				""").compile();
	}
}
