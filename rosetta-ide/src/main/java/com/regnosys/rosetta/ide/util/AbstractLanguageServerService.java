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

package com.regnosys.rosetta.ide.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.SimpleCache;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: contribute to Xtext.
 *
 */
public class AbstractLanguageServerService<T> {
	private static final Logger log = LoggerFactory.getLogger(AbstractLanguageServerService.class);
	
	private final Class<T> resultType;
	private final Class<? extends Annotation> methodAnnotation;
	
	private final ThreadLocal<State> state;

	private volatile Set<MethodWrapper<T>> providerMethods = null;

	private final SimpleCache<Class<?>, List<MethodWrapper<T>>> methodsForType = new SimpleCache<>(
		param -> {
			List<MethodWrapper<T>> result = new ArrayList<>();
			for (MethodWrapper<T> mw : providerMethods) {
				if (mw.isMatching(param))
					result.add(mw);
			}
			return result;
		});
	
	@Inject
	private Injector injector;
	@Inject
	protected RangeUtils rangeUtils;
	
	protected AbstractLanguageServerService(Class<T> resultType, Class<? extends Annotation> methodAnnotation) {
		this.resultType = resultType;
		this.methodAnnotation = methodAnnotation;
		this.state = new ThreadLocal<>();
	}
	
	protected List<T> computeResult(Document document, XtextResource resource, CancelIndicator cancelIndicator) {
		TreeIterator<EObject> contents = resource.getAllContents();
		return computeResult(document, resource, contents, cancelIndicator);
	}
	protected List<T> computeResult(Document document, XtextResource resource, Range range, CancelIndicator cancelIndicator) {
		TreeIterator<EObject> contents = rangeUtils.iterateOverlapping(resource, range);
		return computeResult(document, resource, contents, cancelIndicator);
	}
	@SuppressWarnings("unchecked")
	protected List<T> computeResult(Document document, XtextResource resource, Iterator<EObject> objects, CancelIndicator cancelIndicator) {
		if (providerMethods == null) {
			synchronized (this) {
				if (providerMethods == null) {
					Set<MethodWrapper<T>> providerMethods = Sets.newLinkedHashSet();
					providerMethods.addAll(collectMethods((Class<? extends AbstractLanguageServerService<T>>)getClass()));
					this.providerMethods = providerMethods;
				}
			}
		}
		List<T> result = Lists.newArrayList();
		while (objects.hasNext()) {
			EObject obj = objects.next();
			
			if (cancelIndicator.isCanceled()) return result;
			
			for (MethodWrapper<T> m: methodsForType.get(obj.getClass())) {
				if (cancelIndicator.isCanceled()) return result;
				
				result.addAll(
						m.invoke(new State(obj, m.getMethod(), document, resource, cancelIndicator)));
			}
		}
		return result;
	}
	
	private List<MethodWrapper<T>> collectMethods(Class<? extends AbstractLanguageServerService<T>> clazz) {
		List<MethodWrapper<T>> providerMethods = new ArrayList<>();
		Set<Class<?>> visitedClasses = new HashSet<>(4);
		collectMethods(this, clazz, visitedClasses, providerMethods);
		return providerMethods;
	}

	private void collectMethods(AbstractLanguageServerService<T> instance,
								Class<? extends AbstractLanguageServerService<T>> clazz,
								Collection<Class<?>> visitedClasses,
								Collection<MethodWrapper<T>> result) {
		if (visitedClasses.contains(clazz))
			return;

		collectMethodsImpl(instance, clazz, visitedClasses, result);
	}

