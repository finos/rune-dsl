package test.condition.meta;

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
import test.condition.WithDependency;
import test.condition.validation.WithDependencyTypeFormatValidator;
import test.condition.validation.WithDependencyValidator;
import test.condition.validation.datarule.WithDependencyUsesFunction;
import test.condition.validation.exists.WithDependencyOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=WithDependency.class)
public class WithDependencyMeta implements RosettaMetaData<WithDependency> {

	@Override
	public List<Validator<? super WithDependency>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
			factory.<WithDependency>create(WithDependencyUsesFunction.class)
		);
	}
	
	@Override
	public List<Function<? super WithDependency, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super WithDependency> validator(ValidatorFactory factory) {
		return factory.<WithDependency>create(WithDependencyValidator.class);
	}

	@Override
	public Validator<? super WithDependency> typeFormatValidator(ValidatorFactory factory) {
		return factory.<WithDependency>create(WithDependencyTypeFormatValidator.class);
	}

	@Override
	public ValidatorWithArg<? super WithDependency, Set<String>> onlyExistsValidator() {
		return new WithDependencyOnlyExistsValidator();
	}
}
