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
		return getProxy(key, clazz);
	}
	public <T extends RosettaModelObject> T getProxy(String key, Class<T> clazz) {
		RosettaModelObject proxy = keyToProxyMap.get(key);
		if (proxy == null) {
			ProxyInvocationHandler<T> proxyHandler = new ProxyInvocationHandler<>(key, clazz);
			proxy = (RosettaModelObject) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz, RosettaProxy.class }, proxyHandler);
			keyToProxyMap.put(key, proxy);
		}
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
		private T instance = null;
		
		public ProxyInvocationHandler(String key, Class<T> clazz) {
			this.key = key;
			this.clazz = clazz;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("getKey")) {
				return key;
			}
			if (method.getName().equals("equals") && args.length == 1) {
				if (proxy == args[0]) {
					return true;
				}
			}
			if (method.getName().equals("getType")) {
				return clazz;
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
