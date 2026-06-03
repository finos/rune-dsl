package com.regnosys.rosetta.xcore.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

public class ParserCaseExtractorFragmentTest {
	@Test
	public void testParserCaseExtractorFragment() throws IOException {
		File source = new File("src/test/resources/extractor-test/Parser.java");
		File target = new File("target/extractor-test/Parser.java");
		target.getParentFile().mkdirs();
		Files.copy(source, target);
		
		ParserCaseExtractorFragment fragment = new ParserCaseExtractorFragment();
		fragment.addGrammarFile(target.toString());
		fragment.generate();
		
		File expected = new File("src/test/resources/extractor-test/ExpectedOutputParser.java");
		
		assertEquals(
				Files.asCharSource(expected, StandardCharsets.UTF_8).read().replace("\r\n", "\n"),
				Files.asCharSource(target, StandardCharsets.UTF_8).read().replace("\r\n", "\n")
			);
	}
}
