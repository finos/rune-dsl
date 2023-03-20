package com.regnosys.rosetta.generator.java.util;

@Deprecated
public class JavaType {

	private final String packageName;
	private final String simpleName;

	public JavaType(String packageName, String simpleName) {
		this.packageName = packageName;
		this.simpleName = simpleName;
	}

	public String getSimpleName() {
		return simpleName;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getCanonicalName() {
		return packageName + "." + simpleName;
	}

	static JavaType create(String qualifiedName) {
		int lastDotIndex = qualifiedName.lastIndexOf(".");
		return new JavaType(qualifiedName.substring(0, lastDotIndex), qualifiedName.substring(lastDotIndex + 1));
	}

	@Override
	public String toString() {
		return getCanonicalName();
	}
	
	public JavaType toBuilderType() {
		return new JavaType(packageName, simpleName + "." + simpleName + "Builder");
	}
	public JavaType toImplType() {
		return new JavaType(packageName, simpleName + "." + simpleName + "Impl");
	}
	
	public JavaType toBuilderImplType() {
		return new JavaType(packageName, simpleName + "." + simpleName + "BuilderImpl");
	}
}
