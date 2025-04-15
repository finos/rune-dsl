package com.regnosys.rosetta.parsing;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaCrossReferencingTest {
	@Inject
	private ModelHelper modelHelper;
	
	@Test
	void testTwoModelsSameNamespaceReferencesEachOther() {
		String model1 = """
			namespace test
			type A:
				id string (1..1)
			""";

		String model2 = """
			namespace test
			type B:
				a A (1..1)
			""";

		modelHelper.parseRosettaWithNoIssues(model1, model2);
	}	

	@Test
 	void testCanUseAliasesWhenImporting() {
		String model1 = """
 			namespace foo.bar
 			
 			type A:
 				id string (1..1)
			""";

		String model2 = """
 			namespace test
 			import foo.bar.* as someAlias
 			
 			type B:
 				a someAlias.A (1..1)
			""";

		modelHelper.parseRosettaWithNoIssues(model1, model2);
 	}	
	
	@Test
	void testFullyQualifiedNamesCanBeUsedInExpression() {
		String modelBar = """
			namespace test.bar
			
			enum SomeEnum:
				A
				B
				C
				D
			""";
		
		String modelFoo = """
			namespace test.foo
			
			func Test:
			    output:
			        partyIdType test.bar.SomeEnum (1..1)
			    set partyIdType: test.bar.SomeEnum -> A
			""";
		
		modelHelper.parseRosettaWithNoIssues(modelBar, modelFoo);
	}
	
	@Test
	void orderOfParsingDoesNotMatter() {
		String model1 = """
			namespace test.one
			
			type A:
				n int (1..1)
			""";
		
		String model2 = """
			namespace test.two
			
			import test.one.A
			
			type B:
				a A (1..1)
			""";
		
		modelHelper.parseRosettaWithNoIssues(model1, model2);
		modelHelper.parseRosettaWithNoIssues(model2, model1);
	}
}
