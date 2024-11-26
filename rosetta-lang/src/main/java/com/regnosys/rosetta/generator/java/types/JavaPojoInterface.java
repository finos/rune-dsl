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

import java.util.Collection;
import java.util.NoSuchElementException;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public abstract class JavaPojoInterface extends JavaClass<RosettaModelObject> {	
	public abstract String getJavadoc();
	public abstract String getRosettaName();
	public abstract String getVersion();
	
	public abstract Collection<JavaPojoProperty> getOwnProperties();
	public abstract Collection<JavaPojoProperty> getAllProperties();
	
	public abstract JavaPojoInterface getSuperPojo();
	
	public JavaPojoProperty findProperty(String propertyName, JavaType desiredType) {
		JavaPojoProperty prop = findProperty(propertyName);
		JavaPojoProperty currentProp = prop;
		while (currentProp != null) {
			if (desiredType.isSubtypeOf(currentProp.getType())) {
				return currentProp;
			}
			currentProp = currentProp.getParentProperty();
		}
		// Fallback: no compatible type found
		return prop;
	}
	public JavaPojoProperty findProperty(String propertyName) {
		return getAllProperties().stream().filter(prop -> prop.getName().equals(propertyName))
			.findAny()
			.orElseThrow(() -> new NoSuchElementException("No property named " + propertyName + " in pojo " + this));
	}

	@Override
	public JavaClass<? super RosettaModelObject> getSuperclassDeclaration() {
		return JavaClass.OBJECT;
	}
	
	@Override
	public JavaClass<? super RosettaModelObject> getSuperclass() {
		return getSuperclassDeclaration();
	}

	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		if (other instanceof JavaClass) {
			return this.isSubtypeOf((JavaClass<?>)other);
		}
		return false;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public Class<? extends RosettaModelObject> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		return Class.forName(getCanonicalName().toString(), true, classLoader).asSubclass(RosettaModelObject.class);
	}
}
