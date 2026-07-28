package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.config.file.RuneConfigurationFileProvider;
import com.regnosys.rosetta.utils.RuneConfigurationHolder;

/**
 * Verifies that namespaceConfig is the union of all configs on the classpath, with the current
 * project (the primary config returned by {@link RuneConfigurationFileProvider#get()}) shadowing
 * dependency configs on id collisions, while the model still comes from the current project only.
 */
public class MultiConfigUnionTest {

	@Test
	public void namespaceConfigIsUnionedWithCurrentProjectFirst() {
		Injector injector = Guice.createInjector(new RosettaRuntimeModule() {
			@SuppressWarnings("unused")
			public Class<? extends RuneConfigurationFileProvider> bindRuneConfigurationFileProvider() {
				return UnionConfigFileProvider.class;
			}
		});
		RuneConfiguration config = injector.getInstance(RuneConfigurationHolder.class).get();

		// Model comes from the current project (primary) config only.
		assertEquals("XYZ Model", config.getModel().getName());

		// myXmlSchema is defined in both (under the shared id my-confirmation); the current project's config path wins.
		assertEquals("xml-config/my-xml-schema-config.json",
				config.findSchemaConfig("myXmlSchema").orElseThrow().getConfigPath());
		// myJson is defined only in the current project.
		assertEquals("json-config/my-json-config.json",
				config.findSchemaConfig("myJson").orElseThrow().getConfigPath());
		// myOtherSchema is defined only in the dependency config and is included in the union.
		assertEquals("xml-config/my-other-schema-config.json",
				config.findSchemaConfig("myOtherSchema").orElseThrow().getConfigPath());

		// rosetta-model, my-confirmation, my-json (current project) + my-other (dependency); my-confirmation shadowed by id.
		assertEquals(4, config.getNamespaceConfig().size());
	}

	/**
	 * The Maven path passes the current project's config as an explicit file (because at
	 * {@code generate-sources} time it is not yet on the classpath), while dependency configs are
	 * available on the classpath inside dependency jars. {@link RuneConfigurationFileProvider#getResources()}
	 * must therefore union the classpath dependency configs even when the primary was located as an
	 * explicit file — otherwise the Maven build never sees a dependency's serializationConfig.
	 */
	@Test
	public void explicitFilePrimaryStillUnionsClasspathDependencyConfigs() throws Exception {
		String primaryPath = Paths.get(getClass().getResource("/rune-config-test.yml").toURI()).toString();
		RuneConfigurationFileProvider provider = RuneConfigurationFileProvider.createFromFile(primaryPath);

		URL primary = provider.get();
		Collection<URL> resources = provider.getResources();

		// The explicit primary is present and comes first (so it shadows on id collisions).
		assertEquals(primary, resources.iterator().next());
		// The canonical config on the classpath is discovered as a dependency, even though the
		// primary was located as an explicit file (this is what was missing before the fix).
		assertTrue(resources.stream().anyMatch(u -> u.getPath().endsWith("/" + RuneConfigurationFileProvider.FILE_NAME)),
				"classpath dependency configs should be unioned in the explicit-file (Maven) path");
	}

	/**
	 * Dependency configs are discovered through the injected classloader, not the thread context
	 * classloader. In a Maven build the thread context classloader is the plugin realm (which cannot
	 * see the project's compile dependencies), so the plugin sets a classloader over the project
	 * classpath via {@code setClassLoader}; {@code getResources()} must use it.
	 */
	@Test
	public void getResourcesUsesTheInjectedClassLoader(@TempDir Path depClasspathRoot) throws Exception {
		// A dependency-style config that is only reachable through a dedicated classloader, not the TCCL.
		Files.writeString(depClasspathRoot.resolve(RuneConfigurationFileProvider.FILE_NAME),
				"model:\n  name: Dep\nnamespaceConfig:\n- id: depOnly\n  namespace: dep.only\n  schemaConfig:\n    schema: depOnly\n    configPath: dep.json\n");
		// Closed before the test ends so the @TempDir can be deleted on Windows
		try (URLClassLoader depClassLoader = new URLClassLoader(new URL[] { depClasspathRoot.toUri().toURL() }, null)) {
			String primaryPath = Paths.get(getClass().getResource("/rune-config-test.yml").toURI()).toString();
			RuneConfigurationFileProvider provider = RuneConfigurationFileProvider.createFromFile(primaryPath);
			provider.setClassLoader(depClassLoader);

			Collection<URL> resources = provider.getResources();
			assertTrue(resources.contains(depClasspathRoot.resolve(RuneConfigurationFileProvider.FILE_NAME).toUri().toURL()),
					"getResources() should discover dependency configs via the injected classloader");
		}
	}

	private static class UnionConfigFileProvider extends RuneConfigurationFileProvider {
		private static final ClassLoader CL = Thread.currentThread().getContextClassLoader();

		@Override
		public URL get() {
			return CL.getResource("rune-config-test.yml");
		}

		@Override
		public Collection<URL> getResources() {
			return List.of(CL.getResource("rune-config-test.yml"), CL.getResource("rune-config-dependency.yml"));
		}
	}
}
