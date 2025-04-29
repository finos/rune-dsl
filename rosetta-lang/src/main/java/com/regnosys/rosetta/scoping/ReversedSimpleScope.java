package com.regnosys.rosetta.scoping;

import java.util.Collection;
import java.util.Iterator;

import jakarta.inject.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;
import org.eclipse.xtext.scoping.impl.SimpleScope;
import org.eclipse.xtext.util.SimpleAttributeResolver;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 * A simple scope with reverse shadowing: for a given name, first the parent scope will be checked,
 * and if no element was found, only then local elements will be checked.
 * 
 * In that sense, local elements that shadow an element in the parent scope will be hidden.
 */
public class ReversedSimpleScope extends SimpleScope {
	
	protected static class LocalIterable implements Iterable<IEObjectDescription>, Predicate<IEObjectDescription> {

		private final ReversedSimpleScope scope;
		private final Provider<Iterable<IEObjectDescription>> provider;
		private Iterable<IEObjectDescription> localElements;

		protected LocalIterable(ReversedSimpleScope scope, Provider<Iterable<IEObjectDescription>> provider) {
			this.scope = scope;
			this.provider = provider;
		}

		@Override
		public Iterator<IEObjectDescription> iterator() {
			if (localElements == null) {
				localElements = provider.get();
			}
			Iterator<IEObjectDescription> localIterator = localElements.iterator();
			Iterator<IEObjectDescription> filteredIterator = Iterators.filter(localIterator, this);
			return filteredIterator;
		}

		@Override
		public boolean apply(IEObjectDescription input) {
			return !scope.isShadowed(input);
		}
		
		@Override
		public String toString() {
			return Iterables.toString(this);
		}
		
	}
	
	public static IScope scopeFor(Iterable<? extends EObject> elements, IScope outer) {
		return scopeFor(elements, QualifiedName.wrapper(SimpleAttributeResolver.NAME_RESOLVER), outer);
	}
	
	public static <T extends EObject> IScope scopeFor(Iterable<? extends T> elements,
			final Function<T, QualifiedName> nameComputation, IScope outer) {
		return new ReversedSimpleScope(outer, Scopes.scopedElementsFor(elements, nameComputation));
	}

	public ReversedSimpleScope(IScope parent, Iterable<IEObjectDescription> descriptions) {
		super(parent, descriptions);
	}
	
	public ReversedSimpleScope(Iterable<IEObjectDescription> descriptions) {
		super(IScope.NULLSCOPE, descriptions);
	}

	@Override
	public IEObjectDescription getSingleElement(QualifiedName name) {
		IEObjectDescription result = getParent().getSingleElement(name);
		if (result != null)
			return result;
		return getSingleLocalElementByName(name);
	}

	@Override
	public Iterable<IEObjectDescription> getAllElements() {
		Iterable<IEObjectDescription> parentElements = getParent().getAllElements();
		Iterable<IEObjectDescription> localElements = getLocalElements(new Provider<Iterable<IEObjectDescription>>() {
			@Override
			public Iterable<IEObjectDescription> get() {
				return getAllLocalElements();
			}
		});
		Iterable<IEObjectDescription> result = Iterables.concat(parentElements, localElements);
		return result;
	}
	
	@Override
	public Iterable<IEObjectDescription> getElements(final QualifiedName name) {
		Iterable<IEObjectDescription> parentElements = getParent().getElements(name);
		if (parentElements instanceof Collection) {
			if (((Collection<?>) parentElements).isEmpty())
				return getLocalElementsByName(name);
		}
		Iterable<IEObjectDescription> localElements = getLocalElements(new Provider<Iterable<IEObjectDescription>>() {
			@Override
			public Iterable<IEObjectDescription> get() {
				return getLocalElementsByName(name);
			}
		});
		Iterable<IEObjectDescription> result = Iterables.concat(parentElements, localElements);
		return result;
	}
	
	@Override
	public Iterable<IEObjectDescription> getElements(final EObject object) {
		Iterable<IEObjectDescription> parentElements = getParent().getElements(object);
		final URI uri = EcoreUtil2.getPlatformResourceOrNormalizedURI(object);
		Iterable<IEObjectDescription> localElements = getLocalElements(new Provider<Iterable<IEObjectDescription>>() {
			@Override
			public Iterable<IEObjectDescription> get() {
				return getLocalElementsByEObject(object, uri);
			}
		});
		Iterable<IEObjectDescription> result = Iterables.concat(parentElements, localElements);
		return result;
	}
	
	protected Iterable<IEObjectDescription> getLocalElements(Provider<Iterable<IEObjectDescription>> provider) {
		return new LocalIterable(this, provider);
	}
	
	@Override
	protected boolean isShadowed(IEObjectDescription fromParent) {
		if (shadowingIndex == null) {
			shadowingIndex = Sets.newHashSet();
			for(IEObjectDescription parentElement: getParent().getAllElements()) {
				shadowingIndex.add(getShadowingKey(parentElement));
			}
		}
		boolean result = shadowingIndex.contains(getShadowingKey(fromParent));
		return result;
	}

}
