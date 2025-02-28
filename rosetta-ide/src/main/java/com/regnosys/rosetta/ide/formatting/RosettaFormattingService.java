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

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.formatting.FormattingService;
import org.eclipse.xtext.preferences.ITypedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.TextRegion;

import com.regnosys.rosetta.formatting2.FormattingOptionsAdaptor;

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
	@Inject
	private Provider<IFormatter2> formatter2Provider;
	@Inject
	private FormattingOptionsAdaptor formattingOptionsAdapter;

	@Override
	public List<TextEdit> format(XtextResource resource, Document document, int offset, int length,
			FormattingOptions options) {
		List<TextEdit> result = new ArrayList<>();
		if (this.formatter2Provider != null) {
			ITypedPreferenceValues preferences = formattingOptionsAdapter.createPreferences(options);
			List<ITextReplacement> replacements = format2(resource, new TextRegion(offset, length), preferences);
			for (ITextReplacement r : replacements) {
				result.add(toTextEdit(document, r.getReplacementText(), r.getOffset(), r.getLength()));
			}
		}
		return result;
	}
}
