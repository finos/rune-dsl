package com.regnosys.rosetta.generator.java.util;

import java.util.List;

public class ParameterizedType {
	private final JavaType type;
	private final List<ParameterizedType> typeArgs;
	public ParameterizedType(JavaType type, List<ParameterizedType> typeArgs) {
		super();
		this.type = type;
		this.typeArgs = typeArgs;
	}
	public JavaType getType() {
		return type;
	}
	public List<ParameterizedType> getTypeArgs() {
		return typeArgs;
	}
}
