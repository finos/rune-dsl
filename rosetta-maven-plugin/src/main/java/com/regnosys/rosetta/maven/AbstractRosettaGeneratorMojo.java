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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.xtext.builder.standalone.LanguageAccess;
import org.eclipse.xtext.builder.standalone.compiler.CompilerConfiguration;
import org.eclipse.xtext.builder.standalone.compiler.IJavaCompiler;
import org.eclipse.xtext.maven.AbstractXtextGeneratorMojo;
import org.eclipse.xtext.maven.ClusteringConfig;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

// TODO: decouple from `AbstractXtextGeneratorMojo`/`xtext-maven-plugin`. They differ too much,
// which makes this implementation too hacky.
public abstract class AbstractRosettaGeneratorMojo extends AbstractXtextGeneratorMojo {

	@Parameter(defaultValue = "true")
	boolean addOutputDirectoriesToCompileSourceRoots = Boolean.TRUE;
	
	@Parameter
	private String classPathLookupFilter;
	
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
	
	@Parameter( defaultValue = "${mojoExecution}", readonly = true )
	private MojoExecution mojoExecution;
	
	@Parameter( readonly = true, defaultValue = "${plugin.artifacts}" )
	private List<Artifact> pluginDependencies;
	
	@Parameter(defaultValue = "true") // NOTE: we have a different default from Xtext!! We want to have this enabled by default.
	private boolean incrementalXtextBuild;

	// TODO: add this method to Xtext so I don't have to overwrite `internalExecute`
	// and duplicate all of the above parameters.
	protected Module createModule() {
		return new RosettaMavenStandaloneBuilderModule();
	}
	
	@Override
	protected void internalExecute() throws MojoExecutionException {
		if (addOutputDirectoriesToCompileSourceRoots) {
			configureMavenOutputs();
		}
		Map<String, LanguageAccess> languages = new RosettaLanguageAccessFactory().createLanguageAccess(getLanguages(),
				this.getClass().getClassLoader());
		Injector injector = Guice.createInjector(createModule());
		RosettaStandaloneBuilder builder = injector.getInstance(RosettaStandaloneBuilder.class);
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
}
