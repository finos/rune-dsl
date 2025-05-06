package com.regnosys.rosetta.generator.java.scoping;

public class JavaFileScope extends AbstractJavaScope<JavaPackageScope> {

	protected JavaFileScope(String fileName, JavaPackageScope parentScope) {
		super("File[" + fileName + "]", parentScope);
	}

}
