/*
 * Copyright 2026 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.regnosys.rosetta.serializer;

import com.google.common.collect.MapMaker;
import com.regnosys.rosetta.rosetta.Import;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaRootElement;
import jakarta.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.linking.impl.LinkingHelper;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.serializer.diagnostic.ISerializationDiagnostic.Acceptor;
import org.eclipse.xtext.serializer.tokens.CrossReferenceSerializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Serializes root-element references directly from model namespaces where that
 * is unambiguous. Xtext's default implementation asks the scope for all aliases
 * of the target object; for large generated models with many doc references
 * that repeatedly scans import scopes and resource descriptions.
 */
public class RosettaCrossReferenceSerializer extends CrossReferenceSerializer {
	private static final String WILDCARD_SUFFIX = ".*";
	private static final String IMPLICIT_BUILTIN_NAMESPACE = "com.rosetta.model";

	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;
	@Inject
	private IQualifiedNameConverter qualifiedNameConverter;
	@Inject
	private IValueConverterService valueConverter;
	@Inject
	private LinkingHelper linkingHelper;

	private final ConcurrentMap<RosettaModel, ImportResolutionCache> importResolutionCaches =
			new MapMaker().weakKeys().makeMap();

	@Override
	protected String getCrossReferenceNameFromScope(EObject semanticObject, CrossReference crossref, EObject target,
			IScope scope, Acceptor errors) {
		String serialized = getFastCrossReferenceName(semanticObject, crossref, target);
		if (serialized != null) {
			return serialized;
		}
		return super.getCrossReferenceNameFromScope(semanticObject, crossref, target, scope, errors);
	}

	private String getFastCrossReferenceName(EObject semanticObject, CrossReference crossref, EObject target) {
		if (!(target instanceof RosettaRootElement) || !(target instanceof RosettaNamed)) {
			return null;
		}
		RosettaModel model = EcoreUtil2.getContainerOfType(semanticObject, RosettaModel.class);
		if (model == null) {
			return null;
		}
		QualifiedName targetName = qualifiedNameProvider.getFullyQualifiedName(target);
		if (targetName == null || targetName.isEmpty()) {
			return null;
		}
		String name = getCache(model).getReferenceName(targetName);
		return convert(name, linkingHelper.getRuleNameFrom(crossref));
	}

	private String convert(String name, String ruleName) {
		try {
			return valueConverter.toString(name, ruleName);
		} catch (ValueConverterException e) {
			return null;
		}
	}

	private ImportResolutionCache getCache(RosettaModel model) {
		String signature = importSignature(model);
		return importResolutionCaches.compute(model, (key, existing) ->
				(existing != null && existing.signature.equals(signature))
						? existing
						: new ImportResolutionCache(key, signature));
	}

	private String importSignature(RosettaModel model) {
		StringBuilder signature = new StringBuilder();
		signature.append(model.getName()).append('\n');
		for (Import anImport : model.getImports()) {
			signature.append(anImport.getImportedNamespace())
					.append(" as ")
					.append(anImport.getNamespaceAlias())
					.append('\n');
		}
		return signature.toString();
	}

	private class ImportResolutionCache {
		private final String signature;
		private final Set<String> visibleNamespaces;
		private final Map<String, Integer> visibleSimpleNameCounts;

		private ImportResolutionCache(RosettaModel model, String signature) {
			this.signature = signature;
			this.visibleNamespaces = visibleNamespaces(model);
			this.visibleSimpleNameCounts = visibleSimpleNameCounts(model, visibleNamespaces);
		}

		private String getReferenceName(QualifiedName targetName) {
			if (targetName.getSegmentCount() == 1) {
				return qualifiedNameConverter.toString(targetName);
			}
			String simpleName = targetName.getLastSegment();
			String namespace = qualifiedNameConverter.toString(targetName.skipLast(1));
			if (visibleNamespaces.contains(namespace) && visibleSimpleNameCounts.getOrDefault(simpleName, 0) == 1) {
				return simpleName;
			}
			return qualifiedNameConverter.toString(targetName);
		}

		private Set<String> visibleNamespaces(RosettaModel model) {
			Set<String> namespaces = new HashSet<>();
			if (model.getName() != null) {
				namespaces.add(model.getName());
			}
			namespaces.add(IMPLICIT_BUILTIN_NAMESPACE);
			for (Import anImport : model.getImports()) {
				String importedNamespace = anImport.getImportedNamespace();
				if (importedNamespace == null || anImport.getNamespaceAlias() != null) {
					continue;
				}
				if (importedNamespace.endsWith(WILDCARD_SUFFIX)) {
					namespaces.add(importedNamespace.substring(0, importedNamespace.length() - WILDCARD_SUFFIX.length()));
				} else {
					QualifiedName importedName = qualifiedNameConverter.toQualifiedName(importedNamespace);
					if (importedName != null && importedName.getSegmentCount() > 1) {
						namespaces.add(qualifiedNameConverter.toString(importedName.skipLast(1)));
					}
				}
			}
			return namespaces;
		}

		private Map<String, Integer> visibleSimpleNameCounts(RosettaModel model, Set<String> namespaces) {
			Map<String, Integer> counts = new HashMap<>();
			Resource resource = model.eResource();
			ResourceSet resourceSet = resource == null ? null : resource.getResourceSet();
			if (resourceSet == null) {
				countVisibleNames(model, namespaces, counts);
				return counts;
			}
			for (Resource currentResource : resourceSet.getResources()) {
				for (EObject root : currentResource.getContents()) {
					if (root instanceof RosettaModel currentModel) {
						countVisibleNames(currentModel, namespaces, counts);
					}
				}
			}
			return counts;
		}

		private void countVisibleNames(RosettaModel model, Set<String> namespaces, Map<String, Integer> counts) {
			if (!namespaces.contains(model.getName())) {
				return;
			}
			for (RosettaRootElement element : model.getElements()) {
				if (element instanceof RosettaNamed named && named.getName() != null) {
					counts.merge(named.getName(), 1, Integer::sum);
				}
			}
		}
	}
}
