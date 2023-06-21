package com.regnosys.rosetta.validation

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.BlueprintExtract
import com.regnosys.rosetta.rosetta.BlueprintFilter
import com.regnosys.rosetta.rosetta.BlueprintLookup
import com.regnosys.rosetta.rosetta.BlueprintNode
import com.regnosys.rosetta.rosetta.BlueprintNodeExp
import com.regnosys.rosetta.rosetta.BlueprintOr
import com.regnosys.rosetta.rosetta.BlueprintRef
import com.regnosys.rosetta.rosetta.BlueprintReturn
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.validation.TypedBPNode.BPCardinality
import java.util.HashSet
import java.util.List
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.RosettaTyped
import com.regnosys.rosetta.rosetta.expression.InlineFunction
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.RosettaSymbol
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.expression.ListLiteral
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.TypeSystem
import java.util.Optional
import com.regnosys.rosetta.utils.OptionalUtil
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.types.CardinalityProvider
import com.regnosys.rosetta.rosetta.RosettaBlueprint

class RosettaBlueprintTypeResolver {
	
	@Inject extension RosettaTypeProvider
	@Inject CardinalityProvider cardinality
	@Inject extension TypeSystem
	@Inject extension RBuiltinTypeService
	@Inject extension RosettaExtensions
	
	static class BlueprintTypeException extends Exception {
		new(String string) {
			super(string);
		}
		
	}

	def TypedBPNode buildTypeGraph(BlueprintNodeExp nodeExp) throws BlueprintUnresolvedTypeException {
		val prevNode = new TypedBPNode // a hypothetical node before this BP
		val nextNode = new TypedBPNode // a hypothetical node after this BP

		val result = new TypedBPNode
		try {
			result.next = bindTypes(nodeExp, prevNode, nextNode, new HashSet)
		}
		catch (BlueprintTypeException ex) {
			throw new BlueprintUnresolvedTypeException(ex.message,nodeExp, BLUEPRINT_NODE_EXP__NODE, RosettaIssueCodes.TYPE_ERROR)
		}
		result.input = prevNode.output
		result.inputKey = prevNode.outputKey
		result.output = nextNode.input
		result.outputKey = nextNode.inputKey
		result.repeatable = result.next.repeatable
		return result
	}
	def TypedBPNode nonLegacyBuildTypeGraph(RosettaExpression expr) throws BlueprintUnresolvedTypeException {
		val prevNode = new TypedBPNode // a hypothetical node before this BP
		val nextNode = new TypedBPNode // a hypothetical node after this BP

		val result = new TypedBPNode
		try {
			result.next = nonLegacyBindTypes(expr, prevNode, nextNode, new HashSet)
		}
		catch (BlueprintTypeException ex) {
			throw new BlueprintUnresolvedTypeException(ex.message, expr, null, RosettaIssueCodes.TYPE_ERROR)
		}
		result.input = prevNode.output
		result.inputKey = prevNode.outputKey
		result.output = nextNode.input
		result.outputKey = nextNode.inputKey
		result.repeatable = result.next.repeatable
		return result
	}

