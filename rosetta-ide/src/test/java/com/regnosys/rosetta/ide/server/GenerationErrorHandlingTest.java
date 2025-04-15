package com.regnosys.rosetta.ide.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.generator.java.JavaScope;
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.ide.RosettaIdeModule;
import com.regnosys.rosetta.ide.RosettaIdeSetup;
import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerValidationTest;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.rosetta.util.types.JavaType;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry;
import org.eclipse.xtext.resource.impl.ResourceServiceProviderRegistryImpl;
import org.eclipse.xtext.util.Modules2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;
import java.util.List;

public class GenerationErrorHandlingTest extends AbstractRosettaLanguageServerValidationTest {

    @Override
    protected Module getServerModule() {
        return RosettaServerModule.create(TestServerModule.class);
    }

    @Test
    void testHandlesError() {

        String namespaceUri = createModel("types.rosetta", """
                namespace test
                
                func Foo:
                  output:
                    result string (1..1)
                
                  set result: "myOutput"
                """);
        
        List<Diagnostic> issues = getDiagnostics().get(namespaceUri);

        Assertions.assertEquals(1, issues.size());
        Diagnostic diagnostic = issues.get(0);
        Assertions.assertEquals("Broken expression generator", diagnostic.getMessage());
        Assertions.assertEquals(DiagnosticSeverity.Error, diagnostic.getSeverity());
        Range range = diagnostic.getRange();
        Assertions.assertEquals(2, range.getStart().getLine());
        Assertions.assertEquals(0, range.getStart().getCharacter());
        Assertions.assertEquals(6, range.getEnd().getLine());
        Assertions.assertEquals(24, range.getEnd().getCharacter());
    }

    //TODO: test that issue aggregation is happening by having a multi expression test or multiple

    

    static class TestIdeSetup extends RosettaIdeSetup {
        @Override
        public Injector createInjector() {
            //add new bindings here to break other generators
            RosettaRuntimeModule _rosettaRuntimeModule = new RosettaRuntimeModule() {
                public Class<? extends ExpressionGenerator> bindExpressionGenerator() {
                    return BrokenExpressionGenerator.class;
                }
            };

            RosettaIdeModule _rosettaIdeModule = new RosettaIdeModule();
            return Guice.createInjector(Modules2.mixin(_rosettaRuntimeModule, _rosettaIdeModule));
        }
    }

    static class BrokenExpressionGenerator extends ExpressionGenerator {
        @Override
        public JavaStatementBuilder javaCode(RosettaExpression expr, JavaType expectedType, JavaScope scope) {
            throw new RuntimeException("Broken expression generator");
        }
    }

    static class TestServerModule extends RosettaServerModule {
        public Class<? extends Provider<IResourceServiceProvider.Registry>> providesIResourceServiceProvider$Registry() {
            return TestIResourceServiceProviderRegistryFactory.class;
        }

    }

    static class TestIResourceServiceProviderRegistryFactory implements Provider<IResourceServiceProvider.Registry> {
        private final Registry registry = loadRegistry();
        
        @Override
        public IResourceServiceProvider.Registry get() {
            return registry;
        }

        private Registry loadRegistry() {
            ResourceServiceProviderRegistryImpl registry = new ResourceServiceProviderRegistryImpl();
            ISetup cp = new TestIdeSetup();
            Injector injector = cp.createInjectorAndDoEMFRegistration();
            IResourceServiceProvider resourceServiceProvider = injector.getInstance(IResourceServiceProvider.class);
            FileExtensionProvider extensionProvider = injector.getInstance(FileExtensionProvider.class);
            String primaryFileExtension = extensionProvider.getPrimaryFileExtension();
            for (String ext : extensionProvider.getFileExtensions()) {
                if (registry.getExtensionToFactoryMap().containsKey(ext)) {
                    if (primaryFileExtension.equals(ext))
                        registry.getExtensionToFactoryMap().put(ext, resourceServiceProvider);
                } else {
                    registry.getExtensionToFactoryMap().put(ext, resourceServiceProvider);
                }
            }
            return registry;
        }

    }
}
