package com.regnosys.rosetta.resource;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.util.IAcceptor;

import com.google.inject.Singleton;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.expression.TranslateDispatchOperation;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.translate.Translation;
import com.regnosys.rosetta.utils.TranslateUtil;

@Singleton
public class RosettaResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {
	private final TranslateUtil translateUtil;
	
	@Inject
	public RosettaResourceDescriptionStrategy(TranslateUtil translateUtil) {
		this.translateUtil = translateUtil;
	}

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
			String input = serialize(rule.getInput());
			String expression = serialize(rule.getExpression());
			acceptor.accept(new RuleDescription(qualifiedName, rule, input, expression));
			
			return false;
		} else {
			return super.createEObjectDescriptions(eObject, acceptor);
		}
	}
	
	public boolean createImplicitReferenceDescriptions(EObject from, IAcceptor<IImplicitReferenceDescription> acceptor) {
		if (from instanceof TranslateDispatchOperation) {
			Translation match = translateUtil.findLastMatch((TranslateDispatchOperation) from);
			if (match != null) {
				acceptor.accept(createImplicitReferenceDescription(from, match));
			}
		}
		return true;
	}
	
	protected IImplicitReferenceDescription createImplicitReferenceDescription(EObject from, EObject to) {
		return new DefaultImplicitReferenceDescription(from, to);
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
