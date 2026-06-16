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
import test.deeppath.OuterChoice;
import test.deeppath.validation.OuterChoiceTypeFormatValidator;
import test.deeppath.validation.OuterChoiceValidator;
import test.deeppath.validation.datarule.OuterChoiceChoice;
import test.deeppath.validation.exists.OuterChoiceOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=OuterChoice.class)
public class OuterChoiceMeta implements RosettaMetaData<OuterChoice> {

	@Override
	public List<Validator<? super OuterChoice>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
			factory.<OuterChoice>create(OuterChoiceChoice.class)
		);
	}
	
	@Override
	public List<Function<? super OuterChoice, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super OuterChoice> validator(ValidatorFactory factory) {
		return factory.<OuterChoice>create(OuterChoiceValidator.class);
	}

	@Override
	public Validator<? super OuterChoice> typeFormatValidator(ValidatorFactory factory) {
		return factory.<OuterChoice>create(OuterChoiceTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super OuterChoice> validator() {
		return new OuterChoiceValidator();
	}

	@Deprecated
	@Override
	public Validator<? super OuterChoice> typeFormatValidator() {
		return new OuterChoiceTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super OuterChoice, Set<String>> onlyExistsValidator() {
		return new OuterChoiceOnlyExistsValidator();
	}
}
