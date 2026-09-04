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
import java.util.stream.Stream;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.nodemodel.INode;

/**
 * Converts between a dotted name as it is written in a Rune model - e.g.
 * {@code ^namespace.foo.Bar} - and the name it stands for - e.g. {@code namespace.foo.Bar}.
 *
 * <p>Escaping is a property of a single segment, so each segment is converted on its own by the
 * given segment converter. A {@code *} segment, which a wildcard import ends in, is left alone.
 */
public class EscapableQualifiedNameValueConverter extends AbstractValueConverter<String> {
	private static final String SEPARATOR = ".";
	private static final Pattern SEGMENT_SEPARATOR = Pattern.compile(Pattern.quote(SEPARATOR));
	private static final String WILDCARD = "*";

	private final EscapableIDValueConverter segmentConverter;

	public EscapableQualifiedNameValueConverter(EscapableIDValueConverter segmentConverter) {
		this.segmentConverter = segmentConverter;
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
		return Stream.of(SEGMENT_SEPARATOR.split(name, -1))
				// The grammar allows whitespace around a dot, e.g. a namespace wrapped over two
				// lines, and the text handed to us still holds it. It is not part of the name.
				.map(String::strip)
				.map(segment -> WILDCARD.equals(segment) ? segment : mapper.apply(segment))
				.collect(Collectors.joining(SEPARATOR));
	}
}
