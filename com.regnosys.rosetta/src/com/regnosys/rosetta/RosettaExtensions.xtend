package com.regnosys.rosetta

import com.google.common.base.CaseFormat
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaSynonym
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import java.util.Collection
import java.util.List
import java.util.Set
import org.eclipse.emf.common.util.URI

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaConditionalExpression

class RosettaExtensions {
	
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
			atts.addAll(data.superType.allNonOverridesAttributes
				.filter[superAttr| !atts.exists[extendedAttr|					
					superAttr.name == extendedAttr.name && 
					superAttr.type == extendedAttr.type && 
					superAttr.card.inf == extendedAttr.card.inf &&
					superAttr.card.sup == extendedAttr.card.sup
				]].toList)
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
	
	def boolean isChoiceRuleCondition(Condition cond) {
		return cond.constraint !== null
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
		withAnnotations?.annotations?.filter[annotation !== null && !annotation.eIsProxy]
	}
	
	def String conditionName(Condition cond, Data data) {
		return cond.conditionName(data.name, data.conditions)
	}

	def String conditionName(Condition cond, Function func) {
		return cond.conditionName(func.name, func.conditions)
	}
	
	
	//Name convention: <type name>(<condition name>|<condition type><#>) where condition type should be 'choice' or 'oneof'.
	private def String conditionName(Condition cond, String containerName, Collection<Condition> conditions) {
		val name = if (!cond.name.nullOrEmpty)
				cond.name
			else {
				val idx = conditions.filter[name.nullOrEmpty].toList.indexOf(cond)
				val type = if (cond.constraint !== null) {
						if(cond.constraint.oneOf) 'OneOf' else 'Choice'
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
	def getAllReportingRules(RosettaBlueprintReport report) {
		val rules = newHashSet
		report.reportType.collectReportingRules([rules.add(it)], newHashSet)
		return rules
	}
	
	/**
	 * Recursively collects all reporting rules for all attributes
	 */
	def void collectReportingRules(Data dataType, (RosettaBlueprint) => void visitor, Set<Data> collectedTypes) {
		dataType.allNonOverridesAttributes.forEach[attr|
			val attrType = attr.type
			val attrEx = attr.toExpandedAttribute
			if (attrEx.builtInType || attrEx.enum) {
				val rule = attr.ruleReference?.reportingRule
				if (rule !== null) {
					visitor.apply(rule)
				}
			} else if (attrType instanceof Data) {
				if (!collectedTypes.contains(attrType)) {
					collectedTypes.add(attrType)
					val attrRule = attr.ruleReference?.reportingRule
					// only collect rules from nested type if no rule exists at the top level
					// e.g. nested reporting rules are not supported (except for repeatable rules where only the top level rule should be collected) 
					if (attrRule === null)
						attrType.collectReportingRules(visitor, collectedTypes)
					else 
						visitor.apply(attrRule)
				}
			} else {
				throw new IllegalArgumentException("Did not collect reporting rules from type " + attrType)
			}
		]	
	}
}
