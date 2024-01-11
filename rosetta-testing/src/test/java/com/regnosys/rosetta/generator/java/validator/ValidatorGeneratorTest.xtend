package com.regnosys.rosetta.generator.java.validator

import org.junit.jupiter.api.^extension.ExtendWith
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import com.regnosys.rosetta.tests.RosettaInjectorProvider
import org.junit.jupiter.api.Test
import javax.inject.Inject
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper

import static com.google.common.collect.ImmutableMap.*
import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaInjectorProvider)
class ValidatorGeneratorTest {
	@Inject extension CodeGeneratorTestHelper
	
	@Test
	def void validatorTest() {
		val code = '''
			namespace com.rosetta.test.model
			version "${project.version}"
			
			type Foo:
				a int (0..1)
				b string(pattern: "[a-z]") (1..1)
				
				condition C:
				    it -> a exists and [it, it] any = it
		'''.generateCode
		
		val valCode = code.get("com.rosetta.test.model.validation.FooValidator")
		assertEquals(
			'''
			package com.rosetta.test.model.validation;
			
			import com.rosetta.model.lib.ModelSymbolId;
			import com.rosetta.model.lib.validation.AttributeValidation;
			import com.rosetta.model.lib.validation.RosettaModelObjectValidator;
			import com.rosetta.model.lib.validation.TypeValidation;
			import com.rosetta.model.lib.validation.ValidationResult;
			import com.rosetta.test.model.Foo;
			import com.rosetta.util.DottedPath;
			import java.util.ArrayList;
			import java.util.List;
			import java.util.regex.Pattern;
			
			import static com.rosetta.model.lib.validation.ValidationUtil.checkCardinality;
			import static com.rosetta.model.lib.validation.ValidationUtil.checkNumber;
			import static com.rosetta.model.lib.validation.ValidationUtil.checkString;
			import static java.util.Optional.empty;
			import static java.util.Optional.of;
			
			public class FooValidator implements RosettaModelObjectValidator<Foo>{
				
				@Override
				public TypeValidation validate(Foo o) {
				
					DottedPath packageName = DottedPath.of(o.getClass().getPackage().toString());
					String simpleName = o.getClass().getSimpleName();
					ModelSymbolId modelSymbolId = new ModelSymbolId(packageName, simpleName);
				
				 	List<AttributeValidation> attributeValidations = new ArrayList<>();
				 	attributeValidations.add(validateA(o.getA()));
				 	attributeValidations.add(validateB(o.getB()));
				}
				
				public AttributeValidation validateA(int atr) {
					List<ValidationResult> validationResults = new ArrayList<>();
					ValidationResult cardinalityValidation = checkCardinality("a", o.getA() != null ? 1 : 0, 0, 1);
					validationResults.add(checkNumber("a", o.getA(), empty(), of(0), empty(), empty()));
					
					return new AttributeValidation("a", cardinalityValidation, validationResults);
				}
				public AttributeValidation validateB(String atr) {
					List<ValidationResult> validationResults = new ArrayList<>();
					ValidationResult cardinalityValidation = checkCardinality("b", o.getB() != null ? 1 : 0, 1, 1);
					validationResults.add(checkString("b", o.getB(), 0, empty(), of(Pattern.compile("[a-z]"))));
					
					return new AttributeValidation("b", cardinalityValidation, validationResults);
				}
			}
			'''.toString,
			valCode
		)
		
		val classes = code.compileToClasses
		
	}
}