package test.pojo.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.metafields.ReferenceWithMetaString;
import java.util.List;
import java.util.stream.Collectors;
import test.pojo.Baz;


public class BazValidator implements Validator<Baz> {
    private List<ComparisonResult> getComparisonResults(Baz o) {
        return Lists.<ComparisonResult>newArrayList(
            ExpressionOperatorsNullSafe.checkCardinality("baz", (ReferenceWithMetaString) o.getBaz() != null ? 1 : 0, 1, 1)
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Baz o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("Baz", ValidationResult.ValidationType.CARDINALITY, "Baz", path, "", res.getError());
                }
                return ValidationResult.success("Baz", ValidationResult.ValidationType.CARDINALITY, "Baz", path, "");
            })
            .collect(Collectors.toList());
    }
}
