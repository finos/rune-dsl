package com.regnosys.rosetta.tools.modelimport;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Different strategies of converting strings into different casing conventions.
 * 
 * The input is assumed to be a string consisting out of one or more alphanumerical parts.
 * Different parts may be separated by one or more non-alphanumerical characters, such as
 * whitespace or hyphens. An underscore is considered alphanumerical.
 * 
 * Each part of the input is assumed to be in camelCase or PascalCase.
 */
public enum CasingStrategy {
	
	KeepOriginal {
		@Override
		protected String doTransform(String s) {
			return Arrays.stream(Constants.PARTS_REGEX.split(s))
					.collect(Collectors.joining("_"));
		}
	},
	camelCase {
		@Override
		protected String doTransform(String s) {
			String[] parts = Constants.PARTS_REGEX.split(s);
			StringBuilder builder = new StringBuilder();
	        builder.append(allFirstLowerIfNotAbbrevation(parts[0]));
	        Arrays.stream(parts).skip(1).forEach(part -> {
	        	if (!part.isEmpty()) {
	        		if (Character.isUpperCase(part.charAt(0))) {
		                builder.append(part);
		            } else {
		                builder.append(Character.toUpperCase(part.charAt(0)));
		                builder.append(part, 1, part.length());
		            }
	        	}
	        });
	        return builder.toString();
		}
		
		/**
	     * Transforms a PascalCase string to camelCase, considering abbrevations.
	     * It leaves camelCase strings unchanged.
	     * 
	     * Examples of transformation:
	     * - XSDGenerator -> xsdGenerator
	     * - Generator -> generator
	     * - XSD -> xsd
	     * - myGenerator -> myGenerator
	     */
		private String allFirstLowerIfNotAbbrevation(String s) {
			if (s.isEmpty())
				return s;
			int upperCased = 0;
			while (upperCased < s.length() && Character.isUpperCase(s.charAt(upperCased))) {
				upperCased++;
			}
			if (upperCased == 0)
				return s;
			if (s.length() == upperCased)
				return s.toLowerCase();
			if (upperCased == 1) {
				return s.substring(0, 1).toLowerCase() + s.substring(1);
			}
			return s.substring(0, upperCased - 1).toLowerCase() + s.substring(upperCased - 1);
		}
	},
	PascalCase {
		@Override
		protected String doTransform(String s) {
			String[] parts = Constants.PARTS_REGEX.split(s);
	        StringBuilder builder = new StringBuilder();
	        for (String part : parts) {
	        	if (!part.isEmpty()) {
		            if (Character.isUpperCase(part.charAt(0))) {
		                builder.append(part);
		            } else {
		                builder.append(Character.toUpperCase(part.charAt(0)));
		                builder.append(part, 1, part.length());
		            }
	        	}
	        }
	        return builder.toString();
		}
	},
	lower_snake_case {
		@Override
		protected String doTransform(String s) {
			String[] parts = Constants.PARTS_REGEX.split(s);
			StringBuilder builder = new StringBuilder();
			boolean isNewPart = false;
			for (String part : parts) {
				boolean previousWasLowerCase = false;
				for (int i=0; i<part.length(); i++) {
					char c = part.charAt(i);
					boolean isUpper = Character.isUpperCase(c);
					if (isNewPart) {
						builder.append('_');
						isNewPart = false;
					} else if (isUpper && previousWasLowerCase) {
						builder.append('_');
					}
					builder.append(Character.toLowerCase(c));
					previousWasLowerCase = !isUpper;
				}
				isNewPart = true;
			}
			return builder.toString();
		}
	},
	UPPER_SNAKE_CASE {
		@Override
		protected String doTransform(String s) {
			String[] parts = Constants.PARTS_REGEX.split(s);
			StringBuilder builder = new StringBuilder();
			boolean isNewPart = false;
			for (String part : parts) {
				boolean previousWasLowerCase = false;
				for (int i=0; i<part.length(); i++) {
					char c = part.charAt(i);
					boolean isUpper = Character.isUpperCase(c);
					if (isNewPart) {
						builder.append('_');
						isNewPart = false;
					} else if (isUpper && previousWasLowerCase) {
						builder.append('_');
					}
					builder.append(Character.toUpperCase(c));
					previousWasLowerCase = !isUpper;
				}
				isNewPart = true;
			}
			return builder.toString();
		}
	};
	
	protected abstract String doTransform(String s);
	public String transform(String s) {
		String result = doTransform(s);
		if (result.isEmpty())
			return result;
		if (!Character.isLetter(result.charAt(0)))
        	return "_" + result;
        return result;
	}
	
	private static class Constants {
		private static final Pattern PARTS_REGEX = Pattern.compile("[^a-zA-Z0-9_]+");
	}
}
