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

package com.regnosys.rosetta.tools;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Injector;
import com.regnosys.rosetta.ide.RosettaIdeSetup;
import com.regnosys.rosetta.ide.textmate.RosettaTextMateGrammarUtil;

public class ListTextMateScopes {
	public static void main(String[] args) throws IOException {
		Injector inj = new RosettaIdeSetup().createInjector();
		
		RosettaTextMateGrammarUtil util = inj.getInstance(RosettaTextMateGrammarUtil.class);
		String content;
		URL url = util.getTextMateGrammarURL();
		try (Scanner scanner = new Scanner(url.openStream(),
	            StandardCharsets.UTF_8))
	    {
	        scanner.useDelimiter("\\A");
	        content = scanner.hasNext() ? scanner.next() : "";
	    }
		JsonObject grammarDef = JsonParser.parseString(content).getAsJsonObject();
		
		Set<String> scopes = new HashSet<>();
		addScopesRecursively(grammarDef.get("patterns"), scopes);
		addScopesRecursively(grammarDef.get("repository"), scopes);
		
		List<String[]> sortedScopes = scopes.stream().map(s -> s.replace(".rosetta", "").split("\\.")).collect(Collectors.toList());
		sortedScopes.sort(ListTextMateScopes::compareScopes);
		for (String[] scope: sortedScopes) {
			System.out.println(List.of(scope).stream().collect(Collectors.joining(".")));
		}
	}
	
	private static int compareScopes(String[] scope1, String[] scope2) {
		for (int i=0; i<scope1.length && i<scope2.length; i++) {
			int c = scope1[i].compareTo(scope2[i]);
			if (c != 0) {
				return c;
			}
		}
		return Integer.compare(scope1.length, scope2.length);
	}
	
	private static void addScopesRecursively(JsonElement elem, Collection<String> scopes) {
		if (elem.isJsonObject()) {
			JsonObject object = elem.getAsJsonObject();
			if (object.get("name") != null) {
				String[] objScopes = object.get("name").getAsString().split(" ");
				for (String scope: objScopes)
				scopes.add(scope);
			}
			for (String key: object.keySet()) {
				JsonElement child = object.get(key);
				addScopesRecursively(child, scopes);
			}
		} else if (elem.isJsonArray()) {
			JsonArray array = elem.getAsJsonArray();
			for (JsonElement item: array) {
				addScopesRecursively(item, scopes);
			}
		}
	}
}
