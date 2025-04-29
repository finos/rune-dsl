package com.regnosys.rosetta.formatting2;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.preferences.TypedPreferenceValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * A provider for a formatter request that will respect the Rune's default formatting options
 * as specified in `default-formatting-options.json`.
 * 
 * By binding this provider, all formatting will by default use Rune's default formatting options,
 * including when serializing generated Rune code.
 */
public class FormatterRequestWithDefaultPreferencesProvider implements Provider<FormatterRequest>, javax.inject.Provider<FormatterRequest> {
	private final ITypedPreferenceValues defaultPreferences;
	
	@Inject
	public FormatterRequestWithDefaultPreferencesProvider(FormattingOptionsService optionsService) {
		this.defaultPreferences = optionsService.getDefaultPreferences();
	}
	
	@Override
	public FormatterRequest get() {
		return new FormatterRequestWithDefaultPreferences(defaultPreferences);
	}
	
	private static class FormatterRequestWithDefaultPreferences extends FormatterRequest {
		private static Logger LOGGER = LoggerFactory.getLogger(FormatterRequestWithDefaultPreferences.class);
		
		private final ITypedPreferenceValues defaultPreferences;
		
		public FormatterRequestWithDefaultPreferences(ITypedPreferenceValues defaultPreferences) {
			this.defaultPreferences = defaultPreferences;
			super.setPreferences(defaultPreferences);
		}
		
		@Override
		public FormatterRequest setPreferences(ITypedPreferenceValues preferences) {
			return super.setPreferences(overrideDefaultPreferences(preferences));
		}
		
		private ITypedPreferenceValues overrideDefaultPreferences(ITypedPreferenceValues preferences) {
			if (preferences == null) {
				return defaultPreferences;
			}
			Map<String, String> overrideValues = getValuesMapIfPossible(preferences);
			if (overrideValues == null) {
				LOGGER.error("Could not compute preference values to override from " + preferences, new Exception());
				return defaultPreferences;
			}
			return new MapBasedPreferenceValues(defaultPreferences, overrideValues);
		}
		private Map<String, String> getValuesMapIfPossible(ITypedPreferenceValues preferences) {
			while (!(preferences instanceof MapBasedPreferenceValues && ((MapBasedPreferenceValues) preferences).getDelegate() == null)) {
				if (preferences instanceof TypedPreferenceValues && ((TypedPreferenceValues) preferences).getDelegate() instanceof ITypedPreferenceValues) {
					preferences = (ITypedPreferenceValues) ((TypedPreferenceValues) preferences).getDelegate();
				} else if (preferences instanceof MapBasedPreferenceValues && ((MapBasedPreferenceValues) preferences).getDelegate() instanceof ITypedPreferenceValues) {
					MapBasedPreferenceValues preferencesWithDelegate = (MapBasedPreferenceValues) preferences;
					Map<String, String> delegateValues = getValuesMapIfPossible((ITypedPreferenceValues) preferencesWithDelegate.getDelegate());
					if (delegateValues == null) {
						return null;
					}
					Map<String, String> mergedValues = Stream.of(delegateValues.entrySet(), preferencesWithDelegate.getValues().entrySet())
							.flatMap(Set::stream)
					        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
					return mergedValues;
				} else {
					return null;
				}
			}
			MapBasedPreferenceValues preferencesWithoutDelegate = (MapBasedPreferenceValues) preferences;
			return preferencesWithoutDelegate.getValues();
		}
	}
}
