package com.regnosys.rosetta.generator.java.function;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;
import com.rosetta.model.lib.context.RuneContextFactory;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionExtensionGeneratorTest {
    @Inject
    private RosettaTestModelService modelService;
    @Inject
    private RuneContextFactory contextFactory;
    
    @Test
    void testFunctionExtensionIsSubtypeOfOriginal() {
        var model = modelService.toJavaTestModel("""
                namespace test
                scope MyScope
                
                func Foo:
                    output:
                        result int (1..1)
                    set result: 0
                
                func Bar extends Foo:
                    output:
                        result int (1..1)
                    set result: 42
                """).compile();
        
        var foo = model.getFunctionJavaClass("Foo");
        var bar = model.getFunctionJavaClass("Bar");
        
        assertTrue(foo.isAssignableFrom(bar), "Foo should be a superclass of Bar");
    }

    @Test
    void testCallOverriddenFunctionFromWithinScope() {
        var model = modelService.toJavaTestModel("""
                namespace test
                scope MyScope
                
                func Test:
                    output:
                        result string (1..1)
                    set result: Foo()
                
                func Foo:
                    output:
                        result string (1..1)
                    set result: "Foo"
                
                func Bar extends Foo:
                    output:
                        result string (1..1)
                    set result: "Bar"
                """).compile();
        
        var result = model.evaluateExpression(String.class, "Test()");
        assertEquals("Bar", result);
    }
    
    @Test
    void testCallOverriddenFunctionFromOutsideScope() {
        var model = modelService.toJavaTestModel("""
                namespace test
                
                func Test:
                    output:
                        result string (1..1)
                    set result: Foo()
                """,
                """
                namespace test
                scope MyScope
                
                func Foo:
                    output:
                        result string (1..1)
                    set result: "Foo"
                
                func Bar extends Foo:
                    output:
                        result string (1..1)
                    set result: "Bar"
                """).compile();

        var result = model.evaluateExpression(String.class, "Test()");
        assertEquals("Foo", result);

        var myScope = model.getScopeInstance("MyScope");
        var resultInScope = model.evaluateExpression(String.class, "Test()", contextFactory.withScope(myScope));
        assertEquals("Bar", resultInScope);
    }
    
    @Test
    void testSuperCall() {
        var model = modelService.toJavaTestModel("""
                namespace test
                scope MyScope
                
                func Foo:
                    output:
                        result string (1..1)
                    set result: "Foo"
                
                func Bar extends Foo:
                    output:
                        result string (1..1)
                    set result: super() + "Bar"
                """).compile();

        var result = model.evaluateExpression(String.class, "Bar()");
        assertEquals("FooBar", result);
    }
}
