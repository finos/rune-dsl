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

import org.eclipse.xtend2.lib.StringConcatenationClient;

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
		if (value instanceof RosettaBooleanValue) {
			return toJavaListIfNecessary(((RosettaBooleanValue) value).getItems(), this::convertBooleanValueToJava);
		} else if (value instanceof RosettaDateTimeValue) {
			return toJavaListIfNecessary(((RosettaDateTimeValue) value).getItems(), this::convertDateTimeValueToJava);
		} else if (value instanceof RosettaDateValue) {
			return toJavaListIfNecessary(((RosettaDateValue) value).getItems(), this::convertDateValueToJava);
		} else if (value instanceof RosettaNumberValue) {
			return toJavaListIfNecessary(((RosettaNumberValue) value).getItems(), this::convertNumberValueToJava);
		} else if (value instanceof RosettaStringValue) {
			return toJavaListIfNecessary(((RosettaStringValue) value).getItems(), this::convertStringValueToJava);
		} else if (value instanceof RosettaTimeValue) {
			return toJavaListIfNecessary(((RosettaTimeValue) value).getItems(), this::convertTimeValueToJava);
		} else if (value instanceof RosettaZonedDateTimeValue) {
			return toJavaListIfNecessary(((RosettaZonedDateTimeValue) value).getItems(), this::convertZonedDateTimeValueToJava);
		}
		throw new UnsupportedOperationException("Cannot convert " + value + " to Java code");
	}
	
	private <T> JavaExpression toJavaListIfNecessary(List<T> items, Function<T, JavaExpression> handler) {
		if (items.size() == 1) {
			return handler.apply(items.get(0));
		} else {
			List<JavaExpression> expressions = items.stream().map(handler).collect(Collectors.toList());
			return JavaExpression.from(new StringConcatenationClient() {
				@Override
				protected void appendTo(TargetStringConcatenation target) {
					target.append(Arrays.class);
					target.append(".asList(");
					for (int i=0; i<expressions.size(); i++) {
						target.append(expressions.get(i));
						if (i != expressions.size() - 1) {
							target.append(", ");
						}
					}
					target.append(")");
				}
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
		return JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append(LocalDateTime.class);
				target.append(".of(");
				target.append(value.getYear());
				target.append(", ");
				target.append(value.getMonthValue());
				target.append(", ");
				target.append(value.getDayOfMonth());
				target.append(", ");
				target.append(value.getHour());
				target.append(", ");
				target.append(value.getMinute());
				target.append(", ");
				target.append(value.getSecond());
				target.append(", ");
				target.append(value.getNano());
				target.append(")");
			}
		}, typeUtil.LOCAL_DATE_TIME);
	}
	private JavaExpression convertDateValueToJava(LocalDate value) {
		return JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append(Date.class);
				target.append(".of(");
				target.append(value.getYear());
				target.append(", ");
				target.append(value.getMonthValue());
				target.append(", ");
				target.append(value.getDayOfMonth());
				target.append(")");
			}
		}, typeUtil.DATE);
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
			return JavaExpression.from(new StringConcatenationClient() {
				@Override
				protected void appendTo(TargetStringConcatenation target) {
					target.append("new ");
					target.append(BigInteger.class);
					target.append("(\"");
					target.append(bigIntegerValue);
					target.append("\")");
				}
			}, typeUtil.BIG_INTEGER);
		}
		// Default: value fits in a big decimal
		return JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("new ");
				target.append(BigDecimal.class);
				target.append("(\"");
				target.append(value);
				target.append("\")");
			}
		}, typeUtil.BIG_DECIMAL);
	}
	private JavaExpression convertStringValueToJava(String value) {
		return JavaLiteral.STRING(value);
	}
	private JavaExpression convertTimeValueToJava(LocalTime value) {
		return JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append(LocalTime.class);
				target.append(value.getHour());
				target.append(", ");
				target.append(value.getMinute());
				target.append(", ");
				target.append(value.getSecond());
				target.append(", ");
				target.append(value.getNano());
				target.append(")");
			}
		}, typeUtil.LOCAL_TIME);
	}
	private JavaExpression convertZonedDateTimeValueToJava(ZonedDateTime value) {
		return JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append(ZonedDateTime.class);
				target.append(".of(");
				target.append(value.getYear());
				target.append(", ");
				target.append(value.getMonthValue());
				target.append(", ");
				target.append(value.getDayOfMonth());
				target.append(", ");
				target.append(value.getHour());
				target.append(", ");
				target.append(value.getMinute());
				target.append(", ");
				target.append(value.getSecond());
				target.append(", ");
				target.append(value.getNano());
				target.append(", ");
				target.append(ZoneId.class);
				target.append(".of(");
				target.append(JavaLiteral.STRING(value.getZone().getId()));
				target.append("))");
			}
		}, typeUtil.ZONED_DATE_TIME);
	}
}
