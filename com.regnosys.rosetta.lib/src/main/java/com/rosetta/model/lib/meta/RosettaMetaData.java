package com.rosetta.model.lib.meta;

import java.util.List;
import java.util.function.Function;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.qualify.QualifyResult;
import com.rosetta.model.lib.validation.Validator;
import com.rosetta.model.lib.validation.ValidatorWithArg;

public interface RosettaMetaData<T extends RosettaModelObject> {

	List<Validator<? super T>> dataRules();
    
	List<Validator<? super T>> choiceRuleValidators();

	List<Function<? super T, QualifyResult>> getQualifyFunctions();
	
	Validator<? super T> validator();
	
	ValidatorWithArg<? super T,String> onlyExistsValidator();
}
