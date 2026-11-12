package com.regnosys.rosetta.generator.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.generator.GenerationException;
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaTypeDeclaration;

public abstract class JavaClassGenerator<T, C extends JavaTypeDeclaration<?>> {
	protected abstract Stream<? extends T> streamObjects(RosettaModel model);
	protected abstract EObject getSource(T object);
	protected abstract C createTypeRepresentation(T object);
    // TODO: return InputStream instead (or pass in OutputStream)
	protected abstract String generate(T object, C typeRepresentation, String version, JavaClassScope scope);

	public Stream<C> streamTypeRepresentations(RosettaModel model) {
		return streamObjects(model).map(this::createTypeRepresentation);
	}

	public List<GenerationException> generateClasses(RosettaModel model, String version, IFileSystemAccess2 fsa, CancelIndicator cancelIndicator) {
		List<GenerationException> generationExceptions = new ArrayList<>();
		Map<DottedPath, Set<String>> packageTypeNames = new HashMap<>();
		prepareGeneration(model, version, fsa, cancelIndicator, packageTypeNames, generationExceptions).run();
		return generationExceptions;
	}

	public Runnable prepareGeneration(
			RosettaModel model,
			String version,
			IFileSystemAccess2 fsa,
			CancelIndicator cancelIndicator,
			Map<DottedPath, Set<String>> packageTypeNames,
			List<GenerationException> generationExceptions) {
		List<PreparedClass<T, C>> preparedClasses = new ArrayList<>();
		streamObjects(model)
			.forEach(object -> {
				if (cancelIndicator.isCanceled()) {
					throw new CancellationException();
				}
				try {
					C typeRepresentation = createTypeRepresentation(object);
					packageTypeNames
							.computeIfAbsent(typeRepresentation.getPackageName(), k -> new HashSet<>())
							.add(typeRepresentation.getSimpleName());
					preparedClasses.add(new PreparedClass<>(object, typeRepresentation));
				} catch (CancellationException e) {
					throw e;
				} catch (GenerationException e) {
					generationExceptions.add(e);
				} catch (Exception e) {
					EObject source = getSource(object);
					URI uri = source != null && source.eResource() != null ? source.eResource().getURI() : model.eResource().getURI();
					generationExceptions.add(new GenerationException(e.getMessage(), uri, source, e));
				}
			});
		return () -> {
			for (PreparedClass<T, C> prepared : preparedClasses) {
				if (cancelIndicator.isCanceled()) {
					throw new CancellationException();
				}
				try {
					C typeRepresentation = prepared.typeRepresentation();
					Set<String> typesInPackage = packageTypeNames.getOrDefault(typeRepresentation.getPackageName(), Set.of());
					JavaClassScope classScope = JavaClassScope.createAndRegisterIdentifier(typeRepresentation, typesInPackage);
					String javaFileCode = generate(prepared.object(), typeRepresentation, version, classScope);
					fsa.generateFile(typeRepresentation.getCanonicalName().withForwardSlashes() + ".java", javaFileCode);
				} catch (CancellationException e) {
					throw e;
				} catch (GenerationException e) {
					generationExceptions.add(e);
				} catch (Exception e) {
					EObject source = getSource(prepared.object());
					URI uri = source != null && source.eResource() != null ? source.eResource().getURI() : model.eResource().getURI();
					generationExceptions.add(new GenerationException(e.getMessage(), uri, source, e));
				}
			}
		};
	}

	private record PreparedClass<T, C>(T object, C typeRepresentation) {}
}
