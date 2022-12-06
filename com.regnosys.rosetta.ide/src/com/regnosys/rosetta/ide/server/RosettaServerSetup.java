package com.regnosys.rosetta.ide.server;

import org.eclipse.xtext.util.Modules2;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.ide.RosettaIdeModule;

public class RosettaServerSetup extends RosettaStandaloneSetup {
	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(RosettaServerModule.create(), new RosettaRuntimeModule(), new RosettaIdeModule()));
	}
}
