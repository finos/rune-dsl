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

package com.rosetta.model.lib.meta;

import java.lang.reflect.InvocationTargetException;

import com.rosetta.model.lib.RosettaModelObject;

public class RosettaMetaDataBuilder {

	public static <T extends RosettaModelObject> RosettaMetaData<T> getMetaData(T t) {
		String metaClassName = getMetaClassName(t);
		try {
			return castToMeta(Class.forName(metaClassName, true, t.getClass().getClassLoader()).getConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Failed to instantiate RosettaMetaData class: " + metaClassName, e);
		}
	}

	private static <T extends RosettaModelObject> String getMetaClassName(T t) {
		Class<? extends RosettaModelObject> class1 = t.getType();
		String simpleName = class1.getSimpleName();
		String name = class1.getName();
		String packageName = name.substring(0, name.lastIndexOf(simpleName));
		return packageName + "meta." + simpleName + "Meta";
	}

	@SuppressWarnings("unchecked")
	private static <T extends RosettaModelObject> RosettaMetaData<T> castToMeta(Object object) {
		return (RosettaMetaData<T>) object;
	}	
}