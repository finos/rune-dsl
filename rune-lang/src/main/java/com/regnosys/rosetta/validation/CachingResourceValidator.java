package com.regnosys.rosetta.validation;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.service.OperationCanceledError;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.validation.ResourceValidatorImpl;

// See issue https://github.com/eclipse/xtext-core/issues/1127
// TODO: contribute to Xtext.
public class CachingResourceValidator extends ResourceValidatorImpl {

	public static String VALIDATION_RESULTS_KEY = CachingResourceValidator.class.getCanonicalName() + ".VALIDATION_CACHE";

	@Override
	public List<Issue> validate(Resource resource, CheckMode mode, CancelIndicator cancelIndicator)
			throws OperationCanceledError {
		if (resource instanceof XtextResource) {
			return ((XtextResource) resource).getCache().get(VALIDATION_RESULTS_KEY, resource,
					() -> super.validate(resource, mode, cancelIndicator));
		} else {
			return super.validate(resource, mode, cancelIndicator);
		}
	}
}