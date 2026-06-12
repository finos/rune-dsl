package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.rosetta.util.types.JavaTypeDeclaration;
import jakarta.inject.Inject;
import org.eclipse.xtend2.lib.StringConcatenationClient;

/**
 * Base class for generators that produce a Java class using Xtend templates.
 *
 * @deprecated New generators should extend {@link FluentJavaClassGenerator}.
 * This class will be removed once all generators are migrated to the fluent API.
 */
@Deprecated
public abstract class XtendJavaClassGenerator<T, C extends JavaTypeDeclaration<?>> extends JavaClassGenerator<T, C> {
	@Inject
	private ImportManagerExtension importManager;

	protected abstract StringConcatenationClient generateClass(T object, C typeRepresentation, String version, JavaClassScope scope);

    @Override
    protected String generate(T object, C typeRepresentation, String version, JavaClassScope scope, JavaGeneratorErrorHandler errorHandler) {
        StringConcatenationClient classCode = generateClass(object, typeRepresentation, version, scope);
        return importManager.buildClass(typeRepresentation.getPackageName(), classCode, scope.getFileScope());
    }
}
