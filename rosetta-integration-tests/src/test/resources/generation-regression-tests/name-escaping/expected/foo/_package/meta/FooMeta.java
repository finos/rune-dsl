package foo._package.meta;

import com.rosetta.model.lib.annotations.RosettaMeta;
import com.rosetta.model.lib.meta.RosettaMetaData;
import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
import com.rosetta.model.lib.qualify.QualifyResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;
import com.rosetta.model.lib.validation.ValidatorWithArg;
import foo._package.Foo;
import foo._package.validation.FooTypeFormatValidator;
import foo._package.validation.FooValidator;
import foo._package.validation.exists.FooOnlyExistsValidator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Foo.class)
public class FooMeta implements RosettaMetaData<Foo> {

	@Override
	public List<Validator<? super Foo>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Foo, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Foo> validator(ValidatorFactory factory) {
		return factory.<Foo>create(FooValidator.class);
	}

	@Override
	public Validator<? super Foo> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Foo>create(FooTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Foo> validator() {
		return new FooValidator();
	}

	@Deprecated
	@Override
	public Validator<? super Foo> typeFormatValidator() {
		return new FooTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Foo, Set<String>> onlyExistsValidator() {
		return new FooOnlyExistsValidator();
	}
}
