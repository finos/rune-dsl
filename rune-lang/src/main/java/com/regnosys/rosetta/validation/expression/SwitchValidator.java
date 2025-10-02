package com.regnosys.rosetta.validation.expression;

import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*;

import java.util.*;
import java.util.stream.Collectors;

import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.types.*;
import jakarta.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.interpreter.RosettaInterpreter;
import com.regnosys.rosetta.interpreter.RosettaValue;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.expression.RosettaLiteral;
import com.regnosys.rosetta.rosetta.expression.SwitchCaseOrDefault;
import com.regnosys.rosetta.rosetta.expression.SwitchOperation;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.services.RosettaGrammarAccess;
import com.regnosys.rosetta.types.builtin.RBasicType;

public class SwitchValidator extends ExpressionValidator {
	@Inject
	private RosettaInterpreter interpreter;
	@Inject
	private RosettaGrammarAccess grammar;
    @Inject
    private RObjectFactory rObjectFactory;
	
	@Check
	public void checkSwitch(SwitchOperation op) {
		isSingleCheck(op.getArgument(), op, ROSETTA_UNARY_OPERATION__ARGUMENT, op);
		
		// Check `default` is the last case
		for (int i=0; i<op.getCases().size()-1; i++) {
			SwitchCaseOrDefault caseStatement = op.getCases().get(i);
			if (caseStatement.isDefault()) {
				errorKeyword("A default case is only allowed at the end", caseStatement, grammar.getSwitchCaseOrDefaultAccess().getDefaultKeyword_0_0());
			}
		}
		
		RMetaAnnotatedType argumentType = typeProvider.getRMetaAnnotatedType(op.getArgument());
		RType rType = typeSystem.stripFromTypeAliases(argumentType.getRType());
		if (rType.equals(builtins.NOTHING)) {
			// If there is an error within the argument, do not check further
			return;
		} else if (rType instanceof REnumType) {
			checkEnumSwitch((REnumType) rType, op);
		} else if (rType instanceof RBasicType) {
			checkBasicTypeSwitch((RBasicType) rType, op);
		} else if (rType instanceof RChoiceType) {
			checkChoiceSwitch((RChoiceType) rType, op);
		} else if (rType instanceof RDataType dt) {
            checkDataSwitch(dt, op);
        } else {
			unsupportedTypeError(argumentType, op.getOperator(), op, ROSETTA_UNARY_OPERATION__ARGUMENT, "Supported argument types are basic types, enumerations, complex types, and choice types");
		}
	}
	private void checkEnumSwitch(REnumType argumentType, SwitchOperation op) {
		// When the argument is an enum:
		// - all guards should be enum guards,
		// - there are no duplicate cases,
		// - all enum values must be covered.
		Set<RosettaEnumValue> seenValues = new HashSet<>();
		for (SwitchCaseOrDefault caseStatement : op.getCases()) {
			if (caseStatement.isDefault()) {
				continue;
			}
			RosettaEnumValue guard = caseStatement.getGuard().getEnumGuard();
 			if (guard == null) {
 				error("Case should match an enum value of " + argumentType, caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
 			} else {
 				if (!seenValues.add(guard)) {
 					error("Duplicate case " + guard.getName(), caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
 				}
 			}
 		}

		if (op.getDefault() == null) {
			List<RosettaEnumValue> missingEnumValues = new ArrayList<>(argumentType.getAllEnumValues());
			missingEnumValues.removeAll(seenValues);
			if (!missingEnumValues.isEmpty()) {
				String missingValuesMsg = missingEnumValues.stream().map(v -> v.getName()).collect(Collectors.joining(", "));
				error("Missing the following cases: " + missingValuesMsg + ". Either provide all or add a default.", op, ROSETTA_OPERATION__OPERATOR);
			}
		}
	}
	private void checkBasicTypeSwitch(RBasicType argumentType, SwitchOperation op) {
		// When the argument is a basic type:
		// - all guards should be literal guards,
		// - there are no duplicate cases,
		// - all guards should be comparable to the input.
		Set<RosettaValue> seenValues = new HashSet<>();
		RMetaAnnotatedType argumentTypeWithoutMeta = RMetaAnnotatedType.withNoMeta(argumentType);
 		for (SwitchCaseOrDefault caseStatement : op.getCases()) {
 			if (caseStatement.isDefault()) {
				continue;
			}
 			RosettaLiteral guard = caseStatement.getGuard().getLiteralGuard();
 			if (guard == null) {
 				error("Case should match a literal of type " + argumentType, caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
 			} else {
 				if (!seenValues.add(interpreter.interpret(guard))) {
 					error("Duplicate case", caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
 				}
 				RMetaAnnotatedType conditionType = typeProvider.getRMetaAnnotatedType(guard);
	 			if (!typeSystem.isComparable(conditionType, argumentTypeWithoutMeta)) {
 					error("Invalid case: " + notComparableMessage(conditionType, argumentTypeWithoutMeta), caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
 				}
 			}
 		}
	}
	private void checkChoiceSwitch(RChoiceType argumentType, SwitchOperation op) {
		// When the argument is a choice type:
		// - all guards should be choice option guards,
		// - all cases should be reachable,
		// - all choice options should be covered.
		Map<ChoiceOption, RMetaAnnotatedType> includedOptions = new HashMap<>();
		for (SwitchCaseOrDefault caseStatement : op.getCases()) {
			if (caseStatement.isDefault()) {
				continue;
			}
			ChoiceOption guard = caseStatement.getGuard().getChoiceOptionGuard();
 			if (guard == null) {
 				error("Case should match a choice option of type " + argumentType, caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
 			} else {
 				RMetaAnnotatedType alreadyCovered = includedOptions.get(guard);
 				if (alreadyCovered != null) {
 					error("Case already covered by " + alreadyCovered, caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
 				} else {
 					RMetaAnnotatedType guardType = typeProvider.getRTypeOfSymbol(guard);
 					includedOptions.put(guard, guardType);
 					RType valueType = guardType.getRType();
 					if (valueType instanceof RChoiceType) {
 						((RChoiceType)valueType).getAllOptions().forEach(it -> includedOptions.put(it.getEObject(), guardType));
 					}
 				}
 			}
 		}
		if (op.getDefault() == null) {
 			List<RMetaAnnotatedType> missingOptions = new ArrayList<>();
 			argumentType.getOwnOptions().forEach(opt -> missingOptions.add(opt.getType()));
 			for (RMetaAnnotatedType guard : new LinkedHashSet<>(includedOptions.values())) {
 				for (var i=0; i<missingOptions.size(); i++) {
 					RMetaAnnotatedType opt = missingOptions.get(i);
 					RType optValueType = opt.getRType();
 					if (typeSystem.isSubtypeOf(opt, guard, false)) {
 						missingOptions.remove(i);
 						i--;
 					} else if (optValueType instanceof RChoiceType) {
 						if (typeSystem.isSubtypeOf(guard, opt, false)) {
 							missingOptions.remove(i);
 							i--;
 							((RChoiceType)optValueType).getOwnOptions()
 								.forEach(o -> missingOptions.add(o.getType()));
 						}
 					}
 				}
 			}
			if (!missingOptions.isEmpty()) {
				String missingOptsMsg = missingOptions.stream()
						.map(opt -> opt.toString())
						.collect(Collectors.joining(", "));
				error("Missing the following cases: " + missingOptsMsg + ". Either provide all or add a default.", op, ROSETTA_OPERATION__OPERATOR);
			}
		}
	}
    private void checkDataSwitch(RDataType argumentType, SwitchOperation op) {
        // When the argument is a data type:
        // - all guards should extend the argument type,
        // - all cases should be reachable,
        // - there must be a default case.
        Set<RDataType> seenDataTypes = new LinkedHashSet<>();
        for (SwitchCaseOrDefault caseStatement : op.getCases()) {
            if (caseStatement.isDefault()) {
                continue;
            }
            Data guard = caseStatement.getGuard().getDataGuard();
            if (guard == null) {
                error("Case should be a subtype of type " + argumentType, caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
            } else {
                RDataType dataGuard = rObjectFactory.buildRDataType(guard);
                if (!typeSystem.isSubtypeOf(dataGuard, argumentType)) {
                    error("Case should be a subtype of type " + argumentType, caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
                }
                RDataType alreadyCovered = null;
                if (seenDataTypes.contains(dataGuard)) {
                    alreadyCovered = dataGuard;
                } else {
                    Optional<RDataType> firstAlreadyCovered = seenDataTypes.stream()
                            .filter(seenDataType -> typeSystem.isSubtypeOf(dataGuard, seenDataType))
                            .findFirst();
                    if (firstAlreadyCovered.isPresent()) {
                        alreadyCovered = firstAlreadyCovered.get();
                    }
                }
                if (alreadyCovered != null) {
                    error("Case already covered by " + alreadyCovered, caseStatement, SWITCH_CASE_OR_DEFAULT__GUARD);
                } else {
                    seenDataTypes.add(dataGuard);
                }
            }
        }
        if (op.getDefault() == null) {
            error("A switch on a complex type must have a default case", op, ROSETTA_OPERATION__OPERATOR);
        }
    }
}
