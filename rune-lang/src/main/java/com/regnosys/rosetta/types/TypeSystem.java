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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.commons.lang3.Validate;

import com.regnosys.rosetta.interpreter.RosettaInterpreter;
import com.regnosys.rosetta.interpreter.RosettaInterpreterContext;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.rosetta.RosettaBuiltinType;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaMetaType;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rules.RuleReferenceService;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.regnosys.rosetta.utils.RosettaSimpleSystemSolver;
import com.regnosys.rosetta.utils.RosettaSimpleSystemSolver.Equation;
import com.rosetta.model.lib.ModelSymbolId;

import com.regnosys.rosetta.rosetta.simple.Condition;

public class TypeSystem {
	@Inject
	private RObjectFactory factory;
	@Inject
	private RBuiltinTypeService builtins;
	@Inject
	private RuleReferenceService ruleService;
	@Inject
	private SubtypeRelation subtypeRelation;
	@Inject
	private RosettaInterpreter interpreter;
	@Inject
	private RosettaSimpleSystemSolver systemSolver;
	@Inject
	private ModelIdProvider modelIdProvider;

	public RType getRulesInputType(RDataType data, RosettaExternalRuleSource source) {
		Objects.requireNonNull(data);
    	return ruleService.<RType>traverse(
    			source,
    			data,
    			builtins.ANY,
    			(acc, context) -> {
    				if (context.isExplicitlyEmpty()) {
    					return acc;
    				}
    				RType ruleInputType = getRuleInputType(context.getRule());
    				if (builtins.NOTHING.equals(ruleInputType)) {
    					return acc;
    				}
    				return meet(acc, ruleInputType);
    			}
    		);
	}
    
    public RType getRuleInputType(RosettaRule rule) {
    	Objects.requireNonNull(rule);
    	TypeCall input = rule.getInput();
    	if (input == null) {
    		return builtins.NOTHING;
    	}
    	return typeCallToRType(input);
    }
    
