package com.regnosys.rosetta.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.formatting2.FormattingOptionsAdaptor;
import com.regnosys.rosetta.formatting2.FormattingOptionsService;
import com.regnosys.rosetta.formatting2.ResourceFormatterService;

/**
 * A command-line tool for formatting `.rosetta` files in a specified directory.
 * <p>
 * This tool uses the {@link ResourceFormatterService} to apply consistent formatting to each 
 * `.rosetta` file in the provided directory. It loads each file as a resource, applies 
 * formatting in-place, and saves the modified file back to disk. The tool can be run with a 
 * single directory path argument, which is used to locate `.rosetta` files.
 * </p>
 *
 * <h2>Usage:</h2>
 * <pre>
 * java ResourceFormattingTool /path/to/directory
 * </pre>
 * <p>
 * If no valid directory path is provided as an argument, the program will exit with an error message.
 * </p>
 */
public class ResourceFormattingTool {
	@Inject
	private static FormattingOptionsService formattingOptionsService;
	
	private static Logger LOGGER = LoggerFactory.getLogger(ResourceFormattingTool.class);
	
	public static void main(String[] args) throws IOException {
		int maxArgs = 2;
		
		if (args.length == 0) {
            exitProgram("Please provide the directory path as an argument.");
        }
		
		if (args.length > maxArgs) {
			exitProgram("Too many arguments. Please provide maximum " + maxArgs + " arguments.");
        }
		
		Path directory = Paths.get(args[0]);
        if (!Files.isDirectory(directory)) {
        	exitProgram("The provided path is not a valid directory.");
        }
        
        // check if optional parameter was given. If not use default value
        ITypedPreferenceValues formattingOptions;
        if(args.length > 1) {
        	String formattingOptionsPath = args[1];
        	try {
    			formattingOptions = formattingOptionsService.readPreferencesFromFile(formattingOptionsPath);
    		} catch (IOException e) {
    			throw new IOException("Configuration file at " + formattingOptionsPath + " not found.", e);
    		}
        } else {
        	formattingOptions = formattingOptionsService.getDefaultPreferences();
        }
        
        Injector inj = new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration();
		ResourceSet resourceSet = inj.getInstance(ResourceSet.class);
		ResourceFormatterService formatterService = inj.getInstance(ResourceFormatterService.class);
        
		List<Resource> resources = null;
		try {
            // Find all .rosetta files in the directory and load them from disk
            resources = Files.walk(directory)
                .filter(path -> path.toString().endsWith(".rosetta"))
                .map(file -> resourceSet.getResource(URI.createFileURI(file.toString()), true))
                .collect(Collectors.toList());
            
        } catch (IOException e) {
            LOGGER.error("Error processing files: " + e.getMessage());
        }
		
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
	
	private static void exitProgram(String msg) {
		LOGGER.error(msg);
        System.exit(1);
	}
}
