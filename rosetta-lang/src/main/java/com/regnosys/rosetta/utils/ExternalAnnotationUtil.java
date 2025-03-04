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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import com.regnosys.rosetta.rosetta.ExternalAnnotationSource;
import com.regnosys.rosetta.rosetta.ExternalValueOperator;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.RosettaExternalEnum;
import com.regnosys.rosetta.rosetta.RosettaExternalEnumValue;
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.rosetta.model.lib.path.RosettaPath;

public class ExternalAnnotationUtil {
	@Inject
	private RObjectFactory objectFactory;
	
	
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
	
	public Optional<RosettaExternalClass> getExternalType(ExternalAnnotationSource source, RDataType type) {
		for (RosettaExternalClass extT: source.getExternalClasses()) {
			if (type.getAllSuperTypes().stream().anyMatch(t -> extT.getTypeRef().equals(t.getEObject()))) {
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
	public Set<RosettaExternalClass> getAllExternalTypes(ExternalAnnotationSource source, RDataType type) {
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
	
	public Set<RosettaExternalRegularAttribute> getAllExternalAttributesForType(ExternalAnnotationSource source, RDataType type) {
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
	
	public Map<RAttribute, RosettaRule> getAllRuleReferencesForType(Optional<? extends ExternalAnnotationSource> maybeSource, RDataType type) {
		CollectRuleVisitor.Default visitor = new CollectRuleVisitor.Default(objectFactory);
		
		collectAllRuleReferencesForType(maybeSource, type, visitor);
		
		return visitor.getMap();
	}
	
	public <T extends CollectRuleVisitor> T collectAllRuleReferencesForType(Optional<? extends ExternalAnnotationSource> maybeSource, RDataType type, T visitor) {
		// collect inline rule reference
		type.getAllAttributes()
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
	
	private void collectExternalRuleReferencesForType(ExternalAnnotationSource source, RDataType type, CollectRuleVisitor visitor) {
		getExternalType(source, type).ifPresent(extT -> {
			for (RosettaExternalRegularAttribute extAttr: extT.getRegularAttributes()) {
				if (extAttr.getOperator().equals(ExternalValueOperator.MINUS)) {
					visitor.remove(extAttr);
				} else {
					if (extAttr.getExternalRuleReference() != null) {
						visitor.add(extAttr, extAttr.getExternalRuleReference().getReportingRule());
					}
				}
			}
		});
	}
	
	public interface CollectRuleVisitor {
		
		void add(RAttribute attr, RosettaRule rule);
		void add(RosettaExternalRegularAttribute extAttr, RosettaRule rule);
		void remove(RosettaExternalRegularAttribute attr);
		
		public static class Default implements CollectRuleVisitor {
			private final RObjectFactory objectFactory;
			public Default(RObjectFactory objectFactory) {
				this.objectFactory = objectFactory;
			}

			private final Map<RAttribute, RosettaRule> map = new HashMap<>();
			
			@Override
			public void add(RAttribute attr, RosettaRule rule) {
				map.put(attr, rule);
			}
			
			@Override
			public void add(RosettaExternalRegularAttribute extAttr, RosettaRule rule) {
				map.put(objectFactory.buildRAttribute((Attribute) extAttr.getAttributeRef()), rule);
			}

			@Override
			public void remove(RosettaExternalRegularAttribute extAttr) {
				map.remove(objectFactory.buildRAttribute((Attribute) extAttr.getAttributeRef()));
			}
			
			public Map<RAttribute, RosettaRule> getMap() {
				return map;
			}
		}
	}
	
	/**
	 * Get all reporting rules for a report
	 */
	public Map<PathAttribute, RosettaRule> getAllReportingRules(RosettaReport report) {
		return getAllReportingRules(objectFactory.buildRDataType(report.getReportType()), Optional.ofNullable(report.getRuleSource()));
	}
	
	public Map<PathAttribute, RosettaRule> getAllReportingRules(RDataType type, Optional<RosettaExternalRuleSource> ruleSource) {
		Map<PathAttribute, RosettaRule> rules = new HashMap<>();
		RosettaPath path = RosettaPath.valueOf(type.getName());
		collectReportingRules(type, path, ruleSource, rules, new HashSet<>());
		return rules;
	}
	
	/**
	 * Recursively collects all reporting rules for all attributes
	 */
	private void collectReportingRules(RDataType dataType, RosettaPath path, Optional<RosettaExternalRuleSource> ruleSource, Map<PathAttribute, RosettaRule> visitor, Set<RDataType> collectedTypes) {
		Map<RAttribute, RosettaRule> attrRules = getAllRuleReferencesForType(ruleSource, dataType);
		
		dataType.getAllAttributes().forEach(attr -> {
			RType attrType = attr.getRMetaAnnotatedType().getRType();
			if (attrType instanceof RChoiceType) {
				attrType = ((RChoiceType) attrType).asRDataType();
			}
			RosettaRule rule = attrRules.get(attr);
			
			if (!(attrType instanceof RDataType)) {
				if (rule != null) {
					visitor.put(new PathAttribute(path, attr), rule);
				}
			} else {
				if (rule != null) {
					visitor.put(new PathAttribute(path, attr), rule);
				}
				RDataType attrDataType = (RDataType) attrType;
				if (collectedTypes.add(attrDataType)) {
					RosettaPath subPath = attr.isMulti() ?
						path.newSubPath(attr.getName(), 0) :
						path.newSubPath(attr.getName());
					collectReportingRules(attrDataType, subPath, ruleSource, visitor, collectedTypes);
				}
			}
		});	
	}
	
	public static class PathAttribute {
		private final RosettaPath path;
		private final RAttribute attr;
		
		public PathAttribute(RosettaPath path, RAttribute attr) {
			this.path = path;
			this.attr = attr;
		}

		public RosettaPath getPath() {
			return path;
		}

		public RAttribute getAttr() {
			return attr;
		}

		@Override
		public int hashCode() {
			return Objects.hash(attr, path);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PathAttribute other = (PathAttribute) obj;
			return Objects.equals(attr, other.attr) && Objects.equals(path, other.path);
		}
	}
}
