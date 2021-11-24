package com.regnosys.rosetta.generator.java.calculation

import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import java.util.List
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.IsCollectionContaining.hasItems
import static org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import org.junit.jupiter.api.Disabled

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ListOperationTest {

	@Inject extension FuncGeneratorHelper
	@Inject extension CodeGeneratorTestHelper
	
	@Test
	def void shouldGenerateFunctionWithFilterListItemParameter() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFoos Foo (0..*)
				
				set filteredFoos:
					foos 
						filter [ item -> include = True ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Foo.FooBuilder;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<? extends Foo> evaluate(List<? extends Foo> foos) {
						
						List<Foo.FooBuilder> filteredFoosHolder = doEvaluate(foos);
						List<Foo.FooBuilder> filteredFoos = assignOutput(filteredFoosHolder, foos);
						
						if (filteredFoos!=null) objectValidator.validateAndFailOnErorr(Foo.class, filteredFoos);
						return filteredFoos;
					}
					
					private List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
						filteredFoos = toBuilder(MapperC.of(foos)
							.filter(__item -> areEqual(__item.<Boolean>map("getInclude", _foo -> _foo.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).getMulti())
						;
						return filteredFoos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'attr', 'a'), of())
		val foo2 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'attr', 'b'), of())
		val foo3 = classes.createInstanceUsingBuilder('Foo', of('include', false, 'attr', 'c'), of())
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(2, res.size);
		assertThat(res, hasItems(foo1, foo2));
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterListNamedParameter() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFoos Foo (0..*)
				
				set filteredFoos:
					foos 
						filter fooItem [ fooItem -> include = True ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Foo.FooBuilder;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<? extends Foo> evaluate(List<? extends Foo> foos) {
						
						List<Foo.FooBuilder> filteredFoosHolder = doEvaluate(foos);
						List<Foo.FooBuilder> filteredFoos = assignOutput(filteredFoosHolder, foos);
						
						if (filteredFoos!=null) objectValidator.validateAndFailOnErorr(Foo.class, filteredFoos);
						return filteredFoos;
					}
					
					private List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
						filteredFoos = toBuilder(MapperC.of(foos)
							.filter(__fooItem -> areEqual(__fooItem.<Boolean>map("getInclude", _foo -> _foo.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).getMulti())
						;
						return filteredFoos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'attr', 'a'), of())
		val foo2 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'attr', 'b'), of())
		val foo3 = classes.createInstanceUsingBuilder('Foo', of('include', false, 'attr', 'c'), of())
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(2, res.size);
		assertThat(res, hasItems(foo1, foo2));
	}

	@Test
	def void shouldGenerateFunctionWithFilterList2() {
		val model = '''
			type Foo:
				include boolean (1..1)
				include2 boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFoos Foo (0..*)
				
				set filteredFoos:
					foos 
						filter [ item -> include = True and item -> include2 = True ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'include2', true, 'attr', 'a'), of())
		val foo2 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'include2', false, 'attr', 'b'), of())
		val foo3 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'include2', false, 'attr', 'c'), of())
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(1, res.size);
		assertThat(res, hasItems(foo1));
	}

	@Test
	def void shouldGenerateFunctionWithFilterList3() {
		val model = '''
			type Foo:
				include boolean (1..1)
				include2 boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFoos Foo (0..*)
				
				set filteredFoos:
					foos 
						filter [ item -> include = True ]
						filter [ item -> include2 = True ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Foo.FooBuilder;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<? extends Foo> evaluate(List<? extends Foo> foos) {
						
						List<Foo.FooBuilder> filteredFoosHolder = doEvaluate(foos);
						List<Foo.FooBuilder> filteredFoos = assignOutput(filteredFoosHolder, foos);
						
						if (filteredFoos!=null) objectValidator.validateAndFailOnErorr(Foo.class, filteredFoos);
						return filteredFoos;
					}
					
					private List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
						filteredFoos = toBuilder(MapperC.of(foos)
							.filter(__item -> areEqual(__item.<Boolean>map("getInclude", _foo -> _foo.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get())
							.filter(__item -> areEqual(__item.<Boolean>map("getInclude2", _foo -> _foo.getInclude2()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).getMulti())
						;
						return filteredFoos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'include2', true, 'attr', 'a'), of())
		val foo2 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'include2', false, 'attr', 'b'), of())
		val foo3 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'include2', false, 'attr', 'c'), of())
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(1, res.size);
		assertThat(res, hasItems(foo1));
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterListAndCount() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFoosCount int (1..1)
				
				assign-output filteredFoosCount:
					foos 
						filter fooItem [ fooItem -> include = True ] 
						count
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foos 
					* @return filteredFoosCount 
					*/
					public Integer evaluate(List<? extends Foo> foos) {
						
						Integer filteredFoosCountHolder = doEvaluate(foos);
						Integer filteredFoosCount = assignOutput(filteredFoosCountHolder, foos);
						
						return filteredFoosCount;
					}
					
					private Integer assignOutput(Integer filteredFoosCount, List<? extends Foo> foos) {
						filteredFoosCount = MapperS.of(MapperC.of(foos)
							.filter(__fooItem -> areEqual(__fooItem.<Boolean>map("getInclude", _foo -> _foo.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).resultCount()).get();
						return filteredFoosCount;
					}
				
					protected abstract Integer doEvaluate(List<? extends Foo> foos);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  Integer doEvaluate(List<? extends Foo> foos) {
							return null;
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'attr', 'a'), of())
		val foo2 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'attr', 'b'), of())
		val foo3 = classes.createInstanceUsingBuilder('Foo', of('include', false, 'attr', 'c'), of())
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(Integer, fooList)
		assertEquals(2, res.intValue);
	}
	
	
	@Test
	@Disabled
	def void shouldGenerateFunctionWithNestedFilters() {
		val model = '''
			type Foo:
				bars Bar (0..*)

			type Bar:
				include boolean (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFoos Foo (0..*)
				
				set filteredFoos:
					foos 
						filter foo [ foo -> bars 
							filter bar [ bar -> include = True ] 
							count = 1 ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Foo.FooBuilder;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<? extends Foo> evaluate(List<? extends Foo> foos) {
						
						List<Foo.FooBuilder> filteredFoosHolder = doEvaluate(foos);
						List<Foo.FooBuilder> filteredFoos = assignOutput(filteredFoosHolder, foos);
						
						if (filteredFoos!=null) objectValidator.validateAndFailOnErorr(Foo.class, filteredFoos);
						return filteredFoos;
					}
					
					private List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
						filteredFoos = toBuilder(MapperC.of(foos)
							.filter(__foo -> areEqual(MapperS.of(__foo.<Bar>mapC("getBars", _foo -> _foo.getBars())
								.filter(__bar -> areEqual(__bar.<Boolean>map("getInclude", _bar -> _bar.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).resultCount()), MapperS.of(Integer.valueOf(1)), CardinalityOperator.All).get()).getMulti())
						;
						return filteredFoos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'attr', 'a'), of())
		val foo2 = classes.createInstanceUsingBuilder('Foo', of('include', true, 'attr', 'b'), of())
		val foo3 = classes.createInstanceUsingBuilder('Foo', of('include', false, 'attr', 'c'), of())
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(2, res.size);
		assertThat(res, hasItems(foo1, foo2));
	}
}
