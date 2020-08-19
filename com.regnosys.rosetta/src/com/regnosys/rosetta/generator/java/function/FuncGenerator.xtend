package com.regnosys.rosetta.generator.java.function

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.expression.Context
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator
import com.regnosys.rosetta.generator.java.expression.ExpressionGenerator.ParamMap
import com.regnosys.rosetta.generator.java.expression.ExpressionGeneratorWithBuilder
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.generator.util.Util
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.rosetta.simple.Operation
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RAnnotateType
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.rosetta.model.lib.functions.IQualifyFunctionExtension
import com.rosetta.model.lib.functions.Mapper
import com.rosetta.model.lib.functions.MapperBuilder
import com.rosetta.model.lib.functions.MapperS
import com.rosetta.model.lib.functions.RosettaFunction
import com.rosetta.model.lib.math.BigDecimalExtensions
import com.rosetta.model.lib.validation.ModelObjectValidator
import java.util.Map
import java.util.Optional
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.naming.QualifiedName

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

class FuncGenerator {

	@Inject ExpressionGenerator expressionGenerator
	@Inject ExpressionGeneratorWithBuilder expressionWithBuilder
	@Inject RosettaFunctionDependencyProvider functionDependencyProvider
	@Inject RosettaTypeProvider typeProvider
	@Inject extension RosettaFunctionExtensions
	@Inject extension RosettaExtensions
	@Inject ExpressionHelper exprHelper
	@Inject extension ImportManagerExtension
	@Inject  CardinalityProvider cardinality
	@Inject JavaNames.Factory factory 

	def void generate(JavaNames javaNames, IFileSystemAccess2 fsa, Function func, String version) {
		val fileName = javaNames.packages.model.functions.directoryName + '/' + func.name + '.java'

		val dependencies = collectFunctionDependencies(func)

		val classBody = if (func.handleAsEnumFunction) {
				tracImports(func.dispatchClassBody(func.name, dependencies, javaNames, version), func.name)
			} else {
				tracImports(func.classBody(func.name, dependencies, javaNames, version, false), func.name)
			}
		val content = '''
			package «javaNames.packages.model.functions.name»;
			
			«FOR imp : classBody.imports»
				import «imp»;
			«ENDFOR»
			
			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«classBody.toString»
		'''
		fsa.generateFile(fileName, content)
	}
	
	private def collectFunctionDependencies(Function func) {
		val deps = func.shortcuts.flatMap[functionDependencyProvider.functionDependencies(it.expression)] +
			func.operations.flatMap[functionDependencyProvider.functionDependencies(it.expression)]
		val condDeps = (func.conditions + func.postConditions).map[expression].flatMap [
			functionDependencyProvider.functionDependencies(it)
		]
		return Util.distinctBy(deps + condDeps, [name]).sortBy[it.name]
	}

