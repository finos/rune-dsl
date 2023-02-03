package com.regnosys.rosetta.ide.formatting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.formatting.IIndentationInformation;
import org.eclipse.xtext.formatting2.IFormatter2;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.formatting.FormattingService;
import org.eclipse.xtext.preferences.MapBasedPreferenceValues;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.TextRegion;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.regnosys.rosetta.formatting2.RosettaFormatterPreferenceKeys;

/**
 * This class allows passing a `maxLineWidth` parameter
 * to client requests.
 */
public class RosettaFormattingService extends FormattingService {
	@Inject(optional = true)
	private Provider<IFormatter2> formatter2Provider;

	@Inject
	private IIndentationInformation indentationInformation;
		
	@Override
	public List<TextEdit> format(XtextResource resource, Document document, int offset, int length,
			FormattingOptions options) {
		String indent = indentationInformation.getIndentString();
		if (options != null) {
			if (options.isInsertSpaces()) {
				indent = Strings.padEnd("", options.getTabSize(), ' ');
			}
		}
		List<TextEdit> result = new ArrayList<>();
		if (this.formatter2Provider != null) {
			MapBasedPreferenceValues preferences = new MapBasedPreferenceValues();
			preferences.put("indentation", indent);
			
			Number conditionalMaxLineWidth = options.getNumber("conditionalMaxLineWidth");
			if (conditionalMaxLineWidth != null) {
				preferences.put(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth, conditionalMaxLineWidth.intValue());
			}
			Number maxLineWidth = options.getNumber("maxLineWidth");
			if (maxLineWidth != null) {
				preferences.put(RosettaFormatterPreferenceKeys.maxLineWidth, maxLineWidth.intValue());
				if (conditionalMaxLineWidth == null) {
					int defaultConditionalMaxLineWidth = RosettaFormatterPreferenceKeys.conditionalMaxLineWidth.toValue(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth.getDefaultValue());
					int defaultMaxLineWidth = RosettaFormatterPreferenceKeys.maxLineWidth.toValue(RosettaFormatterPreferenceKeys.maxLineWidth.getDefaultValue());
					double defaultRatio = (double)defaultConditionalMaxLineWidth / defaultMaxLineWidth;
					preferences.put(RosettaFormatterPreferenceKeys.conditionalMaxLineWidth, (int)(maxLineWidth.doubleValue() * defaultRatio));
				}
			}
			
			List<ITextReplacement> replacements = format2(resource, new TextRegion(offset, length), preferences);
			for (ITextReplacement r : replacements) {
				result.add(toTextEdit(document, r.getReplacementText(), r.getOffset(), r.getLength()));
			}
		}
		return result;
	}
}
