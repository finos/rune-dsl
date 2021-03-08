/*
 * generated by Xtext 2.10.0
 */
package com.regnosys.rosetta.formatting2

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaClassSynonym
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaDefinable
import com.regnosys.rosetta.rosetta.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym
import com.regnosys.rosetta.rosetta.RosettaEnumValue
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaExternalClass
import com.regnosys.rosetta.rosetta.RosettaExternalEnum
import com.regnosys.rosetta.rosetta.RosettaExternalEnumValue
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaExternalSynonym
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.rosetta.RosettaParenthesisCalcExpression
import com.regnosys.rosetta.rosetta.RosettaRegulatoryReference
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.RosettaTreeNode
import com.regnosys.rosetta.rosetta.RosettaWorkflowRule
import com.regnosys.rosetta.rosetta.simple.AnnotationRef
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Constraint
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Definable
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.Operation
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.services.RosettaGrammarAccess
import com.rosetta.model.lib.annotations.RosettaChoiceRule
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.formatting2.AbstractFormatter2
import org.eclipse.xtext.formatting2.IFormattableDocument
import org.eclipse.xtext.formatting2.IHiddenRegionFormatter
import org.eclipse.xtext.formatting2.regionaccess.ISemanticRegion
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class RosettaFormatter extends AbstractFormatter2 {
	
	static val Procedure1<? super IHiddenRegionFormatter> NO_SPACE = [noSpace]
	static val Procedure1<? super IHiddenRegionFormatter> NO_SPACE_PRESERVE_NEW_LINE = [setNewLines(0, 0, 1);noSpace]
	static val Procedure1<? super IHiddenRegionFormatter> NO_SPACE_LOW_PRIO = [noSpace; lowPriority]
	static val Procedure1<? super IHiddenRegionFormatter> ONE_SPACE = [oneSpace]
	static val Procedure1<? super IHiddenRegionFormatter> ONE_SPACE_PRESERVE_NEWLINE = [setNewLines(0, 0, 1); oneSpace]
	static val Procedure1<? super IHiddenRegionFormatter> NEW_LINE = [setNewLines(1, 1, 2)]
	
	static val Procedure1<? super IHiddenRegionFormatter> NEW_ROOT_ELEMENT = [setNewLines(2, 2, 3);highPriority]
	
	static val Procedure1<? super IHiddenRegionFormatter> NEW_LINE_LOW_PRIO = [lowPriority; setNewLines(1, 1, 2)]
	static val Procedure1<? super IHiddenRegionFormatter> INDENT = [indent]
	
	@Inject extension RosettaGrammarAccess
	
	def dispatch void format(RosettaModel rosettaModel, extension IFormattableDocument document) {
		rosettaModel.regionFor.keyword('version').prepend[newLine]
		formatChild(rosettaModel.elements, document)
	}


	def dispatch void format(Data ele, extension IFormattableDocument document) {
		ele.regionFor.keyword(dataAccess.typeKeyword_0).append(ONE_SPACE).prepend(NEW_ROOT_ELEMENT)
		ele.regionFor.keyword(dataAccess.extendsKeyword_2_0).append(ONE_SPACE)
		ele.regionFor.keyword(':').prepend(NO_SPACE).append(ONE_SPACE)
		ele.formatDefinition(document)
		val eleEnd = ele.nextHiddenRegion
		set(
			ele.regionFor.keyword(':').nextHiddenRegion,
			eleEnd,
			INDENT
		)
		ele.synonyms.forEach[
			format
		]
		ele.annotations.forEach[
			prepend(NEW_LINE_LOW_PRIO)
			format
		]
		ele.attributes.forEach[
			prepend(NEW_LINE_LOW_PRIO)
			format
		]
		ele.conditions.forEach[
			prepend(NEW_LINE_LOW_PRIO)
			format
		]
		set(eleEnd, NEW_LINE_LOW_PRIO)
	}

	def dispatch void format(Attribute ele, extension IFormattableDocument document) {
		ele.formatDefinition(document)
		ele.interior(INDENT)
		ele.annotations.forEach[
			prepend(NEW_LINE_LOW_PRIO)
			format
		]
		ele.synonyms.forEach[
			formatAttributeSynonym(document)
		]
	}
	
	/**
	 * Use default format() when isEvent, isProduct and Enum formatting is implemented
	 */
	private def formatAttributeSynonym(RosettaSynonym ele,  extension IFormattableDocument document) {
		ele.prepend(NEW_LINE_LOW_PRIO).append(NEW_LINE_LOW_PRIO)
	}
	
	def dispatch void format(Condition ele, extension IFormattableDocument document) {
		
		ele.annotations.forEach[format]
		ele.regionFor.keyword(':').append(ONE_SPACE_PRESERVE_NEWLINE)
		ele.formatDefinition(document)
		val eleEnd = ele.nextHiddenRegion
		set(
			ele.regionFor.keyword(':').nextHiddenRegion,
			eleEnd,
			INDENT
		)
		ele.constraint.format
		ele.expression.format
	}
	
	private def void formatDefinition(Definable ele, extension IFormattableDocument document) {
		if (ele.definition !== null)
			ele.regionFor.keyword('>').append(NEW_LINE)
	}
	
	private def void formatDefinition(RosettaDefinable ele, extension IFormattableDocument document) {
		if (ele.definition !== null)
			ele.regionFor.keyword('>').append(NEW_LINE)
	}
	
	def dispatch void format(Constraint ele, extension IFormattableDocument document) {
		ele.regionFor.keyword(necessityAccess.requiredRequiredKeyword_1_0).prepend(ONE_SPACE_PRESERVE_NEWLINE)
		ele.regionFor.keyword(necessityAccess.optionalOptionalKeyword_0_0).prepend(ONE_SPACE_PRESERVE_NEWLINE)
		ele.regionFor.keyword(
			constraintAccess.choiceKeyword_1
		).surround(ONE_SPACE)
		
		ele.allRegionsFor.keyword(',').prepend(NO_SPACE_LOW_PRIO).append(ONE_SPACE_PRESERVE_NEWLINE)
	}
	
	def dispatch void format(AnnotationRef ele, extension IFormattableDocument document) {
		ele.regionFor.keyword(annotationRefAccess.leftSquareBracketKeyword_0).append(NO_SPACE)
		ele.regionFor.keyword(annotationRefAccess.rightSquareBracketKeyword_3).prepend(NO_SPACE)
		ele.regionFor.assignment(annotationRefAccess.attributeAssignment_2_0).prepend(ONE_SPACE)
	}
	
	def dispatch void format(Function ele, extension IFormattableDocument document) {
		ele.regionFor.keyword(functionAccess.funcKeyword_0).append(ONE_SPACE).prepend(NEW_ROOT_ELEMENT)
		ele.regionFor.keyword(':').prepend(NO_SPACE).append(ONE_SPACE)
		ele.formatDefinition(document)
		ele.annotations.forEach[
			prepend(NEW_LINE)prepend(NEW_LINE)
			format
		]
		
		val inputsKW = ele.regionFor.keyword(functionAccess.inputsKeyword_5_0)
		inputsKW.prepend(NEW_LINE)
		if (ele.inputs.size <= 1) {
			inputsKW.append(ONE_SPACE_PRESERVE_NEWLINE)
		} else {
			inputsKW.append(NEW_LINE)
		}
		ele.interior(INDENT).append(NEW_LINE_LOW_PRIO)
		ele.inputs.forEach[
			surround(INDENT)
			prepend(NEW_LINE_LOW_PRIO)
			format
		]
		
		ele.regionFor.keyword(functionAccess.outputKeyword_6_0).prepend(NEW_LINE).append(ONE_SPACE_PRESERVE_NEWLINE)
		if(ele.output !== null) {
			set(
				ele.regionFor.keyword(functionAccess.outputKeyword_6_0)?.nextHiddenRegion,
				ele.output.nextHiddenRegion,
				INDENT
			)
			ele.output.format
		}
		
		ele.shortcuts.forEach[
			prepend(NEW_LINE)
			format
		]
		ele.conditions.forEach[
			prepend(NEW_LINE)
			format
		]
		ele.operations.forEach[
			prepend(NEW_LINE)
			format
		]
		ele.postConditions.forEach[
			prepend(NEW_LINE)
			format
		]
		
	}
	
	def dispatch void format(ShortcutDeclaration ele, extension IFormattableDocument document) {
		ele.regionFor.keyword(shortcutDeclarationAccess.aliasKeyword_0).append(ONE_SPACE)
		ele.regionFor.keyword(':').prepend(NO_SPACE).append(ONE_SPACE_PRESERVE_NEWLINE)
		ele.formatDefinition(document)
		val eleEnd = ele.nextHiddenRegion
		set(
			ele.regionFor.keyword(':').nextHiddenRegion,
			eleEnd,
			INDENT
		)
		ele.expression.format
	}
	def dispatch void format(RosettaAlias ele, extension IFormattableDocument document) {
		ele.regionFor.keyword(rosettaAliasAccess.aliasKeyword_0).append(ONE_SPACE)
		val eleEnd = ele.nextHiddenRegion
		set(
			ele.regionFor.assignment(rosettaNamedAccess.nameAssignment).nextHiddenRegion,
			eleEnd,
			INDENT
		)
		ele.expression.format
	}
	
	def dispatch void format(Operation ele, extension IFormattableDocument document) {
		ele.regionFor.keyword(':').prepend(NO_SPACE).append(ONE_SPACE_PRESERVE_NEWLINE)
		val eleEnd = ele.nextHiddenRegion
		set(
			ele.regionFor.keyword(':').nextHiddenRegion,
			eleEnd,
			INDENT
		)
		ele.expression.format
	}

	def dispatch void format(RosettaRegulatoryReference rosettaRegulatoryReference,
		extension IFormattableDocument document) {
		rosettaRegulatoryReference.prepend[newLine].surround[indent]
	}

	def dispatch void format(RosettaClassSynonym ele, extension IFormattableDocument document) {
		ele.prepend(NEW_LINE_LOW_PRIO).append(NEW_LINE_LOW_PRIO)
	}
	
	def dispatch void format(RosettaSynonym rosettaSynonym, extension IFormattableDocument document) {
		singleIndentedLine(rosettaSynonym, document)
	}
	
	def dispatch void format(RosettaEnumeration ele, extension IFormattableDocument document) {
		ele.regionFor.keyword(enumerationAccess.enumKeyword_0).prepend(NEW_ROOT_ELEMENT)
		val eleEnd = ele.nextHiddenRegion
		set(
			ele.regionFor.keyword(enumerationAccess.enumKeyword_0).nextHiddenRegion,
			eleEnd,
			INDENT
		)
		ele.synonyms.forEach[formatAttributeSynonym(document)]
		ele.enumValues.forEach[ format ]
	}

	def dispatch void format(RosettaEnumValue rosettaEnumValue, extension IFormattableDocument document) {
		rosettaEnumValue.prepend(NEW_LINE)
		rosettaEnumValue.enumSynonyms.forEach[
			format
		]
	}

	def dispatch void format(RosettaEnumSynonym rosettaEnumSynonym, extension IFormattableDocument document) {
		rosettaEnumSynonym.prepend[newLine].surround[indent]
	}

	def dispatch void format(RosettaContainsExpression ele, extension IFormattableDocument document) {
		ele.regionFor.keywords(
			rosettaCalcExistsAccess.containsKeyword_1_0_2_1_0
		).forEach [
			surround(ONE_SPACE)
		]
		ele.container.format
		ele.contained.format
	}
	
	def dispatch void format(RosettaDisjointExpression ele, extension IFormattableDocument document) {
		ele.regionFor.keywords(
			rosettaCalcExistsAccess.containsKeyword_1_0_2_1_0
		).forEach [
			surround(ONE_SPACE)
		]
		ele.container.format
		ele.disjoint.format
	}

	def dispatch void format(RosettaExpression ele, extension IFormattableDocument document) {
	}
	
	def dispatch void format(RosettaBinaryOperation ele, extension IFormattableDocument document) {
		ele.left.format
		ele.regionFor.feature(RosettaPackage.Literals.ROSETTA_BINARY_OPERATION__OPERATOR).surround(ONE_SPACE_PRESERVE_NEWLINE)
		ele.right.format
	}
	def dispatch void format(RosettaParenthesisCalcExpression ele, extension IFormattableDocument document) {
		ele.regionFor.keyword('(').append(NO_SPACE_PRESERVE_NEW_LINE)
		ele.regionFor.keyword(')').prepend(NO_SPACE_PRESERVE_NEW_LINE)
		ele.expression.format
	}
	
	def dispatch void format(RosettaCallableWithArgsCall ele, extension IFormattableDocument document) {
		ele.regionFor.keyword('(').append(NO_SPACE_PRESERVE_NEW_LINE)
		ele.regionFor.keyword(')').prepend(NO_SPACE_PRESERVE_NEW_LINE)
		ele.regionFor.keywords(',').forEach[prepend(NO_SPACE).append(ONE_SPACE)]
	}
	
	def dispatch void format(RosettaConditionalExpression ele, extension IFormattableDocument document) {
		ele.regionFor.keywords(
			rosettaCalcConditionalExpressionAccess.ifKeyword_1
		).forEach [
			append(ONE_SPACE_PRESERVE_NEWLINE)
		]
		ele.regionFor.keywords(
			rosettaCalcConditionalExpressionAccess.elseKeyword_5_0,
			rosettaCalcConditionalExpressionAccess.thenKeyword_3
		).forEach [
			prepend(ONE_SPACE_PRESERVE_NEWLINE)
			append(ONE_SPACE_PRESERVE_NEWLINE)
		]
		ele.^if.interior(INDENT).format
		ele.elsethen.interior(INDENT).format
		ele.ifthen.interior(INDENT).format
	}

	def dispatch void format(RosettaWorkflowRule rosettaWorkflowRule, extension IFormattableDocument document) {
	}

	def dispatch void format(RosettaTreeNode rosettaTreeNode, extension IFormattableDocument document) {
	}

	def dispatch void format(RosettaChoiceRule rosettaChoiceRule, extension IFormattableDocument document) {
	}

	def dispatch void format(RosettaExistsExpression ele, extension IFormattableDocument document) {
		ele.argument.format
	}

	def dispatch void format(RosettaFeatureCall ele, extension IFormattableDocument document) {
		ele.regionFor.keyword('->').surround(ONE_SPACE)
	}

	def dispatch void format(RosettaGroupByFeatureCall rosettaGroupByFeatureCall, extension IFormattableDocument document) {
	}

	def dispatch void format(RosettaExternalSynonymSource externalSynonymSource,
		extension IFormattableDocument document) {
		indentedBraces(externalSynonymSource, document)
		formatChild(externalSynonymSource.externalClasses, document)
		formatChild(externalSynonymSource.externalEnums, document)
	}

	def dispatch void format(RosettaExternalClass externalClass, extension IFormattableDocument document) {
		externalClass.regionFor.keyword(':').prepend[noSpace]
		externalClass.prepend[lowPriority; setNewLines(2)]
		formatChild(externalClass.regularAttributes, document)
	}

	def dispatch void format(RosettaExternalEnum externalEnum, extension IFormattableDocument document) {
		externalEnum.regionFor.keyword(':').prepend[noSpace]
		externalEnum.prepend[lowPriority; setNewLines(2)]
		formatChild(externalEnum.regularValues, document)
	}

	def dispatch void format(RosettaExternalRegularAttribute externalRegularAttribute,
		extension IFormattableDocument document) {
		externalRegularAttribute.regionFor.keyword('+').append[oneSpace].prepend[newLine]
		externalRegularAttribute.surround[indent]
		formatChild(externalRegularAttribute.externalSynonyms, document)
	}
	
	def dispatch void format(RosettaExternalEnumValue externalEnumValue,
		extension IFormattableDocument document) {
		externalEnumValue.regionFor.keyword('+').append[oneSpace].prepend[newLine]
		externalEnumValue.surround[indent]
		formatChild(externalEnumValue.externalEnumSynonyms, document)
	}
	
	

	def dispatch void format(RosettaExternalSynonym externalSynonym, extension IFormattableDocument document) {
		externalSynonym.prepend[newLine].surround[indent]
	}
	
	def dispatch void format(ListLiteral ele, extension IFormattableDocument document) {
		interior(
			ele.regionFor.keyword('[').append(NO_SPACE_PRESERVE_NEW_LINE),
			ele.regionFor.keyword(']').prepend(NO_SPACE_PRESERVE_NEW_LINE),
			INDENT
		)
		ele.regionFor.keywords(',').forEach[prepend(NO_SPACE).append(ONE_SPACE_PRESERVE_NEWLINE)]
	}

	def void indentedBraces(EObject eObject, extension IFormattableDocument document) {
		val lcurly = eObject.regionFor.keyword('{').prepend[newLine].append[newLine]
		val rcurly = eObject.regionFor.keyword('}').prepend[newLine].append[setNewLines(2)]
		interior(lcurly, rcurly)[highPriority; indent]
	}

	def void formatChild(List<? extends EObject> children, extension IFormattableDocument document) {
		for (EObject child : children) {
			child.format;
		}
	}

	private def void singleIndentedLine(EObject eObject, extension IFormattableDocument document) {
		eObject.prepend(NEW_LINE_LOW_PRIO).append(NEW_LINE_LOW_PRIO).surround[indent]
	}

	def void surroundWithOneSpace(EObject eObject, extension IFormattableDocument document) {
		for (ISemanticRegion w : eObject.allSemanticRegions) {
			w.surround[oneSpace];
		}
	}

	def void appendWithOneSpace(EObject eObject, extension IFormattableDocument document) {
		eObject.regionFor.keyword(',').append[oneSpace]
	}
}
