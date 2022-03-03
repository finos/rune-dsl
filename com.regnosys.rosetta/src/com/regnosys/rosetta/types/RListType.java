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
		return this.itemType.toString() + " " + this.constraint.toString();
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
	    if (!cardinalityEquals(this.constraint, other.constraint))
        	return false;
	    return true;
	}
	
	private static boolean cardinalityEquals(RosettaCardinality c1, RosettaCardinality c2) {
		if (c1 == null) {
			return c2 == null;
		}
		if (c2 == null) {
			return false;
		}
		if (c1.isUnbounded()) {
			return c1.getInf() == c2.getInf();
		}
		return c1.getInf() == c2.getInf() && c1.getSup() == c2.getSup();
	}
	
	@Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.itemType == null ? 0 : this.itemType.hashCode());
        hash = 53 * hash + cardinalityHashCode(this.constraint);
        return hash;
    }
	
	private static int cardinalityHashCode(RosettaCardinality c) {
		int hash = 3;
		hash = 53 * hash + Boolean.hashCode(c.isUnbounded());
		hash = 53 * hash + Integer.hashCode(c.getInf());
		if (!c.isUnbounded()) {
			hash = 53 * hash + Integer.hashCode(c.getSup());
		}
		return hash;
	}
}
