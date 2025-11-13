package com.regnosys.rosetta.config.file;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class RosettaConfigurationFileProvider implements Provider<URL>, javax.inject.Provider<URL> {
    public static final String FILE_NAME = "rosetta-config.yml";
    private final String fileName;
    private final boolean loadFromClasspath;

    public static RosettaConfigurationFileProvider createFromFile(String fileName) {
        return new RosettaConfigurationFileProvider(false, fileName);
    }
    public static RosettaConfigurationFileProvider createFromClasspath(String fileName) {
        return new RosettaConfigurationFileProvider(true, fileName);
    }

    @Inject
    public RosettaConfigurationFileProvider() {
        this(true, FILE_NAME);
    }

    private RosettaConfigurationFileProvider(boolean loadFromClasspath, String fileName) {
        this.loadFromClasspath = loadFromClasspath;
        this.fileName = fileName;
    }

    @Override
    public URL get() {
        if (loadFromClasspath) {
            return Thread.currentThread().getContextClassLoader().getResource(fileName);
        } else {
            return getUrlForFile();
        }
    }

    private URL getUrlForFile() {
        try {
            Path path = Path.of(fileName);
            if (Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path)) {
                return path.toUri().toURL();
            } else {
                throw new IllegalStateException("Configuration file " + path.toAbsolutePath() + " does not exist or is not a regular file.");
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Bad configuration filename " + fileName, e);
        }
    }
}
