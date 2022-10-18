package com.regnosys.rosetta.types;

import com.google.inject.Inject;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.typing.RosettaTyping;

public class TypeSystem {
	@Inject
	RosettaTyping typing;
	
	public RListType inferType(RosettaExpression expr) {
		return typing.inferType(expr).getValue();
	}
	
	public boolean isSubtype(RType t1, RType t2) {
		return typing.subtypeSucceeded(t1, t2);
	}
	public boolean isListSubtype(RListType t1, RListType t2) {
		return typing.listSubtypeSucceeded(t1, t2);
	}
	
	public boolean isComparable(RType t1, RType t2) {
		return typing.comparable(t1, t2);
	}
	public boolean isListComparable(RListType t1, RListType t2) {
		return typing.listComparable(t1, t2);
	}
}
