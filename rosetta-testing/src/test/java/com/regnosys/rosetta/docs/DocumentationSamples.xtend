package com.regnosys.rosetta.docs

import org.eclipse.xtext.testing.InjectWith
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import com.rosetta.util.types.GeneratedJavaClassService
import com.rosetta.model.lib.ModelSymbolId
import com.rosetta.util.DottedPath
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import com.google.inject.Guice
import com.google.inject.Module
import com.rosetta.util.types.JavaClass

/**
 * This test class contains sample code used in the documentation of the DSL.
 * If one of these tests fail, then the documentation should be reviewed as well,
 * as this probably means a code sample is out-dated.
 */
@InjectWith(RosettaInjectorProvider)
@ExtendWith(InjectionExtension)
class DocumentationSamples {
	
	@Inject extension CodeGeneratorTestHelper
	@Inject extension GeneratedJavaClassService
	
	@Test
	def void simpleReportSample() {
		val model = '''
			namespace "test.reg"
			version "test"
			
			report Shield Avengers SokoviaAccords in real-time
			    from Person
			    when HasSuperPowers
			    with type SokoviaAccordsReport
			
			// Definition for regulatory references:
			body Authority Shield
			corpus Act "Avengers Initiative" Avengers
			corpus Regulations "Sokovia Accords" SokoviaAccords
			segment section
			segment field
			
			type Person:
			    name string (1..1)
			    powers PowerEnum (0..*)
			
			type SokoviaAccordsReport:
			    heroName string (1..1)
			        [ruleReference HeroName]
			    canFly boolean (1..1)
			        [ruleReference CanFly]
			
			enum PowerEnum:
			    Armour
			    Flight
			    SuperhumanReflexes
			    SuperhumanStrength
			
			eligibility rule HasSuperPowers from Person:
			    filter powers exists
			
			reporting rule HeroName from Person:
			    [regulatoryReference Shield Avengers SokoviaAccords section "1" field "1" provision "Hero Name."]
			    extract name as "Hero Name"
			
			reporting rule CanFly from Person:
			    [regulatoryReference Shield Avengers SokoviaAccords section "2" field "1" provision "Can Hero Fly."]
			    extract powers any = PowerEnum -> Flight as "Can Hero Fly"
		'''
		val code = model.generateCode
		
		val reportId = ModelSymbolId.fromRegulatoryReference(DottedPath.splitOnDots("test.reg"), "Shield", "Avengers", "SokoviaAccords")
		val reportFunctionClassRepr = reportId.toJavaReportFunction
		
		val reportFunctionCode = code.get(reportFunctionClassRepr.canonicalName.withDots)
		assertNotNull(reportFunctionCode)
		var expected = '''
			package test.reg.reports;
			
			import com.google.inject.ImplementedBy;
			import com.rosetta.model.lib.functions.ModelObjectValidator;
			import com.rosetta.model.lib.mapper.MapperS;
			import com.rosetta.model.lib.reports.ReportFunction;
			import java.util.Optional;
			import javax.inject.Inject;
			import test.reg.Person;
			import test.reg.SokoviaAccordsReport;
			import test.reg.SokoviaAccordsReport.SokoviaAccordsReportBuilder;
			
			
			@ImplementedBy(ShieldAvengersSokoviaAccordsReportFunction.ShieldAvengersSokoviaAccordsReportFunctionDefault.class)
			public abstract class ShieldAvengersSokoviaAccordsReportFunction implements ReportFunction<Person, SokoviaAccordsReport> {
				
				@Inject protected ModelObjectValidator objectValidator;
				
				// RosettaFunction dependencies
				//
				@Inject protected CanFlyRule canFly;
				@Inject protected HeroNameRule heroName;
			
				/**
				* @param input 
				* @return output 
				*/
				@Override
				public SokoviaAccordsReport evaluate(Person input) {
					SokoviaAccordsReport.SokoviaAccordsReportBuilder outputBuilder = doEvaluate(input);
					
					final SokoviaAccordsReport output;
					if (outputBuilder == null) {
						output = null;
					} else {
						output = outputBuilder.build();
						objectValidator.validate(SokoviaAccordsReport.class, output);
					}
					
					return output;
				}
			
				protected abstract SokoviaAccordsReport.SokoviaAccordsReportBuilder doEvaluate(Person input);
			
				public static class ShieldAvengersSokoviaAccordsReportFunctionDefault extends ShieldAvengersSokoviaAccordsReportFunction {
					@Override
					protected SokoviaAccordsReport.SokoviaAccordsReportBuilder doEvaluate(Person input) {
						SokoviaAccordsReport.SokoviaAccordsReportBuilder output = SokoviaAccordsReport.builder();
						return assignOutput(output, input);
					}
					
					protected SokoviaAccordsReport.SokoviaAccordsReportBuilder assignOutput(SokoviaAccordsReport.SokoviaAccordsReportBuilder output, Person input) {
						output
							.setHeroName(MapperS.of(heroName.evaluate(MapperS.of(input).get())).get());
						
						output
							.setCanFly(MapperS.of(canFly.evaluate(MapperS.of(input).get())).get());
						
						return Optional.ofNullable(output)
							.map(o -> o.prune())
							.orElse(null);
					}
				}
			}
		'''
		assertEquals(expected, reportFunctionCode)
		
		val runtimeModuleClassRepr = new JavaClass(DottedPath.splitOnDots("test.reg.reports"), "OverridenReportModule")
		val runtimeModuleCode = '''
		package «runtimeModuleClassRepr.packageName»;
		
		import com.google.inject.AbstractModule;
		import test.reg.Person;
		import test.reg.SokoviaAccordsReport;
		import test.reg.SokoviaAccordsReport.SokoviaAccordsReportBuilder;
		
		public class «runtimeModuleClassRepr.simpleName» extends AbstractModule {
			@Override
			protected void configure() {
				this.bind(ShieldAvengersSokoviaAccordsReportFunction.class).to(CustomShieldAvengersSokoviaAccordsReportFunction.class);
			}
			
			public static class CustomShieldAvengersSokoviaAccordsReportFunction extends ShieldAvengersSokoviaAccordsReportFunction {
				@Override
				protected SokoviaAccordsReport.SokoviaAccordsReportBuilder doEvaluate(Person input) {
					SokoviaAccordsReport.SokoviaAccordsReportBuilder output = SokoviaAccordsReport.builder();
					output.setHeroName("My flying hero");
					output.setCanFly(true);
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
		assertEquals("ShieldAvengersSokoviaAccordsReportFunctionDefault", reportFunction.class.simpleName)
		
		val customInjector = Guice.createInjector(customModule)
		val customReportFunction = customInjector.getInstance(reportFunctionClass)
		assertEquals("CustomShieldAvengersSokoviaAccordsReportFunction", customReportFunction.class.simpleName)
	}
}