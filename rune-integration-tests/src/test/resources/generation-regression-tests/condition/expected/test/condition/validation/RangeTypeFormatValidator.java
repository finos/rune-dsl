package test.condition.validation;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import test.condition.Range;
import test.condition.validation.datarule.BoundedInRange;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkNumber;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class RangeTypeFormatValidator implements Validator<Range> {
    @Inject
    protected BoundedInRange boundedInRange;

    private List<ComparisonResult> getComparisonResults(Range o) {
        return Lists.<ComparisonResult>newArrayList(
            checkNumber("quantity", o.getQuantity(), empty(), of(0), empty(), empty())
        );
    }

    private List<ValidationResult<?>> runConditions(RosettaPath path, Range o) {
        List<ValidationResult<?>> results = new ArrayList();
        results.addAll(boundedInRange.getValidationResults(path.newSubPath("quantity"), o.getQuantity(), 0, 100));return results;
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Range o) {
        return Streams.concat(getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!isNullOrEmpty(res.getError())) {
                    return failure("Range", ValidationResult.ValidationType.TYPE_FORMAT, "Range", path, "", res.getError());
                }
                return success("Range", ValidationResult.ValidationType.TYPE_FORMAT, "Range", path, "");
            }),
            runConditions(path, o).stream()
        )
        .collect(toList());
    }

}
