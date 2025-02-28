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

package com.regnosys.rosetta.resource;

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.DefaultFragmentProvider;
import org.eclipse.xtext.util.IResourceScopeCache;

public class RosettaFragmentProvider extends DefaultFragmentProvider {
	
	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;
	@Inject
	private IQualifiedNameConverter qualifiedNameConverter;
	@Inject
	private IResourceScopeCache cache;
	
	private static final String QUALIFIED_NAME_MAP = "qualifiedNameMap";
	
	@Override
	public String getFragment(EObject obj, Fallback fallback) {
		QualifiedName fqn = qualifiedNameProvider.getFullyQualifiedName(obj);
		if(fqn != null) 
			return qualifiedNameConverter.toString(fqn);
		return super.getFragment(obj, fallback);
	}
	
	@Override
	public EObject getEObject(Resource resource, String fragment, Fallback fallback) {
		QualifiedName fqn = qualifiedNameConverter.toQualifiedName(fragment);
		if(fqn != null) {
			EObject element = cache.get(QUALIFIED_NAME_MAP, resource, () -> {
				Map<QualifiedName, EObject> map = new HashMap<>();
				EcoreUtil2.<EObject>getAllContents(resource, false).forEachRemaining((it) -> {
					QualifiedName qn = qualifiedNameProvider.getFullyQualifiedName(it);
					map.put(qn, it);
				});
				return map;
			}).get(fqn);
			if(element != null)
				return element;
		}
		return super.getEObject(resource, fragment, fallback);
	}
}
