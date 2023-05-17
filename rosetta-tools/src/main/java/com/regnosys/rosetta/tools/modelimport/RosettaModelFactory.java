package com.regnosys.rosetta.tools.modelimport;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.Import;
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

	public RosettaModel createRosettaModel(String type, GenerationProperties properties, List<String> imports) {
		Resource resource = createResource(properties.getNamespace(), type);

		RosettaModel rosettaModel = RosettaFactory.eINSTANCE.createRosettaModel();
		rosettaModel.setName(properties.getNamespace());
		rosettaModel.setDefinition(properties.getNamespaceDefinition());
		rosettaModel.setVersion(PROJECT_VERSION);
		imports.stream().map(this::createImport).forEach(rosettaModel.getImports()::add);

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

	private Import createImport(String imp) {
		Import anImport = RosettaFactory.eINSTANCE.createImport();
		anImport.setImportedNamespace(imp);
		return anImport;
	}

	private Resource createResource(String namespace, String type) {
		return resourceSet.createResource(URI.createURI(namespace.substring(namespace.indexOf('.') + 1)
			.replace('.', '-') + "-" + type + ".rosetta"));
	}
}
