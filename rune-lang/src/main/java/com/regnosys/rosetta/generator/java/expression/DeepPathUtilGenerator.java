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

package com.regnosys.rosetta.generator.java.expression;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.eclipse.xtend2.lib.StringConcatenationClient;

import com.google.common.collect.Lists;
import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaIfThenElseBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.expression.ExistsModifier;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.utils.DeepFeatureCallUtil;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class DeepPathUtilGenerator extends FluentRObjectJavaClassGenerator<RDataType, JavaClass<?>> {
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private DeepFeatureCallUtil deepFeatureCallUtil;
	@Inject
	private ExpressionGenerator expressionGenerator;
	@Inject
	private TypeCoercionService typeCoercionService;
	@Inject
	private JavaIdentifierRepresentationService identifierService;
	@Inject
	private JavaTypeUtil typeUtil;
	@Inject
	private RObjectFactory rObjectFactory;

	@Override
	protected Stream<? extends RDataType> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.filter(Data.class::isInstance)
				.map(Data.class::cast)
				.map(rObjectFactory::buildRDataType)
				.filter(type -> deepFeatureCallUtil.isEligibleForDeepFeatureCall(type) || hasImpliedKey(type));
	}

	@Override
	protected JavaClass<?> createTypeRepresentation(RDataType choiceType) {
		return typeTranslator.toDeepPathUtilJavaClass(choiceType);
	}

	@Override
	protected CodeRenderer generateClass(RDataType choiceType, JavaClass<?> javaClass, String version, JavaClassScope classScope) {
		Collection<RAttribute> deepFeatures = deepFeatureCallUtil.findDeepFeatures(choiceType);
		Set<JavaClass<?>> dependencies = new LinkedHashSet<>();
		Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeatures = computeRecursiveDeepFeatures(choiceType, deepFeatures, dependencies);

		boolean hasImpliedKey = hasImpliedKey(choiceType);
		if (hasImpliedKey) {
			// `metaChooseKey`/`metaChooseGlobalKey` delegate to the same methods on each nested choice
			// option, so we depend on their utils.
			choiceType.getAllAttributes().stream()
					.map(attr -> attr.getRMetaAnnotatedType().getRType())
					.filter(RChoiceType.class::isInstance)
					.map(RChoiceType.class::cast)
					.forEach(option -> dependencies.add(typeTranslator.toDeepPathUtilJavaClass(option.asRDataType())));
		}
		dependencies.forEach(dependency -> classScope.createIdentifier(identifierService.toDependencyInstance(dependency), uncapitalize(dependency.getSimpleName())));

		List<GeneratedMethod> methods = new ArrayList<>();
		deepFeatures.forEach(deepFeature -> methods.add(createMethod(choiceType, "choose" + capitalize(deepFeature.getName()), classScope,
				(input, scope) -> deepFeatureToStatement(choiceType, input, deepFeature, recursiveDeepFeatures, scope))));
		if (hasImpliedKey) {
			methods.add(createMethod(choiceType, "metaChooseKey", classScope,
					(input, scope) -> metaKeyToStatement(choiceType, input, "key", "metaChooseKey", scope)));
			methods.add(createMethod(choiceType, "metaChooseGlobalKey", classScope,
					(input, scope) -> metaKeyToStatement(choiceType, input, "globalKey", "metaChooseGlobalKey", scope)));
		}

		return renderClass(javaClass, classScope, dependencies, methods);
	}

	private boolean hasImpliedKey(RDataType type) {
		return type.asChoiceType().map(RChoiceType::hasImpliedKey).orElse(false);
	}

	private Map<RAttribute, Map<RAttribute, Boolean>> computeRecursiveDeepFeatures(RDataType choiceType, Collection<RAttribute> deepFeatures, Set<JavaClass<?>> dependencies) {
		Map<RAttribute, Map<RAttribute, Boolean>> result = new LinkedHashMap<>();
		for (RAttribute attr : choiceType.getAllAttributes()) {
			RType attrType = normalizeChoiceType(attr.getRMetaAnnotatedType().getRType());
			Map<RAttribute, Boolean> recursiveByDeepFeature = new LinkedHashMap<>();
			for (RAttribute deepFeature : deepFeatures) {
				boolean recursive = false;
				if (attrType instanceof RDataType dataType && deepFeatureCallUtil.findDeepFeatureMap(dataType).containsKey(deepFeature.getName())) {
					dependencies.add(typeTranslator.toDeepPathUtilJavaClass(dataType));
					recursive = true;
				}
				recursiveByDeepFeature.put(deepFeature, recursive);
			}
			result.put(attr, recursiveByDeepFeature);
		}
		return result;
	}

	private GeneratedMethod createMethod(RDataType choiceType, String methodName, JavaClassScope classScope, BiFunction<JavaVariable, JavaStatementScope, JavaStatementBuilder> bodyBuilder) {
		JavaMethodScope methodScope = classScope.createMethodScope(methodName);
		JavaVariable inputParameter = new JavaVariable(methodScope.createUniqueIdentifier(uncapitalize(choiceType.getName())), typeTranslator.toJavaReferenceType(choiceType));
		JavaStatementBuilder methodBody = bodyBuilder.apply(inputParameter, methodScope.getBodyScope());
		return new GeneratedMethod(methodName, methodBody.getExpressionType(), inputParameter, methodBody.completeAsReturn());
	}

	private JavaStatementBuilder deepFeatureToStatement(RDataType choiceType, JavaVariable inputParameter, RAttribute deepFeature, Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeatures, JavaStatementScope scope) {
		JavaType deepFeatureType = typeTranslator.toMetaJavaType(deepFeature);
		JavaStatementBuilder chain = buildOptionChain(choiceType, inputParameter,
				(option, optionValue) -> deepFeatureValue(optionValue, option, deepFeature, deepFeatureType, recursiveDeepFeatures, scope),
				scope);
		return typeCoercionService.addCoercions(chain, deepFeatureType, scope);
	}

	/**
	 * Generates the body of {@code metaChooseKey}/{@code metaChooseGlobalKey}, which returns the
	 * (global) key of whichever option of the choice is populated.
	 */
	private JavaStatementBuilder metaKeyToStatement(RDataType choiceType, JavaVariable choiceParameter, String metaName, String nestedMethodName, JavaStatementScope scope) {
		JavaStatementBuilder chain = buildOptionChain(choiceType, choiceParameter,
				(option, optionValue) -> optionValue.mapExpression(it -> metaKeyAccess(option, it, metaName, nestedMethodName, scope)),
				scope);
		return typeCoercionService.addCoercions(chain, typeUtil.STRING, scope);
	}

	/**
	 * Builds the shared if-then-else chain over choice options (back-to-front). For each option, if it is
	 * populated the result is computed by {@code readStrategy}; otherwise the chain falls through to the next
	 * option. Callers are responsible for adding the final coercion to the desired result type.
	 */
	private JavaStatementBuilder buildOptionChain(RDataType choiceType, JavaVariable inputParameter, BiFunction<RAttribute, JavaExpression, JavaStatementBuilder> readStrategy, JavaStatementScope scope) {
		List<RAttribute> options = new ArrayList<>(choiceType.getAllAttributes());
		JavaStatementBuilder elseBranch = JavaLiteral.NULL;
		for (RAttribute option : Lists.reverse(options)) {
			JavaStatementBuilder nextOptionBranch = elseBranch;
			elseBranch = expressionGenerator
					.attributeCall(inputParameter, RMetaAnnotatedType.withNoMeta(choiceType), option, false, typeTranslator.toMetaJavaType(option), scope)
					.declareAsVariable(true, uncapitalize(option.getName()), scope)
					.mapExpression(optionValue -> typeCoercionService
							.addCoercions(
									expressionGenerator.exists(optionValue, ExistsModifier.NONE, scope).collapseToSingleExpression(scope),
									JavaPrimitiveType.BOOLEAN,
									scope)
							.mapExpression(optionIsPopulated -> new JavaIfThenElseBuilder(
									optionIsPopulated,
									readStrategy.apply(option, optionValue),
									nextOptionBranch,
									typeUtil)));
		}
		return elseBranch;
	}

	private JavaStatementBuilder deepFeatureValue(JavaStatementBuilder attrVar, RAttribute attr, RAttribute deepFeature, JavaType deepFeatureType, Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeatures, JavaStatementScope scope) {
		if (deepFeatureCallUtil.match(deepFeature, attr)) {
			return attrVar;
		}
		RMetaAnnotatedType attrMetaType = attr.getRMetaAnnotatedType();
		RType attrType = normalizeChoiceType(attrMetaType.getRType());
		boolean needsToGoDownDeeper = recursiveDeepFeatures.get(attr).get(deepFeature);
		RAttribute actualFeature;
		if (needsToGoDownDeeper || !(attrType instanceof RDataType dataType)) {
			actualFeature = deepFeature;
		} else {
			actualFeature = dataType.getAllAttributes().stream()
					.filter(candidate -> candidate.getName().equals(deepFeature.getName()))
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("No attribute named " + deepFeature.getName() + " found on " + dataType));
		}
		return expressionGenerator.attributeCall(attrVar, attrMetaType, actualFeature, needsToGoDownDeeper, deepFeatureType, scope);
	}

	/**
	 * Builds the expression that reads the key from a single, populated choice option: a nested choice option
	 * delegates to that choice's generated {@code metaChooseKey}/{@code metaChooseGlobalKey}, while a leaf option
	 * reads the key straight from its metadata.
	 */
	private JavaStatementBuilder metaKeyAccess(RAttribute option, JavaExpression optionMapper, String metaName, String nestedMethodName, JavaStatementScope scope) {
		RType optionType = option.getRMetaAnnotatedType().getRType();
		if (optionType instanceof RChoiceType choice) {
			JavaClass<?> nestedUtil = typeTranslator.toDeepPathUtilJavaClass(choice.asRDataType());
			GeneratedIdentifier nestedChoice = scope.lambdaScope().createUniqueIdentifier("nestedChoice");
			GeneratedIdentifier nestedUtilInstance = scope.getIdentifierOrThrow(identifierService.toDependencyInstance(nestedUtil));
			return JavaExpression.from(out -> out.write(
					optionMapper, ".map(\"", nestedMethodName, "\", ", nestedChoice, " -> ",
					nestedUtilInstance, ".", nestedMethodName, "(", nestedChoice, ")).get()"),
					typeUtil.STRING);
		}
		return leafMetaKeyAccess(optionMapper, metaName, option.getRMetaAnnotatedType().hasAttributeMeta(), scope);
	}

	/**
	 * Reads the key from a leaf option via the shared meta-chain builder. When the option additionally carries
	 * field-level metadata its value is unwrapped via {@code getValue()} before reading the metadata.
	 */
	private JavaStatementBuilder leafMetaKeyAccess(JavaStatementBuilder optionMapper, String metaName, boolean hasFieldMeta, JavaStatementScope scope) {
		JavaExpression metaChain = JavaExpression.from(expressionGenerator.buildMetaChain(metaName, scope), typeUtil.STRING);
		if (hasFieldMeta) {
			GeneratedIdentifier fieldWithMeta = scope.lambdaScope().createUniqueIdentifier("fieldWithMeta");
			return optionMapper.mapExpression(it -> JavaExpression.from(out -> out.write(
					it, ".map(\"getValue\", ", fieldWithMeta, " -> ", fieldWithMeta, ".getValue())", metaChain, ".get()"),
					typeUtil.STRING));
		}
		return optionMapper.mapExpression(it -> JavaExpression.from(out -> out.write(it, metaChain, ".get()"), typeUtil.STRING));
	}

	private CodeRenderer renderClass(JavaClass<?> javaClass, JavaClassScope classScope, Set<JavaClass<?>> dependencies, List<GeneratedMethod> methods) {
		return out -> {
			out.writeln("public class ", javaClass, " {");
			out.indented(() -> {
				if (!dependencies.isEmpty()) {
					renderDependencies(out, javaClass, classScope, dependencies);
					out.newline();
				}
				for (GeneratedMethod method : methods) {
					out.writeln("public ", method.returnType(), " ", method.name(), "(", method.inputParameter().getExpressionType(), " ", method.inputParameter(), ") ", method.body());
					out.newline();
				}
			});
			out.write("}");
		};
	}

	private void renderDependencies(CodeWriter out, JavaClass<?> javaClass, JavaClassScope classScope, Set<JavaClass<?>> dependencies) {
		for (JavaClass<?> dependency : dependencies) {
			out.writeln("private final ", dependency, " ", dependencyIdentifier(classScope, dependency), ";");
		}
		out.newline();
		out.writeln("@", javax.inject.Inject.class);
		out.write("public ", javaClass, "(");
		out.join(dependencies, ", ", dependency -> out.write(dependency, " ", dependencyIdentifier(classScope, dependency)));
		out.writeln(") {");
		out.indented(() -> dependencies.forEach(dependency -> {
			GeneratedIdentifier dependencyId = dependencyIdentifier(classScope, dependency);
			out.writeln("this.", dependencyId, " = ", dependencyId, ";");
		}));
		out.writeln("}");
	}

	private GeneratedIdentifier dependencyIdentifier(JavaClassScope classScope, JavaClass<?> dependency) {
		return classScope.getIdentifierOrThrow(identifierService.toDependencyInstance(dependency));
	}

	private RType normalizeChoiceType(RType type) {
		if (type instanceof RChoiceType choiceType) {
			return choiceType.asRDataType();
		}
		return type;
	}

	private record GeneratedMethod(
			String name,
			JavaType returnType,
			JavaVariable inputParameter,
			JavaStatement body) {
	}
}
