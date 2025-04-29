package com.regnosys.rosetta.formatting2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

import org.eclipse.emf.mwe.core.resources.ResourceLoader;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;

public class FormattingOptionsService {
	private static final String DEFAULT_FORMATTING_OPTIONS_PATH = "default-formatting-options.json";
	
	@Inject
	private FormattingOptionsAdaptor optionsAdaptor;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public FormattingOptions getDefaultOptions() {
		InputStream resourceStream = ResourceLoader.class.getClassLoader().getResourceAsStream(DEFAULT_FORMATTING_OPTIONS_PATH);
		try {
			return fromInputStream(resourceStream);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to read default formatting options.", e);
		}
	}
	public ITypedPreferenceValues getDefaultPreferences() {
		return optionsAdaptor.createPreferences(getDefaultOptions());
	}
	
	public FormattingOptions readOptionsFromFile(String optionsPath) throws IOException {
		InputStream resourceStream = new FileInputStream(optionsPath);
		return fromInputStream(resourceStream);
	}
	public ITypedPreferenceValues readPreferencesFromFile(String optionsPath) throws IOException {
		return optionsAdaptor.createPreferences(readOptionsFromFile(optionsPath));
	}
	
	private FormattingOptions fromInputStream(InputStream optionsStream) throws IOException {
		Map<?, ?> map = null;
		map = objectMapper.readValue(optionsStream, Map.class);

		// Create a FormattingOptions object
		FormattingOptions formattingOptions = new FormattingOptions();

		// Populate the FormattingOptions object
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			String key = (String) entry.getKey();
			Object value = entry.getValue();

			if (value instanceof String) {
				formattingOptions.putString(key, (String) value);
			} else if (value instanceof Number) {
				formattingOptions.putNumber(key, (Number) value);
			} else if (value instanceof Boolean) {
				formattingOptions.putBoolean(key, (Boolean) value);
			} else {
				throw new IllegalArgumentException("Unsupported value type for key: " + key);
			}
		}
		return formattingOptions;
	}
}
