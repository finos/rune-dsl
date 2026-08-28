package test.condition.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.condition.WithDependency;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class WithDependencyTypeFormatValidator implements Validator<WithDependency> {

    private List<ComparisonResult> getComparisonResults(WithDependency o) {
        return Lists.<ComparisonResult>newArrayList(
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, WithDependency o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!isNullOrEmpty(res.getError())) {
                    return failure("WithDependency", ValidationResult.ValidationType.TYPE_FORMAT, "WithDependency", path, "", res.getError());
                }
                return success("WithDependency", ValidationResult.ValidationType.TYPE_FORMAT, "WithDependency", path, "");
            })
            .collect(toList());
    }

}
