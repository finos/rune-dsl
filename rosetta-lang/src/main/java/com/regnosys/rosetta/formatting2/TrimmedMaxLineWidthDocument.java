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
import org.eclipse.xtext.formatting2.regionaccess.IAstRegion;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;

import com.google.common.collect.Lists;

/**
 * A small modification to Xtext's `MaxLineWidthDocument`. In comparison,
 * this subdocument allows you to format the next hidden region as well, but ignores
 * it when checking applicability, i.e., you may add newlines, long comments, etc.
 */
public class TrimmedMaxLineWidthDocument extends SubDocument {
	private final int maxLineWidth;
	
	private final IAstRegion astRegion;

	public TrimmedMaxLineWidthDocument(IAstRegion region, IFormattableDocument parent, int maxLineWidth) {
		super(region.merge(region.getNextHiddenRegion()), parent);
		this.astRegion = region;
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