	def TypedBPNode bindTypes(BlueprintNodeExp nodeExp, TypedBPNode parentNode, TypedBPNode outputNode, Set<BlueprintNode> visited) {
		val typedNode = new TypedBPNode
		typedNode.node = nodeExp.node
		typedNode.input = parentNode.output
		typedNode.inputKey = parentNode.outputKey
		
		val nodeFixedTypes = computeExpected(nodeExp)
		link(typedNode, visited)
		bindFixedTypes(typedNode, nodeFixedTypes, nodeExp.node)
		
		// check outputs
		if (nodeExp.next !== null) {
			typedNode.next = nodeExp.next.bindTypes(typedNode, outputNode, visited)
			typedNode.repeatable = typedNode.repeatable || typedNode.next.repeatable
		} else {
			if (!typedNode.output.type.isSubtypeOf(outputNode.input.type)) {
				BlueprintUnresolvedTypeException.error('''output type of node «typedNode.output» does not match required type of «outputNode.input»''', nodeExp.node,
							BLUEPRINT_NODE__NAME, RosettaIssueCodes.TYPE_ERROR)
			}
			// check the terminal types match the expected
			if (!outputNode.input.bound && typedNode.output.bound) {
				outputNode.input.type = typedNode.output.type
			}

			outputNode.inputKey = typedNode.outputKey;
			outputNode.repeatable = typedNode.repeatable
		}
		typedNode
	}
	def TypedBPNode nonLegacyBindTypes(RosettaExpression expr, TypedBPNode parentNode, TypedBPNode outputNode, Set<BlueprintNode> visited) {
		val typedNode = new TypedBPNode
		typedNode.node = null
		typedNode.input = parentNode.output
		typedNode.inputKey = parentNode.outputKey
		
		val nodeFixedTypes = nonLegacyComputeExpected(expr)
		nonLegacyLink(typedNode, visited)
		bindFixedTypes(typedNode, nodeFixedTypes, expr)
		
		// check outputs
		if (!typedNode.output.type.isSubtypeOf(outputNode.input.type)) {
			BlueprintUnresolvedTypeException.error('''output type of node «typedNode.output» does not match required type of «outputNode.input»''', expr,
						null, RosettaIssueCodes.TYPE_ERROR)
		}
		// check the terminal types match the expected
		if (!outputNode.input.bound && typedNode.output.bound) {
			outputNode.input.type = typedNode.output.type
		}

		outputNode.inputKey = typedNode.outputKey;
		outputNode.repeatable = typedNode.repeatable

		typedNode
	}

	/*Get the types that are fixed by the rosetta expressions etc of these nodes
	 * e.g. for an extract node the input and output types are fixed by the rosettaExpression type
	 */
	def TypedBPNode computeExpected(BlueprintNodeExp nodeExp) {
		val node = nodeExp.node
		val result = new TypedBPNode
		// get the input type for this node
		switch (node) {
			BlueprintExtract: {
				// extract defines both the input and output types
				result.input.type = getInput(node.call)
				// and the output type comes from the expression
				result.output.type = getOutput(node.call)
				result.cardinality.set(0, if (cardinality.isMulti(node.call)) BPCardinality.EXPAND else BPCardinality.UNCHANGED)
				result.repeatable = node.repeatable
			}
			BlueprintReturn: {
				// the output type comes from the expression
				result.output.type = getOutput(node.expression)
				result.cardinality.set(0, if(cardinality.isMulti(node.expression)) BPCardinality.EXPAND else BPCardinality.UNCHANGED)
			}
			BlueprintRef: {
				result.output.type = Optional.ofNullable(node.output).map[typeCallToRType]
			}
			BlueprintFilter: {
				if(node.filter!==null) {
					result.input.type = getInput(node.filter)
				}
				result.cardinality.set(0, BPCardinality.UNCHANGED)
			}
			BlueprintLookup : {
				result.output.type = Optional.ofNullable(node.output).map[typeCallToRType]
				result.cardinality.set(0, BPCardinality.UNCHANGED)
			}
			BlueprintOr: {
				result.cardinality.set(0, BPCardinality.EXPAND)
			}
			default: {
				throw new UnsupportedOperationException("Trying to compute inputs of unknown node type " + node.class)
			}
		}
		result
	}
	def TypedBPNode nonLegacyComputeExpected(RosettaExpression expr) {
		val result = new TypedBPNode
		result.input.type = Optional.ofNullable((expr.eContainer as RosettaBlueprint).input?.typeCallToRType)
		result.output.type = Optional.of(expr.RType)
		result.cardinality.set(0, if (cardinality.isMulti(expr)) BPCardinality.EXPAND else BPCardinality.UNCHANGED)
		result.repeatable = false

		result
	}

