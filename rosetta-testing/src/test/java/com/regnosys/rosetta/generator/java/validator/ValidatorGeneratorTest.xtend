package com.regnosys.rosetta.generator.java.validator

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper
import javax.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import static org.junit.jupiter.api.Assertions.*

@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)

class ValidatorGeneratorTest {
	@Inject extension CodeGeneratorTestHelper
	
	@Test
	@Disabled
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
			import com.rosetta.model.lib.path.RosettaPath;
			import com.rosetta.model.lib.validation.AttributeValidation;
			import com.rosetta.model.lib.validation.ConditionValidation;
			import com.rosetta.model.lib.validation.RosettaModelObjectValidator;
			import com.rosetta.model.lib.validation.TypeValidation;
			import com.rosetta.model.lib.validation.ValidationResult;
			import com.rosetta.test.model.Foo;
			import com.rosetta.test.model.validation.datarule.FooC;
			import com.rosetta.util.DottedPath;
			import java.util.ArrayList;
			import java.util.List;
			import java.util.regex.Pattern;
			import javax.inject.Inject;
			
			import static com.rosetta.model.lib.validation.ValidationUtil.checkCardinality;
			import static com.rosetta.model.lib.validation.ValidationUtil.checkNumber;
			import static com.rosetta.model.lib.validation.ValidationUtil.checkString;
			import static java.util.Optional.empty;
			import static java.util.Optional.of;
			
			public class FooValidator implements RosettaModelObjectValidator<Foo>{
				@Inject protected FooC c;
										
				@Override
				public TypeValidation validate(RosettaPath path, Foo o) {
				
					DottedPath packageName = DottedPath.of(o.getClass().getPackage().toString());
					String simpleName = o.getClass().getSimpleName();
					ModelSymbolId modelSymbolId = new ModelSymbolId(packageName, simpleName);
				
				 	List<AttributeValidation> attributeValidations = new ArrayList<>();
				 	attributeValidations.add(validateA(o.getA(), path));
				 	attributeValidations.add(validateB(o.getB(), path));
				 	
				 	List<ConditionValidation> conditionValidations = new ArrayList<>();
				 	conditionValidations.add(validateC(o, path));
				 	
				 	return new TypeValidation(modelSymbolId, attributeValidations, conditionValidations);
				}
				
				public AttributeValidation validateA(Integer atr, RosettaPath path) {
					List<ValidationResult> validationResults = new ArrayList<>();
					ValidationResult cardinalityValidation = checkCardinality("a", atr != null ? 1 : 0, 0, 1, path);
					validationResults.add(checkNumber("a",atr, empty(), empty(), empty(), path));
					
					return new AttributeValidation("a", cardinalityValidation, validationResults);
				}
				public AttributeValidation validateB(String atr, RosettaPath path) {
					List<ValidationResult> validationResults = new ArrayList<>();
					ValidationResult cardinalityValidation = checkCardinality("b", atr != null ? 1 : 0, 1, 1, path);
					validationResults.add(checkString("b", atr, 0, empty(), of(Pattern.compile("[a-z]")), path));
					
					return new AttributeValidation("b", cardinalityValidation, validationResults);
				}
				
				public ConditionValidation validateC(Foo data, RosettaPath path) {
					ValidationResult result = c.validate(path, data);
					
					return new ConditionValidation(c.toString(), result);
				}
			}
			'''.toString,
			valCode
		)
		
		val classes = code.compileToClasses
		
	}
}