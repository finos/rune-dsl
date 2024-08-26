package com.rosetta.model.lib.meta;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.rosetta.model.lib.RosettaModelObject;

@Singleton
public class ReferenceService {
	private final Map<String, RosettaModelObject> keyToInstanceMap = new HashMap<>();
	private final Map<String, RosettaModelObject> keyToProxyMap = new HashMap<>();

	public <T extends RosettaModelObject> T register(T instance, String key, Class<T> clazz) {
		if (keyToInstanceMap.containsKey(key)) {
			throw new RuntimeException("There is already an instance registered with key '" + key + "'.");
		}
		keyToInstanceMap.put(key, instance);
		return createProxy(key, clazz, instance);
	}
	public <T extends RosettaModelObject> T getProxy(String key, Class<T> clazz) {
		RosettaModelObject proxy = keyToProxyMap.get(key);
		if (proxy == null) {
			T newProxy = createProxy(key, clazz, null);
			keyToProxyMap.put(key, newProxy);
			return newProxy;
		} else {
			return clazz.cast(proxy);
		}
	}
	private <T extends RosettaModelObject> T createProxy(String key, Class<T> clazz, T instance) {
		ProxyInvocationHandler<T> proxyHandler = new ProxyInvocationHandler<>(key, clazz, instance);
		Class<?> proxyClass = instance == null ? RosettaProxy.class : RosettaOriginalProxy.class;
		Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz, proxyClass }, proxyHandler);
		return clazz.cast(proxy);
	}
	
	private <T extends RosettaModelObject> T getInstance(String key, Class<T> clazz) {
		Object instance = keyToInstanceMap.get(key);
		if (instance == null) {
			return null;
		}
		return clazz.cast(instance);
	}
	
	public Map<String, RosettaModelObject> getGlobalScope() {
		return Collections.unmodifiableMap(keyToInstanceMap);
	}

	private class ProxyInvocationHandler<T extends RosettaModelObject> implements InvocationHandler {
		private final String key;
		private final Class<T> clazz;
		private T instance;
		
		public ProxyInvocationHandler(String key, Class<T> clazz, T initialInstance) {
			this.key = key;
			this.clazz = clazz;
			this.instance = initialInstance;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("getKey")) {
				return key;
			}
			if (method.getName().equals("getType")) {
				return clazz;
			}
			if (method.getName().equals("equals") && args.length == 1) {
				if (proxy == args[0]) {
					return true;
				}
			}
			
			resolve();
			if (this.instance == null) {
				return null;
			}
			
			if (method.getName().equals("getInstance")) {
				return instance;
			}
			if (method.getName().equals("equals") && args.length == 1 && args[0] instanceof RosettaProxy) {
				return instance.equals(((RosettaProxy<?>) args[0]).getInstance());
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
