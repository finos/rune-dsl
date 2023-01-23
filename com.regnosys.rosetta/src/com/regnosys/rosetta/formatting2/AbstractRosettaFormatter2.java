package com.regnosys.rosetta.formatting2;

import org.eclipse.xtext.formatting2.AbstractFormatter2;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatting;
import org.eclipse.xtext.formatting2.IMerger;

public abstract class AbstractRosettaFormatter2 extends AbstractFormatter2 {
	@Override
	public IMerger<IHiddenRegionFormatting> createHiddenRegionFormattingMerger() {
		return new PatchedHiddenRegionFormattingMerger(this);
	}
}
