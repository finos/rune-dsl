package com.regnosys.rosetta.generator.java.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.generator.java.RosettaJavaPackages;
import com.regnosys.rosetta.generator.object.ExpandedAttribute;
import com.regnosys.rosetta.generator.object.ExpandedType;
import com.regnosys.rosetta.generator.util.RosettaAttributeExtensions;
import com.regnosys.rosetta.rosetta.RosettaExternalFunction;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.types.RAliasType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RParametrizedType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.builtin.RBasicType;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.types.builtin.RDateTimeType;
import com.regnosys.rosetta.types.builtin.RDateType;
import com.regnosys.rosetta.types.builtin.RNumberType;
import com.regnosys.rosetta.types.builtin.RRecordType;
import com.regnosys.rosetta.types.builtin.RStringType;
import com.regnosys.rosetta.types.builtin.RZonedDateTimeType;
import com.regnosys.rosetta.utils.DottedPath;

public class JavaTypeTranslator {
	@Inject
	private RBuiltinTypeService builtins;
	@Inject
	private RosettaJavaPackages packages;
	@Inject
	private RosettaExtensions extensions;
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private TypeSystem typeSystem;
	
	private JavaClass listClass = JavaClass.from(List.class);
	private JavaClass objectClass = JavaClass.from(Object.class);
	
	private DottedPath getModelPackage(RosettaNamed object) {
		RosettaRootElement rootElement = EcoreUtil2.getContainerOfType(object, RosettaRootElement.class);
		RosettaModel model = rootElement.getModel();
		if (model == null)
			// Artificial attributes
			throw new IllegalArgumentException("Can not compute package name for " + object.eClass().getName() + " " + object.getName() + ". Element is not attached to a RosettaModel.");
		return modelPackage(model);
	}
	private JavaClass rosettaNamedToJavaClass(RosettaNamed object) {
		return new JavaClass(getModelPackage(object), object.getName());
	}
	
	public JavaParametrizedType toPolymorphicList(JavaReferenceType t) {
		return new JavaParametrizedType(listClass, JavaWildcardTypeArgument.extendsBound(t));
	}
	
	public JavaClass toFunctionJavaClass(Function func) {
		return new JavaClass(functions(getModelPackage(func)), func.getName());
	}
	public JavaClass toFunctionJavaClass(RosettaExternalFunction func) {
		return new JavaClass(packages.defaultLibFunctions(), func.getName());
	}
	
	public JavaClass toMetaJavaType(Attribute attribute) {
		JavaReferenceType attrType = toJavaReferenceType(typeProvider.getRTypeOfSymbol(attribute));
		String attributeTypeName = attrType.getSimpleName();
		String name;
		if (extensions.hasMetaFieldAnnotations(attribute)) {
			name = "FieldWithMeta" + attributeTypeName;
		} else {
			name = "ReferenceWithMeta" + attributeTypeName;
		}
		DottedPath pkg = metaField(getModelPackage(attribute.getTypeCall().getType()));
		return new JavaClass(pkg, name);
	}
	public JavaReferenceType toMetaJavaType(ExpandedAttribute expAttr) {
		JavaReferenceType attrType;
		if (expAttr.getRosettaType() != null) {
			attrType = toJavaReferenceType(typeSystem.typeCallToRType(expAttr.getRosettaType()));
		} else {
			attrType = expandedTypeToJavaType(expAttr.getType());
		}
		if (!expAttr.hasMetas()) {
			return attrType;
		}
		String attributeTypeName = attrType.getSimpleName();
		String name;
		if (expAttr.refIndex() < 0) {
			name = "FieldWithMeta" + attributeTypeName;
		} else {
			name = "ReferenceWithMeta" + attributeTypeName;
		}
		
		DottedPath pkg = metaField(getModelPackage(expAttr.getRosettaType().getType()));
		return new JavaClass(pkg, name);
	}
	public JavaReferenceType expandedTypeToJavaType(ExpandedType type) {
		if (type.getName().equals(RosettaAttributeExtensions.METAFIELDS_CLASS_NAME) || type.getName().equals(RosettaAttributeExtensions.META_AND_TEMPLATE_FIELDS_CLASS_NAME)) {
			return new JavaClass(packages.basicMetafields(), type.getName());
		}
		if (type.isMetaType()) {//TODO ExpandedType needs to store the underlying type for meta types if we want them to be anything other than strings
			return JavaClass.from(String.class);
		}
		if (type.isBuiltInType()) {
			throw new UnsupportedOperationException("Cannot convert expanded type " + type + " to a Java type.");
		}
		return new JavaClass(modelPackage(type.getModel()), type.getName());
	}
	
