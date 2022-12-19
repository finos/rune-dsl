package com.regnosys.rosetta.ide.semantictokens;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.DocumentExtensions;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.SimpleCache;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.regnosys.rosetta.ide.util.RangeUtils;

public class AbstractSemanticTokensService implements ISemanticTokensService {
	private static final Logger log = Logger.getLogger(AbstractSemanticTokensService.class);

	@Inject
	private RangeUtils rangeUtils;
	
	@Inject
	private DocumentExtensions documentExtensions;
	
	@Inject
	private ILocationInFileProvider locationInFileProvider;

	@Inject
	private Injector injector;
	
	private final List<ISemanticTokenType> tokenTypes;
	private final List<ISemanticTokenModifier> tokenModifiers;
	
	

	private final ThreadLocal<State> state;

	private volatile Set<MethodWrapper> providerMethods = null;

	private final SimpleCache<Class<?>, List<MethodWrapper>> methodsForType = new SimpleCache<>(
		param -> {
			List<MethodWrapper> result = new ArrayList<>();
			for (MethodWrapper mw : providerMethods) {
				if (mw.isMatching(param))
					result.add(mw);
			}
			return result;
		});
	
	@Inject
	public AbstractSemanticTokensService(ISemanticTokenTypesProvider tokenTypesProvider, ISemanticTokenModifiersProvider tokenModifiersProvider) {
		this.state = new ThreadLocal<>();
		this.tokenTypes = tokenTypesProvider.getSemanticTokenTypes();
		this.tokenModifiers = tokenModifiersProvider.getSemanticTokenModifiers();
	}
	
	@Override
	public SemanticTokensLegend getLegend() {
		return new SemanticTokensLegend(
					this.tokenTypes.stream().map(ISemanticTokenType::getValue).collect(Collectors.toList()),
					this.tokenModifiers.stream().map(ISemanticTokenModifier::getValue).collect(Collectors.toList())
				);
	}
	
	@Override
	public SemanticTokens toSemanticTokensResponse(List<SemanticToken> tokens) {
		List<Integer> data = new ArrayList<Integer>(tokens.size()*5);
		
		tokens.sort(null);
		
		int lastLine = 0;
		int lastStartChar = 0;
		for (int i=0; i < tokens.size(); i++) {
			SemanticToken token = tokens.get(i);
			int deltaLine = token.getLine() - lastLine;
			int deltaStartChar;
			if (deltaLine == 0) {
				deltaStartChar = token.getStartChar() - lastStartChar;
			} else {
				deltaStartChar = token.getStartChar();
			}			
			
			data.add(deltaLine);
			data.add(deltaStartChar);
			data.add(token.getLength());
			data.add(getTokenTypeRepr(token.getTokenType()));
			data.add(getTokenModifiersRepr(token.getTokenModifiers()));
			
			lastLine = token.getLine();
			lastStartChar = token.getStartChar();
		}
		
		return new SemanticTokens(data);
	}
	
	private int getTokenTypeRepr(final ISemanticTokenType tokenType) {
		int repr = this.tokenTypes.indexOf(tokenType);
		if (repr == -1) {
			throw new Error(String.format("Token type `%s` not found. Did you forget to bind it in the `%s`?", tokenType.getValue(), ISemanticTokenTypesProvider.class.getSimpleName()));
		}
		return repr;
	}

	private int getTokenModifiersRepr(final ISemanticTokenModifier[] tokenModifiers) {
		int bitmask = 0;
		for (ISemanticTokenModifier mod : tokenModifiers) {
			int repr = this.tokenModifiers.indexOf(mod);
			if (repr == -1) {
				throw new Error(String.format("Token modifier `%s` not found. Did you forget to bind it in the `%s`?", mod.getValue(), ISemanticTokenModifiersProvider.class.getSimpleName()));
			}
			bitmask |= repr;
		}
		return bitmask;
	}
	
	@Override
	public List<SemanticToken> computeSemanticTokens(Document document, XtextResource resource, SemanticTokensParams params, CancelIndicator cancelIndicator) {
		return computeSemanticTokens(document, resource, x -> true, cancelIndicator);
	}
	
	@Override
	public List<SemanticToken> computeSemanticTokensInRange(Document document, XtextResource resource, SemanticTokensRangeParams params, CancelIndicator cancelIndicator) {
		return computeSemanticTokens(document, resource, x -> rangeUtils.overlap(params.getRange(), x), cancelIndicator);
	}

