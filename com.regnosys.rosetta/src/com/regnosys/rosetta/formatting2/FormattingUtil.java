package com.regnosys.rosetta.formatting2;

import java.util.function.Consumer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.formatting2.FormatterPreferenceKeys;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.eclipse.xtext.formatting2.IFormattableSubDocument;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatter;
import org.eclipse.xtext.formatting2.regionaccess.IAstRegion;
import org.eclipse.xtext.formatting2.regionaccess.IEObjectRegion;
import org.eclipse.xtext.formatting2.regionaccess.IHiddenRegion;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionExtensions;
import org.eclipse.xtext.formatting2.regionaccess.ITextSegment;

public class FormattingUtil {	
	public void formatInlineOrMultiline(IFormattableDocument document, EObject object, Consumer<IFormattableDocument> inlineFormatter, Consumer<IFormattableDocument> multilineFormatter) {
		formatInlineOrMultiline(document, object, FormattingMode.NORMAL, inlineFormatter, multilineFormatter);
	}
	public void formatInlineOrMultiline(IFormattableDocument document, EObject object, FormattingMode mode, Consumer<IFormattableDocument> inlineFormatter, Consumer<IFormattableDocument> multilineFormatter) {
		if (mode.equals(FormattingMode.NORMAL)) {
			IEObjectRegion objRegion = getTextRegionExt(document).regionForEObject(object);
			 // I need to include the next hidden region in the conditional formatting as well,
			 // because that's where I might decrease indentation in case of a (long) multi-line operation.
			ITextSegment region = objRegion.merge(objRegion.getNextHiddenRegion());
			document.formatConditionally(region.getOffset(), region.getLength(),
					(doc) -> { // case: short region
						IFormattableSubDocument singleLineDoc =
								requireTrimmedFitsInLine(
										doc,
										objRegion,
										doc.getRequest()
											.getPreferences()
											.getPreference(FormatterPreferenceKeys.maxLineWidth)
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
	
	public IFormattableSubDocument requireTrimmedFitsInLine(IFormattableDocument document, IAstRegion segment, int maxLineWidth) {
		TrimmedMaxLineWidthDocument subdoc = new TrimmedMaxLineWidthDocument(segment, document, maxLineWidth);
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
	
	private ITextRegionExtensions getTextRegionExt(IFormattableDocument doc) {
		return doc.getRequest().getTextRegionAccess().getExtensions();
	}
}