	private def StringConcatenationClient classBody(Function func, String className,
		Iterable<? extends RosettaCallableWithArgs> dependencies, extension JavaNames names, String version, boolean isStatic) {
//		val isAbstract = func.hasCalculationAnnotation
		val output = getOutput(func)
		val inputs = getInputs(func)
		val outputName = output?.name
		val outputType = func.outputTypeOrVoid(names)
		val aliasOut = func.shortcuts.toMap([it], [exprHelper.usesOutputParameter(it.expression)])
		val outNeedsBuilder = needsBuilder(output)
		'''
			@«ImplementedBy»(«className».«className»Default.class)
			public «IF isStatic»static «ENDIF»abstract class «className» implements «RosettaFunction»«IF func.isQualifierFunction()», «IQualifyFunctionExtension»<«inputs.head.toListOrSingleJavaType»>«ENDIF» {
				«IF outNeedsBuilder»
				
				@«Inject» protected «ModelObjectValidator» objectValidator;
				«ENDIF»
				«IF !dependencies.empty»
					
					// RosettaFunction dependencies
					//
				«ENDIF»
				«FOR dep : dependencies»
					@«Inject» protected «dep.toJavaType» «dep.name.toFirstLower»;
				«ENDFOR»
			
				/**
				«FOR input : inputs»
					* @param «input.name» «ModelGeneratorUtil.escape(input.definition)»
				«ENDFOR»
				«IF output !== null»
					* @return «outputName» «ModelGeneratorUtil.escape(output.definition)»
				«ENDIF»
				*/
				public «outputType» evaluate(«func.inputsAsParameters(names)») {
					«IF !func.conditions.empty»
						// pre-conditions
						«FOR cond:func.conditions»
						
							«cond.contributeCondition»
						«ENDFOR»
					«ENDIF»
					
					«output.toBuilderType(names)» «outputName»Holder = doEvaluate(«func.inputsAsArguments(names)»);
					«outputType» «outputName» = assignOutput(«outputName»Holder«IF !inputs.empty», «ENDIF»«func.inputsAsArguments(names)»)«IF outNeedsBuilder».build()«ENDIF»;
					
					«IF !func.postConditions.empty»
						// post-conditions
						«FOR cond:func.postConditions»

							«cond.contributeCondition»
						«ENDFOR»
					«ENDIF»
					«IF outNeedsBuilder»
					objectValidator.validateAndFailOnErorr(«outputType».class, «outputName»);
					«ENDIF»
					return «outputName»;
				}
				
				private «output.toBuilderType(names)» assignOutput(«output.toBuilderType(names)» «outputName»Holder«IF !inputs.empty», «ENDIF»«func.inputsAsParameters(names)») {
					«FOR indexed : func.operations.indexed»
						«IF outNeedsBuilder»«IF indexed.key == 0»@«SuppressWarnings»("unused") «outputType» «ENDIF»«outputName» = «outputName»Holder.build();«ENDIF»
						«indexed.value.assign(aliasOut, names)»;
					«ENDFOR»
					return «outputName»Holder;
				}

				protected abstract «output.toBuilderType(names)» doEvaluate(«func.inputsAsParameters(names)»);
				
				«FOR alias : func.shortcuts»
					
					«IF aliasOut.get(alias)»
						protected «names.shortcutJavaType(alias)» «alias.name»(«output.toBuilderType(names)» «outputName», «IF !inputs.empty»«func.inputsAsParameters(names)»«ENDIF») {
							return «expressionWithBuilder.toJava(alias.expression, Context.create(names))»;
						}
					«ELSE»
						protected «IF needsBuilder(alias)»«MapperBuilder»«ELSE»«Mapper»«ENDIF»<«toJavaType(typeProvider.getRType(alias.expression))»> «alias.name»(«func.inputsAsParameters(names)») {
							return «expressionGenerator.javaCode(alias.expression, new ParamMap)»;
						}
					«ENDIF»
				«ENDFOR»
				public static final class «className»Default extends «className» {
					@Override
					protected  «output.toBuilderType(names)» doEvaluate(«func.inputsAsParameters(names)») {
						return «IF outNeedsBuilder»«output.toListOrSingleJavaType».builder()«ELSE»null«ENDIF»;
					}
				}
				«IF func.isQualifierFunction()»
				
				@Override
				public String getNamePrefix() {
					return "«getQualifierAnnotations(func).head.annotation.prefix»";
				}
				«ENDIF»
			}
		'''
	}
	
