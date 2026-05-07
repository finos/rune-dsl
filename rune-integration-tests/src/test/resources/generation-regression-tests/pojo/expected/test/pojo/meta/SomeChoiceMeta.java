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
import test.pojo.SomeChoice;
import test.pojo.validation.SomeChoiceTypeFormatValidator;
import test.pojo.validation.SomeChoiceValidator;
import test.pojo.validation.datarule.SomeChoiceChoice;
import test.pojo.validation.exists.SomeChoiceOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=SomeChoice.class)
public class SomeChoiceMeta implements RosettaMetaData<SomeChoice> {

	@Override
	public List<Validator<? super SomeChoice>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
			factory.<SomeChoice>create(SomeChoiceChoice.class)
		);
	}
	
	@Override
	public List<Function<? super SomeChoice, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super SomeChoice> validator(ValidatorFactory factory) {
		return factory.<SomeChoice>create(SomeChoiceValidator.class);
	}

	@Override
	public Validator<? super SomeChoice> typeFormatValidator(ValidatorFactory factory) {
		return factory.<SomeChoice>create(SomeChoiceTypeFormatValidator.class);
	}

	@Override
	public ValidatorWithArg<? super SomeChoice, Set<String>> onlyExistsValidator() {
		return new SomeChoiceOnlyExistsValidator();
	}
}