	//link the object refs forward through types that are unchanged by this type of node
	def void link(TypedBPNode tNode, Set<BlueprintNode> visited) {
		val node = tNode.node
		/*if (visited.contains(node)) {
			return
		}*/
		visited.add(node)
		switch (node) {
			BlueprintExtract, BlueprintReturn: {
				// extract doesn't change the key
				tNode.outputKey = tNode.inputKey
			}
			BlueprintLookup: {
				tNode.outputKey = tNode.inputKey
			}
			BlueprintOr: {

				// if all the inner nodes are producing the same output then the output of the and node is that output
				var allPassThroughInput = true
				var allPassThroughKey = true
				val outputs = newArrayList
				val inputs = newArrayList
				for (child : node.bps) {
					val bpIn = new TypedBPNode
					val bpOut = new TypedBPNode
					bpIn.output = tNode.input
					bpIn.outputKey = tNode.inputKey
					tNode.orNodes.add(bindTypes(child, bpIn, bpOut, visited))

					if (bpIn.output !== bpOut.input) {
						allPassThroughInput = false
					}
					if (bpIn.outputKey !== bpOut.inputKey) {
						allPassThroughInput = false
					}
					outputs.add(bpOut.invert)
					inputs.add(bpIn.invert)
				}
				if (allPassThroughInput) {
					tNode.output = tNode.input
				}
				if (allPassThroughKey) {
					tNode.outputKey = tNode.inputKey
				}

				getUnionType(outputs, tNode)
				getBaseType(inputs, tNode, node)
			}
			BlueprintFilter: {
				tNode.output = tNode.input
				tNode.outputKey = tNode.inputKey
				if (node.filterBP!==null) {
					val bpIn = new TypedBPNode
					val bpOut = new TypedBPNode
					bpIn.output = tNode.input;
					bpIn.inputKey = tNode.inputKey;
					bpOut.input.type = Optional.of(BOOLEAN)
					bpOut.inputKey = tNode.inputKey
					bpIn.outputKey = tNode.inputKey
					val calledBlueprint = node.filterBP.blueprint
					try {
						if (calledBlueprint.isLegacy) {
							bindTypes(calledBlueprint.nodes, bpIn, bpOut, visited)
						} else {
							nonLegacyBindTypes(calledBlueprint.expression, bpIn, bpOut, visited)
						}
					}
					catch (BlueprintUnresolvedTypeException e) {
					//we found an that the types don't match further down the stack - we want to report it as an error with this call
						BlueprintUnresolvedTypeException.error(e.message,
						node.filterBP, BLUEPRINT_REF__BLUEPRINT, e.code)
					}
					tNode.orNodes.add(TypedBPNode.combine(bpOut, bpIn).invert)
				}
			}
			BlueprintRef: {
				val bpIn = new TypedBPNode
				val bpOut = new TypedBPNode
				bpIn.output = tNode.input
				bpIn.outputKey = tNode.inputKey
				bpOut.input = tNode.output
				bpOut.inputKey = tNode.outputKey

				try {
					val child = if (node.blueprint.isLegacy) {
						bindTypes(node.blueprint.nodes, bpIn, bpOut, visited)
					} else {
						nonLegacyBindTypes(node.blueprint.expression, bpIn, bpOut, visited)
					}
					tNode.cardinality = child.cardinality
				}
				catch (BlueprintUnresolvedTypeException e) {
					//we found an that the types don't match further down the stack - we want to report it as an error with this call
					BlueprintUnresolvedTypeException.error(e.message,
					node, BLUEPRINT_REF__BLUEPRINT, e.code)
				}
				tNode.output = bpOut.input
				tNode.outputKey = bpOut.inputKey
			}
			default: {
				throw new UnsupportedOperationException("Trying to compute linkages of unknown node type " + node.class)
			}
		}
	}
	def void nonLegacyLink(TypedBPNode tNode, Set<BlueprintNode> visited) {
		tNode.outputKey = tNode.inputKey
	}

/**
 * Computes the tightest possible type for the output types of a list of nodes
 * and sets the output node of tNode to that type
 */
	private def getUnionType(List<TypedBPNode> types, TypedBPNode tNode) {
		val joinedOutput = types
			.map[output.type]
			.filter[present]
			.map[get]
			.join
		if (joinedOutput == NOTHING) {
			tNode.output.type = Optional.empty
		} else {
			tNode.output.type = Optional.of(joinedOutput)
		}
		val joinedOutputKey = types
			.map[outputKey.type]
			.filter[present]
			.map[get]
			.join
		if (joinedOutputKey == NOTHING) {
			tNode.outputKey.type = Optional.empty
		} else {
			tNode.outputKey.type = Optional.of(joinedOutputKey)
		}
	}

