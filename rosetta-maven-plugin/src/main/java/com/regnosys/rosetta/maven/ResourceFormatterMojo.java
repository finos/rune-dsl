package com.regnosys.rosetta.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.formatting2.ResourceFormatterService;

/**
 * <p>
 * Formatter Plugin: A Maven plugin to help with the formatting of resources.
 * </p>
 * 
 * <p>
 * Given a path to a directory holding {@code .rosetta} resources, it formats the files
 * according to set formatting rules.
 * </p>
 * 
 * <p>
 * To run the goal:
 * <ul>
 * <li>{@code mvn com.regnosys.rosetta:rosetta-maven-plugin:version:format -Dpath="path/to/directory"}</li>
 * </ul>
 * </p>
 */
@Mojo(name = "format")
public class ResourceFormatterMojo extends AbstractMojo {
	private static Logger LOGGER = LoggerFactory.getLogger(ResourceFormatterMojo.class);

	/**
	 * Path to the directory of files to be formatted
	 */
	@Parameter(property = "path", required = true)
	private String path;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Path directory = Paths.get(path);
		LOGGER.info("Mojo running on path:" + directory.toString());

		Injector inj = new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration();
		ResourceSet resourceSet = inj.getInstance(ResourceSet.class);
		ResourceFormatterService formatterService = inj.getInstance(ResourceFormatterService.class);

		RosettaBuiltinsService builtins = inj.getInstance(RosettaBuiltinsService.class);
		resourceSet.getResource(builtins.basicTypesURI, true);
		resourceSet.getResource(builtins.annotationsURI, true);

		List<Resource> resources = new ArrayList<>();
		try {
			// Find all .rosetta files in the directory and load them from disk
			resources = Files.walk(directory).filter(path -> path.toString().endsWith(".rosetta"))
					.map(file -> resourceSet.getResource(URI.createFileURI(file.toString()), true))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new MojoFailureException("Error processing files: " + e.getMessage(), e);
		}
		// format resources
		formatterService.formatCollection(resources, null);

		// save each resource
		resources.forEach(resource -> {
			try {
				resource.save(null);
				LOGGER.info("Successfully formatted and saved file at location " + resource.getURI());
			} catch (IOException e) {
				LOGGER.error("Error saving file at location " + resource.getURI() + ": " + e.getMessage(), e);
			} catch (RuntimeException e) {
				LOGGER.error(
						"RuntimeException while saving in following file: " + resource.getURI() + ": " + e.getMessage(),
						e);
			}
		});

	}
}