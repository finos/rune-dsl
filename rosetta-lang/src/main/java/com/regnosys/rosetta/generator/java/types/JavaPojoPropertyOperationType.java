package com.regnosys.rosetta.generator.java.types;

public enum JavaPojoPropertyOperationType {
	GET("get"),
	GET_OR_CREATE("getOrCreate"),
	
	SET("set"),
	SET_VALUE("set", "Value"),
	
	ADD("add"),
	ADD_VALUE("add", "Value");
	
	private final String prefix;
	private final String postfix;
	
	private JavaPojoPropertyOperationType(String prefix) {
		this(prefix, "");
	}
	private JavaPojoPropertyOperationType(String prefix, String postfix) {
		this.prefix = prefix;
		this.postfix = postfix;
	}
	
	public String getPrefix() {
		return prefix;
	}
	public String getPostfix() {
		return postfix;
	}
}
