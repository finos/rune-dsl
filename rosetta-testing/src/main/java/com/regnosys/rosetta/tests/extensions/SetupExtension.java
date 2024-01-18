package com.regnosys.rosetta.tests.extensions;

import static org.eclipse.xtext.util.Exceptions.throwUncheckedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.testing.GlobalRegistries;
import org.eclipse.xtext.testing.IInjectorProvider;
import org.eclipse.xtext.testing.IRegistryConfigurator;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.GlobalRegistries.GlobalStateMemento;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import com.google.inject.Injector;

public class SetupExtension extends InjectionExtension {
	private static Map<Class<? extends ISetup>, IInjectorProvider> setupClassCache = new HashMap<>();
	
	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
		IInjectorProvider injectorProvider = getOrCreateInjectorProvider(context);
		if (injectorProvider != null) {
			setupRegistry(injectorProvider, context);
			Injector injector = injectorProvider.getInjector();
			if (injector != null) {
				injector.injectMembers(testInstance);
			}
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		IInjectorProvider injectorProvider = getOrCreateInjectorProvider(context);
		if (injectorProvider instanceof IRegistryConfigurator) {
			setupRegistry(injectorProvider, context);
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		IInjectorProvider injectorProvider = getOrCreateInjectorProvider(context);
		restoreRegistry(injectorProvider, context);
	}
	
	protected static IInjectorProvider getOrCreateInjectorProvider(ExtensionContext context) {
		Optional<WithSetup> withSetupOpt = AnnotationSupport.findAnnotation(context.getRequiredTestClass(), WithSetup.class);
		if (withSetupOpt.isPresent()) {
			WithSetup withSetup = withSetupOpt.get();
			Class<? extends ISetup> klass = withSetup.value();
			IInjectorProvider injectorProvider = setupClassCache.get(klass);
			if (injectorProvider == null) {
				try {
					ISetup setup = klass.getDeclaredConstructor().newInstance();
					injectorProvider = new SetupBasedInjectorProvider(setup);
					setupClassCache.put(klass, injectorProvider);
				} catch (Exception e) {
					throwUncheckedException(e);
				}
			}
			return injectorProvider;
		} else if (AnnotationSupport.findAnnotation(context.getRequiredTestClass(), InjectWith.class).isPresent()) {
			return InjectionExtension.getOrCreateInjectorProvider(context);
		} else {
			Optional<ExtensionContext> parentContext = context.getParent().filter(p->p.getTestClass().isPresent());
			if (parentContext.isPresent()) {
				return getOrCreateInjectorProvider(parentContext.get());
			}
		}
		return null;
	}

	protected static class SetupBasedInjectorProvider implements IInjectorProvider, IRegistryConfigurator {
		protected GlobalStateMemento stateBeforeInjectorCreation;
		protected GlobalStateMemento stateAfterInjectorCreation;
		protected Injector injector;
		
		private final ISetup setup;

		static {
			GlobalRegistries.initializeDefaults();
		}
		
		public SetupBasedInjectorProvider(ISetup setup) {
			this.setup = setup;
			
		}

		@Override
		public Injector getInjector() {
			if (injector == null) {
				this.injector = internalCreateInjector();
				stateAfterInjectorCreation = GlobalRegistries.makeCopyOfGlobalState();
			}
			return injector;
		}

		protected Injector internalCreateInjector() {
			return setup.createInjectorAndDoEMFRegistration();
		}

		@Override
		public void restoreRegistry() {
			stateBeforeInjectorCreation.restoreGlobalState();
			stateBeforeInjectorCreation = null;
		}

		@Override
		public void setupRegistry() {
			stateBeforeInjectorCreation = GlobalRegistries.makeCopyOfGlobalState();
			if (injector == null) {
				getInjector();
			}
			stateAfterInjectorCreation.restoreGlobalState();
		}
	}
}
