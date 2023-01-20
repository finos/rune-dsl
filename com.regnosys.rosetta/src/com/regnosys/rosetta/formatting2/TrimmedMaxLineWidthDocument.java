package com.regnosys.rosetta.formatting2;

import java.util.List;

import org.eclipse.xtext.formatting2.FormattingNotApplicableException;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatting;
import org.eclipse.xtext.formatting2.ITextReplacer;
import org.eclipse.xtext.formatting2.ITextReplacerContext;
import org.eclipse.xtext.formatting2.internal.HiddenRegionReplacer;
import org.eclipse.xtext.formatting2.internal.SubDocument;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.formatting2.regionaccess.ITextSegment;

@SuppressWarnings("restriction")
public class TrimmedMaxLineWidthDocument extends SubDocument {
	private final int maxLineWidth;

	public TrimmedMaxLineWidthDocument(ITextSegment region, IFormattableDocument parent, int maxLineWidth) {
		super(region, parent);
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
		String string = applyTextReplacements(replacements).trim();
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
}
