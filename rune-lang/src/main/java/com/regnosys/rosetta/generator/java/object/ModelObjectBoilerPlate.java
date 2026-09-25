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

import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.GET;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral;
import com.regnosys.rosetta.generator.java.types.AttributeMetaType;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RJavaEnum;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.annotations.Accessor;
import com.rosetta.model.lib.annotations.AccessorType;
import com.rosetta.model.lib.annotations.Multi;
import com.rosetta.model.lib.annotations.Required;
import com.rosetta.model.lib.annotations.RosettaAttribute;
import com.rosetta.model.lib.annotations.RuneAttribute;
import com.rosetta.model.lib.annotations.RuneMetaType;
import com.rosetta.model.lib.annotations.RuneScopedAttributeKey;
import com.rosetta.model.lib.annotations.RuneScopedAttributeReference;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.BuilderProcessor;
import com.rosetta.model.lib.process.Processor;
import com.rosetta.util.ListEquals;
import com.rosetta.util.types.JavaType;

import jakarta.inject.Inject;

public class ModelObjectBoilerPlate {
	@Inject
	private ModelObjectBuilderGenerator builderGenerator;
	@Inject
	private JavaTypeTranslator typeTranslator;
	@Inject
	private JavaTypeUtil typeUtil;

	public CodeRenderer builderBoilerPlate(JavaPojoInterface javaType, boolean extended, JavaClassScope scope) {
		return boilerPlate(javaType, extended, javaType.getSimpleName() + "Builder", scope);
	}

	public CodeRenderer boilerPlate(JavaPojoInterface javaType, boolean extended, JavaClassScope scope) {
		return boilerPlate(javaType, extended, javaType.getSimpleName(), scope);
	}

	private CodeRenderer boilerPlate(JavaPojoInterface javaType, boolean extended, String className, JavaClassScope scope) {
		Collection<JavaPojoProperty> properties = extended ? javaType.getOwnProperties() : javaType.getAllProperties();
		return out -> {
			renderEquals(out, javaType, extended, properties, scope);
			renderHashCode(out, extended, properties, scope);
			renderToString(out, extended, properties, className, scope);
		};
	}

	public String javaAnnotation(JavaPojoProperty prop) {
		if (Objects.equals(prop.getType(), typeUtil.REFERENCE)) {
			return "address";
		}
		return prop.getName();
	}

	public String javaRuneAnnotation(JavaPojoProperty prop) {
		if (Objects.equals(prop.getType(), typeUtil.REFERENCE)) {
			return "@ref:scoped";
		}
		return prop.getSerializedName();
	}

	public boolean addRuneMetaAnnotation(JavaPojoProperty prop) {
		return Objects.equals(prop.getType(), typeUtil.REFERENCE)
				|| Objects.equals(prop.getType(), typeUtil.META_FIELDS)
				|| (javaRuneAnnotation(prop).equals("@data") && typeTranslator.isValueRosettaModelObject(prop.getType()));
	}

	public boolean isScopedReference(JavaPojoProperty prop) {
		return prop.getAttributeMetaTypes().contains(AttributeMetaType.SCOPED_REFERENCE);
	}

	public boolean isScopedKey(JavaPojoProperty prop) {
		return prop.getAttributeMetaTypes().contains(AttributeMetaType.SCOPED_KEY);
	}

	/**
	 * The annotations that let the runtime find the accessor of the given kind for a property.
	 */
	public CodeRenderer attributeAnnotations(JavaPojoProperty prop, AccessorType accessorType, boolean multi) {
		return out -> {
			out.writeln("@", RosettaAttribute.class, "(", JavaLiteral.STRING(javaAnnotation(prop)), ")");
			out.writeln("@", Accessor.class, "(", AccessorType.class, ".", accessorType.name(), ")");
			if (prop.isRequired()) {
				out.writeln("@", Required.class);
			}
			if (multi) {
				out.writeln("@", Multi.class);
			}
			out.writeln("@", RuneAttribute.class, "(", JavaLiteral.STRING(javaRuneAnnotation(prop)), ")");
			if (isScopedReference(prop)) {
				out.writeln("@", RuneScopedAttributeReference.class);
			}
			if (isScopedKey(prop)) {
				out.writeln("@", RuneScopedAttributeKey.class);
			}
			if (addRuneMetaAnnotation(prop)) {
				out.writeln("@", RuneMetaType.class);
			}
		};
	}

	private void renderHashCodeContribution(CodeWriter out, JavaPojoProperty prop, JavaStatementScope scope) {
		GeneratedIdentifier id = scope.getIdentifierOrThrow(prop);
		if (typeUtil.getItemType(prop.getType()) instanceof RJavaEnum) {
			if (typeUtil.isList(prop.getType())) {
				out.writeln("_result = 31 * _result + (", id, " != null ? ", id, ".stream().map(", Object.class, "::getClass).map(", Class.class, "::getName).mapToInt(", String.class, "::hashCode).sum() : 0);");
			} else {
				out.writeln("_result = 31 * _result + (", id, " != null ? ", id, ".getClass().getName().hashCode() : 0);");
			}
		} else {
			out.writeln("_result = 31 * _result + (", id, " != null ? ", id, ".hashCode() : 0);");
		}
	}

