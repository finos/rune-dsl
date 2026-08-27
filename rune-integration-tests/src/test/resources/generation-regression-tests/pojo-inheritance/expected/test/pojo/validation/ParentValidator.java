package test.pojo.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.pojo.Parent;


public class ParentValidator implements Validator<Parent> {
    private List<ComparisonResult> getComparisonResults(Parent o) {
        return Lists.<ComparisonResult>newArrayList(
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Parent o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Parent", ValidationResult.ValidationType.CARDINALITY, "Parent", path, "", res.getError());
                }
                return ValidationResult.success("Parent", ValidationResult.ValidationType.CARDINALITY, "Parent", path, "");
            })
            .collect(Collectors.toList());
    }
}
