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

import javax.inject.Inject;

import com.google.inject.Injector;
import com.rosetta.model.lib.RosettaModelObject;

public interface ValidatorFactory {

	<T extends RosettaModelObject> Validator<? super T> create(Class<? extends Validator<T>> clazz);

	public static class Default implements ValidatorFactory {

		@Inject
		Injector injector;

		@Override
		public <T extends RosettaModelObject> Validator<? super T> create(Class<? extends Validator<T>> clazz) {
			return injector.getInstance(clazz);
		}
	}
}
