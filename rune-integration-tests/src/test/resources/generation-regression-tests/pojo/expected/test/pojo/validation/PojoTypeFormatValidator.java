package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.util.List;
import test.pojo.Pojo;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkString;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class PojoTypeFormatValidator implements Validator<Pojo> {

	private List<ComparisonResult> getComparisonResults(Pojo o) {
		return Lists.<ComparisonResult>newArrayList(
				checkString("simpleAttr", o.getSimpleAttr(), 0, of(42), empty()), 
				checkString("multiSimpleAttr", o.getMultiSimpleAttr(), 0, of(42), empty())
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Pojo o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Pojo", ValidationResult.ValidationType.TYPE_FORMAT, "Pojo", path, "", res.getError());
				}
				return success("Pojo", ValidationResult.ValidationType.TYPE_FORMAT, "Pojo", path, "");
			})
			.collect(toList());
	}

}
