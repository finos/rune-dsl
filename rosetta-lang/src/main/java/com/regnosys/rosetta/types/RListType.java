package com.regnosys.rosetta.types;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.RosettaCardinality;

public class RListType {
	private final RType itemType;
	private final RosettaCardinality constraint;
	
	public RListType(RType itemType, RosettaCardinality constraint) {
		this.itemType = itemType;
		this.constraint = constraint;
	}
	
	public RType getItemType() {
		return this.itemType;
	}
	public RosettaCardinality getConstraint() {
		return this.constraint;
	}
	
	public boolean isEmpty() {
		return this.constraint.isEmpty();
	}
	public boolean isOptional() {
		return this.constraint.isOptional();
	}
	public boolean isSingular() {
		return this.constraint.isSingular();
	}
	public boolean isPlural() {
		return this.constraint.isPlural();
	}
	
	@Override
	public String toString() {
		return this.itemType.toString() + " " + constraint.toConstraintString();
	}
	
	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
	    final RListType other = (RListType) object;
	    return Objects.equals(itemType, other.itemType)
	    		&& this.constraint.constraintEquals(other.constraint);
	}
	
	@Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.itemType == null ? 0 : this.itemType.hashCode());
        hash = 53 * hash + this.constraint.constraintHashCode();
        return hash;
    }
}
