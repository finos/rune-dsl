package com.regnosys.rosetta.scoping;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;

public class AliasAwareImportNormalizer extends PatchedImportNormalizer {	
	private final ImportNormalizer aliasNormalizer;

	public AliasAwareImportNormalizer(QualifiedName importedNamespace, QualifiedName namespaceAlias, boolean wildCard,
			boolean ignoreCase) {
		super(importedNamespace, wildCard, ignoreCase);
		this.aliasNormalizer = new PatchedImportNormalizer(namespaceAlias, wildCard, ignoreCase);
	}

	@Override
	public QualifiedName deresolve(QualifiedName fullyQualifiedName) {
		QualifiedName deresolved = super.deresolve(fullyQualifiedName);
		if (deresolved != null) {
			return aliasNormalizer.resolve(deresolved);
		}
		return null;
	}

	@Override
	public QualifiedName resolve(QualifiedName relativeName) {
		QualifiedName deresolved = aliasNormalizer.deresolve(relativeName);
		if (deresolved != null) {
			return super.resolve(deresolved);
		}
		return null;
	}

	@Override
	public String toString() {
		return getImportedNamespacePrefix().toString() + (hasWildCard() ? ".*" : "")
				+ " as " + aliasNormalizer.getImportedNamespacePrefix();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = super.hashCode();
		result = prime * result + aliasNormalizer.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AliasAwareImportNormalizer other = (AliasAwareImportNormalizer)obj;
		if (other.hasWildCard() != hasWildCard() || other.isIgnoreCase() != isIgnoreCase())
			return false;
		if (!aliasNormalizer.equals(other.aliasNormalizer))
			return false;
		if (isIgnoreCase()) {
			return other.getImportedNamespacePrefix().equalsIgnoreCase(getImportedNamespacePrefix());
		}
		return other.getImportedNamespacePrefix().equals(getImportedNamespacePrefix());
	}
}