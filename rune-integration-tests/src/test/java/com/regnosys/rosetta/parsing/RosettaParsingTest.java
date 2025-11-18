package com.regnosys.rosetta.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.MapOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaExistsExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaPatternLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.expression.ThenOperation;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaParsingTest {
	@Inject
    private RosettaTestModelService modelService;
	@Inject
	private RosettaValidationTestHelper validationHelper;

    @Test
    void testCanSwitchOverTypeFromAnotherNamespace() {
        var model1 = """
                namespace other
        
                type Baz:
        
                type Foo extends Baz:
                    someBoolean boolean (0..1)
        
                 type Bar extends Baz:
                    someBoolean boolean (0..1)
        """;

        var model2 = """
                import other.* as other
        
                func MyFunc:
                    inputs:
                        baz other.Baz (1..1)
                    output:
                        result string (0..1)
        
                    set result:
                        baz switch
                            other.Foo then "Foo",
                            other.Bar then "Bar",
                            default empty
        """;


        modelService.toTestModel(model2, true, model1);
    }
	
	@Test
	void testCannotSetEnumAttribute() {
        // TODO: revert back to `assertIssues`
		assertNoIssues("""
				enum FooEnum:
					VALUE1
					VALUE2
	
				func MyFunc:
					output:
						result FooEnum (0..1)
					set result -> VALUE1: empty
				"""); /*,
				"""
				ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Linking) 'Couldn't resolve reference to RosettaFeature 'VALUE1'.' at 11:16, length 6, on Segment
				""");*/
	}
	
	@Test
	void testRuleReferenceAnnotation() {
		assertNoIssues("""
			type SuperFoo:
				superAttr string (1..1)
					[ruleReference SimpleAttr]
				superBar Bar (1..1)
					[ruleReference for barAttr SimpleAttr]
					[ruleReference for item -> nestedBar -> barAttr SimpleAttr]
			
			type Foo extends SuperFoo:
				override superAttr string (1..1)
					[ruleReference empty]
				override superBar Bar (1..1)
					[ruleReference for barAttr empty]
				attr string (1..1)
					[ruleReference for item SimpleAttr]
			
			type Bar:
				barAttr string (1..1)
				nestedBar Bar (0..1)
			
			reporting rule SimpleAttr from int:
				"test"
			""");
	}
	
	@Test
	void testLabelAnnotation() {
		assertNoIssues("""
			type Foo:
				attr1 string (1..1)
					[label "My Attribute"]
				attr2 string (1..1)
					[label for item "My Attribute"]
				bar Bar (1..1)
					[label for barAttr "Bar Attribute"]
					[label for item -> nestedBar -> barAttr "Nested Bar Attribute"]
				qux Qux (1..1)
					[label for item ->> id "Qux ID"]
			
			
			type Bar:
				barAttr string (1..1)
				nestedBar Bar (0..1)
			
			choice Qux:
				Opt1
				Opt2
			
			type Opt1:
				id string (1..1)
				opt1Attr int (1..1)
			
			type Opt2:
				id string (1..1)
				opt2Attribute int (1..1)
			""");
	}
	
	@Test
	void testCanOverrideAttributeOfParent() {
		assertNoIssues("""
			type Bar:
				attr number (0..1)
			
			type Foo extends Bar:
				override attr number (1..1)
		""");
	}
	
	@Test
	void testNamespaceDescription() {
		assertNoIssues("""
			namespace cdm.base.test : <"some description">
			version "test"
			
			enum TestEnum:
				ONE 
				TWO 
			
		""");
	}
	
	@Test
	void canPassMetadataToFunctions() {
		assertNoIssues("""
			func MyFunc:
			    inputs:
			        myInput string (1..1)
			        [metadata scheme]
			    output:
			        myResult string (1..1)
			
			    set myResult: myInput -> scheme
		""");
	}
	
	@Test
	void testOnlyExistsInsideFunctionalOperation() {
		assertNoIssues("""
		type A:
			b B (1..1)
		type B:
			val boolean (0..1)
		
		func Foo:
			inputs: a A (1..1)
			output: result boolean (1..1)
			set result:
				a extract
					if item -> b -> val only exists
					then True
		""");
	}
	
	@Test
	void testMaxCanBeChainedWithThen() {
		assertNoIssues("""		
		func Foo:
			output: result int (0..*)
			add result:
				[1, 2, 3]
					extract item + 1
					then max
		""");
	}
	
	@Test
	@Disabled // see issue https://github.com/finos/rune-dsl/issues/524
	void testPatternLiterals() {
		var model = modelService.toTestModel("""
           func Foo:
             output: result pattern (0..*)
             
             add result: /ABC/
             add result: /[a-z]*/
             add result: /\\/\\+/
	    """);
	    
		var foo = model.getFunction("Foo");
		var patterns = foo.getOperations().stream()
			.map(op -> ((RosettaPatternLiteral) op.getExpression()).getValue().pattern())
			.toList();
		assertEquals("ABC", patterns.get(0));
		assertEquals("[a-z]*", patterns.get(1));
		assertEquals("/\\+", patterns.get(2));
	}
	
	@Test
	void testTypeAliases() {
		assertNoIssues("""
			typeAlias int(digits int, min int, max int): number(digits: digits, fractionalDigits: 0, min: min, max: max)
			typeAlias max4String: string(minLength: 1, maxLength: 4)
		""");
	}
	
	@Test
	void testTypeAliasesWithConditions() {
		assertNoIssues("""
			func Foo:
				inputs:
					code DomainCodeList (1..1)
					domain string (1..1)
				output: result boolean (1..1)
				set result: True
			typeAlias DomainCodeList (domain string): string
				condition IsValidCode: Foo(item, domain)
		""");
	}
	
	@Test
	void testParametrizedBasicTypes() {
		assertNoIssues("""
			basicType pattern
			basicType int(digits int, min int, max int)
			basicType number(digits int, fractionalDigits int, min number, max number)
			basicType string(minLength int, maxLength int, pattern pattern)
		""");
	}
	
	void externalRuleReferenceParseTest() {
		assertNoIssues("""
			type Foo:
				foo string (0..1)
			
			reporting rule RA:
				return "A"
			
			reporting rule RB:
				return "B"
			
			rule source TestA {
				Foo:
				+ foo
					[ruleReference RA]
			}
			
			rule source TestB extends TestA {
				Foo:
				- foo
				+ foo
					[ruleReference RB]
			}
		""");
	}
	
	@Test
	void ambiguousReferenceAllowed() {
		var model = modelService.toTestModel("""
			type Foo:
				a int (1..1)
			
			func F:
				inputs:
					foo Foo (1..1)
					a int (1..1)
				output: result int (1..1)
				set result:
					foo extract a
		""");
		var f = model.getFunction("F");
		var aInput = f.getInputs().get(1);
		RosettaExpression extractBody = ((MapOperation)f.getOperations().get(0).getExpression()).getFunction().getBody();
		assertInstanceOf(RosettaSymbolReference.class, extractBody);
		assertEquals(aInput, ((RosettaSymbolReference) extractBody).getSymbol());
	}
	
	@Test
	void nameParsingDoesNotConflictWithScientificNotation() {
		assertNoIssues("""           
           type E2:
             e2 int (1..1)
	    """);
	}
	
	@Test
	void scientificNotationIsNotTooLoose() {
		assertIssues("""
           func Foo:
             output: result number (0..*)
             
             add result: .4a3
		""",
		"ERROR (org.eclipse.xtext.diagnostics.Diagnostic.Syntax) 'Character a is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.' at 7:24, length 4, on RosettaNumberLiteral"
	    );
	}
	
	@Test
	void canParseScientificNotation() {
		assertNoIssues("""
           func Foo:
             output: result number (0..*)
             
             add result: .4e3
             add result: -5.E-2
             add result: 3.3e+42
             add result: 0.e0
	    """);
	}
	
	@Test
	void testImplicitInput() {
	    var model = modelService.toTestModel("""
           type Foo:
               a int (0..1)
               b string (0..1)
               
               condition C:
                   [deprecated] // the parser should parse this as an annotation, not a list
                   extract item -> a
                   then exists
           
           func F:
               inputs:
                   a int (1..1)
               output:
                   result boolean (1..1)
               set result:
                   a extract
                       if F
                       then False
                       else True and F
	    """, false);
	    
	    var foo = model.getType("Foo");
	    Condition c = foo.getConditions().get(0);
	    assertEquals(1, c.getAnnotations().size());
	    assertTrue(c.getExpression() instanceof ThenOperation);
	    assertTrue(((ThenOperation) c.getExpression()).getFunction().getBody() instanceof RosettaExistsExpression);
	    
	    validationHelper.assertNoIssues(model.getModel());
	}
	
	@Test
	void testExplicitArguments() {
	    var model = modelService.toTestModel("""
           func F:
               inputs:
                   a int (1..1)
               output:
                   result boolean (1..1)
               set result:
                   F(a)
	    """);
	    
	    var f = model.getFunction("F");
	    assertTrue(((RosettaSymbolReference) f.getOperations().get(0).getExpression()).isExplicitArguments());
	}
	
	@Test
	void testMultiExtract() {
	    var model = modelService.toTestModel("""
           func Test:
               output:
                   result boolean (0..*)
               add result:
                   [True, False]
                       extract [item = False]
                       extract [item = True]
	    """, false);
	    
	    var test = model.getFunction("Test");
	    RosettaExpression expression = test.getOperations().get(0).getExpression();
	    assertTrue(expression instanceof MapOperation);
	    RosettaExpression argument = ((MapOperation) expression).getArgument();
	    assertTrue(argument instanceof MapOperation);
	    RosettaExpression list = ((MapOperation) argument).getArgument();
	    assertTrue(list instanceof ListLiteral);
	}
	
	@Test
	void testOnlyElementInsidePath() {
		assertNoIssues("""
	           type A:
	               b B (0..*)
	           type B:
	               c C (1..1)
	           type C:
	           
	           func Test:
	               inputs:
	                   a A (1..1)
	               output:
	                   c C (1..1)
	               set c:
	                   a -> b only-element -> c
	    """);
	}
	
	@Test
	void testClass() {
		assertNoIssues("""
			synonym source FpML
			synonym source FIX
			
			type PartyIdentifier: <"The set of [partyId, PartyIdSource] associated with a party.">
				partyId string (1..1) <"The identifier associated with a party, e.g. the 20 digits LEI code.">
					[synonym FIX value "PartyID" tag 448]
					[synonym FpML value "partyId"]
		""");
	}
	
	@Test
	void testClassWithEnumReference() {
		assertNoIssues("""
			synonym source FpML
			synonym source FIX
			
			type PartyIdentifier: <"Bla">
				partyId string (1..1) <"Bla">
					[synonym FIX value "PartyID" tag 448]
					[synonym FpML value "partyId"]
				partyIdSource PartyIdSourceEnum (1..1)
					[synonym FIX value "PartyIDSource" tag 447]
					[synonym FpML value "PartyIdScheme"]
			
			enum PartyIdSourceEnum:
				LEI <"The Legal Entity Identifier">
				BIC <"The Bank Identifier Code">
				MIC
		""");
	}
	
	@Test
	void testStandards() {
		assertNoIssues("""
			synonym source FIX
			synonym source FpML
			synonym source ISO_20022
			
			type BasicTypes: <"">
				partyId string (1..1) <"The identifier associated with a party, e.g. the 20 digits LEI code.">
					[synonym FIX value "PartyID" tag 448]
					[synonym FpML value "partyId"]
					[synonym ISO_20022 value "partyId"]
		""");
	}
	
	@Test
	void testSynonymRefs() {
		assertNoIssues("""
			synonym source FIX
			type BasicTypes: <"">
				partyId string (1..1) <"The identifier associated with a party, e.g. the 20 digits LEI code.">
					[synonym FIX value "PartyID" tag 448]
					[synonym FIX value "PartyID" componentID 448]
					[synonym FIX value "PartyID.value"]
		""");
	}
	
	@Test
	void testBasicTypes() {
		assertNoIssues("""
			type Standards: <"">
				value1 int (0..1) <"">
				value3 number (0..1) <"">
				value5 boolean (0..1) <"">
				value6 date (0..1) <"">
				value9 string (0..1) <"">
				value10 zonedDateTime (0..1) <"">
		""");
	}
	
	@Test
	void testEnumRegReferences() {
		assertNoIssues("""
			enum PartyIdSourceEnum: <"The enumeration values associated with party identifier sources.">
				LEI <"The ISO 17442:2012 Legal Entity Identifier.">
				BIC <"The Bank Identifier Code.">
				MIC <"The ISO 10383 Market Identifier Code, applicable to certain types of execution venues, such as exchanges.">
				NaturalPersonIdentifier <"The natural person identifier.  When constructed according.">
		""");
	}
	
	@Test
	void testMultipleSynonyms() {
		assertNoIssues("""
			synonym source FpML
			synonym source FIX

			type PartyIdentifier: <"The set of [partyId, PartyIdSource] associated with a party.">
				partyId string (1..1) <"The identifier associated with a party, e.g. the 20 digits LEI code.">
					[synonym FIX value "PartyID" tag 448]
					[synonym FpML value "partyId"]
				partyIdSource PartyIdSourceEnum (1..1) <"The reference source for the partyId, e.g. LEI, BIC.">
					[synonym FIX value "PartyIDSource" tag 447]
					[synonym FpML value "PartyIdScheme"]
			enum PartyIdSourceEnum: <"The enumeration values associated with party identifier sources.">
				LEI <"The Legal Entity Identifier">
				BIC <"The Bank Identifier Code">
				MIC <"The ISO 10383 Market Identifier Code, applicable to certain types of execution venues, such as exchanges.">
		""");
	}

	@Test
	void testEnumeration() {
		assertNoIssues("""
			synonym source FpML
			synonym source FIX
			
			enum QuoteRejectReasonEnum: <"The enumeration values to qualify the reason as to why a quote has been rejected.">
				UnknownSymbol
					[synonym FIX value "1" definition "foo"]
				ExchangeClosed
					[synonym FpML value "exchangeClosed" definition "foo" pattern "" ""]
		""");
	}
	
	@Test
	void testMultipleOrNoAttributeSynonym() {
		assertNoIssues("""
			synonym source FIX
			synonym source FpML
			type TradeIdentifier: <"The trade identifier, along with the party that assigned it.">
				[synonym FpML value "partyTradeIdentifier"]
				identifyingParty string (1..1) <"The party that assigns the trade identifier">
				tradeId string (1..1) <"In FIX, the unique ID assigned to the trade entity once it is received or matched by the exchange or central counterparty.">
					[synonym FIX value "TradeID" tag 1003]
					[synonym FIX value "SecondaryTradeID" tag 1040]
		""");
	}
	
	@Test
	void testDataRuleWithChoice() {
		assertNoIssues("""
			type Party:
				foo boolean (1..1)
				bar BarEnum (0..*)
				foobar string (0..1)
				condition Foo_Bar:
					if foo
					then
						if bar any = BarEnum -> abc
							then foobar exists
						else foobar is absent
			enum BarEnum:
				abc
				bde
				cer
		""");
	}
	
	@Test
	void testAttributeWithReferenceAnchorAndScheme() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo string (1..1)
					[metadata reference]
					[metadata scheme]
					[synonym FpML value "foo" meta "href", "id", "fooScheme"]
		""");
	}
	
	@Test
	void testChoiceRule() {
		assertNoIssues("""
			type Foo:
				foo Color (1..*)
				bar string (0..*)
				condition Foo_bar:
					required choice foo, bar
			type Color:
				 blue boolean (0..1)
		""");
	}
			
	@Test
	void testAttributeWithMetadataReferenceAnnotation() {
		assertNoIssues("""
			metaType reference string
			
			type Foo:
				foo string (1..1)
					[metadata reference]
		""");
	}
	
	@Test
	void testAttributeWithMetadataIdAnnotation() {
		assertNoIssues("""
			metaType id string

			type Foo:
				foo string (1..1)
					[metadata id]
		""");
	}
	
	@Test
	void testAttributeWithMetadataSchemeAnnotation() {
		assertNoIssues("""
			metaType scheme string
			metaType reference string

			type Foo:
				foo string (1..1) 
					[metadata scheme]
			
			type Bar:
				bar string (1..1)
					[metadata scheme]
					[metadata reference]
		""");
	}
	
	@Test
	void testAttributesWithLocationAndAddress() {
		assertNoIssues("""
			metaType scheme string
			metaType reference string

			type Foo:
				foo string (1..1) 
					[metadata location]
			
			type Bar:
				bar string (1..1)
					[metadata address "pointsTo"=Foo->foo]
		""");
	}
	
	@Test
	void testSynonymsWithPathExpression() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo int (0..1)
					[synonym FpML value "foo" path "fooPath1"]
		""");
	}
	
	@Test
	void synonymsWithHint() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo int (0..1)
					[synonym FpML hint "myHint"]
		""");
	}
		
	@Test
	void testSynonymMappingSetToBoolean() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo boolean (0..1)
					[synonym FpML set to True when "FooSyn" exists]
		""");
	}
	
	@Test
	void testSynonymMappingSetToString() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo string (0..1)
					[synonym FpML set to "A" when "FooSyn" exists]
		""");
	}
	
	@Test
	void testSynonymMappingSetToEnum() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo BarEnum (0..1)
					[synonym FpML set to BarEnum -> a when "FooSyn" exists]
			
			enum BarEnum:
				a b
		""");
	}
	
	@Test
	void testSynonymMappingDefaultToEnum() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo BarEnum (0..1)
					[synonym FpML value "FooSyn" default to BarEnum -> a]
			
			enum BarEnum:
				a b
		""");
	}
	
	@Test
	void testSynonymMappingSetWhenEqualsCondition() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo boolean (0..1)
					[synonym FpML value "FooSyn" set when "path->to->string" = BarEnum -> a]
			
			enum BarEnum:
				a b
		""");
	}
	
	@Test
	void testSynonymMappingSetWhenExistsCondition() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo boolean (0..1)
				[synonym FpML value "FooSyn" set when "path->to->string" exists]
		""");
	}
	
	@Test
	void testSynonymMappingSetWhenIsAbsentCondition() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo boolean (0..1)
				[synonym FpML value "FooSyn" set when "path->to->string" is absent]
		""");
	}
	
	@Test
	void testSynonymMappingMultipleSetToWhenConditions() {
		assertNoIssues("""
			synonym source FpML
			type Foo:
				foo string (0..1)
					[synonym FpML
							set to "1" when "path->to->string" = "Foo",
							set to "2" when "path->to->enum" = BarEnum -> a,
							set to "3" when "path->to->string" is absent,
							set to "4"]

			enum BarEnum: a b
		""");
	}
	
	@Test
	void testClassSynonym() {
		assertNoIssues("""
			synonym source FpML
			
			type Foo:
				[synonym FpML value "FooSyn"]
				bar boolean (1..1)
		""");
	}
	
	@Test
	void externalSynonymWithMapperShouldParseWithNoErrors() {
		assertNoIssues("""
			type Foo:
				foo string (0..1)
			
			synonym source TEST_Base
			
			synonym source TEST extends TEST_Base {
				
				Foo:
					+ foo
						[value "bar" path "baz" mapper "BarToFooMapper"]
			}
		""");
	}
	
	@Test
	void externalSynonymWithFormatShouldParseWithNoErrors() {
		assertNoIssues("""
			type Foo:
				foo date (0..1)
			
			synonym source TEST_Base
			
			synonym source TEST extends TEST_Base {
				
				Foo:
					+ foo
						[value "bar" path "baz" dateFormat "MM/dd/yy"]
			}
		""");
	}
	
	@Test
	void externalSynonymWithPattenShouldParseWithNoErrors() {
		assertNoIssues("""
			type Foo:
				foo int (0..1)
			
			synonym source TEST_Base
			
			synonym source TEST extends TEST_Base {
				
				Foo:
					+ foo
						[value "bar" path "baz" pattern "([0-9])*.*" "$1"]
			}
		""");
	}
	
	@Test
	void externalEnumSynonymWithPattenShouldParseWithNoErrors() {
		assertNoIssues("""
			enum Foo:
				FOO
			
			synonym source TEST_Base
			
			synonym source TEST extends TEST_Base {
			enums	
				Foo:
					+ FOO
						[value "bar" pattern "([0-9])*.*" "/$1"]
			}
		""");
	}
	
	@Test
	void externalSynonymWithMetaShouldParseWithNoErrors() {
		assertNoIssues("""
			metaType scheme string
			
			type Foo:
				foo string (0..1)
				[metadata scheme]
			
			synonym source TEST_Base
			
			synonym source TEST extends TEST_Base {
				
				Foo:
					+ foo
						[value "bar" path "baz" meta "barScheme"]
			}
		""");
	}
	
	@Test
	void externalSynonymWithRemoveHtmlShouldParseWithNoErrors() {
		assertNoIssues("""
			type Foo:
				foo string (0..1)
			
			synonym source TEST_Base
			
			synonym source TEST extends TEST_Base {
				
				Foo:
					+ foo
						[value "bar" removeHtml]
			}
		""");
	}
	
	private void assertIssues(String model, String expectedIssues) {
		RosettaTestModel parsedModel = modelService.toTestModel(model, false);
		validationHelper.assertIssues(parsedModel.getModel(), expectedIssues);
	}
	
	private void assertNoIssues(String model) {
		modelService.toTestModel(model, true);
	}
}
