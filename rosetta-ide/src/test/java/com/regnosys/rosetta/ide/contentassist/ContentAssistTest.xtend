package com.regnosys.rosetta.ide.contentassist

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest
import org.junit.jupiter.api.Test

// TODO: fix completion
class ContentAssistTest extends AbstractRosettaLanguageServerTest {
	@Test
	def testInheritedAttributesCondition() {
		val model = '''
		namespace "test"
		version "test"
		type SuperSuperQuote:
			superSuperAttr SuperQuote (0..1)
		
		type SuperQuote extends SuperSuperQuote:
			superAttr SuperQuote (0..1)
		
			condition SomeRule:
				if Quote -> 
		
		
		type Quote extends SuperQuote:
			attr Quote (1..1)
		'''
		testCompletion[
			it.model = model
			it.line = 9
			it.column = 14
			// TODO: this is broken.
			it.expectedCompletionItems = '''
			all -> all [[9, 14] .. [9, 14]]
			and -> and [[9, 14] .. [9, 14]]
			any -> any [[9, 14] .. [9, 14]]
			contains -> contains [[9, 14] .. [9, 14]]
			count -> count [[9, 14] .. [9, 14]]
			default -> default [[9, 14] .. [9, 14]]
			disjoint -> disjoint [[9, 14] .. [9, 14]]
			distinct -> distinct [[9, 14] .. [9, 14]]
			exists -> exists [[9, 14] .. [9, 14]]
			extract -> extract [[9, 14] .. [9, 14]]
			filter -> filter [[9, 14] .. [9, 14]]
			first -> first [[9, 14] .. [9, 14]]
			flatten -> flatten [[9, 14] .. [9, 14]]
			is -> is [[9, 14] .. [9, 14]]
			join -> join [[9, 14] .. [9, 14]]
			last -> last [[9, 14] .. [9, 14]]
			max -> max [[9, 14] .. [9, 14]]
			min -> min [[9, 14] .. [9, 14]]
			multiple -> multiple [[9, 14] .. [9, 14]]
			one-of -> one-of [[9, 14] .. [9, 14]]
			only-element -> only-element [[9, 14] .. [9, 14]]
			optional -> optional [[9, 14] .. [9, 14]]
			or -> or [[9, 14] .. [9, 14]]
			reduce -> reduce [[9, 14] .. [9, 14]]
			required -> required [[9, 14] .. [9, 14]]
			reverse -> reverse [[9, 14] .. [9, 14]]
			single -> single [[9, 14] .. [9, 14]]
			sort -> sort [[9, 14] .. [9, 14]]
			sum -> sum [[9, 14] .. [9, 14]]
			switch -> switch [[9, 14] .. [9, 14]]
			then -> then [[9, 14] .. [9, 14]]
			to-date -> to-date [[9, 14] .. [9, 14]]
			to-date-time -> to-date-time [[9, 14] .. [9, 14]]
			to-enum -> to-enum [[9, 14] .. [9, 14]]
			to-int -> to-int [[9, 14] .. [9, 14]]
			to-number -> to-number [[9, 14] .. [9, 14]]
			to-string -> to-string [[9, 14] .. [9, 14]]
			to-time -> to-time [[9, 14] .. [9, 14]]
			to-zoned-date-time -> to-zoned-date-time [[9, 14] .. [9, 14]]
			* -> * [[9, 14] .. [9, 14]]
			+ -> + [[9, 14] .. [9, 14]]
			- -> - [[9, 14] .. [9, 14]]
			-> -> -> [[9, 14] .. [9, 14]]
			->> -> ->> [[9, 14] .. [9, 14]]
			/ -> / [[9, 14] .. [9, 14]]
			< -> < [[9, 14] .. [9, 14]]
			<= -> <= [[9, 14] .. [9, 14]]
			<> -> <> [[9, 14] .. [9, 14]]
			= -> = [[9, 14] .. [9, 14]]
			> -> > [[9, 14] .. [9, 14]]
			>= -> >= [[9, 14] .. [9, 14]]
			'''
		]
	}
	
