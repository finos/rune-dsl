package test.condition.meta;

import com.rosetta.model.lib.annotations.RosettaMeta;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
import com.rosetta.model.lib.qualify.QualifyResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import test.condition.Range;
import test.condition.validation.RangeTypeFormatValidator;
import test.condition.validation.RangeValidator;
import test.condition.validation.exists.RangeOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Range.class)
public class RangeMeta implements RosettaMetaData<Range> {

	@Override
	public List<Validator<? super Range>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Range, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Range> validator(ValidatorFactory factory) {
		return factory.<Range>create(RangeValidator.class);
	}

	@Override
	public Validator<? super Range> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Range>create(RangeTypeFormatValidator.class);
	}

	@Override
	public ValidatorWithArg<? super Range, Set<String>> onlyExistsValidator() {
		return new RangeOnlyExistsValidator();
	}
}
