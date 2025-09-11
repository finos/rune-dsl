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
    void csvIngestionInputMustBeTabular() {
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
                ERROR (null) 'The input of a CSV ingest function must be a tabular type. Type `Input` has non-simple attributes: `complexAttr`' at 13:12, length 5, on Attribute
                """
    		);
    }

    @Test
    void csvProjectionOutputMustBeTabular() {
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
                WARNING (null) 'A function should specify an implementation, or they should be annotated with codeImplementation' at 10:6, length 6, on Function
                ERROR (null) 'Transform functions must have a single input.' at 11:4, length 16, on AnnotationRef
                ERROR (null) 'The output of a CSV projection function must be a tabular type. Type `Input` has non-simple attributes: `complexAttr`' at 13:12, length 5, on Attribute
                """
        );
    }

    @Test
    void csvIngestionMayNotHaveMultipleInputs() {
        assertIssues("""
                type Input:
                   attr string (1..1)
                
                func MyFunc:
                   [ingest CSV]
                   inputs:
                       inp Input (1..1)
                       inp2 Input (1..1)
                """, """
                ERROR (null) 'Transform functions may only have a single input.' at 11:8, length 17, on Attribute
                """
        );
    }

    @Test
    void csvIngestionMayNotHaveMultiInput() {
        assertIssues("""
                type Input:
                   attr string (1..1)
                
                func MyFunc:
                   [ingest CSV]
                   inputs:
                       inp Input (0..*)
                """, """
                ERROR (null) 'The input of a CSV ingest function must be single cardinality' at 10:18, length 6, on Attribute
                """
        );
    }

    @Test
    void csvIngestionMustHaveOneInput() {
        assertIssues("""
                type Input:
                   attr string (1..1)
                
                func MyFunc:
                   [ingest CSV]
                   output:
                       result string (1..1)
                """, """
                WARNING (null) 'A function should specify an implementation, or they should be annotated with codeImplementation' at 7:6, length 6, on Function
                ERROR (null) 'Transform functions must have a single input.' at 8:4, length 12, on AnnotationRef
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
