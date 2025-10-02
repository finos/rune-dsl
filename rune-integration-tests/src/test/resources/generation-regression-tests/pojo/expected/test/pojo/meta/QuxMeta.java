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
import test.pojo.Qux;
import test.pojo.validation.QuxTypeFormatValidator;
import test.pojo.validation.QuxValidator;
import test.pojo.validation.exists.QuxOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Qux.class)
public class QuxMeta implements RosettaMetaData<Qux> {

	@Override
	public List<Validator<? super Qux>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Qux, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Qux> validator(ValidatorFactory factory) {
		return factory.<Qux>create(QuxValidator.class);
	}

	@Override
	public Validator<? super Qux> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Qux>create(QuxTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Qux> validator() {
		return new QuxValidator();
	}

	@Deprecated
	@Override
	public Validator<? super Qux> typeFormatValidator() {
		return new QuxTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Qux, Set<String>> onlyExistsValidator() {
		return new QuxOnlyExistsValidator();
	}
}
