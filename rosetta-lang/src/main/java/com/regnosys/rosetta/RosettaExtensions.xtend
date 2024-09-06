package com.regnosys.rosetta

import com.google.common.base.CaseFormat
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RType
import java.util.Collection
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet

import com.regnosys.rosetta.types.builtin.RRecordType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import javax.inject.Singleton
import java.util.List
import com.regnosys.rosetta.utils.PositiveIntegerInterval
import org.eclipse.xtext.util.SimpleCache
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.scoping.RosettaScopeProvider
import com.regnosys.rosetta.rosetta.simple.SimpleFactory
import com.regnosys.rosetta.types.RObjectFactory

@Singleton // see `metaFieldsCache`
class RosettaExtensions {
	
	@Inject RBuiltinTypeService builtins
	@Inject RObjectFactory objectFactory
	
	def boolean isResolved(EObject obj) {
		obj !== null && !obj.eIsProxy
	}
	
	def Iterable<? extends RosettaFeature> allFeatures(RType t, EObject context) {
		allFeatures(t, context?.eResource?.resourceSet)
	}
	def Iterable<? extends RosettaFeature> allFeatures(RType t, ResourceSet resourceSet) {
		switch t {
			RDataType:
				t.allNonOverridenAttributes.map[EObject]
			REnumType:
				t.EObject.allEnumValues
			RRecordType: {
				if (resourceSet !== null) {
					builtins.toRosettaType(t, RosettaRecordType, resourceSet).features
				} else {
					#[]
				}
			}
			default:
				#[]
		}
	}
	
	def Set<RosettaEnumeration> getAllSuperEnumerations(RosettaEnumeration e) {
		doGetSuperEnumerations(e, newLinkedHashSet)
	}
	
	private def Set<RosettaEnumeration> doGetSuperEnumerations(RosettaEnumeration e, Set<RosettaEnumeration> seenEnums) {
		if(e !== null && seenEnums.add(e)) 
			doGetSuperEnumerations(e.superType, seenEnums)
		return seenEnums
	}

	def getAllEnumValues(RosettaEnumeration e) {
		e.allSuperEnumerations.map[enumValues].flatten
	}
	
	def Set<RosettaSynonym> getAllSynonyms(RosettaSynonym s) {
		doGetSynonyms(s, newLinkedHashSet)
	}
		
	private def Set<RosettaSynonym> doGetSynonyms(RosettaSynonym s, Set<RosettaSynonym> seenSynonyms) {
		if(s !== null && seenSynonyms.add(s)) 
			doGetSynonyms(s, seenSynonyms)
		return seenSynonyms		
	}

	/**
	 * Collect all expressions
	 */
	def void collectExpressions(RosettaExpression expr, (RosettaExpression) => void visitor) {
		if(expr instanceof RosettaBinaryOperation) {
			if(expr.operator.equals("or") || expr.operator.equals("and")) {
				expr.left.collectExpressions(visitor)
				expr.right.collectExpressions(visitor)
			}
			else {
				visitor.apply(expr)
			}	
		}
		if(expr instanceof RosettaConditionalExpression) {
			expr.ifthen.collectExpressions(visitor)
			expr.elsethen.collectExpressions(visitor)
		}
		else {
			visitor.apply(expr)
		}
	}
	
	def boolean isProjectLocal(URI platformResourceURI, URI candidateUri) {
		if (!platformResourceURI.isPlatformResource) {
			// synthetic tests URI
			return true
		}
		val projectName = platformResourceURI.segment(1)
		if (candidateUri.isPlatformResource) {
			return projectName == candidateUri.segment(1)
		}
		return false
	}
	
	def metaAnnotations(Annotated it) {
		allAnnotations.filter[annotation?.name == "metadata"]
	}
	
