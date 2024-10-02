/*
 * generated by Xtext 2.24.0
 */
package com.regnosys.rosetta.validation

import org.eclipse.xtext.validation.ComposedChecks

/**
 * This class contains custom validation rules. 
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
@ComposedChecks(validators = #[RosettaSimpleValidator, StandaloneRosettaTypingValidator, EnumValidator, ChoiceValidator])
class RosettaValidator extends AbstractRosettaValidator {

	
}
