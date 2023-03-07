package com.regnosys.rosetta.ide.highlight

import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaCalculationType
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalRef
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaSynonymBase
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
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
import com.regnosys.rosetta.rosetta.RegulatoryDocumentReference

class RosettaHighlightingCalculator extends DefaultSemanticHighlightingCalculator implements RosettaHighlightingStyles {

	override protected highlightElement(EObject object, IHighlightedPositionAcceptor acceptor,
		CancelIndicator cancelIndicator) {
		switch (object) {
			RosettaTypedFeature: {
				switch (object.type) {
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
			RegulatoryDocumentReference: {
				highlightFeature(acceptor, object, REGULATORY_DOCUMENT_REFERENCE__BODY, REGULATOR_ID)
				highlightFeature(acceptor, object, REGULATORY_DOCUMENT_REFERENCE__CORPUSES, NAMED_ID)
				highlightFeature(acceptor, object, REGULATORY_DOCUMENT_REFERENCE__SEGMENTS, NAMED_ID)
			}
			RosettaSynonymBase: {
				highlightFeatureForAllChildren(acceptor, object, ROSETTA_SYNONYM_BASE__SOURCES, SOURCE_ID)
			// TODO this works for RosettaSynonym but not RosettaMetaSynonym
			// highlightFeatureForAllChildren(acceptor, object, ROSETTA_SYNONYM_BASE__META_VALUES, META_ID)
			}
			RosettaExternalRef: {
				highlightFeature(acceptor, object, ROSETTA_EXTERNAL_REF__TYPE_REF, CLASS_ID)
			}
			RosettaExternalSynonymSource: {
				highlightFeature(acceptor, object, ROSETTA_NAMED__NAME, SOURCE_ID)
				highlightFeature(acceptor, object, ROSETTA_EXTERNAL_SYNONYM_SOURCE__SUPER_SOURCES, SOURCE_ID)
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
