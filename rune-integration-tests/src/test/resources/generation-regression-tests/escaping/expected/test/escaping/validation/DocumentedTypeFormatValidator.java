package test.escaping.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.escaping.Documented;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.stream.Collectors.toList;

public class DocumentedTypeFormatValidator implements Validator<Documented> {

    private List<ComparisonResult> getComparisonResults(Documented o) {
        return Lists.<ComparisonResult>newArrayList(
        );
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Documented o) {
        return getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!isNullOrEmpty(res.getError())) {
                    return failure("Documented", ValidationResult.ValidationType.TYPE_FORMAT, "Documented", path, "", res.getError());
                }
                return success("Documented", ValidationResult.ValidationType.TYPE_FORMAT, "Documented", path, "");
            })
            .collect(toList());
    }

}
