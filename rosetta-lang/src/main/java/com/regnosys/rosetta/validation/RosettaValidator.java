/*
 * Copyright (c) REGnosys 2018 (www.regnosys.com) 
 * generated by Xtext 2.39.0
 */
package com.regnosys.rosetta.validation;

import org.eclipse.xtext.validation.ComposedChecks;

import com.regnosys.rosetta.validation.expression.ExpressionValidator;
import com.regnosys.rosetta.validation.expression.ParseOperationValidator;


@ComposedChecks(validators = {
   	RosettaSimpleValidator.class,
   	ReportValidator.class,
   	TypeValidator.class,
   	AttributeValidator.class,
   	ParseOperationValidator.class,
   	EnumValidator.class,
   	ChoiceValidator.class,
   	ExpressionValidator.class,
   	FunctionValidator.class
})
public class RosettaValidator extends AbstractRosettaValidator {

}
