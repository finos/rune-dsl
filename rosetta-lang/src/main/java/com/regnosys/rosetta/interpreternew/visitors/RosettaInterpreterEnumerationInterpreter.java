package com.regnosys.rosetta.interpreternew.visitors;

import java.util.ArrayList;
import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumElementValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnumValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterEnumerationInterpreter
	extends RosettaInterpreterConcreteInterpreter {

	public RosettaInterpreterEnumerationInterpreter() {
		super();
	}
	
	/**
	 * Interprets the definition of an enumeration expression, 
	 * then adds the enum value to the environment.
	 *
	 * @param exp the expression to be interpreted
	 * @return the new environment after adding the enum to it
	 */
	public RosettaInterpreterEnvironment interp(RosettaEnumeration exp, 
			RosettaInterpreterEnvironment env) {
		String enumName = exp.getName();
		List<RosettaInterpreterValue> values = new ArrayList<>();
		for (RosettaEnumValue v : exp.getEnumValues()) {
			values.add(new RosettaInterpreterEnumElementValue(
					enumName, v.getName()));
		}
		RosettaInterpreterEnumValue enumeration = 
				new RosettaInterpreterEnumValue(enumName, values);
		env.addValue(enumName, enumeration);
		
		return env;
	}

}
