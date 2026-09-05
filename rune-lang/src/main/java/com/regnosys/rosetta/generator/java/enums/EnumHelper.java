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

package com.regnosys.rosetta.generator.java.enums;

import com.google.common.base.CaseFormat;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumHelper {

	public static String convertValue(RosettaEnumValue enumValue) {
		return formatEnumName(enumValue.getName());
	}

	public static String formatEnumName(String name) {
		if (noFormattingRequired(name)) {
			return name;
		}

		List<String> parts = Arrays.stream(splitAtNumbers(replaceSeparatorsWithUnderscores(name)))
				.flatMap(part -> splitAtUnderscore(part).stream())
				.flatMap(part -> splitAtCamelCase(part).stream())
				.map(EnumHelper::camelCaseToUpperUnderscoreCase)
				.map(String::toUpperCase)
				.collect(Collectors.toList());

		return removeDuplicateUnderscores(prefixWithUnderscoreIfStartsWithNumber(String.join("_", parts)));
	}

	private static boolean noFormattingRequired(String name) {
		return name.matches("^[A-Z0-9_]*$");
	}

	private static String replaceSeparatorsWithUnderscores(String name) {
		return name.replace(".", "_").replace("-", "_").replace(" ", "_");
	}

	private static List<String> splitAtCamelCase(String namePart) {
		return Arrays.asList(namePart.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"));
	}

	private static List<String> splitAtUnderscore(String namePart) {
		return Arrays.asList(namePart.split("_"));
	}

	private static String[] splitAtNumbers(String namePart) {
		return namePart.split("(?=\\d)(?<=\\D)|(?=\\D)(?<=\\d)");
	}

	private static String camelCaseToUpperUnderscoreCase(String namePart) {
		// assume it's camel case if it starts upper case and ends lower case
		if (!namePart.isEmpty() && Character.isUpperCase(namePart.charAt(0))
				&& Character.isLowerCase(namePart.charAt(namePart.length() - 1))) {
			return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, namePart);
		}
		return namePart;
	}

	private static String removeDuplicateUnderscores(String name) {
		return name.replace("__", "_");
	}

	private static String prefixWithUnderscoreIfStartsWithNumber(String name) {
		return Character.isDigit(name.charAt(0)) ? "_" + name : name;
	}
}
