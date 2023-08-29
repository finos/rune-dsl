package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;

public class ROperation {
	private ROperationType rOperationType;
	private RAssignedRoot pathHead;
	private List<RAttribute> pathTail;
	private RosettaExpression expression;
	
	public ROperation(ROperationType rOperationType, RAssignedRoot pathHead, List<RAttribute> pathTail, RosettaExpression expression) {
		this.rOperationType = rOperationType;
		this.pathHead = pathHead;
		this.pathTail = pathTail;
		this.expression = expression;
	}

	public ROperationType getROperationType() {
		return rOperationType;
	}

	public RAssignedRoot getPathHead() {
		return pathHead;
	}

	public List<RAttribute> getPathTail() {
		return pathTail;
	}

	public RosettaExpression getExpression() {
		return expression;
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression, pathHead, pathTail, rOperationType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ROperation other = (ROperation) obj;
		return Objects.equals(expression, other.expression) && Objects.equals(pathHead, other.pathHead)
				&& Objects.equals(pathTail, other.pathTail) && rOperationType == other.rOperationType;
	}


}
