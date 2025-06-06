package com.regnosys.rosetta.cache.caches;

import com.google.common.cache.CacheBuilder;
import com.regnosys.rosetta.cache.AbstractRequestScopedCache;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.types.RMetaAnnotatedType;

public class ExpressionTypeCache extends AbstractRequestScopedCache<RosettaExpression, RMetaAnnotatedType> {
	public ExpressionTypeCache() {
		super(
				CacheBuilder.newBuilder()
					.weakKeys()
			);
	}
}
