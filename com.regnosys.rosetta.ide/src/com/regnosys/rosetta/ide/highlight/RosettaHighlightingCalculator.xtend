package com.regnosys.rosetta.ide.highlight

import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaCalculationType
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaChoiceRule
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaDataRule
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalClass
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource
import com.regnosys.rosetta.rosetta.RosettaHeader
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.RosettaMarketPractice
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaRegulatoryReference
import com.regnosys.rosetta.rosetta.RosettaStereotype
import com.regnosys.rosetta.rosetta.RosettaSynonymBase
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.RosettaWorkflowRule
import com.regnosys.rosetta.rosetta.simple.AnnotationRef
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.SimplePackage
import java.util.regex.Pattern
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultSemanticHighlightingCalculator
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.nodemodel.ILeafNode
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.util.CancelIndicator

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*

class RosettaHighlightingCalculator extends DefaultSemanticHighlightingCalculator implements RosettaHighlightingStyles {

	override protected highlightElement(EObject object, IHighlightedPositionAcceptor acceptor,
		CancelIndicator cancelIndicator) {
		switch (object) {
			RosettaTypedFeature: {
				switch (object.type) {
					RosettaClass,
					Data:
						highlightFeature(acceptor, object, ROSETTA_TYPED__TYPE, CLASS_ID)
					RosettaEnumeration:
						highlightFeature(acceptor, object, ROSETTA_TYPED__TYPE, ENUM_ID)
					RosettaBasicType,
					RosettaRecordType:
						highlightFeature(acceptor, object, ROSETTA_TYPED__TYPE, BASICTYPE_ID)
					RosettaQualifiedType:
						highlightFeature(acceptor, object, ROSETTA_TYPED__TYPE, BASICTYPE_ID)
					RosettaCalculationType:
						highlightFeature(acceptor, object, ROSETTA_TYPED__TYPE, BASICTYPE_ID)
				}
				if (object instanceof RosettaRegularAttribute) {
					highlightFeatureForAllChildren(acceptor, object, ROSETTA_REGULAR_ATTRIBUTE__META_TYPES, META_ID)
				}
			}
			RosettaClass: {
				highlightFeature(acceptor, object, ROSETTA_NAMED__NAME, CLASS_ID)
				highlightFeature(acceptor, object, ROSETTA_CLASS__SUPER_TYPE, CLASS_ID)
			}
			Data: {
				highlightFeature(acceptor, object, ROSETTA_NAMED__NAME, CLASS_ID)
				highlightFeature(acceptor, object, SimplePackage.Literals.DATA__SUPER_TYPE, CLASS_ID)
			}
			RosettaEnumeration: {
				highlightFeature(acceptor, object, ROSETTA_NAMED__NAME, ENUM_ID)
				highlightFeature(acceptor, object, ROSETTA_ENUMERATION__SUPER_TYPE, ENUM_ID)
			}
			RosettaEnumValueReference: {
				highlightFeature(acceptor, object, ROSETTA_ENUM_VALUE_REFERENCE__ENUMERATION, ENUM_ID)
			}
			RosettaRegulatoryReference: {
				highlightFeature(acceptor, object, ROSETTA_REGULATORY_MANDATE__REG_REGIME, REGULATOR_ID)
				highlightFeature(acceptor, object, ROSETTA_REGULATORY_MANDATE__MANDATES, NAMED_ID)
				highlightFeature(acceptor, object, ROSETTA_REGULATORY_REFERENCE__SEGMENTS, NAMED_ID)

			}
			RosettaMarketPractice: {
				highlightFeature(acceptor, object, ROSETTA_MARKET_PRACTICE__ORGANISATION, ORGANISATION_ID)
			}
			RosettaSynonymBase: {
				highlightFeatureForAllChildren(acceptor, object, ROSETTA_SYNONYM_BASE__SOURCES, SOURCE_ID)
			// TODO this works for RosettaSynonym but not RosettaMetaSynonym
			// highlightFeatureForAllChildren(acceptor, object, ROSETTA_SYNONYM_BASE__META_VALUES, META_ID)
			}
			RosettaStereotype: {
				highlightFeatureForAllChildren(acceptor, object, ROSETTA_STEREOTYPE__VALUES, STEREOTYPE_ID)
			}
			RosettaHeader: {
				highlightFeature(acceptor, object, ROSETTA_HEADER__NAMESPACE, SOURCE_ID)
				highlightFeature(acceptor, object, ROSETTA_HEADER__VERSION, SOURCE_ID)
			}
			RosettaChoiceRule: {
				highlightFeature(acceptor, object, ROSETTA_NAMED__NAME, RULE_ID)
			}
			RosettaWorkflowRule: {
				highlightFeature(acceptor, object, ROSETTA_NAMED__NAME, RULE_ID)
			}
			RosettaDataRule: {
				highlightFeature(acceptor, object, ROSETTA_NAMED__NAME, RULE_ID)
			}
			RosettaCallableCall case object.callable instanceof RosettaAlias: {
				highlightFeature(acceptor, object, ROSETTA_CALLABLE_CALL__CALLABLE, ALIASES_ID)
			}
			RosettaAlias: {
				highlightFeature(acceptor, object, ROSETTA_NAMED__NAME, ALIASES_ID)
			}
			RosettaExternalClass: {
				highlightFeature(acceptor, object, ROSETTA_EXTERNAL_CLASS__CLASS_REF, CLASS_ID)
			}
			RosettaExternalSynonymSource: {
				highlightFeature(acceptor, object, ROSETTA_NAMED__NAME, SOURCE_ID)
				highlightFeature(acceptor, object, ROSETTA_EXTERNAL_SYNONYM_SOURCE__SUPER_SYNONYM, SOURCE_ID)
			}
			AnnotationRef: {
				highlightFeature(acceptor, object, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION, ANNO_ID)
				highlightFeature(acceptor, object, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE, ANNO_ATTR_ID)
			}
			RosettaSynonymValueBase: {
				highlightFeature(acceptor, object, ROSETTA_SYNONYM_VALUE_BASE__PATH)
			}
			RosettaMapPathValue: {
				highlightFeature(acceptor, object, ROSETTA_MAP_PATH_VALUE__PATH)
			}
		}
		return false
	}

