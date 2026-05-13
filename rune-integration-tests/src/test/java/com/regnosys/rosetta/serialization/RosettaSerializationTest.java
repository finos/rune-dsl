package com.regnosys.rosetta.serialization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.eclipse.xtext.serializer.impl.Serializer;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaSerializationTest {

	@Inject
	private ModelHelper modelHelper;

	@Inject
	private Serializer serializer;

	@Test
	void serializesImplicitAttributeSymbolReferenceWithLocalName() {
		RosettaModel model = modelHelper.parseRosettaWithNoIssues("""
			namespace test

			type Foo:
				foo string (0..1)
			""");
		Data data = (Data) model.getElements().stream()
				.filter(Data.class::isInstance)
				.findFirst()
				.orElseThrow();
		Attribute attribute = data.getAttributes().stream()
				.filter(attr -> "foo".equals(attr.getName()))
				.findFirst()
				.orElseThrow();

		RosettaSymbolReference reference = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		reference.setSymbol(attribute);
		RosettaExistsExpression exists = ExpressionFactory.eINSTANCE.createRosettaExistsExpression();
		exists.setOperator("exists");
		exists.setArgument(reference);
		Condition condition = SimpleFactory.eINSTANCE.createCondition();
		condition.setName("FooExists");
		condition.setExpression(exists);
		data.getConditions().add(condition);

		String serialized = serializer.serialize(model);

		assertTrue(serialized.contains("condition FooExists:\n        foo exists"), serialized);
	}
}
