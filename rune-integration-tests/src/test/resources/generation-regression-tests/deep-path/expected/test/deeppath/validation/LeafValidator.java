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
import test.deeppath.Leaf;


public class LeafValidator implements Validator<Leaf> {
    private List<ComparisonResult> getComparisonResults(Leaf o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("common", (String) o.getCommon() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Leaf o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Leaf", ValidationResult.ValidationType.CARDINALITY, "Leaf", path, "", res.getError());
                }
                return ValidationResult.success("Leaf", ValidationResult.ValidationType.CARDINALITY, "Leaf", path, "");
            })
            .collect(Collectors.toList());
    }
}
