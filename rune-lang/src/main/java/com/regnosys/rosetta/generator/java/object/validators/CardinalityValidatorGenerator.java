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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
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
import com.regnosys.rosetta.types.RCardinality;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;

import jakarta.inject.Inject;

/**
 * Generates a {@code Validator} that checks the cardinality of every bounded attribute of a type.
 */
public class CardinalityValidatorGenerator extends FluentRObjectJavaClassGenerator<RDataType, RGeneratedJavaClass<?>> {
	private static final Method IS_NULL_OR_EMPTY = staticMethod(Strings.class, "isNullOrEmpty", String.class);
	private static final Method CHECK_CARDINALITY = staticMethod(ExpressionOperatorsNullSafe.class, "checkCardinality", String.class, int.class, int.class, int.class);
	private static final Method VALIDATION_SUCCESS = staticMethod(ValidationResult.class, "success", String.class, ValidationResult.ValidationType.class, String.class, RosettaPath.class, String.class);
	private static final Method VALIDATION_FAILURE = staticMethod(ValidationResult.class, "failure", String.class, ValidationResult.ValidationType.class, String.class, RosettaPath.class, String.class, String.class);
	private static final Method COLLECTORS_TO_LIST = staticMethod(Collectors.class, "toList");

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
		return typeTranslator.toValidatorClass(typeTranslator.toJavaReferenceType(type));
	}

	@Override
	protected CodeRenderer generateClass(RDataType type, RGeneratedJavaClass<?> validatorClass, String version, JavaClassScope scope) {
		JavaPojoInterface pojo = typeTranslator.toJavaReferenceType(type);
		String rosettaName = pojo.getRosettaName();
		List<CodeRenderer> attributeChecks = type.getAllAttributes().stream()
				.map(attr -> checkCardinality(pojo, attr))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		return out -> {
			out.writeln("public ", validatorClass.asClassDeclaration(), " {");
			out.indented(() -> {
				out.writeln("private ", List.class, "<", ComparisonResult.class, "> getComparisonResults(", pojo, " o) {");
				out.indented(() -> {
					// Guava's Lists.newArrayList is used instead of List.of, as generated code must remain Java 8 compatible.
					out.writeln("return ", Lists.class, ".<", ComparisonResult.class, ">newArrayList(");
					out.indented(() -> {
						for (int i = 0; i < attributeChecks.size(); i++) {
							out.write(attributeChecks.get(i));
							out.writeln(i < attributeChecks.size() - 1 ? "," : "");
						}
					});
					out.writeln(");");
				});
				out.writeln("}");
				out.newline();
				out.writeln("@Override");
				out.writeln("public ", List.class, "<", ValidationResult.class, "<?>> getValidationResults(", RosettaPath.class, " path, ", pojo, " o) {");
				out.indented(() -> {
					out.writeln("return getComparisonResults(o)");
					out.indented(() -> {
						out.writeln(".stream()");
						out.writeln(".map(res -> {");
						out.indented(() -> {
							out.writeln("if (!", IS_NULL_OR_EMPTY, "(res.getError())) {");
							out.indented(() -> out.writeln("return ", VALIDATION_FAILURE, "(\"", rosettaName, "\", ", ValidationResult.ValidationType.class, ".CARDINALITY, \"", rosettaName, "\", path, \"\", res.getError());"));
							out.writeln("}");
							out.writeln("return ", VALIDATION_SUCCESS, "(\"", rosettaName, "\", ", ValidationResult.ValidationType.class, ".CARDINALITY, \"", rosettaName, "\", path, \"\");");
						});
						out.writeln("})");
						out.writeln(".collect(", COLLECTORS_TO_LIST, "());");
					});
				});
				out.writeln("}");
			});
			out.write("}");
		};
	}

	private CodeRenderer checkCardinality(JavaPojoInterface pojo, RAttribute attr) {
		if (attr.getCardinality() == RCardinality.UNBOUNDED) {
			return null;
		}
		JavaPojoProperty prop = pojo.findProperty(attr.getName());
		JavaExpression propertyValue = prop.applyGetter(JavaExpression.from(o -> o.write("o"), pojo));
		int min = attr.getCardinality().getMin();
		int max = attr.getCardinality().getMax().orElse(0);
		// Casting is required to ensure types are output to ensure recompilation in Rosetta
		return out -> {
			out.write(CHECK_CARDINALITY, "(\"", attr.getName(), "\", (", prop.getType(), ") ", propertyValue);
			if (attr.isMulti()) {
				out.write(" == null ? 0 : ", propertyValue, ".size()");
			} else {
				out.write(" != null ? 1 : 0");
			}
			out.write(", ", min, ", ", max, ")");
		};
	}
}
