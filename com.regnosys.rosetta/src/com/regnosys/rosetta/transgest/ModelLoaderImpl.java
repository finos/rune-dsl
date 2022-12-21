package com.regnosys.rosetta.transgest;

import static com.regnosys.rosetta.generator.util.Util.fullname;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;

import com.google.common.io.Resources;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.rosetta.model.lib.RosettaModelObject;

public class ModelLoaderImpl implements ModelLoader {
	@Inject IResourceValidator resourceValidator;
	
	public List<RosettaModel> loadRosettaModels(Stream<URL> res) {
		XtextResourceSet resourceSet = new XtextResourceSet();
		return res.map(ModelLoaderImpl::url)
				.map(f -> getResource(resourceSet, f))
				.filter(Objects::nonNull)
				.peek(r -> resourceValidator.validate(r, CheckMode.ALL, CancelIndicator.NullImpl))
				.map(Resource::getContents)
				.flatMap(Collection::stream)
				.map(r -> (RosettaModel) r)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
	public List<RosettaModel> loadRosettaModels(URL... urls) {
		return loadRosettaModels(Arrays.stream(urls));
	}
	
	public List<RosettaModel> loadRosettaModels(Collection<String> resourceLocations) {
		return loadRosettaModels(resourceLocations.stream().map(Resources::getResource));
	}

	@Override
	public RosettaType rosettaClass(List<RosettaModel> rosettaModels, Class<? extends RosettaModelObject> rootObject) {
		return rosettaModels.stream()
				.map(RosettaModel::getElements)
				.flatMap(Collection::stream)
				.filter(c -> c instanceof Data)
				.map(c -> (RosettaType) c)
				.filter(c -> fullname(c).toString().equals(rootObject.getName()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(rootObject.getName() + " not found in Rosetta Model"));
	}

	@Override
	public RosettaType rosettaClass(List<RosettaModel> rosettaModels, String className) {
		return rosettaModels.stream().map(RosettaModel::getElements)
				.flatMap(Collection::stream)
				.filter(c -> c instanceof Data)
				.map(c -> (RosettaType) c)
				.filter(c -> c.getName().equals(className))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(className + " not found in Rosetta Model"));
	}

	@Override
	public <T extends RosettaRootElement> List<T> rosettaElements(List<RosettaModel> rosettaModels, Class<T> clazz) {
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

	private static Resource getResource(XtextResourceSet resourceSet, String f) {
		try {
			return resourceSet.getResource(URI.createURI(f, true), true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}