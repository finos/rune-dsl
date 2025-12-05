package com.rosetta.model.lib.expression;


import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;


import com.rosetta.model.lib.mapper.MapperC;
import com.rosetta.model.lib.mapper.MapperS;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpressionEqualityUtilTest {
//
//    @Test
//    public void sameType_numbers_All_success_when_all_equal_and_same_length() {
//        MapperC<Number> m1 = MapperC.of(Arrays.asList(1, 2, 3));
//        MapperC<Number> m2 = MapperC.of(Arrays.asList(BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.All);
//        assertTrue(res.get());
//    }
//
//    @Test
//    public void sameType_numbers_Any_success_when_any_equal() {
//        MapperC<Number> m1 = MapperC.of(Arrays.asList(1, 9));
//        MapperC<Number> m2 = MapperC.of(Arrays.asList(2, 9));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.Any);
//        assertTrue(res.get());
//    }
//
//    @Test
//    public void sameType_numbers_All_failure_when_any_not_equal() {
//        MapperC<Number> m1 = MapperC.of(Arrays.asList(1, 2, 4));
//        MapperC<Number> m2 = MapperC.of(Arrays.asList(1, 2, 3));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.All);
//        assertFalse(res.get());
//    }
//
//    @Test
//    public void sameType_numbers_Any_failure_when_none_equal() {
//        MapperC<Number> m1 = MapperC.of(Arrays.asList(1, 2));
//        MapperC<Number> m2 = MapperC.of(Arrays.asList(3, 4));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.Any);
//        assertFalse(res.get());
//    }
//
//    @Test
//    public void sameType_failureEmptyOperand_when_any_empty_list() {
//        MapperC<Integer> m1 = MapperC.of(Collections.emptyList());
//        MapperC<Integer> m2 = MapperC.of(Arrays.asList(1, 2));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.All);
//        assertFalse(res.get());
//    }
//
//    @Test
//    public void sameType_failureEmptyOperand_when_length_mismatch() {
//        MapperC<Integer> m1 = MapperC.of(Arrays.asList(1, 2, 3));
//        MapperC<Integer> m2 = MapperC.of(Arrays.asList(1, 2));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.All);
//        assertFalse(res.get());
//    }
//
//    @Test
//    public void sameType_zonedDateTime_All_success_when_equal() {
//        ZonedDateTime z1 = ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
//        ZonedDateTime z2 = ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
//        MapperC<ZonedDateTime> m1 = MapperC.of(Collections.singletonList(z1));
//        MapperC<ZonedDateTime> m2 = MapperC.of(Collections.singletonList(z2));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.All);
//        assertTrue(res.get());
//    }
//
//    @Test
//    public void sameType_genericEquals_All_success_with_nulls_and_strings() {
//        MapperC<String> m1 = MapperC.of(Arrays.asList(null, "b"));
//        MapperC<String> m2 = MapperC.of(Arrays.asList(null, "b"));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.All);
//        assertTrue(res.get());
//    }
//
//    @Test
//    public void differentType_C_vs_S_All_success_when_all_match_number() {
//        MapperC<Integer> m1 = MapperC.of(Arrays.asList(5, 5, 5));
//        MapperS<Number> m2 = MapperS.of(BigDecimal.valueOf(5));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.All);
//        assertTrue(res.get());
//    }
//
//    @Test
//    public void differentType_C_vs_S_All_failure_when_any_not_equal_number() {
//        MapperC<Integer> m1 = MapperC.of(Arrays.asList(5, 6, 5));
//        MapperS<Integer> m2 = MapperS.of(5);
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.All);
//        assertFalse(res.get());
//    }
//
//    @Test
//    public void differentType_C_vs_S_Any_success_when_any_equal_number() {
//        MapperC<Integer> m1 = MapperC.of(Arrays.asList(4, 6, 5));
//        MapperS<Integer> m2 = MapperS.of(5);
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.Any);
//        assertTrue(res.get());
//    }
//
//    @Test
//    public void differentType_C_vs_S_Any_failure_when_none_equal_number() {
//        MapperC<Integer> m1 = MapperC.of(Arrays.asList(1, 2, 3));
//        MapperS<Integer> m2 = MapperS.of(9);
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.Any);
//        assertFalse(res.get());
//    }
//
//    @Test
//    public void differentType_C_vs_S_failureEmptyOperand_when_c_empty() {
//        MapperC<Integer> m1 = MapperC.of(Collections.emptyList());
//        MapperS<Integer> m2 = MapperS.of(1);
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.All);
//        assertFalse(res.get());
//    }
//
//    @Test
//    public void differentType_C_vs_S_genericEquals_with_null_and_string() {
//        MapperC<String> m1 = MapperC.of(Arrays.asList(null, "x", "y"));
//        MapperS<String> m2 = MapperS.of("x");
//
//        ComparisonResult resAny = ExpressionEqualityUtil.areEqual(m1, m2, CardinalityOperator.Any);
//        assertTrue(resAny.get());
//
//        MapperC<String> m3 = MapperC.of(Arrays.asList("x", "x"));
//        MapperS<String> m4 = MapperS.of("x");
//
//        ComparisonResult resAll = ExpressionEqualityUtil.areEqual(m3, m4, CardinalityOperator.All);
//        assertTrue(resAll.get());
//    }
//
//    @Test
//    public void comparisonResult_wrapped_mapper_behaviour() {
//        ComparisonResult left = ComparisonResult.success();
//        MapperC<Integer> right = MapperC.of(Collections.singletonList(1));
//
//        ComparisonResult res = ExpressionEqualityUtil.areEqual(left, right, CardinalityOperator.All);
//        assertFalse(res.get());
//        assertFalse(res.isEmptyOperand());
//    }
}
