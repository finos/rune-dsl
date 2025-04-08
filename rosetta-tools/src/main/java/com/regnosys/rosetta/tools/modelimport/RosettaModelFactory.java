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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.formatting2.ResourceFormatterService;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaModel;

public class RosettaModelFactory {
	
	public static final String PROJECT_VERSION = "${project.version}";
	
	private final ResourceSet resourceSet;
	
	private final Set<Resource> builtinResources = new HashSet<>();
	
	private final ResourceFormatterService formatterService;

	@Inject
	public RosettaModelFactory(ResourceSet resourceSet, RosettaBuiltinsService builtins, ResourceFormatterService formatterService) {
		this.resourceSet = resourceSet;
		builtinResources.add(resourceSet.getResource(builtins.basicTypesURI, true));
		builtinResources.add(resourceSet.getResource(builtins.annotationsURI, true));
		
		this.formatterService = formatterService;
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
		try {
			formatterService.formatCollection(resources,
					(resource, formattedText) -> {
						String fileName = resource.getURI().toFileString();
						Path resourcePath = Path.of(outputDirectory).resolve(fileName);
						try {
							Files.writeString(resourcePath, formattedText);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	private Resource createResource(String namespace, String type) {
		return resourceSet.createResource(URI.createURI(namespace.substring(namespace.indexOf('.') + 1)
			.replace('.', '-') + "-" + type + ".rosetta"));
	}
}
