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

package com.regnosys.rosetta.generator.java.types;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import com.rosetta.util.types.BinaryCommunicativeJavaTypeVisitor;
import com.rosetta.util.types.JavaArrayType;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaGenericTypeDeclaration;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaReferenceType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeArgument;
import com.rosetta.util.types.JavaTypeDeclaration;
import com.rosetta.util.types.JavaTypeVariable;
import com.rosetta.util.types.JavaWildcardTypeArgument;

/**
 * A service that can compute the least common supertype of two given Java types.
 * 
 * Examples: given three classes A, B, C with B <: A.
 * visitTypes(A,             B)                          -> A
 * visitTypes(A,             C)                          -> Object
 * visitTypes(B,             B)                          -> B
 * visitTypes(List<A>,       List<B>)                    -> List<? extends A>
 * visitTypes(List<A>,       List<C>)                    -> List<?>
 * visitTypes(List<A>,       B)                          -> Object
 * visitTypes(boolean,       Boolean)                    -> Boolean
 * visitTypes(int,           float)                      -> float
 * visitTypes(Integer,       BigDecimal)                 -> Number
 * visitTypes(List<Integer>, List<? extends BigDecimal>) -> List<? extends Number>
 * 
 * Based on the subtype relation of the Java specification:
 * https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10
 */
public class JavaTypeJoiner extends BinaryCommunicativeJavaTypeVisitor<JavaType> {
	@Inject
	private JavaTypeUtil typeUtil;
	
	public JavaReferenceType visitTypes(JavaReferenceType left, JavaReferenceType right) {
		return (JavaReferenceType)visitTypes((JavaType)left, (JavaType)right);
	}
	
	// ***************
	// * ARRAY TYPES *
	// ***************
	// 
	// See https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.3
	
	@Override
	public JavaType visitTypes(JavaArrayType left, JavaArrayType right) {
		if (left.equals(right)) {
			return left;
		}
		JavaType leftBase = left.getBaseType();
		JavaType rightBase = right.getBaseType();
		if (leftBase instanceof JavaReferenceType && rightBase instanceof JavaReferenceType) {
			return new JavaArrayType(visitTypes(left.getBaseType(), right.getBaseType()));
		}
		return typeUtil.OBJECT;
	}
	@Override
	protected JavaClass<?> visitTypes(JavaArrayType left, JavaClass<?> right) {
		if (typeUtil.CLONEABLE.equals(right) || typeUtil.SERIALIZABLE.equals(right)) {
			return right;
		}
		return typeUtil.OBJECT;
	}
	@Override
	protected JavaType visitTypes(JavaArrayType left, JavaParameterizedType<?> right) {
		return typeUtil.OBJECT;
	}
	@Override
	protected JavaType visitTypes(JavaArrayType left, JavaPrimitiveType right) {
		return typeUtil.OBJECT;
	}
	@Override
	protected JavaType visitTypes(JavaArrayType left, JavaTypeVariable right) {
		return typeUtil.OBJECT;
	}
	@Override
	protected JavaArrayType visitTypeAndNull(JavaArrayType left) {
		return left;
	}
	
	// *************************
	// * CLASS/INTERFACE TYPES *
	// *************************
	// 
	// See https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.2
	// Note: the join of two Java classes/interfaces in general is ill-defined because of multi-inheritance with interfaces.
	// The implementation below gives priority to class inheritance, followed by interface inheritance.

