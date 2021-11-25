package com.regnosys.rosetta.generator.java.calculation

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import java.util.List
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.IsCollectionContaining.hasItems
import static org.junit.jupiter.api.Assertions.*
import com.rosetta.model.lib.RosettaModelObject
import java.util.Map

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
		
		val foo1 = classes.createFoo(true, 'a')
		val foo2 = classes.createFoo(true, 'b')
		val foo3 = classes.createFoo(false, 'c')
		
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
		
		val foo1 = classes.createFoo(true, 'a')
		val foo2 = classes.createFoo(true, 'b')
		val foo3 = classes.createFoo(false, 'c')
		
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
			type Foo2:
				include boolean (1..1)
				include2 boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo2 (0..*)
				output:
					filteredFoos Foo2 (0..*)
				
				set filteredFoos:
					foos 
						filter [ item -> include = True and item -> include2 = True ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo2(true, true, 'a')
		val foo2 = classes.createFoo2(true, false, 'b')
		val foo3 = classes.createFoo2(true, false, 'c')
		
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
			type Foo2:
				include boolean (1..1)
				include2 boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo2 (0..*)
				output:
					filteredFoos Foo2 (0..*)
				
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
				import com.rosetta.test.model.Foo2;
				import com.rosetta.test.model.Foo2.Foo2Builder;
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
					public List<? extends Foo2> evaluate(List<? extends Foo2> foos) {
						
						List<Foo2.Foo2Builder> filteredFoosHolder = doEvaluate(foos);
						List<Foo2.Foo2Builder> filteredFoos = assignOutput(filteredFoosHolder, foos);
						
						if (filteredFoos!=null) objectValidator.validateAndFailOnErorr(Foo2.class, filteredFoos);
						return filteredFoos;
					}
					
					private List<Foo2.Foo2Builder> assignOutput(List<Foo2.Foo2Builder> filteredFoos, List<? extends Foo2> foos) {
						filteredFoos = toBuilder(MapperC.of(foos)
							.filter(__item -> areEqual(__item.<Boolean>map("getInclude", _foo2 -> _foo2.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get())
							.filter(__item -> areEqual(__item.<Boolean>map("getInclude2", _foo2 -> _foo2.getInclude2()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).getMulti())
						;
						return filteredFoos;
					}
				
					protected abstract List<Foo2.Foo2Builder> doEvaluate(List<? extends Foo2> foos);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Foo2.Foo2Builder> doEvaluate(List<? extends Foo2> foos) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo2(true, true, 'a')
		val foo2 = classes.createFoo2(true, false, 'b')
		val foo3 = classes.createFoo2(true, false, 'c')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(1, res.size);
		assertThat(res, hasItems(foo1));
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterBuiltInTypeList() {
		val model = '''			
			func FuncFoo:
			 	inputs:
			 		foos boolean (0..*)
				output:
					filteredFoos boolean (0..*)
				
				set filteredFoos:
					foos 
						filter [ item = True ]
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
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<Boolean> evaluate(List<Boolean> foos) {
						
						List<Boolean> filteredFoosHolder = doEvaluate(foos);
						List<Boolean> filteredFoos = assignOutput(filteredFoosHolder, foos);
						
						return filteredFoos;
					}
					
					private List<Boolean> assignOutput(List<Boolean> filteredFoos, List<Boolean> foos) {
						filteredFoos = MapperC.of(foos)
							.filter(__item -> areEqual(__item, MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).getMulti();
						return filteredFoos;
					}
				
					protected abstract List<Boolean> doEvaluate(List<Boolean> foos);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Boolean> doEvaluate(List<Boolean> foos) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val fooList = newArrayList
		fooList.add(true)
		fooList.add(true)
		fooList.add(false)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(2, res.size);
		assertThat(res, hasItems(true, true));
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
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo(true, 'a')
		val foo2 = classes.createFoo(true, 'b')
		val foo3 = classes.createFoo(false, 'c')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(Integer, fooList)
		assertEquals(2, res.intValue);
	}
	
	@Test
	@Disabled
	def void shouldGenerateFunctionWithFilterListAndOnlyElement() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFoosOnlyElement Foo (0..1)
				
				assign-output filteredFoosOnlyElement:
					foos 
						filter fooItem [ fooItem -> include = True ]
						only-element
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo(true, 'a')
		val foo2 = classes.createFoo(false, 'b')
		val foo3 = classes.createFoo(false, 'c')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(RosettaModelObject, fooList)
		assertEquals(foo1, res);
	}
	
	@Test
	@Disabled
	def void shouldGenerateFunctionWithFilterListAndDistinct() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFoosDistinct Foo (0..*)
				
				set filteredFoosDistinct:
					foos 
						filter fooItem [ fooItem -> include = True ]
						distinct
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo(true, 'a')
		val foo2 = classes.createFoo(true, 'b')
		val foo3 = classes.createFoo(true, 'b')
		val foo4 = classes.createFoo(false, 'c')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		fooList.add(foo4)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(1, res.size);
		assertThat(res, hasItems(foo2));
	}
	
	@Test
	def void shouldGenerateFunctionWithNestedFilters() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					filteredBars Bar (0..*)
				
				set filteredBars:
					bars 
						filter bar [ bar -> foos 
							filter foo [ foo -> include = True ] 
								count = 2 ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo(true, 'foo')
		val foo2 = classes.createFoo(false, 'foo')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1, foo2, foo2)) // count 1
		val bar2 = classes.createBar(ImmutableList.of(foo1, foo1, foo2)) // count 2
		val bar3 = classes.createBar(ImmutableList.of(foo1, foo1, foo1)) // count 3
		val bar4 = classes.createBar(ImmutableList.of(foo2, foo1, foo1)) // count 2
		
		val barList = newArrayList
		barList.add(bar1)
		barList.add(bar2)
		barList.add(bar3)
		barList.add(bar4)
		
		val res = func.invokeFunc(List, barList)
		assertEquals(2, res.size);
		assertThat(res, hasItems(bar2, bar4));
	}
	
	private def RosettaModelObject createFoo(Map<String, Class<?>> classes, boolean include, String attr) {
		classes.createInstanceUsingBuilder('Foo', of('include', include, 'attr', attr), of()) as RosettaModelObject
	}
	
	private def RosettaModelObject createFoo2(Map<String, Class<?>> classes, boolean include, boolean include2, String attr) {
		classes.createInstanceUsingBuilder('Foo2', of('include', include, 'include2', include2, 'attr', attr), of()) as RosettaModelObject
	}
	
	private def RosettaModelObject createBar(Map<String, Class<?>> classes, List<RosettaModelObject> foos) {
		classes.createInstanceUsingBuilder('Bar', of(), of('foos', foos)) as RosettaModelObject
	}
}
