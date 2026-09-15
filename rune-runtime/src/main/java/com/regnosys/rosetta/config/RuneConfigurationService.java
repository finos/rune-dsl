package com.regnosys.rosetta.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

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

	/**
	 * The configuration at {@code path}, or empty where there is none to read: no file, or a file
	 * holding only blank lines and comments.
	 * <p>
	 * A tool that updates a project's {@code rune-config.yml} needs that distinction. {@code read}
	 * cannot make it, because a file with nothing in it fails the same way a truncated one does, and
	 * an empty file is how a project asks for a configuration to be created rather than a corruption
	 * to be reported. Anything else that will not parse still throws, and the message names the file.
	 */
	public Optional<RuneConfiguration> readIfPresent(Path path) throws IOException {
		if (path == null || !Files.isRegularFile(path)) {
			return Optional.empty();
		}
		if (!holdsContent(path)) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(mapper.readValue(path.toFile(), RuneConfiguration.class));
		} catch (IOException e) {
			String reason = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
			throw new IOException("Cannot read the Rune configuration at " + path + ": " + reason, e);
		}
	}

	private static boolean holdsContent(Path path) throws IOException {
		try (Stream<String> lines = Files.lines(path)) {
			return lines.map(String::trim).anyMatch(line -> !line.isEmpty() && !line.startsWith("#"));
		} catch (UncheckedIOException e) {
			// A file that is not text at all: Files.lines fails on the first malformed byte, and the
			// caller asked whether this is a configuration, which it is not.
			throw new IOException("Cannot read the Rune configuration at " + path + ": " + e.getCause().getMessage(), e);
		}
	}

	/**
	 * Writes {@code configuration} to {@code path}, creating the file or replacing what is there.
	 * <p>
	 * A failure names the file. The path is the caller's and is the only thing it can fix, and what
	 * the file system reports on its own -- "Is a directory", "Permission denied" -- identifies
	 * nothing.
	 */
	public void write(Path path, RuneConfiguration configuration) throws IOException {
		byte[] yaml = writeString(configuration).getBytes(StandardCharsets.UTF_8);
		try {
			Files.write(path, yaml);
		} catch (IOException e) {
			String reason = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
			throw new IOException("Cannot write the Rune configuration at " + path + ": " + reason, e);
		}
	}

	public void write(Writer writer, RuneConfiguration configuration) throws IOException {
		writer.write(writeString(configuration));
	}

	public String writeString(RuneConfiguration configuration) throws IOException {
		return mapper.writeValueAsString(configuration);
	}
}
