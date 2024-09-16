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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaMetaType;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.RosettaRuleReference;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.regnosys.rosetta.utils.ExternalAnnotationUtil;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.regnosys.rosetta.utils.PositiveIntegerInterval;

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
	private ExternalAnnotationUtil externalAnnotationUtil;

	public RFunction buildRFunction(Function function) {
		return new RFunction(
				modelIdProvider.getSymbolId(function),
				function.getDefinition(),
				function.getInputs().stream().map(i -> buildRAttribute(i)).collect(Collectors.toList()),
				buildRAttribute(function.getOutput()),
				RFunctionOrigin.FUNCTION,
				function.getConditions(), function.getPostConditions(),
				function.getShortcuts().stream().map(s -> buildRShortcut(s)).collect(Collectors.toList()),
				function.getOperations().stream().map(o -> buildROperation(o)).collect(Collectors.toList()),
				function.getAnnotations());
	}
	
	// TODO: should be private
	public RAttribute createArtificialAttribute(String name, RType type, boolean isMulti) {
		return new RAttribute(name, null, Collections.emptyList(), type, List.of(), isMulti ? PositiveIntegerInterval.boundedLeft(0) : PositiveIntegerInterval.bounded(0, 1), null, null);
	}
	public RFunction buildRFunction(RosettaRule rule) {		
		RType inputRType = typeSystem.typeCallToRType(rule.getInput());
		RType outputRType = typeProvider.getRType(rule.getExpression());
		boolean outputIsMulti = cardinalityProvider.isMulti(rule.getExpression());
		RAttribute outputAttribute = createArtificialAttribute("output", outputRType, outputIsMulti);
		
		return new RFunction(
				modelIdProvider.getSymbolId(rule),
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
				.map(c -> c.getName())
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
		
		Map<RAttribute, RosettaRule> attributeToRuleMap = externalAnnotationUtil.getAllReportingRules(report)
			.entrySet()
			.stream()
			.collect(Collectors.toMap(e -> e.getKey().getAttr(), e -> e.getValue()));
		
		List<ROperation> operations = generateReportOperations(outputRtype, attributeToRuleMap, inputAttribute, List.of(outputAttribute));
		return new RFunction(
			modelIdProvider.getReportId(report),
			reportDefinition,
			List.of(buildRAttribute(inputAttribute)),
			outputAttribute,
			RFunctionOrigin.REPORT,
			List.of(),
			List.of(),
			List.of(),
			operations,
			List.of()
		);
	}
	
	private List<ROperation> generateReportOperations(RDataType reportDataType, Map<RAttribute, RosettaRule> attributeToRuleMap, Attribute inputAttribute, List<RAttribute> assignPath) {
		Collection<RAttribute> attributes = reportDataType.getAllNonOverridenAttributes();
		List<ROperation> operations = new ArrayList<>();
		
		for (RAttribute attribute : attributes) {
			List<RAttribute> newAssignPath = new ArrayList<>(assignPath);
			newAssignPath.add(attribute);
			if (attributeToRuleMap.containsKey(attribute)) {
				operations.add(generateOperationForRuleReference(inputAttribute, attributeToRuleMap.get(attribute), newAssignPath));
				continue;
			}
			if (attribute.getRType() instanceof RDataType) {
				RDataType rData = (RDataType) attribute.getRType();
				operations.addAll(generateReportOperations(rData, attributeToRuleMap, inputAttribute, newAssignPath));
			}
		}
		return operations;
	}
	
	private ROperation generateOperationForRuleReference(Attribute inputAttribute, RosettaRule rule, List<RAttribute> assignPath) {
		RAttribute pathHead = assignPath.get(0);
		List<RAttribute> pathTail = assignPath.subList(1, assignPath.size());
		
		RosettaSymbolReference inputAttributeSymbolRef = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		inputAttributeSymbolRef.setSymbol(inputAttribute);
		
		RosettaSymbolReference symbolRef = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		symbolRef.setGenerated(true);
		symbolRef.setSymbol(rule);
		symbolRef.setExplicitArguments(true);
		symbolRef.getArgs().add(inputAttributeSymbolRef);
		
		return new ROperation(ROperationType.SET, pathHead, pathTail, symbolRef);
	}

	public RAttribute buildRAttribute(Attribute attribute) {
		return buildRAttribute(attribute, attribute.getTypeCall().getType() instanceof RosettaMetaType);
	}
	private RAttribute buildRAttribute(Attribute attribute, boolean isMeta) {
		RType rType = typeProvider.getRTypeOfSymbol(attribute);
		List<RAttribute> metaAnnotations = attribute.getAnnotations().stream()
				.filter(a -> a.getAnnotation().getName().equals("metadata") && a.getAttribute() != null).map(a -> buildRAttribute(a.getAttribute(), true))
				.collect(Collectors.toList());
		PositiveIntegerInterval card = new PositiveIntegerInterval(
				attribute.getCard().getInf(),
				attribute.getCard().isUnbounded() ? Optional.empty() : Optional.of(attribute.getCard().getSup()));
		RosettaRuleReference ruleRef = attribute.getRuleReference();

		return new RAttribute(attribute.getName(), attribute.getDefinition(), attribute.getReferences(), rType, metaAnnotations,
				card, isMeta, ruleRef != null ? ruleRef.getReportingRule() : null, attribute);
	}

	public RShortcut buildRShortcut(ShortcutDeclaration shortcut) {
		return new RShortcut(shortcut.getName(), cardinalityProvider.isSymbolMulti(shortcut), shortcut.getDefinition(), shortcut.getExpression());

	}

	public ROperation buildROperation(Operation operation) {
		ROperationType operationType = operation.isAdd() ? ROperationType.ADD : ROperationType.SET;
		RAssignedRoot pathHead;

		if (operation.getAssignRoot() instanceof Attribute) {
			pathHead = buildRAttribute((Attribute) operation.getAssignRoot());
		} else {
			pathHead = buildRShortcut((ShortcutDeclaration) operation.getAssignRoot());
		}

		List<RAttribute> pathTail = operation.pathAsSegmentList().stream().map(s -> buildRAttribute(s.getAttribute()))
				.collect(Collectors.toList());

		return new ROperation(operationType, pathHead, pathTail, operation.getExpression());
	}

	public RDataType buildRDataType(Data data) {
		return new RDataType(data, modelIdProvider, this);
	}
	// TODO: remove this hack
	public RDataType buildRDataType(Data data, List<RAttribute> additionalAttributes) {
		return new RDataType(data, modelIdProvider, this, additionalAttributes);
	}
	public REnumType buildREnumType(RosettaEnumeration enumeration) {
		return new REnumType(enumeration, modelIdProvider, this);
	}
}