	private List<SemanticToken> computeSemanticTokens(Document document, XtextResource resource, Predicate<EObject> filter, CancelIndicator cancelIndicator) {
		if (providerMethods == null) {
			synchronized (this) {
				if (providerMethods == null) {
					Set<MethodWrapper> providerMethods = Sets.newLinkedHashSet();
					providerMethods.addAll(collectMethods(getClass()));
					this.providerMethods = providerMethods;
				}
			}
		}
		
		List<SemanticToken> result = Lists.newArrayList();
		TreeIterator<EObject> contents = resource.getAllContents();
		while (contents.hasNext()) {
			EObject obj = contents.next();
			
			if (filter.test(obj)) {
				if (cancelIndicator.isCanceled()) return null;
				
				for (MethodWrapper m: methodsForType.get(obj.getClass())) {
					if (cancelIndicator.isCanceled()) return null;
					
					result.addAll(
							m.invoke(new State(obj, m.getMethod(), document, resource, cancelIndicator)));
				}
			}
		}
		return result;
	}

	protected SemanticToken createSemanticToken(EObject tokenObject, ISemanticTokenType tokenType, ISemanticTokenModifier... tokenModifiers) {
		ITextRegion region = locationInFileProvider.getFullTextRegion(tokenObject);
		Range range = documentExtensions.newRange(tokenObject.eResource(), region);
		return new SemanticToken(range.getStart().getLine(), range.getStart().getCharacter(), region.getLength(), tokenType, tokenModifiers);
	}
	
	protected SemanticToken createSemanticToken(EObject tokenObject, EStructuralFeature feature, ISemanticTokenType tokenType, ISemanticTokenModifier... tokenModifiers) {
		return createSemanticToken(tokenObject, feature, -1, tokenType);
	}
	
	protected SemanticToken createSemanticToken(EObject tokenObject, EStructuralFeature feature, int featureIndex, ISemanticTokenType tokenType, ISemanticTokenModifier... tokenModifiers) {
		ITextRegion region = locationInFileProvider.getFullTextRegion(tokenObject, feature, featureIndex);
		Range range = documentExtensions.newRange(tokenObject.eResource(), region);
		return new SemanticToken(range.getStart().getLine(), range.getStart().getCharacter(), region.getLength(), tokenType, tokenModifiers);
	}
	
	private List<MethodWrapper> collectMethods(Class<? extends AbstractSemanticTokensService> clazz) {
		List<MethodWrapper> providerMethods = new ArrayList<>();
		Set<Class<?>> visitedClasses = new HashSet<>(4);
		collectMethods(this, clazz, visitedClasses, providerMethods);
		return providerMethods;
	}

	private void collectMethods(AbstractSemanticTokensService instance,
								Class<? extends AbstractSemanticTokensService> clazz, Collection<Class<?>> visitedClasses,
								Collection<MethodWrapper> result) {
		if (visitedClasses.contains(clazz))
			return;

		collectMethodsImpl(instance, clazz, visitedClasses, result);
	}

	private void collectMethodsImpl(AbstractSemanticTokensService instance,
									Class<? extends AbstractSemanticTokensService> clazz, Collection<Class<?>> visitedClasses,
									Collection<MethodWrapper> result) {
		if (!visitedClasses.add(clazz))
			return;
		AbstractSemanticTokensService instanceToUse;
		instanceToUse = instance;
		if (instanceToUse == null) {
			instanceToUse = newInstance(clazz);
		}
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (method.getAnnotation(MarkSemanticToken.class) != null && method.getParameterTypes().length == 1) {
				result.add(createMethodWrapper(instanceToUse, method));
			}
		}
		Class<? extends AbstractSemanticTokensService> superClass = getSuperClass(clazz);
		if (superClass != null)
			collectMethodsImpl(instanceToUse, superClass, visitedClasses, result);
	}

	protected MethodWrapper createMethodWrapper(AbstractSemanticTokensService instanceToUse, Method method) {
		return new MethodWrapper(instanceToUse, method);
	}

	protected AbstractSemanticTokensService newInstance(Class<? extends AbstractSemanticTokensService> clazz) {
		AbstractSemanticTokensService instanceToUse;
		if (injector == null)
			throw new IllegalStateException("The class is not configured with an injector.");
		instanceToUse = injector.getInstance(clazz);
		return instanceToUse;
	}


	private Class<? extends AbstractSemanticTokensService> getSuperClass(
		Class<? extends AbstractSemanticTokensService> clazz) {
		try {
			Class<? extends AbstractSemanticTokensService> superClass = clazz.getSuperclass().asSubclass(
					AbstractSemanticTokensService.class);
			if (AbstractSemanticTokensService.class.equals(superClass))
				return null;
			return superClass;
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	static class MethodWrapper {
		private final Method method;
		private final String s;
		private final AbstractSemanticTokensService instance;

		protected MethodWrapper(AbstractSemanticTokensService instance, Method m) {
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

		public List<SemanticToken> invoke(State state) {
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
						return ((List<?>) res).stream().filter(SemanticToken.class::isInstance).map(SemanticToken.class::cast)
							.collect(Collectors.toList());
					} else if (res instanceof SemanticToken) {
						return List.of((SemanticToken) res);
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

		public AbstractSemanticTokensService getInstance() {
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