	private void renderHashCode(CodeWriter out, boolean extended, Collection<JavaPojoProperty> properties, JavaClassScope scope) {
		JavaMethodScope methodScope = scope.createMethodScope("hashCode");
		out.writeln("@Override");
		out.writeln("public int hashCode() {");
		out.indented(() -> {
			out.writeln("int _result = ", extended ? "super.hashCode()" : "0", ";");
			properties.forEach(prop -> renderHashCodeContribution(out, prop, methodScope.getBodyScope()));
			out.writeln("return _result;");
		});
		out.writeln("}");
		out.newline();
	}

	private void renderToString(CodeWriter out, boolean extended, Collection<JavaPojoProperty> properties, String className, JavaClassScope scope) {
		JavaMethodScope methodScope = scope.createMethodScope("toString");
		out.writeln("@Override");
		out.writeln("public ", String.class, " toString() {");
		out.indented(() -> {
			out.writeln("return ", JavaLiteral.STRING(className + " {"), " +");
			out.indented(() -> {
				Iterator<JavaPojoProperty> it = properties.iterator();
				while (it.hasNext()) {
					JavaPojoProperty prop = it.next();
					out.write(JavaLiteral.STRING(prop.getName() + "="), " + this.", methodScope.getIdentifierOrThrow(prop), " +");
					if (it.hasNext()) {
						out.write(" \", \" +");
					}
					out.newline();
				}
			});
			out.writeln("'}'", extended ? " + \" \" + super.toString()" : "", ";");
		});
		out.writeln("}");
	}

	private void renderEquals(CodeWriter out, JavaPojoInterface javaType, boolean extended, Collection<JavaPojoProperty> properties, JavaClassScope scope) {
		JavaMethodScope methodScope = scope.createMethodScope("equals");
		out.writeln("@Override");
		out.writeln("public boolean equals(", Object.class, " o) {");
		out.indented(() -> {
			out.writeln("if (this == o) return true;");
			out.writeln("if (o == null || !(o instanceof ", RosettaModelObject.class, ") || !getType().equals(((", RosettaModelObject.class, ")o).getType())) return false;");
			if (extended) {
				out.writeln("if (!super.equals(o)) return false;");
			}
			out.newline();
			if (!properties.isEmpty()) {
				out.writeln(javaType, " _that = getType().cast(o);");
			}
			out.newline();
			properties.forEach(prop -> renderEqualsContribution(out, prop, methodScope.getBodyScope()));
			out.writeln("return true;");
		});
		out.writeln("}");
		out.newline();
	}

	private void renderEqualsContribution(CodeWriter out, JavaPojoProperty prop, JavaStatementScope scope) {
		GeneratedIdentifier id = scope.getIdentifierOrThrow(prop);
		String getter = prop.getOperationName(GET);
		if (typeUtil.isList(prop.getType())) {
			out.writeln("if (!", ListEquals.class, ".listEquals(", id, ", _that.", getter, "())) return false;");
		} else {
			out.writeln("if (!", Objects.class, ".equals(", id, ", _that.", getter, "())) return false;");
		}
	}

	public CodeRenderer processMethod(JavaPojoInterface javaType) {
		return out -> renderProcessMethod(out, javaType, Processor.class, prop -> typeUtil.getItemType(prop.getType()));
	}

	public CodeRenderer builderProcessMethod(JavaPojoInterface javaType) {
		return out -> renderProcessMethod(out, javaType, BuilderProcessor.class, builderGenerator::toBuilderTypeSingle);
	}

	private void renderProcessMethod(CodeWriter out, JavaPojoInterface javaType, Class<?> processorType, Function<JavaPojoProperty, JavaType> rosettaItemType) {
		out.writeln("@Override");
		out.writeln("default void process(", RosettaPath.class, " path, ", processorType, " processor) {");
		out.indented(() -> {
			for (JavaPojoProperty prop : javaType.getAllProperties()) {
				String getterName = prop.getOperationName(GET);
				if (typeTranslator.isRosettaModelObject(prop.getType())) {
					out.write("processRosetta(path.newSubPath(", JavaLiteral.STRING(prop.getName()), "), processor, ", rosettaItemType.apply(prop), ".class, ", getterName, "()");
				} else {
					out.write("processor.processBasic(path.newSubPath(", JavaLiteral.STRING(prop.getName()), "), ", typeUtil.getItemType(prop.getType()), ".class, ", getterName, "(), this");
				}
				renderMetaFlags(out, prop);
				out.writeln(");");
			}
		});
		out.writeln("}");
		out.newline();
	}

	private void renderMetaFlags(CodeWriter out, JavaPojoProperty prop) {
		if (prop.getMeta() != null) {
			out.write(", ", AttributeMeta.class, ".", prop.getMeta());
		}
	}
}
