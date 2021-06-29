package com.regnosys.rosetta.generator.util

import org.eclipse.xtext.generator.IFileSystemAccess2

/**
 * Generator provides backward compatibility for models stuck on earlier model versions.
 */
class BackwardCompatibilityGenerator {
	
	def generate(IFileSystemAccess2 fsa) {
		fsa.generateFile('com/rosetta/model/lib/mapper/MapperUtils.java',
			'''
			package com.rosetta.model.lib.mapper;
			
			import java.util.function.Supplier;
			import com.rosetta.model.lib.expression.ComparisonResult;
			
			public class MapperUtils {
				
				/**
				 * Used when generating code for nested if statements
				 */
				public static <T> Mapper<T> from(Supplier<Mapper<T>> supplier) {
					return supplier.get();
				}
			
				public static ComparisonResult toComparisonResult(Mapper<Boolean> mapper) {
					if (mapper instanceof ComparisonResult) {
						return (ComparisonResult) mapper;
					} else {
						return mapper.getMulti().stream().allMatch(Boolean::booleanValue) ? ComparisonResult.success() : ComparisonResult.failure("");
					}
				}
			}
			''')
			
		fsa.generateFile('com/rosetta/model/lib/expression/CardinalityOperator.java',
			'''
			package com.rosetta.model.lib.expression;
			
			/**
			 * Generated by com.regnosys.rosetta.generator.util.BackwardCompatabilityGenerator.java.
			 * Provide compatibility for CDM versions before 2.111.0 (which uses DSL version 4.9.1).
			 * Can be removed once all model CDM versions have been updated to 2.111.0.
			 */
			public enum CardinalityOperator {
				All,
				Any
			}
			''')
	}
}