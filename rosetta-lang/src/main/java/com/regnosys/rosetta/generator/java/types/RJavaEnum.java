package com.regnosys.rosetta.generator.java.types;

import java.util.Collections;
import java.util.List;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class RJavaEnum extends JavaClass<Object> {	
	private final RosettaEnumeration enumeration;
	private final DottedPath packageName;

	public RJavaEnum(RosettaEnumeration enumeration) {
		this.enumeration = enumeration;
		this.packageName = DottedPath.splitOnDots(enumeration.getModel().getName());
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (other instanceof JavaPrimitiveType) {
			return false;
		}
		if (other.equals(JavaClass.OBJECT)) {
			return true;
		}
		return false;
	}

	@Override
	public String getSimpleName() {
		return enumeration.getName();
	}

	@Override
	public JavaClass<? super Object> getSuperclassDeclaration() {
		return JavaClass.OBJECT;
	}
	
	@Override
	public JavaClass<? super Object> getSuperclass() {
		return getSuperclassDeclaration();
	}

	@Override
	public List<JavaClass<?>> getInterfaceDeclarations() {
		return Collections.emptyList();
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getInterfaceDeclarations();
	}

	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		return false;
	}

	@Override
	public boolean isFinal() {
		return true;
	}

	@Override
	public Class<?> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return Class.forName(getCanonicalName().toString(), true, classLoader);
	}

	@Override
	public DottedPath getPackageName() {
		return packageName;
	}

}
