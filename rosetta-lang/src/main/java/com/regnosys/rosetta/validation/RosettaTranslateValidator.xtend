package com.regnosys.rosetta.validation

import org.eclipse.xtext.validation.Check
import com.regnosys.rosetta.rosetta.translate.Translation
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import javax.inject.Inject
import com.regnosys.rosetta.types.CardinalityProvider
import com.regnosys.rosetta.utils.TranslateUtil
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.TranslateDispatchOperation
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import com.regnosys.rosetta.types.RErrorType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService

import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.*
import static extension com.regnosys.rosetta.validation.RosettaIssueCodes.*

class RosettaTranslateValidator extends AbstractDeclarativeRosettaValidator {
	
	@Inject extension RosettaTypeProvider
	@Inject extension TypeSystem
	@Inject extension CardinalityProvider
	@Inject extension TranslateUtil util
	@Inject extension RBuiltinTypeService
	
	@Check
	def void checkUniqueTranslateParameterNames(Translation translation) {
		if (translation.parameters.size >= 2) {
			val visited = newHashSet
			for (param: translation.parameters) {
				if (param.name === null) {
					error('''Cannot have unnamed parameters when there are multiple parameters.''', param, null);
				} else {
					if (!visited.add(param.name)) {
						error('''Duplicate parameter name `«param.name»`.''', param, ROSETTA_NAMED__NAME);
					}
				}
			}
		}
		
		val expr = translation.expression
		if (expr.isMulti) {
			error('''Expected an expression of single cardinality, but was multi.''', expr, null)
		}
		if (translation.resultType !== null) {
			checkType(translation.resultType.typeCallToRType, expr, expr, null, INSIGNIFICANT_INDEX)
		}
	}
	
	@Check
	def void checkTranslateDispatch(TranslateDispatchOperation op) {
		op.inputs.forEach[
			if (isMulti) {
				error('''Expected an expression of single cardinality, but was multi.''', it, null)
			}
		]
		
		val source = util.getSource(op)
		if (source === null) {
			error('''Cannot infer the translate source to use. Did you forget to add `using <source name>`?''', op, null)
		} else {
			val inputTypes = op.inputs.map[RType]
			val outputType = op.outputType.typeCallToRType
			if (!source.hasAnyMatch(outputType, inputTypes)) {
				val multipleInputs = inputTypes.size >= 2
				error('''No translation exists to translate «IF multipleInputs»(«ENDIF»«FOR input : inputTypes SEPARATOR ', '»«input.name»«ENDFOR»«IF multipleInputs»)«ENDIF» into «outputType.name».''', op, null);
			}
		}
	}
	
	private def checkType(RType expectedType, RosettaExpression expression, EObject owner, EReference ref, int index) {
		val actualType = expression.RType
		if (actualType === null) {
			return
		}
		if (actualType instanceof RErrorType)
			error('''«actualType.name»''', owner, ref, index, TYPE_ERROR)
		else if (actualType == MISSING) {
			val node = findActualNodeFor(expression)
			if (node !== null) {
				error('''Couldn't infer actual type for '«getTokenText(node)»'«»''', owner, ref, index,
					TYPE_ERROR)
			}
		} else if (expectedType instanceof RErrorType)
			error('''«expectedType.name»''', owner, ref, index, TYPE_ERROR)
		else if (expectedType !== null && expectedType != MISSING) {
			if (!actualType.isSubtypeOf(expectedType))
				error('''Expected type '«expectedType.name»' but was '«actualType?.name ?: 'null'»'«»''', owner, ref,
					index, TYPE_ERROR)
		}
	}
}
