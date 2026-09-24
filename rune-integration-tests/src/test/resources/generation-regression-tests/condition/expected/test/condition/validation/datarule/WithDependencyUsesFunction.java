package test.condition.validation.datarule;

import com.google.inject.ImplementedBy;
import com.rosetta.model.lib.annotations.RosettaDataRule;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.mapper.MapperS;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import test.condition.WithDependency;
import test.condition.functions.IsAllowed;


/**
 * @version 0.0.0
 */
@RosettaDataRule("WithDependencyUsesFunction")
@ImplementedBy(WithDependencyUsesFunction.Default.class)
public interface WithDependencyUsesFunction extends Validator<WithDependency> {

    String NAME = "WithDependencyUsesFunction";
    String DEFINITION = "IsAllowed";

    class Default implements WithDependencyUsesFunction {

        @Inject protected IsAllowed isAllowed;

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, WithDependency withDependency) {
            ComparisonResult result = executeDataRule(withDependency);
            if (result.getOrDefault(true)) {
                return Arrays.asList(ValidationResult.success(NAME, ValidationResult.ValidationType.DATA_RULE, "WithDependency", path, DEFINITION));
            }

            String failureMessage = result.getError();
            if (failureMessage == null || failureMessage.contains("Null") || failureMessage == "") {
                failureMessage = "Condition has failed.";
            }
            return Arrays.asList(ValidationResult.failure(NAME, ValidationResult.ValidationType.DATA_RULE, "WithDependency", path, DEFINITION, failureMessage));
        }

        private ComparisonResult executeDataRule(WithDependency withDependency) {
            try {
                return ComparisonResult.ofNullSafe(MapperS.of(isAllowed.evaluate(withDependency)));
            }
            catch (Exception ex) {
                return ComparisonResult.failure(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    class NoOp implements WithDependencyUsesFunction {

        @Override
        public List<ValidationResult<?>> getValidationResults(RosettaPath path, WithDependency withDependency) {
            return Collections.emptyList();
        }
    }
}
