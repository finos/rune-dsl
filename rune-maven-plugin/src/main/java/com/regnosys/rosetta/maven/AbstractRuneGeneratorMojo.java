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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.regnosys.rosetta.config.file.RuneConfigurationFileProvider;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.xtext.builder.standalone.LanguageAccess;
import org.eclipse.xtext.builder.standalone.compiler.CompilerConfiguration;
import org.eclipse.xtext.builder.standalone.compiler.IJavaCompiler;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.maven.AbstractXtextGeneratorMojo;
import org.eclipse.xtext.maven.ClusteringConfig;
import org.eclipse.xtext.maven.Language;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// TODO: decouple from `AbstractXtextGeneratorMojo`/`xtext-maven-plugin`. They differ too much,
// which makes this implementation too hacky.
public abstract class AbstractRuneGeneratorMojo extends AbstractXtextGeneratorMojo {

    @Parameter(defaultValue = "true")
    boolean addOutputDirectoriesToCompileSourceRoots = Boolean.TRUE;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter
    private List<Language> languages;

    @Parameter
    private String classPathLookupFilter;

    /**
     * @deprecated The configuration file is now discovered by convention at
     * {@code src/main/resources/rune-config.yml} (or the legacy {@code rosetta-config.yml}) and
     * this parameter carries no information the plugin cannot derive itself. It is kept for
     * backwards compatibility and will be removed in the next major Rune DSL release. If both
     * {@code runeConfig} and {@code rosettaConfig} are set, {@code runeConfig} takes precedence.
     */
    @Deprecated
    @Parameter
    private String runeConfig;

    /**
     * @deprecated The configuration file is now discovered by convention at
     * {@code src/main/resources/rune-config.yml} (or the legacy {@code rosetta-config.yml}) and
     * this parameter carries no information the plugin cannot derive itself. It is kept for
     * backwards compatibility and will be removed in the next major Rune DSL release. If both
     * {@code runeConfig} and {@code rosettaConfig} are set, {@code runeConfig} takes precedence.
     */
    @Deprecated
    @Parameter
    private String rosettaConfig;

    @Parameter(defaultValue = "${project.compileSourceRoots}", required = true)
    private List<String> javaSourceRoots;

    @Parameter(defaultValue = "true")
    private Boolean failOnValidationError;

    @Parameter(defaultValue = "${project.build.directory}/xtext-temp")
    private String tmpClassDirectory;

    @Parameter
    private ClusteringConfig clusteringConfig;

    @Parameter(property = "maven.compiler.source", defaultValue = "1.6")
    private String compilerSourceLevel;

    @Parameter(property = "maven.compiler.target", defaultValue = "1.6")
    private String compilerTargetLevel;

    @Parameter(defaultValue = "false")
    private Boolean compilerSkipAnnotationProcessing;

    @Parameter(defaultValue = "false")
    private Boolean compilerPreserveInformationAboutFormalParameters;

    @Parameter(defaultValue = "false")
    private boolean writeStorageResources;

    @Parameter(defaultValue = "false")
    private boolean writeClasspathConfiguration = false;

    @Parameter(defaultValue = "${project.build.directory}/xtext.classpath")
    private String classpathConfigurationLocation;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojoExecution;

    @Parameter(readonly = true, defaultValue = "${plugin.artifacts}")
    private List<Artifact> pluginDependencies;

    @Parameter(defaultValue = "true")
    // NOTE: we have a different default from Xtext!! We want to have this enabled by default.
    private boolean incrementalXtextBuild;

    /**
     * Directory the per-model marker file ({@code META-INF/rune/model.properties}) is written
     * under. Defaults to the build output directory; overridable so tests can redirect it.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private String modelPropertiesOutputDirectory;

    // TODO: add this method to Xtext so I don't have to overwrite `internalExecute`
    // and duplicate all of the above parameters.
    protected Module createModule() {
        return new RuneMavenStandaloneBuilderModule();
    }

    /**
     * Whether this Mojo should emit the {@code META-INF/rune/model.properties} marker. Only
     * {@code RuneGenerateMojo} does; {@code RuneTestGenerateMojo} writes to the test output
     * directory, and a second marker there would compete on the classpath with the main one.
     */
    protected boolean writesModelProperties() {
        return false;
    }

