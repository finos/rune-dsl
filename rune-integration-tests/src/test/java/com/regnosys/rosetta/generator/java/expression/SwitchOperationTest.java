package com.regnosys.rosetta.generator.java.expression;

import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import java.util.List;
import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class SwitchOperationTest {

    @Inject
    private RosettaTestModelService modelService;
    @Inject
    private JavaTypeUtil typeUtil;

    @Test
    void switchOnTypesInAnotherNamespace() {
        var model1 = """
                namespace other
        
                type Baz:
        
                type Foo extends Baz:
                    someBoolean boolean (0..1)
        
                 type Bar extends Baz:
                    someBoolean boolean (0..1)
        """;

        var model2 = """
                import other.* as other
        
                func MyFunc:
                    inputs:
                        baz other.Baz (1..1)
                    output:
                        result string (0..1)
        
                    set result:
                        baz switch
                            other.Foo then "Foo",
                            other.Bar then "Bar",
                            default empty
        """;

        var model = modelService.toJavaTestModel(model2, model1).compile();

        var result = model.evaluateExpression(String.class, "MyFunc(other.Foo { someBoolean: True })");

        assertEquals("Foo", result);
    }

    @Test
    public void switchOnDataType() {
        var model = modelService.toJavaTestModel("""
            type Foo:
        
            type Bar extends Foo:
                barAttr int (1..1)
        
            type Qux extends Foo:
                quxAttr int (1..1)
        """).compile();

        Integer resultBar = model.evaluateExpression(Integer.class, """
            (if True then Bar { barAttr: 42 } else Foo {})
                switch
                    Bar then barAttr,
                    Qux then quxAttr,
                    default 0
        """);
        assertEquals(42, resultBar);

        Integer resultQux = model.evaluateExpression(Integer.class, """
            (if True then Qux { quxAttr: 12 } else Foo {})
                switch
                    Bar then barAttr,
                    Qux then quxAttr,
                    default 0
        """);
        assertEquals(12, resultQux);

        Integer resultNull = model.evaluateExpression(Integer.class, """
            (if False then Foo {}) // mimic null input
                switch
                    Bar then barAttr,
                    Qux then quxAttr,
                    default 0
        """);
        assertNull(resultNull);
    }

    @Test
    public void testIngoreMetaOnSwitchBasicTypeInputs() {
        Integer result = modelService.evaluateExpression(Integer.class, """
            "b" with-meta { scheme: "myScheme" }
                switch
                    "a" then 1,
                    "b" then 2,
                    default 3
        """);
        assertEquals(2, result);
    }

    @Test
    public void switchOnChoiceType() {
        var model = modelService.toJavaTestModel("""
            type Bar:
                barAttr int (1..1)
        
            type Qux:
                quxAttr int (1..1)
        
            choice Foo:
                Bar
                Qux
        """).compile();

        Integer resultBar = model.evaluateExpression(Integer.class, """
            Foo { Bar: Bar { barAttr: 42 }, ... }
                switch
                    Bar then barAttr,
                    Qux then quxAttr
        """);
        assertEquals(42, resultBar);

        Integer resultQux = model.evaluateExpression(Integer.class, """
            Foo { Qux: Qux { quxAttr: 12 }, ... }
                switch
                    Bar then barAttr,
                    Qux then quxAttr
        """);
        assertEquals(12, resultQux);

        Integer resultNull = model.evaluateExpression(Integer.class, """
            (if False then Foo { ... }) // mimic null input
                switch
                    Bar then barAttr,
                    Qux then quxAttr
        """);
        assertNull(resultNull);
    }

    @Test
    public void switchOnNestedType() {
        var model = modelService.toJavaTestModel("""
            choice Foo:
                Opt1
                Bar
        
            choice Bar:
                Opt2
                Opt3
                Opt4
        
            type Opt1:
                opt1Attr int (1..1)
        
            type Opt2:
                barAttr string (1..1)
                opt2Attr boolean (1..1)
        
            type Opt3:
                barAttr string (1..1)
                opt3Attr string (1..1)
        
            type Opt4:
                barAttr string (1..1)
                opt4Attr string (1..1)
        """).compile();

        Integer r1 = model.evaluateExpression(Integer.class, """
            Foo { Opt1: Opt1 { opt1Attr: 1 }, ... }
                switch
                    Opt1 then opt1Attr,
                    Opt2 then if opt2Attr then 2 else 0,
                    Bar then item ->> barAttr to-int
        """);
        assertEquals(1, r1);

        Integer r2 = model.evaluateExpression(Integer.class, """
            Foo { Bar: Bar { Opt2: Opt2 { barAttr: "-1", opt2Attr: True }, ... }, ... }
                switch
                    Opt1 then opt1Attr,
                    Opt2 then if opt2Attr then 2 else 0,
                    Bar then item ->> barAttr to-int
        """);
        assertEquals(2, r2);

        Integer r3 = model.evaluateExpression(Integer.class, """
            Foo { Bar: Bar { Opt3: Opt3 { barAttr: "3", opt3Attr: "Blabla" }, ... }, ... }
                switch
                    Opt1 then opt1Attr,
                    Opt2 then if opt2Attr then 2 else 0,
                    Bar then item ->> barAttr to-int
        """);
        assertEquals(3, r3);

        Integer r4 = model.evaluateExpression(Integer.class, """
            Foo { Bar: Bar { Opt4: Opt4 { barAttr: "4", opt4Attr: "Hello" }, ... }, ... }
                switch
                    Opt1 then opt1Attr,
                    Opt2 then if opt2Attr then 2 else 0,
                    Bar then item ->> barAttr to-int
        """);
        assertEquals(4, r4);
    }

    @Test
    public void switchOnChoiceOptionWithMetadata() {
        var model = modelService.toJavaTestModel("""
            typeAlias MyString: string(maxLength: 42)
        
            choice Foo:
                number
                MyString
                    [metadata scheme]
        """).compile();

        String resNum = model.evaluateExpression(String.class, """
            Foo { number: 42, ... }
                switch
                    number then to-string,
                    MyString then scheme
        """);
        assertEquals("42", resNum);

        String resStr = model.evaluateExpression(String.class, """
            Foo { MyString: "abc123" with-meta { scheme: "myScheme" }, ... }
                switch
                    number then item to-string,
                    MyString then scheme
        """);
        assertEquals("myScheme", resStr);
    }

    @Test
    public void switchCaseCanReturnMultiCardinalityResult() {
        List<?> rB = (List<?>) modelService.evaluateExpression(typeUtil.wrap(typeUtil.LIST, typeUtil.INTEGER), """
            "b" switch
                "a" then [1, 2, 3],
                "b" then 9,
                default 10
        """);
        assertEquals(List.of(9), rB);

        List<?> rNull = (List<?>) modelService.evaluateExpression(typeUtil.wrap(typeUtil.LIST, typeUtil.INTEGER), """
            (if False then "x") switch
                "a" then [1, 2, 3],
                "b" then 9,
                default 10
        """);
        assertEquals(List.of(), rNull);
    }

    @Test
    public void switchOperationWithOnlyDefaultCaseReturnsCorrectResult() {
        var model = modelService.toJavaTestModel("""
            enum SomeEnum:
                A
                B
                C
                D
        """).compile();

        Enum<?> result = model.evaluateExpression(model.getEnumJavaClass("SomeEnum"), """
            "anything" switch
                default SomeEnum -> B
        """);
        assertEquals("B", result.toString());
    }

    @Test
    public void switchOperationWithNoMatchesReturnsDefault() {
        var model = modelService.toJavaTestModel("""
            enum SomeEnum:
                A
                B
                C
                D
        """).compile();

        Enum<?> result = model.evaluateExpression(model.getEnumJavaClass("SomeEnum"), """
            "noMatch" switch
                "aCondition" then SomeEnum -> A,
                "bCondition" then SomeEnum -> B,
                "cCondition" then SomeEnum -> C,
                default SomeEnum -> D
        """);
        assertEquals("D", result.toString());
    }

    @Test
    public void switchOperationMatchingOnString() {
        var model = modelService.toJavaTestModel("""
            enum SomeEnum:
                A
                B
                C
                D
        """).compile();

        Enum<?> result = model.evaluateExpression(model.getEnumJavaClass("SomeEnum"), """
            "bCondition" switch
                "aCondition" then SomeEnum -> A,
                "bCondition" then SomeEnum -> B,
                "cCondition" then SomeEnum -> C,
                "dCondition" then SomeEnum -> D
        """);
        assertEquals("B", result.toString());
    }

    @Test
    public void switchOperationMatchingOnEnum() {
        var model = modelService.toJavaTestModel("""
            enum SomeEnum:
                A
                B
                C
                D
        """).compile();

        String result = model.evaluateExpression(String.class, """
            SomeEnum -> B switch
                A then "aValue",
                B then "bValue",
                C then "cValue",
                D then "dValue"
        """);
        assertEquals("bValue", result);
    }
}
