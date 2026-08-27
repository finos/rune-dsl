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
import test.deeppath.Option1;


public class Option1Validator implements Validator<Option1> {
    private List<ComparisonResult> getComparisonResults(Option1 o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("common", (String) o.getCommon() != null ? 1 : 0, 1, 1),
            ExpressionOperatorsNullSafe.checkCardinality("only1", (Integer) o.getOnly1() != null ? 1 : 0, 0, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Option1 o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Option1", ValidationResult.ValidationType.CARDINALITY, "Option1", path, "", res.getError());
                }
                return ValidationResult.success("Option1", ValidationResult.ValidationType.CARDINALITY, "Option1", path, "");
            })
            .collect(Collectors.toList());
    }
}
