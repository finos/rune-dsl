package com.regnosys.rosetta.maven;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.mwe.core.resources.ResourceLoader;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.formatting2.ResourceFormatterService;
import com.regnosys.rosetta.formatting2.RosettaFormatterPreferenceKeys;

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
 * <li>{@code mvn com.regnosys.rosetta:rosetta-maven-plugin:version:format -Dpath="path/to/directory"}</li>
 * <li>Optionally, provide a custom formatting options file using
 * {@code -DformattingOptionsPath="path/to/formattingOptions.json"}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Example with both parameters:
 * <ul>
 * <li>{@code mvn com.regnosys.rosetta:rosetta-maven-plugin:version:format -Dpath="path/to/directory" -DformattingOptionsPath="path/to/formattingOptions.json"}</li>
 * </ul>
 * </p>
 */
@Mojo(name = "format")
public class ResourceFormatterMojo extends AbstractMojo {
	public static String PREFERENCE_INDENTATION_KEY = "indentation";
	public static String PREFERENCE_MAX_LINE_WIDTH_KEY = "maxLineWidth";
	public static String PREFERENCE_CONDITIONAL_MAX_LINE_WIDTH_KEY = "conditionalMaxLineWidth";

	private static Logger LOGGER = LoggerFactory.getLogger(ResourceFormatterMojo.class);

	/**
	 * Path to the directory of files to be formatted
	 */
	@Parameter(property = "path", required = true)
	private String path;

	/**
	 * Path to the .json file containing formatting options
	 */
	@Parameter(property = "formattingOptionsPath", required = false)
	private String formattingOptionsPath;

	private static final String DEFAULT_FORMATTING_OPTIONS_PATH = "default-formatting-options.json";

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Path directory = Paths.get(path);
		LOGGER.info("Mojo running on path:" + directory.toString());

		FormattingOptions formattingOptions = null;
		try {
			formattingOptions = readFormattingOptions(formattingOptionsPath);
		} catch (IOException e) {
			LOGGER.error("Config file not found.", e);
		}

		Injector inj = new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration();
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
		formatterService.formatCollection(resources, createPreferences(formattingOptions),
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

	public static FormattingOptions readFormattingOptions(String optionsPath) throws IOException, MojoFailureException {
		InputStream resourceStream;
		// If path not given, use default one
		if (optionsPath == null) {
			// Retrieve resource as an InputStream
			resourceStream = ResourceLoader.class.getClassLoader().getResourceAsStream(DEFAULT_FORMATTING_OPTIONS_PATH);
		} else {
			resourceStream = new FileInputStream(optionsPath);
		}

		// Create an ObjectMapper, read JSON into a Map
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = null;
		try {
			map = objectMapper.readValue(resourceStream, Map.class);
		} catch (IOException e) {
			throw new MojoFailureException("Error processing formatting options: " + e.getMessage(), e);
		}

		// Create a FormattingOptions object
		FormattingOptions formattingOptions = new FormattingOptions();

		// Populate the FormattingOptions object
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (value instanceof String) {
				formattingOptions.putString(key, (String) value);
			} else if (value instanceof Number) {
				formattingOptions.putNumber(key, (Number) value);
			} else if (value instanceof Boolean) {
				formattingOptions.putBoolean(key, (Boolean) value);
			} else {
				throw new IllegalArgumentException("Unsupported value type for key: " + key);
			}
		}
		return formattingOptions;
	}

	public static ITypedPreferenceValues createPreferences(FormattingOptions options) {
		MapBasedPreferenceValues preferences = new MapBasedPreferenceValues();

		String indent = "\t";
		if (options != null) {
			if (options.isInsertSpaces()) {
				indent = Strings.padEnd("", options.getTabSize(), ' ');
			}
		}
		preferences.put(PREFERENCE_INDENTATION_KEY, indent);

		if (options == null) {
			return preferences;
		}

		Number conditionalMaxLineWidth = options.getNumber(PREFERENCE_CONDITIONAL_MAX_LINE_WIDTH_KEY);
		if (conditionalMaxLineWidth != null) {
			preferences.put(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth, conditionalMaxLineWidth.intValue());
		}
		Number maxLineWidth = options.getNumber(PREFERENCE_MAX_LINE_WIDTH_KEY);
		if (maxLineWidth != null) {
			preferences.put(RosettaFormatterPreferenceKeys.maxLineWidth, maxLineWidth.intValue());
			if (conditionalMaxLineWidth == null) {
				int defaultConditionalMaxLineWidth = RosettaFormatterPreferenceKeys.conditionalMaxLineWidth
						.toValue(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth.getDefaultValue());
				int defaultMaxLineWidth = RosettaFormatterPreferenceKeys.maxLineWidth
						.toValue(RosettaFormatterPreferenceKeys.maxLineWidth.getDefaultValue());
				double defaultRatio = (double) defaultConditionalMaxLineWidth / defaultMaxLineWidth;
				preferences.put(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth,
						(int) (maxLineWidth.doubleValue() * defaultRatio));
			}
		}

		return preferences;
	}
}