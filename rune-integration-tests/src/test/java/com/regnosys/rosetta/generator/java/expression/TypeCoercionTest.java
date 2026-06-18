package com.regnosys.rosetta.generator.java.expression;

import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta;
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaType;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class TypeCoercionTest {
    @Inject
    private TypeCoercionService coercionService;
    @Inject
    private ImportManagerExtension importManager;
    @Inject
    private JavaTypeUtil typeUtil;
    @Inject
    private ExpressionScopeUtility scopeUtil;

    private void assertCoercion(String expectedCode, StringConcatenationClient expr, Class<?> actual, JavaType expected) {
        assertCoercion(expectedCode, expr, JavaType.from(actual), expected);
    }

    private void assertCoercion(String expectedCode, StringConcatenationClient expr, JavaType actual, Class<?> expected) {
        assertCoercion(expectedCode, expr, actual, JavaType.from(expected));
    }

    private void assertCoercion(String expectedCode, StringConcatenationClient expr, Class<?> actual, Class<?> expected) {
        assertCoercion(expectedCode, expr, JavaType.from(actual), JavaType.from(expected));
    }

    private void assertCoercion(String expectedCode, StringConcatenationClient expr, JavaType actual, JavaType expected) {
        DottedPath pkg = DottedPath.of("test", "ns");
        JavaStatementScope scope = scopeUtil.createTestExpressionScope(pkg);

        JavaStatementBuilder coercedExpr =
                coercionService.addCoercions(JavaExpression.from(expr, actual), expected, scope);
        StringConcatenationClient classCode = new StringConcatenationClient() {
            @Override
            protected void appendTo(TargetStringConcatenation target) {
                target.append(coercedExpr.completeAsReturn());
            }
        };
        // Normalize line endings to '\n' so the comparison holds on Windows, where the
        // generator emits '\r\n' but the expected text blocks always use '\n'.
        String actualCode = importManager.buildClass(pkg, classCode, scope.getFileScope())
                .replace("\r\n", "\n")
                .replace("package test.ns;", "").trim() + "\n";
        assertEquals(expectedCode, actualCode);
    }

    /**
     * Builds a {@link StringConcatenationClient} from literal text fragments and embedded
     * type references ({@link Class} objects), mirroring an Xtend {@code '''...'''} template.
     */
    private static StringConcatenationClient code(Object... parts) {
        return new StringConcatenationClient() {
            @Override
            protected void appendTo(TargetStringConcatenation target) {
                for (Object part : parts) {
                    target.append(part);
                }
            }
        };
    }

    @Test
    void testConvertBigDecimalToFieldWithMetaInteger() {
        String expected = """
                import java.math.BigDecimal;
                import test.FieldWithMetaInteger;


                {
                \tfinal BigDecimal bigDecimal = BigDecimal.valueOf(10);
                \treturn bigDecimal == null ? FieldWithMetaInteger.builder().build() : FieldWithMetaInteger.builder().setValue(bigDecimal.intValueExact()).build();
                }
                """;

        RJavaFieldWithMeta expectedType =
                new RJavaFieldWithMeta(typeUtil.INTEGER, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil);

        assertCoercion(expected, code("BigDecimal.valueOf(10)"), BigDecimal.class, expectedType);
    }

    @Test
    void testConvertFieldWithMetaIntegerToBigDecimal() {
        String expected = """
                import java.math.BigDecimal;
                import test.FieldWithMetaInteger;


                {
                \tfinal FieldWithMetaInteger fieldWithMetaInteger = FieldWithMetaInteger.builder().setValue(10).build();
                \tif (fieldWithMetaInteger == null) {
                \t\treturn null;
                \t}
                \tfinal Integer integer = fieldWithMetaInteger.getValue();
                \treturn integer == null ? null : BigDecimal.valueOf(integer);
                }
                """;

        RJavaFieldWithMeta actualType =
                new RJavaFieldWithMeta(typeUtil.INTEGER, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil);

        assertCoercion(expected, code("FieldWithMetaInteger.builder().setValue(10).build()"), actualType, BigDecimal.class);
    }

    @Test
    void testConvertMetaReferenceToMetaField() {
        String expected = """
                import test.FieldWithMetaString;
                import test.ReferenceWithMetaString;


                {
                \tfinal ReferenceWithMetaString referenceWithMetaString = ReferenceWithMetaString.builder().setValue("foo").build();
                \tif (referenceWithMetaString == null) {
                \t\treturn FieldWithMetaString.builder().build();
                \t}
                \tfinal String string = referenceWithMetaString.getValue();
                \treturn string == null ? FieldWithMetaString.builder().build() : FieldWithMetaString.builder().setValue(string).build();
                }
                """;

        RJavaReferenceWithMeta actualType =
                new RJavaReferenceWithMeta(typeUtil.STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil);
        RJavaFieldWithMeta expectedType =
                new RJavaFieldWithMeta(typeUtil.STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil);

        assertCoercion(expected, code("ReferenceWithMetaString.builder().setValue(\"foo\").build()"), actualType, expectedType);
    }

    @Test
    void testConvertMetaFieldToMetaReference() {
        String expected = """
                import test.FieldWithMetaString;
                import test.ReferenceWithMetaString;


                {
                \tfinal FieldWithMetaString fieldWithMetaString = FieldWithMetaString.builder().setValue("foo").build();
                \tif (fieldWithMetaString == null) {
                \t\treturn ReferenceWithMetaString.builder().build();
                \t}
                \tfinal String string = fieldWithMetaString.getValue();
                \treturn string == null ? ReferenceWithMetaString.builder().build() : ReferenceWithMetaString.builder().setValue(string).build();
                }
                """;

        RJavaFieldWithMeta actualType =
                new RJavaFieldWithMeta(typeUtil.STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil);
        RJavaReferenceWithMeta expectedType =
                new RJavaReferenceWithMeta(typeUtil.STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil);

        assertCoercion(expected, code("FieldWithMetaString.builder().setValue(\"foo\").build()"), actualType, expectedType);
    }

    @Test
    void testConvertStringToMeta() {
        String expected = """
                import test.FieldWithMetaString;


                {
                \tfinal String string = "foo";
                \treturn string == null ? FieldWithMetaString.builder().build() : FieldWithMetaString.builder().setValue(string).build();
                }
                """;
        assertCoercion(expected, code("\"foo\""), String.class,
                new RJavaFieldWithMeta(typeUtil.STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil));

        String expected2 = """
                import test.ReferenceWithMetaString;


                {
                \tfinal String string = "foo";
                \treturn string == null ? ReferenceWithMetaString.builder().build() : ReferenceWithMetaString.builder().setValue(string).build();
                }
                """;
        assertCoercion(expected2, code("\"foo\""), String.class,
                new RJavaReferenceWithMeta(typeUtil.STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil));
    }

    @Test
    void testConvertMetaToString() {
        String expected = """
                import test.FieldWithMetaString;


                {
                \tfinal FieldWithMetaString fieldWithMetaString = FieldWithMetaString.builder().setValue("foo").build();
                \treturn fieldWithMetaString == null ? null : fieldWithMetaString.getValue();
                }
                """;

        assertCoercion(expected, code("FieldWithMetaString.builder().setValue(\"foo\").build()"),
                new RJavaFieldWithMeta(typeUtil.STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil), String.class);

        String expected2 = """
                import test.ReferenceWithMetaString;


                {
                \tfinal ReferenceWithMetaString referenceWithMetaString = ReferenceWithMetaString.builder().setValue("foo").build();;
                \treturn referenceWithMetaString == null ? null : referenceWithMetaString.getValue();
                }
                """;

        assertCoercion(expected2, code("ReferenceWithMetaString.builder().setValue(\"foo\").build();"),
                new RJavaReferenceWithMeta(typeUtil.STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil), String.class);
    }

    @Test
    void testItemToItemConversion() {
        String expected;

        expected = """
                return (long) 42;
                """;
        assertCoercion(expected, code("42"), JavaPrimitiveType.INT, Long.class);

        expected = """
                import java.math.BigDecimal;


                {
                \tfinal Integer integer = Integer.valueOf(42);
                \treturn integer == null ? null : BigDecimal.valueOf(integer);
                }
                """;
        assertCoercion(expected, code(Integer.class, ".valueOf(42)"), Integer.class, BigDecimal.class);

        expected = """
                import java.math.BigInteger;


                {
                \tfinal BigInteger bigInteger = BigInteger.valueOf(42);
                \treturn bigInteger == null ? null : bigInteger.longValueExact();
                }
                """;
        assertCoercion(expected, code(BigInteger.class, ".valueOf(42)"), BigInteger.class, Long.class);

        expected = """
                {
                \tfinal Boolean _boolean = Boolean.valueOf(true);
                \treturn _boolean == null ? false : _boolean;
                }
                """;
        assertCoercion(expected, code(Boolean.class, ".valueOf(true)"), Boolean.class, JavaPrimitiveType.BOOLEAN);

        expected = """
                return null;
                """;
        assertCoercion(expected, code("null"), Void.class, Long.class);
    }

    @Test
    void testItemToWrapperConversion() {
        String expected;

        expected = """
                import com.rosetta.model.lib.mapper.MapperS;


                return MapperS.of(42);
                """;
        assertCoercion(expected, code("42"), JavaPrimitiveType.INT, typeUtil.wrap(typeUtil.MAPPER_S, Integer.class));

        expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import java.math.BigDecimal;
                import java.math.BigInteger;
                import java.util.Collections;


                {
                \tfinal BigDecimal bigDecimal = BigDecimal.valueOf(42);
                \treturn bigDecimal == null ? MapperC.<BigInteger>ofNull() : MapperC.of(Collections.singletonList(bigDecimal.toBigIntegerExact()));
                }
                """;
        assertCoercion(expected, code(BigDecimal.class, ".valueOf(42)"), BigDecimal.class, typeUtil.wrap(typeUtil.MAPPER_C, BigInteger.class));

        expected = """
                import java.util.Collections;


                return Collections.<Long>emptyList();
                """;
        assertCoercion(expected, code("null"), Void.class, typeUtil.wrap(typeUtil.LIST, Long.class));

        expected = """
                import com.rosetta.model.lib.expression.ComparisonResult;
                import com.rosetta.model.lib.mapper.MapperS;


                return ComparisonResult.ofNullSafe(MapperS.of(Boolean.valueOf(true)));
                """;
        assertCoercion(expected, code(Boolean.class, ".valueOf(true)"), Boolean.class, ComparisonResult.class);
    }

    @Test
    void testWrapperToItemConversion() {
        String expected;

        expected = """
                import com.rosetta.model.lib.mapper.MapperS;


                return MapperS.of("ABC").get();
                """;
        assertCoercion(expected, code(MapperS.class, ".of(\"ABC\")"), typeUtil.wrap(typeUtil.MAPPER_S, String.class), String.class);

        expected = """
                import com.rosetta.model.lib.mapper.MapperS;


                {
                \tfinal Long _long = MapperS.of(42).get();
                \treturn _long == null ? null : Math.toIntExact(_long);
                }
                """;
        assertCoercion(expected, code(MapperS.class, ".of(42)"), typeUtil.wrap(typeUtil.MAPPER_S, Long.class), Integer.class);

        expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import java.math.BigInteger;
                import java.util.Arrays;


                {
                \tfinal Integer integer = MapperC.of(Arrays.asList(1, 2, 3)).get();
                \treturn integer == null ? null : BigInteger.valueOf(integer);
                }
                """;
        assertCoercion(expected, code(MapperC.class, ".of(", Arrays.class, ".asList(1, 2, 3))"),
                typeUtil.wrap(typeUtil.MAPPER_C, Integer.class), BigInteger.class);

        expected = """
                import com.rosetta.model.lib.expression.ComparisonResult;


                return ComparisonResult.success().get();
                """;
        assertCoercion(expected, code(ComparisonResult.class, ".success()"), ComparisonResult.class, Boolean.class);

        expected = """
                return null;
                """;
        assertCoercion(expected, code(MapperS.class, ".ofNull()"), typeUtil.wrap(typeUtil.MAPPER_S, Void.class), Integer.class);
    }

    @Test
    void testWrapperToWrapperConversion() {
        String expected;

        expected = """
                import com.rosetta.model.lib.expression.ComparisonResult;
                import com.rosetta.model.lib.mapper.MapperS;


                return ComparisonResult.ofNullSafe(MapperS.of(true));
                """;
        assertCoercion(expected, code(MapperS.class, ".of(true)"), typeUtil.wrap(typeUtil.MAPPER_S, Boolean.class), ComparisonResult.class);

        expected = """
                import com.rosetta.model.lib.mapper.MapperS;


                return MapperS.of(42).<Integer>map("Type coercion", _long -> _long == null ? null : Math.toIntExact(_long)).getMulti();
                """;
        assertCoercion(expected, code(MapperS.class, ".of(42)"), typeUtil.wrap(typeUtil.MAPPER_S, Long.class), typeUtil.wrap(typeUtil.LIST, Integer.class));

        expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import java.math.BigInteger;
                import java.util.Arrays;


                return MapperC.of(Arrays.asList(1, 2, 3)).<BigInteger>map("Type coercion", integer -> BigInteger.valueOf(integer)).getMulti();
                """;
        assertCoercion(expected, code(MapperC.class, ".of(", Arrays.class, ".asList(1, 2, 3))"),
                typeUtil.wrap(typeUtil.MAPPER_C, Integer.class), typeUtil.wrap(typeUtil.LIST, BigInteger.class));

        expected = """
                import com.rosetta.model.lib.expression.ComparisonResult;


                return ComparisonResult.success().asMapper();
                """;
        assertCoercion(expected, code(ComparisonResult.class, ".success()"), ComparisonResult.class, typeUtil.wrap(typeUtil.MAPPER_S, Boolean.class));

        expected = """
                import java.util.Collections;


                return Collections.<String>emptyList();
                """;
        assertCoercion(expected, code(MapperS.class, ".ofNull()"), typeUtil.wrap(typeUtil.MAPPER_S, Void.class), typeUtil.wrap(typeUtil.LIST, String.class));
    }
}
