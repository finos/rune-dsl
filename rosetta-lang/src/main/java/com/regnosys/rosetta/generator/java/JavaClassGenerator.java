package com.regnosys.rosetta.generator.java;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.generator.GenerationException;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.scoping.JavaGlobalScope;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.rosetta.util.types.JavaTypeDeclaration;

import jakarta.inject.Inject;

public abstract class JavaClassGenerator<T, C extends JavaTypeDeclaration<?>> {
	@Inject
	private ImportManagerExtension importManager;
	
	private List<GenerationException> generationExceptions = null;
	private Map<T, C> objectToTypeRepresentationMap = null;
	private JavaGlobalScope globalScope = null;
	
	protected abstract Stream<T> streamObjects(RosettaModel model);
	protected abstract EObject getSource(T object);
	protected abstract C createTypeRepresentation(T object);
	protected abstract StringConcatenationClient generate(T object, C typeRepresentation, String version, JavaClassScope scope);
	
	public void registerClassesAndMethods(RosettaModel model, JavaGlobalScope globalScope) {
		if (objectToTypeRepresentationMap != null) {
			throw new IllegalStateException("Method `registerClassesAndMethods` has been called a second time before calling `generateClasses`.");
		}
		this.generationExceptions = new ArrayList<>();
		this.objectToTypeRepresentationMap = new LinkedHashMap<>();
		this.globalScope = globalScope;
		streamObjects(model)
			.forEach(object -> {
				try {
					C typeRepresentation = createTypeRepresentation(object);
					globalScope.createClassScopeAndRegisterIdentifier(typeRepresentation);
					objectToTypeRepresentationMap.put(object, typeRepresentation);
				} catch (CancellationException e) {
					throw e;
				} catch (GenerationException e) {
					generationExceptions.add(e);
				} catch (Exception e) {
					EObject source = getSource(object);
					generationExceptions.add(new GenerationException(e.getMessage(), source.eResource().getURI(), source, e));
				}
			});
	}
	public void generateClasses(String version, IFileSystemAccess2 fsa, CancelIndicator cancelIndicator) {
		if (objectToTypeRepresentationMap == null) {
			throw new IllegalStateException("Method `generateClasses` has been called without calling `registerClassesAndMethods`.");
		}
		objectToTypeRepresentationMap.forEach((object, typeRepresentation) -> {
			if (cancelIndicator.isCanceled()) {
				throw new CancellationException();
			}
			try {
				JavaClassScope classScope = globalScope.getClassScope(typeRepresentation);
				StringConcatenationClient classCode = generate(object, typeRepresentation, version, classScope);
				String javaFileCode = importManager.buildClass(typeRepresentation.getPackageName(), classCode, classScope.getFileScope());
				fsa.generateFile(typeRepresentation.getCanonicalName().withForwardSlashes() + ".java", javaFileCode);
			} catch (CancellationException e) {
				throw e;
			} catch (GenerationException e) {
				generationExceptions.add(e);
			} catch (Exception e) {
				EObject source = getSource(object);
				generationExceptions.add(new GenerationException(e.getMessage(), source.eResource().getURI(), source, e));
			}
		});
		objectToTypeRepresentationMap = null;
	}
	
	public List<GenerationException> getGenerationExceptions() {
		return generationExceptions;
	}
}
