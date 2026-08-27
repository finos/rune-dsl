package test.pojo.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;
import test.pojo.GrandChild;


public class GrandChildValidator implements Validator<GrandChild> {
    private List<ComparisonResult> getComparisonResults(GrandChild o) {
        return Lists.<ComparisonResult>newArrayList(
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, GrandChild o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!Strings.isNullOrEmpty(res.getError())) {
                    return ValidationResult.failure("GrandChild", ValidationResult.ValidationType.CARDINALITY, "GrandChild", path, "", res.getError());
                }
                return ValidationResult.success("GrandChild", ValidationResult.ValidationType.CARDINALITY, "GrandChild", path, "");
            })
            .collect(Collectors.toList());
    }
}
