/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.ide.formatting;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.formatting.IIndentationInformation;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.formatting.FormattingService;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.TextRegion;

import com.google.common.base.Strings;
import com.regnosys.rosetta.formatting2.RosettaFormatterPreferenceKeys;

/**
 * This class allows passing additional formatting parameters as defined in
 * `RosettaFormatterPreferenceKeys` to client requests.
 * 
 * TODO: contribute some of this stuff to Xtext:
 * - a dedicated `createPreferences` method, instead of having to overwrite `format`.
 * - get rid of magic string "indentation".
 * - expose injected fields to child classes (make them protected)
 */
public class RosettaFormattingService extends FormattingService {
	public static String PREFERENCE_INDENTATION_KEY = "indentation";
	public static String PREFERENCE_MAX_LINE_WIDTH_KEY = "maxLineWidth";
	public static String PREFERENCE_CONDITIONAL_MAX_LINE_WIDTH_KEY = "conditionalMaxLineWidth";
	
	@Inject
	private Provider<IFormatter2> formatter2Provider;

	@Inject
	private IIndentationInformation indentationInformation;
	
	protected ITypedPreferenceValues createPreferences(FormattingOptions options) {
		MapBasedPreferenceValues preferences = new MapBasedPreferenceValues();
		
		String indent = indentationInformation.getIndentString();
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
				int defaultConditionalMaxLineWidth = RosettaFormatterPreferenceKeys.conditionalMaxLineWidth.toValue(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth.getDefaultValue());
				int defaultMaxLineWidth = RosettaFormatterPreferenceKeys.maxLineWidth.toValue(RosettaFormatterPreferenceKeys.maxLineWidth.getDefaultValue());
				double defaultRatio = (double)defaultConditionalMaxLineWidth / defaultMaxLineWidth;
				preferences.put(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth, (int)(maxLineWidth.doubleValue() * defaultRatio));
			}
		}
		
		return preferences;
	}
		
	@Override
	public List<TextEdit> format(XtextResource resource, Document document, int offset, int length,
			FormattingOptions options) {
		List<TextEdit> result = new ArrayList<>();
		if (this.formatter2Provider != null) {
			ITypedPreferenceValues preferences = createPreferences(options);
			List<ITextReplacement> replacements = format2(resource, new TextRegion(offset, length), preferences);
			for (ITextReplacement r : replacements) {
				result.add(toTextEdit(document, r.getReplacementText(), r.getOffset(), r.getLength()));
			}
		}
		return result;
	}
}
