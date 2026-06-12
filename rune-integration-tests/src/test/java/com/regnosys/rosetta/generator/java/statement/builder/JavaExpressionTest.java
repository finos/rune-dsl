package com.regnosys.rosetta.generator.java.statement.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtend2.lib.StringConcatenationClient.TargetStringConcatenation;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.codegen.support.StringCodeWriter;
import com.rosetta.util.types.JavaPrimitiveType;

class JavaExpressionTest {
	@Test
	void xtendExpressionCanRenderNestedFluentExpressionToCodeWriter() {
		JavaExpression expression = JavaExpression.from(new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("left(");
				target.append(JavaExpression.from(out -> out.write("right"), JavaPrimitiveType.INT));
				target.append(")");
			}
		}, JavaPrimitiveType.INT);

		StringCodeWriter out = new StringCodeWriter();
		expression.render(out);

		assertEquals("left(right)", out.toString());
	}

	@Test
	void fluentExpressionCanAppendNestedXtendExpressionToStringConcatenation() {
		JavaExpression expression = JavaExpression.from(out -> {
			out.write("left(");
			out.write(JavaExpression.from(new StringConcatenationClient() {
				@Override
				protected void appendTo(TargetStringConcatenation target) {
					target.append("right");
				}
			}, JavaPrimitiveType.INT));
			out.write(")");
		}, JavaPrimitiveType.INT);

		assertEquals("left(right)", expression.toString());
	}
}
