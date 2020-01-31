package com.regnosys.rosetta.generator.java.util

import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.blueprints.BlueprintGenerator.AttributePath
import com.regnosys.rosetta.generator.java.blueprints.BlueprintGenerator.RegdOutputField
import com.regnosys.rosetta.rosetta.BlueprintDataJoin
import com.regnosys.rosetta.rosetta.BlueprintExtract
import com.regnosys.rosetta.rosetta.BlueprintFilter
import com.regnosys.rosetta.rosetta.BlueprintGroup
import com.regnosys.rosetta.rosetta.BlueprintMerge
import com.regnosys.rosetta.rosetta.BlueprintOneOf
import com.regnosys.rosetta.rosetta.BlueprintReduce
import com.regnosys.rosetta.rosetta.BlueprintSource
import com.regnosys.rosetta.rosetta.BlueprintValidate
import com.regnosys.rosetta.rosetta.RosettaAbsentExpression
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallable
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaDataRule
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaGroupByExpression
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaWhenPresentExpression
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.validation.TypedBPNode
import java.util.Comparator
import java.util.List
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EClass
import org.eclipse.xtend.lib.annotations.Accessors

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.*
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage

class ImportGenerator {

	static Logger LOGGER = Logger.getLogger(ImportGenerator)

	RosettaJavaPackages packages;

	@Accessors val imports = newTreeSet(new ImportComparator)

	@Accessors val staticImports = newTreeSet(new ImportComparator)

	new(RosettaJavaPackages packageName) {
		this.packages = packageName;
	}

	def addBlueprintImports() {
		imports.addAll(
		'''«packages.blueprintLib.name».Blueprint''',
		'''«packages.blueprintLib.name».BlueprintInstance''',
		'''«packages.blueprintLib.name».BlueprintBuilder''',
		'''«packages.blueprintLib.name».runner.actions.rosetta.RosettaActionFactory''')
		staticImports.add('''«packages.blueprintLib.name».BlueprintBuilder''')
	}

	def addSourceAndSink() {
		imports.addAll(
		'''«packages.blueprintLib.name».runner.nodes.SinkNode''',
		'''«packages.blueprintLib.name».runner.nodes.SourceNode''')
	}

	def addSimpleMerger(BlueprintMerge merge, Iterable<RegdOutputField> outRefs) {
		val extraImport2 = outRefs.map[it.attrib.type].map[fullName()].filter[isImportable]
		imports.addAll(extraImport2)
		imports.add('''«packages.model.name».«merge.output.name»''')
		imports.addAll('java.util.function.BiConsumer',
			'java.util.Map', '''«packages.blueprintLib.name».runner.actions.Merger''', '''«packages.blueprintLib.name».runner.data.RosettaIdentifier''', '''«packages.blueprintLib.name».runner.data.StringIdentifier''', '''«packages.blueprintLib.name».runner.data.DataIdentifier''', '''java.util.function.Function''', '''«packages.defaultLib.name».functions.Converter''',
			'java.util.HashMap')
	}
	
	def addIfThen(BlueprintOneOf oneOf) {
		imports.addAll(
		'''«packages.blueprintLib.name».BlueprintIfThen''')
	}

	def addSingleMapping(BlueprintExtract extract) {
		imports.add('''«packages.defaultLib.name».functions.MapperS''')
		imports.add('''«packages.blueprintLib.name».runner.data.StringIdentifier''')
		staticImports.add('''«packages.defaultLib.name».validation.ValidatorHelper''')
		addExpression(extract.call)
	}

	def addMappingImport() {
		imports.add('''«packages.defaultLib.name».functions.MapperS''')
		imports.add('''«packages.blueprintLib.name».runner.data.StringIdentifier''')
	}

	def void addFeatureCall(RosettaFeatureCall call) {
		val feature = call.feature
		switch (feature) {
			RosettaRegularAttribute: {
				imports.add(feature.type.fullName);
				imports.add((feature.eContainer as RosettaClass).fullName)

				if (feature.metaTypes !== null && !feature.metaTypes.isEmpty) {
					imports.add('''«packages.defaultLib.name».meta.FieldWithMeta''')
				}
			}
			Attribute: {
				imports.add(feature.type.fullName);
				imports.add((feature.eContainer as RosettaType).fullName)

//				TODO if (feature.metaTypes !== null && !feature.metaTypes.isEmpty) {
//					imports.add('''«packages.lib.packageName».meta.FieldWithMeta''')
//				}
			}
			RosettaMetaType:{
				imports.add('''«packages.model.metaField.name».*''')
				imports.add('''«packages.basicMetafields.name».*''')
			}
			RosettaEnumValue:{
				imports.add((feature.eContainer as RosettaEnumeration).fullName);
			}
			default:
				throw new UnsupportedOperationException("Unsupported expression type: " + feature.class.simpleName)
		}
		addExpression(call.receiver)
	}

