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

package com.regnosys.rosetta.types;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.Validate;

import com.regnosys.rosetta.cache.IRequestScopedCache;
import com.regnosys.rosetta.interpreter.RosettaInterpreterContext;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.typing.RosettaTyping;
import com.regnosys.rosetta.utils.ExternalAnnotationUtil;

import org.eclipse.xtext.xbase.lib.Pair;

public class TypeSystem {
	public static String RULE_INPUT_TYPE_CACHE_KEY = TypeSystem.class.getCanonicalName() + ".RULE_INPUT_TYPE";
	
	@Inject
	private RosettaTyping typing;
	@Inject
	private RBuiltinTypeService builtins;
	@Inject
	private ExternalAnnotationUtil annotationUtil;
	@Inject
	private IRequestScopedCache cache;
	@Inject
	private SubtypeRelation subtypeRelation;
	
	public RListType inferType(RosettaExpression expr) {
		Objects.requireNonNull(expr);
		
		return typing.inferType(expr).getValue();
	}
	
	public RType getRulesInputType(RDataType data, Optional<RosettaExternalRuleSource> source) {
		return getRulesInputType(data, source, new HashSet<>());
	}
	private RType getRulesInputType(RDataType data, Optional<RosettaExternalRuleSource> source, Set<RDataType> visited) {
		Objects.requireNonNull(data);
        return getRulesInputTypeFromCache(data, source, () -> {
            if (!visited.add(data)) {
                return builtins.ANY;
            }

            Map<RAttribute, RosettaRule> ruleReferences = annotationUtil.getAllRuleReferencesForType(source, data);
            RType result = builtins.ANY;
            for (RAttribute attr: data.getOwnAttributes()) {
                RosettaRule rule = ruleReferences.get(attr);
                if (rule != null) {
                    RType inputType = typeCallToRType(rule.getInput());
                    result = meet(result, inputType);
                } else {
                    RType attrType = stripFromTypeAliases(attr.getRMetaAnnotatedType().getRType());
                    if (attrType instanceof RChoiceType) {
                    	attrType = ((RChoiceType) attrType).asRDataType();
                    }
                    if (attrType instanceof RDataType) {
                    	RDataType attrData = (RDataType)attrType;
                        RType inputType = getRulesInputType(attrData, source, visited);
                        result = meet(result, inputType);
                    }
                }
            }
            return result;
        });
	}
    private RType getRulesInputTypeFromCache(RDataType data, Optional<RosettaExternalRuleSource> source, Provider<RType> typeProvider) {
    	return cache.get(new Pair<>(RULE_INPUT_TYPE_CACHE_KEY, new Pair<>(data, source)), typeProvider);
    }
    
    public RMetaAnnotatedType join(RMetaAnnotatedType t1, RMetaAnnotatedType t2) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		
		return subtypeRelation.join(t1, t2);
    }
    
    public RMetaAnnotatedType joinMetaAnnotatedType(Iterable<RMetaAnnotatedType> types) {
		Objects.requireNonNull(types);
		Validate.noNullElements(types);
		
		RMetaAnnotatedType any = new RMetaAnnotatedType(builtins.ANY, List.of());
		RMetaAnnotatedType acc = new RMetaAnnotatedType(builtins.NOTHING, List.of());
		for (RMetaAnnotatedType t: types) {
			acc = subtypeRelation.join(acc, t);
			if (acc.equals(any)) {
				return acc;
			}
		}
		return acc;
    }

	public RType join(RType t1, RType t2) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		
		return subtypeRelation.join(t1, t2);
	}
	public RType join(Iterable<RType> types) {
		Objects.requireNonNull(types);
		Validate.noNullElements(types);
		
		RType acc = builtins.NOTHING;
		for (RType t: types) {
			acc = subtypeRelation.join(acc, t);
			if (acc.equals(builtins.ANY)) {
				return acc;
			}
		}
		return acc;
	}
	public RListType listJoin(RListType t1, RListType t2) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		
		return Objects.requireNonNull(typing.listJoin(t1, t2));
	}
	
	public RType meet(RType t1, RType t2) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		
		if (isSubtypeOf(t1, t2)) {
			return t1;
		} else if (isSubtypeOf(t2, t1)) {
			return t2;
		}
		return builtins.NOTHING;
	}
	public RType meet(Iterable<RType> types) {
		Objects.requireNonNull(types);
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
	
	public boolean isSubtypeOf(RMetaAnnotatedType sub, RMetaAnnotatedType sup) {
		return isSubtypeOf(sub, sup, true);
	}
	public boolean isSubtypeOf(RMetaAnnotatedType sub, RMetaAnnotatedType sup, boolean treatChoiceTypeAsData) {
		Objects.requireNonNull(sub);
		Objects.requireNonNull(sup);
		
		return subtypeRelation.isSubtypeOf(sub, sup, treatChoiceTypeAsData);
	}
	
	public boolean isSubtypeOf(RType sub, RType sup) {
		return isSubtypeOf(sub, sup, true);
	}
	public boolean isSubtypeOf(RType sub, RType sup, boolean treatChoiceTypeAsData) {
		Objects.requireNonNull(sub);
		Objects.requireNonNull(sup);
		
		return subtypeRelation.isSubtypeOf(sub, sup, treatChoiceTypeAsData);
	}
	public boolean isListSubtypeOf(RListType sub, RListType sup) {
		Objects.requireNonNull(sub);
		Objects.requireNonNull(sup);
		
		return typing.listSubtypeSucceeded(sub, sup);
	}
	
	public boolean isComparable(RType t1, RType t2) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		
		return typing.comparable(t1, t2);
	}
	public boolean isListComparable(RListType t1, RListType t2) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		
		return typing.listComparable(t1, t2);
	}
	
	public RType typeCallToRType(TypeCall typeCall) {
		return typeCallToRType(typeCall, new RosettaInterpreterContext());
	}
	
	public RType typeCallToRType(TypeCall typeCall, RosettaInterpreterContext context) {
		Objects.requireNonNull(typeCall);
		Objects.requireNonNull(context);
		
		return typing.typeCallToRType(typeCall, context);
	}
	
	public RType keepTypeAliasIfPossible(RType t1, RType t2, BiFunction<RType, RType, RType> combineUnderlyingTypes) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		Objects.requireNonNull(combineUnderlyingTypes);
		
		return typing.keepTypeAliasIfPossible(t1, t2, combineUnderlyingTypes);
	}
	
	public RType stripFromTypeAliases(RType t) {
		while (t instanceof RAliasType) {
			t = ((RAliasType)t).getRefersTo();
		}
		return t;
	}
}
