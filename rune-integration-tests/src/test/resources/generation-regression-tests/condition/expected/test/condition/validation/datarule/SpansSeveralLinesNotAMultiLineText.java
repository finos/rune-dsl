package test.condition.validation.datarule;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.annotations.RosettaDataRule;
import com.rosetta.model.lib.expression.CardinalityOperator;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import test.condition.SpansSeveralLines;

import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.*;

/**
 * @version 0.0.0
 */
@RosettaDataRule("SpansSeveralLinesNotAMultiLineText")
@ImplementedBy(SpansSeveralLinesNotAMultiLineText.Default.class)
public interface SpansSeveralLinesNotAMultiLineText extends Validator<SpansSeveralLines> {
	
	String NAME = "SpansSeveralLinesNotAMultiLineText";
	String DEFINITION = "val <> \"first line\n" + 
		"second line\"";
	
	class Default implements SpansSeveralLinesNotAMultiLineText {
	
		@Override
		public List<ValidationResult<?>> getValidationResults(RosettaPath path, SpansSeveralLines spansSeveralLines) {
			ComparisonResult result = executeDataRule(spansSeveralLines);
			if (result.getOrDefault(true)) {
				return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "SpansSeveralLines", path, DEFINITION));
			}
			
			String failureMessage = result.getError();
			if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
				failureMessage = "Condition has failed.";
			}
			return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "SpansSeveralLines", path, DEFINITION, failureMessage));
		}
		
		private ComparisonResult executeDataRule(SpansSeveralLines spansSeveralLines) {
			try {
				return notEqual(MapperS.of(spansSeveralLines).<String>map("getVal", _spansSeveralLines -> _spansSeveralLines.getVal()), MapperS.of("first line\nsecond line"), CardinalityOperator.Any);
			}
			catch (Exception ex) {
				return ComparisonResult.failure(ex.getMessage());
			}
		}
	}
	
	@SuppressWarnings("unused")
	class NoOp implements SpansSeveralLinesNotAMultiLineText {
	
		@Override
		public List<ValidationResult<?>> getValidationResults(RosettaPath path, SpansSeveralLines spansSeveralLines) {
			return Collections.emptyList();
		}
	}
}
