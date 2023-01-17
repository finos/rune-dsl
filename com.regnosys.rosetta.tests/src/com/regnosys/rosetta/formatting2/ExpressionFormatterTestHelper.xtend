package com.regnosys.rosetta.formatting2

import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.testing.formatter.FormatterTestRequest;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.testing.formatter.FormatterTestHelper

class ExpressionFormatterTestHelper extends FormatterTestHelper {
	def void assertFormattedExpression(Procedure1<? super FormatterTestRequest> test) {
		val prefix = '''
		namespace test
		
		type Foo:
			bar Foo (1..1)
		
		func SomeFunc:
			output:
				result int (1..1)
		
		func ExpressionContainer:
			inputs:
				foo Foo (1..1)
			output:
				result int (0..*)
			set result:
		'''
		val postfix = Strings.newLine()
		assertFormatted[
			preferences[
				put(FormatterPreferenceKeys.maxLineWidth, 80);
			]
			test.apply(it)
			setExpectation(prefix + indent(it.getExpectationOrToBeFormatted().toString().trim(), "\t\t") + postfix);
			setToBeFormatted(prefix + indent(it.getToBeFormatted().toString().trim(), "\t\t") + postfix);
		]
	}

	def protected String indent(String string, String indent) {
		return string.split("\\r?\\n").map[
			if ("".equals(it)) {
				return it;
			} else {
				return indent + it;
			}
		].join(Strings.newLine())
	}
}
