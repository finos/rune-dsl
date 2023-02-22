package com.regnosys.rosetta.ide.inlayhints;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.generator.java.function.CardinalityProvider;
import com.regnosys.rosetta.rosetta.RosettaBlueprint;
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.ClosureParameter;
import com.regnosys.rosetta.rosetta.expression.InlineFunction;
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.services.RosettaGrammarAccess;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RosettaTypeProvider;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;
import com.regnosys.rosetta.validation.BindableType;
import com.regnosys.rosetta.validation.RosettaBlueprintTypeResolver;
import com.regnosys.rosetta.validation.RosettaBlueprintTypeResolver.BlueprintUnresolvedTypeException;
import com.regnosys.rosetta.validation.TypedBPNode;

public class RosettaInlayHintsService extends AbstractInlayHintsService {
	@Inject
	private RosettaTypeProvider types;
	@Inject
	private CardinalityProvider card;
	@Inject
	private RosettaBlueprintTypeResolver blueprintTypes;
	@Inject
	private RosettaExtensions extensions;
	@Inject 
	private RosettaGrammarAccess grammar;
	@Inject
	private ImplicitVariableUtil implVarUtil;
	
	// TODO: duplicate code with RosettaSemanticTokensService.
	private boolean isResolved(EObject obj) {
		return obj != null && !obj.eIsProxy();
	}
	
	private String typeInfo(RType type, boolean isMulti) {
		if (isMulti) {
			return type.getName() + "*";
		} else {
			return type.getName();
		}
	}
	private String typeInfo(BindableType type, boolean isMulti) {
		if (type.type != null) {
			return typeInfo(types.getRType(type.type), isMulti);
		}
		if (isMulti) {
			return type.getEither() + "*";
		} else {
			return type.getEither();
		}
	}
	
	@InlayHintCheck
	public InlayHint checkRosettaImplicitFeatureCall(RosettaSymbolReference ref) {
		RosettaSymbol symbol = ref.getSymbol();
		if (isResolved(symbol)) {
			RType implicitType = types.typeOfImplicitVariable(ref);
			boolean implicitMulti = card.isImplicitVariableMulti(ref);
			if (implicitType != null) {
				
				boolean needsInlayHint = false;
				if (symbol instanceof RosettaCallableWithArgs) {
					needsInlayHint = !ref.isExplicitArguments();
				} else if (symbol instanceof RosettaFeature) {
					needsInlayHint = Iterables.contains(extensions.allFeatures(implicitType), symbol);
				}
				
				if (needsInlayHint) {
					return inlayHintBefore(ref, typeInfo(implicitType, implicitMulti) + " ->", null);
				}
			}
		}
		return null;
	}
	
	@InlayHintCheck
	public List<InlayHint> checkFunctionalOperation(RosettaFunctionalOperation op) {
		if (isResolved(op.getFunctionRef()) && op.getFunctionRef() instanceof InlineFunction) {
			InlineFunction f = (InlineFunction)op.getFunctionRef();
			
			List<InlayHint> inlays = new ArrayList<>();

			if (f.getParameters().size() == 0) {
				RType implicitType = types.typeOfImplicitVariable(f);
				boolean implicitMulti = card.isImplicitVariableMulti(f);
				if (implicitType != null) {
					Keyword leftBracket = grammar.getFunctionReferenceAccess().getLeftSquareBracketKeyword_1_2();
					String implVarName = implVarUtil.getDefaultImplicitVariable().getName();
					inlays.add(inlayHintBefore(f, leftBracket, implVarName + " " + typeInfo(implicitType, implicitMulti), null));
				}
			} else {
				for (ClosureParameter param: f.getParameters()) {
					RType paramType = types.getRType(param);
					boolean paramMulti = card.isMulti(param);
					if (paramType != null) {
						inlays.add(inlayHintAfter(param, typeInfo(paramType, paramMulti), null));
					}
				}
			}
			
			RType outputType = types.getRType(op);
			boolean outputMulti = card.isMulti(op);

			if (outputType != null) {
				inlays.add(inlayHintAfter(op, typeInfo(outputType, outputMulti), null));
			}
			return inlays;
		}
		return null;
	}
	
	@InlayHintCheck
	public InlayHint checkRule(RosettaBlueprint rule) {
		TypedBPNode node;
		try {
			node = blueprintTypes.buildTypeGraph(rule.getNodes(), rule.getOutput());
		} catch (BlueprintUnresolvedTypeException e) {
			node = null;
		}
		if (node != null) {
			boolean isMulti = false;
			if (node.cardinality != null && node.cardinality.length >= 1 && node.cardinality[0] != null) {
				switch(node.cardinality[0]) {
					case UNCHANGED: {
						break;
					}
					case EXPAND: {
						isMulti = true;
						break;
					}
					case REDUCE: {
						isMulti = false;
						break;
					}
				}
			}
			while (node.next != null) {
				node = node.next;
				if (node.cardinality != null && node.cardinality.length >= 1 && node.cardinality[0] != null) {
					switch(node.cardinality[0]) {
						case UNCHANGED: {
							break;
						}
						case EXPAND: {
							isMulti = true;
							break;
						}
						case REDUCE: {
							isMulti = false;
							break;
						}
					}
				}
			}
			BindableType type = node.output;
			if (type != null) {
				Keyword colon = grammar.getRosettaBlueprintAccess().getColonKeyword_3();
				// @Compat: checking this may be removed once colons are mandatory
				boolean hasColon = false;
				ICompositeNode n = NodeModelUtils.findActualNodeFor(rule);
				for (INode child: n.getChildren()) {
					EObject elem = child.getGrammarElement();
					if (elem.equals(colon)) {
						hasColon = true;
						break;
					}
				}
				String label = "output " + typeInfo(type, isMulti);
				if (hasColon) {
					return inlayHintAfter(rule, colon, label, null);
				} else {
					return inlayHintAfter(rule, RosettaPackage.Literals.ROSETTA_NAMED__NAME, label, null);
				}
			}
		}
		return null;
	}
}