    static final String CONFIG_DEPRECATION_WARNING =
            "The 'runeConfig'/'rosettaConfig' parameter is deprecated and will be removed in the next "
            + "major Rune DSL release. The configuration file is now discovered by convention at "
            + "'src/main/resources/rune-config.yml' (or the legacy 'rosetta-config.yml'); remove this "
            + "parameter and let the plugin locate it automatically.";

    static final String LEGACY_CONFIG_FILE_NAME_WARNING =
            "Found a legacy '" + RuneConfigurationFileProvider.LEGACY_FILE_NAME + "' configuration file. Please "
            + "rename it to '" + RuneConfigurationFileProvider.FILE_NAME + "'; support for the legacy name is "
            + "deprecated and will be removed in a future release.";

    /**
     * Probes the conventional location for a model's Rune configuration file, on disk, relative
     * to the model's own {@code basedir}. Returns the config {@link File}, or {@code null} if
     * neither the current nor the legacy name is present. This is the single definition of the
     * convention: both {@link #resolveConfig()} and the marker's presence check key off it.
     */
    static File findConventionalConfigFile(File basedir) {
        File resourcesDir = new File(basedir, "src/main/resources");
        File configFile = new File(resourcesDir, RuneConfigurationFileProvider.FILE_NAME);
        if (configFile.isFile()) {
            return configFile;
        }
        File legacyConfigFile = new File(resourcesDir, RuneConfigurationFileProvider.LEGACY_FILE_NAME);
        if (legacyConfigFile.isFile()) {
            return legacyConfigFile;
        }
        return null;
    }

    /**
     * Resolution logic for {@link #resolveConfig()}, extracted as a pure function (no Mojo state)
     * so it is directly unit-testable. If either deprecated parameter is set, it is still honoured
     * (transitional behaviour), with {@code runeConfig} preferred when both are set; either way the
     * shared deprecation warning is logged. If neither is set, the conventional file is resolved by
     * {@link #findConventionalConfigFile(File)}.
     */
    static String resolveConfig(String runeConfig, String rosettaConfig, File basedir, Consumer<String> deprecationWarningSink) {
        if (runeConfig != null || rosettaConfig != null) {
            deprecationWarningSink.accept(CONFIG_DEPRECATION_WARNING);
        }
        if (runeConfig != null) {
            return runeConfig;
        }
        if (rosettaConfig != null) {
            return rosettaConfig;
        }
        File conventional = findConventionalConfigFile(basedir);
        return conventional != null ? conventional.getAbsolutePath() : null;
    }

    /**
     * Resolves the configuration file path: the deprecated {@code runeConfig}/{@code rosettaConfig}
     * parameters if set (with a deprecation warning), otherwise the conventional location.
     */
    private String resolveConfig() {
        return resolveConfig(runeConfig, rosettaConfig, getProject().getBasedir(), getLog()::warn);
    }

    /**
     * Warns when the conventional configuration file found for the marker is the legacy
     * {@code rosetta-config.yml} rather than {@code rune-config.yml}, so a model owner is nudged to
     * rename it. Extracted as a pure function (no Mojo state) so it is directly unit-testable,
     * matching {@link #resolveConfig(String, String, File, Consumer)}.
     */
    static void warnIfLegacyConfigFileName(File conventionalConfigFile, Consumer<String> warningSink) {
        if (conventionalConfigFile != null
                && RuneConfigurationFileProvider.LEGACY_FILE_NAME.equals(conventionalConfigFile.getName())) {
            warningSink.accept(LEGACY_CONFIG_FILE_NAME_WARNING);
        }
    }

