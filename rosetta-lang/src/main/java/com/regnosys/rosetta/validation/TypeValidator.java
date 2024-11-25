package com.regnosys.rosetta.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.xtext.validation.Check;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.rosetta.simple.Data;

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*;
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*;
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*;

public class TypeValidator extends AbstractDeclarativeRosettaValidator {
	@Inject
	private RosettaEcoreUtil ecoreUtil;
	
	@Check
	public void checkTypeNameIsCapitalized(Data data) {
		// TODO: also enforce on Choice's once Choice does not extend Data anymore
		if (Character.isLowerCase(data.getName().charAt(0))) {
			warning("Type name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE);
		}
	}
	
	@Check
	public void checkDoNotExtendChoice(Data data) {
		// TODO: remove once Choice does not extend Data anymore
		if (data.getSuperType() instanceof Choice) {
			warning("Extending a choice type is deprecated", data, DATA__SUPER_TYPE);
		}
	}
	
	@Check
	public void checkCyclicExtensions(Data data) {
		Data p = data.getSuperType();
		if (p != null) {
			List<Data> path = new ArrayList<>();
			path.add(data);
			Set<Data> visited = new HashSet<>();
			visited.add(data);
			if (hasCyclicExtension(p, path, visited)) {
				String pathString = path.stream().map(e -> e.getName()).collect(Collectors.joining(" extends "));
				error("Cyclic extension: " + pathString, data, DATA__SUPER_TYPE);
			}
		}
	}
	private boolean hasCyclicExtension(Data current, List<Data> path, Set<Data> visited) {
		path.add(current);
		if (visited.add(current) && current.getSuperType() != null) {
			if (hasCyclicExtension(current.getSuperType(), path, visited)) {
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
	public void checkAttributeNamesAreUnique(Data data) {
		Set<String> usedNames = new HashSet<>();
		if (data.getSuperType() != null) {
			ecoreUtil.getAllAttributes(data.getSuperType()).forEach(attr -> usedNames.add(attr.getName()));
		}
		Set<String> usedRestrictions = new HashSet<>();
		for (Attribute attr: data.getAttributes()) {
			if (!attr.isRestriction()) {
				if (!usedNames.add(attr.getName())) {
					// TODO: make this an error
					warning("Duplicate attribute '" + attr.getName() + "'. To override the ruleReference or synonym annotations on this attribute, use a rule source or synonym source instead. To restrict the type or cardinality of this attribute, use the keyword `restrict`.", attr, ROSETTA_NAMED__NAME);
				}
			} else {
				if (!usedRestrictions.add(attr.getName())) {
					error("Duplicate attribute restriction for '" + attr.getName() + "'.", attr, ROSETTA_NAMED__NAME);
				}
			}
		}
	}
	
	@Check
	public void checkAttributeRestrictionsShouldComeFirst(Data data) {
		boolean newAttrEncountered = false;
		for (Attribute attr: data.getAttributes()) {
			if (!attr.isRestriction()) {
				newAttrEncountered = true;
			} else if (newAttrEncountered) {
				error("Attribute restrictions should come before any new attributes.", attr, null);
			}
		}
	}
}
