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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;

public interface JavaTypeArgument {
	public static JavaTypeArgument from(Type t, Map<TypeVariable<?>, JavaTypeVariable> context) {
		if (t instanceof Class<?>) {
			JavaType result = JavaType.from(t, context);
			if (result instanceof JavaReferenceType) {
				return (JavaReferenceType) result;
			}
		} else if (t instanceof WildcardType) {
			return JavaWildcardTypeArgument.from((WildcardType) t, context);
		} else if (t instanceof TypeVariable) {
			return JavaTypeVariable.from((TypeVariable<?>)t, context);
		}
		return null;
	}
	
	// See Java specification of the `contains` relationship.
	// https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.5.1
	public boolean contains(JavaTypeArgument other);
	
	public void accept(JavaTypeArgumentVisitor visitor);
}
