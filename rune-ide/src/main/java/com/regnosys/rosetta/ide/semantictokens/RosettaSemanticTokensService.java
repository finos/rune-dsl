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

package com.regnosys.rosetta.ide.semantictokens;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.collect.Sets;
import com.regnosys.rosetta.RosettaEcoreUtil;
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
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.RuleReferenceAnnotation;
import com.regnosys.rosetta.rosetta.simple.Segment;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.types.CardinalityProvider;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RosettaTypeProvider;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.regnosys.rosetta.ide.semantictokens.RosettaSemanticTokenTypesEnum.*;
import static com.regnosys.rosetta.ide.semantictokens.RosettaSemanticTokenModifiersEnum.*;

public class RosettaSemanticTokensService extends AbstractSemanticTokensService {
	@Inject
	private RosettaEcoreUtil extensions;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject
	private RosettaTypeProvider typeProvider;
	
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
	private RosettaSemanticTokenModifiersEnum getCardinalityModifier(RosettaSymbol symbol) {
		if (cardinalityProvider.isSymbolMulti(symbol)) {
			return MULTI_CARDINALITY;
		}
		return SINGLE_CARDINALITY;
	}
	private RosettaSemanticTokenModifiersEnum getCardinalityModifier(RosettaImplicitVariable implicitVar) {
		if (cardinalityProvider.isMulti(implicitVar)) {
			return MULTI_CARDINALITY;
		}
		return SINGLE_CARDINALITY;
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
		if (extensions.isResolved(annotation.getAnnotation()) && "metadata".equals(annotation.getAnnotation().getName()) && annotation.getAttribute() != null) {
			return createSemanticToken(annotation, ANNOTATION_REF__ATTRIBUTE, META_MEMBER);
		}
		return null;
	}
	
	private SemanticToken markFunction(EObject objectToMark, EStructuralFeature featureToMark, Function function) {
		return createSemanticToken(objectToMark, featureToMark, RosettaSemanticTokenTypesEnum.FUNCTION, getCardinalityModifier(function));
	}
	@MarkSemanticToken
	public SemanticToken markFunctionDeclaration(Function function) {
		return markFunction(function, ROSETTA_NAMED__NAME, function);
	}
	
	private SemanticToken markRule(EObject objectToMark, EStructuralFeature featureToMark, RosettaRule rule) {
		return createSemanticToken(objectToMark, featureToMark, RosettaSemanticTokenTypesEnum.RULE, getCardinalityModifier(rule));
	}
	@MarkSemanticToken
	public SemanticToken markRuleDeclaration(RosettaRule rule) {
		return markRule(rule, ROSETTA_NAMED__NAME, rule);
	}
	@MarkSemanticToken
	public SemanticToken markRuleReference(RuleReferenceAnnotation ruleRef) {
		RosettaRule rule = ruleRef.getReportingRule();
		if (extensions.isResolved(rule)) {
			return markRule(ruleRef, RULE_REFERENCE_ANNOTATION__REPORTING_RULE, rule);
		}
		return null;
	}
	
	private SemanticToken markAttribute(EObject objectToMark, EStructuralFeature featureToMark, Attribute attribute, AttributeType t) {
		if (attribute instanceof ChoiceOption) {
			return null;
		}
		
		if (t == AttributeType.INPUT) {
			return createSemanticToken(objectToMark, featureToMark, PARAMETER, getCardinalityModifier(attribute));
		} else if (t == AttributeType.OUTPUT) {
			return createSemanticToken(objectToMark, featureToMark, OUTPUT, getCardinalityModifier(attribute));
		}
		
		RosettaSemanticTokenTypesEnum tokenType = null;
		EReference containmentFeature = attribute.eContainmentFeature();
		if (containmentFeature.equals(FUNCTION__INPUTS)) {
			tokenType = PARAMETER;
		} else if (containmentFeature.equals(FUNCTION__OUTPUT)) {
			tokenType = OUTPUT;
		} else if (containmentFeature.equals(DATA__ATTRIBUTES)) {
			tokenType = PROPERTY;
		}

		if (tokenType == null) {
			return null;
		}
		return createSemanticToken(objectToMark, featureToMark, tokenType, getCardinalityModifier(attribute));
	}
	@MarkSemanticToken
	public SemanticToken markAttributeDeclaration(Attribute attribute) {
		return markAttribute(attribute, ROSETTA_NAMED__NAME, attribute, AttributeType.OTHER);
	}
	
	private SemanticToken markClosureParameter(EObject objectToMark, EStructuralFeature featureToMark, ClosureParameter param) {
		return createSemanticToken(objectToMark, featureToMark, INLINE_PARAMETER, getCardinalityModifier(param));
	}
	@MarkSemanticToken
	public SemanticToken markClosureParameterDeclaration(ClosureParameter param) {
		return markClosureParameter(param, ROSETTA_NAMED__NAME, param);
	}
	