	def getBaseType(List<TypedBPNode> types, TypedBPNode tNode, BlueprintNode node) {
		val inputTypes = types
			.map[input.type]
			.filter[present]
			.map[get]
		val inputType = inputTypes.meet
		if (!inputTypes.empty && inputType == ANY) {
			BlueprintUnresolvedTypeException.error('''input types of orNode «inputTypes.map[name]» are not compatible''', node, BLUEPRINT_NODE__INPUT,
				RosettaIssueCodes.TYPE_ERROR)
		} else {
			if (inputType == ANY) {
				tNode.input.type = Optional.empty
			} else {
				tNode.input.type = Optional.of(inputType)
			}
		}
		
		val inputKeyTypes = types
			.map[inputKey.type]
			.filter[present]
			.map[get]
		val inputKeyType = inputKeyTypes.meet
		if (!inputKeyTypes.empty && inputKeyType == ANY) {
			BlueprintUnresolvedTypeException.error('''inputKey types of orNode «inputKeyTypes.map[name]» are not compatible''', node,
				BLUEPRINT_NODE__INPUT_KEY, RosettaIssueCodes.TYPE_ERROR)
		} else {
			if (inputKeyType == ANY) {
				tNode.inputKey.type = Optional.empty
			} else {
				tNode.inputKey.type = Optional.of(inputKeyType)
			}
		}
	}

	def bindFixedTypes(TypedBPNode node, TypedBPNode expected, EObject bpNode) {
		node.input.type = computeInType(node.input.type, expected.input.type, bpNode, "Input")
		node.inputKey.type = computeInType(node.inputKey.type, expected.inputKey.type, bpNode, "InputKey")
		node.output.type = computeOutType(node.output.type, expected.output.type, bpNode, "Output")
		node.outputKey.type = computeOutType(node.outputKey.type, expected.outputKey.type, bpNode, "OutputKey")
		node.cardinality.set(0,expected.cardinality.get(0))
		node.repeatable = expected.repeatable
	}

	def Optional<RType> computeInType(Optional<RType> nodeType, Optional<RType> expected, EObject node, String fieldName) {
		if (expected.empty) {
			nodeType
		} else if (expected.isSubtypeOf(nodeType)) {
			expected
		} else {
			throw new BlueprintUnresolvedTypeException('''«fieldName» type of «expected.get» is not assignable from type «nodeType.get» of previous node''',
					node, null, RosettaIssueCodes.TYPE_ERROR)
		}
	}

	def Optional<RType> computeOutType(Optional<RType> nodeType, Optional<RType> expected, EObject node, String fieldName) {
		if (expected.present) {
			if (nodeType.empty) {
				// the expected input is know and the actual is unbound - bind it
				expected
			} else {
				// both ends are known, check they are compatible
				if (!nodeType.isSubtypeOf(expected)) {
					throw new BlueprintUnresolvedTypeException('''«fieldName» type of «expected.get» is not assignable to «fieldName» type «nodeType.get» of next node''',
						node, null, RosettaIssueCodes.TYPE_ERROR)
				} else {
					expected
				}
			}
		} else {
			nodeType
		}
	}

	def dispatch Optional<RType> getInput(RosettaExpression expr) {
		val rType = expr.RType
		//TODO this need to be transformed somehow into a nice grammar error
		throw new UnsupportedOperationException(
			"Unexpected input expression "  + expr.class + "... " + rType)
	}
	
	def dispatch Optional<RType> getInput(RosettaLiteral literal) {
		Optional.empty
	}
	
	def dispatch Optional<RType> getInput(ListLiteral list) {
		Optional.empty
	}
	
