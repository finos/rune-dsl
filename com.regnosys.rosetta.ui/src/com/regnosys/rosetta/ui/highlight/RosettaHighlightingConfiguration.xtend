package com.regnosys.rosetta.ui.highlight

import com.regnosys.rosetta.ide.highlight.RosettaHighlightingCalculator
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.RGB
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor
import org.eclipse.xtext.ui.editor.utils.TextStyle

class RosettaHighlightingConfiguration extends DefaultHighlightingConfiguration {

	override configure(IHighlightingConfigurationAcceptor acceptor) {
		super.configure(acceptor)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.PLAYGROUND_REQUEST_ID, "PlaygroundRequest", playgroundRequestTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.PLAYGROUND_REQUEST_KEYWORD_ID, "PlaygroundRequestKeyword", playgroundRequestKeywordTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.DOCUMENTATION_ID, "Documentation", documentationTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.NAMED_ID, "Named", namedTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.CLASS_ID, "Class", classTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.ENUM_ID, "Enumeration", enumTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.BASICTYPE_ID, "BasicType", basicTypeTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.REGULATOR_ID, "Regulator", regulatorTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.ORGANISATION_ID, "Organisation", organisationTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.SOURCE_ID, "Source", sourceTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.STEREOTYPE_ID, "Stereotype", stereotypeTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.DATASOURCE_ID, "DataSource", dataSourceTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.RULE_ID, "Rule", ruleTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.ALIASES_ID, "Aliases", aliasesTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.META_ID, "Meta", metaTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.ANNO_ID, "Annotation", annotationTextStyle)
		acceptor.acceptDefaultHighlighting(RosettaHighlightingCalculator.ANNO_ATTR_ID, "AnnotationAttribute", annotationAttrTextStyle)
	}
	
	def TextStyle playgroundRequestTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.NORMAL
			color = new RGB(130, 130, 130)
		]
	}
	
	def TextStyle playgroundRequestKeywordTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(160, 160, 160)
		]
	}
	
	def TextStyle documentationTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.NORMAL
			color = new RGB(96, 139, 74)
		]
	}

	def TextStyle namedTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(175, 14, 46)
		]
	}
	
	def TextStyle classTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(63, 95, 191)
		]
	}

	def TextStyle enumTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(120, 139, 98)
		]
	}

	def TextStyle basicTypeTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(14, 84, 38)
		]
	}
	def TextStyle annotationTextStyle() {
		basicTypeTextStyle.copy => [
			style = SWT.NORMAL
		]
	}
	def TextStyle annotationAttrTextStyle() {
		annotationTextStyle.copy => [
			color = new RGB(224, 94, 58)
		]
	}

	def TextStyle regulatorTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(80, 101, 11)
		]
	}

	def TextStyle organisationTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(202, 19, 19)
		]
	}
	
	def TextStyle sourceTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(51, 0, 102)
		]
	}

	def TextStyle stereotypeTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(0, 102, 51)
		]
	}

	def TextStyle dataSourceTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(51, 0, 102)
		]
	}

	def TextStyle ruleTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(88, 25, 125)
		]
	}

	def TextStyle aliasesTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.BOLD
			color = new RGB(19, 55, 154)
		]
	}
	
	def TextStyle metaTextStyle() {
		defaultTextStyle.copy => [
			style = SWT.NORMAL
			color = new RGB(153, 0, 76)
		]
	}
}