	private SemanticToken markAlias(EObject objectToMark, EStructuralFeature featureToMark, ShortcutDeclaration alias) {
		return createSemanticToken(objectToMark, featureToMark, VARIABLE, getCardinalityModifier(alias));
	}
	@MarkSemanticToken
	public SemanticToken markAliasDeclaration(ShortcutDeclaration alias) {
		return markAlias(alias, ROSETTA_NAMED__NAME, alias);
	}
	
	@MarkSemanticToken
	public List<SemanticToken> markOperation(Operation operation) {
		List<SemanticToken> result = new ArrayList<>();
		RosettaSymbol assignRoot = operation.getAssignRoot();
		if (extensions.isResolved(assignRoot)) {
			SemanticToken assignRootToken = markSymbol(operation, OPERATION__ASSIGN_ROOT, assignRoot);
			if (assignRootToken != null) {
				result.add(assignRootToken);
			}
		}
		for (Segment seg : operation.pathAsSegmentList()) {
			RosettaFeature segAttr = seg.getFeature();
			if (extensions.isResolved(segAttr)) {
				SemanticToken segmentToken = markFeature(seg, SEGMENT__FEATURE, segAttr, AttributeType.OUTPUT);
				if (segmentToken != null) {
					result.add(segmentToken);
				}
			}
		}
		return result;
	}
	
	@MarkSemanticToken
	public SemanticToken markConstructorFeature(ConstructorKeyValuePair pair) {
		RosettaFeature feature = pair.getKey();
		if (extensions.isResolved(feature)) {
			return markFeature(pair, CONSTRUCTOR_KEY_VALUE_PAIR__KEY, feature, AttributeType.OUTPUT);
		}
		return null;
	}
	
	private SemanticToken markFeature(EObject objectToMark, EStructuralFeature featureToMark, RosettaFeature feature, AttributeType t) {
		if (feature instanceof RosettaEnumValue) {
			return createSemanticToken(objectToMark, featureToMark, ENUM_MEMBER);
		} else if (feature instanceof Attribute) {
			return markAttribute(objectToMark, featureToMark, (Attribute)feature, t);
		} else if (feature instanceof RosettaMetaType) {
			return createSemanticToken(objectToMark, featureToMark, META_MEMBER);
		}
		return null;
	}
	@MarkSemanticToken
	public SemanticToken markFeature(RosettaFeatureCall featureCall) {
		RosettaFeature feature = featureCall.getFeature();
		if (extensions.isResolved(feature)) {
			return markFeature(featureCall, ROSETTA_FEATURE_CALL__FEATURE, feature, AttributeType.OTHER);
		}
		return null;
	}
	
	private SemanticToken markSymbol(EObject objectToMark, EStructuralFeature featureToMark, RosettaSymbol symbol) {
		if (symbol instanceof Attribute) {
			RMetaAnnotatedType implicitType = typeProvider.typeOfImplicitVariable(objectToMark);
			if (implicitType != null) {
				Set<? extends RosettaFeature> implicitFeatures = Sets.newHashSet(extensions.allFeaturesExcludingEnumValues(implicitType, objectToMark));
				if (implicitFeatures.contains(symbol)) {
					return markAttribute(objectToMark, featureToMark, (Attribute)symbol, AttributeType.INPUT);
				}
			}
			return markAttribute(objectToMark, featureToMark, (Attribute)symbol, AttributeType.OTHER);
		} else if (symbol instanceof ClosureParameter) {
			return markClosureParameter(objectToMark, featureToMark, (ClosureParameter)symbol);
		} else if (symbol instanceof Function) {
			return markFunction(objectToMark, featureToMark, (Function)symbol);
		} else if (symbol instanceof RosettaExternalFunction) {
			return createSemanticToken(objectToMark, featureToMark, RosettaSemanticTokenTypesEnum.FUNCTION, DEFAULT_LIBRARY);
		} else if (symbol instanceof RosettaRule) {
			return markRule(objectToMark, featureToMark, (RosettaRule)symbol);
		} else if (symbol instanceof ShortcutDeclaration) {
			return markAlias(objectToMark, featureToMark, (ShortcutDeclaration)symbol);
		} else if (symbol instanceof TypeParameter) {
			return createSemanticToken(objectToMark, featureToMark, PARAMETER);
		}
		return null;
	}
	@MarkSemanticToken
	public SemanticToken markSymbolReference(RosettaSymbolReference reference) {
		RosettaSymbol symbol = reference.getSymbol();
		if (extensions.isResolved(symbol)) {
			return markSymbol(reference, ROSETTA_SYMBOL_REFERENCE__SYMBOL, symbol);
		}
		return null;
	}
	
	@MarkSemanticToken
	public SemanticToken markImplicitVariable(RosettaImplicitVariable implicitVar) {
		if (implicitVar.isGenerated()) {
			return null;
		}
		return createSemanticToken(implicitVar, ROSETTA_NAMED__NAME, IMPLICIT_VARIABLE, getCardinalityModifier(implicitVar));
	}
}