	def dispatch Optional<RType> getInput(RosettaUnaryOperation expr) {
		return getInput(expr.argument);
	}

	def dispatch Optional<RType> getInput(RosettaBinaryOperation expr) {
		val t1 = getInput(expr.left)
		val t2 = getInput(expr.right)
		if (OptionalUtil.zipWith(t1, t2, [a, b| a.join(b) == ANY]).orElse(false)) {
			throw new BlueprintTypeException('''Input types must be the same but were «t1.get.name» and «t2.get.name»''');
		}
		return t1
	}

	def dispatch Optional<RType> getInput(RosettaSymbolReference expr) {
		if (expr.symbol instanceof RosettaCallableWithArgs) {
			val inputs = expr.args.map[getInput].filter[present].toSet
			if (inputs.size == 0) 
				return Optional.empty
			else if (inputs.size > 1)
				throw new BlueprintTypeException('''Input types must be the same but were «inputs.map[get.name]»''');
			return inputs.head
		}
		return getInput(expr.symbol)
	}

	def dispatch Optional<RType> getInput(RosettaFeatureCall call) {
		return getInput(call.receiver)
	}

	def dispatch Optional<RType> getInput(RosettaSymbol callable) {
		if (!callable.isResolved) {
			return Optional.empty
		}
		switch (callable) {
			Data: {
				return Optional.of(new RDataType(callable))
			}
			RosettaEnumeration :{
				//evaluating a enum constant does not require an input type
				return Optional.empty
			}
		}
		throw new UnsupportedOperationException(
			"Unexpected input parsing Rosetta symbol " + callable.class.simpleName)
	}
	
	def dispatch Optional<RType> getInput(RosettaConditionalExpression call) {
		return getInput(call.^if);		
	}
	
	def dispatch Optional<RType> getInput(RosettaOnlyExistsExpression expr) {
		return getInput(expr.args.get(0))
	}
	
	def dispatch Optional<RType> getInput(RosettaEnumValueReference expr) {
		return Optional.empty
	}
	
	def dispatch Optional<RType> getInput(RosettaFunctionalOperation expr) {
		return getInput(expr.argument)
	}
	
	def dispatch Optional<RType> getInput(Void typed) {
		return Optional.empty
	}
	
	def dispatch Optional<RType> getOutput(RosettaExpression expr) {
		Optional.ofNullable(expr.RType)
	}
	
	def dispatch Optional<RType> getOutput(RosettaSymbolReference ref) {
		return ref.symbol.output
	}
	
	def dispatch Optional<RType> getOutput(Function func) {
		return Optional.of(func.output.typeCall.typeCallToRType)
	}

	def dispatch Optional<RType> getOutput(RosettaTyped typed) {
		return Optional.of(typed.typeCall.typeCallToRType)
	}
	
	def dispatch Optional<RType> getOutput(InlineFunction op) {
		return op.body.output
	}
	
	static class BlueprintUnresolvedTypeException extends Exception {
		EObject source
		EStructuralFeature feature
		String code
		String[] issueData
		
		new(String message, EObject source, EStructuralFeature feature, String code, String... issueData) {
			super(message)
			this.source = source
			this.feature = feature
			this.code = code
			this.issueData = issueData
		}
		
		static def void error (String message, EObject source, EStructuralFeature feature, String code, String... issueData) {
			throw new BlueprintUnresolvedTypeException(message, source, feature, code, issueData)
		}
		
		def getEStructuralFeature() {
			feature
		}
	
		def getCode() {
			code
		}
		
		def getIssueData() {
			issueData
		}
		
		def getSource(){
			source
		}
	}

	/**
	 * returns true if a is a subclass of b
	 * i.e. this would be valid
	 * Type1 a;
	 * Type2 b;
	 * b = a
	 * */
	def isSubtypeOf(Optional<RType> type1, Optional<RType> type2) {
		return OptionalUtil.zipWith(type1, type2, [t1, t2| t1.isSubtypeOf(t2)]).orElse(true)
	}
}