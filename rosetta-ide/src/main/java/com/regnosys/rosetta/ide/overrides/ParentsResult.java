package com.regnosys.rosetta.ide.overrides;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;

public class ParentsResult {
	private final Range range;
	private final List<Parent> parents;
	
	public ParentsResult(Range range, List<Parent> parents) {
		this.range = range;
		this.parents = parents;
	}

	public Range getRange() {
		return range;
	}

	public List<Parent> getParents() {
		return parents;
	}
}
