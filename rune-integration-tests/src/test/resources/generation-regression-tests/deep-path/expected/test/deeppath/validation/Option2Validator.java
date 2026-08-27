package test.deeppath.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.deeppath.Option2;


public class Option2Validator implements Validator<Option2> {
    private List<ComparisonResult> getComparisonResults(Option2 o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("common", (String) o.getCommon() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Option2 o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Option2", ValidationResult.ValidationType.CARDINALITY, "Option2", path, "", res.getError());
                }
                return ValidationResult.success("Option2", ValidationResult.ValidationType.CARDINALITY, "Option2", path, "");
            })
            .collect(Collectors.toList());
    }
}
