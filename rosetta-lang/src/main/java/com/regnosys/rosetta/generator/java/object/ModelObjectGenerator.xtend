
package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.ImmutableList
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.types.RDataType
import com.rosetta.model.lib.annotations.RosettaAttribute
import com.rosetta.model.lib.annotations.RosettaDataType
import com.rosetta.model.lib.annotations.RuneAttribute
import com.rosetta.model.lib.annotations.RuneDataType
import com.rosetta.model.lib.annotations.RuneMetaType
import com.rosetta.model.lib.meta.RosettaMetaData
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.generated.GeneratedJavaClass
import java.util.List
import java.util.Objects
import java.util.Optional
import javax.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import com.rosetta.model.lib.annotations.RuneScopedAttributeReference
import com.rosetta.model.lib.annotations.RuneScopedAttributeKey

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
		val superInterface = javaType.superPojo
		val extendSuperImpl = superInterface !== null && javaType.ownProperties.forall[isCompatibleWithParent]
		val interfaceScope = scope.classScope(javaType.toString)
		val metaDataIdentifier = interfaceScope.createUniqueIdentifier("metaData");
		val builderScope = interfaceScope.classScope('''«javaType»Builder''')
		val implScope = interfaceScope.classScope('''«javaType»Impl''')
		val modelShortName = javaType.packageName.first
		'''
			«javaType.javadoc»
			@«RosettaDataType»(value="«javaType.rosettaName»", builder=«javaType.toBuilderImplType».class, version="«javaType.version»")
			@«RuneDataType»(value="«javaType.rosettaName»", model="«modelShortName»", builder=«javaType.toBuilderImplType».class, version="«javaType.version»")
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
					«javaType.pojoBuilderInterfaceSetterMethods(javaType, builderScope)»

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
		'''
		«FOR prop : javaType.ownProperties»
			«IF prop.type.isRosettaModelObject»
				«IF !prop.type.isList»
					«prop.toBuilderTypeSingle» «prop.getOrCreateName»();
					@Override
					«prop.toBuilderTypeSingle» «prop.getterName»();
				«ELSE»
					«prop.toBuilderTypeSingle» «prop.getOrCreateName»(int _index);
					@Override
					«List»<? extends «prop.toBuilderTypeSingle»> «prop.getterName»();
				«ENDIF»
			«ENDIF»
		«ENDFOR»
		'''
	}
	protected def StringConcatenationClient pojoBuilderInterfaceSetterMethods(JavaPojoInterface mainType, JavaPojoInterface currentType, JavaScope builderScope) {
		val isMainPojo = mainType == currentType
		val builderType = mainType.toBuilderType
		'''
		«IF currentType.superPojo !== null»«pojoBuilderInterfaceSetterMethods(mainType, currentType.superPojo, builderScope)»«ENDIF»
		«FOR prop : currentType.ownProperties»
			«val setMethodName = "set" + prop.name.toFirstUpper»
			«val setValueMethodName = setMethodName + "Value"»
			«val propType = prop.type»
			«IF !propType.isList»
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «setMethodName»(«propType» «builderScope.methodScope(setMethodName).createUniqueIdentifier(prop.name)»);
				«IF propType instanceof RJavaWithMetaValue»
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «setValueMethodName»(«propType.valueType» «builderScope.methodScope(setValueMethodName).createUniqueIdentifier(prop.name)»);
				«ENDIF»
			«ELSE»
				«val addMethodName = "add" + prop.name.toFirstUpper»
				«val addValueMethodName = addMethodName + "Value"»
				«val itemType = propType.itemType»
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «addMethodName»(«itemType» «builderScope.methodScope(addMethodName).createUniqueIdentifier(prop.name)»);
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «addMethodName»(«itemType» «builderScope.methodScope(addMethodName).createUniqueIdentifier(prop.name)», int _idx);
				«IF itemType instanceof RJavaWithMetaValue»
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «addValueMethodName»(«itemType.valueType» «builderScope.methodScope(addValueMethodName).createUniqueIdentifier(prop.name)»);
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «addValueMethodName»(«itemType.valueType» «builderScope.methodScope(addValueMethodName).createUniqueIdentifier(prop.name)», int _idx);
				«ENDIF»
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «addMethodName»(«propType» «builderScope.methodScope(addMethodName).createUniqueIdentifier(prop.name)»);
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «setMethodName»(«propType» «builderScope.methodScope(setMethodName).createUniqueIdentifier(prop.name)»);
				«IF itemType instanceof RJavaWithMetaValue»
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «addValueMethodName»(«LIST.wrapExtends(itemType.valueType)» «builderScope.methodScope(addValueMethodName).createUniqueIdentifier(prop.name)»);
				«IF !isMainPojo»@Override«ENDIF»
				«builderType» «setValueMethodName»(«LIST.wrapExtends(itemType.valueType)» «builderScope.methodScope(setValueMethodName).createUniqueIdentifier(prop.name)»);
				«ENDIF»
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
		@«RuneAttribute»("@type")
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
			@«RuneAttribute»("«prop.javaRuneAnnotation»")
			«IF prop.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
			«IF prop.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
			«IF prop.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
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
		if(prop.type.isRosettaModelObject) {
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