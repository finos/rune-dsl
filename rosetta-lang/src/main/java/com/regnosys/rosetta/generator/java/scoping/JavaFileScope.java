package com.regnosys.rosetta.generator.java.scoping;

import java.util.Optional;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;

public class JavaFileScope extends AbstractJavaScope<AbstractJavaScope<?>> {
	private static final DottedPath JAVA_LANG = DottedPath.of("java", "lang");
	
	private final DottedPath packageName;

	public JavaFileScope(String fileName, DottedPath packageName) {
		super("File[" + fileName + "]", null);
		this.packageName = packageName;
	}
	
	public DottedPath getPackageName() {
		return packageName;
	}
	
	@Override
	public JavaFileScope getFileScope() {
		return this;
	}
	
	@Override
	public Optional<GeneratedIdentifier> getIdentifier(Object obj) {
		return super.getIdentifier(obj)
				.or(() -> {
					if (obj instanceof JavaClass<?> c && JAVA_LANG.equals(c.getPackageName()) && !isNameExplicitlyTaken(c.getSimpleName())) {
						return Optional.of(this.overwriteIdentifier(c, c.getSimpleName()));
					}
					return Optional.empty();
				});
	}
	
	@Override
	public boolean isNameTaken(String desiredName) {
		return isNameExplicitlyTaken(desiredName) || isNameImplicitlyTaken(desiredName);
	}
	
	private boolean isNameExplicitlyTaken(String desiredName) {
		return super.isNameTaken(desiredName);
	}
	private boolean isNameImplicitlyTaken(String desiredName) {
		try {
			Class.forName(JAVA_LANG.child(desiredName).withDots());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
