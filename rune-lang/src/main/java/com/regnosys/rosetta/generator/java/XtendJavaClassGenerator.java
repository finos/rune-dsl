package com.regnosys.rosetta.generator.java;

import com.regnosys.rosetta.generator.java.scoping.JavaClassScope;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.rosetta.util.types.JavaTypeDeclaration;
import jakarta.inject.Inject;
import org.eclipse.xtend2.lib.StringConcatenationClient;

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
