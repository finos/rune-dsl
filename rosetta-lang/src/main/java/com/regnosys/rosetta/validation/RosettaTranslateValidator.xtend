package com.regnosys.rosetta.validation

import org.eclipse.xtext.validation.Check
import com.regnosys.rosetta.rosetta.translate.Translation
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import com.regnosys.rosetta.rosetta.translate.TranslateInstruction
import com.regnosys.rosetta.rosetta.translate.TranslateMetaInstruction
import com.regnosys.rosetta.rosetta.translate.TranslationRule
import javax.inject.Inject
import com.regnosys.rosetta.types.CardinalityProvider
import com.regnosys.rosetta.utils.TranslateUtil
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.types.RType
import java.util.List
import com.regnosys.rosetta.rosetta.translate.TranslateSource
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.rosetta.TypeCall
import com.regnosys.rosetta.rosetta.translate.BaseTranslateInstruction

class RosettaTranslateValidator extends AbstractDeclarativeRosettaValidator {
	
	@Inject extension RosettaTypeProvider
	@Inject extension TypeSystem
	@Inject extension CardinalityProvider
	@Inject extension TranslateUtil
	
	@Check
	def void checkUniqueTranslateParameterNames(Translation translation) {
		val visited = newHashSet
		var unnamedSeen = false
		for (param: translation.parameters) {
			if (param.name === null) {
				if (unnamedSeen) {
					error('''Cannot have multiple unnamed parameters.''', param, null);
				}
				unnamedSeen = true
			} else {
				if (!visited.add(param.name)) {
					error('''Duplicate parameter name `«param.name»`.''', param, ROSETTA_NAMED__NAME);
				}
			}
		}
	}
	
	private def void translateTypeCheck(RType resultType, List<RosettaExpression> inputs, BaseTranslateInstruction instruction) {
		val inputTypes = inputs.map[RType]
		if (inputs.size !== 1 || !inputTypes.head.isSubtypeOf(resultType)) {
			// No direct assignment - check if an appropriate translation exists
			val source = EcoreUtil2.getContainerOfType(instruction, TranslateSource)
			if (!source.hasAnyMatch(resultType, inputTypes)) {
				val multipleInputs = inputTypes.size >= 2
				error('''No translation exists to translate «IF multipleInputs»(«ENDIF»«FOR input : inputTypes SEPARATOR ', '»«input.name»«ENDFOR»«IF multipleInputs»)«ENDIF» into «resultType.name».''', instruction, null);
			}
		}
	}
	
	private def void translateToFeatureCheck(RosettaFeature feature, List<RosettaExpression> inputs, BaseTranslateInstruction instruction) {
		// - For single cardinality attributes, all expressions should be single.
		// - For multi cardinality attributes, all expressions should be of the same cardinality.
		// - If the translation calls another translation, there should be at least one matching translation in the same translate source.
		if (!feature.isFeatureMulti) {
			inputs.forEach[
				if (isMulti) {
					error('''Expression must be of single cardinality when mapping to attribute `«feature.name»` of single cardinality.''', it, null);
				}
			]
		} else if (!inputs.empty) {
			val firstIsMulti = inputs.head.isMulti
			inputs.tail.forEach[
				val exprIsMulti = isMulti
				if (exprIsMulti !== firstIsMulti) {
					error('''Expression is of «IF exprIsMulti»multi«ELSE»single«ENDIF» cardinality, whereas the first expression is of «IF firstIsMulti»multi«ELSE»single«ENDIF» cardinality.''', it, null);
				}
			]
		}
		
		translateTypeCheck(feature.RTypeOfFeature, inputs, instruction)
	}
	
	private def void translateToTypeCheck(TypeCall resultType, List<RosettaExpression> inputs, BaseTranslateInstruction instruction) {
		// - All expressions should be singular.
		// - If the translation calls another translation, there should be at least one matching translation in the same translate source.
		inputs.forEach[
			if (isMulti) {
				error('''Expression must be of single cardinality when mapping to a type.''', it, null);
			}
		]
		
		translateTypeCheck(resultType.typeCallToRType, inputs, instruction)
	}
	
	@Check
	def void checkTranslateInstruction(TranslateInstruction instruction) {
		val container = instruction.eContainer
		if (container instanceof TranslationRule) {
			translateToFeatureCheck(container.feature, instruction.expressions, instruction)
		} else if (container instanceof Translation) {
			translateToTypeCheck(container.resultType, instruction.expressions, instruction)
		}
	}
	
	@Check
	def void checkTranslateMetaInstruction(TranslateMetaInstruction metaInstruction) {
		translateToFeatureCheck(metaInstruction.metaFeature, metaInstruction.expressions, metaInstruction)
	}
}
