package com.rosetta.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;

public class DottedPath implements Comparable<DottedPath> {
	protected final String[] segments;
	
	protected DottedPath(String... segments) {
		this.segments = segments;
	}
	
	public String last() {
		return segments[segments.length - 1];
	}
	
	/* Construction */
	public static DottedPath of(String... segments) {
		return new DottedPath(Arrays.copyOf(segments, segments.length));
	}
	public static DottedPath split(String str, String separator) {
		return new DottedPath(str.split(Pattern.quote(separator)));
	}
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
	
	/* Conversion */
	public String withSeparator(CharSequence separator) {
		return String.join(separator, segments);
	}
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
