package com.rosetta.model.lib.expression;

import static com.rosetta.model.lib.expression.ExpressionOperators.areEqual;
import static com.rosetta.model.lib.expression.ExpressionOperators.exists;
import static com.rosetta.model.lib.expression.ExpressionOperators.greaterThan;
import static com.rosetta.model.lib.expression.ExpressionOperators.notEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.mapper.MapperTree;


public class ExpressionOperatorsMapperTreeParameterizedTest {

	private static final Function<Foo, ComparisonResult> GREATER_THAN_WITH_OR = (foo) -> 
			greaterThan(MapperTree.or(
							MapperTree.of(MapperS.of(foo).map("getAttr1", Foo::getAttr1)), 
							MapperTree.of(MapperS.of(foo).map("getAttr2", Foo::getAttr2))), 
					MapperTree.of(MapperS.of(Integer.valueOf(5))));
	
	private static final Function<Foo, ComparisonResult> GREATER_THAN_WITH_OR_AND = (foo) -> 
			greaterThan(MapperTree.or(
					MapperTree.of(MapperS.of(foo).map("getAttr1", Foo::getAttr1)), 
					MapperTree.or(
								MapperTree.and(MapperTree.of(MapperS.of(foo).map("getAttr2", Foo::getAttr2)), 
											   MapperTree.of(MapperS.of(foo).map("getAttr3", Foo::getAttr3))), 
								MapperTree.of(MapperS.of(foo).map("getAttr4", Foo::getAttr4)))), 
					MapperTree.of(MapperS.of(Integer.valueOf(5))));
	
	private static final Function<Foo, ComparisonResult> GREATER_THAN_AND = (foo) -> 
			greaterThan(
					MapperTree.and(
							MapperTree.of(MapperS.of(foo).map("getAttr1", Foo::getAttr1)), 
							MapperTree.of(MapperS.of(foo).map("getAttr2", Foo::getAttr2))), 
					MapperTree.and(
							MapperTree.of(MapperS.of(foo).map("getAttr3", Foo::getAttr3)), 
							MapperTree.of(MapperS.of(foo).map("getAttr4", Foo::getAttr4))));
			
	private static final Function<Foo, ComparisonResult> ARE_EQUAL_AND = (foo) -> 
			areEqual(
					MapperTree.and(
							MapperTree.of(MapperS.of(foo).map("getAttr1", Foo::getAttr1)), 
							MapperTree.of(MapperS.of(foo).map("getAttr2", Foo::getAttr2))), 
					MapperTree.and(
							MapperTree.of(MapperS.of(foo).map("getAttr3", Foo::getAttr3)), 
							MapperTree.of(MapperS.of(foo).map("getAttr4", Foo::getAttr4))));
	
	private static final Function<Foo, ComparisonResult> NOT_EQUAL_AND = (foo) -> 
			notEqual(
					MapperTree.and(
							MapperTree.of(MapperS.of(foo).map("getAttr1", Foo::getAttr1)), 
							MapperTree.of(MapperS.of(foo).map("getAttr2", Foo::getAttr2))), 
					MapperTree.and(
							MapperTree.of(MapperS.of(foo).map("getAttr3", Foo::getAttr3)), 
							MapperTree.of(MapperS.of(foo).map("getAttr4", Foo::getAttr4))));
			
			
	private static final Function<Foo, ComparisonResult> EXISTS_WITH_OR = (foo) -> 
			exists(MapperTree.or(
					MapperTree.of(MapperS.of(foo).map("getAttr1", Foo::getAttr1)), 
								  MapperTree.of(MapperS.of(foo).map("getAttr2", Foo::getAttr2))), 
					false);
	
	@ParameterizedTest(name = "{0}")
    @MethodSource("evaluateFunctionTestParams")
	public void evaluateFunction(String name, Foo foo, Function<Foo, ComparisonResult> func, boolean success, String errorMessage) {
		ComparisonResult result = func.apply(foo);
		assertThat(result.getError(), is(errorMessage));
		assertThat(result.get(), is(success));
	}
	
