package com.regnosys.rosetta.generator.java.rule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.util.DottedPath;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.ROSETTA_SYMBOL_REFERENCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@InjectWith(RosettaTestInjectorProvider.class)
@ExtendWith(InjectionExtension.class)
public class RosettaRuleGeneratorTest {

    @Inject
    private CodeGeneratorTestHelper codeGeneratorTestHelper;
    @Inject
    private ModelHelper modelHelper;
    @Inject
    private ValidationTestHelper validationTestHelper;
    @Inject
    private FunctionGeneratorHelper functionGeneratorHelper;


    static final CharSequence REPORT_TYPES = """
            namespace com.rosetta.test.model
            
            type Bar:
            bar1 string (0..1)
            bar2 string (0..*)
            baz Baz (1..1)
            quxList Qux (0..*)
            
            type Baz:
            baz1 string (1..1)
            
            type Qux:
            qux1 string (1..1)
            qux2 string (1..1)
            
            type Quux:
            quux1 string (1..1)
            quux2 string (1..1)
            
            """;

    static final CharSequence REPORT_RULES = """
            namespace com.rosetta.test.model
            
            eligibility rule FooRule from Bar:
            filter bar1 exists
            
            reporting rule BarBarOne from Bar:
            extract bar1 as "1 BarOne"
            
            reporting rule BarBarTwo from Bar:
            extract bar2 as "2 BarTwo"
            
            reporting rule BarBaz from Bar:
            extract baz->baz1 as "3 BarBaz"
            
            reporting rule BarQuxList from Bar:
            extract quxList
            then extract BarQuxReport {
                bazQux1: QuxQux1,
                        bazQux2: QuxQux2
            } as "4 BarQuxList"
            
            reporting rule QuxQux1 from Qux:
            extract qux1 as "5 QuxQux1"
            
            reporting rule QuxQux2 from Qux:
            extract qux2 as "6 QuxQux2"
            
            reporting rule BarQuux from Bar:
            extract Create_Quux( baz ) as "7 BarQuux"
            
            func Create_Quux:
            inputs:
            baz Baz (1..1)
            output:
            quux Quux (1..1)
            
            set quux -> quux1: baz -> baz1
            set quux -> quux2: baz -> baz1
            
            """;


