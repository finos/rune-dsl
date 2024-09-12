package com.regnosys.rosetta.ide.symbol;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.findReferences.TargetURICollector;
import org.eclipse.xtext.findReferences.TargetURIs;

import com.regnosys.rosetta.rosetta.expression.TranslateDispatchOperation;
import com.regnosys.rosetta.utils.TranslateUtil;

@Singleton
public class RosettaTargetURICollector extends TargetURICollector {
	private final TranslateUtil translateUtil;
	
	@Inject
	public RosettaTargetURICollector(TranslateUtil translateUtil) {
		this.translateUtil = translateUtil;
	}
	
	@Override
	protected void doAdd(EObject object, TargetURIs targetURIs) {
		if (object instanceof TranslateDispatchOperation) {
			translateUtil.findMatches((TranslateDispatchOperation) object).forEach(
						m -> super.doAdd(m, targetURIs)
					);
		} else {
			super.doAdd(object, targetURIs);
		}
	}
}