	private JavaClass<?> joinClasses(JavaClass<?> left, JavaTypeDeclaration<?> leftDecl, JavaClass<?> right, JavaTypeDeclaration<?> rightDecl) {
		if (left.equals(right)) {
			return left;
		}
		JavaTypeDeclaration<?> joinedDeclaration = joinTypeDeclarations(leftDecl, rightDecl);
		if (joinedDeclaration instanceof JavaGenericTypeDeclaration<?>) {
			JavaGenericTypeDeclaration<?> genericSuperDeclaration = (JavaGenericTypeDeclaration<?>) joinedDeclaration;
			Map<JavaTypeVariable, JavaTypeArgument> subLeft = getSubstitution(genericSuperDeclaration, left);
			Map<JavaTypeVariable, JavaTypeArgument> subRight = getSubstitution(genericSuperDeclaration, right);
			
			return JavaParameterizedType.from(
						genericSuperDeclaration,
						genericSuperDeclaration.getParameters().stream()
							.map(p -> joinTypeArguments(subLeft.get(p), subRight.get(p)))
							.collect(Collectors.toList())
					);
		} else {
			return (JavaClass<?>)joinedDeclaration;
		}
	}
	private JavaTypeDeclaration<?> joinTypeDeclarations(JavaTypeDeclaration<?> left, JavaTypeDeclaration<?> right) {
		JavaTypeDeclaration<?> superType = left;
		// First check all superclasses
		while (!right.extendsDeclaration(superType)) {
			superType = superType.getSuperclassDeclaration();
		}
		if (superType.equals(typeUtil.OBJECT)) {
			// If not found, then check all interfaces
			superType = left;
			while (!superType.equals(typeUtil.OBJECT)) {
				JavaTypeDeclaration<?> interfaceJoin = left.getInterfaceDeclarations().stream()
						.<JavaTypeDeclaration<?>>map(i -> joinTypeDeclarations(i, right))
						.filter(d -> !d.equals(typeUtil.OBJECT))
						.findFirst()
						.orElse(typeUtil.OBJECT);
				if (interfaceJoin != typeUtil.OBJECT) {
					return interfaceJoin;
				}
				superType = superType.getSuperclassDeclaration();
			}
		}
		return superType;
	}
	private JavaTypeArgument joinTypeArguments(JavaTypeArgument left, JavaTypeArgument right) {
		// See https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
		if (left.equals(right)) {
			return left;
		}
		if (left == JavaReferenceType.NULL_TYPE) {
			return right;
		} else if (right == JavaReferenceType.NULL_TYPE) {
			return left;
		} else if (left instanceof JavaReferenceType && right instanceof JavaReferenceType) {
			JavaReferenceType join = visitTypes((JavaReferenceType)left, (JavaReferenceType)right);
			if (join.equals(typeUtil.OBJECT)) {
				return JavaWildcardTypeArgument.unbounded();
			}
			return JavaWildcardTypeArgument.extendsBound(join);
		} else if (left instanceof JavaReferenceType || right instanceof JavaReferenceType) {
			JavaReferenceType refType;
			JavaWildcardTypeArgument wildcardType;
			if (left instanceof JavaReferenceType) {
				refType = (JavaReferenceType) left;
				wildcardType = (JavaWildcardTypeArgument) right;
			} else {
				refType = (JavaReferenceType) right;
				wildcardType = (JavaWildcardTypeArgument) left;
			}
			if (wildcardType.isUnbounded()) {
				return wildcardType;
			} else {
				JavaReferenceType bound = wildcardType.getBound().orElseThrow();
				if (wildcardType.hasExtendsBound()) {
					JavaReferenceType join = visitTypes(refType, bound);
					if (join.equals(typeUtil.OBJECT)) {
						return JavaWildcardTypeArgument.unbounded();
					}
					return JavaWildcardTypeArgument.extendsBound(join);
				} else {
					if (bound.isSubtypeOf(refType)) {
						return bound;
					} else if (refType.isSubtypeOf(bound) && !refType.equals(typeUtil.OBJECT)) {
						return JavaWildcardTypeArgument.superBound(refType);
					} else {
						return JavaWildcardTypeArgument.unbounded();
					}
				}
			}
		} else {
			JavaWildcardTypeArgument leftWildcard = (JavaWildcardTypeArgument) left;
			JavaWildcardTypeArgument rightWildcard = (JavaWildcardTypeArgument) right;
			if (leftWildcard.isUnbounded()) {
				return leftWildcard;
			} else if (rightWildcard.isUnbounded()) {
				return rightWildcard;
			} else {
				JavaReferenceType leftBound = leftWildcard.getBound().orElseThrow();
				JavaReferenceType rightBound = rightWildcard.getBound().orElseThrow();
				if (leftWildcard.hasExtendsBound()) {
					if (rightWildcard.hasSuperBound()) {
						return JavaWildcardTypeArgument.unbounded();
					} else {
						JavaReferenceType join = visitTypes(leftBound, rightBound);
						if (join.equals(typeUtil.OBJECT)) {
							return JavaWildcardTypeArgument.unbounded();
						}
						return JavaWildcardTypeArgument.extendsBound(join);
					}
				} else {
					if (rightWildcard.hasExtendsBound()) {
						return JavaWildcardTypeArgument.unbounded();
					} else {
						if (leftBound.isSubtypeOf(rightBound)) {
							return leftWildcard;
						} else if (rightBound.isSubtypeOf(leftBound)) {
							return rightWildcard;
						} else {
							return JavaWildcardTypeArgument.unbounded();
						}
					}
				}
			}
		}
	}
	private Map<JavaTypeVariable, JavaTypeArgument> getSubstitution(JavaGenericTypeDeclaration<?> d, JavaClass<?> c) {
		JavaClass<?> currentSuper = c;
		JavaClass<?> nextSuper = currentSuper.getSuperclass();
		// First check superclasses
		while (nextSuper.extendsDeclaration(d)) {
			currentSuper = nextSuper;
			nextSuper = nextSuper.getSuperclass();
		}
		// Then check interfaces
		Optional<JavaClass<?>> nextInterface = currentSuper.getInterfaces().stream()
				.filter(i -> i.extendsDeclaration(d))
				.findAny();
		while (nextInterface.isPresent()) {
			currentSuper = nextInterface.orElseThrow();
			nextInterface = currentSuper.getInterfaces().stream()
					.filter(i -> i.extendsDeclaration(d))
					.findAny();
		}
		return ((JavaParameterizedType<?>)currentSuper).getTypeVariableSubstitution();
	}
	