	def private StringConcatenationClient dispatchClassBody(Function function,String className, Iterable<? extends RosettaCallableWithArgs> dependencies, extension JavaNames names, String version) {
		val dispatchingFuncs = function.dispatchingFunctions.sortBy[name].toList
		val enumParam = function.inputs.filter[type instanceof RosettaEnumeration].head.name
		val outputType = function.outputTypeOrVoid(names)
		'''
		«emptyJavadocWithVersion(version)»
		public class «className» {
			«FOR dep : dependencies»
				@«Inject» protected «dep.toJavaType» «dep.name.toFirstLower»;
			«ENDFOR»
			
			«FOR enumFunc : dispatchingFuncs»
				@«Inject» protected «toTargetClassName(enumFunc)» «toTargetClassName(enumFunc).lastSegment»;
			«ENDFOR»
			
			public «outputType» evaluate(«function.inputsAsParameters(names)») {
				switch («enumParam») {
					«FOR enumFunc : dispatchingFuncs»
						«val enumValClass = toTargetClassName(enumFunc).lastSegment»
						case «enumValClass»:
							return «enumValClass».evaluate(«function.inputsAsArguments(names)»);
					«ENDFOR»
					default:
						throw new IllegalArgumentException("Enum value not implemented: " + «enumParam»);
				}
			}
			
			«FOR enumFunc : dispatchingFuncs»
			
			«val enumValClass = toTargetClassName(enumFunc).lastSegment»
			«enumFunc.classBody(enumValClass, collectFunctionDependencies(enumFunc), names,  version, true)»
			«ENDFOR»
		}'''
	}
	
	
	private def QualifiedName toTargetClassName(FunctionDispatch ele) {
		return QualifiedName.create(ele.name).append(ele.value.value.name)
	}
	
	private def StringConcatenationClient assign(Operation op, Map<ShortcutDeclaration, Boolean> outs,
		JavaNames names) {
		val pathAsList = op.pathAsSegmentList
		val ctx = Context.create(names)
		if (pathAsList.isEmpty)
			'''
			«IF needsBuilder(op.assignRoot)»
				«op.assignTarget(outs, names)» = «expressionWithBuilder.toJava(op.expression, ctx)»
			«ELSE»
				«op.assignTarget(outs, names)» = «assignPlainValue(op, ctx)»«ENDIF»'''
		else {
			'''
				«op.assignTarget(outs, names)»
					«FOR seg : pathAsList»«IF seg.next !== null».getOrCreate«seg.attribute.name.toFirstUpper»(«IF seg.attribute.many»«seg.index?:0»«ENDIF»)«IF isReference(seg.attribute)».getOrCreateValue()«ENDIF»«ELSE»
					.«IF seg.attribute.isMany»add«ELSE»set«ENDIF»«
					seg.attribute.name.toFirstUpper»«IF op.namedAssignTarget().reference && !op.assignAsKey»Ref«ENDIF
					»(«op.assignValue(names)»«IF op.useIdx», «op.idx»«ENDIF»)«
					ENDIF»«ENDFOR»
			'''
		}
	}
	
	private def JavaType referenceWithMetaJavaType(Operation op, JavaNames names) {
			if (op.path === null) {
				val valueRType = typeProvider.getRType(op.assignRoot)
			 	names.createJavaType(names.packages.model.metaField, "ReferenceWithMeta" + valueRType.name.toFirstUpper)
			} else {
				val attr = op.pathAsSegmentList.last.attribute
				val valueRType = typeProvider.getRType(attr)
			 	names.createJavaType(factory.create(attr.type.model).packages.model.metaField, "ReferenceWithMeta" + valueRType.name.toFirstUpper)
			}
	}
	
	private def StringConcatenationClient assignValue(Operation op, JavaNames names) {
		if(op.assignAsKey) {
			val metaClass = referenceWithMetaJavaType(op, names)
			if (cardinality.isMulti(op.expression)) {
				/*
				.addParty(
					MapperS.of(parties(product, partyA, partyB, quantity).get())
					.getItems().map(
							(item) -> ReferenceWithMetaParty.builder().setGlobalReference(item.getMappedObject().getMeta().getGlobalKey()).build()
						).collect(Collectors.toList())
					);
				*/
				'''
				«expressionGenerator.javaCode(op.expression, new ParamMap)»
				.getItems().map(
						(item) -> «metaClass».builder().setGlobalReference(item.getMappedObject().getMeta().getGlobalKey()).build()
					).collect(«Collectors».toList())
				'''
			} else {
				//  ReferenceWithMetaEvent.builder().setGlobalReference(MapperS.of(executionEvent).get().getMeta().getGlobalKey()).build()
				'''
				«metaClass».builder().setGlobalReference(
						«Optional».ofNullable(«expressionGenerator.javaCode(op.expression, new ParamMap)».get())
							.map(r -> r.getMeta())
							.map(m -> m.getGlobalKey())
							.orElse(null)
					).build()
				'''
			}
		} else {
		'''«expressionGenerator.javaCode(op.expression, new ParamMap)»«
							IF cardinality.isMulti(op.expression)».getMulti()«ELSE».get()«ENDIF»'''
		}
	}
	
