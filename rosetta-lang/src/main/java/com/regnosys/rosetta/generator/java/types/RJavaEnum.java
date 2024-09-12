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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.regnosys.rosetta.generator.java.enums.EnumHelper;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.types.REnumType;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class RJavaEnum extends JavaClass<Object> {	
	private final REnumType enumeration;
	
	private List<RJavaEnum> parents = null;
	private List<RJavaEnumValue> enumValues = null;

	public RJavaEnum(REnumType enumeration) {
		this.enumeration = enumeration;
	}
	
	public List<RJavaEnum> getParents() {
		if (parents == null) {
			parents = enumeration.getParents().stream().map(p -> new RJavaEnum(p)).collect(Collectors.toList());
		}
		return parents;
	}
	
	public List<RJavaEnumValue> getEnumValues() {
		if (enumValues == null) {
			enumValues = new ArrayList<>();
			Set<RosettaEnumValue> visited = new HashSet<>();
			for (RJavaEnum p : getParents()) {
				for (RJavaEnumValue v : p.getEnumValues()) {
					if (visited.add(v.getEObject())) {
						enumValues.add(new RJavaEnumValue(this, v.getName(), v.getEObject(), v));
					}
				}
			}
			for (RosettaEnumValue v : enumeration.getOwnEnumValues()) {
				enumValues.add(new RJavaEnumValue(this, EnumHelper.convertValue(v), v, null));
			}
		}
		return enumValues;
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (other instanceof JavaPrimitiveType) {
			return false;
		}
		if (other.equals(JavaClass.OBJECT)) {
			return true;
		}
		return false;
	}

	@Override
	public String getSimpleName() {
		return enumeration.getName();
	}

	@Override
	public JavaClass<? super Object> getSuperclassDeclaration() {
		return JavaClass.OBJECT;
	}
	
	@Override
	public JavaClass<? super Object> getSuperclass() {
		return getSuperclassDeclaration();
	}

	@Override
	public List<JavaClass<?>> getInterfaceDeclarations() {
		return Collections.emptyList();
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getInterfaceDeclarations();
	}

	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		return other.equals(JavaClass.OBJECT);
	}

	@Override
	public boolean isFinal() {
		return true;
	}

	@Override
	public Class<?> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return Class.forName(getCanonicalName().toString(), true, classLoader);
	}

	@Override
	public DottedPath getPackageName() {
		return enumeration.getNamespace();
	}

}
