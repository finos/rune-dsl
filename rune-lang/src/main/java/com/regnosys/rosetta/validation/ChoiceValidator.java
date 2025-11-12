package com.regnosys.rosetta.validation;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Iterables;
import jakarta.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.types.RChoiceOption;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;

public class ChoiceValidator  extends AbstractDeclarativeRosettaValidator {
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
		for (RChoiceOption opt: t.getOwnOptions()) {
			if (opt.getType().getRType() instanceof RChoiceType) {
				((RChoiceType) opt.getType().getRType()).getAllOptions().forEach(o -> includedOptions.put(o.getType(), opt));
			}
		}
		for (RChoiceOption opt: t.getOwnOptions()) {
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
		}
	}
}
