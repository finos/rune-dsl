package com.regnosys.rosetta.types;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;

public class RShortcut implements RAssignedRoot {
	private String name;
	private String definition;
	private RosettaExpression expression;
	
	public RShortcut(String name, String definition, RosettaExpression expression) {
		this.name = name;
		this.definition = definition;
		this.expression = expression;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getDefinition() {
		return definition;
	}

	public RosettaExpression getExpression() {
		return expression;
	}

	@Override
	public int hashCode() {
		return Objects.hash(definition, expression, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RShortcut other = (RShortcut) obj;
		return Objects.equals(definition, other.definition) && Objects.equals(expression, other.expression)
				&& Objects.equals(name, other.name);
	}	
}
