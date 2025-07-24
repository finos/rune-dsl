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

import java.util.Collections;
import java.util.List;
import java.util.Set;
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
	public List<Function<? super T, QualifyResult>> getQualifyFunctions(QualifyFunctionFactory factory) {
		return Collections.emptyList();
	}
	
	@Override
	public Validator<T> validator() {
		return null;
	}
	
	@Override
	public Validator<T> typeFormatValidator() {
		return null;
	}

	@Override
	public ValidatorWithArg<T, Set<String>> onlyExistsValidator() {
		return null;
	}
}
