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

package com.regnosys.rosetta.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.*;
import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.cache.caches.RDataTypeCache;
import com.regnosys.rosetta.cache.caches.RFunctionCache;
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.regnosys.rosetta.rules.RuleReferenceService;
import com.regnosys.rosetta.utils.ModelIdProvider;

public class RObjectFactory {
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private ModelIdProvider modelIdProvider;
	@Inject
	private RuleReferenceService ruleService;
	@Inject
	private RDataTypeCache typeCache;
	@Inject
	private RFunctionCache functionCache;
	@Inject
	private RosettaFunctionExtensions funcExt;

	private RosettaScope getScope(RosettaRootElement elem) {
		RosettaModel model = elem.getModel();
		if (model == null) {
			return null;
		}
		return model.getScope();
	}
	public RFunction buildRFunction(Function function) {
		return buildRFunction(function, new HashSet<>());
	}
    private RFunction buildRFunction(Function function, Set<Function> visited) {
        if (function == null || !visited.add(function)) {
            return null;
        }
        return functionCache.get(function, () -> doBuildRFunction(function, visited));
    }
	private RFunction doBuildRFunction(Function function, Set<Function> visited) {
		Function superFunc = function.getSuperFunction();
		return new RFunction(
				function,
				superFunc == null ? null : buildRFunction(superFunc, visited),
				getScope(function),
				modelIdProvider.getSymbolId(function),
				function.getDefinition(),
				funcExt.getInputs(function).stream().map(i -> buildRAttributeWithEnclosingType(null, i)).collect(Collectors.toList()),
				buildRAttributeWithEnclosingType(null, funcExt.getOutput(function)),
				RFunctionOrigin.FUNCTION,
				function.getConditions(), function.getPostConditions(),
				function.getShortcuts().stream().map(this::buildRShortcut).collect(Collectors.toList()),
				function.getOperations().stream().map(this::buildROperation).collect(Collectors.toList()),
				function.getAnnotations());
	}
	
	private RAttribute createArtificialAttribute(String name, RType type, boolean isMulti) {
		RMetaAnnotatedType rAnnotatedType = RMetaAnnotatedType.withNoMeta(type);
		return new RAttribute(null, false, name, null, Collections.emptyList(), rAnnotatedType, isMulti ? RCardinality.UNBOUNDED : RCardinality.OPTIONAL, null, Collections.emptyList(), null);
	}
	public RFunction buildRFunction(RosettaRule rule) {		
		RType inputRType = typeSystem.getRuleInputType(rule);
		RType outputRType = typeProvider.getRMetaAnnotatedType(rule.getExpression()).getRType();
		boolean outputIsMulti = cardinalityProvider.isMulti(rule.getExpression());
		RAttribute outputAttribute = createArtificialAttribute("output", outputRType, outputIsMulti);
		
		return new RFunction(
				rule,
				null,
				getScope(rule),
				rule.getName() == null ? null : modelIdProvider.getSymbolId(rule),
				rule.getDefinition(),
				List.of(createArtificialAttribute("input", inputRType, false)),
				outputAttribute,
				RFunctionOrigin.RULE,
				List.of(),
				List.of(),
				List.of(),
				List.of(new ROperation(ROperationType.SET, outputAttribute, List.of(), rule.getExpression())),
				List.of()
			);
	}
	
	public RFunction buildRFunction(RosettaReport report) {
		String reportDefinition = report.getRegulatoryBody().getBody().getName() + " " 
				+ report.getRegulatoryBody().getCorpusList()
				.stream()
				.map(RosettaNamed::getName)
				.collect(Collectors.joining(" "));
		
		RDataType outputRtype = buildRDataType(report.getReportType());
		RAttribute outputAttribute = createArtificialAttribute("output", outputRtype, false);
		
		Attribute inputAttribute = SimpleFactory.eINSTANCE.createAttribute();
		inputAttribute.setName("input");
		inputAttribute.setTypeCall(EcoreUtil2.copy(report.getInputType()));
		RosettaCardinality cardinality =  RosettaFactory.eINSTANCE.createRosettaCardinality();
		cardinality.setInf(0);
		cardinality.setSup(1);
		inputAttribute.setCard(cardinality);
		
		List<ROperation> operations = generateOperations(report, outputAttribute, outputRtype, inputAttribute);
		return new RFunction(
			report,
			null,
			getScope(report),
			modelIdProvider.getReportId(report),
			reportDefinition,
			List.of(buildRAttributeWithEnclosingType(null, inputAttribute)),
			outputAttribute,
			RFunctionOrigin.REPORT,
			List.of(),
			List.of(),
			List.of(),
			operations,
			List.of()
		);
	}
	
