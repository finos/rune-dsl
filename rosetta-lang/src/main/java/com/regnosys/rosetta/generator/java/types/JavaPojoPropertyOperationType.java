package com.regnosys.rosetta.generator.java.types;

public enum JavaPojoPropertyOperationType {
	GETTER("get"),
	GET_OR_CREATE("getOrCreate"),
	
	SETTER("set"),
	SETTER_VALUE("set", "Value"),
	
	ADDER("add"),
	ADDER_VALUE("add", "Value");
	
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