	@SuppressWarnings("unused")
    private static Stream<Arguments> evaluateFunctionTestParams() {
		List<Arguments> args = Arrays.asList(
				Arguments.of("success1: ( Foo -> attr1 or Foo -> attr2 ) > 5", new Foo(10, 2), GREATER_THAN_WITH_OR, true, null),
				Arguments.of("success2: ( Foo -> attr1 or Foo -> attr2 ) > 5", new Foo(2, 10), GREATER_THAN_WITH_OR, true, null),
				Arguments.of("fail: ( Foo -> attr1 or Foo -> attr2 ) > 5", new Foo(1, 2), GREATER_THAN_WITH_OR, false, "all elements of paths [Foo->getAttr1] values [1] are not > than all elements of paths [Integer] values [5] and all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Integer] values [5]"),
				Arguments.of("success1: ( Foo -> attr1 or ( Foo -> attr2 and Foo -> attr3 ) or Foo -> attr4 ) > 5", new Foo(10, 2, 3, 4), GREATER_THAN_WITH_OR_AND, true, null),
				Arguments.of("success2: ( Foo -> attr1 or ( Foo -> attr2 and Foo -> attr3 ) or Foo -> attr4 ) > 5", new Foo(1, 20, 30, 4), GREATER_THAN_WITH_OR_AND, true, null),
				Arguments.of("success3: ( Foo -> attr1 or ( Foo -> attr2 and Foo -> attr3 ) or Foo -> attr4 ) > 5", new Foo(1, 2, 3, 40), GREATER_THAN_WITH_OR_AND, true, null),
				Arguments.of("fail: ( Foo -> attr1 or ( Foo -> attr2 and Foo -> attr3 ) or Foo -> attr4 ) > 5", new Foo(1, 2, 3, 4), GREATER_THAN_WITH_OR_AND, false, "all elements of paths [Foo->getAttr1] values [1] are not > than all elements of paths [Integer] values [5] and all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Integer] values [5] and all elements of paths [Foo->getAttr3] values [3] are not > than all elements of paths [Integer] values [5] and all elements of paths [Foo->getAttr4] values [4] are not > than all elements of paths [Integer] values [5]"),
				Arguments.of("fail: ( Foo -> attr1 or ( Foo -> attr2 and Foo -> attr3 ) or Foo -> attr4 ) > 5", new Foo(1, 20, 3, 4), GREATER_THAN_WITH_OR_AND, false, "all elements of paths [Foo->getAttr1] values [1] are not > than all elements of paths [Integer] values [5] and all elements of paths [Foo->getAttr3] values [3] are not > than all elements of paths [Integer] values [5] and all elements of paths [Foo->getAttr4] values [4] are not > than all elements of paths [Integer] values [5]"),
				Arguments.of("success1: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, 3, 2, 1), GREATER_THAN_AND, true, ""),
				Arguments.of("success2: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, null, 2, 1), GREATER_THAN_AND, true, ""),
				Arguments.of("success3: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 3, 2, 1), GREATER_THAN_AND, true, ""),
				Arguments.of("success4: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, 3, null, 1), GREATER_THAN_AND, true, ""),
				Arguments.of("success5: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, 3, 2, null), GREATER_THAN_AND, true, ""),Arguments.of("success: ( Foo -> attr1 or Foo -> attr2 ) exists", new Foo(null, 2), EXISTS_WITH_OR, true, null),
				Arguments.of("fail1: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(1, 2, 3, 4), GREATER_THAN_AND, false, "all elements of paths [Foo->getAttr1] values [1] are not > than all elements of paths [Foo->getAttr3] values [3] and all elements of paths [Foo->getAttr1] values [1] are not > than all elements of paths [Foo->getAttr4] values [4] and all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Foo->getAttr3] values [3] and all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Foo->getAttr4] values [4]"),
				Arguments.of("fail2: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, 2, 3, 5), GREATER_THAN_AND, false, "all elements of paths [Foo->getAttr1] values [4] are not > than all elements of paths [Foo->getAttr4] values [5] and all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Foo->getAttr3] values [3] and all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Foo->getAttr4] values [5]"),
				Arguments.of("fail3: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 2, 3, 5), GREATER_THAN_AND, false, "all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Foo->getAttr3] values [3] and all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Foo->getAttr4] values [5]"),
				Arguments.of("fail4: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 2, null, 5), GREATER_THAN_AND, false, "all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Foo->getAttr4] values [5]"),
				Arguments.of("fail5: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 2, null, null), GREATER_THAN_AND, false, "Null operand: [[] : null] > [[] : null] and Null operand: [[] : null] > [[] : null] and Null operand: [[Foo->getAttr2] : 2] > [[] : null] and Null operand: [[Foo->getAttr2] : 2] > [[] : null]"),
				Arguments.of("fail6: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, null, null, 2), GREATER_THAN_AND, false, "Null operand: [[] : null] > [[] : null] and Null operand: [[] : null] > [[Foo->getAttr4] : 2] and Null operand: [[] : null] > [[] : null] and Null operand: [[] : null] > [[Foo->getAttr4] : 2]"),
				Arguments.of("fail7: ( Foo -> attr1 and Foo -> attr2 ) > ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, null, null, null), GREATER_THAN_AND, false, "Null operand: [[] : null] > [[] : null] and Null operand: [[] : null] > [[] : null] and Null operand: [[] : null] > [[] : null] and Null operand: [[] : null] > [[] : null]"),
				Arguments.of("success1: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, 4, 4, 4), ARE_EQUAL_AND, true, ""),
				Arguments.of("success2: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, null, 4, 4), ARE_EQUAL_AND, true, ""),
				Arguments.of("success3: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 4, 4, 4), ARE_EQUAL_AND, true, ""),
				Arguments.of("success4: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, 4, null, 4), ARE_EQUAL_AND, true, ""),
				Arguments.of("success5: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, 4, 4, null), ARE_EQUAL_AND, true, ""),
				Arguments.of("fail1: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(1, 4, 4, 4), ARE_EQUAL_AND, false, "[Foo->getAttr1] [1] does not equal [Foo->getAttr3] [4] and [Foo->getAttr1] [1] does not equal [Foo->getAttr4] [4]"),
				Arguments.of("fail2: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, 4, 1, 4), ARE_EQUAL_AND, false, "[Foo->getAttr1] [4] does not equal [Foo->getAttr3] [1] and [Foo->getAttr2] [4] does not equal [Foo->getAttr3] [1]"),
				Arguments.of("fail3: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 1, 4, 4), ARE_EQUAL_AND, false, "[Foo->getAttr2] [1] does not equal [Foo->getAttr3] [4] and [Foo->getAttr2] [1] does not equal [Foo->getAttr4] [4]"),
				Arguments.of("fail4: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 1, null, 4), ARE_EQUAL_AND, false, "[Foo->getAttr2] [1] does not equal [Foo->getAttr4] [4]"),
				Arguments.of("fail5: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 4, null, null), ARE_EQUAL_AND, false, "[Foo->getAttr1] cannot be compared to [Foo->getAttr3] and [Foo->getAttr1] cannot be compared to [Foo->getAttr4] and [Foo->getAttr2] [4] cannot be compared to [Foo->getAttr3] and [Foo->getAttr2] [4] cannot be compared to [Foo->getAttr4]"),
				Arguments.of("fail6: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, null, null, 4), ARE_EQUAL_AND, false, "[Foo->getAttr1] cannot be compared to [Foo->getAttr3] and [Foo->getAttr1] cannot be compared to [Foo->getAttr4] [4] and [Foo->getAttr2] cannot be compared to [Foo->getAttr3] and [Foo->getAttr2] cannot be compared to [Foo->getAttr4] [4]"),
				Arguments.of("fail7: ( Foo -> attr1 and Foo -> attr2 ) = ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, null, null, null), ARE_EQUAL_AND, false, "[Foo->getAttr1] cannot be compared to [Foo->getAttr3] and [Foo->getAttr1] cannot be compared to [Foo->getAttr4] and [Foo->getAttr2] cannot be compared to [Foo->getAttr3] and [Foo->getAttr2] cannot be compared to [Foo->getAttr4]"),
				Arguments.of("success1: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(1, 2, 3, 4), NOT_EQUAL_AND, true, ""),
				Arguments.of("success2: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(1, null, 3, 4), NOT_EQUAL_AND, true, ""),
				Arguments.of("success3: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 2, 3, 4), NOT_EQUAL_AND, true, ""),
				Arguments.of("success4: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(1, 2, null, 4), NOT_EQUAL_AND, true, ""),
				Arguments.of("success5: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(1, 2, 3, null), NOT_EQUAL_AND, true, ""),
				Arguments.of("fail1: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(4, 4, 4, 4), NOT_EQUAL_AND, false, "[Foo->getAttr1] [4] does equal [Foo->getAttr3] [4] and [Foo->getAttr1] [4] does equal [Foo->getAttr4] [4] and [Foo->getAttr2] [4] does equal [Foo->getAttr3] [4] and [Foo->getAttr2] [4] does equal [Foo->getAttr4] [4]"),
				Arguments.of("fail2: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 4, 4, 4), NOT_EQUAL_AND, false, "[Foo->getAttr2] [4] does equal [Foo->getAttr3] [4] and [Foo->getAttr2] [4] does equal [Foo->getAttr4] [4]"),
				Arguments.of("fail3: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 4, null, 4), NOT_EQUAL_AND, false, "[Foo->getAttr2] [4] does equal [Foo->getAttr4] [4]"),
				Arguments.of("fail4: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, 4, null, null), NOT_EQUAL_AND, false, "[Foo->getAttr1] cannot be compared to [Foo->getAttr3] and [Foo->getAttr1] cannot be compared to [Foo->getAttr4] and [Foo->getAttr2] [4] cannot be compared to [Foo->getAttr3] and [Foo->getAttr2] [4] cannot be compared to [Foo->getAttr4]"),
				Arguments.of("fail5: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, null, null, 4), NOT_EQUAL_AND, false, "[Foo->getAttr1] cannot be compared to [Foo->getAttr3] and [Foo->getAttr1] cannot be compared to [Foo->getAttr4] [4] and [Foo->getAttr2] cannot be compared to [Foo->getAttr3] and [Foo->getAttr2] cannot be compared to [Foo->getAttr4] [4]"),
				Arguments.of("fail6: ( Foo -> attr1 and Foo -> attr2 ) <> ( Foo -> attr3 and Foo -> attr4 )", new Foo(null, null, null, null), NOT_EQUAL_AND, false, "[Foo->getAttr1] cannot be compared to [Foo->getAttr3] and [Foo->getAttr1] cannot be compared to [Foo->getAttr4] and [Foo->getAttr2] cannot be compared to [Foo->getAttr3] and [Foo->getAttr2] cannot be compared to [Foo->getAttr4]"),
				Arguments.of("success: ( Foo -> attr1 or Foo -> attr2 ) exists", new Foo(null, 2), EXISTS_WITH_OR, true, null),
				Arguments.of("fail: ( Foo -> attr1 or Foo -> attr2 ) exists", new Foo(null, null), EXISTS_WITH_OR, false, "[Foo->getAttr1] does not exist and [Foo->getAttr2] does not exist"));
		return args.stream();
    }
	
	// Test classes
	
	private static class Foo {
		private final Integer attr1;
		private final Integer attr2;
		private final Integer attr3;
		private final Integer attr4;
		
		public Foo(Integer attr1, Integer attr2) {
			this(attr1, attr2, null, null);
		}
		
		public Foo(Integer attr1, Integer attr2, Integer attr3, Integer attr4) {
			this.attr1 = attr1;
			this.attr2 = attr2;
			this.attr3 = attr3;
			this.attr4 = attr4;
		}

		public Integer getAttr1() {
			return attr1;
		}

		public Integer getAttr2() {
			return attr2;
		}

		public Integer getAttr3() {
			return attr3;
		}

		public Integer getAttr4() {
			return attr4;
		}
	}
}
