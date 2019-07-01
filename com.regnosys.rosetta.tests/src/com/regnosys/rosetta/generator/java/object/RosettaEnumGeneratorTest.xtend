package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.enums.EnumGenerator
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.regnosys.rosetta.tests.util.ModelHelper
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaEnumGeneratorTest {
    
    @Inject extension CodeGeneratorTestHelper
    @Inject extension ModelHelper

    @Test
    def void shouldGenerateAnnotationForEnumSynonyms() {
        val code = '''
            enum TestEnum
            {
            	one <"Some description"> [synonym FpML value "oneSynonym"],
            	two <"Some other description"> [synonym FpML value "twoSynonym"]
            }
        '''.generateCode

        val testEnumCode = code.get(javaPackages.model.packageName + ".TestEnum")
        assertThat(testEnumCode, containsString('''RosettaSynonym(value = "oneSynonym", source = "FpML")'''))

        code.compileToClasses
    }

    @Test
    def void shouldGenerateAllDisplayNameAndConstructors() {
        val code = '''
            enum TestEnumWithDisplay
            {
                one displayName "uno" <"Some description"> [synonym FpML value "oneSynonym"],
                two <"Some other description"> [synonym FpML value "twoSynonym"],
                three displayName "tria" <"Some description"> [synonym FpML value "threeSynonym"],                
                four  displayName "tessera" <"Some description"> [synonym FpML value "fourSynonym"]
            }
        '''.generateCode

        val testEnumCode = code.get(javaPackages.model.packageName + ".TestEnumWithDisplay")
        assertThat(testEnumCode,
            allOf(containsString('''TestEnumWithDisplay()'''),
                containsString('''TestEnumWithDisplay(String displayName)'''),
                containsString('''public String toString()''')))

        code.compileToClasses
    }

    @Test
    def void shouldGenerateUppercaseUnderscoreFormattedEnumNames() {
        assertThat(EnumGenerator.formatEnumName("ISDA1993Commodity"), is("ISDA_1993_COMMODITY"))
        assertThat(EnumGenerator.formatEnumName("ISDA1998FX"), is("ISDA1998FX"))
        assertThat(EnumGenerator.formatEnumName("iTraxxEuropeDealer"), is("I_TRAXX_EUROPE_DEALER"))
        assertThat(EnumGenerator.formatEnumName("StandardLCDS"), is("STANDARD_LCDS"))
        assertThat(EnumGenerator.formatEnumName("_1_1"), is("_1_1"))
        assertThat(EnumGenerator.formatEnumName("_30E_360_ISDA"), is("_30E_360_ISDA"))
        assertThat(EnumGenerator.formatEnumName("ACT_365L"), is("ACT_365L"))
        assertThat(EnumGenerator.formatEnumName("OSPPrice"), is("OSP_PRICE"))
        assertThat(EnumGenerator.formatEnumName("FRAYield"), is("FRA_YIELD"))
        assertThat(EnumGenerator.formatEnumName("AED-EBOR-Reuters"), is("AED_EBOR_REUTERS"))
        assertThat(EnumGenerator.formatEnumName("EUR-EURIBOR-Reuters"), is("EUR_EURIBOR_REUTERS"))
        assertThat(EnumGenerator.formatEnumName("DJ.iTraxx.Europe"), is("DJ_I_TRAXX_EUROPE"))
        assertThat(EnumGenerator.formatEnumName("IVS1OpenMarkets"), is("IVS_1_OPEN_MARKETS"))
        assertThat(EnumGenerator.formatEnumName("D"), is("D"))
        assertThat(EnumGenerator.formatEnumName("_1"), is("_1"))
        assertThat(EnumGenerator.formatEnumName("DJ.CDX.NA"), is("DJ_CDX_NA"))
        assertThat(EnumGenerator.formatEnumName("novation"), is("NOVATION"))
        assertThat(EnumGenerator.formatEnumName("partialNovation"), is("PARTIAL_NOVATION"))
        assertThat(EnumGenerator.formatEnumName("ALUMINIUM_ALLOY_LME_15_MONTH"), is("ALUMINIUM_ALLOY_LME_15_MONTH"))
        assertThat(EnumGenerator.formatEnumName("AggregateClient"), is("AGGREGATE_CLIENT"))
        assertThat(EnumGenerator.formatEnumName("Currency1PerCurrency2"), is("CURRENCY_1_PER_CURRENCY_2"))
    }
}
