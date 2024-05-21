package com.regnosys.rosetta.interpreternew.value;


import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterErrorValue;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterBooleanValue;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterError;



class RosettaInterpreterErrorValueTest {
	
	RosettaInterpreterError e1;
	RosettaInterpreterError e2;
	RosettaInterpreterError e3;
	RosettaInterpreterError e4;
	RosettaInterpreterErrorValue v1;
	RosettaInterpreterErrorValue v2;
	RosettaInterpreterErrorValue v3;
	RosettaInterpreterBooleanValue vb2;
	RosettaInterpreterBooleanValue vb1;
	List<RosettaInterpreterValue> vals;
	
	@BeforeEach
	void setup() {
		e1 = new RosettaInterpreterError("e1");
		e2 = new RosettaInterpreterError("e2");
		e3 = new RosettaInterpreterError("e3");
		e4 = new RosettaInterpreterError("e4");
		v1 = new RosettaInterpreterErrorValue(e1);
		v2 = new RosettaInterpreterErrorValue(e2);
		v3 = new RosettaInterpreterErrorValue(e3);
		vb1 = new RosettaInterpreterBooleanValue(true);
		vb2 = new RosettaInterpreterBooleanValue(false);
		vals = new ArrayList<>(List.of(v1,v2,v3,vb1,vb2));
	}

	@Test
	void testGetErrors() {
		assertEquals(List.of(e1), v1.getErrors());
	}

	@Test
	void testAddError() {
		v1.addError(e2);
		assertEquals(List.of(e1,e2), v1.getErrors());
	}

	@Test
	void testAddAllErrorsRosettaInterpreterErrorValue() {
		v1.addError(e2);
		v3.addAllErrors(v1);
		assertThat(v3.getErrors()).containsExactlyInAnyOrderElementsOf(List.of(e1,e2,e3));
	}

	@Test
	void testAddAllErrorsRosettaInterpreterValue() {
		v1.addError(e2);
		v3.addAllErrors(((RosettaInterpreterValue)v1));
		assertThat(v3.getErrors()).containsExactlyInAnyOrderElementsOf(List.of(e1,e2,e3));
	}

	@Test
	void testAddAllErrorsEListOfRosettaInterpreterBaseError() {
		v3.addAllErrors(List.of(e1,e2));
		assertThat(v3.getErrors()).containsExactlyInAnyOrderElementsOf(List.of(e1,e2,e3));
	}

	@Test
	void testErrorsExistRosettaInterpreterValueRosettaInterpreterValue() {
		assertTrue(RosettaInterpreterErrorValue.errorsExist(v2, vb2));
		assertFalse(RosettaInterpreterErrorValue.errorsExist(vb1, vb2));
	}

	@Test
	void testErrorsExistRosettaInterpreterValue() {
		assertTrue(RosettaInterpreterErrorValue.errorsExist(v2));
		assertFalse(RosettaInterpreterErrorValue.errorsExist(vb1));
	}

	@Test
	void testErrorsExistListOfRosettaInterpreterValue() {
		assertTrue(RosettaInterpreterErrorValue.errorsExist(vals));
		vals.remove(v1);
		vals.remove(v2);
		vals.remove(v3);
		assertFalse(RosettaInterpreterErrorValue.errorsExist(vals));
	}

	@Test
	void testMergeListOfRosettaInterpreterValue() {
		RosettaInterpreterErrorValue val = new RosettaInterpreterErrorValue();
		val.addAllErrors(List.of(e1,e2,e3));
		
		assertEquals(val, RosettaInterpreterErrorValue.merge(vals));
	}

	@Test
	void testMergeRosettaInterpreterValue() {
		RosettaInterpreterErrorValue val = new RosettaInterpreterErrorValue(e2);
		assertEquals(val, RosettaInterpreterErrorValue.merge(v2));
	}

	@Test
	void testMergeRosettaInterpreterValueRosettaInterpreterValue() {
		RosettaInterpreterErrorValue val = new RosettaInterpreterErrorValue();
		val.addAllErrors(List.of(e2, e3));
		
		assertEquals(val, RosettaInterpreterErrorValue.merge(v2, v3));
	}

}
