
package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.ImmutableList
import javax.inject.Inject
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.TypeSystem
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.RosettaModelObjectBuilder
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

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class ModelObjectGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension ModelObjectBuilderGenerator
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem

	def generate(RootPackage root, IFileSystemAccess2 fsa, Data data, String version) {
		fsa.generateFile(root.child(data.name + '.java').withForwardSlashes,
			generateRosettaClass(root, data, version))
	}

	private def generateRosettaClass(RootPackage root, Data d, String version) {
		val scope = new JavaScope(root)
		buildClass(root, d.classBody(scope, new JavaClass(root.meta, d.name+'Meta'), version), scope)
	}
	
	def StringConcatenationClient classBody(Data d, JavaScope scope, JavaClass metaType, String version) {
		classBody(d, scope, metaType, version, Collections.emptyList)
	}

	def StringConcatenationClient classBody(Data d, JavaScope scope, JavaClass metaType, String version, Collection<Object> interfaces) {
		val javaType = new RDataType(d).toJavaType
		val interfaceScope = scope.classScope(javaType.toString)
		val metaDataIdentifier = interfaceScope.createUniqueIdentifier("metaData");
		val builderScope = interfaceScope.classScope('''«javaType»Builder''')
		val implScope = interfaceScope.classScope('''«javaType»Impl''')
		'''
			«javadoc(d, version)»
			@«RosettaDataType»(value="«d.name»", builder=«javaType.toBuilderImplType».class, version="«d.model.version»")
			public interface «javaType» extends «IF d.hasSuperType»«new RDataType(d.superType).toJavaType»«ELSE»«RosettaModelObject»«ENDIF»«implementsClause(d, interfaces)» {

				«metaType» «metaDataIdentifier» = new «metaType»();

				«startComment('Getter Methods')»
				«pojoInterfaceGetterMethods(javaType, metaType, metaDataIdentifier, d)»

				«startComment('Build Methods')»
				«pojoInterfaceBuilderMethods(javaType, d)»

				«startComment('Utility Methods')»
				«pojoInterfaceDefaultOverridenMethods(javaType, metaDataIdentifier, interfaces, d)»

				«startComment('Builder Interface')»
				interface «javaType»Builder extends «d.name», «IF d.hasSuperType»«new RDataType(d.superType).toJavaType.toBuilderType», «ENDIF»«RosettaModelObjectBuilder»«FOR inter:interfaces BEFORE ', ' SEPARATOR ', '»«buildify(inter)»«ENDFOR» {
					«pojoBuilderInterfaceGetterMethods(d, javaType, builderScope)»

					«d.builderProcessMethod»

					«javaType.toBuilderType» prune();
				}

				«startComment('''Immutable Implementation of «d.name»''')»
				class «javaType»Impl «IF d.hasSuperType»extends «new RDataType(d.superType).toJavaType.toImplType» «ENDIF»implements «d.name» {
					«d.rosettaClass(implScope)»

					«d.boilerPlate(implScope)»
				}

				«startComment('''Builder Implementation of «d.name»''')»
				«d.builderClass(interfaceScope)»
			}
		'''
	}

	protected def StringConcatenationClient pojoBuilderInterfaceGetterMethods(Data d, JavaClass javaType, JavaScope builderScope) '''
		«FOR attribute : d.expandedAttributes»
			«IF attribute.isDataType || attribute.hasMetas»
				«IF attribute.cardinalityIsSingleValue»
					«attribute.toBuilderTypeSingle» getOrCreate«attribute.name.toFirstUpper»();
					«attribute.toBuilderTypeSingle» get«attribute.name.toFirstUpper»();
				«ELSE»
					«attribute.toBuilderTypeSingle» getOrCreate«attribute.name.toFirstUpper»(int _index);
					«List»<? extends «attribute.toBuilderTypeSingle»> get«attribute.name.toFirstUpper»();
				«ENDIF»
			«ENDIF»
		«ENDFOR»
		«FOR attribute : d.expandedAttributesPlus»
			«IF attribute.cardinalityIsSingleValue»
				«javaType.toBuilderType» set«attribute.name.toFirstUpper»(«attribute.toListOrSingleMetaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«IF attribute.hasMetas»«javaType.toBuilderType» set«attribute.name.toFirstUpper»Value(«attribute.rosettaType.typeCallToRType.toJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);«ENDIF»
			«ELSE»
				«javaType.toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toMetaOrRegularJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«javaType.toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toMetaOrRegularJavaType» «builderScope.createUniqueIdentifier(attribute.name)», int _idx);
				«IF attribute.hasMetas»«javaType.toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.rosettaType.typeCallToRType.toJavaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«javaType.toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.rosettaType.typeCallToRType.toJavaType» «builderScope.createUniqueIdentifier(attribute.name)», int _idx);«ENDIF»
				«IF !attribute.isOverriding»
				«javaType.toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toListOrSingleMetaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«javaType.toBuilderType» set«attribute.name.toFirstUpper»(«attribute.toListOrSingleMetaType» «builderScope.createUniqueIdentifier(attribute.name)»);
				«IF attribute.hasMetas»«javaType.toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.rosettaType.typeCallToRType.toPolymorphicListOrSingleJavaType(attribute.multiple)» «builderScope.createUniqueIdentifier(attribute.name)»);
				«javaType.toBuilderType» set«attribute.name.toFirstUpper»Value(«attribute.rosettaType.typeCallToRType.toPolymorphicListOrSingleJavaType(attribute.multiple)» «builderScope.createUniqueIdentifier(attribute.name)»);«ENDIF»
				«ENDIF»
			«ENDIF»
		«ENDFOR»
		'''


	protected def StringConcatenationClient pojoInterfaceDefaultOverridenMethods(JavaClass javaType, GeneratedIdentifier metaDataIdentifier, Collection<Object> interfaces, Data d)
		'''
		@Override
		default «RosettaMetaData»<? extends «javaType»> metaData() {
			return «metaDataIdentifier»;
		}

		@Override
		default Class<? extends «javaType»> getType() {
			return «javaType».class;
		}

		«FOR pt :interfaces.filter(JavaParameterizedType).filter[getBaseType.simpleName=="ReferenceWithMeta" || getBaseType.simpleName=="FieldWithMeta"]»
		@Override
		default Class<«pt.getArguments.head»> getValueType() {
			return «pt.getArguments.head».class;
		}
        «ENDFOR»

		«d.processMethod»
        '''


	protected def StringConcatenationClient pojoInterfaceGetterMethods(JavaClass javaType, JavaClass metaType, GeneratedIdentifier metaDataIdentifier, Data d) '''
		«FOR attribute : d.expandedAttributes»
			«javadoc(attribute.definition, attribute.docReferences, null)»
			«attribute.toMultiMetaOrRegularJavaType» get«attribute.name.toFirstUpper»();
		«ENDFOR»
		'''

	protected def StringConcatenationClient pojoInterfaceBuilderMethods(JavaClass javaType, Data d) '''
			«d.name» build();

			«javaType.toBuilderType» toBuilder();

			static «javaType.toBuilderType» builder() {
				return new «javaType.toBuilderImplType»();
			}
		'''


	def dispatch buildify(Object object) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	def dispatch buildify(Class<?> clazz) {
		new JavaClass(DottedPath.splitOnDots(clazz.packageName), clazz.simpleName+"."+clazz.simpleName+"Builder")
	}
	def dispatch buildify(JavaParameterizedType clazz) {
		val builderType = new JavaClass(clazz.getBaseType.packageName, clazz.getBaseType.simpleName+"."+clazz.getBaseType.simpleName+"Builder")
		new JavaParameterizedType(builderType, clazz.getArguments)
	}

	def boolean globalKeyRecursive(Data class1) {
		return class1.globalKey || (class1.superType !== null && class1.superType.globalKeyRecursive)
	}

	private def StringConcatenationClient rosettaClass(Data c, JavaScope scope) {
		val expandedAttributes = c.expandedAttributes
		val javaType = new RDataType(c).toJavaType
		'''
		«FOR attribute : expandedAttributes»
			private final «attribute.toMultiMetaOrRegularJavaType» «scope.createIdentifier(attribute, attribute.name)»;
		«ENDFOR»

		protected «javaType»Impl(«javaType.toBuilderType» builder) {
			«IF c.hasSuperType»
				super(builder);
			«ENDIF»
			«FOR attribute : expandedAttributes»
				this.«scope.getIdentifierOrThrow(attribute)» = «attribute.attributeFromBuilder»;
			«ENDFOR»
		}

		«FOR attribute : expandedAttributes»
			@Override
			@«RosettaAttribute»("«attribute.javaAnnotation»")
			public «attribute.toMultiMetaOrRegularJavaType» get«attribute.name.toFirstUpper»() {
				return «scope.getIdentifierOrThrow(attribute)»;
			}
			
		«ENDFOR»
		@Override
		public «c.name» build() {
			return this;
		}

		@Override
		public «javaType.toBuilderType» toBuilder() {
			«javaType.toBuilderType» builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(«javaType.toBuilderType» builder) {
			«IF (c.hasSuperType)»
				super.setBuilderFields(builder);
			«ENDIF»
			«FOR attribute :expandedAttributes»
				«method(Optional, "ofNullable")»(get«attribute.name.toFirstUpper»()).ifPresent(builder::set«attribute.name.toFirstUpper»);
			«ENDFOR»
		}
		'''
	}

	private def StringConcatenationClient attributeFromBuilder(ExpandedAttribute attribute) {
		if(attribute.isDataType || attribute.hasMetas) {
			if (attribute.cardinalityIsListValue)
				'''ofNullable(builder.get«attribute.name.toFirstUpper»()).filter(_l->!_l.isEmpty()).map(«attribute.buildRosettaObject»).orElse(null)'''
			else
				'''ofNullable(builder.get«attribute.name.toFirstUpper»()).map(«attribute.buildRosettaObject»).orElse(null)'''
		} else {
			if (attribute.cardinalityIsSingleValue)
				'''builder.get«attribute.name.toFirstUpper»()«IF attribute.needsBuilder».build()«ENDIF»'''
			else
				'''ofNullable(builder.get«attribute.name.toFirstUpper»()).filter(_l->!_l.isEmpty()).map(«ImmutableList»::copyOf).orElse(null)'''
		}
	}

	private def StringConcatenationClient buildRosettaObject(ExpandedAttribute attribute) {
		if(attribute.cardinalityIsListValue) {
			'''list -> list.stream().filter(«Objects»::nonNull).map(f->f.build()).filter(«Objects»::nonNull).collect(«ImmutableList».toImmutableList())'''
		} else {
			'''f->f.build()'''
		}
	}
	
	private def StringConcatenationClient startComment(String msg) '''
	/*********************** «msg»  ***********************/'''


}