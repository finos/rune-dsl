package com.regnosys.rosetta.generator.java.blueprints

import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.function.CardinalityProvider
import com.regnosys.rosetta.generator.java.function.FunctionDependencyProvider
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.rosetta.BlueprintExtract
import com.regnosys.rosetta.rosetta.BlueprintFilter
import com.regnosys.rosetta.rosetta.BlueprintLookup
import com.regnosys.rosetta.rosetta.BlueprintNode
import com.regnosys.rosetta.rosetta.BlueprintNodeExp
import com.regnosys.rosetta.rosetta.BlueprintOr
import com.regnosys.rosetta.rosetta.BlueprintRef
import com.regnosys.rosetta.rosetta.BlueprintReturn
import com.regnosys.rosetta.rosetta.BlueprintSource
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport
import com.regnosys.rosetta.rosetta.RosettaDocReference
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.validation.RosettaBlueprintTypeResolver
import com.regnosys.rosetta.validation.TypedBPNode
import java.util.Collection
import java.util.List
import java.util.Map
import javax.inject.Inject
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.blueprints.Blueprint
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.generator.java.types.JavaClass
import com.regnosys.rosetta.generator.java.types.JavaType
import com.regnosys.rosetta.generator.java.types.JavaTypeVariable
import com.regnosys.rosetta.generator.java.types.JavaParameterizedType
import com.regnosys.rosetta.blueprints.BlueprintInstance
import com.regnosys.rosetta.blueprints.runner.actions.rosetta.RosettaActionFactory
import com.regnosys.rosetta.blueprints.BlueprintBuilder
import com.regnosys.rosetta.blueprints.runner.data.RuleIdentifier
import com.regnosys.rosetta.blueprints.runner.nodes.SourceNode
import com.regnosys.rosetta.utils.DottedPath
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.blueprints.runner.actions.Filter
import com.regnosys.rosetta.blueprints.runner.actions.FilterByRule
import com.regnosys.rosetta.blueprints.runner.actions.IdChange
import com.regnosys.rosetta.blueprints.DataItemReportBuilder
import com.regnosys.rosetta.blueprints.runner.data.GroupableData
import com.regnosys.rosetta.blueprints.runner.data.DataIdentifier
import com.regnosys.rosetta.blueprints.DataItemReportUtils
import com.regnosys.rosetta.types.RDataType

class BlueprintGenerator {
	static Logger LOGGER = Logger.getLogger(BlueprintGenerator)
	
	@Inject extension ImportManagerExtension
	@Inject extension RosettaBlueprintTypeResolver
	@Inject extension ExpressionGenerator
	@Inject CardinalityProvider cardinality
	@Inject FunctionDependencyProvider functionDependencyProvider
	@Inject extension RosettaExtensions
	@Inject extension JavaIdentifierRepresentationService
	@Inject RosettaTypeProvider typeProvider

