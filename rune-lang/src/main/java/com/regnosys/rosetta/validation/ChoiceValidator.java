package com.regnosys.rosetta.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.regnosys.rosetta.types.*;
import jakarta.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;

public class ChoiceValidator extends AbstractDeclarativeRosettaValidator {
    @Inject
    private RObjectFactory rObjectFactory;
    @Inject
    private RBuiltinTypeService builtins;
    @Inject
    private CycleValidationHelper cycleValidationHelper;

    @Check
    public void checkCyclicOptions(Choice choice) {
        cycleValidationHelper.detectMultipleCycles(
                choice,
                c -> Iterables.filter(c.getOptions(), opt -> opt.getTypeCall().getType() instanceof Choice),
                opt -> (Choice) opt.getTypeCall().getType(),
                "includes",
                (opt, pathMsg) -> error("Cyclic option: " + pathMsg, opt, ROSETTA_NAMED__NAME)
        );
    }

    @Check
    public void checkChoiceOptionsDoNotOverlap(Choice choice) {
        RChoiceType t = rObjectFactory.buildRChoiceType(choice);
        Map<RMetaAnnotatedType, RChoiceOption> includedOptions = new HashMap<>();
        for (RChoiceOption opt : t.getOwnOptions()) {
            if (opt.getType().getRType() instanceof RChoiceType) {
                ((RChoiceType) opt.getType().getRType()).getAllOptions().forEach(o -> includedOptions.put(o.getType(), opt));
            }
        }
        for (RChoiceOption opt : t.getOwnOptions()) {
            if (builtins.NOTHING.equals(opt.getType().getRType())) {
                continue;
            }
            RChoiceOption alreadyIncluded = includedOptions.put(opt.getType(), opt);
            if (alreadyIncluded != null) {
                String msg;
                if (alreadyIncluded.getType().equals(opt.getType())) {
                    msg = "Duplicate option '" + opt.getType() + "'";
                } else {
                    msg = "Option '" + opt.getType() + "' is already included by option '" + alreadyIncluded.getType() + "'";
                }
                error(msg, opt.getEObject(), null);
            }

            RChoiceOption typeRelation = isInTypeHierarchy(opt, includedOptions);
            if (typeRelation != null) {
                error("Option '%s' is in the same type hierarchy as '%s'".formatted(opt.getType(), typeRelation.getType()), opt.getEObject(), null);
            }
        }
    }

    private RChoiceOption isInTypeHierarchy(RChoiceOption opt, Map<RMetaAnnotatedType, RChoiceOption> includedOptions) {
        if (!(opt.getType().getRType() instanceof RDataType typeToCheck)) {
            return null;
        }

        List<RChoiceOption> includedChoiceDataOptions = includedOptions.values().stream()
                .filter(o -> !o.equals(opt))
                .filter(o -> o.getType().getRType() instanceof RDataType)
                .toList();

        for (RChoiceOption includedChoiceDataOption : includedChoiceDataOptions) {
            if (typeToCheck.getAllSuperTypes().contains((RDataType) includedChoiceDataOption.getType().getRType())) {
                return includedChoiceDataOption;
            }

            if (((RDataType) includedChoiceDataOption.getType().getRType()).getAllSuperTypes().contains(typeToCheck)) {
                return includedChoiceDataOption;
            }
        }
        return null;
    }
}
