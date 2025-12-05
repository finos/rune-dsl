package test.pojo.validation;

import com.google.common.collect.Lists;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.validation.ValidationResult;
import com.rosetta.model.lib.validation.Validator;
import java.math.BigDecimal;
import java.util.List;
import test.pojo.Foo2;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkNumber;
import static com.rosetta.model.lib.expression.ExpressionOperatorsNullSafe.checkString;
import static com.rosetta.model.lib.validation.ValidationResult.failure;
import static com.rosetta.model.lib.validation.ValidationResult.success;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class Foo2TypeFormatValidator implements Validator<Foo2> {

	private List<ComparisonResult> getComparisonResults(Foo2 o) {
		return Lists.<ComparisonResult>newArrayList(
				checkNumber("attr", o.getAttr(), empty(), of(0), empty(), empty()), 
				checkNumber("numberAttr", o.getNumberAttrOverriddenAsBigInteger(), of(30), of(0), empty(), of(new BigDecimal("1E+2"))), 
				checkString("stringAttr", o.getStringAttr().getValue(), 0, of(42), empty())
			);
	}

	@Override
	public List<ValidationResult<?>> getValidationResults(RosettaPath path, Foo2 o) {
		return getComparisonResults(o)
			.stream()
			.map(res -> {
				if (!isNullOrEmpty(res.getError())) {
					return failure("Foo2", ValidationResult.ValidationType.TYPE_FORMAT, "Foo2", path, "", res.getError());
				}
				return success("Foo2", ValidationResult.ValidationType.TYPE_FORMAT, "Foo2", path, "");
			})
			.collect(toList());
	}

}
