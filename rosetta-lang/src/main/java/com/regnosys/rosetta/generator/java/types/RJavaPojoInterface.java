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

import java.util.Collections;
import java.util.List;

import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaPrimitiveType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public class RJavaPojoInterface extends JavaClass<RosettaModelObject> {
	private static JavaClass<RosettaModelObject> ROSETTA_MODEL_OBJECT = JavaClass.from(RosettaModelObject.class);
	
	private final RDataType data;
	
	private final ModelIdProvider modelIdProvider;
	private final TypeSystem typeSystem;

	public RJavaPojoInterface(RDataType data, ModelIdProvider modelIdProvider, TypeSystem typeSystem) {
		this.data = data;
		
		this.modelIdProvider = modelIdProvider;
		this.typeSystem = typeSystem;
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (other instanceof JavaPrimitiveType) {
			return false;
		}
		if (ROSETTA_MODEL_OBJECT.isSubtypeOf(other)) {
			return true;
		}
		if (other instanceof RJavaPojoInterface) {
			return typeSystem.isSubtypeOf(data, ((RJavaPojoInterface)other).data);
		}
		return false;
	}

	@Override
	public String getSimpleName() {
		return data.getName();
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
	public List<JavaClass<?>> getInterfaceDeclarations() {
		Data superType = data.getData().getSuperType();
		if (superType == null) {
			return List.of(ROSETTA_MODEL_OBJECT);
		}
		return Collections.singletonList(new RJavaPojoInterface(new RDataType(superType, modelIdProvider.getSymbolId(superType)), modelIdProvider, typeSystem));
	}
	
	@Override
	public List<JavaClass<?>> getInterfaces() {
		return getInterfaceDeclarations();
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

	@Override
	public DottedPath getPackageName() {
		return data.getNamespace();
	}

}
