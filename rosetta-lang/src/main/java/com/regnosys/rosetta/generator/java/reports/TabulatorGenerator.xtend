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
import com.rosetta.util.types.JavaParameterizedType
import com.rosetta.model.lib.ModelSymbolId
import com.regnosys.rosetta.rosetta.RosettaReport
import com.regnosys.rosetta.rosetta.RosettaRule

class TabulatorGenerator {
	@Inject RosettaTypeProvider typeProvider
	@Inject extension JavaTypeTranslator
	@Inject extension ImportManagerExtension
	
	@Inject extension RosettaExtensions
	
	def generate(IFileSystemAccess2 fsa, RosettaReport report) {
		val tabulatorClass = report.toReportTabulatorJavaClass
		val topScope = new JavaScope(tabulatorClass.packageName)
		
		val context = getContext(report.reportType, Optional.ofNullable(report.ruleSource))
		val classBody = report.reportTabulatorClassBody(context, topScope, tabulatorClass)
		val content = buildClass(tabulatorClass.packageName, classBody, topScope)
		fsa.generateFile(tabulatorClass.canonicalName.withForwardSlashes + ".java", content)
	}
	
	def generate(IFileSystemAccess2 fsa, Data type, Optional<RosettaExternalRuleSource> ruleSource) {
		val context = getContext(type, ruleSource)
		if (type.isReportable(context)) {
			val tabulatorClass = type.toTabulatorJavaClass(ruleSource)
			val topScope = new JavaScope(tabulatorClass.packageName)
			
			val classBody = type.tabulatorClassBody(ruleSource, context, topScope, tabulatorClass)
			val content = buildClass(tabulatorClass.packageName, classBody, topScope)
			fsa.generateFile(tabulatorClass.canonicalName.withForwardSlashes + ".java", content)
		}
	}
	
	private def Map<Attribute, RosettaRule> getContext(Data type, Optional<RosettaExternalRuleSource> ruleSource) {
		val context = newHashMap
		type.getAllReportingRules(ruleSource, false, false).forEach[key, rule| context.put(key.attr, rule)]
		context
	}
	
	private def boolean isReportable(Data type, Map<Attribute, RosettaRule> context) {
		isReportable(type, context, newHashSet)
	}
	private def boolean isReportable(Data type, Map<Attribute, RosettaRule> context, Set<Data> visited) {
		if (visited.add(type)) {
			type.allAttributes.exists[isReportable(context, visited)]
		} else {
			false
		}
	}
	private def boolean isReportable(Attribute attr, Map<Attribute, RosettaRule> context) {
		isReportable(attr, context, newHashSet)
	}
	private def boolean isReportable(Attribute attr, Map<Attribute, RosettaRule> context, Set<Data> visited) {
		val attrType = attr.typeCall.type
		if (attrType instanceof Data) {
			isReportable(attrType, context, visited)
		} else {
			context.containsKey(attr)
		}
	}
	
	private def StringConcatenationClient reportTabulatorClassBody(RosettaReport report, Map<Attribute, RosettaRule> context, JavaScope topScope, JavaClass tabulatorClass) {
		val reportType = report.reportType
		val reportClass = new RDataType(reportType).toJavaReferenceType
		
		val classScope = topScope.classScope(reportClass.simpleName)
		
		val tabulateScope = classScope.methodScope("tabulate")
		val reportParam = tabulateScope.createUniqueIdentifier("report")
		
		if (reportType.isReportable(context)) {
			// There will be a tabulator available for `reportType`,
			// so we can inject it.
			val innerTabulatorClass = reportType.toTabulatorJavaClass(Optional.ofNullable(report.ruleSource))
			val innerTabulatorInstance = classScope.createUniqueIdentifier("tabulator")
			'''
			public class «tabulatorClass» implements «Tabulator»<«reportClass»> {
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
				public «List»<«FieldValue»> tabulate(«reportClass» «reportParam») {
					return «innerTabulatorInstance».tabulate(«reportParam»);
				}
			}
			'''
		} else {
			// There is no available tabulator for `reportType`,
			// so we generate a dummy implementation.
			'''
			public class «tabulatorClass» implements «Tabulator»<«reportClass»> {				
				@Override
				public «List»<«Field»> getFields() {
					return «Arrays».asList();
				}
				
				@Override
				public «List»<«FieldValue»> tabulate(«reportClass» «reportParam») {
					return «Arrays».asList();
				}
			}
			'''
		}
	}
	
