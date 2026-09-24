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

package com.regnosys.rosetta.generator.java.object;

import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.ADD;
import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.ADD_VALUE;
import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.GET;
import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.GET_OR_CREATE;
import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.SET;
import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.SET_VALUE;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.FluentRObjectJavaClassGenerator;
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService;
import com.regnosys.rosetta.generator.java.labels.LabelProviderGeneratorUtil;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral;
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable;
import com.regnosys.rosetta.generator.java.types.JavaPojoBuilderImpl;
import com.regnosys.rosetta.generator.java.types.JavaPojoBuilderInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoImpl;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty;
import com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.RosettaDataType;
import com.rosetta.model.lib.annotations.RosettaIgnore;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneChoiceType;
import com.rosetta.model.lib.annotations.RuneDataType;
import com.rosetta.model.lib.annotations.RuneIgnore;
import com.rosetta.model.lib.annotations.RuneLabelProvider;
import com.rosetta.model.lib.functions.LabelProvider;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class ModelObjectGenerator extends FluentRObjectJavaClassGenerator<RDataType, JavaPojoInterface> {
	@Inject
	private ModelObjectBoilerPlate boilerPlate;
	@Inject
	private ModelObjectBuilderGenerator builderGenerator;
	@Inject
	private ImportManagerExtension importManager;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private JavaTypeUtil typeUtil;
	@Inject
	private TypeCoercionService coercionService;
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private LabelProviderGeneratorUtil labelProviderUtil;

	@Override
	protected Stream<? extends RDataType> streamObjects(RosettaModel model) {
		return model.getElements().stream()
				.filter(Data.class::isInstance)
				.map(Data.class::cast)
				.map(rObjectFactory::buildRDataType);
	}

	@Override
	protected JavaPojoInterface createTypeRepresentation(RDataType t) {
		return typeTranslator.toJavaReferenceType(t);
	}

	@Override
	protected CodeRenderer generateClass(RDataType t, JavaPojoInterface javaType, String version, JavaClassScope pojoScope) {
		RGeneratedJavaClass<? extends LabelProvider> labelProviderClass = labelProviderUtil.shouldGenerateLabelProvider(t) ? typeTranslator.toLabelProviderJavaClass(t) : null;
		return classBody(javaType, pojoScope, typeTranslator.toJavaMetaDataClass(javaType), version, labelProviderClass);
	}

	public CodeRenderer classBody(JavaPojoInterface javaType, JavaClassScope pojoScope, JavaClass<?> metaType, String version) {
		return classBody(javaType, pojoScope, metaType, version, null);
	}

	public CodeRenderer classBody(JavaPojoInterface javaType, JavaClassScope pojoScope, JavaClass<?> metaType, String version, RGeneratedJavaClass<? extends LabelProvider> labelProviderClass) {
		JavaPojoInterface superInterface = javaType.getSuperPojo();
		boolean extendSuperImpl = superInterface != null && javaType.getOwnProperties().stream().allMatch(JavaPojoProperty::isCompatibleTypeWithParent);
		GeneratedIdentifier metaDataIdentifier = pojoScope.createUniqueIdentifier("metaData");
		JavaPojoBuilderInterface builderInterface = javaType.toBuilderInterface();
		JavaClassScope builderScope = pojoScope.createNestedClassScopeAndRegisterIdentifier(builderInterface);
		JavaPojoImpl implClass = javaType.toImplClass();
		JavaClassScope implScope = pojoScope.createNestedClassScopeAndRegisterIdentifier(implClass);
		JavaPojoBuilderImpl builderImplClass = javaType.toBuilderImplClass();
		JavaClassScope builderImplScope = pojoScope.createNestedClassScopeAndRegisterIdentifier(builderImplClass);
		String modelShortName = javaType.getPackageName().first();
		return out -> {
			out.write(javaType.getJavadoc());
			out.writeln("@", RosettaDataType.class, "(value=", JavaLiteral.STRING(javaType.getRosettaName()), ", builder=", builderImplClass, ".class, version=", JavaLiteral.STRING(javaType.getVersion()), ")");
			out.writeln("@", RuneDataType.class, "(value=", JavaLiteral.STRING(javaType.getRosettaName()), ", model=", JavaLiteral.STRING(modelShortName), ", builder=", builderImplClass, ".class, version=", JavaLiteral.STRING(javaType.getVersion()), ")");
			if (javaType.isChoiceType()) {
				out.writeln("@", RuneChoiceType.class);
			}
			if (labelProviderClass != null) {
				out.writeln("@", RuneLabelProvider.class, "(labelProvider=", labelProviderClass, ".class)");
			}
			out.writeln("public ", javaType.asInterfaceDeclaration(), " {");
			out.indented(() -> {
				out.newline();
				out.writeln(metaType, " ", metaDataIdentifier, " = new ", metaType, "();");
				out.newline();
				renderStartComment(out, "Getter Methods");
				renderPojoInterfaceGetterMethods(out, javaType);
				out.newline();
				renderStartComment(out, "Build Methods");
				renderPojoInterfaceBuilderMethods(out, javaType);
				out.newline();
				renderStartComment(out, "Utility Methods");
				renderPojoInterfaceDefaultOverriddenMethods(out, javaType, metaDataIdentifier);
				out.newline();
				renderStartComment(out, "Builder Interface");
				out.writeln(builderInterface.asInterfaceDeclaration(), " {");
				out.indented(() -> {
					renderPojoBuilderInterfaceGetterMethods(out, javaType);
					renderPojoBuilderInterfaceSetterMethods(out, javaType, builderInterface, javaType, builderScope);
					out.newline();
					out.write(boilerPlate.builderProcessMethod(javaType));
					out.newline();
					out.writeln(builderInterface, " prune();");
				});
				out.writeln("}");
				out.newline();
				renderStartComment(out, "Immutable Implementation of " + javaType.getSimpleName());
				out.writeln(implClass.asClassDeclaration(), " {");
				out.indented(() -> {
					renderRosettaClass(out, javaType, implClass, builderInterface, extendSuperImpl, implScope);
					out.newline();
					out.write(boilerPlate.boilerPlate(javaType, extendSuperImpl, implScope));
				});
				out.writeln("}");
				out.newline();
				renderStartComment(out, "Builder Implementation of " + javaType.getSimpleName());
				out.write(builderGenerator.builderClass(javaType, builderImplClass, builderImplScope));
			});
			out.writeln("}");
		};
	}

	private void renderPojoBuilderInterfaceGetterMethods(CodeWriter out, JavaPojoInterface javaType) {
		for (JavaPojoProperty prop : javaType.getOwnProperties()) {
			if (typeTranslator.isRosettaModelObject(prop.getType())) {
				JavaType builderType = builderGenerator.toBuilderTypeSingle(prop);
				if (!typeUtil.isList(prop.getType())) {
					out.writeln(builderType, " ", prop.getOperationName(GET_OR_CREATE), "();");
					out.writeln("@Override");
					out.writeln(builderType, " ", prop.getOperationName(GET), "();");
				} else {
					out.writeln(builderType, " ", prop.getOperationName(GET_OR_CREATE), "(int index);");
					out.writeln("@Override");
					out.writeln(List.class, "<? extends ", builderType, "> ", prop.getOperationName(GET), "();");
				}
			}
		}
	}

	private void renderSetterMethod(CodeWriter out, JavaPojoProperty prop, JavaType mainBuilderType, JavaPojoPropertyOperationType operationType, boolean isOverride, JavaClassScope builderScope, Function<JavaMethodScope, CodeRenderer> computeParameters) {
		String opName = prop.getOperationName(operationType);
		JavaMethodScope scope = builderScope.createMethodScope(opName);
		if (isOverride) {
			out.writeln("@Override");
		}
		out.writeln(mainBuilderType, " ", opName, "(", computeParameters.apply(scope), ");");
	}

	/**
	 * The parameter list of a builder interface setter: a parameter of the given type named after
	 * the property, optionally followed by an index.
	 */
	private Function<JavaMethodScope, CodeRenderer> parameters(JavaPojoProperty prop, JavaType type, boolean indexed) {
		return scope -> {
			GeneratedIdentifier id = scope.createUniqueIdentifier(prop.getName());
			return out -> out.write(type, " ", id, indexed ? ", int idx" : "");
		};
	}

	private void renderPojoBuilderInterfaceSetterMethods(CodeWriter out, JavaPojoInterface mainType, JavaPojoBuilderInterface mainBuilderType, JavaPojoInterface currentType, JavaClassScope builderScope) {
		boolean isOverride = !mainType.equals(currentType);
		if (currentType.getSuperPojo() != null) {
			renderPojoBuilderInterfaceSetterMethods(out, mainType, mainBuilderType, currentType.getSuperPojo(), builderScope);
		}
		for (JavaPojoProperty prop : currentType.getOwnProperties()) {
			JavaType propType = prop.getType();
			if (!typeUtil.isList(propType)) {
				renderSetterMethod(out, prop, mainBuilderType, SET, isOverride, builderScope, parameters(prop, propType, false));
				if (propType instanceof RJavaWithMetaValue propWithMeta) {
					renderSetterMethod(out, prop, mainBuilderType, SET_VALUE, isOverride, builderScope, parameters(prop, propWithMeta.getValueType(), false));
				}
			} else {
				JavaType itemType = typeUtil.getItemType(propType);
				renderSetterMethod(out, prop, mainBuilderType, ADD, isOverride, builderScope, parameters(prop, itemType, false));
				renderSetterMethod(out, prop, mainBuilderType, ADD, isOverride, builderScope, parameters(prop, itemType, true));
				if (itemType instanceof RJavaWithMetaValue itemWithMeta) {
					renderSetterMethod(out, prop, mainBuilderType, ADD_VALUE, isOverride, builderScope, parameters(prop, itemWithMeta.getValueType(), false));
					renderSetterMethod(out, prop, mainBuilderType, ADD_VALUE, isOverride, builderScope, parameters(prop, itemWithMeta.getValueType(), true));
				}
				renderSetterMethod(out, prop, mainBuilderType, ADD, isOverride, builderScope, parameters(prop, propType, false));
				renderSetterMethod(out, prop, mainBuilderType, SET, isOverride, builderScope, parameters(prop, propType, false));
				if (itemType instanceof RJavaWithMetaValue itemWithMeta) {
					JavaType valueListType = typeUtil.wrapExtends(typeUtil.LIST, itemWithMeta.getValueType());
					renderSetterMethod(out, prop, mainBuilderType, ADD_VALUE, isOverride, builderScope, parameters(prop, valueListType, false));
					renderSetterMethod(out, prop, mainBuilderType, SET_VALUE, isOverride, builderScope, parameters(prop, valueListType, false));
				}
			}
		}
	}

	private void renderPojoInterfaceDefaultOverriddenMethods(CodeWriter out, JavaPojoInterface javaType, GeneratedIdentifier metaDataIdentifier) {
		out.writeln("@Override");
		out.writeln("default ", RosettaMetaData.class, "<? extends ", javaType, "> metaData() {");
		out.indented(() -> out.writeln("return ", metaDataIdentifier, ";"));
		out.writeln("}");
		out.newline();
		out.writeln("@Override");
		out.writeln("@", RuneAttribute.class, "(\"@type\")");
		out.writeln("default Class<? extends ", javaType, "> getType() {");
		out.indented(() -> out.writeln("return ", javaType, ".class;"));
		out.writeln("}");
		if (javaType instanceof RJavaWithMetaValue withMeta) {
			out.newline();
			out.writeln("@Override");
			out.writeln("default Class<", withMeta.getValueType(), "> getValueType() {");
			out.indented(() -> out.writeln("return ", withMeta.getValueType(), ".class;"));
			out.writeln("}");
		}
		out.newline();
		out.write(boilerPlate.processMethod(javaType));
	}

	private void renderPojoInterfaceGetterMethods(CodeWriter out, JavaPojoInterface javaType) {
		for (JavaPojoProperty prop : javaType.getOwnProperties()) {
			out.write(prop.getJavadoc());
			if (prop.getterOverridesParentGetter()) {
				out.writeln("@Override");
			}
			out.writeln(prop.getType(), " ", prop.getOperationName(GET), "();");
		}
	}

	private void renderPojoInterfaceBuilderMethods(CodeWriter out, JavaPojoInterface javaType) {
		out.writeln(javaType, " build();");
		out.newline();
		out.writeln(javaType.toBuilderInterface(), " toBuilder();");
		out.newline();
		out.writeln("static ", javaType.toBuilderInterface(), " builder() {");
		out.indented(() -> out.writeln("return new ", javaType.toBuilderImplClass(), "();"));
		out.writeln("}");
	}

	private void renderRosettaClass(CodeWriter out, JavaPojoInterface javaType, JavaPojoImpl implType, JavaPojoBuilderInterface builderType, boolean extended, JavaClassScope implScope) {
		Collection<JavaPojoProperty> properties = extended ? javaType.getOwnProperties() : javaType.getAllProperties();
		for (JavaPojoProperty prop : properties) {
			out.writeln("private final ", prop.getType(), " ", implScope.createIdentifier(prop, uncapitalize(prop.getName())), ";");
		}
		out.newline();
		out.writeln("protected ", implType.getSimpleName(), "(", builderType, " builder) {");
		out.indented(() -> {
			if (extended) {
				out.writeln("super(builder);");
			}
			for (JavaPojoProperty prop : properties) {
				out.write("this.", implScope.getIdentifierOrThrow(prop), " = ");
				renderPropertyFromBuilder(out, prop);
				out.writeln(";");
			}
		});
		out.writeln("}");
		out.newline();
		for (JavaPojoProperty prop : properties) {
			JavaVariable field = new JavaVariable(implScope.getIdentifierOrThrow(prop), prop.getType());
			out.writeln("@Override");
			out.write(boilerPlate.attributeAnnotations(prop, AccessorType.GETTER, typeUtil.isList(prop.getType())));
			out.writeln("public ", prop.getType(), " ", prop.getOperationName(GET), "() ", field.completeAsReturn().toBlock());
			out.newline();
			if (!extended) {
				out.write(derivedIncompatibleGettersForProperty(field, prop, implScope));
			}
		}
		out.writeln("@Override");
		out.writeln("public ", javaType, " build() {");
		out.indented(() -> out.writeln("return this;"));
		out.writeln("}");
		out.newline();
		out.writeln("@Override");
		out.writeln("public ", builderType, " toBuilder() {");
		out.indented(() -> {
			out.writeln(builderType, " builder = builder();");
			out.writeln("setBuilderFields(builder);");
			out.writeln("return builder;");
		});
		out.writeln("}");
		out.newline();
		out.writeln("protected void setBuilderFields(", builderType, " builder) {");
		out.indented(() -> {
			if (extended) {
				out.writeln("super.setBuilderFields(builder);");
			}
			for (JavaPojoProperty prop : properties) {
				out.writeln(importManager.method(Optional.class, "ofNullable"), "(", prop.getOperationName(GET), "()).ifPresent(builder::", prop.getOperationName(SET), ");");
			}
		});
		out.writeln("}");
	}

	private CodeRenderer derivedIncompatibleGettersForProperty(JavaExpression originalField, JavaPojoProperty prop, JavaClassScope implScope) {
		JavaPojoProperty parent = prop.getParentProperty();
		if (parent == null) {
			return null;
		} else if (prop.getterOverridesParentGetter()) {
			return derivedIncompatibleGettersForProperty(originalField, parent, implScope);
		}
		String opName = parent.getOperationName(GET);
		JavaMethodScope getterScope = implScope.createMethodScope(opName);
		return out -> {
			out.writeln("@Override");
			out.writeln("@", RosettaIgnore.class);
			out.writeln("@", RuneIgnore.class);
			out.writeln("public ", parent.getType(), " ", opName, "() ", coercionService.addCoercions(originalField, parent.getType(), getterScope.getBodyScope()).completeAsReturn().toBlock());
			out.newline();
			out.write(derivedIncompatibleGettersForProperty(originalField, parent, implScope));
		};
	}

	private void renderPropertyFromBuilder(CodeWriter out, JavaPojoProperty prop) {
		String getterName = prop.getOperationName(GET);
		if (typeTranslator.isRosettaModelObject(prop.getType())) {
			if (typeUtil.isList(prop.getType())) {
				out.write("ofNullable(builder.", getterName, "()).filter(_l->!_l.isEmpty()).map(");
				out.write("list -> list.stream().filter(", Objects.class, "::nonNull).map(f->f.build()).filter(", Objects.class, "::nonNull).collect(", ImmutableList.class, ".toImmutableList())");
				out.write(").orElse(null)");
			} else {
				out.write("ofNullable(builder.", getterName, "()).map(f->f.build()).orElse(null)");
			}
		} else {
			if (!typeUtil.isList(prop.getType())) {
				out.write("builder.", getterName, "()");
			} else {
				out.write("ofNullable(builder.", getterName, "()).filter(_l->!_l.isEmpty()).map(", ImmutableList.class, "::copyOf).orElse(null)");
			}
		}
	}

	private void renderStartComment(CodeWriter out, String msg) {
		out.writeln("/*********************** ", msg, "  ***********************/");
	}
}
