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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.regnosys.rosetta.rosetta.translate.Translation;
import com.regnosys.rosetta.rosetta.translate.TranslationParameter;
import com.regnosys.rosetta.utils.TranslateUtil;
import com.rosetta.model.lib.ModelReportId;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public class RObjectFactory {
	@Inject
	private RosettaTypeProvider typeProvider;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private RosettaExtensions rosettaExtensions;
	@Inject
	private TranslateUtil translateUtil;

	public RFunction buildRFunction(Function function) {
		return new RFunction(
				new ModelSymbolId(DottedPath.splitOnDots(function.getModel().getName()), function.getName()),
				function.getDefinition(),
				function.getInputs().stream().map(i -> buildRAttribute(i)).collect(Collectors.toList()),
				buildRAttribute(function.getOutput()),
				RFunctionOrigin.FUNCTION,
				function.getConditions(), function.getPostConditions(),
				function.getShortcuts().stream().map(s -> buildRShortcut(s)).collect(Collectors.toList()),
				function.getOperations().stream().map(o -> buildROperation(o)).collect(Collectors.toList()),
				function.getAnnotations());
	}
	
	public RFunction buildRFunction(RosettaRule rule) {		
		RType inputRType = typeSystem.typeCallToRType(rule.getInput());
		RType outputRType = typeProvider.getRType(rule.getExpression());
		boolean outputIsMulti = cardinalityProvider.isMulti(rule.getExpression());
		RAttribute outputAttribute = new RAttribute("output", null, outputRType, List.of(), outputIsMulti);
		
		return new RFunction(
				new ModelSymbolId(DottedPath.splitOnDots(rule.getModel().getName()), rule.getName()), 
				rule.getDefinition(),
				List.of(new RAttribute("input", null, inputRType, List.of(), false)),
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
		String body = report.getRegulatoryBody().getBody().getName();
		String[] corpusList = report.getRegulatoryBody().getCorpusList().stream().map(c -> c.getName()).toArray(String[]::new);

		String reportDefinition = report.getRegulatoryBody().getBody().getName() + " " 
				+ report.getRegulatoryBody().getCorpusList()
				.stream()
				.map(c -> c.getName())
				.collect(Collectors.joining(" "));
		
		RType outputRtype = new RDataType(report.getReportType());
		RAttribute outputAttribute = new RAttribute("output", null, outputRtype, List.of(), false);
		
		Attribute inputAttribute = SimpleFactory.eINSTANCE.createAttribute();
		inputAttribute.setName("input");
		inputAttribute.setTypeCall(EcoreUtil2.copy(report.getInputType()));
		RosettaCardinality cardinality =  RosettaFactory.eINSTANCE.createRosettaCardinality();
		cardinality.setInf(0);
		cardinality.setSup(1);
		inputAttribute.setCard(cardinality);
		
		Map<Attribute, RosettaRule> attributeToRuleMap = rosettaExtensions.getAllReportingRules(report)
			.entrySet()
			.stream()
			.collect(Collectors.toMap(e -> e.getKey().getAttr(), e -> e.getValue()));
		
		List<ROperation> operations = generateReportOperations(report.getReportType(), attributeToRuleMap, inputAttribute, List.of(outputAttribute));
		return new RFunction(
			new ModelReportId(DottedPath.splitOnDots(report.getModel().getName()), body, corpusList),
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
	
	private List<ROperation> generateReportOperations(Data reportDataType, Map<Attribute, RosettaRule> attributeToRuleMap, Attribute inputAttribute, List<RAttribute> assignPath) {
		Iterable<Attribute> attributes = rosettaExtensions.getAllAttributes(reportDataType);
		List<ROperation> operations = new ArrayList<>();
		
		for (Attribute attribute : attributes) {
			RAttribute rAttribute = buildRAttribute(attribute);
			List<RAttribute> newAssignPath = new ArrayList<>(assignPath);
			newAssignPath.add(rAttribute);
			if (attributeToRuleMap.containsKey(attribute)) {
				operations.add(generateOperationForRuleReference(inputAttribute, attributeToRuleMap.get(attribute), newAssignPath));
				continue;
			}
			if (rAttribute.getRType() instanceof RDataType) {
				RDataType rData = (RDataType) rAttribute.getRType();
				Data data = rData.getData();		
				operations.addAll(generateReportOperations(data, attributeToRuleMap, inputAttribute, newAssignPath));
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
	
	public RFunction buildRFunction(Translation translation) {
		List<RAttribute> inputs = translation.getParameters().stream().<RAttribute>map(this::buildRAttribute).collect(Collectors.toList());
		RType outputRType = translateUtil.getResultRType(translation);
		RAttribute outputAttribute = new RAttribute("output", null, outputRType, List.of(), false);
		ROperation operation = new ROperation(ROperationType.SET, outputAttribute, List.of(), translation.getExpression());
		
		return new RFunction(
				translateUtil.toTranslationId(translation), 
				null,
				inputs,
				outputAttribute,
				RFunctionOrigin.TRANSLATION,
				List.of(),
				List.of(),
				List.of(),
				List.of(operation),
				List.of()
			);
	}

	public RAttribute buildRAttribute(Attribute attribute) {
		RType rType = typeProvider.getRTypeOfSymbol(attribute);
		List<RAttribute> metaAnnotations = attribute.getAnnotations().stream()
				.filter(a -> a.getAnnotation().getName().equals("metadata")).map(a -> buildRAttribute(a.getAttribute()))
				.collect(Collectors.toList());

		return new RAttribute(attribute.getName(), attribute.getDefinition(), rType, metaAnnotations,
				cardinalityProvider.isSymbolMulti(attribute));

	}
	
	public RAttribute buildRAttribute(TranslationParameter parameter) {
		RType rType = typeProvider.getRTypeOfSymbol(parameter);
		String name = parameter.getName();
		if (name == null) {
			name = "item";
		}
		return new RAttribute(name, null, rType, Collections.emptyList(), false);

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

}
