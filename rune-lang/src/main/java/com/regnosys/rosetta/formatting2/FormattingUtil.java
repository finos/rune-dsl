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

package com.regnosys.rosetta.formatting2;

import java.util.function.Consumer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.eclipse.xtext.formatting2.IFormattableSubDocument;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatter;
import org.eclipse.xtext.formatting2.regionaccess.IEObjectRegion;
import org.eclipse.xtext.formatting2.regionaccess.IHiddenRegion;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionExtensions;
import org.eclipse.xtext.formatting2.regionaccess.ITextSegment;
import org.eclipse.xtext.preferences.TypedPreferenceKey;

public class FormattingUtil {
	public ITextRegionExtensions getTextRegionExt(IFormattableDocument doc) {
		return doc.getRequest().getTextRegionAccess().getExtensions();
	}
	
	public void formatInlineOrMultiline(IFormattableDocument document, EObject object, Consumer<IFormattableDocument> inlineFormatter, Consumer<IFormattableDocument> multilineFormatter) {
		formatInlineOrMultiline(document, object, FormattingMode.NORMAL, inlineFormatter, multilineFormatter);
	}
	public void formatInlineOrMultiline(IFormattableDocument document, EObject object, FormattingMode mode, Consumer<IFormattableDocument> inlineFormatter, Consumer<IFormattableDocument> multilineFormatter) {
		int maxLineWidth = getPreference(document, RosettaFormatterPreferenceKeys.maxLineWidth);
		formatInlineOrMultiline(document, object, mode, maxLineWidth, inlineFormatter, multilineFormatter);
	}
	public void formatInlineOrMultiline(IFormattableDocument document, EObject object, FormattingMode mode, int maxLineWidth, Consumer<IFormattableDocument> inlineFormatter, Consumer<IFormattableDocument> multilineFormatter) {
		IEObjectRegion objRegion = getTextRegionExt(document).regionForEObject(object);
		// I need to include the next hidden region in the conditional formatting as well,
		// because that's where I might decrease indentation in case of a (long) multi-line operation.
		ITextSegment formattableRegion = objRegion.merge(objRegion.getNextHiddenRegion());
		formatInlineOrMultiline(document, objRegion, formattableRegion, mode, maxLineWidth, inlineFormatter, multilineFormatter);
	}
	public void formatInlineOrMultiline(IFormattableDocument document, ITextSegment astRegion, ITextSegment formattableRegion, FormattingMode mode, Consumer<IFormattableDocument> inlineFormatter, Consumer<IFormattableDocument> multilineFormatter) {
		int maxLineWidth = getPreference(document, RosettaFormatterPreferenceKeys.maxLineWidth);
		formatInlineOrMultiline(document, astRegion, formattableRegion, mode, maxLineWidth, inlineFormatter, multilineFormatter);
	}
	public void formatInlineOrMultiline(IFormattableDocument document, ITextSegment astRegion, ITextSegment formattableRegion, FormattingMode mode, int maxLineWidth, Consumer<IFormattableDocument> inlineFormatter, Consumer<IFormattableDocument> multilineFormatter) {
		if (mode.equals(FormattingMode.NORMAL)) {
			document.formatConditionally(formattableRegion.getOffset(), formattableRegion.getLength(),
					(doc) -> { // case: short region
						IFormattableSubDocument singleLineDoc =
								requireTrimmedFitsInLine(
										doc,
										astRegion,
										formattableRegion,
										maxLineWidth
								);
						inlineFormatter.accept(singleLineDoc);
					},
					(doc) -> { // case: long region
						multilineFormatter.accept(doc);
					}
			);
		} else if (mode.equals(FormattingMode.SINGLE_LINE)) {
			inlineFormatter.accept(document);
		} else if (mode.equals(FormattingMode.CHAIN)) {
			multilineFormatter.accept(document);
		}
	}
	
	public IFormattableSubDocument requireTrimmedFitsInLine(IFormattableDocument document, ITextSegment astRegion, ITextSegment formattableRegion, int maxLineWidth) {
		TrimmedMaxLineWidthDocument subdoc = new TrimmedMaxLineWidthDocument(astRegion, formattableRegion, document, maxLineWidth);
		document.addReplacer(subdoc);
		return subdoc;
	}
	
	public void indentInner(EObject obj, IFormattableDocument document) {
		IHiddenRegion firstHiddenRegion = getTextRegionExt(document).previousHiddenRegion(obj).getNextHiddenRegion();
		indentInner(obj, firstHiddenRegion, document);
	}
	public void indentInner(EObject obj, IHiddenRegion indentationStart, IFormattableDocument document) {
		IHiddenRegion nextHiddenRegion = getTextRegionExt(document).nextHiddenRegion(obj);
		
		document.set(indentationStart, nextHiddenRegion, IHiddenRegionFormatter::indent);
	}
	
	public void formatAllUntil(IFormattableDocument document, IHiddenRegion start, IHiddenRegion end, Consumer<IHiddenRegionFormatter> formatter) {
		while (!start.equals(end)) {
			document.set(start, (f) -> formatter.accept(f));
			start = start.getNextHiddenRegion();
		}
	}
	public void singleSpacesUntil(IFormattableDocument document, IHiddenRegion start, IHiddenRegion end) {
		formatAllUntil(document, start, end, (f) -> f.oneSpace());
	}
	
	public <T> T getPreference(IFormattableDocument document, TypedPreferenceKey<T> key) {
		return document
				.getRequest()
				.getPreferences()
				.getPreference(key);
	}

}
