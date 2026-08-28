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

package com.regnosys.rosetta.generator.java.enums;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RJavaEnum;
import com.regnosys.rosetta.generator.java.types.RJavaEnumValue;
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.rosetta.model.lib.annotations.RosettaEnum;
import com.rosetta.model.lib.annotations.RosettaEnumValue;

import jakarta.inject.Inject;

public class EnumGenerator extends FluentRObjectJavaClassGenerator<REnumType, RJavaEnum> {
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private ModelGeneratorUtil modelGeneratorUtil;
	@Inject
	private RObjectFactory rObjectFactory;

	@Override
	protected Stream<? extends REnumType> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.filter(RosettaEnumeration.class::isInstance)
				.map(RosettaEnumeration.class::cast)
				.map(rObjectFactory::buildREnumType);
	}

	@Override
	protected RJavaEnum createTypeRepresentation(REnumType object) {
		return typeTranslator.toJavaReferenceType(object);
	}

	@Override
	protected CodeRenderer generateClass(REnumType e, RJavaEnum javaEnum, String version, JavaClassScope scope) {
		RosettaEnumeration enumeration = e.getEObject();
		List<RJavaEnumValue> enumValues = javaEnum.getEnumValues();
		return out -> {
			out.write(modelGeneratorUtil.javadoc(enumeration.getDefinition(), enumeration.getReferences(), version));
			out.writeln("@", RosettaEnum.class, "(\"", e.getName(), "\")");
			out.writeln("public enum ", javaEnum, " {");
			out.indented(() -> {
				out.newline();
				for (int i = 0; i < enumValues.size(); i++) {
					renderEnumValue(out, enumValues.get(i));
					out.writeln(i < enumValues.size() - 1 ? "," : ";");
				}
				out.newline();
				out.writeln("private static ", Map.class, "<", String.class, ", ", javaEnum, "> values;");
				out.writeln("static {");
				out.indented(() -> {
					out.writeln(Map.class, "<", String.class, ", ", javaEnum, "> map = new ", ConcurrentHashMap.class, "<>();");
					out.writeln("for (", javaEnum, " instance : ", javaEnum, ".values()) {");
					out.indented(() -> out.writeln("map.put(instance.toDisplayString(), instance);"));
					out.writeln("}");
					out.writeln("values = ", Collections.class, ".unmodifiableMap(map);");
				});
				out.writeln("}");
				out.newline();
				out.writeln("private final ", String.class, " rosettaName;");
				out.writeln("private final ", String.class, " displayName;");
				out.newline();
				out.writeln(javaEnum, "(", String.class, " rosettaName, ", String.class, " displayName) {");
				out.indented(() -> {
					out.writeln("this.rosettaName = rosettaName;");
					out.writeln("this.displayName = displayName;");
				});
				out.writeln("}");
				out.newline();
				out.writeln("public static ", javaEnum, " fromDisplayName(String name) {");
				out.indented(() -> {
					out.writeln(javaEnum, " value = values.get(name);");
					out.writeln("if (value == null) {");
					out.indented(() -> out.writeln("throw new ", IllegalArgumentException.class, "(\"No enum constant with display name \\\"\" + name + \"\\\".\");"));
					out.writeln("}");
					out.writeln("return value;");
				});
				out.writeln("}");
				out.newline();
				out.writeln("@Override");
				out.writeln("public ", String.class, " toString() {");
				out.indented(() -> out.writeln("return toDisplayString();"));
				out.writeln("}");
				out.newline();
				out.writeln("public ", String.class, " toDisplayString() {");
				out.indented(() -> out.writeln("return displayName != null ? displayName : rosettaName;"));
				out.writeln("}");
			});
			out.write("}");
		};
	}

	private void renderEnumValue(CodeWriter out, RJavaEnumValue value) {
		out.write(modelGeneratorUtil.javadoc(value.getEObject()));
		String displayName = value.getDisplayName();
		out.write("@", RosettaEnumValue.class, "(value = \"", value.getRosettaName(), "\"");
		if (displayName != null) {
			out.write(", displayName = \"", displayName, "\"");
		}
		out.writeln(")");
		out.write(value.getName(), "(\"", value.getRosettaName(), "\", ",
				displayName != null ? "\"" + StringEscapeUtils.escapeJava(displayName) + "\"" : "null", ")");
	}
}
