package com.regnosys.rosetta.generator.java.blueprints

import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator.ParamMap
import com.regnosys.rosetta.generator.java.util.ImportGenerator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.rosetta.BlueprintAnd
import com.regnosys.rosetta.rosetta.BlueprintCustomNode
import com.regnosys.rosetta.rosetta.BlueprintDataJoin
import com.regnosys.rosetta.rosetta.BlueprintExtract
import com.regnosys.rosetta.rosetta.BlueprintFilter
import com.regnosys.rosetta.rosetta.BlueprintGroup
import com.regnosys.rosetta.rosetta.BlueprintLookup
import com.regnosys.rosetta.rosetta.BlueprintMerge
import com.regnosys.rosetta.rosetta.BlueprintNode
import com.regnosys.rosetta.rosetta.BlueprintNodeExp
import com.regnosys.rosetta.rosetta.BlueprintOneOf
import com.regnosys.rosetta.rosetta.BlueprintReduce
import com.regnosys.rosetta.rosetta.BlueprintRef
import com.regnosys.rosetta.rosetta.BlueprintReturn
import com.regnosys.rosetta.rosetta.BlueprintSource
import com.regnosys.rosetta.rosetta.BlueprintValidate
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaRegulatoryReference
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaTyped
import com.regnosys.rosetta.rosetta.UnimplementedNode
import com.regnosys.rosetta.validation.BindableType
import com.regnosys.rosetta.validation.RosettaBlueprintTypeResolver
import com.regnosys.rosetta.validation.TypedBPNode
import java.util.List
import java.util.Map
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.*

class BlueprintGenerator {
	
	@Inject extension ImportManagerExtension
	@Inject extension RosettaBlueprintTypeResolver
	@Inject extension ExpressionGenerator

	/**
	 * generate a blueprint java file
	 */
	def generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		elements.filter(RosettaBlueprintReport).forEach [ report |
			fsa.generateFile(packages.blueprint.directoryName + '/' + report.name + 'Report.java',
				generateBlueprint(packages, report.nodes, null, report.name, 'Report', report.URI, version))
		]
		
