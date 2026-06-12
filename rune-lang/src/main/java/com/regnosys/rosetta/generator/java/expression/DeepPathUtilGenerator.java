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
import java.util.stream.Stream;

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

/**
 * Generates a `DeepPathUtil` class for each choice-like type, containing a
 * `choose<Feature>` method per deep feature which selects the feature's value
 * from whichever alternative is set.
 *
 * <p>This is the first generator migrated from Xtend templates to the fluent
 * {@link CodeRenderer} API, and serves as the blueprint for migrating the others.
 * Expression code is built with the existing {@link ExpressionGenerator} and
 * {@link TypeCoercionService} machinery, so the generated output keeps the exact
 * semantics of the original Xtend implementation.
 */
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
				.filter(deepFeatureCallUtil::isEligibleForDeepFeatureCall);
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
		dependencies.forEach(dependency -> classScope.createIdentifier(identifierService.toDependencyInstance(dependency), uncapitalize(dependency.getSimpleName())));

		// All identifiers and method bodies are created up front: the returned
		// renderer must be free of side effects, as it is rendered multiple times.
		List<DeepFeatureMethod> methods = deepFeatures.stream()
				.map(deepFeature -> createMethod(choiceType, deepFeature, recursiveDeepFeatures, classScope))
				.toList();

		return out -> renderClass(out, javaClass, classScope, dependencies, methods);
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

	private DeepFeatureMethod createMethod(RDataType choiceType, RAttribute deepFeature, Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeatures, JavaClassScope classScope) {
		String methodName = "choose" + capitalize(deepFeature.getName());
		JavaMethodScope methodScope = classScope.createMethodScope(methodName);
		JavaVariable inputParameter = new JavaVariable(methodScope.createUniqueIdentifier(uncapitalize(choiceType.getName())), typeTranslator.toJavaReferenceType(choiceType));
		JavaStatementBuilder methodBody = deepFeatureToStatement(choiceType, inputParameter, deepFeature, recursiveDeepFeatures, methodScope.getBodyScope());
		return new DeepFeatureMethod(methodName, methodBody.getExpressionType(), inputParameter, methodBody.completeAsReturn());
	}

	private JavaStatementBuilder deepFeatureToStatement(RDataType choiceType, JavaVariable inputParameter, RAttribute deepFeature, Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeatures, JavaStatementScope scope) {
		List<RAttribute> attrs = new ArrayList<>(choiceType.getAllAttributes());
		JavaType deepFeatureType = typeTranslator.toMetaJavaType(deepFeature);
		JavaStatementBuilder acc = JavaLiteral.NULL;
		for (RAttribute attr : Lists.reverse(attrs)) {
			JavaStatementBuilder currAcc = acc;
			acc = expressionGenerator
					.attributeCall(inputParameter, RMetaAnnotatedType.withNoMeta(choiceType), attr, false, typeTranslator.toMetaJavaType(attr), scope)
					.declareAsVariable(true, uncapitalize(attr.getName()), scope)
					.mapExpression(attrVar -> typeCoercionService
							.addCoercions(
									expressionGenerator.exists(attrVar, ExistsModifier.NONE, scope).collapseToSingleExpression(scope),
									JavaPrimitiveType.BOOLEAN,
									scope)
							.mapExpression(condition -> new JavaIfThenElseBuilder(
									condition,
									deepFeatureValue(attrVar, attr, deepFeature, deepFeatureType, recursiveDeepFeatures, scope),
									currAcc,
									typeUtil)));
		}
		return typeCoercionService.addCoercions(acc, deepFeatureType, scope);
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

	private void renderClass(CodeWriter out, JavaClass<?> javaClass, JavaClassScope classScope, Set<JavaClass<?>> dependencies, List<DeepFeatureMethod> methods) {
		out.writeln("public class ", javaClass, " {");
		out.indented(() -> {
			if (!dependencies.isEmpty()) {
				renderDependencies(out, javaClass, classScope, dependencies);
				out.newline();
			}
			for (DeepFeatureMethod method : methods) {
				out.writeln("public ", method.returnType(), " ", method.name(), "(", method.inputParameter().getExpressionType(), " ", method.inputParameter(), ") ", method.body());
				out.newline();
			}
		});
		out.write("}");
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

	private record DeepFeatureMethod(
			String name,
			JavaType returnType,
			JavaVariable inputParameter,
			JavaStatement body) {
	}
}
