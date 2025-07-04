package com.regnosys.rosetta.generator.java.expression

import com.google.common.collect.ImmutableList
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.records.Date
import java.math.BigDecimal
import java.util.List
import java.util.Map
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static com.google.common.collect.ImmutableMap.*
import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.jupiter.api.Assertions.*
import javax.inject.Inject

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
class ListOperationTest {

	@Inject extension FunctionGeneratorHelper
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
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<? extends Foo> evaluate(List<? extends Foo> foos) {
						List<Foo.FooBuilder> filteredFoosBuilder = doEvaluate(foos);
						
						final List<? extends Foo> filteredFoos;
						if (filteredFoosBuilder == null) {
							filteredFoos = null;
						} else {
							filteredFoos = filteredFoosBuilder.stream().map(Foo::build).collect(Collectors.toList());
							objectValidator.validate(Foo.class, filteredFoos);
						}
						
						return filteredFoos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							List<Foo.FooBuilder> filteredFoos = new ArrayList<>();
							return assignOutput(filteredFoos, foos);
						}
						
						protected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
							filteredFoos = toBuilder(MapperC.<Foo>of(foos)
								.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).get()).getMulti());
							
							return Optional.ofNullable(filteredFoos)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
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
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<? extends Foo> evaluate(List<? extends Foo> foos) {
						List<Foo.FooBuilder> filteredFoosBuilder = doEvaluate(foos);
						
						final List<? extends Foo> filteredFoos;
						if (filteredFoosBuilder == null) {
							filteredFoos = null;
						} else {
							filteredFoos = filteredFoosBuilder.stream().map(Foo::build).collect(Collectors.toList());
							objectValidator.validate(Foo.class, filteredFoos);
						}
						
						return filteredFoos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							List<Foo.FooBuilder> filteredFoos = new ArrayList<>();
							return assignOutput(filteredFoos, foos);
						}
						
						protected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
							filteredFoos = toBuilder(MapperC.<Foo>of(foos)
								.filterItemNullSafe(fooItem -> areEqual(fooItem.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).get()).getMulti());
							
							return Optional.ofNullable(filteredFoos)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
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
						then filter [ item -> include2 = True ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo2;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<? extends Foo2> evaluate(List<? extends Foo2> foos) {
						List<Foo2.Foo2Builder> filteredFoosBuilder = doEvaluate(foos);
						
						final List<? extends Foo2> filteredFoos;
						if (filteredFoosBuilder == null) {
							filteredFoos = null;
						} else {
							filteredFoos = filteredFoosBuilder.stream().map(Foo2::build).collect(Collectors.toList());
							objectValidator.validate(Foo2.class, filteredFoos);
						}
						
						return filteredFoos;
					}
				
					protected abstract List<Foo2.Foo2Builder> doEvaluate(List<? extends Foo2> foos);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Foo2.Foo2Builder> doEvaluate(List<? extends Foo2> foos) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							List<Foo2.Foo2Builder> filteredFoos = new ArrayList<>();
							return assignOutput(filteredFoos, foos);
						}
						
						protected List<Foo2.Foo2Builder> assignOutput(List<Foo2.Foo2Builder> filteredFoos, List<? extends Foo2> foos) {
							final MapperC<Foo2> thenArg = MapperC.<Foo2>of(foos)
								.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude", foo2 -> foo2.getInclude()), MapperS.of(true), CardinalityOperator.All).get());
							filteredFoos = toBuilder(thenArg
								.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude2", foo2 -> foo2.getInclude2()), MapperS.of(true), CardinalityOperator.All).get()).getMulti());
							
							return Optional.ofNullable(filteredFoos)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
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
	def void shouldGenerateFunctionWithFilterListWithMetaData() {
		val model = '''
			type FooWithScheme:
				attr string (1..1)
					[metadata scheme]
			
			func FuncFoo:
			 	inputs:
			 		foos FooWithScheme (0..*)
				output:
					filteredFoos FooWithScheme (0..*)
				
				set filteredFoos:
					foos 
						filter [ item -> attr -> scheme = "foo-scheme" ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.model.metafields.FieldWithMetaString;
				import com.rosetta.test.model.FooWithScheme;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<? extends FooWithScheme> evaluate(List<? extends FooWithScheme> foos) {
						List<FooWithScheme.FooWithSchemeBuilder> filteredFoosBuilder = doEvaluate(foos);
						
						final List<? extends FooWithScheme> filteredFoos;
						if (filteredFoosBuilder == null) {
							filteredFoos = null;
						} else {
							filteredFoos = filteredFoosBuilder.stream().map(FooWithScheme::build).collect(Collectors.toList());
							objectValidator.validate(FooWithScheme.class, filteredFoos);
						}
						
						return filteredFoos;
					}
				
					protected abstract List<FooWithScheme.FooWithSchemeBuilder> doEvaluate(List<? extends FooWithScheme> foos);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<FooWithScheme.FooWithSchemeBuilder> doEvaluate(List<? extends FooWithScheme> foos) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							List<FooWithScheme.FooWithSchemeBuilder> filteredFoos = new ArrayList<>();
							return assignOutput(filteredFoos, foos);
						}
						
						protected List<FooWithScheme.FooWithSchemeBuilder> assignOutput(List<FooWithScheme.FooWithSchemeBuilder> filteredFoos, List<? extends FooWithScheme> foos) {
							filteredFoos = toBuilder(MapperC.<FooWithScheme>of(foos)
								.filterItemNullSafe(item -> areEqual(item.<FieldWithMetaString>map("getAttr", fooWithScheme -> fooWithScheme.getAttr()).map("getMeta", a->a.getMeta()).map("getScheme", a->a.getScheme()), MapperS.of("foo-scheme"), CardinalityOperator.All).get()).getMulti());
							
							return Optional.ofNullable(filteredFoos)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFooWithScheme('a', 'foo-scheme')
		val foo2 = classes.createFooWithScheme('b', 'foo-scheme')
		val foo3 = classes.createFooWithScheme('c', 'bar-scheme')
		

		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(2, res.size);
		assertThat(res, hasItems(foo1, foo2));
	}
	
	@Test
	@Disabled
	def void shouldGenerateFunctionWithFilterListWithMetaData2() {
		val model = '''
			type FooWithScheme:
				attr string (1..1)
					[metadata scheme]
			
			func FuncFoo:
			 	inputs:
			 		foos FooWithScheme (0..*)
				output:
					strings string (0..*)
				
				set strings:
					foos 
						map [ item -> attr ]
						filter [ item -> scheme = "foo-scheme" ]
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
				import com.rosetta.model.metafields.FieldWithMetaString;
				import com.rosetta.test.model.FooWithScheme;
				import java.util.ArrayList;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foos 
					* @return strings 
					*/
					public List<String> evaluate(List<? extends FooWithScheme> foos) {
						List<String> stringsHolder = doEvaluate(foos);
						List<String> strings = assignOutput(stringsHolder, foos);
						
						return strings;
					}
					
					private List<String> assignOutput(List<String> strings, List<? extends FooWithScheme> foos) {
						strings = MapperC.of(foos)
							.mapItem(/*MapperS<? extends FooWithScheme>*/ __item -> (MapperS<String>) __item.<FieldWithMetaString>map("getAttr", _fooWithScheme -> _fooWithScheme.getAttr()).<String>map("getValue", _f->_f.getValue())).getMulti();
						return strings;
					}
				
					protected abstract List<String> doEvaluate(List<? extends FooWithScheme> foos);
					
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(List<? extends FooWithScheme> foos) {
							return new ArrayList<>();
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFooWithScheme('a', 'foo-scheme')
		val foo2 = classes.createFooWithScheme('b', 'foo-scheme')
		val foo3 = classes.createFooWithScheme('c', 'bar-scheme')
		

		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, fooList)
		assertEquals(2, res.size);
		assertThat(res, hasItems(foo1, foo2));
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
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foos 
					* @return filteredFoos 
					*/
					public List<Boolean> evaluate(List<Boolean> foos) {
						List<Boolean> filteredFoos = doEvaluate(foos);
						
						return filteredFoos;
					}
				
					protected abstract List<Boolean> doEvaluate(List<Boolean> foos);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Boolean> doEvaluate(List<Boolean> foos) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							List<Boolean> filteredFoos = new ArrayList<>();
							return assignOutput(filteredFoos, foos);
						}
						
						protected List<Boolean> assignOutput(List<Boolean> filteredFoos, List<Boolean> foos) {
							filteredFoos = MapperC.<Boolean>of(foos)
								.filterItemNullSafe(item -> areEqual(item, MapperS.of(true), CardinalityOperator.All).get()).getMulti();
							
							return filteredFoos;
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
				
				set filteredFoosCount:
					foos 
						filter fooItem [ fooItem -> include = True ] 
						then count
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
						then filter [ FuncFooTest2( item ) ]
			
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
	def void shouldGenerateFunctionWithMultipleFilterList() {
		val model = '''
			type Foo2:
				include boolean (0..1)
				include2 boolean (0..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo2 (0..*)
			 		test boolean (0..1)
			 		test2 boolean (0..1)
			 		test3 boolean (0..1)
				output:
					foo Foo2 (0..1)
				
				alias filteredFoos:
					foos 
						filter a [ if test exists then a -> include = test else True ]
						then filter b [ if test2 exists then b -> include2 = test2 else True ]
						then filter c [ if test3 exists then c -> include2 = test3 else True ]
				
				set foo:
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
		
		val res = func.invokeFunc(RosettaModelObject, fooList, true, true, true)
		assertEquals(foo1, res);
	}
	
	@Test
	def void shouldGenerateFunctionWithMultipleFilterList2() {
		val model = '''
			type Foo2:
				include boolean (0..1)
				include2 boolean (0..1)
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo2 (0..*)
			 		test boolean (0..1)
			 		test2 boolean (0..1)
			 		test3 boolean (0..1)
				output:
					foo Foo2 (0..1)
				
				alias filteredFoos:
					foos 
						filter [ if test exists then item -> include = test else True ]
						then filter [ if test2 exists then item -> include2 = test2 else True ]
						then filter [ if test3 exists then item -> include2 = test3 else True ]
				
				set foo:
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
		
		val res = func.invokeFunc(RosettaModelObject, fooList, true, true, true)
		assertEquals(foo1, res);
	}
	
	@Test
	def void shouldGenerateFunctionWithFilterListAliasAndOnlyElement() {
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
						extract [ if item -> include = True then Foo { include: include, attr: attr + "_bar" } else item ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.expression.MapperMaths;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param bar 
					* @return foos 
					*/
					public List<? extends Foo> evaluate(Bar bar) {
						List<Foo.FooBuilder> foosBuilder = doEvaluate(bar);
						
						final List<? extends Foo> foos;
						if (foosBuilder == null) {
							foos = null;
						} else {
							foos = foosBuilder.stream().map(Foo::build).collect(Collectors.toList());
							objectValidator.validate(Foo.class, foos);
						}
						
						return foos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(Bar bar);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Foo.FooBuilder> doEvaluate(Bar bar) {
							List<Foo.FooBuilder> foos = new ArrayList<>();
							return assignOutput(foos, bar);
						}
						
						protected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> foos, Bar bar) {
							foos = toBuilder(MapperS.of(bar).<Foo>mapC("getFoos", _bar -> _bar.getFoos())
								.mapItem(item -> {
									if (areEqual(item.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).getOrDefault(false)) {
										return MapperS.of(Foo.builder()
											.setInclude(item.<Boolean>map("getInclude", foo -> foo.getInclude()).get())
											.setAttr(MapperMaths.<String, String, String>add(item.<String>map("getAttr", foo -> foo.getAttr()), MapperS.of("_bar")).get())
											.build());
									}
									return item;
								}).getMulti());
							
							return Optional.ofNullable(foos)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
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
					updatedBar Bar (1..1)
				
				add updatedBar -> foos:
					bar -> foos 
						extract [ if item -> include = True then Create_Foo( item -> include, Create_Attr( item -> attr, "_bar" ) ) else item ]
			
			func Create_Foo:
				inputs:
					include boolean (1..1)
					attr string (1..1)
				output:
					foo Foo (1..1)
				
				set foo -> include: include
				set foo -> attr: attr
			
			func Create_Attr:
				inputs:
					s1 string (1..1)
					s2 string (1..1)
				output:
					out string (1..1)
				set out:
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
				
				set filteredFoosOnlyElement:
					foos 
						filter fooItem [ fooItem -> include = True ]
						then only-element
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
						then distinct
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
		assertEquals(2, res.size);
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
								then count = 2 ]
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
	def void shouldGenerateFunctionWithExtractList() {
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
						extract [ item -> attr ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foos 
					* @return strings 
					*/
					public List<String> evaluate(List<? extends Foo> foos) {
						List<String> strings = doEvaluate(foos);
						
						return strings;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Foo> foos);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(List<? extends Foo> foos) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							List<String> strings = new ArrayList<>();
							return assignOutput(strings, foos);
						}
						
						protected List<String> assignOutput(List<String> strings, List<? extends Foo> foos) {
							strings = MapperC.<Foo>of(foos)
								.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
							
							return strings;
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
	def void shouldGenerateFunctionWithExtractList2() {
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
						extract foo [ foo -> attr ]
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
	def void shouldGenerateFunctionWithExtractListOfListThenExtractToListOfCounts() {
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
						extract bar [ bar -> foos ]
						then extract fooListItem [ fooListItem count ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperListOfLists;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param bars 
					* @return fooCounts 
					*/
					public List<Integer> evaluate(List<? extends Bar> bars) {
						List<Integer> fooCounts = doEvaluate(bars);
						
						return fooCounts;
					}
				
					protected abstract List<Integer> doEvaluate(List<? extends Bar> bars);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Integer> doEvaluate(List<? extends Bar> bars) {
							if (bars == null) {
								bars = Collections.emptyList();
							}
							List<Integer> fooCounts = new ArrayList<>();
							return assignOutput(fooCounts, bars);
						}
						
						protected List<Integer> assignOutput(List<Integer> fooCounts, List<? extends Bar> bars) {
							final MapperListOfLists<Foo> thenArg = MapperC.<Bar>of(bars)
								.mapItemToList(bar -> bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos()));
							fooCounts = thenArg
								.mapListToItem(fooListItem -> MapperS.of(fooListItem.resultCount())).getMulti();
							
							return fooCounts;
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
	def void shouldGenerateFunctionWithExtractListOfListThenExtractToListOfCounts2() {
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
						extract [ item -> foos ]
						then extract [ item count ]
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
	def void shouldGenerateFunctionWithExtractListOfListThenFilterOnCount() {
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
						extract [ item -> foos ]
						then filter [ item count > 1 ]
						then extract [ item count ]
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
		assertEquals(2, res.size);
		assertThat(res, hasItems(3, 2));
	}
	
	@Test
	def void shouldGenerateFunctionWithMapListOfListThenFilterOnCount2() {
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
						extract a [ a -> foos ]
						then filter b [ b count > 1 ]
						then extract c [ c count ]
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
		assertEquals(2, res.size);
		assertThat(res, hasItems(3, 2));
	}
	
	@Test
	def void shouldGenerateFunctionWithExtractListOfListsThenFlatten() {
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
						extract bar [ bar -> foos ]
						then flatten
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperListOfLists;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param bars 
					* @return foos 
					*/
					public List<? extends Foo> evaluate(List<? extends Bar> bars) {
						List<Foo.FooBuilder> foosBuilder = doEvaluate(bars);
						
						final List<? extends Foo> foos;
						if (foosBuilder == null) {
							foos = null;
						} else {
							foos = foosBuilder.stream().map(Foo::build).collect(Collectors.toList());
							objectValidator.validate(Foo.class, foos);
						}
						
						return foos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Bar> bars);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Foo.FooBuilder> doEvaluate(List<? extends Bar> bars) {
							if (bars == null) {
								bars = Collections.emptyList();
							}
							List<Foo.FooBuilder> foos = new ArrayList<>();
							return assignOutput(foos, bars);
						}
						
						protected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> foos, List<? extends Bar> bars) {
							final MapperListOfLists<Foo> thenArg = MapperC.<Bar>of(bars)
								.mapItemToList(bar -> bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos()));
							foos = toBuilder(thenArg
								.flattenList().getMulti());
							
							return Optional.ofNullable(foos)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
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
	def void shouldGenerateFunctionWithExtractListOfListsThenFlatten2() {
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
						extract [ item -> foos ]
						then flatten
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
	def void shouldGenerateFunctionWithExtractListOfListsThenFlatten3() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					attrs string (0..*)
				
				set attrs:
					bars 
						extract [ item -> foos ]
						then flatten
						then extract [ item -> attr ]
		'''
		val code = model.generateCode
				val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperListOfLists;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param bars 
					* @return attrs 
					*/
					public List<String> evaluate(List<? extends Bar> bars) {
						List<String> attrs = doEvaluate(bars);
						
						return attrs;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Bar> bars);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(List<? extends Bar> bars) {
							if (bars == null) {
								bars = Collections.emptyList();
							}
							List<String> attrs = new ArrayList<>();
							return assignOutput(attrs, bars);
						}
						
						protected List<String> assignOutput(List<String> attrs, List<? extends Bar> bars) {
							final MapperListOfLists<Foo> thenArg0 = MapperC.<Bar>of(bars)
								.mapItemToList(item -> item.<Foo>mapC("getFoos", bar -> bar.getFoos()));
							final MapperC<Foo> thenArg1 = thenArg0
								.flattenList();
							attrs = thenArg1
								.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
							
							return attrs;
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
		assertThat(res, hasItems('a', 'b', 'c', 'd', 'e', 'f'));
	}
	
	@Test
	def void shouldGenerateFunctionWithExtractListCount() {
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
						extract [ item -> foos count ]
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
						extract bar [ bar -> foos count ]
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
	def void shouldGenerateFunctionWithNestedExtracts() {
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
						extract bar [ bar -> foos 
							extract foo [ NewFoo( foo -> attr + "_bar" ) ]
						]
						then extract updatedFoos [ NewBar( updatedFoos ) ]
			
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
				
				set foo -> attr:
					attr
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.MapperMaths;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperListOfLists;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;
				
				
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
						List<Bar.BarBuilder> updatedBarsBuilder = doEvaluate(bars);
						
						final List<? extends Bar> updatedBars;
						if (updatedBarsBuilder == null) {
							updatedBars = null;
						} else {
							updatedBars = updatedBarsBuilder.stream().map(Bar::build).collect(Collectors.toList());
							objectValidator.validate(Bar.class, updatedBars);
						}
						
						return updatedBars;
					}
				
					protected abstract List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars) {
							if (bars == null) {
								bars = Collections.emptyList();
							}
							List<Bar.BarBuilder> updatedBars = new ArrayList<>();
							return assignOutput(updatedBars, bars);
						}
						
						protected List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> updatedBars, List<? extends Bar> bars) {
							final MapperListOfLists<Foo> thenArg = MapperC.<Bar>of(bars)
								.mapItemToList(bar -> bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos())
									.mapItem(foo -> MapperS.of(newFoo.evaluate(MapperMaths.<String, String, String>add(foo.<String>map("getAttr", _foo -> _foo.getAttr()), MapperS.of("_bar")).get()))));
							updatedBars = toBuilder(thenArg
								.mapListToItem(updatedFoos -> MapperS.of(newBar.evaluate(updatedFoos.getMulti()))).getMulti());
							
							return Optional.ofNullable(updatedBars)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
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
	def void shouldGenerateFunctionWithNestedMaps2() {
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
						extract bar [ 
							NewBar( bar -> foos 
								extract foo [ NewFoo( foo -> attr + "_bar" ) ] )
						]
			
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
				
				set foo -> attr:
					attr
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.MapperMaths;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Bar;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;
				
				
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
						List<Bar.BarBuilder> updatedBarsBuilder = doEvaluate(bars);
						
						final List<? extends Bar> updatedBars;
						if (updatedBarsBuilder == null) {
							updatedBars = null;
						} else {
							updatedBars = updatedBarsBuilder.stream().map(Bar::build).collect(Collectors.toList());
							objectValidator.validate(Bar.class, updatedBars);
						}
						
						return updatedBars;
					}
				
					protected abstract List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars) {
							if (bars == null) {
								bars = Collections.emptyList();
							}
							List<Bar.BarBuilder> updatedBars = new ArrayList<>();
							return assignOutput(updatedBars, bars);
						}
						
						protected List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> updatedBars, List<? extends Bar> bars) {
							updatedBars = toBuilder(MapperC.<Bar>of(bars)
								.mapItem(bar -> MapperS.of(newBar.evaluate(bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos())
									.mapItem(foo -> MapperS.of(newFoo.evaluate(MapperMaths.<String, String, String>add(foo.<String>map("getAttr", _foo -> _foo.getAttr()), MapperS.of("_bar")).get()))).getMulti()))).getMulti());
							
							return Optional.ofNullable(updatedBars)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
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
	def void shouldGenerateFunctionWithExtractListModifyItemFunc() {
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
						extract [ NewFoo( item -> attr + "_1" ) ]
			
			func NewFoo:
			 	inputs:
			 		attr string (1..1)
				output:
					foo Foo (0..1)
				
				set foo -> attr:
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
	def void shouldGenerateFunctionWithFilterThenExtract() {
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
						then extract [ item -> attr ]

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
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foos 
					* @return newFoos 
					*/
					public List<String> evaluate(List<? extends Foo> foos) {
						List<String> newFoos = doEvaluate(foos);
						
						return newFoos;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Foo> foos);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(List<? extends Foo> foos) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							List<String> newFoos = new ArrayList<>();
							return assignOutput(newFoos, foos);
						}
						
						protected List<String> assignOutput(List<String> newFoos, List<? extends Foo> foos) {
							final MapperC<Foo> thenArg = MapperC.<Foo>of(foos)
								.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).get());
							newFoos = thenArg
								.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
							
							return newFoos;
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
	
	@Test
	def void shouldGenerateFunctionWithSameNamespace() {
		val model = '''
			namespace ns1
			
			type Bar:
				barAttr string (1..1)

			type Foo:
				fooAttr string (1..1)
			
			func GetFoo:
				inputs:
					barAttr string (1..1)
				output:
					foo Foo (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					strings string (0..*)
				
				set strings:
					bars 
						extract [ GetFoo( item -> barAttr ) ]
						then extract [ item -> fooAttr ]
		'''
		val code = model.generateCode
		val f = code.get("ns1.functions.FuncFoo")
		assertEquals(
			'''
				package ns1.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import javax.inject.Inject;
				import ns1.Bar;
				import ns1.Foo;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					// RosettaFunction dependencies
					//
					@Inject protected GetFoo getFoo;
				
					/**
					* @param bars 
					* @return strings 
					*/
					public List<String> evaluate(List<? extends Bar> bars) {
						List<String> strings = doEvaluate(bars);
						
						return strings;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Bar> bars);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(List<? extends Bar> bars) {
							if (bars == null) {
								bars = Collections.emptyList();
							}
							List<String> strings = new ArrayList<>();
							return assignOutput(strings, bars);
						}
						
						protected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
							final MapperC<Foo> thenArg = MapperC.<Bar>of(bars)
								.mapItem(item -> MapperS.of(getFoo.evaluate(item.<String>map("getBarAttr", bar -> bar.getBarAttr()).get())));
							strings = thenArg
								.mapItem(item -> item.<String>map("getFooAttr", foo -> foo.getFooAttr())).getMulti();
							
							return strings;
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFunctionWithDifferentNamespace() {
		val model = #['''
			namespace ns1
			
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
		''','''
			namespace ns2
			
			import ns1.*
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					strings string (0..*)
				
				set strings:
					bars 
						extract [ item -> foos ]
						then flatten
						then extract [ item -> attr ]
		''']
		val code = model.generateCode
		val f = code.get("ns2.functions.FuncFoo")
		assertEquals(
			'''
				package ns2.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperListOfLists;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import ns1.Bar;
				import ns1.Foo;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param bars 
					* @return strings 
					*/
					public List<String> evaluate(List<? extends Bar> bars) {
						List<String> strings = doEvaluate(bars);
						
						return strings;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Bar> bars);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(List<? extends Bar> bars) {
							if (bars == null) {
								bars = Collections.emptyList();
							}
							List<String> strings = new ArrayList<>();
							return assignOutput(strings, bars);
						}
						
						protected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
							final MapperListOfLists<Foo> thenArg0 = MapperC.<Bar>of(bars)
								.mapItemToList(item -> item.<Foo>mapC("getFoos", bar -> bar.getFoos()));
							final MapperC<Foo> thenArg1 = thenArg0
								.flattenList();
							strings = thenArg1
								.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
							
							return strings;
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFunctionWithDifferentNamespace2() {
		val model = #['''
			namespace ns1
			
			type Bar:
				barAttr string (1..1)

			type Foo:
				fooAttr string (1..1)
			
			func GetFoo:
				inputs:
					barAttr string (1..1)
				output:
					foo Foo (1..1)
		''','''
			namespace ns2
			
			import ns1.*
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					strings string (0..*)
				
				set strings:
					bars 
						extract [ GetFoo( item -> barAttr ) ]
						then extract [ item -> fooAttr ]
		''']
		val code = model.generateCode
		val f = code.get("ns2.functions.FuncFoo")
		assertEquals(
			'''
				package ns2.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import javax.inject.Inject;
				import ns1.Bar;
				import ns1.Foo;
				import ns1.functions.GetFoo;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					// RosettaFunction dependencies
					//
					@Inject protected GetFoo getFoo;
				
					/**
					* @param bars 
					* @return strings 
					*/
					public List<String> evaluate(List<? extends Bar> bars) {
						List<String> strings = doEvaluate(bars);
						
						return strings;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Bar> bars);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(List<? extends Bar> bars) {
							if (bars == null) {
								bars = Collections.emptyList();
							}
							List<String> strings = new ArrayList<>();
							return assignOutput(strings, bars);
						}
						
						protected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
							final MapperC<Foo> thenArg = MapperC.<Bar>of(bars)
								.mapItem(item -> MapperS.of(getFoo.evaluate(item.<String>map("getBarAttr", bar -> bar.getBarAttr()).get())));
							strings = thenArg
								.mapItem(item -> item.<String>map("getFooAttr", foo -> foo.getFooAttr())).getMulti();
							
							return strings;
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateFunctionWithDifferentNamespace3() {
		val model = #['''
			namespace ns1
			
			type Bar:
				barAttr string (1..1)

			type Foo:
				fooAttr string (1..1)
			
			type Baz:
				fooAttr string (1..1)
			
			func GetFoo:
				inputs:
					baz Baz (1..1)
				output:
					foo Foo (1..1)
			
			func GetBaz:
				inputs:
					attr string (1..1)
				output:
					baz Baz (1..1)
		''','''
			namespace ns2
			
			import ns1.*
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					strings string (0..*)
				
				set strings:
					bars 
						extract [ GetFoo( GetBaz( item -> barAttr ) ) ]
						then extract [ item -> fooAttr ]
		''']
		val code = model.generateCode
		val f = code.get("ns2.functions.FuncFoo")
		assertEquals(
			'''
				package ns2.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import javax.inject.Inject;
				import ns1.Bar;
				import ns1.Foo;
				import ns1.functions.GetBaz;
				import ns1.functions.GetFoo;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					// RosettaFunction dependencies
					//
					@Inject protected GetBaz getBaz;
					@Inject protected GetFoo getFoo;
				
					/**
					* @param bars 
					* @return strings 
					*/
					public List<String> evaluate(List<? extends Bar> bars) {
						List<String> strings = doEvaluate(bars);
						
						return strings;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Bar> bars);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(List<? extends Bar> bars) {
							if (bars == null) {
								bars = Collections.emptyList();
							}
							List<String> strings = new ArrayList<>();
							return assignOutput(strings, bars);
						}
						
						protected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
							final MapperC<Foo> thenArg = MapperC.<Bar>of(bars)
								.mapItem(item -> MapperS.of(getFoo.evaluate(getBaz.evaluate(item.<String>map("getBarAttr", bar -> bar.getBarAttr()).get()))));
							strings = thenArg
								.mapItem(item -> item.<String>map("getFooAttr", foo -> foo.getFooAttr())).getMulti();
							
							return strings;
						}
					}
				}
			'''.toString,
			f
		)
		code.compileToClasses
	}
	
	@Test
	def void shouldGenerateListWithinIf() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
			 		test string (1..1)
				output:
					strings string (0..*)
				
				set strings:
					if test = "a"
					then foos extract [ item -> attr + "_a" ]
					else if test = "b"
					then foos extract [ item -> attr + "_b" ]
					else if test = "c"
					then foos extract [ item -> attr + "_c" ]
					// default else
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.CardinalityOperator;
				import com.rosetta.model.lib.expression.MapperMaths;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				
				import static com.rosetta.model.lib.expression.ExpressionOperators.*;
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param foos 
					* @param test 
					* @return strings 
					*/
					public List<String> evaluate(List<? extends Foo> foos, String test) {
						List<String> strings = doEvaluate(foos, test);
						
						return strings;
					}
				
					protected abstract List<String> doEvaluate(List<? extends Foo> foos, String test);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<String> doEvaluate(List<? extends Foo> foos, String test) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							List<String> strings = new ArrayList<>();
							return assignOutput(strings, foos, test);
						}
						
						protected List<String> assignOutput(List<String> strings, List<? extends Foo> foos, String test) {
							if (areEqual(MapperS.of(test), MapperS.of("a"), CardinalityOperator.All).getOrDefault(false)) {
								strings = MapperC.<Foo>of(foos)
									.mapItem(item -> MapperMaths.<String, String, String>add(item.<String>map("getAttr", foo -> foo.getAttr()), MapperS.of("_a"))).getMulti();
							} else if (areEqual(MapperS.of(test), MapperS.of("b"), CardinalityOperator.All).getOrDefault(false)) {
								strings = MapperC.<Foo>of(foos)
									.mapItem(item -> MapperMaths.<String, String, String>add(item.<String>map("getAttr", foo -> foo.getAttr()), MapperS.of("_b"))).getMulti();
							} else if (areEqual(MapperS.of(test), MapperS.of("c"), CardinalityOperator.All).getOrDefault(false)) {
								strings = MapperC.<Foo>of(foos)
									.mapItem(item -> MapperMaths.<String, String, String>add(item.<String>map("getAttr", foo -> foo.getAttr()), MapperS.of("_c"))).getMulti();
							} else {
								strings = Collections.<String>emptyList();
							}
							
							return strings;
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
		
		val res = func.invokeFunc(List, fooList, "b")
		assertEquals(3, res.size);
		assertThat(res, hasItems('a_b', 'b_b', 'c_b'));
	}
	
	@Test
	def void shouldGenerateListJoin() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		stringList string (0..*)
				output:
					concatenatedString string (1..1)
				
				set concatenatedString:
					stringList
						join
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val stringList = newArrayList
		stringList.add("a")
		stringList.add("b")
		stringList.add("c")
		stringList.add("d")
		stringList.add("e")
		
		val res = func.invokeFunc(String, stringList)
		assertEquals("abcde", res);
	}
	
	@Test
	def void shouldGenerateListJoinWithDelimiter() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		stringList string (0..*)
				output:
					concatenatedString string (1..1)
				
				set concatenatedString:
					stringList
						join "_"
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val stringList = newArrayList
		stringList.add("a")
		stringList.add("b")
		stringList.add("c")
		stringList.add("d")
		stringList.add("e")
		
		val res = func.invokeFunc(String, stringList)
		assertEquals("a_b_c_d_e", res);
	}
	
	@Test
	def void shouldGenerateListReduceString() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		stringList string (0..*)
				output:
					concatenatedString string (1..1)
				
				set concatenatedString:
					stringList
						reduce a, b [ a + b ]
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.MapperMaths;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import java.util.Collections;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param stringList 
					* @return concatenatedString 
					*/
					public String evaluate(List<String> stringList) {
						String concatenatedString = doEvaluate(stringList);
						
						return concatenatedString;
					}
				
					protected abstract String doEvaluate(List<String> stringList);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected String doEvaluate(List<String> stringList) {
							if (stringList == null) {
								stringList = Collections.emptyList();
							}
							String concatenatedString = null;
							return assignOutput(concatenatedString, stringList);
						}
						
						protected String assignOutput(String concatenatedString, List<String> stringList) {
							concatenatedString = MapperC.<String>of(stringList)
								.<String>reduce((a, b) -> MapperMaths.<String, String, String>add(a, b)).get();
							
							return concatenatedString;
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val stringList = newArrayList
		stringList.add("a")
		stringList.add("b")
		stringList.add("c")
		stringList.add("d")
		stringList.add("e")
		
		val res = func.invokeFunc(String, stringList)
		assertEquals("abcde", res);
	}


	
	@Test
	def void shouldGenerateListSumInt() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		intList int (0..*)
				output:
					total int (1..1)
				
				set total:
					intList
						sum
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		intList.add(1)
		intList.add(3)
		intList.add(5)
		intList.add(7)
		intList.add(11)
		
		val res = func.invokeFunc(Integer, intList)
		assertEquals(27, res);
	}
	
	@Test
	def void shouldGenerateListSumBigDecimal() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		numberList number (0..*)
				output:
					total number (1..1)
				
				set total:
					numberList
						sum
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val numberList = newArrayList
		numberList.add(BigDecimal.valueOf(1.1))
		numberList.add(BigDecimal.valueOf(3.1))
		numberList.add(BigDecimal.valueOf(5.1))
		numberList.add(BigDecimal.valueOf(7.1))
		numberList.add(BigDecimal.valueOf(11.1))
		
		val res = func.invokeFunc(BigDecimal, numberList)
		assertEquals(BigDecimal.valueOf(27.5), res);
	}
	
	@Test
	def void shouldGenerateListReduceSum() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		intList int (0..*)
				output:
					total int (1..1)
				
				set total:
					intList
						reduce a, b [ a + b ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		intList.add(1)
		intList.add(3)
		intList.add(5)
		intList.add(7)
		intList.add(11)
		
		val res = func.invokeFunc(Integer, intList)
		assertEquals(27, res);
	}
	
	@Test
	def void shouldGenerateListFirstInt() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		intList int (0..*)
				output:
					firstInt int (1..1)
				
				set firstInt:
					intList
						first
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		intList.add(1)
		intList.add(2)
		intList.add(3)
		intList.add(4)
		intList.add(5)
		
		val res = func.invokeFunc(Integer, intList)
		assertEquals(1, res);
	}
	
	@Test
	def void shouldGenerateListFirstComplexType() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		fooList Foo (0..*)
				output:
					firstFoo Foo (1..1)
				
				set firstFoo:
					fooList
						first
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		val foo5 = classes.createFoo('e')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		fooList.add(foo4)
		fooList.add(foo5)
		
		val res = func.invokeFunc(RosettaModelObject, fooList)
		assertEquals(foo1, res);
	}
	
	@Test
	def void shouldGenerateListFirstComplexTypeEmptyList() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		fooList Foo (0..*)
				output:
					firstFoo Foo (1..1)
				
				set firstFoo:
					fooList
						first
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val res = func.invokeFunc(RosettaModelObject, newArrayList)
		assertNull(res);
	}
	
	@Test
	def void shouldGenerateListLastInt() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		intList int (0..*)
				output:
					lastInt int (1..1)
				
				set lastInt:
					intList
						last
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		intList.add(1)
		intList.add(2)
		intList.add(3)
		intList.add(4)
		intList.add(5)
		
		val res = func.invokeFunc(Integer, intList)
		assertEquals(5, res);
	}
	
	@Test
	def void shouldGenerateListLastComplexType() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		fooList Foo (0..*)
				output:
					lastFoo Foo (1..1)
				
				set lastFoo:
					fooList
						last
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		val foo5 = classes.createFoo('e')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		fooList.add(foo4)
		fooList.add(foo5)
		
		val res = func.invokeFunc(RosettaModelObject, fooList)
		assertEquals(foo5, res);
	}
	@Test
	def void shouldGenerateListReduceSubtract() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		intList int (0..*)
				output:
					total int (1..1)
				
				set total:
					intList
						reduce a, b [ a - b ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		intList.add(10)
		intList.add(7)
		intList.add(1)
		
		val res = func.invokeFunc(Integer, intList)
		assertEquals(2, res);
		
		val intList2 = newArrayList
		intList2.add(1)
		intList2.add(7)
		intList2.add(10)
		
		val res2 = func.invokeFunc(Integer, intList2)
		assertEquals(-16, res2);
	}
	
	@Test
	def void shouldGenerateEmptyListReduceSum() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		numberList int (0..*)
				output:
					total int (1..1)
				
				set total:
					numberList
						reduce a, b [ a + b ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		
		val res = func.invokeFunc(Integer, intList)
		assertNull(res);
	}
	
	@Test
	def void shouldGenerateListReduceProduct() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		numberList int (0..*)
				output:
					total int (1..1)
				
				set total:
					numberList
						reduce a, b [ a * b ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		intList.add(1)
		intList.add(3)
		intList.add(5)
		intList.add(7)
		intList.add(11)
		
		val res = func.invokeFunc(Integer, intList)
		assertEquals(1155, res);
	}
	
	@Test
	def void shouldGenerateListReduceMaxNumber() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		numberList int (0..*)
				output:
					total int (1..1)
				
				set total:
					numberList
						reduce a, b [ if a > b then a else b ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		intList.add(1)
		intList.add(3)
		intList.add(5)
		intList.add(7)
		intList.add(11)
		
		val res = func.invokeFunc(Integer, intList)
		assertEquals(11, res);
	}
	
	@Test
	def void shouldGenerateListReduceMinNumber() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		numberList int (0..*)
				output:
					total int (1..1)
				
				set total:
					numberList
						reduce a, b [ Min( a, b ) ]
			
			func Min:
				inputs:
					a int (1..1)
					b int (1..1)
				output:
					result int (1..1)
				set result:
					if a > b then b else a
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		intList.add(1)
		intList.add(3)
		intList.add(5)
		intList.add(7)
		intList.add(11)
		
		val res = func.invokeFunc(Integer, intList)
		assertEquals(1, res);
	}
	
	@Test
	def void shouldGenerateListReduceComplexType() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					foo Foo (1..1)
				
				set foo:
					foos
						reduce foo1, foo2 [ Create_Foo( foo1 -> attr + foo2 -> attr ) ]
			
			func Create_Foo:
			 	inputs:
			 		attr string (1..1)
				output:
					foo Foo (1..1)
				
				set foo -> attr: attr
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.expression.MapperMaths;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.model.lib.mapper.MapperS;
				import com.rosetta.test.model.Foo;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import javax.inject.Inject;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
					
					// RosettaFunction dependencies
					//
					@Inject protected Create_Foo create_Foo;
				
					/**
					* @param foos 
					* @return foo 
					*/
					public Foo evaluate(List<? extends Foo> foos) {
						Foo.FooBuilder fooBuilder = doEvaluate(foos);
						
						final Foo foo;
						if (fooBuilder == null) {
							foo = null;
						} else {
							foo = fooBuilder.build();
							objectValidator.validate(Foo.class, foo);
						}
						
						return foo;
					}
				
					protected abstract Foo.FooBuilder doEvaluate(List<? extends Foo> foos);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected Foo.FooBuilder doEvaluate(List<? extends Foo> foos) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							Foo.FooBuilder foo = Foo.builder();
							return assignOutput(foo, foos);
						}
						
						protected Foo.FooBuilder assignOutput(Foo.FooBuilder foo, List<? extends Foo> foos) {
							foo = toBuilder(MapperC.<Foo>of(foos)
								.<Foo>reduce((foo1, foo2) -> MapperS.of(create_Foo.evaluate(MapperMaths.<String, String, String>add(foo1.<String>map("getAttr", _foo -> _foo.getAttr()), foo2.<String>map("getAttr", _foo -> _foo.getAttr())).get()))).get());
							
							return Optional.ofNullable(foo)
								.map(o -> o.prune())
								.orElse(null);
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val fooList = newArrayList
		fooList.add(classes.createFoo('a'))
		fooList.add(classes.createFoo('b'))
		fooList.add(classes.createFoo('c'))
		fooList.add(classes.createFoo('d'))
		fooList.add(classes.createFoo('e'))
		
		val res = func.invokeFunc(RosettaModelObject, fooList)
		
		// reflective Foo.getAttr()
		val attr = res.class.getMethod("getAttr").invoke(res) as String;
		
		assertEquals("abcde", attr);
	}
	
	@Test
	def void shouldGenerateListReduceThenMapSingle() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					fooCount int (1..1)
				
				set fooCount:
					bars
						reduce bar1, bar2 [ if bar1 -> foos count > bar2 -> foos count then bar1 else bar2 ]
						then extract [ item -> foos count ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1))
		val bar2 = classes.createBar(ImmutableList.of(foo1, foo2))
		val bar3 = classes.createBar(ImmutableList.of(foo1, foo2, foo3))
		val bar4 = classes.createBar(ImmutableList.of(foo2, foo2, foo3, foo4))
		
		val barList = newArrayList
		barList.add(bar1)
		barList.add(bar2)
		barList.add(bar3)
		barList.add(bar4)
		
		val res = func.invokeFunc(Integer, barList)
		assertEquals(4, res);
	}
	
	@Test
	def void shouldGenerateListReduceThenExtractList() {
		val model = '''
			type Bar:
				foos Foo (0..*)

			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		bars Bar (0..*)
				output:
					attrs string (0..*)
				
				set attrs:
					bars
						reduce bar1, bar2 [ if bar1 -> foos count > bar2 -> foos count then bar1 else bar2 ] // max by foo count
						then extract [ item -> foos ]
						then extract [ item -> attr ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		
		val bar1 = classes.createBar(ImmutableList.of(foo1))
		val bar2 = classes.createBar(ImmutableList.of(foo1, foo2))
		val bar3 = classes.createBar(ImmutableList.of(foo1, foo2, foo3))
		val bar4 = classes.createBar(ImmutableList.of(foo1, foo2, foo3, foo4))
		
		val barList = newArrayList
		barList.add(bar1)
		barList.add(bar2)
		barList.add(bar3)
		barList.add(bar4)
		
		val res = func.invokeFunc(List, barList)
		assertEquals(4, res.size);
		assertThat(res, hasItems('a', 'b', 'c', 'd'));
	}
	
	@Test
	def void shouldGenerateListMaxInt() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		intList int (0..*)
				output:
					result int (0..1)
				
				set result:
					intList
						max
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val intList = newArrayList
		intList.add(1)
		intList.add(2)
		intList.add(3)
		intList.add(4)
		intList.add(5)
		
		val res = func.invokeFunc(Integer, intList)
		assertEquals(5, res);
	}
	
	@Test
	def void shouldGenerateListMaxComplexType() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					foo Foo (0..1)
				
				set foo:
					foos
						max [ item -> attr ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		val foo5 = classes.createFoo('e')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		fooList.add(foo4)
		fooList.add(foo5)
		
		val res = func.invokeFunc(RosettaModelObject, fooList)
		assertEquals(foo5, res);
	}
	
	@Test
	def void shouldGenerateListMinBigDecimal() {
		val model = '''
			func FuncFoo:
			 	inputs:
			 		numberList number (0..*)
				output:
					result number (0..1)
				
				set result:
					numberList
						min
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val numberList = newArrayList
		numberList.add(BigDecimal.valueOf(1.1))
		numberList.add(BigDecimal.valueOf(1.2))
		numberList.add(BigDecimal.valueOf(1.3))
		numberList.add(BigDecimal.valueOf(1.4))
		numberList.add(BigDecimal.valueOf(1.5))
		
		val res = func.invokeFunc(BigDecimal, numberList)
		assertEquals(BigDecimal.valueOf(1.1), res);
	}
	
	@Test
	def void shouldGenerateListMinComplexType() {
		val model = '''
			type Foo:
				attr string (1..1)
			
			func FuncFoo:
			 	inputs:
			 		foos Foo (0..*)
				output:
					foo Foo (0..1)
				
				set foo:
					foos
						min [ item -> attr ]
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		val foo5 = classes.createFoo('e')
		
		val fooList = newArrayList
		fooList.add(foo1)
		fooList.add(foo2)
		fooList.add(foo3)
		fooList.add(foo4)
		fooList.add(foo5)
		
		val res = func.invokeFunc(RosettaModelObject, fooList)
		assertEquals(foo1, res);
	}
	
	@Test
	def void shouldGenerateIntListSort() {
		val model = '''
			func FuncFoo: 
				inputs:
					numbers int (0..*)
				output:
					sortedNumbers int (0..*)
			
				set sortedNumbers:
					numbers sort // sort items
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
				
					/**
					* @param numbers 
					* @return sortedNumbers 
					*/
					public List<Integer> evaluate(List<Integer> numbers) {
						List<Integer> sortedNumbers = doEvaluate(numbers);
						
						return sortedNumbers;
					}
				
					protected abstract List<Integer> doEvaluate(List<Integer> numbers);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Integer> doEvaluate(List<Integer> numbers) {
							if (numbers == null) {
								numbers = Collections.emptyList();
							}
							List<Integer> sortedNumbers = new ArrayList<>();
							return assignOutput(sortedNumbers, numbers);
						}
						
						protected List<Integer> assignOutput(List<Integer> sortedNumbers, List<Integer> numbers) {
							sortedNumbers = MapperC.<Integer>of(numbers)
								.sort().getMulti();
							
							return sortedNumbers;
						}
					}
				}
			'''.toString,
			f
		)
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val res = func.invokeFunc(List, ImmutableList.of(4, 2, 3, 1))
		assertEquals(4, res.size);
		assertEquals(ImmutableList.of(1, 2, 3, 4), res);
	}
	
		@Test
	def void shouldGenerateDistinctIntListSort() {
		val model = '''
			func FuncFoo: 
				inputs:
					numbers int (0..*)
				output:
					sortedNumbers int (0..*)
			
				set sortedNumbers:
					numbers 
						distinct
						sort
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val res = func.invokeFunc(List, ImmutableList.of(4, 2, 2, 4, 3, 1, 1, 3))
		assertEquals(4, res.size);
		assertEquals(ImmutableList.of(1, 2, 3, 4), res);
	}
	
	@Test
	def void shouldGenerateDateListSort() {
		val model = '''
			func FuncFoo: 
				inputs:
					dates date (0..*)
				output:
					sortedDates date (0..*)
			
				set sortedDates:
					dates sort // sort items
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val date1 = Date.of(2000, 1, 1)
		val date2 = Date.of(2000, 1, 2)
		val date3 = Date.of(2000, 2, 1)
		val date4 = Date.of(2001, 1, 1)
		
		val res = func.invokeFunc(List, ImmutableList.of(date4, date1, date2, date3))
		assertEquals(4, res.size);
		assertEquals(ImmutableList.of(date1, date2, date3, date4), res);
	}
	
	@Test
	def void shouldGenerateListSortWithAttribute() {
		val model = '''
			type Foo:
				attr string (1..1) // single
			
			func FuncFoo:
				inputs:
					foos Foo (0..*)
				output:
					sortedFoos Foo (0..*)
			
				set sortedFoos:
					foos sort [item -> attr] // sort based on item attribute
		'''
		val code = model.generateCode
		val f = code.get("com.rosetta.test.model.functions.FuncFoo")
		assertEquals(
			'''
				package com.rosetta.test.model.functions;
				
				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.functions.RosettaFunction;
				import com.rosetta.model.lib.mapper.MapperC;
				import com.rosetta.test.model.Foo;
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.List;
				import java.util.Optional;
				import java.util.stream.Collectors;
				import javax.inject.Inject;
				
				
				@ImplementedBy(FuncFoo.FuncFooDefault.class)
				public abstract class FuncFoo implements RosettaFunction {
					
					@Inject protected ModelObjectValidator objectValidator;
				
					/**
					* @param foos 
					* @return sortedFoos 
					*/
					public List<? extends Foo> evaluate(List<? extends Foo> foos) {
						List<Foo.FooBuilder> sortedFoosBuilder = doEvaluate(foos);
						
						final List<? extends Foo> sortedFoos;
						if (sortedFoosBuilder == null) {
							sortedFoos = null;
						} else {
							sortedFoos = sortedFoosBuilder.stream().map(Foo::build).collect(Collectors.toList());
							objectValidator.validate(Foo.class, sortedFoos);
						}
						
						return sortedFoos;
					}
				
					protected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
				
					public static class FuncFooDefault extends FuncFoo {
						@Override
						protected List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
							if (foos == null) {
								foos = Collections.emptyList();
							}
							List<Foo.FooBuilder> sortedFoos = new ArrayList<>();
							return assignOutput(sortedFoos, foos);
						}
						
						protected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> sortedFoos, List<? extends Foo> foos) {
							sortedFoos = toBuilder(MapperC.<Foo>of(foos)
								.sort(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti());
							
							return Optional.ofNullable(sortedFoos)
								.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
								.orElse(null);
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
		
		val fooList = newArrayList
		fooList.add(foo4)
		fooList.add(foo2)
		fooList.add(foo1)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, ImmutableList.of(foo4, foo2, foo3, foo1))
		assertEquals(4, res.size);
		assertEquals(ImmutableList.of(foo1, foo2, foo3, foo4), res);
	}
	
	@Test
	def void shouldGenerateIntListReverse() {
		val model = '''
			func FuncFoo: 
				inputs:
					numbers int (0..*)
				output:
					sortedNumbers int (0..*)
			
				set sortedNumbers:
					numbers
						reverse // reverse (no sort)
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val res = func.invokeFunc(List, ImmutableList.of(4, 2, 3, 1))
		assertEquals(4, res.size);
		assertEquals(ImmutableList.of(1, 3, 2, 4), res);
	}
	
	@Test
	def void shouldGenerateDateListSortThenReverse() {
		val model = '''
			func FuncFoo: 
				inputs:
					dates date (0..*)
				output:
					sortedDates date (0..*)
			
				set sortedDates:
					dates 
						sort // sort items
						reverse
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val date1 = Date.of(2000, 1, 1)
		val date2 = Date.of(2000, 1, 2)
		val date3 = Date.of(2000, 2, 1)
		val date4 = Date.of(2001, 1, 1)
		
		val res = func.invokeFunc(List, ImmutableList.of(date4, date1, date2, date3))
		assertEquals(4, res.size);
		assertEquals(ImmutableList.of(date4, date3, date2, date1), res);
	}
	
	@Test
	def void shouldGenerateListSortWithAttributeThenReverse() {
		val model = '''
			type Foo:
				attr string (1..1) // single
			
			func FuncFoo:
				inputs:
					foos Foo (0..*)
				output:
					sortedFoos Foo (0..*)
			
				set sortedFoos:
					foos 
						sort [item -> attr] // sort based on item attribute
						reverse
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		
		val fooList = newArrayList
		fooList.add(foo4)
		fooList.add(foo2)
		fooList.add(foo1)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, ImmutableList.of(foo4, foo2, foo3, foo1))
		assertEquals(4, res.size);
		assertEquals(ImmutableList.of(foo4, foo3, foo2, foo1), res);
	}
	
	@Test
	def void shouldGenerateListReverseComplexType() {
		val model = '''
			type Foo:
				attr string (1..1) // single
			
			func FuncFoo:
				inputs:
					foos Foo (0..*)
				output:
					sortedFoos Foo (0..*)
			
				set sortedFoos:
					foos 
						reverse
		'''
		val code = model.generateCode
		val classes = code.compileToClasses
		val func = classes.createFunc("FuncFoo");
		
		val foo1 = classes.createFoo('a')
		val foo2 = classes.createFoo('b')
		val foo3 = classes.createFoo('c')
		val foo4 = classes.createFoo('d')
		
		val fooList = newArrayList
		fooList.add(foo4)
		fooList.add(foo2)
		fooList.add(foo1)
		fooList.add(foo3)
		
		val res = func.invokeFunc(List, ImmutableList.of(foo4, foo2, foo3, foo1))
		assertEquals(4, res.size);
		assertEquals(ImmutableList.of(foo1, foo3, foo2, foo4), res);
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
	
	private def RosettaModelObject createFooWithScheme(Map<String, Class<?>> classes, String attr, String scheme) {
		val fieldWithMetaString = classes.createFieldWithMetaString(attr, scheme)
		classes.createInstanceUsingBuilder('FooWithScheme', of('attr', fieldWithMetaString), of()) as RosettaModelObject
	}
	
	private def RosettaModelObject createBar(Map<String, Class<?>> classes, List<RosettaModelObject> foos) {
		classes.createInstanceUsingBuilder('Bar', of(), of('foos', foos)) as RosettaModelObject
	}
}
