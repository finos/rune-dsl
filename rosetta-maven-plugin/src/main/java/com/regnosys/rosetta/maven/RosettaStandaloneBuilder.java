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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.builder.standalone.IIssueHandler;
import org.eclipse.xtext.builder.standalone.LanguageAccess;
import org.eclipse.xtext.builder.standalone.StandaloneBuilder;
import org.eclipse.xtext.builder.standalone.compiler.CompilerConfiguration;
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.IResourceServiceProviderExtension;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescription.Event;
import org.eclipse.xtext.resource.clustering.IResourceClusteringPolicy;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.resource.persistence.SerializableResourceDescription;
import org.eclipse.xtext.resource.persistence.SourceLevelURIsAdapter;
import org.eclipse.xtext.resource.persistence.StorageAwareResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.regnosys.rosetta.generator.RosettaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RosettaStandaloneBuilder extends StandaloneBuilder {
	private static final Logger LOG = LoggerFactory.getLogger(RosettaStandaloneBuilder.class);
	
	private static Class<?> standaloneBuilderStateClass; 
	static {
		try {
			standaloneBuilderStateClass = Class.forName("org.eclipse.xtext.builder.standalone.StandaloneBuilderState");
		} catch (ClassNotFoundException e) {
			standaloneBuilderStateClass = null;
		}
	}
	
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
	
	private boolean isIncremental() {
		try {
        	Field f = StandaloneBuilder.class.getDeclaredField("incremental");
	        f.setAccessible(true);
			return (boolean) f.get(this);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	private String getClasspathConfigurationLocation() {
		try {
        	Field f = StandaloneBuilder.class.getDeclaredField("classpathConfigurationLocation");
	        f.setAccessible(true);
			return (String) f.get(this);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings("unchecked")
	private Map<LanguageAccess, JavaIoFileSystemAccess> getConfiguredFsas() {
		try {
        	Field f = StandaloneBuilder.class.getDeclaredField("configuredFsas");
	        f.setAccessible(true);
			return (Map<LanguageAccess, JavaIoFileSystemAccess>) f.get(this);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	private Object getBuilderState() {
		try {
        	Field f = StandaloneBuilder.class.getDeclaredField("builderState");
	        f.setAccessible(true);
			return f.get(this);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	private void setBuilderState(Object builderState) {
		try {
        	Field f = StandaloneBuilder.class.getDeclaredField("builderState");
	        f.setAccessible(true);
			f.set(this, builderState);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	private Object callBuilderStateMethod(String methodName, Class<?>[] parameterTypes, Object... arguments) {
		try {
			Method m = standaloneBuilderStateClass.getDeclaredMethod(methodName, parameterTypes);
			m.setAccessible(true);
			return m.invoke(getBuilderState(), arguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	private Object getBuilderStateField(String fieldName) {
		try {
			Field f = standaloneBuilderStateClass.getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(getBuilderState());
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean needsBeforeAllCall = false;
	private ResourceSet currentResourceSet;
	private JavaIoFileSystemAccess currentFileSystemAccess;
	@SuppressWarnings("unchecked")
	@Override
	public boolean launch() {
		needsBeforeAllCall = true;
		
		// START COPY PASTE OF ORIGINAL IMPLEMENTATION
		Stopwatch rootStopwatch = Stopwatch.createStarted();
		File stubsDirectory = stubsDirectory();
		ensureBaseDir();
		handleEncoding();
		LOG.info("Collecting source models.");
		Iterable<String> rootsToTravers = rootsToTraverse();
		List<URI> sourceResourceURIs = collectResources(getSourceDirs());
		File stateFile;
		try {
			stateFile = readOrCreateBuilderState(stubsDirectory);

			Set<URI> changedSourceFiles = new HashSet<>();
			Map<URI, IResourceDescription.Delta> allDeltas = new LinkedHashMap<>();
			// PATCH: changed order of this code block...
			Set<URI> changedLibraryFiles = new HashSet<>();
			List<URI> libraryResourceURIs = Collections.emptyList();
			if ((boolean) callBuilderStateMethod("updateLibraryHash", new Class[] { HashCode.class }, hashClasspath(rootsToTravers))) {
				libraryResourceURIs = collectResources(rootsToTravers);
				aggregateDeltas((Event) callBuilderStateMethod("libraryChanges", new Class[] { List.class, Collection.class }, libraryResourceURIs, changedLibraryFiles), allDeltas);
			} else {
				libraryResourceURIs = new ArrayList<>(((Map<URI, HashCode>)getBuilderStateField("libraryFiles")).keySet());
			}
			// ... with this one. This is a workaround for the following issue: https://github.com/finos/rune-dsl/issues/878 
			aggregateDeltas((Event) callBuilderStateMethod("sourceChanges", new Class[] { List.class, Collection.class }, sourceResourceURIs, changedSourceFiles), allDeltas);
			// END PATCH

			forceDebugLog("Collected source models. Took: " + rootStopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms.");
			
			if (getClasspathConfigurationLocation() != null) {
				writeClassPathConfiguration(rootsToTravers, stubsDirectory != null);
			}
			
			XtextResourceSet resourceSet = resourceSetProvider.get();
			configureWorkspace(resourceSet);
			Iterable<String> allClassPathEntries = Iterables.concat(getSourceDirs(), getClassPathEntries());
			if (stubsDirectory != null) {
				LOG.info("Installing type provider.");
				installTypeProvider(allClassPathEntries, resourceSet, null);
			}
			IResourceClusteringPolicy strategy = getClusteringPolicy();
			aggregateDeltas(indexResources(resourceSet, changedSourceFiles, changedLibraryFiles, strategy), allDeltas);
			// Generate Stubs
			String stubsClasses = generateStubs(stubsDirectory, changedSourceFiles, allDeltas);
			if (stubsClasses != null) {
				LOG.info("Installing type provider for stubs.");
				installTypeProvider(FluentIterable.from(allClassPathEntries).append(stubsClasses), resourceSet, jvmTypeAccess);
			}
			// Validate and generate
			ResourceDescriptionsData index = (ResourceDescriptionsData) getBuilderStateField("index");
			boolean hasValidationErrors = false;
			LOG.info("Validate and generate.");
			while (!changedSourceFiles.isEmpty() || !allDeltas.isEmpty()) {
				installSourceLevelURIs(resourceSet, changedSourceFiles);
				Iterator<URI> sourceResourceIterator = changedSourceFiles.iterator();
				while (sourceResourceIterator.hasNext()) {
					List<Resource> resources = new ArrayList<>();
					int clusterIndex = 0;
					boolean canContinue = true;
					while (sourceResourceIterator.hasNext() && canContinue) {
						URI uri = sourceResourceIterator.next();
						Resource resource = resourceSet.getResource(uri, true);
						resources.add(resource);
						resource.getContents(); // fully initialize
						EcoreUtil2.resolveLazyCrossReferences(resource, CancelIndicator.NullImpl);
						IResourceDescription.Manager manager = resourceDescriptionManager(resource);

						IResourceDescription oldDescription = index.getResourceDescription(uri);
						IResourceDescription newDescription = SerializableResourceDescription
								.createCopy(manager.getResourceDescription(resource));
						index.addDescription(uri, newDescription);
						aggregateDelta(manager.createDelta(oldDescription, newDescription), allDeltas);

						// TODO adjust to handle validations that need an up-to-date index
						hasValidationErrors = validate(resource) || hasValidationErrors;
						clusterIndex++;
						if (!strategy.continueProcessing(resourceSet, null, clusterIndex)) {
							canContinue = false;
						}
					}
					if (isFailOnValidationError() && hasValidationErrors) {
						if (isIncremental()) {
							// since we didn't generate anything yet, we don't want to persist the builder state
							callBuilderStateMethod("processIssues", new Class[] { IIssueHandler.class }, issueHandler);
						}
						return false;
					}
					generate(resources);
					if (!canContinue) {
						clearResourceSet(resourceSet);
					}
				}
				if (!allDeltas.isEmpty()) {
					sourceResourceURIs.removeAll(changedSourceFiles);
					changedSourceFiles.clear();
					for (URI candidate : sourceResourceURIs) {
						IResourceDescription description = index.getResourceDescription(candidate);
						if (resourceDescriptionManager(candidate).isAffected(allDeltas.values(), description,
								index)) {
							changedSourceFiles.add(candidate);
						}
					}
					allDeltas.clear();
				} else {
					changedSourceFiles.clear();
				}
			}
			return commitBuilderState(stateFile, hasValidationErrors);
		} finally {
			setBuilderState(null);
			getConfiguredFsas().clear();

			LOG.info("Build took " + rootStopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms.");
		}
	}

	@Override
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
	
	
	/** COPY PASTE **/
	private boolean commitBuilderState(File stateFile, boolean hasValidationErrors) {
		if (isIncremental()) {
			callBuilderStateMethod("to", new Class[] { File.class }, stateFile);
			return (boolean) callBuilderStateMethod("processIssues", new Class[] { IIssueHandler.class }, issueHandler) && !hasValidationErrors;
		}
		return !hasValidationErrors;
	}
	
	private HashCode hashClasspath(Iterable<String> classpathEntries) {
		try {
			Method m = StandaloneBuilder.class.getDeclaredMethod("hashClasspath", Iterable.class);
			m.setAccessible(true);
			return (HashCode) m.invoke(this, classpathEntries);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private File stubsDirectory() {
		boolean needsJava = IterableExtensions.exists(getLanguages().values(), LanguageAccess::isLinksAgainstJava);
		File stubsDirectory;
		if (needsJava) {
			forceDebugLog("Using common types.");
			stubsDirectory = createTempDir("stubs");
		} else {
			stubsDirectory = null;
		}
		return stubsDirectory;
	}
	
	private void ensureBaseDir() {
		if (getBaseDir() == null) {
			setBaseDir(System.getProperty("user.dir"));
			LOG.warn("Property baseDir not set. Using '" + getBaseDir() + "'");
		}
	}
	
	private void handleEncoding() {
		if (getEncoding() != null) {
			forceDebugLog("Setting encoding.");
			fileEncodingSetup(getLanguages().values(), getEncoding());
		}
	}
	
	private Iterable<String> rootsToTraverse() {
		Iterable<String> rootsToTravers = getClassPathEntries();
		if (getClassPathLookUpFilter() != null) {
			LOG.info("Class path look up filter is active.");
			Pattern cpLookUpFilter = Pattern.compile(getClassPathLookUpFilter());
			rootsToTravers = Lists
					.newArrayList(Iterables.filter(getClassPathEntries(), root -> cpLookUpFilter.matcher(root).matches()));
			LOG.info("Investigating " + Iterables.size(rootsToTravers) + " of " + Iterables.size(getClassPathEntries())
					+ " class path entries.");
		}
		return rootsToTravers;
	}
	
	private File readOrCreateBuilderState(File stubsDirectory) {
		try {
			Method m = StandaloneBuilder.class.getDeclaredMethod("readOrCreateBuilderState", File.class);
			m.setAccessible(true);
			return (File) m.invoke(this, stubsDirectory);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void aggregateDeltas(Event event, Map<URI, Delta> allDeltas) {
		for (Delta delta : event.getDeltas()) {
			aggregateDelta(delta, allDeltas);
		}
	}

	private void aggregateDelta(Delta delta, Map<URI, Delta> allDeltas) {
		URI uri = delta.getUri();
		allDeltas.merge(uri, delta, (prev, current) -> {
			return resourceDescriptionManager(uri).createDelta(prev.getOld(), current.getNew());
		});
	}
	
	private IResourceDescription.Manager resourceDescriptionManager(Resource resource) {
		if (resource instanceof XtextResource) {
			return ((XtextResource) resource).getResourceServiceProvider().getResourceDescriptionManager();
		}
		return resourceDescriptionManager(resource.getURI());
	}
	
	private IResourceDescription.Manager resourceDescriptionManager(URI uri) {
		return languageAccess(uri).getResourceDescriptionManager();
	}
	
	private LanguageAccess languageAccess(URI uri) {
		return getLanguages().get(uri.fileExtension());
	}
	
	private void writeClassPathConfiguration(Iterable<String> modelRoots, boolean classpath) {
		try {
			Method m = StandaloneBuilder.class.getDeclaredMethod("writeClassPathConfiguration", Iterable.class, boolean.class);
			m.setAccessible(true);
			m.invoke(this, modelRoots, classpath);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String generateStubs(File stubsDirectory, Set<URI> changedSourceFiles,
			Map<URI, IResourceDescription.Delta> allDeltas) {
		if (stubsDirectory == null) {
			return null;
		}
		generateStubs(changedSourceFiles, stubsDirectory);
		CompilerConfiguration configuration = getCompiler().getConfiguration();
		if (isIncremental()) {
			configuration.enableIncrementalCompilation(createTempDir("state"), event -> {
				aggregateDeltas(event, allDeltas);
			});
		}
		String stubsClasses = compileStubs(stubsDirectory);
		if (isIncremental()) {
			configuration.disableIncrementalCompilation();
		}
		return stubsClasses;
	}
	
	private void installSourceLevelURIs(ResourceSet resourceSet, Collection<URI> changes) {
		Set<URI> effectiveSourceLevelUris = new HashSet<>();
		for (URI uri : changes) {
			if (isSource(uri)) {
				effectiveSourceLevelUris.add(uri);
				Resource resource = resourceSet.getResource(uri, false);
				if (resource != null && isLoadedFromStorage(resource)) {
					resourceSet.getResources().remove(resource);
					// proxify
					resource.unload();
				}
			}
		}
		SourceLevelURIsAdapter.setSourceLevelUris(resourceSet, effectiveSourceLevelUris);
	}
	
	private boolean isLoadedFromStorage(Resource resource) {
		return resource instanceof StorageAwareResource && ((StorageAwareResource) resource).isLoadedFromStorage();
	}

	private boolean isSource(URI uri) {
		IResourceServiceProvider provider = languageAccess(uri).getResourceServiceProvider();
		return provider instanceof IResourceServiceProviderExtension
				&& ((IResourceServiceProviderExtension) provider).isSource(uri);
	}
}
