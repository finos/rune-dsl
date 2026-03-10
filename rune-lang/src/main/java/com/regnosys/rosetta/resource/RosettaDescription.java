package com.regnosys.rosetta.resource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;

import java.util.HashMap;
import java.util.Map;

import static com.regnosys.rosetta.resource.RosettaResourceDescriptionStrategy.IN_OVERRIDDEN_NAMESPACE;

public class RosettaDescription extends EObjectDescription {

    public RosettaDescription(QualifiedName qualifiedName, EObject element, Map<String, String> userData, boolean isInOverriddenNamespace) {
        super(qualifiedName, element, mergeUserData(userData, isInOverriddenNamespace));
    }

    private static Map<String, String> mergeUserData(Map<String, String> userData, boolean isInOverriddenNamespace) {
        if ((userData == null || userData.isEmpty()) && !isInOverriddenNamespace) {
            return Map.of();
        }
        Map<String, String> merged = new HashMap<>();
        if (userData != null) {
            merged.putAll(userData);
        }
        if (isInOverriddenNamespace) {
            merged.put(IN_OVERRIDDEN_NAMESPACE, "true");
        }
        return merged;
    }
}