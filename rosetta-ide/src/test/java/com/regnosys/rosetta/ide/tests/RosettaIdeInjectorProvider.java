package com.regnosys.rosetta.ide.tests;

import org.eclipse.xtext.testing.GlobalRegistries;
import org.eclipse.xtext.testing.IInjectorProvider;
import org.eclipse.xtext.testing.IRegistryConfigurator;
import org.eclipse.xtext.util.Modules2;
import org.eclipse.xtext.testing.GlobalRegistries.GlobalStateMemento;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.ide.RosettaIdeModule;
import com.regnosys.rosetta.ide.RosettaIdeSetup;

public class RosettaIdeInjectorProvider implements IInjectorProvider, IRegistryConfigurator {

	protected GlobalStateMemento stateBeforeInjectorCreation;
	protected GlobalStateMemento stateAfterInjectorCreation;
	protected Injector injector;

	static {
		GlobalRegistries.initializeDefaults();
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
		return new RosettaIdeSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(Modules2.mixin(createRuntimeModule(), new RosettaIdeModule()));
			}
		}.createInjectorAndDoEMFRegistration();
	}

	protected RosettaRuntimeModule createRuntimeModule() {
		// make it work also with Maven/Tycho and OSGI
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=493672
		return new RosettaRuntimeModule() {
			@Override
			public ClassLoader bindClassLoaderToInstance() {
				return RosettaIdeInjectorProvider.class
						.getClassLoader();
			}
		};
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
