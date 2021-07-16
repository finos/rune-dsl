package com.regnosys.rosetta.blueprints.runner.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.blueprints.runner.actions.ReduceParent.Action;

public class ReduceByTest {
	
	@Test
	void shouldMergeMaxAndReturn() {
		ReduceBy<String, String, String> reduceBy = new ReduceBy<>("", "", Action.MAXBY, null, null);
		var c1 = reduceBy.new Candidate("a", null);
		var c2 = reduceBy.new Candidate("b", null);
		var result = reduceBy.mergeMax(c1, c2);
		assertEquals(c2, result);
	}
	
	@Test
	void shouldMergeMaxWithNullAndReturn() {
		ReduceBy<String, String, String> reduceBy = new ReduceBy<>("", "", Action.MAXBY, null, null);
		var c1 = reduceBy.new Candidate(null, null);
		var c2 = reduceBy.new Candidate("b", null);
		var result = reduceBy.mergeMax(c1, c2);
		assertEquals(c2, result);
	}
	
	@Test
	void shouldMergeMaxWithNullAndReturn2() {
		ReduceBy<String, String, String> reduceBy = new ReduceBy<>("", "", Action.MAXBY, null, null);
		var c1 = reduceBy.new Candidate("a", null);
		var c2 = reduceBy.new Candidate(null, null);
		var result = reduceBy.mergeMax(c1, c2);
		assertEquals(c1, result);
	}
	
	@Test
	void shouldMergeMaxWithNullAndReturn3() {
		ReduceBy<String, String, String> reduceBy = new ReduceBy<>("", "", Action.MAXBY, null, null);
		var c1 = reduceBy.new Candidate("a", null);
		var result = reduceBy.mergeMax(c1, null);
		assertEquals(c1, result);
	}
	
	@Test
	void shouldMergeMaxWithNullAndReturn4() {
		ReduceBy<String, String, String> reduceBy = new ReduceBy<>("", "", Action.MAXBY, null, null);
		var result = reduceBy.mergeMax(null, null);
		assertNull(result);
	}
	
	@Test
	void shouldMergeMinAndReturn() {
		ReduceBy<String, String, String> reduceBy = new ReduceBy<>("", "", Action.MINBY, null, null);
		var c1 = reduceBy.new Candidate("a", null);
		var c2 = reduceBy.new Candidate("b", null);
		var result = reduceBy.mergeMin(c1, c2);
		assertEquals(c1, result);
	}
}
