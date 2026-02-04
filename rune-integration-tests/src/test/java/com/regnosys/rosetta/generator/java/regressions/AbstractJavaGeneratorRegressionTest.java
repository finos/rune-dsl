package com.regnosys.rosetta.generator.java.regressions;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.xbase.testing.RegisteringFileSystemAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

import com.google.common.io.Resources;
import com.regnosys.rosetta.tests.compiler.CompilationException;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;

/**
 * Given a folder with .rosetta files, this test will parse those files, validate them,
 * generate Java code, compile the Java code and then check whether they match expectations.
 * Any expectation mismatches or compilation errors will result in a failure.
 * 
 * Expectations can be written by setting UPDATE_EXPECTATIONS to `true` and running the test.
 * 
 * How to add a new regression test:
 * 1. Create a folder under `src/test/resources`. Let's call it ROOT_FOLDER.
 * 2. Under ROOT_FOLDER, create a folder called `model`.
 * 3. Put any .rosetta files to test in this folder.
 * 4. Create a new Java test class that inherits from `AbstractJavaGeneratorRegressionTest`.
 * 5. Implement `getTestRootResourceFolder` to return ROOT_FOLDER.
 * For your first run, you might want to set UPDATE_EXPECTATIONS to `true` so it will create all generated files for you.
 */
public abstract class AbstractJavaGeneratorRegressionTest {
	private static final boolean UPDATE_EXPECTATIONS = true;

	private final static String MODEL_FOLDER = "model";
	private final static String EXPECTATIONS_FOLDER = "expected";

	@Inject
	private ModelHelper modelHelper;
	@Inject
	private ValidationTestHelper validationHelper;
	@Inject
	private CodeGeneratorTestHelper codeGeneratorTestHelper;

	private SortedMap<String, String> generatedCode;
	private SortedMap<String, String> expectedCode;

	private SortedMap<String, String> generatedClasses;
	private CompilationException compilationException = null;

	protected abstract String getTestRootResourceFolder();

	@BeforeAll
	void generateCodeAndCompile() throws IOException {
		List<Resource> models = readModelsFromResourceFolder();
		RegisteringFileSystemAccess fsa = codeGeneratorTestHelper.generateCodeWithFSA(models);
		generatedCode = new TreeMap<>();
		fsa.getGeneratedFiles()
				.forEach(f -> generatedCode.put(f.getPath().replace("/null/null/", ""), f.getContents().toString()));

		generatedClasses = fsa.getGeneratedFiles().stream().filter(f -> f.getJavaClassName() != null).collect(Collectors
				.toMap(f -> f.getJavaClassName(), f -> f.getContents().toString(), (v1, v2) -> v2, TreeMap::new));
		try {
			codeGeneratorTestHelper.compileToClasses(generatedClasses);
		} catch (CompilationException e) {
			compilationException = e;
		}
	}

