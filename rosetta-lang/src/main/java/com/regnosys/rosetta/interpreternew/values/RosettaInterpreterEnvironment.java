package com.regnosys.rosetta.interpreternew.values;

import java.util.HashMap;
import java.util.Map;

import com.regnosys.rosetta.rosetta.impl.RosettaInterpreterBaseEnvironmentImpl;

public class RosettaInterpreterEnvironment extends RosettaInterpreterBaseEnvironmentImpl {
	private Map<String, RosettaInterpreterBaseValue> environment;
	
	public RosettaInterpreterEnvironment() {
		this.setEnvironment(new HashMap<>());
	}
	
	public RosettaInterpreterEnvironment(Map<String, RosettaInterpreterBaseValue> el) {
		this.setEnvironment(el);
	}

	public Map<String, RosettaInterpreterBaseValue> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, RosettaInterpreterBaseValue> env) {
		this.environment = env;
	}
	
	/**
	 * Find a value, by name, in the environment.
	 *
	 * @param name - name of the variable you search for
	 * @return - the value iff variable exists in environment
	 * 		   error otherwise
	 */
	public RosettaInterpreterBaseValue findValue(String name) {
		if (environment.containsKey(name)) { 
			return environment.get(name);
		}
		else {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterEmptyError(
							name 
							+ " does not exist in the environment"));
		}
		
	}
	
	/**
	 * Add a variable and its value to the environment.
	 *
	 * @param name - name of the variable
	 * @param val - value of the variable
	 */
	public void addValue(String name, 
			RosettaInterpreterBaseValue val) {
		
		if (environment.containsKey(name)) { 
			//update env
			environment.replace(name, val);
		}
		else {
			environment.put(name, val);
		}
		
	}
	
	
}
