/*
 * Copyright 2026 REGnosys
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

package com.regnosys.rosetta.serializer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.impl.Serializer;

import jakarta.inject.Inject;

/**
 * Makes serializing this language safe from several threads, by building the grammar analysis once
 * before the first serialization rather than racing to build it during them — see
 * {@link SerializerAnalysisWarmUp} for what is unsafe.
 *
 * <p>{@link Serializer#getIContext} is the gate because it is the one place every public entry point
 * passes through before touching a sequencer, including {@code serializeToRegions}, which is not on
 * {@link org.eclipse.xtext.serializer.ISerializer} and so cannot be covered by wrapping it. Only the
 * two {@code protected serialize(context, …)} overloads bypass it, and
 * {@code SerializerAnalysisTest} pins that every public path still arrives here.
 */
public class RosettaSerializer extends Serializer {

	@Inject
	private SerializerAnalysisWarmUp warmUp;

	@Override
	protected ISerializationContext getIContext(EObject semanticObject) {
		warmUp.ensureWarm();
		return super.getIContext(semanticObject);
	}
}
