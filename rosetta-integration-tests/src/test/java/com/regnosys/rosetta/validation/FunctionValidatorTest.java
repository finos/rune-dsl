package com.regnosys.rosetta.validation;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionValidatorTest extends AbstractValidatorTest {
    @Test
    void csvIngestionInputMayOnlyHaveAttributesOfBasicType() {
    	assertIssues("""
                type Input:
                   attr string (1..1)
                   complexAttr Foo (1..1)
                
                type Foo:
                
                func MyFunc:
                   [ingest CSV]
                   inputs:
                       inp Input (1..1)
                """, """
                ERROR
                """
    		);
    }

    @Test
    void csvProjectionOutputMayOnlyHaveAttributesOfBasicType() {
        assertIssues("""
                type Input:
                   attr string (1..1)
                   complexAttr Foo (1..1)
                
                type Foo:
                
                func MyFunc:
                   [projection CSV]
                   output:
                       inp Input (1..1)
                """, """
                ERROR
                """
        );
    }
    
    @Test
    void functionWithNoImplementationAndAnnotationShouldNotWarn() {
        assertNoIssues("""
            func Foo:
              [codeImplementation]
              output:
                result string (1..1)
            """);
    }
    
    @Test
    void functionWithNoImplementationAndNoAnnotationShouldWarn() {
        assertIssues("""
            func Foo:
              output:
                result string (1..1)
            """, """
            WARNING (null) 'A function should specify an implementation, or they should be annotated with codeImplementation' at 4:6, length 3, on Function
            """);
    }

    @Test
    void functionWithCodeImplementationAnnotationAndBodyShouldWarn() {
        assertIssues("""
            func Foo:
              [codeImplementation]
              output:
                result string (1..1)
            
              set result: "output"
            """, """
            WARNING (null) 'Functions annotated with codeImplementation should not have any setter operations as they will be overriden' at 4:6, length 3, on Function
            """);
    }
}