	def hasKeyedAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "key"]
	}
	
	def hasTemplateAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "template"]
	}
	
	def boolean hasMetaDataAnnotations(RAttribute attribute) {
		attribute.metaAnnotations.exists[name == "reference" || name == "location" || name == "scheme" || name == "id"]
	}
	
	def boolean hasMetaDataAnnotations(Annotated it) {
		metaAnnotations.exists[attribute?.name == "reference" || attribute?.name == "location" || attribute?.name == "scheme" || attribute?.name == "id"]
	}
	
	def boolean hasMetaFieldAnnotations(Annotated it) {
		metaAnnotations.exists[attribute?.name != "reference" && attribute?.name != "address"]
	}
	
	def boolean hasMetaDataAddress(RAttribute attribute) {
		attribute.metaAnnotations.exists[name == "address"]
	}
	
	def boolean hasMetaDataAddress(Annotated it) {
		metaAnnotations.exists[attribute?.name == "address"]
	}
	
	def boolean hasIdAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "id"]
	}
	def boolean hasIdAnnotation(RAttribute it) {
		metaAnnotations.exists[name == "id"]
	}
	def boolean hasReferenceAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "reference"]
	}
	def hasCalculationAnnotation(Annotated it) {
		allAnnotations.exists[annotation?.name == "calculation"]
	}
	
	def boolean isReference(Attribute attribute) {
		return attribute.hasMetaDataAnnotations || attribute.hasMetaDataAddress
	}
	def boolean isReference(RAttribute attribute) {
		return attribute.hasMetaDataAnnotations || attribute.hasMetaDataAddress
	}
	
	def private allAnnotations(Annotated withAnnotations) {
		withAnnotations?.annotations?.filter[annotation.isResolved]
	}
	
	def String conditionName(Condition cond, RDataType data) {
		return cond.conditionName(data.name, data.EObject.conditions)
	}

	def String conditionName(Condition cond, Function func) {
		return cond.conditionName(func.name, func.conditions)
	}
	
	def boolean isConstraintCondition(Condition cond) {
		return cond.isOneOf || cond.isChoice
	}
	
	private def boolean isOneOf(Condition cond) {
		return cond.expression instanceof OneOfOperation
	}
	
	private def boolean isChoice(Condition cond) {
		return cond.expression instanceof ChoiceOperation
	}
	
	//Name convention: <type name>(<condition name>|<condition type><#>) where condition type should be 'choice' or 'oneof'.
	private def String conditionName(Condition cond, String containerName, Collection<Condition> conditions) {
		val name = if (!cond.name.nullOrEmpty)
				cond.name
			else {
				val idx = conditions.filter[name.nullOrEmpty].toList.indexOf(cond)
				val type = if (cond.isOneOf) {
						'OneOf' 
					} else if (cond.isChoice) {
						 'Choice'
					} else 'DataRule'
				'''«type»«idx»'''
			}
		return '''«containerName»«name»'''
	}
	
	def String toConditionJavaType(String conditionName) {
		val allUnderscore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, conditionName)
		val camel = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, allUnderscore)
		return camel
	}
	
	
	def String javaAnnotation(RAttribute attr) {
		if (attr.name == "key" && attr.RType.name == "Key" && attr.RType.namespace.toString == "com.rosetta.model.lib.meta") {
			return 'location'
		} else if (attr.name == "reference" && attr.RType.name == "Reference" && attr.RType.namespace.toString == "com.rosetta.model.lib.meta") {
			return 'address'
		} else
			return attr.name
	}
	// Copied over from RosettaAttributeExtensions.
	def List<RAttribute> additionalAttributes(RDataType t) {
		val res = newArrayList
		if(hasKeyedAnnotation(t.EObject)){
			res.add(new RAttribute(
				'meta',
				null,
				emptyList,
				provideMetaFieldsType(t),
				emptyList,
				PositiveIntegerInterval.bounded(0, 1),
				null,
				null
			))
		}
		return res
	}
	
	String METAFIELDS_CLASS_NAME = 'MetaFields'
	String META_AND_TEMPLATE_FIELDS_CLASS_NAME = 'MetaAndTemplateFields'
	
	SimpleCache<RDataType, RDataType> metaFieldsCache = new SimpleCache[RDataType t|
		val rosModel = RosettaFactory.eINSTANCE.createRosettaModel()
		rosModel.name = RosettaScopeProvider.LIB_NAMESPACE + ".metafields"
		val name = if (hasTemplateAnnotation(t.EObject)) META_AND_TEMPLATE_FIELDS_CLASS_NAME else METAFIELDS_CLASS_NAME
		val data = SimpleFactory.eINSTANCE.createData
		data.model = rosModel
		data.name = name
		return objectFactory.buildRDataType(data)
	]
	private def RType provideMetaFieldsType(RDataType t) {
		metaFieldsCache.get(t)
	}
}
