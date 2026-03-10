package com.regnosys.rosetta.resource;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.ExternalAnnotationSource;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import com.regnosys.rosetta.rosetta.expression.RosettaExpression;
import org.apache.log4j.Logger;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.util.IAcceptor;

import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.Attribute;

import jakarta.inject.Singleton;

@Singleton
public class RosettaResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {
    public static final String IN_OVERRIDDEN_NAMESPACE = "IN_OVERRIDDEN_NAMESPACE";

    private final static Logger LOGGER = Logger.getLogger(RosettaResourceDescriptionStrategy.class);

    @Override
    public boolean createEObjectDescriptions(EObject eObject, IAcceptor<IEObjectDescription> acceptor) {
        if (getQualifiedNameProvider() == null) {
            return false;
        }
        try {
            return doCreateEObjectDescriptions(eObject, isInOverriddenNamespace(eObject), acceptor);
        } catch (Exception exc) {
            LOGGER.error(exc.getMessage(), exc);
            return true;
        }
    }
    
    private boolean isInOverriddenNamespace(EObject object) {
        if (!(object instanceof RosettaRootElement elem) || elem.getModel() == null) {
            return false;
        }
        return elem.getModel().isOverridden();
    }

    protected boolean doCreateEObjectDescriptions(EObject eObject, boolean isInOverriddenNamespace, IAcceptor<IEObjectDescription> acceptor) {
        if (eObject instanceof RosettaExpression) {
            return false;
        } else if (eObject instanceof RosettaModel model) {
            return createRosettaModelDescription(model, isInOverriddenNamespace, acceptor);
        } else if (eObject instanceof Attribute attribute) {
            return createAttributeDescription(attribute, isInOverriddenNamespace, acceptor);
        } else if (eObject instanceof RosettaRule rule) {
            return createRosettaRuleDescription(rule, isInOverriddenNamespace, acceptor);
        } else if (eObject instanceof ExternalAnnotationSource) {
            defaultCreateRosettaDescriptions(eObject, isInOverriddenNamespace, acceptor);
            return false; // Do not traverse down annotation sources
        } else {
            defaultCreateRosettaDescriptions(eObject, isInOverriddenNamespace, acceptor);
            return true;
        }
    }
    
    private void defaultCreateRosettaDescriptions(EObject eObject, boolean isInOverriddenNamespace, IAcceptor<IEObjectDescription> acceptor) {
        QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(eObject);
        if (qualifiedName != null) {
            acceptor.accept(new RosettaDescription(qualifiedName, eObject, Map.of(), isInOverriddenNamespace));
        }
    }

    private boolean createRosettaModelDescription(RosettaModel model, boolean isInOverriddenNamespace, IAcceptor<IEObjectDescription> acceptor) {
        QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(model);
        acceptor.accept(new RosettaModelDescription(qualifiedName, model, isInOverriddenNamespace));
        return true;
    }

    private boolean createAttributeDescription(Attribute attr, boolean isInOverriddenNamespace, IAcceptor<IEObjectDescription> acceptor) {
        QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(attr);
        String typeCall = serialize(attr.getTypeCall());
        String cardinality = serialize(attr.getCard());
        String ruleReferences = serialize(attr.getRuleReferences());
        String labels = serialize(attr.getLabels());
        acceptor.accept(new AttributeDescription(qualifiedName, attr, typeCall, cardinality, ruleReferences, labels, isInOverriddenNamespace));
        return false;
    }

    private boolean createRosettaRuleDescription(RosettaRule rule, boolean isInOverriddenNamespace, IAcceptor<IEObjectDescription> acceptor) {
        QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(rule);
        String input = serialize(rule.getInput());
        String expression = serialize(rule.getExpression());
        if (qualifiedName != null) {
            acceptor.accept(new RuleDescription(qualifiedName, rule, input, expression, isInOverriddenNamespace));
        }
        return false;
    }

    private String serialize(EObject eObject) {
        if (eObject == null) {
            return null;
        }
        INode node = NodeModelUtils.getNode(eObject);
        if (node != null) {
            return NodeModelUtils.getTokenText(node);
        }
        return null;
    }
    private String serialize(EList<? extends EObject> list) {
    	if (list.isEmpty()) {
    		return null;
    	}
        return list.stream()
        		.map(this::serialize)
        		.filter(Objects::nonNull)
        		.collect(Collectors.joining(","));
    }

}
