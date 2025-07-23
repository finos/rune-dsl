package com.regnosys.rosetta.validation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.EValidatorRegistrar;
import org.eclipse.xtext.validation.FeatureBasedDiagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.simple.Annotated;
import com.regnosys.rosetta.rosetta.simple.Attribute;

public abstract class AbstractDeclarativeRosettaValidator extends AbstractDeclarativeValidator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDeclarativeRosettaValidator.class);
	
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	
	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>();
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/Rosetta"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaSimple"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaExpression"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaTranslate"));
		return result;
	}
	
	@Override
	public void register(EValidatorRegistrar registrar) {
	}
	
	@Override
	protected void handleExceptionDuringValidation(Throwable targetException) throws RuntimeException  {
		super.handleExceptionDuringValidation(targetException);
		LOGGER.error(targetException.getMessage(), targetException);
	}
	
	@Override
	protected MethodWrapper createMethodWrapper(AbstractDeclarativeValidator instanceToUse, Method method) {
		return new RosettaMethodWrapper(instanceToUse, method);
	}

	private static class RosettaMethodWrapper extends MethodWrapper {
		protected RosettaMethodWrapper(AbstractDeclarativeValidator instance, Method m) {
			super(instance, m);
		}

		@Override
		public void invoke(State state) {
			try {
				super.invoke(state);
			} catch (Exception e) {
				String message = "Unexpected validation failure running " + getMethod().getName();
				LOGGER.error(message, e);
				state.hasErrors = true;
				state.chain.add(createDiagnostic(message, state));
			}
		}

		private Diagnostic createDiagnostic(String message, State state) {
			return new FeatureBasedDiagnostic(Diagnostic.ERROR, message, state.currentObject, null, -1, state.currentCheckType, null);
		}
	}

	protected void errorKeyword(String message, EObject o, Keyword keyword) {
		INode k = findDirectKeyword(o, keyword);
		if (k != null) {
			getMessageAcceptor().acceptError(
				message,
				o,
				k.getOffset(),
				k.getLength(),
				null
			);
		}
	}
	protected void errorKeyword(String message, EObject o, String keyword) {
		INode k = findDirectKeyword(o, keyword);
		if (k != null) {
			getMessageAcceptor().acceptError(
				message,
				o,
				k.getOffset(),
				k.getLength(),
				null
			);
		}
	}
	protected INode findDirectKeyword(EObject o, Keyword keyword) {
		return findDirectKeyword(o, keyword.getValue());
	}
	protected INode findDirectKeyword(EObject o, String keyword) {
		ICompositeNode node = NodeModelUtils.findActualNodeFor(o);
		return findDirectKeyword(node, keyword);
	}
	protected INode findDirectKeyword(ICompositeNode node, String keyword) {
		for (INode n : node.getChildren()) {
			EObject ge = n.getGrammarElement();
			if (ge instanceof Keyword && ((Keyword)ge).getValue().equals(keyword)) { // I compare the keywords by value instead of directly by reference because of an issue that sometimes arises when running multiple tests consecutively. I'm not sure what the issue is.
				return n;
			}
			if ((ge instanceof RuleCall || ge instanceof Action) && n instanceof ICompositeNode && !(ge.eContainer() instanceof Assignment)) {
				INode keywordInFragment = findDirectKeyword((ICompositeNode)n, keyword);
				if (keywordInFragment != null) {
					return keywordInFragment;
				}
			}
		}
		return null;
	}
	
	protected void checkDeprecatedAnnotation(Annotated annotated, EObject owner, EStructuralFeature ref, int index) {
		if (annotated.getAnnotations().stream().anyMatch(ann -> ecoreUtil.isResolved(ann.getAnnotation()) && "deprecated".equals(ann.getAnnotation().getName()))) {
			String msg;
			if (annotated instanceof RosettaNamed) {
				msg = ((RosettaNamed)annotated).getName() + " is deprecated";
			} else {
				msg = "Deprecated";
			}
			warning(msg, owner, ref, index);
		} else if (annotated instanceof Attribute) {
			// Check if deprecated annotation is inherited
			Attribute parent = ecoreUtil.getParentAttribute((Attribute) annotated);
			if (parent != null) {
				checkDeprecatedAnnotation(parent, owner, ref, index);
			}
		}
	}
}
