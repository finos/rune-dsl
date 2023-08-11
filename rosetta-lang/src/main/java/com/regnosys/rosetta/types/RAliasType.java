package com.regnosys.rosetta.types;

import java.util.LinkedHashMap;
import java.util.Objects;

import com.regnosys.rosetta.interpreter.RosettaValue;
import com.rosetta.util.DottedPath;


public class RAliasType extends RParametrizedType {
	private final RTypeFunction typeFunction;
	private final RType refersTo;

	public RAliasType(RTypeFunction typeFunction, LinkedHashMap<String, RosettaValue> params, RType refersTo) {
		super(params);
		this.typeFunction = typeFunction;
		this.refersTo = refersTo;
	}

	@Override
	public String getName() {
		return typeFunction.getName();
	}
	
	@Override
	public DottedPath getNamespace() {
		return typeFunction.getNamespace();
	}
	
	public RTypeFunction getTypeFunction() {
		return typeFunction;
	}

	public RType getRefersTo() {
		return refersTo;
	}

	@Override
	public boolean hasMeta() {
	    return refersTo.hasMeta();
	}
	
	@Override
	public boolean hasNaturalOrder() {
	    return refersTo.hasNaturalOrder();
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeFunction, getArguments(), refersTo);
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RAliasType other = (RAliasType) object;
		return Objects.equals(typeFunction, other.typeFunction)
				&& Objects.equals(getArguments(), other.getArguments())
				&& Objects.equals(refersTo, other.refersTo);
	}
}
