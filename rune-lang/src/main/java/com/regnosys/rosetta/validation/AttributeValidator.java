package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.simple.Annotation;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rules.RulePathMap;
import com.regnosys.rosetta.rules.RuleReferenceService;
import com.regnosys.rosetta.rules.RuleResult;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RFunction;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.TypeSystem;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*;

import java.util.List;
import java.util.Map;

public class AttributeValidator extends AbstractDeclarativeRosettaValidator {
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	@Inject
	private TypeSystem typeSystem;
	@Inject
	private RuleReferenceService ruleService;
	@Inject
	private RBuiltinTypeService builtins;
	
	@Check
	public void checkAttributeNameStartsWithLowerCase(Attribute attribute) {
		if (!(attribute instanceof ChoiceOption)
				&& !(attribute.eContainer() instanceof Annotation)
				&& Character.isUpperCase(attribute.getName().charAt(0))) {
			warning("Attribute name should start with a lower case", attribute, ROSETTA_NAMED__NAME, INVALID_CASE);
		}
	}
	
	@Check
	public void checkAttribute(Attribute attr) {
		RAttribute attribute = rObjectFactory.buildRAttribute(attr);
		RType attrType = attribute.getRMetaAnnotatedType().getRType();

		if (attrType instanceof RChoiceType) {
			attrType = ((RChoiceType)attrType).asRDataType();
		}
		if (attrType instanceof RDataType attrDataType) {
            if (ecoreUtil.hasReferenceAnnotation(attr) 
					&& !(attrDataType.hasMetaAttribute("key") || attrDataType.getAllSuperTypes().stream().anyMatch(st -> st.hasMetaAttribute("key")))) {
				// TODO: make error instead
				warning(attrDataType.getName() + " must be annotated with [metadata key] as reference annotation is used", attr,
					ROSETTA_TYPED__TYPE_CALL);
			}
		}
	}
	
	@Check
	public void checkAttributeOverride(Attribute attr) {
		if (attr.isOverride()) {
			EObject container = attr.eContainer();
			if (!(container instanceof Data)) {
				error("You can only override the attribute of a type", attr, ATTRIBUTE__OVERRIDE);
			} else {
				RAttribute attribute = rObjectFactory.buildRAttribute(attr);
				RAttribute parentAttribute = attribute.getParentAttribute();
				if (parentAttribute != null) {
					// If parent is deprecated, mark name of attribute as deprecated
					checkDeprecatedAnnotation(parentAttribute.getEObject(), attr, ROSETTA_NAMED__NAME, INSIGNIFICANT_INDEX);
					// Check types
					RMetaAnnotatedType overriddenType = attribute.getRMetaAnnotatedType();
					RMetaAnnotatedType parentAttrType = parentAttribute.getRMetaAnnotatedType();
					if (!typeSystem.isSubtypeOf(overriddenType, parentAttrType) && !builtins.NOTHING.equals(parentAttrType.getRType())) {
						error("The overridden type should be a subtype of the parent type " + parentAttrType, attr, ROSETTA_TYPED__TYPE_CALL);
					}
					// Check cardinality
					if (!parentAttribute.getCardinality().includes(attribute.getCardinality())) {
						error("Cardinality may not be broader than the cardinality of the parent attribute " + parentAttribute.getCardinality(), attr, ATTRIBUTE__CARD);
					}
					// Check inherited rule references are compatible
					if (!overriddenType.equals(parentAttrType) || !attribute.getCardinality().equals(parentAttribute.getCardinality())) {
						RulePathMap ruleMap = ruleService.computeRulePathMap(attribute);
						Map<List<String>, RuleResult> inheritedRules = ruleMap.getInheritedRules();
						inheritedRules.forEach((path, ruleResult) -> {
							RosettaRule rule = ruleResult.getRule();
							if (rule != null) {
								RAttribute target = ruleService.getTargetAttribute(attribute, path);
								if (target != null) {
									RFunction ruleFunc = rObjectFactory.buildRFunction(rule);
									
									// check type
									RMetaAnnotatedType ruleType = ruleFunc.getOutput().getRMetaAnnotatedType();
									if (!typeSystem.isSubtypeOf(ruleType, target.getRMetaAnnotatedType())) {
										error("The overridden type is incompatible with the inherited rule reference `" + rule.getName() + "`" + toPathMessage(path) + ". Either change the type or override the rule reference", attr, ROSETTA_TYPED__TYPE_CALL);
									}
									
									// check cardinality
									if (!target.isMulti() && ruleFunc.getOutput().isMulti()) {
										error("Cardinality is incompatible with the inherited rule reference `" + rule.getName() + "`" + toPathMessage(path) + ". Either change the cardinality or override the rule reference", attr, ATTRIBUTE__CARD);
									}
								}
							}
						});
					}
				} else {
					error("Attribute " + attr.getName() + " does not exist in supertype", attr, ROSETTA_NAMED__NAME);
				}
			}
		}
	}
	private String toPathMessage(List<String> path) {
		if (path.isEmpty()) {
			return "";
		}
		return " for " + String.join(" -> ", path);
	}
	
	@Check
	public void checkCardinality(RosettaCardinality card) {
		if (!card.isUnbounded()) {
			if (card.getInf() > card.getSup()) {
				error("The upper bound must be greater than the lower bound", card, null);
			}
		}
	}
}
