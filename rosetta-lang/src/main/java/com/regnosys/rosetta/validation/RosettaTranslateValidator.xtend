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
	
	@Check
	def void checkTranslateInstruction(TranslateInstruction instruction) {
		val container = instruction.eContainer
		if (container instanceof TranslationRule) {
			// Case attribute translation:
			// - For single cardinality attributes, all expressions should be single.
			// - For multi cardinality attributes, all expressions should be of the same cardinality.
			// - If the translation calls another translation, there should be at least one matching translation in the same translate source.
			if (!container.feature.isFeatureMulti) {
				instruction.expressions.forEach[
					if (isMulti) {
						error('''Expression must be of single cardinality when mapping to attribute `«container.feature.name»` of single cardinality .''', it, null);
					}
				]
			} else if (!instruction.expressions.empty) {
				val firstIsMulti = instruction.expressions.head.isMulti
				instruction.expressions.tail.forEach[
					val exprIsMulti = isMulti
					if (exprIsMulti !== firstIsMulti) {
						error('''Expression is of «IF exprIsMulti»multi«ELSE»single«ENDIF» cardinality, whereas the first expression is of «IF firstIsMulti»multi«ELSE»single«ENDIF» cardinality.''', it, null);
					}
				]
			}
			
			val resultType = container.feature.RTypeOfFeature
			val inputTypes = instruction.expressions.map[RType]
			if (inputTypes.size === 1 && inputTypes)
			container.translation.source.hasAnyMatch(resultType, inputTypes)
		} else if (container instanceof Translation) {
			// Case type translation
			// - All expressions should be singular.
			// - If the translation calls another translation, there should be at least one matching translation in the same translate source.
			instruction.expressions.forEach[
				if (isMulti) {
					error('''Expression must be of single cardinality when mapping to a type.''', it, null);
				}
			]
		}
	}
	
	@Check
	def void checkTranslateMetaInstruction(TranslateMetaInstruction metaInstruction) {
		metaInstruction.expressions.forEach[
			if (isMulti) {
				error('''Expression must be of single cardinality.''', it, null);
			}
		]
	}
}
