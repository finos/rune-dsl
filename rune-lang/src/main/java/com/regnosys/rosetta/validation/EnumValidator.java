package com.regnosys.rosetta.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*;

public class EnumValidator extends AbstractDeclarativeRosettaValidator {
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	
	@Check
	public void checkEnumNameIsCapitalized(RosettaEnumeration enumeration) {
		if (Character.isLowerCase(enumeration.getName().charAt(0))) {
			warning("Enumeration name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE);
		}
	}
	
	@Check
	public void checkCyclicExtensions(RosettaEnumeration enumeration) {
		RosettaEnumeration p = enumeration.getParent();
		if (p != null) {
			List<RosettaEnumeration> path = new ArrayList<>();
			path.add(enumeration);
			Set<RosettaEnumeration> visited = new HashSet<>();
			visited.add(enumeration);
			if (hasCyclicExtension(p, path, visited)) {
				String pathString = path.stream().map(e -> e.getName()).collect(Collectors.joining(" extends "));
				error("Cyclic extension: " + pathString, enumeration, ROSETTA_ENUMERATION__PARENT);
			}
		}
	}
	private boolean hasCyclicExtension(RosettaEnumeration current, List<RosettaEnumeration> path, Set<RosettaEnumeration> visited) {
		path.add(current);
		if (visited.add(current) && current.getParent() != null) {
			if (hasCyclicExtension(current.getParent(), path, visited)) {
				return true;
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
	public void checkEnumValuesAreUnique(RosettaEnumeration enumeration) {
		Set<String> usedNames = new HashSet<>();
		if (enumeration.getParent() != null) {
			ecoreUtil.getAllEnumValues(enumeration.getParent()).forEach(v -> usedNames.add(v.getName()));
		}
		for (RosettaEnumValue value: enumeration.getEnumValues()) {
			if (!usedNames.add(value.getName())) {
				error("Duplicate enum value '" + value.getName() + "'", value, ROSETTA_NAMED__NAME);
			}
		}
	}
}
