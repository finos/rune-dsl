package com.regnosys.rosetta.generator.java.reports

import com.google.inject.ImplementedBy
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.config.RosettaConfiguration
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource
import com.regnosys.rosetta.rosetta.RosettaReport
import com.regnosys.rosetta.rosetta.RosettaRule
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.rosetta.model.lib.ModelSymbolId
import com.rosetta.model.lib.reports.Tabulator
import com.rosetta.model.lib.reports.Tabulator.Field
import com.rosetta.model.lib.reports.Tabulator.FieldImpl
import com.rosetta.model.lib.reports.Tabulator.FieldValue
import com.rosetta.model.lib.reports.Tabulator.FieldValueImpl
import com.rosetta.model.lib.reports.Tabulator.MultiNestedFieldValueImpl
import com.rosetta.model.lib.reports.Tabulator.NestedFieldValueImpl
import com.rosetta.util.DottedPath
import com.rosetta.util.types.JavaClass
import java.util.Arrays
import java.util.List
import java.util.Map
import java.util.Optional
import java.util.Set
import java.util.stream.Collectors
import javax.inject.Inject
import org.apache.commons.text.StringEscapeUtils
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import java.util.Objects

class TabulatorGenerator {
	private interface TabulatorContext {
		def boolean needsTabulator(Data type)
		def boolean isTabulated(Attribute attr)
		def JavaClass<Tabulator<?>> toTabulatorJavaClass(Data type)
		def Optional<RosettaRule> getRule(Attribute attr)
		def Function getFunction()
	}

	@org.eclipse.xtend.lib.annotations.Data
	private static class ReportTabulatorContext implements TabulatorContext {
		extension RosettaExtensions
		extension JavaTypeTranslator
		Map<Attribute, RosettaRule> ruleMap
		Optional<RosettaExternalRuleSource> ruleSource
		Set<Data> reportedTypes
		
		override needsTabulator(Data type) {
			reportedTypes.contains(type)
		}
		private def boolean needsTabulator(Data type, Set<Data> visited) {
			if (visited.add(type)) {
				type.allAttributes.exists[isTabulated(visited)]
			} else {
				false
			}
		}
		override boolean isTabulated(Attribute attr) {
			isTabulated(attr, newHashSet)
		}
		private def boolean isTabulated(Attribute attr, Set<Data> visited) {
			val attrType = attr.typeCall.type
			if (attrType instanceof Data) {
				needsTabulator(attrType, visited)
			} else {
				ruleMap.containsKey(attr)
			}
		}
		
		override toTabulatorJavaClass(Data type) {
			type.toTabulatorJavaClass(ruleSource)
		}
		
		override getRule(Attribute attr) {
			Optional.ofNullable(ruleMap.get(attr))
		}
		
		override getFunction() {
			throw new UnsupportedOperationException("getFunction not available for ReportTabulatorContext")
		}
	}
	@Deprecated
	@org.eclipse.xtend.lib.annotations.Data
	private static class ProjectionTabulatorContext implements TabulatorContext {
		extension JavaTypeTranslator
		Function projection
		
		override needsTabulator(Data type) {
			true
		}
		
		override isTabulated(Attribute attr) {
			true
		}
		
		override toTabulatorJavaClass(Data type) {
			type.toProjectionTabulatorJavaClass(projection)
		}
		
		override getRule(Attribute attr) {
			Optional.empty
		}
		
		override getFunction() {
			projection
		}
	}
	@org.eclipse.xtend.lib.annotations.Data
	private static class FunctionTabulatorContext implements TabulatorContext {
		extension JavaTypeTranslator
		Function function
		
		override needsTabulator(Data type) {
			true
		}
		
		override isTabulated(Attribute attr) {
			true
		}
		
		override toTabulatorJavaClass(Data type) {
			type.toTabulatorJavaClass(function)
		}
		
		override getRule(Attribute attr) {
			Optional.empty
		}
	}
	@org.eclipse.xtend.lib.annotations.Data
	private static class DataTabulatorContext implements TabulatorContext {
		extension JavaTypeTranslator typeTranslator

		override needsTabulator(Data type) {
			true
		}

		override isTabulated(Attribute attr) {
			true
		}

		override toTabulatorJavaClass(Data type) {
			typeTranslator.toTabulatorJavaClass(type)
		}

		override getRule(Attribute attr) {
			Optional.empty
		}

		override getFunction() {
			throw new UnsupportedOperationException("TODO: remove")
		}
	}
	