	def dispatch add(RosettaCallable call) {
		switch (call) {
			RosettaAlias: {
				addExpression(call.expression)
			}
		}
	}

	def dispatch add(RosettaClass call) {
		imports.add(call.fullName)
	}

	def dispatch add(Object call) {
		println
	}


	def void addExpression(RosettaGroupByExpression groupBy) {
		if (groupBy.attribute === null) return;
		imports.add(groupBy.attribute.type.fullName)
		imports.add((groupBy.attribute.eContainer as RosettaType).fullName)
		if (groupBy.right !== null) {
			addExpression(groupBy.right)
		}
	}

	def void addExpression(RosettaExpression expression) {
		imports.add('''«packages.defaultLib.name».functions.MapperS''')
		imports.add('''«packages.defaultLib.name».functions.MapperTree''')
		switch (expression) {
			RosettaCallableCall: {
				add(expression.callable)
			}
			RosettaGroupByFeatureCall: {
				staticImports.add(packages.defaultLibValidation.name + ".ValidatorHelper")
				staticImports.add(packages.defaultLibValidation.name + ".MapperTreeValidatorHelper")
				addExpression(expression.call)
				if (expression.groupBy !== null) addExpression(expression.groupBy)
			}
			RosettaFeatureCall: {
				addFeatureCall(expression)
			}
			RosettaExistsExpression: {
				staticImports.add(packages.defaultLibValidation.name + ".ValidatorHelper")
				staticImports.add(packages.defaultLibValidation.name + ".MapperTreeValidatorHelper")
				addExpression(expression.argument)
			}
			RosettaBinaryOperation: {
				staticImports.add(packages.defaultLibValidation.name + ".ValidatorHelper")
				staticImports.add(packages.defaultLibValidation.name + ".MapperTreeValidatorHelper")

				if (#['+', '-'].contains(expression.operator)) {
					imports.add(packages.defaultLibFunctions.name + ".MapperMaths")
					imports.add("java.math.BigDecimal")
				}

				addExpression(expression.left)
				addExpression(expression.right)
			}
			RosettaCountOperation: {
				staticImports.add(packages.defaultLibValidation.name + ".ValidatorHelper")
				staticImports.add(packages.defaultLibValidation.name + ".MapperTreeValidatorHelper")
				addExpression(expression.argument)
			}
			RosettaWhenPresentExpression: {
				staticImports.add(packages.defaultLibValidation.name + ".ValidatorHelper")
				staticImports.add(packages.defaultLibValidation.name + ".MapperTreeValidatorHelper")
				addExpression(expression.left)
				addExpression(expression.right)
			}
			RosettaBigDecimalLiteral: {
				imports.add("java.math.BigDecimal")
			}
			RosettaLiteral: {
			}
			RosettaAbsentExpression: {
				staticImports.add(packages.defaultLibValidation.name + ".ValidatorHelper")
				staticImports.add(packages.defaultLibValidation.name + ".MapperTreeValidatorHelper")
				addExpression(expression.argument);
			}
			RosettaEnumValueReference: {
				imports.add(expression.enumeration.fullName)
			}
			RosettaConditionalExpression: {
				addExpression(expression.^if)
				addExpression(expression.ifthen)
				addExpression(expression.elsethen)
			}
			RosettaContainsExpression: {
				staticImports.add(packages.defaultLibValidation.name + ".ValidatorHelper")
				staticImports.add(packages.defaultLibValidation.name + ".MapperTreeValidatorHelper")
				addExpression(expression.contained)
				addExpression(expression.container)
			}
			default:
				throw new UnsupportedOperationException("Unsupported expression type of " + expression.class.simpleName)
		}
	}

	def toFullNames(AttributePath path) {
		path.path.map[eContainer as RosettaClass].map[fullName()].filter[isImportable]
	}

	def fullName(RosettaType type) {
		if (type instanceof RosettaClass || type instanceof Data) {
			val targetPackage = new RootPackage(type.model.name)
			'''«targetPackage.name».«type.name»'''.toString
		} else if (type instanceof RosettaEnumeration) {
			val targetPackage = new RootPackage(type.model.name)
			'''«targetPackage.name».«type.name»'''.toString
		} else {
			val simple = type.name.toJavaFullType
			if (simple === null) {
				LOGGER.info("Don't know what to import for " + type.name)
			}
			simple
		}
	}

