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

import org.eclipse.xtext.util.Modules2;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.ide.RosettaIdeModule;

/**
 * TODO: contribute to Xtext.
 *
 */
public class RosettaServerSetup extends RosettaStandaloneSetup {
	@Override
	public Injector createInjector() {
		return Guice.createInjector(Modules2.mixin(RosettaServerModule.create(), new RosettaRuntimeModule(), new RosettaIdeModule()));
	}
}
