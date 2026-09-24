package test.expressions.validation.datarule;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.annotations.RosettaDataRule;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ChoiceRuleValidationMethod;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import test.expressions.Foo;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

/**
 * @version 0.0.0
 */
@RosettaDataRule("FooRules")
@ImplementedBy(FooRules.Default.class)
public interface FooRules extends Validator<Foo> {
	
	String NAME = "FooRules";
	String DEFINITION = "one-of and (optional choice i, code) and (i only exists or (i, code) only exists) and item -> i exists";
	
	class Default implements FooRules {
	
		@Override
		public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo foo) {
			ComparisonResult result = executeDataRule(foo);
			if (result.getOrDefault(true)) {
				return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "Foo", path, DEFINITION));
			}
			
			String failureMessage = result.getError();
			if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
				failureMessage = "Condition has failed.";
			}
			return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "Foo", path, DEFINITION, failureMessage));
		}
		
		private ComparisonResult executeDataRule(Foo foo) {
			try {
				return choice(MapperS.of(foo), Arrays.asList("i", "s", "code", "ref", "refs"), ChoiceRuleValidationMethod.REQUIRED).andNullSafe(choice(MapperS.of(foo), Arrays.asList("i", "code"), ChoiceRuleValidationMethod.OPTIONAL)).andNullSafe(onlyExists(MapperS.of(foo), Arrays.asList("i", "s", "code", "ref", "refs"), Arrays.asList("i")).orNullSafe(onlyExists(MapperS.of(foo), Arrays.asList("i", "s", "code", "ref", "refs"), Arrays.asList("i", "code")))).andNullSafe(exists(MapperS.of(foo).<Integer>map("getI", _foo -> _foo.getI())));
			}
			catch (Exception ex) {
				return ComparisonResult.failure(ex.getMessage());
			}
		}
	}
	
	@SuppressWarnings("unused")
	class NoOp implements FooRules {
	
		@Override
		public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo foo) {
			return Collections.emptyList();
		}
	}
}
