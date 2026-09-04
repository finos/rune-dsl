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

package com.regnosys.rosetta.parsing;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.nodemodel.INode;

/**
 * Converts between a dotted name as it is written in a Rune model - e.g.
 * {@code ^namespace.foo.Bar} - and the name it stands for - e.g. {@code namespace.foo.Bar}.
 *
 * <p>Escaping is a property of a single segment, so each segment is converted on its own by the
 * given segment converter. A wildcard converter additionally accepts a trailing {@code *}, which
 * is what an import ends in; anywhere else a {@code *} is rejected along with anything else that
 * cannot be written as a Rune identifier.
 */
public class EscapableQualifiedNameValueConverter extends AbstractValueConverter<String> {
	private static final String SEPARATOR = ".";
	private static final Pattern SEGMENT_SEPARATOR = Pattern.compile(Pattern.quote(SEPARATOR));
	private static final String WILDCARD = "*";

	private final EscapableIDValueConverter segmentConverter;
	private final boolean allowTrailingWildcard;

	public EscapableQualifiedNameValueConverter(EscapableIDValueConverter segmentConverter,
			boolean allowTrailingWildcard) {
		this.segmentConverter = segmentConverter;
		this.allowTrailingWildcard = allowTrailingWildcard;
	}

	@Override
	public String toValue(String string, INode node) {
		if (string == null) {
			return null;
		}
		return mapSegments(string, segment -> segmentConverter.toValue(segment, node));
	}

	@Override
	public String toString(String value) throws ValueConverterException {
		if (value == null) {
			throw new ValueConverterException("A qualified name may not be null.", null, null);
		}
		return mapSegments(value, segmentConverter::toString);
	}

	private String mapSegments(String name, UnaryOperator<String> mapper) {
		// -1 keeps a trailing empty segment, so a malformed name is rejected rather than truncated.
		String[] segments = SEGMENT_SEPARATOR.split(name, -1);
		return IntStream.range(0, segments.length)
				// A name never contains whitespace, so this is a no-op on the way out. On the way
				// in it matters: the grammar allows whitespace around a dot, e.g. a namespace
				// wrapped over two lines, and the text handed to us still holds it.
				.mapToObj(i -> isWildcard(segments[i].strip(), i, segments.length)
						? WILDCARD
						: mapper.apply(segments[i].strip()))
				.collect(Collectors.joining(SEPARATOR));
	}

	private boolean isWildcard(String segment, int index, int segmentCount) {
		return allowTrailingWildcard && index == segmentCount - 1 && WILDCARD.equals(segment);
	}
}
