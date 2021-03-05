package com.regnosys.rosetta.generator.java.util;

public class JavaType {

	private String name;
	private String simpleName;

	public JavaType(String name) {
		this.name = name;
		String[] split = name.split("\\.");
		this.simpleName = split[split.length - 1];
	}

	protected JavaType(String name, String simpleName) {
		super();
		this.name = name;
		this.simpleName = simpleName;
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

	static JavaType create(String qName) {
		return new JavaType(qName);
	}

	@Override
	public String toString() {
		return getSimpleName();
	}
	
	public JavaType toBuilderType() {
		return new JavaType(name+"."+simpleName+"Builder", simpleName + "." + simpleName+"Builder");
	}
	public JavaType toImplType() {
		return new JavaType(name+"."+simpleName+"Impl", simpleName + "." + simpleName+"Impl");
	}
	
	public JavaType toBuilderImplType() {
		return new JavaType(name+"."+simpleName+"BuilderImpl", simpleName + "." + simpleName+"BuilderImpl");
	}
}
