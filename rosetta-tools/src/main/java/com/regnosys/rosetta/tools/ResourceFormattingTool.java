package com.regnosys.rosetta.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.formatting2.ResourceFormatterService;
import com.regnosys.rosetta.formatting2.XtextResourceFormatter;

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
	private static Logger LOGGER = LoggerFactory.getLogger(XtextResourceFormatter.class);
	
	public static void main(String[] args) {
		if (args.length == 0) {
            System.out.println("Please provide the directory path as an argument.");
            System.exit(1);
        }
		
		Path directory = Paths.get(args[0]);
        if (!Files.isDirectory(directory)) {
            System.out.println("The provided path is not a valid directory.");
            System.exit(1);
        }
        
        Injector inj = new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration();
		ResourceSet resourceSet = inj.getInstance(ResourceSet.class);
		ResourceFormatterService formatterService = inj.getInstance(ResourceFormatterService.class);
        
		try {
            // Find all .rosetta files in the directory and load them from disk
            List<Resource> resources = Files.walk(directory)
                .filter(path -> path.toString().endsWith(".rosetta"))
                .map(file -> resourceSet.getResource(URI.createFileURI(file.toString()), true))
                .collect(Collectors.toList());
            
            //format resources
            formatterService.formatCollection(resources, null);
        } catch (IOException e) {
            LOGGER.error("Error processing files: " + e.getMessage());
        }
	}
}
