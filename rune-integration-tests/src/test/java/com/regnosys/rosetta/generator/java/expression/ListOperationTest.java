package com.regnosys.rosetta.generator.java.expression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.model.lib.records.Date;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ListOperationTest {

    @Inject
    private FunctionGeneratorHelper functionGeneratorHelper;
    @Inject
    private CodeGeneratorTestHelper codeGeneratorTestHelper;

    @Test
    public void shouldGenerateFunctionWithFilterListItemParameter() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		filteredFoos Foo (0..*)
        \t
        	set filteredFoos:
        		foos\s
        			filter [ item -> include = True ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        
        import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
        
        @ImplementedBy(FuncFoo.FuncFooDefault.class)
        public abstract class FuncFoo implements RosettaFunction {
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        
        	/**
        	* @param foos\s
        	* @return filteredFoos\s
        	*/
        	public List<? extends Foo> evaluate(List<? extends Foo> foos) {
        		List<Foo.FooBuilder> filteredFoosBuilder = doEvaluate(foos);
        \t\t
        		final List<? extends Foo> filteredFoos;
        		if (filteredFoosBuilder == null) {
        			filteredFoos = null;
        		} else {
        			filteredFoos = filteredFoosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        			objectValidator.validate(Foo.class, filteredFoos);
        		}
        \t\t
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
        \t\t
        		protected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
        			filteredFoos = toBuilder(MapperC.<Foo>of(foos)
        				.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).get()).getMulti());
        \t\t\t
        			return Optional.ofNullable(filteredFoos)
        				.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) foo1, (Object) foo2));
    }

    @Test
    public void shouldGenerateFunctionWithFilterListNamedParameter() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		filteredFoos Foo (0..*)
        \t
        	set filteredFoos:
        		foos\s
        			filter fooItem [ fooItem -> include = True ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        
        import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
        
        @ImplementedBy(FuncFoo.FuncFooDefault.class)
        public abstract class FuncFoo implements RosettaFunction {
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        
        	/**
        	* @param foos\s
        	* @return filteredFoos\s
        	*/
        	public List<? extends Foo> evaluate(List<? extends Foo> foos) {
        		List<Foo.FooBuilder> filteredFoosBuilder = doEvaluate(foos);
        \t\t
        		final List<? extends Foo> filteredFoos;
        		if (filteredFoosBuilder == null) {
        			filteredFoos = null;
        		} else {
        			filteredFoos = filteredFoosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        			objectValidator.validate(Foo.class, filteredFoos);
        		}
        \t\t
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
        \t\t
        		protected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
        			filteredFoos = toBuilder(MapperC.<Foo>of(foos)
        				.filterItemNullSafe(fooItem -> areEqual(fooItem.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).get()).getMulti());
        \t\t\t
        			return Optional.ofNullable(filteredFoos)
        				.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) foo1, (Object) foo2));
    }

    @Test
    public void shouldGenerateFunctionWithFilterList2() {
        String model = """
        type Foo2:
        	include boolean (1..1)
        	include2 boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo2 (0..*)
        	output:
        		filteredFoos Foo2 (0..*)
        \t
        	set filteredFoos:
        		foos\s
        			filter [ item -> include = True and item -> include2 = True ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo2(classes, true, true, "a");
        var foo2 = createFoo2(classes, true, false, "b");
        var foo3 = createFoo2(classes, true, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(1, res.size());
        assertThat(res, hasItems((Object) foo1));
    }

    @Test
    public void shouldGenerateFunctionWithFilterList3() {
        String model = """
        type Foo2:
        	include boolean (1..1)
        	include2 boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo2 (0..*)
        	output:
        		filteredFoos Foo2 (0..*)
        \t
        	set filteredFoos:
        		foos\s
        			filter [ item -> include = True ]
        			then filter [ item -> include2 = True ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        
        import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
        
        @ImplementedBy(FuncFoo.FuncFooDefault.class)
        public abstract class FuncFoo implements RosettaFunction {
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        
        	/**
        	* @param foos\s
        	* @return filteredFoos\s
        	*/
        	public List<? extends Foo2> evaluate(List<? extends Foo2> foos) {
        		List<Foo2.Foo2Builder> filteredFoosBuilder = doEvaluate(foos);
        \t\t
        		final List<? extends Foo2> filteredFoos;
        		if (filteredFoosBuilder == null) {
        			filteredFoos = null;
        		} else {
        			filteredFoos = filteredFoosBuilder.stream().map(Foo2::build).collect(Collectors.toList());
        			objectValidator.validate(Foo2.class, filteredFoos);
        		}
        \t\t
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
        \t\t
        		protected List<Foo2.Foo2Builder> assignOutput(List<Foo2.Foo2Builder> filteredFoos, List<? extends Foo2> foos) {
        			final MapperC<Foo2> thenArg = MapperC.<Foo2>of(foos)
        				.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude", foo2 -> foo2.getInclude()), MapperS.of(true), CardinalityOperator.All).get());
        			filteredFoos = toBuilder(thenArg
        				.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude2", foo2 -> foo2.getInclude2()), MapperS.of(true), CardinalityOperator.All).get()).getMulti());
        \t\t\t
        			return Optional.ofNullable(filteredFoos)
        				.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo2(classes, true, true, "a");
        var foo2 = createFoo2(classes, true, false, "b");
        var foo3 = createFoo2(classes, true, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(1, res.size());
        assertThat(res, hasItems((Object) foo1));
    }

    @Test
    public void shouldGenerateFunctionWithFilterListWithMetaData() {
        String model = """
        type FooWithScheme:
        	attr string (1..1)
        		[metadata scheme]
        
        func FuncFoo:
        \s	inputs:
        \s		foos FooWithScheme (0..*)
        	output:
        		filteredFoos FooWithScheme (0..*)
        \t
        	set filteredFoos:
        		foos\s
        			filter [ item -> attr -> scheme = "foo-scheme" ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        
        import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
        
        @ImplementedBy(FuncFoo.FuncFooDefault.class)
        public abstract class FuncFoo implements RosettaFunction {
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        
        	/**
        	* @param foos\s
        	* @return filteredFoos\s
        	*/
        	public List<? extends FooWithScheme> evaluate(List<? extends FooWithScheme> foos) {
        		List<FooWithScheme.FooWithSchemeBuilder> filteredFoosBuilder = doEvaluate(foos);
        \t\t
        		final List<? extends FooWithScheme> filteredFoos;
        		if (filteredFoosBuilder == null) {
        			filteredFoos = null;
        		} else {
        			filteredFoos = filteredFoosBuilder.stream().map(FooWithScheme::build).collect(Collectors.toList());
        			objectValidator.validate(FooWithScheme.class, filteredFoos);
        		}
        \t\t
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
        \t\t
        		protected List<FooWithScheme.FooWithSchemeBuilder> assignOutput(List<FooWithScheme.FooWithSchemeBuilder> filteredFoos, List<? extends FooWithScheme> foos) {
        			filteredFoos = toBuilder(MapperC.<FooWithScheme>of(foos)
        				.filterItemNullSafe(item -> areEqual(item.<FieldWithMetaString>map("getAttr", fooWithScheme -> fooWithScheme.getAttr()).map("getMeta", a->a.getMeta()).map("getScheme", a->a.getScheme()), MapperS.of("foo-scheme"), CardinalityOperator.All).get()).getMulti());
        \t\t\t
        			return Optional.ofNullable(filteredFoos)
        				.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFooWithScheme(classes, "a", "foo-scheme");
        var foo2 = createFooWithScheme(classes, "b", "foo-scheme");
        var foo3 = createFooWithScheme(classes, "c", "bar-scheme");


        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) foo1, (Object) foo2));
    }

    @Test
    @Disabled
    public void shouldGenerateFunctionWithFilterListWithMetaData2() {
        String model = """
        type FooWithScheme:
        	attr string (1..1)
        		[metadata scheme]
        
        func FuncFoo:
        \s	inputs:
        \s		foos FooWithScheme (0..*)
        	output:
        		strings string (0..*)
        \t
        	set strings:
        		foos\s
        			map [ item -> attr ]
        			filter [ item -> scheme = "foo-scheme" ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        	* @param foos\s
        	* @return strings\s
        	*/
        	public List<String> evaluate(List<? extends FooWithScheme> foos) {
        		List<String> stringsHolder = doEvaluate(foos);
        		List<String> strings = assignOutput(stringsHolder, foos);
        \t\t
        		return strings;
        	}
        \t
        	private List<String> assignOutput(List<String> strings, List<? extends FooWithScheme> foos) {
        		strings = MapperC.of(foos)
        			.mapItem(/*MapperS<? extends FooWithScheme>*/ __item -> (MapperS<String>) __item.<FieldWithMetaString>map("getAttr", _fooWithScheme -> _fooWithScheme.getAttr()).<String>map("getValue", _f->_f.getValue())).getMulti();
        		return strings;
        	}
        
        	protected abstract List<String> doEvaluate(List<? extends FooWithScheme> foos);
        \t
        	public static class FuncFooDefault extends FuncFoo {
        		@Override
        		protected List<String> doEvaluate(List<? extends FooWithScheme> foos) {
        			return new ArrayList<>();
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFooWithScheme(classes, "a", "foo-scheme");
        var foo2 = createFooWithScheme(classes, "b", "foo-scheme");
        var foo3 = createFooWithScheme(classes, "c", "bar-scheme");


        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) foo1, (Object) foo2));
    }


    @Test
    public void shouldGenerateFunctionWithFilterBuiltInTypeList() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		foos boolean (0..*)
        	output:
        		filteredFoos boolean (0..*)
        \t
        	set filteredFoos:
        		foos\s
        			filter [ item = True ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
        package com.rosetta.test.model.functions;
        
        import com.google.inject.ImplementedBy;
        import com.rosetta.model.lib.expression.CardinalityOperator;
        import com.rosetta.model.lib.functions.RosettaFunction;
        import com.rosetta.model.lib.mapper.MapperC;
        import com.rosetta.model.lib.mapper.MapperS;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.List;
        
        import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
        
        @ImplementedBy(FuncFoo.FuncFooDefault.class)
        public abstract class FuncFoo implements RosettaFunction {
        
        	/**
        	* @param foos\s
        	* @return filteredFoos\s
        	*/
        	public List<Boolean> evaluate(List<Boolean> foos) {
        		List<Boolean> filteredFoos = doEvaluate(foos);
        \t\t
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
        \t\t
        		protected List<Boolean> assignOutput(List<Boolean> filteredFoos, List<Boolean> foos) {
        			filteredFoos = MapperC.<Boolean>of(foos)
        				.filterItemNullSafe(item -> areEqual(item, MapperS.of(true), CardinalityOperator.All).get()).getMulti();
        \t\t\t
        			return filteredFoos;
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var fooList = new ArrayList<>();
        fooList.add(true);
        fooList.add(true);
        fooList.add(false);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) true, (Object) true));
    }

    @Test
    public void shouldGenerateFunctionWithFilterListAndInputParameter() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        \s		test boolean (1..1)
        	output:
        		filteredFoos Foo (0..*)
        \t
        	set filteredFoos:
        		foos\s
        			filter [ item -> include = test ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList, true);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) foo1, (Object) foo2));
    }

    @Test
    public void shouldGenerateFunctionWithFilterListAndCount() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		filteredFoosCount int (1..1)
        \t
        	set filteredFoosCount:
        		foos\s
        			filter fooItem [ fooItem -> include = True ]\s
        			then count
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, fooList);
        assertEquals(2, res.intValue());
    }

    @Test
    public void shouldGenerateFunctionWithFilterListAndFuncCalls() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		filteredFoos Foo (0..*)
        \t
        	set filteredFoos:
        		foos\s
        			filter [ FuncFooTest( item ) ]
        			then filter [ FuncFooTest2( item ) ]
        
        func FuncFooTest:
        \s	inputs:
        \s		foo Foo (1..1)
        	output:
        		result boolean (0..1)
        \t
        	set result:
        		foo -> include
        
        func FuncFooTest2:
        \s	inputs:
        \s		foo Foo (1..1)
        	output:
        		result boolean (0..1)
        \t
        	set result:
        		foo -> include
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) foo1, (Object) foo2));
    }

    @Test
    public void shouldGenerateFunctionWithFilterListAndAliasParameter() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        \s		test boolean (1..1)
        	output:
        		filteredFoos Foo (0..*)
        \t
        	alias testAlias:
        		test
        \t
        	set filteredFoos:
        		foos\s
        			filter [ item -> include = testAlias ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList, true);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) foo1, (Object) foo2));
    }

    @Test
    public void shouldGenerateFunctionWithFilterAndAlias() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		filteredFooAttrs string (0..*)
        \t
        	alias filteredFoosAlias:
        		foos\s
        			filter [ item -> include = True ]
        \t
        	set filteredFooAttrs:
        		filteredFoosAlias -> attr
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) "a", (Object) "b"));
    }

    @Test
    public void shouldGenerateFunctionWithMultipleFilterList() {
        String model = """
        type Foo2:
        	include boolean (0..1)
        	include2 boolean (0..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo2 (0..*)
        \s		test boolean (0..1)
        \s		test2 boolean (0..1)
        \s		test3 boolean (0..1)
        	output:
        		foo Foo2 (0..1)
        \t
        	alias filteredFoos:
        		foos\s
        			filter a [ if test exists then a -> include = test else True ]
        			then filter b [ if test2 exists then b -> include2 = test2 else True ]
        			then filter c [ if test3 exists then c -> include2 = test3 else True ]
        \t
        	set foo:
        		filteredFoos only-element
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo2(classes, true, true, "a");
        var foo2 = createFoo2(classes, true, false, "b");
        var foo3 = createFoo2(classes, false, true, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, fooList, true, true, true);
        assertEquals(foo1, res);
    }

    @Test
    public void shouldGenerateFunctionWithMultipleFilterList2() {
        String model = """
        type Foo2:
        	include boolean (0..1)
        	include2 boolean (0..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo2 (0..*)
        \s		test boolean (0..1)
        \s		test2 boolean (0..1)
        \s		test3 boolean (0..1)
        	output:
        		foo Foo2 (0..1)
        \t
        	alias filteredFoos:
        		foos\s
        			filter [ if test exists then item -> include = test else True ]
        			then filter [ if test2 exists then item -> include2 = test2 else True ]
        			then filter [ if test3 exists then item -> include2 = test3 else True ]
        \t
        	set foo:
        		filteredFoos only-element
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo2(classes, true, true, "a");
        var foo2 = createFoo2(classes, true, false, "b");
        var foo3 = createFoo2(classes, false, true, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, fooList, true, true, true);
        assertEquals(foo1, res);
    }

    @Test
    public void shouldGenerateFunctionWithFilterListAliasAndOnlyElement() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bar Bar (1..1)
        	output:
        		foos Foo (0..*)
        \t
        	set foos:
        		bar -> foos\s
        			extract [ if item -> include = True then Foo { include: include, attr: attr + "_bar" } else item ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        
        import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
        
        @ImplementedBy(FuncFoo.FuncFooDefault.class)
        public abstract class FuncFoo implements RosettaFunction {
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        
        	/**
        	* @param bar\s
        	* @return foos\s
        	*/
        	public List<? extends Foo> evaluate(Bar bar) {
        		List<Foo.FooBuilder> foosBuilder = doEvaluate(bar);
        \t\t
        		final List<? extends Foo> foos;
        		if (foosBuilder == null) {
        			foos = null;
        		} else {
        			foos = foosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        			objectValidator.validate(Foo.class, foos);
        		}
        \t\t
        		return foos;
        	}
        
        	protected abstract List<Foo.FooBuilder> doEvaluate(Bar bar);
        
        	public static class FuncFooDefault extends FuncFoo {
        		@Override
        		protected List<Foo.FooBuilder> doEvaluate(Bar bar) {
        			List<Foo.FooBuilder> foos = new ArrayList<>();
        			return assignOutput(foos, bar);
        		}
        \t\t
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
        \t\t\t
        			return Optional.ofNullable(foos)
        				.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "foo");
        var foo2 = createFoo(classes, false, "foo");

        var bar = createBar(classes, ImmutableList.of(foo1, foo2, foo2));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, bar);
        assertEquals(3, res.size());

        var expectedNewFoo = createFoo(classes, true, "foo_bar");

        assertThat(res, hasItems((Object) expectedNewFoo, (Object) foo2));
    }

    @Test
    public void shouldGenerateFunctionWithFilterListAliasAndOnlyElement2() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bar Bar (1..1)
        	output:
        		updatedBar Bar (1..1)
        \t
        	add updatedBar -> foos:
        		bar -> foos\s
        			extract [ if item -> include = True then Create_Foo( item -> include, Create_Attr( item -> attr, "_bar" ) ) else item ]
        
        func Create_Foo:
        	inputs:
        		include boolean (1..1)
        		attr string (1..1)
        	output:
        		foo Foo (1..1)
        \t
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
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "foo");
        var foo2 = createFoo(classes, false, "foo");

        var bar = createBar(classes, ImmutableList.of(foo1, foo2, foo2));

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, bar);

        var expectedBar = createBar(classes, ImmutableList.of(createFoo(classes, true, "foo_bar"), foo2, foo2));

        assertEquals(expectedBar, res);
    }

    @Test
    public void shouldGenerateFunctionWithFilterListAndOnlyElement() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		filteredFoosOnlyElement Foo (0..1)
        \t
        	set filteredFoosOnlyElement:
        		foos\s
        			filter fooItem [ fooItem -> include = True ]
        			then only-element
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, false, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, fooList);
        assertEquals(foo1, res);
    }

    @Test
    public void shouldGenerateFunctionWithFilterListAndDistinct() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		filteredFoosDistinct Foo (0..*)
        \t
        	set filteredFoosDistinct:
        		foos\s
        			filter fooItem [ fooItem -> include = True ]
        			then distinct
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, true, "b");
        var foo4 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);
        fooList.add(foo4);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) foo2));
    }

    @Test
    @Disabled // Add syntax support
    public void shouldGenerateFunctionWithFilterListAndPath() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		filteredFooAttr string (0..*)
        \t
        	set filteredFooAttr:
        		foos\s
        			filter fooItem [ fooItem -> include = True ]
        				-> attr
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) "a", (Object) "b"));
    }

    @Test
    public void shouldGenerateFunctionWithNestedFilters() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		filteredBars Bar (0..*)
        \t
        	set filteredBars:
        		bars\s
        			filter bar [ bar -> foos\s
        				filter foo [ foo -> include = True ]\s
        					then count = 2 ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "foo");
        var foo2 = createFoo(classes, false, "foo");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo2));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1, foo1, foo1));
        var bar4 = createBar(classes, ImmutableList.of(foo2, foo1, foo1));

        var barList = new ArrayList<>();
        barList.add(bar1);
        barList.add(bar2);
        barList.add(bar3);
        barList.add(bar4);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, barList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) bar2, (Object) bar4));
    }

    @Test
    public void shouldGenerateFunctionWithExtractList() {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		strings string (0..*)
        \t
        	set strings:
        		foos\s
        			extract [ item -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        	* @param foos\s
        	* @return strings\s
        	*/
        	public List<String> evaluate(List<? extends Foo> foos) {
        		List<String> strings = doEvaluate(foos);
        \t\t
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
        \t\t
        		protected List<String> assignOutput(List<String> strings, List<? extends Foo> foos) {
        			strings = MapperC.<Foo>of(foos)
        				.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
        \t\t\t
        			return strings;
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) "a", (Object) "b", (Object) "c"));
    }

    @Test
    public void shouldGenerateFunctionWithExtractList2() {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		strings string (0..*)
        \t
        	set strings:
        		foos\s
        			extract foo [ foo -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) "a", (Object) "b", (Object) "c"));
    }

    @Test
    public void shouldGenerateFunctionWithExtractListOfListThenExtractToListOfCounts() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		fooCounts int (0..*)
        \t
        	set fooCounts:
        		bars\s
        			extract bar [ bar -> foos ]
        			then extract fooListItem [ fooListItem count ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        	* @param bars\s
        	* @return fooCounts\s
        	*/
        	public List<Integer> evaluate(List<? extends Bar> bars) {
        		List<Integer> fooCounts = doEvaluate(bars);
        \t\t
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
        \t\t
        		protected List<Integer> assignOutput(List<Integer> fooCounts, List<? extends Bar> bars) {
        			final MapperListOfLists<Foo> thenArg = MapperC.<Bar>of(bars)
        				.mapItemToList(bar -> bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos()));
        			fooCounts = thenArg
        				.mapListToItem(fooListItem -> MapperS.of(fooListItem.resultCount())).getMulti();
        \t\t\t
        			return fooCounts;
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) 3, (Object) 2, (Object) 1));
    }

    @Test
    public void shouldGenerateFunctionWithExtractListOfListThenExtractToListOfCounts2() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		fooCounts int (0..*)
        \t
        	set fooCounts:
        		bars\s
        			extract [ item -> foos ]
        			then extract [ item count ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) 3, (Object) 2, (Object) 1));
    }

    @Test
    public void shouldGenerateFunctionWithExtractListOfListThenFilterOnCount() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		fooCounts int (0..*)
        \t
        	set fooCounts:
        		bars\s
        			extract [ item -> foos ]
        			then filter [ item count > 1 ]
        			then extract [ item count ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) 3, (Object) 2));
    }

    @Test
    public void shouldGenerateFunctionWithMapListOfListThenFilterOnCount2() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		fooCounts int (0..*)
        \t
        	set fooCounts:
        		bars\s
        			extract a [ a -> foos ]
        			then filter b [ b count > 1 ]
        			then extract c [ c count ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) 3, (Object) 2));
    }

    @Test
    public void shouldGenerateFunctionWithExtractListOfListsThenFlatten() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		foos Foo (0..*)
        \t
        	set foos:
        		bars\s
        			extract bar [ bar -> foos ]
        			then flatten
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        
        	/**
        	* @param bars\s
        	* @return foos\s
        	*/
        	public List<? extends Foo> evaluate(List<? extends Bar> bars) {
        		List<Foo.FooBuilder> foosBuilder = doEvaluate(bars);
        \t\t
        		final List<? extends Foo> foos;
        		if (foosBuilder == null) {
        			foos = null;
        		} else {
        			foos = foosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        			objectValidator.validate(Foo.class, foos);
        		}
        \t\t
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
        \t\t
        		protected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> foos, List<? extends Bar> bars) {
        			final MapperListOfLists<Foo> thenArg = MapperC.<Bar>of(bars)
        				.mapItemToList(bar -> bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos()));
        			foos = toBuilder(thenArg
        				.flattenList().getMulti());
        \t\t\t
        			return Optional.ofNullable(foos)
        				.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");
        var foo5 = createFoo(classes, "e");
        var foo6 = createFoo(classes, "f");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo4, foo5));
        var bar3 = createBar(classes, ImmutableList.of(foo6));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(6, res.size());
        assertThat(res, hasItems((Object) foo1, (Object) foo2, (Object) foo3, (Object) foo4, (Object) foo5, (Object) foo6));
    }

    @Test
    public void shouldGenerateFunctionWithExtractListOfListsThenFlatten2() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		foos Foo (0..*)
        \t
        	set foos:
        		bars\s
        			extract [ item -> foos ]
        			then flatten
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");
        var foo5 = createFoo(classes, "e");
        var foo6 = createFoo(classes, "f");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo4, foo5));
        var bar3 = createBar(classes, ImmutableList.of(foo6));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(6, res.size());
        assertThat(res, hasItems((Object) foo1, (Object) foo2, (Object) foo3, (Object) foo4, (Object) foo5, (Object) foo6));
    }

    @Test
    public void shouldGenerateFunctionWithExtractListOfListsThenFlatten3() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		attrs string (0..*)
        \t
        	set attrs:
        		bars\s
        			extract [ item -> foos ]
        			then flatten
        			then extract [ item -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        	* @param bars\s
        	* @return attrs\s
        	*/
        	public List<String> evaluate(List<? extends Bar> bars) {
        		List<String> attrs = doEvaluate(bars);
        \t\t
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
        \t\t
        		protected List<String> assignOutput(List<String> attrs, List<? extends Bar> bars) {
        			final MapperListOfLists<Foo> thenArg0 = MapperC.<Bar>of(bars)
        				.mapItemToList(item -> item.<Foo>mapC("getFoos", bar -> bar.getFoos()));
        			final MapperC<Foo> thenArg1 = thenArg0
        				.flattenList();
        			attrs = thenArg1
        				.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
        \t\t\t
        			return attrs;
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");
        var foo5 = createFoo(classes, "e");
        var foo6 = createFoo(classes, "f");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo4, foo5));
        var bar3 = createBar(classes, ImmutableList.of(foo6));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(6, res.size());
        assertThat(res, hasItems((Object) "a", (Object) "b", (Object) "c", (Object) "d", (Object) "e", (Object) "f"));
    }

    @Test
    public void shouldGenerateFunctionWithExtractListCount() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		fooCounts int (0..*)
        \t
        	set fooCounts:
        		bars\s
        			extract [ item -> foos count ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) 3, (Object) 2, (Object) 1));
    }

    @Test
    public void shouldGenerateFunctionWithMapListCount2() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		fooCounts int (0..*)
        \t
        	set fooCounts:
        		bars\s
        			extract bar [ bar -> foos count ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) 3, (Object) 2, (Object) 1));
    }

    @Test
    public void shouldGenerateFunctionWithNestedExtracts() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		updatedBars Bar (0..*)
        \t
        	set updatedBars:
        		bars\s
        			extract bar [ bar -> foos\s
        				extract foo [ NewFoo( foo -> attr + "_bar" ) ]
        			]
        			then extract updatedFoos [ NewBar( updatedFoos ) ]
        
        func NewBar:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		bar Bar (1..1)
        \t
        	set bar -> foos:
        		foos
        
        func NewFoo:
        \s	inputs:
        \s		attr string (1..1)
        	output:
        		foo Foo (0..1)
        \t
        	set foo -> attr:
        		attr
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        \t
        	// RosettaFunction dependencies
        	//
        	@Inject protected NewBar newBar;
        	@Inject protected NewFoo newFoo;
        
        	/**
        	* @param bars\s
        	* @return updatedBars\s
        	*/
        	public List<? extends Bar> evaluate(List<? extends Bar> bars) {
        		List<Bar.BarBuilder> updatedBarsBuilder = doEvaluate(bars);
        \t\t
        		final List<? extends Bar> updatedBars;
        		if (updatedBarsBuilder == null) {
        			updatedBars = null;
        		} else {
        			updatedBars = updatedBarsBuilder.stream().map(Bar::build).collect(Collectors.toList());
        			objectValidator.validate(Bar.class, updatedBars);
        		}
        \t\t
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
        \t\t
        		protected List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> updatedBars, List<? extends Bar> bars) {
        			final MapperListOfLists<Foo> thenArg = MapperC.<Bar>of(bars)
        				.mapItemToList(bar -> bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos())
        					.mapItem(foo -> MapperS.of(newFoo.evaluate(MapperMaths.<String, String, String>add(foo.<String>map("getAttr", _foo -> _foo.getAttr()), MapperS.of("_bar")).get()))));
        			updatedBars = toBuilder(thenArg
        				.mapListToItem(updatedFoos -> MapperS.of(newBar.evaluate(updatedFoos.getMulti()))).getMulti());
        \t\t\t
        			return Optional.ofNullable(updatedBars)
        				.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(3, res.size());

        var expectedFoo1 = createFoo(classes, "a_bar");
        var expectedFoo2 = createFoo(classes, "b_bar");
        var expectedFoo3 = createFoo(classes, "c_bar");

        var expectedBar1 = createBar(classes, ImmutableList.of(expectedFoo1, expectedFoo2, expectedFoo3));
        var expectedBar2 = createBar(classes, ImmutableList.of(expectedFoo1, expectedFoo2));
        var expectedBar3 = createBar(classes, ImmutableList.of(expectedFoo1));

        assertThat(res, hasItems((Object) expectedBar1, (Object) expectedBar2, (Object) expectedBar3));
    }

    @Test
    public void shouldGenerateFunctionWithNestedMaps2() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		updatedBars Bar (0..*)
        \t
        	set updatedBars:
        		bars\s
        			extract bar [\s
        				NewBar( bar -> foos\s
        					extract foo [ NewFoo( foo -> attr + "_bar" ) ] )
        			]
        
        func NewBar:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		bar Bar (1..1)
        \t
        	set bar -> foos:
        		foos
        
        func NewFoo:
        \s	inputs:
        \s		attr string (1..1)
        	output:
        		foo Foo (0..1)
        \t
        	set foo -> attr:
        		attr
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        \t
        	// RosettaFunction dependencies
        	//
        	@Inject protected NewBar newBar;
        	@Inject protected NewFoo newFoo;
        
        	/**
        	* @param bars\s
        	* @return updatedBars\s
        	*/
        	public List<? extends Bar> evaluate(List<? extends Bar> bars) {
        		List<Bar.BarBuilder> updatedBarsBuilder = doEvaluate(bars);
        \t\t
        		final List<? extends Bar> updatedBars;
        		if (updatedBarsBuilder == null) {
        			updatedBars = null;
        		} else {
        			updatedBars = updatedBarsBuilder.stream().map(Bar::build).collect(Collectors.toList());
        			objectValidator.validate(Bar.class, updatedBars);
        		}
        \t\t
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
        \t\t
        		protected List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> updatedBars, List<? extends Bar> bars) {
        			updatedBars = toBuilder(MapperC.<Bar>of(bars)
        				.mapItem(bar -> MapperS.of(newBar.evaluate(bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos())
        					.mapItem(foo -> MapperS.of(newFoo.evaluate(MapperMaths.<String, String, String>add(foo.<String>map("getAttr", _foo -> _foo.getAttr()), MapperS.of("_bar")).get()))).getMulti()))).getMulti());
        \t\t\t
        			return Optional.ofNullable(updatedBars)
        				.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var bar1 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1));

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(bar1, bar2, bar3));
        assertEquals(3, res.size());

        var expectedFoo1 = createFoo(classes, "a_bar");
        var expectedFoo2 = createFoo(classes, "b_bar");
        var expectedFoo3 = createFoo(classes, "c_bar");

        var expectedBar1 = createBar(classes, ImmutableList.of(expectedFoo1, expectedFoo2, expectedFoo3));
        var expectedBar2 = createBar(classes, ImmutableList.of(expectedFoo1, expectedFoo2));
        var expectedBar3 = createBar(classes, ImmutableList.of(expectedFoo1));

        assertThat(res, hasItems((Object) expectedBar1, (Object) expectedBar2, (Object) expectedBar3));
    }

    @Test
    public void shouldGenerateFunctionWithExtractListModifyItemFunc() {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		updatedFoos Foo (0..*)
        \t
        	set updatedFoos:
        		foos\s
        			extract [ NewFoo( item -> attr + "_1" ) ]
        
        func NewFoo:
        \s	inputs:
        \s		attr string (1..1)
        	output:
        		foo Foo (0..1)
        \t
        	set foo -> attr:
        		attr
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(3, res.size());

        var expectedFoo1 = createFoo(classes, "a_1");
        var expectedFoo2 = createFoo(classes, "b_1");
        var expectedFoo3 = createFoo(classes, "c_1");

        assertThat(res, hasItems((Object) expectedFoo1, (Object) expectedFoo2, (Object) expectedFoo3));
    }

    @Test
    public void shouldGenerateFunctionWithFilterThenExtract() {
        String model = """
        type Foo:
        	include boolean (1..1)
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		newFoos string (0..*)
        \t
        	set newFoos:
        		foos\s
        			filter [ item -> include = True ]
        			then extract [ item -> attr ]
        
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        
        import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
        
        @ImplementedBy(FuncFoo.FuncFooDefault.class)
        public abstract class FuncFoo implements RosettaFunction {
        
        	/**
        	* @param foos\s
        	* @return newFoos\s
        	*/
        	public List<String> evaluate(List<? extends Foo> foos) {
        		List<String> newFoos = doEvaluate(foos);
        \t\t
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
        \t\t
        		protected List<String> assignOutput(List<String> newFoos, List<? extends Foo> foos) {
        			final MapperC<Foo> thenArg = MapperC.<Foo>of(foos)
        				.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).get());
        			newFoos = thenArg
        				.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
        \t\t\t
        			return newFoos;
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, true, "a");
        var foo2 = createFoo(classes, true, "b");
        var foo3 = createFoo(classes, false, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList);
        assertEquals(2, res.size());
        assertThat(res, hasItems((Object) "a", (Object) "b"));
    }

    @Test
    public void shouldGenerateFunctionWithSameNamespace() {
        String model = """
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
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		strings string (0..*)
        \t
        	set strings:
        		bars\s
        			extract [ GetFoo( item -> barAttr ) ]
        			then extract [ item -> fooAttr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("ns1.functions.FuncFoo"));
        assertEquals("""
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
        \t
        	// RosettaFunction dependencies
        	//
        	@Inject protected GetFoo getFoo;
        
        	/**
        	* @param bars\s
        	* @return strings\s
        	*/
        	public List<String> evaluate(List<? extends Bar> bars) {
        		List<String> strings = doEvaluate(bars);
        \t\t
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
        \t\t
        		protected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
        			final MapperC<Foo> thenArg = MapperC.<Bar>of(bars)
        				.mapItem(item -> MapperS.of(getFoo.evaluate(item.<String>map("getBarAttr", bar -> bar.getBarAttr()).get())));
        			strings = thenArg
        				.mapItem(item -> item.<String>map("getFooAttr", foo -> foo.getFooAttr())).getMulti();
        \t\t\t
        			return strings;
        		}
        	}
        }
        """, f);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    public void shouldGenerateFunctionWithDifferentNamespace() {
        String model0 = """
        namespace ns1
        
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        """;
        String model1 = """
        namespace ns2
        
        import ns1.*
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		strings string (0..*)
        \t
        	set strings:
        		bars\s
        			extract [ item -> foos ]
        			then flatten
        			then extract [ item -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model0, model1);
        String f = normalize(code.get("ns2.functions.FuncFoo"));
        assertEquals("""
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
        	* @param bars\s
        	* @return strings\s
        	*/
        	public List<String> evaluate(List<? extends Bar> bars) {
        		List<String> strings = doEvaluate(bars);
        \t\t
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
        \t\t
        		protected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
        			final MapperListOfLists<Foo> thenArg0 = MapperC.<Bar>of(bars)
        				.mapItemToList(item -> item.<Foo>mapC("getFoos", bar -> bar.getFoos()));
        			final MapperC<Foo> thenArg1 = thenArg0
        				.flattenList();
        			strings = thenArg1
        				.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
        \t\t\t
        			return strings;
        		}
        	}
        }
        """, f);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    public void shouldGenerateFunctionWithDifferentNamespace2() {
        String model0 = """
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
        """;
        String model1 = """
        namespace ns2
        
        import ns1.*
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		strings string (0..*)
        \t
        	set strings:
        		bars\s
        			extract [ GetFoo( item -> barAttr ) ]
        			then extract [ item -> fooAttr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model0, model1);
        String f = normalize(code.get("ns2.functions.FuncFoo"));
        assertEquals("""
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
        \t
        	// RosettaFunction dependencies
        	//
        	@Inject protected GetFoo getFoo;
        
        	/**
        	* @param bars\s
        	* @return strings\s
        	*/
        	public List<String> evaluate(List<? extends Bar> bars) {
        		List<String> strings = doEvaluate(bars);
        \t\t
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
        \t\t
        		protected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
        			final MapperC<Foo> thenArg = MapperC.<Bar>of(bars)
        				.mapItem(item -> MapperS.of(getFoo.evaluate(item.<String>map("getBarAttr", bar -> bar.getBarAttr()).get())));
        			strings = thenArg
        				.mapItem(item -> item.<String>map("getFooAttr", foo -> foo.getFooAttr())).getMulti();
        \t\t\t
        			return strings;
        		}
        	}
        }
        """, f);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    public void shouldGenerateFunctionWithDifferentNamespace3() {
        String model0 = """
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
        """;
        String model1 = """
        namespace ns2
        
        import ns1.*
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		strings string (0..*)
        \t
        	set strings:
        		bars\s
        			extract [ GetFoo( GetBaz( item -> barAttr ) ) ]
        			then extract [ item -> fooAttr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model0, model1);
        String f = normalize(code.get("ns2.functions.FuncFoo"));
        assertEquals("""
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
        \t
        	// RosettaFunction dependencies
        	//
        	@Inject protected GetBaz getBaz;
        	@Inject protected GetFoo getFoo;
        
        	/**
        	* @param bars\s
        	* @return strings\s
        	*/
        	public List<String> evaluate(List<? extends Bar> bars) {
        		List<String> strings = doEvaluate(bars);
        \t\t
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
        \t\t
        		protected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
        			final MapperC<Foo> thenArg = MapperC.<Bar>of(bars)
        				.mapItem(item -> MapperS.of(getFoo.evaluate(getBaz.evaluate(item.<String>map("getBarAttr", bar -> bar.getBarAttr()).get()))));
        			strings = thenArg
        				.mapItem(item -> item.<String>map("getFooAttr", foo -> foo.getFooAttr())).getMulti();
        \t\t\t
        			return strings;
        		}
        	}
        }
        """, f);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    public void shouldGenerateListWithinIf() {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        \s		test string (1..1)
        	output:
        		strings string (0..*)
        \t
        	set strings:
        		if test = "a"
        		then foos extract [ item -> attr + "_a" ]
        		else if test = "b"
        		then foos extract [ item -> attr + "_b" ]
        		else if test = "c"
        		then foos extract [ item -> attr + "_c" ]
        		// default else
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        
        import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
        
        @ImplementedBy(FuncFoo.FuncFooDefault.class)
        public abstract class FuncFoo implements RosettaFunction {
        
        	/**
        	* @param foos\s
        	* @param test\s
        	* @return strings\s
        	*/
        	public List<String> evaluate(List<? extends Foo> foos, String test) {
        		List<String> strings = doEvaluate(foos, test);
        \t\t
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
        \t\t
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
        \t\t\t
        			return strings;
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, fooList, "b");
        assertEquals(3, res.size());
        assertThat(res, hasItems((Object) "a_b", (Object) "b_b", (Object) "c_b"));
    }

    @Test
    public void shouldGenerateListJoin() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		stringList string (0..*)
        	output:
        		concatenatedString string (1..1)
        \t
        	set concatenatedString:
        		stringList
        			join
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var stringList = new ArrayList<>();
        stringList.add("a");
        stringList.add("b");
        stringList.add("c");
        stringList.add("d");
        stringList.add("e");

        var res = functionGeneratorHelper.invokeFunc(func, String.class, stringList);
        assertEquals("abcde", res);
    }

    @Test
    public void shouldGenerateListJoinWithDelimiter() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		stringList string (0..*)
        	output:
        		concatenatedString string (1..1)
        \t
        	set concatenatedString:
        		stringList
        			join "_"
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var stringList = new ArrayList<>();
        stringList.add("a");
        stringList.add("b");
        stringList.add("c");
        stringList.add("d");
        stringList.add("e");

        var res = functionGeneratorHelper.invokeFunc(func, String.class, stringList);
        assertEquals("a_b_c_d_e", res);
    }

    @Test
    public void shouldGenerateListReduceString() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		stringList string (0..*)
        	output:
        		concatenatedString string (1..1)
        \t
        	set concatenatedString:
        		stringList
        			reduce a, b [ a + b ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        	* @param stringList\s
        	* @return concatenatedString\s
        	*/
        	public String evaluate(List<String> stringList) {
        		String concatenatedString = doEvaluate(stringList);
        \t\t
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
        \t\t
        		protected String assignOutput(String concatenatedString, List<String> stringList) {
        			concatenatedString = MapperC.<String>of(stringList)
        				.<String>reduce((a, b) -> MapperMaths.<String, String, String>add(a, b)).get();
        \t\t\t
        			return concatenatedString;
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var stringList = new ArrayList<>();
        stringList.add("a");
        stringList.add("b");
        stringList.add("c");
        stringList.add("d");
        stringList.add("e");

        var res = functionGeneratorHelper.invokeFunc(func, String.class, stringList);
        assertEquals("abcde", res);
    }



    @Test
    public void shouldGenerateListSumInt() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		intList int (0..*)
        	output:
        		total int (1..1)
        \t
        	set total:
        		intList
        			sum
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();
        intList.add(1);
        intList.add(3);
        intList.add(5);
        intList.add(7);
        intList.add(11);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertEquals(27, res);
    }

    @Test
    public void shouldGenerateListSumBigDecimal() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		numberList number (0..*)
        	output:
        		total number (1..1)
        \t
        	set total:
        		numberList
        			sum
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var numberList = new ArrayList<>();
        numberList.add(BigDecimal.valueOf(1.1));
        numberList.add(BigDecimal.valueOf(3.1));
        numberList.add(BigDecimal.valueOf(5.1));
        numberList.add(BigDecimal.valueOf(7.1));
        numberList.add(BigDecimal.valueOf(11.1));

        var res = functionGeneratorHelper.invokeFunc(func, BigDecimal.class, numberList);
        assertEquals(BigDecimal.valueOf(27.5), res);
    }

    @Test
    public void shouldGenerateListReduceSum() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		intList int (0..*)
        	output:
        		total int (1..1)
        \t
        	set total:
        		intList
        			reduce a, b [ a + b ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();
        intList.add(1);
        intList.add(3);
        intList.add(5);
        intList.add(7);
        intList.add(11);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertEquals(27, res);
    }

    @Test
    public void shouldGenerateListFirstInt() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		intList int (0..*)
        	output:
        		firstInt int (1..1)
        \t
        	set firstInt:
        		intList
        			first
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();
        intList.add(1);
        intList.add(2);
        intList.add(3);
        intList.add(4);
        intList.add(5);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertEquals(1, res);
    }

    @Test
    public void shouldGenerateListFirstComplexType() {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		fooList Foo (0..*)
        	output:
        		firstFoo Foo (1..1)
        \t
        	set firstFoo:
        		fooList
        			first
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");
        var foo5 = createFoo(classes, "e");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);
        fooList.add(foo4);
        fooList.add(foo5);

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, fooList);
        assertEquals(foo1, res);
    }

    @Test
    public void shouldGenerateListFirstComplexTypeEmptyList() {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		fooList Foo (0..*)
        	output:
        		firstFoo Foo (1..1)
        \t
        	set firstFoo:
        		fooList
        			first
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, new ArrayList<>());
        assertNull(res);
    }

    @Test
    public void shouldGenerateListLastInt() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		intList int (0..*)
        	output:
        		lastInt int (1..1)
        \t
        	set lastInt:
        		intList
        			last
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();
        intList.add(1);
        intList.add(2);
        intList.add(3);
        intList.add(4);
        intList.add(5);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertEquals(5, res);
    }

    @Test
    public void shouldGenerateListLastComplexType() {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		fooList Foo (0..*)
        	output:
        		lastFoo Foo (1..1)
        \t
        	set lastFoo:
        		fooList
        			last
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");
        var foo5 = createFoo(classes, "e");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);
        fooList.add(foo4);
        fooList.add(foo5);

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, fooList);
        assertEquals(foo5, res);
    }
    @Test
    public void shouldGenerateListReduceSubtract() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		intList int (0..*)
        	output:
        		total int (1..1)
        \t
        	set total:
        		intList
        			reduce a, b [ a - b ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();
        intList.add(10);
        intList.add(7);
        intList.add(1);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertEquals(2, res);

        var intList2 = new ArrayList<>();
        intList2.add(1);
        intList2.add(7);
        intList2.add(10);

        var res2 = functionGeneratorHelper.invokeFunc(func, Integer.class, intList2);
        assertEquals(-16, res2);
    }

    @Test
    public void shouldGenerateEmptyListReduceSum() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		numberList int (0..*)
        	output:
        		total int (1..1)
        \t
        	set total:
        		numberList
        			reduce a, b [ a + b ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertNull(res);
    }

    @Test
    public void shouldGenerateListReduceProduct() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		numberList int (0..*)
        	output:
        		total int (1..1)
        \t
        	set total:
        		numberList
        			reduce a, b [ a * b ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();
        intList.add(1);
        intList.add(3);
        intList.add(5);
        intList.add(7);
        intList.add(11);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertEquals(1155, res);
    }

    @Test
    public void shouldGenerateListReduceMaxNumber() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		numberList int (0..*)
        	output:
        		total int (1..1)
        \t
        	set total:
        		numberList
        			reduce a, b [ if a > b then a else b ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();
        intList.add(1);
        intList.add(3);
        intList.add(5);
        intList.add(7);
        intList.add(11);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertEquals(11, res);
    }

    @Test
    public void shouldGenerateListReduceMinNumber() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		numberList int (0..*)
        	output:
        		total int (1..1)
        \t
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
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();
        intList.add(1);
        intList.add(3);
        intList.add(5);
        intList.add(7);
        intList.add(11);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertEquals(1, res);
    }

    @Test
    public void shouldGenerateListReduceComplexType() throws Exception {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		foo Foo (1..1)
        \t
        	set foo:
        		foos
        			reduce foo1, foo2 [ Create_Foo( foo1 -> attr + foo2 -> attr ) ]
        
        func Create_Foo:
        \s	inputs:
        \s		attr string (1..1)
        	output:
        		foo Foo (1..1)
        \t
        	set foo -> attr: attr
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        \t
        	// RosettaFunction dependencies
        	//
        	@Inject protected Create_Foo create_Foo;
        
        	/**
        	* @param foos\s
        	* @return foo\s
        	*/
        	public Foo evaluate(List<? extends Foo> foos) {
        		Foo.FooBuilder fooBuilder = doEvaluate(foos);
        \t\t
        		final Foo foo;
        		if (fooBuilder == null) {
        			foo = null;
        		} else {
        			foo = fooBuilder.build();
        			objectValidator.validate(Foo.class, foo);
        		}
        \t\t
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
        \t\t
        		protected Foo.FooBuilder assignOutput(Foo.FooBuilder foo, List<? extends Foo> foos) {
        			foo = toBuilder(MapperC.<Foo>of(foos)
        				.<Foo>reduce((foo1, foo2) -> MapperS.of(create_Foo.evaluate(MapperMaths.<String, String, String>add(foo1.<String>map("getAttr", _foo -> _foo.getAttr()), foo2.<String>map("getAttr", _foo -> _foo.getAttr())).get()))).get());
        \t\t\t
        			return Optional.ofNullable(foo)
        				.map(o -> o.prune())
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var fooList = new ArrayList<>();
        fooList.add(createFoo(classes, "a"));
        fooList.add(createFoo(classes, "b"));
        fooList.add(createFoo(classes, "c"));
        fooList.add(createFoo(classes, "d"));
        fooList.add(createFoo(classes, "e"));

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, fooList);

        // reflective Foo.getAttr()
        String attr = (String) res.getClass().getMethod("getAttr").invoke(res);

        assertEquals("abcde", attr);
    }

    @Test
    public void shouldGenerateListReduceThenMapSingle() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		fooCount int (1..1)
        \t
        	set fooCount:
        		bars
        			reduce bar1, bar2 [ if bar1 -> foos count > bar2 -> foos count then bar1 else bar2 ]
        			then extract [ item -> foos count ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");

        var bar1 = createBar(classes, ImmutableList.of(foo1));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar4 = createBar(classes, ImmutableList.of(foo2, foo2, foo3, foo4));

        var barList = new ArrayList<>();
        barList.add(bar1);
        barList.add(bar2);
        barList.add(bar3);
        barList.add(bar4);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, barList);
        assertEquals(4, res);
    }

    @Test
    public void shouldGenerateListReduceThenExtractList() {
        String model = """
        type Bar:
        	foos Foo (0..*)
        
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		bars Bar (0..*)
        	output:
        		attrs string (0..*)
        \t
        	set attrs:
        		bars
        			reduce bar1, bar2 [ if bar1 -> foos count > bar2 -> foos count then bar1 else bar2 ] // max by foo count
        			then extract [ item -> foos ]
        			then extract [ item -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");

        var bar1 = createBar(classes, ImmutableList.of(foo1));
        var bar2 = createBar(classes, ImmutableList.of(foo1, foo2));
        var bar3 = createBar(classes, ImmutableList.of(foo1, foo2, foo3));
        var bar4 = createBar(classes, ImmutableList.of(foo1, foo2, foo3, foo4));

        var barList = new ArrayList<>();
        barList.add(bar1);
        barList.add(bar2);
        barList.add(bar3);
        barList.add(bar4);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, barList);
        assertEquals(4, res.size());
        assertThat(res, hasItems((Object) "a", (Object) "b", (Object) "c", (Object) "d"));
    }

    @Test
    public void shouldGenerateListMaxInt() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		intList int (0..*)
        	output:
        		result int (0..1)
        \t
        	set result:
        		intList
        			max
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var intList = new ArrayList<>();
        intList.add(1);
        intList.add(2);
        intList.add(3);
        intList.add(4);
        intList.add(5);

        var res = functionGeneratorHelper.invokeFunc(func, Integer.class, intList);
        assertEquals(5, res);
    }

    @Test
    public void shouldGenerateListMaxComplexType() {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		foo Foo (0..1)
        \t
        	set foo:
        		foos
        			max [ item -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");
        var foo5 = createFoo(classes, "e");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);
        fooList.add(foo4);
        fooList.add(foo5);

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, fooList);
        assertEquals(foo5, res);
    }

    @Test
    public void shouldGenerateListMinBigDecimal() {
        String model = """
        func FuncFoo:
        \s	inputs:
        \s		numberList number (0..*)
        	output:
        		result number (0..1)
        \t
        	set result:
        		numberList
        			min
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var numberList = new ArrayList<>();
        numberList.add(BigDecimal.valueOf(1.1));
        numberList.add(BigDecimal.valueOf(1.2));
        numberList.add(BigDecimal.valueOf(1.3));
        numberList.add(BigDecimal.valueOf(1.4));
        numberList.add(BigDecimal.valueOf(1.5));

        var res = functionGeneratorHelper.invokeFunc(func, BigDecimal.class, numberList);
        assertEquals(BigDecimal.valueOf(1.1), res);
    }

    @Test
    public void shouldGenerateListMinComplexType() {
        String model = """
        type Foo:
        	attr string (1..1)
        
        func FuncFoo:
        \s	inputs:
        \s		foos Foo (0..*)
        	output:
        		foo Foo (0..1)
        \t
        	set foo:
        		foos
        			min [ item -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");
        var foo5 = createFoo(classes, "e");

        var fooList = new ArrayList<>();
        fooList.add(foo1);
        fooList.add(foo2);
        fooList.add(foo3);
        fooList.add(foo4);
        fooList.add(foo5);

        var res = functionGeneratorHelper.invokeFunc(func, RosettaModelObject.class, fooList);
        assertEquals(foo1, res);
    }

    @Test
    public void shouldGenerateIntListSort() {
        String model = """
        func FuncFoo:\s
        	inputs:
        		numbers int (0..*)
        	output:
        		sortedNumbers int (0..*)
        
        	set sortedNumbers:
        		numbers sort // sort items
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        	* @param numbers\s
        	* @return sortedNumbers\s
        	*/
        	public List<Integer> evaluate(List<Integer> numbers) {
        		List<Integer> sortedNumbers = doEvaluate(numbers);
        \t\t
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
        \t\t
        		protected List<Integer> assignOutput(List<Integer> sortedNumbers, List<Integer> numbers) {
        			sortedNumbers = MapperC.<Integer>of(numbers)
        				.sort().getMulti();
        \t\t\t
        			return sortedNumbers;
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(4, 2, 3, 1));
        assertEquals(4, res.size());
        assertEquals(ImmutableList.of(1, 2, 3, 4), res);
    }

    @Test
    public void shouldGenerateDistinctIntListSort() {
        String model = """
        func FuncFoo:\s
        	inputs:
        		numbers int (0..*)
        	output:
        		sortedNumbers int (0..*)
        
        	set sortedNumbers:
        		numbers\s
        			distinct
        			sort
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(4, 2, 2, 4, 3, 1, 1, 3));
        assertEquals(4, res.size());
        assertEquals(ImmutableList.of(1, 2, 3, 4), res);
    }

    @Test
    public void shouldGenerateDateListSort() {
        String model = """
        func FuncFoo:\s
        	inputs:
        		dates date (0..*)
        	output:
        		sortedDates date (0..*)
        
        	set sortedDates:
        		dates sort // sort items
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var date1 = Date.of(2000, 1, 1);
        var date2 = Date.of(2000, 1, 2);
        var date3 = Date.of(2000, 2, 1);
        var date4 = Date.of(2001, 1, 1);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(date4, date1, date2, date3));
        assertEquals(4, res.size());
        assertEquals(ImmutableList.of(date1, date2, date3, date4), res);
    }

    @Test
    public void shouldGenerateListSortWithAttribute() {
        String model = """
        type Foo:
        	attr string (1..1) // single
        
        func FuncFoo:
        	inputs:
        		foos Foo (0..*)
        	output:
        		sortedFoos Foo (0..*)
        
        	set sortedFoos:
        		foos sort [item -> attr] // sort based on item attribute
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = normalize(code.get("com.rosetta.test.model.functions.FuncFoo"));
        assertEquals("""
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
        \t
        	@Inject protected ModelObjectValidator objectValidator;
        
        	/**
        	* @param foos\s
        	* @return sortedFoos\s
        	*/
        	public List<? extends Foo> evaluate(List<? extends Foo> foos) {
        		List<Foo.FooBuilder> sortedFoosBuilder = doEvaluate(foos);
        \t\t
        		final List<? extends Foo> sortedFoos;
        		if (sortedFoosBuilder == null) {
        			sortedFoos = null;
        		} else {
        			sortedFoos = sortedFoosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        			objectValidator.validate(Foo.class, sortedFoos);
        		}
        \t\t
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
        \t\t
        		protected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> sortedFoos, List<? extends Foo> foos) {
        			sortedFoos = toBuilder(MapperC.<Foo>of(foos)
        				.sort(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti());
        \t\t\t
        			return Optional.ofNullable(sortedFoos)
        				.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        				.orElse(null);
        		}
        	}
        }
        """, f);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");

        var fooList = new ArrayList<>();
        fooList.add(foo4);
        fooList.add(foo2);
        fooList.add(foo1);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(foo4, foo2, foo3, foo1));
        assertEquals(4, res.size());
        assertEquals(ImmutableList.of(foo1, foo2, foo3, foo4), res);
    }

    @Test
    public void shouldGenerateIntListReverse() {
        String model = """
        func FuncFoo:\s
        	inputs:
        		numbers int (0..*)
        	output:
        		sortedNumbers int (0..*)
        
        	set sortedNumbers:
        		numbers
        			reverse // reverse (no sort)
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(4, 2, 3, 1));
        assertEquals(4, res.size());
        assertEquals(ImmutableList.of(1, 3, 2, 4), res);
    }

    @Test
    public void shouldGenerateDateListSortThenReverse() {
        String model = """
        func FuncFoo:\s
        	inputs:
        		dates date (0..*)
        	output:
        		sortedDates date (0..*)
        
        	set sortedDates:
        		dates\s
        			sort // sort items
        			reverse
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var date1 = Date.of(2000, 1, 1);
        var date2 = Date.of(2000, 1, 2);
        var date3 = Date.of(2000, 2, 1);
        var date4 = Date.of(2001, 1, 1);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(date4, date1, date2, date3));
        assertEquals(4, res.size());
        assertEquals(ImmutableList.of(date4, date3, date2, date1), res);
    }

    @Test
    public void shouldGenerateListSortWithAttributeThenReverse() {
        String model = """
        type Foo:
        	attr string (1..1) // single
        
        func FuncFoo:
        	inputs:
        		foos Foo (0..*)
        	output:
        		sortedFoos Foo (0..*)
        
        	set sortedFoos:
        		foos\s
        			sort [item -> attr] // sort based on item attribute
        			reverse
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");

        var fooList = new ArrayList<>();
        fooList.add(foo4);
        fooList.add(foo2);
        fooList.add(foo1);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(foo4, foo2, foo3, foo1));
        assertEquals(4, res.size());
        assertEquals(ImmutableList.of(foo4, foo3, foo2, foo1), res);
    }

    @Test
    public void shouldGenerateListReverseComplexType() {
        String model = """
        type Foo:
        	attr string (1..1) // single
        
        func FuncFoo:
        	inputs:
        		foos Foo (0..*)
        	output:
        		sortedFoos Foo (0..*)
        
        	set sortedFoos:
        		foos\s
        			reverse
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var func = functionGeneratorHelper.createFunc(classes, "FuncFoo");

        var foo1 = createFoo(classes, "a");
        var foo2 = createFoo(classes, "b");
        var foo3 = createFoo(classes, "c");
        var foo4 = createFoo(classes, "d");

        var fooList = new ArrayList<>();
        fooList.add(foo4);
        fooList.add(foo2);
        fooList.add(foo1);
        fooList.add(foo3);

        List<Object> res = functionGeneratorHelper.invokeFunc(func, List.class, ImmutableList.of(foo4, foo2, foo3, foo1));
        assertEquals(4, res.size());
        assertEquals(ImmutableList.of(foo1, foo3, foo2, foo4), res);
    }

    private String normalize(String generatedCode) {
        // The legacy code generator emits platform line endings (\r\n on Windows), whereas the
        // expected values are Java text blocks, which the JLS normalizes to \n. Normalize before comparing.
        return generatedCode.replace("\r\n", "\n");
    }

    private RosettaModelObject createFoo(Map<String, Class<?>> classes, String attr) {
        return codeGeneratorTestHelper.createInstanceUsingBuilder(classes, "Foo", ImmutableMap.of("attr", attr), ImmutableMap.of());
    }

    private RosettaModelObject createFoo(Map<String, Class<?>> classes, boolean include, String attr) {
        return codeGeneratorTestHelper.createInstanceUsingBuilder(classes, "Foo", ImmutableMap.of("include", include, "attr", attr), ImmutableMap.of());
    }

    private RosettaModelObject createFoo2(Map<String, Class<?>> classes, boolean include, boolean include2, String attr) {
        return codeGeneratorTestHelper.createInstanceUsingBuilder(classes, "Foo2", ImmutableMap.of("include", include, "include2", include2, "attr", attr), ImmutableMap.of());
    }

    private RosettaModelObject createFooWithScheme(Map<String, Class<?>> classes, String attr, String scheme) {
        FieldWithMeta<String> fieldWithMetaString = codeGeneratorTestHelper.createFieldWithMetaString(classes, attr, scheme);
        return codeGeneratorTestHelper.createInstanceUsingBuilder(classes, "FooWithScheme", ImmutableMap.of("attr", fieldWithMetaString), ImmutableMap.of());
    }

    private RosettaModelObject createBar(Map<String, Class<?>> classes, List<RosettaModelObject> foos) {
        return codeGeneratorTestHelper.createInstanceUsingBuilder(classes, "Bar", ImmutableMap.of(), ImmutableMap.of("foos", foos));
    }
}
