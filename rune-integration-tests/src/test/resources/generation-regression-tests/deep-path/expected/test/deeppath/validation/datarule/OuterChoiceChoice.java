package test.deeppath.validation.datarule;

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
import test.deeppath.OuterChoice;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

/**
 * @version 0.0.0
 */
@RosettaDataRule("OuterChoiceChoice")
@ImplementedBy(OuterChoiceChoice.Default.class)
public interface OuterChoiceChoice extends Validator<OuterChoice> {
	
	String NAME = "OuterChoiceChoice";
	String DEFINITION = "";
	
	class Default implements OuterChoiceChoice {
	
		@Override
		public List<ValidationResult<?>> getValidationResults(RosettaPath path, OuterChoice outerChoice) {
			ComparisonResult result = executeDataRule(outerChoice);
			if (result.getOrDefault(true)) {
				return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "OuterChoice", path, DEFINITION));
			}
			
			String failureMessage = result.getError();
			if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
				failureMessage = "Condition has failed.";
			}
			return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "OuterChoice", path, DEFINITION, failureMessage));
		}
		
		private ComparisonResult executeDataRule(OuterChoice outerChoice) {
			try {
				return choice(MapperS.of(outerChoice), Arrays.asList("InnerChoice", "Leaf"), ChoiceRuleValidationMethod.REQUIRED);
			}
			catch (Exception ex) {
				return ComparisonResult.failure(ex.getMessage());
			}
		}
	}
	
	@SuppressWarnings("unused")
	class NoOp implements OuterChoiceChoice {
	
		@Override
		public List<ValidationResult<?>> getValidationResults(RosettaPath path, OuterChoice outerChoice) {
			return Collections.emptyList();
		}
	}
}
