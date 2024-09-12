package com.regnosys.rosetta.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.rosetta.RosettaEnumValue;
import com.regnosys.rosetta.rosetta.RosettaEnumeration;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RObjectFactory;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;

public class EnumValidator extends AbstractDeclarativeRosettaValidator {
	@Inject
	private RObjectFactory rObjectFactory;
	
	@Check
	public void checkCyclicExtensions(RosettaEnumeration enumeration) {
		for (int i=0; i<enumeration.getParentEnums().size(); i++) {
			RosettaEnumeration p = enumeration.getParentEnums().get(i);
			List<RosettaEnumeration> path = new ArrayList<>();
			path.add(enumeration);
			Set<RosettaEnumeration> visited = new HashSet<>();
			visited.add(enumeration);
			if (hasCyclicExtension(p, path, visited)) {
				String pathString = path.stream().map(e -> e.getName()).collect(Collectors.joining(" extends "));
				error("Cyclic extension: " + pathString, enumeration, ROSETTA_ENUMERATION__PARENT_ENUMS, i);
			}
		}
	}
	
	private boolean hasCyclicExtension(RosettaEnumeration current, List<RosettaEnumeration> path, Set<RosettaEnumeration> visited) {
		path.add(current);
		if (visited.add(current)) {
			for (RosettaEnumeration p : current.getParentEnums()) {
				if (hasCyclicExtension(p, path, visited)) {
					return true;
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
	public void checkEnumValuesAreUnique(RosettaEnumeration enumeration) {
		REnumType t = rObjectFactory.buildREnumType(enumeration);
		Set<String> usedNames = new HashSet<>();
		t.getParents().forEach(p -> p.getAllEnumValues().forEach(v -> usedNames.add(v.getName())));
		for (RosettaEnumValue value: t.getOwnEnumValues()) {
			if (!usedNames.add(value.getName())) {
				error("Duplicate enum value '" + value.getName() + "'", value, ROSETTA_NAMED__NAME);
			}
		}
	}
	
	@Check
	public void checkEnumExtensionsDoNotHaveOverlappingEnumValueNames(RosettaEnumeration enumeration) {
		REnumType t = rObjectFactory.buildREnumType(enumeration);
		Map<String, RosettaEnumValue> usedNames = new HashMap<>();
		for (int i=0; i<t.getParents().size(); i++) {
			REnumType p = t.getParents().get(i);
			for (RosettaEnumValue v : p.getAllEnumValues()) {
				RosettaEnumValue existing = usedNames.get(v.getName());
				if (existing == null) {
					usedNames.put(v.getName(), v);
				} else if (!existing.equals(v)) {
					error("The value '" + v.getName() + "' of enum " + p + " overlaps with the value '" + v.getName() + "' of enum " + existing.getEnumeration().getName(), enumeration, ROSETTA_ENUMERATION__PARENT_ENUMS, i);
				}
			}
		}
	}
}