	@Inject RosettaTypeProvider typeProvider
	@Inject RosettaConfiguration rosettaConfiguration
	@Inject extension JavaTypeTranslator typeTranslator
	@Inject extension ImportManagerExtension
	
	@Inject extension RosettaExtensions extensions
	@Inject extension JavaTypeUtil
	
	def generate(IFileSystemAccess2 fsa, RosettaReport report) {
		val tabulatorClass = report.toReportTabulatorJavaClass
		val topScope = new JavaScope(tabulatorClass.packageName)

		val context = getContext(report.reportType, Optional.ofNullable(report.ruleSource))
		val classBody = report.reportType.mainTabulatorClassBody(context, topScope, tabulatorClass)
		val content = buildClass(tabulatorClass.packageName, classBody, topScope)
		fsa.generateFile(tabulatorClass.canonicalName.withForwardSlashes + ".java", content)
	}
	
	def generate(IFileSystemAccess2 fsa, Data type, Optional<RosettaExternalRuleSource> ruleSource) {
		val context = getContext(type, ruleSource)
		if (context.needsTabulator(type)) {
			val tabulatorClass = type.toTabulatorJavaClass(ruleSource)
			val topScope = new JavaScope(tabulatorClass.packageName)

			val classBody = type.tabulatorClassBody(context, topScope, tabulatorClass)
			val content = buildClass(tabulatorClass.packageName, classBody, topScope)
			fsa.generateFile(tabulatorClass.canonicalName.withForwardSlashes + ".java", content)
		}
	}
	
	def generate(IFileSystemAccess2 fsa, Data type) {
		if (type.isDataTabulatable) {
			val context = createDataTabulatorContext(typeTranslator)

			val tabulatorClass = type.toTabulatorJavaClass
			val topScope = new JavaScope(tabulatorClass.packageName)

			generateTabulator(type, context, topScope, tabulatorClass, fsa)
		}
	}

	def generate(IFileSystemAccess2 fsa, Function func) {
		if (func.isFunctionTabulatable) {
			val tabulatorClass = func.toApplicableTabulatorClass
			val topScope = new JavaScope(tabulatorClass.packageName)
			
			val functionOutputType = typeProvider.getRTypeOfSymbol(func.output)
			if (functionOutputType instanceof RDataType) {
				val context = createFunctionTabulatorContext(typeTranslator, func)
				
				generateTabulator(functionOutputType.data, context, topScope, tabulatorClass, fsa)
			}
		}
	}

	private def void generateTabulator(Data type, TabulatorContext context, JavaScope topScope, JavaClass<Tabulator<?>> tabulatorClass, IFileSystemAccess2 fsa) {
		if (!tabulatorClass.equals(context.toTabulatorJavaClass(type))) {
			val classBody = type.mainTabulatorClassBody(context, topScope, tabulatorClass)
			val content = buildClass(tabulatorClass.packageName, classBody, topScope)
			fsa.generateFile(tabulatorClass.canonicalName.withForwardSlashes + ".java", content)
		}
		
		recursivelyGenerateFunctionTypeTabulators(fsa, type, context, newHashSet)
	}

	private def void recursivelyGenerateFunctionTypeTabulators(IFileSystemAccess2 fsa, Data type, TabulatorContext context, Set<Data> visited) {
		if (visited.add(type)) {
			val tabulatorClass = context.toTabulatorJavaClass(type)
			val topScope = new JavaScope(tabulatorClass.packageName)
			
			val classBody = type.tabulatorClassBody(context, topScope, tabulatorClass)
			val content = buildClass(tabulatorClass.packageName, classBody, topScope)
			fsa.generateFile(tabulatorClass.canonicalName.withForwardSlashes + ".java", content)
		
			type
				.allNonOverridesAttributes
				.map[typeProvider.getRTypeOfSymbol(it)]
				.filter(RDataType)
				.forEach[recursivelyGenerateFunctionTypeTabulators(fsa, data, context, visited)]
		}
	}
	
	private def ReportTabulatorContext getContext(Data type, Optional<RosettaExternalRuleSource> ruleSource) {
		val ruleMap = newHashMap
		type.getAllReportingRules(ruleSource).forEach[key, rule| ruleMap.put(key.attr, rule)]
		val reportedTypes = type.getAllReportedTypes
		println("    ")
		println("all collected rule types for type " + type.name)
		reportedTypes.forEach[println("    type " + it.name)]
		
		new ReportTabulatorContext(extensions, typeTranslator, ruleMap, ruleSource, reportedTypes)
	}
	
