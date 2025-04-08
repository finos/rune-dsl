package com.regnosys.rosetta.formatting2;

import javax.inject.Inject;

import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;

import com.google.inject.Provider;

public class RosettaFormatterRequestProvider implements Provider<FormatterRequest> {
	@Inject
	private FormattingOptionsService optionsService;
	
	@Override
	public FormatterRequest get() {
		FormatterRequest req = new FormatterRequest() {
			@Override
			public FormatterRequest setPreferences(ITypedPreferenceValues preferences) {
				if (preferences == null)
					preferences = optionsService.getDefaultPreferences();
				return super.setPreferences(preferences);
			}
		};
		req.setPreferences(null);
		return req;
	}
}
