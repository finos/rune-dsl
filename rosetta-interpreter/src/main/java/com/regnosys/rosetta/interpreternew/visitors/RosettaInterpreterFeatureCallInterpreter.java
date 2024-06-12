package com.regnosys.rosetta.interpreternew.visitors;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumElementValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall;
import com.regnosys.rosetta.rosetta.expression.impl.RosettaSymbolReferenceImpl;
import com.regnosys.rosetta.rosetta.impl.RosettaEnumValueImpl;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterFeatureCallInterpreter
				extends RosettaInterpreterConcreteInterpreter {
	
	/**
	 * Based on what class the symbol reference belongs to, it chooses what method to send it to.
	 *
	 * @param expr The RosettaFeatureCall expression to redirect
	 * @param env The Environment
	 * @return If no errors are encountered, a RosettaInterpreterNumberValue or
	 * 		   RosettaInterpreterStringValue representing
	 * 		   the result of the arithmetic/concatenation operation.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interp(RosettaFeatureCall expr,
			RosettaInterpreterEnvironment env) {
			RosettaSymbolReferenceImpl ref = (RosettaSymbolReferenceImpl) expr.getReceiver();
			RosettaEnumValue enumVal;
			//if(expr.getFeature() instanceof RosettaEnumValueImpl) {
			enumVal = (RosettaEnumValueImpl) expr.getFeature();
			RosettaEnumeration enumeration = (RosettaEnumeration) ref.getSymbol();
			return interpEnum(enumeration, enumVal, env);
			//}
			//else Here comes the DataType ref call implementation that will have a different interp method
			
	}
	
	/**
	 * Interprets an enum feature call.
	 *
	 * @param enumeration The RosettaEnumeration to interpret alongside
	 * @param val The enum value to interpret
	 * @param env The Environment
	 * @return If no errors are encountered, a RosettaInterpreterNumberValue or
	 * 		   RosettaInterpreterStringValue representing
	 * 		   the result of the arithmetic/concatenation operation.
	 * 		   If errors are encountered, a RosettaInterpreterErrorValue representing
     *         the error.
	 */
	public RosettaInterpreterValue interpEnum(RosettaEnumeration enumeration, RosettaEnumValue val,
			RosettaInterpreterEnvironment env) {
		
			return new RosettaInterpreterEnumElementValue(val.getEnumeration().getName(),
							val.getName());
			
	
	}
	
}
