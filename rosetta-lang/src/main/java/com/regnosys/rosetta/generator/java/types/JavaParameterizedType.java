package com.regnosys.rosetta.generator.java.types;

import java.util.List;
import java.util.Objects;

import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;


public class JavaParameterizedType implements JavaType {
	private final JavaClass baseType;
	private final List<JavaTypeArgument> arguments;
	
	public JavaParameterizedType(JavaClass baseType, JavaTypeArgument... arguments) {
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

        JavaParameterizedType other = (JavaParameterizedType) object;
        return Objects.equals(baseType, other.baseType)
        		&& Objects.equals(arguments, other.arguments);
	}
}
