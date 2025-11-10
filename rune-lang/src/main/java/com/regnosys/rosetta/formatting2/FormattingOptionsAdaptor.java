package com.regnosys.rosetta.formatting2;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;

import com.google.common.base.Strings;

public class FormattingOptionsAdaptor {
    public static String PREFERENCE_LINE_SEPARATOR_KEY = "lineSeparator";
	public static String PREFERENCE_INDENTATION_KEY = "indentation";
	public static String PREFERENCE_MAX_LINE_WIDTH_KEY = "maxLineWidth";
	public static String PREFERENCE_CONDITIONAL_MAX_LINE_WIDTH_KEY = "conditionalMaxLineWidth";

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
        
        String lineSeparator = options.getString(PREFERENCE_LINE_SEPARATOR_KEY);
        if (lineSeparator != null) {
            preferences.put(FormatterPreferenceKeys.lineSeparator, lineSeparator);
        }

		return preferences;
	}
}
