package com.regnosys.rosetta.types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.regnosys.rosetta.rosetta.RosettaBlueprint;
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.RosettaRuleReference;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
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
		
		RType inputRtype = typeSystem.typeCallToRType(report.getInputType());
		RType outputRtype = new RDataType(report.getReportType());
		RAttribute outputAttribute = new RAttribute("output", null, outputRtype, List.of(), false);
		
		
		List<ROperation> operations = generateReportOperations(report.getReportType(), List.of(), List.of(outputAttribute));
		
		return new RFunction(
			report.name(),
			DottedPath.splitOnDots(report.getModel().getName()),
			reportDefinition,
			List.of(new RAttribute("input", null, inputRtype, List.of(), false)),
			outputAttribute,
			RFunctionOrigin.REPORT,
			List.of(),
			List.of(),
			List.of(),
			operations,
			List.of()
		);
	}
	
	private List<ROperation> generateReportOperations(Data reportDataType, List<ROperation> acc, List<RAttribute> assignPath) {
		List<Attribute> attributes = reportDataType.getAttributes();
		List<ROperation> operations = new ArrayList<>(acc);
		
		for (Attribute attribute : attributes) {
			if (attribute.getRuleReference() != null) {
				operations.add(genreteOperationForRuleReference(attribute, assignPath));
			}
			
			
		}
		return operations;
	}
	
	private ROperation genreteOperationForRuleReference(Attribute attributeWithReference, List<RAttribute> assignPath) {
		RosettaRuleReference ruleReference = attributeWithReference.getRuleReference();
		return null;
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
