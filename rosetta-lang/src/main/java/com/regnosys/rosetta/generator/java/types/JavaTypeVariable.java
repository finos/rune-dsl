package com.regnosys.rosetta.generator.java.types;

import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

public class JavaTypeVariable implements JavaReferenceType {
	private final JavaClass declaringClass;
	private final String name;
	public JavaTypeVariable(JavaClass declaringClass, String name) {
		Validate.notNull(declaringClass);
		Validate.notNull(name);
		this.declaringClass = declaringClass;
		this.name = name;
	}
	
	public JavaClass getDeclaringClass() {
		return declaringClass;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append(name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(declaringClass, name);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        JavaTypeVariable other = (JavaTypeVariable) object;
        return Objects.equals(declaringClass, other.declaringClass)
        		&& Objects.equals(name, other.name);
	}
}
