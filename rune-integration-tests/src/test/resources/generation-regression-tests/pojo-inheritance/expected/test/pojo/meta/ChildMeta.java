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
import test.pojo.Child;
import test.pojo.validation.ChildTypeFormatValidator;
import test.pojo.validation.ChildValidator;
import test.pojo.validation.exists.ChildOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Child.class)
public class ChildMeta implements RosettaMetaData<Child> {

	@Override
	public List<Validator<? super Child>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Child, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Child> validator(ValidatorFactory factory) {
		return factory.<Child>create(ChildValidator.class);
	}

	@Override
	public Validator<? super Child> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Child>create(ChildTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Child> validator() {
		return new ChildValidator();
	}

	@Deprecated
	@Override
	public Validator<? super Child> typeFormatValidator() {
		return new ChildTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Child, Set<String>> onlyExistsValidator() {
		return new ChildOnlyExistsValidator();
	}
}
