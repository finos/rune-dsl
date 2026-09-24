package com.regnosys.rosetta.generator.java.expression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.interpreter.RosettaBooleanValue;
import com.regnosys.rosetta.interpreter.RosettaDateTimeValue;
import com.regnosys.rosetta.interpreter.RosettaDateValue;
import com.regnosys.rosetta.interpreter.RosettaNumberValue;
import com.regnosys.rosetta.interpreter.RosettaStringValue;
import com.regnosys.rosetta.interpreter.RosettaTimeValue;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.interpreter.RosettaZonedDateTimeValue;
import com.rosetta.model.lib.RosettaNumber;
import com.rosetta.model.lib.records.Date;

import jakarta.inject.Inject;

public class InterpreterValueJavaConverter {
	@Inject
	private JavaTypeUtil typeUtil;
	
	public JavaExpression convertValueToJava(RosettaValue value) {
		if (value.size() == 0) {
			return JavaLiteral.NULL;
		}
		return switch (value) {
			case RosettaBooleanValue booleanValue -> toJavaListIfNecessary(booleanValue.getItems(), this::convertBooleanValueToJava);
			case RosettaDateTimeValue dateTimeValue -> toJavaListIfNecessary(dateTimeValue.getItems(), this::convertDateTimeValueToJava);
			case RosettaDateValue dateValue -> toJavaListIfNecessary(dateValue.getItems(), this::convertDateValueToJava);
			case RosettaNumberValue numberValue -> toJavaListIfNecessary(numberValue.getItems(), this::convertNumberValueToJava);
			case RosettaStringValue stringValue -> toJavaListIfNecessary(stringValue.getItems(), this::convertStringValueToJava);
			case RosettaTimeValue timeValue -> toJavaListIfNecessary(timeValue.getItems(), this::convertTimeValueToJava);
			case RosettaZonedDateTimeValue zonedDateTimeValue -> toJavaListIfNecessary(zonedDateTimeValue.getItems(), this::convertZonedDateTimeValueToJava);
			default -> throw new UnsupportedOperationException("Cannot convert " + value + " to Java code");
		};
	}
	
	private <T> JavaExpression toJavaListIfNecessary(List<T> items, Function<T, JavaExpression> handler) {
		if (items.size() == 1) {
			return handler.apply(items.get(0));
		} else {
			List<JavaExpression> expressions = items.stream().map(handler).collect(Collectors.toList());
			return JavaExpression.from(out -> {
				out.write(Arrays.class, ".asList(");
				out.join(expressions, ", ");
				out.write(")");
			}, typeUtil.wrap(typeUtil.LIST, expressions.get(0).getExpressionType()));
		}
	}
		
	private JavaExpression convertBooleanValueToJava(boolean value) {
		if (value) {
			return JavaLiteral.TRUE;
		}
		return JavaLiteral.FALSE;
	}
	private JavaExpression convertDateTimeValueToJava(LocalDateTime value) {
		return JavaExpression.from(out -> out.write(LocalDateTime.class, ".of(",
				value.getYear(), ", ", value.getMonthValue(), ", ", value.getDayOfMonth(), ", ",
				value.getHour(), ", ", value.getMinute(), ", ", value.getSecond(), ", ", value.getNano(), ")"), typeUtil.LOCAL_DATE_TIME);
	}
	private JavaExpression convertDateValueToJava(LocalDate value) {
		return JavaExpression.from(out -> out.write(Date.class, ".of(",
				value.getYear(), ", ", value.getMonthValue(), ", ", value.getDayOfMonth(), ")"), typeUtil.DATE);
	}
	private JavaExpression convertNumberValueToJava(RosettaNumber value) {
		int intValue = value.intValue();
		if (value.equals(RosettaNumber.valueOf(intValue))) {
			// Value fits in an int
			return JavaLiteral.INT(intValue);
		}
		long longValue = value.longValue();
		if (value.equals(RosettaNumber.valueOf(longValue))) {
			// Value fits in a long
			return JavaLiteral.LONG(longValue);
		}
		BigInteger bigIntegerValue = value.bigIntegerValue();
		if (value.equals(RosettaNumber.valueOf(bigIntegerValue))) {
			// Value fits in a big integer
			return JavaExpression.from(out -> out.write("new ", BigInteger.class, "(\"", bigIntegerValue, "\")"), typeUtil.BIG_INTEGER);
		}
		// Default: value fits in a big decimal
		return JavaExpression.from(out -> out.write("new ", BigDecimal.class, "(\"", value, "\")"), typeUtil.BIG_DECIMAL);
	}
	private JavaExpression convertStringValueToJava(String value) {
		return JavaLiteral.STRING(value);
	}
	private JavaExpression convertTimeValueToJava(LocalTime value) {
		return JavaExpression.from(out -> out.write(LocalTime.class, ".of(",
				value.getHour(), ", ", value.getMinute(), ", ", value.getSecond(), ", ", value.getNano(), ")"), typeUtil.LOCAL_TIME);
	}
	private JavaExpression convertZonedDateTimeValueToJava(ZonedDateTime value) {
		return JavaExpression.from(out -> out.write(ZonedDateTime.class, ".of(",
				value.getYear(), ", ", value.getMonthValue(), ", ", value.getDayOfMonth(), ", ",
				value.getHour(), ", ", value.getMinute(), ", ", value.getSecond(), ", ", value.getNano(), ", ",
				ZoneId.class, ".of(", JavaLiteral.STRING(value.getZone().getId()), "))"), typeUtil.ZONED_DATE_TIME);
	}
}
