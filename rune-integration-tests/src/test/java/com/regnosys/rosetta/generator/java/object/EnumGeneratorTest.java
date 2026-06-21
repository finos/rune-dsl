package com.regnosys.rosetta.generator.java.object;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.generator.java.enums.EnumHelper;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class EnumGeneratorTest {

	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;
	@Inject
	private ModelHelper modelHelper;

	@Test
	void shouldGenerateAnnotationForEnumSynonyms() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				synonym source FpML
				enum TestEnum:
					one <"Some description"> [synonym FpML value "oneSynonym"]
					two <"Some other description"> [synonym FpML value "twoSynonym"]
				""");

		String testEnumCode = code.get(modelHelper.rootPackage() + ".TestEnum");
		assertThat(testEnumCode, containsString("RosettaSynonym(value = \"oneSynonym\", source = \"FpML\")"));

		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void shouldGenerateBasicReferenceForEnum() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				namespace "com.rosetta.test.model"
				version "test"

				enum TestEnum:
					one
					two

				type TestObj:
					attr TestEnum (1..1)
				""");

		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void shouldGenerateAllDisplayNameAndConstructors() {
		Map<String, String> code = generatorTestHelper.generateCode("""
				synonym source FpML
				enum TestEnumWithDisplay:
					one displayName "uno" <"Some description"> [synonym FpML value "oneSynonym"]
					two <"Some other description"> [synonym FpML value "twoSynonym"]
					three displayName "tria" <"Some description"> [synonym FpML value "threeSynonym"]
					four  displayName "tessera" <"Some description"> [synonym FpML value "fourSynonym"]
				""");

		String testEnumCode = code.get(modelHelper.rootPackage() + ".TestEnumWithDisplay");
		assertThat(testEnumCode,
				allOf(containsString("TestEnumWithDisplay(String rosettaName, String displayName)"),
						containsString("public String toDisplayString()")));

		generatorTestHelper.compileToClasses(code);
	}

	@Test
	void shouldGenerateUppercaseUnderscoreFormattedEnumNames() {
		assertThat(EnumHelper.formatEnumName("ISDA1993Commodity"), is("ISDA_1993_COMMODITY"));
		assertThat(EnumHelper.formatEnumName("ISDA1998FX"), is("ISDA1998FX"));
		assertThat(EnumHelper.formatEnumName("iTraxxEuropeDealer"), is("I_TRAXX_EUROPE_DEALER"));
		assertThat(EnumHelper.formatEnumName("StandardLCDS"), is("STANDARD_LCDS"));
		assertThat(EnumHelper.formatEnumName("_1_1"), is("_1_1"));
		assertThat(EnumHelper.formatEnumName("_30E_360_ISDA"), is("_30E_360_ISDA"));
		assertThat(EnumHelper.formatEnumName("ACT_365L"), is("ACT_365L"));
		assertThat(EnumHelper.formatEnumName("OSPPrice"), is("OSP_PRICE"));
		assertThat(EnumHelper.formatEnumName("FRAYield"), is("FRA_YIELD"));
		assertThat(EnumHelper.formatEnumName("AED-EBOR-Reuters"), is("AED_EBOR_REUTERS"));
		assertThat(EnumHelper.formatEnumName("EUR-EURIBOR-Reuters"), is("EUR_EURIBOR_REUTERS"));
		assertThat(EnumHelper.formatEnumName("DJ.iTraxx.Europe"), is("DJ_I_TRAXX_EUROPE"));
		assertThat(EnumHelper.formatEnumName("IVS1OpenMarkets"), is("IVS_1_OPEN_MARKETS"));
		assertThat(EnumHelper.formatEnumName("D"), is("D"));
		assertThat(EnumHelper.formatEnumName("_1"), is("_1"));
		assertThat(EnumHelper.formatEnumName("DJ.CDX.NA"), is("DJ_CDX_NA"));
		assertThat(EnumHelper.formatEnumName("novation"), is("NOVATION"));
		assertThat(EnumHelper.formatEnumName("partialNovation"), is("PARTIAL_NOVATION"));
		assertThat(EnumHelper.formatEnumName("ALUMINIUM_ALLOY_LME_15_MONTH"), is("ALUMINIUM_ALLOY_LME_15_MONTH"));
		assertThat(EnumHelper.formatEnumName("AggregateClient"), is("AGGREGATE_CLIENT"));
		assertThat(EnumHelper.formatEnumName("Currency1PerCurrency2"), is("CURRENCY_1_PER_CURRENCY_2"));
	}
}
