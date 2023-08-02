package com.rosetta.util.types;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;


public class JavaParametrizedType implements JavaReferenceType {
	private final JavaClass baseType;
	private final List<JavaTypeArgument> arguments;
	
	public JavaParametrizedType(JavaClass baseType, JavaTypeArgument... arguments) {
		Validate.notNull(baseType);
		Validate.noNullElements(arguments);
		this.baseType = baseType;
		this.arguments = Arrays.asList(arguments);
	}
	
	public JavaClass getBaseType() {
		return baseType;
	}
	public List<JavaTypeArgument> getArguments() {
		return arguments;
	}
	
	@Override
	public String getSimpleName() {
		return baseType.getSimpleName();
	}
	
	@Override
	public String toString() {
		return baseType.toString() + "<" + arguments.stream().map(Object::toString).collect(Collectors.joining(", ")) + ">";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(baseType, arguments);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        JavaParametrizedType other = (JavaParametrizedType) object;
        return Objects.equals(baseType, other.baseType)
        		&& Objects.equals(arguments, other.arguments);
	}
	
	@Override
	public void accept(JavaTypeVisitor visitor) {
		visitor.visitType(this);
	}
}
