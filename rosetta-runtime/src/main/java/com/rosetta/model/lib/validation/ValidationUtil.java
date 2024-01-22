package com.rosetta.model.lib.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.rosetta.model.lib.path.RosettaPath;


public class ValidationUtil {
	
	public static ValidationResult checkCardinality(String msgPrefix, int actual, int min, int max, RosettaPath path) {
		
		CardinalityValidationData cardinalityValidationData = new CardinalityValidationData(min, max, actual);
		String failureMessage = "";
		if (actual < min) {
			if(actual == 0){
				failureMessage = "'" + msgPrefix + "' is a required field but does not exist."; 
				return ValidationResult
						.failure(null, failureMessage, cardinalityValidationData);
			}
			else {
				failureMessage = "Minimum of " + min + " '" + msgPrefix + "' is expected but found " + actual + ".";
				return ValidationResult
						.failure(null, failureMessage, cardinalityValidationData);
			}
		} else if (max > 0 && actual > max) {
			failureMessage = "Maximum of " + max + " '" + msgPrefix + "' are expected but found " + actual + ".";
			return ValidationResult
					.failure(path, failureMessage, cardinalityValidationData);
		}
		return ValidationResult.success(path);
	
		
	}
	
	public static ValidationResult checkString(String msgPrefix, String value, int minLength, Optional<Integer> maxLength, Optional<Pattern> pattern, RosettaPath path) {
		if (value == null) {
			return ValidationResult.success(path);
		}
		StringValidationData stringValidationData = new StringValidationData(minLength, maxLength, pattern, value);
		List<String> failures = new ArrayList<>();
		if (value.length() < minLength) {
			failures.add("Field '" + msgPrefix + "' requires a value with minimum length of " + minLength + " characters but value '" + value + "' has length of " + value.length() + " characters.");

		}
		if (maxLength.isPresent()) {
			int m = maxLength.get();
			if (value.length() > m) {
				failures.add("Field '" + msgPrefix + "' must have a value with maximum length of " + m + " characters but value '" + value + "' has length of " + value.length() + " characters.");
			}
		}
		if (pattern.isPresent()) {
			Pattern p = pattern.get();
			Matcher match = p.matcher(value);
			if (!match.matches()) {
				failures.add("Field '" + msgPrefix + "' with value '"+ value + "' does not match the pattern /" + p.toString() + "/.");

			}
		}
		if (failures.isEmpty()) {
			return ValidationResult.success(path);
		}
		return ValidationResult.failure(path,
					failures.stream().collect(Collectors.joining(" ")), stringValidationData
				);
	}
	
	public static ValidationResult checkNumber(String msgPrefix, BigDecimal value, Optional<Integer> digits, Optional<Integer> fractionalDigits, Optional<BigDecimal> min, Optional<BigDecimal> max, RosettaPath path) {
		if (value == null) {
			return ValidationResult.success(path);
		}
		
		NumberValidationData numValData = new NumberValidationData(min, max, digits.isPresent()?digits:Optional.of(0), fractionalDigits.isPresent()?fractionalDigits:Optional.of(0), value);
		List<String> failures = new ArrayList<>();
		if (digits.isPresent()) {
			int d = digits.get();
			BigDecimal normalized = value.stripTrailingZeros();
			int actual = normalized.precision();
			if (normalized.scale() >= normalized.precision()) {
				// case 0.0012 => `actual` should be 5
				actual = normalized.scale() + 1;
			}
			if (normalized.scale() < 0) {
				// case 12000 => `actual` should include unsignificant zeros
				actual -= normalized.scale();
			}
			if (actual > d) {
				failures.add("Expected a maximum of " + d + " digits for '" + msgPrefix + "', but the number " + value + " has " + actual + ".");
			}
		}
		if (fractionalDigits.isPresent()) {
			int f = fractionalDigits.get();
			BigDecimal normalized = value.stripTrailingZeros();
			int actual = normalized.scale();
			if (normalized.scale() < 0) {
				actual = 0;
			}
			if (actual > f) {
				failures.add("Expected a maximum of " + f + " fractional digits for '" + msgPrefix + "', but the number " + value + " has " + actual + ".");
			}
		}
		if (min.isPresent()) {
			BigDecimal m = min.get();
			if (value.compareTo(m) < 0) {
				failures.add("Expected a number greater than or equal to " + m.toPlainString()+ " for '" + msgPrefix + "', but found " + value + ".");
			}
		}
		if (max.isPresent()) {
			BigDecimal m = max.get();
			if (value.compareTo(m) > 0) {
				failures.add("Expected a number less than or equal to " + m.toPlainString() + " for '" + msgPrefix + "', but found " + value + ".");
			}
		}
		if (failures.isEmpty()) {
			return ValidationResult.success(path);
		}
		return ValidationResult.failure(path,
				failures.stream().collect(Collectors.joining(" ")), numValData
			);
	}
	//integer
	public static ValidationResult checkNumber(String msgPrefix, Integer value, Optional<Integer> digits, Optional<BigDecimal> min, Optional<BigDecimal> max, RosettaPath path) {
		if (value == null) {
			return ValidationResult.success(path);
		}
		BigDecimal valuasDecimal = BigDecimal.valueOf(value);
		NumberValidationData numValData = new NumberValidationData(min, max, digits.isPresent()?digits:Optional.of(0), Optional.of(0), valuasDecimal);
		List<String> failures = new ArrayList<>();
		if (digits.isPresent()) {
			int d = digits.get();
			BigDecimal normalized = valuasDecimal.stripTrailingZeros();
			int actual = normalized.precision();
			if (normalized.scale() >= normalized.precision()) {
				// case 0.0012 => `actual` should be 5
				actual = normalized.scale() + 1;
			}
			if (normalized.scale() < 0) {
				// case 12000 => `actual` should include unsignificant zeros
				actual -= normalized.scale();
			}
			if (actual > d) {
				failures.add("Expected a maximum of " + d + " digits for '" + msgPrefix + "', but the number " + value + " has " + actual + ".");
			}
		}
		if (min.isPresent()) {
			BigDecimal m = min.get();
			if (valuasDecimal.compareTo(m) < 0) {
				failures.add("Expected a number greater than or equal to " + m.toPlainString()+ " for '" + msgPrefix + "', but found " + value + ".");
			}
		}
		if (max.isPresent()) {
			BigDecimal m = max.get();
			if (valuasDecimal.compareTo(m) > 0) {
				failures.add("Expected a number less than or equal to " + m.toPlainString() + " for '" + msgPrefix + "', but found " + value + ".");
			}
		}
		if (failures.isEmpty()) {
			return ValidationResult.success(path);
		}
		return ValidationResult.failure(path,
				failures.stream().collect(Collectors.joining(" ")), numValData
			);
	}


}
