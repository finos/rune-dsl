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

package com.regnosys.rosetta.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.rosetta.ExternalAnnotationSource;
import com.regnosys.rosetta.rosetta.ExternalValueOperator;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.RosettaExternalEnum;
import com.regnosys.rosetta.rosetta.RosettaExternalEnumValue;
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.RosettaRuleReference;

public class ExternalAnnotationUtil {
	
	@Inject
	private RosettaExtensions rosettaExtensions;
	
	
	// @Compat. Can be removed once `RosettaSynonymSource` is gone.
	public List<ExternalAnnotationSource> getSuperSources(ExternalAnnotationSource source) {
		if (source instanceof RosettaExternalSynonymSource) {
			RosettaExternalSynonymSource synSource = (RosettaExternalSynonymSource)source;
			return synSource.getSuperSources().stream()
					.filter(s -> s instanceof ExternalAnnotationSource)
					.map(s -> (ExternalAnnotationSource)s)
					.collect(Collectors.toList());
		} else if (source instanceof RosettaExternalRuleSource) {
			RosettaExternalRuleSource ruleSource = (RosettaExternalRuleSource)source;
			return ruleSource.getSuperSources();
		} else {
			return Collections.emptyList();
		}
	}
	
	public Optional<RosettaExternalClass> getExternalType(ExternalAnnotationSource source, Data type) {
		for (RosettaExternalClass extT: source.getExternalClasses()) {
			if (extT.getTypeRef().equals(type)) {
				return Optional.of(extT);
			}
		}
		return Optional.empty();
	}
	
	public Optional<RosettaExternalEnum> getExternalEnum(ExternalAnnotationSource source, RosettaEnumeration type) {
		for (RosettaExternalEnum extT: source.getExternalEnums()) {
			if (extT.getTypeRef().equals(type)) {
				return Optional.of(extT);
			}
		}
		return Optional.empty();
	}
	
	/**
	 * 
	 * @param source
	 * @param type
	 * @return
	 */
	public Set<RosettaExternalClass> getAllExternalTypes(ExternalAnnotationSource source, Data type) {
		Set<RosettaExternalClass> result = new HashSet<RosettaExternalClass>();
		getSuperSources(source).stream().map(s -> getAllExternalTypes(s, type))
			.forEach(s -> result.addAll(s));
		getExternalType(source, type).ifPresent(extT -> result.add(extT));
		return result;
	}
	
	public Set<RosettaExternalEnum> getAllExternalEnums(ExternalAnnotationSource source, RosettaEnumeration type) {
		Set<RosettaExternalEnum> result = new HashSet<RosettaExternalEnum>();
		getSuperSources(source).stream().map(s -> getAllExternalEnums(s, type))
			.forEach(s -> result.addAll(s));
		getExternalEnum(source, type).ifPresent(extT -> result.add(extT));
		return result;
	}
	
	public Set<RosettaExternalRegularAttribute> getAllExternalAttributesForType(ExternalAnnotationSource source, Data type) {
		Set<RosettaExternalRegularAttribute> result = new HashSet<RosettaExternalRegularAttribute>();
		getSuperSources(source).stream().map(s -> getAllExternalAttributesForType(s, type))
			.forEach(s -> result.addAll(s));
		getExternalType(source, type).ifPresent(extT -> {
			for (RosettaExternalRegularAttribute attr: extT.getRegularAttributes()) {
				if (attr.getOperator().equals(ExternalValueOperator.MINUS)) {
					result.removeIf(a -> a.getAttributeRef().equals(attr.getAttributeRef()));
				} else { // attr.getOperator().equals(ExternalValueOperator.PLUS)
					result.add(attr);
				}
			}
		});
		return result;
	}
	
	public Set<RosettaExternalEnumValue> getAllExternalEnumValuesForType(ExternalAnnotationSource source, RosettaEnumeration type) {
		Set<RosettaExternalEnumValue> result = new HashSet<RosettaExternalEnumValue>();
		getSuperSources(source).stream().map(s -> getAllExternalEnumValuesForType(s, type))
			.forEach(s -> result.addAll(s));
		getExternalEnum(source, type).ifPresent(extT -> {
			for (RosettaExternalEnumValue val: extT.getRegularValues()) {
				if (val.getOperator().equals(ExternalValueOperator.MINUS)) {
					result.removeIf(a -> a.getEnumRef().equals(val.getEnumRef()));
				} else { // val.getOperator().equals(ExternalValueOperator.PLUS)
					result.add(val);
				}
			}
		});
		return result;
	}
	
	public Map<RosettaFeature, RosettaRuleReference> getAllRuleReferencesForType(Optional<? extends ExternalAnnotationSource> maybeSource, Data type) {
		CollectRuleVisitor.Default visitor = new CollectRuleVisitor.Default();
		
		collectAllRuleReferencesForType(maybeSource, type, visitor);
		
		return visitor.getMap();
	}
	
	public <T extends CollectRuleVisitor> T collectAllRuleReferencesForType(Optional<? extends ExternalAnnotationSource> maybeSource, Data type, T visitor) {
		// collect inline rule reference
		rosettaExtensions.getAllAttributes(type)
			.forEach(attr -> 
				Optional.ofNullable(attr.getRuleReference()).ifPresent(rule -> visitor.add(attr, rule)));
		
		maybeSource.ifPresent((source) -> {
			// collect external super sources
			List<ExternalAnnotationSource> superSources = getSuperSources(source);
			// reverse so the sources are applied in the correct order
			Collections.reverse(superSources);
			superSources.forEach(s -> collectExternalRuleReferencesForType(s, type, visitor));
			
			// collect this external source
			collectExternalRuleReferencesForType(source, type, visitor);
		});
		
		return visitor;
	}
	
	private void collectExternalRuleReferencesForType(ExternalAnnotationSource source, Data type, CollectRuleVisitor visitor) {
		getExternalType(source, type).ifPresent(extT -> {
			for (RosettaExternalRegularAttribute extAttr: extT.getRegularAttributes()) {
				if (extAttr.getOperator().equals(ExternalValueOperator.MINUS)) {
					visitor.remove(extAttr);
				} else {
					if (extAttr.getExternalRuleReference() != null) {
						visitor.add(extAttr, extAttr.getExternalRuleReference());
					}
				}
			}
		});
	}
	
	public interface CollectRuleVisitor {
		
		void add(RosettaFeature attr, RosettaRuleReference rule);
		void add(RosettaExternalRegularAttribute extAttr, RosettaRuleReference rule);
		void remove(RosettaExternalRegularAttribute attr);
		
		public static class Default implements CollectRuleVisitor {

			private final Map<RosettaFeature, RosettaRuleReference> map = new HashMap<>();
			
			@Override
			public void add(RosettaFeature attr, RosettaRuleReference rule) {
				map.put(attr, rule);
			}
			
			@Override
			public void add(RosettaExternalRegularAttribute extAttr, RosettaRuleReference rule) {
				map.put(extAttr.getAttributeRef(), rule);
			}

			@Override
			public void remove(RosettaExternalRegularAttribute extAttr) {
				map.remove(extAttr.getAttributeRef());
			}
			
			public Map<RosettaFeature, RosettaRuleReference> getMap() {
				return map;
			}
		}
	}
}
