package com.regnosys.rosetta.generator.java.util

import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.rosetta.BlueprintExtract
import com.regnosys.rosetta.rosetta.BlueprintFilter
import com.regnosys.rosetta.rosetta.BlueprintSource
import com.regnosys.rosetta.rosetta.expression.RosettaBigDecimalLiteral
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import com.regnosys.rosetta.rosetta.RosettaCallable
import com.regnosys.rosetta.rosetta.expression.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.expression.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.validation.TypedBPNode
import java.util.Comparator
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EClass
import org.eclipse.xtend.lib.annotations.Accessors

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.*
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.FunctionReference
import com.regnosys.rosetta.rosetta.expression.NamedFunctionReference
import com.regnosys.rosetta.rosetta.expression.InlineFunction

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

	def void addBlueprintImports() {
		imports.addAll(
		'''«packages.blueprintLib.name».Blueprint''',
		'''«packages.blueprintLib.name».BlueprintInstance''',
		'''«packages.blueprintLib.name».BlueprintBuilder''',
		'''«packages.blueprintLib.name».runner.actions.rosetta.RosettaActionFactory''')
		staticImports.add('''«packages.blueprintLib.name».BlueprintBuilder''')
	}

	def void addSourceAndSink() {
		imports.addAll(
		'''«packages.blueprintLib.name».runner.nodes.SinkNode''',
		'''«packages.blueprintLib.name».runner.nodes.SourceNode''')
	}
	
	def void addSingleMapping(BlueprintExtract extract) {
		addMappingImport
		addExpression(extract.call)
	}

	def void addMappingImport() {
		imports.add('''«packages.blueprintLib.name».runner.data.StringIdentifier''')
		imports.add('''«packages.blueprintLib.name».runner.data.RuleIdentifier''')
	}
	
	def void addDataItemReportBuilder(Data reportType) {
		addMappingImport
		imports.addAll(
			'''«packages.model.name».«reportType.name»''',
			'''«packages.blueprintLib.name».DataItemReportBuilder''',
			'''«packages.blueprintLib.name».DataItemReportUtils''',
			'''«packages.blueprintLib.name».runner.data.DataIdentifier''',
			'''«packages.blueprintLib.name».runner.data.GroupableData''')
	}
	
	def void addDataItemReportRule(RosettaBlueprint blueprint) {
		imports.add('''«(blueprint.eContainer as RosettaModel).name».blueprint.«blueprint.name»Rule''')
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

	def void addCallableWithArgs(RosettaCallableWithArgs callable) {
		switch (callable) {
			Function: {
				imports.add(callable.output.type.fullName);
			}
			default:
				throw new UnsupportedOperationException("Unsupported expression type: " + callable.class.simpleName)
		}
	}

	def void add(Object call) {
		// println
	}


	def void addExpression(RosettaExpression expression) {
		switch (expression) {
			case null: {
				// do nothing
			}	
			RosettaCallableCall: {
				add(expression.callable)
			}
			RosettaFeatureCall: {
				addFeatureCall(expression)
			}
			RosettaOnlyExistsExpression: {
				expression.args.forEach[addExpression]
			}
			RosettaFunctionalOperation: {
				addExpression(expression.argument)
				addFunctionReference(expression.functionRef)
			}
			RosettaUnaryOperation: {
				addExpression(expression.argument)
			}
			RosettaBinaryOperation: {
				
				if (#['+', '-'].contains(expression.operator)) {
					imports.add("java.math.BigDecimal")
				}

				addExpression(expression.left)
				addExpression(expression.right)
			}
			RosettaBigDecimalLiteral: {
				imports.add("java.math.BigDecimal")
			}
			RosettaLiteral: {
			}
			RosettaEnumValueReference: {
				imports.add(expression.enumeration.fullName)
			}
			RosettaConditionalExpression: {
				addExpression(expression.^if)
				addExpression(expression.ifthen)
				if (expression.elsethen!==null) 
					addExpression(expression.elsethen)
			}
			RosettaCallable:{}
			RosettaCallableWithArgsCall: {
				addCallableWithArgs(expression.callable)
			}
			default:
				LOGGER.warn("Unsupported expression type of " + expression.class.simpleName)
		}
	}
	
	def addFunctionReference(FunctionReference ref) {
		switch ref {
			NamedFunctionReference: {
				addCallableWithArgs(ref.function)
			}
			InlineFunction: {
				addExpression(ref.body)
			}
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

	def addFilter(BlueprintFilter filter) {
		if (filter.filter!==null) {
			addExpression(filter.filter);
			imports.add(packages.blueprintLib.name + ".runner.actions.Filter")
		}
		if (filter.filterBP!==null) {
			imports.add(packages.blueprintLib.name + ".runner.actions.FilterByRule")
		}
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
		
	def addBPRef(RosettaBlueprint blueprint) {
		imports.add('''«(blueprint.eContainer as RosettaModel).name».blueprint.«blueprint.name»Rule''')
		imports.add('''«packages.blueprintLib.name».runner.actions.IdChange''')
		imports.add('''«packages.blueprintLib.name».runner.data.StringIdentifier''')
		imports.add('''«packages.blueprintLib.name».runner.data.RuleIdentifier''')
	}

}

class ImportComparator implements Comparator<String> {

	override compare(String o1, String o2) {
		return o1.compareTo(o2);
	}
}
