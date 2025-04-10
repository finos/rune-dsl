package com.regnosys.rosetta.tests;

import java.util.Iterator;

import javax.inject.Provider;

import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.generator.external.ExternalGenerator;
import com.regnosys.rosetta.generator.external.ExternalGenerators;
import com.regnosys.rosetta.tests.generator.FailingGenerator;
import com.regnosys.rosetta.tests.validation.RosettaValidationTestHelper;
import com.rosetta.model.lib.validation.ValidatorFactory;

public class BrokenGeneratorTestInjectorProvider extends RosettaInjectorProvider {
    protected RosettaRuntimeModule createRuntimeModule() {
        // make it work also with Maven/Tycho and OSGI
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=493672
        return new RosettaRuntimeModule() {
            @Override
            public ClassLoader bindClassLoaderToInstance() {
                return BrokenGeneratorTestInjectorProvider.class.getClassLoader();
            }

            public void configureValidationTestHelper(Binder binder) {
                binder.bind(ValidationTestHelper.class).to(RosettaValidationTestHelper.class);
            }

            public Class<? extends ValidatorFactory> bindValidatorFactory() {
                return ValidatorFactory.Default.class;
            }

            @Override
            public Class<? extends Provider<ExternalGenerators>> provideExternalGenerators() {
                return ExternalGeneratorsProvider.class;
            }
        };
    }
    
    private static class ExternalGeneratorsProvider implements Provider<ExternalGenerators> {
        @Override
        public ExternalGenerators get() {
            return new ExternalGenerators() {
                @Override
                public Iterator<ExternalGenerator> iterator() {
                    return Lists.<ExternalGenerator>newArrayList(new FailingGenerator()).iterator();
                }
            };
        }
        
    }
}
