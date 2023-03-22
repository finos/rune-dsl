package com.regnosys.rosetta.generator.java.types;

import java.util.Objects;

import com.regnosys.rosetta.utils.DottedPath;

public class JavaInterface extends JavaClass {
	public JavaInterface(DottedPath packageName, String simpleName) {
		super(packageName, simpleName);
	}
	
	public static JavaInterface from(Class<?> t) {
		if (t.isArray() || t.isPrimitive() || !t.isInterface() || t.getSimpleName().equals("")) {
			return null;
		}
		String fullName = t.getSimpleName();
		Class<?> parent = t;
		while (parent.getDeclaringClass() != null) {
			parent = t.getDeclaringClass();
			fullName = parent.getSimpleName() + "." + fullName;
		}
		return new JavaInterface(DottedPath.splitOnDots(t.getPackageName()), fullName);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (this.getClass() != object.getClass()) return false;

		JavaInterface other = (JavaInterface) object;
        return Objects.equals(getPackageName(), other.getPackageName())
        		&& Objects.equals(getSimpleName(), other.getSimpleName());
	}
}
