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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.xtext.formatting2.FormattingNotApplicableException;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatting;
import org.eclipse.xtext.formatting2.ITextReplacer;
import org.eclipse.xtext.formatting2.ITextReplacerContext;
import org.eclipse.xtext.formatting2.internal.HiddenRegionReplacer;
import org.eclipse.xtext.formatting2.internal.SubDocument;
import org.eclipse.xtext.formatting2.regionaccess.ISequentialRegion;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.formatting2.regionaccess.ITextSegment;

import com.google.common.collect.Lists;

/**
 * A small modification to Xtext's `MaxLineWidthDocument`. In comparison,
 * this subdocument allows you to format the next hidden region as well, but ignores
 * it when checking applicability, i.e., you may add newlines, long comments, etc.
 */
public class TrimmedMaxLineWidthDocument extends SubDocument {
	private final int maxLineWidth;
	
	private final ITextSegment astRegion;

	public TrimmedMaxLineWidthDocument(ISequentialRegion astRegion, IFormattableDocument parent, int maxLineWidth) {
		this(astRegion, astRegion.merge(astRegion.getNextHiddenRegion()), parent, maxLineWidth);
	}
	
	public TrimmedMaxLineWidthDocument(ITextSegment astRegion, ITextSegment formattableRegion, IFormattableDocument parent, int maxLineWidth) {
		super(formattableRegion, parent);
		this.astRegion = astRegion;
		this.maxLineWidth = maxLineWidth;
	}

	@Override
	public void addReplacer(ITextReplacer replacer) {
		validate(replacer);
		super.addReplacer(replacer);
	}

	@Override
	public ITextReplacerContext createReplacements(ITextReplacerContext context) {
		ITextReplacerContext last = super.createReplacements(context);
		List<ITextReplacement> replacements = last.getReplacementsUntil(context);
		String string = applyTextReplacementsOnAstRegion(replacements);
		if (string.contains("\n"))
			throw new FormattingNotApplicableException();
		int leadingCharCount = context.getLeadingCharsInLineCount();
		int formattedLength = string.length();
		int lineLength = leadingCharCount + formattedLength;
		if (lineLength > maxLineWidth)
			throw new FormattingNotApplicableException();
		return last;
	}

	protected void validate(HiddenRegionReplacer replacer) throws FormattingNotApplicableException {
		IHiddenRegionFormatting formatting = replacer.getFormatting();
		Integer newLineMin = formatting.getNewLineMin();
		if (newLineMin != null && newLineMin < 0)
			throw new FormattingNotApplicableException();
	}

	protected void validate(ITextReplacer replacer) throws FormattingNotApplicableException {
		if (replacer instanceof HiddenRegionReplacer)
			validate((HiddenRegionReplacer) replacer);
	}
	
	protected String applyTextReplacementsOnAstRegion(Iterable<ITextReplacement> replacements) {
		String input = astRegion.getText();
		ArrayList<ITextReplacement> list = Lists.newArrayList(replacements);
		Collections.sort(list);
		int startOffset = astRegion.getOffset();
		int lastOffset = 0;
		StringBuilder result = new StringBuilder();
		for (ITextReplacement r : list) {
			if (r.getEndOffset() <= startOffset) {
				continue;
			}
			if (r.getOffset() >= astRegion.getEndOffset()) {
				break;
			}
			int offset = r.getOffset() - startOffset;
			result.append(input.subSequence(lastOffset, offset));
			result.append(r.getReplacementText());
			lastOffset = offset + r.getLength();
		}
		result.append(input.subSequence(lastOffset, input.length()));
		return result.toString();
	}
}
