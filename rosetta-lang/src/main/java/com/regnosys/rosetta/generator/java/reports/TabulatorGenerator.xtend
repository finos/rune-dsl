package com.regnosys.rosetta.generator.java.reports

import org.eclipse.xtext.generator.IFileSystemAccess2
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.generator.java.JavaScope
import javax.inject.Inject
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.rosetta.util.types.JavaClass
import com.rosetta.model.lib.reports.Tabulator
import com.regnosys.rosetta.types.RDataType
import com.rosetta.util.DottedPath
import com.rosetta.model.lib.reports.Tabulator.Field
import com.rosetta.model.lib.reports.Tabulator.FieldImpl
import java.util.Optional
import com.regnosys.rosetta.generator.GeneratedIdentifier
import java.util.List
import java.util.Arrays
import com.rosetta.model.lib.reports.Tabulator.FieldValue
import java.util.stream.Collectors
import com.rosetta.model.lib.reports.Tabulator.FieldValueImpl
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource
import java.util.Map
import com.regnosys.rosetta.types.RosettaTypeProvider
import java.util.Set
import org.apache.commons.text.StringEscapeUtils
import com.rosetta.model.lib.reports.Tabulator.MultiNestedFieldValueImpl
import com.rosetta.model.lib.reports.Tabulator.NestedFieldValueImpl
import com.rosetta.model.lib.ModelSymbolId
import com.regnosys.rosetta.rosetta.RosettaReport
import com.regnosys.rosetta.rosetta.RosettaRule
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.rosetta.simple.Function

class TabulatorGenerator {
	private interface TabulatorContext {
		def boolean needsTabulator(Data type)
		def boolean isTabulated(Attribute attr)
		def JavaClass<Tabulator<?>> toTabulatorJavaClass(Data type)
		def Optional<RosettaRule> getRule(Attribute attr)
	}
	@org.eclipse.xtend.lib.annotations.Data
	private static class ReportTabulatorContext implements TabulatorContext {
		extension RosettaExtensions
		extension JavaTypeTranslator
		Map<Attribute, RosettaRule> ruleMap
		Optional<RosettaExternalRuleSource> ruleSource
		
		override needsTabulator(Data type) {
			needsTabulator(type, newHashSet)
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
		
	}
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
			type.toTabulatorJavaClass(projection)
		}
		
