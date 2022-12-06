package com.regnosys.rosetta.validation;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xsemantics.runtime.ErrorInformation;
import org.eclipse.xsemantics.runtime.RuleFailedException;
import org.eclipse.xsemantics.runtime.validation.XsemanticsValidatorFilter;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.google.common.collect.Lists;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;

/**
 * By default, Xsemantics removes validation issues when they point to a
 * source that isn't represented by a node, e.g., when the source is generated
 * by our {@code RosettaDerivedStateComputer}. This class fixes that.
 */
public class RetainXsemanticsIssuesOnGeneratedInputsFilter extends XsemanticsValidatorFilter {
	@Override
	public Iterable<RuleFailedException> filterRuleFailedExceptions(
			RuleFailedException e) {
		final RuleFailedException inner = innermostRuleFailedExceptionWithNodeModelSourcesOrGenerated(e);
		if (inner != null) {
			return Lists.newArrayList(inner);
		}
		// we must return at least a failure, so we default to the passed one
		return Lists.newArrayList(e);
	}
	
	@Override
	public Iterable<ErrorInformation> filterErrorInformation(
			RuleFailedException e) {
		return filteredErrorInformation(e);
	}
	
	/**
	 * Adjustment of {@code TraceUtils::innermostRuleFailedExceptionWithNodeModelSources}
	 */
	private RuleFailedException innermostRuleFailedExceptionWithNodeModelSourcesOrGenerated(RuleFailedException e) {
		List<RuleFailedException> failures = traceUtils.failureAsList(e);
		for (int i=failures.size()-1; i>=0; i--) {
			RuleFailedException failure = failures.get(i);
			if (!filteredErrorInformation(failure).isEmpty()) {
				return failure;
			}
		}
		return null;
	}
	
	/**
	 * Adjustment of {@code TraceUtils::filteredErrorInformation}
	 */
	private List<ErrorInformation> filteredErrorInformation(RuleFailedException e) {
		return removeNonNodeModelSourcesIfNotGenerated(
				traceUtils.removeDuplicateErrorInformation(
						traceUtils.allErrorInformation(
								e)));
	}
	
	/**
	 * Adjustment of {@code TraceUtils::removeNonNodeModelSources}
	 */
	private List<ErrorInformation> removeNonNodeModelSourcesIfNotGenerated(List<ErrorInformation> errorInformations) {
		return errorInformations.stream()
			.filter(errorInfo -> {
				EObject source = errorInfo.getSource();
				if (source instanceof RosettaExpression) {
					if (((RosettaExpression)source).isGenerated()) {
						return true;
					}
				}
				return NodeModelUtils.getNode(source) != null;
			})
			.collect(Collectors.toCollection(LinkedList::new));
	}
}
