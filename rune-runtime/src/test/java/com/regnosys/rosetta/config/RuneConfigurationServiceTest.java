package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class RuneConfigurationServiceTest {

	private final RuneConfigurationService service = new RuneConfigurationService();

	private static final String CONFIG =
			"model:\n"
			+ "  name: DEMO\n"
			+ "generators:\n"
			+ "  namespaces:\n"
			+ "  - demo.*\n"
			+ "  - com.rosetta.model\n"
			+ "  tabulators:\n"
			+ "    types:\n"
			+ "    - demo.Foo\n"
			+ "namespaceConfig:\n"
			+ "- id: demoHello\n"
			+ "  namespace: demo.hello\n"
			+ "  readOnly: true\n"
			+ "  schemaConfig:\n"
			+ "    schema: demoHello\n"
			+ "    configPath: xml-config/demo.hello.json\n";

	@Test
	void roundtripIsLosslessForKnownPropertiesAndCleanForTheRest() throws IOException {
		RuneConfiguration config = service.readString(CONFIG);
		String out = service.writeString(config);

		// known properties survive the roundtrip
		assertTrue(out.contains("name: DEMO"), out);
		assertTrue(out.contains("demo.*"), out);
		assertTrue(out.contains("com.rosetta.model"), out);
		assertTrue(out.contains("namespaceConfig"), out);
		assertTrue(out.contains("demoHello"), out);
		assertTrue(out.contains("namespace: demo.hello"), out);
		assertTrue(out.contains("readOnly: true"), out);
		assertTrue(out.contains("schemaConfig"), out);
		assertTrue(out.contains("configPath: xml-config/demo.hello.json"), out);

		// deprecated / unknown keys are dropped, not preserved
		assertFalse(out.contains("tabulators"), out);

		// no derived properties leak into the file, and no null/{}/[] noise
		assertFalse(out.contains("namespaceFilter"), out);
		assertFalse(out.contains("readOnlyNamespaces"), out);
		assertFalse(out.contains("null"), out);
		assertFalse(out.contains("{}"), out);
		assertFalse(out.contains("[]"), out);
	}

	@Test
	void builderAddsNamespaceConfigWithoutRebuildingTheWholeConfig() throws IOException {
		RuneConfiguration config = service.readString(CONFIG);

		RuneConfiguration updated = config.toBuilder()
				.addNamespaceConfig(new RuneNamespaceConfiguration(
						"demoWorld", "demo.world", true,
						new RuneSchemaConfiguration("demoWorld", "xml-config/demo.world.json")))
				.build();

		String out = service.writeString(updated);
		// the pre-existing entry is preserved and the new one is added
		assertTrue(out.contains("demoHello"), out);
		assertTrue(out.contains("demoWorld"), out);
		// generators are untouched by the builder edit
		assertTrue(out.contains("demo.*"), out);
		assertEquals(2, updated.getNamespaceConfig().size());
	}

	@Test
	void builderUpsertsNamespaceConfigById() throws IOException {
		RuneConfiguration config = service.readString(CONFIG);

		RuneConfiguration updated = config.toBuilder()
				.addNamespaceConfig(new RuneNamespaceConfiguration("demoHello", "demo.hello.v2", false, null))
				.build();

		assertEquals(1, updated.getNamespaceConfig().size());
		assertEquals("demo.hello.v2", updated.getNamespaceConfig().get(0).getNamespace());
	}

	@Test
	void namespaceFilterIsDerivedFromNamespaces() throws IOException {
		RuneConfiguration config = service.readString(CONFIG);

		assertTrue(config.getGenerators().getNamespaceFilter().test("demo.foo"));
		assertTrue(config.getGenerators().getNamespaceFilter().test("com.rosetta.model"));
		assertFalse(config.getGenerators().getNamespaceFilter().test("other.namespace"));
		assertEquals(Arrays.asList("demo.*", "com.rosetta.model"), config.getGenerators().getNamespaces());
	}

	@Test
	void readOnlyNamespacesAreDerivedFromReadOnlyEntries() throws IOException {
		RuneConfiguration config = service.readString(CONFIG);
		assertEquals(Arrays.asList("demo.hello"), config.getReadOnlyNamespaces());
	}

	@Test
	void namespaceConfigIdIsOptional() throws IOException {
		// a read-only namespace entry needs no id
		String yaml = "model:\n  name: X\nnamespaceConfig:\n- namespace: com.rosetta.model.*\n  readOnly: true\n";
		RuneConfiguration config = service.readString(yaml);
		assertEquals(1, config.getNamespaceConfig().size());
		assertNull(config.getNamespaceConfig().get(0).getId());
		assertTrue(config.getNamespaceConfig().get(0).isReadOnly());

		// it roundtrips without introducing an id
		String out = service.writeString(config);
		assertTrue(out.contains("namespace: com.rosetta.model.*"), out);
		assertTrue(out.contains("readOnly: true"), out);
		assertFalse(out.contains("id:"), out);

		// the builder appends id-less entries (no dedup, no NPE)
		RuneConfiguration updated = config.toBuilder()
				.addNamespaceConfig(new RuneNamespaceConfiguration(null, "cdm.base.datetime", true, null))
				.build();
		assertEquals(2, updated.getNamespaceConfig().size());
	}
}
