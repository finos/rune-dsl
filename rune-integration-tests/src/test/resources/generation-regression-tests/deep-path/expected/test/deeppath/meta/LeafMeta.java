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
import test.deeppath.Leaf;
import test.deeppath.validation.LeafTypeFormatValidator;
import test.deeppath.validation.LeafValidator;
import test.deeppath.validation.exists.LeafOnlyExistsValidator;


/**
 * @version 0.0.0
 */
@RosettaMeta(model=Leaf.class)
public class LeafMeta implements RosettaMetaData<Leaf> {

	@Override
	public List<Validator<? super Leaf>> dataRules(ValidatorFactory factory) {
		return Arrays.asList(
		);
	}
	
	@Override
	public List<Function<? super Leaf, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<? super Leaf> validator(ValidatorFactory factory) {
		return factory.<Leaf>create(LeafValidator.class);
	}

	@Override
	public Validator<? super Leaf> typeFormatValidator(ValidatorFactory factory) {
		return factory.<Leaf>create(LeafTypeFormatValidator.class);
	}

	@Deprecated
	@Override
	public Validator<? super Leaf> validator() {
		return new LeafValidator();
	}

	@Deprecated
	@Override
	public Validator<? super Leaf> typeFormatValidator() {
		return new LeafTypeFormatValidator();
	}
	
	@Override
	public ValidatorWithArg<? super Leaf, Set<String>> onlyExistsValidator() {
		return new LeafOnlyExistsValidator();
	}
}
