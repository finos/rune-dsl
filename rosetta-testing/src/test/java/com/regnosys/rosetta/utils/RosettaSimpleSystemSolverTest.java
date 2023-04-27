package com.regnosys.rosetta.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.regnosys.rosetta.utils.RosettaSimpleSystemSolver.Equation;
import com.regnosys.rosetta.utils.RosettaSimpleSystemSolver.SolutionSet;

import com.regnosys.rosetta.interpreter.RosettaInterpreterContext;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.tests.RosettaInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.RosettaValueHelper;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.RosettaSymbol;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaSimpleSystemSolverTest {
	@Inject
	private RosettaSimpleSystemSolver solver;
	@Inject
	private ExpressionParser parser;
	@Inject
	private RosettaValueHelper helper;

	private RosettaExpression parse(String expr, Attribute... attributes) {
		return parser.parseExpression(expr, attributes);
	}
	
	private Equation parse(String left, String right, Attribute... attributes) {
		return new Equation(parse(left, attributes), parse(right, attributes));
	}
	
	private List<Equation> parse(String left1, String right1, String left2, String right2, Attribute... attributes) {
		return List.of(parse(left1, right1, attributes), parse(left2, right2, attributes));
	}
	
	private List<Equation> parse(String left1, String right1, String left2, String right2, String left3, String right3, Attribute... attributes) {
		return List.of(parse(left1, right1, attributes), parse(left2, right2, attributes), parse(left3, right3, attributes));
	}
	
	private Optional<SolutionSet> solve(Equation eq, Attribute... solveFor) {
		return solve(List.of(eq), solveFor);
	}
	private Optional<SolutionSet> solve(List<Equation> system, Attribute... solveFor) {
		return solver.solve(system, Set.of(solveFor));
	}
	
	private void assertSolution(Map<RosettaSymbol, RosettaValue> expected, SolutionSet set) {
		assertSolution(expected, set, Collections.emptyMap());
	}
	private void assertSolution(Map<RosettaSymbol, RosettaValue> expected, SolutionSet set, Map<RosettaSymbol, RosettaValue> symbolMap) {
		Map<RosettaSymbol, RosettaValue> solution = set.getSolution(RosettaInterpreterContext.ofSymbolMap(symbolMap)).orElseThrow();
		assertEquals(expected, solution);
	}
	
	@Test
	public void testSimpleSystem() {
		Attribute x = parser.createAttribute("x int (1..1)");
		Attribute y = parser.createAttribute("y int (1..1)");
		List<Equation> system = parse("x", "42", "-1", "y", "10", "10", x, y);
		SolutionSet set = solve(system, x, y).orElseThrow();
		assertSolution(Map.of(x, helper.toValue(42), y, helper.toValue(-1)), set);
	}
	
	@Test
	public void testSimpleSystemWithVariable() {
		Attribute x = parser.createAttribute("x int (1..1)");
		Attribute y = parser.createAttribute("y int (1..1)");
		Attribute a = parser.createAttribute("a int (1..1)");
		List<Equation> system = parse("x", "a", "a", "y", "x", "29", x, y, a);
		SolutionSet set = solve(system, x, y).orElseThrow();
		assertSolution(Map.of(x, helper.toValue(29), y, helper.toValue(29)), set, Map.of(a, helper.toValue(29)));
	}
	
	@Test
	public void testNotSimpleSystem() {
		Attribute x = parser.createAttribute("x int (1..1)");
		Equation system = parse("x", "42 + 1", x);
		assertTrue(solve(system, x).isEmpty());
	}
	
	@Test
	public void testUnsolvableSystem() {
		Attribute x = parser.createAttribute("x int (1..1)");
		Attribute a = parser.createAttribute("a int (1..1)");
		List<Equation> system = parse("x", "a", "a", "0", x, a);
		SolutionSet set = solve(system, x).orElseThrow();
		assertTrue(set.getSolution(RosettaInterpreterContext.ofSymbolMap(Map.of(a, helper.toValue(1)))).isEmpty());
	}
	
	@Test
	public void testUnderdeterminedSystem() {
		Attribute x = parser.createAttribute("x int (1..1)");
		Attribute y = parser.createAttribute("y int (1..1)");
		List<Equation> system = parse("x", "42", "10", "10", x, y);
		assertTrue(solve(system, x, y).isEmpty());
	}
}
