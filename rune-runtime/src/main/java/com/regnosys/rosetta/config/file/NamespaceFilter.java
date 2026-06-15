package com.regnosys.rosetta.config.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.rosetta.util.DottedPath;

public class NamespaceFilter implements Predicate<String> {
	private final List<DottedPath> genericNamespaces; // of the form `abc.efg.*`.
	private final List<DottedPath> specificNamespaces; // of the form `abc.efg`.
	
	public NamespaceFilter() {
		this(Collections.emptyList());
	}
	@JsonCreator
	public NamespaceFilter(List<DottedPath> allowedNamespacePatterns) {
		Validate.noNullElements(allowedNamespacePatterns);
		
		this.genericNamespaces = new ArrayList<>();
		this.specificNamespaces = new ArrayList<>();
		for (DottedPath namespacePattern : allowedNamespacePatterns) {
			if (namespacePattern.last().equals("*")) {
				genericNamespaces.add(namespacePattern.parent());
			} else {
				specificNamespaces.add(namespacePattern);
			}
		}
	}

	@Override
	public boolean test(String t) {
		if (genericNamespaces.isEmpty() && specificNamespaces.isEmpty()) {
			return true;
		}
		
		DottedPath namespace = DottedPath.splitOnDots(t);
		return genericNamespaces.stream().anyMatch(n -> namespace.startsWith(n)) || specificNamespaces.contains(namespace);
	}
}
