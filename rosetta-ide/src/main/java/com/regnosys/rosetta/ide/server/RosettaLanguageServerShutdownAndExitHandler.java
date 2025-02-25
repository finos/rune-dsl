package com.regnosys.rosetta.ide.server;

import jakarta.inject.Inject;

import org.eclipse.xtext.ide.server.ILanguageServerShutdownAndExitHandler;
import org.eclipse.xtext.ide.server.concurrent.RequestManager;

/**
 * A shutdown handler that also shuts down the request manager.
 * 
 * Without shutting down the request manager, pending requests may still be send out to the client
 * and threads may be left hanging. (in case `LanguageServer::exit` is not called)
 * 
 * TODO: contribute to Xtext.
 */
public class RosettaLanguageServerShutdownAndExitHandler extends ILanguageServerShutdownAndExitHandler.DefaultImpl {

	@Inject
	private RequestManager requestManager;
	
	@Override
	public void shutdown() {
		requestManager.shutdown();
		super.shutdown();
	}
}
