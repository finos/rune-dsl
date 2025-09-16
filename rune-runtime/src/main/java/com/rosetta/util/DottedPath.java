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

package com.rosetta.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public final class DottedPath implements Comparable<DottedPath> {
	protected final String[] segments;
	
	protected DottedPath(String... segments) {
		this.segments = segments;
	}
	protected DottedPath(DottedPath other) {
		this(other.segments);
	}
	
	public String first() {
		return segments[0];
	}
	public DottedPath tail() {
		return new DottedPath(Arrays.copyOfRange(segments, 1, segments.length));
	}
	public String last() {
		return segments[segments.length - 1];
	}
	
	/* Construction */
	public static DottedPath of(String... segments) {
		return new DottedPath(Arrays.copyOf(segments, segments.length));
	}
	public static DottedPath split(String str, String separator) {
		return new DottedPath(StringUtils.splitByWholeSeparator(str, separator));
	}
	@JsonCreator
	public static DottedPath splitOnDots(String str) {
		return split(str, ".");
	}
	public static DottedPath splitOnForwardSlashes(String str) {
		return split(str, "/");
	}
	
	/* Navigation */
	public DottedPath child(String newSegment) {
		String[] newSegments = Arrays.copyOf(segments, segments.length + 1);
		newSegments[newSegments.length - 1] = newSegment;
		return new DottedPath(newSegments);
	}
	public DottedPath parent() {
		return new DottedPath(Arrays.copyOf(segments, segments.length - 1));
	}
	public DottedPath concat(DottedPath second) {
		return new DottedPath(ArrayUtils.addAll(segments, second.segments));
	}
	
	/* Conversion */
	public String withSeparator(CharSequence separator) {
		return String.join(separator, segments);
	}
	@JsonValue
	public String withDots() {
		return withSeparator(".");
	}
	public String withForwardSlashes() {
		return withSeparator("/");
	}
	public Path toPath() {
		String[] tail = Arrays.copyOfRange(segments, 1, segments.length);
		return Paths.get(segments[0], tail);
	}
	public Stream<String> stream() {
		return Arrays.stream(segments);
	}
	
	/* Utility */
	public boolean startsWith(DottedPath other) {
		if (segments.length < other.segments.length) {
			return false;
		}
		for (int i=0; i<other.segments.length; i++) {
			if (!segments[i].equals(other.segments[i])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int compareTo(DottedPath o) {
		for (int i=0; i<segments.length && i<o.segments.length; i++) {
			int c = segments[i].compareTo(o.segments[i]);
			if (c != 0) {
				return c;
			}
		}
		return Integer.compare(segments.length, o.segments.length);
	}
	
	@Override
	public String toString() {
		return withDots();
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(segments);
	}
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (getClass() != object.getClass()) return false;
		
		DottedPath other = (DottedPath)object;
		return Arrays.equals(segments, other.segments);
	}
}
