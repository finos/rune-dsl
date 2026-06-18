package com.regnosys.rosetta.ide.hover;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;
import org.junit.jupiter.api.Test;

public class RosettaDocumentationProviderTest extends AbstractRosettaLanguageServerTest {
	@Test
	void testMultiCardinalityDocs() {
		testHover(configuration -> {
			configuration.setModel("""
					namespace foo.bar

					type Foo:
						attr int (2..3)
					""");
			configuration.setLine(3);
			configuration.setColumn(1);
			configuration.setAssertHover(hover -> {
				String expected = """
						[[3, 1] .. [3, 5]]
						kind: markdown
						value: **Multi cardinality.**
						""";
				assertEquals(expected, toExpectation(hover));
			});
		});
	}

	@Test
	void testImplicitEnumDocs() {
		testHover(configuration -> {
			configuration.setModel("""
					namespace foo.bar

					enum MyEnum:
						VALUE

					func Foo:
						output:
							result MyEnum (1..1)

						set result:
							VALUE
					""");
			configuration.setLine(10);
			configuration.setColumn(2);
			configuration.setAssertHover(hover -> {
				String expected = """
						[[10, 2] .. [10, 7]]
						kind: markdown
						value: MyEnum
						""";
				assertEquals(expected, toExpectation(hover));
			});
		});
	}
}
