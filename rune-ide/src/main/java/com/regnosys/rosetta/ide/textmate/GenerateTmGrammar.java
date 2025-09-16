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

package com.regnosys.rosetta.ide.textmate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.naming.ConfigurationException;

import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.services.RosettaGrammarAccess;

public class GenerateTmGrammar {
	private static List<String> ignoredRosettaKeywords = List.of("..", "namespace", "condition", "required", "optional", "override", "for");
	
	/**
	 * param 0: path to input yaml file
	 * param 1: output path for json file
	 * @throws IOException 
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws IOException, ConfigurationException {
		if (args.length != 2) {
			throw new IllegalArgumentException("Expected two variables, but received " + args.length);
		}
		String inputPath = args[0];
		String outputPath = args[1];
		
		GenerateTmGrammar generator = new GenerateTmGrammar();
		generator.generateTmLanguage(inputPath, outputPath);
	}
	
	private Pattern variablePattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
	private List<String> regexKeys = List.of("match", "begin", "end", "while");
	
	private void generateTmLanguage(String inputPath, String outputPath) throws IOException, ConfigurationException {
		Map<Object, Object> input = loadYaml(inputPath);
		Map<String, String> variables = readVariables(input);
		input.remove("variables");
		applyVariablesRecursively(input, variables);
		inlineParameterizedIncludes(input);
		writeJson(input, outputPath);
		validateTm(input);
	}
	
	private void inlineParameterizedIncludes(Map<Object, Object> input) {
		Map<String, Set<Map<String, String>>> argumentMapPerInclude = new HashMap<>();
		gatherIncludeArguments(input, argumentMapPerInclude);
		inlineParameterizedIncludesRecursively(input, argumentMapPerInclude);
	}
	
	@SuppressWarnings("unchecked")
	private void gatherIncludeArguments(Object input, Map<String, Set<Map<String, String>>> argumentMapPerInclude) {
		if (input instanceof Map) {
			Map<Object, Object> inputMap = (Map<Object, Object>)input;
			Object rawInclude = inputMap.get("include");
			if (rawInclude != null && rawInclude instanceof String && inputMap.containsKey("arguments")) {
				Map<String, String> argumentMap = readStringMap(inputMap.get("arguments"), "argument");
				String include = ((String)rawInclude).substring(1);
				argumentMapPerInclude.computeIfAbsent(include, a -> new LinkedHashSet<>()).add(argumentMap);
				
				String inlineName = toInlineName(include, argumentMap);
				inputMap.put("include", "#" + inlineName);
				inputMap.remove("arguments");
			}
			for (Entry<?, ?> node : inputMap.entrySet()) {
				gatherIncludeArguments(node.getValue(), argumentMapPerInclude);
			}
		} else if (input instanceof List) {
			for (Object item : (List<?>)input) {
				gatherIncludeArguments(item, argumentMapPerInclude);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void inlineParameterizedIncludesRecursively(Object input, Map<String, Set<Map<String, String>>> argumentMapPerInclude) {
		if (input instanceof Map) {
			Map<?, ?> inputMap = (Map<?, ?>)input;
			Object rawRepo = inputMap.get("repository");
			if (rawRepo != null && rawRepo instanceof Map<?, ?>) {
				Map<Object, Object> repo = (Map<Object, Object>)rawRepo;
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				Map<Object, Object> inlinedDefinitions = new LinkedHashMap<>();
				for (Entry<Object, Object> definitionEntry : repo.entrySet()) {
					Object definitionName = definitionEntry.getKey();
					Object rawDefinition = definitionEntry.getValue();
					if (rawDefinition instanceof Map<?, ?>) {
						Map<?, ?> definition = (Map<?, ?>) rawDefinition;
						if (definition.containsKey("parameters")) {
							for (Map<String, String> argumentMap : argumentMapPerInclude.getOrDefault(definitionName, Collections.emptySet())) {
								String inlineName = toInlineName((String)definitionName, argumentMap);
								
								Map<?, ?> inlinedDefinition = gson.fromJson(gson.toJson(definition), Map.class);
								inlinedDefinition.remove("parameters");
								argumentMap.put("this", inlineName);
								applyVariablesRecursively(inlinedDefinition, argumentMap);
								inlinedDefinitions.put(inlineName, inlinedDefinition);
							}
						}
					}
				}
				repo.putAll(inlinedDefinitions);
				repo.entrySet().removeIf(e -> e.getValue() instanceof Map<?, ?> && ((Map<?, ?>)e.getValue()).containsKey("parameters"));
			}
			for (Entry<?, ?> node : inputMap.entrySet()) {
				inlineParameterizedIncludesRecursively(node.getValue(), argumentMapPerInclude);
			}
		} else if (input instanceof List) {
			for (Object item : (List<?>)input) {
				inlineParameterizedIncludesRecursively(item, argumentMapPerInclude);
			}
		}
	}
	private String toInlineName(String include, Map<String, String> arguments) {
		return include + new TreeMap<>(arguments).entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(",", "(", ")"));
	}
	
	private void validateTm(Map<Object, Object> input) throws ConfigurationException {
		ensureNoUnknownVariables(input);
		Map<Object, Object> namedPatterns = findNamedPatterns(input);
		List<TmValue<Object>> allPatterns = findAllPatterns(input, new ArrayList<>());
		for (TmValue<Object> pattern: allPatterns) {
			validatePattern(pattern, namedPatterns);
		}
		
		ensureAllRosettaKeywordsAreSupported(input);
	}
	
	private void ensureNoUnknownVariables(Object input) throws ConfigurationException {
		if (input instanceof Map) {
			Map<?, ?> inputMap = (Map<?, ?>)input;
			for (Entry<?, ?> node : inputMap.entrySet()) {
				if (node.getValue() instanceof String) {
					Matcher unknownVariableMatcher = variablePattern.matcher((String)node.getValue());
					List<MatchResult> unknownVariables = unknownVariableMatcher.results().collect(Collectors.toList());
					if (unknownVariables.size() > 0) {
						throw new ConfigurationException("At " + node.getKey() + ": Unknown variable(s): " + unknownVariables.stream().map(v -> v.group(1)).collect(Collectors.joining(", ")));
					}
			    } else {
			    	ensureNoUnknownVariables(node.getValue());
			    }
			}
		} else if (input instanceof List) {
			for (Object item : (List<?>)input) {
				ensureNoUnknownVariables(item);
			}
		}
	}
	
	private void ensureAllRosettaKeywordsAreSupported(Map<Object, Object> input) throws ConfigurationException {
		List<Pattern> regexes = findAllRegexes(input);
		
		Injector inj = new RosettaStandaloneSetup().createInjectorAndDoEMFRegistration();
		
		RosettaGrammarAccess grammarAccess = inj.getInstance(RosettaGrammarAccess.class);
		Grammar grammar = grammarAccess.getGrammar();
		Set<String> keywords = GrammarUtil.getAllKeywords(grammar);
		for (String ignoredKeyword: ignoredRosettaKeywords) {
			if (!keywords.contains(ignoredKeyword)) {
				throw new ConfigurationException("Sanity check failed. Please remove `" + ignoredKeyword + "` from the list of ignored keywords, as it is not a keyword of Rosetta.");
			}
		}
		
		List<String> keywordsWithoutToken = new ArrayList<>();
		for (String keyword: keywords) {
			if (!ignoredRosettaKeywords.contains(keyword)) {
				if (!regexes.stream().anyMatch(regex -> regex.matcher(keyword).matches())) {
					keywordsWithoutToken.add(keyword);
				}
			}
		}
		if (!keywordsWithoutToken.isEmpty()) {
			String keywordList = keywordsWithoutToken.stream()
					.map(k -> "`" + k + "`")
					.collect(Collectors.joining(", "));
			throw new ConfigurationException("The TextMate grammar contains no pattern that highlights the Rosetta keyword(s) " + keywordList + ". Add an appropriate pattern to `rosetta.tmLanguage.yaml` or add the keywords to the list of ignored Rosetta keywords.");
		}
	}
	
	private void validatePattern(TmValue<Object> pattern, Map<Object, Object> namedPatterns) throws ConfigurationException {
		if (!(pattern.value instanceof Map)) {
			throw new ConfigurationException("A pattern may not be of type " + pattern.value.getClass().getSimpleName() + ". " + pattern.getPath());
		}
		TmValue<Map<?, ?>> tmMap = pattern.asMap();
		
		Function<Predicate<String>, Predicate<Object>> string = pred -> (obj -> {
			if (obj instanceof String) {
				return pred.test((String)obj);
			}
			return false;
		});
		
		Predicate<Object> comment = obj -> obj == null || obj instanceof String;
		Predicate<Object> repository = obj -> obj == null || obj instanceof Map;
		Predicate<Object> patterns = obj -> obj == null || obj instanceof List;
		Predicate<Object> include = string.apply(value ->
			value.startsWith("#") && namedPatterns.containsKey(value.substring(1))
		);
		Predicate<Object> regex = string.apply(value -> {
			try {
				Pattern.compile(value);
			} catch (PatternSyntaxException e) {
				return false;
			}
			return true;
		});
		Predicate<Object> scopes = string.apply(v -> {
			List<String> allScopes = List.of(v.split(" "));
			return allScopes.stream().allMatch(scope -> {
				List<String> parts = List.of(scope.split("\\."));
				if (parts.size() == 0) {
					return false;
				}
				if (!parts.stream().allMatch(p -> p.matches("[a-z\\-]+"))) {
					return false;
				}
				return parts.get(parts.size() - 1).equals("rosetta");
			});
		}).or(v -> v == null);
		Predicate<Object> captures = obj -> {
			if (obj == null) {
				return true;
			}
			if (!(obj instanceof Map)) {
				return false;
			}
			for (Entry<?, ?> capture: ((Map<?, ?>)obj).entrySet()) {
				if (!(capture.getValue() instanceof Map)) {
					return false;
				}
				try {
					TmValue<Map<?, ?>> captureMap = new TmValue<>((Map<?, ?>) capture.getValue(), new ArrayList<>());
					runValidators(captureMap, Map.of("name", scopes, "patterns", patterns, "comment", comment));
				} catch (ConfigurationException e) {
					return false;
				}
			}
			return true;
		};
		// Types of patterns:
		// - include
		// - match
		// - begin/end
		// - begin/while
		// - list of patterns
		if (tmMap.value.get("include") != null) {
			if (!tmMap.path.get(tmMap.path.size() - 1).equals("patterns")) {
				// Note: this check is only necessary for Monaco. See https://github.com/zikaari/monaco-textmate/issues/13.
				// VS Code supports direct includes.
				throw new ConfigurationException("Validation failed on include: may only be used inside 'patterns'. " + tmMap.getPath());
			}
			runValidators(tmMap, Map.of("include", include, "comment", comment, "repository", repository));
		} else if (tmMap.value.get("match") != null) {
			runValidators(tmMap, Map.of("name", scopes, "match", regex, "captures", captures, "comment", comment, "repository", repository));
		} else if (tmMap.value.get("begin") != null && tmMap.value.get("end") != null) {
			runValidators(tmMap, Map.of("name", scopes, "begin", regex, "beginCaptures", captures, "end", regex, "endCaptures", captures, "comment", comment, "patterns", patterns, "repository", repository));
		} else if (tmMap.value.get("begin") != null && tmMap.value.get("while") != null) {
			runValidators(tmMap, Map.of("name", scopes, "begin", regex, "beginCaptures", captures, "while", regex, "whileCaptures", captures, "comment", comment, "patterns", patterns, "repository", repository));
		} else if (tmMap.value.get("patterns") != null) {
			runValidators(tmMap, Map.of("patterns", patterns, "comment", comment, "repository", repository));
		} else {
			throw new ConfigurationException("Unknown pattern object. ");
		}
	}
	
	private void runValidators(TmValue<Map<?, ?>> tmValue, Map<?, Predicate<Object>> validators) throws ConfigurationException {		
		for (Entry<?, ?> entry: tmValue.value.entrySet()) {
			Predicate<?> validator = validators.get(entry.getKey());
			if (validator == null) {
				throw new ConfigurationException("Unknown property " + entry.getKey() + ". " + tmValue.getPath());
			}
		}
		for (Entry<?, Predicate<Object>> validatorEntry: validators.entrySet()) {
			Predicate<Object> validator = validatorEntry.getValue();
			Object patternValue = tmValue.value.get(validatorEntry.getKey());
			if (!validator.test(patternValue)) {
				throw new ConfigurationException("Validation failed on " + validatorEntry.getKey() + ": " + patternValue + ". " + tmValue.getPath());
			}
		}
	}
	
	private Map<Object, Object> findNamedPatterns(Object input) {
		Map<Object, Object> result = new LinkedHashMap<>();
		if (input instanceof Map) {
			Map<?, ?> inputMap = (Map<?, ?>)input;
			for (Entry<?, ?> node : inputMap.entrySet()) {
			    if (node.getKey().equals("repository")) {
			    	result.putAll((Map<?, ?>) node.getValue());
			    }
			    result.putAll(findNamedPatterns(node.getValue()));
			}
		} else if (input instanceof List) {
			for (Object item : (List<?>)input) {
				result.putAll(findNamedPatterns(item));
			}
		}
		return result;
	}
	
	private List<TmValue<Object>> findAllPatterns(Object input, List<String> path) {
		List<TmValue<Object>> result = new ArrayList<>();
		if (input instanceof Map) {
			Map<?, ?> inputMap = (Map<?, ?>)input;
			for (Entry<?, ?> node : inputMap.entrySet()) {
				path.add(node.getKey().toString());
				if (node.getKey().equals("patterns")) {
					for (Object p: (List<?>)node.getValue()) {
						result.add(new TmValue<>(p, path));
					}
			    } else if (node.getKey().equals("repository")) {
			    	for (Entry<?, ?> p: ((Map<?, ?>)node.getValue()).entrySet()) {
						path.add((String)p.getKey());
			    		result.add(new TmValue<>(p.getValue(), path));
			    		path.remove(path.size() - 1);
					}
			    }
			    result.addAll(findAllPatterns(node.getValue(), path));
			    path.remove(path.size() - 1);
			}
		} else if (input instanceof List) {
			for (Object item : (List<?>)input) {
				result.addAll(findAllPatterns(item, path));
			}
		}
		return result;
	}
	
	private List<Pattern> findAllRegexes(Object input) {
		List<Pattern> result = new ArrayList<>();
		if (input instanceof Map) {
			Map<?, ?> inputMap = (Map<?, ?>)input;
			for (Entry<?, ?> node : inputMap.entrySet()) {
				if (regexKeys.contains(node.getKey())) {
					result.add(Pattern.compile(node.getValue().toString()));
				}
			    result.addAll(findAllRegexes(node.getValue()));
			}
		} else if (input instanceof List) {
			for (Object item : (List<?>)input) {
				result.addAll(findAllRegexes(item));
			}
		}
		return result;
	}

	private Map<Object, Object> loadYaml(String inputPath) throws FileNotFoundException {
		File f = new File(inputPath);
        Yaml yaml = new Yaml();
		return yaml.load(new FileReader(f));
	}
	
	private void writeJson(Map<Object, Object> input, String outputPath) throws IOException {
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		File result = new File(outputPath);
		result.getParentFile().mkdirs();
		result.createNewFile();
		try (Writer writer = new FileWriter(result, false)) {
		    gson.toJson(input, writer);
		}
	}

	private Map<String, String> readVariables(Map<Object, Object> yaml) throws ConfigurationException {		
		Object rawVariables = yaml.get("variables");
		return readStringMap(rawVariables, "variable");
	}
	private Map<String, String> readStringMap(Object raw, String errorVarName) {
		if (!(raw instanceof Map)) {
			return Collections.emptyMap();
		}
		Map<?, ?> variables = (Map<?, ?>)raw;
		LinkedHashMap<String, String> result = new LinkedHashMap<>(variables.size());
		for (Entry<?, ?> variable : variables.entrySet()) {
		    if (variable.getValue() instanceof String) {
		    	String rawValue = (String)variable.getValue();
				result.put((String)variable.getKey(), applyVariables(rawValue, result));
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
	private void applyVariablesRecursively(Object input, Map<String, String> variables) {
		if (input instanceof Map) {
			Map<?, ?> inputMap = (Map<?, ?>)input;
			for (Entry<?, ?> node : inputMap.entrySet()) {
				if (node.getValue() instanceof String) {
					((Entry<?, String>)node).setValue(applyVariables((String)node.getValue(), variables));
			    } else {
			    	applyVariablesRecursively(node.getValue(), variables);
			    }
			}
		} else if (input instanceof List) {
			for (Object item : (List<?>)input) {
				applyVariablesRecursively(item, variables);
			}
		}
	}
	
	private class TmValue<T> {
		public T value;
		private List<String> path;
		
		public TmValue(T value, List<String> path) {
			this.value = value;
			this.path = new ArrayList<>(path);
		}
		
		public TmValue<Map<?, ?>> asMap() {
			Map<?, ?> newValue = (Map<?, ?>) value;
			return new TmValue<>(newValue, path);
		}
		
		public String getPath() {
			return this.path.stream().collect(Collectors.joining(" -> "));
		}
	}
}
