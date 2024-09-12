package com.regnosys.rosetta.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.eclipse.xtext.EcoreUtil2;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.rosetta.expression.TranslateDispatchOperation;
import com.regnosys.rosetta.rosetta.translate.TranslateSource;
import com.regnosys.rosetta.rosetta.translate.Translation;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.types.TypeSystem;

public class TranslateUtil {
	private final TypeSystem typeSystem;
	private final RosettaTypeProvider typeProvider;
	
	@Inject
	public TranslateUtil(TypeSystem typeSystem, RosettaTypeProvider typeProvider) {
		this.typeSystem = typeSystem;
		this.typeProvider = typeProvider;
	}
	
	private Stream<Translation> getAllTranslations(TranslateSource source) {
		return Streams.concat(
				source.getSuperSources().stream().flatMap(s -> getAllTranslations(s)),
				source.getTranslations().stream()
			);
	}
	
	public List<Translation> findMatches(TranslateDispatchOperation op) {
		return findMatches(
				getSource(op),
				typeSystem.typeCallToRType(op.getOutputType()),
				op.getInputs().stream().map(typeProvider::getRType).collect(Collectors.toList())
			);
	}
	private List<Translation> findMatches(TranslateSource source, RType resultType, List<RType> inputTypes) {
		return getAllTranslations(source)
				.filter(t -> matches(t, resultType, inputTypes))
				.collect(Collectors.toList());
	}
	
	public Translation findLastMatch(TranslateDispatchOperation op) {
		return findLastMatch(
				getSource(op),
				typeSystem.typeCallToRType(op.getOutputType()),
				op.getInputs().stream().map(typeProvider::getRType).collect(Collectors.toList())
			);
	}
	private Translation findLastMatch(TranslateSource source, RType resultType, List<RType> inputTypes) {
		return Streams.findLast(
					getAllTranslations(source)
						.filter(t -> matches(t, resultType, inputTypes)))
				.orElse(null);
	}
	
	public boolean hasAnyMatch(TranslateDispatchOperation op) {
		return hasAnyMatch(
				getSource(op),
				typeSystem.typeCallToRType(op.getOutputType()),
				op.getInputs().stream().map(typeProvider::getRType).collect(Collectors.toList())
			);
	}
	public boolean hasAnyMatch(TranslateSource source, RType resultType, List<RType> inputTypes) {
		return getAllTranslations(source)
				.anyMatch(t -> matches(t, resultType, inputTypes));
	}

	public boolean matches(Translation translation, RType resultType, List<RType> inputTypes) {
		if (resultType == null) {
			// For invalid models
			return false;
		}
		// Check parameters match
		if (translation.getParameters().size() != inputTypes.size()) {
			return false;
		}
		for (int i=0;i<inputTypes.size();i++) {
			RType inputType = inputTypes.get(i);
			RType actualInputType = typeProvider.getRTypeOfSymbol(translation.getParameters().get(i));
			if (inputType == null || actualInputType == null) {
				// For invalid models
				return false;
			}
			if (!typeSystem.isSubtypeOf(inputType, actualInputType)) {
				return false;
			}
		}
		
		// Check output matches
		RType actualResultType = typeProvider.getRTypeOfTranslation(translation);
		if (!typeSystem.stripFromTypeAliases(resultType).equals(typeSystem.stripFromTypeAliases(actualResultType))) {
			return false;
		}
		
		return true;
	}
	
	public TranslateSource getSource(TranslateDispatchOperation op) {
		if (op.getSource() == null) {
			return EcoreUtil2.getContainerOfType(op, TranslateSource.class);
		}
		return op.getSource();
	}
}
