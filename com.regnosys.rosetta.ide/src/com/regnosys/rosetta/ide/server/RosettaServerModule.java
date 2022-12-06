package com.regnosys.rosetta.ide.server;

import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.xtext.ide.server.ServerModule;
import org.eclipse.xtext.service.AbstractGenericModule;

import com.google.inject.util.Modules;
import com.google.inject.Module;

public class RosettaServerModule extends AbstractGenericModule {
	/**
	 * Do not use the constructor. Use {@code RosettaServerModule.create()} instead.
	 */
	private RosettaServerModule() {}
	
	public static Module create() {
		return Modules.override(new ServerModule()).with(new RosettaServerModule());
	}
	
	
	public Class<? extends LanguageServer> bindLanguageServer() {
		return RosettaIdeLanguageServerImpl.class;
	}
	
	public Class<? extends LanguageServerImpl> bindLanguageServerImpl() {
		return RosettaIdeLanguageServerImpl.class;
	}
}
