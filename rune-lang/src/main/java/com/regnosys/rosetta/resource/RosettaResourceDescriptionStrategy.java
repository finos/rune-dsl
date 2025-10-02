package com.regnosys.rosetta.resource;

import java.util.stream.Collectors;

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

    private final static Logger LOGGER = Logger.getLogger(RosettaResourceDescriptionStrategy.class);

    @Override
    public boolean createEObjectDescriptions(EObject eObject, IAcceptor<IEObjectDescription> acceptor) {
        if (getQualifiedNameProvider() == null) {
            return false;
        }
        try {
            if (eObject instanceof RosettaExpression) {
                return false;
            } else if (eObject instanceof RosettaModel) {
                return createRosettaModelDescription((RosettaModel) eObject, acceptor);
            } else if (eObject instanceof Attribute) {
                return createAttributeDescription((Attribute) eObject, acceptor);
            } else if (eObject instanceof RosettaRule) {
                return createRosettaRuleDescription((RosettaRule) eObject, acceptor);
            }
        } catch (Exception exc) {
            LOGGER.error(exc.getMessage(), exc);
            return true;
        }
        return super.createEObjectDescriptions(eObject, acceptor);
    }

    private boolean createRosettaModelDescription(RosettaModel model, IAcceptor<IEObjectDescription> acceptor) {
        QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(model);
        acceptor.accept(new RosettaModelDescription(qualifiedName, model));
        return true;
    }

    private boolean createAttributeDescription(Attribute attr, IAcceptor<IEObjectDescription> acceptor) {
        QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(attr);
        String typeCall = serialize(attr.getTypeCall());
        String cardinality = serialize(attr.getCard());
        String ruleReferences = serialize(attr.getRuleReferences());
        String labels = serialize(attr.getLabels());
        acceptor.accept(new AttributeDescription(qualifiedName, attr, typeCall, cardinality, ruleReferences, labels));
        return false;
    }

    private boolean createRosettaRuleDescription(RosettaRule rule, IAcceptor<IEObjectDescription> acceptor) {
        QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(rule);
        String input = serialize(rule.getInput());
        String expression = serialize(rule.getExpression());
        if (qualifiedName != null) {
            acceptor.accept(new RuleDescription(qualifiedName, rule, input, expression));
        }
        return false;
    }

    private String serialize(EObject eObject) {
        if (eObject == null) {
            return null;
        }
        INode node = NodeModelUtils.getNode(eObject);
        if (node != null) {
            return node.getText();
        }
        return null;
    }
    private String serialize(EList<? extends EObject> list) {
    	if (list.isEmpty()) {
    		return null;
    	}
        return list.stream()
        		.map(e -> serialize(e))
        		.filter(s -> s != null)
        		.collect(Collectors.joining(","));
    }
}
