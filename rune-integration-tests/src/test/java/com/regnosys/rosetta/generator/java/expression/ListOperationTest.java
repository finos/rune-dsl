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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfilteredFoos Foo (0..*)
        \t
        \tset filteredFoos:
        \t\tfoos\s
        \t\t\tfilter [ item -> include = True ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        
        \t/**
        \t* @param foos\s
        \t* @return filteredFoos\s
        \t*/
        \tpublic List<? extends Foo> evaluate(List<? extends Foo> foos) {
        \t\tList<Foo.FooBuilder> filteredFoosBuilder = doEvaluate(foos);
        \t\t
        \t\tfinal List<? extends Foo> filteredFoos;
        \t\tif (filteredFoosBuilder == null) {
        \t\t\tfilteredFoos = null;
        \t\t} else {
        \t\t\tfilteredFoos = filteredFoosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        \t\t\tobjectValidator.validate(Foo.class, filteredFoos);
        \t\t}
        \t\t
        \t\treturn filteredFoos;
        \t}
        
        \tprotected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Foo.FooBuilder> filteredFoos = new ArrayList<>();
        \t\t\treturn assignOutput(filteredFoos, foos);
        \t\t}
        \t\t
        \t\tprotected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
        \t\t\tfilteredFoos = toBuilder(MapperC.<Foo>of(foos)
        \t\t\t\t.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).get()).getMulti());
        \t\t\t
        \t\t\treturn Optional.ofNullable(filteredFoos)
        \t\t\t\t.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfilteredFoos Foo (0..*)
        \t
        \tset filteredFoos:
        \t\tfoos\s
        \t\t\tfilter fooItem [ fooItem -> include = True ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        
        \t/**
        \t* @param foos\s
        \t* @return filteredFoos\s
        \t*/
        \tpublic List<? extends Foo> evaluate(List<? extends Foo> foos) {
        \t\tList<Foo.FooBuilder> filteredFoosBuilder = doEvaluate(foos);
        \t\t
        \t\tfinal List<? extends Foo> filteredFoos;
        \t\tif (filteredFoosBuilder == null) {
        \t\t\tfilteredFoos = null;
        \t\t} else {
        \t\t\tfilteredFoos = filteredFoosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        \t\t\tobjectValidator.validate(Foo.class, filteredFoos);
        \t\t}
        \t\t
        \t\treturn filteredFoos;
        \t}
        
        \tprotected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Foo.FooBuilder> filteredFoos = new ArrayList<>();
        \t\t\treturn assignOutput(filteredFoos, foos);
        \t\t}
        \t\t
        \t\tprotected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> filteredFoos, List<? extends Foo> foos) {
        \t\t\tfilteredFoos = toBuilder(MapperC.<Foo>of(foos)
        \t\t\t\t.filterItemNullSafe(fooItem -> areEqual(fooItem.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).get()).getMulti());
        \t\t\t
        \t\t\treturn Optional.ofNullable(filteredFoos)
        \t\t\t\t.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tinclude boolean (1..1)
        \tinclude2 boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo2 (0..*)
        \toutput:
        \t\tfilteredFoos Foo2 (0..*)
        \t
        \tset filteredFoos:
        \t\tfoos\s
        \t\t\tfilter [ item -> include = True and item -> include2 = True ]
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
        \tinclude boolean (1..1)
        \tinclude2 boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo2 (0..*)
        \toutput:
        \t\tfilteredFoos Foo2 (0..*)
        \t
        \tset filteredFoos:
        \t\tfoos\s
        \t\t\tfilter [ item -> include = True ]
        \t\t\tthen filter [ item -> include2 = True ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        
        \t/**
        \t* @param foos\s
        \t* @return filteredFoos\s
        \t*/
        \tpublic List<? extends Foo2> evaluate(List<? extends Foo2> foos) {
        \t\tList<Foo2.Foo2Builder> filteredFoosBuilder = doEvaluate(foos);
        \t\t
        \t\tfinal List<? extends Foo2> filteredFoos;
        \t\tif (filteredFoosBuilder == null) {
        \t\t\tfilteredFoos = null;
        \t\t} else {
        \t\t\tfilteredFoos = filteredFoosBuilder.stream().map(Foo2::build).collect(Collectors.toList());
        \t\t\tobjectValidator.validate(Foo2.class, filteredFoos);
        \t\t}
        \t\t
        \t\treturn filteredFoos;
        \t}
        
        \tprotected abstract List<Foo2.Foo2Builder> doEvaluate(List<? extends Foo2> foos);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Foo2.Foo2Builder> doEvaluate(List<? extends Foo2> foos) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Foo2.Foo2Builder> filteredFoos = new ArrayList<>();
        \t\t\treturn assignOutput(filteredFoos, foos);
        \t\t}
        \t\t
        \t\tprotected List<Foo2.Foo2Builder> assignOutput(List<Foo2.Foo2Builder> filteredFoos, List<? extends Foo2> foos) {
        \t\t\tfinal MapperC<Foo2> thenArg = MapperC.<Foo2>of(foos)
        \t\t\t\t.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude", foo2 -> foo2.getInclude()), MapperS.of(true), CardinalityOperator.All).get());
        \t\t\tfilteredFoos = toBuilder(thenArg
        \t\t\t\t.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude2", foo2 -> foo2.getInclude2()), MapperS.of(true), CardinalityOperator.All).get()).getMulti());
        \t\t\t
        \t\t\treturn Optional.ofNullable(filteredFoos)
        \t\t\t\t.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tattr string (1..1)
        \t\t[metadata scheme]
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos FooWithScheme (0..*)
        \toutput:
        \t\tfilteredFoos FooWithScheme (0..*)
        \t
        \tset filteredFoos:
        \t\tfoos\s
        \t\t\tfilter [ item -> attr -> scheme = "foo-scheme" ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        
        \t/**
        \t* @param foos\s
        \t* @return filteredFoos\s
        \t*/
        \tpublic List<? extends FooWithScheme> evaluate(List<? extends FooWithScheme> foos) {
        \t\tList<FooWithScheme.FooWithSchemeBuilder> filteredFoosBuilder = doEvaluate(foos);
        \t\t
        \t\tfinal List<? extends FooWithScheme> filteredFoos;
        \t\tif (filteredFoosBuilder == null) {
        \t\t\tfilteredFoos = null;
        \t\t} else {
        \t\t\tfilteredFoos = filteredFoosBuilder.stream().map(FooWithScheme::build).collect(Collectors.toList());
        \t\t\tobjectValidator.validate(FooWithScheme.class, filteredFoos);
        \t\t}
        \t\t
        \t\treturn filteredFoos;
        \t}
        
        \tprotected abstract List<FooWithScheme.FooWithSchemeBuilder> doEvaluate(List<? extends FooWithScheme> foos);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<FooWithScheme.FooWithSchemeBuilder> doEvaluate(List<? extends FooWithScheme> foos) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tList<FooWithScheme.FooWithSchemeBuilder> filteredFoos = new ArrayList<>();
        \t\t\treturn assignOutput(filteredFoos, foos);
        \t\t}
        \t\t
        \t\tprotected List<FooWithScheme.FooWithSchemeBuilder> assignOutput(List<FooWithScheme.FooWithSchemeBuilder> filteredFoos, List<? extends FooWithScheme> foos) {
        \t\t\tfilteredFoos = toBuilder(MapperC.<FooWithScheme>of(foos)
        \t\t\t\t.filterItemNullSafe(item -> areEqual(item.<FieldWithMetaString>map("getAttr", fooWithScheme -> fooWithScheme.getAttr()).map("getMeta", a->a.getMeta()).map("getScheme", a->a.getScheme()), MapperS.of("foo-scheme"), CardinalityOperator.All).get()).getMulti());
        \t\t\t
        \t\t\treturn Optional.ofNullable(filteredFoos)
        \t\t\t\t.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tattr string (1..1)
        \t\t[metadata scheme]
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos FooWithScheme (0..*)
        \toutput:
        \t\tstrings string (0..*)
        \t
        \tset strings:
        \t\tfoos\s
        \t\t\tmap [ item -> attr ]
        \t\t\tfilter [ item -> scheme = "foo-scheme" ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        
        \t/**
        \t* @param foos\s
        \t* @return strings\s
        \t*/
        \tpublic List<String> evaluate(List<? extends FooWithScheme> foos) {
        \t\tList<String> stringsHolder = doEvaluate(foos);
        \t\tList<String> strings = assignOutput(stringsHolder, foos);
        \t\t
        \t\treturn strings;
        \t}
        \t
        \tprivate List<String> assignOutput(List<String> strings, List<? extends FooWithScheme> foos) {
        \t\tstrings = MapperC.of(foos)
        \t\t\t.mapItem(/*MapperS<? extends FooWithScheme>*/ __item -> (MapperS<String>) __item.<FieldWithMetaString>map("getAttr", _fooWithScheme -> _fooWithScheme.getAttr()).<String>map("getValue", _f->_f.getValue())).getMulti();
        \t\treturn strings;
        \t}
        
        \tprotected abstract List<String> doEvaluate(List<? extends FooWithScheme> foos);
        \t
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<String> doEvaluate(List<? extends FooWithScheme> foos) {
        \t\t\treturn new ArrayList<>();
        \t\t}
        \t}
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
        \s\tinputs:
        \s\t\tfoos boolean (0..*)
        \toutput:
        \t\tfilteredFoos boolean (0..*)
        \t
        \tset filteredFoos:
        \t\tfoos\s
        \t\t\tfilter [ item = True ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        
        \t/**
        \t* @param foos\s
        \t* @return filteredFoos\s
        \t*/
        \tpublic List<Boolean> evaluate(List<Boolean> foos) {
        \t\tList<Boolean> filteredFoos = doEvaluate(foos);
        \t\t
        \t\treturn filteredFoos;
        \t}
        
        \tprotected abstract List<Boolean> doEvaluate(List<Boolean> foos);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Boolean> doEvaluate(List<Boolean> foos) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Boolean> filteredFoos = new ArrayList<>();
        \t\t\treturn assignOutput(filteredFoos, foos);
        \t\t}
        \t\t
        \t\tprotected List<Boolean> assignOutput(List<Boolean> filteredFoos, List<Boolean> foos) {
        \t\t\tfilteredFoos = MapperC.<Boolean>of(foos)
        \t\t\t\t.filterItemNullSafe(item -> areEqual(item, MapperS.of(true), CardinalityOperator.All).get()).getMulti();
        \t\t\t
        \t\t\treturn filteredFoos;
        \t\t}
        \t}
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \s\t\ttest boolean (1..1)
        \toutput:
        \t\tfilteredFoos Foo (0..*)
        \t
        \tset filteredFoos:
        \t\tfoos\s
        \t\t\tfilter [ item -> include = test ]
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfilteredFoosCount int (1..1)
        \t
        \tset filteredFoosCount:
        \t\tfoos\s
        \t\t\tfilter fooItem [ fooItem -> include = True ]\s
        \t\t\tthen count
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfilteredFoos Foo (0..*)
        \t
        \tset filteredFoos:
        \t\tfoos\s
        \t\t\tfilter [ FuncFooTest( item ) ]
        \t\t\tthen filter [ FuncFooTest2( item ) ]
        
        func FuncFooTest:
        \s\tinputs:
        \s\t\tfoo Foo (1..1)
        \toutput:
        \t\tresult boolean (0..1)
        \t
        \tset result:
        \t\tfoo -> include
        
        func FuncFooTest2:
        \s\tinputs:
        \s\t\tfoo Foo (1..1)
        \toutput:
        \t\tresult boolean (0..1)
        \t
        \tset result:
        \t\tfoo -> include
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \s\t\ttest boolean (1..1)
        \toutput:
        \t\tfilteredFoos Foo (0..*)
        \t
        \talias testAlias:
        \t\ttest
        \t
        \tset filteredFoos:
        \t\tfoos\s
        \t\t\tfilter [ item -> include = testAlias ]
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfilteredFooAttrs string (0..*)
        \t
        \talias filteredFoosAlias:
        \t\tfoos\s
        \t\t\tfilter [ item -> include = True ]
        \t
        \tset filteredFooAttrs:
        \t\tfilteredFoosAlias -> attr
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
        \tinclude boolean (0..1)
        \tinclude2 boolean (0..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo2 (0..*)
        \s\t\ttest boolean (0..1)
        \s\t\ttest2 boolean (0..1)
        \s\t\ttest3 boolean (0..1)
        \toutput:
        \t\tfoo Foo2 (0..1)
        \t
        \talias filteredFoos:
        \t\tfoos\s
        \t\t\tfilter a [ if test exists then a -> include = test else True ]
        \t\t\tthen filter b [ if test2 exists then b -> include2 = test2 else True ]
        \t\t\tthen filter c [ if test3 exists then c -> include2 = test3 else True ]
        \t
        \tset foo:
        \t\tfilteredFoos only-element
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
        \tinclude boolean (0..1)
        \tinclude2 boolean (0..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo2 (0..*)
        \s\t\ttest boolean (0..1)
        \s\t\ttest2 boolean (0..1)
        \s\t\ttest3 boolean (0..1)
        \toutput:
        \t\tfoo Foo2 (0..1)
        \t
        \talias filteredFoos:
        \t\tfoos\s
        \t\t\tfilter [ if test exists then item -> include = test else True ]
        \t\t\tthen filter [ if test2 exists then item -> include2 = test2 else True ]
        \t\t\tthen filter [ if test3 exists then item -> include2 = test3 else True ]
        \t
        \tset foo:
        \t\tfilteredFoos only-element
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbar Bar (1..1)
        \toutput:
        \t\tfoos Foo (0..*)
        \t
        \tset foos:
        \t\tbar -> foos\s
        \t\t\textract [ if item -> include = True then Foo { include: include, attr: attr + "_bar" } else item ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        
        \t/**
        \t* @param bar\s
        \t* @return foos\s
        \t*/
        \tpublic List<? extends Foo> evaluate(Bar bar) {
        \t\tList<Foo.FooBuilder> foosBuilder = doEvaluate(bar);
        \t\t
        \t\tfinal List<? extends Foo> foos;
        \t\tif (foosBuilder == null) {
        \t\t\tfoos = null;
        \t\t} else {
        \t\t\tfoos = foosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        \t\t\tobjectValidator.validate(Foo.class, foos);
        \t\t}
        \t\t
        \t\treturn foos;
        \t}
        
        \tprotected abstract List<Foo.FooBuilder> doEvaluate(Bar bar);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Foo.FooBuilder> doEvaluate(Bar bar) {
        \t\t\tList<Foo.FooBuilder> foos = new ArrayList<>();
        \t\t\treturn assignOutput(foos, bar);
        \t\t}
        \t\t
        \t\tprotected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> foos, Bar bar) {
        \t\t\tfoos = toBuilder(MapperS.of(bar).<Foo>mapC("getFoos", _bar -> _bar.getFoos())
        \t\t\t\t.mapItem(item -> {
        \t\t\t\t\tif (areEqual(item.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).getOrDefault(false)) {
        \t\t\t\t\t\treturn MapperS.of(Foo.builder()
        \t\t\t\t\t\t\t.setInclude(item.<Boolean>map("getInclude", foo -> foo.getInclude()).get())
        \t\t\t\t\t\t\t.setAttr(MapperMaths.<String, String, String>add(item.<String>map("getAttr", foo -> foo.getAttr()), MapperS.of("_bar")).get())
        \t\t\t\t\t\t\t.build());
        \t\t\t\t\t}
        \t\t\t\t\treturn item;
        \t\t\t\t}).getMulti());
        \t\t\t
        \t\t\treturn Optional.ofNullable(foos)
        \t\t\t\t.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbar Bar (1..1)
        \toutput:
        \t\tupdatedBar Bar (1..1)
        \t
        \tadd updatedBar -> foos:
        \t\tbar -> foos\s
        \t\t\textract [ if item -> include = True then Create_Foo( item -> include, Create_Attr( item -> attr, "_bar" ) ) else item ]
        
        func Create_Foo:
        \tinputs:
        \t\tinclude boolean (1..1)
        \t\tattr string (1..1)
        \toutput:
        \t\tfoo Foo (1..1)
        \t
        \tset foo -> include: include
        \tset foo -> attr: attr
        
        func Create_Attr:
        \tinputs:
        \t\ts1 string (1..1)
        \t\ts2 string (1..1)
        \toutput:
        \t\tout string (1..1)
        \tset out:
        \t\ts1 + s2
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfilteredFoosOnlyElement Foo (0..1)
        \t
        \tset filteredFoosOnlyElement:
        \t\tfoos\s
        \t\t\tfilter fooItem [ fooItem -> include = True ]
        \t\t\tthen only-element
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfilteredFoosDistinct Foo (0..*)
        \t
        \tset filteredFoosDistinct:
        \t\tfoos\s
        \t\t\tfilter fooItem [ fooItem -> include = True ]
        \t\t\tthen distinct
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfilteredFooAttr string (0..*)
        \t
        \tset filteredFooAttr:
        \t\tfoos\s
        \t\t\tfilter fooItem [ fooItem -> include = True ]
        \t\t\t\t-> attr
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfilteredBars Bar (0..*)
        \t
        \tset filteredBars:
        \t\tbars\s
        \t\t\tfilter bar [ bar -> foos\s
        \t\t\t\tfilter foo [ foo -> include = True ]\s
        \t\t\t\t\tthen count = 2 ]
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
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tstrings string (0..*)
        \t
        \tset strings:
        \t\tfoos\s
        \t\t\textract [ item -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        
        \t/**
        \t* @param foos\s
        \t* @return strings\s
        \t*/
        \tpublic List<String> evaluate(List<? extends Foo> foos) {
        \t\tList<String> strings = doEvaluate(foos);
        \t\t
        \t\treturn strings;
        \t}
        
        \tprotected abstract List<String> doEvaluate(List<? extends Foo> foos);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<String> doEvaluate(List<? extends Foo> foos) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tList<String> strings = new ArrayList<>();
        \t\t\treturn assignOutput(strings, foos);
        \t\t}
        \t\t
        \t\tprotected List<String> assignOutput(List<String> strings, List<? extends Foo> foos) {
        \t\t\tstrings = MapperC.<Foo>of(foos)
        \t\t\t\t.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
        \t\t\t
        \t\t\treturn strings;
        \t\t}
        \t}
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
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tstrings string (0..*)
        \t
        \tset strings:
        \t\tfoos\s
        \t\t\textract foo [ foo -> attr ]
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfooCounts int (0..*)
        \t
        \tset fooCounts:
        \t\tbars\s
        \t\t\textract bar [ bar -> foos ]
        \t\t\tthen extract fooListItem [ fooListItem count ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        
        \t/**
        \t* @param bars\s
        \t* @return fooCounts\s
        \t*/
        \tpublic List<Integer> evaluate(List<? extends Bar> bars) {
        \t\tList<Integer> fooCounts = doEvaluate(bars);
        \t\t
        \t\treturn fooCounts;
        \t}
        
        \tprotected abstract List<Integer> doEvaluate(List<? extends Bar> bars);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Integer> doEvaluate(List<? extends Bar> bars) {
        \t\t\tif (bars == null) {
        \t\t\t\tbars = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Integer> fooCounts = new ArrayList<>();
        \t\t\treturn assignOutput(fooCounts, bars);
        \t\t}
        \t\t
        \t\tprotected List<Integer> assignOutput(List<Integer> fooCounts, List<? extends Bar> bars) {
        \t\t\tfinal MapperListOfLists<Foo> thenArg = MapperC.<Bar>of(bars)
        \t\t\t\t.mapItemToList(bar -> bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos()));
        \t\t\tfooCounts = thenArg
        \t\t\t\t.mapListToItem(fooListItem -> MapperS.of(fooListItem.resultCount())).getMulti();
        \t\t\t
        \t\t\treturn fooCounts;
        \t\t}
        \t}
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfooCounts int (0..*)
        \t
        \tset fooCounts:
        \t\tbars\s
        \t\t\textract [ item -> foos ]
        \t\t\tthen extract [ item count ]
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfooCounts int (0..*)
        \t
        \tset fooCounts:
        \t\tbars\s
        \t\t\textract [ item -> foos ]
        \t\t\tthen filter [ item count > 1 ]
        \t\t\tthen extract [ item count ]
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfooCounts int (0..*)
        \t
        \tset fooCounts:
        \t\tbars\s
        \t\t\textract a [ a -> foos ]
        \t\t\tthen filter b [ b count > 1 ]
        \t\t\tthen extract c [ c count ]
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfoos Foo (0..*)
        \t
        \tset foos:
        \t\tbars\s
        \t\t\textract bar [ bar -> foos ]
        \t\t\tthen flatten
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        
        \t/**
        \t* @param bars\s
        \t* @return foos\s
        \t*/
        \tpublic List<? extends Foo> evaluate(List<? extends Bar> bars) {
        \t\tList<Foo.FooBuilder> foosBuilder = doEvaluate(bars);
        \t\t
        \t\tfinal List<? extends Foo> foos;
        \t\tif (foosBuilder == null) {
        \t\t\tfoos = null;
        \t\t} else {
        \t\t\tfoos = foosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        \t\t\tobjectValidator.validate(Foo.class, foos);
        \t\t}
        \t\t
        \t\treturn foos;
        \t}
        
        \tprotected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Bar> bars);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Foo.FooBuilder> doEvaluate(List<? extends Bar> bars) {
        \t\t\tif (bars == null) {
        \t\t\t\tbars = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Foo.FooBuilder> foos = new ArrayList<>();
        \t\t\treturn assignOutput(foos, bars);
        \t\t}
        \t\t
        \t\tprotected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> foos, List<? extends Bar> bars) {
        \t\t\tfinal MapperListOfLists<Foo> thenArg = MapperC.<Bar>of(bars)
        \t\t\t\t.mapItemToList(bar -> bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos()));
        \t\t\tfoos = toBuilder(thenArg
        \t\t\t\t.flattenList().getMulti());
        \t\t\t
        \t\t\treturn Optional.ofNullable(foos)
        \t\t\t\t.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfoos Foo (0..*)
        \t
        \tset foos:
        \t\tbars\s
        \t\t\textract [ item -> foos ]
        \t\t\tthen flatten
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tattrs string (0..*)
        \t
        \tset attrs:
        \t\tbars\s
        \t\t\textract [ item -> foos ]
        \t\t\tthen flatten
        \t\t\tthen extract [ item -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        
        \t/**
        \t* @param bars\s
        \t* @return attrs\s
        \t*/
        \tpublic List<String> evaluate(List<? extends Bar> bars) {
        \t\tList<String> attrs = doEvaluate(bars);
        \t\t
        \t\treturn attrs;
        \t}
        
        \tprotected abstract List<String> doEvaluate(List<? extends Bar> bars);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<String> doEvaluate(List<? extends Bar> bars) {
        \t\t\tif (bars == null) {
        \t\t\t\tbars = Collections.emptyList();
        \t\t\t}
        \t\t\tList<String> attrs = new ArrayList<>();
        \t\t\treturn assignOutput(attrs, bars);
        \t\t}
        \t\t
        \t\tprotected List<String> assignOutput(List<String> attrs, List<? extends Bar> bars) {
        \t\t\tfinal MapperListOfLists<Foo> thenArg0 = MapperC.<Bar>of(bars)
        \t\t\t\t.mapItemToList(item -> item.<Foo>mapC("getFoos", bar -> bar.getFoos()));
        \t\t\tfinal MapperC<Foo> thenArg1 = thenArg0
        \t\t\t\t.flattenList();
        \t\t\tattrs = thenArg1
        \t\t\t\t.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
        \t\t\t
        \t\t\treturn attrs;
        \t\t}
        \t}
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfooCounts int (0..*)
        \t
        \tset fooCounts:
        \t\tbars\s
        \t\t\textract [ item -> foos count ]
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfooCounts int (0..*)
        \t
        \tset fooCounts:
        \t\tbars\s
        \t\t\textract bar [ bar -> foos count ]
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tupdatedBars Bar (0..*)
        \t
        \tset updatedBars:
        \t\tbars\s
        \t\t\textract bar [ bar -> foos\s
        \t\t\t\textract foo [ NewFoo( foo -> attr + "_bar" ) ]
        \t\t\t]
        \t\t\tthen extract updatedFoos [ NewBar( updatedFoos ) ]
        
        func NewBar:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tbar Bar (1..1)
        \t
        \tset bar -> foos:
        \t\tfoos
        
        func NewFoo:
        \s\tinputs:
        \s\t\tattr string (1..1)
        \toutput:
        \t\tfoo Foo (0..1)
        \t
        \tset foo -> attr:
        \t\tattr
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        \t
        \t// RosettaFunction dependencies
        \t//
        \t@Inject protected NewBar newBar;
        \t@Inject protected NewFoo newFoo;
        
        \t/**
        \t* @param bars\s
        \t* @return updatedBars\s
        \t*/
        \tpublic List<? extends Bar> evaluate(List<? extends Bar> bars) {
        \t\tList<Bar.BarBuilder> updatedBarsBuilder = doEvaluate(bars);
        \t\t
        \t\tfinal List<? extends Bar> updatedBars;
        \t\tif (updatedBarsBuilder == null) {
        \t\t\tupdatedBars = null;
        \t\t} else {
        \t\t\tupdatedBars = updatedBarsBuilder.stream().map(Bar::build).collect(Collectors.toList());
        \t\t\tobjectValidator.validate(Bar.class, updatedBars);
        \t\t}
        \t\t
        \t\treturn updatedBars;
        \t}
        
        \tprotected abstract List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars) {
        \t\t\tif (bars == null) {
        \t\t\t\tbars = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Bar.BarBuilder> updatedBars = new ArrayList<>();
        \t\t\treturn assignOutput(updatedBars, bars);
        \t\t}
        \t\t
        \t\tprotected List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> updatedBars, List<? extends Bar> bars) {
        \t\t\tfinal MapperListOfLists<Foo> thenArg = MapperC.<Bar>of(bars)
        \t\t\t\t.mapItemToList(bar -> bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos())
        \t\t\t\t\t.mapItem(foo -> MapperS.of(newFoo.evaluate(MapperMaths.<String, String, String>add(foo.<String>map("getAttr", _foo -> _foo.getAttr()), MapperS.of("_bar")).get()))));
        \t\t\tupdatedBars = toBuilder(thenArg
        \t\t\t\t.mapListToItem(updatedFoos -> MapperS.of(newBar.evaluate(updatedFoos.getMulti()))).getMulti());
        \t\t\t
        \t\t\treturn Optional.ofNullable(updatedBars)
        \t\t\t\t.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tupdatedBars Bar (0..*)
        \t
        \tset updatedBars:
        \t\tbars\s
        \t\t\textract bar [\s
        \t\t\t\tNewBar( bar -> foos\s
        \t\t\t\t\textract foo [ NewFoo( foo -> attr + "_bar" ) ] )
        \t\t\t]
        
        func NewBar:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tbar Bar (1..1)
        \t
        \tset bar -> foos:
        \t\tfoos
        
        func NewFoo:
        \s\tinputs:
        \s\t\tattr string (1..1)
        \toutput:
        \t\tfoo Foo (0..1)
        \t
        \tset foo -> attr:
        \t\tattr
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        \t
        \t// RosettaFunction dependencies
        \t//
        \t@Inject protected NewBar newBar;
        \t@Inject protected NewFoo newFoo;
        
        \t/**
        \t* @param bars\s
        \t* @return updatedBars\s
        \t*/
        \tpublic List<? extends Bar> evaluate(List<? extends Bar> bars) {
        \t\tList<Bar.BarBuilder> updatedBarsBuilder = doEvaluate(bars);
        \t\t
        \t\tfinal List<? extends Bar> updatedBars;
        \t\tif (updatedBarsBuilder == null) {
        \t\t\tupdatedBars = null;
        \t\t} else {
        \t\t\tupdatedBars = updatedBarsBuilder.stream().map(Bar::build).collect(Collectors.toList());
        \t\t\tobjectValidator.validate(Bar.class, updatedBars);
        \t\t}
        \t\t
        \t\treturn updatedBars;
        \t}
        
        \tprotected abstract List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Bar.BarBuilder> doEvaluate(List<? extends Bar> bars) {
        \t\t\tif (bars == null) {
        \t\t\t\tbars = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Bar.BarBuilder> updatedBars = new ArrayList<>();
        \t\t\treturn assignOutput(updatedBars, bars);
        \t\t}
        \t\t
        \t\tprotected List<Bar.BarBuilder> assignOutput(List<Bar.BarBuilder> updatedBars, List<? extends Bar> bars) {
        \t\t\tupdatedBars = toBuilder(MapperC.<Bar>of(bars)
        \t\t\t\t.mapItem(bar -> MapperS.of(newBar.evaluate(bar.<Foo>mapC("getFoos", _bar -> _bar.getFoos())
        \t\t\t\t\t.mapItem(foo -> MapperS.of(newFoo.evaluate(MapperMaths.<String, String, String>add(foo.<String>map("getAttr", _foo -> _foo.getAttr()), MapperS.of("_bar")).get()))).getMulti()))).getMulti());
        \t\t\t
        \t\t\treturn Optional.ofNullable(updatedBars)
        \t\t\t\t.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tupdatedFoos Foo (0..*)
        \t
        \tset updatedFoos:
        \t\tfoos\s
        \t\t\textract [ NewFoo( item -> attr + "_1" ) ]
        
        func NewFoo:
        \s\tinputs:
        \s\t\tattr string (1..1)
        \toutput:
        \t\tfoo Foo (0..1)
        \t
        \tset foo -> attr:
        \t\tattr
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
        \tinclude boolean (1..1)
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tnewFoos string (0..*)
        \t
        \tset newFoos:
        \t\tfoos\s
        \t\t\tfilter [ item -> include = True ]
        \t\t\tthen extract [ item -> attr ]
        
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        
        \t/**
        \t* @param foos\s
        \t* @return newFoos\s
        \t*/
        \tpublic List<String> evaluate(List<? extends Foo> foos) {
        \t\tList<String> newFoos = doEvaluate(foos);
        \t\t
        \t\treturn newFoos;
        \t}
        
        \tprotected abstract List<String> doEvaluate(List<? extends Foo> foos);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<String> doEvaluate(List<? extends Foo> foos) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tList<String> newFoos = new ArrayList<>();
        \t\t\treturn assignOutput(newFoos, foos);
        \t\t}
        \t\t
        \t\tprotected List<String> assignOutput(List<String> newFoos, List<? extends Foo> foos) {
        \t\t\tfinal MapperC<Foo> thenArg = MapperC.<Foo>of(foos)
        \t\t\t\t.filterItemNullSafe(item -> areEqual(item.<Boolean>map("getInclude", foo -> foo.getInclude()), MapperS.of(true), CardinalityOperator.All).get());
        \t\t\tnewFoos = thenArg
        \t\t\t\t.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
        \t\t\t
        \t\t\treturn newFoos;
        \t\t}
        \t}
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
        \tbarAttr string (1..1)
        
        type Foo:
        \tfooAttr string (1..1)
        
        func GetFoo:
        \tinputs:
        \t\tbarAttr string (1..1)
        \toutput:
        \t\tfoo Foo (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tstrings string (0..*)
        \t
        \tset strings:
        \t\tbars\s
        \t\t\textract [ GetFoo( item -> barAttr ) ]
        \t\t\tthen extract [ item -> fooAttr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("ns1.functions.FuncFoo");
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
        \t// RosettaFunction dependencies
        \t//
        \t@Inject protected GetFoo getFoo;
        
        \t/**
        \t* @param bars\s
        \t* @return strings\s
        \t*/
        \tpublic List<String> evaluate(List<? extends Bar> bars) {
        \t\tList<String> strings = doEvaluate(bars);
        \t\t
        \t\treturn strings;
        \t}
        
        \tprotected abstract List<String> doEvaluate(List<? extends Bar> bars);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<String> doEvaluate(List<? extends Bar> bars) {
        \t\t\tif (bars == null) {
        \t\t\t\tbars = Collections.emptyList();
        \t\t\t}
        \t\t\tList<String> strings = new ArrayList<>();
        \t\t\treturn assignOutput(strings, bars);
        \t\t}
        \t\t
        \t\tprotected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
        \t\t\tfinal MapperC<Foo> thenArg = MapperC.<Bar>of(bars)
        \t\t\t\t.mapItem(item -> MapperS.of(getFoo.evaluate(item.<String>map("getBarAttr", bar -> bar.getBarAttr()).get())));
        \t\t\tstrings = thenArg
        \t\t\t\t.mapItem(item -> item.<String>map("getFooAttr", foo -> foo.getFooAttr())).getMulti();
        \t\t\t
        \t\t\treturn strings;
        \t\t}
        \t}
        }
        """, f);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    public void shouldGenerateFunctionWithDifferentNamespace() {
        String model0 = """
        namespace ns1
        
        type Bar:
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        """;
        String model1 = """
        namespace ns2
        
        import ns1.*
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tstrings string (0..*)
        \t
        \tset strings:
        \t\tbars\s
        \t\t\textract [ item -> foos ]
        \t\t\tthen flatten
        \t\t\tthen extract [ item -> attr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model0, model1);
        String f = code.get("ns2.functions.FuncFoo");
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
        
        \t/**
        \t* @param bars\s
        \t* @return strings\s
        \t*/
        \tpublic List<String> evaluate(List<? extends Bar> bars) {
        \t\tList<String> strings = doEvaluate(bars);
        \t\t
        \t\treturn strings;
        \t}
        
        \tprotected abstract List<String> doEvaluate(List<? extends Bar> bars);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<String> doEvaluate(List<? extends Bar> bars) {
        \t\t\tif (bars == null) {
        \t\t\t\tbars = Collections.emptyList();
        \t\t\t}
        \t\t\tList<String> strings = new ArrayList<>();
        \t\t\treturn assignOutput(strings, bars);
        \t\t}
        \t\t
        \t\tprotected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
        \t\t\tfinal MapperListOfLists<Foo> thenArg0 = MapperC.<Bar>of(bars)
        \t\t\t\t.mapItemToList(item -> item.<Foo>mapC("getFoos", bar -> bar.getFoos()));
        \t\t\tfinal MapperC<Foo> thenArg1 = thenArg0
        \t\t\t\t.flattenList();
        \t\t\tstrings = thenArg1
        \t\t\t\t.mapItem(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti();
        \t\t\t
        \t\t\treturn strings;
        \t\t}
        \t}
        }
        """, f);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    public void shouldGenerateFunctionWithDifferentNamespace2() {
        String model0 = """
        namespace ns1
        
        type Bar:
        \tbarAttr string (1..1)
        
        type Foo:
        \tfooAttr string (1..1)
        
        func GetFoo:
        \tinputs:
        \t\tbarAttr string (1..1)
        \toutput:
        \t\tfoo Foo (1..1)
        """;
        String model1 = """
        namespace ns2
        
        import ns1.*
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tstrings string (0..*)
        \t
        \tset strings:
        \t\tbars\s
        \t\t\textract [ GetFoo( item -> barAttr ) ]
        \t\t\tthen extract [ item -> fooAttr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model0, model1);
        String f = code.get("ns2.functions.FuncFoo");
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
        \t// RosettaFunction dependencies
        \t//
        \t@Inject protected GetFoo getFoo;
        
        \t/**
        \t* @param bars\s
        \t* @return strings\s
        \t*/
        \tpublic List<String> evaluate(List<? extends Bar> bars) {
        \t\tList<String> strings = doEvaluate(bars);
        \t\t
        \t\treturn strings;
        \t}
        
        \tprotected abstract List<String> doEvaluate(List<? extends Bar> bars);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<String> doEvaluate(List<? extends Bar> bars) {
        \t\t\tif (bars == null) {
        \t\t\t\tbars = Collections.emptyList();
        \t\t\t}
        \t\t\tList<String> strings = new ArrayList<>();
        \t\t\treturn assignOutput(strings, bars);
        \t\t}
        \t\t
        \t\tprotected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
        \t\t\tfinal MapperC<Foo> thenArg = MapperC.<Bar>of(bars)
        \t\t\t\t.mapItem(item -> MapperS.of(getFoo.evaluate(item.<String>map("getBarAttr", bar -> bar.getBarAttr()).get())));
        \t\t\tstrings = thenArg
        \t\t\t\t.mapItem(item -> item.<String>map("getFooAttr", foo -> foo.getFooAttr())).getMulti();
        \t\t\t
        \t\t\treturn strings;
        \t\t}
        \t}
        }
        """, f);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    public void shouldGenerateFunctionWithDifferentNamespace3() {
        String model0 = """
        namespace ns1
        
        type Bar:
        \tbarAttr string (1..1)
        
        type Foo:
        \tfooAttr string (1..1)
        
        type Baz:
        \tfooAttr string (1..1)
        
        func GetFoo:
        \tinputs:
        \t\tbaz Baz (1..1)
        \toutput:
        \t\tfoo Foo (1..1)
        
        func GetBaz:
        \tinputs:
        \t\tattr string (1..1)
        \toutput:
        \t\tbaz Baz (1..1)
        """;
        String model1 = """
        namespace ns2
        
        import ns1.*
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tstrings string (0..*)
        \t
        \tset strings:
        \t\tbars\s
        \t\t\textract [ GetFoo( GetBaz( item -> barAttr ) ) ]
        \t\t\tthen extract [ item -> fooAttr ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model0, model1);
        String f = code.get("ns2.functions.FuncFoo");
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
        \t// RosettaFunction dependencies
        \t//
        \t@Inject protected GetBaz getBaz;
        \t@Inject protected GetFoo getFoo;
        
        \t/**
        \t* @param bars\s
        \t* @return strings\s
        \t*/
        \tpublic List<String> evaluate(List<? extends Bar> bars) {
        \t\tList<String> strings = doEvaluate(bars);
        \t\t
        \t\treturn strings;
        \t}
        
        \tprotected abstract List<String> doEvaluate(List<? extends Bar> bars);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<String> doEvaluate(List<? extends Bar> bars) {
        \t\t\tif (bars == null) {
        \t\t\t\tbars = Collections.emptyList();
        \t\t\t}
        \t\t\tList<String> strings = new ArrayList<>();
        \t\t\treturn assignOutput(strings, bars);
        \t\t}
        \t\t
        \t\tprotected List<String> assignOutput(List<String> strings, List<? extends Bar> bars) {
        \t\t\tfinal MapperC<Foo> thenArg = MapperC.<Bar>of(bars)
        \t\t\t\t.mapItem(item -> MapperS.of(getFoo.evaluate(getBaz.evaluate(item.<String>map("getBarAttr", bar -> bar.getBarAttr()).get()))));
        \t\t\tstrings = thenArg
        \t\t\t\t.mapItem(item -> item.<String>map("getFooAttr", foo -> foo.getFooAttr())).getMulti();
        \t\t\t
        \t\t\treturn strings;
        \t\t}
        \t}
        }
        """, f);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    public void shouldGenerateListWithinIf() {
        String model = """
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \s\t\ttest string (1..1)
        \toutput:
        \t\tstrings string (0..*)
        \t
        \tset strings:
        \t\tif test = "a"
        \t\tthen foos extract [ item -> attr + "_a" ]
        \t\telse if test = "b"
        \t\tthen foos extract [ item -> attr + "_b" ]
        \t\telse if test = "c"
        \t\tthen foos extract [ item -> attr + "_c" ]
        \t\t// default else
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        
        \t/**
        \t* @param foos\s
        \t* @param test\s
        \t* @return strings\s
        \t*/
        \tpublic List<String> evaluate(List<? extends Foo> foos, String test) {
        \t\tList<String> strings = doEvaluate(foos, test);
        \t\t
        \t\treturn strings;
        \t}
        
        \tprotected abstract List<String> doEvaluate(List<? extends Foo> foos, String test);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<String> doEvaluate(List<? extends Foo> foos, String test) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tList<String> strings = new ArrayList<>();
        \t\t\treturn assignOutput(strings, foos, test);
        \t\t}
        \t\t
        \t\tprotected List<String> assignOutput(List<String> strings, List<? extends Foo> foos, String test) {
        \t\t\tif (areEqual(MapperS.of(test), MapperS.of("a"), CardinalityOperator.All).getOrDefault(false)) {
        \t\t\t\tstrings = MapperC.<Foo>of(foos)
        \t\t\t\t\t.mapItem(item -> MapperMaths.<String, String, String>add(item.<String>map("getAttr", foo -> foo.getAttr()), MapperS.of("_a"))).getMulti();
        \t\t\t} else if (areEqual(MapperS.of(test), MapperS.of("b"), CardinalityOperator.All).getOrDefault(false)) {
        \t\t\t\tstrings = MapperC.<Foo>of(foos)
        \t\t\t\t\t.mapItem(item -> MapperMaths.<String, String, String>add(item.<String>map("getAttr", foo -> foo.getAttr()), MapperS.of("_b"))).getMulti();
        \t\t\t} else if (areEqual(MapperS.of(test), MapperS.of("c"), CardinalityOperator.All).getOrDefault(false)) {
        \t\t\t\tstrings = MapperC.<Foo>of(foos)
        \t\t\t\t\t.mapItem(item -> MapperMaths.<String, String, String>add(item.<String>map("getAttr", foo -> foo.getAttr()), MapperS.of("_c"))).getMulti();
        \t\t\t} else {
        \t\t\t\tstrings = Collections.<String>emptyList();
        \t\t\t}
        \t\t\t
        \t\t\treturn strings;
        \t\t}
        \t}
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
        \s\tinputs:
        \s\t\tstringList string (0..*)
        \toutput:
        \t\tconcatenatedString string (1..1)
        \t
        \tset concatenatedString:
        \t\tstringList
        \t\t\tjoin
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
        \s\tinputs:
        \s\t\tstringList string (0..*)
        \toutput:
        \t\tconcatenatedString string (1..1)
        \t
        \tset concatenatedString:
        \t\tstringList
        \t\t\tjoin "_"
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
        \s\tinputs:
        \s\t\tstringList string (0..*)
        \toutput:
        \t\tconcatenatedString string (1..1)
        \t
        \tset concatenatedString:
        \t\tstringList
        \t\t\treduce a, b [ a + b ]
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        
        \t/**
        \t* @param stringList\s
        \t* @return concatenatedString\s
        \t*/
        \tpublic String evaluate(List<String> stringList) {
        \t\tString concatenatedString = doEvaluate(stringList);
        \t\t
        \t\treturn concatenatedString;
        \t}
        
        \tprotected abstract String doEvaluate(List<String> stringList);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected String doEvaluate(List<String> stringList) {
        \t\t\tif (stringList == null) {
        \t\t\t\tstringList = Collections.emptyList();
        \t\t\t}
        \t\t\tString concatenatedString = null;
        \t\t\treturn assignOutput(concatenatedString, stringList);
        \t\t}
        \t\t
        \t\tprotected String assignOutput(String concatenatedString, List<String> stringList) {
        \t\t\tconcatenatedString = MapperC.<String>of(stringList)
        \t\t\t\t.<String>reduce((a, b) -> MapperMaths.<String, String, String>add(a, b)).get();
        \t\t\t
        \t\t\treturn concatenatedString;
        \t\t}
        \t}
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
        \s\tinputs:
        \s\t\tintList int (0..*)
        \toutput:
        \t\ttotal int (1..1)
        \t
        \tset total:
        \t\tintList
        \t\t\tsum
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
        \s\tinputs:
        \s\t\tnumberList number (0..*)
        \toutput:
        \t\ttotal number (1..1)
        \t
        \tset total:
        \t\tnumberList
        \t\t\tsum
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
        \s\tinputs:
        \s\t\tintList int (0..*)
        \toutput:
        \t\ttotal int (1..1)
        \t
        \tset total:
        \t\tintList
        \t\t\treduce a, b [ a + b ]
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
        \s\tinputs:
        \s\t\tintList int (0..*)
        \toutput:
        \t\tfirstInt int (1..1)
        \t
        \tset firstInt:
        \t\tintList
        \t\t\tfirst
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
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfooList Foo (0..*)
        \toutput:
        \t\tfirstFoo Foo (1..1)
        \t
        \tset firstFoo:
        \t\tfooList
        \t\t\tfirst
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
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfooList Foo (0..*)
        \toutput:
        \t\tfirstFoo Foo (1..1)
        \t
        \tset firstFoo:
        \t\tfooList
        \t\t\tfirst
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
        \s\tinputs:
        \s\t\tintList int (0..*)
        \toutput:
        \t\tlastInt int (1..1)
        \t
        \tset lastInt:
        \t\tintList
        \t\t\tlast
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
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfooList Foo (0..*)
        \toutput:
        \t\tlastFoo Foo (1..1)
        \t
        \tset lastFoo:
        \t\tfooList
        \t\t\tlast
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
        \s\tinputs:
        \s\t\tintList int (0..*)
        \toutput:
        \t\ttotal int (1..1)
        \t
        \tset total:
        \t\tintList
        \t\t\treduce a, b [ a - b ]
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
        \s\tinputs:
        \s\t\tnumberList int (0..*)
        \toutput:
        \t\ttotal int (1..1)
        \t
        \tset total:
        \t\tnumberList
        \t\t\treduce a, b [ a + b ]
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
        \s\tinputs:
        \s\t\tnumberList int (0..*)
        \toutput:
        \t\ttotal int (1..1)
        \t
        \tset total:
        \t\tnumberList
        \t\t\treduce a, b [ a * b ]
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
        \s\tinputs:
        \s\t\tnumberList int (0..*)
        \toutput:
        \t\ttotal int (1..1)
        \t
        \tset total:
        \t\tnumberList
        \t\t\treduce a, b [ if a > b then a else b ]
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
        \s\tinputs:
        \s\t\tnumberList int (0..*)
        \toutput:
        \t\ttotal int (1..1)
        \t
        \tset total:
        \t\tnumberList
        \t\t\treduce a, b [ Min( a, b ) ]
        
        func Min:
        \tinputs:
        \t\ta int (1..1)
        \t\tb int (1..1)
        \toutput:
        \t\tresult int (1..1)
        \tset result:
        \t\tif a > b then b else a
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
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfoo Foo (1..1)
        \t
        \tset foo:
        \t\tfoos
        \t\t\treduce foo1, foo2 [ Create_Foo( foo1 -> attr + foo2 -> attr ) ]
        
        func Create_Foo:
        \s\tinputs:
        \s\t\tattr string (1..1)
        \toutput:
        \t\tfoo Foo (1..1)
        \t
        \tset foo -> attr: attr
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        \t
        \t// RosettaFunction dependencies
        \t//
        \t@Inject protected Create_Foo create_Foo;
        
        \t/**
        \t* @param foos\s
        \t* @return foo\s
        \t*/
        \tpublic Foo evaluate(List<? extends Foo> foos) {
        \t\tFoo.FooBuilder fooBuilder = doEvaluate(foos);
        \t\t
        \t\tfinal Foo foo;
        \t\tif (fooBuilder == null) {
        \t\t\tfoo = null;
        \t\t} else {
        \t\t\tfoo = fooBuilder.build();
        \t\t\tobjectValidator.validate(Foo.class, foo);
        \t\t}
        \t\t
        \t\treturn foo;
        \t}
        
        \tprotected abstract Foo.FooBuilder doEvaluate(List<? extends Foo> foos);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected Foo.FooBuilder doEvaluate(List<? extends Foo> foos) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tFoo.FooBuilder foo = Foo.builder();
        \t\t\treturn assignOutput(foo, foos);
        \t\t}
        \t\t
        \t\tprotected Foo.FooBuilder assignOutput(Foo.FooBuilder foo, List<? extends Foo> foos) {
        \t\t\tfoo = toBuilder(MapperC.<Foo>of(foos)
        \t\t\t\t.<Foo>reduce((foo1, foo2) -> MapperS.of(create_Foo.evaluate(MapperMaths.<String, String, String>add(foo1.<String>map("getAttr", _foo -> _foo.getAttr()), foo2.<String>map("getAttr", _foo -> _foo.getAttr())).get()))).get());
        \t\t\t
        \t\t\treturn Optional.ofNullable(foo)
        \t\t\t\t.map(o -> o.prune())
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tfooCount int (1..1)
        \t
        \tset fooCount:
        \t\tbars
        \t\t\treduce bar1, bar2 [ if bar1 -> foos count > bar2 -> foos count then bar1 else bar2 ]
        \t\t\tthen extract [ item -> foos count ]
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
        \tfoos Foo (0..*)
        
        type Foo:
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tbars Bar (0..*)
        \toutput:
        \t\tattrs string (0..*)
        \t
        \tset attrs:
        \t\tbars
        \t\t\treduce bar1, bar2 [ if bar1 -> foos count > bar2 -> foos count then bar1 else bar2 ] // max by foo count
        \t\t\tthen extract [ item -> foos ]
        \t\t\tthen extract [ item -> attr ]
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
        \s\tinputs:
        \s\t\tintList int (0..*)
        \toutput:
        \t\tresult int (0..1)
        \t
        \tset result:
        \t\tintList
        \t\t\tmax
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
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfoo Foo (0..1)
        \t
        \tset foo:
        \t\tfoos
        \t\t\tmax [ item -> attr ]
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
        \s\tinputs:
        \s\t\tnumberList number (0..*)
        \toutput:
        \t\tresult number (0..1)
        \t
        \tset result:
        \t\tnumberList
        \t\t\tmin
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
        \tattr string (1..1)
        
        func FuncFoo:
        \s\tinputs:
        \s\t\tfoos Foo (0..*)
        \toutput:
        \t\tfoo Foo (0..1)
        \t
        \tset foo:
        \t\tfoos
        \t\t\tmin [ item -> attr ]
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
        \tinputs:
        \t\tnumbers int (0..*)
        \toutput:
        \t\tsortedNumbers int (0..*)
        
        \tset sortedNumbers:
        \t\tnumbers sort // sort items
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        
        \t/**
        \t* @param numbers\s
        \t* @return sortedNumbers\s
        \t*/
        \tpublic List<Integer> evaluate(List<Integer> numbers) {
        \t\tList<Integer> sortedNumbers = doEvaluate(numbers);
        \t\t
        \t\treturn sortedNumbers;
        \t}
        
        \tprotected abstract List<Integer> doEvaluate(List<Integer> numbers);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Integer> doEvaluate(List<Integer> numbers) {
        \t\t\tif (numbers == null) {
        \t\t\t\tnumbers = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Integer> sortedNumbers = new ArrayList<>();
        \t\t\treturn assignOutput(sortedNumbers, numbers);
        \t\t}
        \t\t
        \t\tprotected List<Integer> assignOutput(List<Integer> sortedNumbers, List<Integer> numbers) {
        \t\t\tsortedNumbers = MapperC.<Integer>of(numbers)
        \t\t\t\t.sort().getMulti();
        \t\t\t
        \t\t\treturn sortedNumbers;
        \t\t}
        \t}
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
        \tinputs:
        \t\tnumbers int (0..*)
        \toutput:
        \t\tsortedNumbers int (0..*)
        
        \tset sortedNumbers:
        \t\tnumbers\s
        \t\t\tdistinct
        \t\t\tsort
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
        \tinputs:
        \t\tdates date (0..*)
        \toutput:
        \t\tsortedDates date (0..*)
        
        \tset sortedDates:
        \t\tdates sort // sort items
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
        \tattr string (1..1) // single
        
        func FuncFoo:
        \tinputs:
        \t\tfoos Foo (0..*)
        \toutput:
        \t\tsortedFoos Foo (0..*)
        
        \tset sortedFoos:
        \t\tfoos sort [item -> attr] // sort based on item attribute
        """;
        var code = codeGeneratorTestHelper.generateCode(model);
        String f = code.get("com.rosetta.test.model.functions.FuncFoo");
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
        \t@Inject protected ModelObjectValidator objectValidator;
        
        \t/**
        \t* @param foos\s
        \t* @return sortedFoos\s
        \t*/
        \tpublic List<? extends Foo> evaluate(List<? extends Foo> foos) {
        \t\tList<Foo.FooBuilder> sortedFoosBuilder = doEvaluate(foos);
        \t\t
        \t\tfinal List<? extends Foo> sortedFoos;
        \t\tif (sortedFoosBuilder == null) {
        \t\t\tsortedFoos = null;
        \t\t} else {
        \t\t\tsortedFoos = sortedFoosBuilder.stream().map(Foo::build).collect(Collectors.toList());
        \t\t\tobjectValidator.validate(Foo.class, sortedFoos);
        \t\t}
        \t\t
        \t\treturn sortedFoos;
        \t}
        
        \tprotected abstract List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos);
        
        \tpublic static class FuncFooDefault extends FuncFoo {
        \t\t@Override
        \t\tprotected List<Foo.FooBuilder> doEvaluate(List<? extends Foo> foos) {
        \t\t\tif (foos == null) {
        \t\t\t\tfoos = Collections.emptyList();
        \t\t\t}
        \t\t\tList<Foo.FooBuilder> sortedFoos = new ArrayList<>();
        \t\t\treturn assignOutput(sortedFoos, foos);
        \t\t}
        \t\t
        \t\tprotected List<Foo.FooBuilder> assignOutput(List<Foo.FooBuilder> sortedFoos, List<? extends Foo> foos) {
        \t\t\tsortedFoos = toBuilder(MapperC.<Foo>of(foos)
        \t\t\t\t.sort(item -> item.<String>map("getAttr", foo -> foo.getAttr())).getMulti());
        \t\t\t
        \t\t\treturn Optional.ofNullable(sortedFoos)
        \t\t\t\t.map(o -> o.stream().map(i -> i.prune()).collect(Collectors.toList()))
        \t\t\t\t.orElse(null);
        \t\t}
        \t}
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
        \tinputs:
        \t\tnumbers int (0..*)
        \toutput:
        \t\tsortedNumbers int (0..*)
        
        \tset sortedNumbers:
        \t\tnumbers
        \t\t\treverse // reverse (no sort)
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
        \tinputs:
        \t\tdates date (0..*)
        \toutput:
        \t\tsortedDates date (0..*)
        
        \tset sortedDates:
        \t\tdates\s
        \t\t\tsort // sort items
        \t\t\treverse
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
        \tattr string (1..1) // single
        
        func FuncFoo:
        \tinputs:
        \t\tfoos Foo (0..*)
        \toutput:
        \t\tsortedFoos Foo (0..*)
        
        \tset sortedFoos:
        \t\tfoos\s
        \t\t\tsort [item -> attr] // sort based on item attribute
        \t\t\treverse
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
        \tattr string (1..1) // single
        
        func FuncFoo:
        \tinputs:
        \t\tfoos Foo (0..*)
        \toutput:
        \t\tsortedFoos Foo (0..*)
        
        \tset sortedFoos:
        \t\tfoos\s
        \t\t\treverse
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
