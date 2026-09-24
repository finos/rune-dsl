package com.regnosys.rosetta.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import com.rosetta.model.lib.transform.SerializationFormat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

	private static final String GENERATED_CONFIG =
			"model:\n"
			+ "  name: DEMO\n"
			+ "namespaceConfig:\n"
			+ "- namespace: demo.unavista.csv\n"
			+ "  origin:\n"
			+ "    modelImport: csv\n"
			+ "  schemaConfig:\n"
			+ "    schema: unavistaTransaction\n"
			+ "    configPath: csv-config/unavistaTransaction-csv-config.json\n";

	@Test
	void originMarkerIsReadAndRoundtrips() throws IOException {
		RuneConfiguration config = service.readString(GENERATED_CONFIG);

		RuneNamespaceConfiguration entry = config.getNamespaceConfig().get(0);
		assertEquals("csv", entry.getOrigin().getModelImport());
		// a sample-derived namespace is editable, so `readOnly` stays absent
		assertFalse(entry.isReadOnly());

		String out = service.writeString(config);
		assertTrue(out.contains("origin:"), out);
		assertTrue(out.contains("modelImport: csv"), out);
		assertFalse(out.contains("readOnly"), out);
	}

	@Test
	void aNamespaceCanBeReadOnlyWithoutCarryingAnOrigin() throws IOException {
		RuneConfiguration config = service.readString(CONFIG);

		RuneNamespaceConfiguration entry = config.getNamespaceConfig().get(0);
		assertNull(entry.getOrigin());
		assertTrue(entry.isReadOnly());
	}

	@Test
	void anUnrecognizedOriginKeyIsDroppedLikeAnyOtherUnknownProperty() throws IOException {
		// A configuration is only ever read by the version that wrote it or a newer one, so a key this
		// version does not recognize can only be a mistake -- and is dropped, as `tabulators` is above.
		String yaml = "model:\n  name: X\n"
				+ "namespaceConfig:\n"
				+ "- namespace: demo.future\n"
				+ "  origin:\n"
				+ "    someUnknownTool: v1\n";
		RuneConfiguration config = service.readString(yaml);

		assertNull(config.getNamespaceConfig().get(0).getOrigin().getModelImport());
		assertFalse(service.writeString(config).contains("someUnknownTool"));
	}

	@Test
	void anOriginNamingNoToolIsNotWrittenBack() throws IOException {
		String yaml = "model:\n  name: X\nnamespaceConfig:\n- namespace: demo.plain\n  origin: {}\n";
		RuneConfiguration config = service.readString(yaml);

		assertNull(config.getNamespaceConfig().get(0).getOrigin().getModelImport());
		assertFalse(service.writeString(config).contains("origin"), service.writeString(config));
	}

	@Test
	void defaultSerialisationFormatIsReadWhenPresent() throws IOException {
		String yaml = "model:\n  name: DEMO\n  defaultSerialisationFormat: RUNE_JSON\n";
		RuneConfiguration config = service.readString(yaml);
		assertEquals(SerializationFormat.RUNE_JSON, config.getModel().getDefaultSerialisationFormat());
	}

	@Test
	void defaultSerialisationFormatIsNullWhenAbsent() throws IOException {
		RuneConfiguration config = service.readString(CONFIG);
		assertNull(config.getModel().getDefaultSerialisationFormat());
	}

	@Test
	void defaultSerialisationFormatRoundtrips() throws IOException {
		String yaml = "model:\n  name: DEMO\n  defaultSerialisationFormat: RUNE_JSON\n";
		RuneConfiguration config = service.readString(yaml);

		String out = service.writeString(config);
		assertTrue(out.contains("defaultSerialisationFormat: RUNE_JSON"), out);

		RuneConfiguration reread = service.readString(out);
		assertEquals(SerializationFormat.RUNE_JSON, reread.getModel().getDefaultSerialisationFormat());
	}

	@Test
	void absentDefaultSerialisationFormatIsNotWritten() throws IOException {
		RuneConfiguration config = service.readString(CONFIG);
		String out = service.writeString(config);
		assertFalse(out.contains("defaultSerialisationFormat"), out);
	}

	// --- readIfPresent -----------------------------------------------------------------------------

	@Test
	void noPathAtAllIsACallerMistakeRatherThanAnAbsentFile() {
		// Reading null as "there is no configuration" would hide a caller that never resolved one.
		assertThrows(NullPointerException.class, () -> service.readIfPresent(null));
	}

	@Test
	void aByteOrderMarkIsNotContent(@TempDir Path dir) throws IOException {
		// Several Windows editors write one. Counted as content, a comment-only file would then be
		// read, and fail with "No content to map due to end-of-input".
		assertFalse(service.readIfPresent(write(dir, "bom-blank.yml", "\uFEFF\n")).isPresent());
		assertFalse(service.readIfPresent(write(dir, "bom-comments.yml", "\uFEFF# nothing yet\n")).isPresent());
	}

	@Test
	void aFileWithNothingToReadIsAbsent(@TempDir Path dir) throws IOException {
		// How a project asks for a configuration to be created: the file is there to be written into,
		// and read() cannot tell it apart from a truncated one.
		assertFalse(service.readIfPresent(dir.resolve("missing.yml")).isPresent());
		assertFalse(service.readIfPresent(write(dir, "empty.yml", "")).isPresent());
		assertFalse(service.readIfPresent(write(dir, "blank.yml", "\n  \n\n")).isPresent());
		assertFalse(service.readIfPresent(write(dir, "comments.yml", "# nothing yet\n#   still nothing\n")).isPresent());
	}

	@Test
	void aFileWithAConfigurationIsRead(@TempDir Path dir) throws IOException {
		assertEquals("DEMO", service.readIfPresent(write(dir, "plain.yml", CONFIG)).get().getModel().getName());
		// A mark in front of real content is Jackson's to handle, and it does.
		assertEquals("DEMO", service.readIfPresent(write(dir, "bom.yml", "\uFEFF" + CONFIG)).get().getModel().getName());
	}

	@Test
	void aFileThatWillNotParseFailsNamingIt(@TempDir Path dir) throws IOException {
		Path file = write(dir, "rune-config.yml", "model:\n\tname: tabs are not YAML\n");

		IOException failure = assertThrows(IOException.class, () -> service.readIfPresent(file));

		assertTrue(failure.getMessage().contains(file.toString()), failure.getMessage());
	}

	@Test
	void aFileThatIsNotTextFailsNamingIt(@TempDir Path dir) throws IOException {
		Path file = dir.resolve("rune-config.yml");
		Files.write(file, new byte[] {(byte) 0xC3, (byte) 0x28});

		IOException failure = assertThrows(IOException.class, () -> service.readIfPresent(file));

		assertTrue(failure.getMessage().contains(file.toString()), failure.getMessage());
	}

	@Test
	void aConfigurationThatCannotBeWrittenFailsNamingTheFile(@TempDir Path dir) throws IOException {
		Path file = dir.resolve("rune-config.yml");
		Files.createDirectory(file);

		IOException failure = assertThrows(IOException.class, () -> service.write(file, service.readString(CONFIG)));

		assertTrue(failure.getMessage().contains(file.toString()), failure.getMessage());
	}

	@Test
	void aConfigurationWithNoModelSectionSaysSoRatherThanNull(@TempDir Path dir) throws IOException {
		// Jackson wraps the required-field NullPointerException, which carries no message of its own,
		// so reading the cause alone would end the message in ": null".
		Path file = write(dir, "rune-config.yml", "namespaceConfig: []\n");

		IOException failure = assertThrows(IOException.class, () -> service.readIfPresent(file));

		assertTrue(failure.getMessage().contains(file.toString()), failure.getMessage());
		assertFalse(failure.getMessage().endsWith("null"), failure.getMessage());
		assertTrue(failure.getMessage().contains("`model` section"), failure.getMessage());
	}

	@Test
	void everyGeneratorSettingSurvivesARoundTrip() throws IOException {
		// doNotPrune is the one that did not: a project that let a tool rewrite its configuration lost
		// every entry it had pinned there.
		String yaml = "model:\n  name: DEMO\n"
				+ "generators:\n"
				+ "  namespaces:\n"
				+ "    - demo.*\n"
				+ "  doNotPrune:\n"
				+ "    - type: demo.Party\n"
				+ "      attribute: details\n";

		RuneConfiguration read = service.readString(yaml);
		RuneConfiguration reread = service.readString(service.writeString(read));

		assertEquals(Collections.singletonList("demo.*"), reread.getGenerators().getNamespaces());
		assertEquals(1, reread.getGenerators().doNotPrune().size());
		assertEquals("demo.Party", reread.getGenerators().doNotPrune().get(0).getType());
		assertEquals("details", reread.getGenerators().doNotPrune().get(0).getAttribute());
	}

	private static Path write(Path dir, String name, String content) throws IOException {
		Path file = dir.resolve(name);
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
		return file;
	}

}
