package com.regnosys.rosetta.types;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.regnosys.rosetta.rosetta.RosettaBlueprint;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.rosetta.util.DottedPath;

public class RTypeBuilderFactory {
	@Inject
	private RosettaTypeProvider rosettaTypeProvider;
	@Inject
	private CardinalityProvider cardinalityProvider;
	@Inject
	private TypeSystem typeSystem;

	public RFunction buildRFunction(Function function) {
		return new RFunction(function.getName(), DottedPath.splitOnDots(function.getModel().getName()),
				function.getDefinition(),
				function.getInputs().stream().map(i -> buildRAttribute(i)).collect(Collectors.toList()),
				buildRAttribute(function.getOutput()), function.getConditions(), function.getPostConditions(),
				function.getShortcuts().stream().map(s -> buildRShortcut(s)).collect(Collectors.toList()),
				function.getOperations().stream().map(o -> buildROperation(o)).collect(Collectors.toList()),
				function.getAnnotations());

	}
	
	public RFunction buildRFunction(RosettaBlueprint rule) {
		RType inputRtype = typeSystem.typeCallToRType(rule.getInput());
		RType outputRtype = rosettaTypeProvider.getRType(rule.getExpression());
		RAttribute outputAttribute = new RAttribute("output", null, outputRtype, List.of(), cardinalityProvider.isMulti(rule.getExpression()));
		
		return new RFunction(
				rule.getName(), 
				DottedPath.splitOnDots(rule.getModel().getName()),
				rule.getDefinition(),
				List.of(new RAttribute("input", null, inputRtype, List.of(), false)),
				outputAttribute,
				List.of(),
				List.of(),
				List.of(),
				List.of(new ROperation(ROperationType.SET, outputAttribute, List.of(), rule.getExpression())),
				List.of()
			);
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
