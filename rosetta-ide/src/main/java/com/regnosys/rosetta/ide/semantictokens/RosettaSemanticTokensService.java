package com.regnosys.rosetta.ide.semantictokens;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EReference;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference;
import com.regnosys.rosetta.rosetta.RosettaBasicType;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaExternalFunction;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaMetaType;
import com.regnosys.rosetta.rosetta.RosettaRecordType;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.RosettaSegmentRef;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.RosettaType;
import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.TypeParameter;
import com.regnosys.rosetta.rosetta.expression.ClosureParameter;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.regnosys.rosetta.ide.semantictokens.RosettaSemanticTokenTypesEnum.*;
import static com.regnosys.rosetta.ide.semantictokens.lsp.LSPSemanticTokenModifiersEnum.*;

public class RosettaSemanticTokensService extends AbstractSemanticTokensService {
	@Inject
	private RosettaExtensions extensions;
	
	@Inject
	public RosettaSemanticTokensService(ISemanticTokenTypesProvider tokenTypesProvider,
			ISemanticTokenModifiersProvider tokenModifiersProvider) {
		super(tokenTypesProvider, tokenModifiersProvider);
	}
	
	private Optional<RosettaSemanticTokenTypesEnum> typeToToken(RosettaType t) {
		if (extensions.isResolved(t)) {
			if (t instanceof Data) {
				return Optional.of(TYPE);
			} else if (t instanceof RosettaBasicType) {
				return Optional.of(BASIC_TYPE);
			} else if (t instanceof RosettaRecordType) {
				return Optional.of(RECORD_TYPE);
			} else if (t instanceof RosettaEnumeration) {
				return Optional.of(ENUM);
			} else if (t instanceof RosettaTypeAlias) {
				return Optional.of(TYPE_ALIAS);
			}
		}
		return Optional.empty();
	}

	@MarkSemanticToken
	public Optional<SemanticToken> markType(TypeCall typeCall) {
		RosettaType t = typeCall.getType();
		return typeToToken(t)
				.map(token -> 
					createSemanticToken(typeCall, TYPE_CALL__TYPE, token));
	}
	
	@MarkSemanticToken
	public List<SemanticToken> markDocumentReferences(RegulatoryDocumentReference docRef) {
		List<SemanticToken> tokens = new ArrayList<>();
		for (int i = 0; i < docRef.getCorpusList().size(); i++) {
			tokens.add(createSemanticToken(docRef, REGULATORY_DOCUMENT_REFERENCE__CORPUS_LIST, i, DOCUMENT_CORPUS));
		}
		for (RosettaSegmentRef seg: docRef.getSegments()) {
			tokens.add(createSemanticToken(seg, ROSETTA_SEGMENT_REF__SEGMENT, DOCUMENT_SEGMENT));
		}
		return tokens;
	}
	
	@MarkSemanticToken
	public SemanticToken markMetaMemberInAnnotation(AnnotationRef annotation) {
		if (annotation.getAnnotation().getName().equals("metadata") && annotation.getAttribute() != null) {
			return createSemanticToken(annotation, ANNOTATION_REF__ATTRIBUTE, META_MEMBER);
		}
		return null;
	}
	
	@MarkSemanticToken
	public SemanticToken markFeature(RosettaFeatureCall featureCall) {
		RosettaFeature feature = featureCall.getFeature();
		if (extensions.isResolved(feature)) {
			if (feature instanceof RosettaEnumValue) {
				return createSemanticToken(featureCall, ROSETTA_FEATURE_CALL__FEATURE, ENUM_MEMBER);
			} else if (feature instanceof Attribute) {
				return createSemanticToken(featureCall, ROSETTA_FEATURE_CALL__FEATURE, PROPERTY);
			} else if (feature instanceof RosettaMetaType) {
				return createSemanticToken(featureCall, ROSETTA_FEATURE_CALL__FEATURE, META_MEMBER);
			}
		}
		return null;
	}
	
	@MarkSemanticToken
	public SemanticToken markRosettaReference(RosettaSymbolReference reference) {
		RosettaSymbol symbol = reference.getSymbol();
		if (extensions.isResolved(symbol)) {
			if (symbol instanceof Attribute) {
				EReference containmentFeature = symbol.eContainmentFeature();
				if (containmentFeature.equals(FUNCTION__INPUTS)) {
					return createSemanticToken(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, PARAMETER);
				} else if (containmentFeature.equals(DATA__ATTRIBUTES)) {
					return createSemanticToken(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, PROPERTY);
				}
				return null;
			} else if (symbol instanceof ClosureParameter) {
				return createSemanticToken(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, INLINE_PARAMETER);
			} else if (symbol instanceof Function) {
				return createSemanticToken(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, RosettaSemanticTokenTypesEnum.FUNCTION);
			} else if (symbol instanceof RosettaExternalFunction) {
				return createSemanticToken(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, RosettaSemanticTokenTypesEnum.FUNCTION, DEFAULT_LIBRARY);
			} else if (symbol instanceof RosettaRule) {
				return createSemanticToken(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, RosettaSemanticTokenTypesEnum.RULE);
			} else if (symbol instanceof ShortcutDeclaration) {
				return createSemanticToken(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, VARIABLE);
			} else if (symbol instanceof RosettaType) {
				return createSemanticToken(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, typeToToken((RosettaType)symbol).get());
			} else if (symbol instanceof TypeParameter) {
				return createSemanticToken(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, PARAMETER);
			}
		}
		return null;
	}
}
