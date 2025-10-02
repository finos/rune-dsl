/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.ide.textmate.tests;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import jakarta.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
