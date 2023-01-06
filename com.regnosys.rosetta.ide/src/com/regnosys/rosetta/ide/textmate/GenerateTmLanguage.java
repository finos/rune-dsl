package com.regnosys.rosetta.ide.textmate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GenerateTmLanguage {
	/**
	 * param 0: path to input yaml file
	 * param 1: output path for json file
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			throw new IllegalArgumentException("Expected two variables, but received " + args.length);
		}
		String inputPath = args[0];
		String outputPath = args[1];
		
		GenerateTmLanguage generator = new GenerateTmLanguage();
		generator.generateTmLanguage(inputPath, outputPath);
	}
	
	private List<String> regexKeys = List.of("begin", "end", "match", "while");
	
	private void generateTmLanguage(String inputPath, String outputPath) throws IOException {
		Map<String, Object> input = loadYaml(inputPath);
		Map<String, String> variables = readVariables(input);
		input.remove("variables");
		applyVariablesRecursively(input, (key) -> regexKeys.contains(key), variables);
		writeJson(input, outputPath);
	}

	private Map<String, Object> loadYaml(String inputPath) throws FileNotFoundException {
		File f = new File(inputPath);
        Yaml yaml = new Yaml();
		return yaml.load(new FileReader(f));
	}
	
	private void writeJson(Map<String, Object> input, String outputPath) throws IOException {
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		File result = new File(outputPath);
		result.getParentFile().mkdirs();
		result.createNewFile();
		try (Writer writer = new FileWriter(result, false)) {
		    gson.toJson(input, writer);
		}
	}

	private Map<String, String> readVariables(Map<String, Object> yaml) {
		LinkedHashMap<String, String> result = new LinkedHashMap<>(yaml.size());
		
		Object rawVariables = yaml.get("variables");
		if (!(rawVariables instanceof Map)) {
			return result;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> variables = (Map<String, Object>)rawVariables;
		for (Entry<String, Object> variable : variables.entrySet()) {
		    if (variable.getValue() instanceof String) {
		    	String rawValue = (String)variable.getValue();
		    	result.put(variable.getKey(), applyVariables(rawValue, result));
		    }
		}
		return result;
	}
	
	private String applyVariables(String input, Map<String, String> variables) {
		for (Entry<String, String> variable : variables.entrySet()) {
		    input = applyVariable(input, variable);
		}
		return input;
	}
	
	private String applyVariable(String input, Entry<String, String> variable) {
		return input.replace("{{" + variable.getKey() + "}}", variable.getValue());
	}
	
	@SuppressWarnings("unchecked")
	private void applyVariablesRecursively(Object input, Predicate<String> keyMatcher, Map<String, String> variables) {
		if (input instanceof Map) {
			for (Entry<String, Object> node : ((Map<String, Object>)input).entrySet()) {
			    if (node.getValue() instanceof String) {
			    	if (keyMatcher.test(node.getKey())) {
				    	node.setValue(applyVariables((String)node.getValue(), variables));
			    	}
			    } else {
			    	applyVariablesRecursively(node.getValue(), keyMatcher, variables);
			    }
			}
		} else if (input instanceof List) {
			for (Object item : (List<Object>)input) {
				applyVariablesRecursively(item, keyMatcher, variables);
			}
		}
	}
}
