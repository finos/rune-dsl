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

package com.regnosys.rosetta.derivedstate;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.resource.IDerivedStateComputer;

import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.HasGeneratedInput;
import com.regnosys.rosetta.rosetta.expression.JoinOperation;
import com.regnosys.rosetta.rosetta.expression.ListLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaStringLiteral;
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference;
import com.regnosys.rosetta.utils.ImplicitVariableUtil;

import jakarta.inject.Inject;

/**
 * Derived state:
 * - syntactic sugar for if-then: automatically add 'empty'
 * to the 'else' clause.
 * - syntactic sugar for `join`: automatically add "" if no
 * explicit separator is given.
 * - syntactic sugar for parameter omission: automatically add
 * `item` if the left operand of an operator is omitted.
 */
public class RosettaDerivedStateComputer implements IDerivedStateComputer {
	@Inject
	ImplicitVariableUtil implicitVariableUtil;
	
	@Override
	public void installDerivedState(DerivedStateAwareResource resource, boolean preLinkingPhase) {
		if (!preLinkingPhase) {
			setAllDerivedState(resource.getAllContents());
		}
	}

	@Override
	public void discardDerivedState(DerivedStateAwareResource resource) {
		removeAllDerivedState(resource.getAllContents());
	}
	
	public void setDerivedState(EObject obj) {
		if (obj instanceof RosettaConditionalExpression) {
			this.setDefaultElseToEmpty((RosettaConditionalExpression)obj);
		} else if (obj instanceof JoinOperation) {
			this.setDefaultJoinSeparator((JoinOperation)obj);
		} else if (obj instanceof RosettaSymbolReference) {
			this.setImplicitVariableInContextOfSymbolReference((RosettaSymbolReference)obj);
		}
		if (obj instanceof HasGeneratedInput) {
			this.setDefaultInput((HasGeneratedInput)obj);
		}
	}
	public void setAllDerivedState(TreeIterator<EObject> tree) {
		tree.forEachRemaining((obj) -> {
			setDerivedState(obj);
		});
	}
	public void setAllDerivedState(EObject root) {
		setAllDerivedState(root.eAllContents());
	}
	
	public void removeDerivedState(EObject obj) {
		if (obj instanceof RosettaConditionalExpression) {
			this.discardDefaultElse((RosettaConditionalExpression)obj);
		} else if (obj instanceof JoinOperation) {
			this.discardDefaultJoinSeparator((JoinOperation)obj);
		} else if (obj instanceof RosettaSymbolReference) {
			this.discardImplicitVariableInContextOfSymbolReference((RosettaSymbolReference)obj);
		}
		if (obj instanceof HasGeneratedInput) {
			this.discardDefaultInput((HasGeneratedInput)obj);
		}
	}
	public void removeAllDerivedState(TreeIterator<EObject> tree) {
		tree.forEachRemaining((obj) -> {
			removeDerivedState(obj);
		});
	}
	public void removeAllDerivedState(EObject root) {
		removeAllDerivedState(root.eAllContents());
	}
	
	
	private void setDefaultInput(HasGeneratedInput expr) {
		if (expr.needsGeneratedInput() && implicitVariableUtil.implicitVariableExistsInContext(expr)) {
			expr.setGeneratedInputIfAbsent(implicitVariableUtil.getDefaultImplicitVariable());
		}
	}
	
	private void discardDefaultInput(HasGeneratedInput expr) {
		expr.setGeneratedInputIfAbsent(null);
	}
	
	private void setDefaultElseToEmpty(RosettaConditionalExpression expr) {
		if (!expr.isFull()) {
			ListLiteral lit = ExpressionFactory.eINSTANCE.createListLiteral();
			lit.setGenerated(true);
			expr.setElsethen(lit);
		}
	}
	private void discardDefaultElse(RosettaConditionalExpression expr) {
		if (!expr.isFull()) {
			expr.setElsethen(null);
		}
	}
	
	private void setDefaultJoinSeparator(JoinOperation expr) {
		if (expr.getRight() == null) {
			RosettaStringLiteral lit = ExpressionFactory.eINSTANCE.createRosettaStringLiteral();
			lit.setGenerated(true);
			lit.setValue("");
			expr.setRight(lit);
			expr.setExplicitSeparator(false);
		} else {
			expr.setExplicitSeparator(true);
		}
	}
	private void discardDefaultJoinSeparator(JoinOperation expr) {
		if (!expr.isExplicitSeparator()) {
			if (expr.getRight() != null && expr.getRight().isGenerated()) {
				expr.setRight(null);
			}
		}
	}
	
	private void setImplicitVariableInContextOfSymbolReference(RosettaSymbolReference expr) {
		if (implicitVariableUtil.implicitVariableExistsInContext(expr) && !expr.isExplicitArguments()) {
			if (expr.getSymbol() instanceof RosettaCallableWithArgs callableWithArgs
					&& callableWithArgs.numberOfParameters() == 1) {
				
				expr.setImplicitArgument(implicitVariableUtil.getDefaultImplicitVariable());
			}
		}
	}
	private void discardImplicitVariableInContextOfSymbolReference(RosettaSymbolReference expr) {
		expr.setImplicitArgument(null);
	}
}
