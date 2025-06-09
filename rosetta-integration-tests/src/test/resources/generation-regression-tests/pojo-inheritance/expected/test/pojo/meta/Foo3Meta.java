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
import test.pojo.Foo3;
import test.pojo.validation.Foo3TypeFormatValidator;
import test.pojo.validation.Foo3Validator;
import test.pojo.validation.exists.Foo3OnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Foo3.class)
public class Foo3Meta implements RosettaMetaData<Foo3> {

	@Override
	public List<Validator<? super Foo3>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Foo3, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Foo3> validator(ValidatorFactory factory) {
		return factory.<Foo3>create(Foo3Validator.class);
	}

	@Override
	public Validator<? super Foo3> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Foo3>create(Foo3TypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Foo3> validator() {
		return new Foo3Validator();
	}

	@Deprecated
	@Override
	public Validator<? super Foo3> typeFormatValidator() {
		return new Foo3TypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Foo3, Set<String>> onlyExistsValidator() {
		return new Foo3OnlyExistsValidator();
	}
}
