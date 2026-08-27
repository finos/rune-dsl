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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.expression.InterpreterValueJavaConverter;
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaBlock;
import com.regnosys.rosetta.generator.java.statement.JavaForLoop;
import com.regnosys.rosetta.generator.java.statement.JavaIfThenStatement;
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable;
import com.regnosys.rosetta.generator.java.types.JavaConditionInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.simple.Condition;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.AliasHierarchy;
import com.regnosys.rosetta.types.RAliasType;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

/**
 * Generates a {@code Validator} that checks the type-format constraints (string/number bounds and
 * patterns) declared on a type's attributes, together with any conditions declared on type aliases
 * used by those attributes.
 */
public class TypeFormatValidatorGenerator extends FluentRObjectJavaClassGenerator<RDataType, RGeneratedJavaClass<?>> {
	private static final Method IS_NULL_OR_EMPTY = staticMethod(Strings.class, "isNullOrEmpty", String.class);
	private static final Method CHECK_STRING = staticMethod(ExpressionOperatorsNullSafe.class, "checkString", String.class, String.class, int.class, Optional.class, Optional.class);
	private static final Method CHECK_NUMBER = staticMethod(ExpressionOperatorsNullSafe.class, "checkNumber", String.class, BigDecimal.class, Optional.class, Optional.class, Optional.class, Optional.class);
	private static final Method VALIDATION_SUCCESS = staticMethod(ValidationResult.class, "success", String.class, ValidationResult.ValidationType.class, String.class, RosettaPath.class, String.class);
	private static final Method VALIDATION_FAILURE = staticMethod(ValidationResult.class, "failure", String.class, ValidationResult.ValidationType.class, String.class, RosettaPath.class, String.class, String.class);
	private static final Method COLLECTORS_TO_LIST = staticMethod(Collectors.class, "toList");
	private static final Method OPTIONAL_OF = staticMethod(Optional.class, "of", Object.class);
	private static final Method OPTIONAL_EMPTY = staticMethod(Optional.class, "empty");

	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private RBuiltinTypeService builtinTypes;
	@Inject
	private JavaTypeUtil typeUtil;
	@Inject
	private JavaIdentifierRepresentationService identifierRepresentationService;
	@Inject
	private InterpreterValueJavaConverter interpreterValueJavaConverter;
	@Inject
	private TypeCoercionService typeCoercionService;

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
		return typeTranslator.toTypeFormatValidatorClass(typeTranslator.toJavaReferenceType(type));
	}

	@Override
	protected CodeRenderer generateClass(RDataType type, RGeneratedJavaClass<?> validatorClass, String version, JavaClassScope scope) {
		JavaPojoInterface javaType = typeTranslator.toJavaReferenceType(type);
		String rosettaName = javaType.getRosettaName();
		Collection<RAttribute> attributes = type.getAllAttributes();
		GeneratedIdentifier pathId = scope.createUniqueIdentifier("path");

		Map<RAttribute, AliasHierarchy> aliasHierarchyPerAttribute = new LinkedHashMap<>();
		for (RAttribute attr : attributes) {
			aliasHierarchyPerAttribute.put(attr, typeSystem.computeAliasHierarchy(attr.getRMetaAnnotatedType().getRType()));
		}
		Set<JavaConditionInterface> conditionDependencies = aliasHierarchyPerAttribute.values().stream()
				.flatMap(hierarchy -> hierarchy.getAliases().stream())
				.flatMap(alias -> alias.getConditions().stream())
				.map(typeTranslator::toConditionJavaClass)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		JavaMethodScope runConditionsScope = scope.createMethodScope("runConditions");
		GeneratedIdentifier instanceId = runConditionsScope.createUniqueIdentifier("o");
		JavaVariable instanceVar = new JavaVariable(instanceId, javaType);
		GeneratedIdentifier resultsId = runConditionsScope.createUniqueIdentifier("results");

		List<CodeRenderer> attributeChecks = attributes.stream()
				.flatMap(attr -> checkTypeFormat(javaType, attr).stream())
				.collect(Collectors.toList());

		return out -> {
			out.writeln("public ", validatorClass.asClassDeclaration(), " {");
			out.indented(() -> {
				for (JavaConditionInterface dep : conditionDependencies) {
					out.writeln("@", javax.inject.Inject.class);
					out.writeln("protected ", dep, " ", scope.createIdentifier(identifierRepresentationService.toDependencyInstance(dep), StringUtils.uncapitalize(dep.getSimpleName())), ";");
				}
				out.newline();
				out.writeln("private ", List.class, "<", ComparisonResult.class, "> getComparisonResults(", javaType, " o) {");
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
				if (!conditionDependencies.isEmpty()) {
					out.newline();
					out.writeln("private ", List.class, "<", ValidationResult.class, "<?>> runConditions(", RosettaPath.class, " ", pathId, ", ", javaType, " ", instanceId, ") {");
					out.indented(() -> {
						out.writeln(List.class, "<", ValidationResult.class, "<?>> ", resultsId, " = new ", ArrayList.class, "();");
						for (RAttribute attr : attributes) {
							out.write(checkTypeConditions(javaType, attr, aliasHierarchyPerAttribute.get(attr), pathId, instanceVar, resultsId, runConditionsScope.getBodyScope()).asStatementList());
						}
						out.writeln("return ", resultsId, ";");
					});
					out.writeln("}");
				}
				out.newline();
				out.writeln("@Override");
				out.writeln("public ", List.class, "<", ValidationResult.class, "<?>> getValidationResults(", RosettaPath.class, " ", pathId, ", ", javaType, " o) {");
				out.indented(() -> {
					if (conditionDependencies.isEmpty()) {
						out.writeln("return getComparisonResults(o)");
						out.indented(() -> {
							out.writeln(".stream()");
							out.writeln(".map(res -> {");
							out.indented(() -> {
								out.writeln("if (!", IS_NULL_OR_EMPTY, "(res.getError())) {");
								out.indented(() -> out.writeln("return ", VALIDATION_FAILURE, "(\"", rosettaName, "\", ", ValidationResult.ValidationType.class, ".TYPE_FORMAT, \"", rosettaName, "\", ", pathId, ", \"\", res.getError());"));
								out.writeln("}");
								out.writeln("return ", VALIDATION_SUCCESS, "(\"", rosettaName, "\", ", ValidationResult.ValidationType.class, ".TYPE_FORMAT, \"", rosettaName, "\", ", pathId, ", \"\");");
							});
							out.writeln("})");
							out.writeln(".collect(", COLLECTORS_TO_LIST, "());");
						});
					} else {
						out.writeln("return ", Streams.class, ".concat(getComparisonResults(o)");
						out.indented(() -> {
							out.writeln(".stream()");
							out.writeln(".map(res -> {");
							out.indented(() -> {
								out.writeln("if (!", IS_NULL_OR_EMPTY, "(res.getError())) {");
								out.indented(() -> out.writeln("return ", VALIDATION_FAILURE, "(\"", rosettaName, "\", ", ValidationResult.ValidationType.class, ".TYPE_FORMAT, \"", rosettaName, "\", ", pathId, ", \"\", res.getError());"));
								out.writeln("}");
								out.writeln("return ", VALIDATION_SUCCESS, "(\"", rosettaName, "\", ", ValidationResult.ValidationType.class, ".TYPE_FORMAT, \"", rosettaName, "\", ", pathId, ", \"\");");
							});
							out.writeln("}),");
							out.writeln("runConditions(", pathId, ", o).stream()");
						});
						out.writeln(")");
						out.writeln(".collect(", COLLECTORS_TO_LIST, "());");
					}
				});
				out.writeln("}");
			});
			out.newline();
			out.write("}");
		};
	}

	private List<CodeRenderer> checkTypeFormat(JavaPojoInterface javaType, RAttribute attr) {
		List<CodeRenderer> checks = new ArrayList<>();
		RType t = typeSystem.stripFromTypeAliases(attr.getRMetaAnnotatedType().getRType());
		if (t instanceof RStringType stringType) {
			if (!stringType.equals(builtinTypes.UNCONSTRAINED_STRING)) {
				int min = stringType.getInterval().getMinBound();
				CodeRenderer max = optional(stringType.getInterval().getMax());
				CodeRenderer pattern = optionalPattern(stringType.getPattern());
				JavaExpression attributeValue = getAttributeValue(javaType, attr);
				checks.add(out -> out.write(CHECK_STRING, "(\"", attr.getName(), "\", ", attributeValue, ", ", min, ", ", max, ", ", pattern, ")"));
			}
		} else if (t instanceof RNumberType numberType) {
			if (!numberType.equals(builtinTypes.UNCONSTRAINED_NUMBER)) {
				CodeRenderer digits = optional(numberType.getDigits());
				CodeRenderer fractionalDigits = optional(numberType.getFractionalDigits());
				CodeRenderer min = optionalBigDecimal(numberType.getInterval().getMin());
				CodeRenderer max = optionalBigDecimal(numberType.getInterval().getMax());
				JavaExpression attributeValue = getAttributeValue(javaType, attr);
				checks.add(out -> out.write(CHECK_NUMBER, "(\"", attr.getName(), "\", ", attributeValue, ", ", digits, ", ", fractionalDigits, ", ", min, ", ", max, ")"));
			}
		}
		return checks;
	}

	private JavaStatement checkTypeConditions(JavaPojoInterface javaType, RAttribute attr, AliasHierarchy hierarchy, GeneratedIdentifier pathId, JavaVariable instanceVar, GeneratedIdentifier resultsId, JavaStatementScope scope) {
		if (!attr.isMulti()) {
			JavaBlock conditionCalls = JavaBlock.EMPTY;
			for (RAliasType alias : hierarchy.getAliases()) {
				for (Condition condition : alias.getConditions()) {
					JavaConditionInterface conditionClass = typeTranslator.toConditionJavaClass(condition);
					JavaPojoProperty prop = javaType.findProperty(attr.getName(), conditionClass.getInstanceClass());
					CodeRenderer pathCode = out -> out.write(pathId, ".newSubPath(\"", attr.getName(), "\")");
					JavaExpression attributeItemCode = prop.applyGetter(JavaExpression.from(o -> o.write("o"), javaType));
					conditionCalls = conditionCalls.append(
							addConditionValidationResultsCode(resultsId, pathCode, attributeItemCode, alias, conditionClass, scope));
				}
			}
			return conditionCalls;
		} else {
			if (hierarchy.getAliases().stream().anyMatch(alias -> !alias.getConditions().isEmpty())) {
				JavaPojoProperty prop = javaType.findProperty(attr.getName());
				JavaExpression attrVarExpr = prop.applyGetter(JavaExpression.from(o -> o.write("o"), javaType));

				GeneratedIdentifier forIndex = scope.createUniqueIdentifier("i");
				return attrVarExpr.declareAsVariable(true, attr.getName(), scope)
						.complete(attrVar -> {
							JavaBlock forBody = JavaBlock.EMPTY;
							for (RAliasType alias : hierarchy.getAliases()) {
								for (Condition condition : alias.getConditions()) {
									CodeRenderer pathCode = out -> out.write(pathId, ".newSubPath(\"", attr.getName(), "\").withIndex(", forIndex, ")");
									JavaExpression attributeItemCode = JavaExpression.from(o -> o.write(attrVar, ".get(", forIndex, ")"), typeUtil.getItemType(attrVar.getExpressionType()));
									forBody = forBody.append(
											addConditionValidationResultsCode(resultsId, pathCode, attributeItemCode, alias, typeTranslator.toConditionJavaClass(condition), scope));
								}
							}
							return new JavaIfThenStatement(
									JavaExpression.from(o -> o.write(attrVar, " != null"), JavaPrimitiveType.BOOLEAN),
									new JavaForLoop(
											new JavaLocalVariableDeclarationStatement(false, JavaPrimitiveType.INT, forIndex, JavaExpression.from(o -> o.write("0"), JavaPrimitiveType.INT)),
											JavaExpression.from(o -> o.write(forIndex, " < ", attrVar, ".size()"), JavaPrimitiveType.BOOLEAN),
											JavaExpression.from(o -> o.write(forIndex, "++"), JavaPrimitiveType.INT),
											forBody
									)
							);
						});
			}
			return JavaBlock.EMPTY;
		}
	}

	private JavaStatement addConditionValidationResultsCode(GeneratedIdentifier resultsId, CodeRenderer pathCode, JavaExpression attributeItemCode, RAliasType alias, JavaConditionInterface conditionClass, JavaStatementScope scope) {
		GeneratedIdentifier conditionVar = scope.getIdentifierOrThrow(identifierRepresentationService.toDependencyInstance(conditionClass));
		List<JavaStatementBuilder> arguments = new ArrayList<>();

		arguments.add(JavaExpression.from(pathCode, JavaType.from(RosettaPath.class)));
		arguments.add(typeCoercionService.addCoercions(attributeItemCode, conditionClass.getInstanceClass(), scope));
		conditionClass.getParameters().forEach((param, paramType) ->
				arguments.add(typeCoercionService.addCoercions(interpreterValueJavaConverter.convertValueToJava(alias.getArguments().get(param)), paramType, scope)));

		return JavaStatementBuilder.invokeMethod(
				arguments,
				args -> JavaExpression.from(o -> o.write(resultsId, ".addAll(", conditionVar, ".getValidationResults(", args, "))"), JavaPrimitiveType.VOID),
				scope
		).completeAsExpressionStatement();
	}

	private JavaExpression getAttributeValue(JavaPojoInterface javaType, RAttribute attr) {
		JavaPojoProperty prop = javaType.findProperty(attr.getName());
		JavaExpression propCode = prop.applyGetter(JavaExpression.from(o -> o.write("o"), javaType));
		JavaType propType = propCode.getExpressionType();
		JavaType propItemType = typeUtil.getItemType(propType);
		if (propItemType instanceof RJavaWithMetaValue withMetaType) {
			if (typeUtil.isList(propType)) {
				return JavaExpression.from(o -> o.write(propCode, ".stream().map(", withMetaType, "::getValue).collect(", Collectors.class, ".toList())"), typeUtil.wrap(typeUtil.LIST, withMetaType.getValueType()));
			} else {
				return JavaExpression.from(o -> o.write(propCode, ".getValue()"), withMetaType.getValueType());
			}
		} else {
			return propCode;
		}
	}

	private CodeRenderer optional(Optional<?> v) {
		if (v.isPresent()) {
			return out -> out.write(OPTIONAL_OF, "(", v.get(), ")");
		} else {
			return out -> out.write(OPTIONAL_EMPTY, "()");
		}
	}

	private CodeRenderer optionalPattern(Optional<Pattern> v) {
		if (v.isPresent()) {
			return out -> out.write(OPTIONAL_OF, "(", Pattern.class, ".compile(\"", StringEscapeUtils.escapeJava(v.get().toString()), "\"))");
		} else {
			return out -> out.write(OPTIONAL_EMPTY, "()");
		}
	}

	private CodeRenderer optionalBigDecimal(Optional<BigDecimal> v) {
		if (v.isPresent()) {
			return out -> out.write(OPTIONAL_OF, "(new ", BigDecimal.class, "(\"", StringEscapeUtils.escapeJava(v.get().toString()), "\"))");
		} else {
			return out -> out.write(OPTIONAL_EMPTY, "()");
		}
	}
}