	private def StringConcatenationClient assignPlainValue(Operation operation, Context ctx) {
		val valType = typeProvider.getRType(operation.expression)
		if (operation.path === null && operation.assignRoot instanceof Attribute) {
			val assignRootType = typeProvider.getRType((operation.assignRoot as Attribute ).type)
			if (assignRootType === RBuiltinType.NUMBER && valType !== RBuiltinType.NUMBER) {
				// / case: number = 1
				return '''«BigDecimalExtensions».valueOf(«MapperS».of(«expressionWithBuilder.toJava(operation.expression, ctx)»))'''
			}
		}
		'''«expressionGenerator.javaCode(operation.expression,  new ParamMap)».get()'''
	}
	
	def boolean hasMeta(RType type) {
		if(type instanceof RAnnotateType) {
			type.hasMeta
		}
		
		false
	}
	
	private def boolean useIdx(Operation operation) {
		if (operation.pathAsSegmentList.nullOrEmpty)
			return false
		return operation.idx !== null
	}
	
	private def idx(Operation operation) {
		operation.pathAsSegmentList.last.index
	}
	
	private def boolean isReference(RosettaNamed ele) {
		switch(ele) {
			Annotated: hasMetaReferenceAnnotations(ele)
			RosettaRegularAttribute: !ele.metaTypes.empty
			default:false
		}
	}

	private def namedAssignTarget(Operation operation) {
		if (operation.path === null) {
			return operation.assignRoot
		} else {
			operation.pathAsSegmentList.last.attribute
		}
	}
	private def StringConcatenationClient assignTarget(Operation operation, Map<ShortcutDeclaration, Boolean> outs,
		JavaNames names) {
		val root = operation.assignRoot
		switch (root) {
			Attribute: '''«root.name»Holder'''
			ShortcutDeclaration: '''«root.name»(«IF outs.get(root)»«getOutput(operation.function)?.name»Holder«IF !getInputs(operation.function).empty», «ENDIF»«ENDIF»«inputsAsArguments(operation.function, names)»)'''
		}
	}

	private def StringConcatenationClient contributeCondition(Condition condition) {
		'''
			assert
				«expressionGenerator.javaCode(condition.expression, null)».get()
				: "«condition.definition»";
		'''
	}

	private def JavaType outputTypeOrVoid(Function function, extension JavaNames names) {
		val out = getOutput(function)
		if (out === null) {
			names.voidType()
		} else {
			out.type.toJavaType()
		}
	}

	private def StringConcatenationClient inputsAsArguments(extension Function function, extension JavaNames names) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«input.name»«ENDFOR»'''
	}

	private def StringConcatenationClient inputsAsParameters(extension Function function, extension JavaNames names) {
		'''«FOR input : getInputs(function) SEPARATOR ', '»«input.toListOrSingleJavaType()» «input.name»«ENDFOR»'''
	}

	def private StringConcatenationClient shortcutJavaType(JavaNames names, ShortcutDeclaration feature) {
		val rType = typeProvider.getRType(feature.expression)
		val javaType = names.toJavaType(rType)
		'''«javaType»«IF needsBuilder(rType)».«javaType»Builder«ENDIF»'''
	}

	private def StringConcatenationClient toBuilderType(Attribute attr, JavaNames names) {
		val javaType = names.toJavaType(attr.type)
		'''«IF needsBuilder(attr)»«javaType».«javaType»Builder«ELSE»«javaType»«ENDIF»'''
	}

	private def isMany(RosettaFeature feature) {
		switch (feature) {
			RosettaRegularAttribute: feature.card.isMany
			Attribute: feature.card.isMany
			default: throw new IllegalStateException('Unsupported type passed ' + feature?.eClass?.name)
		}
	}
}
