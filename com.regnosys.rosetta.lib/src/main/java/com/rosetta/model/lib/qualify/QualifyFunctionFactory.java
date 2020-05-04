package com.rosetta.model.lib.qualify;

import java.util.function.Function;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.functions.IQualifyFunctionExtension;
import com.rosetta.model.lib.validation.ComparisonResult;

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
