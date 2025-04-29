package com.regnosys.rosetta.tests.util

import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.services.RosettaGrammarAccess
import java.io.StringReader
import org.eclipse.xtext.parser.IParseResult
import org.eclipse.xtext.parser.IParser

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
import jakarta.inject.Inject
import java.util.List
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.linking.lazy.LazyLinker
import jakarta.inject.Provider
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.emf.ecore.util.InternalEList
import org.eclipse.xtext.linking.lazy.LazyLinkingResource
import org.eclipse.xtext.linking.impl.DefaultLinkingService
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic
import org.eclipse.xtext.resource.XtextSyntaxDiagnosticWithRange
import org.eclipse.xtext.diagnostics.Diagnostic

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
		return parseExpression(expr, emptyList, attrs)
	}
	
	def RosettaExpression parseExpression(CharSequence expr, List<RosettaModel> context, Collection<? extends CharSequence> attrs) {
		val attributes = attrs.map[parseAttribute(context)].toList
		return parseExpression(expr, context, attributes)
	}
	
	def RosettaExpression parseExpression(CharSequence expr, Attribute... attributes) {
		return parseExpression(expr, emptyList, attributes)
	}
	
	def RosettaExpression parseExpression(CharSequence expr, List<RosettaModel> context, Attribute... attributes) {
		val cont = context.isEmpty ? defaultContext : context
		val IParseResult result = parser.parse(grammar.expressionRule, new StringReader(expr.toString()))
		val expression = result.rootASTElement as RosettaExpression
		addSyntaxDiagnostics(createResource("expr", expression, cont), result)
		link(expression, cont, attributes)
		return expression
	}
	
	def Attribute parseAttribute(CharSequence attr) {
		return parseAttribute(attr, defaultContext)
	}
	
	def Attribute parseAttribute(CharSequence attr, List<RosettaModel> context) {
		val cont = context.isEmpty ? defaultContext : context
		val IParseResult result = parser.parse(grammar.attributeRule, new StringReader(attr.toString()))
		val attribute = result.rootASTElement as Attribute
		addSyntaxDiagnostics(createResource("attribute", attribute, cont), result)
		link(attribute, context, emptyList)
		return attribute
	}
	
	private def void addSyntaxDiagnostics(Resource resource, IParseResult parseResult) {
		for (INode error : parseResult.getSyntaxErrors()) {
			val syntaxErrorMessage = error.getSyntaxErrorMessage()
			if (Diagnostic.SYNTAX_DIAGNOSTIC_WITH_RANGE.equals(syntaxErrorMessage.getIssueCode())) {
				val issueData = syntaxErrorMessage.getIssueData()
				if (issueData.length === 1) {
					val data = issueData.get(0)
					val colon = data.indexOf(':')
					resource.errors.add(new XtextSyntaxDiagnosticWithRange(error, Integer.valueOf(data.substring(0, colon)), Integer.valueOf(data.substring(colon + 1)), null))
					return;
				}
			}
			resource.errors.add(new XtextSyntaxDiagnostic(error))
		}
	}
	
	private def Resource createResource(String name, EObject content, List<RosettaModel> context) {
		val resourceSet = context.head.eResource.resourceSet
		var nr = 0
		var uniqueURI = URI.createURI("synthetic://" + name + nr++)
		while (resourceSet.getResource(uniqueURI, false) !== null) {
			uniqueURI = URI.createURI("synthetic://" + name + nr++)
		}
		val resource = resourceProvider.get()
		resource.URI = uniqueURI
		resource.contents.add(content)
		resourceSet.resources.add(resource)
		resource
	}
	
	private def List<RosettaModel> defaultContext() {
		return newArrayList(modelHelper.testResourceSet.resources.map[contents.head as RosettaModel])
	}
	
	private def void link(EObject obj, List<RosettaModel> context, Collection<? extends EObject> globals) {
		linker.setStateForNextLink(context, globals)
		val consumer = new ListBasedDiagnosticConsumer
		linker.linkModel(obj, consumer)
		
		obj.eResource.errors.addAll(consumer.getResult(Severity.ERROR))
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