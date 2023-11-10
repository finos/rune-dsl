package com.rosetta.util.types;

import java.util.Objects;

import com.rosetta.util.DottedPath;

public class JavaInterface extends JavaClass {
	protected static class RunningJavaInterface extends JavaInterface {
		private final Class<?> runningClass;

		public RunningJavaInterface(Class<?> runningClass) {
			super(DottedPath.splitOnDots(runningClass.getCanonicalName()).parent(), runningClass.getSimpleName());
			this.runningClass = runningClass;
		}
		
		public Class<?> getRunningClass() {
			return runningClass;
		}
		
		@Override
		public boolean isAssignableFrom(JavaType other) {
			if (other instanceof RunningJavaInterface) {
				return runningClass.isAssignableFrom(((RunningJavaInterface)other).runningClass);
			}
			if (other instanceof RunningJavaClass) {
				return runningClass.isAssignableFrom(((RunningJavaClass)other).getRunningClass());
			}
			return this.equals(other);
		}
	}
	
	public JavaInterface(DottedPath packageName, String simpleName) {
		super(packageName, simpleName);
	}
	
	public static JavaInterface from(Class<?> t) {
		if (t.isArray() || t.isPrimitive() || !t.isInterface() || t.getSimpleName().equals("")) {
			return null;
		}
		return new RunningJavaInterface(t);
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
