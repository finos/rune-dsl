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

package com.regnosys.rosetta.generator.java.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import java.util.stream.Collectors;

public class RosettaGrammarUtil {

	public static String quote(String text) {
		String escaped = text.trim()
				.replace("\"", "\\\"")
				.replace("\r\n", "\n")
				.replace("\n\n", "\n")
				.replace("\n", "\\n\" + \n\t\"");
		return "\"" + escaped + "\"";
	}

	public static String extractNodeText(EObject rosettaFeature, EStructuralFeature feature) {
		return NodeModelUtils.findNodesForFeature(rosettaFeature, feature).stream()
				.map(NodeModelUtils::getTokenText)
				.collect(Collectors.joining());
	}
}
