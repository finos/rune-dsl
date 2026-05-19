package com.regnosys.rosetta.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import javax.inject.Inject;

import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.serializer.impl.Serializer;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.LogicalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaAbsentExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaSerializationTest {

	@Inject
	private RosettaTestModelService modelService;

	@Inject
	private Serializer serializer;

	@Test
	void serializesImplicitAttributeSymbolReferenceWithLocalName() {
		RosettaTestModel testModel = modelService.toTestModel("""
			namespace test

			type Foo:
				foo string (0..1)
			""");
		Data data = testModel.getType("Foo");
		Attribute attribute = data.getAttributes().getFirst();

		RosettaSymbolReference reference = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		reference.setSymbol(attribute);
		RosettaExistsExpression exists = ExpressionFactory.eINSTANCE.createRosettaExistsExpression();
		exists.setOperator("exists");
		exists.setArgument(reference);
		Condition condition = SimpleFactory.eINSTANCE.createCondition();
		condition.setName("FooExists");
		condition.setExpression(exists);
		data.getConditions().add(condition);

		String serialized = serializer.serialize(testModel.getModel());
		Condition serializedCondition = modelService.toTestModel(serialized).getCondition("Foo", "FooExists");
		RosettaExistsExpression serializedExists = (RosettaExistsExpression) serializedCondition.getExpression();
		RosettaSymbolReference serializedReference = (RosettaSymbolReference) serializedExists.getArgument();

		assertEquals("foo", NodeModelUtils.getTokenText(NodeModelUtils.findActualNodeFor(serializedReference)));
	}

	@Test
	void serializesRightNestedLogicalOperationWithoutFormatterExplosion() {
		RosettaTestModel testModel = modelService.toTestModel("""
			namespace test

			type Foo:
				foo string (0..1)
			""");
		Data data = testModel.getType("Foo");
		Attribute attribute = data.getAttributes().getFirst();

		Condition condition = SimpleFactory.eINSTANCE.createCondition();
		condition.setName("ManyFoosExist");
		condition.setExpression(rightNestedAnd(attribute, 1_000));
		data.getConditions().add(condition);

		String serialized = assertTimeout(Duration.ofSeconds(10), () -> serializer.serialize(testModel.getModel()));

		assertTrue(serialized.contains("condition ManyFoosExist:"));
	}

	@Test
	void serializesElseIfChainWithoutFormatterExplosion() {
		RosettaTestModel testModel = modelService.toTestModel("""
			namespace test

			type Foo:
				foo string (0..1)
			""");
		Data data = testModel.getType("Foo");
		Attribute attribute = data.getAttributes().getFirst();

		Condition condition = SimpleFactory.eINSTANCE.createCondition();
		condition.setName("ChoiceChain");
		condition.setExpression(rightNestedConditional(attribute, 100));
		data.getConditions().add(condition);

		String serialized = assertTimeout(Duration.ofSeconds(10), () -> serializer.serialize(testModel.getModel()));

		assertTrue(serialized.contains("condition ChoiceChain:"));
	}

	private RosettaExpression rightNestedAnd(Attribute attribute, int count) {
		RosettaExpression result = exists(attribute);
		for (int i = 1; i < count; i++) {
			LogicalOperation and = ExpressionFactory.eINSTANCE.createLogicalOperation();
			and.setOperator("and");
			and.setLeft(exists(attribute));
			and.setRight(result);
			result = and;
		}
		return result;
	}

	private RosettaExpression rightNestedConditional(Attribute attribute, int count) {
		RosettaExpression result = absent(attribute);
		for (int i = 0; i < count; i++) {
			RosettaConditionalExpression conditional = ExpressionFactory.eINSTANCE.createRosettaConditionalExpression();
			conditional.setIf(exists(attribute));
			conditional.setIfthen(absent(attribute));
			conditional.setFull(true);
			conditional.setElsethen(result);
			result = conditional;
		}
		return result;
	}

	private RosettaExistsExpression exists(Attribute attribute) {
		RosettaSymbolReference reference = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		reference.setSymbol(attribute);
		RosettaExistsExpression exists = ExpressionFactory.eINSTANCE.createRosettaExistsExpression();
		exists.setOperator("exists");
		exists.setArgument(reference);
		return exists;
	}

	private RosettaAbsentExpression absent(Attribute attribute) {
		RosettaSymbolReference reference = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		reference.setSymbol(attribute);
		RosettaAbsentExpression absent = ExpressionFactory.eINSTANCE.createRosettaAbsentExpression();
		absent.setOperator("is absent");
		absent.setArgument(reference);
		return absent;
	}
}
