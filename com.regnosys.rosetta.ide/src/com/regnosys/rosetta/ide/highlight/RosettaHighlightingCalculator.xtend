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
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaMarketPractice
import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaRegulatoryReference
import com.regnosys.rosetta.rosetta.RosettaStereotype
import com.regnosys.rosetta.rosetta.RosettaSynonymBase
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.RosettaWorkflowRule
import com.regnosys.rosetta.rosetta.simple.AnnotationRef
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.SimplePackage
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultSemanticHighlightingCalculator
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.util.CancelIndicator

class RosettaHighlightingCalculator extends DefaultSemanticHighlightingCalculator implements RosettaHighlightingStyles {

	override protected highlightElement(EObject object, IHighlightedPositionAcceptor acceptor,
		CancelIndicator cancelIndicator) {
		if (object instanceof RosettaTypedFeature) {
			switch (object.type) {
				RosettaClass, Data:
					highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_TYPED__TYPE, CLASS_ID)
				RosettaEnumeration:
					highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_TYPED__TYPE, ENUM_ID)
				RosettaBasicType,
				RosettaRecordType:
					highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_TYPED__TYPE, BASICTYPE_ID)
				RosettaQualifiedType:
					highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_TYPED__TYPE, BASICTYPE_ID)
				RosettaCalculationType:
					highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_TYPED__TYPE, BASICTYPE_ID)
			}
			if (object instanceof RosettaRegularAttribute) {
				highlightFeatureForAllChildren(acceptor, object, RosettaPackage.Literals.ROSETTA_REGULAR_ATTRIBUTE__META_TYPES, META_ID)
			}
		} else if (object instanceof RosettaClass) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_NAMED__NAME, CLASS_ID)
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_CLASS__SUPER_TYPE, CLASS_ID)
		} else if (object instanceof Data) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_NAMED__NAME, CLASS_ID)
			highlightFeature(acceptor, object, SimplePackage.Literals.DATA__SUPER_TYPE, CLASS_ID)
		} else if (object instanceof RosettaEnumeration) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_NAMED__NAME, ENUM_ID)
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_ENUMERATION__SUPER_TYPE, ENUM_ID)
		} else if (object instanceof RosettaEnumValueReference) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_ENUM_VALUE_REFERENCE__ENUMERATION, ENUM_ID)
		} else if (object instanceof RosettaRegulatoryReference) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_REGULATORY_MANDATE__REG_REGIME, REGULATOR_ID)
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_REGULATORY_MANDATE__MANDATES, NAMED_ID)
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_REGULATORY_REFERENCE__SEGMENTS, NAMED_ID)
		} else if (object instanceof RosettaMarketPractice) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_MARKET_PRACTICE__ORGANISATION, ORGANISATION_ID)
		} else if (object instanceof RosettaSynonymBase) {
			highlightFeatureForAllChildren(acceptor, object, RosettaPackage.Literals.ROSETTA_SYNONYM_BASE__SOURCES, SOURCE_ID)
			// TODO this works for RosettaSynonym but not RosettaMetaSynonym
			//highlightFeatureForAllChildren(acceptor, object, RosettaPackage.Literals.ROSETTA_SYNONYM_BASE__META_VALUES, META_ID)
		} else if (object instanceof RosettaStereotype) {
			highlightFeatureForAllChildren(acceptor, object, RosettaPackage.Literals.ROSETTA_STEREOTYPE__VALUES, STEREOTYPE_ID)
		} else if (object instanceof RosettaChoiceRule) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_NAMED__NAME, RULE_ID)
		} else if (object instanceof RosettaWorkflowRule) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_NAMED__NAME, RULE_ID)
		} else if (object instanceof RosettaDataRule) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_NAMED__NAME, RULE_ID)
		} else if (object instanceof RosettaCallableCall) {
			if (object.callable instanceof RosettaAlias)
				highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_CALLABLE_CALL__CALLABLE, ALIASES_ID)
		} else if (object instanceof RosettaAlias) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_NAMED__NAME, ALIASES_ID)
		} else if (object instanceof RosettaExternalClass) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_EXTERNAL_CLASS__CLASS_REF, CLASS_ID)
		} else if (object instanceof RosettaExternalSynonymSource) {
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_NAMED__NAME, SOURCE_ID)
			highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_EXTERNAL_SYNONYM_SOURCE__SUPER_SYNONYM, SOURCE_ID)
		} else if (object instanceof AnnotationRef) {
			highlightFeature(acceptor, object, SimplePackage.Literals.ANNOTATION_REF__ANNOTATION, ANNO_ID)
			highlightFeature(acceptor, object, SimplePackage.Literals.ANNOTATION_REF__ATTRIBUTE, ANNO_ATTR_ID)
		} else {
			switch(object) {
				RosettaFeatureCall:
					highlightFeature(acceptor, object, RosettaPackage.Literals.ROSETTA_FEATURE_CALL__FEATURE, DEFAULT_ID)
			}
		}
		return false
	}
	
	/**
	 * Highlights all the feature's child nodes (not just the first one)
	 */
	protected def void highlightFeatureForAllChildren(IHighlightedPositionAcceptor acceptor, EObject object, EStructuralFeature feature, String... styleIds) {
		val children = NodeModelUtils.findNodesForFeature(object, feature);
		children.forEach[highlightNode(acceptor, it, styleIds)];
	}
}
