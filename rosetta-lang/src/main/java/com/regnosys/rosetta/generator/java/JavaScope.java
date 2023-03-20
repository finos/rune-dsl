package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.GeneratorScope;
import com.regnosys.rosetta.generator.java.types.JavaClass;
import com.regnosys.rosetta.generator.java.types.JavaType;

import java.util.Optional;

import javax.lang.model.SourceVersion;

public class JavaScope extends GeneratorScope<JavaScope> {

	public JavaScope() {
		super();
	}
	protected JavaScope(JavaScope parent) {
		super(parent);
	}
	
	@Override
	public Optional<GeneratedIdentifier> getIdentifier(Object obj) {
		return super.getIdentifier(obj).or(() -> {
			JavaType t = JavaType.from(obj);
			if (t != null) {
				if (t instanceof JavaClass) {
					JavaClass clazz = (JavaClass)t;
					if (clazz.getPackageName().withDots().equals("java.lang")) {
						return Optional.of(new GeneratedIdentifier(this, clazz.getSimpleName()));
					}
				}
			}
			return Optional.empty();
		});
	}

	@Override
	public JavaScope childScope() {
		return new JavaScope(this);
	}

	@Override
	public boolean isValidIdentifier(String name) {
		return SourceVersion.isName(name);
	}
}
