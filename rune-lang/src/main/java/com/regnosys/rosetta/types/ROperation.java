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

package com.regnosys.rosetta.types;

import java.util.List;
import java.util.Objects;

import com.regnosys.rosetta.rosetta.expression.RosettaExpression;

public class ROperation {
	private final ROperationType rOperationType;
	private final RAssignedRoot pathHead;
	private final List<? extends RFeature> pathTail;
	private final RosettaExpression expression;
	
	public ROperation(ROperationType rOperationType, RAssignedRoot pathHead, List<? extends RFeature> pathTail, RosettaExpression expression) {
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

	public List<? extends RFeature> getPathTail() {
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
