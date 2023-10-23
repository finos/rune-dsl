package com.regnosys.rosetta.ide.contentassist.cancellable;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.xtext.service.OperationCanceledManager;

// TODO: contribute to Xtext.
public class RosettaOperationCanceledManager extends OperationCanceledManager {
	@Override
	protected RuntimeException getPlatformOperationCanceledException(Throwable t) {
		RuntimeException result = super.getPlatformOperationCanceledException(t);
		if (result != null) {
			return result;
		}
		// Also inspect `InvocationTargetException`'s target exception.
		if (t instanceof InvocationTargetException) {
			return getPlatformOperationCanceledException(((InvocationTargetException)t).getTargetException());
		} else if (t instanceof RuntimeException && t.getCause() instanceof InvocationTargetException) {
			return getPlatformOperationCanceledException(((InvocationTargetException)t.getCause()).getTargetException());
		}
		return null;
	}
}
