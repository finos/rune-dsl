package com.regnosys.rosetta.types;

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
	
	@Override
	public String toString() {
		return this.itemType.toString() + " " + constraint.toConstraintString();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
	      return true;
	    if (obj == null)
	      return false;
	    if (getClass() != obj.getClass())
	      return false;
	    final RListType other = (RListType) obj;
	    if (this.itemType == null ? other.itemType != null : !this.itemType.equals(other.itemType))
        	return false;
	    if (!this.constraint.constraintEquals(other.constraint))
        	return false;
	    return true;
	}
	
	@Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.itemType == null ? 0 : this.itemType.hashCode());
        hash = 53 * hash + this.constraint.constraintHashCode();
        return hash;
    }
}
