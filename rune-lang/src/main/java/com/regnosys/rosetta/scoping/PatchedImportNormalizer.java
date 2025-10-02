package com.regnosys.rosetta.scoping;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;

/**
 * The {@code ImportNormalizer} class does not implement `equals` correctly, causing weird behavior
 * when used in polymorphic collections. More specifically, it violates commutative property
 * and symmetry when comparing instances. This class patches the `equals` implementation
 * to ensure proper behavior in collections.
 * 
 * TODO: contribute to Xtext
 */
public class PatchedImportNormalizer extends ImportNormalizer {

    public PatchedImportNormalizer(QualifiedName importedNamespace, boolean wildCard, boolean ignoreCase) {
        super(importedNamespace, wildCard, ignoreCase);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PatchedImportNormalizer other = (PatchedImportNormalizer)obj;
        if (other.hasWildCard() != hasWildCard() || other.isIgnoreCase() != isIgnoreCase())
            return false;
        if (isIgnoreCase()) {
            return other.getImportedNamespacePrefix().equalsIgnoreCase(getImportedNamespacePrefix());
        }
        return other.getImportedNamespacePrefix().equals(getImportedNamespacePrefix());
    }
}
