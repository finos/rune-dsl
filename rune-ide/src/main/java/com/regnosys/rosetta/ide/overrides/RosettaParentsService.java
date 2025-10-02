package com.regnosys.rosetta.ide.overrides;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.simple.Attribute;

import jakarta.inject.Inject;

public class RosettaParentsService extends AbstractParentsService {
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	
	@ParentsCheck
	public ParentsResult checkAttributeOverride(Attribute attribute) {
		Attribute parentAttribute = ecoreUtil.getParentAttribute(attribute);
		if (parentAttribute != null) {
			return fromEObject(attribute, parentAttribute);
		}
		return null;
	}
	
	@ParentsCheck
	public ParentsResult checkExternalType(RosettaExternalClass externalType) {
		List<EObject> parents = ecoreUtil.getParentsOfExternalType(externalType);
		if (parents.isEmpty()) {
			return null;
		}
		return fromEObjects(externalType, parents);
	}
}
