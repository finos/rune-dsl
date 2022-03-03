package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.typing.RosettaTyping
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.tests.util.ModelHelper
import com.regnosys.rosetta.rosetta.simple.Function
import static org.junit.jupiter.api.Assertions.*

class TypeTestUtil {
	@Inject
	extension RosettaTyping
	@Inject
	extension ModelHelper
	
	def RosettaExpression parseExpression(CharSequence expr, String listType) {
		val model = '''
			func Test:
				output: result «listType»
				assign-output result: «expr»
		'''.parseRosetta;
		return (model.elements.last as Function).operations.head.expression;
	}
	def RosettaExpression parseExpression(CharSequence expr) {
		var pexpr = parseExpression(expr, 'int (0..*)')
		return pexpr
	}
	
	def RListType getType(CharSequence expr) {
		return getType(expr.parseExpression);
	}
	def RListType getType(RosettaExpression expr) {
		val res = expr.inferType();
		assertFalse(res.failed);
		return res.value;
	}
}