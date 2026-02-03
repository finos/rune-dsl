package com.regnosys.rosetta.generator.java.scoping;

import java.io.IOException;
import java.lang.module.ModuleReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;

public class JavaFileScope extends AbstractJavaScope<AbstractJavaScope<?>> {
	private static final DottedPath JAVA_LANG = DottedPath.of("java", "lang");
	private static volatile Set<String> JAVA_LANG_SIMPLE_NAMES;
	
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
		return getJavaLangSimpleNames().contains(desiredName);
	}

	private static Set<String> getJavaLangSimpleNames() {
		Set<String> cached = JAVA_LANG_SIMPLE_NAMES;
		if (cached != null) {
			return cached;
		}
		synchronized (JavaFileScope.class) {
			if (JAVA_LANG_SIMPLE_NAMES != null) {
				return JAVA_LANG_SIMPLE_NAMES;
			}
			Set<String> names = new HashSet<>();
			try (ModuleReader reader = ModuleLayer.boot()
					.configuration()
					.findModule("java.base")
					.orElseThrow()
					.reference()
					.open()) {
				reader.list()
						.filter(resource -> resource.startsWith("java/lang/") && resource.endsWith(".class"))
						.map(resource -> resource.substring("java/lang/".length(), resource.length() - ".class".length()))
						.filter(resource -> !resource.contains("$"))
						.forEach(names::add);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to read java.base module resources.", e);
			}
			JAVA_LANG_SIMPLE_NAMES = Collections.unmodifiableSet(names);
			return JAVA_LANG_SIMPLE_NAMES;
		}
	}
}
