package com.regnosys.rosetta.builtin;

import java.net.URL;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.rosetta.RosettaModel;

public class RosettaBuiltinsService {
	private final String[] basicTypesPath = new String[]{"model", "basictypes.rosetta"};
	private final String[] annotationsPath = new String[]{"model", "annotations.rosetta"};
	
	public final URI basicTypesURI = URI.createHierarchicalURI("classpath", null, null, basicTypesPath, null, null);
	public final URI annotationsURI = URI.createHierarchicalURI("classpath", null, null, annotationsPath, null, null);
	public final URL basicTypesURL = Objects.requireNonNull(this.getClass().getResource(basicTypesURI.path()));
	public final URL annotationsURL = Objects.requireNonNull(this.getClass().getResource(annotationsURI.path()));
	

	// TODO: cache
	private RosettaModel getModel(ResourceSet resourceSet, URI uri) {
		return (RosettaModel)resourceSet.getResource(uri, false).getContents().get(0);
	}
	public RosettaModel getBasicTypesModel(ResourceSet resourceSet) {
		return getModel(resourceSet, basicTypesURI);
	}
	public RosettaModel getAnnotationsResource(ResourceSet resourceSet) {
		return getModel(resourceSet, annotationsURI);
	}
}
