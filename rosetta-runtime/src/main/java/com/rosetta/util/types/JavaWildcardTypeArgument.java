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

package com.rosetta.util.types;

import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class JavaWildcardTypeArgument implements JavaTypeArgument {
	/*
	 * If extendsBound is `true`, represents `? extends T`
	 * If extendsBound is `false`, represents `? super T`
	 */
	private final boolean extendsBound;
	private final Optional<JavaReferenceType> bound;
	
	protected JavaWildcardTypeArgument() {
		this.extendsBound = false; // the value of `extendsBound` does not matter in this case.
		this.bound = Optional.empty();
	}
	protected JavaWildcardTypeArgument(boolean extendsBound, JavaReferenceType bound) {
		Objects.requireNonNull(bound);
		this.extendsBound = extendsBound;
		this.bound = Optional.of(bound);
	}
	
	public static JavaWildcardTypeArgument from(WildcardType a, Map<TypeVariable<?>, JavaTypeVariable> context) {
		if (a.getUpperBounds().length > 0) {
			JavaType bound = JavaType.from(a.getUpperBounds()[0], context);
			if (bound instanceof JavaReferenceType) {
				return extendsBound((JavaReferenceType)bound);
			}
		} else if (a.getLowerBounds().length > 0) {
			JavaType bound = JavaType.from(a.getLowerBounds()[0], context);
			if (bound instanceof JavaReferenceType) {
				return superBound((JavaReferenceType)bound);
			}
		} else {
			return unbounded();
		}
		return null;
	}
	
	public static JavaWildcardTypeArgument unbounded() {
		return new JavaWildcardTypeArgument();
	}
	public static JavaWildcardTypeArgument extendsBound(JavaReferenceType bound) {
		return new JavaWildcardTypeArgument(true, bound);
	}
	public static JavaWildcardTypeArgument superBound(JavaReferenceType bound) {
		return new JavaWildcardTypeArgument(false, bound);
	}
	
	public boolean isUnbounded() {
		return !bound.isPresent();
	}
	public boolean hasExtendsBound() {
		return bound.isPresent() && extendsBound;
	}
	public boolean hasSuperBound() {
		return bound.isPresent() && !extendsBound;
	}
	
	public Optional<JavaReferenceType> getBound() {
		return bound;
	}
	
	@Override
	public String toString() {
		StringBuilder target = new StringBuilder();
		target.append("?");
		if (hasExtendsBound()) {
			target.append(" extends ");
			target.append(bound.get());
		}
		if (hasSuperBound()) {
			target.append(" super ");
			target.append(bound.get());
		}
		return target.toString();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(extendsBound, bound);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;

        JavaWildcardTypeArgument other = (JavaWildcardTypeArgument) object;
        return extendsBound == other.extendsBound
        		&& Objects.equals(bound, other.bound);
	}
	@Override
	public void accept(JavaTypeArgumentVisitor visitor) {
		visitor.visitTypeArgument(this);
	}

	@Override
	public boolean contains(JavaTypeArgument other) {
		// See https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
		if (this.isUnbounded() || this.hasExtendsBound() && this.getBound().get().equals(JavaClass.OBJECT)) {
			return true;
		} else {
			JavaReferenceType bound = this.getBound().get();
			if (extendsBound) {
				if (other instanceof JavaReferenceType) {
					return ((JavaReferenceType)other).isSubtypeOf(bound);
				} else {
					JavaWildcardTypeArgument otherWildcard = (JavaWildcardTypeArgument)other;
					return otherWildcard.hasExtendsBound() && otherWildcard.getBound().get().isSubtypeOf(bound);
				}
			} else {
				if (other instanceof JavaReferenceType) {
					return bound.isSubtypeOf((JavaReferenceType)other);
				} else {
					JavaWildcardTypeArgument otherWildcard = (JavaWildcardTypeArgument)other;
					return otherWildcard.hasSuperBound() && bound.isSubtypeOf(otherWildcard.getBound().get());
				}
			}
		}
	}
}
