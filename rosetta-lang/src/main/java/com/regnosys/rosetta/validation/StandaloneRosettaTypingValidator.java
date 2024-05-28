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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import com.regnosys.rosetta.rosetta.ExternalValueOperator;
import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.RosettaExternalClass;
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute;
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rosetta.RosettaRule;
import com.regnosys.rosetta.rosetta.expression.ChoiceOperation;
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyElement;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.rosetta.simple.RosettaRuleReference;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RListType;
import com.regnosys.rosetta.types.TypeFactory;
import com.regnosys.rosetta.types.TypeSystem;
import com.regnosys.rosetta.types.TypeValidationUtil;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;
import com.regnosys.rosetta.typing.validation.RosettaTypingCheckingValidator;
import com.regnosys.rosetta.utils.ExternalAnnotationUtil;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;

public class StandaloneRosettaTypingValidator extends RosettaTypingCheckingValidator {
	@Inject
	private TypeSystem ts;
	
	@Inject
	private TypeFactory tf;
	
	@Inject
	private TypeValidationUtil tu;
	
	@Inject
	private RBuiltinTypeService builtins;
	
	@Inject
	private ExternalAnnotationUtil annotationUtil;
	
	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>();
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/Rosetta"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaSimple"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaExpression"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaTranslate"));
		return result;
	}
	
	@Override
	public void register(EValidatorRegistrar registrar) {
	}
	
	/**
	 * Xsemantics does not allow raising warnings. See https://github.com/eclipse/xsemantics/issues/149.
	 */
	@Check
	public void checkOnlyElement(RosettaOnlyElement e) {
		RListType t = ts.inferType(e.getArgument());
		if (t != null) {
			RosettaCardinality minimalConstraint = tf.createConstraint(1, 2);
			if (!minimalConstraint.isSubconstraintOf(t.getConstraint())) {
				warning(tu.notLooserConstraintMessage(minimalConstraint, t), e, ROSETTA_UNARY_OPERATION__ARGUMENT);
			}
		}
	}
	
	/**
	 * Xsemantics does not allow raising errors on a specific index of a multi-valued feature.
	 * See https://github.com/eclipse/xsemantics/issues/64.
	 */
	@Check
	public void checkChoiceOperationHasNoDuplicateAttributes(ChoiceOperation e) {
		for (var i = 1; i < e.getAttributes().size(); i++) {
			Attribute attr = e.getAttributes().get(i);
			for (var j = 0; j < i; j++) {
				if (attr.equals(e.getAttributes().get(j))) {
					error("Duplicate attribute.", e, CHOICE_OPERATION__ATTRIBUTES, i);
				}
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
			RType ruleInputType = ts.typeCallToRType(eligibilityRule.getInput());
			if (!ts.isSubtypeOf(ruleInputType, inputType)) {
				error("Eligibility rule " + eligibilityRule.getName() + " expects a `" + ruleInputType + "` as input, but this report is generated from a `" + inputType + "`.", report, ROSETTA_REPORT__ELIGIBILITY_RULES, i);
			}
		}
		
		RType reportTypeInputType = ts.getRulesInputType(report.getReportType(), Optional.ofNullable(report.getRuleSource()));
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
		RType current;
		if (data.getSuperType() != null) {
			current = ts.getRulesInputType(data.getSuperType(), Optional.empty());
			if (current.equals(builtins.NOTHING)) {
				return;
			}
		} else {
			current = builtins.ANY;
		}
		for (Attribute attr: data.getAttributes()) {
			RosettaRuleReference ref = attr.getRuleReference();
			if (ref != null) {
				RosettaRule rule = ref.getReportingRule();
				RType inputType = ts.typeCallToRType(rule.getInput());
				RType newCurrent = ts.meet(current, inputType);
				if (newCurrent.equals(builtins.NOTHING)) {
					error("Rule `" + rule.getName() + "` expects an input of type `" + inputType + "`, while previous rules expect an input of type `" + current + "`.", ref, ROSETTA_RULE_REFERENCE__REPORTING_RULE);
				} else {
					current = newCurrent;
				}
			} else {
				RType attrType = ts.stripFromTypeAliases(ts.typeCallToRType(attr.getTypeCall()));
				if (attrType instanceof RDataType) {
					Data attrData = ((RDataType)attrType).getData();
					RType inputType = ts.getRulesInputType(attrData, Optional.empty());
					if (!inputType.equals(builtins.NOTHING)) {
						RType newCurrent = ts.meet(current, inputType);
						if (newCurrent.equals(builtins.NOTHING)) {
							error("Attribute `" + attr.getName() + "` contains rules that expect an input of type `" + inputType + "`, while previous rules expect an input of type `" + current + "`.", attr, null);
						} else {
							current = newCurrent;
						}
					}
				}
			}
		}
	}
	
	@Check
	public void checkExternalRuleSource(RosettaExternalRuleSource source) {
		for (RosettaExternalClass externalClass: source.getExternalClasses()) {
			Data data = externalClass.getData();
			Map<RosettaFeature, RosettaRuleReference> ruleReferences = annotationUtil.getAllRuleReferencesForType(Optional.of(source), data);
			
			RType current = builtins.ANY;
			for (Attribute attr: data.getAttributes()) {
				Optional<RosettaExternalRegularAttribute> maybeExtAttr = externalClass.getRegularAttributes().stream()
						.filter(ext -> ext.getOperator() == ExternalValueOperator.PLUS)
						.filter(ext -> ext.getAttributeRef().equals(attr))
						.findAny();
				RosettaRuleReference ref = ruleReferences.get(attr);
				if (ref != null) {
					RosettaRule rule = ref.getReportingRule();
					RType inputType = ts.typeCallToRType(rule.getInput());
					RType newCurrent = ts.meet(current, inputType);
					if (newCurrent.equals(builtins.NOTHING)) {
						if (maybeExtAttr.isPresent()) {
							RosettaExternalRegularAttribute extAttr = maybeExtAttr.get();
							error("Attribute `" + attr.getName() + "` has a rule that expects an input of type `" + inputType + "`, while other rules expect an input of type `" + current + "`.", extAttr, ROSETTA_EXTERNAL_REGULAR_ATTRIBUTE__ATTRIBUTE_REF);
						}
					} else {
						current = newCurrent;
					}
				} else {
					RType attrType = ts.stripFromTypeAliases(ts.typeCallToRType(attr.getTypeCall()));
					if (attrType instanceof RDataType) {
						Data attrData = ((RDataType)attrType).getData();
						RType inputType = ts.getRulesInputType(attrData, Optional.of(source));
						if (!inputType.equals(builtins.NOTHING)) {
							RType newCurrent = ts.meet(current, inputType);
							if (newCurrent.equals(builtins.NOTHING)) {
								if (maybeExtAttr.isPresent()) {
									RosettaExternalRegularAttribute extAttr = maybeExtAttr.get();
									error("Attribute `" + attr.getName() + "` contains rules that expect an input of type `" + inputType + "`, while other rules expect an input of type `" + current + "`.", extAttr, ROSETTA_EXTERNAL_REGULAR_ATTRIBUTE__ATTRIBUTE_REF);
								} else {
									error("Attribute `" + attr.getName() + "` contains rules that expect an input of type `" + inputType + "`, while other rules expect an input of type `" + current + "`.", externalClass, ROSETTA_EXTERNAL_CLASS__DATA);
								}
							} else {
								current = newCurrent;
							}
						}
					}
				}
			}
		}
	}
}
