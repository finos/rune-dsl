package com.regnosys.rosetta.interpreternew.visitors;

import java.util.ArrayList;
import java.util.List;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedFeatureValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterTypedValue;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;

public class RosettaInterpreterDataInterpreter 
extends RosettaInterpreterConcreteInterpreter {

	/**
	 * Interprets the definition of a data type, 
	 * then adds the data type to the environment.
	 *
	 * @param exp 		the data type to be interpreted
	 * @return 			the new environment after adding the data type to it
	 */
	public RosettaInterpreterEnvironment interp(Data exp, 
			RosettaInterpreterEnvironment env) {
		String name = exp.getName();
		String superType;
		if (exp.hasSuperType()) {
			superType = exp.getSuperType().getName();
		} else {
			superType = null;
		}
		List<RosettaInterpreterTypedFeatureValue> attributes = new ArrayList<>();
		
		for (Attribute att : exp.getAttributes()) {
			String attName = att.getName();
			RosettaCardinality card = att.getCard();
			
			RosettaInterpreterListValue empty = new RosettaInterpreterListValue(List.of());
			
			attributes.add(new RosettaInterpreterTypedFeatureValue(attName, empty, card));
		}
	
		env.addValue(name, new RosettaInterpreterTypedValue(superType, name, attributes));
		return env;
	}
}
