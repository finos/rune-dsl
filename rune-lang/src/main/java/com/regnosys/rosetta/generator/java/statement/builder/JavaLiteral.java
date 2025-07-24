package com.regnosys.rosetta.generator.java.statement.builder;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;

import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;

public class JavaLiteral extends JavaExpression {
	public static final JavaLiteral NULL = new JavaLiteral("null", JavaReferenceType.NULL_TYPE);
	public static final JavaLiteral TRUE = new JavaLiteral("true", JavaPrimitiveType.BOOLEAN);
	public static final JavaLiteral FALSE = new JavaLiteral("false", JavaPrimitiveType.BOOLEAN);
	public static JavaLiteral INT(int intValue) {
		return new JavaLiteral(Integer.toString(intValue), JavaPrimitiveType.INT);
	}
	public static JavaLiteral LONG(long longValue) {
		return new JavaLiteral(Long.toString(longValue) + "l", JavaPrimitiveType.LONG);
	}
	public static JavaLiteral STRING(String stringValue) {
		return new JavaLiteral("\"" + StringEscapeUtils.escapeJava(stringValue) + "\"", JavaType.from(String.class));
	}
	
	private final String representation;
	
	public JavaLiteral(String representation, JavaType type) {
		super(type);
		this.representation = representation;
	}

	@Override
	public void appendTo(TargetStringConcatenation target) {
		target.append(representation);
	}
	
	@Override
	public String toString() {
		return representation;
	}
}
