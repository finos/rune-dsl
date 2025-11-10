package com.regnosys.rosetta.ide.inlayhints.tests;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InlayHintTest extends AbstractRosettaLanguageServerTest {
	@Test
	public void testFunctionalOperation() {
		testInlayHint(cfg -> {
			String model = """
				namespace foo.bar

				func Foo:
					inputs:
						a int (1..1)
					output:
						b number (1..1)
					set b:
						a + 1

				func Bar:
					output:
						result int (0..*)
					add result:
						[0, 1, 2] extract i [ Foo(i) ]
				""";
			cfg.setModel(model);
			cfg.setAssertNumberOfInlayHints(1);
			cfg.setAssertInlayHints(hints -> {
				var first = hints.getFirst();
				assertEquals("number (0..*)", first.getLabel().getLeft());
				Assertions.assertEquals(14, first.getPosition().getLine());
			});
		});
	}
}
