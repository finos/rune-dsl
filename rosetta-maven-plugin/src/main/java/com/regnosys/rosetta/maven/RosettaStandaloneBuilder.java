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

package com.regnosys.rosetta.maven;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.builder.standalone.LanguageAccess;
import org.eclipse.xtext.builder.standalone.StandaloneBuilder;
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.clustering.DisabledClusteringPolicy;
import org.eclipse.xtext.resource.clustering.DynamicResourceClusteringPolicy;
import org.eclipse.xtext.resource.clustering.IResourceClusteringPolicy;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.regnosys.rosetta.generator.RosettaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RosettaStandaloneBuilder extends StandaloneBuilder {
	private static final Logger LOG = LoggerFactory.getLogger(RosettaStandaloneBuilder.class);
	
	@Inject
	private Provider<XtextResourceSet> resourceSetProvider;
	@Inject
	private IndexedJvmTypeAccess jvmTypeAccess;
	
	private LanguageAccess rosettaLanguageAccess = null;
	// TODO: patch Xtext to make `languages` available
	@SuppressWarnings("unchecked")
	private LanguageAccess getRosettaLanguageAccess() {
		if (rosettaLanguageAccess == null) {
			Map<String, LanguageAccess> languages;
	        try {
	        	Field f = StandaloneBuilder.class.getDeclaredField("languages");
		        f.setAccessible(true);
				languages = (Map<String, LanguageAccess>) f.get(this);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
	        rosettaLanguageAccess = languages.get("rosetta");
		}
		return rosettaLanguageAccess;
	}
	
	private RosettaGenerator rosettaGenerator = null;
	private RosettaGenerator getRosettaGenerator() {
		if (rosettaGenerator == null) {
			LanguageAccess access = getRosettaLanguageAccess();
	        GeneratorDelegate delegate = access.getGenerator();
	        try {
	        	Field f = GeneratorDelegate.class.getDeclaredField("generator");
		        f.setAccessible(true);
		        rosettaGenerator = (RosettaGenerator) f.get(delegate);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return rosettaGenerator;
	}
	
	/* START ORDERING HACK */
	private List<String> fileOrder = null;
	public void setFileOrder(List<String> fileOrder) {
		this.fileOrder = fileOrder;
	}
	
	private List<URI> getOrderedURIs(Iterable<URI> resourceURIs) {
		if (fileOrder == null) {
			if (resourceURIs instanceof List<?>) {
				return (List<URI>)resourceURIs;
			}
			return Lists.newArrayList(resourceURIs);
		}
		LOG.warn("Using ordering hack");
		Set<URI> unordered = Sets.newLinkedHashSet(resourceURIs);
		List<URI> result = new ArrayList<>(unordered.size());
		fileOrder.forEach(file -> {
			URI match = unordered.stream().filter(uri -> uri.toString().endsWith("/" + file)).findAny().orElse(null);
			if (match != null) {
				unordered.remove(match);
				result.add(match);
			}
		});
		result.addAll(unordered);
		return result;
	}
	/* END ORDERING HACK */

	private boolean needsBeforeAllCall = false;
	private ResourceSet currentResourceSet;
	private JavaIoFileSystemAccess currentFileSystemAccess;
	@Override
	public boolean launch() {
		needsBeforeAllCall = true;
		
		// START COPY PASTE OF ORIGINAL IMPLEMENTATION
		boolean needsJava = IterableExtensions.exists(getLanguages().values(), l -> l.isLinksAgainstJava());
		if (getBaseDir() == null) {
			setBaseDir(System.getProperty("user.dir"));
			LOG.warn("Property baseDir not set. Using '" + getBaseDir() + "'");
		}
		if (needsJava) {
			LOG.info("Using common types.");
		}
		XtextResourceSet resourceSet = resourceSetProvider.get();
		if (getEncoding() != null) {
			forceDebugLog("Setting encoding.");
			fileEncodingSetup(getLanguages().values(), getEncoding());
		}
		LOG.info("Collecting source models.");
		long startedAt = System.currentTimeMillis();
		Iterable<String> rootsToTravers = getClassPathEntries();
		if (getClassPathLookUpFilter() != null) {
			LOG.info("Class path look up filter is active.");
			Pattern cpLookUpFilter = Pattern.compile(getClassPathLookUpFilter());
			rootsToTravers = Iterables.filter(getClassPathEntries(), root -> cpLookUpFilter.matcher(root).matches());
			LOG.info("Investigating " + Iterables.size(rootsToTravers) + " of " + Iterables.size(getClassPathEntries())
					+ " class path entries.");
		}
		List<URI> sourceResourceURIs = getOrderedURIs(collectResources(getSourceDirs(), resourceSet));
		// PATCHED THE FOLLOWING LINE TO WORKAROUND ISSUE: https://github.com/finos/rune-dsl/issues/878
		Iterable<URI> allResourcesURIs = getOrderedURIs(Iterables.concat(collectResources(rootsToTravers, resourceSet), sourceResourceURIs));
		forceDebugLog("Finished collecting source models. Took: " + (System.currentTimeMillis() - startedAt) + " ms.");
		Iterable<String> allClassPathEntries = Iterables.concat(getSourceDirs(), getClassPathEntries());
		if (needsJava) {
			LOG.info("Installing type provider.");
			installTypeProvider(allClassPathEntries, resourceSet, null);
		}
		IResourceClusteringPolicy strategy = null;
		if (getClusteringConfig() != null) {
			LOG.info("Clustering configured.");
			DynamicResourceClusteringPolicy dynamicResourceClusteringPolicy = new DynamicResourceClusteringPolicy();
			// Convert MB to byte to make it easier for the user
			dynamicResourceClusteringPolicy.setMinimumFreeMemory(getClusteringConfig().getMinimumFreeMemory() * 1024 * 1024);
			dynamicResourceClusteringPolicy.setMinimumClusterSize(getClusteringConfig().getMinimumClusterSize());
			dynamicResourceClusteringPolicy.setMinimumPercentFreeMemory(getClusteringConfig().getMinimumPercentFreeMemory());
			strategy = dynamicResourceClusteringPolicy;
		} else {
			strategy = new DisabledClusteringPolicy();
		}
		// Fill index
		ResourceDescriptionsData index = new ResourceDescriptionsData(new ArrayList<>());
		Iterator<URI> allResourceIterator = allResourcesURIs.iterator();
		while (allResourceIterator.hasNext()) {
			List<Resource> resources = new ArrayList<>();
			int clusterIndex = 0;
			boolean canContinue = true;
			while (allResourceIterator.hasNext() && canContinue) {
				URI uri = allResourceIterator.next();
				Resource resource = resourceSet.getResource(uri, true);
				resources.add(resource);
				fillIndex(uri, resource, index);
				clusterIndex++;
				if (!strategy.continueProcessing(resourceSet, null, clusterIndex)) {
					canContinue = false;
				}
			}
			if (!canContinue) {
				clearResourceSet(resourceSet);
			}
		}
		installIndex(resourceSet, index);
		// Generate Stubs
		if (needsJava) {
			String stubsClasses = compileStubs(generateStubs(index, sourceResourceURIs));
			LOG.info("Installing type provider for stubs.");
			installTypeProvider(Iterables.concat(allClassPathEntries, Lists.newArrayList(stubsClasses)), resourceSet,
					jvmTypeAccess);
		}
		// Validate and generate
		LOG.info("Validate and generate.");
		Iterator<URI> sourceResourceIterator = sourceResourceURIs.iterator();
		boolean hasValidationErrors = false;
		while (sourceResourceIterator.hasNext()) {
			List<Resource> resources = new ArrayList<>();
			int clusterIndex = 0;
			boolean canContinue = true;
			while (sourceResourceIterator.hasNext() && canContinue) {
				URI uri = sourceResourceIterator.next();
				Resource resource = resourceSet.getResource(uri, true);
				resources.add(resource);
				resource.getContents(); // full initialize
				EcoreUtil2.resolveLazyCrossReferences(resource, CancelIndicator.NullImpl);
				hasValidationErrors = !validate(resource) || hasValidationErrors;
				clusterIndex++;
				if (!strategy.continueProcessing(resourceSet, null, clusterIndex)) {
					canContinue = false;
				}
			}
			if (isFailOnValidationError() && hasValidationErrors) {
				return !hasValidationErrors;
			}
			generate(resources);
			if (!canContinue) {
				clearResourceSet(resourceSet);
			}
		}
		boolean success = !hasValidationErrors;
		// END COPY PASTE OF ORIGINAL IMPLEMENTATION
		
		LOG.info("Starting after all generation");
		GeneratorContext context = new GeneratorContext();
		context.setCancelIndicator(CancelIndicator.NullImpl);
		getRosettaGenerator().afterAllGenerate(currentResourceSet, currentFileSystemAccess, context);
		
		return success;
	}

	protected void generate(List<Resource> sourceResources) {
		if (needsBeforeAllCall) {
			LOG.info("Starting before all generation");
			currentResourceSet = sourceResources.get(0).getResourceSet();
			LanguageAccess access = getRosettaLanguageAccess();
			currentFileSystemAccess = getFileSystemAccess(access);
			GeneratorContext context = new GeneratorContext();
			context.setCancelIndicator(CancelIndicator.NullImpl);
			getRosettaGenerator().beforeAllGenerate(currentResourceSet, currentFileSystemAccess, context);
			needsBeforeAllCall = false;
		}
		
		super.generate(sourceResources);
	}
	
	private JavaIoFileSystemAccess getFileSystemAccess(LanguageAccess language) {
		try {
			Method m = StandaloneBuilder.class.getDeclaredMethod("getFileSystemAccess", LanguageAccess.class);
			m.setAccessible(true);
			return (JavaIoFileSystemAccess) m.invoke(this, language);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
