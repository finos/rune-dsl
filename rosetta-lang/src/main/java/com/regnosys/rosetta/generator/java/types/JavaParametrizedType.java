package com.regnosys.rosetta.generator.java.types;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;


public class JavaParametrizedType implements JavaType {
	private final JavaClass baseType;
	private final List<JavaTypeArgument> arguments;
	
	public JavaParametrizedType(JavaClass baseType, JavaTypeArgument... arguments) {
		Validate.notNull(baseType);
		Validate.noNullElements(arguments);
		this.baseType = baseType;
		this.arguments = List.of(arguments);
	}
	
	public JavaClass getBaseType() {
		return baseType;
	}
	public List<JavaTypeArgument> getArguments() {
		return arguments;
	}
	
	@Override
	public String toString() {
		return baseType.toString() + "<" + arguments.stream().map(Object::toString).collect(Collectors.joining(", ")) + ">";
	}
	
	@Override
	public void appendTo(TargetStringConcatenation target) {
		baseType.appendTo(target);
		target.append("<");
		if (!arguments.isEmpty()) {
			arguments.get(0).appendTo(target);
			for (int i=1; i<arguments.size(); i++) {
				target.append(", ");
				arguments.get(i).appendTo(target);
			}
		}
		target.append(">");
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(baseType, arguments);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
        if (this.getClass() != object.getClass()) return false;

        JavaParametrizedType other = (JavaParametrizedType) object;
        return Objects.equals(baseType, other.baseType)
        		&& Objects.equals(arguments, other.arguments);
	}
}
