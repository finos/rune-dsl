package com.regnosys.rosetta.ui.hyperlinking

import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.ui.editor.hyperlinking.HyperlinkHelper
import org.eclipse.xtext.ui.editor.hyperlinking.IHyperlinkAcceptor

class RosettaHyperlinkHelper extends HyperlinkHelper {

	override createHyperlinksByOffset(XtextResource resource, int offset, IHyperlinkAcceptor acceptor) {
		super.createHyperlinksByOffset(resource, offset, acceptor)
		val eObj = EObjectAtOffsetHelper.resolveElementAt(resource, offset)
		if (eObj instanceof FunctionDispatch) {
			val nameNode = getFeatureNode(eObj, RosettaPackage.Literals.ROSETTA_NAMED__NAME, offset)
			if (nameNode !== null) {
				val dispatch = EcoreUtil2.getSiblingsOfType(eObj, Function).filter [
					!(it instanceof FunctionDispatch) && it.name == eObj.name && it.operations.nullOrEmpty
				].head
				if (dispatch !== null)
					createHyperlinksTo(resource, nameNode, dispatch, acceptor);
			}
		}
	}

	def protected INode getFeatureNode(EObject obj, EStructuralFeature feature, int offset) {
		NodeModelUtils.findNodesForFeature(obj, feature).findFirst [ node |
			node.textRegion.contains(offset)
		]
	}

}
