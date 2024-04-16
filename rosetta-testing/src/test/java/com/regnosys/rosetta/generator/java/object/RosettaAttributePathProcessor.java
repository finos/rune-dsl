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

package com.regnosys.rosetta.generator.java.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.Processor;

public class RosettaAttributePathProcessor implements Processor {

	private final List<RosettaPath> result = new ArrayList<>();

	@Override
	public <R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<? extends R> rosettaType,
			R instance, RosettaModelObject parent, AttributeMeta... metas) {
		result.add(path);
		return true;
	}

	@Override
	public <R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<? extends R> rosettaType,
			List<? extends R> instance, RosettaModelObject parent, AttributeMeta... metas) {
		result.add(path);
		return true;
	}

	@Override
	public <T> void processBasic(RosettaPath path, Class<? extends T> rosettaType, T instance,
			RosettaModelObject parent, AttributeMeta... metas) {
		result.add(path);
	}

	@Override
	public <T> void processBasic(RosettaPath path, Class<? extends T> rosettaType, Collection<? extends T> instance,
			RosettaModelObject parent, AttributeMeta... metas) {
		result.add(path);
	}

	@Override
	public Report report() {
		return null;
	}

	public List<RosettaPath> getResult() {
		return result;
	}
}
