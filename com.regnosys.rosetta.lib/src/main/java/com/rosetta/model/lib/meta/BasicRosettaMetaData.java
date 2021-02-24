package com.rosetta.model.lib.meta;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
import com.rosetta.model.lib.qualify.QualifyResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;
import com.rosetta.model.lib.validation.ValidatorWithArg;

public class BasicRosettaMetaData<T extends RosettaModelObject> implements RosettaMetaData<T> {

	@Override
	public List<Validator<? super T>> dataRules(ValidatorFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Validator<? super T>> choiceRuleValidators() {
		return Collections.emptyList();
	}
	
	@Override
	public List<Function<? super T, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<T> validator() {
		return null;
	}

	@Override
	public ValidatorWithArg<T, String> onlyExistsValidator() {
		return null;
	}
}