	/**
	 * generate a blueprint java file
	 */
	def generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version, extension JavaNames names) {
		elements.filter(RosettaBlueprintReport).forEach [ report |
			// generate blueprint report
			fsa.generateFile(packages.model.blueprint.withForwardSlashes + '/' + report.name + 'BlueprintReport.java',
				generateBlueprint(packages, firstNodeExpression(report), null, report.name, 'BlueprintReport', report.URI, report.reportType?.name, version, names))
			// generate output report type builder
			if (report.reportType !== null) {
				fsa.generateFile(packages.model.blueprint.withForwardSlashes + '/' + report.reportType.name.toDataItemReportBuilderName + '.java',
					generateReportBuilder(packages, report, version, names))
			}
		]
		
		elements.filter(RosettaBlueprint)
			.filter[nodes !== null]
			.forEach [ bp |
			fsa.generateFile(packages.model.blueprint.withForwardSlashes + '/' + bp.name + 'Rule.java',
				generateBlueprint(packages, bp.nodes, typeProvider.getRType(bp.output), bp.name, 'Rule', bp.URI, null, version, names))
		]
	}

	/**
	 * get first node expression
	 */
	def firstNodeExpression(RosettaBlueprintReport report) {
		var BlueprintNodeExp currentNodeExpr = null
		var BlueprintNodeExp firstNodeExpr = null
		
		for (eligibilityRule : report.eligibilityRules) {
			val ref = RosettaFactory.eINSTANCE.createBlueprintRef
			ref.blueprint = eligibilityRule
			ref.name = eligibilityRule.name
			
			var newNodeExpr = RosettaFactory.eINSTANCE.createBlueprintNodeExp
			newNodeExpr.node = ref
			newNodeExpr.node.name = ref.name
						
			if (null === currentNodeExpr) firstNodeExpr = newNodeExpr
			else currentNodeExpr.next = newNodeExpr
				
			currentNodeExpr = newNodeExpr
		}
		
		val node = RosettaFactory.eINSTANCE.createBlueprintOr
		node.name = report.name
		
		report.allReportingRules.sortBy[name].forEach[
			val ref = RosettaFactory.eINSTANCE.createBlueprintRef
			ref.blueprint = it
			ref.name = it.name
			val rule = RosettaFactory.eINSTANCE.createBlueprintNodeExp
			rule.node = ref
			rule.node.name = ref.name
			node.bps.add(rule)
		]
		
		if (!node.bps.empty) {
			val orNodeExpr = RosettaFactory.eINSTANCE.createBlueprintNodeExp
			orNodeExpr.node = node
			currentNodeExpr.next = orNodeExpr			
		}
			
		return firstNodeExpr
	}

	/**
	 * Generate the text of a blueprint
	 */
	def generateBlueprint(RosettaJavaPackages packageName, BlueprintNodeExp nodes, RType output, String name, String type, String uri, String dataItemReportBuilderName, String version, extension JavaNames names) {
		try {
			
			val typed = buildTypeGraph(nodes, output, names)
			val clazz = new JavaClass(packageName.model.blueprint, name + type)
			val clazzWithArgs = bindArgs(typed, clazz)
			
			val topScope = new JavaScope(packageName.model.blueprint)
			
			val classScope = topScope.classScope(clazzWithArgs.toString)
			
			val StringConcatenationClient body = '''
				«emptyJavadocWithVersion(version)»
				public class «clazzWithArgs» implements «Blueprint»<«typed.input.getEither(names)», «typed.output.getEither(names)», «typed.inputKey.getEither(names)», «typed.outputKey.getEither(names)»> {
					
					private final «RosettaActionFactory» actionFactory;
					
					@«Inject»
					public «name»«type»(«RosettaActionFactory» actionFactory) {
						this.actionFactory = actionFactory;
					}
					
					@Override
					public String getName() {
						return "«name»"; 
					}
					
					@Override
					public String getURI() {
						return "«uri»";
					}
					
					«nodes.buildBody(classScope, typed, dataItemReportBuilderName, names)»
				}
				'''
				
				buildClass(packageName.model.blueprint, body, topScope)
			}
			catch (Exception e) {
				LOGGER.error("Error generating blueprint java for "+name, e);
				return '''Unexpected Error generating «name».java Please see log for details'''
			}
	}
	
	/**
	 * Provide Generic names for the blueprint for parameters that haven't been bound to specific classes
	 * and generate the generic args string e.g. <Input, ?, ?, ?> becomes <Input, OUTPUT, INKEY, OUTKEY>
	 */
	def JavaType bindArgs(TypedBPNode node, JavaClass clazz) {
		var typeArgs = newArrayList
		if (!node.input.bound) {
			val IN = new JavaTypeVariable(clazz, "IN")
			node.input.genericType = IN
			typeArgs.add(IN)
		}
		if (!node.output.bound) {
			val OUT = new JavaTypeVariable(clazz, "OUT")
			node.output.genericType = OUT
			typeArgs.add(OUT)
		}
		if (!node.inputKey.bound) {
			val INKEY = new JavaTypeVariable(clazz, "INKEY")
			node.inputKey.genericType = INKEY
			typeArgs.add(INKEY)
		}
		if (!node.outputKey.bound) {
			val OUTKEY = new JavaTypeVariable(clazz, "OUTKEY")
			node.outputKey.genericType = OUTKEY
			typeArgs.add(OUTKEY)
		}
		if (typeArgs.size>0) {
			return new JavaParameterizedType(clazz, typeArgs)
		} else {
			return clazz
		}
	}
	
	/**
	 * build the body of the blueprint class
	 */
	def StringConcatenationClient buildBody(BlueprintNodeExp nodes, JavaScope scope, TypedBPNode typedNode, String dataItemReportBuilderName, extension JavaNames names) {
		nodes.functionDependencies.toSet.forEach[
			scope.createIdentifier(it.toFunctionInstance, it.name.toFirstLower)
		]
		
		val context = new Context(nodes)
		val blueprintScope = scope.methodScope("blueprint")
		return '''
			«FOR dep : nodes.functionDependencies.toSet»
				@«Inject» protected «dep.toJavaType» «scope.getIdentifierOrThrow(dep.toFunctionInstance)»;
			«ENDFOR»
			
			@Override
			public «BlueprintInstance»<«typedNode.input.getEither(names)», «typedNode.output.getEither(names)», «typedNode.inputKey.getEither(names)», «typedNode.outputKey.getEither(names)»> blueprint() { 
				return 
					«importWildcard(method(BlueprintBuilder, "startsWith"))»(actionFactory, «nodes.buildGraph(blueprintScope, names, typedNode.next, context)»)
					«IF dataItemReportBuilderName !== null».addDataItemReportBuilder(new «dataItemReportBuilderName.toDataItemReportBuilderName»())«ENDIF»
					.toBlueprint(getURI(), getName());
			}
			«FOR bpRef : context.bpRefs.entrySet»
			
			«bpRef.key.blueprintRef(bpRef.value, context, names)»
			«ENDFOR»
			«FOR source : context.sources.entrySet»
			
			«source.key.getSource(source.value, context, names)»
			«ENDFOR»
		'''
	}
	
	/**
	 * recursive function that builds the graph of nodes
	 */
	def StringConcatenationClient buildGraph(BlueprintNodeExp nodeExp, JavaScope scope, JavaNames names, TypedBPNode typedNode, Context context) 
		'''
		«nodeExp.buildNode(scope, names, typedNode, context)»«IF nodeExp.next !== null»)
		.then(« nodeExp.next.buildGraph(scope, names, typedNode.next, context)»«ENDIF»'''
	
	/**
	 * write out an individual graph node
	 */
	def StringConcatenationClient buildNode(BlueprintNodeExp nodeExp, JavaScope scope, JavaNames names, TypedBPNode typedNode, Context context) {
		val node = nodeExp.node
		val id = createIdentifier(nodeExp);
		switch (node) {
			BlueprintExtract: {				
				val cond = node.call
				val multi = cardinality.isMulti(cond)
				val repeatable = node.repeatable
				
				val lambdaScope = scope.lambdaScope
				val implicitVar = if (typedNode.input.type instanceof RDataType) {
					lambdaScope.createIdentifier((typedNode.input.type as RDataType).toBlueprintImplicitVar, typedNode.input.type.name.toFirstLower)
				} else {
					lambdaScope.createUniqueIdentifier(typedNode.input.type.name.toFirstLower)
				}
				
				if (!multi)
				'''actionFactory.<«typedNode.input.getEither(names)», «
					typedNode.output.getEither(names)», «typedNode.inputKey.getEither(names)»>newRosettaSingleMapper("«node.URI»", "«(cond).toNodeLabel
						»", «id», «implicitVar» -> «node.call.javaCode(lambdaScope, names)»)'''
				else if (repeatable)
				'''actionFactory.<«typedNode.input.getEither(names)», «
									typedNode.output.getEither(names)», «typedNode.inputKey.getEither(names)»>newRosettaRepeatableMapper("«node.URI»", "«(cond).toNodeLabel
														»", «id», «implicitVar» -> «node.call.javaCode(lambdaScope, names)»)'''
				else
				'''actionFactory.<«typedNode.input.getEither(names)», «
									typedNode.output.getEither(names)», «typedNode.inputKey.getEither(names)»>newRosettaMultipleMapper("«node.URI»", "«(cond).toNodeLabel
														»", «id», «implicitVar» -> «node.call.javaCode(lambdaScope, names)»)'''
			}
			BlueprintReturn: {				
				val expr = node.expression
				
				val lambdaScope = scope.lambdaScope
				
				'''actionFactory.<«typedNode.input.getEither(names)», «typedNode.output.getEither(names)», «typedNode.inputKey.getEither(names)»> newRosettaReturn("«node.URI»", "«expr.toNodeLabel»",  «id»,  () -> «expr.javaCode(lambdaScope, names)»)'''
			}
			BlueprintLookup: {
				val nodeName = if (nodeExp.identifier !== null) nodeExp.identifier else node.name
				//val lookupLamda = '''«typedNode.input.type.name.toFirstLower» -> lookup«node.name»(«typedNode.input.type.name.toFirstLower»)'''
				'''actionFactory.<«typedNode.input.getEither(names)», «
					typedNode.output.getEither(names)», «typedNode.inputKey.getEither(names)»>newRosettaLookup("«node.URI»", "«nodeName»", «id», "«node.name»")'''
			
			}
			BlueprintOr : {
				node.orNode(scope, names, typedNode, context, id)
			}
			BlueprintRef : {
				context.addBPRef(typedNode)
				'''get«node.blueprint.name.toFirstUpper»()«IF nodeExp.identifier!==null»)
				.then(new «IdChange»("«node.URI»", "as «nodeExp.identifier»", «id»)«ENDIF»'''				
			}
			BlueprintFilter :{								
				if(node.filter!==null) {
					val lambdaScope = scope.lambdaScope
					val implicitVar = if (typedNode.input.type instanceof RDataType) {
						lambdaScope.createIdentifier((typedNode.input.type as RDataType).toBlueprintImplicitVar, typedNode.input.type.name.toFirstLower)
					} else {
						lambdaScope.createUniqueIdentifier(typedNode.input.type.name.toFirstLower)
					}
					'''new «Filter»<«typedNode.input.getEither(names)», «typedNode.inputKey.getEither(names)»>("«node.URI»", "«node.filter.toNodeLabel»", «implicitVar
						» -> «node.filter.javaCode(lambdaScope, names)».get(), «id»)'''
				}
				else {
					context.addBPRef(typedNode)
					'''new «FilterByRule»<«typedNode.input.getEither(names)», «typedNode.inputKey.getEither(names)»>("«node.URI»", "«node.filterBP.blueprint.name»", 
					get«node.filterBP.blueprint.name.toFirstUpper»(), «id»)'''
				}
			}
			BlueprintSource: {
				context.addSource(node, typedNode)
				'''get«node.name.toFirstUpper»()'''
			}
			default: {
				throw new UnsupportedOperationException("Can't generate code for node of type "+node.class)
			}
		}
	}
	
	def StringConcatenationClient createIdentifier(BlueprintNodeExp nodeExp) {
		if (nodeExp.identifier !== null) {
			return '''new «RuleIdentifier»("«nodeExp.identifier»", getClass())'''
		}
		val node = nodeExp.node
		switch (node) {
			BlueprintExtract: {
				val nodeName = if (node.name !== null) node.name
								else node.call.toNodeLabel
				'''new «RuleIdentifier»("«nodeName»", getClass())'''
			}
			BlueprintReturn: {
				val nodeName = if (node.name !== null) node.name
								else node.expression.toNodeLabel
				
				'''new «RuleIdentifier»("«nodeName»", getClass())'''
			}
			BlueprintLookup: {
				'''new «RuleIdentifier»("Lookup «node.name»", getClass())'''
			}
			default: {
				'''null'''
			}
		}
	}
	
	static def getURI(EObject eObject) {
		val res = eObject.eResource;
		if (res !== null) {
			val uri = res.URI
			return uri.lastSegment +"#" + res.getURIFragment(eObject)
		} else {
			val id = EcoreUtil.getID(eObject);
			if (id !== null) {
				return id;
			} else {
				return "";
			}
		}
	}
	
	def StringConcatenationClient orNode(BlueprintOr orNode, JavaScope scope, JavaNames names, TypedBPNode orTyped, Context context, StringConcatenationClient id) {
		'''
		«IF !orNode.bps.isEmpty»
			«BlueprintBuilder».<«orTyped.getOutFullS(names)»>or(actionFactory,
				«FOR bp:orNode.bps.indexed  SEPARATOR ","»
				«importWildcard(method(BlueprintBuilder, "startsWith"))»(actionFactory, «bp.value.buildGraph(scope, names, orTyped.orNodes.get(bp.key), context)»)
				«ENDFOR»
				)
			«ENDIF»
		'''
	}
	
	def StringConcatenationClient getOutFullS(TypedBPNode node, JavaNames names) {
		'''«node.input.getEither(names)», «node.output.getEither(names)», «node.inputKey.getEither(names)», «node.outputKey.getEither(names)»'''
	}
	
	def StringConcatenationClient blueprintRef(RosettaBlueprint ref, TypedBPNode typedNode, Context context, JavaNames names) {	
		val className = ref.name + "Rule"
		val refName = ref.name.toFirstLower + "Ref"
		val dep = new JavaClass(DottedPath.splitOnDots((ref.eContainer as RosettaModel).name).child("blueprint"), className)
		'''
		@«Inject» private «dep» «refName»;
		protected «BlueprintInstance»«typedNode.typeArgs(names)» get«ref.name.toFirstUpper»() {
			return «refName».blueprint();
		}'''
	}
	
		
	protected def StringConcatenationClient typeArgs(TypedBPNode typedNode, JavaNames names)
		'''<«typedNode.input.getEither(names)», «typedNode.output.getEither(names)», «typedNode.inputKey.getEither(names)», «typedNode.outputKey.getEither(names)»>'''
		
	
	def StringConcatenationClient getSource(String source, TypedBPNode node, Context context, JavaNames names)
	'''
		protected «SourceNode»<«node.output.getEither(names)», «node.outputKey.getEither(names)»> get«source.toFirstUpper()»() {
			throw new «UnsupportedOperationException»();
		}
	'''
	
	def fullname(RosettaType type, RosettaJavaPackages packageName, extension JavaNames names) {
		if (type instanceof Data)
			'''«packageName.model».«type.name»'''.toString
		else 
			typeProvider.getRType(type).toJavaType
	}
	
	def Iterable<Function> functionDependencies(BlueprintNodeExp node) {
		return node.node.functionDependencies + (node.next===null?#[]:node.next.functionDependencies)
	}
	
	def Iterable<Function> functionDependencies(BlueprintNode node) {
		switch (node) {
			BlueprintOr : {
				node.bps.flatMap[functionDependencies].toList
			}
			BlueprintExtract: {
				functionDependencyProvider.functionDependencies(node.call)
			}
			BlueprintReturn: {
				functionDependencyProvider.functionDependencies(node.expression)
			}
			BlueprintFilter: {
				functionDependencyProvider.functionDependencies(node.filter)
			}
			default :{
				#[]
			}
		}
	}

	/**
	 * Builds DataItemReportBuilder that takes a list of GroupableData
	 */
	def String generateReportBuilder(RosettaJavaPackages packageName, RosettaBlueprintReport report, String version, extension JavaNames names) {
		try {			
			val scope = new JavaScope(packageName.model.blueprint)
			
			val StringConcatenationClient body = '''
				«emptyJavadocWithVersion(version)»
				public class «report.reportType.name.toDataItemReportBuilderName» implements «DataItemReportBuilder» {
				
					«report.buildDataItemReportBuilderBody(names)»
				}
				'''
			buildClass(packageName.model.blueprint, body, scope)
		}
		catch (Exception e) {
			LOGGER.error("Error generating blueprint java for "+report.reportType.name, e);
			return '''Unexpected Error generating «report.reportType.name».java Please see log for details'''
		}
	}
	
	def StringConcatenationClient buildDataItemReportBuilderBody(RosettaBlueprintReport report, extension JavaNames names) {
		val reportType = typeProvider.getRType(report.reportType).toJavaType
		val builderName = "dataItemReportBuilder"
		'''
		@Override
		public <T> «reportType» buildReport(«Collection»<«GroupableData»<?, T>> reportData) {
			«reportType».«reportType»Builder «builderName» = «reportType».builder();
			
			for («GroupableData»<?, T> groupableData : reportData) {
				«DataIdentifier» dataIdentifier = groupableData.getIdentifier();
				if (dataIdentifier instanceof «RuleIdentifier») {
					«RuleIdentifier» ruleIdentifier = («RuleIdentifier») dataIdentifier;
					«Class»<?> ruleType = ruleIdentifier.getRuleType();
					«Object» data = groupableData.getData();
					if (data == null) {
						continue;
					}
					«report.reportType.buildRules(builderName, names)»
				}
			}
			
			return «builderName».build();
		}'''
	}
	
	def StringConcatenationClient buildRules(Data dataType, String builderPath, extension JavaNames names) {
		'''«FOR attr : dataType.allAttributes»
			«val attrType = attr.type»
			«val rule = attr.ruleReference?.reportingRule»
			«IF rule !== null»
				«val ruleClass = new JavaClass(DottedPath.splitOnDots((rule.eContainer as RosettaModel).name).child("blueprint"), rule.name + "Rule")»
				«IF attr.card.isIsMany»
					«IF attrType instanceof Data»
						«attrType.buildRules('''«builderPath».getOrCreate«attr.name.toFirstUpper»(ruleIdentifier.getRepeatableIndex().orElse(0))''', names)»
					«ENDIF»
				«ELSE»
					if («ruleClass».class.isAssignableFrom(ruleType)) {
						«DataItemReportUtils».setField(«builderPath»::set«attr.name.toFirstUpper», «typeProvider.getRType(attrType).toJavaType».class, data, «rule.name»Rule.class);
					}
				«ENDIF»
			«ELSEIF attrType instanceof Data»
				«IF !attr.card.isIsMany»
					«attrType.buildRules('''«builderPath».getOrCreate«attr.name.toFirstUpper»()''', names)»
				«ENDIF»
			«ENDIF»
		«ENDFOR»
		'''	
	}
	
	def String toDataItemReportBuilderName(String dataItemReportTypeName) {
		'''«dataItemReportTypeName»_DataItemReportBuilder'''
	}
	
	@org.eclipse.xtend.lib.annotations.Data static class AttributePath {
		List<Attribute> path
		RosettaDocReference ref
	}
	
	@org.eclipse.xtend.lib.annotations.Data static class RegdOutputField {
		Attribute attrib
		RosettaDocReference ref
	}
	
	@org.eclipse.xtend.lib.annotations.Data static class Context {
		BlueprintNodeExp nodes
		Map<RosettaBlueprint, TypedBPNode> bpRefs = newLinkedHashMap
		Map<String, TypedBPNode> sources = newHashMap
		
		def addBPRef(TypedBPNode node) {
			addBPRef(node.node, node)		
		}
		def dispatch addBPRef(BlueprintNode node, TypedBPNode nodeType) {
			LOGGER.error("unexpected node type adding bpRef")
			""
		}
		def dispatch addBPRef(BlueprintRef ref, TypedBPNode node) {
			bpRefs.put(ref.blueprint, node)
		}
		
		def dispatch addBPRef(BlueprintFilter ref, TypedBPNode node) {
			bpRefs.put(ref.filterBP.blueprint, node.orNodes.get(0))
		}
		
		def addSource(BlueprintSource source, TypedBPNode typed) {
			sources.put(source.name, typed)
		}
		
	}
}
