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

package com.rosetta.model.lib.qualify;

import java.util.function.Function;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.expression.ComparisonResult;
import com.rosetta.model.lib.functions.IQualifyFunctionExtension;

public interface QualifyFunctionFactory {

	<T extends RosettaModelObject> Function<? super T, QualifyResult> create(
			Class<? extends IQualifyFunctionExtension<T>> clazz);

	public static class Default implements QualifyFunctionFactory {

		@Inject
		Injector injector;

		@Override
		public <T extends RosettaModelObject> Function<? super T, QualifyResult> create(
				Class<? extends IQualifyFunctionExtension<T>> clazz) {
			return new Function<T, QualifyResult>() {

				@Override
				public QualifyResult apply(T t) {
					String funcName = clazz.getSimpleName();
					ComparisonResult result = ComparisonResult.success();
					if (injector == null)
						throw new IllegalArgumentException(
								"Injector instance not available. Use @Inject to get an instance of QualifyFunctionFactory.Default");
					IQualifyFunctionExtension<T> functionExtension = injector.getInstance(clazz);
					if (!functionExtension.evaluate(t)) {
						result = ComparisonResult.failure(funcName + " returned false.");
					}
					String prefix = functionExtension.getNamePrefix() + "_";
					String qualifiedName = funcName.replaceFirst(prefix, "");
					return QualifyResult.builder().setName(qualifiedName).setExpressionResult(funcName, result).build();
				}
			};
		}
	}
}
