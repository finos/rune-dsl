package com.regnosys.rosetta.types;

import com.google.inject.Inject;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.typing.RosettaTyping;

public class TypeSystem {
	@Inject
	RosettaTyping typing;
	
	public RListType inferType(RosettaExpression expr) {
		return typing.inferType(expr).getValue();
	}

	public RType join(RType t1, RType t2) {
		return typing.join(t1, t2);
	}
	public RListType listJoin(RListType t1, RListType t2) {
		return typing.listJoin(t1, t2);
	}
	
	public boolean isSubtypeOf(RType sub, RType sup) {
		return typing.subtypeSucceeded(sub, sup);
	}
	public boolean isListSubtypeOf(RListType sub, RListType sup) {
		return typing.listSubtypeSucceeded(sub, sup);
	}
	
	public boolean isComparable(RType t1, RType t2) {
		return typing.comparable(t1, t2);
	}
	public boolean isListComparable(RListType t1, RListType t2) {
		return typing.listComparable(t1, t2);
	}
	
	public RType typeCallToRType(TypeCall typeCall) {
		return typing.typeCallToRType(typeCall);
	}
}
