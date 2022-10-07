package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.services.RosettaGrammarAccess
import java.io.StringReader
import org.eclipse.xtext.parser.IParseResult
import org.eclipse.xtext.parser.IParser

import static org.junit.jupiter.api.Assertions.*

class TypeTestUtil {
	@Inject
	extension TypeSystem
	
	@Inject
	extension ExpressionValidationHelper
	
	@Inject IParser parser
	@Inject RosettaGrammarAccess grammar
	
	def RosettaExpression parseExpression(CharSequence expr) {
		val IParseResult result = parser.parse(grammar.rosettaCalcExpressionRule, new StringReader(expr.toString()))
		assertFalse(result.hasSyntaxErrors)
		return result.rootASTElement as RosettaExpression;
	}
	
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
		val t = e.type
		assertEquals(expected, t)
	}
	
	def void assertListSubtype(RListType a, RListType b) {
		assertTrue(isListSubtype(a, b))
	}
	def void assertNotListSubtype(RListType a, RListType b) {
		assertFalse(isListSubtype(a, b))
	}
}