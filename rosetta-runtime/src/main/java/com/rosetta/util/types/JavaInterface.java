package com.rosetta.util.types;

import java.util.Objects;

import com.rosetta.util.DottedPath;

public class JavaInterface extends JavaClass {
	public JavaInterface(DottedPath packageName, String simpleName) {
		super(packageName, simpleName);
	}
	
	public static JavaInterface from(Class<?> t) {
		if (t.isArray() || t.isPrimitive() || !t.isInterface() || t.getSimpleName().equals("")) {
			return null;
		}
		DottedPath packageName = DottedPath.splitOnDots(t.getCanonicalName()).parent();
		String simpleName = t.getSimpleName();
		return new JavaInterface(packageName, simpleName);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

		JavaInterface other = (JavaInterface) object;
        return Objects.equals(getPackageName(), other.getPackageName())
        		&& Objects.equals(getSimpleName(), other.getSimpleName());
	}
	
	@Override
	public void accept(JavaTypeVisitor visitor) {
		visitor.visitType(this);
	}
}