		elements.filter(RosettaBlueprint).forEach [ bp |
			fsa.generateFile(packages.blueprint.directoryName + '/' + bp.name + 'Rule.java',
				generateBlueprint(packages, bp.nodes, bp.output, bp.name, 'Rule', bp.URI, version))
		]
	}

	/**
	 * Generate the text of a blueprint
	 */
	def generateBlueprint(RosettaJavaPackages packageName, BlueprintNodeExp nodes, RosettaType output, String name, String type, String uri, String version) {
		val imports = new ImportGenerator(packageName)
		imports.addBlueprintImports
		imports.addSourceAndSink
		
		val typed = buildTypeGraph(nodes, output)
		val typeArgs = bindArgs(typed)
		imports.addTypes(typed)
		val body = tracImports(nodes.buildBody(typed, imports))
		
		return '''
			package «packageName.blueprint.packageName»;
			
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
			result += "INKEY extends Comparable<INKEY>"
			node.inputKey.genericName = "INKEY"
			first=false
		}
		if (!node.outputKey.bound) {
			if (!first) result+=", "
			result+="OUTKEY extends Comparable<OUTKEY>"
			node.outputKey.genericName = "OUTKEY"
		}
		if (result.length>0) return "<"+result+">"
	}
	
	/**
	 * build the body of the blueprint class
	 */
	def StringConcatenationClient buildBody(BlueprintNodeExp nodes, TypedBPNode typedNode, ImportGenerator imports) {
		val context = new Context(nodes, imports)
		return '''
			
			@Override
			public BlueprintInstance<«typedNode.input.either», «typedNode.output.either», «typedNode.inputKey.either», «typedNode.outputKey.either»> blueprint() { 
				return 
					startsWith(actionFactory, «nodes.buildGraph(typedNode.next, context)»)
					.toBlueprint(getURI(), getName());
			}
			«FOR unimplemented : context.customs.entrySet»
			
			«unimplemented.key.nodeDef(unimplemented.value,context)»
			«ENDFOR»
			«FOR merger : context.mergers.entrySet»
			
			«merger.key.merger(merger.value, context)»
			«ENDFOR»
			«FOR merger : context.mergers.entrySet»
			
			«merger.key.simpleMerger(merger.value, context)»
			«ENDFOR»
			«FOR merger : context.mergers.entrySet»
			
			«merger.key.supplier(merger.value, context)»
			«ENDFOR»
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
		«nodeExp.node.buildNode(typedNode, context)»«IF nodeExp.next!==null»)
		.then(«nodeExp.next.buildGraph(typedNode.next, context)»«ENDIF»'''
	
	/**
	 * write out an individual graph node
	 */
	def StringConcatenationClient buildNode(BlueprintNode node, TypedBPNode typedNode, Context context) {
		switch (node) {
			BlueprintMerge: {
				context.addMerger(node, typedNode)
				'''new Merger<>("«node.URI»", "Create «node.output.name»", «node.toFunctionName»(), this::«node.toFunctionName()»Supplier, «node.output.name».«node.output.name»Builder::build, 
					new StringIdentifier("«node.output.name»"), false)'''
			}
			BlueprintExtract: {
				context.imports.addTypes(typedNode)
				context.imports.addSingleMapping(node)
				
				val cond = node.call
				val nodeName = if (node.identifier !== null) node.identifier 
								else if (node.name !== null) node.name
								else cond.toNodeLabel
				
				val id = '''new StringIdentifier("«nodeName»")'''
				if (!node.multiple)
				'''actionFactory.<«typedNode.input.getEither», «
					typedNode.output.getEither», «typedNode.inputKey.getEither»>newRosettaSingleMapper("«node.URI»", "«(cond).toNodeLabel
						»", «id», «typedNode.input.type.name.toFirstLower» -> «node.call.javaCode(new ParamMap(typedNode.input.type))»)'''
				else
				'''actionFactory.<«typedNode.input.getEither», «
									typedNode.output.getEither», «typedNode.inputKey.getEither»>newRosettaMultipleMapper("«node.URI»", "«(cond).toNodeLabel
														»", «id», «typedNode.input.type.name.toFirstLower» -> «node.call.javaCode(new ParamMap(typedNode.input.type as RosettaType))»)'''
			}
			BlueprintReturn: {
				context.imports.addTypes(typedNode)
				context.imports.addMappingImport()
				
				val expr = node.expression 
				val nodeName = if (node.identifier !== null) node.identifier 
								else if (node.name !== null) node.name
								else expr.toNodeLabel
				
				val id = '''new StringIdentifier("«nodeName»")'''
				'''actionFactory.<«typedNode.input.getEither», «typedNode.output.getEither», «typedNode.inputKey.getEither»> newRosettaReturn("«node.URI»", "«expr.toNodeLabel»",  «id»,  () -> «expr.javaCode(new ParamMap())»)'''
			}
			BlueprintLookup: {
				context.imports.addTypes(typedNode)
				context.imports.addMappingImport()
				val nodeName = if (node.identifier !== null) node.identifier else node.name
				//val lookupLamda = '''«typedNode.input.type.name.toFirstLower» -> lookup«node.name»(«typedNode.input.type.name.toFirstLower»)'''
				val id = '''new StringIdentifier("Lookup «nodeName»")'''
				'''actionFactory.<«typedNode.input.getEither», «
					typedNode.output.getEither», «typedNode.inputKey.getEither»>newRosettaLookup("«node.URI»", "«nodeName»", «id», "«node.name»")'''
			
			}
			BlueprintAnd : {
				node.andNode(typedNode, context)
			}
			BlueprintOneOf : {
				context.addIfElse(node, typedNode)
				node.ifElse(typedNode, context)
			}
			BlueprintRef : {
				context.addBPRef(typedNode)
				context.imports.addTypes(typedNode)
				'''get«node.blueprint.name.toFirstUpper»()'''
			}
			BlueprintValidate : {
				context.imports.addValidate(node);
				'''actionFactory.<«node.input.name», «typedNode.inputKey.either»>newRosettaValidator("«node.URI»", "Validate Rosetta", «node.input.name».class)'''
			}
			BlueprintFilter :{
				context.imports.addFilter(node);
				if(node.filter!==null) {
				'''new Filter<«typedNode.input.either», «typedNode.inputKey.either»>("«node.URI»", "«node.filter.toNodeLabel»", «typedNode.input.either.toFirstLower
					» -> «node.filter.javaCode(new ParamMap(typedNode.input.type))».get())'''
				}
				else {
					'''new FilterByRule<«typedNode.input.either», «typedNode.inputKey.either»>("«node.URI»", "«node.filterBP.blueprint.name»", new «node.filterBP.blueprint.name»Rule<«typedNode.inputKey.either»>(actionFactory).blueprint())'''
				}
			}
			BlueprintReduce : {
				context.imports.addReduce(node);
				if (node.expression!==null) {
					'''new ReduceBy<«typedNode.input.either», «node.expression.getOutput.name.toJavaType», «typedNode.inputKey.either»>("«
					node.URI»", "«node.expression.toNodeLabel»", ReduceBy.Action.«node.action.toUpperCase», 
					«typedNode.input.either.toFirstLower» -> «node.expression.javaCode(new ParamMap(typedNode.input.type))».get())'''
				}
				else {
					'''new ReduceBy<«typedNode.input.either», Integer, «typedNode.inputKey.either»>("«node.URI»", "«node.expression.toNodeLabel»
					", ReduceBy.Action.«node.action.toUpperCase»)'''
				}
			}
			BlueprintDataJoin: {
				context.imports.addJoin(node)
				val joinInput = (node.key as RosettaFeatureCall).input;
				val foreignInput = (node.foreign as RosettaFeatureCall).input
				val fkType = (node.key as RosettaFeatureCall).output
				'''
				actionFactory.<«joinInput.name», «foreignInput.name», «typedNode.inputKey.either», «fkType.name.toJavaType»>newRosettaDataJoin("«node.URI»", "join«joinInput.name»", «joinInput.name.toFirstLower» -> «node.key.javaCode(new ParamMap(joinInput))»,
						«foreignInput.name.toFirstLower» -> «node.foreign.javaCode(new ParamMap(foreignInput))»,
						«joinInput.name».class, «foreignInput.name».class)'''
			}
			BlueprintSource: {
				context.addSource(node, typedNode)
				'''get«node.name.toFirstUpper»()'''
			}
			BlueprintCustomNode : {
				context.addCustom(node, typedNode)
				'''«node.toFunctionName»()'''
			}
			BlueprintGroup : {
				context.imports.addGrouper(node);
				'''actionFactory.<«typedNode.input.either», «typedNode.inputKey.either», «typedNode.outputKey.either
				»>newRosettaGrouper("«node.URI»", "group by «node.key.toNodeLabel»", «typedNode.input.type.name.toFirstLower» -> «node.key.javaCode(new ParamMap(typedNode.input.type))»)'''
			}
			default: {
				throw new UnsupportedOperationException("Can't generate code for node of type "+node.class)
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
	
	def StringConcatenationClient andNode(BlueprintAnd andNode, TypedBPNode andTyped, Context context) {
		'''
		BlueprintBuilder.<«andTyped.outFullS»>and(actionFactory,
			«FOR bp:andNode.bps.indexed  SEPARATOR ","»
			startsWith(actionFactory, «bp.value.buildGraph(andTyped.andNodes.get(bp.key), context)»)
			«ENDFOR»
			)
		'''
	}
	
	def StringConcatenationClient ifElse(BlueprintOneOf node, TypedBPNode andTyped, Context context) {
		'''ifElse(actionFactory, getURI(), getName(),
				«FOR bp:node.bps.indexed SEPARATOR ","»
					new BlueprintIfThen(startsWith(actionFactory, «bp.value.ifNode.buildGraph(andTyped.ifNodes.get(bp.key), context)»),
						startsWith(actionFactory, «bp.value.thenNode.buildGraph(andTyped.andNodes.get(bp.key), context)»))
				«ENDFOR»
				«IF node.elseNode!==null»,
					new BlueprintIfThen(
						startsWith(actionFactory, «node.elseNode.buildGraph(andTyped.andNodes.get(node.bps.size), context)»))
				«ENDIF»
		)'''
	}
	
	def getOutFullS(TypedBPNode node) {
		'''«node.input.either», «node.output.either», «node.inputKey.either», «node.outputKey.either»'''
	}

	private def toFunctionName(BlueprintCustomNode node) '''get«node.name.toFirstUpper»'''
	private def toFunctionName(BlueprintMerge node) 
	'''merge«node.output.name»'''
	
	private def nodeDef(BlueprintCustomNode node, TypedBPNode typed, Context context) 
	'''protected Node<«typed.outFullS»> «node.toFunctionName»() {
			throw new UnsupportedOperationException();
	}'''
	
	/**
	 * Creates an place holder method to return the function used by a merger node
	 */
	def merger(BlueprintMerge merge, TypedBPNode types, Context context) {
//		val refs = context.blueprint.references.flatMap[merge.findOutputRefs(it)]
//		context.addMergeFuncs(merge, refs);
		return '''protected Function<DataIdentifier, BiConsumer<«merge.output.name».«merge.output.name»Builder, «genExtends(types.input)»>> merge«merge.output.name»() {
					throw new UnsupportedOperationException();
			}'''
	}
	
	/**
	 * creates an place holder function to return the builder for a merger
	 */
	def supplier(BlueprintMerge merge, TypedBPNode mergeTypes, Context context) {
		return '''protected «merge.output.name».«merge.output.name»Builder «merge.toFunctionName»Supplier(«mergeTypes.inputKey.getEither» k) {
					throw new UnsupportedOperationException();
			}'''
	}
	
	def blueprintRef(String ref, TypedBPNode typedNode, Context context) {	
		'''
		protected BlueprintInstance «typedNode.typeArgs» get«ref.toFirstUpper»() {
			Blueprint«typedNode.typeArgs» rule = new «ref.toFirstUpper»Rule<>(actionFactory);
			return rule.blueprint();
		}'''
	}
		
	protected def CharSequence typeArgs(TypedBPNode typedNode)
		'''<«typedNode.input.toString», «typedNode.output.toString», «typedNode.inputKey.toString», «typedNode.outputKey.toString»>'''
		
	
	def simpleMerger(BlueprintMerge merge, TypedBPNode types, Context context) {
		val outputRefs = context.mergerFuncs.get(merge)
		return '''
		protected Map<DataIdentifier, BiConsumer<«merge.output.name».«merge.output.name»Builder, «genExtends(types.input)»>> simpleMerge«merge.output.name»() {
			Map<DataIdentifier, BiConsumer<«merge.output.name».«merge.output.name»Builder, «genExtends(types.input)»>> result = new HashMap<>();
			«FOR outputRef: outputRefs»
««« TODO - add this in when things break			result.put(new RosettaIdentifier("«outputRef.ref.refId»"), (builder, input) -> builder.set«outputRef.attrib.name.toFirstUpper»(Converter.convert(«outputRef.attrib.type.name.toJavaType».class, input)));
			«ENDFOR»
			«FOR field : (merge.output).getAttributes»
			result.put(new StringIdentifier("«field.name»"), (builder, input) -> builder.set«field.name.toFirstUpper»(Converter.convert(«(field as RosettaTyped).type .name.toJavaType».class, input)));
			«ENDFOR»
			return result;
		}''' 
	}
	
	dispatch def  List<? extends RosettaNamed> getAttributes(RosettaClass type) {
		return type.regularAttributes
	}
	
	dispatch def List<? extends RosettaNamed> getAttributes(com.regnosys.rosetta.rosetta.simple.Data type) {
		return type.attributes
	}
	
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
	
	def findOutputRefs(BlueprintMerge merge, RosettaRegulatoryReference bpRef) {
//		val a = merge.output as RosettaClass
//		val b = a.regularAttributes
//		val c = b.filter[it.references.exists[matchesRef(bpRef)]]
//		val d = c.map[new RegdOutputField(it, it.references.filter[matchesRef(bpRef)].last)]
// TODO - add this back in 
		return newArrayList
	}
	
	def fullname(RosettaType type, RosettaJavaPackages packageName) {
		if (type instanceof RosettaClass || type instanceof com.regnosys.rosetta.rosetta.simple.Data)
			'''«packageName.model.packageName».«type.name»'''.toString
		else 
			type.name.toJavaFullType
	}

	
	@Data static class AttributePath {
		List<RosettaRegularAttribute> path
		RosettaRegulatoryReference ref
	}
	
	@Data static class RegdOutputField {
		RosettaRegularAttribute attrib
		RosettaRegulatoryReference ref
	}
	
	@Data static class Context {
		BlueprintNodeExp nodes
		ImportGenerator imports
		Map<BlueprintMerge, TypedBPNode> mergers = newHashMap
		Map<BlueprintMerge,  Iterable<RegdOutputField>> mergerFuncs = newHashMap
		List<UnimplementedNode> unimplemened = newArrayList
		Map<String, TypedBPNode> bpRefs = newHashMap
		Map<BlueprintCustomNode, TypedBPNode> customs = newHashMap
		Map<String, TypedBPNode> sources = newHashMap
		
		def void addMergeFuncs(BlueprintMerge merge, Iterable<RegdOutputField> fields) {
			mergerFuncs.put(merge, fields)
			imports.addSimpleMerger(merge, fields)
		}
		
		def addMerger(BlueprintMerge merge, TypedBPNode node) {
			mergers.put(merge, node)
		}
		
		def addBPRef(TypedBPNode node) {
			bpRefs.put((node.node as BlueprintRef).blueprint.name, node)
		}
		
		def addIfElse(BlueprintOneOf oneOf, TypedBPNode node) {
			imports.addIfThen(oneOf);
		}
		
		def addCustom(BlueprintCustomNode node, TypedBPNode typed) {
			customs.put(node, typed);
			imports.addNode(typed)
		}
		
		def addSource(BlueprintSource source, TypedBPNode typed) {
			sources.put(source.name, typed)
			imports.addSource(source, typed)
		}
		
	}
}
