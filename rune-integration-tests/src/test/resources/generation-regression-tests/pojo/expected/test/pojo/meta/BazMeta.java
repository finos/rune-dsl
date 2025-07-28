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
import test.pojo.Baz;
import test.pojo.validation.BazTypeFormatValidator;
import test.pojo.validation.BazValidator;
import test.pojo.validation.exists.BazOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Baz.class)
public class BazMeta implements RosettaMetaData<Baz> {

	@Override
	public List<Validator<? super Baz>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Baz, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Baz> validator(ValidatorFactory factory) {
		return factory.<Baz>create(BazValidator.class);
	}

	@Override
	public Validator<? super Baz> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Baz>create(BazTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Baz> validator() {
		return new BazValidator();
	}

	@Deprecated
	@Override
	public Validator<? super Baz> typeFormatValidator() {
		return new BazTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Baz, Set<String>> onlyExistsValidator() {
		return new BazOnlyExistsValidator();
	}
}
