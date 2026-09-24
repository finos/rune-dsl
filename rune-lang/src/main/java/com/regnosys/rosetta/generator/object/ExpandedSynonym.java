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

package com.regnosys.rosetta.generator.object;

import java.util.List;
import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaMapping;
import com.regnosys.rosetta.rosetta.RosettaMergeSynonymValue;
import com.regnosys.rosetta.rosetta.RosettaSynonymSource;

@Deprecated
public final class ExpandedSynonym {
	private final List<RosettaSynonymSource> sources;
	private final List<ExpandedSynonymValue> values;
	private final List<String> hints;
	private final RosettaMergeSynonymValue merge;
	private final List<ExpandedSynonymValue> metaValues;
	private final RosettaMapping mappingLogic;
	private final String mapperName;
	private final String format;
	private final String patternMatcher;
	private final String patternReplace;
	private final boolean removeHtml;

	public ExpandedSynonym(List<RosettaSynonymSource> sources, List<ExpandedSynonymValue> values, List<String> hints, RosettaMergeSynonymValue merge, List<ExpandedSynonymValue> metaValues, RosettaMapping mappingLogic, String mapperName, String format, String patternMatcher, String patternReplace, boolean removeHtml) {
		this.sources = sources;
		this.values = values;
		this.hints = hints;
		this.merge = merge;
		this.metaValues = metaValues;
		this.mappingLogic = mappingLogic;
		this.mapperName = mapperName;
		this.format = format;
		this.patternMatcher = patternMatcher;
		this.patternReplace = patternReplace;
		this.removeHtml = removeHtml;
	}

	public List<RosettaSynonymSource> getSources() {
		return sources;
	}

	public List<ExpandedSynonymValue> getValues() {
		return values;
	}

	public List<String> getHints() {
		return hints;
	}

	public RosettaMergeSynonymValue getMerge() {
		return merge;
	}

	public List<ExpandedSynonymValue> getMetaValues() {
		return metaValues;
	}

	public RosettaMapping getMappingLogic() {
		return mappingLogic;
	}

	public String getMapperName() {
		return mapperName;
	}

	public String getFormat() {
		return format;
	}

	public String getPatternMatcher() {
		return patternMatcher;
	}

	public String getPatternReplace() {
		return patternReplace;
	}

	public boolean isRemoveHtml() {
		return removeHtml;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sources, values, hints, merge, metaValues, mappingLogic, mapperName, format, patternMatcher, patternReplace, removeHtml);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof ExpandedSynonym other
				&& Objects.equals(sources, other.sources)
				&& Objects.equals(values, other.values)
				&& Objects.equals(hints, other.hints)
				&& Objects.equals(merge, other.merge)
				&& Objects.equals(metaValues, other.metaValues)
				&& Objects.equals(mappingLogic, other.mappingLogic)
				&& Objects.equals(mapperName, other.mapperName)
				&& Objects.equals(format, other.format)
				&& Objects.equals(patternMatcher, other.patternMatcher)
				&& Objects.equals(patternReplace, other.patternReplace)
				&& removeHtml == other.removeHtml;
	}

	@Override
	public String toString() {
		return "ExpandedSynonym [sources=" + sources + ", values=" + values + ", hints=" + hints + ", mapperName=" + mapperName + ", format=" + format + "]";
	}
}
