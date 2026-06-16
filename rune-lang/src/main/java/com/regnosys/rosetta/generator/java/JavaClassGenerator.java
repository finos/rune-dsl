package com.regnosys.rosetta.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.generator.GenerationException;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.rosetta.util.types.JavaTypeDeclaration;

public abstract class JavaClassGenerator<T, C extends JavaTypeDeclaration<?>> {
	protected abstract Stream<? extends T> streamObjects(RosettaModel model);
	protected abstract EObject getSource(T object);
	protected abstract C createTypeRepresentation(T object);
    // TODO: return InputStream instead (or pass in OutputStream)
	protected abstract String generate(T object, C typeRepresentation, String version, JavaClassScope scope);

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
					String javaFileCode = generate(object, typeRepresentation, version, classScope);
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
		return generationExceptions;
	}
}
