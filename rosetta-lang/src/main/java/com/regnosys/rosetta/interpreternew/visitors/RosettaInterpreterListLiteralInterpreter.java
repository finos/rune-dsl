package com.regnosys.rosetta.interpreternew.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


import com.regnosys.rosetta.interpreternew.RosettaInterpreterVisitor;
import com.regnosys.rosetta.interpreternew.values.RosettaInterpreterListValue;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterListLiteralInterpreter extends RosettaInterpreterConcreteInterpreter {

	public RosettaInterpreterListLiteralInterpreter() {
		super();
	}
	
	public RosettaInterpreterListValue interp(ListLiteral exp) {
		List<RosettaExpression> expressions = exp.getElements();
		List<RosettaInterpreterValue> interpretedExpressions = new ArrayList<>();
		
		for(RosettaExpression e : expressions) {
			RosettaInterpreterValue val = e.accept(visitor);
			
			if (val instanceof RosettaInterpreterListValue) {
				interpretedExpressions.addAll(((RosettaInterpreterListValue)val).getExpressions());
			}
			else interpretedExpressions.add(val);
		}
		
		
		return new RosettaInterpreterListValue(interpretedExpressions);
	}

}
