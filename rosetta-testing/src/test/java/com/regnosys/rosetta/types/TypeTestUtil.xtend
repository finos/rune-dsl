package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.expression.RosettaExpression

import static org.junit.jupiter.api.Assertions.*
import com.regnosys.rosetta.tests.util.ExpressionValidationHelper
import com.regnosys.rosetta.tests.util.ExpressionParser

class TypeTestUtil {
	@Inject
	extension TypeSystem
	
	@Inject
	extension ExpressionValidationHelper
	
	@Inject
	extension ExpressionParser
	
	def RListType getType(CharSequence expr) {
		return getType(expr.parseExpression);
	}
	def RListType getType(RosettaExpression expr) {
		val res = expr.inferType
		assertNotNull(res);
		return res;
	}
	
	def void assertIsValidWithType(CharSequence expr, RListType expected) {
		val e = expr.parseExpression
		e.assertNoIssues
		e.assertHasType(expected)
	}
	
	def void assertHasType(RosettaExpression e, RListType expected) {
		val t = e.type
		assertEquals(expected, t)
	}
	
	def void assertListSubtype(RListType a, RListType b) {
		assertTrue(isListSubtypeOf(a, b))
	}
	def void assertNotListSubtype(RListType a, RListType b) {
		assertFalse(isListSubtypeOf(a, b))
	}
}