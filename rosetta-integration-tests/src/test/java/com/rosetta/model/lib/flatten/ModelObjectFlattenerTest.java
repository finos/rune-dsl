package com.rosetta.model.lib.flatten;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPathValue;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class ModelObjectFlattenerTest {
	@Inject
	private RosettaTestModelService modelService;
	@Inject
	private ModelObjectFlattener modelObjectFlattener;

	private void assertFlattenedValues(RosettaModelObject instance, String expected) {
		List<RosettaPathValue> table = modelObjectFlattener.flatten(instance);
		String actual = table.stream().map(pv -> pv.getPath() + ": " + pv.getValue()).collect(Collectors.joining("\n", "", "\n"));
		Assertions.assertEquals(expected, actual);
	}

	@Test
	void flattenTest() throws IOException {
		JavaTestModel model = modelService.toJavaTestModel("""
				namespace test
				
				type Root:
				    r1 string (0..1)
				    foo Foo (0..1)
				    bar Bar (0..*)
				
				type Foo:
				    f1 string (0..1)
				    bar Bar (0..1)
				        [metadata reference]
				    bars Bar (0..*)
				   		[metadata reference]
				
				type Bar:
				    [metadata key]
				    b1 string (0..1)
				    	[metadata scheme]
				    b2 string (0..*)
				    b3 string (0..*)
				        [metadata scheme]
				""").compile();
		
		RosettaModelObject instance = model.evaluateExpression(RosettaModelObject.class, """
				Root {
		            r1: "VALUE1",
		            foo: Foo {
		                f1: "VALUE2",
		                bar: Bar {
		                    b1: "VALUE3",
		                    b3: ["VALUE4"],
		                    ...
		                },
		                bars: [
		                	Bar {
			                    b1: "VALUE5",
			                    b2: ["VALUE6", "VALUE7"],
			                    ...
			                },
			                Bar {
			                    b1: "VALUE8",
			                    b2: ["VALUE9", "VALUE10"],
			                    b3: ["VALUE11", "VALUE12"],
			                }
		                ]
		            },
		            bar: [
		                Bar {
		                    b1: "VALUE13",
		                    b2: ["VALUE14", "VALUE15"],
		                    ...
		                },
		                Bar {
		                    b1: "VALUE16",
		                    b2: ["VALUE17", "VALUE18"],
		                    b3: ["VALUE19", "VALUE20"],
		                }
		            ]
		        }
				""");

		assertFlattenedValues(instance, """
				r1: VALUE1
				foo.f1: VALUE2
				foo.bar.b1: VALUE3
				foo.bar.b3(0): VALUE4
				foo.bars(0).b1: VALUE5
				foo.bars(0).b2(0): VALUE6
				foo.bars(0).b2(1): VALUE7
				foo.bars(1).b1: VALUE8
				foo.bars(1).b2(0): VALUE9
				foo.bars(1).b2(1): VALUE10
				foo.bars(1).b3(0): VALUE11
				foo.bars(1).b3(1): VALUE12
				bar(0).b1: VALUE13
				bar(0).b2(0): VALUE14
				bar(0).b2(1): VALUE15
				bar(1).b1: VALUE16
				bar(1).b2(0): VALUE17
				bar(1).b2(1): VALUE18
				bar(1).b3(0): VALUE19
				bar(1).b3(1): VALUE20
				""");
	}

	@Test
	void flattenTestWithInheritance() throws IOException {
		JavaTestModel model = modelService.toJavaTestModel("""
				namespace test
				
				type Foo1:
					attr int (1..1)
					numberAttr number (0..1)
					parent Parent (1..1)
					parentList Parent (0..10)
					stringAttr string (1..1)
						[metadata scheme]
				
				type Foo2 extends Foo1:
					override numberAttr int(digits: 30, max: 100) (1..1)
					override parent Child (1..1)
					override parentList Child (1..1)
						[metadata reference]
					override stringAttr string(maxLength: 42) (1..1)
				
				type Foo3 extends Foo2:
					override numberAttr int (1..1)
					override parentList GrandChild (1..1)
				
				type Parent:
					subAttr boolean (1..1)
				
				type Child extends Parent:
					[metadata key]
				
				type GrandChild extends Child:
				""").compile();
		
		RosettaModelObject instance = model.evaluateExpression(RosettaModelObject.class, """
				Foo3 {
		            attr: 42,
		            numberAttr: 10,
		            parent: Child { subAttr: True },
		            parentList: GrandChild { subAttr: False },
		            stringAttr: "My string"
		        }
				""");

		assertFlattenedValues(instance, """
				attr: 42
				numberAttr: 10
				parent.subAttr: true
				parentList.subAttr: false
				stringAttr: My string
				""");
	}
}