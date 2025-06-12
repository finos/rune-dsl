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
import test.pojo.Foo1;
import test.pojo.validation.Foo1TypeFormatValidator;
import test.pojo.validation.Foo1Validator;
import test.pojo.validation.exists.Foo1OnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Foo1.class)
public class Foo1Meta implements RosettaMetaData<Foo1> {

	@Override
	public List<Validator<? super Foo1>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Foo1, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Foo1> validator(ValidatorFactory factory) {
		return factory.<Foo1>create(Foo1Validator.class);
	}

	@Override
	public Validator<? super Foo1> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Foo1>create(Foo1TypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Foo1> validator() {
		return new Foo1Validator();
	}

	@Deprecated
	@Override
	public Validator<? super Foo1> typeFormatValidator() {
		return new Foo1TypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Foo1, Set<String>> onlyExistsValidator() {
		return new Foo1OnlyExistsValidator();
	}
}
