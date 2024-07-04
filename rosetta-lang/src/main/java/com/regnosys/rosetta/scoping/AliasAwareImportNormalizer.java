package com.regnosys.rosetta.scoping;

import java.util.Objects;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;

public class AliasAwareImportNormalizer extends ImportNormalizer {
	private final String namespaceAlias;

	public AliasAwareImportNormalizer(QualifiedName importedNamespace, boolean wildCard, boolean ignoreCase, String namespaceAlias) {
		super(importedNamespace, wildCard, ignoreCase);
		this.namespaceAlias = namespaceAlias;
	}
	
	@Override
	public QualifiedName deresolve(QualifiedName fullyQualifiedName) { 
		if (namespaceAlias != null) {
			return null; //TODO: implement this
		} else {
			return super.deresolve(fullyQualifiedName);
		}	
	}
	
	@Override
	public QualifiedName resolve(QualifiedName relativeName) {
		if (namespaceAlias != null) {
			return null; //TODO: implement this
		} else {
			return super.resolve(relativeName);
		}
	}

	@Override
	public String toString() {
		return getImportedNamespacePrefix().toString() + (hasWildCard() ? ".*" : "") + (namespaceAlias != null ? "as " + namespaceAlias : "");
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
		if (this==obj)
			return true;
		if (obj==null)
			return false;
		if (super.equals(obj) == false) {
			return false;
		}
		if (obj instanceof AliasAwareImportNormalizer) {
			AliasAwareImportNormalizer other = (AliasAwareImportNormalizer)obj;
			return other.namespaceAlias.equals(namespaceAlias);
		}
		return false;
	}
	
	
	
}
