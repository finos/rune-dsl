package com.rosetta.util.types;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.rosetta.util.DottedPath;


public class JavaClass implements JavaReferenceType {
	private final DottedPath packageName;
	private final String simpleName;
	
	public JavaClass(DottedPath packageName, String simpleName) {
		Validate.notNull(packageName);
		Validate.notNull(simpleName);
		this.packageName = packageName;
		this.simpleName = simpleName;
	}
	
	public static JavaClass from(Class<?> t) {
		if (t.isArray() || t.isPrimitive() || t.getSimpleName().equals("")) {
			return null;
		}
		if (t.isInterface()) {
			return JavaInterface.from(t);
		}
		String fullName = t.getSimpleName();
		Class<?> parent = t;
		while (parent.getDeclaringClass() != null) {
			parent = t.getDeclaringClass();
			fullName = parent.getSimpleName() + "." + fullName;
		}
		return new JavaClass(DottedPath.splitOnDots(t.getPackage().getName()), fullName);
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}
	
	public DottedPath getPackageName() {
		return packageName;
	}
	
	public DottedPath getCanonicalName() {
		return packageName.child(simpleName);
	}
	
	public Class<?> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return Class.forName(getCanonicalName().toString(), true, classLoader);
	}
	
	@Override
	public String toString() {
		return getCanonicalName().withDots();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(packageName, simpleName);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        JavaClass other = (JavaClass) object;
        return Objects.equals(packageName, other.packageName)
        		&& Objects.equals(simpleName, other.simpleName);
	}
	
	@Override
	public void accept(JavaTypeVisitor visitor) {
		visitor.visitType(this);
	}
}
