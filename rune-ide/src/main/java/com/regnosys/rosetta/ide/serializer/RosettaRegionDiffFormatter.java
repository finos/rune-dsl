/*
 * Copyright 2026 REGnosys
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

package com.regnosys.rosetta.ide.serializer;

import java.util.Collections;
import java.util.List;

import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccess;
import org.eclipse.xtext.formatting2.regionaccess.ITextRegionAccessDiff;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.ide.serializer.impl.RegionDiffFormatter;

/**
 * Avoids formatting unchanged resources during serializer-driven edits such as
 * rename. Xtext's {@link RegionDiffFormatter} represents "no modified regions"
 * as an empty region collection, but the downstream formatter interprets an
 * empty region collection as "format the whole document". The merge step would
 * then discard every replacement because there are no diff regions to merge.
 *
 * TODO: contribute this fast path to Xtext. It is language-agnostic and belongs
 * in {@code RegionDiffFormatter.format(ITextRegionAccess)} before calling the
 * language formatter.
 */
public class RosettaRegionDiffFormatter extends RegionDiffFormatter {
	@Override
	public List<ITextReplacement> format(ITextRegionAccess regions) {
		if (regions instanceof ITextRegionAccessDiff diff && collectRegionsToFormat(diff).isEmpty()) {
			return Collections.emptyList();
		}
		return super.format(regions);
	}
}
