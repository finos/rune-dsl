package com.regnosys.rosetta.generator.java.types;

import static com.rosetta.model.lib.SerializedNameConstants.*;

import java.util.Collection;
import java.util.List;

import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class RJavaFieldWithMeta extends RJavaWithMetaValue {
	private final DottedPath namespace;
	private final JavaTypeUtil typeUtil;
	private final JavaParameterizedType<FieldWithMeta<?>> fieldWithMetaParameterisedType;
	
	public RJavaFieldWithMeta(JavaReferenceType valueType, DottedPath namespace, JavaTypeUtil typeUtil) {
		super(valueType);
		this.namespace = namespace;
		this.typeUtil = typeUtil;
		fieldWithMetaParameterisedType = typeUtil.wrap(typeUtil.FIELD_WITH_META, valueType);
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (fieldWithMetaParameterisedType.isSubtypeOf(other)) {
			return true;
		}
		if (typeUtil.ROSETTA_MODEL_OBJECT.isSubtypeOf(other)) {
			return true;
		}
		if (typeUtil.GLOBAL_KEY.isSubtypeOf(other)) {
			return true;
		}
		return false;
	}

	@Override
	public String getSimpleName() {
		return "FieldWithMeta" + valueType.getSimpleName();
	}

	@Override
	public List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations() {
		return List.of(typeUtil.ROSETTA_MODEL_OBJECT, typeUtil.FIELD_WITH_META, typeUtil.GLOBAL_KEY);
	}

	@Override
	public DottedPath getPackageName() {
		return namespace;
	}

	@Override
	public List<JavaClass<?>> getInterfaces() {
		return List.of(typeUtil.ROSETTA_MODEL_OBJECT, fieldWithMetaParameterisedType, typeUtil.GLOBAL_KEY);
	}

	@Override
	public Collection<JavaPojoProperty> getOwnProperties() {
		return List.of(
				new JavaPojoProperty("value", null, DATA, "value", valueType, null, null, false, List.of()),
				new JavaPojoProperty("meta", null, META, "meta", typeUtil.META_FIELDS, null, null, false, List.of())
			);
	}	
}
