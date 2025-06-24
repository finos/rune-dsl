package com.regnosys.rosetta.generator.java.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xtend.lib.annotations.Accessors;
import com.regnosys.rosetta.generator.java.JavaScope;
import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.TargetLanguageStringConcatenation;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;
import com.regnosys.rosetta.generator.java.types.JavaTypeRepresentation;
import com.rosetta.util.types.JavaParameterizedType;

class ImportingStringConcatenation extends TargetLanguageStringConcatenation {
	private final Map<DottedPath, DottedPath> imports = new HashMap<>();
	private final Map<DottedPath, DottedPath> staticImports = new HashMap<>();
	
	private final JavaScope scope;
		
	public ImportingStringConcatenation(JavaScope topScope) {
		this.scope = topScope;
	}
	
	@Override
	protected void append(Object object, int index) {
		if (object instanceof JavaClass) {
			throw new IllegalStateException();
		} else {
			super.append(object, index);
		}
	}
	@Override
	protected void append(Object object, String indentation, int index) {
		if (object instanceof JavaClass) {
			throw new IllegalStateException();
		} else {
			super.append(object, indentation, index);
		}
	}
	
	@Override
	protected Object handle(Object object) {
		if (object instanceof JavaClass clazz) {
			return getOrImportIdentifier(clazz, clazz.getPackageName(), clazz.getSimpleName());
		} else if (object instanceof PreferWildcardImportClass clazz) {
			var jc = clazz.getJavaClass();
			return getOrWildcardImportIdentifier(jc, jc.getPackageName(), jc.getSimpleName());
		} else if (object instanceof Method m) {
			return getOrStaticImportIdentifier(m, DottedPath.splitOnDots(m.getDeclaringClass().getCanonicalName()), m.getName());
		} else if (object instanceof PreferWildcardImportMethod pm) {
			var m = pm.getMethod();
			return getOrStaticWildcardImportIdentifier(m, DottedPath.splitOnDots(m.getDeclaringClass().getCanonicalName()), m.getName());
		}
		return super.handle(object);
	}
	
	@Override
	protected Object normalize(Object object) {
		var n = super.normalize(object);
		var t = JavaType.from(n);
		if (t == null) {
			return n;
		}
		if (t instanceof JavaClass) {
			if (t instanceof JavaParameterizedType) {
				return new JavaTypeRepresentation(t);
			}
			return t;
		}
		return new JavaTypeRepresentation(t);
	}
			
	public GeneratedIdentifier getOrImportIdentifier(Object object, DottedPath packageName, String simpleName) {
		var canonicalName = packageName.child(simpleName);
		return scope.getIdentifier(object)
				.orElseGet(() -> {
					if (scope.getIdentifiers().stream().anyMatch(id -> id.getDesiredName().equals(simpleName))) {
						// There is a conflicting name in the scope. Use the canonical name.
						return scope.createIdentifier(object, canonicalName.withDots());
					}
					addImportIfNotAlreadyImported(packageName, canonicalName);
					return scope.createIdentifier(object, simpleName);
				});
	}
	public GeneratedIdentifier getOrWildcardImportIdentifier(Object object, DottedPath packageName, String simpleName) {
		var canonicalName = packageName.child(simpleName);
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
				];
	}
	
	public GeneratedIdentifier getOrStaticImportIdentifier(Object object, DottedPath canonicalClassName, String staticMemberName) {
		var canonicalStaticMemberName = canonicalClassName.child(staticMemberName);
		return scope.getIdentifier(object)
				.orElseGet[
					if (scope.identifiers.findFirst[desiredName == staticMemberName] !== null) {
						// There is a conflicting name in the scope. Use the canonical name.
						return scope.createIdentifier(object, canonicalStaticMemberName.withDots);
					}
					addStaticImportIfNotAlreadyImported(canonicalClassName, canonicalStaticMemberName)
					return scope.createIdentifier(object, staticMemberName);
				];
	}
	public GeneratedIdentifier getOrStaticWildcardImportIdentifier(Object object, DottedPath canonicalClassName, String staticMemberName) {
		var canonicalStaticMemberName = canonicalClassName.child(staticMemberName);
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
				];
	}
	
	public void addImportIfNotAlreadyImported(DottedPath packageName, DottedPath canonicalName) {
		if (!imports.containsKey(packageName.child("*"))) {
			imports.put(canonicalName, packageName);
		}
	}
	
	public void addWildcardImport(DottedPath packageName) {
		var wildcard = packageName.child("*");
		if (imports.put(wildcard, packageName) == null) {
			imports.entrySet.removeIf[key != wildcard && value == packageName];
		}
	}
	
	public void addStaticImportIfNotAlreadyImported(DottedPath canonicalClassName, DottedPath canonicalStaticMemberName) {
		if (!staticImports.containsKey(canonicalClassName.child("*"))) {
			staticImports.put(canonicalStaticMemberName, canonicalClassName);
		}
	}
	
	public void addStaticWildcardImport(DottedPath canonicalClassName) {
		var wildcard = canonicalClassName.child("*");
		if (staticImports.put(wildcard, canonicalClassName) == null) {
			staticImports.entrySet.removeIf[key != wildcard && value == canonicalClassName];
		}
	}

	
	public List<DottedPath> getImports() {
		imports.keySet.sort;
	}
	
	public List<DottedPath> getStaticImports() {
		staticImports.keySet.sort;
	}
}
