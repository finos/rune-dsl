package com.regnosys.rosetta.ide.formatting;

import com.regnosys.rosetta.formatting2.FormattingOptionsAdaptor;
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import org.eclipse.lsp4j.FormattingOptions;
import org.junit.jupiter.api.Test;

public class FormattingTest extends AbstractRosettaLanguageServerTest {
	@Test
	void testFormattingWithSmallMaxLineWidth() {
		String model = """
				namespace foo.bar

				type Foo:
					a int (1..1)

				func Foo:
					inputs: foo Foo (1..1)
					output: result int (1..1)

					set result:
						foo -> a
				""";
		testFormatting(
				params -> {
					FormattingOptions options = new FormattingOptions();
					options.putNumber(FormattingOptionsAdaptor.PREFERENCE_MAX_LINE_WIDTH_KEY, 10);
					params.setOptions(options);
				},
				configuration -> {
					configuration.setModel(model);
					configuration.setExpectedText("""
							namespace foo.bar

							type Foo:
								a int (1..1)

							func Foo:
								inputs:
									foo Foo (1..1)
								output:
									result int (1..1)

								set result:
									foo -> a
							""");
				}
		);
	}

	@Test
	void testFormattingWithSmallConditionalMaxLineWidth() {
		String model = """
				namespace foo.bar

				type Foo:
					a int (1..1)

				func Foo:
					inputs: foo Foo (1..1)
					output: result int (0..*)

					add result:
						foo -> a
					add result:
						if True then 42 else 10
				""";
		testFormatting(
				params -> {
					FormattingOptions options = new FormattingOptions();
					options.putNumber(FormattingOptionsAdaptor.PREFERENCE_CONDITIONAL_MAX_LINE_WIDTH_KEY, 10);
					params.setOptions(options);
				},
				configuration -> {
					configuration.setModel(model);
					configuration.setExpectedText("""
							namespace foo.bar

							type Foo:
								a int (1..1)

							func Foo:
								inputs:
									foo Foo (1..1)
								output:
									result int (0..*)

								add result: foo -> a
								add result:
									if True
									then 42
									else 10
							""");
				}
		);
	}
}
