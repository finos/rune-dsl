package com.regnosys.rosetta.generator.java.blueprints

import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator.ParamMap
import com.regnosys.rosetta.generator.java.function.CardinalityProvider
import com.regnosys.rosetta.generator.java.function.FunctionDependencyProvider
import com.regnosys.rosetta.generator.java.util.ImportGenerator
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
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaDocReference
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.validation.BindableType
import com.regnosys.rosetta.validation.RosettaBlueprintTypeResolver
import com.regnosys.rosetta.validation.TypedBPNode
import java.util.Collection
import java.util.List
import java.util.Map
import javax.inject.Inject
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.*
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class BlueprintGenerator {
	static Logger LOGGER = Logger.getLogger(BlueprintGenerator)
	
	@Inject extension ImportManagerExtension
	@Inject extension RosettaBlueprintTypeResolver
	@Inject extension ExpressionGenerator
	@Inject CardinalityProvider cardinality
	@Inject FunctionDependencyProvider functionDependencyProvider
	@Inject extension RosettaExtensions

	/**
	 * generate a blueprint java file
	 */
	def generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version, extension JavaNames names) {
		elements.filter(RosettaBlueprintReport).forEach [ report |
			// generate blueprint report
			fsa.generateFile(packages.model.blueprint.directoryName + '/' + report.name + 'BlueprintReport.java',
				generateBlueprint(packages, firstNodeExpression(report), null, report.name, 'BlueprintReport', report.URI, report.reportType?.name, version, names))
			// generate output report type builder
			if (report.reportType !== null) {
				fsa.generateFile(packages.model.blueprint.directoryName + '/' + report.reportType.name.toDataItemReportBuilderName + '.java',
					generateReportBuilder(packages, report, version, names))
			}
		]
		
		elements.filter(RosettaBlueprint)
			.filter[nodes !== null]
			.forEach [ bp |
			fsa.generateFile(packages.model.blueprint.directoryName + '/' + bp.name + 'Rule.java',
				generateBlueprint(packages, bp.nodes, bp.output, bp.name, 'Rule', bp.URI, null, version, names))
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
	def generateBlueprint(RosettaJavaPackages packageName, BlueprintNodeExp nodes, RosettaType output, String name, String type, String uri, String dataItemReportBuilderName, String version, extension JavaNames names) {
		try {
			val imports = new ImportGenerator(packageName)
			imports.addBlueprintImports
			imports.addSourceAndSink
			
			val typed = buildTypeGraph(nodes, output)
			val typeArgs = bindArgs(typed)
			imports.addTypes(typed)
			val StringConcatenationClient scc = nodes.buildBody(typed, dataItemReportBuilderName, imports, names)
			val body = tracImports(scc)
			body.addImport("javax.inject.Inject", "Inject")
			return '''
				package «packageName.model.blueprint.name»;
				
				«FOR imp : body.imports»
					import «imp»;
				«ENDFOR»
				«FOR imp : body.staticImports»
					import static «imp»;
				«ENDFOR»
				// manual imports
				«FOR importClass : imports.imports.filter[imports.isImportable(it)]»
				import «importClass»;
				«ENDFOR»
				«FOR importClass : imports.staticImports»
				import static «importClass».*;
				«ENDFOR»
				
				«emptyJavadocWithVersion(version)»
				public class «name»«type»«typeArgs» implements Blueprint<«typed.input.getEither», «typed.output.getEither», «typed.inputKey.getEither», «typed.outputKey.getEither»> {
					
					private final RosettaActionFactory actionFactory;
					
					@Inject
					public «name»«type»(RosettaActionFactory actionFactory) {
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
					
					«body.toString»
				}
				'''
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
	def bindArgs(TypedBPNode node) {
		var result=""
		var first=true;
		if (!node.input.bound) {
			result+="IN"
			node.input.genericName = "IN"
			first= false
		}
		if (!node.output.bound) {
			if (!first) result+=", "
			result += "OUT"
			node.output.genericName = "OUT"
			first=false
		}
		if (!node.inputKey.bound) {
			if (!first) result+=", "
			result += "INKEY"
			node.inputKey.genericName = "INKEY"
			first=false
		}
		if (!node.outputKey.bound) {
			if (!first) result+=", "
			result+="OUTKEY"
			node.outputKey.genericName = "OUTKEY"
		}
		if (result.length>0) return "<"+result+">"
	}
	
	/**
	 * build the body of the blueprint class
	 */
	def StringConcatenationClient buildBody(BlueprintNodeExp nodes, TypedBPNode typedNode, String dataItemReportBuilderName, ImportGenerator imports, extension JavaNames names) {
		val context = new Context(nodes, imports)
		return '''
			«FOR dep : nodes.functionDependencies.toSet»
				@«Inject» protected «dep.toJavaType» «dep.name.toFirstLower»;
			«ENDFOR»
			
			@Override
			public BlueprintInstance<«typedNode.input.either», «typedNode.output.either», «typedNode.inputKey.either», «typedNode.outputKey.either»> blueprint() { 
				return 
					startsWith(actionFactory, «nodes.buildGraph(typedNode.next, context)»)
					«IF dataItemReportBuilderName !== null».addDataItemReportBuilder(new «dataItemReportBuilderName.toDataItemReportBuilderName»())«ENDIF»
					.toBlueprint(getURI(), getName());
			}
			«FOR bpRef : context.bpRefs.entrySet»
			
			«bpRef.key.blueprintRef(bpRef.value, context)»
			«ENDFOR»
			«FOR source : context.sources.entrySet»
			
			«source.key.getSource(source.value, context)»
			«ENDFOR»
		'''
	}
	
	/**
	 * recursive function that builds the graph of nodes
	 */
	def StringConcatenationClient buildGraph(BlueprintNodeExp nodeExp, TypedBPNode typedNode, Context context) 
		'''
		«nodeExp.node.buildNode(typedNode, context)»«IF nodeExp.next !== null»)
		.then(« nodeExp.next.buildGraph(typedNode.next, context)»«ENDIF»'''
	
	/**
	 * write out an individual graph node
	 */
	def StringConcatenationClient buildNode(BlueprintNode node, TypedBPNode typedNode, Context context) {
		val id = createIdentifier(node); 
		switch (node) {
			BlueprintExtract: {
				context.imports.addTypes(typedNode)
				context.imports.addSingleMapping(node)
				
				val cond = node.call
				val multi = cardinality.isMulti(cond)
				val repeatable = node.repeatable
				
				if (!multi)
				'''actionFactory.<«typedNode.input.getEither», «
					typedNode.output.getEither», «typedNode.inputKey.getEither»>newRosettaSingleMapper("«node.URI»", "«(cond).toNodeLabel
						»", «id», «typedNode.input.type.name.toFirstLower» -> «node.call.javaCode(new ParamMap(typedNode.input.type))»)'''
				else if (repeatable)
				'''actionFactory.<«typedNode.input.getEither», «
									typedNode.output.getEither», «typedNode.inputKey.getEither»>newRosettaRepeatableMapper("«node.URI»", "«(cond).toNodeLabel
														»", «id», «typedNode.input.type.name.toFirstLower» -> «node.call.javaCode(new ParamMap(typedNode.input.type))»)'''
				else
				'''actionFactory.<«typedNode.input.getEither», «
									typedNode.output.getEither», «typedNode.inputKey.getEither»>newRosettaMultipleMapper("«node.URI»", "«(cond).toNodeLabel
														»", «id», «typedNode.input.type.name.toFirstLower» -> «node.call.javaCode(new ParamMap(typedNode.input.type))»)'''
			}
			BlueprintReturn: {
				context.imports.addTypes(typedNode)
				context.imports.addMappingImport()
				
				val expr = node.expression 
				'''actionFactory.<«typedNode.input.getEither», «typedNode.output.getEither», «typedNode.inputKey.getEither»> newRosettaReturn("«node.URI»", "«expr.toNodeLabel»",  «id»,  () -> «expr.javaCode(new ParamMap())»)'''
			}
			BlueprintLookup: {
				context.imports.addTypes(typedNode)
				context.imports.addMappingImport()
				val nodeName = if (node.identifier !== null) node.identifier else node.name
				//val lookupLamda = '''«typedNode.input.type.name.toFirstLower» -> lookup«node.name»(«typedNode.input.type.name.toFirstLower»)'''
				'''actionFactory.<«typedNode.input.getEither», «
					typedNode.output.getEither», «typedNode.inputKey.getEither»>newRosettaLookup("«node.URI»", "«nodeName»", «id», "«node.name»")'''
			
			}
			BlueprintOr : {
				node.orNode(typedNode, context, id)
			}
			BlueprintRef : {
				context.addBPRef(typedNode)
				context.imports.addTypes(typedNode)
				context.imports.addBPRef(node.blueprint)
				'''get«node.blueprint.name.toFirstUpper»()«IF node.identifier!==null»)
				.then(new IdChange("«node.URI»", "as «node.identifier»", «id»)«ENDIF»'''				
			}
			BlueprintFilter :{
				context.imports.addFilter(node);
				if(node.filter!==null) {
				'''new Filter<«typedNode.input.either», «typedNode.inputKey.either»>("«node.URI»", "«node.filter.toNodeLabel»", «typedNode.input.either.toFirstLower
					» -> «node.filter.javaCode(new ParamMap(typedNode.input.type))».get(), «id»)'''
				}
				else {
					context.addBPRef(typedNode)
					'''new FilterByRule<«typedNode.input.either», «typedNode.inputKey.either»>("«node.URI»", "«node.filterBP.blueprint.name»", 
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
	
	def createIdentifier(BlueprintNode node) {
		switch (node) {
			BlueprintExtract: {
				val nodeName = if (node.identifier !== null) node.identifier 
								else if (node.name !== null) node.name
								else node.call.toNodeLabel
				'''new RuleIdentifier("«nodeName»", getClass())'''
			}
			BlueprintReturn: {
				val nodeName = if (node.identifier !== null) node.identifier 
								else if (node.name !== null) node.name
								else node.expression.toNodeLabel
				
				'''new RuleIdentifier("«nodeName»", getClass())'''
			}
			BlueprintLookup: {
				val nodeName = if (node.identifier !== null) node.identifier else node.name
				'''new RuleIdentifier("Lookup «nodeName»", getClass())'''
			}
			default: {
				if (node.identifier!==null) {
					'''new RuleIdentifier("«node.identifier»", getClass())'''
				}
				else {
					'''null'''
				}
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
	
	def StringConcatenationClient orNode(BlueprintOr orNode, TypedBPNode orTyped, Context context, CharSequence id) {
		'''
		«IF !orNode.bps.isEmpty»
			BlueprintBuilder.<«orTyped.outFullS»>or(actionFactory,
				«FOR bp:orNode.bps.indexed  SEPARATOR ","»
				startsWith(actionFactory, «bp.value.buildGraph(orTyped.orNodes.get(bp.key), context)»)
				«ENDFOR»
				)
			«ENDIF»
		'''
	}
	
	def getOutFullS(TypedBPNode node) {
		'''«node.input.either», «node.output.either», «node.inputKey.either», «node.outputKey.either»'''
	}
	
	def StringConcatenationClient blueprintRef(String ref, TypedBPNode typedNode, Context context) 	
		'''
		@«Inject» private «ref.toFirstUpper»Rule «ref.toFirstLower»Ref;
		protected BlueprintInstance «typedNode.typeArgs» get«ref.toFirstUpper»() {
			return «ref.toFirstLower»Ref.blueprint();
		}'''
	
		
	protected def CharSequence typeArgs(TypedBPNode typedNode)
		'''<«typedNode.input.toString», «typedNode.output.toString», «typedNode.inputKey.toString», «typedNode.outputKey.toString»>'''
		
	
	def getSource(String source, TypedBPNode node, Context context)
	'''
		protected SourceNode<«node.output.either», «node.outputKey.either»> get«source.toFirstUpper()»() {
			throw new UnsupportedOperationException();
		}
	'''
	
	def genExtends(BindableType type) {
		val typeS = type.either;
		if (typeS=="?") return typeS
		if (typeS=="Object") return "?"
		else return "? extends "+ typeS 
	}
	
	def fullname(RosettaType type, RosettaJavaPackages packageName) {
		if (type instanceof com.regnosys.rosetta.rosetta.simple.Data)
			'''«packageName.model.name».«type.name»'''.toString
		else 
			type.name.toJavaFullType
	}
	
	def Iterable<RosettaCallableWithArgs> functionDependencies(BlueprintNodeExp node) {
		return node.node.functionDependencies + (node.next===null?#[]:node.next.functionDependencies)
	}
	
	def Iterable<RosettaCallableWithArgs> functionDependencies(BlueprintNode node) {
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
	def generateReportBuilder(RosettaJavaPackages packageName, RosettaBlueprintReport report, String version, extension JavaNames names) {
		try {
			val imports = new ImportGenerator(packageName)
			imports.addDataItemReportBuilder(report.reportType)

			val StringConcatenationClient scc = report.buildDataItemReportBuilderBody(names, imports)
			val body = tracImports(scc)

			return '''
				package «packageName.model.blueprint.name»;
				
				«FOR imp : body.imports»
					import «imp»;
				«ENDFOR»
				«FOR imp : body.staticImports»
					import static «imp»;
				«ENDFOR»
				
				«FOR importClass : imports.imports.filter[imports.isImportable(it)]»
				import «importClass»;
				«ENDFOR»
				«FOR importClass : imports.staticImports»
				import static «importClass».*;
				«ENDFOR»
				
				«emptyJavadocWithVersion(version)»
				public class «report.reportType.name.toDataItemReportBuilderName» implements DataItemReportBuilder {
				
					«body.toString»
				}
				'''
			}
			catch (Exception e) {
				LOGGER.error("Error generating blueprint java for "+report.reportType.name, e);
				return '''Unexpected Error generating «report.reportType.name».java Please see log for details'''
			}
	}
	
	def StringConcatenationClient buildDataItemReportBuilderBody(RosettaBlueprintReport report, extension JavaNames names, ImportGenerator imports) {
		val reportTypeName = report.reportType.name
		val builderName = "dataItemReportBuilder"
		'''
		@Override
		public <T> «reportTypeName» buildReport(«Collection»<GroupableData<?, T>> reportData) {
			«reportTypeName».«reportTypeName»Builder «builderName» = «reportTypeName».builder();
			
			for (GroupableData<?, T> groupableData : reportData) {
				DataIdentifier dataIdentifier = groupableData.getIdentifier();
				if (dataIdentifier instanceof RuleIdentifier) {
					RuleIdentifier ruleIdentifier = (RuleIdentifier) dataIdentifier;
					Class<?> ruleType = ruleIdentifier.getRuleType();
					Object data = groupableData.getData();
					if (data == null) {
						continue;
					}
					«report.reportType.buildRules(builderName, names, imports)»
				}
			}
			
			return «builderName».build();
		}'''
	}
	
	def StringConcatenationClient buildRules(com.regnosys.rosetta.rosetta.simple.Data dataType, String builderPath, extension JavaNames names, ImportGenerator imports) {
		'''«FOR attr : dataType.allAttributes»
			«val attrEx = attr.toExpandedAttribute»
			«val attrType = attr.type»
			«val rule = attr.ruleReference?.reportingRule»
			«IF rule !== null»
				«imports.addDataItemReportRule(rule)»
				«IF attr.card.isIsMany»
					«IF attrType instanceof com.regnosys.rosetta.rosetta.simple.Data»
						«attrType.buildRules('''«builderPath».getOrCreate«attr.name.toFirstUpper»(ruleIdentifier.getRepeatableIndex().orElse(0))''', names, imports)»
					«ENDIF»
				«ELSE»
					if («rule.name»Rule.class.isAssignableFrom(ruleType)) {
						DataItemReportUtils.setField(«builderPath»::set«attr.name.toFirstUpper», «attrEx.type.toJavaType».class, data, «rule.name»Rule.class);
					}
				«ENDIF»
			«ELSEIF attrType instanceof com.regnosys.rosetta.rosetta.simple.Data»
				«IF !attr.card.isIsMany»
					«attrType.buildRules('''«builderPath».getOrCreate«attr.name.toFirstUpper»()''', names, imports)»
				«ENDIF»
			«ENDIF»
		«ENDFOR»
		'''	
	}
	
	def String toDataItemReportBuilderName(String dataItemReportTypeName) {
		'''«dataItemReportTypeName»_DataItemReportBuilder'''
	}
	
	@Data static class AttributePath {
		List<Attribute> path
		RosettaDocReference ref
	}
	
	@Data static class RegdOutputField {
		Attribute attrib
		RosettaDocReference ref
	}
	
	@Data static class Context {
		BlueprintNodeExp nodes
		ImportGenerator imports
		Map<String, TypedBPNode> bpRefs = newHashMap
		Map<String, TypedBPNode> sources = newHashMap
		
		def addBPRef(TypedBPNode node) {
			addBPRef(node.node, node)		
		}
		def dispatch addBPRef(BlueprintNode node, TypedBPNode nodeType) {
			LOGGER.error("unexpected node type adding bpRef")
			""
		}
		def dispatch addBPRef(BlueprintRef ref, TypedBPNode node) {
			bpRefs.put(ref.blueprint.name, node)
			imports.addBPRef(ref.blueprint)
		}
		
		def dispatch addBPRef(BlueprintFilter ref, TypedBPNode node) {
			bpRefs.put(ref.filterBP.blueprint.name, node.orNodes.get(0))
			imports.addBPRef(ref.filterBP.blueprint)
		}
		
		def addSource(BlueprintSource source, TypedBPNode typed) {
			sources.put(source.name, typed)
			imports.addSource(source, typed)
		}
		
	}
}
