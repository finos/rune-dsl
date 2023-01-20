package com.regnosys.rosetta.formatting2

import org.junit.jupiter.api.Test
import javax.inject.Inject

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaInjectorProvider

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class RosettaExpressionFormattingTest {
	@Inject
	extension ExpressionFormatterTestHelper
	
	def ->(CharSequence unformated, CharSequence expectation) {
		assertFormattedExpression[
			it.expectation = expectation
			it.toBeFormatted = unformated
			
			// extra check to make sure we didn't miss any hidden region in our formatter:
			it.allowUnformattedWhitespace = false 
			
			// see issue https://github.com/eclipse/xtext-core/issues/2058
			it.request.allowIdentityEdits = true
		
			// see issue https://github.com/eclipse/xtext-core/issues/164
			// and issue https://github.com/eclipse/xtext-core/issues/2060
			it.useSerializer = false
		]
	}
	
	def =>(CharSequence unformated, CharSequence expectation) {
		assertFormattedRuleExpression[
			it.expectation = expectation
			it.toBeFormatted = unformated
			
			// extra check to make sure we didn't miss any hidden region in our formatter:
			it.allowUnformattedWhitespace = false 
			
			// see issue https://github.com/eclipse/xtext-core/issues/2058
			it.request.allowIdentityEdits = true
		
			// see issue https://github.com/eclipse/xtext-core/issues/164
			// and issue https://github.com/eclipse/xtext-core/issues/2060
			it.useSerializer = false
		]
	}
	
	@Test
	def void testShortListFormatting1() {
		'''
		[  1,   2 ,3  ]
		''' -> '''
		[1, 2, 3]
		'''
	}
	
	@Test
	def void testShortListFormatting2() {
		'''
		[  1
		,   
		
		2 , 
		3  
		]
		''' -> '''
		[1, 2, 3]
		'''
	}
	
	@Test
	def void testShortListFormatting3() {
		'''
		[1  extract-all  [  item] ]
		''' -> '''
		[1 extract-all [ item ]]
		'''
	}
	
	@Test
	def void testLongListFormatting1() {
		'''
		["This", "is", "a", "veeeeeeeeeeeery", "looooooooooooooooooooong", "list"]
		''' -> '''
		[
			"This",
			"is",
			"a",
			"veeeeeeeeeeeery",
			"looooooooooooooooooooong",
			"list"
		]
		'''
	}
	
	@Test
	def void testLongListFormatting2() {
		'''
		[
			"This" ,
			"is", "a", "veeeeeeeeeeeery",
			"looooooooooooooooooooong",
			"list"
		]
		''' -> '''
		[
			"This",
			"is",
			"a",
			"veeeeeeeeeeeery",
			"looooooooooooooooooooong",
			"list"
		]
		'''
	}
	
	@Test
	def void testLongListFormatting3() {
		'''
		["This is a veeeeeery looooong list" extract-all [PerformComputation], 2, 3]
		''' -> '''
		[
			"This is a veeeeeery looooong list"
				extract-all [ PerformComputation ],
			2,
			3
		]
		'''
	}
	
	@Test
	def void testLongListFormatting4() {
		'''
		["This", "is", "a", "veeeeeeeeeeeery", "looooooooooooooooooooong", ["nested", "list"]]
		''' -> '''
		[
			"This",
			"is",
			"a",
			"veeeeeeeeeeeery",
			"looooooooooooooooooooong",
			["nested", "list"]
		]
		'''
	}
	
	@Test
	def void testShortConditional1() {
		'''
		if  True 
		  then 10   else  42
		''' -> '''
		if True then 10 else 42
		'''
	}
	
	@Test
	def void testShortConditional2() {
		'''
		if  True 
		  then 10
		''' -> '''
		if True then 10
		'''
	}
	
	@Test
	def void testLongConditional1() {
		'''
		if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999 then 1 else 2
		''' -> '''
		if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999
		then 1
		else 2
		'''
	}
	
	@Test
	def void testLongConditional2() {
		'''
		if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999 then 1 else if False then "foo" else "bar"
		''' -> '''
		if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999
		then 1
		else if False
		then "foo"
		else "bar"
		'''
	}
	
	@Test
	def void testFeatureCall() {
		'''
		foo 
		-> bar  ->bar
		''' -> '''
		foo -> bar -> bar
		'''
	}
	
	@Test
	def void testOnlyExists1() {
		'''
		foo 
		-> 
		bar only  
		exists
		''' -> '''
		foo -> bar only exists
		'''
	}
	
	@Test
	def void testOnlyExists2() {
		'''
		( foo -> bar, foo  ->  bar,
		foo -> 
		bar )  only  
		exists
		''' -> '''
		(foo -> bar, foo -> bar, foo -> bar) only exists
		'''
	}
	
	@Test
	def void testShortFunctionCall1() {
		'''
		SomeFunc 
		 ( 
		   )
		''' -> '''
		SomeFunc()
		'''
	}
	
	@Test
	def void testShortFunctionCall2() {
		'''
		SomeFunc 
		 ( 1 ,  [ 43 exists , False ],
		  42 )
		''' -> '''
		SomeFunc(1, [43 exists, False], 42)
		'''
	}
	
	@Test
	def void testLongFunctionCall1() {
		'''
		SomeFunc 
		 ( "This", "is", "a", "verrrrryyyyyyyyy" ,
		   "looooooooooooooooooooooong", "function", "call")
		''' -> '''
		SomeFunc(
			"This",
			"is",
			"a",
			"verrrrryyyyyyyyy",
			"looooooooooooooooooooooong",
			"function",
			"call"
		)
		'''
	}
	
	@Test
	def void testLongFunctionCall2() {
		'''
		SomeFunc(if "This is a veryyyyyyyy loooooooong expression" count > 999 then 1 else 2, "another param", "and another")
		''' -> '''
		SomeFunc(
			if "This is a veryyyyyyyy loooooooong expression" count > 999
			then 1
			else 2,
			"another param",
			"and another"
		)
		'''
	}
	
	@Test
	def void testBinaryOperation1() {
		'''
		1  
		 contains 
		2
		''' -> '''
		1 contains 2
		'''
	}
	
	@Test
	def void testBinaryOperation2() {
		'''
		1  all  =
		2
		''' -> '''
		1 all = 2
		'''
	}
	
	@Test
	def void testBinaryOperation3() { // with implicit left parameter
		'''
		contains
		
		  5
		''' -> '''
		contains 5
		'''
	}
	
	@Test
	def void testBinaryOperation4() {
		'''
		SomeFunc(if "This is a veryyyyyyyy loooooooong expression" count > 999 then 1 else 2, "another param", "and another")  all  =
		if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999 then 1 else 2
		''' -> '''
		SomeFunc(
			if "This is a veryyyyyyyy loooooooong expression" count > 999
			then 1
			else 2,
			"another param",
			"and another"
		) all = if "This is a verryyyyyyyyy loooooooooooooong expression" count > 999
		then 1
		else 2
		'''
	}
	
	@Test
	def void testShortUnaryOperation1() {
		'''
		1
		 exists
		''' -> '''
		1 exists
		'''
	}
	
	@Test
	def void testShortUnaryOperation2() {
		'''
		[3, 2, 1]  
		  distinct
		  sort
		 multiple  exists
		''' -> '''
		[3, 2, 1] distinct sort multiple exists
		'''
	}
	
	@Test
	def void testShortUnaryOperation3() {
		'''
		[3, 2, 1]  
		  is 
		 absent
		''' -> '''
		[3, 2, 1] is absent
		'''
	}
	
	@Test
	def void testShortUnaryOperation4() { // with implicit left parameter
		'''
		is 
		 absent
		''' -> '''
		is absent
		'''
	}
	
	@Test
	def void testShortUnaryOperation5() {
		'''
		execution -> foo is absent
		''' -> '''
		execution -> foo is absent
		'''
	}
	
	@Test
	def void testLongUnaryOperation1() {
		'''
		[10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0]  distinct  
		sort
		multiple  exists is  absent
		''' -> '''
		[10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0]
			distinct
			sort
			multiple exists
			is absent
		'''
	}
	
	@Test
	def void testLongUnaryOperation2() { // with implicit left parameter
		'''
		distinct  
		sort   reverse count   only-element
		multiple  exists is  absent
		  sum last
		''' -> '''
		distinct
			sort
			reverse
			count
			only-element
			multiple exists
			is absent
			sum
			last
		'''
	}
	
	@Test
	def void testShortFunctionalOperation1() {
		'''
		[3, 2, 1]  
		  extract   MyFunc
		''' -> '''
		[3, 2, 1] extract MyFunc
		'''
	}
	
	@Test
	def void testShortFunctionalOperation2() {
		'''
		[3, 2, 1]  
		  extract  a  [  a+1]
		''' -> '''
		[3, 2, 1] extract a [ a + 1 ]
		'''
	}
	
	@Test
	def void testLongFunctionalOperation1() {
		'''
		[3, 2, 1]  
		  reduce  a ,
		  b [if "This is a veryyyyyyyy loooooooong expression" count > a then b else a]
		''' -> '''
		[3, 2, 1]
			reduce a, b [
				if "This is a veryyyyyyyy loooooooong expression" count > a
				then b
				else a
			]
		'''
	}
	
	@Test
	def void testLongFunctionalOperation2() { // with implicit parameter
		'''
		reduce  a ,
		  b [if "This is a veryyyyyyyy loooooooong expression" count > a then b else a]
		   only-element
		''' -> '''
		reduce a, b [
			if "This is a veryyyyyyyy loooooooong expression" count > a
			then b
			else a
		]
			only-element
		'''
	}
	
	@Test
	def void testRuleChaining() {
		'''
		OtherRule
		   then  OtherRule
		''' => '''
		OtherRule
		then OtherRule
		'''
	}
	
	@Test
	def void testShortRuleFilter1() {
		'''
		filter
		  when   True
		''' => '''
		filter when True
		'''
	}
	
	@Test
	def void testShortRuleFilter2() {
		'''
		filter
		  when  
		  rule  OtherRule
		''' => '''
		filter when rule OtherRule
		'''
	}
	
	@Test
	def void testShortRuleFilter3() {
		'''
		filter
		  when  
		  rule  OtherRule  as"07 Original swap USI"
		''' => '''
		filter when rule OtherRule as "07 Original swap USI"
		'''
	}
	
	@Test
	def void testLongRuleFilter1() {
		'''
		filter
		  when   ["This", "is", "a", "loooooooooooooooooooooooong", "list"] count > 10
		''' => '''
		filter when
			["This", "is", "a", "loooooooooooooooooooooooong", "list"] count > 10
		'''
	}
	
	@Test
	def void testLongRuleFilter2() {
		'''
		filter
		  when   ["This", "is", "a", "loooooooooooooooooooooooong", "list"] count > 10
		 as"07 Original swap USI"
		''' => '''
		filter when
			["This", "is", "a", "loooooooooooooooooooooooong", "list"] count > 10
		as "07 Original swap USI"
		'''
	}
	
	@Test
	def void testRuleOr1() {
		'''
		( OtherRule
		,  extract  True)
		''' => '''
		(
			OtherRule,
			extract True
		)
		'''
	}
	
	@Test
	def void testShortRuleExtract1() {
		'''
		extract
		
		 42
		''' => '''
		extract 42
		'''
	}
	
	@Test
	def void testShortRuleExtract2() {
		'''
		extract  repeatable
		
		 42
		''' => '''
		extract repeatable 42
		'''
	}
	
	@Test
	def void testLongRuleExtract1() {
		'''
		extract
		     ["This", "is", "a", "loooooooooooooooooooooooong", "list"] 
		   count > 10
		''' => '''
		extract
			["This", "is", "a", "loooooooooooooooooooooooong", "list"] count > 10
		'''
	}
	
	@Test
	def void testLongRuleExtract2() {
		'''
		extract   repeatable
		     ["This", "is", "a", "loooooooooooooooooooooooong", "list"] 
		   count > 10
		''' => '''
		extract repeatable
			["This", "is", "a", "loooooooooooooooooooooooong", "list"] count > 10
		'''
	}
	
	@Test
	def void testShortRuleReturn1() {
		'''
		return
		
		 42
		''' => '''
		return 42
		'''
	}
	
	@Test
	def void testLongRuleReturn1() {
		'''
		return
		     ["This", "is", "a", "loooooooooooooooooooooooooong", "list"] 
		   count > 10
		''' => '''
		return
			["This", "is", "a", "loooooooooooooooooooooooooong", "list"] count > 10
		'''
	}
	
	@Test
	def void testRuleLookup1() {
		'''
		lookup  foo  Foo
		''' => '''
		lookup foo Foo
		'''
	}
}