	private def boolean isFunctionTabulatable(Function func) {
		if (shouldGenerateLegacyTabulator) {
			return func.isAnnotatedWith("projection")
		} else {
			val annotations = rosettaConfiguration.generators.tabulators.annotations
			annotations.findFirst[func.isAnnotatedWith(it)] !== null
		}
	}
	
	private def boolean isDataTabulatable(Data type) {
		val types = rosettaConfiguration.generators.tabulators.types
		val fqn = String.format("%s.%s", type.model.name, type.name)
		types.contains(fqn)
	}

	private def boolean isAnnotatedWith(Function func, String with) {
		func.annotations.findFirst[annotation.name == with] !== null
	}
	
	private def boolean shouldGenerateLegacyTabulator() {
		rosettaConfiguration.generators.tabulators.annotations.empty
	}
	
	private def TabulatorContext createFunctionTabulatorContext(JavaTypeTranslator typeTranslator, Function func) {
		shouldGenerateLegacyTabulator ? new ProjectionTabulatorContext(typeTranslator, func) : new FunctionTabulatorContext(typeTranslator, func)
	}
	
	private def TabulatorContext createDataTabulatorContext(JavaTypeTranslator typeTranslator) {
		new DataTabulatorContext(typeTranslator)
	}

	private def JavaClass<Tabulator<?>> toApplicableTabulatorClass(Function func) {
		shouldGenerateLegacyTabulator ? func.toProjectionTabulatorJavaClass : func.toTabulatorJavaClass
	}
	
	private def StringConcatenationClient mainTabulatorClassBody(Data inputType, TabulatorContext context, JavaScope topScope, JavaClass<Tabulator<?>> tabulatorClass) {
		val inputClass = new RDataType(inputType).toJavaReferenceType
		
		val classScope = topScope.classScope(tabulatorClass.simpleName)
		
		val tabulateScope = classScope.methodScope("tabulate")
		val inputParam = tabulateScope.createUniqueIdentifier("input")
		
		println("main inputType " + inputType)
		println("main tabulatorClass " + tabulatorClass)
		println("main innerTabulatorClass " + context.toTabulatorJavaClass(inputType))
		println("main needsTabulator " + context.needsTabulator(inputType))
		
		if (context.needsTabulator(inputType)) { // TODO temp change to investigate Windows issue //  && !tabulatorClass.equals(context.toTabulatorJavaClass(inputType)
			// There will be a tabulator available for `inputType`,
			// so we can inject it.
			val innerTabulatorClass = context.toTabulatorJavaClass(inputType)
			val innerTabulatorInstance = classScope.createUniqueIdentifier("tabulator")
			'''
			@«ImplementedBy»(«tabulatorClass».Impl.class)
			public interface «tabulatorClass» extends «Tabulator»<«inputClass»> {
				public class Impl implements «tabulatorClass» {
					private final «innerTabulatorClass» «innerTabulatorInstance»;
					
					@«Inject»
					public Impl(«innerTabulatorClass» «innerTabulatorInstance») {
						this.«innerTabulatorInstance» = «innerTabulatorInstance»;
					}
					
					@Override
					public «List»<«FieldValue»> tabulate(«inputClass» «inputParam») {
						return «innerTabulatorInstance».tabulate(«inputParam»);
					}
				}
			}
			'''
		} else {
			// There is no available tabulator for `inputType`,
			// so we generate a dummy implementation.
			'''
			@«ImplementedBy»(«tabulatorClass».Impl.class)
			public interface «tabulatorClass» extends «Tabulator»<«inputClass»> {
				class Impl implements «tabulatorClass» {
					
					@Override
					public «List»<«FieldValue»> tabulate(«inputClass» «inputParam») {
						return «Arrays».asList();
					}
				}
			}
			'''
		}
	}
	
