package com.regnosys.rosetta.formatting2;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaExpressionFormattingTest {

    @Inject
    private ExpressionFormatterTestHelper expressionFormatterTestHelper;

    private void formatAndAssertExpression(CharSequence unformatted, CharSequence expectation) {
        expressionFormatterTestHelper.assertFormattedExpression(cfg -> {
            cfg.setExpectation(expectation);
            cfg.setToBeFormatted(unformatted);

            // extra check to make sure we didn't miss any hidden region in our formatter:
            cfg.setAllowUnformattedWhitespace(false);

            // see issue https://github.com/eclipse/xtext-core/issues/2058
            cfg.getRequest().setAllowIdentityEdits(true);

            // see issue https://github.com/eclipse/xtext-core/issues/164
            // and issue https://github.com/eclipse/xtext-core/issues/2060
            cfg.setUseSerializer(false);
        });
    }

    private void formatAndAssertRuleExpression(CharSequence unformatted, CharSequence expectation) {
        expressionFormatterTestHelper.assertFormattedRuleExpression(cfg -> {
            cfg.setExpectation(expectation);
            cfg.setToBeFormatted(unformatted);

            // extra check to make sure we didn't miss any hidden region in our formatter:
            cfg.setAllowUnformattedWhitespace(false);

            // see issue https://github.com/eclipse/xtext-core/issues/2058
            cfg.getRequest().setAllowIdentityEdits(true);

            // see issue https://github.com/eclipse/xtext-core/issues/164
            // and issue https://github.com/eclipse/xtext-core/issues/2060
            cfg.setUseSerializer(false);
        });
    }

    @Test
    void testSwitchFormat() {
        formatAndAssertExpression("""
                input
                switch   Foo then "Some expression",
                	Bar then   extract
                	if True
                then "This is a looong expression"
                	else "other", default empty
                """, """
                input switch
                	Foo then "Some expression",
                	Bar then
                		extract
                			if True
                			then "This is a looong expression"
                			else "other",
                	default empty
                """);
    }

    @Test
    void testWithMetaOnConstructorWithInnerConstructorFormat() {
        formatAndAssertExpression("""
                Bar {
                    someBarField: "blah" with-meta {
                            scheme: "someScheme"
                        },
                    someFooField: "foo",
                    innerBar:  Bar {
                                    someBarField: "blah" with-meta {
                                        scheme: "someScheme"
                                    },
                                    someFooField: "foo",
                                    ...
                                } 
                                with-meta {
                                    key: inKey
                                },
                    innerBar2: MyFunc2() with-meta {key: inKey } 
                } 
                with-meta {
                    key: inKey
                }
                """, """
                Bar {
                	someBarField: "blah" with-meta {
                			scheme: "someScheme"
                		},
                	someFooField: "foo",
                	innerBar:
                		Bar {
                			someBarField: "blah" with-meta {
                					scheme: "someScheme"
                				},
                			someFooField: "foo",
                			...
                		} with-meta {
                			key: inKey
                		},
                	innerBar2: MyFunc2() with-meta {
                			key: inKey
                }} with-meta {
                	key: inKey
                }
                """);
    }

    @Test
    void testInnerConstructorIsOnNewLine() {
        formatAndAssertExpression("""
                Bar {
                	innerBar: Bar {
                			someBarField: "blah"
                		}
                	}
                """, """
                Bar {
                	innerBar:
                		Bar {
                			someBarField: "blah"
                }}
                """);
    }

    @Test
    void testWithMetaOnLiteralConstructorFields() {
        formatAndAssertExpression("""
                Bar {
                    someBarField: "blah" with-meta {
                    scheme: "someScheme"
                    },
                    someFooField: "blah" with-meta {
                    scheme: "someScheme"
                    },
                    ...
                }
                """, """
                Bar {
                	someBarField: "blah" with-meta {
                			scheme: "someScheme"
                		},
                	someFooField: "blah" with-meta {
                			scheme: "someScheme"
                		},
                	...
                }
                """);
    }

    @Test
    void testWithMetaOnConstructorFormat() {
        formatAndAssertExpression("""
                SomeType {
                	attr1: "Some expression"
                }
                with-meta   {
                	scheme: "Some expression",
                	id: foo extract
                	if True
                	then "This is a looong expression"
                	else "other"
                }
                """, """
                SomeType {
                	attr1: "Some expression"
                } with-meta {
                	scheme: "Some expression",
                	id: foo
                			extract
                				if True
                				then "This is a looong expression"
                				else "other"
                }
                """);
    }

    @Test
    void testWithMetaFormat() {
        formatAndAssertExpression("""
                input
                with-meta   {
                	scheme: "Some expression",
                	id: foo extract
                	if True
                	then "This is a looong expression"
                	else "other"
                }
                """, """
                input with-meta {
                	scheme: "Some expression",
                	id: foo
                			extract
                				if True
                				then "This is a looong expression"
                				else "other"
                }
                """);
    }

    @Test
    void testConstructorFormat1() {
        formatAndAssertExpression("""
                SomeType {
                	attr1: "Some expression",
                	attr2: foo extract
                		if True
                		then ["This is a looong", "expression"]
                		else 42,
                }
                """, """
                SomeType {
                	attr1: "Some expression",
                	attr2: foo
                			extract
                				if True
                				then ["This is a looong", "expression"]
                				else 42,
                }
                """);
    }

    @Test
    void testConstructorFormat2() {
        formatAndAssertExpression("""
                SomeType {
                	attr1: "Some expression",
                	attr2: foo extract
                		if True
                		then ["This is a looong", "expression"]
                		else 42,
                	...
                }
                """, """
                SomeType {
                	attr1: "Some expression",
                	attr2: foo
                			extract
                				if True
                				then ["This is a looong", "expression"]
                				else 42,
                	...
                }
                """);
    }

    @Test
    void testConstructorFormat3() {
        formatAndAssertExpression("""
                SomeType {
                	attr1: "Some expression",
                	attr2: Foo {
                		bar: True
                	},
                }
                """, """
                SomeType {
                	attr1: "Some expression",
                	attr2:
                		Foo {
                			bar: True
                },}
                """);
    }

    @Test
    void testConstructorNestedWithBooleanFormat() {
        formatAndAssertExpression("""
                Constr1 {
                attr1: if True
                then False,
                attr2: if False
                then Constr2 {
                attr11: Constr3 {
                attr111: 42
                }}
                }
                """, """
                Constr1 {
                	attr1: if True then False,
                	attr2: if False
                			then Constr2 {
                				attr11:
                					Constr3 {
                						attr111: 42
                }}}
                """);
    }

    @Test
    void testCollapsingBracketsDeepNested() {
        formatAndAssertExpression("""
                Constr1 {
                			attr1: if True then False,
                			attr2: if False
                				then 42 extract Constr2 {
                					attr11: Constr3 {
                						attr111: item
                	}}}
                """, """
                Constr1 {
                	attr1: if True then False,
                	attr2: if False
                			then 42
                			extract
                				Constr2 {
                					attr11:
                						Constr3 {
                							attr111: item
                }}}
                """);
    }

    @Test
    void testConstructorNestedInUnaryOperation() {
        formatAndAssertExpression("""
                el1
                extract
                	Constr1 {
                    	attr1: val1
                	}
                """, """
                el1
                	extract
                		Constr1 {
                			attr1: val1
                		}
                """);
    }

    @Test
    void testOperationChainingFormat1() {
        formatAndAssertExpression("""
                input
                	extract [
                		item -> bar
                			filter "this is a loooooooooooong expression" count > 2
                	]
                	then extract
                		if True
                		then ["This is a looong", "expression"]
                		else 42
                """, """
                input
                	extract [
                		item -> bar
                			filter "this is a loooooooooooong expression" count > 2
                	]
                	then extract
                		if True
                		then ["This is a looong", "expression"]
                		else 42
                """);
    }

    @Test
    void testOperationChainingFormat2() {
        formatAndAssertExpression("""
                input
                	extract [
                		item -> bar
                			filter "this is a loooooooooooong expression" count > 2
                	]
                	then
                		if True
                		then ["This is a looong", "expression"]
                		else 42
                """, """
                input
                	extract [
                		item -> bar
                			filter "this is a loooooooooooong expression" count > 2
                	]
                	then if True
                		then ["This is a looong", "expression"]
                		else 42
                """);
    }

    @Test
    void testShortParenthesesAreFormatted() {
        formatAndAssertExpression("""
                (  
                  [  1,   ( 2) ,3  ] )
                """, """
                ([1, (2), 3])
                """);
    }

    @Test
    void testLongParenthesesAreFormatted() {
        formatAndAssertExpression("""
                (  
                  [["This"], "is", "a", "veeeeeeeeeeeery", "looooooooooooooooooooong", "list"] )
                """, """
                ([
                	["This"],
                	"is",
                	"a",
                	"veeeeeeeeeeeery",
                	"looooooooooooooooooooong",
                	"list"
                ])
                """);
    }

    @Test
    void testLongParenthesesOnSameLineStayOnSameLine() {
        formatAndAssertExpression("""
                ( ["This", "is", "a", "veeeeeeeeeeeery", "looooooooooooooooooooong", "list"] )
                """, """
                (["This", "is", "a", "veeeeeeeeeeeery", "looooooooooooooooooooong", "list"])
                """);
    }

    @Test
    void testShortExpressionWithLongCommentOnSameLine() {
        formatAndAssertExpression("""
                [  1,   2 ,3  ] // this is a veeeerrrrrrrrrryyyyyyyyyyyy looooooooooong comment
                """, """
                [1, 2, 3] // this is a veeeerrrrrrrrrryyyyyyyyyyyy looooooooooong comment
                """);
    }

    @Test
    void testShortListFormatting1() {
        formatAndAssertExpression("""
                [  1,   2 ,3  ]
                """, """
                [1, 2, 3]
                """);
    }

    @Test
    void testShortListFormatting2() {
        formatAndAssertExpression("""
                [  1
                ,   
                
                2 , 
                3  
                ]
                """, """
                [1, 2, 3]
                """);
    }

    @Test
    void testShortListFormatting3() {
        formatAndAssertExpression("""
                [1  then extract  [  item] ]
                """, """
                [1 then extract [ item ]]
                """);
    }

    @Test
    void testLongListFormatting1() {
        formatAndAssertExpression("""
                [["This"], "is", "a", "veeeeeeeeeeeery", "looooooooooooooooooooong", "list"]
                """, """
                [
                	["This"],
                	"is",
                	"a",
                	"veeeeeeeeeeeery",
                	"looooooooooooooooooooong",
                	"list"
                ]
                """);
    }

    @Test
    void testLongListFormatting2() {
        formatAndAssertExpression("""
                [
                	["This"] ,
                	"is", "a", "veeeeeeeeeeeery",
                	"looooooooooooooooooooong",
                	"list"
                ]
                """, """
                [
                	["This"],
                	"is",
                	"a",
                	"veeeeeeeeeeeery",
                	"looooooooooooooooooooong",
                	"list"
                ]
                """);
    }

    @Test
    void testLongListFormatting3() {
        formatAndAssertExpression("""
                ["This is a veeeeeeeeery loooooooong list" then extract [PerformComputation], 2, 3]
                """, """
                [
                	"This is a veeeeeeeeery loooooooong list"
                		then extract [ PerformComputation ],
                	2,
                	3
                ]
                """);
    }

    @Test
    void testLongListFormatting4() {
        formatAndAssertExpression("""
                ["This", "is", "a", "veeeeeeeeeeeery", "looooooooooooooooooooong", ["nested", "list"]]
                """, """
                [
                	"This",
                	"is",
                	"a",
                	"veeeeeeeeeeeery",
                	"looooooooooooooooooooong",
                	["nested", "list"]
                ]
                """);
    }

    @Test
    void testShortConditional1() {
        formatAndAssertExpression("""
                if  True 
                  then 10   else  42
                """, """
                if True then 10 else 42
                """);
    }

    @Test
    void testShortConditional2() {
        formatAndAssertExpression("""
                if  True 
                  then 10
                """, """
                if True then 10
                """);
    }

    @Test
    void testLongConditional1() {
        formatAndAssertExpression("""
                if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999 then 1 else 2
                """, """
                if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999
                then 1
                else 2
                """);
    }

    @Test
    void testLongConditional2() {
        formatAndAssertExpression("""
                if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999 then 1 else if False then "foo" else "bar"
                """, """
                if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999
                then 1
                else if False
                then "foo"
                else "bar"
                """);
    }

    @Test
    void testNestedConditional() {
        formatAndAssertExpression("""
                if True then if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999 then "foo" else "bar" else if False then "foo" else "bar"
                """, """
                if True
                then if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999
                	then "foo"
                	else "bar"
                else if False
                then "foo"
                else "bar"
                """);
    }

    @Test
    void testFeatureCall() {
        formatAndAssertExpression("""
                foo 
                -> bar  ->bar
                """, """
                foo -> bar -> bar
                """);
    }

    @Test
    void testShortAsKey1() {
        formatAndAssertExpression("""
                "bla" 
                		 as-key
                """, """
                "bla" as-key
                """);
    }

    @Test
    void testLongAsKey1() {
        formatAndAssertExpression("""
                if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999 then 1 else if False then "foo" else "bar"
                 as-key
                """, """
                if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999
                then 1
                else if False
                then "foo"
                else "bar"
                	as-key
                """);
    }

    @Test
    void testOnlyExists1() {
        formatAndAssertExpression("""
                foo 
                -> 
                bar only  
                exists
                """, """
                foo -> bar only exists
                """);
    }

    @Test
    void testOnlyExists2() {
        formatAndAssertExpression("""
                ( foo -> bar, foo  ->  bar,
                foo -> 
                bar )  only  
                exists
                """, """
                (foo -> bar, foo -> bar, foo -> bar) only exists
                """);
    }

    @Test
    void testShortFunctionCall1() {
        formatAndAssertExpression("""
                SomeFunc 
                 ( 
                   )
                """, """
                SomeFunc()
                """);
    }

    @Test
    void testShortFunctionCall2() {
        formatAndAssertExpression("""
                SomeFunc 
                 ( 1 ,  [ 43 exists , False ],
                  42 )
                """, """
                SomeFunc(1, [43 exists, False], 42)
                """);
    }

    @Test
    void testLongFunctionCall1() {
        formatAndAssertExpression("""
                SomeFunc 
                 ( "This", "is", "a", "verrrrryyyyyyyyy" ,
                   "looooooooooooooooooooooong", "function", "call")
                """, """
                SomeFunc(
                		"This",
                		"is",
                		"a",
                		"verrrrryyyyyyyyy",
                		"looooooooooooooooooooooong",
                		"function",
                		"call"
                	)
                """);
    }

    @Test
    void testLongFunctionCall2() {
        formatAndAssertExpression("""
                SomeFunc(if "This is a veryyyyyyyy loooooooong expression" count > 999 then 1 else 2, "another param", "and another")
                """, """
                SomeFunc(
                		if "This is a veryyyyyyyy loooooooong expression" count > 999
                		then 1
                		else 2,
                		"another param",
                		"and another"
                	)
                """);
    }

    @Test
    void testShortBinaryOperation1() {
        formatAndAssertExpression("""
                1  
                 contains 
                2
                """, """
                1 contains 2
                """);
    }

    @Test
    void testShortBinaryOperation2() {
        formatAndAssertExpression("""
                1  all  =
                2
                """, """
                1 all = 2
                """);
    }

    @Test
    void testShortBinaryOperation3() { // with implicit left parameter
        formatAndAssertExpression("""
                contains
                
                  5
                """, """
                contains 5
                """);
    }

    @Test
    void testLongBinaryOperation1() {
        formatAndAssertExpression("""
                SomeFunc(if "This is a veryyyyyyyy loooooooong expression" count > 999 then 1 else 2, "another param", "and another")  all  =
                if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999 then 1 else 2
                """, """
                SomeFunc(
                		if "This is a veryyyyyyyy loooooooong expression" count > 999
                		then 1
                		else 2,
                		"another param",
                		"and another"
                	)
                	all = if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999
                		then 1
                		else 2

                """);
    }

    @Test
    void testLongBinaryOperation2() {
        formatAndAssertExpression("""
                adjustedDate exists
                	or relativeDate exists
                	or unadjustedDate exists
                	or (unadjustedDate exists and dateAdjustments exists and adjustedDate is absent)
                """, """
                adjustedDate exists
                	or relativeDate exists
                	or unadjustedDate exists
                	or (unadjustedDate exists and dateAdjustments exists and adjustedDate is absent)
                """);
    }

    @Test
    void testShortUnaryOperation1() {
        formatAndAssertExpression("""
                1
                 exists
                """, """
                1 exists
                """);
    }

    @Test
    void testShortUnaryOperation2() {
        formatAndAssertExpression("""
                [3, 2, 1]  
                  distinct
                  sort
                 multiple  exists
                """, """
                [3, 2, 1] distinct sort multiple exists
                """);
    }

    @Test
    void testShortUnaryOperation3() {
        formatAndAssertExpression("""
                [3, 2, 1]  
                  is 
                 absent
                """, """
                [3, 2, 1] is absent
                """);
    }

    @Test
    void testShortUnaryOperation4() { // with implicit left parameter
        formatAndAssertExpression("""
                is 
                 absent
                """, """
                is absent
                """);
    }

    @Test
    void testShortUnaryOperation5() {
        formatAndAssertExpression("""
                execution -> foo is absent
                """, """
                execution -> foo is absent
                """);
    }

    @Test
    void testLongUnaryOperation1() {
        formatAndAssertExpression("""
                [10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0]  distinct  
                sort
                multiple  exists is  absent
                """, """
                [10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0]
                	distinct
                	sort
                	multiple exists
                	is absent
                """);
    }

    @Test
    void testLongUnaryOperation2() { // with implicit left parameter
        formatAndAssertExpression("""
                distinct  
                sort   reverse count   only-element
                multiple  exists is  absent
                  sum last
                """, """
                distinct
                sort
                reverse
                count
                only-element
                multiple exists
                is absent
                sum
                last
                """);
    }

    @Test
    void testShortFunctionalOperation1() {
        formatAndAssertExpression("""
                [3, 2, 1]  
                  extract   MyFunc
                """, """
                [3, 2, 1] extract MyFunc
                """);
    }

    @Test
    void testShortFunctionalOperation2() {
        formatAndAssertExpression("""
                [3, 2, 1]  
                  extract  a  [  a+1]
                """, """
                [3, 2, 1] extract a [ a + 1 ]
                """);
    }

    @Test
    void testLongFunctionalOperation1() {
        formatAndAssertExpression("""
                [3, 2, 1]  
                  reduce  a ,
                  b [if "This is a veryyyyyyyy loooooooong expression" count > a then b else a]
                """, """
                [3, 2, 1]
                	reduce a, b [
                		if "This is a veryyyyyyyy loooooooong expression" count > a
                		then b
                		else a
                	]
                """);
    }

    @Test
    void testLongFunctionalOperation2() { // with implicit parameter
        formatAndAssertExpression("""
                reduce  a ,
                  b [if "This is a veryyyyyyyy loooooooong expression" count > a then b else a]
                   only-element
                """, """
                reduce a, b [
                	if "This is a veryyyyyyyy loooooooong expression" count > a
                	then b
                	else a
                ]
                only-element
                """);
    }

    @Test
    void testLongFunctionalOperation3() {
        formatAndAssertExpression("""
                FilterQuantity( quantity1, unitOfAmount )
                	extract q1 [
                		FilterQuantity( quantity2, unitOfAmount )
                			extract q2 [ CompareNumbers( q1 -> value, op, q2 -> value ) ]
                		] flatten
                		all = True
                """, """
                FilterQuantity(quantity1, unitOfAmount)
                	extract q1 [
                		FilterQuantity(quantity2, unitOfAmount)
                			extract q2 [ CompareNumbers(q1 -> value, op, q2 -> value) ]
                	]
                	flatten all = True
                """);
    }

    @Test
    void testFunctionalOperationWithoutBrackets() {
        formatAndAssertExpression("""
                FilterQuantity( quantity1, unitOfAmount )
                	extract FilterQuantity( quantity2, unitOfAmount )
                		extract q2 [ CompareNumbers( value, op, q2 -> value ) ]
                		flatten
                	then filter item = False
                """, """
                FilterQuantity(quantity1, unitOfAmount)
                	extract
                		FilterQuantity(quantity2, unitOfAmount)
                			extract q2 [ CompareNumbers(value, op, q2 -> value) ]
                			flatten
                	then filter item = False
                """);
    }

    @Test
    void testRuleChaining1() {
        formatAndAssertRuleExpression("""
                OtherRule
                   then  OtherRule
                """, """
                OtherRule then OtherRule
                """);
    }

    @Test
    void testRuleChaining2() {
        formatAndAssertRuleExpression("""
                extract  OtherRule
                   then    extract OtherRule
                """, """
                extract OtherRule
                then extract OtherRule
                """);
    }

    @Test
    void testShortRuleFilter1() {
        formatAndAssertRuleExpression("""
                filter
                    True
                """, """
                filter True
                """);
    }

    @Test
    void testShortRuleExtract1() {
        formatAndAssertRuleExpression("""
                extract
                
                 42
                """, """
                extract 42
                """);
    }

    @Test
    void testLongRuleExtract1() {
        formatAndAssertRuleExpression("""
                extract
                     (["This", "is", "a", "loooooooooooooooooooooooong", "list"] 
                   count > 10)
                """, """
                extract
                	(["This", "is", "a", "loooooooooooooooooooooooong", "list"] count > 10)
                """);
    }

    @Test
    void testFunctionCallInParenthesis() {
        formatAndAssertExpression("""
                (SomeFunc 
                 ( 
                   ))
                """, """
                (SomeFunc())
                """);
    }

    @Test
    void testConditionalInParenthesis() {
        formatAndAssertExpression("""
                (if  True 
                		  then 10)
                """, """
                (if True then 10)
                """);
    }
}