package com.regnosys.rosetta.interpreternew.values;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterListValue extends RosettaInterpreterBaseValue {
	private List<RosettaInterpreterValue> expressions;
	
	public RosettaInterpreterListValue(List<RosettaInterpreterValue> expressions) {
		super();
		this.expressions = expressions;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(expressions);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterListValue [expressions=" + expressions + "]";
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
		RosettaInterpreterListValue other = (RosettaInterpreterListValue) obj;
		return Objects.equals(expressions, other.expressions);
	}

	public List<RosettaInterpreterValue> getExpressions() { return expressions; }

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(expressions.toArray());
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return expressions.stream();
	}
}
