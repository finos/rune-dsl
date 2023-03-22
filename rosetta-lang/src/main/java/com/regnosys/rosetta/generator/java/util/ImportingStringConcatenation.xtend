package com.regnosys.rosetta.generator.java.util

import java.lang.reflect.Method
import java.util.List
import java.util.Map
import org.eclipse.xtend.lib.annotations.Accessors
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.types.JavaType;
import com.regnosys.rosetta.generator.TargetLanguageStringConcatenation
import com.regnosys.rosetta.generator.java.types.JavaClass
import com.regnosys.rosetta.utils.DottedPath

class ImportingStringConcatenation extends TargetLanguageStringConcatenation {
	@Accessors(PUBLIC_GETTER)
	Map<DottedPath, DottedPath> imports = newHashMap
	Map<DottedPath, DottedPath> staticImports = newHashMap
	
	JavaScope scope;
		
	new(JavaScope topScope) {
		this.scope = topScope;
	}
	
	override protected void append(Object object, int index) {
		if (object instanceof JavaClass) {
			throw new IllegalStateException()
		} else {
			super.append(object, index)
		}
	}
	override protected void append(Object object, String indentation, int index) {
		if (object instanceof JavaClass) {
			throw new IllegalStateException()
		} else {
			super.append(object, indentation, index)
		}
	}
	
	override protected Object handle(Object object) {
		if (object instanceof JavaClass) {
			return getOrImportIdentifier(object, object.packageName, object.simpleName)
		} else if (object instanceof PreferWildcardImportClass) {
			val jc = object.javaClass
			return getOrWildcardImportIdentifier(jc, jc.packageName, jc.simpleName)
		} else if (object instanceof Method) {
			return getOrStaticImportIdentifier(object, DottedPath.splitOnDots(object.declaringClass.canonicalName), object.name)
		} else if (object instanceof PreferWildcardImportMethod) {
			val m = object.method
			return getOrStaticWildcardImportIdentifier(m, DottedPath.splitOnDots(m.declaringClass.canonicalName), m.name)
		}
		return super.handle(object)
	}
	
	override protected Object normalize(Object object) {
		val n = super.normalize(object)
		val t = JavaType.from(n)
		if (t === null) {
			return n
		}
		return t
	}
			
	def GeneratedIdentifier getOrImportIdentifier(Object object, DottedPath packageName, String simpleName) {
		val canonicalName = packageName.child(simpleName)
		return scope.getIdentifier(object)
				.orElseGet[
					if (scope.identifiers.findFirst[desiredName == simpleName] !== null) {
						// There is a conflicting name in the scope. Use the canonical name.
						return scope.createIdentifier(object, canonicalName.withDots);
					}
					addImportIfNotAlreadyImported(packageName, canonicalName)
					return scope.createIdentifier(object, simpleName);
				]
	}
	def GeneratedIdentifier getOrWildcardImportIdentifier(Object object, DottedPath packageName, String simpleName) {
		val canonicalName = packageName.child(simpleName)
		return scope.getIdentifier(object)
				.map[
					// If the identifier is already imported, overwrite with a wildcard import.
					if (imports.containsKey(canonicalName)) {
						addWildcardImport(packageName)
					}
					return it
				]
				.orElseGet[
					if (scope.identifiers.findFirst[desiredName == simpleName] !== null) {
						// There is a conflicting name in the scope. Use the canonical name.
						return scope.createIdentifier(object, canonicalName.withDots);
					}
					addWildcardImport(packageName)
					return scope.createIdentifier(object, simpleName);
				]
	}
	
	def GeneratedIdentifier getOrStaticImportIdentifier(Object object, DottedPath canonicalClassName, String staticMemberName) {
		val canonicalStaticMemberName = canonicalClassName.child(staticMemberName)
		return scope.getIdentifier(object)
				.orElseGet[
					if (scope.identifiers.findFirst[desiredName == staticMemberName] !== null) {
						// There is a conflicting name in the scope. Use the canonical name.
						return scope.createIdentifier(object, canonicalStaticMemberName.withDots);
					}
					addStaticImportIfNotAlreadyImported(canonicalClassName, canonicalStaticMemberName)
					return scope.createIdentifier(object, staticMemberName);
				]
	}
	def GeneratedIdentifier getOrStaticWildcardImportIdentifier(Object object, DottedPath canonicalClassName, String staticMemberName) {
		val canonicalStaticMemberName = canonicalClassName.child(staticMemberName)
		return scope.getIdentifier(object)
				.map[
					// If the identifier is already imported, overwrite with a wildcard import.
					if (staticImports.containsKey(canonicalStaticMemberName)) {
						addStaticWildcardImport(canonicalClassName)
					}
					return it
				]
				.orElseGet[
					if (scope.identifiers.findFirst[desiredName == staticMemberName] !== null) {
						// There is a conflicting name in the scope. Use the canonical name.
						return scope.createIdentifier(object, canonicalStaticMemberName.withDots);
					}
					addStaticWildcardImport(canonicalClassName)
					return scope.createIdentifier(object, staticMemberName);
				]
	}
	
	def void addImportIfNotAlreadyImported(DottedPath packageName, DottedPath canonicalName) {
		if (!imports.containsKey(packageName.child("*"))) {
			imports.put(canonicalName, packageName)
		}
	}
	
	def void addWildcardImport(DottedPath packageName) {
		val wildcard = packageName.child("*")
		if (imports.put(wildcard, packageName) === null) {
			imports.entrySet.removeIf[key != wildcard && value == packageName]
		}
	}
	
	def void addStaticImportIfNotAlreadyImported(DottedPath canonicalClassName, DottedPath canonicalStaticMemberName) {
		if (!staticImports.containsKey(canonicalClassName.child("*"))) {
			staticImports.put(canonicalStaticMemberName, canonicalClassName)
		}
	}
	
	def void addStaticWildcardImport(DottedPath canonicalClassName) {
		val wildcard = canonicalClassName.child("*")
		if (staticImports.put(wildcard, canonicalClassName) === null) {
			staticImports.entrySet.removeIf[key != wildcard && value == canonicalClassName]
		}
	}

	
	def List<DottedPath> getImports() {
		imports.keySet.sort
	}
	
	def List<DottedPath> getStaticImports() {
		staticImports.keySet.sort
	}
}
