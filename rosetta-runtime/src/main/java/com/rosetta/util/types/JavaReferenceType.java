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

public interface JavaReferenceType extends JavaType, JavaTypeArgument {
	/*
	 * Representation of the null type. See Java specification: https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.1
	 */
	public static final JavaReferenceType NULL_TYPE = new JavaReferenceType() {
		@Override
		public String getSimpleName() {
			throw new UnsupportedOperationException("The null type has no name.");
		}

		@Override
		public void accept(JavaTypeVisitor visitor) {
			visitor.visitNullType();
		}

		@Override
		public boolean isSubtypeOf(JavaType other) {
			return other instanceof JavaReferenceType;
		}
		
		@Override
		public String toString() {
			return "NULL";
		}
	};
	
	@Override
	default JavaReferenceType toReferenceType() {
		return this;
	}
	@Override
	default void accept(JavaTypeArgumentVisitor visitor) {
		visitor.visitTypeArgument(this);
	}
	@Override
	default boolean contains(JavaTypeArgument other) {
		return this.equals(other);
	}
}
