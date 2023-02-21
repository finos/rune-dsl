package com.regnosys.rosetta.ide.util;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ide.server.DocumentExtensions;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

/**
 * TODO: contribute to Xtext?
 *
 */
public class RangeUtils {
	private static final int INSIGNIFICANT_INDEX = -1;
	
	@Inject
	private DocumentExtensions documentExtensions;
	
	public Range getRange(EObject obj) {
		return documentExtensions.newFullLocation(obj).getRange();
	}
	
	public Range getRange(EObject obj, EStructuralFeature feature) {
		return getRange(obj, feature, INSIGNIFICANT_INDEX);
	}
	
	public Range getRange(EObject obj, EStructuralFeature feature, int featureIndex) {
		return documentExtensions.newLocation(obj, feature, featureIndex).getRange();
	}
	
	public Range getRange(EObject obj, Keyword keyword) {
		ICompositeNode node = NodeModelUtils.findActualNodeFor(obj);
		for (INode child: node.getChildren()) {
			EObject elem = child.getGrammarElement();
			if (elem.equals(keyword)) {
				return documentExtensions.newLocation(obj.eResource(), child.getTextRegionWithLineInformation()).getRange();
			}
		}
		return null;
	}
	
	public boolean overlap(Range a, Range b) {
		return strictlyComesBefore(a.getStart(), b.getEnd()) && strictlyComesBefore(b.getStart(), a.getEnd());
	}
	
	public boolean overlap(Range a, EObject b) {
		return overlap(a, getRange(b));
	}
	
	public boolean overlap(EObject a, EObject b) {
		return overlap(getRange(a), getRange(b));
	}
	
	public boolean strictlyComesBefore(Position a, Position b) {
		return a.getLine() < b.getLine() || a.getLine() == b.getLine() && a.getCharacter() < b.getCharacter();
	}
	
	public int comparePositions(Position a, Position b) {
		if (a.getLine() < b.getLine()) {
			return -1;
		}
		if (a.getLine() == b.getLine()) {
			if (a.getCharacter() < b.getCharacter()) {
				return -1;
			}
			if (a.getCharacter() == b.getCharacter()) {
				return 0;
			}
		}
		return 1;
	}
}
