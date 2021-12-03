package com.regnosys.rosetta.generator.java.calculation

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.RosettaModelObject
import java.util.List
import java.util.Map
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.core.IsCollectionContaining.hasItems
import static org.junit.jupiter.api.Assertions.*

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
							.filterItem(__item -> areEqual(__item.<Boolean>map("getInclude", _foo -> _foo.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).getMulti())
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
							.filterItem(__fooItem -> areEqual(__fooItem.<Boolean>map("getInclude", _foo -> _foo.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).getMulti())
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
							.filterItem(__item -> areEqual(__item.<Boolean>map("getInclude", _foo2 -> _foo2.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get())
							.filterItem(__item -> areEqual(__item.<Boolean>map("getInclude2", _foo2 -> _foo2.getInclude2()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).getMulti())
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
							.filterItem(__item -> areEqual(__item, MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()).getMulti();
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
	def void shouldGenerateFunctionWithFilterListAndInputParameter() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
			 		test boolean (1..1)
				output:
					filteredFoos Foo (0..*)
				
				set filteredFoos:
					foos 
						filter [ item -> include = test ]
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
		
		val res = func.invokeFunc(List, fooList, true)
		assertEquals(2, res.size);
		assertThat(res, hasItems(foo1, foo2));
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
	def void shouldGenerateFunctionWithFilterListAndFuncCalls() {
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
						filter [ FuncFooTest( item ) ]
						filter [ FuncFooTest2( item ) ]
			
			func FuncFooTest:
			 	inputs:
			 		foo Foo (1..1)
				output:
					result boolean (0..1)
				
				set result:
					foo -> include
			
			func FuncFooTest2:
			 	inputs:
			 		foo Foo (1..1)
				output:
					result boolean (0..1)
				
				set result:
					foo -> include
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
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(2, res.size);
		assertThat(res, hasItems(foo1, foo2));
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterListAndAliasParameter() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
			 		test boolean (1..1)
				output:
					filteredFoos Foo (0..*)
				
				alias testAlias:
					test
				
				set filteredFoos:
					foos 
						filter [ item -> include = testAlias ]
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
		
		val res = func.invokeFunc(List, fooList, true)
		assertEquals(2, res.size);
		assertThat(res, hasItems(foo1, foo2));
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterAndAlias() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFooAttrs string (0..*)
				
				alias filteredFoosAlias:
					foos 
						filter [ item -> include = True ]
				
				set filteredFooAttrs:
					filteredFoosAlias -> attr
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
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(2, res.size);
		assertThat(res, hasItems('a', 'b'));
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterListAliasAndOnlyElement() {
		val model = '''
			type Foo2:
				include boolean (0..1)
				include2 boolean (0..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo2 (0..*)
			 		test boolean (1..1)
			 		test2 boolean (1..1)
				output:
					foo Foo2 (0..1)
				
				alias filteredFoos:
					foos 
						filter [ if test exists then item -> include = test else True ]
						filter [ if test2 exists then item -> include2 = test2 else True ]
				
				assign-output foo:
					filteredFoos only-element
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo2(true, true, 'a')
		val foo2 = classes.createFoo2(true, false, 'b')
		val foo3 = classes.createFoo2(false, true, 'c')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(RosettaModelObject, fooList, true, true)
		assertEquals(foo1, res);
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterListAliasAndOnlyElement2() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bar Bar (1..1)
				output:
					foos Foo (0..*)
				
				set foos:
					bar -> foos 
						map [ if item -> include = True then Create_Foo( item -> include, item -> attr + "_bar" ) else item ]
			
			func Create_Foo:
				inputs:
					include boolean (1..1)
					attr string (1..1)
				output:
					foo Foo (1..1)
				
				assign-output foo -> include: include
				assign-output foo -> attr: attr
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.expression.MapperMaths;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Foo.FooBuilder;
				import com.rosetta.test.model.functions.Create_Foo;
				import java.util.Arrays;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
					
					// RosettaFunction dependencies
					//
					@Inject protected Create_Foo create_Foo;
				
					/**
					* @param bar 
					* @return foos 
					*/
					public List<? extends Foo> evaluate(Bar bar) {
						
						List<Foo.FooBuilder> foosHolder = doEvaluate(bar);
						List<Foo.FooBuilder> foos = assignOutput(foosHolder, bar);
						
						if (foos!=null) objectValidator.validateAndFailOnErorr(Foo.class, foos);
						return foos;
					}
					
					private List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> foos, Bar bar) {
						foos = toBuilder(MapperS.of(bar).<Foo>mapC("getFoos", _bar -> _bar.getFoos())
							.mapItem(/*MapperS<? extends Foo>*/ __item -> (MapperS<? extends Foo>) com.rosetta.model.lib.mapper.MapperUtils.fromDataType(() -> {
							if (areEqual(__item.<Boolean>map("getInclude", _foo -> _foo.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get()) {
								return MapperS.of(create_Foo.evaluate(__item.<Boolean>map("getInclude", _foo -> _foo.getInclude()).get(), MapperMaths.<String, String, String>add(__item.<String>map("getAttr", _foo -> _foo.getAttr()), MapperS.of("_bar")).get()));
							}
							else {
								return __item;
							}
							})).getMulti())
						;
						return foos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(Bar bar);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Foo.FooBuilder> doEvaluate(Bar bar) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo(true, 'foo')
		val foo2 = classes.createFoo(false, 'foo')
		
		val bar = classes.createBar(ImmutableList.of(foo1, foo2, foo2))
		
		val res = func.invokeFunc(List, bar)
		assertEquals(3, res.size);
		
		val expectedNewFoo = classes.createFoo(true, 'foo_bar')
		
		assertThat(res, hasItems(expectedNewFoo, foo2));
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterListAliasAndOnlyElement3() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bar Bar (1..1)
				output:
					updatedBar Bar (1..1)
				
				assign-output updatedBar -> foos:
					bar -> foos 
						map [ if item -> include = True then Create_Foo( item -> include, Create_Attr( item -> attr, "_bar" ) ) else item ]
			
			func Create_Foo:
				inputs:
					include boolean (1..1)
					attr string (1..1)
				output:
					foo Foo (1..1)
				
				assign-output foo -> include: include
				assign-output foo -> attr: attr
			
			func Create_Attr:
				inputs:
					s1 string (1..1)
					s2 string (1..1)
				output:
					out string (1..1)
				assign-output out:
					s1 + s2
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo(true, 'foo')
		val foo2 = classes.createFoo(false, 'foo')
		
		val bar = classes.createBar(ImmutableList.of(foo1, foo2, foo2))
		
		val res = func.invokeFunc(RosettaModelObject, bar)
		
		val expectedBar = classes.createBar(ImmutableList.of(classes.createFoo(true, 'foo_bar'), foo2, foo2))
		
		assertEquals(expectedBar, res);
	}
	
	@Test
	@Disabled // Add syntax support
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
	@Disabled // Add syntax support
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
	@Disabled // Add syntax support
	def void shouldGenerateFunctionWithFilterListAndPath() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					filteredFooAttr string (0..*)
				
				set filteredFooAttr:
					foos 
						filter fooItem [ fooItem -> include = True ]
							-> attr
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
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(2, res.size);
		assertThat(res, hasItems('a', 'b'));
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
	
	@Test
	def void shouldGenerateFunctionWithMapList() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					strings string (0..*)
				
				set strings:
					foos 
						map [ item -> attr ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foos 
					* @return strings 
					*/
					public List<String> evaluate(List<? extends Foo> foos) {
						
						List<String> stringsHolder = doEvaluate(foos);
						List<String> strings = assignOutput(stringsHolder, foos);
						
						return strings;
					}
					
					private List<String> assignOutput(List<String> strings, List<? extends Foo> foos) {
						strings = MapperC.of(foos)
							.mapItem(/*MapperS<? extends Foo>*/ __item -> (MapperS<String>) __item.<String>map("getAttr", _foo -> _foo.getAttr())).getMulti();
						return strings;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Foo> foos);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<String> doEvaluate(List<? extends Foo> foos) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(3, res.size);
		assertThat(res, hasItems('a', 'b', 'c'));
	}
	
	@Test
	def void shouldGenerateFunctionWithMapList2() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					strings string (0..*)
				
				set strings:
					foos 
						map foo [ foo -> attr ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(3, res.size);
		assertThat(res, hasItems('a', 'b', 'c'));
	}
	
	@Test
	def void shouldGenerateFunctionWithMapListOfListThenMapToListOfCounts() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					fooCounts int (0..*)
				
				set fooCounts:
					bars 
						map bar [ bar -> foos ]
						map fooListItem [ fooListItem count ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param bars 
					* @return fooCounts 
					*/
					public List<Integer> evaluate(List<? extends Bar> bars) {
						
						List<Integer> fooCountsHolder = doEvaluate(bars);
						List<Integer> fooCounts = assignOutput(fooCountsHolder, bars);
						
						return fooCounts;
					}
					
					private List<Integer> assignOutput(List<Integer> fooCounts, List<? extends Bar> bars) {
						fooCounts = MapperC.of(bars)
							.mapItemToList((/*MapperS<? extends Bar>*/ __bar) -> (MapperC<? extends Foo>) __bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos()))
							.mapListToItem((/*MapperC<? extends Foo>*/ __fooListItem) -> (MapperS<Integer>) MapperS.of(__fooListItem.resultCount()))
						.getMulti();
						return fooCounts;
					}
				
					protected abstract List<Integer> doEvaluate(List<? extends Bar> bars);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Integer> doEvaluate(List<? extends Bar> bars) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1, foo2, foo3))
		val bar2 = classes.createBar(ImmutableList.of(foo1, foo2))
		val bar3 = classes.createBar(ImmutableList.of(foo1))
		
		val res = func.invokeFunc(List, ImmutableList.of(bar1, bar2, bar3))
		assertEquals(3, res.size);
		assertThat(res, hasItems(3, 2, 1));
	}
	
	@Test
	def void shouldGenerateFunctionWithMapListOfListThenMapToListOfCounts2() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					fooCounts int (0..*)
				
				set fooCounts:
					bars 
						map [ item -> foos ]
						map [ item count ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1, foo2, foo3))
		val bar2 = classes.createBar(ImmutableList.of(foo1, foo2))
		val bar3 = classes.createBar(ImmutableList.of(foo1))
		
		val res = func.invokeFunc(List, ImmutableList.of(bar1, bar2, bar3))
		assertEquals(3, res.size);
		assertThat(res, hasItems(3, 2, 1));
	}
	
	@Test
	def void shouldGenerateFunctionWithMapListOfListsThenFlatten() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					foos Foo (0..*)
				
				set foos:
					bars 
						map bar [ bar -> foos ]
						flatten
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.Foo.FooBuilder;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param bars 
					* @return foos 
					*/
					public List<? extends Foo> evaluate(List<? extends Bar> bars) {
						
						List<Foo.FooBuilder> foosHolder = doEvaluate(bars);
						List<Foo.FooBuilder> foos = assignOutput(foosHolder, bars);
						
						if (foos!=null) objectValidator.validateAndFailOnErorr(Foo.class, foos);
						return foos;
					}
					
					private List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> foos, List<? extends Bar> bars) {
						foos = toBuilder(MapperC.of(bars)
							.mapItemToList((/*MapperS<? extends Bar>*/ __bar) -> (MapperC<? extends Foo>) __bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos()))
							.flattenList().getMulti())
						;
						return foos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Bar> bars);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Foo.FooBuilder> doEvaluate(List<? extends Bar> bars) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		val foo5 = classes.createFoo('e')
		val foo6 = classes.createFoo('f')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1, foo2, foo3))
		val bar2 = classes.createBar(ImmutableList.of(foo4, foo5))
		val bar3 = classes.createBar(ImmutableList.of(foo6))
		
		val res = func.invokeFunc(List, ImmutableList.of(bar1, bar2, bar3))
		assertEquals(6, res.size);
		assertThat(res, hasItems(foo1, foo2, foo3, foo4, foo5, foo6));
	}
	
	@Test
	def void shouldGenerateFunctionWithMapListOfListsThenFlatten2() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					foos Foo (0..*)
				
				set foos:
					bars 
						map [ item -> foos ]
						flatten
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		val foo5 = classes.createFoo('e')
		val foo6 = classes.createFoo('f')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1, foo2, foo3))
		val bar2 = classes.createBar(ImmutableList.of(foo4, foo5))
		val bar3 = classes.createBar(ImmutableList.of(foo6))
		
		val res = func.invokeFunc(List, ImmutableList.of(bar1, bar2, bar3))
		assertEquals(6, res.size);
		assertThat(res, hasItems(foo1, foo2, foo3, foo4, foo5, foo6));
	}
	
	@Test
	def void shouldGenerateFunctionWithMapListCount() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					fooCounts int (0..*)
				
				set fooCounts:
					bars 
						map [ item -> foos count ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1, foo2, foo3))
		val bar2 = classes.createBar(ImmutableList.of(foo1, foo2))
		val bar3 = classes.createBar(ImmutableList.of(foo1))
		
		val res = func.invokeFunc(List, ImmutableList.of(bar1, bar2, bar3))
		assertEquals(3, res.size);
		assertThat(res, hasItems(3, 2, 1));
	}
	
	@Test
	def void shouldGenerateFunctionWithMapListCount2() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					fooCounts int (0..*)
				
				set fooCounts:
					bars 
						map bar [ bar -> foos count ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1, foo2, foo3))
		val bar2 = classes.createBar(ImmutableList.of(foo1, foo2))
		val bar3 = classes.createBar(ImmutableList.of(foo1))
		
		val res = func.invokeFunc(List, ImmutableList.of(bar1, bar2, bar3))
		assertEquals(3, res.size);
		assertThat(res, hasItems(3, 2, 1));
	}
	
	@Test
	def void shouldGenerateFunctionWithNestedMaps() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					updatedBars Bar (0..*)
				
				set updatedBars:
					bars 
						map bar [ bar -> foos 
							map foo [ NewFoo( foo -> attr + "_bar" ) ]
						]
						map updatedFoos [ NewBar( updatedFoos ) ]
			
			func NewBar:
			 	inputs:
			 		foos Foo (0..*)
				output:
					bar Bar (1..1)
				
				set bar -> foos:
					foos
			
			func NewFoo:
			 	inputs:
			 		attr string (1..1)
				output:
					foo Foo (0..1)
				
				assign-output foo -> attr:
					attr
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.google.inject.Inject;
				import com.rosetta.model.lib.expression.MapperMaths;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.lib.validation.ModelObjectValidator;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Bar.BarBuilder;
				import com.rosetta.test.model.Foo;
				import com.rosetta.test.model.functions.NewBar;
				import com.rosetta.test.model.functions.NewFoo;
				import java.util.Arrays;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
					
					// RosettaFunction dependencies
					//
					@Inject protected NewBar newBar;
					@Inject protected NewFoo newFoo;
				
					/**
					* @param bars 
					* @return updatedBars 
					*/
					public List<? extends Bar> evaluate(List<? extends Bar> bars) {
						
						List<Bar.BarBuilder> updatedBarsHolder = doEvaluate(bars);
						List<Bar.BarBuilder> updatedBars = assignOutput(updatedBarsHolder, bars);
						
						if (updatedBars!=null) objectValidator.validateAndFailOnErorr(Bar.class, updatedBars);
						return updatedBars;
					}
					
					private List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> updatedBars, List<? extends Bar> bars) {
						updatedBars = toBuilder(MapperC.of(bars)
							.mapItemToList((/*MapperS<? extends Bar>*/ __bar) -> (MapperC<? extends Foo>) __bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos())
								.mapItem(/*MapperS<? extends Foo>*/ __foo -> (MapperS<? extends Foo>) MapperS.of(newFoo.evaluate(MapperMaths.<String, String, String>add(__foo.<String>map("getAttr", _foo -> _foo.getAttr()), MapperS.of("_bar")).get()))))
							.mapListToItem((/*MapperC<? extends Foo>*/ __updatedFoos) -> (MapperS<? extends Bar>) MapperS.of(newBar.evaluate(__updatedFoos.getMulti())))
						.getMulti())
						;
						return updatedBars;
					}
				
					protected abstract List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars) {
							return Arrays.asList();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1, foo2, foo3))
		val bar2 = classes.createBar(ImmutableList.of(foo1, foo2))
		val bar3 = classes.createBar(ImmutableList.of(foo1))
		
		val res = func.invokeFunc(List, ImmutableList.of(bar1, bar2, bar3))
		assertEquals(3, res.size);
		
		val expectedFoo1 = classes.createFoo('a_bar')
		val expectedFoo2 = classes.createFoo('b_bar')
		val expectedFoo3 = classes.createFoo('c_bar')
		
		val expectedBar1 = classes.createBar(ImmutableList.of(expectedFoo1, expectedFoo2, expectedFoo3))
		val expectedBar2 = classes.createBar(ImmutableList.of(expectedFoo1, expectedFoo2))
		val expectedBar3 = classes.createBar(ImmutableList.of(expectedFoo1))
		
		assertThat(res, hasItems(expectedBar1, expectedBar2, expectedBar3));
	}
	
	
	@Test
	def void shouldGenerateFunctionWithMapListModifyItemFunc() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					updatedFoos Foo (0..*)
				
				set updatedFoos:
					foos 
						map [ NewFoo( item -> attr + "_1" ) ]
			
			func NewFoo:
			 	inputs:
			 		attr string (1..1)
				output:
					foo Foo (0..1)
				
				assign-output foo -> attr:
					attr
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(3, res.size);
		
		val expectedFoo1 = classes.createFoo('a_1')
		val expectedFoo2 = classes.createFoo('b_1')
		val expectedFoo3 = classes.createFoo('c_1')
		
		assertThat(res, hasItems(expectedFoo1, expectedFoo2, expectedFoo3));
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterThenMap() {
		val model = '''
			type Foo:
				include boolean (1..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					newFoos string (0..*)
				
				set newFoos:
					foos 
						filter [ item -> include = True ]
						map [ item -> attr ]

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
					* @return newFoos 
					*/
					public List<String> evaluate(List<? extends Foo> foos) {
						
						List<String> newFoosHolder = doEvaluate(foos);
						List<String> newFoos = assignOutput(newFoosHolder, foos);
						
						return newFoos;
					}
					
					private List<String> assignOutput(List<String> newFoos, List<? extends Foo> foos) {
						newFoos = MapperC.of(foos)
							.filterItem(__item -> areEqual(__item.<Boolean>map("getInclude", _foo -> _foo.getInclude()), MapperS.of(Boolean.valueOf(true)), CardinalityOperator.All).get())
							.mapItem(/*MapperS<? extends Foo>*/ __item -> (MapperS<String>) __item.<String>map("getAttr", _foo -> _foo.getAttr())).getMulti();
						return newFoos;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Foo> foos);
					
					public static final class FuncFooDefault extends FuncFoo {
						@Override
						protected  List<String> doEvaluate(List<? extends Foo> foos) {
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
		assertThat(res, hasItems('a', 'b'));
	}
	
	private def RosettaModelObject createFoo(Map<String, Class<?>> classes, String attr) {
		classes.createInstanceUsingBuilder('Foo', of('attr', attr), of()) as RosettaModelObject
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
