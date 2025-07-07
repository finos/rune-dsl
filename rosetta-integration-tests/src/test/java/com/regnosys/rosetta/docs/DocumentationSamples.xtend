package com.regnosys.rosetta.docs

import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.util.DottedPath
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import com.google.inject.Guice
import com.google.inject.Module
import com.regnosys.rosetta.tests.util.ModelHelper
import com.rosetta.model.lib.ModelReportId
import com.rosetta.util.types.generated.GeneratedJavaClassService
import com.rosetta.util.types.generated.GeneratedJavaClass

/**
 * This test class contains sample code used in the documentation of the DSL.
 * If one of these tests fail, then the documentation should be reviewed as well,
 * as this probably means a code sample is out-dated.
 */
@InjectWith(RosettaTestInjectorProvider)
@ExtendWith(InjectionExtension)
class DocumentationSamples {
	
	@Inject extension ModelHelper
	@Inject extension CodeGeneratorTestHelper
	@Inject extension GeneratedJavaClassService
	
	@Test
	def void simpleReportSample() {
		val model = '''
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
			        [ruleReference VehicleRegistrationID]
			    vehicleClassificationType VehicleClassificationEnum (1..1)
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
			        as "Vehicle Registration ID"
			
			reporting rule VehicleClassificationType from VehicleOwnership: <"Classification type of the vehicle">
			    extract vehicle -> vehicleClassification
			        as "Vehicle Classification Type"
		'''
		model.parseRosettaWithNoIssues
		val code = model.generateCode
		
		val reportId = new ModelReportId(DottedPath.splitOnDots("test.reg"), "EuropeanParliament", "EmissionPerformanceStandardsEU")
		val reportFunctionClassRepr = reportId.toJavaReportFunction
		
		val reportFunctionCode = code.get(reportFunctionClassRepr.canonicalName.withDots)
		assertNotNull(reportFunctionCode)
		var expected = '''
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
				
				@Inject protected ModelObjectValidator objectValidator;
				
				// RosettaFunction dependencies
				//
				@Inject protected VehicleClassificationTypeRule vehicleClassificationTypeRule;
				@Inject protected VehicleRegistrationIDRule vehicleRegistrationIDRule;
			
				/**
				* @param input 
				* @return output 
				*/
				@Override
				public EuropeanParliamentReport evaluate(VehicleOwnership input) {
					EuropeanParliamentReport.EuropeanParliamentReportBuilder outputBuilder = doEvaluate(input);
					
					final EuropeanParliamentReport output;
					if (outputBuilder == null) {
						output = null;
					} else {
						output = outputBuilder.build();
						objectValidator.validate(EuropeanParliamentReport.class, output);
					}
					
					return output;
				}
			
				protected abstract EuropeanParliamentReport.EuropeanParliamentReportBuilder doEvaluate(VehicleOwnership input);
			
				public static class EuropeanParliamentEmissionPerformanceStandardsEUReportFunctionDefault extends EuropeanParliamentEmissionPerformanceStandardsEUReportFunction {
					@Override
					protected EuropeanParliamentReport.EuropeanParliamentReportBuilder doEvaluate(VehicleOwnership input) {
						EuropeanParliamentReport.EuropeanParliamentReportBuilder output = EuropeanParliamentReport.builder();
						return assignOutput(output, input);
					}
					
					protected EuropeanParliamentReport.EuropeanParliamentReportBuilder assignOutput(EuropeanParliamentReport.EuropeanParliamentReportBuilder output, VehicleOwnership input) {
						output
							.setVehicleRegistrationID(vehicleRegistrationIDRule.evaluate(input));
						
						output
							.setVehicleClassificationType(vehicleClassificationTypeRule.evaluate(input));
						
						return Optional.ofNullable(output)
							.map(o -> o.prune())
							.orElse(null);
					}
				}
			}
		'''
		assertEquals(expected, reportFunctionCode)
		
		val runtimeModuleClassRepr = new GeneratedJavaClass<Object>(DottedPath.splitOnDots("test.reg.reports"), "OverridenReportModule", Object)
		val runtimeModuleCode = '''
		package «runtimeModuleClassRepr.packageName»;
		
		import com.google.inject.AbstractModule;
		import test.reg.VehicleOwnership;
		import test.reg.EuropeanParliamentReport;
		import test.reg.EuropeanParliamentReport.EuropeanParliamentReportBuilder;
		
		public class «runtimeModuleClassRepr.simpleName» extends AbstractModule {
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
		'''
		code.put(runtimeModuleClassRepr.canonicalName.withDots, runtimeModuleCode)
		
		val classes = code.compileToClasses
		val reportFunctionClass = classes.get(reportFunctionClassRepr.canonicalName.withDots)
		val emptyModule = new EmptyModule()
		val customModule = classes.get(runtimeModuleClassRepr.canonicalName.withDots).constructors.head.newInstance() as Module
		
		val simpleInjector = Guice.createInjector(emptyModule)
		val reportFunction = simpleInjector.getInstance(reportFunctionClass)
		assertEquals("EuropeanParliamentEmissionPerformanceStandardsEUReportFunctionDefault", reportFunction.class.simpleName)
		
		val customInjector = Guice.createInjector(customModule)
		val customReportFunction = customInjector.getInstance(reportFunctionClass)
		assertEquals("CustomEuropeanParliamentEmissionPerformanceStandardsEUReportFunction", customReportFunction.class.simpleName)
	}
}