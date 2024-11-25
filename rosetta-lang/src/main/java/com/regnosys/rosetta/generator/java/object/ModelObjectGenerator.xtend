
package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.ImmutableList
import javax.inject.Inject
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.types.RDataType
import com.rosetta.model.lib.annotations.RosettaAttribute
import com.rosetta.model.lib.annotations.RosettaDataType
import com.rosetta.model.lib.meta.RosettaMetaData
import com.rosetta.util.types.JavaClass
import java.util.List
import java.util.Objects
import java.util.Optional
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import com.rosetta.util.types.generated.GeneratedJavaClass
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable

class ModelObjectGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension ModelObjectBuilderGenerator
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil
	@Inject extension TypeCoercionService

	def generate(RootPackage root, IFileSystemAccess2 fsa, RDataType t, String version) {
		fsa.generateFile(root.child(t.name + '.java').withForwardSlashes,
			generateRosettaClass(root, t, version))
	}

	private def generateRosettaClass(RootPackage root, RDataType t, String version) {
		val scope = new JavaScope(root)
		val javaType = t.toJavaReferenceType
		buildClass(root, javaType.classBody(scope, new GeneratedJavaClass<Object>(root.meta, t.name+'Meta', Object), version), scope)
	}

	def StringConcatenationClient classBody(JavaPojoInterface javaType, JavaScope scope, JavaClass<?> metaType, String version) {
		val superInterface = javaType.interfaces.head
		val extendSuperImpl = superInterface instanceof JavaPojoInterface && javaType.ownProperties.forall[isCompatibleWithParent]
		val interfaceScope = scope.classScope(javaType.toString)
		val metaDataIdentifier = interfaceScope.createUniqueIdentifier("metaData");
		val builderScope = interfaceScope.classScope('''«javaType»Builder''')
		val implScope = interfaceScope.classScope('''«javaType»Impl''')
		'''
			«javaType.javadoc»
			@«RosettaDataType»(value="«javaType.rosettaName»", builder=«javaType.toBuilderImplType».class, version="«javaType.version»")
			public interface «javaType» extends «implementsClause(javaType)» {

				«metaType» «metaDataIdentifier» = new «metaType»();

				«startComment('Getter Methods')»
				«pojoInterfaceGetterMethods(javaType)»

				«startComment('Build Methods')»
				«pojoInterfaceBuilderMethods(javaType)»

				«startComment('Utility Methods')»
				«pojoInterfaceDefaultOverridenMethods(javaType, metaDataIdentifier)»

				«startComment('Builder Interface')»
				interface «javaType»Builder extends «javaType»«FOR inter : javaType.interfaces BEFORE ', ' SEPARATOR ', '»«inter.toBuilderType»«ENDFOR» {
					«javaType.pojoBuilderInterfaceGetterMethods(builderScope)»

					«javaType.builderProcessMethod»

					«javaType.toBuilderType» prune();
				}

				«startComment('''Immutable Implementation of «javaType.simpleName»''')»
				class «javaType»Impl «IF extendSuperImpl»extends «superInterface.toImplType» «ENDIF»implements «javaType» {
					«javaType.rosettaClass(extendSuperImpl, implScope)»

					«javaType.boilerPlate(extendSuperImpl, implScope)»
				}

				«startComment('''Builder Implementation of «javaType.simpleName»''')»
				«javaType.builderClass(interfaceScope)»
			}
		'''
	}

	protected def StringConcatenationClient pojoBuilderInterfaceGetterMethods(JavaPojoInterface javaType, JavaScope builderScope) {
		val properties = javaType.ownProperties
		'''
		«FOR prop : properties»
			«IF prop.isRosettaModelObject»
				«IF !prop.type.isList»
					«prop.toBuilderTypeSingle» getOrCreate«prop.name.toFirstUpper»();
					@Override
					«prop.toBuilderTypeSingle» «prop.getterName»();
				«ELSE»
					«prop.toBuilderTypeSingle» getOrCreate«prop.name.toFirstUpper»(int _index);
					@Override
					«List»<? extends «prop.toBuilderTypeSingle»> «prop.getterName»();
				«ENDIF»
			«ENDIF»
		«ENDFOR»
		«FOR prop : javaType.allProperties»
			«IF !prop.type.isList»
				«javaType.toBuilderType» set«prop.name.toFirstUpper»(«prop.type» «builderScope.createUniqueIdentifier(prop.name)»);
				«IF prop.type instanceof RJavaWithMetaValue»«javaType.toBuilderType» set«prop.name.toFirstUpper»Value(«(prop.type as RJavaWithMetaValue).valueType» «builderScope.createUniqueIdentifier(prop.name)»);«ENDIF»
			«ELSE»
				«val itemType = prop.type.itemType»
				«javaType.toBuilderType» add«prop.name.toFirstUpper»(«itemType» «builderScope.createUniqueIdentifier(prop.name)»);
				«javaType.toBuilderType» add«prop.name.toFirstUpper»(«itemType» «builderScope.createUniqueIdentifier(prop.name)», int _idx);
				«IF itemType instanceof RJavaWithMetaValue»«javaType.toBuilderType» add«prop.name.toFirstUpper»Value(«(itemType as RJavaWithMetaValue).valueType» «builderScope.createUniqueIdentifier(prop.name)»);
				«javaType.toBuilderType» add«prop.name.toFirstUpper»Value(«(itemType as RJavaWithMetaValue).valueType» «builderScope.createUniqueIdentifier(prop.name)», int _idx);«ENDIF»
				«javaType.toBuilderType» add«prop.name.toFirstUpper»(«prop.type» «builderScope.createUniqueIdentifier(prop.name)»);
				«javaType.toBuilderType» set«prop.name.toFirstUpper»(«prop.type» «builderScope.createUniqueIdentifier(prop.name)»);
				«IF itemType instanceof RJavaWithMetaValue»«javaType.toBuilderType» add«prop.name.toFirstUpper»Value(«LIST.wrapExtends((itemType as RJavaWithMetaValue).valueType)» «builderScope.createUniqueIdentifier(prop.name)»);
				«javaType.toBuilderType» set«prop.name.toFirstUpper»Value(«LIST.wrapExtends((itemType as RJavaWithMetaValue).valueType)» «builderScope.createUniqueIdentifier(prop.name)»);«ENDIF»
			«ENDIF»
		«ENDFOR»
		'''
	}


	protected def StringConcatenationClient pojoInterfaceDefaultOverridenMethods(JavaPojoInterface javaType, GeneratedIdentifier metaDataIdentifier)
		'''
		@Override
		default «RosettaMetaData»<? extends «javaType»> metaData() {
			return «metaDataIdentifier»;
		}

		@Override
		default Class<? extends «javaType»> getType() {
			return «javaType».class;
		}
		«IF javaType instanceof RJavaWithMetaValue»
		
		@Override
		default Class<«javaType.valueType»> getValueType() {
			return «javaType.valueType».class;
		}
        «ENDIF»

		«javaType.processMethod»
        '''


	protected def StringConcatenationClient pojoInterfaceGetterMethods(JavaPojoInterface javaType) '''
		«FOR prop : javaType.ownProperties»
			«prop.getJavadoc»
			«IF prop.parentProperty !== null && prop.getterName == prop.parentProperty.getterName»@Override«ENDIF»
			«prop.getType» «prop.getGetterName»();
		«ENDFOR»
		'''

	protected def StringConcatenationClient pojoInterfaceBuilderMethods(JavaClass<?> javaType) '''
			«javaType» build();

			«javaType.toBuilderType» toBuilder();

			static «javaType.toBuilderType» builder() {
				return new «javaType.toBuilderImplType»();
			}
		'''

	def boolean globalKeyRecursive(RDataType class1) {
		if (class1.hasMetaAttribute('key')) {
			return true
		}
		val s = class1.superType
		if (s !== null) {
			return globalKeyRecursive(s)
		}
		return false
	}

	private def StringConcatenationClient rosettaClass(JavaPojoInterface javaType, boolean extended, JavaScope scope) {
		val properties = extended ? javaType.ownProperties : javaType.allProperties
		'''
		«FOR prop : properties»
			private final «prop.type» «scope.createIdentifier(prop, prop.name.toFirstLower)»;
		«ENDFOR»

		protected «javaType»Impl(«javaType.toBuilderType» builder) {
			«IF extended»super(builder);«ENDIF»
			«FOR prop : properties»
				this.«scope.getIdentifierOrThrow(prop)» = «prop.propertyFromBuilder»;
			«ENDFOR»
		}

		«FOR prop : properties»
			«val field = new JavaVariable(scope.getIdentifierOrThrow(prop), prop.type)»
			@Override
			@«RosettaAttribute»("«prop.javaAnnotation»")
			public «prop.type» «prop.getterName»() «field.completeAsReturn.toBlock»
			
			«IF !extended»«derivedIncompatibleGettersForProperty(field, prop, scope)»«ENDIF»
		«ENDFOR»
		@Override
		public «javaType» build() {
			return this;
		}

		@Override
		public «javaType.toBuilderType» toBuilder() {
			«javaType.toBuilderType» builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(«javaType.toBuilderType» builder) {
			«IF extended»super.setBuilderFields(builder);«ENDIF»
			«FOR prop : properties»
				«method(Optional, "ofNullable")»(«prop.getterName»()).ifPresent(builder::set«prop.name.toFirstUpper»);
			«ENDFOR»
		}
		'''
	}
	private def StringConcatenationClient derivedIncompatibleGettersForProperty(JavaExpression originalField, JavaPojoProperty prop, JavaScope scope) {
		val parent = prop.parentProperty
		if (parent === null) {
			return null
		} else if (parent.getterName == prop.getterName) {
			return derivedIncompatibleGettersForProperty(originalField, parent, scope)
		}
		val getterScope = scope.methodScope(parent.getterName)
		'''
		@Override
		public «parent.type» «parent.getterName»() «originalField.addCoercions(parent.type, getterScope).completeAsReturn.toBlock»
		
		«derivedIncompatibleGettersForProperty(originalField, parent, scope)»
		'''
	}

	private def StringConcatenationClient propertyFromBuilder(JavaPojoProperty prop) {
		if(prop.isRosettaModelObject) {
			if (prop.type.isList)
				'''ofNullable(builder.«prop.getterName»()).filter(_l->!_l.isEmpty()).map(«prop.buildRosettaObjectList»).orElse(null)'''
			else
				'''ofNullable(builder.«prop.getterName»()).map(f->f.build()).orElse(null)'''
		} else {
			if (!prop.type.isList)
				'''builder.«prop.getterName»()'''
			else
				'''ofNullable(builder.«prop.getterName»()).filter(_l->!_l.isEmpty()).map(«ImmutableList»::copyOf).orElse(null)'''
		}
	}

	private def StringConcatenationClient buildRosettaObjectList(JavaPojoProperty prop) {
		'''list -> list.stream().filter(«Objects»::nonNull).map(f->f.build()).filter(«Objects»::nonNull).collect(«ImmutableList».toImmutableList())'''
	}
	
	private def StringConcatenationClient startComment(String msg) '''
	/*********************** «msg»  ***********************/'''


}