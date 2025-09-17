package com.regnosys.rosetta.generator.java.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.regnosys.rosetta.generator.GeneratedIdentifier;
import com.regnosys.rosetta.generator.TargetLanguageStringConcatenation;
import com.regnosys.rosetta.generator.java.scoping.JavaFileScope;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaType;
import com.regnosys.rosetta.generator.java.types.JavaTypeRepresentation;
import com.rosetta.util.types.JavaParameterizedType;

class ImportingStringConcatenation extends TargetLanguageStringConcatenation {
	private final Map<DottedPath, DottedPath> imports = new HashMap<>();
	private final Map<DottedPath, DottedPath> staticImports = new HashMap<>();
	
	private final JavaFileScope scope;
		
	public ImportingStringConcatenation(JavaFileScope topScope) {
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
		if (object instanceof JavaClass<?> clazz) {
			return getOrImportIdentifier(clazz, clazz.getPackageName(), clazz.getNestedTypeName());
		} else if (object instanceof PreferWildcardImportClass clazz) {
			var jc = clazz.getJavaClass();
			return getOrWildcardImportIdentifier(jc, jc.getPackageName(), jc.getNestedTypeName());
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

	public GeneratedIdentifier getOrImportIdentifier(Object object, DottedPath packageName, DottedPath nestedTypeName) {
		return scope.getIdentifier(object)
				.orElseGet(() -> internalDoImportIfPossible(object, nestedTypeName, packageName, topLevelTypeInFile -> addImportIfNotAlreadyImported(packageName, packageName.child(topLevelTypeInFile))));
	}

	public GeneratedIdentifier getOrWildcardImportIdentifier(Object object, DottedPath packageName, DottedPath nestedTypeName) {
		var canonicalName = packageName.concat(nestedTypeName);
		return scope.getIdentifier(object)
				.map(it -> {
					// If the identifier is already imported, overwrite with a wildcard import.
					if (imports.containsKey(canonicalName)) {
						addWildcardImport(packageName);
					}
					return it;
				})
				.orElseGet(() -> internalDoImportIfPossible(object, nestedTypeName, packageName, topLevelTypeInFile -> addWildcardImport(packageName)));
	}

	private GeneratedIdentifier internalDoImportIfPossible(Object object, DottedPath nestedTypeName, DottedPath packageName, Consumer<String> addImport) {
        String desiredName = nestedTypeName.withDots();
        String topLevelTypeInFile = nestedTypeName.first();
        DottedPath topLevelCanonicalName = packageName.child(topLevelTypeInFile);
        if (isAlreadyAccessible(packageName, topLevelCanonicalName)) {
            return scope.createIdentifier(object, desiredName);
        }
        if (scope.isNameTaken(topLevelTypeInFile)) {
            var canonicalName = packageName.concat(nestedTypeName);
            // There is a conflicting name in the scope. Use the canonical name.
            return scope.createIdentifier(object, canonicalName.withDots());
		}
		addImport.accept(topLevelTypeInFile);
		return scope.createIdentifier(object, desiredName);
	}
	
	public GeneratedIdentifier getOrStaticImportIdentifier(Object object, DottedPath canonicalClassName, String staticMemberName) {
		var canonicalStaticMemberName = canonicalClassName.child(staticMemberName);
		return scope.getIdentifier(object)
				.orElseGet(() -> internalDoStaticImportIfPossible(object, staticMemberName, canonicalStaticMemberName, () -> addStaticImportIfNotAlreadyImported(canonicalClassName, canonicalStaticMemberName)));
	}
	public GeneratedIdentifier getOrStaticWildcardImportIdentifier(Object object, DottedPath canonicalClassName, String staticMemberName) {
		var canonicalStaticMemberName = canonicalClassName.child(staticMemberName);
		return scope.getIdentifier(object)
				.map(it -> {
					// If the identifier is already imported, overwrite with a wildcard import.
					if (staticImports.containsKey(canonicalStaticMemberName)) {
						addStaticWildcardImport(canonicalClassName);
					}
					return it;
				})
				.orElseGet(() -> internalDoStaticImportIfPossible(object, staticMemberName, canonicalStaticMemberName, () -> addStaticWildcardImport(canonicalClassName)));
	}

	private GeneratedIdentifier internalDoStaticImportIfPossible(Object object, String staticMemberName, DottedPath canonicalStaticMemberName, Runnable addImport) {
		if (scope.isNameTaken(staticMemberName)) {
			// There is a conflicting name in the scope. Use the canonical name.
			return scope.createIdentifier(object, canonicalStaticMemberName.withDots());
		}
		addImport.run();
		return scope.createIdentifier(object, staticMemberName);
	}
	
	public void addImportIfNotAlreadyImported(DottedPath packageName, DottedPath canonicalName) {
		if (needsImport(packageName)) {
			imports.put(canonicalName, packageName);
		}
	}

    private boolean needsImport(DottedPath packageName) {
        return !packageName.equals(scope.getPackageName()) && !imports.containsKey(packageName.child("*"));
    }

    public boolean isAlreadyAccessible(DottedPath packageName, DottedPath canonicalName) {
    	return !needsImport(packageName) || imports.containsKey(canonicalName);
    }
	
	public void addWildcardImport(DottedPath packageName) {
		var wildcard = packageName.child("*");
		if (!packageName.equals(scope.getPackageName()) && imports.put(wildcard, packageName) == null) {
			imports.entrySet().removeIf(imp -> !imp.getKey().equals(wildcard) && imp.getValue().equals(packageName));
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
			staticImports.entrySet().removeIf(imp -> !imp.getKey().equals(wildcard) && imp.getValue().equals(canonicalClassName));
		}
	}

	
	public List<DottedPath> getImports() {
		return imports.keySet().stream().sorted().toList();
	}

	public List<DottedPath> getStaticImports() {
		return staticImports.keySet().stream().sorted().toList();
	}
}
