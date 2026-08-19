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
    void functionNameShouldStartWithUpperCaseCanBeSuppressed() {
        assertNoIssues("""
                func myFunc:
                  [suppressWarnings capitalisation]
                   output:
                       result string (1..1)
                
                    set result: "output"
                """
        );
    }
    @Test
    void functionNameShouldStartWithUpperCase() {
        assertIssues("""
                func myFunc:
                   output:
                       result string (1..1)
                
                    set result: "output"
                """, """
                WARNING (RosettaIssueCodes.invalidCase) 'Function name should start with a capital' at 4:6, length 6, on Function
                """
        );
    }

    @Test
    void csvIngestionInputMustBeTabular() {
    	assertIssues("""
                type Thing:
                   attr string (1..1)
                   complexAttr Foo (1..1)
                
                type Foo:
                
                func MyFunc:
                   [ingest CSV]
                   inputs:
                       inp Thing (1..1)
                """, """
                ERROR (null) 'The input of a CSV ingest function must be a tabular type. Type `Thing` has non-simple attributes: `complexAttr`' at 13:12, length 5, on Attribute
                """
    		);
    }

    @Test
    void csvProjectionOutputMustBeTabular() {
        assertIssues("""
                type Thing:
                   attr string (1..1)
                   complexAttr Foo (1..1)
                
                type Foo:
                
                func MyFunc:
                   [projection CSV]
                   output:
                       inp Thing (1..1)
                """, """
                WARNING (null) 'A function should specify an implementation, or they should be annotated with codeImplementation' at 10:6, length 6, on Function
                ERROR (null) 'Transform functions must have a single input.' at 11:4, length 16, on TransformAnnotation
                ERROR (null) 'The output of a CSV projection function must be a tabular type. Type `Thing` has non-simple attributes: `complexAttr`' at 13:12, length 5, on Attribute
                """
        );
    }

    @Test
    void csvIngestionMayNotHaveMultipleInputs() {
        assertIssues("""
                type Thing:
                   attr string (1..1)
                
                func MyFunc:
                   [ingest CSV]
                   inputs:
                       inp Thing (1..1)
                       inp2 Thing (1..1)
                """, """
                ERROR (null) 'Transform functions may only have a single input.' at 11:8, length 17, on Attribute
                """
        );
    }

    @Test
    void csvIngestionMayNotHaveMultiInput() {
        assertIssues("""
                type Thing:
                   attr string (1..1)
                
                func MyFunc:
                   [ingest CSV]
                   inputs:
                       inp Thing (0..*)
                """, """
                ERROR (null) 'The input of a CSV ingest function must be single cardinality' at 10:18, length 6, on Attribute
                """
        );
    }

    @Test
    void csvIngestionInputWithMultiCardinalitySimpleAttributeIsTabular() {
        assertNoIssues("""
                type Thing:
                   attr string (1..1)
                   stringList string (0..*)

                func MyFunc:
                   [ingest CSV]
                   inputs:
                       inp Thing (1..1)
                """
        );
    }

    @Test
    void csvProjectionOutputWithMultiCardinalitySimpleAttributeIsTabular() {
        assertNoIssues("""
                type Thing:
                   attr string (1..1)
                   stringList string (0..*)

                func MyFunc:
                   [projection CSV]
                   inputs:
                       inp string (1..1)
                   output:
                       result Thing (1..1)

                   set result: Thing { attr: inp, stringList: empty }
                """
        );
    }

    @Test
    void csvProjectionOutputMustBeSingleCardinality() {
    	assertIssues("""
                type Thing:
                   attr string (1..1)

                func MyFunc:
                   [projection CSV]
                   inputs:
                       inp string (1..1)
                   output:
                       result Thing (0..*)

                   set result: [ Thing { attr: inp } ]
                """, """
                ERROR (null) 'The output of a CSV projection function must be single cardinality' at 12:21, length 6, on Attribute
                """
    		);
    }

    @Test
    void csvLabelledProjectionOutputMustBeTabular() {
        assertIssues("""
                type Thing:
                   attr string (1..1)
                   complexAttr Foo (1..1)

                type Foo:

                func MyFunc:
                   [projection CSV_LABELLED]
                   inputs:
                       inp string (1..1)
                   output:
                       result Thing (1..1)

                   set result: Thing { attr: inp, complexAttr: Foo { } }
                """, """
                WARNING (null) 'CSV_LABELLED is deprecated. Use CSV instead, with "headerStyle": "LABEL" in the CSV serialization configuration. The CSV format honours the whole configuration and resolves the label provider by the same rules.' at 11:16, length 12, on TransformAnnotation
                ERROR (null) 'The output of a CSV projection function must be a tabular type. Type `Thing` has non-simple attributes: `complexAttr`' at 15:15, length 5, on Attribute
                """
        );
    }

    @Test
    void csvLabelledIngestionInputMustBeTabular() {
        assertIssues("""
                type Thing:
                   attr string (1..1)
                   complexAttr Foo (1..1)

                type Foo:

                func MyFunc:
                   [ingest CSV_LABELLED]
                   inputs:
                       inp Thing (1..1)
                """, """
                WARNING (null) 'CSV_LABELLED is deprecated. Use CSV instead, with "headerStyle": "LABEL" in the CSV serialization configuration. The CSV format honours the whole configuration and resolves the label provider by the same rules.' at 11:12, length 12, on TransformAnnotation
                ERROR (null) 'The input of a CSV ingest function must be a tabular type. Type `Thing` has non-simple attributes: `complexAttr`' at 13:12, length 5, on Attribute
                """
        );
    }

    @Test
    void csvLabelledProjectionOverFlatTypeIsTabular() {
        // The deprecation warning is the only issue: the tabular rule accepts this output.
        assertIssues("""
                type Thing:
                   attr string (1..1)
                   enumAttr Bar (1..1)
                   aliasAttr Qux (1..1)

                enum Bar:
                    VALUE1
                    VALUE2

                typeAlias Qux:
                    string

                func MyFunc:
                   [projection CSV_LABELLED]
                   inputs:
                       inp string (1..1)
                   output:
                       result Thing (1..1)

                   set result: Thing { attr: inp, enumAttr: Bar -> VALUE1, aliasAttr: inp }
                """, """
                WARNING (null) 'CSV_LABELLED is deprecated. Use CSV instead, with "headerStyle": "LABEL" in the CSV serialization configuration. The CSV format honours the whole configuration and resolves the label provider by the same rules.' at 17:16, length 12, on TransformAnnotation
                """
        );
    }

    @Test
    void csvLabelledProjectionOutputWithMultiCardinalitySimpleAttributeIsTabular() {
        // The deprecation warning is the only issue: the tabular rule accepts a multi-cardinality simple attribute.
        assertIssues("""
                type Thing:
                   attr string (1..1)
                   stringList string (0..*)

                func MyFunc:
                   [projection CSV_LABELLED]
                   inputs:
                       inp string (1..1)
                   output:
                       result Thing (1..1)

                   set result: Thing { attr: inp, stringList: empty }
                """, """
                WARNING (null) 'CSV_LABELLED is deprecated. Use CSV instead, with "headerStyle": "LABEL" in the CSV serialization configuration. The CSV format honours the whole configuration and resolves the label provider by the same rules.' at 9:16, length 12, on TransformAnnotation
                """
        );
    }

    @Test
    void csvLabelledIngestionInputMustBeSingleCardinality() {
        assertIssues("""
                type Thing:
                   attr string (1..1)

                func MyFunc:
                   [ingest CSV_LABELLED]
                   inputs:
                       inp Thing (0..*)
                """, """
                WARNING (null) 'CSV_LABELLED is deprecated. Use CSV instead, with "headerStyle": "LABEL" in the CSV serialization configuration. The CSV format honours the whole configuration and resolves the label provider by the same rules.' at 8:12, length 12, on TransformAnnotation
                ERROR (null) 'The input of a CSV ingest function must be single cardinality' at 10:18, length 6, on Attribute
                """
        );
    }

    @Test
    void csvIngestionMustHaveOneInput() {
        assertIssues("""
                type Thing:
                   attr string (1..1)
                
                func MyFunc:
                   [ingest CSV]
                   output:
                       result string (1..1)
                """, """
                WARNING (null) 'A function should specify an implementation, or they should be annotated with codeImplementation' at 7:6, length 6, on Function
                ERROR (null) 'Transform functions must have a single input.' at 8:4, length 12, on TransformAnnotation
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
    @Test
    void csvProjectionOutputWithMetadataAttributeIsNotTabular() {
        assertIssues("""
                type Thing:
                   attr string (1..1)
                   withScheme string (1..1)
                      [metadata scheme]

                func MyFunc:
                   [projection CSV]
                   inputs:
                       inp string (1..1)
                   output:
                       result Thing (1..1)

                   set result -> attr: inp
                """, """
                ERROR (null) 'The output of a CSV projection function must be a tabular type. Type `Thing` has non-simple attributes: `withScheme`' at 14:15, length 5, on Attribute
                """
        );
    }

}
