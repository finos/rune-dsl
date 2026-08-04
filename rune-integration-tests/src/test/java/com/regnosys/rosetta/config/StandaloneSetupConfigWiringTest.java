package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.xtext.testing.GlobalRegistries;
import org.eclipse.xtext.testing.GlobalRegistries.GlobalStateMemento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import com.regnosys.rosetta.utils.RuneConfigurationHolder;

/**
 * A setup's configured config file and classpath classloader must reach the configuration provider
 * even when a subclass replaces {@link RosettaStandaloneSetup#createInjector()} to mix in its own
 * runtime module — which is what the IDE/language-server setups here and every custom generator
 * setup downstream do. When it did not, a model built through such a setup silently fell back to the
 * default configuration and looked its dependencies' configs up through the thread context
 * classloader, which in a Maven build is the plugin realm and cannot see the model's dependencies.
 */
public class StandaloneSetupConfigWiringTest {

	private GlobalStateMemento stateBeforeSetup;

	@BeforeEach
	void rememberGlobalState() {
		stateBeforeSetup = GlobalRegistries.makeCopyOfGlobalState();
	}

	@AfterEach
	void restoreGlobalState() {
		stateBeforeSetup.restoreGlobalState();
	}

	/** Mirrors {@code CDMRosettaSetup}/{@code RosettaIdeSetup}: a custom module, built in createInjector. */
	private static final class CustomModuleSetup extends RosettaStandaloneSetup {
		@Override
		public Injector createInjector() {
			return Guice.createInjector(new RosettaRuntimeModule() {
			});
		}
	}

	@Test
	void configFileAndClasspathClassLoaderSurviveACustomCreateInjector(@TempDir Path tempDir) throws Exception {
		// The model's own config, passed as an explicit file - this is what the Maven plugin does, since
		// at generate-sources time the config is not on the classpath yet.
		Path primaryConfig = tempDir.resolve("rune-config.yml");
		Files.writeString(primaryConfig, "model:\n  name: Child Model\n");

		// A dependency's config, only reachable through the project classloader the plugin passes in.
		Path dependencyRoot = Files.createDirectory(tempDir.resolve("dependency-classes"));
		Files.writeString(dependencyRoot.resolve("rune-config.yml"),
				"model:\n  name: Parent Model\nnamespaceConfig:\n- id: parentSchema\n  namespace: parent.ns\n"
						+ "  schemaConfig:\n    schema: parentSchema\n    configPath: xml-config/parent-config.json\n");

		// Closed before the test ends so the @TempDir can be deleted on Windows
		try (URLClassLoader dependencyClassLoader =
				new URLClassLoader(new URL[] { dependencyRoot.toUri().toURL() }, null)) {
			Injector injector = new CustomModuleSetup()
					.setConfigFile(primaryConfig.toString())
					.setClasspathClassLoader(dependencyClassLoader)
					.createInjectorAndDoEMFRegistration();

			RuneConfiguration config = injector.getInstance(RuneConfigurationHolder.class).get();

			// The explicit config file was read, rather than falling back to the default configuration.
			assertEquals("Child Model", config.getModel().getName());
			// The dependency's schema config was discovered through the given classloader and unioned in,
			// so a schema marked [externalConfig] in a parent model still resolves its config path.
			assertEquals("xml-config/parent-config.json",
					config.findSchemaConfig("parentSchema").orElseThrow().getConfigPath());
		}
	}
}
