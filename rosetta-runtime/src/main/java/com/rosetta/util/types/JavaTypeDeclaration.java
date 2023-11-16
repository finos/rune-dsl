package com.rosetta.util.types;

import java.util.List;
import java.util.Map;

public interface JavaTypeDeclaration<T> {
	public static <T> JavaTypeDeclaration<T> from(Class<T> c) {
		if (c.getTypeParameters().length >= 1) {
			return JavaGenericTypeDeclaration.from(c);
		}
		return JavaClass.from(c);
	}
	
	JavaTypeDeclaration<? super T> getSuperclassDeclaration();
	List<? extends JavaTypeDeclaration<?>> getInterfaceDeclarations();
	boolean extendsDeclaration(JavaTypeDeclaration<?> other);
	boolean isFinal();
	JavaClass<T> applySubstitution(Map<JavaTypeVariable, JavaTypeArgument> substitution);
	Class<? extends T> loadClass(ClassLoader classLoader) throws ClassNotFoundException;
}
