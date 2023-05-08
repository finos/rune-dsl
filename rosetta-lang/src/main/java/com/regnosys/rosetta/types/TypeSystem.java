package com.regnosys.rosetta.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;

import com.google.inject.Inject;
import com.regnosys.rosetta.interpreter.RosettaInterpreterContext;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.typing.RosettaTyping;

public class TypeSystem {
	@Inject
	private RosettaTyping typing;
	@Inject
	private RBuiltinTypeService builtins;
	
	public RListType inferType(RosettaExpression expr) {
		Validate.notNull(expr);
		
		return typing.inferType(expr).getValue();
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
}
