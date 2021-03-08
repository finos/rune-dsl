package com.regnosys.rosetta.generator.java.util

import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
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
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaDisjointExpression
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
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.validation.TypedBPNode
import java.util.Comparator
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EClass
import org.eclipse.xtend.lib.annotations.Accessors

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.*

/**
 * This class should go away - the ImportingStringConcatenation method is superior
 */
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
		imports.add('''«packages.blueprintLib.name».runner.data.StringIdentifier''')
		addExpression(extract.call)
	}

	def addMappingImport() {
		imports.add('''«packages.blueprintLib.name».runner.data.StringIdentifier''')
	}

	def void addFeatureCall(RosettaFeatureCall call) {
		val feature = call.feature
		switch (feature) {
			Attribute: {
				imports.add(feature.type.fullName);
				imports.add((feature.eContainer as RosettaType).fullName)

//				TODO if (feature.metaTypes !== null && !feature.metaTypes.isEmpty) {
//					imports.add('''«packages.lib.packageName».meta.FieldWithMeta''')
//				}
			}
			RosettaMetaType:{
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
		switch (expression) {
			RosettaCallableCall: {
				add(expression.callable)
			}
			RosettaGroupByFeatureCall: {
				addExpression(expression.call)
				if (expression.groupBy !== null) addExpression(expression.groupBy)
			}
			RosettaFeatureCall: {
				addFeatureCall(expression)
			}
			RosettaExistsExpression: {
				addExpression(expression.argument)
			}
			RosettaBinaryOperation: {
				
				if (#['+', '-'].contains(expression.operator)) {
					imports.add("java.math.BigDecimal")
				}

				addExpression(expression.left)
				addExpression(expression.right)
			}
			RosettaCountOperation: {
				addExpression(expression.argument)
			}
			RosettaBigDecimalLiteral: {
				imports.add("java.math.BigDecimal")
			}
			RosettaLiteral: {
			}
			RosettaAbsentExpression: {
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
				addExpression(expression.contained)
				addExpression(expression.container)
			}
			RosettaDisjointExpression: {
				addExpression(expression.disjoint)
				addExpression(expression.container)
			}
			default:
				throw new UnsupportedOperationException("Unsupported expression type of " + expression.class.simpleName)
		}
	}

	def fullName(RosettaType type) {
		if (type instanceof Data) {
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
		if (type instanceof Data)
			'''«packages.model.name».«type.name»'''.toString
		else
			type.name.toJavaFullType
	}

	def addValidate(BlueprintValidate validate) {
		imports.add(validate.input.fullName)
	}

	def addFilter(BlueprintFilter filter) {
		if (filter.filter!==null) {
			addExpression(filter.filter);
			imports.add(packages.blueprintLib.name + ".runner.actions.Filter")
		}
		if (filter.filterBP!==null) {
			imports.add(packages.blueprintLib.name + ".runner.actions.FilterByRule")
		}
	}
	
	def addReduce(BlueprintReduce reduce) {
		if (reduce.expression!==null) {
			addExpression(reduce.expression);
		}
		imports.add(packages.blueprintLib.name + ".runner.actions.ReduceBy")
	}

	def addGrouper(BlueprintGroup group) {
		addFeatureCall(group.key as RosettaFeatureCall)
	}

	def addQualifyClass(RosettaExpression expr,
		RosettaType rClass) {
		imports.addAll("com.rosetta.model.lib.annotations.RosettaQualifiable", "java.util.function.Function",
			rClass.fullName, packages.model.dataRule.name + ".*")
		imports.add('''«packages.defaultLibQualify.name».QualifyResult''')
		imports.add(packages.defaultLib.name + ".validation.ComparisonResult")
		imports.add(packages.defaultLib.name + ".meta.FieldWithMeta")
		addExpression(expr)
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
