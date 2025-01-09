package com.regnosys.rosetta.resource;

import org.apache.log4j.Logger;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.util.IAcceptor;

import com.google.inject.Singleton;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.Attribute;

@Singleton
public class RosettaResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {

    private final static Logger LOG = Logger.getLogger(RosettaResourceDescriptionStrategy.class);

    @Override
    public boolean createEObjectDescriptions(EObject eObject, IAcceptor<IEObjectDescription> acceptor) {
        if (getQualifiedNameProvider() == null) {
            return false;
        }
        try {
            if (eObject instanceof RosettaModel) {
                return createRosettaModelDescription((RosettaModel) eObject, acceptor);
            } else if (eObject instanceof Attribute) {
                return createAttributeDescription((Attribute) eObject, acceptor);
            } else if (eObject instanceof RosettaRule) {
                return createRosettaRuleDescription((RosettaRule) eObject, acceptor);
            }
        } catch (Exception exc) {
            LOG.error(exc.getMessage(), exc);
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
        acceptor.accept(new AttributeDescription(qualifiedName, attr, typeCall, cardinality));
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
}
