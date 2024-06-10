package com.regnosys.rosetta.interpreternew.value;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBaseValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterNumberValue;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterBaseValueTest {

	@Test
	void elementStreamTest() {
		RosettaInterpreterValue val = new RosettaInterpreterNumberValue(4);
		List<Object> result = new ArrayList<Object>();
		result.add(new RosettaInterpreterNumberValue(BigDecimal.valueOf(4)).getValue());
		
		assertEquals(result, RosettaInterpreterBaseValue.elementStream(val).collect(Collectors.toList()));
	}
}