    public RMetaAnnotatedType joinMetaAnnotatedTypes(RMetaAnnotatedType t1, RMetaAnnotatedType t2) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		
		return subtypeRelation.join(t1, t2);
    }
    
    public RMetaAnnotatedType joinMetaAnnotatedTypes(Iterable<RMetaAnnotatedType> types) {
		Objects.requireNonNull(types);
		Validate.noNullElements(types);
		
		RMetaAnnotatedType acc = builtins.NOTHING_WITH_ANY_META;
		for (RMetaAnnotatedType t: types) {
			acc = subtypeRelation.join(acc, t);
			if (acc.equals(builtins.ANY_WITH_NO_META)) {
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
	
	public boolean isComparable(RMetaAnnotatedType t1, RMetaAnnotatedType t2) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		
		return isSubtypeOf(t1, t2) || isSubtypeOf(t2, t1);
	}
	
	public RType typeCallToRType(TypeCall typeCall) {
		return typeCallToRType(typeCall, new RosettaInterpreterContext());
	}
	
	public RType typeCallToRType(TypeCall typeCall, RosettaInterpreterContext context) {
		Objects.requireNonNull(typeCall);
		Objects.requireNonNull(context);
		
		Map<String, RosettaValue> args = new LinkedHashMap<>();
		typeCall.getArguments().forEach(arg -> {
			RosettaValue eval = interpreter.interpret(arg.getValue(), context);
			args.put(arg.getParameter().getName(), eval);
		});
		
		return typeCallToRType(typeCall.getType(), args, context);
	}
	public RType typeWithUnknownArgumentsToRType(RosettaType type) {
		Objects.requireNonNull(type);
		
		return typeCallToRType(type, Collections.emptyMap(), new RosettaInterpreterContext());
	}
	private RType typeCallToRType(RosettaType type, Map<String, RosettaValue> arguments, RosettaInterpreterContext context) {
		if (type instanceof Choice) {
			return factory.buildRChoiceType((Choice) type);
		} else if (type instanceof Data) {
			return factory.buildRDataType((Data) type);
		} else if (type instanceof RosettaEnumeration) {
			return factory.buildREnumType((RosettaEnumeration) type);
		} else if (type instanceof RosettaBuiltinType) {
			return builtins.getType(type, arguments).orElse(builtins.NOTHING);
		} else if (type instanceof RosettaMetaType) {
			return builtins.getType(type, arguments)
					.orElseGet(() -> typeCallToRType(((RosettaMetaType) type).getTypeCall(), context));
		} else if (type instanceof RosettaTypeAlias) {
			RosettaTypeAlias alias = (RosettaTypeAlias) type;
			LinkedHashMap<String, RosettaValue> args = new LinkedHashMap<>(arguments);
			((RosettaTypeAlias) type).getParameters().forEach(param -> {
				if (!arguments.containsKey(param.getName())) {
					args.put(param.getName(), RosettaValue.empty());
				}
			});
			RType refersTo = typeCallToRType(alias.getTypeCall(), RosettaInterpreterContext.of(args));
			List<Condition> conditions = alias.getConditions();
			return new RAliasType(typeFunctionOfTypeAlias(alias), args, refersTo, conditions);
		}
		return builtins.NOTHING;
	}
	
	private RTypeFunction typeFunctionOfTypeAlias(RosettaTypeAlias typeAlias) {
		if (typeAlias.getName().equals(builtins.INT_NAME)) {
			return builtins.INT_FUNCTION;
		}
		ModelSymbolId symbolId = modelIdProvider.getSymbolId(typeAlias);
		List<Equation> equations = 
				typeAlias.getTypeCall().getArguments().stream().map(arg -> {
					RosettaSymbolReference ref = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
					ref.setGenerated(true);
					ref.setSymbol(arg.getParameter());
					return new Equation(ref, arg.getValue());
				}).collect(Collectors.toList());
		return systemSolver.solve(equations, new HashSet<>(typeAlias.getParameters())).<RTypeFunction>map(solutionSet ->
			new RTypeFunction(symbolId.getNamespace(), symbolId.getName()) {
				@Override
				public RType evaluate(Map<String, RosettaValue> arguments) {
					return typeCallToRType(typeAlias.getTypeCall(), RosettaInterpreterContext.of(arguments));
				}
				@Override
				public Optional<LinkedHashMap<String, RosettaValue>> reverse(RType type) {
					if (!(type instanceof RParametrizedType)) {
						return Optional.empty();
					}			
					RosettaInterpreterContext context = RosettaInterpreterContext.of(((RParametrizedType)type).getArguments());
					return solutionSet.getSolution(context).map(solution -> {
						LinkedHashMap<String, RosettaValue> newArgs = new LinkedHashMap<>();
						typeAlias.getParameters().forEach(p -> newArgs.put(p.getName(), solution.get(p)));
						return newArgs;
					});
				}
			}
		).orElseGet(() ->
			new RTypeFunction(symbolId.getNamespace(), symbolId.getName()) {
				@Override
				public RType evaluate(Map<String, RosettaValue> arguments) {
					return typeCallToRType(typeAlias.getTypeCall(), RosettaInterpreterContext.of(arguments));
				}
			}
		);
	}
	
	public RMetaAnnotatedType keepTypeAliasIfPossibleWithAnyMeta(RType t1, RType t2, BiFunction<RType, RType, RType> combineUnderlyingTypes) {
	    RType typeWithMaybeAlias = keepTypeAliasIfPossible(t1, t2, combineUnderlyingTypes);
	    
	    if (typeWithMaybeAlias.equals(builtins.NOTHING)) {
	        return builtins.NOTHING_WITH_ANY_META;
	    }
	    
	    return RMetaAnnotatedType.withNoMeta(typeWithMaybeAlias);
	}
	
	public RType keepTypeAliasIfPossible(RType t1, RType t2, BiFunction<RType, RType, RType> combineUnderlyingTypes) {
		Objects.requireNonNull(t1);
		Objects.requireNonNull(t2);
		Objects.requireNonNull(combineUnderlyingTypes);
		
		if (t1 instanceof RAliasType && t2 instanceof RAliasType) {
			RAliasType alias1 = (RAliasType) t1;
			RAliasType alias2 = (RAliasType) t2;
			if (alias1.getTypeFunction().equals(alias2.getTypeFunction())) {
				RTypeFunction typeFunc = alias1.getTypeFunction();
				RType underlier = keepTypeAliasIfPossible(alias1.getRefersTo(), alias2.getRefersTo(), combineUnderlyingTypes);
				//Condition intersection should be considered in the long term. Picked one set of conditions since are tightly coupled with typeFunctions
				return typeFunc.reverse(underlier)
					.<RType>map(args -> new RAliasType(typeFunc, args, underlier, alias1.getConditions()))
					.orElse(underlier);
			} else {
				List<RAliasType> superAliases = new ArrayList<>();
				RAliasType curr = alias1;
				superAliases.add(curr);
				while (curr.getRefersTo() instanceof RAliasType) {
					curr = (RAliasType) curr.getRefersTo();
					superAliases.add(curr);
				}
				curr = alias2;
				RTypeFunction tf1 = curr.getTypeFunction();
				Optional<RAliasType> match = superAliases.stream().filter(a -> tf1.equals(a.getTypeFunction())).findFirst();
				if (match.isPresent()) {
					return keepTypeAliasIfPossible(match.get(), curr, combineUnderlyingTypes);
				}
				while (curr.getRefersTo() instanceof RAliasType) {
					curr = (RAliasType) curr.getRefersTo();
					RTypeFunction tf2 = curr.getTypeFunction();
					match = superAliases.stream().filter(a -> tf2.equals(a.getTypeFunction())).findFirst();
					if (match.isPresent()) {
						return keepTypeAliasIfPossible(match.get(), curr, combineUnderlyingTypes);
					}
				}
				return keepTypeAliasIfPossible(alias1.getRefersTo(), alias2.getRefersTo(), combineUnderlyingTypes);
			}
		} else if (t1 instanceof RAliasType) {
			return keepTypeAliasIfPossible(((RAliasType) t1).getRefersTo(), t2, combineUnderlyingTypes);
		} else if (t2 instanceof RAliasType) {
			return keepTypeAliasIfPossible(t1, ((RAliasType) t2).getRefersTo(), combineUnderlyingTypes);
		}
		return combineUnderlyingTypes.apply(t1, t2);
	}

	public RType stripFromTypeAliases(RType t) {
		while (t instanceof RAliasType) {
			t = ((RAliasType)t).getRefersTo();
		}
		return t;
	}
	
	public AliasHierarchy computeAliasHierarchy(RType t) {
		List<RAliasType> aliasHierarchy = new ArrayList<>();
		RType underlyingType = t;
		while (underlyingType instanceof RAliasType) {
			RAliasType alias = (RAliasType)underlyingType;
			aliasHierarchy.add(alias);
			underlyingType = alias.getRefersTo();
		}
		return new AliasHierarchy(underlyingType, aliasHierarchy);
	}
}
