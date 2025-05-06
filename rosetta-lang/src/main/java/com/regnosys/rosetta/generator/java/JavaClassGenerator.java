package com.regnosys.rosetta.generator.java;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.generator.IFileSystemAccess2;

import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaGlobalScope;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.rosetta.util.types.JavaTypeDeclaration;

import jakarta.inject.Inject;

public abstract class JavaClassGenerator<T, C extends JavaTypeDeclaration<?>> {
	@Inject
	private ImportManagerExtension importManager;
	
	private Map<T, C> objectToTypeRepresentationMap = null;
	private JavaGlobalScope globalScope = null;
	
	protected abstract Stream<T> streamObjects(RosettaModel model);
	protected abstract C createTypeRepresentation(T object);
	protected abstract void registerMethods(T object, C typeRepresentation, JavaClassScope scope);
	protected abstract StringConcatenationClient generate(T object, C typeRepresentation, String version, JavaClassScope scope);
	
	public void registerClassesAndMethods(RosettaModel model, JavaGlobalScope globalScope) {
		if (objectToTypeRepresentationMap != null) {
			throw new IllegalStateException("Method `registerClassesAndMethods` has been called a second time before calling `generateClasses`.");
		}
		this.objectToTypeRepresentationMap = new LinkedHashMap<>();
		this.globalScope = globalScope;
		streamObjects(model)
			.forEach(object -> {
				C typeRepresentation = createTypeRepresentation(object);
				JavaClassScope classScope = globalScope.createClassScopeAndIdentifier(typeRepresentation);
				registerMethods(object, typeRepresentation, classScope);
				objectToTypeRepresentationMap.put(object, typeRepresentation);
			});
	}
	public void generateClasses(String version, IFileSystemAccess2 fsa) {
		if (objectToTypeRepresentationMap == null) {
			throw new IllegalStateException("Method `generateClasses` has been called without calling `registerClassesAndMethods`.");
		}
		objectToTypeRepresentationMap.forEach((object, typeRepresentation) -> {
			JavaClassScope classScope = globalScope.getClassScope(typeRepresentation);
			StringConcatenationClient classCode = generate(object, typeRepresentation, version, classScope);
			String javaFileCode = importManager.buildClass(typeRepresentation.getPackageName(), classCode, null);
			fsa.generateFile(typeRepresentation.getCanonicalName().withForwardSlashes() + ".java", javaFileCode);
		});
		objectToTypeRepresentationMap = null;
	}
}
