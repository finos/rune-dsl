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

class RosettaTranslateValidator extends AbstractDeclarativeRosettaValidator {
	
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
			if (!container.feature.isFeatureMulti && instruction.expression.isMulti) {
				error('''Expression must be of single cardinality when mapping to attribute `«container.feature.name»` of single cardinality .''', instruction.expression, null);
			}
		} else if (container instanceof Translation) {
			if (instruction.expression.isMulti) {
				error('''Expression must be of single cardinality when mapping to a type.''', instruction.expression, null);
			}
		}
	}
	
	@Check
	def void checkTranslateMetaInstruction(TranslateMetaInstruction metaInstruction) {
		if (metaInstruction.expression.isMulti) {
			error('''Expression must be of single cardinality.''', metaInstruction.expression, null);
		}
	}
}
