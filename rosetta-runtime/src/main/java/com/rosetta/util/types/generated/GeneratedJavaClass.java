package com.rosetta.util.types.generated;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class GeneratedJavaClass<T> extends JavaClass<T> {
	private final DottedPath packageName;
	private final String simpleName;
	private final Class<T> superType;
	
	public GeneratedJavaClass(DottedPath packageName, String simpleName, Class<T> superType) {
		Objects.requireNonNull(packageName);
		Objects.requireNonNull(simpleName);
		Objects.requireNonNull(superType);
		this.packageName = packageName;
		this.simpleName = simpleName;
		this.superType = superType;
	}
	
	@Override
	public boolean isSubtypeOf(JavaType other) {
		return this.equals(other);
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}
	
	@Override
	public DottedPath getPackageName() {
		return packageName;
	}

	@Override
	public Class<? extends T> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return Class.forName(getCanonicalName().toString(), true, classLoader).asSubclass(superType);
	}

	@Override
	public JavaClass<? super T> getSuperclassDeclaration() {
		if (superType.isInterface()) {
			return JavaClass.OBJECT;
		}
		return JavaClass.from(superType);
	}
	
	@Override
	public JavaClass<? super T> getSuperclass() {
		return getSuperclassDeclaration();
	}
	
	@Override
	public List<JavaClass<?>> getInterfaceDeclarations() {
		if (superType.isInterface()) {
			return Arrays.asList(JavaClass.from(superType));
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getInterfaceDeclarations();
	}

	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		return this.equals(other) || OBJECT.equals(other);
	}

	@Override
	public boolean isFinal() {
		return false;
	}
}
