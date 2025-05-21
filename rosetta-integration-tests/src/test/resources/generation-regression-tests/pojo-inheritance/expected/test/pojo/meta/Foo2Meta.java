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
import test.pojo.Foo2;
import test.pojo.validation.Foo2TypeFormatValidator;
import test.pojo.validation.Foo2Validator;
import test.pojo.validation.exists.Foo2OnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Foo2.class)
public class Foo2Meta implements RosettaMetaData<Foo2> {

	@Override
	public List<Validator<? super Foo2>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Foo2, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Foo2> validator(ValidatorFactory factory) {
		return factory.<Foo2>create(Foo2Validator.class);
	}

	@Override
	public Validator<? super Foo2> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Foo2>create(Foo2TypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Foo2> validator() {
		return new Foo2Validator();
	}

	@Deprecated
	@Override
	public Validator<? super Foo2> typeFormatValidator() {
		return new Foo2TypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Foo2, Set<String>> onlyExistsValidator() {
		return new Foo2OnlyExistsValidator();
	}
}
