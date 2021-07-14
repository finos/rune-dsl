package com.rosetta.model.lib.meta;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.qualify.QualifyFunctionFactory;
import com.rosetta.model.lib.qualify.QualifyResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorFactory;
import com.rosetta.model.lib.validation.ValidatorWithArg;

public interface RosettaMetaData<T extends RosettaModelObject> {

	List<Validator<? super T>> dataRules(ValidatorFactory factory);
	
	List<Validator<? super T>> choiceRuleValidators();
	
	List<Function<? super T, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory);
	
	Validator<? super T> validator();
	
	ValidatorWithArg<? super T, Set<String>> onlyExistsValidator();
}
