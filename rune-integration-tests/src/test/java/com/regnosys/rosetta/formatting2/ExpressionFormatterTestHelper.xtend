package com.regnosys.rosetta.formatting2

import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.testing.formatter.FormatterTestRequest;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.testing.formatter.FormatterTestHelper
import org.eclipse.xtext.preferences.MapBasedPreferenceValues
import com.google.common.collect.Maps
import org.eclipse.xtext.util.ExceptionAcceptor
import static com.google.common.base.Preconditions.checkNotNull
import static com.google.common.base.Preconditions.checkArgument
import org.junit.jupiter.api.Assertions
import javax.inject.Inject
import javax.inject.Provider

class ExpressionFormatterTestHelper extends FormatterTestHelper {
	@Inject
	Provider<FormatterTestRequest> formatterRequestProvider;
	
	def void assertFormattedExpression(Procedure1<? super FormatterTestRequest> test) {
		val request = formatterRequestProvider.get();
		request.preferences[
			put(FormatterPreferenceKeys.maxLineWidth, 80);
			put(FormatterPreferenceKeys.indentation, '\t'); // Note: this should not be required if we have proper code formatting...
		]
		test.apply(request);
		assertFormattedExpression(request);
	}
	
	def void assertFormattedRuleExpression(Procedure1<? super FormatterTestRequest> test) {
		val prefix = '''
		namespace test
		
		type Foo:
		
			bar Foo (1..1)
		
		func SomeFunc:
			output:
				result int (1..1)
		
		reporting rule OtherRule:
			True
		
		reporting rule ExpressionContainer:
		'''
		assertFormatted[
			preferences[
				put(FormatterPreferenceKeys.maxLineWidth, 80);
				put(FormatterPreferenceKeys.indentation, '\t'); // Note: this should not be required if we have proper code formatting...
			]
			test.apply(it)
			it.expectation = prefix + indent(it.expectationOrToBeFormatted.toString().trim(), "\t") + Strings.newLine()
			it.toBeFormatted = prefix + indent(it.toBeFormatted.toString().trim(), "\t")
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
	
	/**
	 * Slight modification of FormatterTestHelper::assertFormatted to allow
	 * the formatted expression to occur either on the same line or on a newline.
	 */
	def void assertFormattedExpression(FormatterTestRequest req) {
		checkNotNull(req);
		checkNotNull(req.getToBeFormatted());

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
			set result:'''
		req.toBeFormatted = prefix + Strings.newLine() + indent(req.toBeFormatted.toString.trim, "\t\t")

		val request = req.getRequest();
		checkArgument(request.getTextRegionAccess() === null);

		val document = req.getToBeFormatted().toString();
		val parsed = parse(document);
		if (req.isAllowSyntaxErrors()) {
			if (request.getExplicitExceptionHandler() === null) {
				request.setExceptionHandler(ExceptionAcceptor.IGNORING);
			}
		} else {
			assertNoSyntaxErrors(parsed);
			if (request.getExplicitExceptionHandler() === null) {
				request.setExceptionHandler(ExceptionAcceptor.THROWING);
			}
		}
		request.setTextRegionAccess(createRegionAccess(parsed, req));
		if (request.getPreferences() === null)
			request.setPreferences(new MapBasedPreferenceValues(Maps.<String, String> newLinkedHashMap()));
		val replacements = createFormatter(req).format(request);
		assertReplacementsAreInRegion(replacements, request.getRegions(), document);
		if (!req.isAllowUnformattedWhitespace())
			assertAllWhitespaceIsFormatted(request.getTextRegionAccess(), replacements);
		val formatted = request.getTextRegionAccess().getRewriter().renderToString(replacements);

		val prefixLines = prefix.split("\r\n|\r|\n").length
		val resultLines = formatted.split("\r\n|\r|\n").length
		if (prefixLines === resultLines) {
			req.expectation = prefix + ' ' + req.expectationOrToBeFormatted.toString().trim() + Strings.newLine()
		} else {
			req.expectation = prefix + Strings.newLine + indent(req.expectationOrToBeFormatted.toString().trim(), "\t\t") + Strings.newLine()
		}
		
		Assertions.assertEquals(req.getExpectationOrToBeFormatted().toString(), formatted);
	}
}
