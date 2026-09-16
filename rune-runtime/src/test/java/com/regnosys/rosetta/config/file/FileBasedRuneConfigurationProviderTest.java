package com.regnosys.rosetta.config.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.regnosys.rosetta.config.DefaultRuneConfigurationProvider;
import com.regnosys.rosetta.config.RuneConfiguration;

class FileBasedRuneConfigurationProviderTest {

	private static final String MODEL = "model:\n  name: DEMO\n";
	private static final String NOT_YAML = "model:\n\tname: tabs are not YAML\n";

	@Test
	void theProjectConfigurationIsRead(@TempDir Path dir) throws IOException {
		RuneConfiguration config = providerOver(write(dir, MODEL)).get();

		assertEquals("DEMO", config.getModel().getName());
	}

	@Test
	void aProjectConfigurationThatWillNotParseFailsNamingTheFile(@TempDir Path dir) throws IOException {
		Path file = write(dir, NOT_YAML);
		FileBasedRuneConfigurationProvider provider = providerOver(file);

		FileBasedRuneConfigurationRuntimeException failure =
				assertThrows(FileBasedRuneConfigurationRuntimeException.class, provider::get);

		assertTrue(failure.getMessage().contains(file.toUri().toURL().toString()), failure.getMessage());
	}

	@Test
	void aDependencyConfigurationThatWillNotParseFailsNamingThatFileRatherThanTheProjectOne(@TempDir Path dir)
			throws IOException {
		// The project's own configuration parses; only the one on the classpath does not. Reading the
		// two together would report a parse error over both and leave the reader to work out which.
		Path project = write(dir.resolve("project"), MODEL);
		Path dependency = write(dir.resolve("dependency"), NOT_YAML);
		FileBasedRuneConfigurationProvider provider = providerOver(project, dependency.getParent());

		FileBasedRuneConfigurationRuntimeException failure =
				assertThrows(FileBasedRuneConfigurationRuntimeException.class, provider::get);

		assertTrue(failure.getMessage().contains(dependency.toUri().toURL().toString()), failure.getMessage());
	}

	private static FileBasedRuneConfigurationProvider providerOver(Path configFile, Path... classpath)
			throws IOException {
		URL[] roots = new URL[classpath.length];
		for (int i = 0; i < classpath.length; i++) {
			roots[i] = classpath[i].toUri().toURL();
		}
		RuneConfigurationFileProvider files =
				RuneConfigurationFileProvider.createFromFile(configFile.toString());
		// No parent, so the test's own classpath contributes no rune-config.yml of its own.
		files.setClassLoader(new URLClassLoader(roots, null));
		return new FileBasedRuneConfigurationProvider(new DefaultRuneConfigurationProvider(), files);
	}

	private static Path write(Path dir, String content) throws IOException {
		Files.createDirectories(dir);
		Path file = dir.resolve(RuneConfigurationFileProvider.FILE_NAME);
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
		return file;
	}
}
