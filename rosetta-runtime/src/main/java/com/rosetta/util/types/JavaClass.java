package com.rosetta.util.types;

import java.util.Objects;

import com.rosetta.util.DottedPath;


public class JavaClass implements JavaReferenceType {
	protected static class RunningJavaClass extends JavaClass {
		private final Class<?> runningClass;

		public RunningJavaClass(Class<?> runningClass) {
			super(DottedPath.splitOnDots(runningClass.getCanonicalName()).parent(), runningClass.getSimpleName());
			this.runningClass = runningClass;
		}
		
		public Class<?> getRunningClass() {
			return runningClass;
		}
		
		@Override
		public boolean isAssignableFrom(JavaType other) {
			if (other instanceof RunningJavaClass) {
				return getRunningClass().isAssignableFrom(((RunningJavaClass)other).getRunningClass());
			}
			return this.equals(other);
		}
	}
	
	private final DottedPath packageName;
	private final String simpleName;
	
	public JavaClass(DottedPath packageName, String simpleName) {
		Objects.requireNonNull(packageName);
		Objects.requireNonNull(simpleName);
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
		return new RunningJavaClass(t);
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

	public boolean isAssignableFrom(JavaType other) {
		return this.equals(other);
	}
}
