package com.regnosys.rosetta.transgest;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.common.io.Resources;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.rosetta.RosettaClass;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.rosetta.model.lib.RosettaModelObject;

public class ModelLoaderImpl implements ModelLoader {

	private final List<RosettaModel> rosettaModels;
	protected XtextResourceSet resourceSet;

	public ModelLoaderImpl(Collection<String> resourceLocations) {
		rosettaModels = loadRosettaModels(resourceLocations.stream().map(Resources::getResource));
	}

	public ModelLoaderImpl(URL... urls) {
		rosettaModels = loadRosettaModels(Arrays.stream(urls));
	}

	public void addRosource(InputStream resourceStream) {

	}

	protected List<RosettaModel> loadRosettaModels(Stream<URL> res) {
		RosettaStandaloneSetup.doSetup();
		resourceSet = new XtextResourceSet();
		return res.map(ModelLoaderImpl::url)
				.map(f -> getResource(resourceSet, f))
				.filter(Objects::nonNull)
				.map(Resource::getContents)
				.flatMap(Collection::stream)
				.map(r -> (RosettaModel) r)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.regnosys.rosetta.transgest.ModelLoader#rosettaClass(java.lang.Class)
	 */
	@Override
	public RosettaType rosettaClass(Class<? extends RosettaModelObject> rootObject) {
		return rosettaModels.stream()
				.map(RosettaModel::getElements)
				.flatMap(Collection::stream)
				.filter(c -> c instanceof RosettaClass || c instanceof Data)
				.map(c -> (RosettaType) c)
				.filter(c -> c.getName().equals(rootObject.getSimpleName()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(rootObject.getSimpleName() + " not found in Rosetta Model"));
	}

	@Override
	public RosettaType rosettaClass(String className) {
		return rosettaModels.stream().map(RosettaModel::getElements)
				.flatMap(Collection::stream)
				.filter(c -> c instanceof RosettaClass || c instanceof Data)
				.map(c -> (RosettaType) c)
				.filter(c -> c.getName().equals(className))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(className + " not found in Rosetta Model"));
	}

	@Override
	public <T extends RosettaRootElement> List<T> rosettaElements(Class<T> clazz) {
		return rosettaModels.stream()
				.map(RosettaModel::getElements)
				.flatMap(Collection::stream)
				.filter(c -> clazz.isInstance(c))
				.map(c -> clazz.cast(c))
				.collect(Collectors.toList());
	}

	private static String url(URL c) {
		try {
			String asciiString = c.toURI().toURL().toURI().toASCIIString();
			return asciiString;
		} catch (MalformedURLException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	protected static Resource getResource(XtextResourceSet resourceSet, String f) {
		try {
			return resourceSet.getResource(URI.createURI(f, true), true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<RosettaModel> models() {
		return rosettaModels;
	}

	@Override
	public XtextResourceSet getResourceSet() {
		return resourceSet;
	}

	@Override
	public void addModel(RosettaModel model) {
		rosettaModels.add(model);
	}
}