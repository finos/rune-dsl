package com.regnosys.rosetta.interpreternew.values;
import java.util.List;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaInterpreterValue;

public class RosettaInterpreterListValue extends RosettaInterpreterBaseValue {
	private List<RosettaInterpreterValue> expressions;
	
	public RosettaInterpreterListValue(List<RosettaInterpreterValue> expressions) {
		super();
		this.expressions = expressions;
	}
	
	public List<RosettaInterpreterValue> getExpressions() { return expressions; }
}
