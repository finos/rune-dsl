package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

class RuneNamespacePatternTest {

	/**
	 * The shared corpus, also read by {@code test_namespace_patterns.py}. Matching is implemented
	 * twice &mdash; here and in Python, for the CI checkers &mdash; and a comment saying the two must
	 * be kept in step is not a mechanism. Running both over the same cases is.
	 */
	private static final Path CASES = findRepositoryFile(".github/scripts/namespace-pattern-cases.yml");

	@TestFactory
	List<DynamicTest> matchesTheSharedCorpus() throws IOException {
		JsonNode corpus = new ObjectMapper(new YAMLFactory())
				.readTree(new String(Files.readAllBytes(CASES), StandardCharsets.UTF_8));
		List<DynamicTest> tests = new ArrayList<>();
		for (JsonNode testCase : corpus.get("cases")) {
			String pattern = testCase.get("pattern").asText();
			addCases(tests, pattern, testCase.get("matches"), true);
			addCases(tests, pattern, testCase.get("does-not-match"), false);
		}
		return tests;
	}

	private static void addCases(List<DynamicTest> tests, String pattern, JsonNode namespaces, boolean expected) {
		if (namespaces == null) {
			return;
		}
		for (JsonNode namespace : namespaces) {
			String name = "'" + pattern + "' " + (expected ? "matches" : "does not match") + " '" + namespace.asText() + "'";
			tests.add(DynamicTest.dynamicTest(name, () -> {
				RuneNamespacePattern parsed = RuneNamespacePattern.parse(pattern);
				if (expected) {
					assertTrue(parsed.matches(namespace.asText()), name);
				} else {
					assertFalse(parsed.matches(namespace.asText()), name);
				}
			}));
		}
	}

	@Test
	void anyOfMatchesAcrossPatterns() {
		Predicate<String> filter = RuneNamespacePattern.anyOf(Arrays.asList("foo.*", "abc.def"));

		assertTrue(filter.test("foo"));
		assertTrue(filter.test("foo.sub"));
		assertTrue(filter.test("abc.def"));
		assertFalse(filter.test("abc.def.sub"));
		assertFalse(filter.test("other"));
	}

	@Test
	void noPatternsMatchesNothing() {
		assertFalse(RuneNamespacePattern.anyOf(Collections.emptyList()).test("foo.bar"));
	}

	/** Resolves a path relative to the repository root, whichever module the test runs from. */
	private static Path findRepositoryFile(String relativePath) {
		for (Path directory = Paths.get("").toAbsolutePath(); directory != null; directory = directory.getParent()) {
			Path candidate = directory.resolve(relativePath);
			if (Files.isRegularFile(candidate)) {
				return candidate;
			}
		}
		throw new IllegalStateException("Could not find '" + relativePath + "' above " + Paths.get("").toAbsolutePath());
	}
}
