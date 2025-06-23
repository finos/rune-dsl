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

package com.rosetta.model.lib.validation;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.path.RosettaPath;

public interface Validator<T> {

	default List<ValidationResult<?>> getValidationResults(RosettaPath path, T objectToBeValidated) {
		return Lists.newArrayList(validate(path, (RosettaModelObject) objectToBeValidated)); // @Compat: for backwards compatibility. Old generated code will not have an implementation for this method.
	}

	/**
	 * @deprecated Since 9.7.0: use `getValidationResults` instead.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	default ValidationResult<T> validate(RosettaPath path, T objectToBeValidated) {
		List<ValidationResult<?>> results = getValidationResults(path, objectToBeValidated);
		if (results.isEmpty()) {
			return ValidationResult.success(null, null, null, path, null);
		}
		ValidationResult<?> first = results.get(0);
		if (results.size() == 1) {
			return (ValidationResult<T>) first;
		}

		String error = results.stream()
				.filter(res -> res.getFailureReason().isPresent())
				.map(res -> res.getFailureReason().get())
				.collect(Collectors.joining("; "));
		if (!Strings.isNullOrEmpty(error)) {
			return ValidationResult.failure(first.getName(), first.getValidationType(), first.getModelObjectName(), path, first.getDefinition(), error);
		}
		return ValidationResult.success(first.getName(), first.getValidationType(), first.getModelObjectName(), path, first.getDefinition());
	}

	/**
	 * @deprecated Since 9.58.1: this method is here to remain backwards compatible and will be removed in the future.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	default List<ValidationResult<?>> getValidationResults(RosettaPath path, RosettaModelObject objectToBeValidated) {
		return getValidationResults(path, (T)objectToBeValidated);
	}
	/**
	 * @deprecated Since 9.58.1: this method is here to remain backwards compatible and will be removed in the future.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	default ValidationResult<T> validate(RosettaPath path, RosettaModelObject objectToBeValidated) {
		return validate(path, (T)objectToBeValidated);
	}
}
