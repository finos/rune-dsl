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
        assertCoercion(expectedCode, expr, actual, expected, true);
    }

    private void assertCoercion(String expectedCode, StringConcatenationClient expr, JavaType actual, JavaType expected, boolean throwOnFail) {
        DottedPath pkg = DottedPath.of("test", "ns");
        JavaStatementScope scope = scopeUtil.createTestExpressionScope(pkg);

        JavaStatementBuilder coercedExpr =
                coercionService.addCoercions(JavaExpression.from(expr, actual), expected, throwOnFail, scope);
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

    private RJavaFieldWithMeta fieldWithMetaString() {
        return new RJavaFieldWithMeta(typeUtil.STRING, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil);
    }

    @Test
    void testIntToPrimitiveLong() {
        String expected = """
                return 42;
                """;
        assertCoercion(expected, code("42"), JavaPrimitiveType.INT, JavaPrimitiveType.LONG);
    }

    @Test
    void testIntegerToLong() {
        String expected = """
                {
                \tfinal Integer integer = Integer.valueOf(42);
                \treturn integer == null ? null : integer.longValue();
                }
                """;
        assertCoercion(expected, code(Integer.class, ".valueOf(42)"), typeUtil.INTEGER, typeUtil.LONG);
    }

    @Test
    void testLongToBigInteger() {
        String expected = """
                import java.math.BigInteger;


                {
                \tfinal Long _long = Long.valueOf(42);
                \treturn _long == null ? null : BigInteger.valueOf(_long);
                }
                """;
        assertCoercion(expected, code(Long.class, ".valueOf(42)"), typeUtil.LONG, typeUtil.BIG_INTEGER);
    }

    @Test
    void testLongToBigDecimal() {
        String expected = """
                import java.math.BigDecimal;


                {
                \tfinal Long _long = Long.valueOf(42);
                \treturn _long == null ? null : BigDecimal.valueOf(_long);
                }
                """;
        assertCoercion(expected, code(Long.class, ".valueOf(42)"), typeUtil.LONG, typeUtil.BIG_DECIMAL);
    }

    @Test
    void testBigIntegerToInteger() {
        String expected = """
                import java.math.BigInteger;


                {
                \tfinal BigInteger bigInteger = BigInteger.valueOf(42);
                \treturn bigInteger == null ? null : bigInteger.intValueExact();
                }
                """;
        assertCoercion(expected, code(BigInteger.class, ".valueOf(42)"), typeUtil.BIG_INTEGER, typeUtil.INTEGER);
    }

    @Test
    void testBigIntegerToBigDecimal() {
        String expected = """
                import java.math.BigDecimal;
                import java.math.BigInteger;


                {
                \tfinal BigInteger bigInteger = BigInteger.valueOf(42);
                \treturn bigInteger == null ? null : new BigDecimal(bigInteger);
                }
                """;
        assertCoercion(expected, code(BigInteger.class, ".valueOf(42)"), typeUtil.BIG_INTEGER, typeUtil.BIG_DECIMAL);
    }

    @Test
    void testBigDecimalToInteger() {
        String expected = """
                import java.math.BigDecimal;


                {
                \tfinal BigDecimal bigDecimal = BigDecimal.valueOf(42);
                \treturn bigDecimal == null ? null : bigDecimal.intValueExact();
                }
                """;
        assertCoercion(expected, code(BigDecimal.class, ".valueOf(42)"), typeUtil.BIG_DECIMAL, typeUtil.INTEGER);
    }

    @Test
    void testBigDecimalToLong() {
        String expected = """
                import java.math.BigDecimal;


                {
                \tfinal BigDecimal bigDecimal = BigDecimal.valueOf(42);
                \treturn bigDecimal == null ? null : bigDecimal.longValueExact();
                }
                """;
        assertCoercion(expected, code(BigDecimal.class, ".valueOf(42)"), typeUtil.BIG_DECIMAL, typeUtil.LONG);
    }

    @Test
    void testLongToIntegerWithoutThrowing() {
        String expected = """
                {
                \tfinal Long _long = Long.valueOf(42);
                \tif (_long == null) {
                \t\treturn null;
                \t}
                \treturn _long <= Integer.MAX_VALUE && _long >= Integer.MIN_VALUE ? (int) _long : null;
                }
                """;
        assertCoercion(expected, code(Long.class, ".valueOf(42)"), typeUtil.LONG, typeUtil.INTEGER, false);
    }

    @Test
    void testBigIntegerToIntegerWithoutThrowing() {
        String expected = """
                import java.math.BigInteger;


                {
                \tfinal BigInteger bigInteger = BigInteger.valueOf(42);
                \tif (bigInteger == null) {
                \t\treturn null;
                \t}
                \treturn BigInteger.valueOf(bigInteger.intValue()).equals(bigInteger) ? bigInteger.intValue() : null;
                }
                """;
        assertCoercion(expected, code(BigInteger.class, ".valueOf(42)"), typeUtil.BIG_INTEGER, typeUtil.INTEGER, false);
    }

    @Test
    void testBigIntegerToLongWithoutThrowing() {
        String expected = """
                import java.math.BigInteger;


                {
                \tfinal BigInteger bigInteger = BigInteger.valueOf(42);
                \tif (bigInteger == null) {
                \t\treturn null;
                \t}
                \treturn BigInteger.valueOf(bigInteger.longValue()).equals(bigInteger) ? bigInteger.longValue() : null;
                }
                """;
        assertCoercion(expected, code(BigInteger.class, ".valueOf(42)"), typeUtil.BIG_INTEGER, typeUtil.LONG, false);
    }

    @Test
    void testBigDecimalToIntegerWithoutThrowing() {
        String expected = """
                import java.math.BigDecimal;


                {
                \tfinal BigDecimal bigDecimal = BigDecimal.valueOf(42);
                \tif (bigDecimal == null) {
                \t\treturn null;
                \t}
                \treturn BigDecimal.valueOf(bigDecimal.intValue()).compareTo(bigDecimal) == 0 ? bigDecimal.intValue() : null;
                }
                """;
        assertCoercion(expected, code(BigDecimal.class, ".valueOf(42)"), typeUtil.BIG_DECIMAL, typeUtil.INTEGER, false);
    }

    @Test
    void testBigDecimalToLongWithoutThrowing() {
        String expected = """
                import java.math.BigDecimal;


                {
                \tfinal BigDecimal bigDecimal = BigDecimal.valueOf(42);
                \tif (bigDecimal == null) {
                \t\treturn null;
                \t}
                \treturn BigDecimal.valueOf(bigDecimal.longValue()).compareTo(bigDecimal) == 0 ? bigDecimal.longValue() : null;
                }
                """;
        assertCoercion(expected, code(BigDecimal.class, ".valueOf(42)"), typeUtil.BIG_DECIMAL, typeUtil.LONG, false);
    }

    @Test
    void testBigDecimalToBigIntegerWithoutThrowing() {
        String expected = """
                import java.math.BigDecimal;


                {
                \tfinal BigDecimal bigDecimal = BigDecimal.valueOf(42);
                \tif (bigDecimal == null) {
                \t\treturn null;
                \t}
                \treturn new BigDecimal(bigDecimal.toBigInteger()).compareTo(bigDecimal) == 0 ? bigDecimal.toBigInteger() : null;
                }
                """;
        assertCoercion(expected, code(BigDecimal.class, ".valueOf(42)"), typeUtil.BIG_DECIMAL, typeUtil.BIG_INTEGER, false);
    }

    @Test
    void testComparisonResultToList() {
        String expected = """
                import com.rosetta.model.lib.expression.ComparisonResult;


                return ComparisonResult.success().getMulti();
                """;
        assertCoercion(expected, code(ComparisonResult.class, ".success()"), typeUtil.COMPARISON_RESULT, typeUtil.wrap(typeUtil.LIST, Boolean.class));
    }

    @Test
    void testImmutableMapperSToMutableMapperS() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperS;
                import java.util.function.Function;


                return MapperS.of("a").map("Make mutable", Function.identity());
                """;
        assertCoercion(expected, code(MapperS.class, ".of(\"a\")"), typeUtil.wrapExtends(typeUtil.MAPPER_S, String.class), typeUtil.wrap(typeUtil.MAPPER_S, String.class));
    }

    @Test
    void testImmutableMapperCToMutableMapperC() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import java.util.function.Function;


                return MapperC.of("a").map("Make mutable", Function.identity());
                """;
        assertCoercion(expected, code(MapperC.class, ".of(\"a\")"), typeUtil.wrapExtends(typeUtil.MAPPER_C, String.class), typeUtil.wrap(typeUtil.MAPPER_C, String.class));
    }

    @Test
    void testMapperSToMapperC() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import com.rosetta.model.lib.mapper.MapperS;


                return MapperC.of(MapperS.of("a"));
                """;
        assertCoercion(expected, code(MapperS.class, ".of(\"a\")"), typeUtil.wrap(typeUtil.MAPPER_S, String.class), typeUtil.wrap(typeUtil.MAPPER_C, String.class));
    }

    @Test
    void testMapperCToMapperS() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import com.rosetta.model.lib.mapper.MapperS;


                return MapperS.of(MapperC.of("a").get());
                """;
        assertCoercion(expected, code(MapperC.class, ".of(\"a\")"), typeUtil.wrap(typeUtil.MAPPER_C, String.class), typeUtil.wrap(typeUtil.MAPPER_S, String.class));
    }

    @Test
    void testImmutableMapperCToMutableList() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import java.util.ArrayList;


                return new ArrayList<>(MapperC.of("a").getMulti());
                """;
        assertCoercion(expected, code(MapperC.class, ".of(\"a\")"), typeUtil.wrapExtends(typeUtil.MAPPER_C, String.class), typeUtil.wrap(typeUtil.LIST, String.class));
    }

    @Test
    void testImmutableMapperCToImmutableList() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;


                return MapperC.of("a").getMulti();
                """;
        assertCoercion(expected, code(MapperC.class, ".of(\"a\")"), typeUtil.wrapExtends(typeUtil.MAPPER_C, String.class), typeUtil.wrapExtends(typeUtil.LIST, String.class));
    }

    @Test
    void testListToComparisonResult() {
        String expected = """
                import com.rosetta.model.lib.expression.ComparisonResult;
                import com.rosetta.model.lib.mapper.MapperC;
                import java.util.Arrays;


                return ComparisonResult.ofNullSafe(MapperC.of(Arrays.asList(true)));
                """;
        assertCoercion(expected, code(Arrays.class, ".asList(true)"), typeUtil.wrap(typeUtil.LIST, Boolean.class), typeUtil.COMPARISON_RESULT);
    }

    @Test
    void testListToMapperS() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperS;
                import java.util.Arrays;


                return MapperS.of(Arrays.asList("a").get(0));
                """;
        assertCoercion(expected, code(Arrays.class, ".asList(\"a\")"), typeUtil.wrap(typeUtil.LIST, String.class), typeUtil.wrap(typeUtil.MAPPER_S, String.class));
    }

    @Test
    void testListToMapperC() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import java.util.Arrays;


                return MapperC.<String>of(Arrays.asList("a"));
                """;
        assertCoercion(expected, code(Arrays.class, ".asList(\"a\")"), typeUtil.wrap(typeUtil.LIST, String.class), typeUtil.wrap(typeUtil.MAPPER_C, String.class));
    }

    @Test
    void testImmutableListToMutableList() {
        String expected = """
                import java.util.ArrayList;
                import java.util.Arrays;


                return new ArrayList(Arrays.asList("a"));
                """;
        assertCoercion(expected, code(Arrays.class, ".asList(\"a\")"), typeUtil.wrapExtends(typeUtil.LIST, String.class), typeUtil.wrap(typeUtil.LIST, String.class));
    }

    @Test
    void testListItemConversion() {
        String expected = """
                import java.math.BigDecimal;
                import java.util.Arrays;
                import java.util.stream.Collectors;


                return Arrays.asList(1, 2).stream()
                \t.<BigDecimal>map(integer -> BigDecimal.valueOf(integer))
                \t.collect(Collectors.toList())
                ;
                """;
        assertCoercion(expected, code(Arrays.class, ".asList(1, 2)"), typeUtil.wrap(typeUtil.LIST, Integer.class), typeUtil.wrap(typeUtil.LIST, BigDecimal.class));
    }

    @Test
    void testListItemConversionWithoutThrowing() {
        String expected = """
                import java.util.Arrays;
                import java.util.stream.Collectors;


                return Arrays.asList(1L, 2L).stream()
                \t.<Integer>map(_long -> _long <= Integer.MAX_VALUE && _long >= Integer.MIN_VALUE ? (int) _long : null)
                \t.collect(Collectors.toList())
                ;
                """;
        assertCoercion(expected, code(Arrays.class, ".asList(1L, 2L)"), typeUtil.wrap(typeUtil.LIST, Long.class), typeUtil.wrap(typeUtil.LIST, Integer.class), false);
    }

    @Test
    void testMapperCItemConversionWithoutThrowing() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;


                return MapperC.of(1L).<Integer>map("Type coercion", _long -> _long <= Integer.MAX_VALUE && _long >= Integer.MIN_VALUE ? (int) _long : null);
                """;
        assertCoercion(expected, code(MapperC.class, ".of(1L)"), typeUtil.wrap(typeUtil.MAPPER_C, Long.class), typeUtil.wrap(typeUtil.MAPPER_C, Integer.class), false);
    }

    @Test
    void testListItemAndWrapperConversion() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperS;
                import java.math.BigDecimal;
                import java.util.Arrays;
                import java.util.stream.Collectors;


                return MapperS.of(Arrays.asList(1, 2).stream()
                \t.<BigDecimal>map(integer -> BigDecimal.valueOf(integer))
                \t.collect(Collectors.toList())
                .get(0));
                """;
        assertCoercion(expected, code(Arrays.class, ".asList(1, 2)"), typeUtil.wrap(typeUtil.LIST, Integer.class), typeUtil.wrap(typeUtil.MAPPER_S, BigDecimal.class));
    }

    @Test
    void testMapperListOfListsItemConversion() {
        String expected = """
                import java.math.BigDecimal;


                return lists.<BigDecimal>mapListToList(mapperC -> mapperC.<BigDecimal>map("Type coercion", integer -> BigDecimal.valueOf(integer)));
                """;
        assertCoercion(expected, code("lists"), typeUtil.wrap(typeUtil.MAPPER_LIST_OF_LISTS, Integer.class), typeUtil.wrap(typeUtil.MAPPER_LIST_OF_LISTS, BigDecimal.class));
    }

    @Test
    void testListToItem() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import java.util.Arrays;


                return MapperC.of(Arrays.asList("a")).get();
                """;
        assertCoercion(expected, code(Arrays.class, ".asList(\"a\")"), typeUtil.wrap(typeUtil.LIST, String.class), typeUtil.STRING);
    }

    @Test
    void testMapperSToPrimitiveBoolean() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperS;


                return MapperS.of(true).getOrDefault(false);
                """;
        assertCoercion(expected, code(MapperS.class, ".of(true)"), typeUtil.wrap(typeUtil.MAPPER_S, Boolean.class), JavaPrimitiveType.BOOLEAN);
    }

    @Test
    void testVoidToMapperS() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperS;


                return MapperS.<String>ofNull();
                """;
        assertCoercion(expected, code("null"), typeUtil.VOID, typeUtil.wrap(typeUtil.MAPPER_S, String.class));
    }

    @Test
    void testVoidToMapperC() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;


                return MapperC.<String>ofNull();
                """;
        assertCoercion(expected, code("null"), typeUtil.VOID, typeUtil.wrap(typeUtil.MAPPER_C, String.class));
    }

    @Test
    void testVoidToComparisonResult() {
        String expected = """
                import com.rosetta.model.lib.expression.ComparisonResult;


                return ComparisonResult.ofEmpty();
                """;
        assertCoercion(expected, code("null"), typeUtil.VOID, typeUtil.COMPARISON_RESULT);
    }

    @Test
    void testVoidToPrimitiveBoolean() {
        String expected = """
                return false;
                """;
        assertCoercion(expected, code("null"), typeUtil.VOID, JavaPrimitiveType.BOOLEAN);
    }

    @Test
    void testVoidToMeta() {
        String expected = """
                import test.FieldWithMetaString;


                return FieldWithMetaString.builder().build();
                """;
        assertCoercion(expected, code("null"), typeUtil.VOID, fieldWithMetaString());
    }

    @Test
    void testStringToList() {
        String expected = """
                import java.util.Collections;


                {
                \tfinal String string = "a";
                \treturn string == null ? Collections.<String>emptyList() : Collections.singletonList(string);
                }
                """;
        assertCoercion(expected, code("\"a\""), typeUtil.STRING, typeUtil.wrap(typeUtil.LIST, String.class));
    }

    @Test
    void testStringToMapperC() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperC;
                import java.util.Collections;


                return MapperC.of(Collections.singletonList("a"));
                """;
        assertCoercion(expected, code("\"a\""), typeUtil.STRING, typeUtil.wrap(typeUtil.MAPPER_C, String.class));
    }

    @Test
    void testMetaToList() {
        String expected = """
                import java.util.Collections;
                import test.FieldWithMetaString;


                {
                \tfinal FieldWithMetaString fieldWithMetaString = FieldWithMetaString.builder().setValue("a").build();
                \treturn fieldWithMetaString == null || fieldWithMetaString.getValue() == null ? Collections.<String>emptyList() : Collections.singletonList(fieldWithMetaString.getValue());
                }
                """;
        assertCoercion(expected, code("FieldWithMetaString.builder().setValue(\"a\").build()"), fieldWithMetaString(), typeUtil.wrap(typeUtil.LIST, String.class));
    }

    @Test
    void testMetaToMapperS() {
        String expected = """
                import com.rosetta.model.lib.mapper.MapperS;
                import test.FieldWithMetaString;


                {
                \tfinal FieldWithMetaString fieldWithMetaString = FieldWithMetaString.builder().setValue("a").build();
                \treturn fieldWithMetaString == null ? MapperS.<String>ofNull() : MapperS.of(fieldWithMetaString.getValue());
                }
                """;
        assertCoercion(expected, code("FieldWithMetaString.builder().setValue(\"a\").build()"), fieldWithMetaString(), typeUtil.wrap(typeUtil.MAPPER_S, String.class));
    }

    @Test
    void testListOfMetaItemConversion() {
        String expected = """
                import java.math.BigDecimal;
                import java.util.stream.Collectors;


                return metas.stream()
                \t.<BigDecimal>map(fieldWithMetaInteger -> {
                \t\tfinal Integer integer = fieldWithMetaInteger.getValue();
                \t\treturn integer == null ? null : BigDecimal.valueOf(integer);
                \t})
                \t.collect(Collectors.toList())
                ;
                """;
        assertCoercion(expected, code("metas"), typeUtil.wrap(typeUtil.LIST, fieldWithMetaInteger()), typeUtil.wrap(typeUtil.LIST, BigDecimal.class));
    }

    @Test
    void testMapperSOfMetaItemConversion() {
        String expected = """
                import java.math.BigDecimal;


                return metas.<BigDecimal>map("Type coercion", fieldWithMetaInteger -> {
                \tif (fieldWithMetaInteger == null) {
                \t\treturn null;
                \t}
                \tfinal Integer integer = fieldWithMetaInteger.getValue();
                \treturn integer == null ? null : BigDecimal.valueOf(integer);
                });
                """;
        assertCoercion(expected, code("metas"), typeUtil.wrap(typeUtil.MAPPER_S, fieldWithMetaInteger()), typeUtil.wrap(typeUtil.MAPPER_S, BigDecimal.class));
    }

    private RJavaFieldWithMeta fieldWithMetaInteger() {
        return new RJavaFieldWithMeta(typeUtil.INTEGER, JavaPackageName.splitOnDotsAndEscape("test"), typeUtil);
    }
}
