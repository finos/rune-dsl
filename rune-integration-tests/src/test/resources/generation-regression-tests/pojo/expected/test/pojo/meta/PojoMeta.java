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
import test.pojo.Pojo;
import test.pojo.validation.PojoTypeFormatValidator;
import test.pojo.validation.PojoValidator;
import test.pojo.validation.exists.PojoOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Pojo.class)
public class PojoMeta implements RosettaMetaData<Pojo> {

	@Override
	public List<Validator<? super Pojo>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Pojo, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Pojo> validator(ValidatorFactory factory) {
		return factory.<Pojo>create(PojoValidator.class);
	}

	@Override
	public Validator<? super Pojo> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Pojo>create(PojoTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Pojo> validator() {
		return new PojoValidator();
	}

	@Deprecated
	@Override
	public Validator<? super Pojo> typeFormatValidator() {
		return new PojoTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Pojo, Set<String>> onlyExistsValidator() {
		return new PojoOnlyExistsValidator();
	}
}
