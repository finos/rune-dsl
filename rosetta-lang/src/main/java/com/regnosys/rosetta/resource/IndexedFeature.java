package com.regnosys.rosetta.resource;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;

public interface IndexedFeature<T> {

	T getValue(IEObjectDescription desc);

	T getValue(EObject object);

	void index(EObject obj, Map<String, String> userData);

	abstract static public class IndexedSingleKeyFeature<T> implements IndexedFeature<T> {

		protected abstract String toString(T value);

		protected abstract T toValue(String string);

		protected abstract String getKey();

		@Override
		public T getValue(IEObjectDescription desc) {
			String string = desc.getUserData(getKey());
			if (string != null && !string.isEmpty()) {
				T value = toValue(string);
				return value;
			}
			return null;
		}

		@Override
		public void index(EObject obj, Map<String, String> userData) {
			T value = getValue(obj);
			if (value != null) {
				String converted = toString(value);
				userData.put(getKey(), converted);
			}
		}
	}

	abstract static public class IndexedStringFeature extends IndexedSingleKeyFeature<String> {

		@Override
		protected String toString(String value) {
			return value;
		}

		@Override
		protected String toValue(String string) {
			return string;
		}

	}

}
