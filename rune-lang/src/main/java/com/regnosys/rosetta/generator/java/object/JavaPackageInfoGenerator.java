/*
 * Copyright 2026 REGnosys
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

package com.regnosys.rosetta.generator.java.object;

import java.util.Collection;
import java.util.List;

import org.eclipse.xtext.generator.IFileSystemAccess2;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.codegen.support.StringCodeWriter;
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil;
import com.regnosys.rosetta.rosetta.RosettaModel;

import jakarta.inject.Inject;

public class JavaPackageInfoGenerator {
	@Inject
	private ModelGeneratorUtil modelGeneratorUtil;

	public void generatePackageInfoClasses(IFileSystemAccess2 fsa, List<RosettaModel> elements) {
		namespaceToDescriptionMap(elements).asMap().forEach((packageName, descriptions) ->
				fsa.generateFile(packageName.replace('.', '/') + "/package-info.java", generatePackageInfo(packageName, descriptions)));
	}

	private Multimap<String, String> namespaceToDescriptionMap(List<RosettaModel> elements) {
		Multimap<String, String> namespaceToDescription = LinkedHashMultimap.create();
		elements.stream()
				.filter(model -> model.getDefinition() != null)
				.forEach(model -> namespaceToDescription.put(model.getName(), model.getDefinition()));
		return namespaceToDescription;
	}

	private String generatePackageInfo(String packageName, Collection<String> descriptions) {
		CodeWriter out = new StringCodeWriter();
		out.writeln("/**");
		out.writeln("*");
		for (String description : descriptions) {
			out.writeln("*\t", modelGeneratorUtil.escapeComment(description));
			out.writeln("*\t<p>");
			out.writeln("*");
		}
		out.writeln("*");
		out.writeln("*/");
		out.newline();
		out.writeln("package ", packageName, ";");
		return out.toString();
	}
}
