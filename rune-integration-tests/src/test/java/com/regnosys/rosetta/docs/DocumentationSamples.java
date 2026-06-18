package com.regnosys.rosetta.docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.generated.GeneratedJavaClass;
import com.rosetta.util.types.generated.GeneratedJavaClassService;

/**
 * This test class contains sample code used in the documentation of the DSL.
 * If one of these tests fail, then the documentation should be reviewed as well,
 * as this probably means a code sample is out-dated.
 */
@InjectWith(RosettaTestInjectorProvider.class)
@ExtendWith(InjectionExtension.class)
class DocumentationSamples {

	@Inject
	private ModelHelper modelHelper;
	@Inject
	private CodeGeneratorTestHelper codeGeneratorTestHelper;
	@Inject
	private GeneratedJavaClassService generatedJavaClassService;

	@Test
	void simpleReportSample() throws Exception {
		String model = """
				namespace "test.reg"
				version "test"

				report EuropeanParliament EmissionPerformanceStandardsEU in real-time
				    from VehicleOwnership
				    when IsEuroStandardsCoverage
				    with type EuropeanParliamentReport

				// Definition for regulatory references:
				body Authority EuropeanParliament
				corpus Regulation "Regulation (EU) 2019/631" EmissionPerformanceStandardsEU

				type VehicleOwnership:
				    drivingLicence DrivingLicence (1..1)
				    vehicle Vehicle (1..1)

				type EuropeanParliamentReport:
				    vehicleRegistrationID string (1..1)
				        [label "Vehicle Registration ID"]
				        [ruleReference VehicleRegistrationID]
				    vehicleClassificationType VehicleClassificationEnum (1..1)
				        [label "Vehicle Classification Type"]
				        [ruleReference VehicleClassificationType]

				type Vehicle:
				    registrationID string (1..1)
				    vehicleClassification VehicleClassificationEnum (1..1)

				enum VehicleClassificationEnum:
					M1_Passengers
				    M2_Passengers
				    M3_Passengers
				    N1I_Commercial
				    N1II_Commercial
				    N1III_Commercial
				    N2_Commercial
				    N3_Commercial
				    l1e_Moped
				    l2e_Moped
				    l3e_Motorcycle
				    l4e_Motorcycle
				    l5e_Motortricycle
				    l6e_Quadricycle
				    l7e_Quadricycle
				    O1_Trailers
				    O2_Trailers
				    O3_Trailers
				    O4_Trailers

				type Person:
				    name string (1..1)

				type DrivingLicence:
				    owner Person (1..1)
				    countryofIssuance string (1..1)
				    dateofIssuance date (1..1)
				    dateOfRenewal date (0..1)
				    vehicleEntitlement VehicleClassificationEnum (0..*)

				eligibility rule IsEuroStandardsCoverage from VehicleOwnership:
				    filter
				        vehicle -> vehicleClassification = VehicleClassificationEnum -> M1_Passengers
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> M2_Passengers
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> M3_Passengers
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> N1I_Commercial
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> N1II_Commercial
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> N1III_Commercial
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> N2_Commercial
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> N3_Commercial
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> l3e_Motorcycle
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> l4e_Motorcycle
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> l5e_Motortricycle
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> l6e_Quadricycle
				            or vehicle -> vehicleClassification = VehicleClassificationEnum -> l7e_Quadricycle

				reporting rule VehicleRegistrationID from VehicleOwnership:
				    extract vehicle -> registrationID

				reporting rule VehicleClassificationType from VehicleOwnership: <"Classification type of the vehicle">
				    extract vehicle -> vehicleClassification
				""";
		RosettaModel parsed = modelHelper.parseRosettaWithNoIssues(model);
		Map<String, String> code = codeGeneratorTestHelper.generateCode(parsed);

		ModelReportId reportId = new ModelReportId(DottedPath.splitOnDots("test.reg"), "EuropeanParliament", "EmissionPerformanceStandardsEU");
		JavaClass<?> reportFunctionClassRepr = generatedJavaClassService.toJavaReportFunction(reportId);

		String reportFunctionCode = code.get(reportFunctionClassRepr.getCanonicalName().withDots());
		assertNotNull(reportFunctionCode);
		String expected = """
				package test.reg.reports;

				import com.google.inject.ImplementedBy;
				import com.rosetta.model.lib.annotations.RosettaReport;
				import com.rosetta.model.lib.annotations.RuneLabelProvider;
				import com.rosetta.model.lib.functions.ModelObjectValidator;
				import com.rosetta.model.lib.reports.ReportFunction;
				import java.util.Optional;
				import javax.inject.Inject;
				import test.reg.EuropeanParliamentReport;
				import test.reg.VehicleOwnership;
				import test.reg.labels.EuropeanParliamentEmissionPerformanceStandardsEULabelProvider;


				@RosettaReport(namespace="test.reg", body="EuropeanParliament", corpusList={"EmissionPerformanceStandardsEU"})
				@RuneLabelProvider(labelProvider=EuropeanParliamentEmissionPerformanceStandardsEULabelProvider.class)
				@ImplementedBy(EuropeanParliamentEmissionPerformanceStandardsEUReportFunction.EuropeanParliamentEmissionPerformanceStandardsEUReportFunctionDefault.class)
				public abstract class EuropeanParliamentEmissionPerformanceStandardsEUReportFunction implements ReportFunction<VehicleOwnership, EuropeanParliamentReport> {
				\t
				\t@Inject protected ModelObjectValidator objectValidator;
				\t
				\t// RosettaFunction dependencies
				\t//
				\t@Inject protected VehicleClassificationTypeRule vehicleClassificationTypeRule;
				\t@Inject protected VehicleRegistrationIDRule vehicleRegistrationIDRule;

				\t/**
				\t* @param input\s
				\t* @return output\s
				\t*/
				\t@Override
				\tpublic EuropeanParliamentReport evaluate(VehicleOwnership input) {
				\t\tEuropeanParliamentReport.EuropeanParliamentReportBuilder outputBuilder = doEvaluate(input);
				\t\t
				\t\tfinal EuropeanParliamentReport output;
				\t\tif (outputBuilder == null) {
				\t\t\toutput = null;
				\t\t} else {
				\t\t\toutput = outputBuilder.build();
				\t\t\tobjectValidator.validate(EuropeanParliamentReport.class, output);
				\t\t}
				\t\t
				\t\treturn output;
				\t}

				\tprotected abstract EuropeanParliamentReport.EuropeanParliamentReportBuilder doEvaluate(VehicleOwnership input);

				\tpublic static class EuropeanParliamentEmissionPerformanceStandardsEUReportFunctionDefault extends EuropeanParliamentEmissionPerformanceStandardsEUReportFunction {
				\t\t@Override
				\t\tprotected EuropeanParliamentReport.EuropeanParliamentReportBuilder doEvaluate(VehicleOwnership input) {
				\t\t\tEuropeanParliamentReport.EuropeanParliamentReportBuilder output = EuropeanParliamentReport.builder();
				\t\t\treturn assignOutput(output, input);
				\t\t}
				\t\t
				\t\tprotected EuropeanParliamentReport.EuropeanParliamentReportBuilder assignOutput(EuropeanParliamentReport.EuropeanParliamentReportBuilder output, VehicleOwnership input) {
				\t\t\toutput
				\t\t\t\t.setVehicleRegistrationID(vehicleRegistrationIDRule.evaluate(input));
				\t\t\t
				\t\t\toutput
				\t\t\t\t.setVehicleClassificationType(vehicleClassificationTypeRule.evaluate(input));
				\t\t\t
				\t\t\treturn Optional.ofNullable(output)
				\t\t\t\t.map(o -> o.prune())
				\t\t\t\t.orElse(null);
				\t\t}
				\t}
				}
				""";
		assertEquals(expected, reportFunctionCode);

		GeneratedJavaClass<Object> runtimeModuleClassRepr = new GeneratedJavaClass<>(DottedPath.splitOnDots("test.reg.reports"), "OverridenReportModule", Object.class);
		String runtimeModuleCode = """
				package %s;

				import com.google.inject.AbstractModule;
				import test.reg.VehicleOwnership;
				import test.reg.EuropeanParliamentReport;
				import test.reg.EuropeanParliamentReport.EuropeanParliamentReportBuilder;

				public class %s extends AbstractModule {
					@Override
					protected void configure() {
						this.bind(EuropeanParliamentEmissionPerformanceStandardsEUReportFunction.class).to(CustomEuropeanParliamentEmissionPerformanceStandardsEUReportFunction.class);
					}

					public static class CustomEuropeanParliamentEmissionPerformanceStandardsEUReportFunction extends EuropeanParliamentEmissionPerformanceStandardsEUReportFunction {
						@Override
						protected EuropeanParliamentReport.EuropeanParliamentReportBuilder doEvaluate(VehicleOwnership input) {
							EuropeanParliamentReport.EuropeanParliamentReportBuilder output = EuropeanParliamentReport.builder();
							return output;
						}
					}
				}
				""".formatted(runtimeModuleClassRepr.getPackageName().withDots(), runtimeModuleClassRepr.getSimpleName());
		code.put(runtimeModuleClassRepr.getCanonicalName().withDots(), runtimeModuleCode);

		Map<String, Class<?>> classes = codeGeneratorTestHelper.compileToClasses(code);
		Class<?> reportFunctionClass = classes.get(reportFunctionClassRepr.getCanonicalName().withDots());
		Module emptyModule = new EmptyModule();
		Module customModule = (Module) classes.get(runtimeModuleClassRepr.getCanonicalName().withDots()).getConstructors()[0].newInstance();

		Injector simpleInjector = Guice.createInjector(emptyModule);
		Object reportFunction = simpleInjector.getInstance(reportFunctionClass);
		assertEquals("EuropeanParliamentEmissionPerformanceStandardsEUReportFunctionDefault", reportFunction.getClass().getSimpleName());

		Injector customInjector = Guice.createInjector(customModule);
		Object customReportFunction = customInjector.getInstance(reportFunctionClass);
		assertEquals("CustomEuropeanParliamentEmissionPerformanceStandardsEUReportFunction", customReportFunction.getClass().getSimpleName());
	}
}