	@BeforeAll
	void loadExpectedCode() throws IOException, URISyntaxException {
		Path resourcePath = toResourcePath(EXPECTATIONS_FOLDER);
		expectedCode = new TreeMap<>();
		walkFiles(resourcePath).forEach(p -> {
			String relativePath = resourcePath.relativize(p).toString().replace(File.separatorChar, '/');
			String fileContents;
			try {
				fileContents = Files.readString(p);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			expectedCode.put(relativePath, fileContents);
		});
	}

	@ParameterizedTest(name = "Generated {0} equals expectation")
	@MethodSource("provideGeneratedFiles")
	void testGeneratedCodeIsAsExpected(String fileName, String relativePath, String generatedCode) {
		String expected = expectedCode.get(relativePath);
		updateExpectationIfAssertionFails(() -> {
			Assertions.assertEquals(expected, generatedCode,
					expected == null
							? "No expected code found for generated file " + relativePath + "."
							: "The generated code for " + relativePath + " does not match the expected code.");
		}, () -> writeExpectationFile(relativePath, generatedCode));
	}

	private Stream<Arguments> provideGeneratedFiles() {
		return generatedCode.entrySet().stream().map(e -> {
			String relativePath = e.getKey();
			String generatedCode = e.getValue();
			String testName = relativePath.substring(relativePath.lastIndexOf('/') + 1);
			return Arguments.of(testName, relativePath, generatedCode);
		});
	}

	@ParameterizedTest(name = "Expectation {0} is generated")
	@MethodSource("provideExpectedFiles")
	void testExpectedFilesHaveCorrespondingGeneratedFile(String fileName, String relativePath,
			boolean anyExpectationsFound) {
		Assertions.assertTrue(anyExpectationsFound, "No expectations were found in resource folder "
				+ getTestRootResourceFolder() + "/" + EXPECTATIONS_FOLDER);
		updateExpectationIfAssertionFails(
				() -> Assertions.assertTrue(generatedCode.containsKey(relativePath),
						"The expected file " + relativePath + " is never generated."),
				() -> deleteExpectationFile(relativePath));
	}

	private Stream<Arguments> provideExpectedFiles() {
		if (expectedCode.isEmpty()) {
			return Stream.of(Arguments.of("<no expectations found>", null, false));
		}
		return expectedCode.keySet().stream().map(relativePath -> {
			String testName = relativePath.substring(relativePath.lastIndexOf('/') + 1);
			return Arguments.of(testName, relativePath, true);
		});
	}

	@ParameterizedTest(name = "Class {0} compiles")
	@MethodSource("provideDiagnosticsPerClass")
	void testClassesCompile(String className, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.stream()
				.filter(d -> d.getKind() == Kind.ERROR || d.getKind() == Kind.OTHER).toList();
		if (!errors.isEmpty()) {
			StringBuilder compilationFailureMsg = new StringBuilder();
			compilationFailureMsg.append("Class " + className + " failed to compile.\n");
			int number = 0;
			for (Diagnostic<? extends JavaFileObject> d : errors) {
				compilationFailureMsg.append("\n").append(++number).append(". ").append(d);
			}
			Assertions.fail(compilationFailureMsg.toString());
		}
	}

	private Stream<Arguments> provideDiagnosticsPerClass() {
		if (compilationException == null) {
			return generatedClasses.keySet().stream()
					.map(className -> Arguments.of(className, Collections.emptyList()));
		}
		Map<String, List<Diagnostic<? extends JavaFileObject>>> diagnosticsPerClass = compilationException
				.getDiagnosticsPerClass();
		return generatedClasses.keySet().stream().map(className -> Arguments.of(className,
				diagnosticsPerClass.getOrDefault(className, Collections.emptyList())));
	}

	@Test
	void doNotUpdateExpectations() {
		Assertions.assertFalse(UPDATE_EXPECTATIONS, "UPDATE_EXPECTATIONS should not be enabled.");
	}

	private void updateExpectationIfAssertionFails(Executable assertion, UpdateExpectation updateExpectation) {
		try {
			try {
				assertion.execute();
			} catch (AssertionFailedError e) {
				if (UPDATE_EXPECTATIONS) {
					String relativePath = updateExpectation.updateExpectation();
					throw wrapAssertionFailure(
							"Expectation has been updated at " + getTestRootResourceFolder() + "/" + relativePath + ".\n" + e.getMessage(), e);
				} else {
					throw wrapAssertionFailure(
							"Run with UPDATE_EXPECTATIONS=true to update expectation.\n" + e.getMessage(), e);
				}
			}
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private AssertionFailedError wrapAssertionFailure(String newMessage, AssertionFailedError error) {
		return new AssertionFailedError(newMessage, error.getExpected() == null ? null : error.getExpected().getEphemeralValue(),
				error.getActual() == null ? null : error.getActual().getEphemeralValue(), error);
	}

	private String writeExpectationFile(String relativePath, String content) throws IOException {
		Path expectationPath = getExpectationPath(relativePath);
		Files.createDirectories(expectationPath.getParent());
		Files.writeString(expectationPath, content, StandardCharsets.UTF_8);
		return relativePath;
	}

	private String deleteExpectationFile(String relativePath) throws IOException {
		Path expectationPath = getExpectationPath(relativePath);
		Files.delete(expectationPath);
		return relativePath;
	}

	private Path getExpectationPath(String relativePath) {
		return getTestResourcesPath().resolve(getTestRootResourceFolder()).resolve(EXPECTATIONS_FOLDER)
				.resolve(relativePath);
	}

	private Path getTestResourcesPath() {
		try {
			// Points to target/test-classes
			Path classPath = Path.of(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			// Go up to the module root
			Path moduleRoot = classPath.getParent().getParent();
			return moduleRoot.resolve("src/test/resources");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private List<Resource> readModelsFromResourceFolder() throws IOException {
		ResourceSet resourceSet = modelHelper.testResourceSet();

		List<Resource> resources = new ArrayList<>();
		walkFiles(toResourcePath(MODEL_FOLDER)).map(p -> URI.createURI(p.toUri().toString()))
				.filter(uri -> uri.fileExtension().equals("rosetta")).forEach(uri -> {
					Resource res = resourceSet.getResource(uri, true);
					resources.add(res);
				});
		resources.forEach(res -> {
			EcoreUtil2.resolveAll(res);
			validationHelper.assertNoIssues(res);
		});

		return resources;
	}

	private Stream<Path> walkFiles(Path resourcePath) throws IOException {
		if (Files.notExists(resourcePath)) {
			return Stream.empty();
		}
		return Files.walk(resourcePath, Integer.MAX_VALUE).filter(p -> !Files.isDirectory(p));
	}

	private Path toResourcePath(String folder) {
		URL rootResourceURL = Resources.getResource(getTestRootResourceFolder());
		try {
			return Path.of(rootResourceURL.toURI()).resolve(folder);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@FunctionalInterface
	private static interface UpdateExpectation {
		String updateExpectation() throws Throwable;
	}
}
