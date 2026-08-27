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

package com.regnosys.rosetta.generator.java.object.validators;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ExistenceChecker;
import com.rosetta.model.lib.validation.ValidationResult;

import jakarta.inject.Inject;

/**
 * Generates a {@code Validator} that checks that exactly the given set of fields is set on a type.
 */
public class OnlyExistsValidatorGenerator extends FluentRObjectJavaClassGenerator<RDataType, RGeneratedJavaClass<?>> {
	private static final Method VALIDATION_SUCCESS = staticMethod(ValidationResult.class, "success", String.class, ValidationResult.ValidationType.class, String.class, RosettaPath.class, String.class);
	private static final Method VALIDATION_FAILURE = staticMethod(ValidationResult.class, "failure", String.class, ValidationResult.ValidationType.class, String.class, RosettaPath.class, String.class, String.class);

	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private JavaTypeTranslator typeTranslator;

	private static Method staticMethod(Class<?> owner, String name, Class<?>... parameterTypes) {
		try {
			return owner.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected Stream<? extends RDataType> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.filter(Data.class::isInstance)
				.map(Data.class::cast)
				.map(rObjectFactory::buildRDataType);
	}

	@Override
	protected RGeneratedJavaClass<?> createTypeRepresentation(RDataType type) {
		return typeTranslator.toOnlyExistsValidatorClass(typeTranslator.toJavaReferenceType(type));
	}

	@Override
	protected CodeRenderer generateClass(RDataType type, RGeneratedJavaClass<?> validatorClass, String version, JavaClassScope scope) {
		JavaPojoInterface pojo = typeTranslator.toJavaReferenceType(type);
		String rosettaName = pojo.getRosettaName();
		Collection<RAttribute> attributes = type.getAllAttributes();

		return out -> {
			out.writeln("public ", validatorClass.asClassDeclaration(), " {");
			out.indented(() -> {
				out.newline();
				out.writeln("/* Casting is required to ensure types are output to ensure recompilation in Rosetta */");
				out.writeln("@Override");
				out.writeln("public <T2 extends ", pojo, "> ", ValidationResult.class, "<", pojo, "> validate(", RosettaPath.class, " path, T2 o, ", Set.class, "<String> fields) {");
				out.indented(() -> {
					out.writeln(Map.class, "<String, Boolean> fieldExistenceMap = ", ImmutableMap.class, ".<String, Boolean>builder()");
					out.indented(() -> {
						for (RAttribute attr : attributes) {
							JavaPojoProperty prop = pojo.findProperty(attr.getName());
							JavaExpression propertyValue = prop.applyGetter(JavaExpression.from(o -> o.write("o"), pojo));
							out.writeln(".put(\"", prop.getName(), "\", ", ExistenceChecker.class, ".isSet((", prop.getType(), ") ", propertyValue, "))");
						}
						out.writeln(".build();");
					});
					out.newline();
					out.writeln("// Find the fields that are set");
					out.writeln(Set.class, "<String> setFields = fieldExistenceMap.entrySet().stream()");
					out.indented(() -> {
						out.writeln(".filter(Map.Entry::getValue)");
						out.writeln(".map(Map.Entry::getKey)");
						out.writeln(".collect(", Collectors.class, ".toSet());");
					});
					out.newline();
					out.writeln("if (setFields.equals(fields)) {");
					out.indented(() -> out.writeln("return ", VALIDATION_SUCCESS, "(\"", rosettaName, "\", ", ValidationResult.ValidationType.class, ".ONLY_EXISTS, \"", rosettaName, "\", path, \"\");"));
					out.writeln("}");
					out.writeln("return ", VALIDATION_FAILURE, "(\"", rosettaName, "\", ", ValidationResult.ValidationType.class, ".ONLY_EXISTS, \"", rosettaName, "\", path, \"\",");
					out.indented(() -> out.writeln(String.class, ".format(\"[%s] should only be set.  Set fields: %s\", fields, setFields));"));
				});
				out.writeln("}");
			});
			out.write("}");
		};
	}
}
