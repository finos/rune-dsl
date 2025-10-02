package test.escaping.index.meta;

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
import test.escaping.index.ResultEscaping;
import test.escaping.index.validation.ResultEscapingTypeFormatValidator;
import test.escaping.index.validation.ResultEscapingValidator;
import test.escaping.index.validation.exists.ResultEscapingOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=ResultEscaping.class)
public class ResultEscapingMeta implements RosettaMetaData<ResultEscaping> {

	@Override
	public List<Validator<? super ResultEscaping>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super ResultEscaping, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super ResultEscaping> validator(ValidatorFactory factory) {
		return factory.<ResultEscaping>create(ResultEscapingValidator.class);
	}

	@Override
	public Validator<? super ResultEscaping> typeFormatValidator(ValidatorFactory factory) {
		return factory.<ResultEscaping>create(ResultEscapingTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super ResultEscaping> validator() {
		return new ResultEscapingValidator();
	}

	@Deprecated
	@Override
	public Validator<? super ResultEscaping> typeFormatValidator() {
		return new ResultEscapingTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super ResultEscaping, Set<String>> onlyExistsValidator() {
		return new ResultEscapingOnlyExistsValidator();
	}
}
