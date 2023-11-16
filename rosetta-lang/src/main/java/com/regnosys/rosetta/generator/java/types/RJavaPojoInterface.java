package com.regnosys.rosetta.generator.java.types;

import java.util.Collections;
import java.util.List;

import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.TypeSystem;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class RJavaPojoInterface extends JavaClass<RosettaModelObject> {
	private static JavaClass<RosettaModelObject> ROSETTA_MODEL_OBJECT = JavaClass.from(RosettaModelObject.class);
	
	private final Data data;
	private final DottedPath packageName;
	
	private final TypeSystem typeSystem;

	public RJavaPojoInterface(Data data, TypeSystem typeSystem) {
		this.data = data;
		this.packageName = DottedPath.splitOnDots(data.getModel().getName());
		
		this.typeSystem = typeSystem;
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (other instanceof JavaPrimitiveType) {
			return false;
		}
		if (ROSETTA_MODEL_OBJECT.isSubtypeOf(other)) {
			return true;
		}
		if (other instanceof RJavaPojoInterface) {
			return typeSystem.isSubtypeOf(new RDataType(data), new RDataType(((RJavaPojoInterface)other).data));
		}
		return false;
	}

	@Override
	public String getSimpleName() {
		return data.getName();
	}

	@Override
	public JavaClass<? super RosettaModelObject> getSuperclassDeclaration() {
		return JavaClass.OBJECT;
	}
	
	@Override
	public JavaClass<? super RosettaModelObject> getSuperclass() {
		return getSuperclassDeclaration();
	}

	@Override
	public List<JavaClass<?>> getInterfaceDeclarations() {
		if (data.getSuperType() == null) {
			return List.of(ROSETTA_MODEL_OBJECT);
		}
		return Collections.singletonList(new RJavaPojoInterface(data.getSuperType(), typeSystem));
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getInterfaceDeclarations();
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
		return packageName;
	}

}
