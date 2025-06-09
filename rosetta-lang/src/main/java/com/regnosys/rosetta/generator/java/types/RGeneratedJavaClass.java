package com.regnosys.rosetta.generator.java.types;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public abstract class RGeneratedJavaClass<T> extends JavaClass<T> {
	private final String simpleName;
	private final JavaPackageName packageName;
	
	protected RGeneratedJavaClass(JavaPackageName packageName, String simpleName) {
		this.packageName = packageName;
		this.simpleName = simpleName;
	}
	
	public static <U> RGeneratedJavaClass<? extends U> create(JavaPackageName packageName, String simpleName, Class<U> superclassOrInterface) {
		return new SimpleGeneratedJavaClass<>(packageName, simpleName, superclassOrInterface);
	}
	public static <U> RGeneratedJavaClass<? extends U> create(JavaPackageName packageName, String simpleName, TypeReference<U> supertypeRef) {
		return new SimpleGeneratedJavaClass<>(packageName, simpleName, supertypeRef);
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (this.equals(other)) {
			return true;
		}
		if (this.getSuperclass().isSubtypeOf(other)) {
			return true;
		}
		if (this.getInterfaces().stream().anyMatch(t -> t.isSubtypeOf(other))) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		if (this.equals(other)) {
			return true;
		}
		if (this.getSuperclassDeclaration().extendsDeclaration(other)) {
			return true;
		}
		if (this.getInterfaceDeclarations().stream().anyMatch(t -> t.extendsDeclaration(other))) {
			return true;
		}
		return false;
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public Class<? extends T> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		throw new UnsupportedOperationException("Cannot load a generated class");
	}

	@Override
	public DottedPath getPackageName() {
		return packageName.getName();
	}
	
	private static class SimpleGeneratedJavaClass<U> extends RGeneratedJavaClass<U> {		
		private final Type supertype;
		private final Class<? super U> rawSupertype;

		private SimpleGeneratedJavaClass(JavaPackageName packageName, String simpleName, Type supertype, Class<? super U> rawSupertype) {
			super(packageName, simpleName);
			this.supertype = supertype;
			this.rawSupertype = rawSupertype;
		}
		public SimpleGeneratedJavaClass(JavaPackageName packageName, String simpleName, Class<U> supertype) {
			this(packageName, simpleName, supertype, supertype);
		}
		public SimpleGeneratedJavaClass(JavaPackageName packageName, String simpleName, TypeReference<U> supertypeRef) {
			this(packageName, simpleName, supertypeRef.getType(), JavaParameterizedType.extractRawClass(supertypeRef.getType()));
		}

		@Override
		public JavaTypeDeclaration<? super U> getSuperclassDeclaration() {
			if (rawSupertype.isInterface()) {
				return JavaClass.OBJECT;
			}
			return JavaTypeDeclaration.from(rawSupertype);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public JavaClass<? super U> getSuperclass() {
			if (rawSupertype.isInterface()) {
				return JavaClass.OBJECT;
			}
			return (JavaClass<? super U>) JavaClass.from(supertype, Collections.emptyMap());
		}
		
		@Override
		public List<JavaTypeDeclaration<?>> getInterfaceDeclarations() {
			if (rawSupertype.isInterface()) {
				return Arrays.asList(JavaTypeDeclaration.from(rawSupertype));
			}
			return Collections.emptyList();
		}
		
		@Override
		public List<JavaClass<?>> getInterfaces() {
			if (rawSupertype.isInterface()) {
				return Arrays.asList(JavaClass.from(supertype, Collections.emptyMap()));
			}
			return Collections.emptyList();
		}
	}
}
