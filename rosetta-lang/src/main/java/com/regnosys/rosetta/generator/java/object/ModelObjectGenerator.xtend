
package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.ImmutableList
import javax.inject.Inject
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.types.RDataType
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.annotations.RosettaAttribute
import com.rosetta.model.lib.annotations.RosettaDataType
import com.rosetta.model.lib.meta.RosettaMetaData
import com.rosetta.util.DottedPath
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaParameterizedType
import java.util.Collection
import java.util.Collections
import java.util.List
import java.util.Objects
import java.util.Optional
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

import com.rosetta.util.types.generated.GeneratedJavaClass
import com.rosetta.util.types.generated.GeneratedJavaGenericTypeDeclaration
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.RosettaEcoreUtil

class ModelObjectGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension ModelObjectBuilderGenerator
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator
	@Inject extension RosettaEcoreUtil

	def generate(RootPackage root, IFileSystemAccess2 fsa, RDataType t, String version) {
		fsa.generateFile(root.child(t.name + '.java').withForwardSlashes,
			generateRosettaClass(root, t, version))
	}

	private def generateRosettaClass(RootPackage root, RDataType t, String version) {
		val scope = new JavaScope(root)
		buildClass(root, t.classBody(scope, new GeneratedJavaClass<Object>(root.meta, t.name+'Meta', Object), version), scope)
	}
	
	def StringConcatenationClient classBody(RDataType t, JavaScope scope, JavaClass<?> metaType, String version) {
		classBody(t, scope, metaType, version, Collections.emptyList)
	}

	def StringConcatenationClient classBody(RDataType t, JavaScope scope, JavaClass<?> metaType, String version, Collection<Object> interfaces) {
		val javaType = t.toJavaType
		val superInterface = javaType.interfaces.head
		val interfaceScope = scope.classScope(javaType.toString)
		val metaDataIdentifier = interfaceScope.createUniqueIdentifier("metaData");
		val builderScope = interfaceScope.classScope('''«javaType»Builder''')
		val implScope = interfaceScope.classScope('''«javaType»Impl''')
		'''
			«javadoc(t.EObject, version)»
			@«RosettaDataType»(value="«t.name»", builder=«javaType.toBuilderImplType».class, version="«EcoreUtil2.getContainerOfType(t.EObject, RosettaModel).version»")
			public interface «javaType» extends «superInterface»«implementsClause(t, interfaces)» {

				«metaType» «metaDataIdentifier» = new «metaType»();

				«startComment('Getter Methods')»
				«pojoInterfaceGetterMethods(javaType, t)»

				«startComment('Build Methods')»
				«pojoInterfaceBuilderMethods(javaType, t)»

				«startComment('Utility Methods')»
				«pojoInterfaceDefaultOverridenMethods(javaType, metaDataIdentifier, interfaces, t)»

				«startComment('Builder Interface')»
				interface «javaType»Builder extends «t.name», «superInterface.toBuilderType»«FOR inter:interfaces BEFORE ', ' SEPARATOR ', '»«buildify(inter)»«ENDFOR» {
					«pojoBuilderInterfaceGetterMethods(t, javaType, builderScope)»

					«t.builderProcessMethod»

					«javaType.toBuilderType» prune();
				}

				«startComment('''Immutable Implementation of «t.name»''')»
				class «javaType»Impl «IF superInterface != JavaClass.from(RosettaModelObject)»extends «superInterface.toImplType» «ENDIF»implements «t.name» {
					«t.rosettaClass(implScope)»

					«t.boilerPlate(implScope)»
				}

				«startComment('''Builder Implementation of «t.name»''')»
				«t.builderClass(interfaceScope)»
			}
		'''
	}

	protected def StringConcatenationClient pojoBuilderInterfaceGetterMethods(RDataType t, JavaClass<?> javaType, JavaScope builderScope) '''
		«FOR attribute : t.ownAttributes + t.additionalAttributes»
			«IF attribute.RType instanceof RDataType || !attribute.metaAnnotations.isEmpty»
				«IF !attribute.isMulti»
					«attribute.toBuilderTypeSingle» getOrCreate«attribute.name.toFirstUpper»();
					«attribute.toBuilderTypeSingle» get«attribute.name.toFirstUpper»();
				«ELSE»
					«attribute.toBuilderTypeSingle» getOrCreate«attribute.name.toFirstUpper»(int _index);
					«List»<? extends «attribute.toBuilderTypeSingle»> get«attribute.name.toFirstUpper»();
				«ENDIF»
			«ENDIF»
		«ENDFOR»
		«FOR attribute : t.allNonOverridenAttributes + t.additionalAttributes»
			«IF !attribute.isMulti»
				«javaType.toBuilderType» set«attribute.name.toFirstUpper»(«attribute.toMetaJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«IF !attribute.metaAnnotations.isEmpty»«javaType.toBuilderType» set«attribute.name.toFirstUpper»Value(«attribute.toJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);«ENDIF»
			«ELSE»
				«javaType.toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toMetaItemJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«javaType.toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toMetaItemJavaType» «builderScope.createUniqueIdentifier(attribute.name)», int _idx);
				«IF !attribute.metaAnnotations.isEmpty»«javaType.toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.toItemJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«javaType.toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.toItemJavaType» «builderScope.createUniqueIdentifier(attribute.name)», int _idx);«ENDIF»
				«javaType.toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toMetaJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«javaType.toBuilderType» set«attribute.name.toFirstUpper»(«attribute.toMetaJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«IF !attribute.metaAnnotations.isEmpty»«javaType.toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.toJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«javaType.toBuilderType» set«attribute.name.toFirstUpper»Value(«attribute.toJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);«ENDIF»
			«ENDIF»
		«ENDFOR»
		'''


	protected def StringConcatenationClient pojoInterfaceDefaultOverridenMethods(JavaClass<?> javaType, GeneratedIdentifier metaDataIdentifier, Collection<Object> interfaces, RDataType t)
		'''
		@Override
		default «RosettaMetaData»<? extends «javaType»> metaData() {
			return «metaDataIdentifier»;
		}

		@Override
		default Class<? extends «javaType»> getType() {
			return «javaType».class;
		}

		«FOR pt :interfaces.filter(JavaParameterizedType).filter[simpleName=="ReferenceWithMeta" || simpleName=="FieldWithMeta"]»
		@Override
		default Class<«pt.getArguments.head»> getValueType() {
			return «pt.getArguments.head».class;
		}
        «ENDFOR»

		«t.processMethod»
        '''


	protected def StringConcatenationClient pojoInterfaceGetterMethods(JavaClass<?> javaType, RDataType t) '''
		«FOR attribute : t.ownAttributes + t.additionalAttributes»
			«javadoc(attribute.definition, attribute.docReferences, null)»
			«attribute.toMetaJavaType» get«attribute.name.toFirstUpper»();
		«ENDFOR»
		'''

	protected def StringConcatenationClient pojoInterfaceBuilderMethods(JavaClass<?> javaType, RDataType t) '''
			«t.name» build();

			«javaType.toBuilderType» toBuilder();

			static «javaType.toBuilderType» builder() {
				return new «javaType.toBuilderImplType»();
			}
		'''


	def dispatch buildify(Object object) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	def dispatch buildify(Class<?> clazz) {
		new GeneratedJavaClass<Object>(DottedPath.splitOnDots(clazz.packageName), clazz.simpleName+"."+clazz.simpleName+"Builder", Object)
	}
	def dispatch buildify(JavaParameterizedType<?> clazz) {
		val builderClass = new GeneratedJavaClass(clazz.packageName, clazz.simpleName+"."+clazz.simpleName+"Builder", Object)
		val builderDeclaration = new GeneratedJavaGenericTypeDeclaration(builderClass, "T")
		JavaParameterizedType.from(builderDeclaration, clazz.getArguments)
	}

	def boolean globalKeyRecursive(RDataType class1) {
		if (class1.EObject.hasKeyedAnnotation) {
			return true
		}
		val s = class1.superType
		if (s !== null) {
			return globalKeyRecursive(s as RDataType)
		}
		return false
	}

	private def StringConcatenationClient rosettaClass(RDataType t, JavaScope scope) {
		val attributes = t.ownAttributes + t.additionalAttributes
		val javaType = t.toJavaType
		val superInterface = javaType.interfaces.head
		'''
		«FOR attribute : attributes»
			private final «attribute.toMetaJavaType» «scope.createIdentifier(attribute, attribute.name.toFirstLower)»;
		«ENDFOR»

		protected «javaType»Impl(«javaType.toBuilderType» builder) {
			«IF superInterface != JavaClass.from(RosettaModelObject)»
				super(builder);
			«ENDIF»
			«FOR attribute : attributes»
				this.«scope.getIdentifierOrThrow(attribute)» = «attribute.attributeFromBuilder»;
			«ENDFOR»
		}

		«FOR attribute : attributes»
			@Override
			@«RosettaAttribute»("«attribute.javaAnnotation»")
			public «attribute.toMetaJavaType» get«attribute.name.toFirstUpper»() {
				return «scope.getIdentifierOrThrow(attribute)»;
			}
			
		«ENDFOR»
		@Override
		public «t.name» build() {
			return this;
		}

		@Override
		public «javaType.toBuilderType» toBuilder() {
			«javaType.toBuilderType» builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(«javaType.toBuilderType» builder) {
			«IF (superInterface != JavaClass.from(RosettaModelObject))»
				super.setBuilderFields(builder);
			«ENDIF»
			«FOR attribute : attributes»
				«method(Optional, "ofNullable")»(get«attribute.name.toFirstUpper»()).ifPresent(builder::set«attribute.name.toFirstUpper»);
			«ENDFOR»
		}
		'''
	}

	private def StringConcatenationClient attributeFromBuilder(RAttribute attribute) {
		if(attribute.RType instanceof RDataType || !attribute.metaAnnotations.isEmpty) {
			if (attribute.isMulti)
				'''ofNullable(builder.get«attribute.name.toFirstUpper»()).filter(_l->!_l.isEmpty()).map(«attribute.buildRosettaObject»).orElse(null)'''
			else
				'''ofNullable(builder.get«attribute.name.toFirstUpper»()).map(«attribute.buildRosettaObject»).orElse(null)'''
		} else {
			if (!attribute.isMulti)
				'''builder.get«attribute.name.toFirstUpper»()«IF attribute.needsBuilder».build()«ENDIF»'''
			else
				'''ofNullable(builder.get«attribute.name.toFirstUpper»()).filter(_l->!_l.isEmpty()).map(«ImmutableList»::copyOf).orElse(null)'''
		}
	}

	private def StringConcatenationClient buildRosettaObject(RAttribute attribute) {
		if(attribute.isMulti) {
			'''list -> list.stream().filter(«Objects»::nonNull).map(f->f.build()).filter(«Objects»::nonNull).collect(«ImmutableList».toImmutableList())'''
		} else {
			'''f->f.build()'''
		}
	}
	
	private def StringConcatenationClient startComment(String msg) '''
	/*********************** «msg»  ***********************/'''


}