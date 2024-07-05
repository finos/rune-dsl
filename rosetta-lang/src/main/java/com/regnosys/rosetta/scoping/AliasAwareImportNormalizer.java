package com.regnosys.rosetta.scoping;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;

public class AliasAwareImportNormalizer extends ImportNormalizer {
	private final QualifiedName namespaceAlias;

	public AliasAwareImportNormalizer(QualifiedName importedNamespace, String namespaceAlias, boolean wildCard,
			boolean ignoreCase) {
		super(importedNamespace, wildCard, ignoreCase);
		this.namespaceAlias = namespaceAlias != null ? QualifiedName.create(namespaceAlias) : null;
	}

	@Override
	public QualifiedName deresolve(QualifiedName fullyQualifiedName) {
		if (namespaceAlias != null) {
			QualifiedName deresolved = super.deresolve(fullyQualifiedName);
			if (deresolved != null) {
				return namespaceAlias.append(deresolved);
			}
			return null;
		} else {
			return super.deresolve(fullyQualifiedName);
		}
	}

	@Override
	public QualifiedName resolve(QualifiedName relativeName) {
		if (relativeName.isEmpty())
			return null;

		if (namespaceAlias != null && relativeName.startsWith(namespaceAlias)
				&& relativeName.getSegmentCount() != namespaceAlias.getSegmentCount()) {
			return super.resolve(relativeName.skipFirst(namespaceAlias.getSegmentCount()));
		} else {
			return super.resolve(relativeName);
		}
	}

	@Override
	public String toString() {
		return getImportedNamespacePrefix().toString() + (hasWildCard() ? ".*" : "")
				+ (namespaceAlias != null ? "as " + namespaceAlias : "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = super.hashCode();
		result = prime * result + namespaceAlias.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (super.equals(obj) == false) {
			return false;
		}
		if (obj instanceof AliasAwareImportNormalizer) {
			AliasAwareImportNormalizer other = (AliasAwareImportNormalizer) obj;
			return other.namespaceAlias.equals(namespaceAlias);
		}
		return false;
	}

}
