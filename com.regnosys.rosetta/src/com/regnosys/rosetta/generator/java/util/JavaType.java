package com.regnosys.rosetta.generator.java.util;

public class JavaType {

	private String name;
	private String simpleName;

	public JavaType(String name) {
		this.name = name;
		String[] split = name.split("\\.");
		this.simpleName = split[split.length - 1];
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	public static JavaType create(String qName) {
		return new JavaType(qName);
	}

	@Override
	public String toString() {
		return getSimpleName();
	}
}
