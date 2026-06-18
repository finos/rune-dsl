package com.regnosys.rosetta.generator.java.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaSynonym;
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
				[synonym ISO value "QuoteRejectReason" componentID 24]
					UnknownSymbol <"unknown symbol">
					[synonym ISO_20022 value "UK" definition "Unknown Symbol"]
					KnownSymbol

				synonym source ISO
				synonym source ISO_20022
				""");

		RosettaEnumeration enumeration = (RosettaEnumeration) model.getElements().get(0);
		assertEquals("QuoteRejectReasonEnum", enumeration.getName());
		assertEquals("The enumeration values.", enumeration.getDefinition());

		RosettaSynonym synonym = enumeration.getSynonyms().get(0);
		assertEquals("ISO", synonym.getSources().get(0).getName());
		assertEquals("QuoteRejectReason", synonym.getBody().getValues().get(0).getName());
		assertEquals("componentID", synonym.getBody().getValues().get(0).getRefType().getName());
		assertEquals(24, synonym.getBody().getValues().get(0).getValue());

		RosettaEnumValue enumValue1 = enumeration.getEnumValues().get(0);
		assertEquals("UnknownSymbol", enumValue1.getName());
		assertEquals("unknown symbol", enumValue1.getDefinition());

		RosettaEnumSynonym enumSynonym = enumValue1.getEnumSynonyms().get(0);
		assertEquals("ISO_20022",
				enumSynonym.getSources().stream().map(s -> s.getName()).collect(Collectors.joining()));

		assertEquals("UK", enumSynonym.getSynonymValue());
		assertEquals("Unknown Symbol", enumSynonym.getDefinition());

		RosettaEnumValue enumValue2 = enumeration.getEnumValues().get(1);
		assertEquals("KnownSymbol", enumValue2.getName());
	}
}
