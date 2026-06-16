package test.deeppath.meta;

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
import test.deeppath.InnerChoice;
import test.deeppath.validation.InnerChoiceTypeFormatValidator;
import test.deeppath.validation.InnerChoiceValidator;
import test.deeppath.validation.datarule.InnerChoiceChoice;
import test.deeppath.validation.exists.InnerChoiceOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=InnerChoice.class)
public class InnerChoiceMeta implements RosettaMetaData<InnerChoice> {

	@Override
	public List<Validator<? super InnerChoice>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
			factory.<InnerChoice>create(InnerChoiceChoice.class)
		);
	}
	
	@Override
	public List<Function<? super InnerChoice, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super InnerChoice> validator(ValidatorFactory factory) {
		return factory.<InnerChoice>create(InnerChoiceValidator.class);
	}

	@Override
	public Validator<? super InnerChoice> typeFormatValidator(ValidatorFactory factory) {
		return factory.<InnerChoice>create(InnerChoiceTypeFormatValidator.class);
	}

	@Override
	public ValidatorWithArg<? super InnerChoice, Set<String>> onlyExistsValidator() {
		return new InnerChoiceOnlyExistsValidator();
	}
}