	private def StringConcatenationClient tabulatorClassBody(Data inputType, TabulatorContext context, JavaScope topScope, JavaClass<Tabulator<?>> tabulatorClass) {
		val inputClass = new RDataType(inputType).toJavaReferenceType
		
		val classScope = topScope.classScope(tabulatorClass.simpleName)
		val tabulatedFields = findTabulatedFieldsAndCreateIdentifiers(inputType, context, classScope)
		val nestedTabulatorInstances = findNestedTabulatorsAndCreateIdentifiers(inputType, context, classScope)
		val tabulateScope = classScope.methodScope("tabulate")
		val inputParam = tabulateScope.createUniqueIdentifier("input")
		
		println("    ")
		println("class inputType2 " + inputType)
		println("class tabulatorClass2 " + tabulatorClass)
		println("class innerTabulatorClass2 " + context.toTabulatorJavaClass(inputType))
		
		'''
		@«ImplementedBy»(«tabulatorClass».Impl.class)
		public interface «tabulatorClass» extends «Tabulator»<«inputClass»> {
			public class Impl implements «tabulatorClass» {
				«FOR attr : inputType.allNonOverridesAttributes»
					«IF context.isTabulated(attr)»
						«val fieldId = classScope.getIdentifierOrThrow(attr)»
						private final «Field» «fieldId»;
					«ENDIF»
				«ENDFOR»
				«IF !nestedTabulatorInstances.empty»
				
				«FOR tabInst : nestedTabulatorInstances»
					private final «context.toTabulatorJavaClass(tabInst.type)» «classScope.getIdentifierOrThrow(tabInst)»;
				«ENDFOR»
				«ENDIF»
				
				«IF !nestedTabulatorInstances.empty»@«Inject»«ENDIF»
				public Impl(«FOR tabInst : nestedTabulatorInstances SEPARATOR ", "»«context.toTabulatorJavaClass(tabInst.type)» «classScope.getIdentifierOrThrow(tabInst)»«ENDFOR») {
					«FOR tabInst : nestedTabulatorInstances»
						this.«classScope.getIdentifierOrThrow(tabInst)» = «classScope.getIdentifierOrThrow(tabInst)»;
					«ENDFOR»
					«initializeFields(inputType, context, classScope)»
				}
				
				@Override
				public «List»<«FieldValue»> tabulate(«inputClass» «inputParam») {
					«computeFieldValues(inputType, inputParam, context, tabulateScope)»
					return «fieldValuesAsList(inputType, context, tabulateScope)»;
				}
			}
		}
		'''
	}
	
	private def List<Attribute> findTabulatedFieldsAndCreateIdentifiers(Data type, TabulatorContext context, JavaScope scope) {
		type
			.allNonOverridesAttributes
			.filter[context.isTabulated(it)]
			.map[
				scope.createIdentifier(it, name + "Field")
				it
			].toList
	}
	private def StringConcatenationClient initializeFields(Data type, TabulatorContext context, JavaScope scope) {
		'''
		«FOR attr : type.allNonOverridesAttributes»
			«IF context.isTabulated(attr)»
				«val fieldId = scope.getIdentifierOrThrow(attr)»
				«val rule = context.getRule(attr)»
				«val attrType = attr.typeCall.type»
				this.«fieldId» = new «FieldImpl»(
					"«StringEscapeUtils.escapeJava(attr.name)»",
					«attr.card.isMany»,
					«rule.map[model].map[name].map[new ModelSymbolId(DottedPath.splitOnDots(it), rule.get.name).toModelSymbolCode].toOptionalCode»,
					«rule.map[identifier].map['"' + it + '"'].toOptionalCode»,
					«Arrays».asList()
				);
			«ENDIF»
		«ENDFOR»
		'''
	}
	
	private def Set<NestedTabulatorInstance> findNestedTabulatorsAndCreateIdentifiers(Data type, TabulatorContext context, JavaScope scope) {
		val result = type.allNonOverridesAttributes
			.filter[context.isTabulated(it)]
			.map[typeCall.type]
			.filter(Data)
			.map[toNestedTabulatorInstance]
			.toSet
		result.forEach[scope.createIdentifier(it, context.toTabulatorJavaClass(it.type).simpleName.toFirstLower)]
		result
	}
	
	private def StringConcatenationClient computeFieldValues(Data type, GeneratedIdentifier inputParam, TabulatorContext context, JavaScope scope) {
		'''
		«FOR attr : type.allNonOverridesAttributes»
			«IF context.isTabulated(attr)»
				«fieldValue(attr, inputParam, scope)»
			«ENDIF»
		«ENDFOR»
		'''
	}

