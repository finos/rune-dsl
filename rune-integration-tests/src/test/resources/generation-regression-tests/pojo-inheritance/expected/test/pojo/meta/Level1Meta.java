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
import test.pojo.Level1;
import test.pojo.validation.Level1TypeFormatValidator;
import test.pojo.validation.Level1Validator;
import test.pojo.validation.exists.Level1OnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Level1.class)
public class Level1Meta implements RosettaMetaData<Level1> {

	@Override
	public List<Validator<? super Level1>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Level1, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Level1> validator(ValidatorFactory factory) {
		return factory.<Level1>create(Level1Validator.class);
	}

	@Override
	public Validator<? super Level1> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Level1>create(Level1TypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Level1> validator() {
		return new Level1Validator();
	}

	@Deprecated
	@Override
	public Validator<? super Level1> typeFormatValidator() {
		return new Level1TypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Level1, Set<String>> onlyExistsValidator() {
		return new Level1OnlyExistsValidator();
	}
}
