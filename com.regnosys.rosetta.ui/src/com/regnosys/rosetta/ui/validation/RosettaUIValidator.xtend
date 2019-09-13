package com.regnosys.rosetta.ui.validation

import com.google.inject.Inject
import com.regnosys.rosetta.resource.Indexed
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaPackage
import com.regnosys.rosetta.validation.RosettaIssueCodes
import java.util.List
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.common.types.xtext.ui.ProjectAwareResourceDescriptionsProvider
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.validation.AbstractDeclarativeValidator
import org.eclipse.xtext.validation.Check

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*

class RosettaUIValidator extends AbstractDeclarativeValidator implements RosettaIssueCodes{

	override protected List<EPackage> getEPackages() {
		return #[RosettaPackage.eINSTANCE]
	}

	@Inject IQualifiedNameProvider qNames
	@Inject ProjectAwareResourceDescriptionsProvider resDecrProvider

	@Check(FAST)
	def void checkClassesMayNotShareSameClassTypeAttributes(RosettaClass element) {
		if (element.root) {
			val resources = resDecrProvider.getResourceDescriptions(element.eResource)
			val allRootClasses = resources.getExportedObjectsByType(ROSETTA_CLASS).filter [
				"true" == Indexed.CLASS_ROOT.getValue(it)
			].map[EObjectOrProxy]
			val resolved = allRootClasses.map[if(eIsProxy) EcoreUtil.resolve(it, element) else it].filter(RosettaClass)
			val allRootAttributes = resolved.flatMap[regularAttributes]
			allRootAttributes.filter[type instanceof RosettaClass].groupBy[type].forEach [ type, attrs |
				if (attrs.size > 1) {
					val attr = attrs.findFirst[element == it.eContainer]
					if (attr !== null) {
						attrs.filter[it != attr].forEach [
							error('''Attribute with Type «it.type.name» is already used in «qNames.getFullyQualifiedName(it)»''',
								attr, RosettaPackage.Literals.ROSETTA_NAMED__NAME)
						]
					}
				}
			]
		}
	}
}