	private def StringConcatenationClient fieldValue(Attribute attr, GeneratedIdentifier inputParam, JavaScope scope) {
		val rType = typeProvider.getRTypeOfSymbol(attr)
			
		val resultId = scope.createIdentifier(attr.toComputedField, attr.name)
		
		val lambdaScope = scope.lambdaScope
		val lambdaParam = lambdaScope.createUniqueIdentifier("x")
		
		val nestedLambdaScope = lambdaScope.lambdaScope
		val nestedLambdaParam = nestedLambdaScope.createUniqueIdentifier("x")
		
		if (rType instanceof RDataType) {
			val resultType = if (attr.card.isMany) {
				LIST.wrap(LIST.wrap(JavaClass.from(FieldValue)))
			} else {
				LIST.wrap(JavaClass.from(FieldValue))
			}
			rType.toPolymorphicListOrSingleJavaType(attr.card.isMany)
			val attrType = rType.data
			val nestedTabulator = scope.getIdentifierOrThrow(attrType.toNestedTabulatorInstance)
			'''
			«FieldValue» «resultId» = «Optional».ofNullable(«inputParam».get«attr.name.toFirstUpper»())
				«IF attr.card.isMany»
				.map(«lambdaParam» -> «lambdaParam».stream()
					«IF !attr.metaAnnotations.empty»
						.map(«nestedLambdaParam» -> «nestedLambdaParam».getValue())
						.filter(«Objects»::nonNull)
					«ENDIF»
					.map(«nestedLambdaParam» -> «nestedTabulator».tabulate(«nestedLambdaParam»))
					.collect(«Collectors».toList()))
				.map(fieldValues -> new «MultiNestedFieldValueImpl»(«scope.getIdentifierOrThrow(attr)», Optional.of(fieldValues)))
				.orElse(new «MultiNestedFieldValueImpl»(«scope.getIdentifierOrThrow(attr)», Optional.empty()));
				«ELSE»
				«IF !attr.metaAnnotations.empty».map(«lambdaParam» -> «lambdaParam».getValue())«ENDIF»
				.map(«lambdaParam» -> new «NestedFieldValueImpl»(«scope.getIdentifierOrThrow(attr)», Optional.of(«nestedTabulator».tabulate(«lambdaParam»))))
				.orElse(new «NestedFieldValueImpl»(«scope.getIdentifierOrThrow(attr)», Optional.empty()));
				«ENDIF»
			'''
		} else {
			val resultType = rType.toPolymorphicListOrSingleJavaType(attr.card.isMany)
			'''
			«IF attr.metaAnnotations.empty»
			«FieldValue» «resultId» = new «FieldValueImpl»(«scope.getIdentifierOrThrow(attr)», «Optional».ofNullable(«inputParam».get«attr.name.toFirstUpper»()));
			«ELSEIF attr.card.isMany»
			«FieldValue» «resultId» = new «FieldValueImpl»(«scope.getIdentifierOrThrow(attr)», «Optional».ofNullable(«inputParam».get«attr.name.toFirstUpper»())
				.map(«lambdaParam» -> «lambdaParam».stream()
					.map(«nestedLambdaParam» -> «nestedLambdaParam».getValue())
					.filter(«Objects»::nonNull)
					.collect(«Collectors».toList())));
			«ELSE»
			«FieldValue» «resultId» = new «FieldValueImpl»(«scope.getIdentifierOrThrow(attr)», «Optional».ofNullable(«inputParam».get«attr.name.toFirstUpper»())
				.map(«lambdaParam» -> «lambdaParam».getValue()));
			«ENDIF»
			'''
		}
	}
	
	private def StringConcatenationClient fieldValuesAsList(Data type, TabulatorContext context, JavaScope scope) {
		'''
		«Arrays».asList(
			«FOR attr : type.allNonOverridesAttributes.filter[context.isTabulated(it)] SEPARATOR ","»
			«scope.getIdentifier(attr.toComputedField)»
			«ENDFOR»
		)'''
	}
	
	private def StringConcatenationClient toOptionalCode(Optional<?> object) {
		if (object.isPresent) {
			'''«Optional».of(«object.get»)'''
		} else {
			'''«Optional».empty()'''
		}
	}
	private def StringConcatenationClient toDottedPathCode(DottedPath path) {
		'''«DottedPath».of("«path.withSeparator("\", \"")»")'''
	}
	private def StringConcatenationClient toModelSymbolCode(ModelSymbolId symbolId) {
		'''new «ModelSymbolId»(«symbolId.namespace.toDottedPathCode», "«symbolId.name»")'''
	}
	
	private def toNestedTabulatorInstance(Data type) {
		new NestedTabulatorInstance(type)
	}
	@org.eclipse.xtend.lib.annotations.Data
	private static class NestedTabulatorInstance {
		Data type
	}
	private def toComputedField(Attribute attr) {
		new ComputedField(attr)
	}
	@org.eclipse.xtend.lib.annotations.Data
	private static class ComputedField {
		Attribute attribute
	}
}