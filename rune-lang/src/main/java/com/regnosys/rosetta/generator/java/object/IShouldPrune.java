package com.regnosys.rosetta.generator.java.object;

import com.google.inject.ImplementedBy;
import com.regnosys.rosetta.config.RosettaGeneratorsConfiguration;
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface;
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;

import jakarta.inject.Inject;

@ImplementedBy(IShouldPrune.Default.class)
public interface IShouldPrune {
	boolean shouldBePruned(JavaPojoInterface pojo, JavaPojoProperty prop);
	boolean mayBeEmpty(JavaPojoInterface pojo, JavaPojoProperty prop);
	
	/**
	 * TODO: this should be implemented based on annotations rather than configuration.
	 * Once this is done, restore PruningTest.
	 */
	static class Default implements IShouldPrune {
		@Inject
		private JavaTypeTranslator translator;
		@Inject
		private RosettaGeneratorsConfiguration config;
		
		@Override
		public boolean shouldBePruned(JavaPojoInterface pojo, JavaPojoProperty prop) {
			return !isPruningDisabledInConfig(pojo, prop) && translator.isRosettaModelObject(prop.getType());
		}
		
		@Override
		public boolean mayBeEmpty(JavaPojoInterface pojo, JavaPojoProperty prop) {
			return !isPruningDisabledInConfig(pojo, prop) && translator.isValueRosettaModelObject(prop.getType());
		}
		
		private boolean isPruningDisabledInConfig(JavaPojoInterface pojo, JavaPojoProperty prop) {
			return config.doNotPrune().stream()
					.filter(ref -> ref.getType().equals(pojo.getCanonicalName().withDots()))
					.filter(ref -> ref.getAttribute().equals(prop.getRuneName()))
					.findAny()
					.isPresent();
		}
	}
}
