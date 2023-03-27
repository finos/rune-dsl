package com.regnosys.rosetta.utils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.ExternalAnnotationSource;
import com.regnosys.rosetta.rosetta.ExternalValueOperator;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.RosettaExternalEnum;
import com.regnosys.rosetta.rosetta.RosettaExternalEnumValue;
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource;
import com.regnosys.rosetta.rosetta.simple.Data;

public class ExternalAnnotationUtil {
	// @Compat. Can be removed once `RosettaSynonymSource` is gone.
	public Stream<ExternalAnnotationSource> getSuperSources(ExternalAnnotationSource source) {
		if (source instanceof RosettaExternalSynonymSource) {
			RosettaExternalSynonymSource synSource = (RosettaExternalSynonymSource)source;
			return synSource.getSuperSources().stream()
					.filter(s -> s instanceof ExternalAnnotationSource)
					.map(s -> (ExternalAnnotationSource)s);
		} else if (source instanceof RosettaExternalRuleSource) {
			RosettaExternalRuleSource ruleSource = (RosettaExternalRuleSource)source;
			return ruleSource.getSuperSources().stream();
		} else {
			throw new IllegalArgumentException();
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
		getSuperSources(source).map(s -> getAllExternalTypes(s, type))
			.forEach(s -> result.addAll(s));
		getExternalType(source, type).ifPresent(extT -> result.add(extT));
		return result;
	}
	
	public Set<RosettaExternalEnum> getAllExternalEnums(ExternalAnnotationSource source, RosettaEnumeration type) {
		Set<RosettaExternalEnum> result = new HashSet<RosettaExternalEnum>();
		getSuperSources(source).map(s -> getAllExternalEnums(s, type))
			.forEach(s -> result.addAll(s));
		getExternalEnum(source, type).ifPresent(extT -> result.add(extT));
		return result;
	}
	
	public Set<RosettaExternalRegularAttribute> getAllExternalAttributesForType(ExternalAnnotationSource source, Data type) {
		Set<RosettaExternalRegularAttribute> result = new HashSet<RosettaExternalRegularAttribute>();
		getSuperSources(source).map(s -> getAllExternalAttributesForType(s, type))
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
		getSuperSources(source).map(s -> getAllExternalEnumValuesForType(s, type))
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
}
