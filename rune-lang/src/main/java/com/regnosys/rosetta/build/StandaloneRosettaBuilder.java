package com.regnosys.rosetta.build;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.build.BuildRequest;
import org.eclipse.xtext.build.IncrementalBuilder;
import org.eclipse.xtext.build.IndexState;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ChunkedResourceDescriptions;
import org.eclipse.xtext.resource.impl.ProjectDescription;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.util.CancelIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class StandaloneRosettaBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneRosettaBuilder.class);
    private static final String PROJECT_NAME = "StandaloneProject";

    @Inject
    private Provider<XtextResourceSet> resourceSetProvider;
    @Inject
    private IncrementalBuilder builder;
    @Inject
    private IResourceServiceProvider.Registry registry;
    @Inject
    private RosettaBuiltinsService builtins;
    
    private volatile BuildRequest currentBuildRequest;

    public IncrementalBuilder.Result launch(RosettaBuildRequest request) {
        ProjectDescription project = createProjectDescription();
        List<URI> files;
        try {
            files = collectRosettaFiles(request.getSourcePaths());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            currentBuildRequest = createFreshBuildRequest(project, request.getBaseDir(), files, request.getValidationCallback());
            return runBuild();
        } finally {
            currentBuildRequest = null;
        }
    }

    /**
     * Expose current build request for inspection while the builder is running.
     */
    public BuildRequest getCurrentBuildRequest() {
        return currentBuildRequest;
    }

    private IncrementalBuilder.Result runBuild() {
        return builder.build(currentBuildRequest, registry::getResourceServiceProvider);
    }

    private ProjectDescription createProjectDescription() {
        ProjectDescription result = new ProjectDescription();
        result.setName(PROJECT_NAME);
        return result;
    }

    private List<URI> collectRosettaFiles(List<Path> sourcePaths) throws IOException {
        // Use a map to track files by filename - first file is kept
        HashMap<String, URI> fileMap = new HashMap<>();

        // Add user Rosetta files from all paths
        for (Path sourcePath : sourcePaths) {
            try (Stream<Path> paths = Files.walk(sourcePath)) {
                paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".rosetta"))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            URI uri = URI.createFileURI(path.toAbsolutePath().toString());
                            // Only add if not already present (first is kept)
                            URI previousUri = fileMap.putIfAbsent(fileName, uri);
                            if (previousUri != null) {
                                LOGGER.debug("File '{}' from '{}' already exists (from '{}'), skipping",
                                        fileName, sourcePath, previousUri.toFileString());
                            }
                        });
            }
        }
        List<URI> result = new ArrayList<>(fileMap.values());
        result.add(builtins.annotationsURI);
        result.add(builtins.basicTypesURI);
        return result;
    }

    private BuildRequest createFreshBuildRequest(ProjectDescription project, Path baseDir, List<URI> files, BuildRequest.IPostValidationCallback validationCallback) {
        BuildRequest request = new BuildRequest();
        request.setBaseDir(URI.createFileURI(baseDir.toAbsolutePath().toString()));
        request.setState(new IndexState());
        request.setResourceSet(createNewResourceSet(project, request.getState().getResourceDescriptions()));
        request.setDirtyFiles(files);
        request.setDeletedFiles(List.of());
        request.setExternalDeltas(List.of());
        request.setAfterValidate(validationCallback);
        request.setCancelIndicator(CancelIndicator.NullImpl);
        request.setIndexOnly(false);
        return request;
    }

    private XtextResourceSet createNewResourceSet(ProjectDescription project, ResourceDescriptionsData newIndex) {
        XtextResourceSet result = resourceSetProvider.get();
        project.attachToEmfObject(result);
        ChunkedResourceDescriptions index = new ChunkedResourceDescriptions(new HashMap<>(), result);
        index.setContainer(project.getName(), newIndex);
        return result;
    }
}