	// TODO: debug null pointer exception in log
	@Test
	def void testConditionAfterArrow() {
		val model = '''
			namespace "test"
			version "test"
			
			type Test: otherAttr Other (1..1)
			
			condition CreditDefaultSwap_LongForm:
				if Test -> otherAttr -> testAttr -> otherAttr -> testAttr
				then Test -> otherAttr -> 
				
			type Other: testAttr Test (1..1)
		'''
		testCompletion[
			it.model = model
			it.line = 7
			it.column = 27
			// TODO: this is broken
			it.expectedCompletionItems = '''
			all -> all [[7, 27] .. [7, 27]]
			and -> and [[7, 27] .. [7, 27]]
			annotation -> annotation [[7, 27] .. [7, 27]]
			any -> any [[7, 27] .. [7, 27]]
			basicType -> basicType [[7, 27] .. [7, 27]]
			body -> body [[7, 27] .. [7, 27]]
			calculationType -> calculationType [[7, 27] .. [7, 27]]
			choice -> choice [[7, 27] .. [7, 27]]
			condition -> condition [[7, 27] .. [7, 27]]
			contains -> contains [[7, 27] .. [7, 27]]
			corpus -> corpus [[7, 27] .. [7, 27]]
			count -> count [[7, 27] .. [7, 27]]
			default -> default [[7, 27] .. [7, 27]]
			disjoint -> disjoint [[7, 27] .. [7, 27]]
			distinct -> distinct [[7, 27] .. [7, 27]]
			eligibility -> eligibility [[7, 27] .. [7, 27]]
			else -> else [[7, 27] .. [7, 27]]
			enum -> enum [[7, 27] .. [7, 27]]
			exists -> exists [[7, 27] .. [7, 27]]
			extract -> extract [[7, 27] .. [7, 27]]
			filter -> filter [[7, 27] .. [7, 27]]
			first -> first [[7, 27] .. [7, 27]]
			flatten -> flatten [[7, 27] .. [7, 27]]
			func -> func [[7, 27] .. [7, 27]]
			is -> is [[7, 27] .. [7, 27]]
			join -> join [[7, 27] .. [7, 27]]
			last -> last [[7, 27] .. [7, 27]]
			library -> library [[7, 27] .. [7, 27]]
			max -> max [[7, 27] .. [7, 27]]
			metaType -> metaType [[7, 27] .. [7, 27]]
			min -> min [[7, 27] .. [7, 27]]
			multiple -> multiple [[7, 27] .. [7, 27]]
			one-of -> one-of [[7, 27] .. [7, 27]]
			only-element -> only-element [[7, 27] .. [7, 27]]
			optional -> optional [[7, 27] .. [7, 27]]
			or -> or [[7, 27] .. [7, 27]]
			qualifiedType -> qualifiedType [[7, 27] .. [7, 27]]
			recordType -> recordType [[7, 27] .. [7, 27]]
			reduce -> reduce [[7, 27] .. [7, 27]]
			report -> report [[7, 27] .. [7, 27]]
			reporting -> reporting [[7, 27] .. [7, 27]]
			required -> required [[7, 27] .. [7, 27]]
			reverse -> reverse [[7, 27] .. [7, 27]]
			rule -> rule [[7, 27] .. [7, 27]]
			segment -> segment [[7, 27] .. [7, 27]]
			single -> single [[7, 27] .. [7, 27]]
			sort -> sort [[7, 27] .. [7, 27]]
			sum -> sum [[7, 27] .. [7, 27]]
			switch -> switch [[7, 27] .. [7, 27]]
			synonym -> synonym [[7, 27] .. [7, 27]]
			then -> then [[7, 27] .. [7, 27]]
			to-date -> to-date [[7, 27] .. [7, 27]]
			to-date-time -> to-date-time [[7, 27] .. [7, 27]]
			to-enum -> to-enum [[7, 27] .. [7, 27]]
			to-int -> to-int [[7, 27] .. [7, 27]]
			to-number -> to-number [[7, 27] .. [7, 27]]
			to-string -> to-string [[7, 27] .. [7, 27]]
			to-time -> to-time [[7, 27] .. [7, 27]]
			to-zoned-date-time -> to-zoned-date-time [[7, 27] .. [7, 27]]
			type -> type [[7, 27] .. [7, 27]]
			typeAlias -> typeAlias [[7, 27] .. [7, 27]]
			* -> * [[7, 27] .. [7, 27]]
			+ -> + [[7, 27] .. [7, 27]]
			- -> - [[7, 27] .. [7, 27]]
			-> -> -> [[7, 27] .. [7, 27]]
			->> -> ->> [[7, 27] .. [7, 27]]
			/ -> / [[7, 27] .. [7, 27]]
			< -> < [[7, 27] .. [7, 27]]
			<= -> <= [[7, 27] .. [7, 27]]
			<> -> <> [[7, 27] .. [7, 27]]
			= -> = [[7, 27] .. [7, 27]]
			> -> > [[7, 27] .. [7, 27]]
			>= -> >= [[7, 27] .. [7, 27]]
			'''
		]
	}
	
