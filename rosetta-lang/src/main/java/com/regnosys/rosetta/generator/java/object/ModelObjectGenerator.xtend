
package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.ImmutableList
import com.regnosys.rosetta.generator.GeneratedIdentifier
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
import java.util.List
import java.util.Objects
import java.util.Optional
import jakarta.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.rosetta.model.lib.annotations.RuneScopedAttributeReference
import com.rosetta.model.lib.annotations.RuneScopedAttributeKey
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope
import com.rosetta.model.lib.annotations.RosettaIgnore
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RObjectFactory
import com.regnosys.rosetta.generator.java.types.JavaPojoBuilderInterface
import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.*
import com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType
import com.rosetta.util.types.JavaType
import java.util.function.Function
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope
import com.regnosys.rosetta.generator.java.types.JavaPojoImpl

class ModelObjectGenerator extends RObjectJavaClassGenerator<RDataType, JavaPojoInterface> {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension ModelObjectBuilderGenerator
	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil
	@Inject extension TypeCoercionService
	@Inject extension RObjectFactory
	
	override protected streamObjects(RosettaModel model) {
		model.elements.stream.filter[it instanceof Data].map[it as Data].map[buildRDataType]
	}
	override protected createTypeRepresentation(RDataType t) {
		t.toJavaReferenceType
	}
	override protected registerMethods(RDataType t, JavaPojoInterface javaType, JavaClassScope pojoScope) {
		for (prop : javaType.ownProperties) {
			registerOperation(prop, GETTER, pojoScope)
			
			val propType = prop.type
			registerOperation(prop, SETTER, pojoScope)
			if (propType instanceof RJavaWithMetaValue) {
				registerOperation(prop, SETTER_VALUE, pojoScope)
			}
			if (propType.isList) {
				registerOperation(prop, ADDER, pojoScope)
				if (propType instanceof RJavaWithMetaValue) {
					registerOperation(prop, ADDER_VALUE, pojoScope)
				}
			}
		}
	}
	private def void registerOperation(JavaPojoProperty prop, JavaPojoPropertyOperationType operationType, JavaClassScope pojoScope) {
		val opKey = prop.getOperationKey(operationType)
		pojoScope.createIdentifier(opKey, opKey.desiredOperationName)
	}
	override protected generate(RDataType t, JavaPojoInterface javaType, String version, JavaClassScope pojoScope) {
		classBody(javaType, pojoScope, javaType.toJavaMetaDataClass, version)
	}

	def StringConcatenationClient classBody(JavaPojoInterface javaType, JavaClassScope pojoScope, JavaClass<?> metaType, String version) {
		val superInterface = javaType.superPojo
		val extendSuperImpl = superInterface !== null && javaType.ownProperties.forall[isCompatibleTypeWithParent]
		val metaDataIdentifier = pojoScope.createUniqueIdentifier("metaData");
		val builderInterface = javaType.toBuilderInterface
		val builderScope = pojoScope.createNestedClassScopeAndRegisterIdentifier(builderInterface)
		val implClass = javaType.toImplClass
		val implScope = pojoScope.createNestedClassScopeAndRegisterIdentifier(implClass)
		val builderImplClass = javaType.toBuilderImplClass
		val modelShortName = javaType.packageName.first
		'''
			«javaType.javadoc»
			@«RosettaDataType»(value="«javaType.rosettaName»", builder=«builderImplClass».class, version="«javaType.version»")
			@«RuneDataType»(value="«javaType.rosettaName»", model="«modelShortName»", builder=«builderImplClass».class, version="«javaType.version»")
			public «javaType.asInterfaceDeclaration» {

				«metaType» «metaDataIdentifier» = new «metaType»();

				«startComment('Getter Methods')»
				«pojoInterfaceGetterMethods(javaType, pojoScope)»

				«startComment('Build Methods')»
				«pojoInterfaceBuilderMethods(javaType)»

				«startComment('Utility Methods')»
				«pojoInterfaceDefaultOverridenMethods(javaType, metaDataIdentifier, pojoScope)»

				«startComment('Builder Interface')»
				«builderInterface.asInterfaceDeclaration» {
					«javaType.pojoBuilderInterfaceGetterMethods(builderScope)»
					«javaType.pojoBuilderInterfaceSetterMethods(builderInterface, javaType, builderScope)»

					«javaType.builderProcessMethod(builderScope)»

					«builderInterface» prune();
				}

				«startComment('''Immutable Implementation of «javaType.simpleName»''')»
				«implClass.asClassDeclaration» {
					«javaType.rosettaClass(implClass, builderInterface, extendSuperImpl, implScope)»

					«javaType.boilerPlate(extendSuperImpl, implScope)»
				}

				«startComment('''Builder Implementation of «javaType.simpleName»''')»
				«javaType.builderClass(interfaceScope)»
			}
		'''
	}

