package com.regnosys.rosetta.generator.java.object;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.rosetta.model.lib.RosettaModelObject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
@Disabled
public class PruningTest {
	@Inject
	private RosettaTestModelService modelService;

	@Nested
	class SingleComplexTypeWithOptionalAttribute extends AbstractPruningTest {
		@Override
		String getModel() {
			return """
					type Root:
						foo Foo (1..1)

					type Foo:
						bar Bar (0..1)

					type Bar:
						attr int (0..1)
					""";
		}

		@Override
		Stream<Instance> provideTestInstances() {
			return Stream.of(Instance.of("No pruning", """
					Root {
						foo: Foo {
							bar: Bar {
								attr: 42
							}
						}
					}
					"""), Instance.of("Prune bar", """
					Root {
						foo: Foo {
							bar: Bar {
								attr: empty
							}
						}
					}
					""", """
					Root {
						foo: Foo {
							bar: empty
						}
					}
					"""), Instance.of("Empty foo is kept", """
					Root {
						foo: empty
					}
					"""));
		}
	}

	@Nested
	class MultiComplexTypeWithOptionalAttribute extends AbstractPruningTest {
		@Override
		String getModel() {
			return """
					type Root:
						foo Foo (1..1)

					type Foo:
						bars Bar (0..*)

					type Bar:
						attr int (0..1)
					""";
		}

		@Override
		Stream<Instance> provideTestInstances() {
			return Stream.of(Instance.of("Prune single bar", """
					Root {
						foo: Foo {
							bars: [
								Bar {
									attr: 42
								},
								Bar {
									attr: empty
								}
							]
						}
					}
					""", """
					Root {
						foo: Foo {
							bars: [
								Bar {
									attr: 42
								}
							]
						}
					}
					"""), Instance.of("Prune all bar", """
					Root {
						foo: Foo {
							bars: [
								Bar {
									attr: empty
								},
								Bar {
									attr: empty
								}
							]
						}
					}
					""", """
					Root {
						foo: Foo {
							bars: empty
						}
					}
					"""));
		}
	}

	@Nested
	class SingleComplexTypeWithRequiredAttribute extends AbstractPruningTest {
		@Override
		String getModel() {
			return """
					type Root:
						foo Foo (0..1)

					type Foo:
						bar Bar (1..1)

					type Bar:
						attr int (0..1)
					""";
		}

		@Override
		Stream<Instance> provideTestInstances() {
			return Stream.of(Instance.of("Do not prune bar", """
					Root {
						foo: Foo {
							bar: Bar {
								attr: empty
							}
						}
					}
					"""), Instance.of("Prune foo", """
					Root {
						foo: Foo {
							bar: empty
						}
					}
					""", """
					Root {
						foo: empty
					}
					"""), Instance.of("Empty foo is kept", """
					Root {
						foo: empty
					}
					"""));
		}
	}

	@Nested
	class OverrideAttributeCardinality extends AbstractPruningTest {
		@Override
		String getModel() {
			return """
					type Parent:
						foo Foo (0..1)

					type Child extends Parent:
						override foo Foo (1..1)

					type Foo:
						attr int (0..1)
					""";
		}

		@Override
		Stream<Instance> provideTestInstances() {
			return Stream.of(Instance.of("Prune optional foo", """
					Parent {
						foo: Foo {
							attr: empty
						}
					}
					""", """
					Parent {
						foo: empty
					}
					"""), Instance.of("Keep required foo", """
					Child {
						foo: Foo {
							attr: empty
						}
					}
					"""));
		}
	}

	/** General test utilities **/

	@TestInstance(Lifecycle.PER_CLASS)
	private abstract class AbstractPruningTest {
		private JavaTestModel model;

		abstract String getModel();

		abstract Stream<Instance> provideTestInstances();

		@BeforeAll
		void setup() {
			model = modelService.toJavaTestModel(getModel()).compile();
		}

		private Stream<Arguments> provideArguments() {
			return provideTestInstances().map(
					inst -> Arguments.of(inst.description(), inst.instanceExpression(), inst.expectedPrunedResult()));
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("provideArguments")
		void testPruning(String description, String instanceExpr, String expectedResult) {
			var instance = model.evaluateExpression(RosettaModelObject.class, instanceExpr);
			var expected = model.evaluateExpression(RosettaModelObject.class, expectedResult);

			Assertions.assertEquals(expected, instance.toBuilder().prune().build());
		}
	}

	private static record Instance(String description, String instanceExpression, String expectedPrunedResult) {
		static Instance of(String description, String instanceExpression, String expectedPrunedResult) {
			return new Instance(description, instanceExpression, expectedPrunedResult);
		}

		static Instance of(String description, String instanceExpression) {
			return of(description, instanceExpression, instanceExpression);
		}
	}
}