	@Test
	def void testConditionAfterArrow2() {
		val model = '''
			namespace "test"
			version "test"
			
			type Test: otherAttr Other (1..1)
			
			condition CreditDefaultSwap_LongForm:
				if Test -> otherAttr -> 
				
			type Other: testAttr Test (1..1)
		'''
		testCompletion[
			it.model = model
			it.line = 6
			it.column = 25
			// TODO: this is broken
			it.expectedCompletionItems = '''
			all -> all [[6, 25] .. [6, 25]]
			and -> and [[6, 25] .. [6, 25]]
			any -> any [[6, 25] .. [6, 25]]
			contains -> contains [[6, 25] .. [6, 25]]
			count -> count [[6, 25] .. [6, 25]]
			default -> default [[6, 25] .. [6, 25]]
			disjoint -> disjoint [[6, 25] .. [6, 25]]
			distinct -> distinct [[6, 25] .. [6, 25]]
			exists -> exists [[6, 25] .. [6, 25]]
			extract -> extract [[6, 25] .. [6, 25]]
			filter -> filter [[6, 25] .. [6, 25]]
			first -> first [[6, 25] .. [6, 25]]
			flatten -> flatten [[6, 25] .. [6, 25]]
			is -> is [[6, 25] .. [6, 25]]
			join -> join [[6, 25] .. [6, 25]]
			last -> last [[6, 25] .. [6, 25]]
			max -> max [[6, 25] .. [6, 25]]
			min -> min [[6, 25] .. [6, 25]]
			multiple -> multiple [[6, 25] .. [6, 25]]
			one-of -> one-of [[6, 25] .. [6, 25]]
			only-element -> only-element [[6, 25] .. [6, 25]]
			optional -> optional [[6, 25] .. [6, 25]]
			or -> or [[6, 25] .. [6, 25]]
			reduce -> reduce [[6, 25] .. [6, 25]]
			required -> required [[6, 25] .. [6, 25]]
			reverse -> reverse [[6, 25] .. [6, 25]]
			single -> single [[6, 25] .. [6, 25]]
			sort -> sort [[6, 25] .. [6, 25]]
			sum -> sum [[6, 25] .. [6, 25]]
			switch -> switch [[6, 25] .. [6, 25]]
			then -> then [[6, 25] .. [6, 25]]
			to-date -> to-date [[6, 25] .. [6, 25]]
			to-date-time -> to-date-time [[6, 25] .. [6, 25]]
			to-enum -> to-enum [[6, 25] .. [6, 25]]
			to-int -> to-int [[6, 25] .. [6, 25]]
			to-number -> to-number [[6, 25] .. [6, 25]]
			to-string -> to-string [[6, 25] .. [6, 25]]
			to-time -> to-time [[6, 25] .. [6, 25]]
			to-zoned-date-time -> to-zoned-date-time [[6, 25] .. [6, 25]]
			* -> * [[6, 25] .. [6, 25]]
			+ -> + [[6, 25] .. [6, 25]]
			- -> - [[6, 25] .. [6, 25]]
			-> -> -> [[6, 25] .. [6, 25]]
			->> -> ->> [[6, 25] .. [6, 25]]
			/ -> / [[6, 25] .. [6, 25]]
			< -> < [[6, 25] .. [6, 25]]
			<= -> <= [[6, 25] .. [6, 25]]
			<> -> <> [[6, 25] .. [6, 25]]
			= -> = [[6, 25] .. [6, 25]]
			> -> > [[6, 25] .. [6, 25]]
			>= -> >= [[6, 25] .. [6, 25]]
			'''
		]
	}
	
