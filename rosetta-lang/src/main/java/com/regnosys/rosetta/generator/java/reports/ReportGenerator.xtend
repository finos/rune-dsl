package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.blueprints.DataItemReportBuilder
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.function.FunctionGenerator
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.rosetta.BlueprintNodeExp
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.types.RObjectFactory
import javax.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

class ReportGenerator {
	@Inject extension RosettaExtensions
	@Inject extension RObjectFactory
	@Inject FunctionGenerator functionGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension ImportManagerExtension

	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaBlueprintReport report, String version) {
		val firstNodeExpression = firstNodeExpression(report)
		
		val rFunction = buildRFunction(firstNodeExpression)
		val functionJavaClass = rFunction.toFunctionJavaClass
		
		val topScope = new JavaScope(functionJavaClass.packageName)
		val classBody = functionGenerator.rBuildClass(rFunction, topScope)
		val content = buildClass(functionJavaClass.packageName, classBody, topScope)
	
		// generate blueprint report
		fsa.generateFile(root.reports.withForwardSlashes + '/' + report.name + 'Report.java', content)
		
		// generate output report type builder
		if (report.reportType !== null) {
			fsa.generateFile(
				root.reports.withForwardSlashes + '/' + report.reportType.name.toDataItemReportBuilderName +
					'.java', generateReportBuilder(root, report, version))
		}
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

			if(null === currentNodeExpr) firstNodeExpr = newNodeExpr else currentNodeExpr.next = newNodeExpr

			currentNodeExpr = newNodeExpr
		}

		val node = RosettaFactory.eINSTANCE.createBlueprintOr
		node.name = report.name

		report.getAllReportingRules(false).values.sortBy[name].forEach [
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

		val rule = RosettaFactory.eINSTANCE.createRosettaBlueprint
		rule.legacy = true
		rule.nodes = firstNodeExpr

		return rule
	}
	
	/**
	 * Builds DataItemReportBuilder that takes a list of GroupableData
	 */
	def String generateReportBuilder(RootPackage packageName, RosettaBlueprintReport report, String version) {
		try {
			val scope = new JavaScope(packageName.reports)

			val StringConcatenationClient body = '''
				«emptyJavadocWithVersion(version)»
				public class «report.reportType.name.toDataItemReportBuilderName» implements «DataItemReportBuilder» {
				
					«report.buildDataItemReportBuilderBody»
				}
			'''
			buildClass(packageName.reports, body, scope)
		} catch (Exception e) {
			LOGGER.error("Error generating blueprint java for " + report.reportType.name, e);
			return '''Unexpected Error generating «report.reportType.name».java Please see log for details'''
		}
	}
	
	def String toDataItemReportBuilderName(String dataItemReportTypeName) {
		'''«dataItemReportTypeName»_DataItemReportBuilder'''
	}

}
