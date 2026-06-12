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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty;
import com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.utils.DeepFeatureCallUtil;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class DeepPathUtilGeneratorFluent extends FluentRObjectJavaClassGenerator<RDataType, JavaClass<?>> {
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private DeepFeatureCallUtil deepFeatureCallUtil;
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
		List<RAttribute> deepFeatures = new ArrayList<>(deepFeatureCallUtil.findDeepFeatures(choiceType));
		Set<JavaClass<?>> dependencies = new LinkedHashSet<>();
		Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeatures = computeRecursiveDeepFeatures(choiceType, deepFeatures, dependencies);
		dependencies.forEach(dependency -> classScope.createIdentifier(identifierService.toDependencyInstance(dependency), lowerFirst(dependency.getSimpleName())));

		List<MethodModel> methods = deepFeatures.stream()
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

	private MethodModel createMethod(RDataType choiceType, RAttribute deepFeature, Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeatures, JavaClassScope classScope) {
		String methodName = chooseMethodName(deepFeature);
		JavaMethodScope methodScope = classScope.createMethodScope(methodName);
		GeneratedIdentifier inputId = methodScope.createUniqueIdentifier(lowerFirst(choiceType.getName()));
		JavaStatementScope bodyScope = methodScope.getBodyScope();

		JavaType inputType = typeTranslator.toJavaReferenceType(choiceType);
		JavaType returnType = typeTranslator.toMetaJavaType(deepFeature);
		List<BranchModel> branches = choiceType.getAllAttributes().stream()
				.map(attr -> createBranch(attr, deepFeature, recursiveDeepFeatures, bodyScope))
				.toList();
		return new MethodModel(methodName, deepFeature, inputType, returnType, inputId, branches);
	}

	private BranchModel createBranch(RAttribute attr, RAttribute deepFeature, Map<RAttribute, Map<RAttribute, Boolean>> recursiveDeepFeatures, JavaStatementScope bodyScope) {
		JavaType attrType = typeTranslator.toMetaJavaType(attr);
		GeneratedIdentifier attrId = bodyScope.createUniqueIdentifier(lowerFirst(attr.getName()));
		boolean recursive = recursiveDeepFeatures.get(attr).get(deepFeature);
		RAttribute actualFeature = findActualFeature(attr, deepFeature, recursive);
		return new BranchModel(attr, attrType, attrId, recursive, actualFeature);
	}

	private RAttribute findActualFeature(RAttribute attr, RAttribute deepFeature, boolean recursive) {
		RType attrType = normalizeChoiceType(attr.getRMetaAnnotatedType().getRType());
		if (recursive || !(attrType instanceof RDataType dataType)) {
			return deepFeature;
		}
		return dataType.getAllAttributes().stream()
				.filter(candidate -> candidate.getName().equals(deepFeature.getName()))
				.findFirst()
				.orElse(deepFeature);
	}

	private void renderClass(CodeWriter out, JavaClass<?> javaClass, JavaClassScope classScope, Set<JavaClass<?>> dependencies, List<MethodModel> methods) {
		out.writeln("public class ", javaClass, " {");
		out.indented(() -> {
			if (!dependencies.isEmpty()) {
				renderDependencies(out, javaClass, classScope, dependencies);
				if (!methods.isEmpty()) {
					out.newline();
				}
			}
			for (int i = 0; i < methods.size(); i++) {
				renderMethod(out, methods.get(i), classScope);
				if (i < methods.size() - 1) {
					out.newline();
				}
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

	private void renderMethod(CodeWriter out, MethodModel method, JavaClassScope classScope) {
		out.write("public ", method.returnType(), " ", method.name(), "(", method.inputType(), " ", method.inputId(), ") ");
		out.writeln("{");
		out.indented(() -> {
			out.writeln("if (", method.inputId(), " == null) {");
			out.indented(() -> out.writeln("return null;"));
			out.writeln("}");

			Expression input = new Expression(writer -> writer.write(method.inputId()), method.inputType());
			for (BranchModel branch : method.branches()) {
				renderBranch(out, input, method, branch, classScope);
			}
			out.writeln("return null;");
		});
		out.write("}");
	}

	private void renderBranch(CodeWriter out, Expression input, MethodModel method, BranchModel branch, JavaClassScope classScope) {
		Expression attrValue = new Expression(writer -> writer.write(branch.attrId()), branch.attrType());
		out.write("final ", branch.attrType(), " ", branch.attrId(), " = ");
		renderGetter(out, input, asPojo(method.inputType()), branch.attr(), branch.attrType());
		out.writeln(";");
		out.write("if (");
		renderExists(out, attrValue);
		out.writeln(") {");
		out.indented(() -> {
			out.write("return ");
			renderDeepFeatureValue(out, attrValue, method, branch, classScope);
			out.writeln(";");
		});
		out.writeln("}");
	}

	private void renderDeepFeatureValue(CodeWriter out, Expression attrValue, MethodModel method, BranchModel branch, JavaClassScope classScope) {
		if (deepFeatureCallUtil.match(method.deepFeature(), branch.attr())) {
			attrValue.render(out);
			return;
		}

		RType attrType = normalizeChoiceType(branch.attr().getRMetaAnnotatedType().getRType());
		if (!(attrType instanceof RDataType dataType)) {
			out.write("null");
			return;
		}

		Expression receiver = unwrapMetaValue(attrValue);
		if (branch.recursive()) {
			JavaClass<?> dependency = typeTranslator.toDeepPathUtilJavaClass(dataType);
			out.write(dependencyIdentifier(classScope, dependency), ".", chooseMethodName(method.deepFeature()), "(");
			receiver.render(out);
			out.write(")");
		} else {
			renderNullSafeGetter(out, attrValue, receiver, asPojo(typeTranslator.toJavaReferenceType(dataType)), branch.actualFeature(), method.returnType());
		}
	}

	private void renderNullSafeGetter(CodeWriter out, Expression original, Expression receiver, JavaPojoInterface receiverType, RAttribute attr, JavaType expectedType) {
		if (original.type() instanceof RJavaWithMetaValue) {
			receiver.render(out);
			out.write(" == null ? null : ");
		}
		renderGetter(out, receiver, receiverType, attr, expectedType);
	}

	private void renderGetter(CodeWriter out, Expression receiver, JavaPojoInterface receiverType, RAttribute attr, JavaType expectedType) {
		JavaPojoProperty property = receiverType.findProperty(attr.getName(), expectedType);
		receiver.render(out);
		out.write(".", property.getOperationName(JavaPojoPropertyOperationType.GET), "()");
	}

	private void renderExists(CodeWriter out, Expression expression) {
		expression.render(out);
		out.write(" != null");
		if (typeUtil.isList(expression.type())) {
			out.write(" && !");
			expression.render(out);
			out.write(".isEmpty()");
		}
	}

	private Expression unwrapMetaValue(Expression expression) {
		if (expression.type() instanceof RJavaWithMetaValue metaType) {
			return new Expression(out -> {
				expression.render(out);
				out.write(".getValue()");
			}, metaType.getValueType());
		}
		return expression;
	}

	private JavaPojoInterface asPojo(JavaType type) {
		if (type instanceof JavaPojoInterface pojo) {
			return pojo;
		}
		throw new IllegalArgumentException("Expected a Java POJO interface, but got " + type);
	}

	private RType normalizeChoiceType(RType type) {
		if (type instanceof RChoiceType choiceType) {
			return choiceType.asRDataType();
		}
		return type;
	}

	private String chooseMethodName(RAttribute deepFeature) {
		return "choose" + upperFirst(deepFeature.getName());
	}

	private String lowerFirst(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		return Character.toLowerCase(value.charAt(0)) + value.substring(1);
	}

	private String upperFirst(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}

	private record Expression(CodeRenderer renderer, JavaType type) {
		void render(CodeWriter out) {
			renderer.render(out);
		}
	}

	private record MethodModel(
			String name,
			RAttribute deepFeature,
			JavaType inputType,
			JavaType returnType,
			GeneratedIdentifier inputId,
			List<BranchModel> branches) {
	}

	private record BranchModel(
			RAttribute attr,
			JavaType attrType,
			GeneratedIdentifier attrId,
			boolean recursive,
			RAttribute actualFeature) {
	}
}
