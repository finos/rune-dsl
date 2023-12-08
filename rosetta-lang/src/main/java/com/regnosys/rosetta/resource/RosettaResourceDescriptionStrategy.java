package com.regnosys.rosetta.resource;

import javax.inject.Inject;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.util.IAcceptor;

import com.google.inject.Singleton;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.Attribute;

@Singleton
public class RosettaResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {
	
	@Inject
	private ISerializer serializer;

	@Override
	public boolean createEObjectDescriptions(EObject eObject, IAcceptor<IEObjectDescription> acceptor) {		
		if (eObject instanceof RosettaModel) {
			RosettaModel model = (RosettaModel)eObject;
			
			QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(eObject);
			acceptor.accept(new RosettaModelDescription(qualifiedName, model));
			
			return true;
		} else if (eObject instanceof Attribute) {
			Attribute attr = (Attribute)eObject;
			
			QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(attr);
			String typeCall = serialize(attr.getTypeCall());
			String cardinality = serialize(attr.getCard());
			acceptor.accept(new AttributeDescription(qualifiedName, attr, typeCall, cardinality));
			
			return false;
		} else if (eObject instanceof RosettaRule) {
			RosettaRule rule = (RosettaRule)eObject;
			
			QualifiedName qualifiedName = getQualifiedNameProvider().getFullyQualifiedName(rule);
			String typeCall = serialize(rule.getInput());
			acceptor.accept(new RuleDescription(qualifiedName, rule, typeCall));
			
			return false;
		} else {
			return super.createEObjectDescriptions(eObject, acceptor);
		}
	}
	
	private String serialize(EObject eObject) {
		INode node = NodeModelUtils.getNode(eObject);
		if (node != null) {
			return node.getText();
		}
		return serializer.serialize(eObject);
	}
}
