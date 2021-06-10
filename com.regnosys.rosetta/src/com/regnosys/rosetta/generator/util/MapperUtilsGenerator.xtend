package com.regnosys.rosetta.generator.util

import org.eclipse.xtext.generator.IFileSystemAccess2

class MapperUtilsGenerator {
	
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
	}
}