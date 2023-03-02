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