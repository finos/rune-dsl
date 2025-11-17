package com.regnosys.rosetta.generator.java.reports;

import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.RosettaFunction;
import com.rosetta.util.DottedPath;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ReportGeneratorTest {

    private static final CharSequence COMMON_REPORT_TYPES = """
            type BarReportInstruction:
            	bar1 string (0..1)
            	bar2 string (0..1)
            	bar3 string (0..1)
            	bar4 string (0..1)
            	bar5 string (0..1)
            	bar6 string (0..1)
            	bar7 string (0..1)
            
            """;

    private static final CharSequence INLINE_REPORT_RULES = """
            body Authority TestBody
            corpus TestBody TestCorpus1
            
            report TestBody TestCorpus1 in T+1
                from BarReportInstruction
                when FooEligibilityRule
                with type BarReport
            
            type BarReport:
              	out1 string (1..1)
              	    [ruleReference CommonBar1]
              	out2 string (1..1)
              	    [ruleReference CommonBar2]
                out3 string (0..1)
            
            eligibility rule FooEligibilityRule from BarReportInstruction:
                filter bar1 exists
            
            reporting rule CommonBar1 from BarReportInstruction:
                extract bar1
            
            reporting rule CommonBar2 from BarReportInstruction:
                extract bar2
            
            """;

    private static final CharSequence EXTERNAL_REPORT_RULES = """
            corpus TestBody TestCorpus2
            
            report TestBody TestCorpus2 in T+1
                from BarReportInstruction
                when FooEligibilityRule
                with type BarReport
                with source ExtRules
            
            rule source ExtRules
            {
                BarReport:
                   - out1
                   + out1
                       [ruleReference ExtBar1]
                   + out3
                       [ruleReference ExtBar3]
            }
            
            reporting rule ExtBar1 from BarReportInstruction:
                extract bar3
            
            reporting rule ExtBar3 from BarReportInstruction:
                extract bar4
            
            """;

    private static final CharSequence EXTENDED_EXT_REPORT_RULES = """
            corpus TestBody TestCorpus3
            
            report TestBody TestCorpus3 in T+1
                from BarReportInstruction
                when FooEligibilityRule
                with type BarReport
                with source ExtendedExtRules
            
            rule source ExtendedExtRules extends ExtRules
            {
                BarReport:
                   - out1
                   + out1
                       [ruleReference ExtendedExtBar1]
            }
            
            reporting rule ExtendedExtBar1 from BarReportInstruction:
                extract bar5
            
            """;

    private static final CharSequence EXTENDED_EXT2_REPORT_RULES = """
            corpus TestBody TestCorpus4
            
            report TestBody TestCorpus4 in T+1
                from BarReportInstruction
                when FooEligibilityRule
                with type ExtendedBarReport
                with source ExtendedExt2Rules
            
            type ExtendedBarReport extends BarReport:
                out4 string (0..1)
                    [ruleReference ExtendedExt2Bar4]
                out5 string (0..1)
            
            rule source ExtendedExt2Rules extends ExtRules
            {
                ExtendedBarReport:
                - out1
                + out1
                    [ruleReference ExtendedExt2Bar1]
                + out5
                    [ruleReference ExtendedExt2Bar5]
            }
            
            reporting rule ExtendedExt2Bar1 from BarReportInstruction:
                extract bar5
            
            reporting rule ExtendedExt2Bar4 from BarReportInstruction:
                extract bar6
            
            reporting rule ExtendedExt2Bar5 from BarReportInstruction:
                extract bar7
            
            """;


    @Inject
    FunctionGeneratorHelper functionGeneratorHelper;
    @Inject
    CodeGeneratorTestHelper generatorTestHelper;

    @Test
    void shouldReportBasedOnInlineRuleReference() {
        var code = generatorTestHelper.generateCode(COMMON_REPORT_TYPES, INLINE_REPORT_RULES);
        var classes = generatorTestHelper.compileToClasses(code);
        var reportFunc = getFunc(classes, "TestBodyTestCorpus1ReportFunction");

        var result = functionGeneratorHelper.invokeFunc(reportFunc, RosettaModelObject.class, getInput(classes));

        var expectedResult = getOutput(classes, "BarReport", Map.of(
                "out1", "v1",
                "out2", "v2"
        ));

        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReportBasedOnExternalRuleReference() {
        var code = generatorTestHelper.generateCode(COMMON_REPORT_TYPES, INLINE_REPORT_RULES, EXTERNAL_REPORT_RULES);
        var classes = generatorTestHelper.compileToClasses(code);
        var reportFunc = getFunc(classes, "TestBodyTestCorpus2ReportFunction");

        var result = functionGeneratorHelper.invokeFunc(reportFunc, RosettaModelObject.class, getInput(classes));

        var expectedResult = getOutput(classes, "BarReport", Map.of(
                "out1", "v3",
                "out2", "v2",
                "out3", "v4"
        ));

        assertEquals(expectedResult, result);
    }


    @Test
    void shouldReportBasedOnExtendedExternalRuleReference() {
        var code = generatorTestHelper.generateCode(COMMON_REPORT_TYPES, INLINE_REPORT_RULES, EXTERNAL_REPORT_RULES, EXTENDED_EXT_REPORT_RULES);
        var classes = generatorTestHelper.compileToClasses(code);
        var reportFunc = getFunc(classes, "TestBodyTestCorpus3ReportFunction");

        var result = functionGeneratorHelper.invokeFunc(reportFunc, RosettaModelObject.class, getInput(classes));

        var expectedResult = getOutput(classes, "BarReport", Map.of(
                "out1", "v5",
                "out2", "v2",
                "out3", "v4"
        ));

        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReportBasedOnExtendedExternalRuleReferenceAndExtendedReportType() {
        var code = generatorTestHelper.generateCode(COMMON_REPORT_TYPES, """
                
                type AdditionalFields:
                    [rootType]
                    attr string (0..1)
                	    [ruleReference AttrRuleHello]
                
                type CommonReport:
                    additionalFields AdditionalFields (0..1)
                
                type Report extends CommonReport:
                    override additionalFields AdditionalFields (0..1)
                        [ruleReference for attr empty]
                
                reporting rule AttrRuleHello from BarReportInstruction:
                    extract "hello"
                
                reporting rule AttrRuleEmpty from BarReportInstruction:
                    extract empty
                
                body Authority TestBody
                corpus TestBody TestCorpus1
                
                report TestBody TestCorpus1 in T+1
                    from BarReportInstruction
                    when FooEligibilityRule
                    with type Report
                
                eligibility rule FooEligibilityRule from BarReportInstruction:
                    filter True
                """);

        var classes = generatorTestHelper.compileToClasses(code);
        var reportFunc = getFunc(classes, "TestBodyTestCorpus1ReportFunction");

        var result = functionGeneratorHelper.invokeFunc(reportFunc, RosettaModelObject.class, getInput(classes));

        //we have an overridden empty rule so nothing reported
        var expectedResult = getOutput(classes, "Report", Map.of());

        assertEquals(expectedResult, result);
    }


    @Test
    void shouldOverrideEmptyUsingRuleReferenceForAttribute() {
        var code = generatorTestHelper.generateCode(COMMON_REPORT_TYPES, INLINE_REPORT_RULES, EXTERNAL_REPORT_RULES, EXTENDED_EXT2_REPORT_RULES);
        var classes = generatorTestHelper.compileToClasses(code);
        var reportFunc = getFunc(classes, "TestBodyTestCorpus4ReportFunction");

        var result = functionGeneratorHelper.invokeFunc(reportFunc, RosettaModelObject.class, getInput(classes));

        var expectedResult = getOutput(classes, "ExtendedBarReport", Map.of(
                "out1", "v5",
                "out2", "v2",
                "out3", "v4",
                "out4", "v6",
                "out5", "v7"
        ));

        assertEquals(expectedResult, result);
    }


    private RosettaModelObject getInput(Map<String, Class<?>> classes) {
        return generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), "BarReportInstruction", Map.of(
                "bar1", "v1",
                "bar2", "v2",
                "bar3", "v3",
                "bar4", "v4",
                "bar5", "v5",
                "bar6", "v6",
                "bar7", "v7"
        ));
    }

    private RosettaFunction getFunc(Map<String, Class<?>> classes, String funcName) {
        return functionGeneratorHelper.createFunc(classes, funcName, DottedPath.of("com.rosetta.test.model.reports"));
    }

    private RosettaModelObject getOutput(Map<String, Class<?>> classes, String typeName, Map<String, Object> values) {
        return generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("com.rosetta.test.model"), typeName, values);
    }
}
