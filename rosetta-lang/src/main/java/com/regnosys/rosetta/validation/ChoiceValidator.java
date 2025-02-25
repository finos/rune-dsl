package com.regnosys.rosetta.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
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
	
	@Check
	public void checkCyclicOptions(Choice choice) {
		for (ChoiceOption opt : choice.getOptions()) {
			if (opt.getTypeCall().getType() instanceof Choice) {
				Choice choiceOpt = (Choice) opt.getTypeCall().getType();
				List<Choice> path = new ArrayList<>();
				path.add(choice);
				Set<Choice> visited = new HashSet<>();
				visited.add(choice);
				if (hasCyclicOption(choiceOpt, path, visited)) {
					String pathString = path.stream().map(e -> e.getName()).collect(Collectors.joining(" includes "));
					error("Cyclic option: " + pathString, opt, ROSETTA_NAMED__NAME);
				}
			}
		}
	}
	private boolean hasCyclicOption(Choice current, List<Choice> path, Set<Choice> visited) {
		path.add(current);
		if (visited.add(current)) {
			for (ChoiceOption opt : current.getOptions()) {
				if (opt.getTypeCall().getType() instanceof Choice) {
					Choice choiceOpt = (Choice) opt.getTypeCall().getType();
					if (hasCyclicOption(choiceOpt, path, visited)) {
						return true;
					}
				}
			}
		} else {
			if (path.get(0).equals(path.get(path.size() - 1))) {
				return true;
			}
		}
		path.remove(path.size() - 1);
		return false;
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
