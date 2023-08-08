package com.regnosys.rosetta

import com.google.common.base.CaseFormat
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation
import com.regnosys.rosetta.rosetta.expression.OneOfOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.utils.ExternalAnnotationUtil
import com.rosetta.model.lib.path.RosettaPath
import java.util.Collection
import java.util.List
import java.util.Map
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import com.regnosys.rosetta.types.builtin.RRecordType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import org.eclipse.emf.ecore.resource.ResourceSet
import com.regnosys.rosetta.rosetta.RosettaRecordType
import java.util.Optional

class RosettaExtensions {
	
	@Inject ExternalAnnotationUtil externalAnn
	@Inject RBuiltinTypeService builtins
	
	def boolean isResolved(EObject obj) {
		obj !== null && !obj.eIsProxy
	}
	
	def Iterable<? extends RosettaFeature> allFeatures(RType t, EObject context) {
		allFeatures(t, context?.eResource?.resourceSet)
	}
	def Iterable<? extends RosettaFeature> allFeatures(RType t, ResourceSet resourceSet) {
		switch t {
			RDataType:
				t.data.allAttributes
			REnumType:
				t.enumeration.allEnumValues
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
	
	def Set<Data> getAllSuperTypes(Data clazz) {
		doGetSuperTypes(clazz, newLinkedHashSet)
	}
	
	private def Set<Data> doGetSuperTypes(Data clazz, Set<Data> seenClasses) {
		if(clazz !== null && seenClasses.add(clazz)) 
			doGetSuperTypes(clazz.superType, seenClasses)
		return seenClasses
	}

	def getAllAttributes(Data clazz) {
		clazz.allSuperTypes.map[attributes].flatten
	}
	
	def Set<RosettaEnumeration> getAllSuperEnumerations(RosettaEnumeration e) {
		doGetSuperEnumerations(e, newLinkedHashSet)
	}
	
	 def List<Attribute>allNonOverridesAttributes(Data data) {
		val atts = newArrayList;
		atts.addAll(data.attributes)
		if (data.hasSuperType) {
			val attsWithSuper = data.superType.allNonOverridesAttributes
				.filter[superAttr| !atts.exists[extendedAttr|					
					superAttr.name == extendedAttr.name && 
					superAttr.typeCall.type == extendedAttr.typeCall.type && 
					superAttr.card.inf == extendedAttr.card.inf &&
					superAttr.card.sup == extendedAttr.card.sup
				]].toList
			attsWithSuper.addAll(atts)
			return attsWithSuper
		}
		return atts
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
	
	def boolean hasMetaDataAnnotations(Annotated it) {
		metaAnnotations.exists[attribute?.name == "reference" || attribute?.name == "location" || attribute?.name == "scheme" || attribute?.name == "id"]
	}
	
	def boolean hasMetaFieldAnnotations(Annotated it) {
		metaAnnotations.exists[attribute?.name != "reference" && attribute?.name != "address"]
	}
	
	def boolean hasMetaDataAddress(Annotated it) {
		metaAnnotations.exists[attribute?.name == "address"]
	}
	
	def boolean hasIdAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "id"]
	}
	def boolean hasReferenceAnnotation(Annotated it) {
		metaAnnotations.exists[attribute?.name == "reference"]
	}
	def hasCalculationAnnotation(Annotated it) {
		allAnnotations.exists[annotation?.name == "calculation"]
	}
	
	def private allAnnotations(Annotated withAnnotations) {
		withAnnotations?.annotations?.filter[annotation.isResolved]
	}
	
	def String conditionName(Condition cond, Data data) {
		return cond.conditionName(data.name, data.conditions)
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
	
	/**
	 * Get all reporting rules for blueprint report
	 */
	def getAllReportingRules(RosettaBlueprintReport report, boolean allLeafNodes) {
		getAllReportingRules(report.reportType, Optional.ofNullable(report.ruleSource), allLeafNodes)
	}
	
	def getAllReportingRules(Data type, Optional<RosettaExternalRuleSource> ruleSource, boolean onlyLeafNodes) {
		val rules = newHashMap
		val path = RosettaPath.valueOf(type.name)
		type.collectReportingRules(path, ruleSource, rules, newHashSet, onlyLeafNodes)
		rules
	}
	
	/**
	 * Recursively collects all reporting rules for all attributes
	 */
	private def void collectReportingRules(Data dataType, RosettaPath path, Optional<RosettaExternalRuleSource> ruleSource, Map<PathAttribute, RosettaBlueprint> visitor, Set<Data> collectedTypes, boolean onlyLeafNodes) {
		val attrRules = externalAnn.getAllRuleReferencesForType(ruleSource, dataType)
		
		dataType.allNonOverridesAttributes.forEach[attr |
			val attrType = attr.typeCall.type
			val attrEx = attr.toExpandedAttribute
			val rule = attrRules.get(attr)
			
			if (attrEx.builtInType || attrEx.isEnum) {
				if (rule !== null) {
					visitor.put(new PathAttribute(path, attr), rule.reportingRule)
				}
			} 
			else if (attrType instanceof Data) {
				// TODO - get rid of repeatable rules
					// if allLeafNodes is false - for repeatable rules only collect rules from nested type 
					// if no rule exists at the top level, e.g., nested reporting rules are not supported 
					// (except for repeatable rules where only the top level rule should be collected)
				if (rule !== null && (!attrEx.isMultiple || !onlyLeafNodes)) {
					visitor.put(new PathAttribute(path, attr), rule.reportingRule)
				}
				if (collectedTypes.add(attrType)) {
					val subPath = attrEx.isMultiple ?
						path.newSubPath(attr.name, 0) :
						path.newSubPath(attr.name)
					attrType.collectReportingRules(subPath, ruleSource, visitor, collectedTypes, onlyLeafNodes)
				}
			} 
			else {
				throw new IllegalArgumentException("Did not collect reporting rules from type " + attrType)
			}
		]	
	}
	
	@org.eclipse.xtend.lib.annotations.Data static class PathAttribute {
		RosettaPath path
		Attribute attr
	}
}
