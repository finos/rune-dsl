package com.regnosys.rosetta.generator.java.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaModelTest {

	@Inject
	private ModelHelper modelHelper;

	@Test
	void testEnumeration() {
		RosettaModel model = modelHelper.parseRosettaWithNoErrors("""
				enum QuoteRejectReasonEnum: <"The enumeration values.">
					UnknownSymbol <"unknown symbol">
					KnownSymbol
				""");

		RosettaEnumeration enumeration = (RosettaEnumeration) model.getElements().get(0);
		assertEquals("QuoteRejectReasonEnum", enumeration.getName());
		assertEquals("The enumeration values.", enumeration.getDefinition());

		RosettaEnumValue enumValue1 = enumeration.getEnumValues().get(0);
		assertEquals("UnknownSymbol", enumValue1.getName());
		assertEquals("unknown symbol", enumValue1.getDefinition());

		RosettaEnumValue enumValue2 = enumeration.getEnumValues().get(1);
		assertEquals("KnownSymbol", enumValue2.getName());
	}
}
