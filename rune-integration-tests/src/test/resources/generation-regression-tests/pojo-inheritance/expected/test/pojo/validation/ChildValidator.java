package test.pojo.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.pojo.Child;


public class ChildValidator implements Validator<Child> {
    private List<ComparisonResult> getComparisonResults(Child o) {
        return Lists.<ComparisonResult>newArrayList(
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Child o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Child", ValidationResult.ValidationType.CARDINALITY, "Child", path, "", res.getError());
                }
                return ValidationResult.success("Child", ValidationResult.ValidationType.CARDINALITY, "Child", path, "");
            })
            .collect(Collectors.toList());
    }
}
