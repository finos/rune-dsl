package com.regnosys.rosetta.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.serializer.impl.Serializer;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
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
}
