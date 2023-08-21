package com.regnosys.rosetta.types;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;

import com.regnosys.rosetta.rosetta.RosettaBlueprint;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.Operation;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.validation.BindableType;
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
		boolean outputIsMulti = false;
		if (rule.isLegacy()) {
			try {
				TypedBPNode node = bpTypeResolver.buildTypeGraph(rule);
				inputRType = node.input.type.orElse(builtins.ANY);
				if (!node.repeatable) {
					outputRType = node.output.type.orElse(builtins.ANY);
				} else {
					BindableType outputTypeRef = new BindableType();
					if (rule.getModel() != null) {
						EcoreUtil2.findCrossReferences(rule.getModel(), Collections.singleton(rule), (EObject referrer, EObject referenced, EReference reference, int index) -> {
							if (!outputTypeRef.isBound()) {
								if (reference == SimplePackage.eINSTANCE.getRosettaRuleReference_ReportingRule()) {
									EObject refContainer = referrer.eContainer();
									if (refContainer instanceof Attribute) {
										outputTypeRef.type = Optional.of(rosettaTypeProvider.getRTypeOfSymbol((Attribute)refContainer));
									}
								}
							}
						});
					}
					if (!outputTypeRef.isBound() && rule.eResource() != null && rule.eResource().getResourceSet() != null) {
						ResourceSet resourceSet = rule.eResource().getResourceSet();
						outer:
						for (Resource r : resourceSet.getResources()) {
							for (EObject root : r.getContents()) {
								EcoreUtil2.findCrossReferences(root, Collections.singleton(rule), (EObject referrer, EObject referenced, EReference reference, int index) -> {
									if (!outputTypeRef.isBound()) {
										if (reference == SimplePackage.eINSTANCE.getRosettaRuleReference_ReportingRule()) {
											EObject refContainer = referrer.eContainer();
											if (refContainer instanceof Attribute) {
												outputTypeRef.type = Optional.of(rosettaTypeProvider.getRTypeOfSymbol((Attribute)refContainer));
											}
										}
									}
								});
								if (outputTypeRef.isBound()) {
									break outer;
								}
							}
						}
					}
					outputRType = outputTypeRef.type.orElse(builtins.ANY);
				}
				TypedBPNode last = node;
				if (last.cardinality[0] != null) {
					switch (last.cardinality[0]) {
						case EXPAND: {
							outputIsMulti = true;
							break;
						}
						case REDUCE: {
							outputIsMulti = false;
							break;
						}
						default:
							outputIsMulti = false;
							break;
					}
				}
				while (last.next != null) {
					last = last.next;
					if (last.cardinality[0] != null) {
						switch (last.cardinality[0]) {
							case EXPAND: {
								outputIsMulti = true;
								break;
							}
							case REDUCE: {
								outputIsMulti = false;
								break;
							}
							default:
								break;
						}
					}
				}
			} catch (BlueprintUnresolvedTypeException e) {
				throw new RuntimeException(e);
			}
		} else {
			inputRType = typeSystem.typeCallToRType(rule.getInput());
			outputRType = rosettaTypeProvider.getRType(rule.getExpression());
			outputIsMulti = cardinalityProvider.isMulti(rule.getExpression());
		}
		RAttribute outputAttribute = new RAttribute("output", null, outputRType, List.of(), outputIsMulti);
		
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
