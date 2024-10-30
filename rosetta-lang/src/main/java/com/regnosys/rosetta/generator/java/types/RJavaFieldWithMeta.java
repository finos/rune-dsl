package com.regnosys.rosetta.generator.java.types;

import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class RJavaFieldWithMeta extends RJavaWithMetaValue {
	private final DottedPath namespace;
	private final JavaTypeUtil javaTypeUtil;
	private final JavaParameterizedType<FieldWithMeta<?>> fieldWithMetaParameterisedType;

	
	public RJavaFieldWithMeta(JavaReferenceType valueType, DottedPath namespace, JavaTypeUtil javaTypeUtil) {
		super(valueType);
		this.namespace = namespace;
		this.javaTypeUtil = javaTypeUtil;
		fieldWithMetaParameterisedType = javaTypeUtil.wrap(javaTypeUtil.FIELD_WITH_META, valueType);
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (fieldWithMetaParameterisedType.isSubtypeOf(other)) {
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
		return "FieldWithMeta" + valueType.getSimpleName();
	}

	@Override
	public JavaTypeDeclaration<? super RosettaModelObject> getSuperclassDeclaration() {
		return JavaClass.OBJECT;
	}

	@Override
	public List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations() {
		return List.of(javaTypeUtil.ROSETTA_MODEL_OBJECT, javaTypeUtil.FIELD_WITH_META, javaTypeUtil.GLOBAL_KEY);
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
	public List<JavaClass<?>> getInterfaces() {
		return List.of(javaTypeUtil.ROSETTA_MODEL_OBJECT,fieldWithMetaParameterisedType, javaTypeUtil.GLOBAL_KEY);
	}	
	
}
