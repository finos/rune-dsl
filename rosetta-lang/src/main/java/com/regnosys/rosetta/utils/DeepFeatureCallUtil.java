package com.regnosys.rosetta.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.OneOfOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaDeepFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;

public class DeepFeatureCallUtil {
	private final RosettaTypeProvider typeProvider;
	
	private final RosettaExtensions ext;
	
	@Inject
	public DeepFeatureCallUtil(RosettaTypeProvider typeProvider, RosettaExtensions ext) {
		this.typeProvider = typeProvider;
		this.ext = ext;
	}
	
	public List<List<RosettaSymbol>> getFullPaths(RosettaDeepFeatureCall featureCall) {
		
	}
	
	private Stream<List<Attribute>> getFullPaths(RDataType receiver, String featureName) {
		Stream<Attribute> allAttributes = StreamSupport.stream(ext.allNonOverridesAttributes(receiver.getData()).spliterator(), false);
		if (isEligibleForDeepFeatureCall(receiver)) {
			return allAttributes
					.<List<Attribute>>flatMap(attr -> {
						RType attrType = typeProvider.getRTypeOfSymbol(attr);
						if (attrType instanceof RDataType) {
							List<List<Attribute>> subPaths = getFullPaths((RDataType) attrType, featureName).collect(Collectors.toList());
							subPaths.forEach(p -> p.add(0, attr));
							return subPaths.stream();
						}
						return Stream.of();
					});
		} else {
			return allAttributes
					.filter(attr -> attr.getName().equals(featureName))
					.map(attr -> {
						List<Attribute> path = new LinkedList<>();
						path.add(attr);
						return path;
					});
		}
	}
	
	public boolean isEligibleForDeepFeatureCall(RDataType type) {
		// Return true if:
		// 1. The data type has a `one-of` condition.
		// 2. All attributes have a cardinality of the form `(0..1)`.
		Data data = ((RDataType) type).getData();
		if (data.getConditions().stream().anyMatch(cond -> isOneOfItem(cond.getExpression()))) {
			if (data.getAttributes().stream().allMatch(a -> isSingularOptional(a.getCard()))) {
				return true;
			}
		}
		return false;
	}
	private boolean isOneOfItem(RosettaExpression expr) {
		if (expr instanceof OneOfOperation) {
			if (((OneOfOperation) expr).getArgument() instanceof RosettaImplicitVariable) {
				return true;
			}
		}
		return false;
	}
	private boolean isSingularOptional(RosettaCardinality card) {
		return !card.isUnbounded() && card.getInf() == 0 && card.getSup() == 1;
	}
}
