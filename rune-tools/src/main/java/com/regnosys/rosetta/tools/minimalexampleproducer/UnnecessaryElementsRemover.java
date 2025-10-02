/*
 * Copyright 2024 REGnosys
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

package com.regnosys.rosetta.tools.minimalexampleproducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaPackage;
import com.regnosys.rosetta.rosetta.simple.SimplePackage;
import com.rosetta.util.DottedPath;

public class UnnecessaryElementsRemover {
	private static Logger LOGGER = LoggerFactory.getLogger(UnnecessaryElementsRemover.class);
	
	@Inject
	private RosettaBuiltinsService builtins;
	
	private RosettaPackage pckg = RosettaPackage.eINSTANCE;
	private SimplePackage sPckg = SimplePackage.eINSTANCE;
	private Set<EClass> removableClasses = Set.of(
			
		);
	private Map<EClass, Set<EStructuralFeature>> removableFeatures = Map.of(
			pckg.getRosettaDefinable(), Set.of(pckg.getRosettaDefinable_Definition()),
			sPckg.getAnnotated(), Set.of(sPckg.getAnnotated_Annotations()),
			pckg.getRosettaModel(), Set.of(pckg.getRosettaModel_Elements()),
			pckg.getRosettaTypeWithConditions(), Set.of(pckg.getRosettaTypeWithConditions_Conditions()),
			sPckg.getData(), Set.of(sPckg.getData_Attributes()),
			sPckg.getReferences(), Set.of(sPckg.getReferences_References()),
			sPckg.getFunction(), Set.of(sPckg.getFunction_Shortcuts(), sPckg.getFunction_Conditions(), sPckg.getFunction_Operations(), sPckg.getFunction_PostConditions()),
			pckg.getRosettaEnumeration(), Set.of(pckg.getRosettaEnumeration_EnumValues())
		);
	
	private Map<EClass, Set<EStructuralFeature>> removableFeaturesCache = new HashMap<>();
	private Set<EStructuralFeature> getRemovableFeatures(EClass eclass) {
		return removableFeaturesCache.computeIfAbsent(eclass, (key) -> {
			Set<EStructuralFeature> result = new HashSet<>();
			removableFeatures.entrySet().stream()
				.filter(e -> e.getKey().isSuperTypeOf(eclass))
				.forEach(e -> result.addAll(e.getValue()));
			eclass.getEAllStructuralFeatures().stream()
				.filter(f -> {
					EClassifier featureClass = f.getEType();
					if (featureClass instanceof EClass) {
						if (removableClasses.stream().anyMatch(removableClass -> removableClass.isSuperTypeOf((EClass) featureClass))) {
							return true;
						}
					}
					return false;
				})
				.forEach(f -> result.add(f));
			return result;
		});
	}

	public void removeUnnecessaryElementsFromResourceSet(EObject element, boolean persist) throws IOException {
		Set<EObject> necessary = findNecessaryElements(element);
		
		ResourceSet resourceSet = element.eResource().getResourceSet();
		List<Resource> resources = new ArrayList<>(resourceSet.getResources());
		for (Resource resource : resources) {
			if (resource.getURI().equals(builtins.annotationsURI) || resource.getURI().equals(builtins.basicTypesURI)) {
				continue;
			}
			RosettaModel model = getModel(resource);
			if (!necessary.contains(model)) {
				LOGGER.info("Removing resource " + resource.getURI());
				if (persist) {
					resource.delete(null);
				} else {
					resourceSet.getResources().remove(resource);
				}
				if (resourceSet.getResources().stream().allMatch(r -> !getModel(r).getName().equals(model.getName()))) {
					LOGGER.info("Removing imports to " + model.getName());
					removeImports(resourceSet, DottedPath.splitOnDots(model.getName()));
				}
			} else {
				LOGGER.info("Cleaning resource " + resource.getURI());
				removeUnnecessaryChildren(model, necessary);
				if (persist) {
					resource.save(null);
				}
			}
		}
	}
	private void removeUnnecessaryChildren(EObject parent, Set<EObject> necessary) {
		EClass eclass = parent.eClass();
		Set<EStructuralFeature> removableFeaturesForClass = getRemovableFeatures(eclass);
		eclass.getEAllStructuralFeatures().stream()
			.filter(f -> !(f instanceof EReference && ((EReference)f).isContainer()))
			.forEach(f -> {
				boolean isRemovable = removableFeaturesForClass != null && removableFeaturesForClass.contains(f);
				if (f instanceof EReference && !((EReference)f).isContainment()) {
					if (isRemovable) {
						parent.eUnset(f);
					}
				} else {
					Object value = parent.eGet(f);
					if (value instanceof EObject) {
						if (necessary.contains(value)) {
							removeUnnecessaryChildren((EObject) value, necessary);
						} else {
							parent.eUnset(f);
						}
					} else if (value instanceof EList<?>) {
						((EList<?>)value).removeIf(item -> {
							if (necessary.contains(item)) {
								removeUnnecessaryChildren((EObject) item, necessary);
								return false;
							} else {
								return true;
							}
						});
					} else {
						if (isRemovable) {
							parent.eUnset(f);
						}
					}
				}
			});
	}
	private void removeImports(ResourceSet resourceSet, DottedPath namespaceToRemove) {
		for (Resource resource : resourceSet.getResources()) {
			RosettaModel model = (RosettaModel) resource.getContents().get(0);
			model.getImports().removeIf(imp -> {
				DottedPath namespace = DottedPath.splitOnDots(imp.getImportedNamespace());
				if (namespace.last().equals("*")) {
					namespace = namespace.parent();
				}
				return namespace.equals(namespaceToRemove);
			});
		}
		
	}
	private RosettaModel getModel(Resource resource) {
		return (RosettaModel) resource.getContents().get(0);
	}

	public Set<EObject> findNecessaryElements(EObject element) {
		Set<EObject> result = new HashSet<>();
		addNecessaryElements(element, result);
		return result;
	}
	private void addNecessaryElements(EObject element, Set<EObject> found) {
		if (found.add(element)) {
			if (element.eContainer() != null) {
				addNecessaryElements(element.eContainer(), found);
			}
			EClass eclass = element.eClass();
			Stream<EReference> necessaryRefs = eclass.getEAllReferences().stream().filter(f -> !f.isContainer());
			Set<EStructuralFeature> removableFeaturesForClass = getRemovableFeatures(eclass);
			if (removableFeaturesForClass != null) {
				necessaryRefs = necessaryRefs.filter(f -> !removableFeaturesForClass.contains(f));
			}
			necessaryRefs.forEach(f -> {
				Object value = element.eGet(f);
				if (value instanceof EObject) {
					addNecessaryElements((EObject) value, found);
				} else if (value instanceof EList<?>) {
					((EList<?>)value).forEach(item -> {
						if (item instanceof EObject) {
							addNecessaryElements((EObject) item, found);
						}
					});
				}
			});
		}
	}
}
