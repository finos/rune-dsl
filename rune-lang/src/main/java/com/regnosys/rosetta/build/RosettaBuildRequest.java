package com.regnosys.rosetta.build;

import org.eclipse.xtext.build.BuildRequest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RosettaBuildRequest {
    private Path baseDir;
    private final List<Path> sourcePaths;
    private BuildRequest.IPostValidationCallback validationCallback;
    
    public RosettaBuildRequest() {
        sourcePaths = new ArrayList<>();
        validationCallback = (uri, issues) -> true;
    }
    
    public Path getBaseDir() {
        return baseDir;
    }
    public void setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
    }
    
    public List<Path> getSourcePaths() {
        return sourcePaths;
    }
    public void addSourcePath(Path sourcePath) {
        sourcePaths.add(sourcePath);
    }
    public void addSourcePaths(Collection<Path> sourcePaths) {
        sourcePaths.addAll(sourcePaths);
    }
    
    public BuildRequest.IPostValidationCallback getValidationCallback() {
        return validationCallback;
    }
    public void setValidationCallback(BuildRequest.IPostValidationCallback validationCallback) {
        this.validationCallback = validationCallback;
    }
}
