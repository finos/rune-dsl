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
import test.pojo.Bar;
import test.pojo.validation.BarTypeFormatValidator;
import test.pojo.validation.BarValidator;
import test.pojo.validation.exists.BarOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Bar.class)
public class BarMeta implements RosettaMetaData<Bar> {

	@Override
	public List<Validator<? super Bar>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Bar, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Bar> validator(ValidatorFactory factory) {
		return factory.<Bar>create(BarValidator.class);
	}

	@Override
	public Validator<? super Bar> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Bar>create(BarTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Bar> validator() {
		return new BarValidator();
	}

	@Deprecated
	@Override
	public Validator<? super Bar> typeFormatValidator() {
		return new BarTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Bar, Set<String>> onlyExistsValidator() {
		return new BarOnlyExistsValidator();
	}
}
