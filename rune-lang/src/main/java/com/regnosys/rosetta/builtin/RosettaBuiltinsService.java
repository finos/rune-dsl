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

package com.regnosys.rosetta.builtin;

import java.net.URL;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.regnosys.rosetta.rosetta.RosettaModel;

public class RosettaBuiltinsService {
	public static final String BASIC_TYPES_FILE_NAME = "basictypes.rosetta";
	public static final String ANNOTATIONS_FILE_NAME = "annotations.rosetta";

	private final String[] basicTypesPath = new String[]{"model", BASIC_TYPES_FILE_NAME};
	private final String[] annotationsPath = new String[]{"model", ANNOTATIONS_FILE_NAME};

	public final URI basicTypesURI = URI.createHierarchicalURI("classpath", null, null, basicTypesPath, null, null);
	public final URI annotationsURI = URI.createHierarchicalURI("classpath", null, null, annotationsPath, null, null);

	/**
	 * Whether the given resource URI is one of the built-in models shipped in {@code rune-runtime}.
	 *
	 * <p>Matched on the file name rather than against {@link #basicTypesURI} / {@link #annotationsURI},
	 * because the builtins reach a resource set under their physical location rather than their canonical
	 * {@code classpath:} URI — and under more than one physical location when the same builtin is present in
	 * several jars on the classpath. Both {@link #getModel} and
	 * {@code RosettaNamesAreUniqueValidationHelper#isBuiltin} already work around the same thing; see the TODO
	 * on the latter for the root fix.
	 */
	public static boolean isBuiltinResource(URI uri) {
		if (uri == null) {
			return false;
		}
		String fileName = uri.trimFragment().lastSegment();
		return BASIC_TYPES_FILE_NAME.equals(fileName) || ANNOTATIONS_FILE_NAME.equals(fileName);
	}
	public final URL basicTypesURL = Objects.requireNonNull(this.getClass().getResource(basicTypesURI.path()));
	public final URL annotationsURL = Objects.requireNonNull(this.getClass().getResource(annotationsURI.path()));
	
	// TODO: cache?
	private RosettaModel getModel(ResourceSet resourceSet, URI uri) {
		Resource resource = resourceSet.getResource(uri, false);
		if (resource == null) { // TODO: this is a workaround for not having proper support for classpath uris in the Xtext language server
			String[] pathParts = uri.path().split("/");
			String uriFile = pathParts[pathParts.length - 1];
			resource = resourceSet.getResources().stream()
				.filter(r -> r.getURI().path().endsWith(uriFile))
				.findAny()
				.orElseThrow();
		}
		return (RosettaModel)resource.getContents().get(0);
	}
	public RosettaModel getBasicTypesModel(ResourceSet resourceSet) {
		return getModel(resourceSet, basicTypesURI);
	}
	public RosettaModel getAnnotationsResource(ResourceSet resourceSet) {
		return getModel(resourceSet, annotationsURI);
	}
}
