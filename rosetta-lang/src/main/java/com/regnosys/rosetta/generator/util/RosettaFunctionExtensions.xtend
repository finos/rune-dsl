package com.regnosys.rosetta.generator.util

import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaTyped
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.AssignPathRoot
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.rosetta.simple.SimplePackage
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaTypeProvider
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.TypeCall
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RAssignedRoot
import com.regnosys.rosetta.types.RShortcut
import com.regnosys.rosetta.rosetta.simple.AnnotationRef
import java.util.List
import com.regnosys.rosetta.types.RFunction
import jakarta.inject.Inject
import com.regnosys.rosetta.types.RChoiceType
import com.regnosys.rosetta.RosettaEcoreUtil

class RosettaFunctionExtensions {
	@Inject RosettaEcoreUtil ecoreUtil
	@Inject RosettaTypeProvider typeProvider
	
	/** 
	 * 
	 * spec functions do not have operation hence, do not provide an implementation
	 */
	def Boolean handleAsSpecFunction(Function function) {
		function.operations.nullOrEmpty && !function.isDispatchingFunction() && !function.handleAsEnumFunction
	}

	def Boolean handleAsEnumFunction(Function function) {
		function.operations.nullOrEmpty && !function.dispatchingFunctions.empty
	}

	def Boolean isDispatchingFunction(Function function) {
		(function instanceof FunctionDispatch)
	}

	def getDispatchingFunctions(Function function) {
		// TODO Look-up other Rosetta files?
		EcoreUtil2.getSiblingsOfType(function, FunctionDispatch).filter[it.name == function.name]
	}

	def getMainFunction(Function function) {
		// TODO Look-up other Rosetta files?
		if (function.isDispatchingFunction) {
			return EcoreUtil2.getSiblingsOfType(function, Function).filter [
				it.name == function.name && it.operations.nullOrEmpty
			].head
		}
	}

	dispatch def getOutput(Function function) {
		return function.output
	}

	dispatch def getOutput(FunctionDispatch function) {
		val mainFunction = function.mainFunction
		if (mainFunction !== null) {
			return mainFunction.output
		}
	}

	dispatch def getInputs(Function function) {
		return function.inputs
	}

	dispatch def getInputs(FunctionDispatch function) {
		val mainFunction = function.mainFunction
		if (mainFunction !== null) {
			return mainFunction.inputs
		}
		emptyList
	}

	def inputsAsArgs(ShortcutDeclaration alias) {
		val func = EcoreUtil2.getContainerOfType(alias, Function)
		getInputs(func).join(', ')[name]
	}

	dispatch def boolean needsBuilder(Void ele) {
		false
	}
	
	dispatch def boolean needsBuilder(RAttribute rAttribute) {
		needsBuilder(rAttribute.RMetaAnnotatedType.RType)
	}

	dispatch def boolean needsBuilder(RosettaTyped ele) {
		needsBuilder(ele.typeCall.type)
	}

	dispatch def boolean needsBuilder(RosettaExpression expr) {
		needsBuilder(typeProvider.getRMetaAnnotatedType(expr).RType)
	}

	dispatch def boolean needsBuilder(AssignPathRoot root) {
		switch (root) {
			Attribute: root.typeCall.type.needsBuilder
			ShortcutDeclaration: root.expression.needsBuilder
			default: false
		}
	}
	
	dispatch def boolean needsBuilder(RAssignedRoot root) {
		switch (root) {
			RAttribute: root.RMetaAnnotatedType.RType.needsBuilder
			RShortcut: root.expression.needsBuilder
			default: false
		}
	}

	dispatch def boolean needsBuilder(RosettaType type) {
		switch (type) {
			Data: true
			default: false
		}
	}
	
	dispatch def boolean needsBuilder(TypeCall typeCall) {
		typeCall.type.needsBuilder
	}

	dispatch def boolean needsBuilder(RType type) {
		switch (type) {
			RDataType, RChoiceType: true
			default: false
		}
	}

	def boolean isOutput(Attribute attr) {
		attr.eContainingFeature === SimplePackage.Literals.FUNCTION__OUTPUT
	}
	
	def boolean isQualifierFunctionFor(Function function, Data type) {
		function.isQualifierFunction && getInputs(function).get(0).typeCall.type == type
	}
	
	def boolean isQualifierFunction(Function function) {
		!getQualifierAnnotations(function).empty
	}
	
	def boolean isQualifierFunction(RFunction function) {
		!getQualifierAnnotations(function.annotations).empty
	}
	
	def getMetadataAnnotations(Annotated element) {
		element.annotations.filter["metadata" == it.annotation.name].toList
	}
	
	def getQualifierAnnotations(Annotated element) {
		getQualifierAnnotations(element.annotations)
	}
	
	def getQualifierAnnotations(List<AnnotationRef> annotations) {
		annotations.filter["qualification" == it.annotation.name].toList
	}
	
	def getCreationAnnotations(Annotated element) {
		element.annotations.filter["creation" == it.annotation.name].toList
	}
	
	def getTransformAnnotations(Annotated element) {
		element.annotations
			.filter[ecoreUtil.isResolved(annotation)]
			.filter["com.rosetta.model" == it.annotation.model.name]
			.filter["ingest" == it.annotation.name || "enrich" == it.annotation.name || "projection" == it.annotation.name].toList
	}
}
