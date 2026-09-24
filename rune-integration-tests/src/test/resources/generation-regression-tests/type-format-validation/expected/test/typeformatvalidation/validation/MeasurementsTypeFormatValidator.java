package test.typeformatvalidation.validation;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import test.typeformatvalidation.Measurements;
import test.typeformatvalidation.validation.datarule.PositiveIntIsPositive;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkNumber;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class MeasurementsTypeFormatValidator implements Validator<Measurements> {
    @Inject
    protected PositiveIntIsPositive positiveIntIsPositive;

    private List<ComparisonResult> getComparisonResults(Measurements o) {
        return Lists.<ComparisonResult>newArrayList(
            checkNumber("amount", o.getAmount(), empty(), of(0), empty(), empty()),
            checkNumber("amounts", o.getAmounts(), empty(), of(0), empty(), empty())
        );
    }

    private List<ValidationResult<?>> runConditions(RosettaPath path, Measurements o) {
        List<ValidationResult<?>> results = new ArrayList();
        results.addAll(positiveIntIsPositive.getValidationResults(path.newSubPath("amount"), o.getAmount()));
        final List<Integer> amounts = o.getAmounts();
        if (amounts != null) {
            for (int i = 0; i < amounts.size(); i++) {
                results.addAll(positiveIntIsPositive.getValidationResults(path.newSubPath("amounts").withIndex(i), amounts.get(i)));
            }
        }
        return results;
    }

    @Override
    public List<ValidationResult<?>> getValidationResults(RosettaPath path, Measurements o) {
        return Streams.concat(getComparisonResults(o)
            .stream()
            .map(res -> {
                if (!isNullOrEmpty(res.getError())) {
                    return failure("Measurements", ValidationResult.ValidationType.TYPE_FORMAT, "Measurements", path, "", res.getError());
                }
                return success("Measurements", ValidationResult.ValidationType.TYPE_FORMAT, "Measurements", path, "");
            }),
            runConditions(path, o).stream()
        )
        .collect(toList());
    }

}
