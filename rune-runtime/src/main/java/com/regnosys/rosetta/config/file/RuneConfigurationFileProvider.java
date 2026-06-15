package com.regnosys.rosetta.config.file;

import javax.inject.Inject;
import javax.inject.Provider;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuneConfigurationFileProvider implements Provider<URL> {
    public static final String FILE_NAME = "rune-config.yml";
    public static final String LEGACY_FILE_NAME = "rosetta-config.yml";

    private static final Logger LOGGER = LoggerFactory.getLogger(RuneConfigurationFileProvider.class);
    private static final AtomicBoolean LEGACY_WARNING_LOGGED = new AtomicBoolean(false);

    // When null, the default name is resolved with a fallback to the legacy name.
    private final String fileName;
    private final boolean loadFromClasspath;

    public static RuneConfigurationFileProvider createFromFile(String fileName) {
        return new RuneConfigurationFileProvider(false, fileName);
    }
    public static RuneConfigurationFileProvider createFromClasspath(String fileName) {
        return new RuneConfigurationFileProvider(true, fileName);
    }

    @Inject
    public RuneConfigurationFileProvider() {
        this(true, null);
    }

    private RuneConfigurationFileProvider(boolean loadFromClasspath, String fileName) {
        this.loadFromClasspath = loadFromClasspath;
        this.fileName = fileName;
    }

    @Override
    public URL get() {
        if (fileName != null) {
            // An explicit file name was requested: resolve it as-is, without any fallback.
            return loadFromClasspath ? fromClasspath(fileName) : requireFile(fileName);
        }
        // Default resolution: prefer the new name, fall back to the legacy name with a deprecation warning.
        URL url = loadFromClasspath ? fromClasspath(FILE_NAME) : findFile(FILE_NAME);
        if (url != null) {
            return url;
        }
        URL legacy = loadFromClasspath ? fromClasspath(LEGACY_FILE_NAME) : findFile(LEGACY_FILE_NAME);
        if (legacy != null && LEGACY_WARNING_LOGGED.compareAndSet(false, true)) {
            LOGGER.warn("Found a legacy '{}' configuration file. Please rename it to '{}'; "
                    + "support for the legacy name is deprecated and will be removed in a future release.",
                    LEGACY_FILE_NAME, FILE_NAME);
        }
        return legacy;
    }

    private URL fromClasspath(String name) {
        return Thread.currentThread().getContextClassLoader().getResource(name);
    }

    private URL findFile(String name) {
        Path path = Paths.get(name);
        if (Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path)) {
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Bad configuration filename " + name, e);
            }
        }
        return null;
    }

    private URL requireFile(String name) {
        URL url = findFile(name);
        if (url == null) {
            throw new IllegalStateException("Configuration file " + Paths.get(name).toAbsolutePath()
                    + " does not exist or is not a regular file.");
        }
        return url;
    }
}
