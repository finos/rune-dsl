package com.regnosys.rosetta.generator.java;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
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
	protected abstract String generate(T object, C typeRepresentation, String version, JavaClassScope scope, JavaGeneratorErrorHandler errorHandler);
    
    protected JavaGeneratorErrorHandler createErrorHandler(Consumer<GenerationException> addGenerationException, T object, RosettaModel model) {
        return new ErrorHandler(addGenerationException, object, model);
    }

	public List<GenerationException> generateClasses(RosettaModel model, String version, IFileSystemAccess2 fsa, CancelIndicator cancelIndicator) {
		List<GenerationException> generationExceptions = new ArrayList<>();
		streamObjects(model)
			.forEach(object -> {
				if (cancelIndicator.isCanceled()) {
					throw new CancellationException();
				}
                JavaGeneratorErrorHandler errorHandler = createErrorHandler(generationExceptions::add, object, model);
				try {
					C typeRepresentation = createTypeRepresentation(object);
					JavaClassScope classScope = JavaClassScope.createAndRegisterIdentifier(typeRepresentation);
					String javaFileCode = generate(object, typeRepresentation, version, classScope, errorHandler);
					fsa.generateFile(typeRepresentation.getCanonicalName().withForwardSlashes() + ".java", javaFileCode);
				} catch (CancellationException e) {
					throw e;
				} catch (GenerationException e) {
					generationExceptions.add(e);
				} catch (Exception e) {
                    errorHandler.handleError(e);
				}
			});
		return generationExceptions;
	}
    
    private class ErrorHandler implements JavaGeneratorErrorHandler {
        private final Consumer<GenerationException> addGenerationException;
        private final T object;
        private final RosettaModel model;
        
        public ErrorHandler(Consumer<GenerationException> addGenerationException, T object, RosettaModel model) {
            this.addGenerationException = addGenerationException;
            this.object = object;
            this.model = model;
        }
        
        private URI getURI() {
            return object == null ? model.eResource().getURI() : getSource(object).eResource().getURI();
        }
        
        @Override
        public void handleError(String message) {
            handleError(message, getSource(object));
        }

        @Override
        public void handleError(String message, EObject source) {
            addGenerationException.accept(new GenerationException(message, getURI(), source));
        }

        @Override
        public void handleError(String message, Throwable cause) {
            handleError(message, getSource(object), cause);
        }

        @Override
        public void handleError(String message, EObject source, Throwable cause) {
            addGenerationException.accept(new GenerationException(message, getURI(), source, cause));
        }

        @Override
        public void handleError(Throwable cause) {
            handleError(getSource(object), cause);
        }

        @Override
        public void handleError(EObject source, Throwable cause) {
            handleError(cause.getMessage(), source, cause);
        }
    }
}
