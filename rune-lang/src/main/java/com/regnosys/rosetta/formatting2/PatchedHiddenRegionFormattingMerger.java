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

import java.util.List;

import org.eclipse.xtext.formatting2.AbstractFormatter2;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatting;
import org.eclipse.xtext.formatting2.IMerger;

/**
 * Patch for issue https://github.com/eclipse/xtext-core/issues/2061
 */
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
