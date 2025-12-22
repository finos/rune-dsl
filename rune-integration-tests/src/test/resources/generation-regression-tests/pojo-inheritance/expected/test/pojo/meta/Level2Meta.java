package test.pojo.meta;

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
import test.pojo.Level2;
import test.pojo.validation.Level2TypeFormatValidator;
import test.pojo.validation.Level2Validator;
import test.pojo.validation.exists.Level2OnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Level2.class)
public class Level2Meta implements RosettaMetaData<Level2> {

	@Override
	public List<Validator<? super Level2>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Level2, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Level2> validator(ValidatorFactory factory) {
		return factory.<Level2>create(Level2Validator.class);
	}

	@Override
	public Validator<? super Level2> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Level2>create(Level2TypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Level2> validator() {
		return new Level2Validator();
	}

	@Deprecated
	@Override
	public Validator<? super Level2> typeFormatValidator() {
		return new Level2TypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Level2, Set<String>> onlyExistsValidator() {
		return new Level2OnlyExistsValidator();
	}
}
