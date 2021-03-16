package com.regnosys.rosetta.generator.java.util;

import java.util.List;
import java.util.stream.Collectors;

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
	
	public ParameterizedType extendedArgs() {
		if (typeArgs==null || typeArgs.isEmpty()) {
			return new ParameterizedType(type.asExtended(), typeArgs);
		}
		else {
			return new ParameterizedType(type, typeArgs.stream().map(p->p.extendedArgs()).collect(Collectors.toList()));
		}
	}
	public ParameterizedType extendedParam() {
		if (typeArgs==null || typeArgs.isEmpty()) {
			return this;
		}
		else {
			return new ParameterizedType(type, typeArgs.stream().map(p->p.extendedArgs()).collect(Collectors.toList()));
		}
	}
}