    @Test
    void parseSimpleReportForTypeWithInlineRuleReferences() {
        var model = List.of(
                REPORT_TYPES,
                REPORT_RULES,
                """
                         namespace com.rosetta.test.model
                        
                         body Authority TEST_REG
                         corpus TEST_REG MiFIR
                        
                         report TEST_REG MiFIR in T+1
                         from Bar
                         when FooRule
                         with type BarReport
                        
                         type BarReport:
                         barBarOne string (1..1)
                        [ruleReference BarBarOne]
                         barBarTwo string (0..*)
                                 [ruleReference BarBarTwo]
                         barBaz BarBazReport (1..1)
                         barQuxList BarQuxReport (0..*)
                                 [ruleReference BarQuxList]
                         barQuux Quux (1..1)
                        [ruleReference BarQuux]
                        
                         type BarBazReport:
                         barBaz1 string (1..1)
                        [ruleReference BarBaz]
                        
                         type BarQuxReport:
                         bazQux1 string (1..1)
                        [ruleReference QuxQux1]
                         bazQux2 string (1..1)
                        [ruleReference QuxQux2]
                        
                        """);

        var code = codeGeneratorTestHelper.generateCode(model.toArray(CharSequence[]::new));

        //println(code)
        var reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction");
        assertThat(reportJava, CoreMatchers.notNullValue());
        var expected = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.annotations.RosettaReport;
                import com.rosetta.model.lib.annotations.RuneLabelProvider;
                import com.rosetta.model.lib.functions.ModelObjectValidator;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Bar;
                import com.rosetta.test.model.BarReport;
                import com.rosetta.test.model.labels.TEST_REGMiFIRLabelProvider;
                import java.util.Optional;
                import javax.inject.Inject;
                
                
                @RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
                @RuneLabelProvider(labelProvider=TEST_REGMiFIRLabelProvider.class)
                @ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
                public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
                \t
                    @Inject protected ModelObjectValidator objectValidator;
                \t
                    // RosettaFunction dependencies
                    //
                    @Inject protected BarBarOneRule barBarOneRule;
                    @Inject protected BarBarTwoRule barBarTwoRule;
                    @Inject protected BarBazRule barBazRule;
                    @Inject protected BarQuuxRule barQuuxRule;
                    @Inject protected BarQuxListRule barQuxListRule;
                
                    /**
                    * @param input\s
                    * @return output\s
                    */
                    @Override
                    public BarReport evaluate(Bar input) {
                        BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
                \t\t
                        final BarReport output;
                        if (outputBuilder == null) {
                            output = null;
                        } else {
                            output = outputBuilder.build();
                            objectValidator.validate(BarReport.class, output);
                        }
                \t\t
                        return output;
                    }
                
                    protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
                
                    public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
                        @Override
                        protected BarReport.BarReportBuilder doEvaluate(Bar input) {
                            BarReport.BarReportBuilder output = BarReport.builder();
                            return assignOutput(output, input);
                        }
                \t\t
                        protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
                            output
                                .setBarBarOne(barBarOneRule.evaluate(input));
                \t\t\t
                            output
                                .setBarBarTwo(barBarTwoRule.evaluate(input));
                \t\t\t
                            output
                                .getOrCreateBarBaz()
                                .setBarBaz1(barBazRule.evaluate(input));
                \t\t\t
                            output
                                .setBarQuxList(barQuxListRule.evaluate(input));
                \t\t\t
                            output
                                .setBarQuux(barQuuxRule.evaluate(input));
                \t\t\t
                            return Optional.ofNullable(output)
                                .map(o -> o.prune())
                                .orElse(null);
                        }
                    }
                }
                """;
        assertJavaEquals(expected, reportJava);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void parseSimpleReportForTypeWithExternalRuleReferences() {
        var model = List.of(
                REPORT_TYPES,
                REPORT_RULES,
                """
                        namespace com.rosetta.test.model
                        
                        body Authority TEST_REG
                        corpus TEST_REG MiFIR
                        
                        report TEST_REG MiFIR in T+1
                        from Bar
                        when FooRule
                        with type BarReport
                        with source RuleSource
                        
                        type BarReport:
                        barBarOne string (1..1)
                        barBarTwo string (0..*)
                        barBaz BarBazReport (1..1)
                        barQuxList BarQuxReport (0..*)
                        barQuux Quux (1..1)
                        
                        type BarBazReport:
                        barBaz1 string (1..1)
                        
                        type BarQuxReport:
                        bazQux1 string (1..1)
                        bazQux2 string (1..1)
                        
                        rule source RuleSource {
                        
                            BarReport:
                            + barBarOne
                                    [ruleReference BarBarOne]
                            + barBarTwo
                                    [ruleReference BarBarTwo]
                            + barQuxList
                                    [ruleReference BarQuxList]
                            + barQuux
                                    [ruleReference BarQuux]
                        
                            BarBazReport:
                            + barBaz1
                                    [ruleReference BarBaz]
                        
                            BarQuxReport:
                            + bazQux1
                                    [ruleReference QuxQux1]
                            + bazQux2
                                    [ruleReference QuxQux2]
                        }
                        """);
        var code = codeGeneratorTestHelper.generateCode(model.toArray(CharSequence[]::new));
        //println(code)
        var reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction");
        assertThat(reportJava, CoreMatchers.notNullValue());
        var expected = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.annotations.RosettaReport;
                import com.rosetta.model.lib.annotations.RuneLabelProvider;
                import com.rosetta.model.lib.functions.ModelObjectValidator;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Bar;
                import com.rosetta.test.model.BarReport;
                import com.rosetta.test.model.labels.TEST_REGMiFIRLabelProvider;
                import java.util.Optional;
                import javax.inject.Inject;
                
                
                @RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
                @RuneLabelProvider(labelProvider=TEST_REGMiFIRLabelProvider.class)
                @ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
                public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
                \t
                    @Inject protected ModelObjectValidator objectValidator;
                \t
                    // RosettaFunction dependencies
                    //
                    @Inject protected BarBarOneRule barBarOneRule;
                    @Inject protected BarBarTwoRule barBarTwoRule;
                    @Inject protected BarBazRule barBazRule;
                    @Inject protected BarQuuxRule barQuuxRule;
                    @Inject protected BarQuxListRule barQuxListRule;
                
                    /**
                    * @param input\s
                    * @return output\s
                    */
                    @Override
                    public BarReport evaluate(Bar input) {
                        BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
                \t\t
                        final BarReport output;
                        if (outputBuilder == null) {
                            output = null;
                        } else {
                            output = outputBuilder.build();
                            objectValidator.validate(BarReport.class, output);
                        }
                \t\t
                        return output;
                    }
                
                    protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
                
                    public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
                        @Override
                        protected BarReport.BarReportBuilder doEvaluate(Bar input) {
                            BarReport.BarReportBuilder output = BarReport.builder();
                            return assignOutput(output, input);
                        }
                \t\t
                        protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
                            output
                                .setBarBarOne(barBarOneRule.evaluate(input));
                \t\t\t
                            output
                                .setBarBarTwo(barBarTwoRule.evaluate(input));
                \t\t\t
                            output
                                .getOrCreateBarBaz()
                                .setBarBaz1(barBazRule.evaluate(input));
                \t\t\t
                            output
                                .setBarQuxList(barQuxListRule.evaluate(input));
                \t\t\t
                            output
                                .setBarQuux(barQuuxRule.evaluate(input));
                \t\t\t
                            return Optional.ofNullable(output)
                                .map(o -> o.prune())
                                .orElse(null);
                        }
                    }
                }
                """;
        assertJavaEquals(expected, reportJava);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void parseSimpleReportForTypeWithExternalRuleReferences_OverrideAdd() {
        var model = List.of(
                REPORT_TYPES,
                REPORT_RULES,
                """
                         namespace com.rosetta.test.model
                        
                         body Authority TEST_REG
                         corpus TEST_REG MiFIR
                        
                         report TEST_REG MiFIR in T+1
                         from Bar
                         when FooRule
                         with type BarReport
                         with source RuleSource
                        
                         type BarReport:
                         barBarOne string (1..1)
                        [ruleReference BarBarOne]
                         barBarTwo string (0..*)
                                 [ruleReference BarBarTwo]
                         barBaz BarBazReport (1..1)
                         barQuxList BarQuxReport (0..*)
                                 [ruleReference BarQuxList]
                        
                         type BarBazReport:
                         barBaz1 string (1..1)
                        [ruleReference BarBaz]
                        
                         type BarQuxReport:
                         bazQux1 string (1..1)
                        [ruleReference QuxQux1]
                         bazQux2 string (1..1)
                        [ruleReference QuxQux2]
                        
                         reporting rule New_BarBarOne from Bar:
                         extract bar1 + "NEW" as "1 New BarOne"
                        
                         rule source RuleSource {
                        
                             BarReport:
                             - barBarOne
                                     + barBarOne
                                     [ruleReference New_BarBarOne]
                         }
                        """);
        var code = codeGeneratorTestHelper.generateCode(model.toArray(CharSequence[]::new));
        //println(code)
        var reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction");
        assertThat(reportJava, CoreMatchers.notNullValue());
        var expected = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.annotations.RosettaReport;
                import com.rosetta.model.lib.annotations.RuneLabelProvider;
                import com.rosetta.model.lib.functions.ModelObjectValidator;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Bar;
                import com.rosetta.test.model.BarReport;
                import com.rosetta.test.model.labels.TEST_REGMiFIRLabelProvider;
                import java.util.Optional;
                import javax.inject.Inject;
                
                
                @RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
                @RuneLabelProvider(labelProvider=TEST_REGMiFIRLabelProvider.class)
                @ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
                public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
                \t
                    @Inject protected ModelObjectValidator objectValidator;
                \t
                    // RosettaFunction dependencies
                    //
                    @Inject protected BarBarTwoRule barBarTwoRule;
                    @Inject protected BarBazRule barBazRule;
                    @Inject protected BarQuxListRule barQuxListRule;
                    @Inject protected New_BarBarOneRule new_BarBarOneRule;
                
                    /**
                    * @param input\s
                    * @return output\s
                    */
                    @Override
                    public BarReport evaluate(Bar input) {
                        BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
                \t\t
                        final BarReport output;
                        if (outputBuilder == null) {
                            output = null;
                        } else {
                            output = outputBuilder.build();
                            objectValidator.validate(BarReport.class, output);
                        }
                \t\t
                        return output;
                    }
                
                    protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
                
                    public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
                        @Override
                        protected BarReport.BarReportBuilder doEvaluate(Bar input) {
                            BarReport.BarReportBuilder output = BarReport.builder();
                            return assignOutput(output, input);
                        }
                \t\t
                        protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
                            output
                                .setBarBarOne(new_BarBarOneRule.evaluate(input));
                \t\t\t
                            output
                                .setBarBarTwo(barBarTwoRule.evaluate(input));
                \t\t\t
                            output
                                .getOrCreateBarBaz()
                                .setBarBaz1(barBazRule.evaluate(input));
                \t\t\t
                            output
                                .setBarQuxList(barQuxListRule.evaluate(input));
                \t\t\t
                            return Optional.ofNullable(output)
                                .map(o -> o.prune())
                                .orElse(null);
                        }
                    }
                }
                """;
        assertJavaEquals(expected, reportJava);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void parseSimpleReportForTypeWithExternalRuleReferences_OverrideRemove() {
        var model = List.of(
                REPORT_TYPES,
                REPORT_RULES,
                """
                         namespace com.rosetta.test.model
                        
                         body Authority TEST_REG
                         corpus TEST_REG MiFIR
                        
                         report TEST_REG MiFIR in T+1
                         from Bar
                         when FooRule
                         with type BarReport
                         with source RuleSource
                        
                         type BarReport:
                         barBarOne string (1..1)
                        [ruleReference BarBarOne]
                         barBarTwo string (0..*)
                                 [ruleReference BarBarTwo]
                         barBaz BarBazReport (1..1)
                         barQuxList BarQuxReport (0..*)
                                 [ruleReference BarQuxList]
                        
                         type BarBazReport:
                         barBaz1 string (1..1)
                        [ruleReference BarBaz]
                        
                         type BarQuxReport:
                         bazQux1 string (1..1)
                        [ruleReference QuxQux1]
                         bazQux2 string (1..1)
                        [ruleReference QuxQux2]
                        
                         rule source RuleSource {
                        
                             BarReport:
                             - barBarOne
                         }
                        """);
        var code = codeGeneratorTestHelper.generateCode(model.toArray(CharSequence[]::new));
        //println(code)
        var reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction");
        assertThat(reportJava, CoreMatchers.notNullValue());
        var expected = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.annotations.RosettaReport;
                import com.rosetta.model.lib.annotations.RuneLabelProvider;
                import com.rosetta.model.lib.functions.ModelObjectValidator;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Bar;
                import com.rosetta.test.model.BarReport;
                import com.rosetta.test.model.labels.TEST_REGMiFIRLabelProvider;
                import java.util.Optional;
                import javax.inject.Inject;
                
                
                @RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
                @RuneLabelProvider(labelProvider=TEST_REGMiFIRLabelProvider.class)
                @ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
                public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
                \t
                    @Inject protected ModelObjectValidator objectValidator;
                \t
                    // RosettaFunction dependencies
                    //
                    @Inject protected BarBarTwoRule barBarTwoRule;
                    @Inject protected BarBazRule barBazRule;
                    @Inject protected BarQuxListRule barQuxListRule;
                
                    /**
                    * @param input\s
                    * @return output\s
                    */
                    @Override
                    public BarReport evaluate(Bar input) {
                        BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
                \t\t
                        final BarReport output;
                        if (outputBuilder == null) {
                            output = null;
                        } else {
                            output = outputBuilder.build();
                            objectValidator.validate(BarReport.class, output);
                        }
                \t\t
                        return output;
                    }
                
                    protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
                
                    public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
                        @Override
                        protected BarReport.BarReportBuilder doEvaluate(Bar input) {
                            BarReport.BarReportBuilder output = BarReport.builder();
                            return assignOutput(output, input);
                        }
                \t\t
                        protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
                            output
                                .setBarBarTwo(barBarTwoRule.evaluate(input));
                \t\t\t
                            output
                                .getOrCreateBarBaz()
                                .setBarBaz1(barBazRule.evaluate(input));
                \t\t\t
                            output
                                .setBarQuxList(barQuxListRule.evaluate(input));
                \t\t\t
                            return Optional.ofNullable(output)
                                .map(o -> o.prune())
                                .orElse(null);
                        }
                    }
                }
                """;
        assertJavaEquals(expected, reportJava);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void parseSimpleReportForTypeWithExternalRuleReferencesWithEmptyAs() {
        var model = List.of(
                REPORT_TYPES,
                """
                        namespace com.rosetta.test.model
                        
                        eligibility rule FooRule from Bar:
                        filter bar1 exists
                        
                        reporting rule BarToBazReport from Bar:
                        extract BazReport {
                            qux: QuxReport {
                                attr: item -> bar1
                            }
                        }
                        as "Label 1"
                        
                        reporting rule EmptyWithAs from Bar:
                        empty
                        as "Label 2"
                        
                        """,
                """
                        namespace com.rosetta.test.model
                        
                        body Authority TEST_REG
                        corpus TEST_REG MiFIR
                        
                        report TEST_REG MiFIR in T+1
                        from Bar
                        when FooRule
                        with type BarReport
                        with source RuleSource
                        
                        type BarReport:
                        baz BazReport (1..1)
                        
                        type BazReport:
                        qux QuxReport (1..1)
                        
                        type QuxReport:
                        attr string (1..1)
                        
                        rule source RuleSource {
                        
                            BarReport:
                            + baz
                                    [ruleReference BarToBazReport]
                        
                            QuxReport:
                            + attr
                                    [ruleReference EmptyWithAs]
                        }
                        """);
        var code = codeGeneratorTestHelper.generateCode(model.toArray(CharSequence[]::new));
        //println(code)
        var reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction");
        assertThat(reportJava, CoreMatchers.notNullValue());
        var expected =
        		"""
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.annotations.RosettaReport;
                import com.rosetta.model.lib.annotations.RuneLabelProvider;
                import com.rosetta.model.lib.functions.ModelObjectValidator;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Bar;
                import com.rosetta.test.model.BarReport;
                import com.rosetta.test.model.labels.TEST_REGMiFIRLabelProvider;
                import java.util.Optional;
                import javax.inject.Inject;
                
                
                @RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
                @RuneLabelProvider(labelProvider=TEST_REGMiFIRLabelProvider.class)
                @ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
                public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
                \t
                    @Inject protected ModelObjectValidator objectValidator;
                \t
                    // RosettaFunction dependencies
                    //
                    @Inject protected BarToBazReportRule barToBazReportRule;
                
                    /**
                    * @param input\s
                    * @return output\s
                    */
                    @Override
                    public BarReport evaluate(Bar input) {
                        BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
                \t\t
                        final BarReport output;
                        if (outputBuilder == null) {
                            output = null;
                        } else {
                            output = outputBuilder.build();
                            objectValidator.validate(BarReport.class, output);
                        }
                \t\t
                        return output;
                    }
                
                    protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
                
                    public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
                        @Override
                        protected BarReport.BarReportBuilder doEvaluate(Bar input) {
                            BarReport.BarReportBuilder output = BarReport.builder();
                            return assignOutput(output, input);
                        }
                \t\t
                        protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
                            output
                                .setBaz(barToBazReportRule.evaluate(input));
                \t\t\t
                            return Optional.ofNullable(output)
                                .map(o -> o.prune())
                                .orElse(null);
                        }
                    }
                }
                """;
        assertJavaEquals(expected, reportJava);

        var classes = codeGeneratorTestHelper.compileToClasses(code);

        var test = functionGeneratorHelper.createFunc(classes, "TEST_REGMiFIRReportFunction", DottedPath.splitOnDots("com.rosetta.test.model.reports"));

        var input = codeGeneratorTestHelper.createInstanceUsingBuilder(classes, "Bar", Map.of("bar1", "bar1Value"));
        var output = functionGeneratorHelper.invokeFunc(test, RosettaModelObject.class, input);

        // expected output
        var expectedQuxReport = codeGeneratorTestHelper.createInstanceUsingBuilder(classes, "QuxReport", Map.of("attr", "bar1Value"));
        var expectedBazReport = codeGeneratorTestHelper.createInstanceUsingBuilder(classes, "BazReport", Map.of("qux", expectedQuxReport));
        var expectedBarReport = codeGeneratorTestHelper.createInstanceUsingBuilder(classes, "BarReport", Map.of("baz", expectedBazReport));

        assertEquals(expectedBarReport, output);
    }

    @Test
    void parseSimpleReportWithEmptyType() {
        var model = """
                body Authority TEST_REG
                corpus TEST_REG MiFIR
                
                report TEST_REG MiFIR in T+1
                from Bar
                when FooRule
                with type BarReport
                
                eligibility rule FooRule from Bar:
                filter bar1 exists
                
                type BarReport:
                barBarOne string (1..1)
                // no rules
                
                type Bar:
                bar1 string (0..1)
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);
        //println(code)
        var reportJava = code.get("com.rosetta.test.model.reports.TEST_REGMiFIRReportFunction");
        assertThat(reportJava, CoreMatchers.notNullValue());
        var expected = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.annotations.RosettaReport;
                import com.rosetta.model.lib.annotations.RuneLabelProvider;
                import com.rosetta.model.lib.functions.ModelObjectValidator;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Bar;
                import com.rosetta.test.model.BarReport;
                import com.rosetta.test.model.labels.TEST_REGMiFIRLabelProvider;
                import java.util.Optional;
                import javax.inject.Inject;
                
                
                @RosettaReport(namespace="com.rosetta.test.model", body="TEST_REG", corpusList={"MiFIR"})
                @RuneLabelProvider(labelProvider=TEST_REGMiFIRLabelProvider.class)
                @ImplementedBy(TEST_REGMiFIRReportFunction.TEST_REGMiFIRReportFunctionDefault.class)
                public abstract class TEST_REGMiFIRReportFunction implements ReportFunction<Bar, BarReport> {
                \t
                    @Inject protected ModelObjectValidator objectValidator;
                
                    /**
                    * @param input\s
                    * @return output\s
                    */
                    @Override
                    public BarReport evaluate(Bar input) {
                        BarReport.BarReportBuilder outputBuilder = doEvaluate(input);
                \t\t
                        final BarReport output;
                        if (outputBuilder == null) {
                            output = null;
                        } else {
                            output = outputBuilder.build();
                            objectValidator.validate(BarReport.class, output);
                        }
                \t\t
                        return output;
                    }
                
                    protected abstract BarReport.BarReportBuilder doEvaluate(Bar input);
                
                    public static class TEST_REGMiFIRReportFunctionDefault extends TEST_REGMiFIRReportFunction {
                        @Override
                        protected BarReport.BarReportBuilder doEvaluate(Bar input) {
                            BarReport.BarReportBuilder output = BarReport.builder();
                            return assignOutput(output, input);
                        }
                \t\t
                        protected BarReport.BarReportBuilder assignOutput(BarReport.BarReportBuilder output, Bar input) {
                            return Optional.ofNullable(output)
                                .map(o -> o.prune())
                                .orElse(null);
                        }
                    }
                }
                """;
        assertJavaEquals(expected, reportJava);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    Object loadRule(Map<String, Class<?>> classes, String ruleName) {
        Class<?> ruleClass = classes.get(ruleName);
        assertNotNull(ruleClass);
        Injector injector = Guice.createInjector();
        return injector.getInstance(ruleClass);
    }

    @Test
    void validPath() {
        var model = """
                type Foo:
                bar Bar (1..1)
                
                type Bar:
                baz string (1..1)
                
                reporting rule Rule1 from Foo:
                extract item->bar
                then extract item->baz
                """;
        var code = codeGeneratorTestHelper.generateCode(model);

        var ruleJava = code.get("com.rosetta.test.model.reports.Rule1Rule");
        assertThat(ruleJava, CoreMatchers.notNullValue());
        var expected = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.mapper.MapperS;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Bar;
                import com.rosetta.test.model.Foo;
                
                
                @ImplementedBy(Rule1Rule.Rule1RuleDefault.class)
                public abstract class Rule1Rule implements ReportFunction<Foo, String> {
                
                	/**
                	* @param input\s
                	* @return output\s
                	*/
                	@Override
                	public String evaluate(Foo input) {
                		String output = doEvaluate(input);
                \t\t
                		return output;
                	}
                
                	protected abstract String doEvaluate(Foo input);
                
                	public static class Rule1RuleDefault extends Rule1Rule {
                		@Override
                		protected String doEvaluate(Foo input) {
                			String output = null;
                			return assignOutput(output, input);
                		}
                \t\t
                		protected String assignOutput(String output, Foo input) {
                			final MapperS<Bar> thenArg = MapperS.of(input)
                				.mapSingleToItem(item -> item.<Bar>map("getBar", foo -> foo.getBar()));
                			output = thenArg
                				.mapSingleToItem(item -> item.<String>map("getBaz", bar -> bar.getBaz())).get();
                \t\t\t
                			return output;
                		}
                	}
                }
                """;
        assertJavaEquals(expected, ruleJava);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var ruleImpl = loadRule(classes, "com.rosetta.test.model.reports.Rule1Rule");
        assertNotNull(ruleImpl);
    }

    @Test
    void ruleRef() {
        var model = """
                type Foo:
                bar Bar (1..1)
                
                type Bar:
                val string (1..1)
                
                reporting rule Rule1 from Foo:
                Rule2 then
                extract val
                
                reporting rule Rule2 from Foo:
                extract bar
                
                """;

        var code = codeGeneratorTestHelper.generateCode(model);
        var rule = code.get("com.rosetta.test.model.reports.Rule1Rule");
        assertThat(rule, CoreMatchers.notNullValue());
        var expected = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.mapper.MapperS;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Bar;
                import com.rosetta.test.model.Foo;
                import javax.inject.Inject;
                
                
                @ImplementedBy(Rule1Rule.Rule1RuleDefault.class)
                public abstract class Rule1Rule implements ReportFunction<Foo, String> {
                \t
                	// RosettaFunction dependencies
                	//
                	@Inject protected Rule2Rule rule2Rule;
                
                	/**
                	* @param input\s
                	* @return output\s
                	*/
                	@Override
                	public String evaluate(Foo input) {
                		String output = doEvaluate(input);
                \t\t
                		return output;
                	}
                
                	protected abstract String doEvaluate(Foo input);
                
                	public static class Rule1RuleDefault extends Rule1Rule {
                		@Override
                		protected String doEvaluate(Foo input) {
                			String output = null;
                			return assignOutput(output, input);
                		}
                \t\t
                		protected String assignOutput(String output, Foo input) {
                			final MapperS<Bar> thenArg = MapperS.of(rule2Rule.evaluate(input));
                			output = thenArg
                				.mapSingleToItem(item -> item.<String>map("getVal", bar -> bar.getVal())).get();
                \t\t\t
                			return output;
                		}
                	}
                }
                """;
        assertJavaEquals(expected, rule);
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var ruleImpl = loadRule(classes, "com.rosetta.test.model.reports.Rule1Rule");
        assertNotNull(ruleImpl);
    }

    @Test
    void filter() {
        var model = """
                
                reporting rule SimpleRule from Input:
                    filter traderef="Hello"
                
                type Input:
                    traderef string (1..1)
                """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var ruleJava = code.get("com.rosetta.test.model.reports.SimpleRuleRule");
        assertThat(ruleJava, CoreMatchers.notNullValue());
        var expected = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.expression.CardinalityOperator;
                import com.rosetta.model.lib.functions.ModelObjectValidator;
                import com.rosetta.model.lib.mapper.MapperS;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Input;
                import java.util.Optional;
                import javax.inject.Inject;
                
                import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
                
                @ImplementedBy(SimpleRuleRule.SimpleRuleRuleDefault.class)
                public abstract class SimpleRuleRule implements ReportFunction<Input, Input> {
                \t
                	@Inject protected ModelObjectValidator objectValidator;
                
                	/**
                	* @param input\s
                	* @return output\s
                	*/
                	@Override
                	public Input evaluate(Input input) {
                		Input.InputBuilder outputBuilder = doEvaluate(input);
                \t\t
                		final Input output;
                		if (outputBuilder == null) {
                			output = null;
                		} else {
                			output = outputBuilder.build();
                			objectValidator.validate(Input.class, output);
                		}
                \t\t
                		return output;
                	}
                
                	protected abstract Input.InputBuilder doEvaluate(Input input);
                
                	public static class SimpleRuleRuleDefault extends SimpleRuleRule {
                		@Override
                		protected Input.InputBuilder doEvaluate(Input input) {
                			Input.InputBuilder output = Input.builder();
                			return assignOutput(output, input);
                		}
                \t\t
                		protected Input.InputBuilder assignOutput(Input.InputBuilder output, Input input) {
                			output = toBuilder(MapperS.of(input)
                				.filterSingleNullSafe(item -> areEqual(item.<String>map("getTraderef", _input -> _input.getTraderef()), MapperS.of("Hello"), CardinalityOperator.All).get()).get());
                \t\t\t
                			return Optional.ofNullable(output)
                				.map(o -> o.prune())
                				.orElse(null);
                		}
                	}
                }
                """;
        assertJavaEquals(expected, ruleJava);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void filter2() {
        var model = """
                reporting rule SimpleRule from Input:
                    filter traderef exists
                
                type Input:
                    traderef string (0..1)
                """;
        var code = codeGeneratorTestHelper.generateCode(model);
        var ruleJava = code.get("com.rosetta.test.model.reports.SimpleRuleRule");
        assertThat(ruleJava, CoreMatchers.notNullValue());

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void filterWhenRule() {
        var model = """
                reporting rule TestRule from Input:
                extract item->flag
                
                reporting rule FilterRule from Input:
                filter TestRule then extract traderef
                
                type Input:
                traderef string (1..1)
                flag boolean (1..1)
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);

        var ruleJava = code.get("com.rosetta.test.model.reports.FilterRuleRule");
        assertThat(ruleJava, CoreMatchers.notNullValue());

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void filterWhenCount() {
        var model = """
                reporting rule IsFixedFloat from Foo:
                extract fixed count = 12
                
                type Foo:
                fixed string (0..*)
                floating string (0..*)
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);

        var ruleJava = code.get("com.rosetta.test.model.reports.IsFixedFloatRule");
        assertThat(ruleJava, CoreMatchers.notNullValue());
        var expected = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.expression.CardinalityOperator;
                import com.rosetta.model.lib.mapper.MapperS;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.Foo;
                
                import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;
                
                @ImplementedBy(IsFixedFloatRule.IsFixedFloatRuleDefault.class)
                public abstract class IsFixedFloatRule implements ReportFunction<Foo, Boolean> {
                
                	/**
                	* @param input\s
                	* @return output\s
                	*/
                	@Override
                	public Boolean evaluate(Foo input) {
                		Boolean output = doEvaluate(input);
                \t\t
                		return output;
                	}
                
                	protected abstract Boolean doEvaluate(Foo input);
                
                	public static class IsFixedFloatRuleDefault extends IsFixedFloatRule {
                		@Override
                		protected Boolean doEvaluate(Foo input) {
                			Boolean output = null;
                			return assignOutput(output, input);
                		}
                \t\t
                		protected Boolean assignOutput(Boolean output, Foo input) {
                			output = MapperS.of(input)
                				.mapSingleToItem(item -> areEqual(MapperS.of(item.<String>mapC("getFixed", foo -> foo.getFixed()).resultCount()), MapperS.of(12), CardinalityOperator.All).asMapper()).get();
                \t\t\t
                			return output;
                		}
                	}
                }
                """;
        codeGeneratorTestHelper.compileToClasses(code);
        assertJavaEquals(expected, ruleJava);
    }

    @Test
    void functionCallsWithLiteralInputFromExtract() {
        var model = """ 
                   reporting rule FooRule from Foo:
                   extract
                   if FooFunc( a, "x" ) then "Y"
                else "Z"
                
                   type Foo:
                   a string (1..1)
                
                   func FooFunc:
                   inputs:
                   a string (1..1)
                   b string (1..1)
                   output:
                   result boolean (1..1)
                """;
        var parsed = modelHelper.parseRosettaWithNoErrors(model);
        var code = codeGeneratorTestHelper.generateCode(model);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldUseRuleFromDifferentNS() {
        var model = List.of("""
                namespace ns1
                
                
                type TestObject:
                fieldOne string (0..1)
                
                reporting rule Rule1 from TestObject:
                extract fieldOne
                
                """, """
                     namespace ns2
                
                import ns1.*
                
                             reporting rule Rule2 from TestObject:
                     Rule1
                """
        );

        var code = codeGeneratorTestHelper.generateCode(model.toArray(String[]::new));
        var classes = codeGeneratorTestHelper.compileToClasses(code);
        var ruleImpl = loadRule(classes, "ns2.reports.Rule2Rule");

        assertNotNull(ruleImpl);
    }

    @Test
    void callRuleWithAs() {
        var model = """
                
                
                type TestObject: <"">
                        fieldOne string (0..1)
                
                reporting rule Rule1 from TestObject:
                extract fieldOne
                
                reporting rule Rule2 from TestObject:
                Rule1 as "BLAH"
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);
        //code.writeClasses("callRuleWithAs")
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void brokenRuleTypes() {
        var model = """
                type Foo: <"">
                        bar Bar (0..1)
                
                type Bar:
                str string (1..1)
                
                reporting rule Rule1 from Foo:
                extract bar then
                        Rule2
                
                reporting rule Rule2 from Foo:
                extract bar->str
                
                """;
        var code = modelHelper.parseRosetta(model);
        validationTestHelper.assertError(code, ROSETTA_SYMBOL_REFERENCE, null,
                "Expected type `Foo`, but got `Bar` instead. Rule `Rule2` cannot be called with type `Bar`");
    }


    @Test
    void longNestedIfElseWithReturn0() {
        var model = """
                   type Foo:
                   bar Bar (1..1)
                
                   enum Bar:
                   A B C D F G H I J K L M N O P Q R S T U V W X Y Z
                
                   reporting rule BarField from Foo:
                   extract if bar = Bar -> A then "A"
                else if bar = Bar -> B then "B"
                else if bar = Bar -> C then "C"
                else if bar = Bar -> D then "D"
                else if bar = Bar -> F then "F"
                else if bar = Bar -> G then "G"
                else if bar = Bar -> H then "H"
                else if bar = Bar -> I then "I"
                else if bar = Bar -> B then "B"
                else if bar = Bar -> C then "C"
                else if bar = Bar -> D then "D"
                else if bar = Bar -> F then "F"
                else if bar = Bar -> G then "G"
                else if bar = Bar -> H then "H"
                else if bar = Bar -> I then "I"
                else if bar = Bar -> J then "J"
                else if bar = Bar -> K then "K"
                else if bar = Bar -> L then "L"
                else if bar = Bar -> M then "M"
                else if bar = Bar -> N then "N"
                else if bar = Bar -> O then "O"
                else if bar = Bar -> P then "P"
                else if bar = Bar -> Q then "Q"
                else if bar = Bar -> R then "R"
                else if bar = Bar -> S then "S"
                else if bar = Bar -> T then "T"
                else if bar = Bar -> U then "U"
                else if bar = Bar -> V then "V"
                else if bar = Bar -> W then "W"
                else if bar = Bar -> X then "X"
                else if bar = Bar -> Y then "Y"
                else if bar = Bar -> Z then "Z"
                else "0"
                
                """
                ;
        var code = codeGeneratorTestHelper.generateCode(model);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void longNestedIfElseWithNoReturn() {
        var model = """
                   type Foo:
                   bar Bar (1..1)
                
                   enum Bar:
                   A B C D F G H I J K L M N O P Q R S T U V W X Y Z
                
                   reporting rule BarField from Foo:
                   extract if bar = Bar -> A then "A"
                else if bar = Bar -> B then "B"
                else if bar = Bar -> C then "C"
                else if bar = Bar -> D then "D"
                else if bar = Bar -> F then "F"
                else if bar = Bar -> G then "G"
                else if bar = Bar -> H then "H"
                else if bar = Bar -> I then "I"
                else if bar = Bar -> B then "B"
                else if bar = Bar -> C then "C"
                else if bar = Bar -> D then "D"
                else if bar = Bar -> F then "F"
                else if bar = Bar -> G then "G"
                else if bar = Bar -> H then "H"
                else if bar = Bar -> I then "I"
                else if bar = Bar -> J then "J"
                else if bar = Bar -> K then "K"
                else if bar = Bar -> L then "L"
                else if bar = Bar -> M then "M"
                else if bar = Bar -> N then "N"
                else if bar = Bar -> O then "O"
                else if bar = Bar -> P then "P"
                else if bar = Bar -> Q then "Q"
                else if bar = Bar -> R then "R"
                else if bar = Bar -> S then "S"
                else if bar = Bar -> T then "T"
                else if bar = Bar -> U then "U"
                else if bar = Bar -> V then "V"
                else if bar = Bar -> W then "W"
                else if bar = Bar -> X then "X"
                else if bar = Bar -> Y then "Y"
                else if bar = Bar -> Z then "Z"
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);
        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void ifWithSingleCardinality() {
        var model = """
                    type Foo:
                    test boolean (1..1)
                    bar Bar (1..1)
                    bar2 Bar (1..1)
                
                    type Bar:
                    attr string (1..1)
                
                    reporting rule BarField from Foo:
                    extract
                    if test
                    then bar
                else bar2
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void ifWithMultipleCardinality() {
        var model = """
                    type Foo:
                    test boolean (1..1)
                    bar Bar (1..*)
                    bar2 Bar (1..*)
                
                    type Bar:
                    attr string (1..1)
                
                    reporting rule BarField from Foo:
                    extract
                    if test
                    then bar
                else bar2
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldGenerateDataType() {
        var model = """
                   body Authority TEST_REG
                   corpus TEST_REG MiFIR
                
                   report TEST_REG MiFIR in T+1
                   from Bar
                   when FooRule
                   with type BarReport
                
                   eligibility rule FooRule from Bar:
                   filter barA exists
                
                   reporting rule Aa from Bar:
                   extract barA as "A"
                
                   reporting rule Bb from Bar:
                   extract barB as "B"
                
                   reporting rule Cc from Bar:
                   extract barC as "C"
                
                   reporting rule Dd from Bar:
                   extract barD as "D"
                
                   reporting rule Ee from Bar:
                   extract barE as "E"
                
                   reporting rule Ff from Bar:
                   extract barF as "F"
                
                   type Bar:
                   barA date (0..1)
                   barB time (0..1)
                   barC zonedDateTime (0..1)
                   barD int (0..1)
                   barE number (0..1)
                   barF BazEnum (0..1)
                
                   enum BazEnum:
                   X
                           Y
                   Z
                
                   type BarReport:
                   aa date (1..1)
                [ruleReference Aa]
                   bb time (1..1)
                [ruleReference Bb]
                   cc zonedDateTime (1..1)
                [ruleReference Cc]
                   dd int (1..1)
                [ruleReference Dd]
                   ee number (1..1)
                [ruleReference Ee]
                   ff BazEnum (1..1)
                [ruleReference Ff]
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldTypeResolutionForListOperation() {
        var model = """
                     type Foo:
                     bar Bar (0..*)
                
                     type Bar:
                     attr string (1..1)
                
                     reporting rule FooRule from Foo:
                     extract [
                             item -> bar
                     max [ item -> attr ]
                ] then extract item -> attr
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldTypeResolutionForListOperation2() {
        var model = """
                     type Foo:
                     bar Bar (0..*)
                
                     type Bar:
                     baz Baz (1..1)
                
                     type Baz:
                     attr string (1..1)
                
                     reporting rule FooRule from Foo:
                     extract [
                             item -> bar
                     extract item -> baz
                ]
                     then extract item -> attr
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    @Test
    void shouldReturnEnumValue() {
        var model = """
                enum FooEnum:
                Bar
                        Baz
                
                reporting rule ReturnEnumValue from string:
                FooEnum -> Bar
                
                """;
        var code = codeGeneratorTestHelper.generateCode(model);


        var rule = code.get("com.rosetta.test.model.reports.ReturnEnumValueRule");

        var expectedRule = """
                package com.rosetta.test.model.reports;
                
                import com.google.inject.ImplementedBy;
                import com.rosetta.model.lib.reports.ReportFunction;
                import com.rosetta.test.model.FooEnum;
                
                
                @ImplementedBy(ReturnEnumValueRule.ReturnEnumValueRuleDefault.class)
                public abstract class ReturnEnumValueRule implements ReportFunction<String, FooEnum> {
                
                	/**
                	* @param input\s
                	* @return output\s
                	*/
                	@Override
                	public FooEnum evaluate(String input) {
                		FooEnum output = doEvaluate(input);
                \t\t
                		return output;
                	}
                
                	protected abstract FooEnum doEvaluate(String input);
                
                	public static class ReturnEnumValueRuleDefault extends ReturnEnumValueRule {
                		@Override
                		protected FooEnum doEvaluate(String input) {
                			FooEnum output = null;
                			return assignOutput(output, input);
                		}
                \t\t
                		protected FooEnum assignOutput(FooEnum output, String input) {
                			output = FooEnum.BAR;
                \t\t\t
                			return output;
                		}
                	}
                }
                """;

        assertJavaEquals(expectedRule, rule);

        codeGeneratorTestHelper.compileToClasses(code);
    }

    private static void assertJavaEquals(String expected, String actual) {
        assertEquals(expected.replace("\r", "").replace("\t", "    "), 
                actual.replace("\r", "").replace("\t", "    "));
    }

}