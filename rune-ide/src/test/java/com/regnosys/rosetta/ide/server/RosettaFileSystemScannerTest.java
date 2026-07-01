package com.regnosys.rosetta.ide.server;

import com.google.inject.Injector;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.util.IFileSystemScanner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Regression test: the language server's workspace scan must skip build-output directories
 * (target, build, node_modules, .git), otherwise it indexes copies of source files that were
 * copied there by the build (e.g. test resources copied to target/test-classes) as if they were
 * separate, genuine source files, which the language server then reports as duplicate types and
 * functions.
 */
class RosettaFileSystemScannerTest {
	private static Injector injector;

	@BeforeAll
	static void setUp() {
		injector = new RosettaServerSetup().createInjector();
	}

	@Test
	void doesNotDescendIntoTargetDirectory(@TempDir Path root) throws IOException {
		Files.createDirectories(root.resolve("src/main/rosetta"));
		Files.writeString(root.resolve("src/main/rosetta/Foo.rosetta"), "namespace foo");
		Files.createDirectories(root.resolve("target/test-classes/rosetta"));
		Files.writeString(root.resolve("target/test-classes/rosetta/Foo.rosetta"), "namespace foo");

		List<URI> found = scan(root);

		Assertions.assertTrue(found.stream().anyMatch(u -> normalize(u).endsWith("src/main/rosetta/Foo.rosetta")),
				"Expected to find the source file: " + found);
		Assertions.assertTrue(found.stream().noneMatch(u -> normalize(u).contains("/target/")),
				"Expected no URIs under target/: " + found);
	}

	@Test
	void stillScansDirectoriesNotNamedLikeBuildOutput(@TempDir Path root) throws IOException {
		Files.createDirectories(root.resolve("src/main/rosetta/targetting"));
		Files.writeString(root.resolve("src/main/rosetta/targetting/Foo.rosetta"), "namespace foo");

		List<URI> found = scan(root);

		Assertions.assertTrue(
				found.stream().anyMatch(u -> normalize(u).endsWith("src/main/rosetta/targetting/Foo.rosetta")),
				"Expected to find the source file: " + found);
	}

	private static String normalize(URI uri) {
		return uri.toFileString().replace('\\', '/');
	}

	private List<URI> scan(Path root) {
		IFileSystemScanner scanner = injector.getInstance(IFileSystemScanner.class);
		List<URI> found = new ArrayList<>();
		scanner.scan(URI.createFileURI(root.toString() + "/"), found::add);
		return found;
	}
}