	@Test
	def void testSynonymSource() {
		val model = '''
			namespace "test"
			
			synonym source FIX
			synonym source FpML
			
			type Foo:
				[synonym ]
		'''
		testCompletion[
			it.model = model
			it.line = 6
			it.column = 10
			// TODO: should only have the first two?
			it.expectedCompletionItems = '''
			FIX (RosettaSynonymSource) -> FIX [[6, 10] .. [6, 10]]
			FpML (RosettaSynonymSource) -> FpML [[6, 10] .. [6, 10]]
			test.FIX (RosettaSynonymSource) -> test.FIX [[6, 10] .. [6, 10]]
			test.FpML (RosettaSynonymSource) -> test.FpML [[6, 10] .. [6, 10]]
			'''
		]
	}
	
	@Test
	def void testSynonymSetToEnum() {
		val model = '''
			namespace "test"
			version "test"
			
			type Foo:
				action ActionEnum (1..1)
					[synonym FpML set to ActionEnum -> ]
			
			enum ActionEnum: new correct cancel
		'''
		testCompletion[
			it.model = model
			it.line = 5
			it.column = 37
			it.expectedCompletionItems = '''
			cancel (RosettaEnumValue) -> cancel [[5, 37] .. [5, 37]]
			correct (RosettaEnumValue) -> correct [[5, 37] .. [5, 37]]
			new (RosettaEnumValue) -> new [[5, 37] .. [5, 37]]
			'''
		]
	}
	
	@Test
	def void testSynonymSetToBoolean() {
		val model = '''
			namespace "test"
			version "test"
			
			type Foo:
				attr boolean (1..1)
				[synonym FpML set to T]
			}
		'''
		testCompletion[
			it.model = model
			it.line = 5
			it.column = 23
			it.expectedCompletionItems = '''
			True -> True [[5, 22] .. [5, 23]]
			-> -> -> [[5, 23] .. [5, 23]]
			'''
		]
	}
	
