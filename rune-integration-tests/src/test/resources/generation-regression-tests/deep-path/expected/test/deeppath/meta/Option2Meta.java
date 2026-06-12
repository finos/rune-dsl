package test.deeppath.meta;

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
import test.deeppath.Option2;
import test.deeppath.validation.Option2TypeFormatValidator;
import test.deeppath.validation.Option2Validator;
import test.deeppath.validation.exists.Option2OnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Option2.class)
public class Option2Meta implements RosettaMetaData<Option2> {

	@Override
	public List<Validator<? super Option2>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Option2, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Option2> validator(ValidatorFactory factory) {
		return factory.<Option2>create(Option2Validator.class);
	}

	@Override
	public Validator<? super Option2> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Option2>create(Option2TypeFormatValidator.class);
	}

	@Override
	public ValidatorWithArg<? super Option2, Set<String>> onlyExistsValidator() {
		return new Option2OnlyExistsValidator();
	}
}
