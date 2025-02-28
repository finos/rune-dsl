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

package com.regnosys.rosetta.tools.modelimport;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaModel;

public class RosettaModelFactory {
	
	public static final String PROJECT_VERSION = "${project.version}";
	
	private final ResourceSet resourceSet;
	
	private final Set<Resource> builtinResources = new HashSet<>();

	@Inject
	public RosettaModelFactory(ResourceSet resourceSet, RosettaBuiltinsService builtins) {
		this.resourceSet = resourceSet;
		builtinResources.add(resourceSet.getResource(builtins.basicTypesURI, true));
		builtinResources.add(resourceSet.getResource(builtins.annotationsURI, true));
	}
	
	public ResourceSet getResourceSet() {
		return resourceSet;
	}

	public RosettaModel createRosettaModel(String type, ImportTargetConfig targetConfig) {
		Resource resource = createResource(targetConfig.getNamespace(), type);

		RosettaModel rosettaModel = RosettaFactory.eINSTANCE.createRosettaModel();
		rosettaModel.setName(targetConfig.getNamespace());
		rosettaModel.setDefinition(targetConfig.getNamespaceDefinition());
		rosettaModel.setVersion(PROJECT_VERSION);

		resource.getContents().add(rosettaModel);
		return rosettaModel;
	}

	public void saveResources(String outputDirectory) throws IOException {
		List<Resource> resources = resourceSet.getResources().stream()
			.filter(r -> !builtinResources.contains(r))
			.collect(Collectors.toList());
		for (Resource resource : resources) {
			String fileName = resource.getURI().toFileString();
			resource.setURI(URI.createFileURI(outputDirectory + "/" + fileName));
			resource.save(Map.of());
		}
	}

	private Resource createResource(String namespace, String type) {
		return resourceSet.createResource(URI.createURI(namespace.substring(namespace.indexOf('.') + 1)
			.replace('.', '-') + "-" + type + ".rosetta"));
	}
}