    private void writeModelProperties() throws MojoExecutionException {
        File conventionalConfigFile = findConventionalConfigFile(getProject().getBasedir());
        warnIfLegacyConfigFileName(conventionalConfigFile, getLog()::warn);
        MavenProject project = getProject();
        String modelSourceGav = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
        String modelId;
        List<String> parentModels;
        try {
            modelId = ModelAncestry.computeModelId(project);
            parentModels = ModelAncestry.parseDirectParents(project.getProperties(), getLog()::warn).stream()
                    .map(ModelAncestry.ParentGav::ga)
                    .collect(Collectors.toList());
            ModelAncestry.crossCheckClasspathModels(classpathJars(), parentModels, getLog()::warn);
        } catch (RuntimeException e) {
            // Broken ancestry computation must degrade the marker (consumers fall back to classpath
            // order when modelId is absent), never fail the build.
            getLog().warn("Failed to compute the model's ancestry for the model marker; writing it without "
                    + "identity/ancestry information.", e);
            modelId = null;
            parentModels = null;
        }
        try {
            new ModelPropertiesWriter().write(new File(modelPropertiesOutputDirectory), conventionalConfigFile != null,
                    mojoExecution.getVersion(), modelSourceGav, modelId, parentModels);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write model properties.", e);
        }
    }

