package com.regnosys.rosetta.ide.inlayhints;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.regnosys.rosetta.ide.util.RangeUtils;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.SimpleCache;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: contribute to Xtext.
 *
 */
public abstract class AbstractInlayHintsService implements IInlayHintsService, IInlayHintsResolver {

	private static final Logger log = Logger.getLogger(AbstractInlayHintsService.class);

	@Inject
	private RangeUtils rangeUtils;

	@Inject
	private Injector injector;

	private final ThreadLocal<State> state;

	private volatile Set<MethodWrapper> checkMethods = null;

	private final SimpleCache<Class<?>, List<MethodWrapper>> methodsForType = new SimpleCache<>(
		param -> {
			List<MethodWrapper> result = new ArrayList<>();
			for (MethodWrapper mw : checkMethods) {
				if (mw.isMatching(param))
					result.add(mw);
			}
			return result;
		});

	public AbstractInlayHintsService() {
		this.state = new ThreadLocal<>();
	}

	@Override
	public List<InlayHint> computeInlayHint(Document document, XtextResource resource, InlayHintParams params, CancelIndicator cancelIndicator) {
		Range range = params.getRange();
		
		if (checkMethods == null) {
			synchronized (this) {
				if (checkMethods == null) {
					Set<MethodWrapper> checkMethods = Sets.newLinkedHashSet();
					checkMethods.addAll(collectMethods(getClass()));
					this.checkMethods = checkMethods;
				}
			}
		}
		
		List<InlayHint> result = Lists.newArrayList();
		TreeIterator<EObject> contents = resource.getAllContents();
		while (contents.hasNext()) {
			EObject obj = contents.next();
			
			if (cancelIndicator.isCanceled()) return null;
			
			if (rangeUtils.overlap(range, obj)) {
				for (MethodWrapper m: methodsForType.get(obj.getClass())) {
					if (cancelIndicator.isCanceled()) return null;
					
					result.addAll(
							m.invoke(new State(obj, m.getMethod(), document, resource, params, cancelIndicator)));
				}
			}
		}
		return result;
	}

	protected InlayHint createInlayHint(EObject hintObject, String label, String tooltip) {
		Position start = rangeUtils.getRange(hintObject).getStart();
		InlayHint inlayHint = new InlayHint();
		inlayHint.setPosition(start);
		inlayHint.setLabel(label);
		inlayHint.setTooltip(tooltip);
		inlayHint.setPaddingLeft(true);
		inlayHint.setPaddingRight(true);
		return inlayHint;
	}

	@Override
	public InlayHint resolveInlayHint(Document document, XtextResource resource, InlayHint unresolved, CancelIndicator cancelIndicator) {
		return unresolved;
	}

	protected EObject getCurrentObject() {
		return state.get().currentObject;
	}

	protected Document getCurrentDocument() {
		return state.get().currentDocument;
	}

	protected XtextResource getCurrentResource() {
		return state.get().currentResource;
	}

	protected InlayHintParams getCurrentParams() {
		return state.get().currentParams;
	}

	protected CancelIndicator getCancelIndicator() {
		return state.get().cancelIndicator;
	}

	private List<MethodWrapper> collectMethods(Class<? extends AbstractInlayHintsService> clazz) {
		List<MethodWrapper> checkMethods = new ArrayList<>();
		Set<Class<?>> visitedClasses = new HashSet<>(4);
		collectMethods(this, clazz, visitedClasses, checkMethods);
		return checkMethods;
	}

	private void collectMethods(AbstractInlayHintsService instance,
								Class<? extends AbstractInlayHintsService> clazz, Collection<Class<?>> visitedClasses,
								Collection<MethodWrapper> result) {
		if (visitedClasses.contains(clazz))
			return;

		collectMethodsImpl(instance, clazz, visitedClasses, result);
	}

