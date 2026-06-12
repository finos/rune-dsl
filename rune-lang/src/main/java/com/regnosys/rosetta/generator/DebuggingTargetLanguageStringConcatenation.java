package com.regnosys.rosetta.generator;

import org.eclipse.xtend2.lib.StringConcatenationClient;

/**
 * Converts target language representations to a debug string, resolving generated
 * identifiers to their desired name.
 *
 * @deprecated Part of the legacy Xtend template machinery; will be removed once
 * all generators are migrated to the fluent {@code CodeWriter} API.
 */
@Deprecated
public class DebuggingTargetLanguageStringConcatenation extends TargetLanguageStringConcatenation {
	private static final ThreadLocal<Integer> recursionDepth = ThreadLocal.withInitial(() -> 0);
	private static final int MAX_RECURSION_DEPTH = 3;

	public static String convertToDebugString(Object object) {
		int depth = recursionDepth.get();
		if (depth >= MAX_RECURSION_DEPTH) {
			// Prevent infinite recursion during debug string generation
			return getRecursionLimitMessage(object);
		}

		recursionDepth.set(depth + 1);
		try {
			DebuggingTargetLanguageStringConcatenation repr = new DebuggingTargetLanguageStringConcatenation();
			StringConcatenationClient processed = repr.preprocess(new StringConcatenationClient() {
				protected void appendTo(TargetStringConcatenation target) {
					target.append(object);
				}
			});
			repr.append(processed);
			return repr.toString();
		} finally {
			recursionDepth.set(depth);
		}
	}
    private static String getRecursionLimitMessage(Object object) {
        String className = object.getClass().getSimpleName();
        if (className.isEmpty()) {
            // Anonymous class - use the full name
            className = object.getClass().getName();
        }
        return "<recursion-limit:" + className + "@" + Integer.toHexString(System.identityHashCode(object)) + ">";
    }
	
	@Override
	protected Object handle(Object object) {
		if (object instanceof GeneratedIdentifier) {
			return ((GeneratedIdentifier) object).getDesiredName();
		}
		return object;
	}
}
