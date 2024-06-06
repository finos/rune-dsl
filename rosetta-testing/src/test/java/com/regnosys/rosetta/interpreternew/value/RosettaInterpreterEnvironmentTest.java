package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterEnvironment;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;

public class RosettaInterpreterEnvironmentTest {
	
	@Test
	void replaceValueTest() {
		Map<String, RosettaInterpreterBaseValue> map = new HashMap<>();
		map.put("a", new RosettaInterpreterNumberValue(2));
		
		RosettaInterpreterEnvironment env = new RosettaInterpreterEnvironment(map);
		
		env.addValue("a", new RosettaInterpreterNumberValue(3));
		
		Map<String, RosettaInterpreterBaseValue> updatedMap = new HashMap<>();
		updatedMap.put("a", new RosettaInterpreterNumberValue(3));
		
		assertEquals(updatedMap, env.getEnvironment());
		
	}

}
