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

import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.services.RosettaGrammarAccess;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;

public class ListRosettaKeywordsAsRegex {

	public static void main(String[] args) {
		Injector inj = new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration();
		
		RosettaGrammarAccess grammarAccess = inj.getInstance(RosettaGrammarAccess.class);
		Grammar grammar = grammarAccess.getGrammar();
		Set<String> keywords = GrammarUtil.getAllKeywords(grammar);
		
		System.out.println(
				keywords.stream()
					.filter(keyword -> keyword.matches(".*[a-zA-Z].*")) // only match keywords that at least contain one alpha character
					.map(keyword -> {
						if (keyword.matches("[a-zA-Z]*")) {
							return keyword;
						} else {
							return Pattern.quote(keyword).replace("\\", "\\\\");
						}
					})
					.collect(Collectors.joining("|"))
		);
	}
}
