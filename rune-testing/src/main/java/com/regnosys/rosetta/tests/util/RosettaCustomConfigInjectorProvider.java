package com.regnosys.rosetta.tests.util;

import java.net.URL;

import org.eclipse.xtext.testing.GlobalRegistries;
import org.eclipse.xtext.testing.IInjectorProvider;
import org.eclipse.xtext.testing.IRegistryConfigurator;
import org.eclipse.xtext.testing.GlobalRegistries.GlobalStateMemento;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

public class RosettaCustomConfigInjectorProvider implements IInjectorProvider, IRegistryConfigurator {
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
		return new RosettaStandaloneSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(createRuntimeModule());
			}
		}.createInjectorAndDoEMFRegistration();
	}

	protected RosettaRuntimeModule createRuntimeModule() {
		// make it work also with Maven/Tycho and OSGI
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=493672
		return new RosettaRuntimeModule() {
			@Override
			public ClassLoader bindClassLoaderToInstance() {
				return RosettaTestInjectorProvider.class
						.getClassLoader();
			}
			
			public Class<? extends RosettaConfigurationFileProvider> bindRosettaConfigurationFileProvider() {
				return CustomConfigFileProvider.class;
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
	
	private static class CustomConfigFileProvider extends RosettaConfigurationFileProvider {
		@Override
		public URL get() {
			return Thread.currentThread().getContextClassLoader().getResource("rosetta-custom-config.yml");
		}
	}
}
