/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.inject.Inject;

import com.regnosys.rosetta.interpreter.RosettaInterpreter;
import com.regnosys.rosetta.interpreter.RosettaInterpreterContext;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
import com.regnosys.rosetta.rosetta.RosettaSymbol;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;

/**
 * An solver for simple systems of equations of Rosetta expressions.
 * 
 * Definition of a "simple" equation:
 * 1. Left and right hand side only contain literals and variables.
 * 2. Each equation contains at most a single variable to solve to.
 * 3. The solution does not contain free variables.
 */
public class RosettaSimpleSystemSolver {
	public static class Equation {
		private final RosettaExpression left;
		private final RosettaExpression right;
		
		public Equation(RosettaExpression left, RosettaExpression right) {
			this.left = left;
			this.right = right;
		}
		
		public RosettaExpression getLeft() {
			return left;
		}
		public RosettaExpression getRight() {
			return right;
		}
	}
	public class SolutionSet {
		private Map<RosettaSymbol, RosettaExpression> solutionMap = new HashMap<>();
		private Set<Equation> conditions = new HashSet<>();
		
		public void addMapping(RosettaSymbol var, RosettaExpression solution) {
			RosettaExpression existingSolution = solutionMap.get(var);
			if (existingSolution == null) {
				solutionMap.put(var, solution);
			} else {
				conditions.add(new Equation(solution, existingSolution));
			}
		}
		
		public void addCondition(Equation eq) {
			conditions.add(eq);
		}
		
		public Set<RosettaSymbol> getSolvedVariables() {
			return solutionMap.keySet();
		}
		
		public Optional<Map<RosettaSymbol, RosettaValue>> getSolution(RosettaInterpreterContext context) {
			for (Equation condition: conditions) {
				RosettaValue evalLeft = interpreter.interpret(condition.getLeft(), context);
				RosettaValue evalRight = interpreter.interpret(condition.getRight(), context);
				if (!evalLeft.equals(evalRight)) {
					return Optional.empty();
				}
			}
			
			Map<RosettaSymbol, RosettaValue> solution = new HashMap<>();
			solutionMap.entrySet().forEach(e -> {
				solution.put(e.getKey(), interpreter.interpret(e.getValue(), context));
			});
			return Optional.of(solution);
		}
	}
	
	@Inject
	private RosettaInterpreter interpreter;
	
	public Optional<SolutionSet> solve(Collection<Equation> equations, Set<? extends RosettaSymbol> variablesToSolve) {
		SolutionSet solution = new SolutionSet();
		
		for (Equation eq: equations) {
			if (!isSimple(eq, variablesToSolve)) {
				return Optional.empty();
			}
			RosettaExpression left = eq.getLeft();
			RosettaExpression right = eq.getRight();
			if (isVariable(left) && isVariable(right)) {
				// ** Case x = y **
				RosettaSymbol leftSymbol = ((RosettaSymbolReference)left).getSymbol();
				RosettaSymbol rightSymbol = ((RosettaSymbolReference)right).getSymbol();
				if (variablesToSolve.contains(leftSymbol)) {
					solution.addMapping(leftSymbol, right);
				} else if (variablesToSolve.contains(rightSymbol)) {
					solution.addMapping(rightSymbol, left);
				} else {
					solution.addCondition(eq);
				}
			} else if (isVariable(left)) {
				// ** Case x = 42 **
				RosettaSymbol leftSymbol = ((RosettaSymbolReference)left).getSymbol();
				if (variablesToSolve.contains(leftSymbol)) {
					solution.addMapping(leftSymbol, right);
				} else {
					solution.addCondition(eq);
				}
			} else if (isVariable(right)) {
				// ** Case 42 = x **
				RosettaSymbol rightSymbol = ((RosettaSymbolReference)right).getSymbol();
				if (variablesToSolve.contains(rightSymbol)) {
					solution.addMapping(rightSymbol, left);
				} else {
					solution.addCondition(eq);
				}
			} else {
				// ** Case 42 = 50 **
				solution.addCondition(eq);
			}
		}
		// Check condition 3
		if (!solution.getSolvedVariables().equals(variablesToSolve)) {
			return Optional.empty();
		}
		return Optional.of(solution);
	}
	
	public boolean isSimple(Equation equation, Collection<? extends RosettaSymbol> variablesToSolve) {
		RosettaExpression left = equation.getLeft();
		RosettaExpression right = equation.getRight();
		
		// Check condition 1
		if (!isVariable(left) && !isLiteral(left)) {
			return false;
		}
		if (!isVariable(right) && !isLiteral(right)) {
			return false;
		}
		
		// Check condition 2
		if (isVariable(left) && isVariable(right)) {
			RosettaSymbol leftSymbol = ((RosettaSymbolReference)left).getSymbol();
			RosettaSymbol rightSymbol = ((RosettaSymbolReference)right).getSymbol();
			if (variablesToSolve.contains(leftSymbol) && variablesToSolve.contains(rightSymbol)) {
				return false;
			}
		}
		return true;
	}
	private boolean isVariable(RosettaExpression expr) {
		return expr instanceof RosettaSymbolReference 
				&& !(((RosettaSymbolReference)expr).getSymbol() instanceof RosettaCallableWithArgs);
	}
	private boolean isLiteral(RosettaExpression expr) {
		return expr instanceof RosettaLiteral;
	}
}