	private List<ROperation> generateOperations(RosettaReport report, RAttribute outputAttribute, RDataType reportType, Attribute inputAttribute) {
		return ruleService.<Map<List<RAttribute>, RosettaRule>>traverse(
					report.getRuleSource(),
					reportType,
					new LinkedHashMap<>(),
					(acc, context) -> {
						if (!context.isExplicitlyEmpty()) {
							acc.put(context.getPath(), context.getRule());
						}
						return acc;
					}
				).entrySet().stream()
					.map(e -> generateOperationForRuleReference(inputAttribute, outputAttribute, e.getValue(), e.getKey()))
					.collect(Collectors.toList());
	}
	
	private ROperation generateOperationForRuleReference(Attribute inputAttribute, RAttribute outputAttribute, RosettaRule rule, List<? extends RFeature> assignPath) {
		RosettaSymbolReference inputAttributeSymbolRef = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		inputAttributeSymbolRef.setSymbol(inputAttribute);
		
		RosettaSymbolReference symbolRef = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		symbolRef.setGenerated(true);
		symbolRef.setSymbol(rule);
		symbolRef.setExplicitArguments(true);
		symbolRef.getArgs().add(inputAttributeSymbolRef);
		
		return new ROperation(ROperationType.SET, outputAttribute, assignPath, symbolRef);
	}

	public RAttribute buildRAttribute(Attribute attr) {
		RDataType enclosingType = null;
		EObject container = attr.eContainer();
		if (container instanceof Data) {
			enclosingType = buildRDataType((Data) container);
		}
		return buildRAttributeWithEnclosingType(enclosingType, attr);
	}
	public RAttribute buildRAttributeWithEnclosingType(RDataType enclosingType, Attribute attr) {
		RMetaAnnotatedType rAnnotatedType = typeProvider.getRTypeOfFeature(attr, null);
		RCardinality card = buildRCardinality(attr.getCard());

		return new RAttribute(enclosingType, attr.isOverride(), attr.getName(), attr.getDefinition(), attr.getReferences(), rAnnotatedType,
				card, attr.getRuleReferences(), attr.getLabels(), attr);
	}
	public RCardinality buildRCardinality(RosettaCardinality card) {
		if (card == null) {
			return RCardinality.OPTIONAL;
		}
		if (card.isUnbounded()) {
			if (card.getInf() == 0) {
				return RCardinality.UNBOUNDED;
			}
			return RCardinality.unbounded(card.getInf());
		}
		if (card.getSup() == 1) {
			if (card.getInf() == 1) {
				return RCardinality.SINGLE;
			} else if (card.getInf() == 0) {
				return RCardinality.OPTIONAL;
			}
		}
		return RCardinality.bounded(card.getInf(), card.getSup());
	}

	public RShortcut buildRShortcut(ShortcutDeclaration shortcut) {
		return new RShortcut(shortcut.getName(), cardinalityProvider.isSymbolMulti(shortcut), shortcut.getDefinition(), shortcut.getExpression(), shortcut.getFunction(), this);

	}

	public ROperation buildROperation(Operation operation) {
		ROperationType operationType = operation.isAdd() ? ROperationType.ADD : ROperationType.SET;
		RAssignedRoot pathHead;

		if (operation.getAssignRoot() instanceof Attribute) {
			pathHead = buildRAttributeWithEnclosingType(null, (Attribute) operation.getAssignRoot());
		} else {
			pathHead = buildRShortcut((ShortcutDeclaration) operation.getAssignRoot());
		}

		List<RFeature> pathTail = operation.pathAsSegmentList()
				.stream()
				.map(s -> {
					RosettaFeature feature = s.getFeature();
					if (feature instanceof Attribute) {
						return buildRAttribute((Attribute) feature);
					}
					if (feature instanceof RosettaMetaType) {
						return buildRMetaAttribute((RosettaMetaType) feature);
					}
					return null;
					})
				.collect(Collectors.toList());

		return new ROperation(operationType, pathHead, pathTail, operation.getExpression());
	}

	public RDataType buildRDataType(Data data) {
		return typeCache.get(data, () -> new RDataType(data, modelIdProvider, this, typeProvider));
	}
	public RChoiceType buildRChoiceType(Choice choice) {
		return new RChoiceType(choice, modelIdProvider, typeProvider, this);
	}
	public REnumType buildREnumType(RosettaEnumeration enumeration) {
		return new REnumType(enumeration, modelIdProvider, this);
	}
	public RMetaAttribute buildRMetaAttribute(RosettaMetaType rosettaMetaType) {
		return new RMetaAttribute(rosettaMetaType.getName(), typeSystem.typeCallToRType(rosettaMetaType.getTypeCall()));
	}
}
