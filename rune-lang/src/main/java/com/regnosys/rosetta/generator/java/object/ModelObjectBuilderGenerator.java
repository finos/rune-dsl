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
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.JavaEnhancedForLoop;
import com.regnosys.rosetta.generator.java.statement.JavaIfThenStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaIfThenElseBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.generator.java.statement.builder.JavaThis;
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable;
import com.regnosys.rosetta.generator.java.types.JavaPojoBuilderImpl;
import com.regnosys.rosetta.generator.java.types.JavaPojoBuilderInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty;
import com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.RosettaIgnore;
import com.rosetta.model.lib.annotations.RuneIgnore;
import com.rosetta.model.lib.meta.Key;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class ModelObjectBuilderGenerator {
	@Inject
	private ModelObjectBoilerPlate boilerPlate;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private JavaTypeUtil typeUtil;
	@Inject
	private TypeCoercionService coercionService;
	@Inject
	private IShouldPrune shouldPrune;

	public CodeRenderer builderClass(JavaPojoInterface javaType, JavaPojoBuilderImpl builderImplClass, JavaClassScope scope) {
		JavaPojoInterface superPojo = javaType.getSuperPojo();
		boolean extendSuperImpl = superPojo != null && javaType.getOwnProperties().stream()
				.allMatch(prop -> prop.getParentProperty() == null || Objects.equals(prop.getParentProperty().getType(), prop.getType()));
		JavaPojoBuilderInterface builderInterface = javaType.toBuilderInterface();
		Collection<JavaPojoProperty> properties = extendSuperImpl ? javaType.getOwnProperties() : javaType.getAllProperties();
		javaType.getAllProperties().forEach(prop -> scope.createIdentifier(prop, uncapitalize(prop.getName())));
		return out -> {
			out.write(builderImplClass.asClassDeclaration());
			out.writeln(" {");
			out.indented(() -> {
				out.newline();
				for (JavaPojoProperty prop : properties) {
					out.write("protected ", toBuilderType(prop), " ", scope.getIdentifierOrThrow(prop));
					if (typeUtil.isList(prop.getType())) {
						out.write(" = new ", ArrayList.class, "<>()");
					}
					out.writeln(";");
				}
				renderBuilderGetters(out, properties, extendSuperImpl, scope);
				renderSetters(out, javaType, scope);
				out.newline();
				out.writeln("@Override");
				out.writeln("public ", javaType, " build() {");
				out.indented(() -> out.writeln("return new ", javaType.toImplClass(), "(this);"));
				out.writeln("}");
				out.newline();
				out.writeln("@Override");
				out.writeln("public ", builderInterface, " toBuilder() {");
				out.indented(() -> out.writeln("return this;"));
				out.writeln("}");
				out.newline();
				out.writeln("@SuppressWarnings(\"unchecked\")");
				out.writeln("@Override");
				out.writeln("public ", builderInterface, " prune() {");
				out.indented(() -> {
					if (extendSuperImpl) {
						out.writeln("super.prune();");
					}
					properties.stream()
							.filter(prop -> typeTranslator.isRosettaModelObject(prop.getType()))
							.forEach(prop -> renderPrune(out, javaType, prop, scope.getIdentifierOrThrow(prop)));
					out.writeln("return this;");
				});
				out.writeln("}");
				out.newline();
				renderHasData(out, javaType, properties, extendSuperImpl);
				out.newline();
				out.write(boilerPlate.builderBoilerPlate(javaType, extendSuperImpl, scope));
			});
			out.writeln("}");
		};
	}

	private void renderPrune(CodeWriter out, JavaPojoInterface javaType, JavaPojoProperty prop, GeneratedIdentifier id) {
		if (!typeUtil.isList(prop.getType())) {
			if (shouldPrune.shouldBePruned(javaType, prop)) {
				out.writeln("if (", id, "!=null && !", id, ".prune().hasData()) ", id, " = null;");
			} else {
				out.writeln("if (", id, "!=null) ", id, ".prune();");
			}
		} else {
			out.write(id, " = ", id, ".stream().filter(b->b!=null).<", toBuilderTypeSingle(prop), ">map(b->b.prune())");
			if (shouldPrune.shouldBePruned(javaType, prop)) {
				out.write(".filter(b->b.hasData())");
			}
			out.writeln(".collect(", Collectors.class, ".toList());");
		}
	}

	private void renderBuilderGetters(CodeWriter out, Collection<JavaPojoProperty> properties, boolean extended, JavaClassScope scope) {
		for (JavaPojoProperty prop : properties) {
			JavaVariable field = new JavaVariable(scope.getIdentifierOrThrow(prop), prop.getType());
			out.newline();
			out.writeln("@Override");
			out.write(boilerPlate.attributeAnnotations(prop, AccessorType.GETTER, typeUtil.isList(prop.getType())));
			out.writeln("public ", toBuilderTypeExt(prop), " ", prop.getOperationName(GET), "() ", field.completeAsReturn().toBlock());
			if (typeTranslator.isRosettaModelObject(prop.getType())) {
				JavaMethodScope getOrCreateScope = scope.createMethodScope(prop.getOperationName(GET_OR_CREATE));
				if (!typeUtil.isList(prop.getType())) {
					renderGetOrCreateSingle(out, prop, field, getOrCreateScope);
				} else {
					renderGetOrCreateIndexed(out, prop, field, getOrCreateScope);
				}
			}
			if (!extended) {
				out.write(derivedIncompatibleGettersForProperty(field, prop, prop, scope));
			}
		}
	}

	private void renderGetOrCreateSingle(CodeWriter out, JavaPojoProperty prop, JavaVariable field, JavaMethodScope getOrCreateScope) {
		GeneratedIdentifier resultId = getOrCreateScope.getBodyScope().createUniqueIdentifier("result");
		out.newline();
		out.writeln("@Override");
		out.writeln("public ", toBuilderTypeSingle(prop), " ", prop.getOperationName(GET_OR_CREATE), "() {");
		out.indented(() -> {
			out.writeln(toBuilderTypeSingle(prop), " ", resultId, ";");
			out.writeln("if (", field, "!=null) {");
			out.indented(() -> out.writeln(resultId, " = ", field, ";"));
			out.writeln("}");
			out.writeln("else {");
			out.indented(() -> {
				out.writeln(resultId, " = ", field, " = ", prop.getType(), ".builder();");
				if (prop.hasLocation()) {
					out.writeln(resultId, ".getOrCreateMeta().toBuilder().addKey(", Key.class, ".builder().setScope(\"DOCUMENT\"));");
				}
			});
			out.writeln("}");
			out.newline();
			out.writeln("return ", resultId, ";");
		});
		out.writeln("}");
	}

	private void renderGetOrCreateIndexed(CodeWriter out, JavaPojoProperty prop, JavaVariable field, JavaMethodScope getOrCreateScope) {
		GeneratedIdentifier indexId = getOrCreateScope.createUniqueIdentifier("index");
		GeneratedIdentifier newObjectId = getOrCreateScope.getBodyScope().lambdaScope().createUniqueIdentifier("new" + capitalize(prop.getName()));
		out.newline();
		out.writeln("@Override");
		out.writeln("public ", toBuilderTypeSingle(prop), " ", prop.getOperationName(GET_OR_CREATE), "(int ", indexId, ") {");
		out.indented(() -> {
			out.writeln("if (", field, "==null) {");
			out.indented(() -> out.writeln("this.", field, " = new ", ArrayList.class, "<>();"));
			out.writeln("}");
			out.writeln("return getIndex(", field, ", ", indexId, ", () -> {");
			out.indented(() -> out.indented(() -> {
				out.indented(() -> {
					out.writeln(toBuilderTypeSingle(prop), " ", newObjectId, " = ", typeUtil.getItemType(prop.getType()), ".builder();");
					if (prop.hasLocation()) {
						out.writeln(newObjectId, ".getOrCreateMeta().addKey(", Key.class, ".builder().setScope(\"DOCUMENT\"));");
					}
					out.writeln("return ", newObjectId, ";");
				});
				out.writeln("});");
			}));
		});
		out.writeln("}");
	}

	private CodeRenderer derivedIncompatibleGettersForProperty(JavaExpression originalField, JavaPojoProperty originalProp, JavaPojoProperty prop, JavaClassScope scope) {
		JavaPojoProperty parent = prop.getParentProperty();
		if (parent == null) {
			return null;
		} else if (prop.getterOverridesParentGetter()) {
			return derivedIncompatibleGettersForProperty(originalField, originalProp, parent, scope);
		}
		String getterName = parent.getOperationName(GET);
		JavaMethodScope getterScope = scope.createMethodScope(getterName);
		JavaStatementScope bodyScope = getterScope.getBodyScope();
		return out -> {
			out.newline();
			out.writeln("@Override");
			out.writeln("@", RosettaIgnore.class);
			out.writeln("@", RuneIgnore.class);
			JavaStatementBuilder getterBody;
			if (typeUtil.isList(parent.getType())) {
				// The child field may be either single- or multi-cardinality. In both cases,
				// coercing it to the parent's list type is null-safe (a null item becomes an empty
				// list), so we can uniformly map the resulting list to its builder type.
				getterBody = coercionService.addCoercions(originalField, parent.getType(), bodyScope)
						.collapseToSingleExpression(bodyScope)
						.mapExpression(list -> {
							JavaType itemType = typeUtil.getItemType(parent.getType());
							JavaVariable lambdaParam = new JavaVariable(bodyScope.lambdaScope().createUniqueIdentifier(uncapitalize(itemType.getSimpleName())), itemType);
							return JavaExpression.from(
									o -> o.write(list, ".stream().map(", lambdaParam, " -> ", toBuilder(lambdaParam).toLambdaBody(), ").collect(", Collectors.class, ".toList())"),
									toBuilderTypeExt(parent));
						});
			} else {
				getterBody = coercionService.addCoercions(originalField, parent.getType(), bodyScope)
						.mapExpressionIfNotNull(this::toBuilder);
			}
			out.writeln("public ", toBuilderTypeExt(parent), " ", getterName, "() ", getterBody.completeAsReturn().toBlock());
			if (typeTranslator.isRosettaModelObject(parent.getType())) {
				String getOrCreateName = parent.getOperationName(GET_OR_CREATE);
				JavaMethodScope getOrCreateScope = scope.createMethodScope(getOrCreateName);
				String originalGetOrCreateName = originalProp.getOperationName(GET_OR_CREATE);
				JavaType originalItemType = typeUtil.getItemType(originalProp.getType());
				JavaType parentItemType = typeUtil.getItemType(parent.getType());
				if (!typeUtil.isList(parent.getType())) {
					JavaExpression getOrCreate = JavaExpression.from(o -> o.write(originalGetOrCreateName, "()"), originalItemType);
					out.newline();
					out.writeln("@Override");
					out.writeln("public ", toBuilderTypeSingle(parent), " ", getOrCreateName, "() ", coercionService.addCoercions(getOrCreate, parentItemType, getOrCreateScope.getBodyScope())
							.mapExpressionIfNotNull(this::toBuilder)
							.completeAsReturn()
							.toBlock());
				} else {
					GeneratedIdentifier indexId = getOrCreateScope.createUniqueIdentifier("index");
					JavaExpression getOrCreate = JavaExpression.from(o -> {
						o.write(originalGetOrCreateName, "(");
						if (typeUtil.isList(originalProp.getType())) {
							o.write(indexId);
						}
						o.write(")");
					}, originalItemType);
					out.newline();
					out.writeln("@Override");
					out.writeln("public ", toBuilderTypeSingle(parent), " ", getOrCreateName, "(int ", indexId, ") ", coercionService.addCoercions(getOrCreate, parentItemType, getOrCreateScope.getBodyScope())
							.mapExpressionIfNotNull(this::toBuilder)
							.completeAsReturn()
							.toBlock());
				}
			}
			out.write(derivedIncompatibleGettersForProperty(originalField, originalProp, parent, scope));
		};
	}

	private void renderSetters(CodeWriter out, JavaPojoInterface javaType, JavaClassScope scope) {
		for (JavaPojoProperty prop : javaType.getAllProperties()) {
			out.newline();
			renderSetter(out, javaType, prop, prop, scope);
		}
	}

	private void renderSetterMethod(CodeWriter out, JavaPojoProperty prop, JavaType mainBuilderType, JavaPojoPropertyOperationType operationType, JavaClassScope builderScope, Function<JavaMethodScope, CodeRenderer> computeParametersAndBody) {
		String opName = prop.getOperationName(operationType);
		JavaMethodScope scope = builderScope.createMethodScope(opName);
		out.writeln("@Override");
		out.write("public ", mainBuilderType, " ", opName);
		out.writeln(computeParametersAndBody.apply(scope));
	}

	private void renderSetterAnnotations(CodeWriter out, JavaPojoProperty prop, boolean isMainProp, AccessorType accessorType, boolean multi) {
		if (isMainProp) {
			out.write(boilerPlate.attributeAnnotations(prop, accessorType, multi));
		} else {
			out.writeln("@", RosettaIgnore.class);
			out.writeln("@", RuneIgnore.class);
		}
	}

	/**
	 * Calls the setter or adder of {@code mainProp} with the given argument, the way a setter of a
	 * property that {@code mainProp} overrides delegates to it.
	 */
	private JavaExpression delegateTo(JavaPojoProperty mainProp, JavaPojoPropertyOperationType operationType, JavaExpression argument, JavaType builderType, GeneratedIdentifier indexId) {
		return JavaExpression.from(o -> {
			o.write(mainProp.getOperationName(operationType), "(", argument);
			if (indexId != null) {
				o.write(", ", indexId);
			}
			o.write(")");
		}, builderType);
	}

	private void renderSetter(CodeWriter out, JavaPojoInterface javaType, JavaPojoProperty mainProp, JavaPojoProperty currentProp, JavaClassScope builderScope) {
		JavaPojoBuilderInterface builderType = javaType.toBuilderInterface();
		JavaType mainPropType = mainProp.getType();
		JavaType propType = currentProp.getType();
		boolean isMainProp = mainProp.equals(currentProp);
		GeneratedIdentifier field = builderScope.getIdentifierOrThrow(mainProp);
		JavaThis thisExpr = new JavaThis(builderType);
		boolean mainPropIsList = typeUtil.isList(mainPropType);
		if (typeUtil.isList(propType)) {
			JavaType itemType = typeUtil.getItemType(propType);
			JavaType mainItemType = typeUtil.getItemType(mainPropType);
			JavaPojoPropertyOperationType mainAddValue = mainItemType instanceof RJavaWithMetaValue ? ADD_VALUE : ADD;
			JavaPojoPropertyOperationType mainSetValue = mainItemType instanceof RJavaWithMetaValue ? SET_VALUE : SET;
			renderSetterAnnotations(out, currentProp, isMainProp, AccessorType.ADDER, true);
			renderSetterMethod(out, currentProp, builderType, ADD, builderScope, scope -> {
				JavaVariable addMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName())), itemType);
				JavaStatementBuilder body;
				if (isMainProp) {
					body = new JavaIfThenStatement(
							JavaExpression.from(o -> o.write(addMethodArg, " != null"), JavaPrimitiveType.BOOLEAN),
							JavaExpression.from(o -> o.write("this.", field, ".add(", toBuilder(addMethodArg), ")"), JavaPrimitiveType.VOID)
									.completeAsExpressionStatement()
					).append(thisExpr);
				} else {
					body = coercionService.addCoercions(addMethodArg, mainItemType, false, scope.getBodyScope())
							.collapseToSingleExpression(scope.getBodyScope())
							.mapExpression(it -> delegateTo(mainProp, mainPropIsList ? ADD : SET, it, builderType, null));
				}
				JavaStatement block = body.completeAsReturn().toBlock();
				return o -> o.write("(", itemType, " ", addMethodArg, ") ", block);
			});
			out.newline();
			renderSetterMethod(out, currentProp, builderType, ADD, builderScope, scope -> {
				JavaVariable indexedAddMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName())), itemType);
				GeneratedIdentifier indexId = scope.createUniqueIdentifier("idx");
				JavaStatementBuilder body;
				if (isMainProp) {
					body = JavaExpression.from(o -> o.write("getIndex(this.", field, ", ", indexId, ", () -> ", toBuilder(indexedAddMethodArg), ")"), JavaPrimitiveType.VOID)
							.completeAsExpressionStatement()
							.append(thisExpr);
				} else {
					body = coercionService.addCoercions(indexedAddMethodArg, mainItemType, false, scope.getBodyScope())
							.collapseToSingleExpression(scope.getBodyScope())
							.mapExpression(it -> mainPropIsList
									? delegateTo(mainProp, ADD, it, builderType, indexId)
									: delegateTo(mainProp, SET, it, builderType, null));
				}
				JavaStatement block = body.completeAsReturn().toBlock();
				return o -> o.write("(", itemType, " ", indexedAddMethodArg, ", int ", indexId, ") ", block);
			});
			if (itemType instanceof RJavaWithMetaValue itemWithMeta) {
				JavaType valueType = itemWithMeta.getValueType();
				JavaType mainValueType = mainItemType instanceof RJavaWithMetaValue mainItemWithMeta ? mainItemWithMeta.getValueType() : mainItemType;
				out.newline();
				renderSetterMethod(out, currentProp, builderType, ADD_VALUE, builderScope, scope -> {
					JavaVariable addValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName())), valueType);
					JavaStatementBuilder body;
					if (isMainProp) {
						body = JavaExpression.from(o -> o.write("this.", currentProp.getOperationName(GET_OR_CREATE), "(-1).setValue(", toBuilder(addValueMethodArg), ")"), JavaPrimitiveType.VOID)
								.completeAsExpressionStatement()
								.append(thisExpr);
					} else {
						body = coercionService.addCoercions(addValueMethodArg, mainValueType, false, scope.getBodyScope())
								.collapseToSingleExpression(scope.getBodyScope())
								.mapExpression(it -> delegateTo(mainProp, mainPropIsList ? mainAddValue : mainSetValue, it, builderType, null));
					}
					JavaStatement block = body.completeAsReturn().toBlock();
					return o -> o.write("(", valueType, " ", addValueMethodArg, ") ", block);
				});
				out.newline();
				renderSetterMethod(out, currentProp, builderType, ADD_VALUE, builderScope, scope -> {
					JavaVariable indexedAddValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName())), valueType);
					GeneratedIdentifier indexId = scope.createUniqueIdentifier("idx");
					JavaStatementBuilder body;
					if (isMainProp) {
						body = JavaExpression.from(o -> o.write("this.", currentProp.getOperationName(GET_OR_CREATE), "(", indexId, ").setValue(", toBuilder(indexedAddValueMethodArg), ")"), JavaPrimitiveType.VOID)
								.completeAsExpressionStatement()
								.append(thisExpr);
					} else {
						body = coercionService.addCoercions(indexedAddValueMethodArg, mainValueType, false, scope.getBodyScope())
								.collapseToSingleExpression(scope.getBodyScope())
								.mapExpression(it -> mainPropIsList
										? delegateTo(mainProp, mainAddValue, it, builderType, indexId)
										: delegateTo(mainProp, mainSetValue, it, builderType, null));
					}
					JavaStatement block = body.completeAsReturn().toBlock();
					return o -> o.write("(", valueType, " ", indexedAddValueMethodArg, ", int ", indexId, ") ", block);
				});
			}
			out.newline();
			renderSetterMethod(out, currentProp, builderType, ADD, builderScope, scope -> {
				JavaVariable addMultiMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName()) + "s"), propType);
				JavaStatementBuilder body;
				if (isMainProp) {
					GeneratedIdentifier forLoopId = scope.getBodyScope().createUniqueIdentifier("toAdd");
					JavaVariable forLoopVar = new JavaVariable(forLoopId, itemType);
					body = new JavaIfThenStatement(
							JavaExpression.from(o -> o.write(addMultiMethodArg, " != null"), JavaPrimitiveType.BOOLEAN),
							new JavaEnhancedForLoop(true, itemType, forLoopId, addMultiMethodArg,
									JavaExpression.from(o -> o.write("this.", field, ".add(", toBuilder(forLoopVar), ")"), JavaPrimitiveType.VOID)
											.completeAsExpressionStatement()
							)
					).append(thisExpr);
				} else {
					body = coercionService.addCoercions(addMultiMethodArg, mainPropType, false, scope.getBodyScope())
							.collapseToSingleExpression(scope.getBodyScope())
							.mapExpression(it -> delegateTo(mainProp, mainPropIsList ? ADD : SET, it, builderType, null));
				}
				JavaStatement block = body.completeAsReturn().toBlock();
				return o -> o.write("(", propType, " ", addMultiMethodArg, ") ", block);
			});
			out.newline();
			renderSetterAnnotations(out, currentProp, isMainProp, AccessorType.SETTER, true);
			renderSetterMethod(out, currentProp, builderType, SET, builderScope, scope -> {
				JavaVariable setMultiMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName()) + "s"), propType);
				JavaStatementBuilder body;
				if (isMainProp) {
					body = new JavaIfThenElseBuilder(
							JavaExpression.from(o -> o.write(setMultiMethodArg, " == null"), JavaPrimitiveType.BOOLEAN),
							JavaExpression.from(o -> o.write("this.", field, " = new ", ArrayList.class, "<>()"), JavaPrimitiveType.VOID),
							JavaExpression.from(o -> {
								o.writeln("this.", field, " = ", setMultiMethodArg, ".stream()");
								o.indented(() -> {
									if (typeTranslator.isRosettaModelObject(propType)) {
										o.writeln(".map(_a->_a.toBuilder())");
									}
									o.write(".collect(", Collectors.class, ".toCollection(()->new ArrayList<>()))");
								});
							}, JavaPrimitiveType.VOID),
							typeUtil
					).completeAsExpressionStatement()
							.append(thisExpr);
				} else {
					body = coercionService.addCoercions(setMultiMethodArg, mainPropType, false, scope.getBodyScope())
							.collapseToSingleExpression(scope.getBodyScope())
							.mapExpression(it -> delegateTo(mainProp, SET, it, builderType, null));
				}
				JavaStatement block = body.completeAsReturn().toBlock();
				return o -> o.write("(", propType, " ", setMultiMethodArg, ") ", block);
			});
			if (itemType instanceof RJavaWithMetaValue itemWithMeta) {
				JavaType valueType = itemWithMeta.getValueType();
				JavaType mainValueType = mainItemType instanceof RJavaWithMetaValue mainItemWithMeta ? mainItemWithMeta.getValueType() : mainItemType;
				JavaType mainValueListType = mainPropIsList ? typeUtil.wrapExtendsIfNotFinal(typeUtil.LIST, mainValueType) : mainValueType;
				out.newline();
				renderSetterMethod(out, currentProp, builderType, ADD_VALUE, builderScope, scope -> {
					JavaVariable addMultiValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName()) + "s"), typeUtil.wrapExtends(typeUtil.LIST, valueType));
					JavaStatementBuilder body;
					if (isMainProp) {
						GeneratedIdentifier forLoopId = scope.getBodyScope().createUniqueIdentifier("toAdd");
						JavaVariable forLoopVar = new JavaVariable(forLoopId, itemType);
						body = new JavaIfThenStatement(
								JavaExpression.from(o -> o.write(addMultiValueMethodArg, " != null"), JavaPrimitiveType.BOOLEAN),
								new JavaEnhancedForLoop(true, valueType, forLoopId, addMultiValueMethodArg,
										JavaExpression.from(o -> o.write("this.", mainProp.getOperationName(ADD_VALUE), "(", forLoopVar, ")"), JavaPrimitiveType.VOID)
												.completeAsExpressionStatement()
								)
						).append(thisExpr);
					} else {
						body = coercionService.addCoercions(addMultiValueMethodArg, mainValueListType, false, scope.getBodyScope())
								.collapseToSingleExpression(scope.getBodyScope())
								.mapExpression(it -> delegateTo(mainProp, mainPropIsList ? mainAddValue : mainSetValue, it, builderType, null));
					}
					JavaStatement block = body.completeAsReturn().toBlock();
					return o -> o.write("(", addMultiValueMethodArg.getExpressionType(), " ", addMultiValueMethodArg, ") ", block);
				});
				out.newline();
				renderSetterMethod(out, currentProp, builderType, SET_VALUE, builderScope, scope -> {
					JavaVariable setMultiValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName()) + "s"), typeUtil.wrapExtends(typeUtil.LIST, valueType));
					JavaStatementBuilder body;
					if (isMainProp) {
						body = JavaExpression.from(o -> o.write("this.", field, ".clear()"), JavaPrimitiveType.VOID).completeAsExpressionStatement()
								.append(new JavaIfThenStatement(
										JavaExpression.from(o -> o.write(setMultiValueMethodArg, " != null"), JavaPrimitiveType.BOOLEAN),
										JavaExpression.from(o -> o.write(setMultiValueMethodArg, ".forEach(this::", mainProp.getOperationName(ADD_VALUE), ")"), JavaPrimitiveType.VOID)
												.completeAsExpressionStatement()
								)).append(thisExpr);
					} else {
						body = coercionService.addCoercions(setMultiValueMethodArg, mainValueListType, false, scope.getBodyScope())
								.collapseToSingleExpression(scope.getBodyScope())
								.mapExpression(it -> delegateTo(mainProp, mainSetValue, it, builderType, null));
					}
					JavaStatement block = body.completeAsReturn().toBlock();
					return o -> o.write("(", setMultiValueMethodArg.getExpressionType(), " ", setMultiValueMethodArg, ") ", block);
				});
			}
		} else {
			renderSetterAnnotations(out, currentProp, isMainProp, AccessorType.SETTER, false);
			renderSetterMethod(out, currentProp, builderType, SET, builderScope, scope -> {
				JavaVariable setMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName())), propType);
				JavaStatementBuilder body;
				if (isMainProp) {
					body = JavaExpression.from(o -> o.write("this.", field, " = ", setMethodArg, " == null ? null : ", toBuilder(setMethodArg)), JavaPrimitiveType.VOID)
							.completeAsExpressionStatement()
							.append(thisExpr);
				} else {
					body = coercionService.addCoercions(setMethodArg, mainPropType, false, scope.getBodyScope())
							.collapseToSingleExpression(scope.getBodyScope())
							.mapExpression(it -> delegateTo(mainProp, SET, it, builderType, null));
				}
				JavaStatement block = body.completeAsReturn().toBlock();
				return o -> o.write("(", propType, " ", setMethodArg, ") ", block);
			});
			if (propType instanceof RJavaWithMetaValue propWithMeta) {
				JavaType valueType = propWithMeta.getValueType();
				JavaType mainValueType = mainPropType instanceof RJavaWithMetaValue mainPropWithMeta ? mainPropWithMeta.getValueType() : mainPropType;
				JavaPojoPropertyOperationType mainSetValue = mainPropType instanceof RJavaWithMetaValue ? SET_VALUE : SET;
				out.newline();
				renderSetterMethod(out, currentProp, builderType, SET_VALUE, builderScope, scope -> {
					JavaVariable setValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(uncapitalize(currentProp.getName())), valueType);
					JavaStatementBuilder body;
					if (isMainProp) {
						body = JavaExpression.from(o -> o.write("this.", mainProp.getOperationName(GET_OR_CREATE), "().setValue(", setValueMethodArg, ")"), JavaPrimitiveType.VOID)
								.completeAsExpressionStatement()
								.append(thisExpr);
					} else {
						body = coercionService.addCoercions(setValueMethodArg, mainValueType, false, scope.getBodyScope())
								.collapseToSingleExpression(scope.getBodyScope())
								.mapExpression(it -> delegateTo(mainProp, mainSetValue, it, builderType, null));
					}
					JavaStatement block = body.completeAsReturn().toBlock();
					return o -> o.write("(", valueType, " ", setValueMethodArg, ") ", block);
				});
			}
		}
		if (currentProp.getParentProperty() != null) {
			out.newline();
			renderSetter(out, javaType, mainProp, currentProp.getParentProperty(), builderScope);
		}
	}

	private void renderHasData(CodeWriter out, JavaPojoInterface type, Collection<JavaPojoProperty> properties, boolean extended) {
		out.writeln("@Override");
		out.writeln("public boolean hasData() {");
		out.indented(() -> {
			if (extended) {
				out.writeln("if (super.hasData()) return true;");
			}
			for (JavaPojoProperty prop : properties) {
				if (prop.getName().equals("meta")) {
					continue;
				}
				String getter = prop.getOperationName(GET);
				if (typeUtil.isList(prop.getType())) {
					if (shouldPrune.mayBeEmpty(type, prop)) {
						out.writeln("if (", getter, "()!=null && ", getter, "().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;");
					} else {
						out.writeln("if (", getter, "()!=null && !", getter, "().isEmpty()) return true;");
					}
				} else if (shouldPrune.mayBeEmpty(type, prop)) {
					out.writeln("if (", getter, "()!=null && ", getter, "().hasData()) return true;");
				} else {
					out.writeln("if (", getter, "()!=null) return true;");
				}
			}
			out.writeln("return false;");
		});
		out.writeln("}");
	}

	private JavaType toBuilderType(JavaPojoProperty prop) {
		if (typeUtil.isList(prop.getType())) {
			return typeUtil.wrap(typeUtil.LIST, toBuilderTypeSingle(prop));
		}
		return toBuilderTypeSingle(prop);
	}

	private JavaType toBuilderTypeExt(JavaPojoProperty prop) {
		if (typeUtil.isList(prop.getType())) {
			return typeTranslator.isRosettaModelObject(prop.getType())
					? typeUtil.wrapExtends(typeUtil.LIST, toBuilderTypeSingle(prop))
					: typeUtil.wrap(typeUtil.LIST, toBuilderTypeSingle(prop));
		}
		return toBuilderTypeSingle(prop);
	}

	public JavaType toBuilderTypeSingle(JavaPojoProperty prop) {
		return typeUtil.toBuilder(typeUtil.getItemType(prop.getType()));
	}

	// TODO: replace with coercions
	private JavaExpression toBuilder(JavaExpression expr) {
		JavaType t = expr.getExpressionType();
		if (typeUtil.hasBuilderType(t)) {
			return JavaExpression.from(o -> o.write(expr, ".toBuilder()"), typeUtil.toBuilder(t));
		}
		return expr;
	}
}
