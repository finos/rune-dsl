package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.typing.RosettaTyping
import com.regnosys.rosetta.rosetta.RosettaExpression
import static org.junit.jupiter.api.Assertions.*
import org.eclipse.xtext.parser.IParser
import com.regnosys.rosetta.services.RosettaGrammarAccess
import org.eclipse.xtext.parser.IParseResult
import java.io.StringReader

class TypeTestUtil {
	@Inject
	extension RosettaTyping
	
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
		val res = expr.inferType();
		assertFalse(res.failed);
		return res.value;
	}
	
	def void assertIsValidWithType(CharSequence expr, RListType expected) {
		val e = expr.parseExpression
		e.assertNoIssues
		val t = e.type
		assertEquals(expected, t)
	}
	
	def void assertListSubtype(RListType a, RListType b) {
		assertTrue(listSubtype(a, b).value)
	}
	def void assertNotListSubtype(RListType a, RListType b) {
		assertFalse(listSubtype(a, b).value)
	}
}