	protected def StringConcatenationClient pojoBuilderInterfaceGetterMethods(JavaPojoInterface javaType, JavaClassScope builderScope) {
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
	// workaround to put a StringConcatenationClient inside a lambda without having Xtend convert it to a string.
	private def StringConcatenationClient scc(StringConcatenationClient arg) {
		arg
	}
	private def StringConcatenationClient setterMethod(JavaPojoProperty prop, JavaType mainBuilderType, JavaPojoPropertyOperationType operationType, boolean isOverride, JavaClassScope builderScope, (JavaMethodScope) => StringConcatenationClient computeParameters) {
		val opKey = prop.getOperationKey(operationType)
		val scope = builderScope.createOverloadMethodScope(opKey, opKey.desiredOperationName)
		val methodName = scope.getIdentifierOrThrow(opKey)
		'''
		«IF isOverride»@Override«ENDIF»
		«mainBuilderType» «methodName»(«computeParameters.apply(scope)»);
		'''
	}
	protected def StringConcatenationClient pojoBuilderInterfaceSetterMethods(JavaPojoInterface mainType, JavaPojoBuilderInterface mainBuilderType, JavaPojoInterface currentType, JavaClassScope builderScope) {
		val isMainPojo = mainType == currentType
		'''
		«IF currentType.superPojo !== null»«pojoBuilderInterfaceSetterMethods(mainType, mainBuilderType, currentType.superPojo, builderScope)»«ENDIF»
		«FOR prop : currentType.ownProperties»
			«val propType = prop.type»
			«IF !propType.isList»
				«setterMethod(prop, mainBuilderType, SETTER, !isMainPojo, builderScope, [scope|scc('''«propType» «scope.createUniqueIdentifier(prop.name)»''')])»
				«IF propType instanceof RJavaWithMetaValue»
				«setterMethod(prop, mainBuilderType, SETTER_VALUE, !isMainPojo, builderScope, [scope|scc('''«propType.valueType» «scope.createUniqueIdentifier(prop.name)»''')])»
				«ENDIF»
			«ELSE»
				«val itemType = propType.itemType»
				«setterMethod(prop, mainBuilderType, ADDER, !isMainPojo, builderScope, [scope|scc('''«itemType» «scope.createUniqueIdentifier(prop.name)»''')])»
				«setterMethod(prop, mainBuilderType, ADDER, !isMainPojo, builderScope, [scope|scc('''«itemType» «scope.createUniqueIdentifier(prop.name)», int _idx''')])»
				«IF itemType instanceof RJavaWithMetaValue»
				«setterMethod(prop, mainBuilderType, ADDER_VALUE, !isMainPojo, builderScope, [scope|scc('''«itemType.valueType» «scope.createUniqueIdentifier(prop.name)»''')])»
				«setterMethod(prop, mainBuilderType, ADDER_VALUE, !isMainPojo, builderScope, [scope|scc('''«itemType.valueType» «scope.createUniqueIdentifier(prop.name)», int _idx''')])»
				«ENDIF»
				«setterMethod(prop, mainBuilderType, ADDER, !isMainPojo, builderScope, [scope|scc('''«propType» «scope.createUniqueIdentifier(prop.name)»''')])»
				«setterMethod(prop, mainBuilderType, SETTER, !isMainPojo, builderScope, [scope|scc('''«propType» «scope.createUniqueIdentifier(prop.name)»''')])»
				«IF itemType instanceof RJavaWithMetaValue»
				«setterMethod(prop, mainBuilderType, ADDER_VALUE, !isMainPojo, builderScope, [scope|scc('''«LIST.wrapExtends(itemType.valueType)» «scope.createUniqueIdentifier(prop.name)»''')])»
				«setterMethod(prop, mainBuilderType, SETTER_VALUE, !isMainPojo, builderScope, [scope|scc('''«LIST.wrapExtends(itemType.valueType)» «scope.createUniqueIdentifier(prop.name)»''')])»
				«ENDIF»
			«ENDIF»
		«ENDFOR»
		'''
	}


	protected def StringConcatenationClient pojoInterfaceDefaultOverridenMethods(JavaPojoInterface javaType, GeneratedIdentifier metaDataIdentifier, JavaClassScope pojoScope)
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

		«javaType.processMethod(pojoScope)»
        '''


	protected def StringConcatenationClient pojoInterfaceGetterMethods(JavaPojoInterface javaType, JavaClassScope pojoScope) {
		'''
		«FOR prop : javaType.ownProperties»
			«prop.getJavadoc»
			«IF prop.getterOverridesParentGetter»@Override«ENDIF»
			«prop.getType» «prop.getOperationIdentifier(GETTER, pojoScope)»();
		«ENDFOR»
		'''
	}

	protected def StringConcatenationClient pojoInterfaceBuilderMethods(JavaPojoInterface javaType) {
		'''
			«javaType» build();

			«javaType.toBuilderInterface» toBuilder();

			static «javaType.toBuilderInterface» builder() {
				return new «javaType.toBuilderImplClass»();
			}
		'''
	}

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

	private def StringConcatenationClient rosettaClass(JavaPojoInterface javaType, JavaPojoImpl implType, JavaPojoBuilderInterface builderType, boolean extended, JavaClassScope implScope) {
		val properties = extended ? javaType.ownProperties : javaType.allProperties
		'''
		«FOR prop : properties»
			private final «prop.type» «implScope.createIdentifier(prop, prop.name.toFirstLower)»;
		«ENDFOR»

		protected «implType.simpleName»(«builderType» builder) {
			«IF extended»super(builder);«ENDIF»
			«FOR prop : properties»
				this.«implScope.getIdentifierOrThrow(prop)» = «prop.propertyFromBuilder(implScope)»;
			«ENDFOR»
		}

		«FOR prop : properties»
			«val field = new JavaVariable(implScope.getIdentifierOrThrow(prop), prop.type)»
			@Override
			@«RosettaAttribute»("«prop.javaAnnotation»")
			@«RuneAttribute»("«prop.javaRuneAnnotation»")
			«IF prop.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
			«IF prop.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
			«IF prop.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
			public «prop.type» «prop.getOperationIdentifier(GETTER, implScope)»() «field.completeAsReturn.toBlock»
			
			«IF !extended»«derivedIncompatibleGettersForProperty(field, prop, implScope)»«ENDIF»
		«ENDFOR»
		@Override
		public «javaType» build() {
			return this;
		}

		@Override
		public «builderType» toBuilder() {
			«builderType» builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(«builderType» builder) {
			«IF extended»super.setBuilderFields(builder);«ENDIF»
			«FOR prop : properties»
				«method(Optional, "ofNullable")»(«prop.getOperationIdentifier(GETTER, implScope)»()).ifPresent(builder::«prop.getOperationIdentifier(SETTER, implScope)»);
			«ENDFOR»
		}
		'''
	}
	private def StringConcatenationClient derivedIncompatibleGettersForProperty(JavaExpression originalField, JavaPojoProperty prop, JavaClassScope implScope) {
		val parent = prop.parentProperty
		if (parent === null) {
			return null
		} else if (prop.getterOverridesParentGetter) {
			return derivedIncompatibleGettersForProperty(originalField, parent, implScope)
		}
		val opKey = parent.getOperationKey(GETTER)
		val getterScope = implScope.createOverrideMethodScope(opKey)
		'''
		@Override
		@«RosettaIgnore»
		public «parent.type» «parent.getOperationIdentifier(GETTER, implScope)»() «originalField.addCoercions(parent.type, getterScope.bodyScope).completeAsReturn.toBlock»
		
		«derivedIncompatibleGettersForProperty(originalField, parent, implScope)»
		'''
	}

	private def StringConcatenationClient propertyFromBuilder(JavaPojoProperty prop, JavaClassScope scope) {
		val getterName = prop.getOperationIdentifier(GETTER, scope)
		if(prop.type.isRosettaModelObject) {
			if (prop.type.isList)
				'''ofNullable(builder.«getterName»()).filter(_l->!_l.isEmpty()).map(«prop.buildRosettaObjectList»).orElse(null)'''
			else
				'''ofNullable(builder.«getterName»()).map(f->f.build()).orElse(null)'''
		} else {
			if (!prop.type.isList)
				'''builder.«getterName»()'''
			else
				'''ofNullable(builder.«getterName»()).filter(_l->!_l.isEmpty()).map(«ImmutableList»::copyOf).orElse(null)'''
		}
	}

	private def StringConcatenationClient buildRosettaObjectList(JavaPojoProperty prop) {
		'''list -> list.stream().filter(«Objects»::nonNull).map(f->f.build()).filter(«Objects»::nonNull).collect(«ImmutableList».toImmutableList())'''
	}
	
	private def StringConcatenationClient startComment(String msg) '''
	/*********************** «msg»  ***********************/'''
	


}