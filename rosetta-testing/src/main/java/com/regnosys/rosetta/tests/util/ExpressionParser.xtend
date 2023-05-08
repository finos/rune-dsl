package com.regnosys.rosetta.tests.util

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.services.RosettaGrammarAccess
import java.io.StringReader
import org.eclipse.xtext.parser.IParseResult
import org.eclipse.xtext.parser.IParser

import static org.junit.jupiter.api.Assertions.*
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.diagnostics.IDiagnosticConsumer
import org.eclipse.xtext.resource.impl.ListBasedDiagnosticConsumer
import java.util.Map
import org.eclipse.xtext.diagnostics.IDiagnosticProducer
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.CrossReference
import org.eclipse.xtext.nodemodel.ICompositeNode
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.ParserRule
import org.eclipse.xtext.GrammarUtil
import com.google.common.collect.Iterables
import org.eclipse.xtext.linking.impl.LinkingDiagnosticProducer
import org.eclipse.xtext.diagnostics.Severity
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.linking.ILinkingDiagnosticMessageProvider
import org.eclipse.xtext.linking.impl.Linker
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.builtin.RosettaBuiltinsService
import org.eclipse.emf.ecore.resource.ResourceSet
import com.regnosys.rosetta.rosetta.RosettaType
import java.util.Collection
import java.util.Collections
import com.regnosys.rosetta.scoping.RosettaScopeProvider
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.scoping.IScope

class ExpressionParser {
	@Inject IParser parser
	@Inject RosettaGrammarAccess grammar
	@Inject RosettaStaticLinker linker
	
	Map<String, ? extends EObject> basicTypes
	
	@Inject
	new(RosettaBuiltinsService builtins, ResourceSet resourceSet) {
		builtins.getBasicTypesModel(resourceSet) => [
			basicTypes = elements
				.filter[it instanceof RosettaType]
				.map[it as RosettaType]
				.toMap[name]
		]
	}
	
	def RosettaExpression parseExpression(CharSequence expr) {
		return parseExpression(expr, Collections.emptyList)
	}
	
	def RosettaExpression parseExpression(CharSequence expr, Collection<? extends CharSequence> attrs) {
		val attributes = attrs.map[createAttribute].toList
		return parseExpression(expr, attributes)
	}
	
	def RosettaExpression parseExpression(CharSequence expr, Attribute... attributes) {
		val IParseResult result = parser.parse(grammar.rosettaCalcExpressionRule, new StringReader(expr.toString()))
		assertFalse(result.hasSyntaxErrors)
		val expression = result.rootASTElement as RosettaExpression
		linkVariables(expression, attributes)
		return expression
	}
	
	private def void linkVariables(EObject obj, Collection<Attribute> attrs) {
		val attributeMap = attrs
			.toMap[name]
		link(obj, attributeMap)
	}
	
	def Attribute createAttribute(CharSequence attr) {
		val IParseResult result = parser.parse(grammar.attributeRule, new StringReader(attr.toString()))
		assertFalse(result.hasSyntaxErrors)
		val attribute = result.rootASTElement as Attribute
		
		link(attribute, basicTypes)
		attribute
	}
	
	private def void link(EObject obj, Map<String, ? extends EObject> globals) {
		linker.setGlobalsForNextLink(globals)
		val consumer = new ListBasedDiagnosticConsumer
		linker.linkModel(obj, consumer)
		
		val errors = consumer.getResult(Severity.ERROR)
		val warnings = consumer.getResult(Severity.WARNING)
		if (!errors.empty) {
			throw new RuntimeException(errors.toString)
		}
		if (!warnings.empty) {
			throw new RuntimeException(warnings.toString)
		}
	}
	
	private static class RosettaNullResourceScopeProvider extends RosettaScopeProvider {
		override protected IScope getResourceScope(Resource res, EReference reference) {
			return IScope.NULLSCOPE
		}
		
		override protected IScope getLocalElementsScope(IScope parent, EObject context,
			EReference reference) {
			return parent
		}
	}
	private static class RosettaStaticLinker extends Linker {
		@Inject
		RosettaNullResourceScopeProvider scopeProvider
		@Inject
		ILinkingDiagnosticMessageProvider linkingDiagnosticMessageProvider
		Map<String, ? extends EObject> globals = newHashMap
		
		def void setGlobalsForNextLink(Map<String, ? extends EObject> globals) {
			this.globals = globals
		}
		private def void clearGlobals() {
			globals = newHashMap
		}
		
		override protected doLinkModel(EObject root, IDiagnosticConsumer consumer) {
			val producer = new LinkingDiagnosticProducer(consumer);
			val iterator = getAllLinkableContents(root)
			while (iterator.hasNext()) {
				val eObject = iterator.next();
				installLinks(eObject, producer);
			}
			clearGlobals
		}
		override void beforeModelLinked(EObject model, IDiagnosticConsumer diagnosticsConsumer) {
			
		}
		
		protected def void installLinks(EObject obj, IDiagnosticProducer producer) {
			val node = NodeModelUtils.getNode(obj);
			if (node === null)
				return;
			installLinks(obj, producer, node, false);
		}
	
		private def void installLinks(EObject obj, IDiagnosticProducer producer, ICompositeNode parentNode, boolean dontCheckParent) {
			val eClass = obj.eClass();
			if (eClass.EAllReferences.size - eClass.EAllContainments.size === 0)
				return;
	
			for (var node = parentNode.firstChild; node !== null; node = node.nextSibling) {
				val grammarElement = node.grammarElement
				if (grammarElement instanceof CrossReference && hasLeafNodes(node)) {
					producer.setNode(node);
					val crossReference = grammarElement as CrossReference;
					val eRef = GrammarUtil.getReference(crossReference, eClass);
					if (eRef === null) {
						val parserRule = GrammarUtil.containingParserRule(crossReference);
						val feature = GrammarUtil.containingAssignment(crossReference).getFeature();
						throw new IllegalStateException("Couldn't find EReference for crossreference '"+eClass.getName()+"::"+feature+"' in parser rule '"+parserRule.getName()+"'.");
					}
					setLink(obj, node, eRef, producer);
				} else if (grammarElement instanceof RuleCall && node instanceof ICompositeNode) {
					val ruleCall = grammarElement as RuleCall;
					val calledRule = ruleCall.getRule();
					if (calledRule instanceof ParserRule && (calledRule as ParserRule).isFragment()) {
						installLinks(obj, producer, node as ICompositeNode, true);
					}
				}
			}
			if (!dontCheckParent && shouldCheckParentNode(parentNode)) {
				installLinks(obj, producer, parentNode.getParent(), dontCheckParent);
			}
		}
		
		private def void setLink(EObject obj, INode node, EReference eRef, IDiagnosticProducer producer) {
			val varName = NodeModelUtils.getTokenText(node)
			val scope = scopeProvider.getScope(obj, eRef)
			val elementInScope = scope.getSingleElement(QualifiedName.create(varName))
			if (elementInScope !== null) {
				obj.eSet(eRef, elementInScope)
			} else {
				val elementInGlobals = globals.get(varName)
				if (elementInGlobals !== null) {
					obj.eSet(eRef, elementInGlobals)
				} else {
					producer.addDiagnostic(linkingDiagnosticMessageProvider.getUnresolvedProxyMessage(createDiagnosticContext(obj, eRef, node)))
				}
			}
		}
		
		protected def boolean hasLeafNodes(INode node) {
			return !Iterables.isEmpty(node.getLeafNodes());
		}
	}
}