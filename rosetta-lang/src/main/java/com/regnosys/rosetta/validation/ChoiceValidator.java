package com.regnosys.rosetta.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.types.RChoiceOption;
import com.regnosys.rosetta.types.RChoiceType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.types.TypeSystem;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;

public class ChoiceValidator  extends AbstractDeclarativeRosettaValidator {
	@Inject
	private RObjectFactory rObjectFactory;
	@Inject
	private TypeSystem typeSystem;
	
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
		for (int i=0; i<t.getOwnOptions().size(); i++) {
			RChoiceOption toCheckForOverlap = t.getOwnOptions().get(i);
			for (int j=0; j<t.getOwnOptions().size(); j++) {
				if (i != j) {
					RType other = t.getOwnOptions().get(j).getType();
					if (typeSystem.isSubtypeOf(toCheckForOverlap.getType(), other, false)) {
						String msg;
						if (toCheckForOverlap.getType().equals(other)) {
							msg = "Duplicate option '" + other + "'";
						} else {
							msg = "Option is already included by option '" + other + "'";
						}
						error(msg, toCheckForOverlap.getEObject(), ROSETTA_NAMED__NAME);
					}
				}
			}
		}
	}
}
