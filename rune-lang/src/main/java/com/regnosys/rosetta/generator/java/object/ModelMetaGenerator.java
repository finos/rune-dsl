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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.types.JavaConditionInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil;
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.utils.RosettaConfigExtension;
import com.rosetta.model.lib.annotations.RosettaMeta;
import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
import com.rosetta.model.lib.qualify.QualifyResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import com.rosetta.util.types.JavaClass;

import jakarta.inject.Inject;

public class ModelMetaGenerator extends FluentRObjectJavaClassGenerator<RDataType, RGeneratedJavaClass<?>> {
	@Inject
	private RosettaConfigExtension confExt;
	@Inject
	private RosettaFunctionExtensions funcExt;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private ModelGeneratorUtil modelGeneratorUtil;
	@Inject
	private RObjectFactory rObjectFactory;

	@Override
	protected Stream<? extends RDataType> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.filter(Data.class::isInstance)
				.map(Data.class::cast)
				.map(rObjectFactory::buildRDataType);
	}

	@Override
	protected RGeneratedJavaClass<?> createTypeRepresentation(RDataType t) {
		return typeTranslator.toJavaMetaDataClass(typeTranslator.toJavaReferenceType(t));
	}

	@Override
	protected CodeRenderer generateClass(RDataType t, RGeneratedJavaClass<?> metaClass, String version, JavaClassScope scope) {
		JavaPojoInterface dataClass = typeTranslator.toJavaType(t);
		JavaClass<?> validator = typeTranslator.toValidatorClass(dataClass);
		JavaClass<?> typeFormatValidator = typeTranslator.toTypeFormatValidatorClass(dataClass);
		JavaClass<?> onlyExistsValidator = typeTranslator.toOnlyExistsValidatorClass(dataClass);
		Set<RosettaModel> models = t.getEObject().eResource().getResourceSet().getResources().stream()
				.filter(resource -> !resource.getContents().isEmpty())
				.map(resource -> (RosettaModel) resource.getContents().get(0))
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<Function> qualifierFuncs = qualifyFuncs(t.getEObject(), models);
		List<JavaConditionInterface> conditionClasses = t.getAllSuperTypes().stream()
				.flatMap(superType -> superType.getEObject().getConditions().stream())
				.map(typeTranslator::toConditionJavaClass)
				.toList();
		return out -> {
			out.write(modelGeneratorUtil.emptyJavadocWithVersion(version));
			out.writeln("@", RosettaMeta.class, "(model=", dataClass, ".class)");
			out.writeln("public ", metaClass.asClassDeclaration(), " {");
			out.indented(() -> {
				out.newline();
				out.writeln("@Override");
				out.writeln("public ", List.class, "<", Validator.class, "<? super ", dataClass, ">> dataRules(", ValidatorFactory.class, " factory) {");
				out.indented(() -> {
					out.writeln("return ", Arrays.class, ".asList(");
					out.indented(() -> out.join(conditionClasses, ",\n", conditionClass ->
							out.write("factory.<", conditionClass.getInstanceClass(), ">create(", conditionClass, ".class)")));
					if (!conditionClasses.isEmpty()) {
						out.newline();
					}
					out.writeln(");");
				});
				out.writeln("}");
				out.newline();
				out.writeln("@Override");
				// java.util.function.Function stays qualified: it clashes with the imported Rune Function
				out.writeln("public ", List.class, "<", java.util.function.Function.class, "<? super ", dataClass, ", ", QualifyResult.class, ">> getQualifyFunctions(", QualifyFunctionFactory.class, " factory) {");
				out.indented(() -> {
					if (!qualifierFuncs.isEmpty()) {
						out.writeln("return ", Arrays.class, ".asList(");
						out.indented(() -> out.join(qualifierFuncs, ",\n", qf ->
								out.write("factory.<", dataClass, ">create(", typeTranslator.toFunctionJavaClass(qf), ".class)")));
						out.newline();
						out.writeln(");");
					} else {
						out.writeln("return ", Collections.class, ".emptyList();");
					}
				});
				out.writeln("}");
				out.newline();
				out.writeln("@Override");
				out.writeln("public ", Validator.class, "<? super ", dataClass, "> validator(", ValidatorFactory.class, " factory) {");
				out.indented(() -> out.writeln("return factory.<", dataClass, ">create(", validator, ".class);"));
				out.writeln("}");
				out.newline();
				out.writeln("@Override");
				out.writeln("public ", Validator.class, "<? super ", dataClass, "> typeFormatValidator(", ValidatorFactory.class, " factory) {");
				out.indented(() -> out.writeln("return factory.<", dataClass, ">create(", typeFormatValidator, ".class);"));
				out.writeln("}");
				out.newline();
				out.writeln("@Deprecated");
				out.writeln("@Override");
				out.writeln("public ", Validator.class, "<? super ", dataClass, "> validator() {");
				out.indented(() -> out.writeln("return new ", validator, "();"));
				out.writeln("}");
				out.newline();
				out.writeln("@Deprecated");
				out.writeln("@Override");
				out.writeln("public ", Validator.class, "<? super ", dataClass, "> typeFormatValidator() {");
				out.indented(() -> out.writeln("return new ", typeFormatValidator, "();"));
				out.writeln("}");
				out.newline();
				out.writeln("@Override");
				out.writeln("public ", ValidatorWithArg.class, "<? super ", dataClass, ", ", Set.class, "<String>> onlyExistsValidator() {");
				out.indented(() -> out.writeln("return new ", onlyExistsValidator, "();"));
				out.writeln("}");
			});
			out.write("}");
		};
	}

	private Set<Function> qualifyFuncs(Data type, Set<RosettaModel> models) {
		// TODO: make sure this method doesn't need to go through all models in the resource set
		if (!confExt.isRootEventOrProduct(type)) {
			return Collections.emptySet();
		}
		return models.stream()
				.flatMap(model -> model.getElements().stream())
				.filter(Function.class::isInstance)
				.map(Function.class::cast)
				.filter(func -> funcExt.isQualifierFunctionFor(func, type))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
