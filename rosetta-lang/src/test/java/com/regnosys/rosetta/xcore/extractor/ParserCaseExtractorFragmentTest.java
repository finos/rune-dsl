package com.regnosys.rosetta.xcore.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.xtext.xtext.generator.XtextGenerator;
import org.eclipse.xtext.xtext.generator.XtextGeneratorLanguage;
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
		
		XtextGeneratorLanguage lang = new XtextGeneratorLanguage();
		lang.addReferencedResource("platform:/resource/com.regnosys.rosetta/model/Rosetta.xcore");
		lang.addReferencedResource("platform:/resource/com.regnosys.rosetta/model/RosettaSimple.xcore");
		lang.addReferencedResource("platform:/resource/com.regnosys.rosetta/model/RosettaExpression.xcore");
		lang.setGrammarUri(new File("src/main/java", "com.regnosys.rosetta.Rosetta".replace(".", "/") + ".xtext").toURI().toString());
		lang.addFragment(fragment);
		
		XtextGenerator generator = new XtextGenerator();
		generator.addLanguage(lang);
		generator.initialize();
		
		fragment.generate();
		
		File expected = new File("src/test/resources/extractor-test/ExpectedOutputParser.java");
		
		assertEquals(
				Files.asCharSource(expected, StandardCharsets.UTF_8).read().replace("\r\n", "\n"),
				Files.asCharSource(target, StandardCharsets.UTF_8).read().replace("\r\n", "\n")
			);
	}
}
