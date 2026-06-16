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
import test.deeppath.Option1;
import test.deeppath.validation.Option1TypeFormatValidator;
import test.deeppath.validation.Option1Validator;
import test.deeppath.validation.exists.Option1OnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Option1.class)
public class Option1Meta implements RosettaMetaData<Option1> {

	@Override
	public List<Validator<? super Option1>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Option1, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Option1> validator(ValidatorFactory factory) {
		return factory.<Option1>create(Option1Validator.class);
	}

	@Override
	public Validator<? super Option1> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Option1>create(Option1TypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Option1> validator() {
		return new Option1Validator();
	}

	@Deprecated
	@Override
	public Validator<? super Option1> typeFormatValidator() {
		return new Option1TypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Option1, Set<String>> onlyExistsValidator() {
		return new Option1OnlyExistsValidator();
	}
}
