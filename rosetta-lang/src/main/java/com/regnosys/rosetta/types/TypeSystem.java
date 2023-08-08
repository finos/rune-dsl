package com.regnosys.rosetta.types;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;

import com.google.inject.Inject;
import com.regnosys.rosetta.interpreter.RosettaInterpreterContext;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.RosettaRuleReference;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.typing.RosettaTyping;
import com.regnosys.rosetta.utils.ExternalAnnotationUtil;

public class TypeSystem {
	@Inject
	private RosettaTyping typing;
	@Inject
	private RBuiltinTypeService builtins;
	@Inject
	private ExternalAnnotationUtil annotationUtil;
	
	public RListType inferType(RosettaExpression expr) {
		Validate.notNull(expr);
		
		return typing.inferType(expr).getValue();
	}
	
	public RType getRulesInputType(Data data, Optional<RosettaExternalRuleSource> source) {
		return getRulesInputType(data, source, new HashSet<>());
	}
	private RType getRulesInputType(Data data, Optional<RosettaExternalRuleSource> source, Set<Data> visited) {
		Validate.notNull(data);
		if (!visited.add(data)) {
			return builtins.ANY;
		}
		
		Map<RosettaFeature, RosettaRuleReference> ruleReferences = annotationUtil.getAllRuleReferencesForType(source, data);
		RType result = builtins.ANY;
		for (Attribute attr: data.getAttributes()) {
			RosettaRuleReference ref = ruleReferences.get(attr);
			if (ref != null) {
				RType inputType = typeCallToRType(ref.getReportingRule().getInput());
				result = meet(result, inputType);
			} else {
				RType attrType = stripFromTypeAliases(typeCallToRType(attr.getTypeCall()));
				if (attrType instanceof RDataType) {
					Data attrData = ((RDataType)attrType).getData();
					RType inputType = getRulesInputType(attrData, source, visited);
					result = meet(result, inputType);
				}
			}
		}
		return result;
	}

	public RType join(RType t1, RType t2) {
		Validate.notNull(t1);
		Validate.notNull(t2);
		
		return Objects.requireNonNull(typing.join(t1, t2));
	}
	public RType join(Iterable<RType> types) {
		Validate.notNull(types);
		Validate.noNullElements(types);
		
		RType acc = builtins.NOTHING;
		for (RType t: types) {
			acc = join(acc, t);
			if (acc.equals(builtins.ANY)) {
				return acc;
			}
		}
		return acc;
	}
	public RListType listJoin(RListType t1, RListType t2) {
		Validate.notNull(t1);
		Validate.notNull(t2);
		
		return Objects.requireNonNull(typing.listJoin(t1, t2));
	}
	
	public RType meet(RType t1, RType t2) {
		Validate.notNull(t1);
		Validate.notNull(t2);
		
		if (isSubtypeOf(t1, t2)) {
			return t1;
		} else if (isSubtypeOf(t2, t1)) {
			return t2;
		}
		return builtins.NOTHING;
	}
	public RType meet(Iterable<RType> types) {
		Validate.notNull(types);
		Validate.noNullElements(types);
		
		RType acc = builtins.ANY;
		for (RType t: types) {
			acc = meet(acc, t);
			if (acc.equals(builtins.NOTHING)) {
				return acc;
			}
		}
		return acc;
	}
	
	public boolean isSubtypeOf(RType sub, RType sup) {
		Validate.notNull(sub);
		Validate.notNull(sup);
		
		return typing.subtypeSucceeded(sub, sup);
	}
	public boolean isListSubtypeOf(RListType sub, RListType sup) {
		Validate.notNull(sub);
		Validate.notNull(sup);
		
		return typing.listSubtypeSucceeded(sub, sup);
	}
	
	public boolean isComparable(RType t1, RType t2) {
		Validate.notNull(t1);
		Validate.notNull(t2);
		
		return typing.comparable(t1, t2);
	}
	public boolean isListComparable(RListType t1, RListType t2) {
		Validate.notNull(t1);
		Validate.notNull(t2);
		
		return typing.listComparable(t1, t2);
	}
	
	public RType typeCallToRType(TypeCall typeCall) {
		return typeCallToRType(typeCall, new RosettaInterpreterContext());
	}
	
	public RType typeCallToRType(TypeCall typeCall, RosettaInterpreterContext context) {
		Validate.notNull(typeCall);
		Validate.notNull(context);
		
		return typing.typeCallToRType(typeCall, context);
	}
	
	public RType keepTypeAliasIfPossible(RType t1, RType t2, BiFunction<RType, RType, RType> combineUnderlyingTypes) {
		Validate.notNull(t1);
		Validate.notNull(t2);
		Validate.notNull(combineUnderlyingTypes);
		
		return typing.keepTypeAliasIfPossible(t1, t2, combineUnderlyingTypes);
	}
	
	public RType stripFromTypeAliases(RType t) {
		if (t instanceof RAliasType) {
			return ((RAliasType)t).getRefersTo();
		}
		return t;
	}
}
