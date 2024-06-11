package com.regnosys.rosetta.interpreternew.values;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterBaseEnvironment;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterEnvironment extends MinimalEObjectImpl implements RosettaInterpreterBaseEnvironment {
	private Map<String, RosettaInterpreterValue> environment;
	
	public RosettaInterpreterEnvironment() {
		this.setEnvironment(new HashMap<>());
	}
	
	public RosettaInterpreterEnvironment(Map<String, RosettaInterpreterValue> el) {
		this.setEnvironment(el);
	}

	public Map<String, RosettaInterpreterValue> getEnvironment() {
		return environment;
	}

	public void setEnvironment(Map<String, RosettaInterpreterValue> env) {
		this.environment = env;
	}
	
	/**
	 * Find a value, by name, in the environment.
	 *
	 * @param name - name of the variable you search for
	 * @return - the value iff variable exists in environment
	 * 		   error otherwise
	 */
	public RosettaInterpreterValue findValue(String name) {
		if (environment.containsKey(name)) { 
			return environment.get(name);
		}
		else {
			return new RosettaInterpreterErrorValue(
					new RosettaInterpreterError(
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
	public RosettaInterpreterValue addValue(String name, 
			RosettaInterpreterValue val) {
		
		if (environment.containsKey(name)) { 
			//update env
			return environment.replace(name, val);
		}
		else {
			return environment.put(name, val);
		}
		
	}

	
	
	@Override
	public int hashCode() {
		return Objects.hash(environment);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RosettaInterpreterEnvironment other = (RosettaInterpreterEnvironment) obj;
		return Objects.equals(environment, other.environment);
	}
	

	@Override
	public String toString() {
		return "RosettaInterpreterEnvironment [environment=" + environment + "]";
	}
}
