package com.regnosys.rosetta.formatting2;

import com.google.common.collect.Maps;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.testing.formatter.FormatterTestHelper;
import org.eclipse.xtext.testing.formatter.FormatterTestRequest;
import org.eclipse.xtext.util.ExceptionAcceptor;
import org.eclipse.xtext.util.Strings;
import org.junit.jupiter.api.Assertions;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ExpressionFormatterTestHelper extends FormatterTestHelper {

	@Inject
	private Provider<FormatterTestRequest> formatterRequestProvider;

	public void assertFormattedExpression(Consumer<? super FormatterTestRequest> test) {
		FormatterTestRequest request = formatterRequestProvider.get();
		request.preferences(prefs -> {
			prefs.put(FormatterPreferenceKeys.maxLineWidth, 80);
			prefs.put(FormatterPreferenceKeys.indentation, "\t"); // Note: this should not be required if we have proper code formatting...
		});
		test.accept(request);
		assertFormattedExpression(request);
	}

	public void assertFormattedRuleExpression(Consumer<? super FormatterTestRequest> test) {
		String prefix = """
				namespace test

				type Foo:

					bar Foo (1..1)

				func SomeFunc:
					output:
						result int (1..1)

				reporting rule OtherRule:
					True

				reporting rule ExpressionContainer:
				""";
		assertFormatted(cfg -> {
			cfg.preferences(prefs -> {
				prefs.put(FormatterPreferenceKeys.maxLineWidth, 80);
				prefs.put(FormatterPreferenceKeys.indentation, "\t"); // Note: this should not be required if we have proper code formatting...
			});
			test.accept(cfg);
			cfg.setExpectation(prefix + indent(cfg.getExpectationOrToBeFormatted().toString().trim(), "\t") + Strings.newLine());
			cfg.setToBeFormatted(prefix + indent(cfg.getToBeFormatted().toString().trim(), "\t"));
		});
	}

	protected String indent(String string, String indent) {
		return Arrays.stream(string.split("\\n"))
				.map(line -> {
					if (line.isEmpty()) {
						return line;
					} else {
						return indent + line;
					}
				})
				.collect(Collectors.joining(Strings.newLine()));
	}

	/**
	 * Slight modification of FormatterTestHelper::assertFormatted to allow
	 * the formatted expression to occur either on the same line or on a newline.
	 */
	public void assertFormattedExpression(FormatterTestRequest req) {
		checkNotNull(req);
		checkNotNull(req.getToBeFormatted());

		String prefix = """
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
					set result:""";
		req.setToBeFormatted(prefix + "\n" + indent(req.getToBeFormatted().toString().trim(), "\t\t"));

		var request = req.getRequest();
		checkArgument(request.getTextRegionAccess() == null);

		String document = req.getToBeFormatted().toString();
		var parsed = parse(document);
		if (req.isAllowSyntaxErrors()) {
			if (request.getExplicitExceptionHandler() == null) {
				request.setExceptionHandler(ExceptionAcceptor.IGNORING);
			}
		} else {
			assertNoSyntaxErrors(parsed);
			if (request.getExplicitExceptionHandler() == null) {
				request.setExceptionHandler(ExceptionAcceptor.THROWING);
			}
		}
		request.setTextRegionAccess(createRegionAccess(parsed, req));
		if (request.getPreferences() == null)
			request.setPreferences(new MapBasedPreferenceValues(Maps.newLinkedHashMap()));
		var replacements = createFormatter(req).format(request);
		assertReplacementsAreInRegion(replacements, request.getRegions(), document);
		if (!req.isAllowUnformattedWhitespace())
			assertAllWhitespaceIsFormatted(request.getTextRegionAccess(), replacements);
		String formatted = request.getTextRegionAccess().getRewriter().renderToString(replacements);

		int prefixLines = prefix.split("\n").length;
		int resultLines = formatted.split("\n").length;
		if (prefixLines == resultLines) {
			req.setExpectation(prefix + ' ' + req.getExpectationOrToBeFormatted().toString().trim() + "\n");
		} else {
			req.setExpectation(prefix + "\n" + indent(req.getExpectationOrToBeFormatted().toString().trim(), "\t\t") + "\n");
		}

		Assertions.assertEquals(req.getExpectationOrToBeFormatted().toString(), formatted);
	}
}
