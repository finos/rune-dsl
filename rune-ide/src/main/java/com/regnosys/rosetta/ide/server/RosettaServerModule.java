/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.ide.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.xtext.build.IncrementalBuilder;
import org.eclipse.xtext.ide.server.ILanguageServerShutdownAndExitHandler;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.xtext.ide.server.ServerModule;
import org.eclipse.xtext.ide.server.WorkspaceManager;
import org.eclipse.xtext.ide.server.concurrent.RequestManager;
import org.eclipse.xtext.service.AbstractGenericModule;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.regnosys.rosetta.ide.build.RosettaStatefulIncrementalBuilder;

public class RosettaServerModule extends AbstractGenericModule {
	/**
	 * Do not use the constructor. Use {@code RosettaServerModule.create()} instead.
	 */
	protected RosettaServerModule() {}
	
	/**
	 * TODO: make a contribution to XText.
	 * The fact that we need this create method is just
	 * to hack around the fact that the `ServerModule` from xtext
	 * doesn't inherit from `AbstractGenericModule`, but rather from `AbstractModule`,
	 * which doesn't allow us to override bindings.
	 * The best fix would be with a patch in Xtext.
	 */
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
	
	/**
	 * Some classes in Xtext inject `LanguageServerImpl` instead of
	 * the interface `LanguageServer`. (e.g., `AbstractLanguageServerTest`)
	 * TODO: make a patch in Xtext.
	 */
	public Class<? extends LanguageServerImpl> bindLanguageServerImpl() {
		return RosettaLanguageServerImpl.class;
	}
	
	public Class<? extends RequestManager> bindRequestManager() {
		return RosettaRequestManager.class;
	}
	
	public Class<? extends ILanguageServerShutdownAndExitHandler> bindILanguageServerShutdownAndExitHandler() {
		return RosettaLanguageServerShutdownAndExitHandler.class;
	}
	
	public Class<? extends IncrementalBuilder.InternalStatefulIncrementalBuilder> bindIncrementalBuilder$InternalStatefulIncrementalBuilder() {
	    return RosettaStatefulIncrementalBuilder.class;
	}
	public Class<? extends WorkspaceManager> bindWorkspaceManager() {
		return RosettaWorkspaceManager.class;
	}
}
