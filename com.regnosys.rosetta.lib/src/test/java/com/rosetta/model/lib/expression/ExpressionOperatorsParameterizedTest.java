package com.rosetta.model.lib.expression;

import static com.rosetta.model.lib.expression.ExpressionOperators.exists;
import static com.rosetta.model.lib.expression.ExpressionOperators.greaterThan;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;

import com.rosetta.model.lib.mapper.MapperS;


public class ExpressionOperatorsParameterizedTest {

	private static final Function<Foo, ComparisonResult> GREATER_THAN = (foo) -> 
			greaterThan(MapperS.of(foo).map("getAttr1", Foo::getAttr1), MapperS.of(Integer.valueOf(5)), CardinalityOperator.All);
	
	private static final Function<Foo, ComparisonResult> GREATER_THAN_WITH_OR = (foo) -> 
			greaterThan(MapperS.of(foo).map("getAttr1", Foo::getAttr1), MapperS.of(Integer.valueOf(5)), CardinalityOperator.All)
					.or(greaterThan(MapperS.of(foo).map("getAttr2", Foo::getAttr2), MapperS.of(Integer.valueOf(5)), CardinalityOperator.All));
	
	private static final Function<Foo, ComparisonResult> EXISTS = (foo) -> 
			exists(MapperS.of(foo).map("getAttr1", Foo::getAttr1), false);
	
	private static final Function<Foo, ComparisonResult> EXISTS_WITH_OR = (foo) -> 
			exists(MapperS.of(foo).map("getAttr1", Foo::getAttr1), false)
					.or(exists(MapperS.of(foo).map("getAttr2", Foo::getAttr2), false));
	
	private static final Function<Bar, ComparisonResult> GREATER_THAN_LIST = (bar) -> 
			greaterThan(MapperS.of(bar).mapC("getFoos1", Bar::getFoos1).map("getAttr1", 
					Foo::getAttr1), MapperS.of(Integer.valueOf(5)), 
					CardinalityOperator.All);
	
	private static final Function<Bar, ComparisonResult> GREATER_THAN_LIST_GROUP_BY = (bar) -> 
			greaterThan(MapperS.of(bar).mapC("getFoos1", Bar::getFoos1).groupBy(i->new MapperS<>(i).map("getAttr1",Foo::getAttr1)).map("getAttr2", Foo::getAttr2), 
						MapperS.of(bar).mapC("getFoos2", Bar::getFoos2).groupBy(i->new MapperS<>(i).map("getAttr1", Foo::getAttr1)).map("getAttr2", Foo::getAttr2), 
						CardinalityOperator.All);
	
			
	@ParameterizedTest(name = "{0}")
    @MethodSource("evaluateFunctionTestParams")
	public <T> void evaluateFunction(String name, T object, Function<T, ComparisonResult> func, boolean success, List<String> errorMessageParts) {
		ComparisonResult result = func.apply(object);
		assertThat(result.get(), is(success));
		assertThat(StringUtils.isBlank(result.getError()), is(errorMessageParts.isEmpty()));
		for(String errorMessagePart : errorMessageParts)
			assertThat(result.getError(), containsString(errorMessagePart));
	}
	