	private String getTypeDebugInfo(RType type) {
		return type.toString() + " (" + type.getClass().getSimpleName() + ")";
	}
	public JavaReferenceType toJavaReferenceType(RType type) {
		JavaType jt = toJavaType(type);
		if (jt instanceof JavaPrimitiveType) {
			return ((JavaPrimitiveType)jt).toReferenceType();
		} else if (jt instanceof JavaReferenceType) {
			return (JavaReferenceType)jt;
		} else {
			throw new UnsupportedOperationException("Cannot convert type " + getTypeDebugInfo(type) + " to a Java reference type.");
		}
	}
	public JavaReferenceType toJavaReferenceType(Optional<RType> type) {
		return type.map(t -> toJavaReferenceType(t)).orElse(objectClass);
	}
	public JavaType toJavaType(RType type) {
		if (type instanceof RAliasType) {
			return toJavaType((RAliasType)type);
		} else if (type instanceof RDataType) {
			return toJavaType((RDataType)type);
		} else if (type instanceof REnumType) {
			return toJavaType((REnumType)type);
		} else if (type instanceof RParametrizedType) {
			return toJavaType((RParametrizedType)type);
		} else if (type instanceof RRecordType) {
			return toJavaType((RRecordType)type);
		} else {
			throw new UnsupportedOperationException("Cannot convert type " + getTypeDebugInfo(type) + " to a Java type.");
		}
	}
	public JavaType toJavaType(Optional<RType> type) {
		return type.map(t -> toJavaType(t)).orElse(objectClass);
	}
	public JavaClass toJavaType(RDataType type) {
		return rosettaNamedToJavaClass(type.getData());
	}
	public JavaClass toJavaType(REnumType type) {
		return rosettaNamedToJavaClass(type.getEnumeration());
	}
	public JavaType toJavaType(RParametrizedType type) {
		if (type instanceof RBasicType) {
			return toJavaType((RBasicType)type);
		} else if (type instanceof RAliasType) {
			return toJavaType((RAliasType)type);
		} else {
			throw new UnsupportedOperationException("Cannot convert builtin type " + getTypeDebugInfo(type) + " to a Java type.");
		}
	}
	public JavaType toJavaType(RBasicType type) {
		if (type.equals(builtins.BOOLEAN)) {
			return JavaPrimitiveType.BOOLEAN;
		} else if (type.equals(builtins.TIME)) {
			return JavaClass.from(LocalTime.class);
		} else if (type.equals(builtins.NOTHING)) {
			return JavaClass.from(Void.class);
		} else if (type.equals(builtins.ANY)) {
			return objectClass;
		} else if (type instanceof RNumberType) {
			return toJavaType((RNumberType)type);
		} else if (type instanceof RStringType) {
			return toJavaType((RStringType)type);
		} else {
			throw new UnsupportedOperationException("Cannot convert basic type " + getTypeDebugInfo(type) + " to a Java type.");
		}
	}
	public JavaType toJavaType(RNumberType type) {
		if (!type.isInteger()) {
			return JavaClass.from(BigDecimal.class);
		} else {
			int digits = type.getDigits().orElse(9);
			if (digits <= 9) {
				return JavaPrimitiveType.INT;
			} else if (digits <= 18) {
				return JavaPrimitiveType.LONG;
			} else {
				return JavaClass.from(BigInteger.class);
			}
		}
	}
	public JavaClass toJavaType(RStringType type) {
		return JavaClass.from(String.class);
	}
	public JavaType toJavaType(RAliasType type) {
		return toJavaType(type.getRefersTo());
	}
	public JavaClass toJavaType(RRecordType type) {
		if (type instanceof RDateType) {
			return toJavaType((RDateType)type);
		} else if (type instanceof RDateTimeType) {
			return toJavaType((RDateTimeType)type);
		} else if (type instanceof RZonedDateTimeType) {
			return toJavaType((RZonedDateTimeType)type);
		} else {
			throw new UnsupportedOperationException("Cannot convert record type " + getTypeDebugInfo(type) + " to a Java type.");
		}
	}
	public JavaClass toJavaType(RDateType type) {
		return JavaClass.from(com.rosetta.model.lib.records.Date.class);
	}
	public JavaClass toJavaType(RDateTimeType type) {
		return JavaClass.from(LocalDateTime.class);
	}
	public JavaClass toJavaType(RZonedDateTimeType type) {
		return JavaClass.from(ZonedDateTime.class);
	}
	
	public JavaType toPolymorphicListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return toPolymorphicList(toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	public JavaType toListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return new JavaParametrizedType(listClass, toJavaReferenceType(type));
		} else
			return toJavaReferenceType(type);
	}
	
	public JavaClass toImplType(JavaClass type) {
		return new JavaClass(type.getPackageName(), type.getSimpleName() + "." + type.getSimpleName() + "Impl");
	}
	public JavaClass toBuilderType(JavaClass type) {
		return new JavaClass(type.getPackageName(), type.getSimpleName() + "." + type.getSimpleName() + "Builder");
	}
	public JavaClass toBuilderImplType(JavaClass type) {
		return new JavaClass(type.getPackageName(), type.getSimpleName() + "." + type.getSimpleName() + "BuilderImpl");
	}
	
	private DottedPath modelPackage(RosettaModel model) {
		return DottedPath.splitOnDots(model.getName());
	}
	private DottedPath metaField(DottedPath p) {
		return p.child("metafields");
	}
	private DottedPath functions(DottedPath p) {
		return p.child("functions");
	}
}