	private void collectMethodsImpl(AbstractLanguageServerService<T> instance,
									Class<? extends AbstractLanguageServerService<T>> clazz, Collection<Class<?>> visitedClasses,
									Collection<MethodWrapper<T>> result) {
		if (!visitedClasses.add(clazz))
			return;
		AbstractLanguageServerService<T> instanceToUse;
		instanceToUse = instance;
		if (instanceToUse == null) {
			instanceToUse = newInstance(clazz);
		}
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (method.getAnnotation(methodAnnotation) != null && method.getParameterTypes().length == 1) {
				result.add(createMethodWrapper(instanceToUse, method));
			}
		}
		Class<? extends AbstractLanguageServerService<T>> superClass = getSuperClass(clazz);
		if (superClass != null)
			collectMethodsImpl(instanceToUse, superClass, visitedClasses, result);
	}

	protected MethodWrapper<T> createMethodWrapper(AbstractLanguageServerService<T> instanceToUse, Method method) {
		return new MethodWrapper<T>(instanceToUse, method);
	}

	protected AbstractLanguageServerService<T> newInstance(Class<? extends AbstractLanguageServerService<T>> clazz) {
		AbstractLanguageServerService<T> instanceToUse;
		if (injector == null)
			throw new IllegalStateException("The class is not configured with an injector.");
		instanceToUse = injector.getInstance(clazz);
		return instanceToUse;
	}


	@SuppressWarnings("unchecked")
	private Class<? extends AbstractLanguageServerService<T>> getSuperClass(
		Class<? extends AbstractLanguageServerService<T>> clazz) {
		try {
			Class<?> superClass = clazz.getSuperclass().asSubclass(
					AbstractLanguageServerService.class);
			if (AbstractLanguageServerService.class.equals(superClass))
				return null;
			return (Class<? extends AbstractLanguageServerService<T>>) superClass;
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	private static class MethodWrapper<Result> {
		private final AbstractLanguageServerService<Result> instance;
		private final Method method;
		private final String s;

		protected MethodWrapper(AbstractLanguageServerService<Result> instance, Method m) {
			this.instance = instance;
			this.method = m;
			this.s = m.getName() + ":" + m.getParameterTypes()[0].getName();
		}

		public boolean isMatching(Class<?> param) {
			return method.getParameterTypes()[0].isAssignableFrom(param);
		}

		public List<Result> invoke(State state) {
			State instanceState = getInstance().state.get();
			if (instanceState != null && instanceState != state)
				throw new IllegalStateException("State is already assigned.");
			boolean wasNull = instanceState == null;
			if (wasNull)
				getInstance().state.set(state);
			try {
				try {
					method.setAccessible(true);
					if (state.cancelIndicator.isCanceled()) {
						return List.of();
					}
					Object res = method.invoke(getInstance(), state.currentObject);
					if (res instanceof Optional) {
						res = ((Optional<?>)res).orElse(null);
					}
					if (res == null) {
						return List.of();
					}
					if (res instanceof List<?>) {
						return ((List<?>) res).stream()
								.filter(r -> {
									if (getInstance().resultType.isInstance(r)) {
										return true;
									} else {
										log.error("Incorrect return type for method " + method);
										return false;
									}
								})
								.map(getInstance().resultType::cast)
							.collect(Collectors.toList());
					} else if (getInstance().resultType.isInstance(res)) {
						return List.of(getInstance().resultType.cast(res));
					} else {
						log.error("Incorrect return type for method " + method);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error(e.getMessage(), e);
				} catch (InvocationTargetException e) {
					Throwable targetException = e.getTargetException();
					log.error(e.getMessage(), targetException);
				}
			} finally {
				if (wasNull)
					getInstance().state.remove();
			}
			return List.of();
		}
		

		@Override
		public int hashCode() {
			return s.hashCode() ^ getInstance().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof AbstractLanguageServerService.MethodWrapper))
				return false;
			MethodWrapper<?> mw = (MethodWrapper<?>)obj;
			return s.equals(mw.s) && getInstance() == mw.getInstance();
		}

		public Method getMethod() {
			return method;
		}
		
		public AbstractLanguageServerService<Result> getInstance() {
			return this.instance;
		}
	}

	private static class State {
	
		private final EObject currentObject;
		private final Method currentMethod;
		private final Document currentDocument;
		private final XtextResource currentResource;
		private final CancelIndicator cancelIndicator;
		
		private State(EObject currentObject, Method currentMethod, Document currentDocument,
				XtextResource currentResource, CancelIndicator cancelIndicator) {
			super();
			this.currentObject = currentObject;
			this.currentMethod = currentMethod;
			this.currentDocument = currentDocument;
			this.currentResource = currentResource;
			this.cancelIndicator = cancelIndicator;
		}

		@Override
		public int hashCode() {
			return Objects.hash(cancelIndicator, currentDocument, currentMethod, currentObject,
					currentResource);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			State other = (State) obj;
			return Objects.equals(cancelIndicator, other.cancelIndicator)
					&& Objects.equals(currentDocument, other.currentDocument)
					&& Objects.equals(currentMethod, other.currentMethod)
					&& Objects.equals(currentObject, other.currentObject)
					&& Objects.equals(currentResource, other.currentResource);
		}
		 
	}
}
