package com.regnosys.rosetta.ide.textmate.tests;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.regnosys.rosetta.ide.tests.RosettaIdeInjectorProvider;
import com.regnosys.rosetta.ide.textmate.RosettaTextMateGrammarUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaIdeInjectorProvider.class)
public class RosettaTextMateGrammarTest {	
	@Inject
	private RosettaTextMateGrammarUtil tmUtil;
	
	@Test
	public void testCanOpenGrammar() throws IOException {
		String content;
		URL url = tmUtil.getTextMateGrammarURL();
		assertNotNull(url);
		try (Scanner scanner = new Scanner(url.openStream(),
	            StandardCharsets.UTF_8))
	    {
	        scanner.useDelimiter("\\A");
	        content = scanner.hasNext() ? scanner.next() : "";
	    }
		JsonObject grammarDef = JsonParser.parseString(content).getAsJsonObject();
		assertEquals(grammarDef.get("name").getAsString(), "Rosetta DSL");
	}
}
