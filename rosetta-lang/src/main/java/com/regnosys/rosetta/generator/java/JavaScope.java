package com.regnosys.rosetta.generator.java;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.GeneratorScope;
import com.regnosys.rosetta.generator.ImplicitVariableRepresentation;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.lang.model.SourceVersion;

public class JavaScope extends GeneratorScope<JavaScope> {
	private final Set<DottedPath> defaultPackages = new HashSet<>();
	private final Set<BlueprintImplicitVariableRepresentation> blueprintVars = new HashSet<>();
	
	public JavaScope(DottedPath packageName) {
		super("Package[" + packageName.withDots() + "]");
		this.defaultPackages.add(DottedPath.of("java", "lang"));
		this.defaultPackages.add(packageName);
	}
	protected JavaScope(String description, JavaScope parent) {
		super(description, parent);
	}

	@Override
	public JavaScope childScope(String description) {
		return new JavaScope(description, this);
	}
	public JavaScope classScope(String className) {
		return childScope("Class[" + className + "]");
	}
	public JavaScope methodScope(String methodName) {
		return childScope("Method[" + methodName + "]");
	}
	public JavaScope lambdaScope() {
		return childScope("Lambda[]");
	}

	@Override
	public boolean isValidIdentifier(String name) {
		return SourceVersion.isName(name);
	}
	
	// Make sure identifiers from package "java.lang" are always in scope.
	@Override
	public Optional<GeneratedIdentifier> getIdentifier(Object obj) {
		return super.getIdentifier(obj).or(() -> {
			JavaType t = JavaType.from(obj);
			if (t != null) {
				if (t instanceof JavaClass) {
					JavaClass clazz = (JavaClass)t;
					String desiredName = clazz.getSimpleName();
					if (this.getIdentifiers().stream().anyMatch(id -> id.getDesiredName().equals(desiredName))) {
						return Optional.of(overwriteIdentifier(clazz, clazz.getCanonicalName().withDots()));
					}
					if (this.defaultPackages.contains(clazz.getPackageName())) {
						return Optional.of(overwriteIdentifier(clazz, clazz.getSimpleName()));
					}
					return Optional.empty();
				}
			}
			if (obj instanceof BlueprintImplicitVariableRepresentation) {
				BlueprintImplicitVariableRepresentation repr = (BlueprintImplicitVariableRepresentation)obj;
				return this.getAllBlueprintVars()
						.filter(otherRepr -> repr.match(otherRepr))
						.findFirst()
						.flatMap(otherRepr -> getIdentifier(otherRepr))
						.or(() -> getIdentifier(new ImplicitVariableRepresentation(repr.getType().getData())));
			}
			return Optional.empty();
		});
	}
	
	@Override
	public GeneratedIdentifier createIdentifier(Object obj, String name) {
		GeneratedIdentifier id = super.createIdentifier(obj, name);
		if (obj instanceof BlueprintImplicitVariableRepresentation) {
			this.blueprintVars.add((BlueprintImplicitVariableRepresentation)obj);
		}
		return id;
	}
	
	private Stream<BlueprintImplicitVariableRepresentation> getAllBlueprintVars() {
		return Streams.concat(
				this.getParent().map(p -> p.getAllBlueprintVars()).orElseGet(() -> Stream.empty()),
				this.blueprintVars.stream()
			);
	}
}
