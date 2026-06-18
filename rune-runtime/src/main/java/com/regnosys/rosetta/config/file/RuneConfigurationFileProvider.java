package com.regnosys.rosetta.config.file;

import javax.inject.Inject;
import javax.inject.Provider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
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
    // The classloader used to discover configuration files on the classpath. When null, the thread
    // context classloader is used. In a Maven build the thread context classloader is the plugin
    // realm, which does not see the project's compile dependencies, so the plugin sets this to a
    // classloader over the project classpath (see RosettaStandaloneSetup#setClasspathClassLoader).
    private ClassLoader classLoader;

    public static RuneConfigurationFileProvider createFromFile(String fileName) {
        return new RuneConfigurationFileProvider(false, fileName);
    }
    public static RuneConfigurationFileProvider createFromClasspath(String fileName) {
        return new RuneConfigurationFileProvider(true, fileName);
    }

    /** Sets the classloader used to discover configuration files on the classpath. */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private ClassLoader resourceClassLoader() {
        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
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

    /**
     * Returns every Rune configuration file to union, with the primary one (the value returned by
     * {@link #get()}, i.e. the current project's config) first. Dependency configs that share an id
     * are shadowed by the current project's. Used to build the union of {@code serializationConfig}
     * entries across a project and its dependencies.
     * <p>
     * Dependency configs are always discovered from the classpath by their canonical names
     * ({@link #FILE_NAME}/{@link #LEGACY_FILE_NAME}), regardless of how the primary config was
     * located. This matters for the Maven path: there the current project's config is passed
     * explicitly (an absolute file path, because at {@code generate-sources} time it is not yet
     * copied onto the classpath), while its dependencies' configs are available on the classpath
     * inside their jars. The explicit primary is added first, so it still shadows on id collisions.
     */
    public Collection<URL> getResources() {
        LinkedHashSet<URL> resources = new LinkedHashSet<>();
        URL primary = get();
        if (primary != null) {
            resources.add(primary);
        }
        addClasspathResources(resources, FILE_NAME);
        addClasspathResources(resources, LEGACY_FILE_NAME);
        return resources;
    }

    private void addClasspathResources(LinkedHashSet<URL> resources, String name) {
        try {
            Enumeration<URL> found = resourceClassLoader().getResources(name);
            while (found.hasMoreElements()) {
                resources.add(found.nextElement());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to enumerate configuration files named " + name, e);
        }
    }

    private URL fromClasspath(String name) {
        return resourceClassLoader().getResource(name);
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
