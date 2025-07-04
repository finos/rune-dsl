package com.regnosys.rosetta.generator.java.types;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import org.eclipse.xtend2.lib.StringConcatenationClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.regnosys.rosetta.generator.java.scoping.JavaPackageName;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import com.rosetta.util.types.JavaParameterizedType;
import com.rosetta.util.types.JavaType;
import com.rosetta.util.types.JavaTypeDeclaration;

public abstract class RGeneratedJavaClass<T> extends JavaClass<T> {
	private final DottedPath nestedTypeName;
	private final JavaPackageName packageName;
	
	protected RGeneratedJavaClass(JavaPackageName packageName, DottedPath nestedTypeName) {
		this.packageName = packageName;
		this.nestedTypeName = nestedTypeName;
	}
	
	public static <U> RGeneratedJavaClass<? extends U> create(JavaPackageName packageName, String simpleName, Class<U> superclassOrInterface) {
		return new SimpleGeneratedJavaClass<>(packageName, DottedPath.of(simpleName), superclassOrInterface);
	}
	public static <U> RGeneratedJavaClass<? extends U> create(JavaPackageName packageName, String simpleName, TypeReference<U> supertypeRef) {
		return new SimpleGeneratedJavaClass<>(packageName, DottedPath.of(simpleName), supertypeRef);
	}
	public static <U> RGeneratedJavaClass<? extends U> createWithSuperclass(JavaPackageName packageName, String simpleName, JavaClass<U> superclass) {
		return new SimpleGeneratedJavaClass<>(packageName, DottedPath.of(simpleName), superclass, null);
	}
	public static <U> RGeneratedJavaClass<? extends U> createImplementingInterface(JavaPackageName packageName, String simpleName, JavaClass<U> interf) {
		return new SimpleGeneratedJavaClass<>(packageName, DottedPath.of(simpleName), null, interf);
	}
	
	public <U> RGeneratedJavaClass<? extends U> createNestedClass(String simpleName, Class<U> superclassOrInterface) {
		return new SimpleGeneratedJavaClass<>(packageName, this.getNestedTypeName().child(simpleName), superclassOrInterface);
	}
	public <U> RGeneratedJavaClass<? extends U> createNestedClassWithSuperclass(String simpleName, JavaClass<U> superclass) {
		return new SimpleGeneratedJavaClass<>(packageName, this.getNestedTypeName().child(simpleName), superclass, null);
	}
	public <U> RGeneratedJavaClass<? extends U> createNestedClassImplementingInterface(String simpleName, JavaClass<U> interf) {
		return new SimpleGeneratedJavaClass<>(packageName, this.getNestedTypeName().child(simpleName), null, interf);
	}
	
