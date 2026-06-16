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
import test.deeppath.InnerChoice;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

/**
 * @version 0.0.0
 */
@RosettaDataRule("InnerChoiceChoice")
@ImplementedBy(InnerChoiceChoice.Default.class)
public interface InnerChoiceChoice extends Validator<InnerChoice> {
	
	String NAME = "InnerChoiceChoice";
	String DEFINITION = "";
	
	class Default implements InnerChoiceChoice {
	
		@Override
		public List<ValidationResult<?>> getValidationResults(RosettaPath path, InnerChoice innerChoice) {
			ComparisonResult result = executeDataRule(innerChoice);
			if (result.getOrDefault(true)) {
				return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "InnerChoice", path, DEFINITION));
			}
			
			String failureMessage = result.getError();
			if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
				failureMessage = "Condition has failed.";
			}
			return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "InnerChoice", path, DEFINITION, failureMessage));
		}
		
		private ComparisonResult executeDataRule(InnerChoice innerChoice) {
			try {
				return choice(MapperS.of(innerChoice), Arrays.asList("Option1", "Option2"), ChoiceRuleValidationMethod.REQUIRED);
			}
			catch (Exception ex) {
				return ComparisonResult.failure(ex.getMessage());
			}
		}
	}
	
	@SuppressWarnings("unused")
	class NoOp implements InnerChoiceChoice {
	
		@Override
		public List<ValidationResult<?>> getValidationResults(RosettaPath path, InnerChoice innerChoice) {
			return Collections.emptyList();
		}
	}
}