	@Test
	def void testAssignOutputEnumLiteral() {
		val model = '''
			namespace "test"
			
			type Quote:
				action ActionEnum (1..1)

			enum ActionEnum:
				new
				correct
				cancel

			enum BadEnum:
				new
				correct
				cancel

			func test:
				inputs: attrIn Quote (0..1)
				output: attrOut Quote (0..1)
				set attrOut -> action:
					
		'''
		testCompletion[
			it.model = model
			it.line = 19
			it.column = 2
			// TODO: shouldn't have this much?
			it.expectedCompletionItems = '''
			ActionEnum (RosettaEnumeration) -> ActionEnum [[19, 2] .. [19, 2]]
			attrIn (Attribute) -> attrIn [[19, 2] .. [19, 2]]
			attrOut (Attribute) -> attrOut [[19, 2] .. [19, 2]]
			BadEnum (RosettaEnumeration) -> BadEnum [[19, 2] .. [19, 2]]
			boolean (RosettaBasicType) -> boolean [[19, 2] .. [19, 2]]
			calculation (RosettaTypeAlias) -> calculation [[19, 2] .. [19, 2]]
			com.rosetta.model.boolean (RosettaBasicType) -> com.rosetta.model.boolean [[19, 2] .. [19, 2]]
			com.rosetta.model.calculation (RosettaTypeAlias) -> com.rosetta.model.calculation [[19, 2] .. [19, 2]]
			com.rosetta.model.date (RosettaRecordType) -> com.rosetta.model.date [[19, 2] .. [19, 2]]
			com.rosetta.model.dateTime (RosettaRecordType) -> com.rosetta.model.dateTime [[19, 2] .. [19, 2]]
			com.rosetta.model.eventType (RosettaTypeAlias) -> com.rosetta.model.eventType [[19, 2] .. [19, 2]]
			com.rosetta.model.int (RosettaTypeAlias) -> com.rosetta.model.int [[19, 2] .. [19, 2]]
			com.rosetta.model.number (RosettaBasicType) -> com.rosetta.model.number [[19, 2] .. [19, 2]]
			com.rosetta.model.pattern (RosettaBasicType) -> com.rosetta.model.pattern [[19, 2] .. [19, 2]]
			com.rosetta.model.productType (RosettaTypeAlias) -> com.rosetta.model.productType [[19, 2] .. [19, 2]]
			com.rosetta.model.string (RosettaBasicType) -> com.rosetta.model.string [[19, 2] .. [19, 2]]
			com.rosetta.model.time (RosettaBasicType) -> com.rosetta.model.time [[19, 2] .. [19, 2]]
			com.rosetta.model.zonedDateTime (RosettaRecordType) -> com.rosetta.model.zonedDateTime [[19, 2] .. [19, 2]]
			date (RosettaRecordType) -> date [[19, 2] .. [19, 2]]
			dateTime (RosettaRecordType) -> dateTime [[19, 2] .. [19, 2]]
			eventType (RosettaTypeAlias) -> eventType [[19, 2] .. [19, 2]]
			int (RosettaTypeAlias) -> int [[19, 2] .. [19, 2]]
			number (RosettaBasicType) -> number [[19, 2] .. [19, 2]]
			pattern (RosettaBasicType) -> pattern [[19, 2] .. [19, 2]]
			productType (RosettaTypeAlias) -> productType [[19, 2] .. [19, 2]]
			Quote (Data) -> Quote [[19, 2] .. [19, 2]]
			string (RosettaBasicType) -> string [[19, 2] .. [19, 2]]
			test.ActionEnum (RosettaEnumeration) -> test.ActionEnum [[19, 2] .. [19, 2]]
			test.BadEnum (RosettaEnumeration) -> test.BadEnum [[19, 2] .. [19, 2]]
			test.Quote (Data) -> test.Quote [[19, 2] .. [19, 2]]
			time (RosettaBasicType) -> time [[19, 2] .. [19, 2]]
			zonedDateTime (RosettaRecordType) -> zonedDateTime [[19, 2] .. [19, 2]]
			"value" (STRING) -> "value" [[19, 2] .. [19, 2]]
			all -> all [[19, 2] .. [19, 2]]
			and -> and [[19, 2] .. [19, 2]]
			any -> any [[19, 2] .. [19, 2]]
			contains -> contains [[19, 2] .. [19, 2]]
			count -> count [[19, 2] .. [19, 2]]
			default -> default [[19, 2] .. [19, 2]]
			disjoint -> disjoint [[19, 2] .. [19, 2]]
			distinct -> distinct [[19, 2] .. [19, 2]]
			empty -> empty [[19, 2] .. [19, 2]]
			exists -> exists [[19, 2] .. [19, 2]]
			extract -> extract [[19, 2] .. [19, 2]]
			False -> False [[19, 2] .. [19, 2]]
			filter -> filter [[19, 2] .. [19, 2]]
			first -> first [[19, 2] .. [19, 2]]
			flatten -> flatten [[19, 2] .. [19, 2]]
			if -> if [[19, 2] .. [19, 2]]
			is -> is [[19, 2] .. [19, 2]]
			it -> it [[19, 2] .. [19, 2]]
			item -> item [[19, 2] .. [19, 2]]
			join -> join [[19, 2] .. [19, 2]]
			last -> last [[19, 2] .. [19, 2]]
			max -> max [[19, 2] .. [19, 2]]
			min -> min [[19, 2] .. [19, 2]]
			multiple -> multiple [[19, 2] .. [19, 2]]
			one-of -> one-of [[19, 2] .. [19, 2]]
			only-element -> only-element [[19, 2] .. [19, 2]]
			optional -> optional [[19, 2] .. [19, 2]]
			or -> or [[19, 2] .. [19, 2]]
			reduce -> reduce [[19, 2] .. [19, 2]]
			required -> required [[19, 2] .. [19, 2]]
			reverse -> reverse [[19, 2] .. [19, 2]]
			single -> single [[19, 2] .. [19, 2]]
			sort -> sort [[19, 2] .. [19, 2]]
			sum -> sum [[19, 2] .. [19, 2]]
			switch -> switch [[19, 2] .. [19, 2]]
			to-date -> to-date [[19, 2] .. [19, 2]]
			to-date-time -> to-date-time [[19, 2] .. [19, 2]]
			to-enum -> to-enum [[19, 2] .. [19, 2]]
			to-int -> to-int [[19, 2] .. [19, 2]]
			to-number -> to-number [[19, 2] .. [19, 2]]
			to-string -> to-string [[19, 2] .. [19, 2]]
			to-time -> to-time [[19, 2] .. [19, 2]]
			to-zoned-date-time -> to-zoned-date-time [[19, 2] .. [19, 2]]
			True -> True [[19, 2] .. [19, 2]]
			( -> ( [[19, 2] .. [19, 2]]
			* -> * [[19, 2] .. [19, 2]]
			/ -> / [[19, 2] .. [19, 2]]
			< -> < [[19, 2] .. [19, 2]]
			<= -> <= [[19, 2] .. [19, 2]]
			<> -> <> [[19, 2] .. [19, 2]]
			= -> = [[19, 2] .. [19, 2]]
			> -> > [[19, 2] .. [19, 2]]
			>= -> >= [[19, 2] .. [19, 2]]
			[ -> [ [[19, 2] .. [19, 2]]
			'''
		]
	}
	
	@Test
	def void testImport() {
		val model = '''
			namespace my.ns
			
			import 
		'''
		testCompletion[
			it.filesInScope = #{"otherns.rosetta" -> '''namespace my.other.ns'''}
			it.model = model
			it.line = 2
			it.column = 7
			// TODO: should have an auto completion?
			it.expectedCompletionItems = '''
			'''
		]
	}
	
	@Test
	def void testAttributeOverride() {
		val model = '''
			namespace my.ns
			
			type Parent:
				attr int (0..1)
				parentAttr int (1..1)
			
			type Foo extends Parent:
				override attr int (1..1)
				otherAttr string (1..1)
			
			type Bar extends Foo:
				override 
				barAttr int (1..1)
		'''
		testCompletion[
			it.model = model
			it.line = 11
			it.column = 10
			it.expectedCompletionItems = '''
			attr -> attr [[11, 10] .. [11, 10]]
			otherAttr -> otherAttr [[11, 10] .. [11, 10]]
			parentAttr -> parentAttr [[11, 10] .. [11, 10]]
			'''
		]
	}
}