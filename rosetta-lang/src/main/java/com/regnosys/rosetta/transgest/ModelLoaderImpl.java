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

package com.regnosys.rosetta.transgest;

import static com.regnosys.rosetta.generator.util.IterableUtil.fullname;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.common.io.Resources;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.rosetta.model.lib.RosettaModelObject;

public class ModelLoaderImpl implements ModelLoader {
	@Inject Provider<XtextResourceSet> resourceSetProvider;
	
	public List<RosettaModel> loadRosettaModels(Stream<URL> res) {
		XtextResourceSet resourceSet = resourceSetProvider.get();
		List<RosettaModel> models = res.map(ModelLoaderImpl::url)
				.map(f -> getResource(resourceSet, f))
				.filter(Objects::nonNull)
				.map(Resource::getContents)
				.flatMap(Collection::stream)
				.map(r -> (RosettaModel) r)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		return models;
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