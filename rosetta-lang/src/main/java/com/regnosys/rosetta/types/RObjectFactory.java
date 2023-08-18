package com.regnosys.rosetta.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.RosettaExtensions;
import com.regnosys.rosetta.rosetta.RosettaBlueprint;
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.RosettaRuleReference;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.validation.RosettaBlueprintTypeResolver;
import com.regnosys.rosetta.validation.RosettaBlueprintTypeResolver.BlueprintUnresolvedTypeException;
import com.regnosys.rosetta.validation.TypedBPNode;
import com.rosetta.util.DottedPath;

public class RObjectFactory {
	@Inject
	private RosettaTypeProvider rosettaTypeProvider;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private RosettaBlueprintTypeResolver bpTypeResolver;
	@Inject
	private RBuiltinTypeService builtins;
	@Inject
	private RosettaExtensions rosettaExtensions;

	public RFunction buildRFunction(Function function) {
		return new RFunction(function.getName(), DottedPath.splitOnDots(function.getModel().getName()),
				function.getDefinition(),
				function.getInputs().stream().map(i -> buildRAttribute(i)).collect(Collectors.toList()),
				buildRAttribute(function.getOutput()),
				RFunctionOrigin.FUNCTION,
				function.getConditions(), function.getPostConditions(),
				function.getShortcuts().stream().map(s -> buildRShortcut(s)).collect(Collectors.toList()),
				function.getOperations().stream().map(o -> buildROperation(o)).collect(Collectors.toList()),
				function.getAnnotations());
	}
	
	public RFunction buildRFunction(RosettaBlueprint rule) {
		RType inputRType, outputRType;
		if (rule.isLegacy()) {
			try {
				TypedBPNode node = bpTypeResolver.buildTypeGraph(rule);
				inputRType = node.input.type.orElse(builtins.ANY);
				outputRType = node.output.type.orElse(builtins.ANY);
			} catch (BlueprintUnresolvedTypeException e) {
				throw new RuntimeException(e);
			}
		} else {
			inputRType = typeSystem.typeCallToRType(rule.getInput());
			outputRType = rosettaTypeProvider.getRType(rule.getExpression());
		}
		RAttribute outputAttribute = new RAttribute("output", null, outputRType, List.of(), cardinalityProvider.isMulti(rule.getExpression()));
		
		return new RFunction(
				rule.getName(), 
				DottedPath.splitOnDots(rule.getModel().getName()),
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
	
	public RFunction buildRFunction(RosettaBlueprintReport report) {
		String reportDefinition = report.getRegulatoryBody().getBody().getName() + " " 
				+ report.getRegulatoryBody().getCorpuses()
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
		
		Map<Attribute, RosettaBlueprint> attributeToRuleMap = rosettaExtensions.getAllReportingRules(report, false)
			.entrySet()
			.stream()
			.collect(Collectors.toMap(e -> e.getKey().getAttr(), e -> e.getValue()));
		
		
		List<ROperation> operations = generateReportOperations(report.getReportType(), attributeToRuleMap, inputAttribute, List.of(outputAttribute));
		
		return new RFunction(
			report.name(),
			DottedPath.splitOnDots(report.getModel().getName()),
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
	
	private List<ROperation> generateReportOperations(Data reportDataType, Map<Attribute, RosettaBlueprint> attributeToRuleMap, Attribute inputAttribute, List<RAttribute> assignPath) {
		List<Attribute> attributes = reportDataType.getAttributes();
		List<ROperation> operations = new ArrayList<>();
		
		for (Attribute attribute : attributes) {
			if (attributeToRuleMap.containsKey(attribute)) {
				operations.add(generateOperationForRuleReference(inputAttribute, attributeToRuleMap.get(attribute), assignPath));
				continue;
			}
			RAttribute rAttribute = buildRAttribute(attribute);
			if (rAttribute.getRType() instanceof RDataType) {
				RDataType rData = (RDataType) rAttribute.getRType();
				Data data = rData.getData();
				List<RAttribute> newAssignPath = new ArrayList<>(assignPath);
				newAssignPath.add(rAttribute);		
				operations.addAll(generateReportOperations(data, attributeToRuleMap, inputAttribute, newAssignPath));
			}
		}
		return operations;
	}
	
	private ROperation generateOperationForRuleReference(Attribute inputAttribute, RosettaBlueprint rule, List<RAttribute> assignPath) {
		RAttribute pathHead = assignPath.get(0);
		List<RAttribute> pathTail = assignPath.subList(1, assignPath.size());
		
		RosettaSymbolReference inputAttributeSymbolRef = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		inputAttributeSymbolRef.setSymbol(inputAttribute);
		
		RosettaSymbolReference symbolRef = ExpressionFactory.eINSTANCE.createRosettaSymbolReference();
		symbolRef.setSymbol(rule);
		symbolRef.setExplicitArguments(true);
		symbolRef.getArgs().add(inputAttributeSymbolRef);
		
		return new ROperation(ROperationType.SET, pathHead, pathTail, null);
	}

	public RAttribute buildRAttribute(Attribute attribute) {
		RType rType = this.rosettaTypeProvider.getRTypeOfSymbol(attribute);
		List<RAttribute> metaAnnotations = attribute.getAnnotations().stream()
				.filter(a -> a.getAnnotation().getName().equals("metadata")).map(a -> buildRAttribute(a.getAttribute()))
				.collect(Collectors.toList());

		return new RAttribute(attribute.getName(), attribute.getDefinition(), rType, metaAnnotations,
				cardinalityProvider.isMulti(attribute));

	}

	public RShortcut buildRShortcut(ShortcutDeclaration shortcut) {
		return new RShortcut(shortcut.getName(), shortcut.getDefinition(), shortcut.getExpression());

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