	/**
	 * Highlights all the feature's child nodes (not just the first one)
	 */
	private def void highlightFeatureForAllChildren(IHighlightedPositionAcceptor acceptor, EObject object,
		EStructuralFeature feature, String... styleIds) {
		val children = NodeModelUtils.findNodesForFeature(object, feature);
		children.forEach[highlightNode(acceptor, it, styleIds)];
	}

	override protected void highlightFeature(IHighlightedPositionAcceptor acceptor, EObject object,
		EStructuralFeature feature, String... styleIds) {
		switch (feature) {
			case ROSETTA_SYNONYM_VALUE_BASE__PATH,
			case ROSETTA_MAP_PATH_VALUE__PATH: {
				val node = NodeModelUtils.findNodesForFeature(object, feature).head
				if (node !== null) {
					highlightPathString(acceptor, if(node instanceof ILeafNode) #[node] else node.leafNodes.filter [
						!isHidden
					], styleIds)
				}
			}
			default:
				super.highlightFeature(acceptor, object, feature, styleIds)
		}
	}
	
	val arrowPattern  = Pattern.compile("(\\w+)(->)?")
	
	private def highlightPathString(IHighlightedPositionAcceptor acceptor, Iterable<ILeafNode> nodes,
		String... styleIds) {
		nodes.forEach [ node |
			val text = node.text
			if (text.length >= 2) {
				val first = text.charAt(0)
				val last = text.charAt(text.length - 1)
				val isQuote = [char ch | ch == '"'.charAt(0) || ch == "'".charAt(0)]
				if (isQuote.apply(first)) {
					acceptor.addPosition(node.offset, 1, NUMBER_ID)
				}
				if (isQuote.apply(last)) {
					acceptor.addPosition(node.offset + node.length - 1, 1, NUMBER_ID)
				}
				var matcher = arrowPattern.matcher(text)
				while(matcher.find) {
					val id = matcher.group(1)?:''
					val arrow = matcher.group(2)?:''
					acceptor.addPosition(node.offset + matcher.start, id.length, DEFAULT_ID)
					acceptor.addPosition(node.offset + matcher.start + id.length, arrow.length, NUMBER_ID)
				}
			}
		]
	}

}