	@Override
	protected JavaClass<?> visitTypes(JavaClass<?> left, JavaClass<?> right) {
		return joinClasses(left, left, right, right);
	}
	@Override
	protected JavaClass<?> visitTypes(JavaClass<?> left, JavaParameterizedType<?> right) {
		return joinClasses(left, left, right, right.getGenericTypeDeclaration());
	}
	@Override
	protected JavaType visitTypes(JavaClass<?> left, JavaPrimitiveType right) {
		return visitTypes(left, right.toReferenceType());
	}
	@Override
	protected JavaReferenceType visitTypes(JavaClass<?> left, JavaTypeVariable right) {
		return right.getBounds().stream()
				.map(b -> visitTypes(left, b))
				.filter(j -> !j.equals(typeUtil.OBJECT))
				.findAny()
				.orElse(typeUtil.OBJECT);
	}
	@Override
	protected JavaClass<?> visitTypeAndNull(JavaClass<?> left) {
		return left;
	}
	
	@Override
	protected JavaClass<?> visitTypes(JavaParameterizedType<?> left, JavaParameterizedType<?> right) {
		return joinClasses(left, left.getGenericTypeDeclaration(), right, right.getGenericTypeDeclaration());
	}
	@Override
	protected JavaType visitTypes(JavaParameterizedType<?> left, JavaPrimitiveType right) {
		return visitTypes(left, right.toReferenceType());
	}
	@Override
	protected JavaType visitTypes(JavaParameterizedType<?> left, JavaTypeVariable right) {
		return right.getBounds().stream()
				.map(b -> visitTypes(left, b))
				.filter(j -> !j.equals(typeUtil.OBJECT))
				.findAny()
				.orElse(typeUtil.OBJECT);
	}
	@Override
	protected JavaParameterizedType<?> visitTypeAndNull(JavaParameterizedType<?> left) {
		return left;
	}

	// *******************
	// * PRIMITIVE TYPES *
	// *******************
	// 
	// See https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.10.1
	
	@Override
	protected JavaType visitTypes(JavaPrimitiveType left, JavaPrimitiveType right) {
		if (left.isSubtypeOf(right)) {
			return right;
		} else if (right.isSubtypeOf(left)) {
			return left;
		} else if (left.equals(JavaPrimitiveType.CHAR) && right.isSubtypeOf(JavaPrimitiveType.INT) || right.equals(JavaPrimitiveType.CHAR) && left.isSubtypeOf(JavaPrimitiveType.INT)) {
			return JavaPrimitiveType.INT;
		}
		return typeUtil.OBJECT;
	}

	@Override
	protected JavaReferenceType visitTypes(JavaPrimitiveType left, JavaTypeVariable right) {
		return visitTypes(left.toReferenceType(), right);
	}

	@Override
	protected JavaClass<?> visitTypeAndNull(JavaPrimitiveType left) {
		return left.toReferenceType();
	}

	@Override
	protected JavaReferenceType visitTypes(JavaTypeVariable left, JavaTypeVariable right) {
		if (left.equals(right)) {
			return left;
		}
		return left.getBounds().stream()
				.flatMap(lb -> right.getBounds().stream().map(rb -> visitTypes(lb, rb)))
				.filter(j -> !j.equals(typeUtil.OBJECT))
				.findAny()
				.orElse(typeUtil.OBJECT);
	}

	@Override
	protected JavaTypeVariable visitTypeAndNull(JavaTypeVariable left) {
		return left;
	}

	@Override
	protected JavaReferenceType visitBothNull() {
		return JavaReferenceType.NULL_TYPE;
	}
}
