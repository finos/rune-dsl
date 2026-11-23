package test.escaping.javalang.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class ObjectTypeFormatValidator implements Validator<test.escaping.javalang.Object> {

    private List<ComparisonResult> getComparisonResults(test.escaping.javalang.Object o) {
        return Lists.<ComparisonResult>newArrayList(
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, test.escaping.javalang.Object o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!isNullOrEmpty(res.getError())) {
                    return failure("Object", ValidationResult.ValidationType.TYPE_FORMAT, "Object", path, "", res.getError());
                }
                return success("Object", ValidationResult.ValidationType.TYPE_FORMAT, "Object", path, "");
            })
            .collect(toList());
    }

}