	private def StringConcatenationClient tabulatorClassBody(Data reportType, Optional<RosettaExternalRuleSource> ruleSource, Map<Attribute, RosettaRule> context, JavaScope topScope, JavaClass tabulatorClass) {
		val reportClass = new RDataType(reportType).toJavaReferenceType
		
		val classScope = topScope.classScope(reportClass.simpleName)
		val reportedFields = findReportedFieldsAndCreateIdentifiers(reportType, context, classScope)
		val nestedTabulatorInstances = findNestedTabulatorsAndCreateIdentifiers(reportType, ruleSource, context, classScope)
		
		val tabulateScope = classScope.methodScope("tabulate")
		val reportParam = tabulateScope.createUniqueIdentifier("report")
		'''
		public class «tabulatorClass» implements «Tabulator»<«reportClass»> {
			«FOR attr : reportType.allNonOverridesAttributes»
				«IF attr.isReportable(context)»
					«val fieldId = classScope.getIdentifierOrThrow(attr)»
					private final «Field» «fieldId»;
				«ENDIF»
			«ENDFOR»
			«IF !nestedTabulatorInstances.empty»
			
			«FOR tabInst : nestedTabulatorInstances»
				private final «tabInst.type.toTabulatorJavaClass(ruleSource)» «classScope.getIdentifierOrThrow(tabInst)»;
			«ENDFOR»
			«ENDIF»
			
			«IF !nestedTabulatorInstances.empty»@«Inject»«ENDIF»
			public «tabulatorClass»(«FOR tabInst : nestedTabulatorInstances SEPARATOR ", "»«tabInst.type.toTabulatorJavaClass(ruleSource)» «classScope.getIdentifierOrThrow(tabInst)»«ENDFOR») {
				«FOR tabInst : nestedTabulatorInstances»
					this.«classScope.getIdentifierOrThrow(tabInst)» = «classScope.getIdentifierOrThrow(tabInst)»;
				«ENDFOR»
				«initializeFields(reportType, context, classScope)»
			}
			
			@Override
			public «List»<«Field»> getFields() {
				return «Arrays».asList(«FOR field : reportedFields SEPARATOR ", "»«classScope.getIdentifierOrThrow(field)»«ENDFOR»);
			}
			
			@Override
			public «List»<«FieldValue»> tabulate(«reportClass» «reportParam») {
				«computeFieldValues(reportType, reportParam, context, tabulateScope)»
				return «fieldValuesAsList(reportType, context, tabulateScope)»;
			}
		}
		'''
	}
	
	private def List<Attribute> findReportedFieldsAndCreateIdentifiers(Data type, Map<Attribute, RosettaRule> context, JavaScope scope) {
		type
			.allNonOverridesAttributes
			.filter[isReportable(context)]
			.map[
				scope.createIdentifier(it, name + "Field")
				it
			].toList
	}
	private def StringConcatenationClient initializeFields(Data type, Map<Attribute, RosettaRule> context, JavaScope scope) {
		'''
		«FOR attr : type.allNonOverridesAttributes»
			«IF attr.isReportable(context)»
				«val fieldId = scope.getIdentifierOrThrow(attr)»
				«val rule = Optional.ofNullable(context.get(attr))»
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
	
	private def Set<NestedTabulatorInstance> findNestedTabulatorsAndCreateIdentifiers(Data type, Optional<RosettaExternalRuleSource> ruleSource, Map<Attribute, RosettaRule> context, JavaScope scope) {
		val result = type.allNonOverridesAttributes
			.filter[isReportable(context)]
			.map[typeCall.type]
			.filter(Data)
			.map[toNestedTabulatorInstance]
			.toSet
		result.forEach[scope.createIdentifier(it, it.type.toTabulatorJavaClass(ruleSource).simpleName.toFirstLower)]
		result
	}
	
	private def StringConcatenationClient computeFieldValues(Data type, GeneratedIdentifier reportVariable, Map<Attribute, RosettaRule> context, JavaScope scope) {
		'''
		«FOR attr : type.allNonOverridesAttributes»
			«IF attr.isReportable(context)»
				«fieldValue(attr, reportVariable, scope)»
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
				new JavaParameterizedType(JavaClass.from(List), new JavaParameterizedType(JavaClass.from(List), JavaClass.from(FieldValue)))
			} else {
				new JavaParameterizedType(JavaClass.from(List), JavaClass.from(FieldValue))
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
	
	private def StringConcatenationClient fieldValuesAsList(Data type, Map<Attribute, RosettaRule> context, JavaScope scope) {
		'''
		«Arrays».asList(
			«FOR attr : type.allNonOverridesAttributes.filter[isReportable(context)] SEPARATOR ","»
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