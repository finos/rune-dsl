package com.regnosys.rosetta.resource

import com.google.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.naming.IQualifiedNameConverter
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.resource.DefaultFragmentProvider
import org.eclipse.xtext.util.IResourceScopeCache

class RosettaFragmentProvider extends DefaultFragmentProvider {
	
	@Inject IQualifiedNameProvider qualifiedNameProvider 
	@Inject IQualifiedNameConverter qualifiedNameConverter
	@Inject IResourceScopeCache cache
	
	static val QUALIFIED_NAME_MAP = 'qualifiedNameMap' 
	
	override getFragment(EObject obj, Fallback fallback) {
		val fqn = qualifiedNameProvider.getFullyQualifiedName(obj)
		if(fqn !== null) 
			return qualifiedNameConverter.toString(fqn)
		return super.getFragment(obj, fallback)
	}
	
	override getEObject(Resource resource, String fragment, Fallback fallback) {
		val fqn = qualifiedNameConverter.toQualifiedName(fragment)
		if(fqn !== null) {
			val element = cache.get(QUALIFIED_NAME_MAP, resource, [
				val map = newHashMap
				EcoreUtil.getAllContents(resource, false).forEach [
					val qn = qualifiedNameProvider.getFullyQualifiedName(it)
					map.put(qn, it)
				]		
				map
			]).get(fqn)
			if(element !== null)
				return element
		}
		super.getEObject(resource, fragment, fallback)
	}
}