    /**
     * Narrows {@code artifacts} to the compile classpath (scopes {@code compile}, {@code provided}
     * and {@code system}). {@link MavenProject#getArtifacts()} returns every artifact resolved for
     * this Mojo execution regardless of scope, but {@link ModelAncestry#crossCheckClasspathModels}
     * and its warning text are documented in terms of "the compile classpath", so this filter is
     * what keeps that claim accurate — without it a test- or runtime-scope model jar could trigger a
     * warning to declare a parent that is not actually a compile dependency. Extracted as a static,
     * pure function (no Mojo state) so it is directly unit-testable.
     */
    static List<ModelAncestry.ClasspathJar> classpathJars(Collection<Artifact> artifacts) {
        ArtifactFilter compileScope = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);
        return artifacts.stream()
                .filter(artifact -> artifact.getFile() != null)
                .filter(compileScope::include)
                .map(artifact -> new ModelAncestry.ClasspathJar(artifact.getGroupId(), artifact.getArtifactId(),
                        artifact.getVersion(), artifact.getFile()))
                .collect(Collectors.toList());
    }

    private List<ModelAncestry.ClasspathJar> classpathJars() {
        return classpathJars(getProject().getArtifacts());
    }

    @Override
    public MavenProject getProject() {
        return project;
    }

    @Override
    public List<Language> getLanguages() {
        return languages;
    }

    @Override
    protected void internalExecute() throws MojoExecutionException {
        if (addOutputDirectoriesToCompileSourceRoots) {
            configureMavenOutputs();
        }
        Language language = getLanguages().stream().findFirst().orElseThrow(() -> new MojoExecutionException("Only one language supported by the Rosetta Plugin."));
        // Turn off "Java support", which is enabled by default, since the Rune DSL does not link against Java.
        // This saves time and memory during the build.
        language.setJavaSupport(false);

        String resolvedConfig = resolveConfig();
        Map<String, LanguageAccess> languages = new RuneLanguageAccessFactory()
                .createLanguageAccess(language, resolvedConfig, this.getClass().getClassLoader(), createClasspathClassLoader());
        Injector injector = Guice.createInjector(createModule());
        RuneStandaloneBuilder builder = injector.getInstance(RuneStandaloneBuilder.class);
        builder.setBaseDir(getProject().getBasedir().getAbsolutePath());
        builder.setLanguages(languages);
        builder.setEncoding(getEncoding());
        builder.setClassPathEntries(getClasspathEntries());
        builder.setClassPathLookUpFilter(classPathLookupFilter);
        builder.setSourceDirs(getSourceRoots());
        builder.setJavaSourceDirs(javaSourceRoots);
        builder.setFailOnValidationError(failOnValidationError);
        builder.setTempDir(createTempDir().getAbsolutePath());
        builder.setDebugLog(getLog().isDebugEnabled());
        builder.setIncrementalBuild(incrementalXtextBuild);
        builder.setWriteStorageResources(writeStorageResources);
        if (writeClasspathConfiguration) {
            builder.setClasspathConfigurationLocation(classpathConfigurationLocation, mojoExecution.getGoal(), getClassOutputDirectory());
        }
        if (clusteringConfig != null) {
            builder.setClusteringConfig(clusteringConfig.convertToStandaloneConfig());
        }
        configureCompiler(builder.getCompiler());
        logState();
        boolean errorDetected = !builder.launch();
        if (errorDetected && failOnValidationError) {
            throw new MojoExecutionException("Execution failed due to a severe validation error.");
        }
        if (writesModelProperties()) {
            writeModelProperties();
        }
    }
    // Override to ensure we use this class's injected MavenProject
    @Override
    protected void addCompileSourceRoots(Language language) {
        if (language.getOutputConfigurations() == null) {
            return;
        }
        for (OutputConfiguration configuration : language.getOutputConfigurations()) {
            for (String output : configuration.getOutputDirectories()) {
                getLog().debug("Adding output folder " + output + " to compile roots");
                getProject().addCompileSourceRoot(output);
            }
        }
    }

    @Override
    protected void addTestCompileSourceRoots(Language language) {
        if (language.getOutputConfigurations() == null) {
            return;
        }
        for (OutputConfiguration configuration : language.getOutputConfigurations()) {
            for (String output : configuration.getOutputDirectories()) {
                getLog().debug("Adding output folder " + output + " to test compile roots");
                getProject().addTestCompileSourceRoot(output);
            }
        }
    }

    private void configureCompiler(IJavaCompiler compiler) {
        CompilerConfiguration conf = compiler.getConfiguration();
        conf.setSourceLevel(compilerSourceLevel);
        conf.setTargetLevel(compilerTargetLevel);
        conf.setVerbose(getLog().isDebugEnabled());
        conf.setSkipAnnotationProcessing(compilerSkipAnnotationProcessing);
        conf.setPreserveInformationAboutFormalParameters(compilerPreserveInformationAboutFormalParameters);
    }

    private File createTempDir() {
        File tmpDir = new File(tmpClassDirectory + tmpDirSuffix());
        if (!tmpDir.mkdirs() && !tmpDir.exists()) {
            throw new IllegalArgumentException("Couldn't create directory '" + tmpClassDirectory + "'.");
        }
        return tmpDir;
    }

    private void logState() {
        getLog().info(
                "Encoding: " + (getEncoding() == null ? "not set. Encoding provider will be used." : getEncoding()));
        getLog().info("Compiler source level: " + compilerSourceLevel);
        getLog().info("Compiler target level: " + compilerTargetLevel);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Source dirs: " + IterableExtensions.join(getSourceRoots(), ", "));
            getLog().debug("Java source dirs: " + IterableExtensions.join(javaSourceRoots, ", "));
            getLog().debug("Classpath entries: " + IterableExtensions.join(getClasspathEntries(), ", "));
        }
    }

    private Set<String> getClasspathEntries() {
        Set<String> classpathElements = getClasspathElements();
        if (isIncludePluginDependencies()) {
            getLog().info("Including plugin dependencies");
            List<String> pluginClasspathElements = pluginDependencies.stream()
                    .map(e -> e.getFile().toPath().toString())
                    .collect(Collectors.toList());
            classpathElements.addAll(pluginClasspathElements);
        }
        return classpathElements;
    }

    /**
     * Builds a classloader over the project's compile classpath, with the plugin realm as parent.
     * The config provider uses this to discover dependency configuration files, which the plugin
     * realm's (thread context) classloader cannot see.
     */
    private ClassLoader createClasspathClassLoader() {
        Set<String> entries = getClasspathEntries();
        List<URL> urls = new ArrayList<>(entries.size());
        for (String entry : entries) {
            try {
                urls.add(new File(entry).toURI().toURL());
            } catch (MalformedURLException e) {
                getLog().warn("Skipping malformed classpath entry " + entry, e);
            }
        }
        return new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
    }
}
