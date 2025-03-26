package com.regnosys.rosetta.tests.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class ReflectiveInvoker<T> {
	private final Object invokedObject;
	private final Method method;
	private final Class<T> returnType;
	
	private ReflectiveInvoker(Object invokedObject, Method method, Class<T> returnType) {
		this.invokedObject = invokedObject;
		this.method = method;
		this.returnType = returnType;
	}
	
	public static <U> ReflectiveInvoker<U> from(Object invokedObject, String methodName, Class<U> returnType) {
		Method method = Arrays.stream(invokedObject.getClass().getMethods())
			.filter(m -> methodName.equals(m.getName()))
			.filter(m -> returnType.equals(m.getReturnType()))
			.findAny()
			.orElseThrow(() -> getMethodNotFoundException(invokedObject.getClass(), methodName, returnType));
		return new ReflectiveInvoker<>(invokedObject, method, returnType);
	}
	private static NoSuchElementException getMethodNotFoundException(Class<?> clazz, String methodName, Class<?> returnType) {
		String allMethods = Arrays.stream(clazz.getMethods())
			.map(m -> m.toGenericString())
			.collect(Collectors.joining("\n"));
		return new NoSuchElementException("No method found named " + methodName + " with return type " + returnType + ". Available methods:\n" + allMethods);
	}
	
	public T invoke(Object... args) {
		Object result;
		try {
			result = method.invoke(invokedObject, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return returnType.cast(result);
	}
}
