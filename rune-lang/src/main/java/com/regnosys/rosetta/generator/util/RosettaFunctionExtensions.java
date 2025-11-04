package com.regnosys.rosetta.generator.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.RosettaModel;
import org.eclipse.xtext.EcoreUtil2;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaTyped;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.AnnotationRef;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.Function;
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch;
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.RShortcut;
import com.regnosys.rosetta.types.RosettaTypeProvider;

import jakarta.inject.Inject;

public class RosettaFunctionExtensions {
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	@Inject
	private RosettaTypeProvider typeProvider;
	
	/** 
	 * 
	 * spec functions do not have operation hence, do not provide an implementation
	 */
	public boolean handleAsSpecFunction(Function function) {
		return function.getOperations().isEmpty() && !isDispatchingFunction(function) && !handleAsEnumFunction(function);
	}

	public boolean handleAsEnumFunction(Function function) {
		return function.getOperations().isEmpty() && !Iterables.isEmpty(getDispatchingFunctions(function));
	}

	public boolean isDispatchingFunction(Function function) {
		return function instanceof FunctionDispatch;
	}

	public Iterable<FunctionDispatch> getDispatchingFunctions(Function function) {
		// TODO Look-up other Rosetta files?
		return Iterables.filter(EcoreUtil2.getSiblingsOfType(function, FunctionDispatch.class), f -> f.getName().equals(function.getName()));
	}

	public Function getMainFunction(Function function) {
		// TODO Look-up other Rosetta files?
		if (isDispatchingFunction(function)) {
			return Iterables.getFirst(Iterables.filter(EcoreUtil2.getSiblingsOfType(function, Function.class),
				f -> f.getName().equals(function.getName()) && f.getOperations().isEmpty()
			), null);
		}
		return null;
	}

	public Attribute getOutput(Function function) {
		var mainFunction = getMainFunction(function);
		if (mainFunction == null) {
			mainFunction = function;
		}
		return mainFunction.getOutput();
	}
	public List<Attribute> getInputs(Function function) {
		var mainFunction = getMainFunction(function);
		if (mainFunction == null) {
			mainFunction = function;
		}
		return mainFunction.getInputs();
	}

	public String inputsAsArgs(ShortcutDeclaration alias) {
		var func = EcoreUtil2.getContainerOfType(alias, Function.class);
		return getInputs(func).stream().map(Attribute::getName).collect(Collectors.joining(", "));
	}

	public boolean needsBuilder(Object ele) {
		if (ele instanceof Void) {
			return false;
		} else if (ele instanceof RAttribute attr) {
			return needsBuilder(attr.getRMetaAnnotatedType().getRType());
		} else if (ele instanceof RosettaTyped typed) {
			return needsBuilder(typed.getTypeCall().getType());
		} else if (ele instanceof RosettaExpression expr) {
			return needsBuilder(typeProvider.getRMetaAnnotatedType(expr).getRType());
		} else if (ele instanceof ShortcutDeclaration alias) {
			return needsBuilder(alias.getExpression());
		} else if (ele instanceof RShortcut alias) {
			return needsBuilder(alias.getExpression());
		} else if (ele instanceof Data || ele instanceof RDataType || ele instanceof RChoiceType) {
			return true;
		} else if (ele instanceof TypeCall tc) {
			return needsBuilder(tc.getType());
		}
		return false;
	}

	public boolean isOutput(Attribute attr) {
		return attr.eContainingFeature() == SimplePackage.Literals.FUNCTION__OUTPUT;
	}
	
	public boolean isQualifierFunctionFor(Function function, Data type) {
		return isQualifierFunction(function) && getInputs(function).get(0).getTypeCall().getType().equals(type);
	}
	
	public boolean isQualifierFunction(Function function) {
		return !getQualifierAnnotations(function).isEmpty();
	}
	
	public boolean isQualifierFunction(RFunction function) {
		return !getQualifierAnnotations(function.getAnnotations()).isEmpty();
	}
	
	public List<AnnotationRef> getMetadataAnnotations(Annotated element) {
		return element.getAnnotations().stream().filter(it -> "metadata".equals(it.getAnnotation().getName())).toList();
	}
	
	public List<AnnotationRef> getQualifierAnnotations(Annotated element) {
		return getQualifierAnnotations(element.getAnnotations());
	}
	
	public List<AnnotationRef> getQualifierAnnotations(List<AnnotationRef> annotations) {
        if(annotations == null || annotations.isEmpty()) {
            return List.of();
        }
		return annotations.stream().filter(it -> "qualification".equals(it.getAnnotation().getName())).toList();
	}
	
	public List<AnnotationRef> getTransformAnnotations(Annotated element) {
        if(element.getAnnotations() == null || element.getAnnotations().isEmpty()) {
            return List.of();
        }
		return element.getAnnotations().stream()
			.filter(ecoreUtil::isResolved)
			.filter(it -> {
                RosettaModel model = it.getAnnotation().getModel();
                if (model == null) {
                    return false;
                }
                return "com.rosetta.model".equals(model.getName());
            })
			.filter(it -> Stream.of("ingest", "enrich", "projection").anyMatch(transformName -> transformName.equals(it.getAnnotation().getName())))
			.toList();
	}
	
	public List<AnnotationRef> getCreationAnnotations(Annotated element) {
		return element.getAnnotations().stream()
			.filter(it -> "creation".equals(it.getAnnotation().getName()))
			.toList();
	}
}
