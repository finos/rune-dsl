package com.regnosys.rosetta.generator.java.calculation

import com.google.common.collect.Iterables
import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaAlias
import com.regnosys.rosetta.rosetta.RosettaArgumentFeature
import com.regnosys.rosetta.rosetta.RosettaArguments
import com.regnosys.rosetta.rosetta.RosettaCalculation
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.RosettaExistsExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaGroupByFeatureCall
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.types.RUnionType
import com.regnosys.rosetta.types.RosettaTypeProvider
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.naming.IQualifiedNameConverter
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider

import static com.regnosys.rosetta.generator.util.Util.*
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration

/**
 * A class that helps determine which RosettaFunctions a Rosetta object refers to
 */
class RosettaExternalFunctionDependencyProvider {

	@Inject extension RosettaTypeProvider
	@Inject extension ResourceDescriptionsProvider
	@Inject extension IQualifiedNameConverter
	@Inject extension RosettaFunctionExtensions

	def Iterable<RosettaCallableWithArgs> functionDependencies(EObject object) {
		switch object {
			RosettaArguments: {
				Iterables.concat(
					functionDependencies(object.features.filter(RosettaArgumentFeature)),
					functionDependencies(object.aliases.filter(RosettaAlias)))
			}
			RosettaArgumentFeature: {
				val rtype = object.expression.getRType

				if (rtype instanceof RUnionType) {
					functionDependencies(rtype.converter)
				} else {
					functionDependencies(object.expression)
				}
			}
			RosettaConditionalExpression: {
				Iterables.concat(
					functionDependencies(object.^if),
					functionDependencies(object.ifthen),
					functionDependencies(object.elsethen)
				)
			}
			RosettaExistsExpression: {
					functionDependencies(object.argument)
			}
			RosettaAlias: {
				val rtype = object.expression.getRType

				if (rtype instanceof RUnionType) {
					functionDependencies(rtype.converter)
				} else {
					functionDependencies(object.expression)
				}
			}
			RosettaGroupByFeatureCall:
				functionDependencies(object.call)
			RosettaFeatureCall:
				functionDependencies(object.receiver)
			RosettaCallableWithArgsCall: {
				Iterables.concat(
					functionDependencies(object.callable),
					object.args.flatMap[args | functionDependencies(args)]
				)
			}
			RosettaCalculation: {
				val qualifiedName = object.name?.toQualifiedName

				if (qualifiedName !== null && qualifiedName.segmentCount == 2) {
					val index = object.eResource.resourceDescriptions
					val matchingArgumentFeatures = index.getExportedObjectsByType(ROSETTA_ARGUMENT_FEATURE)
						.filter [name.firstSegment == qualifiedName.firstSegment]
					val arguments = matchingArgumentFeatures
						.map[IEObjectDescription e | EcoreUtil.resolve(e.EObjectOrProxy, object.eResource)]
						.map[eContainer].filter(RosettaArguments)

					functionDependencies(arguments).toSet
				} else {
					newArrayList
				}
			}
			RosettaExternalFunction: {
				if(!object.isLibrary) newArrayList(object) else newArrayList
			}
			Function: {
				// TODO change linking to link against main dispatch func only
				val me = if(!object.handleAsEnumFunction && !object.dispatchingFunction) newArrayList(object) else newArrayList
				if (object.handleAsEnumFunction) {
					distinctBy((functionDependencies(object.shortcuts) +
						object.dispatchingFunctions.map[functionDependencies].flatten), [name])
				} else {
					Iterables.concat(functionDependencies(object.shortcuts), me)
				}
			}
			ShortcutDeclaration: {
				functionDependencies(object.expression)
			}
			default:
				newArrayList
		}
	}
	
	def Iterable<RosettaCallableWithArgs> functionDependencies(Iterable<? extends EObject> objects) {
		distinctBy(objects.map[object | functionDependencies(object)].flatten, [f|f.name]);
	}
}
