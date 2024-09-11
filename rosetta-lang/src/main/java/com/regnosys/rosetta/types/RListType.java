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