		override getRule(Attribute attr) {
			Optional.empty
		}
	}
	
	@Inject RosettaTypeProvider typeProvider
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
	
	def generate(IFileSystemAccess2 fsa, Function func) {
		if (func.isProjection) {
			val tabulatorClass = func.toProjectionTabulatorJavaClass
			val topScope = new JavaScope(tabulatorClass.packageName)
			
			val projectionType = typeProvider.getRTypeOfSymbol(func.output)
			if (projectionType instanceof RDataType) {
				val context = new ProjectionTabulatorContext(typeTranslator, func)
				
				val classBody = projectionType.data.mainTabulatorClassBody(context, topScope, tabulatorClass)
				val content = buildClass(tabulatorClass.packageName, classBody, topScope)
				fsa.generateFile(tabulatorClass.canonicalName.withForwardSlashes + ".java", content)
				
				recursivelyGenerateProjectionTypeTabulators(fsa, projectionType.data, context, newHashSet)
			}
		}
	}
	private def void recursivelyGenerateProjectionTypeTabulators(IFileSystemAccess2 fsa, Data type, ProjectionTabulatorContext context, Set<Data> visited) {
		if (visited.add(type)) {
			val tabulatorClass = type.toTabulatorJavaClass(context.projection)
			val topScope = new JavaScope(tabulatorClass.packageName)
			
			val classBody = type.tabulatorClassBody(context, topScope, tabulatorClass)
			val content = buildClass(tabulatorClass.packageName, classBody, topScope)
			fsa.generateFile(tabulatorClass.canonicalName.withForwardSlashes + ".java", content)
		
			type
				.allNonOverridesAttributes
				.map[typeProvider.getRTypeOfSymbol(it)]
				.filter(RDataType)
				.forEach[recursivelyGenerateProjectionTypeTabulators(fsa, data, context, visited)]
		}
	}
	
	private def ReportTabulatorContext getContext(Data type, Optional<RosettaExternalRuleSource> ruleSource) {
		val ruleMap = newHashMap
		type.getAllReportingRules(ruleSource).forEach[key, rule| ruleMap.put(key.attr, rule)]
		new ReportTabulatorContext(extensions, typeTranslator, ruleMap, ruleSource)
	}
	
	private def boolean isProjection(Function func) {
		func.annotations.findFirst[annotation.name == "projection"] !== null
	}
	
	private def StringConcatenationClient mainTabulatorClassBody(Data inputType, TabulatorContext context, JavaScope topScope, JavaClass<Tabulator<?>> tabulatorClass) {
		val inputClass = new RDataType(inputType).toJavaReferenceType
		
		val classScope = topScope.classScope(tabulatorClass.simpleName)
		
		val tabulateScope = classScope.methodScope("tabulate")
		val inputParam = tabulateScope.createUniqueIdentifier("input")
		
		if (context.needsTabulator(inputType)) {
			// There will be a tabulator available for `inputType`,
			// so we can inject it.
			val innerTabulatorClass = context.toTabulatorJavaClass(inputType)
			val innerTabulatorInstance = classScope.createUniqueIdentifier("tabulator")
			'''
			public class «tabulatorClass» implements «Tabulator»<«inputClass»> {
				private final «innerTabulatorClass» «innerTabulatorInstance»;
				
				@«Inject»
				public «tabulatorClass»(«innerTabulatorClass» «innerTabulatorInstance») {
					this.«innerTabulatorInstance» = «innerTabulatorInstance»;
				}
				
				@Override
				public «List»<«Field»> getFields() {
					return «innerTabulatorInstance».getFields();
				}
				
				@Override
				public «List»<«FieldValue»> tabulate(«inputClass» «inputParam») {
					return «innerTabulatorInstance».tabulate(«inputParam»);
				}
			}
			'''
		} else {
			// There is no available tabulator for `inputType`,
			// so we generate a dummy implementation.
			'''
			public class «tabulatorClass» implements «Tabulator»<«inputClass»> {
				@Override
				public «List»<«Field»> getFields() {
					return «Arrays».asList();
				}
				
				@Override
				public «List»<«FieldValue»> tabulate(«inputClass» «inputParam») {
					return «Arrays».asList();
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
		'''
		public class «tabulatorClass» implements «Tabulator»<«inputClass»> {
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
			public «tabulatorClass»(«FOR tabInst : nestedTabulatorInstances SEPARATOR ", "»«context.toTabulatorJavaClass(tabInst.type)» «classScope.getIdentifierOrThrow(tabInst)»«ENDFOR») {
				«FOR tabInst : nestedTabulatorInstances»
					this.«classScope.getIdentifierOrThrow(tabInst)» = «classScope.getIdentifierOrThrow(tabInst)»;
				«ENDFOR»
				«initializeFields(inputType, context, classScope)»
			}
			
			@Override
			public «List»<«Field»> getFields() {
				return «Arrays».asList(«FOR field : tabulatedFields SEPARATOR ", "»«classScope.getIdentifierOrThrow(field)»«ENDFOR»);
			}
			
			@Override
			public «List»<«FieldValue»> tabulate(«inputClass» «inputParam») {
				«computeFieldValues(inputType, inputParam, context, tabulateScope)»
				return «fieldValuesAsList(inputType, context, tabulateScope)»;
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
					«IF attrType instanceof Data»
						«scope.getIdentifierOrThrow(attrType.toNestedTabulatorInstance)».getFields()
					«ELSE»
						«Arrays».asList()
					«ENDIF»
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
			«Optional»<«resultType»> «resultId» = «Optional».ofNullable(«inputParam».get«attr.name.toFirstUpper»())
				«IF attr.card.isMany»
				.map(«lambdaParam» -> «lambdaParam».stream()
					.map(«nestedLambdaParam» -> «nestedTabulator».tabulate(«nestedLambdaParam»«IF !attr.metaAnnotations.empty».getValue()«ENDIF»))
					.collect(«Collectors».toList()));
				«ELSE»
				.map(«lambdaParam» -> «nestedTabulator».tabulate(«lambdaParam»«IF !attr.metaAnnotations.empty».getValue()«ENDIF»));
				«ENDIF»
			'''
		} else {
			val resultType = rType.toPolymorphicListOrSingleJavaType(attr.card.isMany)
			'''
			«IF attr.metaAnnotations.empty»
			«Optional»<«resultType»> «resultId» = «Optional».ofNullable(«inputParam».get«attr.name.toFirstUpper»());
			«ELSEIF attr.card.isMany»
			«Optional»<«resultType»> «resultId» = «Optional».ofNullable(«inputParam».get«attr.name.toFirstUpper»())
				.map(«lambdaParam» -> «lambdaParam».stream()
					.map(«nestedLambdaParam» -> «nestedLambdaParam».getValue())
					.collect(«Collectors».toList()));
			«ELSE»
			«Optional»<«resultType»> «resultId» = «Optional».ofNullable(«inputParam».get«attr.name.toFirstUpper»())
				.map(«lambdaParam» -> «lambdaParam».getValue());
			«ENDIF»
			'''
		}
	}
	
	private def StringConcatenationClient fieldValuesAsList(Data type, TabulatorContext context, JavaScope scope) {
		'''
		«Arrays».asList(
			«FOR attr : type.allNonOverridesAttributes.filter[context.isTabulated(it)] SEPARATOR ","»
			«val attrType = attr.typeCall.type»
			«val valueClass = if (attrType instanceof Data) {
				if (attr.card.isMany) {
					MultiNestedFieldValueImpl
				} else {
					NestedFieldValueImpl
				}
			} else {
				FieldValueImpl
			}»
			new «valueClass»(«scope.getIdentifierOrThrow(attr)», «scope.getIdentifierOrThrow(attr.toComputedField)»)
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