	@SuppressWarnings("unused")
    private static Stream<Arguments> evaluateFunctionTestParams() {
		List<Arguments> args = Arrays.asList(
				Arguments.of("success: ( Foo -> attr1 ) > 5", 
						new Foo(10, 20), 
						GREATER_THAN, true, 
						Collections.emptyList()),
				Arguments.of("fail: ( Foo -> attr1 ) > 5", 
						new Foo(1, 2), 
						GREATER_THAN, false, 
						Collections.singletonList("all elements of paths [Foo->getAttr1] values [1] are not > than all elements of paths [Integer] values [5]")),
				Arguments.of("success: ( Foo -> attr1 ) > 5 or ( Foo -> attr2 ) > 5", 
						new Foo(10, 2), 
						GREATER_THAN_WITH_OR, true, 
						Collections.emptyList()),
				Arguments.of("fail: ( Foo -> attr1 ) > 5 or ( Foo -> attr2 ) > 5", 
						new Foo(1, 2), 
						GREATER_THAN_WITH_OR, false, 
						Arrays.asList("all elements of paths [Foo->getAttr1] values [1] are not > than all elements of paths [Integer] values [5]", 
								"all elements of paths [Foo->getAttr2] values [2] are not > than all elements of paths [Integer] values [5]")),
				Arguments.of("success: ( Bar -> foos1 -> attr1 ) > 5", 
						new Bar(Arrays.asList(new Foo(10), new Foo(20))), 
						GREATER_THAN_LIST, true, 
						Collections.emptyList()),
				Arguments.of("fail: ( Bar -> foos1 -> attr1 ) > 5", 
						new Bar(Arrays.asList(new Foo(10), new Foo(3))), 
						GREATER_THAN_LIST, false, 
						Collections.singletonList("all elements of paths [Bar->getFoos1[0]->getAttr1, Bar->getFoos1[1]->getAttr1] values [10, 3] are not > than all elements of paths [Integer] values [5]")),
				Arguments.of("success: ( Bar -> foos1 -> attr2 group by attr1 ) > ( Bar -> foos2 -> attr2 group by attr1 )", 
						new Bar(Arrays.asList(new Foo(1, 20), new Foo(2, 10)), Arrays.asList(new Foo(1, 15), new Foo(2, 5))), 
						GREATER_THAN_LIST_GROUP_BY, true, 
						Collections.emptyList()),
				Arguments.of("fail1: ( Bar -> foos1 -> attr2 group by attr1 ) > ( Bar -> foos2 -> attr2 group by attr1 )", 
						new Bar(Arrays.asList(new Foo(1, 20), new Foo(2, 10)), Arrays.asList(new Foo(1, 25), new Foo(2, 5))), 
						GREATER_THAN_LIST_GROUP_BY, false, 
						Collections.singletonList("all elements of paths [Bar->getFoos1[0]->getAttr2] values [20] are not > than all elements of paths [Bar->getFoos2[0]->getAttr2] values [25]")),
				Arguments.of("fail2: ( Bar -> foos1 -> attr2 group by attr1 ) > ( Bar -> foos2 -> attr2 group by attr1 )", 
						new Bar(Arrays.asList(new Foo(1, 20), new Foo(2, 10)), Arrays.asList(new Foo(1, 25), new Foo(2, 15))), 
						GREATER_THAN_LIST_GROUP_BY, false, 
						Arrays.asList("all elements of paths [Bar->getFoos1[1]->getAttr2] values [10] are not > than all elements of paths [Bar->getFoos2[1]->getAttr2] values [15]", 
								"all elements of paths [Bar->getFoos1[0]->getAttr2] values [20] are not > than all elements of paths [Bar->getFoos2[0]->getAttr2] values [25]")),
				Arguments.of("fail3: ( Bar -> foos1 -> attr2 group by attr1 ) > ( Bar -> foos2 -> attr2 group by attr1 )", 
						new Bar(Arrays.asList(new Foo(1, 20), new Foo(2, 10)), Arrays.asList(new Foo(1, 15), new Foo(3, 5))), 
						GREATER_THAN_LIST_GROUP_BY, false, 
						Collections.singletonList("Group mismatched: [Bar->getFoos1[0]->getAttr1] [1],[Bar->getFoos1[1]->getAttr1] [2] cannot be compared to [Bar->getFoos2[0]->getAttr1] [1],[Bar->getFoos2[1]->getAttr1] [3]")),
				Arguments.of("success: ( Foo -> attr ) exists", 
						new Foo(1, 2), 
						EXISTS, true, 
						Collections.emptyList()),
				Arguments.of("fail: ( Foo -> attr ) exists", 
						new Foo(null, 2), 
						EXISTS, false, 
						Collections.singletonList("[Foo->getAttr1] does not exist")),
				Arguments.of("success: ( Foo -> attr1 ) exists or ( Foo -> attr2 ) exists", 
						new Foo(null, 2), 
						EXISTS_WITH_OR, true, 
						Collections.emptyList()),
				Arguments.of("fail: ( Foo -> attr1 ) exists or ( Foo -> attr2 ) exists", 
						new Foo(null, null), 
						EXISTS_WITH_OR, false, 
						Arrays.asList("[Foo->getAttr1] does not exist", "[Foo->getAttr2] does not exist")));
		return args.stream();
    }

	// Test classes
	
	private static class Foo {
		private final Integer attr1;
		private final Integer attr2;
		
		public Foo(Integer attr1) {
			this(attr1, null);
		}
		
		public Foo(Integer attr1, Integer attr2) {
			this.attr1 = attr1;
			this.attr2 = attr2;
		}
		
		public Integer getAttr1() {
			return attr1;
		}

		public Integer getAttr2() {
			return attr2;
		}
	}
	
	private static class Bar {
		private final List<Foo> foos1, foos2;
		
		public Bar(List<Foo> foos1) {
			this(foos1, Collections.emptyList());
		}
		
		public Bar(List<Foo> foos1, List<Foo> foos2) {
			this.foos1 = foos1;
			this.foos2 = foos2;
		}

		public List<Foo> getFoos1() {
			return foos1;
		}
		
		public List<Foo> getFoos2() {
			return foos2;
		}
	}
}
