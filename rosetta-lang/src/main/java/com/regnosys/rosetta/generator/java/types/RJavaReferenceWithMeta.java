package com.regnosys.rosetta.generator.java.types;

import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.meta.ReferenceWithMeta;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;
import com.rosetta.util.types.RJavaWithMetaValue;

public class RJavaReferenceWithMeta extends JavaClass<RosettaModelObject> implements RJavaWithMetaValue {
	private final JavaReferenceType valueType;
	private final DottedPath namespace;
	private final JavaTypeUtil javaTypeUtil;
	private final JavaParameterizedType<ReferenceWithMeta<?>> referenceWithMetaParameterisedType;

	
	public RJavaReferenceWithMeta(JavaReferenceType valueType, DottedPath namespace, JavaTypeUtil javaTypeUtil) {
		this.valueType = valueType;
		this.namespace = namespace;
		this.javaTypeUtil = javaTypeUtil;
		referenceWithMetaParameterisedType = javaTypeUtil.wrap(javaTypeUtil.REFERENCE_WITH_META, valueType);
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (referenceWithMetaParameterisedType.isSubtypeOf(other)) {
			return true;
		}
		if (javaTypeUtil.ROSETTA_MODEL_OBJECT.isSubtypeOf(other)) {
			return true;
		}
		if (javaTypeUtil.GLOBAL_KEY.isSubtypeOf(other)) {
			return true;
		}
		return false;
	}

	@Override
	public String getSimpleName() {
		return "ReferenceWithMeta" + valueType.getSimpleName();
	}

	@Override
	public JavaTypeDeclaration<? super RosettaModelObject> getSuperclassDeclaration() {
		return JavaClass.OBJECT;
	}

	@Override
	public List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations() {
		return List.of(javaTypeUtil.ROSETTA_MODEL_OBJECT, javaTypeUtil.FIELD_WITH_META);

	}

	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		if (other instanceof JavaClass) {
			return this.isSubtypeOf((JavaClass<?>)other);
		}
		return false;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public Class<? extends RosettaModelObject> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return Class.forName(getCanonicalName().toString(), true, classLoader).asSubclass(RosettaModelObject.class);
	}

	@Override
	public DottedPath getPackageName() {
		return namespace;
	}

	@Override
	public JavaClass<? super RosettaModelObject> getSuperclass() {
		return JavaClass.OBJECT;
	}

	@Override
	public List<JavaClass<?>> getInterfaces() {
		return List.of(javaTypeUtil.ROSETTA_MODEL_OBJECT,referenceWithMetaParameterisedType);

	}

	@Override
	public JavaReferenceType getValueType() {
		return valueType;
	}
}
