package com.regnosys.rosetta.ide.util;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.ide.server.DocumentExtensions;

public class RangeUtils {
	@Inject
	private DocumentExtensions documentExtensions;
	
	public Range getRange(EObject obj) {
		return documentExtensions.newLocation(obj).getRange();
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
}
