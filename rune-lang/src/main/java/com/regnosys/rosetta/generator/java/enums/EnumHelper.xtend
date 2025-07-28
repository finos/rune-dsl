package com.regnosys.rosetta.generator.java.enums

import com.google.common.base.CaseFormat
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import java.util.Arrays
import java.util.List
import java.util.stream.Collectors

class EnumHelper {
	
	def static convertValue(RosettaEnumValue enumValue) {
		formatEnumName(enumValue.name)
	}

	def static String formatEnumName(String name) {
		if (noFormattingRequired(name))
			return name

		val parts = Arrays.asList(name.replaceSeparatorsWithUnderscores.splitAtNumbers).stream.map[splitAtUnderscore].
			flatMap[stream].map[splitAtCamelCase].flatMap[stream].map[camelCaseToUpperUnderscoreCase].map [
				it.toUpperCase
			].collect(Collectors.toList)

		return String.join("_", parts).prefixWithUnderscoreIfStartsWithNumber.removeDuplicateUnderscores
	}

	private def static boolean noFormattingRequired(String name) {
		return name.matches("^[A-Z0-9_]*$")
	}

	private def static String replaceSeparatorsWithUnderscores(String name) {
		return name.replace(".", "_").replace("-", "_").replace(" ", "_")
	}

	private def static List<String> splitAtCamelCase(String namePart) {
		return Arrays.asList(namePart.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"))
	}

	private def static List<String> splitAtUnderscore(String namePart) {
		return Arrays.asList(namePart.split("_"))
	}

	private def static String[] splitAtNumbers(String namePart) {
		return namePart.split("(?=[X])(?<=[^X])|(?=[^X])(?<=[X])".replace("X", "\\d"))
	}

	private def static String camelCaseToUpperUnderscoreCase(String namePart) {
		// if it starts with an upper case and ends with a lower case then assume it's camel case
		if (!namePart.empty && Character.isUpperCase(namePart.charAt(0)) &&
			Character.isLowerCase(namePart.charAt(namePart.length() - 1))) {
			return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, namePart)
		}
		return namePart
	}

	private def static String removeDuplicateUnderscores(String name) {
		return name.replace("__", "_")
	}

	private def static String prefixWithUnderscoreIfStartsWithNumber(String name) {
		if (Character.isDigit(name.charAt(0)))
			return "_" + name
		else
			return name
	}
}
