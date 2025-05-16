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
import test.pojo.Parent;
import test.pojo.validation.ParentTypeFormatValidator;
import test.pojo.validation.ParentValidator;
import test.pojo.validation.exists.ParentOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Parent.class)
public class ParentMeta implements RosettaMetaData<Parent> {

	@Override
	public List<Validator<? super Parent>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Parent, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Parent> validator(ValidatorFactory factory) {
		return factory.<Parent>create(ParentValidator.class);
	}

	@Override
	public Validator<? super Parent> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Parent>create(ParentTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Parent> validator() {
		return new ParentValidator();
	}

	@Deprecated
	@Override
	public Validator<? super Parent> typeFormatValidator() {
		return new ParentTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Parent, Set<String>> onlyExistsValidator() {
		return new ParentOnlyExistsValidator();
	}
}