	private void collectMethodsImpl(AbstractInlayHintsService instance,
									Class<? extends AbstractInlayHintsService> clazz, Collection<Class<?>> visitedClasses,
									Collection<MethodWrapper> result) {
		if (!visitedClasses.add(clazz))
			return;
		AbstractInlayHintsService instanceToUse;
		instanceToUse = instance;
		if (instanceToUse == null) {
			instanceToUse = newInstance(clazz);
		}
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (method.getAnnotation(InlayHintCheck.class) != null && method.getParameterTypes().length == 1) {
				result.add(createMethodWrapper(instanceToUse, method));
			}
		}
		Class<? extends AbstractInlayHintsService> superClass = getSuperClass(clazz);
		if (superClass != null)
			collectMethodsImpl(instanceToUse, superClass, visitedClasses, result);
	}

	protected MethodWrapper createMethodWrapper(AbstractInlayHintsService instanceToUse, Method method) {
		return new MethodWrapper(instanceToUse, method);
	}

	protected AbstractInlayHintsService newInstance(Class<? extends AbstractInlayHintsService> clazz) {
		AbstractInlayHintsService instanceToUse;
		if (injector == null)
			throw new IllegalStateException("The class is not configured with an injector.");
		instanceToUse = injector.getInstance(clazz);
		return instanceToUse;
	}


	private Class<? extends AbstractInlayHintsService> getSuperClass(
		Class<? extends AbstractInlayHintsService> clazz) {
		try {
			Class<? extends AbstractInlayHintsService> superClass = clazz.getSuperclass().asSubclass(
				AbstractInlayHintsService.class);
			if (AbstractInlayHintsService.class.equals(superClass))
				return null;
			return superClass;
		} catch (ClassCastException e) {
			return null;
		}
	}

	static class MethodWrapper {
		private final Method method;
		private final String s;
		private final AbstractInlayHintsService instance;

		protected MethodWrapper(AbstractInlayHintsService instance, Method m) {
			this.instance = instance;
			this.method = m;
			this.s = m.getName() + ":" + m.getParameterTypes()[0].getName();
		}

		@Override
		public int hashCode() {
			return s.hashCode() ^ instance.hashCode();
		}

		public boolean isMatching(Class<?> param) {
			return method.getParameterTypes()[0].isAssignableFrom(param);
		}

		public List<InlayHint> invoke(State state) {
			State instanceState = instance.state.get();
			if (instanceState != null && instanceState != state)
				throw new IllegalStateException("State is already assigned.");
			boolean wasNull = instanceState == null;
			if (wasNull)
				instance.state.set(state);
			try {
				try {
					method.setAccessible(true);
					if (state.cancelIndicator.isCanceled()) {
						return List.of();
					}
					Object res = method.invoke(instance, state.currentObject);
					if (res == null) {
						return List.of();
					}
					if (res instanceof List<?>) {
						return ((List<?>) res).stream().filter(InlayHint.class::isInstance).map(InlayHint.class::cast)
							.collect(Collectors.toList());
					} else if (res instanceof InlayHint) {
						return List.of((InlayHint) res);
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
					instance.state.remove();
			}
			return List.of();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MethodWrapper))
				return false;
			MethodWrapper mw = (MethodWrapper) obj;
			return s.equals(mw.s) && instance == mw.instance;
		}

		public AbstractInlayHintsService getInstance() {
			return instance;
		}

		public Method getMethod() {
			return method;
		}
	}

	private static class State {
	
		private final EObject currentObject;
		private final Method currentMethod;
		private final Document currentDocument;
		private final XtextResource currentResource;
		private final InlayHintParams currentParams;
		private final CancelIndicator cancelIndicator;
		
		private State(EObject currentObject, Method currentMethod, Document currentDocument,
				XtextResource currentResource, InlayHintParams currentParams, CancelIndicator cancelIndicator) {
			super();
			this.currentObject = currentObject;
			this.currentMethod = currentMethod;
			this.currentDocument = currentDocument;
			this.currentResource = currentResource;
			this.currentParams = currentParams;
			this.cancelIndicator = cancelIndicator;
		}

		@Override
		public int hashCode() {
			return Objects.hash(cancelIndicator, currentDocument, currentMethod, currentObject, currentParams,
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
					&& Objects.equals(currentParams, other.currentParams)
					&& Objects.equals(currentResource, other.currentResource);
		}
		 
	}

}
