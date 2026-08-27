package test.pojo.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.pojo.Level1;


public class Level1Validator implements Validator<Level1> {
    private List<ComparisonResult> getComparisonResults(Level1 o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("attr", (Integer) o.getAttr() != null ? 1 : 0, 0, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Level1 o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Level1", ValidationResult.ValidationType.CARDINALITY, "Level1", path, "", res.getError());
                }
                return ValidationResult.success("Level1", ValidationResult.ValidationType.CARDINALITY, "Level1", path, "");
            })
            .collect(Collectors.toList());
    }
}
