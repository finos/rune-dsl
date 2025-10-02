package com.regnosys.rosetta.utils;

public class EnvironmentUtil {
	/**
	 * Parse the given system property or environment variable as a boolean.
	 * If neither exists, or the value cannot be parsed, return a default value.
	 */
	public static boolean getBooleanOrDefault(String systemPropertyOrEnvironmentVariableName, boolean defaultValue) {
		boolean result = defaultValue;
		String value = getValue(systemPropertyOrEnvironmentVariableName);
		if ("true".equals(value)) {
			result = true;
		} else if ("false".equals(value)) {
			result = false;
		}
        return result;
	}
	
	private static String getValue(String systemPropertyOrEnvironmentVariableName) {
		String value = System.getProperty(systemPropertyOrEnvironmentVariableName);
		if (value == null) {
			value = System.getenv(systemPropertyOrEnvironmentVariableName);
		}
		return value;
	}
}
