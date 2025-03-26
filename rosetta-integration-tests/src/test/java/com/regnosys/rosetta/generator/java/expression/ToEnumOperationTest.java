package com.regnosys.rosetta.generator.java.expression;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.testmodel.JavaTestModel;
import com.regnosys.rosetta.tests.testmodel.RosettaTestModelService;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ToEnumOperationTest {
    @Inject
    private RosettaTestModelService modelService;
    
    @Test
    void stringToEnumTest() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test
                
                enum Foo:
                    VALUE1
                    VALUE2
                """).compile();
        
        Enum<?> result = model.evaluateExpression(Enum.class, """
                    "VALUE1" to-enum Foo
                    """);
        
        var expected = model.getEnumJavaValue("Foo", "VALUE1");
        
        assertEquals(expected, result);
    }
    
    
    @Test
    void enumToEnumTest() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test
                
                enum Foo:
                    VALUE1
                    VALUE2
                    
                enum Bar:
                    VALUE1
                    VALUE2
                """).compile();
        
        Enum<?> result = model.evaluateExpression(Enum.class, """
                    Bar -> VALUE2 to-enum Foo
                    """);
        
        var expected = model.getEnumJavaValue("Foo", "VALUE2");
        
        assertEquals(expected, result);
    }
    
    @Test
    void emptyToEnumTest() {
        JavaTestModel model = modelService.toJavaTestModel("""
                namespace test
                
                enum Foo:
                    VALUE1
                    VALUE2
                    
                enum Bar:
                    VALUE1
                    VALUE2
                """).compile();
        
        Enum<?> result = model.evaluateExpression(Enum.class, """
                    (if False then Bar -> VALUE2) to-enum Foo
                    """);
        
        assertEquals(null, result);
    }    
}
