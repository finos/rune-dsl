package com.regnosys.rosetta.formatting2;

import java.util.List;

import org.eclipse.xtext.formatting2.AbstractFormatter2;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatting;
import org.eclipse.xtext.formatting2.IMerger;

public class PatchedHiddenRegionFormattingMerger implements IMerger<IHiddenRegionFormatting> {
	private final AbstractFormatter2 formatter;

	public PatchedHiddenRegionFormattingMerger(AbstractFormatter2 formatter) {
		super();
		this.formatter = formatter;
	}

	@Override
	public IHiddenRegionFormatting merge(List<? extends IHiddenRegionFormatting> conflicting) {
		IHiddenRegionFormatting result = formatter.createHiddenRegionFormatting();
		for (IHiddenRegionFormatting conflict : conflicting)
			result.mergeValuesFrom(conflict);
		return result;
	}
}
