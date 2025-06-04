package com.regnosys.rosetta.cache.caches;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import com.google.common.cache.Cache;
import com.regnosys.rosetta.cache.AbstractRequestScopedCache;
import com.regnosys.rosetta.types.RMetaAnnotatedType;

import jakarta.inject.Provider;

public class ExpectedTypeCache extends AbstractRequestScopedCache<ExpectedTypeCache.FeatureReference, RMetaAnnotatedType> {
	public ExpectedTypeCache(Cache<FeatureReference, Object> managedCache) {
		super(managedCache);
	}
	
	public RMetaAnnotatedType get(EObject owner, EReference reference, int index, Provider<? extends RMetaAnnotatedType> loader) {
		return get(new FeatureReference(owner, reference, index), loader);
	}

	static record FeatureReference(EObject owner, EReference reference, int index) {}
}
