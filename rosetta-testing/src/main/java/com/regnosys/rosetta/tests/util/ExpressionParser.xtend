package com.regnosys.rosetta.tests.util

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
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.diagnostics.Severity
import org.eclipse.xtext.naming.QualifiedName
import com.regnosys.rosetta.rosetta.simple.Attribute
import java.util.Collection
import com.regnosys.rosetta.scoping.RosettaScopeProvider
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.scoping.IScope
import com.regnosys.rosetta.rosetta.RosettaModel
import javax.inject.Inject
import java.util.List
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.linking.lazy.LazyLinker
import javax.inject.Provider
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.emf.ecore.util.InternalEList
import org.eclipse.xtext.linking.lazy.LazyLinkingResource
import org.eclipse.xtext.linking.impl.DefaultLinkingService

class ExpressionParser {
	@Inject IParser parser
	@Inject RosettaGrammarAccess grammar
	@Inject ModelHelper modelHelper
	@Inject Provider<XtextResource> resourceProvider
	@Inject RosettaStaticLinker linker
	
	def RosettaExpression parseExpression(CharSequence expr) {
		return parseExpression(expr, emptyList)
	}
	
	def RosettaExpression parseExpression(CharSequence expr, Collection<? extends CharSequence> attrs) {
		return parseExpression(expr, defaultContext, attrs)
	}
	
	def RosettaExpression parseExpression(CharSequence expr, List<RosettaModel> context, Collection<? extends CharSequence> attrs) {
		val attributes = attrs.map[createAttribute(context)].toList
		return parseExpression(expr, context, attributes)
	}
	
	def RosettaExpression parseExpression(CharSequence expr, Attribute... attributes) {
		return parseExpression(expr, defaultContext, attributes)
	}
	
	def RosettaExpression parseExpression(CharSequence expr, List<RosettaModel> context, Attribute... attributes) {
		val IParseResult result = parser.parse(grammar.rosettaCalcExpressionRule, new StringReader(expr.toString()))
		assertFalse(result.hasSyntaxErrors)
		val expression = result.rootASTElement as RosettaExpression
		val exprRes = createResource("expr", expression, context)
		link(expression, context, attributes)
		deleteResource(exprRes, context)
		return expression
	}
	
	def Attribute createAttribute(CharSequence attr) {
		return createAttribute(attr, defaultContext)
	}
	
	def Attribute createAttribute(CharSequence attr, List<RosettaModel> context) {
		val IParseResult result = parser.parse(grammar.attributeRule, new StringReader(attr.toString()))
		assertFalse(result.hasSyntaxErrors)
		val attribute = result.rootASTElement as Attribute
		val attrRes = createResource("attribute " + attr, attribute, context)
		link(attribute, context, emptyList)
		deleteResource(attrRes, context)
		return attribute
	}
	
	private def Resource createResource(String name, EObject content, List<RosettaModel> context) {
		val resource = resourceProvider.get()
		resource.URI = URI.createURI("synthetic://" + name)
		resource.contents.add(content)
		context.head.eResource.resourceSet.resources.add(resource)
		resource
	}
	private def void deleteResource(Resource resource, List<RosettaModel> context) {
		context.head.eResource.resourceSet.resources.remove(resource)
	}
	
	private def List<RosettaModel> defaultContext() {
		return newArrayList(modelHelper.testResourceSet.resources.map[contents.head as RosettaModel])
	}
	
	private def void link(EObject obj, List<RosettaModel> context, Collection<? extends EObject> globals) {
		linker.setStateForNextLink(context, globals)
		val consumer = new ListBasedDiagnosticConsumer
		linker.linkModel(obj, consumer)
				
		val errors = consumer.getResult(Severity.ERROR) + obj.eResource.errors
		val warnings = consumer.getResult(Severity.WARNING)
		if (!errors.empty) {
			throw new RuntimeException(errors.toString)
		}
		if (!warnings.empty) {
			throw new RuntimeException(warnings.toString)
		}
	}
	
	private static class RosettaContextBasedScopeProvider extends RosettaScopeProvider {
		List<RosettaModel> context = emptyList
		
		def void setContext(List<RosettaModel> context) {
			this.context = context
		}
		
		override protected getImplicitImports(boolean ignoreCase) {
			(super.getImplicitImports(ignoreCase) + context.map[name].toSet.map[createImportedNamespaceResolver(it + ".*", ignoreCase)]).toList
		}
	}
	private static class RosettaStaticLinker extends LazyLinker {
		@Inject
		RosettaContextBasedScopeProvider scopeProvider
		
		IScope staticScope = IScope.NULLSCOPE
		
		def void setStateForNextLink(List<RosettaModel> context, Collection<? extends EObject> globals) {
			scopeProvider.setContext(context)
			staticScope = Scopes.scopeFor(globals)
		}
		private def void clearState() {
			scopeProvider.setContext(emptyList)
			staticScope = IScope.NULLSCOPE
		}
		
		override protected doLinkModel(EObject root, IDiagnosticConsumer consumer) {
			// TODO: this is hacky
			((root.eResource as LazyLinkingResource).linkingService as DefaultLinkingService).setScopeProvider(scopeProvider)
			
			super.doLinkModel(root, consumer)
			EcoreUtil2.resolveAll(root)
			clearState
		}

		protected override void createAndSetProxy(EObject obj, INode node, EReference eRef) {
			val varName = NodeModelUtils.getTokenText(node)
			val staticElement = staticScope.getSingleElement(QualifiedName.create(varName))
			if (staticElement !== null) {
				val resolved = staticElement.getEObjectOrProxy()
				if (eRef.isMany()) {
					(obj.eGet(eRef, false) as InternalEList<EObject>).addUnique(resolved)
				} else {
					obj.eSet(eRef, resolved);
				}
			} else {
				super.createAndSetProxy(obj, node, eRef)
			}
		}
	}
}