/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.*;
import com.regnosys.rosetta.rosetta.simple.*;
import com.regnosys.rosetta.rules.RulePathMap;
import com.regnosys.rosetta.rules.RuleReferenceService;
import com.regnosys.rosetta.rules.RuleReferenceService.RuleReferenceContext;
import com.regnosys.rosetta.rules.RuleResult;
import com.regnosys.rosetta.types.*;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.utils.AnnotationPathExpressionUtil;
import jakarta.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;

public class ReportValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private RosettaEcoreUtil ecoreUtil;
    
    @Inject
    private TypeSystem ts;

    @Inject
    private RBuiltinTypeService builtins;

    @Inject
    private RuleReferenceService ruleService;

    @Inject
    private RObjectFactory objectFactory;

    @Inject
    private AnnotationPathExpressionUtil annotationPathUtil;

    @Check
    public void checkRuleReferenceAnnotation(RuleReferenceAnnotation ann) {
        AnnotationPathExpression path = ann.getPath();
        if (path != null) {
            // Disallow deep path operator
            EcoreUtil2.eAllOfType(path, AnnotationDeepPath.class).forEach(deepPath -> {
                error("Deep paths are not allowed for `ruleReference` annotations", deepPath, ANNOTATION_DEEP_PATH__OPERATOR);
            });

            // Disallow paths on multi-cardinality attributes
            EObject container = ann.eContainer();
            if (container instanceof Attribute) {
                Attribute containingAttribute = (Attribute) container;
                if (containingAttribute.getCard().isPlural()) {
                    error("Paths on multi-cardinality attributes are not allowed", ann, BUILTIN_ANNOTATION_WITH_PATH__PATH);
                } else {
                    EcoreUtil2.eAllOfType(path, AnnotationPath.class).forEach(p -> {
                        Attribute target = annotationPathUtil.getTargetAttribute(p.getReceiver());
                        if (ecoreUtil.isResolved(target) && target.getCard().isPlural()) {
                            error("Paths on multi-cardinality attributes are not allowed", p, ANNOTATION_PATH__OPERATOR);
                        }
                    });
                }
            }

            // Deprecate rules with a label if a path is used
            RosettaRule rule = ann.getReportingRule();
            if (rule != null && rule.getIdentifier() != null) {
                warning("Specifying a label in a reporting rule is deprecated. Add a `label` annotation instead", ann, RULE_REFERENCE_ANNOTATION__REPORTING_RULE);
            }
        }
    }

    @Check
    public void checkReport(RosettaReport report) {
        RType inputType = ts.typeCallToRType(report.getInputType());
        List<RosettaRule> eligibilityRules = report.getEligibilityRules();
        for (var i = 0; i < eligibilityRules.size(); i++) {
            RosettaRule eligibilityRule = eligibilityRules.get(i);
            if (!eligibilityRule.isEligibility()) {
                error("Rule " + eligibilityRule.getName() + " is not an eligibility rule.", report, ROSETTA_REPORT__ELIGIBILITY_RULES, i);
            }
            RType ruleInputType = ts.getRuleInputType(eligibilityRule);
            if (!ts.isSubtypeOf(ruleInputType, inputType)) {
                error("Eligibility rule " + eligibilityRule.getName() + " expects a `" + ruleInputType + "` as input, but this report is generated from a `" + inputType + "`.", report, ROSETTA_REPORT__ELIGIBILITY_RULES, i);
            }
        }

        RType reportTypeInputType = ts.getRulesInputType(objectFactory.buildRDataType(report.getReportType()), report.getRuleSource());
        if (reportTypeInputType != builtins.ANY) {
            if (!ts.isSubtypeOf(reportTypeInputType, inputType)) {
                if (report.getRuleSource() != null) {
                    error("Rule source " + report.getRuleSource().getName() + " expects a `" + reportTypeInputType + "` as input, but this report is generated from a `" + inputType + "`.", report, ROSETTA_REPORT__RULE_SOURCE);
                } else {
                    error("Report type " + report.getReportType().getName() + " expects a `" + reportTypeInputType + "` as input, but this report is generated from a `" + inputType + "`.", report, ROSETTA_REPORT__REPORT_TYPE);
                }
            }
        }
    }

    @Check
    public void checkReportType(Data data) {
        RDataType rData = objectFactory.buildRDataType(data);
        checkReportInputType(data, null, rData);
    }

    @Check
    public void checkExternalRuleSource(RosettaExternalRuleSource source) {
        for (RosettaExternalClass externalClass : source.getExternalClasses()) {
            RDataType data = objectFactory.buildRDataType(externalClass.getData());
            checkReportInputType(externalClass, source, data);

            // TODO: somehow generalize this with the one for inline attributes
            externalClass.getRegularAttributes().forEach(annotatedAttr -> {
                RAttribute attribute = data.getAttributeByName(annotatedAttr.getAttributeRef().getName());
                if (attribute != null) {
                    checkOwnRuleReferenceAnnotations(source, data, attribute);
                }
            });
        }
    }

    @Check
    public void checkAttribute(Attribute attr) {
        RAttribute attribute = objectFactory.buildRAttribute(attr);
        if (!attribute.getOwnRuleReferences().isEmpty()) {
            EObject container = attr.eContainer();
            if (!(container instanceof Data)) {
                for (int i = 0; i < attribute.getOwnRuleReferences().size(); i++) {
                    error("You can only add rule references on the attribute of a type", attr, ATTRIBUTE__RULE_REFERENCES, i);
                }
            } else {
                checkOwnRuleReferenceAnnotations(null, attribute.getEnclosingType(), attribute);
            }
        }
    }

    private void checkOwnRuleReferenceAnnotations(RosettaExternalRuleSource source, RDataType type, RAttribute attribute) {
        RulePathMap ruleMap = ruleService.computeRulePathMapInContext(source, type, attribute);
        Map<List<String>, RuleResult> parentRules = ruleMap.getParentRules();
        Map<List<String>, RuleResult> ownRules = ruleMap.getOwnRules();
        ownRules.forEach((path, ruleResult) -> {
            if (ruleResult.isExplicitlyEmpty()) {
                RAttribute targetAttribute = ruleService.getTargetAttribute(attribute, path);
                RulePathMap targetRuleMap = ruleService.computeRulePathMapInContext(source, targetAttribute.getEnclosingType(), targetAttribute);
                RuleResult ruleDefinedOnTarget = targetRuleMap.get(List.of());

                if (!parentRules.containsKey(path) && ruleDefinedOnTarget != null && ruleDefinedOnTarget.isExplicitlyEmpty()) {
                    EObject errorOrigin = ruleResult.getOrigin();
                    String msg = "There is no rule reference" + toPathMessage(path) + " to remove";
                    if (errorOrigin instanceof RuleReferenceAnnotation) {
                        error(msg, errorOrigin, RULE_REFERENCE_ANNOTATION__EMPTY);
                    } else {
                        error(msg, errorOrigin, null);
                    }
                }
            } else {
                RosettaRule rule = ruleResult.getRule();
                RAttribute target = ruleService.getTargetAttribute(attribute, path);
                if (target != null) {
                    RFunction ruleFunc = objectFactory.buildRFunction(rule);

                    // check type
                    RMetaAnnotatedType ruleType = ruleFunc.getOutput().getRMetaAnnotatedType();
                    if (!ts.isSubtypeOf(ruleType, target.getRMetaAnnotatedType())) {
                        error("Expected type " + target.getRMetaAnnotatedType() + toPathMessage(path) + ", but rule has type " + ruleType, ruleResult.getOrigin(), RULE_REFERENCE_ANNOTATION__EMPTY);
                    }

                    // check cardinality
                    if (!target.isMulti() && ruleFunc.getOutput().isMulti()) {
                        // TODO: make an error
                        warning("Expected single cardinality" + toPathMessage(path) + ", but rule has multi cardinality", ruleResult.getOrigin(), RULE_REFERENCE_ANNOTATION__EMPTY);
                    }
                }
            }
        });
    }

    private void checkReportInputType(EObject objectBeingChecked, RosettaExternalRuleSource source, RDataType type) {
        ruleService.<RType>traverse(
                source,
                type,
                builtins.ANY,
                (current, context) -> {
                    if (context.isExplicitlyEmpty()) {
                        return current;
                    }
                    RType ruleInputType = ts.getRuleInputType(context.getRule());
                    if (ruleInputType.equals(builtins.NOTHING)) {
                        // There is already an existing error to do with the rule itself
                        return current;
                    }
                    RType newCurrent = ts.meet(current, ruleInputType);
                    if (newCurrent.equals(builtins.NOTHING)) {
                        // The rule is not compatible with previous types - raise an error
                        inputTypeErrorForRule(objectBeingChecked, context, current, ruleInputType);
                        return current;
                    }
                    return newCurrent;
                }
        );
    }

    private void inputTypeErrorForRule(EObject objectBeingChecked, RuleReferenceContext context, RType previousInputType, RType inputType) {
        EObject origin = context.getRuleOrigin();
        EObject container = EcoreUtil2.getContainerOfType(origin, objectBeingChecked.getClass());
        RosettaRule rule = context.getRule();
        if (objectBeingChecked.equals(container)) {
            // If the cause of the error is contained in the object being checked, we can raise a specific error.
            if (origin instanceof RuleReferenceAnnotation) {
                // Because of a rule reference annotation
                RuleReferenceAnnotation ann = (RuleReferenceAnnotation) origin;
                error("Rule `" + rule.getName() + "` expects an input of type `" + inputType + "`, while previous rules expect an input of type `" + previousInputType + "`", ann, RULE_REFERENCE_ANNOTATION__REPORTING_RULE);
            } else if (origin instanceof RosettaExternalRegularAttribute) {
                // A minus in a rule source should never cause an error, because it does not specify new rules
            }
        } else {
            // Without knowing exactly which (nested/inherited) rule reference is responsible for the error, just put the error on the type of the attribute
            // with a message pointing to which rule we were processing.
            RAttribute containingAttribute = context.getPath().get(0);
            String pathMsg = getPathMessage(context.getPath());
            error("Rule `" + rule.getName() + "`" + pathMsg + " expects an input of type `" + inputType + "`, while previous rules expect an input of type `" + previousInputType + "`", containingAttribute.getEObject(), ROSETTA_TYPED__TYPE_CALL);
        }
    }

    private String getPathMessage(List<RAttribute> path) {
        if (path.isEmpty()) {
            return "";
        }
        return " for " + path.stream().map(a -> a.getName()).collect(Collectors.joining(" -> "));
    }

    private String toPathMessage(List<String> path) {
        if (path.isEmpty()) {
            return "";
        }
        return " for " + String.join(" -> ", path);
    }
}
