package com.regnosys.rosetta.utils;

import com.google.common.collect.Comparators;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaRootElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.naming.QualifiedName;

public class ImportManagementService {

	@Inject
	RosettaEcoreUtil rosettaEcoreUtil;
	@Inject
	IQualifiedNameProvider qualifiedNameProvider;
	
	private Comparator<Import> importComparator = Comparator.comparing(Import::getImportedNamespace, Comparator.nullsLast(String::compareTo));

	public void cleanupImports(RosettaModel model) {
		EList<Import> imports = model.getImports();

		//TODO need to rework on findUnused so that it can work on parent model imports as well
		// remove all duplicate/unused imports
		/*List<Import> unusedImports = findUnused(model);
		imports.removeAll(unusedImports);*/

		//Removing duplicate import causes problems as some of the imports are needed
		//as duplicate with use of ... as common etc
		/*List<Import> duplicateImports = findDuplicates(imports);
		imports.removeAll(duplicateImports);*/

		sortImports(imports);
	}

	public List<Import> findUnused(RosettaModel model) {
		List<QualifiedName> usedNames = new ArrayList<>();

		model.eAllContents().forEachRemaining(content -> {
			content.eCrossReferences().stream().filter(ref -> ref instanceof RosettaRootElement)
					.filter(ref -> rosettaEcoreUtil.isResolved((RosettaRootElement) ref)).forEach(ref -> {
						// Extract fully qualified name and add it to the list
						QualifiedName fullyQualifiedName = qualifiedNameProvider
								.getFullyQualifiedName((RosettaRootElement) ref);
						usedNames.add(fullyQualifiedName);
					});
		});

		List<Import> unusedImports = new ArrayList<>();
		for (Import ns : model.getImports()) {
			if (ns.getImportedNamespace() != null) {

				String[] segments = ns.getImportedNamespace().split("\\.");
				QualifiedName qn = QualifiedName.create(segments);
				boolean isWildcard = "*".equals(qn.getLastSegment());

				// Check if the import is used
				boolean isUsed;
				if (isWildcard) {
					QualifiedName importNamespace = qn.skipLast(1);
					isUsed = usedNames.stream().anyMatch(name -> {
	                    if (name.getSegmentCount() < importNamespace.getSegmentCount()) {
	                        return false; // Used name is too short to match
	                    }
	                    // compare first segments of the used name with the import namespace
	                    return name.skipLast(name.getSegmentCount() - importNamespace.getSegmentCount())
	                            .equals(importNamespace);
	                });
					
				} else {
					isUsed = usedNames.contains(qn);
				}

				if (!isUsed) {
					unusedImports.add(ns);
				}
			}
		}

		return unusedImports;

	}

	public List<Import> findDuplicates(List<Import> imports) {
		Set<String> seenNamespaces = new HashSet<String>();
		List<Import> duplicates = new ArrayList<Import>();
		// check duplicates
		for (Import imp : imports) {
			if (!(seenNamespaces.add(imp.getImportedNamespace()))) {
				duplicates.add(imp);
			}
		}
		return duplicates;
	}

	//TODO: adding a new line between package blocks is a whitespace operation move this to XtextResourceFormatter
	public String toString(List<Import> imports) {
		StringBuilder sortedImportsText = new StringBuilder();
		String lineSeparator = detectLineSeparator(imports);
		
		Import previousImport = null;
		for (Import imp : imports) {
			// if previous import comes from a different top-level package, insert blank line
			if (previousImport != null) {
				String previousFirstSegment = previousImport.getImportedNamespace().split("\\.")[0];
				String currentFirstSegment = imp.getImportedNamespace().split("\\.")[0];
				if (!previousFirstSegment.equals(currentFirstSegment)) {
					sortedImportsText.append(lineSeparator);
				}
			}
			sortedImportsText.append("import ").append(imp.getImportedNamespace());
			if (imp.getNamespaceAlias() != null) {
				sortedImportsText.append(" as ").append(imp.getNamespaceAlias());
			}
			sortedImportsText.append(lineSeparator);
			
			previousImport = imp;
		}

		return sortedImportsText.toString().strip();
	}

	/**
	 * The import block is inserted into an existing document, so it uses the line
	 * separator that document already uses (rather than the platform separator,
	 * which would mix line endings when the document was written on another OS).
	 * Defaults to "\n" when the document is empty or has no line breaks yet.
	 */
	private String detectLineSeparator(List<Import> imports) {
		if (!imports.isEmpty()) {
			INode node = NodeModelUtils.getNode(imports.get(0));
			if (node != null && node.getRootNode().getText().contains("\r\n")) {
				return "\r\n";
			}
		}
		return "\n";
	}

	public boolean isSorted(List<Import> imports) {
		return Comparators.isInOrder(imports, importComparator);
	}

	public void sortImports(EList<Import> imports) {
		ECollections.sort(imports, importComparator);
	}
}
