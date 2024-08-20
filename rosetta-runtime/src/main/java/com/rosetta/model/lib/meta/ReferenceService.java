package com.rosetta.model.lib.meta;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.rosetta.model.lib.RosettaModelObject;

@Singleton
public class ReferenceService {
	private final Map<String, RosettaModelObject> keyToInstanceMap = new HashMap<>();
	
	public <T extends RosettaModelObject> T getInstance(String key, Class<T> clazz) {
		RosettaModelObject instance = keyToInstanceMap.get(key);
		if (instance == null) {
			return null;
		}
		return clazz.cast(instance);
	}
		
	public void register(RosettaModelObject instance, String key) {
		if (keyToInstanceMap.containsKey(key)) {
			throw new RuntimeException("There is already an instance registered with key '" + key + "'.");
		}
		keyToInstanceMap.put(key, instance);
	}
	
	public <T extends RosettaModelObject> T getInstanceOrProxy(String key, Class<T> clazz) {
		T instance = getInstance(key, clazz);
		if (instance != null) {
			return instance;
		}
		return createProxy(key, clazz);
	}
	
	private <T extends RosettaModelObject> T createProxy(String key, Class<T> clazz) {
		ProxyInvocationHandler<T> proxyHandler = new ProxyInvocationHandler<>(key, clazz);
		return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, proxyHandler));
	}
	
	private class ProxyInvocationHandler<T extends RosettaModelObject> implements InvocationHandler {
		private final String key;
		private final Class<T> clazz;
		private T instance = null;
		
		public ProxyInvocationHandler(String key, Class<T> clazz) {
			this.key = key;
			this.clazz = clazz;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			resolve();
			
			if (this.instance == null) {
				return null;
			}
			return method.invoke(instance, args);
		}
		
		private void resolve() {
			if (this.instance == null) {
				this.instance = getInstance(key, clazz);
			}
		}
	}
}
