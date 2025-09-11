package com.regnosys.rosetta.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.generator.GenerationException;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.rosetta.util.types.JavaTypeDeclaration;

import jakarta.inject.Inject;

public abstract class JavaClassGenerator<T, C extends JavaTypeDeclaration<?>> {
	@Inject
	private ImportManagerExtension importManager;
	
	protected abstract Stream<? extends T> streamObjects(RosettaModel model);
	protected abstract EObject getSource(T object);
	protected abstract C createTypeRepresentation(T object);
	protected abstract StringConcatenationClient generate(T object, C typeRepresentation, String version, JavaClassScope scope);

	public List<GenerationException> generateClasses(RosettaModel model, String version, IFileSystemAccess2 fsa, CancelIndicator cancelIndicator) {
		List<GenerationException> generationExceptions = new ArrayList<>();
		streamObjects(model)
			.forEach(object -> {
				if (cancelIndicator.isCanceled()) {
					throw new CancellationException();
				}
				try {
					C typeRepresentation = createTypeRepresentation(object);
					JavaClassScope classScope = JavaClassScope.createAndRegisterIdentifier(typeRepresentation);
					StringConcatenationClient classCode = generate(object, typeRepresentation, version, classScope);
					String javaFileCode = importManager.buildClass(typeRepresentation.getPackageName(), classCode, classScope.getFileScope());
					fsa.generateFile(typeRepresentation.getCanonicalName().withForwardSlashes() + ".java", javaFileCode);
				} catch (CancellationException e) {
					throw e;
				} catch (GenerationException e) {
					generationExceptions.add(e);
				} catch (Exception e) {
					EObject source = getSource(object);
					URI sourceURI = source == null ? model.eResource().getURI() : source.eResource().getURI();
					generationExceptions.add(new GenerationException(e.getMessage(), sourceURI, source, e));
				}
			});
		return generationExceptions;
	}
}
