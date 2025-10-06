package com.regnosys.rosetta.formatting2;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class RosettaExpressionFormattingTest {

    @Inject
    private ExpressionFormatterTestHelper expressionFormatterTestHelper;

    private void arrow(CharSequence unformatted, CharSequence expectation) {
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

    private void doubleArrow(CharSequence unformatted, CharSequence expectation) {
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
    public void testSwitchFormat() {
        arrow("""
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
    public void testWithMetaOnConstructorWithInnerConstructorFormat() {
        arrow("""
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
    public void testInnerConstructorIsOnNewLine() {
        arrow("""
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
    public void testWithMetaOnLiteralConstructorFields() {
        arrow("""
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

    //TODO: fix issue where multi line expression in a with-meta on a constructor causes formatting exception
    @Disabled
    @Test
    public void testWithMetaOnConstructorFormat() {
        arrow("""
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
    public void testWithMetaFormat() {
        arrow("""
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
    public void testConstructorFormat1() {
        arrow("""
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
    public void testConstructorFormat2() {
        arrow("""
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
    public void testConstructorFormat3() {
        arrow("""
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
    public void testConstructorNestedWithBooleanFormat() {
        arrow("""
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
    public void testCollapsingBracketsDeepNested() {
        arrow("""
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
    public void testConstructorNestedInUnaryOperation() {
        arrow("""
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
    public void testOperationChainingFormat1() {
        arrow("""
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
    public void testOperationChainingFormat2() {
        arrow("""
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
    public void testShortParenthesesAreFormatted() {
        arrow("""
                (  
                  [  1,   ( 2) ,3  ] )
                """, """
                ([1, (2), 3])
                """);
    }

    @Test
    public void testLongParenthesesAreFormatted() {
        arrow("""
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
    public void testLongParenthesesOnSameLineStayOnSameLine() {
        arrow("""
                ( ["This", "is", "a", "veeeeeeeeeeeery", "looooooooooooooooooooong", "list"] )
                """, """
                (["This", "is", "a", "veeeeeeeeeeeery", "looooooooooooooooooooong", "list"])
                """);
    }

    @Test
    public void testShortExpressionWithLongCommentOnSameLine() {
        arrow("""
                [  1,   2 ,3  ] // this is a veeeerrrrrrrrrryyyyyyyyyyyy looooooooooong comment
                """, """
                [1, 2, 3] // this is a veeeerrrrrrrrrryyyyyyyyyyyy looooooooooong comment
                """);
    }

    @Test
    public void testShortListFormatting1() {
        arrow("""
                [  1,   2 ,3  ]
                """, """
                [1, 2, 3]
                """);
    }

    @Test
    public void testShortListFormatting2() {
        arrow("""
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
    public void testShortListFormatting3() {
        arrow("""
                [1  then extract  [  item] ]
                """, """
                [1 then extract [ item ]]
                """);
    }

    @Test
    public void testLongListFormatting1() {
        arrow("""
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
    public void testLongListFormatting2() {
        arrow("""
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
    public void testLongListFormatting3() {
        arrow("""
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
    public void testLongListFormatting4() {
        arrow("""
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
    public void testShortConditional1() {
        arrow("""
                if  True 
                  then 10   else  42
                """, """
                if True then 10 else 42
                """);
    }

    @Test
    public void testShortConditional2() {
        arrow("""
                if  True 
                  then 10
                """, """
                if True then 10
                """);
    }

    @Test
    public void testLongConditional1() {
        arrow("""
                if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999 then 1 else 2
                """, """
                if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999
                then 1
                else 2
                """);
    }

    @Test
    public void testLongConditional2() {
        arrow("""
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
    public void testNestedConditional() {
        arrow("""
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
    public void testFeatureCall() {
        arrow("""
                foo 
                -> bar  ->bar
                """, """
                foo -> bar -> bar
                """);
    }

    @Test
    public void testShortAsKey1() {
        arrow("""
                "bla" 
                		 as-key
                """, """
                "bla" as-key
                """);
    }

    @Test
    public void testLongAsKey1() {
        arrow("""
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
    public void testOnlyExists1() {
        arrow("""
                foo 
                -> 
                bar only  
                exists
                """, """
                foo -> bar only exists
                """);
    }

    @Test
    public void testOnlyExists2() {
        arrow("""
                ( foo -> bar, foo  ->  bar,
                foo -> 
                bar )  only  
                exists
                """, """
                (foo -> bar, foo -> bar, foo -> bar) only exists
                """);
    }

    @Test
    public void testShortFunctionCall1() {
        arrow("""
                SomeFunc 
                 ( 
                   )
                """, """
                SomeFunc()
                """);
    }

    @Test
    public void testShortFunctionCall2() {
        arrow("""
                SomeFunc 
                 ( 1 ,  [ 43 exists , False ],
                  42 )
                """, """
                SomeFunc(1, [43 exists, False], 42)
                """);
    }

    @Test
    public void testLongFunctionCall1() {
        arrow("""
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
    public void testLongFunctionCall2() {
        arrow("""
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
    public void testShortBinaryOperation1() {
        arrow("""
                1  
                 contains 
                2
                """, """
                1 contains 2
                """);
    }

    @Test
    public void testShortBinaryOperation2() {
        arrow("""
                1  all  =
                2
                """, """
                1 all = 2
                """);
    }

    @Test
    public void testShortBinaryOperation3() { // with implicit left parameter
        arrow("""
                contains
                
                  5
                """, """
                contains 5
                """);
    }

    @Test
    public void testLongBinaryOperation1() {
        arrow("""
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
    public void testLongBinaryOperation2() {
        arrow("""
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
    public void testShortUnaryOperation1() {
        arrow("""
                1
                 exists
                """, """
                1 exists
                """);
    }

    @Test
    public void testShortUnaryOperation2() {
        arrow("""
                [3, 2, 1]  
                  distinct
                  sort
                 multiple  exists
                """, """
                [3, 2, 1] distinct sort multiple exists
                """);
    }

    @Test
    public void testShortUnaryOperation3() {
        arrow("""
                [3, 2, 1]  
                  is 
                 absent
                """, """
                [3, 2, 1] is absent
                """);
    }

    @Test
    public void testShortUnaryOperation4() { // with implicit left parameter
        arrow("""
                is 
                 absent
                """, """
                is absent
                """);
    }

    @Test
    public void testShortUnaryOperation5() {
        arrow("""
                execution -> foo is absent
                """, """
                execution -> foo is absent
                """);
    }

    @Test
    public void testLongUnaryOperation1() {
        arrow("""
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
    public void testLongUnaryOperation2() { // with implicit left parameter
        arrow("""
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
    public void testShortFunctionalOperation1() {
        arrow("""
                [3, 2, 1]  
                  extract   MyFunc
                """, """
                [3, 2, 1] extract MyFunc
                """);
    }

    @Test
    public void testShortFunctionalOperation2() {
        arrow("""
                [3, 2, 1]  
                  extract  a  [  a+1]
                """, """
                [3, 2, 1] extract a [ a + 1 ]
                """);
    }

    @Test
    public void testLongFunctionalOperation1() {
        arrow("""
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
    public void testLongFunctionalOperation2() { // with implicit parameter
        arrow("""
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
    public void testLongFunctionalOperation3() {
        arrow("""
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
    public void testFunctionalOperationWithoutBrackets() {
        arrow("""
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
    public void testRuleChaining1() {
        doubleArrow("""
                OtherRule
                   then  OtherRule
                """, """
                OtherRule then OtherRule
                """);
    }

    @Test
    public void testRuleChaining2() {
        doubleArrow("""
                extract  OtherRule
                   then    extract OtherRule
                """, """
                extract OtherRule
                then extract OtherRule
                """);
    }

    @Test
    public void testShortRuleFilter1() {
        doubleArrow("""
                filter
                    True
                """, """
                filter True
                """);
    }

    @Test
    public void testShortRuleExtract1() {
        doubleArrow("""
                extract
                
                 42
                """, """
                extract 42
                """);
    }

    @Test
    public void testLongRuleExtract1() {
        doubleArrow("""
                extract
                     (["This", "is", "a", "loooooooooooooooooooooooong", "list"] 
                   count > 10)
                """, """
                extract
                	(["This", "is", "a", "loooooooooooooooooooooooong", "list"] count > 10)
                """);
    }

    @Test
    public void testFunctionCallInParenthesis() {
        arrow("""
                (SomeFunc 
                 ( 
                   ))
                """, """
                (SomeFunc())
                """);
    }

    @Test
    public void testConditionalInParenthesis() {
        arrow("""
                (if  True 
                		  then 10)
                """, """
                (if True then 10)
                """);
    }
}