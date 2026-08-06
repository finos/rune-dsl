package com.regnosys.rosetta.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.rosetta.util.DottedPath;

/**
 * A namespace pattern as it appears in a {@code rune-config.yml} &mdash; in {@code generators.namespaces}
 * and in the {@code namespace} of a {@code namespaceConfig} entry.
 * <p>
 * Namespaces are dot-separated sequences of alphanumeric segments, so only two forms need supporting:
 * <ul>
 * <li>a bare namespace, e.g. {@code abc.def}, which matches that namespace exactly;</li>
 * <li>a terminal {@code .*}, e.g. {@code abc.def.*}, which matches {@code abc.def} <em>and</em> all of its
 * subnamespaces. A bare {@code *} is the degenerate case of this, and matches every namespace.</li>
 * </ul>
 * Matching is segment-aware, so {@code abc.def.*} never matches {@code abc.defghi}. No other glob
 * syntax is recognized: {@code abc*}, {@code a?c} and {@code a[bc]} are read as exact namespaces, which
 * is to say they match nothing.
 * <p>
 * This is the single interpretation every consumer of a namespace pattern shares. The checkers under
 * {@code .github/scripts} implement the same rule in Python (see {@code namespace_patterns.py}), and
 * both read the same case corpus &mdash; {@code .github/scripts/namespace-pattern-cases.yml} &mdash;
 * so that a divergence between them fails a test rather than waiting to be noticed.
 */
public final class RuneNamespacePattern {
	private static final String WILDCARD = "*";

	private final String pattern;
	private final DottedPath path;
	private final boolean prefixMatch;

	private RuneNamespacePattern(String pattern, DottedPath path, boolean prefixMatch) {
		this.pattern = pattern;
		this.path = path;
		this.prefixMatch = prefixMatch;
	}

	/** Parses a pattern of the form {@code abc.def}, {@code abc.def.*} or {@code *}. */
	public static RuneNamespacePattern parse(String pattern) {
		Objects.requireNonNull(pattern);
		DottedPath path = DottedPath.splitOnDots(pattern);
		if (path.last().equals(WILDCARD)) {
			// `abc.*` keeps `abc` as the prefix; a bare `*` leaves the empty prefix, which every
			// namespace starts with.
			return new RuneNamespacePattern(pattern, path.parent(), true);
		}
		return new RuneNamespacePattern(pattern, path, false);
	}

	/** Whether the given namespace matches this pattern. */
	public boolean matches(String namespace) {
		return matches(DottedPath.splitOnDots(namespace));
	}

	/** Whether the given namespace matches this pattern. */
	public boolean matches(DottedPath namespace) {
		// `DottedPath.startsWith` is segment-aware and prefix-inclusive, so `abc.def.*` matches
		// `abc.def` itself but not `abc.defghi`.
		return prefixMatch ? namespace.startsWith(path) : namespace.equals(path);
	}

	/**
	 * A predicate matching any namespace covered by one of the given patterns. An empty collection
	 * matches nothing &mdash; a caller for which no patterns means "no restriction" should say so itself.
	 */
	public static Predicate<String> anyOf(Collection<String> patterns) {
		List<RuneNamespacePattern> parsed = parseAll(patterns);
		if (parsed.isEmpty()) {
			return namespace -> false;
		}
		return namespace -> {
			DottedPath path = DottedPath.splitOnDots(namespace);
			return parsed.stream().anyMatch(p -> p.matches(path));
		};
	}

	public static List<RuneNamespacePattern> parseAll(Collection<String> patterns) {
		List<RuneNamespacePattern> parsed = new ArrayList<>(patterns.size());
		for (String pattern : patterns) {
			parsed.add(parse(pattern));
		}
		return parsed;
	}

	/** The pattern as written in the configuration file. */
	@Override
	public String toString() {
		return pattern;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof RuneNamespacePattern)) {
			return false;
		}
		RuneNamespacePattern other = (RuneNamespacePattern) object;
		return prefixMatch == other.prefixMatch && path.equals(other.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, prefixMatch);
	}
}
