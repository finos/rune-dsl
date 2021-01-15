package com.regnosys.rosetta.generator.java.util

import java.lang.reflect.Method
import java.util.Set
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend2.lib.StringConcatenation
import org.eclipse.xtext.naming.QualifiedName
import java.util.Map
import java.util.List

class ImportingStringConcatination extends StringConcatenation {
	@Accessors(PUBLIC_GETTER)
	Map<String,QualifiedName> imports = newHashMap
	Set<String> staticImports = newHashSet
	Set<String> reservedSimpleNames = newHashSet
	

	def addReservedSimpleName(String reservedSimpleName) {
		reservedSimpleNames.add(reservedSimpleName)
	}
	
	def dispatch protected String getStringRepresentation(Object object) {
		super.getStringRepresentation(object)
	}

	def dispatch protected String getStringRepresentation(Class<?> object) {
		if(reservedSimpleNames.contains(object.simpleName)) {
			return object.name
		}
		addImport(object.name, object.simpleName)
	}
	
	def dispatch protected String getStringRepresentation(Method object) {
		addStaticImport(object)
		return object.name
	}

	def dispatch protected String getStringRepresentation(JavaType object) {
		if(object.simpleName == '*') {
			staticImports.add(object.name)
			return ''
		} else {
			if(reservedSimpleNames.contains(object.simpleName)) {
				return object.name
			}
			return addImport(object.name, object.simpleName)
		}
	}
	
	def dispatch protected String getStringRepresentation(ParameterizedType type) {
		return '''«type.type.stringRepresentation»«IF type.typeArgs!==null && !type.typeArgs.isEmpty»<«FOR t:type.typeArgs SEPARATOR ', '»«t.stringRepresentation»«ENDFOR»>«ENDIF»'''
	}
	
	def dispatch protected String getStringRepresentation(MetaType object) {
		if(object.simpleName == '*') {
			staticImports.add(object.name)
			return ''
		} else {
			if(reservedSimpleNames.contains(object.metaFieldSimpleName)) {
				return object.metaFieldName
			}
			addImport(object.name, object.name)
			return addImport(object.metaFieldName, object.metaFieldSimpleName)
		}
	}
	
	def private addStaticImport(Method method) {
		val qName = method.declaringClass.name
		
		val qualified = QualifiedName.create(qName.split('\\.'))
		val target = qualified.lastSegment
		
		if (target.contains('$')) {
			val toImport = qualified.skipLast(1).append(target.split('\\$').head)
			staticImports.add(toImport.toString + '.' + method.name)	
		} else {
			staticImports.add(qName  + '.' + method.name)	
		}
	}

	/** returns the name that should be used at in generated code. If an import can be added to then the shortname can be used
	 * if an import cannot be added because it will clash then the full name must be used
	*/
	def String addImport(String qName, String shortName) {
		if(qName.startsWith('java.lang.'))
			return shortName
		val fullName = qName.replaceAll('\\$','.')
		var qualified = QualifiedName.create(fullName.split('\\.'))
		
		if (imports.getOrDefault(shortName, qualified)!=qualified) return fullName//if an import has already been added for this shortname (that is different from this fullname) then we can't add a clashing import 
		
		imports.put(shortName, qualified)
		return shortName
	}
	
	def getImports() {
		imports.values.map[toString].sortBy[it]
	}
	def getImports(String... excluding) {
		val exQ = excluding.map[QualifiedName.create(split('\\.'))] 
		
		imports.values.filter[notMatch(exQ)].map[toString].sortBy[it]
	}
	
	def boolean notMatch(QualifiedName importName, List<QualifiedName> packageName) {
		return !packageName.contains(importName.skipLast(1))
	}
	
	def getStaticImports() {
		staticImports.sortBy[it]
	}
	
	
}
