package com.regnosys.rosetta.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * Reads and writes a {@link RuneConfiguration} to/from its YAML representation ({@code rune-config.yml}).
 * <p>
 * A read/write roundtrip is lossless for the properties the model knows about and produces clean YAML:
 * no {@code null}, no empty {@code {}} objects and no empty {@code []} lists. Unknown (deprecated)
 * properties are not preserved &mdash; they are dropped on read.
 */
public class RuneConfigurationService {

	private final ObjectMapper mapper;

	public RuneConfigurationService() {
		this.mapper = createObjectMapper();
	}

	// The YAML (de)serialization is an internal detail of this service: callers go through read/write,
	// so the ObjectMapper is deliberately not exposed.
	private static ObjectMapper createObjectMapper() {
		YAMLFactory yamlFactory = new YAMLFactory()
				.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
				.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
				.enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR);
		return new ObjectMapper(yamlFactory)
				// omit null, empty strings and empty collections so a roundtrip writes no null/{}/[] noise
				.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
				// deprecated / unknown keys are dropped rather than failing the read
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public RuneConfiguration read(URL url) throws IOException {
		return mapper.readValue(url, RuneConfiguration.class);
	}

	public RuneConfiguration read(File file) throws IOException {
		return mapper.readValue(file, RuneConfiguration.class);
	}

	public RuneConfiguration read(Path path) throws IOException {
		return mapper.readValue(path.toFile(), RuneConfiguration.class);
	}

	public RuneConfiguration read(InputStream input) throws IOException {
		return mapper.readValue(input, RuneConfiguration.class);
	}

	public RuneConfiguration readString(String yaml) throws IOException {
		return mapper.readValue(yaml, RuneConfiguration.class);
	}

	public void write(Path path, RuneConfiguration configuration) throws IOException {
		Files.write(path, writeString(configuration).getBytes(StandardCharsets.UTF_8));
	}

	public void write(Writer writer, RuneConfiguration configuration) throws IOException {
		writer.write(writeString(configuration));
	}

	public String writeString(RuneConfiguration configuration) throws IOException {
		return mapper.writeValueAsString(configuration);
	}
}
