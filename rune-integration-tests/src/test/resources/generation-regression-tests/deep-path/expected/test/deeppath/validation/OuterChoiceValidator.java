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
import test.deeppath.InnerChoice;
import test.deeppath.Leaf;
import test.deeppath.OuterChoice;


public class OuterChoiceValidator implements Validator<OuterChoice> {
    private List<ComparisonResult> getComparisonResults(OuterChoice o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("InnerChoice", (InnerChoice) o.getInnerChoice() != null ? 1 : 0, 0, 1),
            ExpressionOperatorsNullSafe.checkCardinality("Leaf", (Leaf) o.getLeaf() != null ? 1 : 0, 0, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, OuterChoice o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("OuterChoice", ValidationResult.ValidationType.CARDINALITY, "OuterChoice", path, "", res.getError());
                }
                return ValidationResult.success("OuterChoice", ValidationResult.ValidationType.CARDINALITY, "OuterChoice", path, "");
            })
            .collect(Collectors.toList());
    }
}
