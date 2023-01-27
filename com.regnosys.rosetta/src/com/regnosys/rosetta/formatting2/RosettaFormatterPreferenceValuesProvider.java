package com.regnosys.rosetta.formatting2;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.formatting2.FormatterPreferenceValuesProvider;
import org.eclipse.xtext.preferences.IPreferenceValues;
import org.eclipse.xtext.preferences.PreferenceKey;

public class RosettaFormatterPreferenceValuesProvider extends FormatterPreferenceValuesProvider {
	private static final int MAX_LINE_WIDTH = 80;
	
	@Override
	public IPreferenceValues getPreferenceValues(final Resource resource) {
		final IPreferenceValues preferenceValues = super.getPreferenceValues(resource);

		return new IPreferenceValues() {

			@Override
			public String getPreference(PreferenceKey key) {
				if (key == FormatterPreferenceKeys.maxLineWidth) {
					return FormatterPreferenceKeys.maxLineWidth.toString(MAX_LINE_WIDTH);
				}
				return preferenceValues.getPreference(key);
			}

		};
	}
}
