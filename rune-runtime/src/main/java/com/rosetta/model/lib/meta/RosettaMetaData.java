/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	
	List<Function<? super T, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory);
	
	default Validator<? super T> validator(ValidatorFactory factory) {
		return validator();
	}
	
	default Validator<? super T> typeFormatValidator(ValidatorFactory factory) {
		return typeFormatValidator();
	}

	/**
	 * @deprecated Since 9.37.0 - This method is deprecated and can be removed if models are all upgraded to this version or later.
	 *             Use {@link #validator(ValidatorFactory)} instead.
	 */
	@Deprecated
	Validator<? super T> validator();

	// @Compat. The default can be removed once validation/ingestion is in the BSP.
	/**
	 * @deprecated Since 9.37.0 - This method is deprecated and can be removed if models are all upgraded to this version or later.
	 *             Use {@link #typeFormatValidator(ValidatorFactory)} instead.
	 */
	@Deprecated
	default Validator<? super T> typeFormatValidator() {
		return null;
	}
	
	ValidatorWithArg<? super T, Set<String>> onlyExistsValidator();
}
