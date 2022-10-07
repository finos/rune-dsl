package com.regnosys.rosetta.derivedstate;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.resource.IDerivedStateComputer;

import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.JoinOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;

/**
 * Derived state:
 * - syntactic sugar for if-then: automatically add 'empty' to the 'else' clause.
 * - static type of expressions
 */
public class RosettaDerivedStateComputer implements IDerivedStateComputer {	
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
		}
	}
	public void setAllDerivedState(TreeIterator<EObject> tree) {
		tree.forEachRemaining((obj) -> {
			setDerivedState(obj);
		});
	}
	
	public void removeDerivedState(EObject obj) {
		if (obj instanceof RosettaExpression) {
			if (obj instanceof RosettaConditionalExpression) {
				this.discardDefaultElse((RosettaConditionalExpression)obj);
			} else if (obj instanceof JoinOperation) {
				this.discardDefaultJoinSeparator((JoinOperation)obj);
			}
		}
	}
	public void removeAllDerivedState(TreeIterator<EObject> tree) {
		tree.forEachRemaining((obj) -> {
			removeDerivedState(obj);
		});
	}
	
	private void setDefaultElseToEmpty(RosettaConditionalExpression expr) {
		if (!expr.isFull()) {
			expr.setElsethen(ExpressionFactory.eINSTANCE.createListLiteral());
		}
	}
	private void discardDefaultElse(RosettaConditionalExpression expr) {
		if (!expr.isFull()) {
			expr.setElsethen(null);
		}
	}
	
	private void setDefaultJoinSeparator(JoinOperation expr) {
		if (expr.getRight() == null) {
			expr.setRight(ExpressionFactory.eINSTANCE.createRosettaStringLiteral());
			expr.setExplicitSeparator(false);
		} else {
			expr.setExplicitSeparator(true);
		}
	}
	private void discardDefaultJoinSeparator(JoinOperation expr) {
		if (!expr.isExplicitSeparator()) {
			expr.setRight(null);
		}
	}
}
