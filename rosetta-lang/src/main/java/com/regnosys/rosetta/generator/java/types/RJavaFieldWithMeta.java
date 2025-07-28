package com.regnosys.rosetta.generator.java.types;

import static com.rosetta.model.lib.SerializedNameConstants.*;

import java.util.Collection;
import java.util.List;

import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class RJavaFieldWithMeta extends RJavaWithMetaValue {
	private final JavaTypeUtil typeUtil;
	private final JavaParameterizedType<FieldWithMeta<?>> fieldWithMetaParameterisedType;
	
	public RJavaFieldWithMeta(JavaReferenceType valueType, JavaPackageName packageName, JavaTypeUtil typeUtil) {
		super(valueType, packageName, "FieldWithMeta" + valueType.getSimpleName(), typeUtil);
		this.typeUtil = typeUtil;
		this.fieldWithMetaParameterisedType = typeUtil.wrap(typeUtil.FIELD_WITH_META, valueType);
	}

	@Override
	public List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations() {
		return List.of(typeUtil.ROSETTA_MODEL_OBJECT, typeUtil.FIELD_WITH_META, typeUtil.GLOBAL_KEY);
	}

	@Override
	public List<JavaClass<?>> getInterfaces() {
		return List.of(typeUtil.ROSETTA_MODEL_OBJECT, fieldWithMetaParameterisedType, typeUtil.GLOBAL_KEY);
	}

	@Override
	public Collection<JavaPojoProperty> getOwnProperties() {
		return List.of(
				new JavaPojoProperty(this, "value", null, DATA, "value", "value", valueType, null, null, false, List.of(), false),
				new JavaPojoProperty(this, "meta", null, META, "meta", "meta", typeUtil.META_FIELDS, null, null, false, List.of(), false)
			);
	}
}
