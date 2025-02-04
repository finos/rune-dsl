package com.regnosys.rosetta.formatting2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.mwe.core.resources.ResourceLoader;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

public class FormattingOptionsAdaptor {
	public static String PREFERENCE_INDENTATION_KEY = "indentation";
	public static String PREFERENCE_MAX_LINE_WIDTH_KEY = "maxLineWidth";
	public static String PREFERENCE_CONDITIONAL_MAX_LINE_WIDTH_KEY = "conditionalMaxLineWidth";
	public static String OPTIMIZE_IMPORTS ="optimizeImports";

	private static final String DEFAULT_FORMATTING_OPTIONS_PATH = "default-formatting-options.json";

	public ITypedPreferenceValues createPreferences(FormattingOptions options) {
		MapBasedPreferenceValues preferences = new MapBasedPreferenceValues();

		String indent = "\t";
		if (options != null) {
			if (options.isInsertSpaces()) {
				indent = Strings.padEnd("", options.getTabSize(), ' ');
			}
		}
		preferences.put(PREFERENCE_INDENTATION_KEY, indent);

		if (options == null) {
			return preferences;
		}

		Number conditionalMaxLineWidth = options.getNumber(PREFERENCE_CONDITIONAL_MAX_LINE_WIDTH_KEY);
		if (conditionalMaxLineWidth != null) {
			preferences.put(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth, conditionalMaxLineWidth.intValue());
		}
		Number maxLineWidth = options.getNumber(PREFERENCE_MAX_LINE_WIDTH_KEY);
		if (maxLineWidth != null) {
			preferences.put(RosettaFormatterPreferenceKeys.maxLineWidth, maxLineWidth.intValue());
			if (conditionalMaxLineWidth == null) {
				int defaultConditionalMaxLineWidth = RosettaFormatterPreferenceKeys.conditionalMaxLineWidth
						.toValue(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth.getDefaultValue());
				int defaultMaxLineWidth = RosettaFormatterPreferenceKeys.maxLineWidth
						.toValue(RosettaFormatterPreferenceKeys.maxLineWidth.getDefaultValue());
				double defaultRatio = (double) defaultConditionalMaxLineWidth / defaultMaxLineWidth;
				preferences.put(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth,
						(int) (maxLineWidth.doubleValue() * defaultRatio));
			}
		}

		Boolean optimizeImports = options.getBoolean(OPTIMIZE_IMPORTS);

		preferences.put(OPTIMIZE_IMPORTS, String.valueOf(optimizeImports));

		return preferences;
	}

	public FormattingOptions readFormattingOptions(String optionsPath) throws IOException {
		InputStream resourceStream;
		// If path not given, use default one
		if (optionsPath == null) {
			// Retrieve resource as an InputStream
			resourceStream = ResourceLoader.class.getClassLoader().getResourceAsStream(DEFAULT_FORMATTING_OPTIONS_PATH);
		} else {
			resourceStream = new FileInputStream(optionsPath);
		}

		// Create an ObjectMapper, read JSON into a Map
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = null;
		map = objectMapper.readValue(resourceStream, Map.class);

		// Create a FormattingOptions object
		FormattingOptions formattingOptions = new FormattingOptions();

		// Populate the FormattingOptions object
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
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