	public StringConcatenationClient asClassDeclaration() {
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("class ");
				target.append(RGeneratedJavaClass.this.getSimpleName());
				JavaClass<?> superclass = RGeneratedJavaClass.this.getSuperclass();
				if (!JavaClass.OBJECT.equals(superclass)) {
					target.append(" extends ");
					target.append(superclass);
				}
				List<JavaClass<?>> interfaces = RGeneratedJavaClass.this.getInterfaces();
				if (!interfaces.isEmpty()) {
					target.append(" implements ");
					target.append(interfaces.get(0));
					for (int i=1; i<interfaces.size(); i++) {
						target.append(", ");
						target.append(interfaces.get(i));
					}
				}
			}
		};
	}
	public StringConcatenationClient asInterfaceDeclaration() {
		return new StringConcatenationClient() {
			@Override
			protected void appendTo(TargetStringConcatenation target) {
				target.append("interface ");
				target.append(RGeneratedJavaClass.this.getSimpleName());
				List<JavaClass<?>> interfaces = RGeneratedJavaClass.this.getInterfaces();
				if (!interfaces.isEmpty()) {
					target.append(" extends ");
					target.append(interfaces.get(0));
					for (int i=1; i<interfaces.size(); i++) {
						target.append(", ");
						target.append(interfaces.get(i));
					}
				}
			}
		};
	}

	@Override
	public boolean isSubtypeOf(JavaType other) {
		if (this.equals(other)) {
			return true;
		}
		if (this.getSuperclass().isSubtypeOf(other)) {
			return true;
		}
		if (this.getInterfaces().stream().anyMatch(t -> t.isSubtypeOf(other))) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean extendsDeclaration(JavaTypeDeclaration<?> other) {
		if (this.equals(other)) {
			return true;
		}
		if (this.getSuperclassDeclaration().extendsDeclaration(other)) {
			return true;
		}
		if (this.getInterfaceDeclarations().stream().anyMatch(t -> t.extendsDeclaration(other))) {
			return true;
		}
		return false;
	}

	@Override
	public DottedPath getNestedTypeName() {
		return nestedTypeName;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public Class<? extends T> loadClass(ClassLoader classLoader) throws ClassNotFoundException {
		throw new UnsupportedOperationException("Cannot load a generated class");
	}

	@Override
	public DottedPath getPackageName() {
		return packageName.getName();
	}
	
	public JavaPackageName getEscapedPackageName() {
		return packageName;
	}
	
	private static class SimpleGeneratedJavaClass<U> extends RGeneratedJavaClass<U> {		
		private final Type supertype;
		private final Class<? super U> rawSupertype;
		
		private JavaTypeDeclaration<? super U> superclassDeclaration;
		private JavaClass<? super U> superclass;
		
		private JavaTypeDeclaration<? super U> interfaceDeclaration;
		private JavaClass<? super U> interf;

		private SimpleGeneratedJavaClass(JavaPackageName packageName, DottedPath nestedTypeName, Type supertype, Class<? super U> rawSupertype) {
			super(packageName, nestedTypeName);
			this.supertype = supertype;
			this.rawSupertype = rawSupertype;
		}
		public SimpleGeneratedJavaClass(JavaPackageName packageName, DottedPath nestedTypeName, Class<U> supertype) {
			this(packageName, nestedTypeName, supertype, supertype);
		}
		public SimpleGeneratedJavaClass(JavaPackageName packageName, DottedPath nestedTypeName, TypeReference<U> supertypeRef) {
			this(packageName, nestedTypeName, supertypeRef.getType(), JavaParameterizedType.extractRawClass(supertypeRef.getType()));
		}
		public SimpleGeneratedJavaClass(JavaPackageName packageName, DottedPath nestedTypeName, JavaClass<? super U> superclass, JavaClass<? super U> interf) {
			super(packageName, nestedTypeName);
			this.supertype = null;
			this.rawSupertype = null;
			if (superclass != null) {
				if (superclass instanceof JavaParameterizedType<? super U> t) {
					this.superclassDeclaration = t.getGenericTypeDeclaration();
				} else {
					this.superclassDeclaration = superclass;
				}
				this.superclass = superclass;
			}
			if (interf != null) {
				if (interf instanceof JavaParameterizedType<? super U> t) {
					this.interfaceDeclaration = t.getGenericTypeDeclaration();
				} else {
					this.interfaceDeclaration = superclass;
				}
				this.interf = interf;
			}
		}

		@Override
		public JavaTypeDeclaration<? super U> getSuperclassDeclaration() {
			if (rawSupertype != null && rawSupertype.isInterface() || this.interfaceDeclaration != null) {
				return JavaClass.OBJECT;
			}
			if (superclassDeclaration == null) {
				superclassDeclaration = JavaTypeDeclaration.from(rawSupertype);
			}
			return superclassDeclaration;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public JavaClass<? super U> getSuperclass() {
			if (rawSupertype != null && rawSupertype.isInterface() || this.interf != null) {
				return JavaClass.OBJECT;
			}
			if (superclass == null) {
				superclass = (JavaClass<? super U>) JavaClass.from(supertype, Collections.emptyMap());
			}
			return superclass;
		}
		
		@Override
		public List<JavaTypeDeclaration<?>> getInterfaceDeclarations() {
			if (interfaceDeclaration == null) {
				if (rawSupertype != null && rawSupertype.isInterface()) {
					interfaceDeclaration = JavaTypeDeclaration.from(rawSupertype);
				}
			}
			if (interfaceDeclaration == null) {
				return Collections.emptyList();
			}
			return Collections.singletonList(interfaceDeclaration);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public List<JavaClass<?>> getInterfaces() {
			if (interf == null) {
				if (rawSupertype != null && rawSupertype.isInterface()) {
					interf = (JavaClass<? super U>) JavaClass.from(supertype, Collections.emptyMap());
				}
			}
			if (interf == null) {
				return Collections.emptyList();
			}
			return Collections.singletonList(interf);
		}
	}
}
