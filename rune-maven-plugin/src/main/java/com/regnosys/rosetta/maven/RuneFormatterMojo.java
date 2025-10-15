package com.regnosys.rosetta.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.formatting2.FormattingOptionsService;
import com.regnosys.rosetta.formatting2.ResourceFormatterService;

/**
 * <p>
 * Formatter Plugin: A Maven plugin to help with the formatting of resources.
 * </p>
 * 
 * <p>
 * Given a path to a directory holding {@code .rosetta} resources, it formats
 * the files according to set formatting rules. Additionally, you can specify a
 * custom configuration file for formatting options using the
 * {@code formattingOptionsPath} parameter. If the {@code formattingOptionsPath}
 * is not provided, the plugin will use default formatting options.
 * </p>
 * 
 * 
 * <p>
 * To run the goal:
 * <ul>
 * <li>{@code mvn org.finos.rune:rune-maven-plugin:version:format -Dpath="path/to/directory"}</li>
 * <li>Optionally, provide a custom formatting options file using
 * {@code -DformattingOptionsPath="path/to/formattingOptions.json"}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Example with both parameters:
 * <ul>
 * <li>{@code mvn org.finos.rune:rune-maven-plugin:version:format -Dpath="path/to/directory" -DformattingOptionsPath="path/to/formattingOptions.json"}</li>
 * </ul>
 * </p>
 */
@Mojo(name = "format")
public class RuneFormatterMojo extends AbstractMojo {
	private static Logger LOGGER = LoggerFactory.getLogger(RuneFormatterMojo.class);

	/**
	 * Path to the directory of files to be formatted
	 */
	@Parameter(property = "path", required = true)
	private String path;

	/**
	 * Optional path to a .json file containing formatting options
	 */
	@Parameter(property = "formattingOptionsPath")
	private String formattingOptionsPath;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Path directory = Paths.get(path);
		LOGGER.info("Mojo running on path:" + directory.toString());

		Injector inj = new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration();
		
		FormattingOptionsService formattingOptionsService = inj.getInstance(FormattingOptionsService.class);
		ITypedPreferenceValues formattingOptions;
		if (formattingOptionsPath == null) {
			formattingOptions = formattingOptionsService.getDefaultPreferences();
		} else {
			try {
				formattingOptions = formattingOptionsService.readPreferencesFromFile(formattingOptionsPath);
			} catch (IOException e) {
				throw new MojoFailureException("Failed to read formatting options from file " + formattingOptionsPath + ".", e);
			}
		}
		
		ResourceSet resourceSet = inj.getInstance(ResourceSet.class);
		ResourceFormatterService formatterService = inj.getInstance(ResourceFormatterService.class);

		List<Resource> resources;
		try {
			// Find all .rosetta files in the directory and load them from disk
			resources = Files.walk(directory).filter(path -> path.toString().endsWith(".rosetta"))
					.map(file -> resourceSet.getResource(URI.createFileURI(file.toString()), true))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new MojoFailureException("Error processing files: " + e.getMessage(), e);
		}
		// format resources
		formatterService.formatCollection(resources, formattingOptions,
				(resource, formattedText) -> {
					Path resourcePath = Path.of(resource.getURI().toFileString());
					try {
						Files.writeString(resourcePath, formattedText);
						LOGGER.info("Content written to file: " + resourcePath);
					} catch (IOException e) {
						LOGGER.error("Error writing to file.", e);
					}
				});
	}
}