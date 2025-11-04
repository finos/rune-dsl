package com.regnosys.rosetta.tests.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class ReflectiveInvoker<T> {
	private final Object invokedObject;
	private final List<Method> methods;
	private final Class<T> returnType;
	
	private ReflectiveInvoker(Object invokedObject, List<Method> methods, Class<T> returnType) {
		this.invokedObject = invokedObject;
		this.methods = methods;
		this.returnType = returnType;
	}
	
	public static <U> ReflectiveInvoker<U> from(Object invokedObject, String methodName, Class<U> returnType) {
		List<Method> methods = Arrays.stream(invokedObject.getClass().getMethods())
			.filter(m -> methodName.equals(m.getName()))
			.filter(m -> returnType.equals(m.getReturnType()))
			.toList();
		if (methods.isEmpty()) {
			throw getMethodNotFoundException(invokedObject.getClass(), methodName, returnType);
		}
		return new ReflectiveInvoker<>(invokedObject, methods, returnType);
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
			result = findMatchingMethod(args).invoke(invokedObject, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return returnType.cast(result);
	}
	
	private Method findMatchingMethod(Object... args) {
		List<Method> matching = methods.stream().filter(m -> m.getParameterCount() == args.length).toList();
		if (matching.isEmpty()) {
			throw getMethodNotFoundException(invokedObject.getClass(), methods.get(0).getName(), returnType);
		}
		matching = matching.stream().filter(m -> {
			Class<?>[] parameterTypes = m.getParameterTypes();
			for (int i=0; i<args.length; i++) {
				Object arg = args[i];
				Class<?> paramType = parameterTypes[i];
				if (arg == null) {
					if (paramType.isPrimitive()) {
						return false;
					}
					continue;
				}
				Class<?> argType = arg.getClass();
				if (!paramType.isAssignableFrom(argType)) {
					return false;
				}
			}
			return true;
		}).toList();
		if (matching.isEmpty()) {
			throw getMethodNotFoundException(invokedObject.getClass(), methods.get(0).getName(), returnType);
		}
		return matching.get(0);
	}
}
