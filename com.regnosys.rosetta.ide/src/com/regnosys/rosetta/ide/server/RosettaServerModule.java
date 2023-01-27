package com.regnosys.rosetta.ide.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
	protected RosettaServerModule() {}
	
	public static Module create() {
		return create(RosettaServerModule.class);
	}
	public static Module create(Class<? extends RosettaServerModule> serverModuleClass) {
		try {
			Constructor<? extends RosettaServerModule> moduleConstructor = serverModuleClass.getDeclaredConstructor();
			moduleConstructor.setAccessible(true);
			return Modules.override(new ServerModule()).with(moduleConstructor.newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public Class<? extends LanguageServer> bindLanguageServer() {
		return RosettaLanguageServerImpl.class;
	}
	
	public Class<? extends LanguageServerImpl> bindLanguageServerImpl() {
		return RosettaLanguageServerImpl.class;
	}
}