	def isImportable(String typeName) {
		!typeName.startsWith('java.lang')
	}

	def addTypes(TypedBPNode node) {
		if (node.input.type !== null) imports.add(node.input.type.fullName)
		if (node.inputKey.type !== null) imports.add(node.inputKey.type.fullName)
		if (node.output.type !== null) imports.add(node.output.type.fullName)
		if (node.outputKey.type !== null) imports.add(node.outputKey.type.fullName)
	}

	def fullName(EClass type) {
		if (type instanceof RosettaClass || type instanceof RosettaClass)
			'''«packages.model.name».«type.name»'''.toString
		else
			type.name.toJavaFullType
	}

	def addValidate(BlueprintValidate validate) {
		imports.add(validate.input.fullName)
	}

	def addRule(RosettaDataRule rule) {
		addExpression(rule.when)
		addExpression(rule.then)
		imports.addAll(packages.defaultLibAnnotations.name + ".RosettaDataRule",
			packages.defaultLibValidation.name + ".ValidationResult", packages.defaultLibValidation.name + ".Validator",
			packages.defaultLib.name + ".functions.MapperS", packages.defaultLib.name + ".validation.ComparisonResult",
			packages.defaultLib.name + ".meta.FieldWithMeta", packages.defaultLib.name + ".path.RosettaPath",
			packages.defaultLib.name + ".RosettaModelObjectBuilder")
	}

	def addFilter(BlueprintFilter filter) {
		if (filter.filter!==null) {
			addExpression(filter.filter);
			imports.add('''«packages.defaultLib.name».functions.MapperS''')
			imports.add(packages.blueprintLib.name + ".runner.actions.Filter")
		}
		if (filter.filterBP!==null) {
			imports.add(packages.blueprintLib.name + ".runner.actions.FilterByRule")
		}
	}
	
	def addReduce(BlueprintReduce reduce) {
		if (reduce.expression!==null) {
			addExpression(reduce.expression);
			imports.add('''«packages.defaultLib.name».functions.MapperS''')
		}
		imports.add(packages.blueprintLib.name + ".runner.actions.ReduceBy")
	}

	def addGrouper(BlueprintGroup group) {
		addFeatureCall(group.key as RosettaFeatureCall)
	}

	def addQualifyClass(RosettaExpression expr, List<RosettaDataRule> andDataRules, List<RosettaDataRule> orDataRules,
		RosettaType rClass) {
		imports.addAll("com.rosetta.model.lib.annotations.RosettaQualifiable", "java.util.function.Function",
			rClass.fullName, packages.model.dataRule.name + ".*")
		imports.add('''«packages.defaultLibQualify.name».QualifyResult''')
		imports.add('''«packages.defaultLib.name».functions.MapperS''')
		imports.add(packages.defaultLib.name + ".validation.ComparisonResult")
		imports.add(packages.defaultLib.name + ".meta.FieldWithMeta")
		addExpression(expr)
		for (andDataRule : andDataRules) {
			addRule(andDataRule)
		}
		for (orDataRule : orDataRules) {
			addRule(orDataRule)
		}
		staticImports.add(packages.defaultLibValidation.name + ".ValidatorHelper")
	}

	def addMeta(RosettaClass class1) {
		imports.addAll(
			"com.rosetta.model.lib.annotations.RosettaMeta",
			"com.rosetta.model.lib.meta.RosettaMetaData",
			"java.util.Arrays",
			"com.google.common.collect.Multimap",
			"com.google.common.collect.ImmutableMultimap",
			"java.util.List",
			"java.util.function.Function",
			packages.defaultLib.name + ".validation.Validator",
			packages.defaultLib.name + ".validation.ValidatorWithArg",
			packages.defaultLib.name + ".qualify.QualifyResult"
		)
		imports.add(packages.model.name + "." + class1.name)
		for (attrib : class1.regularAttributes) {
			imports.add(attrib.type.fullName)
		}
	}


	def addNode(TypedBPNode typed) {
		imports.add('''«packages.blueprintLib.name».runner.nodes.Node''')
		addTypes(typed)
	}

	def addSource(BlueprintSource source, TypedBPNode typed) {
		imports.add('''«packages.blueprintLib.name».runner.nodes.SourceNode''')
		addTypes(typed)
	}

	def addJoin(BlueprintDataJoin join) {
		addExpression(join.foreign)
		addExpression(join.key);
	}

}

class ImportComparator implements Comparator<String> {

	override compare(String o1, String o2) {
		return o1.compareTo(o2);
	}
}
