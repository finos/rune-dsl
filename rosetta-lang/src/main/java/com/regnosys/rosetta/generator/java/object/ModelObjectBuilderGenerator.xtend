package com.regnosys.rosetta.generator.java.object

import com.rosetta.model.lib.meta.Key
import com.rosetta.model.lib.process.BuilderMerger
import java.util.ArrayList
import java.util.function.Consumer
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import javax.inject.Inject
import com.rosetta.model.lib.annotations.RosettaAttribute
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty
import com.rosetta.util.types.JavaType
import com.rosetta.util.types.JavaClass
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue

class ModelObjectBuilderGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil
	@Inject extension TypeCoercionService

	def StringConcatenationClient builderClass(JavaPojoInterface javaType, JavaScope scope) {
		val superInterface = javaType.interfaces.head
		val extendSuperImpl = superInterface instanceof JavaPojoInterface && javaType.ownProperties.forall[parentProperty === null || parentProperty.type == type]
		val builderScope = scope.classScope('''«javaType»BuilderImpl''')
		val properties = extendSuperImpl ? javaType.ownProperties : javaType.allProperties
		javaType.allProperties.forEach[
			builderScope.createIdentifier(it, it.name.toFirstLower)
		]
		'''
		class «javaType»BuilderImpl«IF extendSuperImpl» extends «superInterface.toBuilderImplType»«ENDIF» implements «javaType.toBuilderType» {
		
			«FOR prop : properties»
				protected «prop.toBuilderType» «builderScope.getIdentifierOrThrow(prop)»«IF prop.type.isList» = new «ArrayList»<>()«ENDIF»;
			«ENDFOR»
		
			«properties.builderGetters(extendSuperImpl, builderScope)»
			«javaType.setters(builderScope)»
			
			@Override
			public «javaType» build() {
				return new «javaType.toImplType»(this);
			}
			
			@Override
			public «javaType.toBuilderType» toBuilder() {
				return this;
			}
		
			@SuppressWarnings("unchecked")
			@Override
			public «javaType.toBuilderType» prune() {
				«IF extendSuperImpl»super.prune();«ENDIF»
				«FOR prop : properties»
					«IF !prop.type.isList && prop.isRosettaModelObject»
						if («builderScope.getIdentifierOrThrow(prop)»!=null && !«builderScope.getIdentifierOrThrow(prop)».prune().hasData()) «builderScope.getIdentifierOrThrow(prop)» = null;
					«ELSEIF prop.type.isList && prop.isRosettaModelObject»
						«builderScope.getIdentifierOrThrow(prop)» = «builderScope.getIdentifierOrThrow(prop)».stream().filter(b->b!=null).<«prop.toBuilderTypeSingle»>map(b->b.prune()).filter(b->b.hasData()).collect(«Collectors».toList());
					«ENDIF»
				«ENDFOR»
				return this;
			}
			
			«properties.hasData(extendSuperImpl)»
		
			«properties.merge(javaType, extendSuperImpl)»
		
			«javaType.builderBoilerPlate(extendSuperImpl, builderScope)»
		}
		'''
	}

	private def StringConcatenationClient merge(Iterable<JavaPojoProperty> properties, JavaPojoInterface javaType, boolean extended) {
		val builderType = javaType.toBuilderType
		'''
			@SuppressWarnings("unchecked")
			@Override
			public «builderType» merge(«RosettaModelObjectBuilder» other, «BuilderMerger» merger) {
				«IF extended»
					super.merge(other, merger);
					
				«ENDIF»
				«builderType» o = («builderType») other;
				
				«FOR prop : properties.filter[isRosettaModelObject]»
					«IF prop.type.isList»
						merger.mergeRosetta(«prop.getterName»(), o.«prop.getterName»(), this::getOrCreate«prop.name.toFirstUpper»);
					«ELSE»
						merger.mergeRosetta(«prop.getterName»(), o.«prop.getterName»(), this::set«prop.name.toFirstUpper»);
					«ENDIF»
				«ENDFOR»
				
				«FOR prop : properties.filter[!isRosettaModelObject]»
					«IF prop.type.isList»
						merger.mergeBasic(«prop.getterName»(), o.«prop.getterName»(), («Consumer»<«prop.type.itemType»>) this::add«prop.name.toFirstUpper»);
					«ELSE»
						merger.mergeBasic(«prop.getterName»(), o.«prop.getterName»(), this::set«prop.name.toFirstUpper»);
					«ENDIF»
				«ENDFOR»
				return this;
			}
		'''
	}

	private def StringConcatenationClient builderGetters(Iterable<JavaPojoProperty> properties, boolean extended, JavaScope scope) '''
		«FOR prop : properties»
			«val field = new JavaVariable(scope.getIdentifierOrThrow(prop), prop.type)»
			@Override
			@«RosettaAttribute»("«prop.javaAnnotation»")
			public «prop.toBuilderTypeExt» «prop.getterName»() «field.completeAsReturn.toBlock»
			
			«IF !extended»«derivedIncompatibleGettersForProperty(field, prop, scope)»«ENDIF»
			«IF prop.isRosettaModelObject»
				«IF !prop.type.isList»
					@Override
					public «prop.toBuilderTypeSingle» getOrCreate«prop.name.toFirstUpper»() {
						«prop.toBuilderTypeSingle» result;
						if («field»!=null) {
							result = «field»;
						}
						else {
							result = «field» = «prop.type».builder();
							«IF prop.hasLocation»
								result.getOrCreateMeta().toBuilder().addKey(«Key».builder().setScope("DOCUMENT"));
							«ENDIF»
						}
						
						return result;
					}
					
					«IF !extended && prop.hasListParent»
					@Override
					public «prop.toBuilderTypeSingle» getOrCreate«prop.name.toFirstUpper»(int _index) {
						return getOrCreate«prop.name.toFirstUpper»();
					}
					
					«ENDIF»
				«ELSE»
					@Override
					public «prop.toBuilderTypeSingle» getOrCreate«prop.name.toFirstUpper»(int _index) {

						if («field»==null) {
							this.«field» = new «ArrayList»<>();
						}
						«prop.toBuilderTypeSingle» result;
						return getIndex(«field», _index, () -> {
									«prop.toBuilderTypeSingle» new«prop.name.toFirstUpper» = «prop.type.itemType».builder();
									«IF prop.hasLocation»
										new«prop.name.toFirstUpper».getOrCreateMeta().addKey(«Key».builder().setScope("DOCUMENT"));
									«ENDIF»
									return new«prop.name.toFirstUpper»;
								});
					}
					
				«ENDIF»
			«ENDIF»
		«ENDFOR»
	'''
	private def boolean hasListParent(JavaPojoProperty prop) {
		val p = prop.parentProperty
		return p !== null && (p.type.isList || hasListParent(p))
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
		public «parent.toBuilderTypeExt» «parent.getterName»() «originalField.addCoercions(parent.toBuilderTypeExt, getterScope).completeAsReturn.toBlock»
		
		«derivedIncompatibleGettersForProperty(originalField, parent, scope)»
		'''
	}
	
	
	private def StringConcatenationClient setters(JavaPojoInterface javaType, JavaScope scope)
		'''
		«FOR prop : javaType.allProperties»
			«doSetter(javaType, prop, scope)»
		«ENDFOR»
		'''
	
	private def StringConcatenationClient doSetter(JavaPojoInterface javaType, JavaPojoProperty prop, JavaScope scope) {
		val builderType = javaType.toBuilderType
		val propType = prop.type
		val field = new JavaVariable(scope.getIdentifierOrThrow(prop), prop.toBuilderType)
		'''
		«IF propType.isList»
			«val itemType = propType.itemType»
			@Override
			@«RosettaAttribute»("«prop.javaAnnotation»")
			public «builderType» add«prop.name.toFirstUpper»(«itemType» «field») {
				if («field»!=null) this.«field».add(«prop.toBuilder(scope)»);
				return this;
			}
			
			@Override
			public «builderType» add«prop.name.toFirstUpper»(«itemType» «field», int _idx) {
				getIndex(this.«field», _idx, () -> «prop.toBuilder(scope)»);
				return this;
			}
			«IF itemType instanceof RJavaWithMetaValue»
			
			@Override
			public «builderType» add«prop.name.toFirstUpper»Value(«itemType.valueType» «field») {
				this.getOrCreate«prop.name.toFirstUpper»(-1).setValue(«field»«IF itemType.isValueRosettaModelObject».toBuilder()«ENDIF»);
				return this;
			}

			@Override
			public «builderType» add«prop.name.toFirstUpper»Value(«itemType.valueType» «field», int _idx) {
				this.getOrCreate«prop.name.toFirstUpper»(_idx).setValue(«field»«IF itemType.isValueRosettaModelObject».toBuilder()«ENDIF»);
				return this;
			}
			«ENDIF»
			
			@Override 
			public «builderType» add«prop.name.toFirstUpper»(«propType» «field»s) {
				if («field»s != null) {
					for («itemType» toAdd : «field»s) {
						this.«field».add(toAdd«IF prop.isRosettaModelObject».toBuilder()«ENDIF»);
					}
				}
				return this;
			}
			
			@Override 
			public «builderType» set«prop.name.toFirstUpper»(«propType» «field»s) {
				if («field»s == null)  {
					this.«field» = new «ArrayList»<>();
				}
				else {
					this.«field» = «field»s.stream()
						«IF prop.isRosettaModelObject».map(_a->_a.toBuilder())«ENDIF»
						.collect(«Collectors».toCollection(()->new ArrayList<>()));
				}
				return this;
			}
			«IF itemType instanceof RJavaWithMetaValue»
			
			@Override
			public «builderType» add«prop.name.toFirstUpper»Value(«LIST.wrapExtends(itemType.valueType)» «field»s) {
				if («field»s != null) {
					for («itemType.valueType» toAdd : «field»s) {
						this.add«prop.name.toFirstUpper»Value(toAdd);
					}
				}
				return this;
			}
			
			@Override
			public «builderType» set«prop.name.toFirstUpper»Value(«LIST.wrapExtends(itemType.valueType)» «field»s) {
				this.«field».clear();
				if («field»s!=null) {
					«field»s.forEach(this::add«prop.name.toFirstUpper»Value);
				}
				return this;
			}
			«ENDIF»
			
		«ELSE»
			@Override
			@«RosettaAttribute»("«prop.javaAnnotation»")
			public «builderType» set«prop.name.toFirstUpper»(«prop.type» «field») {
				this.«field» = «field»==null?null:«prop.toBuilder(scope)»;
				return this;
			}
			«IF propType instanceof RJavaWithMetaValue»
			
			@Override
			public «builderType» set«prop.name.toFirstUpper»Value(«propType.valueType» «field») {
				this.getOrCreate«prop.name.toFirstUpper»().setValue(«field»);
				return this;
			}
			«ENDIF»
		«ENDIF»
		«IF prop.parentProperty !== null»«doCompatibleSetter(javaType, prop, prop.parentProperty, scope)»«ENDIF»
		'''
	}
	private def StringConcatenationClient doCompatibleSetter(JavaPojoInterface javaType, JavaPojoProperty originalProp, JavaPojoProperty parentProp, JavaScope scope) {
		val builderType = javaType.toBuilderType
		val originalPropType = originalProp.type
		val parentPropType = parentProp.type
		val addMethodName = "add" + parentProp.name.toFirstUpper
		val setMethodName = "set" + parentProp.name.toFirstUpper
		'''
		«IF parentPropType.isList»
			«val itemType = parentPropType.itemType»
			«val originalItemType = originalPropType.itemType»
			«val addMethodScope = scope.methodScope(addMethodName)»
			«val addMethodArg = new JavaVariable(addMethodScope.createUniqueIdentifier(parentProp.name.toFirstLower), itemType)»
			«val convertedAddMethodArg = addMethodArg.addCoercions(originalItemType, JavaExpression.NULL, addMethodScope).collapseToSingleExpression(addMethodScope)»
			@Override
			public «builderType» «addMethodName»(«itemType» «addMethodArg») «
				convertedAddMethodArg
					.mapExpression[
						if (originalPropType.isList) {
							JavaExpression.from('''«addMethodName»(«it»)''', builderType)
						} else {
							JavaExpression.from('''«setMethodName»(«it»)''', builderType)
						}
					].completeAsReturn.toBlock
				»
			
			@Override
			public «builderType» «addMethodName»(«itemType» «addMethodArg», int _idx) «
					convertedAddMethodArg
						.mapExpression[
							if (originalPropType.isList) {
								JavaExpression.from('''«addMethodName»(«it», _idx)''', builderType)
							} else {
								JavaExpression.from('''«setMethodName»(«it»)''', builderType)
							}
						].completeAsReturn.toBlock
					»
			«IF itemType instanceof RJavaWithMetaValue»
			«val valueType = itemType.valueType»
			«val originalValueType = originalItemType instanceof RJavaWithMetaValue ? originalItemType.valueType : originalItemType»
			«val addValueMethodScope = scope.methodScope(addMethodName + "Value")»
			«val addValueMethodArg = new JavaVariable(addValueMethodScope.createUniqueIdentifier(parentProp.name.toFirstLower), valueType)»
			«val convertedAddValueMethodArg = addMethodArg.addCoercions(originalValueType, JavaExpression.NULL, addValueMethodScope).collapseToSingleExpression(addValueMethodScope)»
			
			@Override
			public «builderType» «addMethodName»Value(«valueType» «addValueMethodArg») «
				convertedAddValueMethodArg
					.mapExpression[
						if (originalPropType.isList) {
							JavaExpression.from('''«addMethodName»«IF originalItemType instanceof RJavaWithMetaValue»Value«ENDIF»(«it»)''', builderType)
						} else {
							JavaExpression.from('''«setMethodName»«IF originalItemType instanceof RJavaWithMetaValue»Value«ENDIF»(«it»)''', builderType)
						}
					].completeAsReturn.toBlock
				»

			@Override
			public «builderType» «addMethodName»Value(«valueType» «addValueMethodArg», int _idx) «
				convertedAddValueMethodArg
					.mapExpression[
						if (originalPropType.isList) {
							JavaExpression.from('''«addMethodName»«IF originalItemType instanceof RJavaWithMetaValue»Value«ENDIF»(«it», _idx)''', builderType)
						} else {
							JavaExpression.from('''«setMethodName»«IF originalItemType instanceof RJavaWithMetaValue»Value«ENDIF»(«it»)''', builderType)
						}
					].completeAsReturn.toBlock
				»
			«ENDIF»
			«val addMultiMethodScope = scope.methodScope(addMethodName)»
			«val addMultiMethodArg = new JavaVariable(addMultiMethodScope.createUniqueIdentifier(parentProp.name.toFirstLower + "s"), parentPropType)»
			«val convertedAddMultiMethodArg = addMultiMethodArg.addCoercions(originalPropType, JavaExpression.NULL, addMultiMethodScope).collapseToSingleExpression(addMultiMethodScope)»
			
			@Override 
			public «builderType» «addMethodName»(«parentPropType» «addMultiMethodArg») «
				convertedAddMultiMethodArg
					.mapExpression[
						if (originalPropType.isList) {
							JavaExpression.from('''«addMethodName»(«it»)''', builderType)
						} else {
							JavaExpression.from('''«setMethodName»(«it»)''', builderType)
						}
					].completeAsReturn.toBlock
				»
			
			@Override 
			public «builderType» «setMethodName»(«parentPropType» «addMultiMethodArg») «
				convertedAddMultiMethodArg
					.mapExpression[
						JavaExpression.from('''«setMethodName»(«it»)''', builderType)
					].completeAsReturn.toBlock
				»
			«IF itemType instanceof RJavaWithMetaValue»
			«val valueType = itemType.valueType»
			«val originalValueType = originalItemType instanceof RJavaWithMetaValue ? originalItemType.valueType : originalItemType»
			«val addMultiValueMethodScope = scope.methodScope(addMethodName + "Value")»
			«val addMultiValueMethodArg = new JavaVariable(addMultiMethodScope.createUniqueIdentifier(parentProp.name.toFirstLower + "s"), LIST.wrapExtends(valueType))»
			«val convertedAddMultiValueMethodArg = addMultiValueMethodArg.addCoercions(originalPropType.isList ? LIST.wrapExtendsIfNotFinal(originalValueType) : originalValueType, JavaExpression.NULL, addMultiValueMethodScope).collapseToSingleExpression(addMultiValueMethodScope)»
			
			@Override
			public «builderType» «addMethodName»Value(«addMultiValueMethodArg.expressionType» «addMultiValueMethodArg») «
				convertedAddMultiValueMethodArg
					.mapExpression[
						if (originalPropType.isList) {
							JavaExpression.from('''«addMethodName»«IF originalItemType instanceof RJavaWithMetaValue»Value«ENDIF»(«it»)''', builderType)
						} else {
							JavaExpression.from('''«setMethodName»«IF originalItemType instanceof RJavaWithMetaValue»Value«ENDIF»(«it»)''', builderType)
						}
					].completeAsReturn.toBlock
				»
			
			@Override
			public «builderType» «setMethodName»Value(«addMultiValueMethodArg.expressionType» «addMultiValueMethodArg») «
				convertedAddMultiValueMethodArg
					.mapExpression[
						JavaExpression.from('''«setMethodName»«IF originalItemType instanceof RJavaWithMetaValue»Value«ENDIF»(«it»)''', builderType)
					].completeAsReturn.toBlock
				»
			«ENDIF»
		«ELSE»
			«val setMethodScope = scope.methodScope(setMethodName)»
			«val setMethodArg = new JavaVariable(setMethodScope.createUniqueIdentifier(parentProp.name.toFirstLower), parentPropType)»
			«val convertedSetMethodArg = setMethodArg.addCoercions(originalPropType, JavaExpression.NULL, setMethodScope).collapseToSingleExpression(setMethodScope)»
			@Override
			public «builderType» «setMethodName»(«parentPropType» «setMethodArg») «
				convertedSetMethodArg
					.mapExpression[
						JavaExpression.from('''«setMethodName»(«it»)''', builderType)
					].completeAsReturn.toBlock
				»
			«IF parentPropType instanceof RJavaWithMetaValue»
			«val valueType = parentPropType.valueType»
			«val originalValueType = originalPropType instanceof RJavaWithMetaValue ? originalPropType.valueType : originalPropType»
			«val setValueMethodScope = scope.methodScope(setMethodName + "Value")»
			«val setValueMethodArg = new JavaVariable(setMethodScope.createUniqueIdentifier(parentProp.name.toFirstLower), valueType)»
			«val convertedSetValueMethodArg = setMethodArg.addCoercions(originalValueType, JavaExpression.NULL, setValueMethodScope).collapseToSingleExpression(setValueMethodScope)»
			
			@Override
			public «builderType» «setMethodName»Value(«valueType» «setValueMethodArg») «
				convertedSetValueMethodArg
					.mapExpression[
						JavaExpression.from('''«setMethodName»«IF originalPropType instanceof RJavaWithMetaValue»Value«ENDIF»(«it»)''', builderType)
					].completeAsReturn.toBlock
				»
			«ENDIF»
		«ENDIF»
		«IF parentProp.parentProperty !== null»«doCompatibleSetter(javaType, originalProp, parentProp.parentProperty, scope)»«ENDIF»
		'''
	}
	
	
	private def hasData(Iterable<JavaPojoProperty> properties, boolean extended) '''
		@Override
		public boolean hasData() {
			«IF extended»if (super.hasData()) return true;«ENDIF»
			«FOR prop : properties.filter[name!="meta"]»
				«IF prop.type.isList»
					«IF prop.isValueRosettaModelObject»
						if («prop.getterName»()!=null && «prop.getterName»().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
					«ELSE»
						if («prop.getterName»()!=null && !«prop.getterName»().isEmpty()) return true;
					«ENDIF»
				«ELSEIF prop.isValueRosettaModelObject»
					if («prop.getterName»()!=null && «prop.getterName»().hasData()) return true;
				«ELSE»
					if («prop.getterName»()!=null) return true;
				«ENDIF»
			«ENDFOR»
			return false;
		}
	'''

	private def JavaType toBuilderType(JavaPojoProperty prop) {
		if (prop.type.isList) LIST.wrap(prop.toBuilderTypeSingle)
		else prop.toBuilderTypeSingle
	}

	private def JavaType toBuilderTypeExt(JavaPojoProperty prop) {
		if (prop.type.isList) (prop.isRosettaModelObject ? LIST.wrapExtends(prop.toBuilderTypeSingle) : LIST.wrap(prop.toBuilderTypeSingle))
		else prop.toBuilderTypeSingle
	}

	def JavaType toBuilderTypeSingle(JavaPojoProperty prop) {
		val itemType = prop.type.itemType
		if (prop.isRosettaModelObject) {
			(itemType as JavaClass<?>).toBuilderType
		} else {
			itemType
		}
	}
	
		
	private def StringConcatenationClient toBuilder(JavaPojoProperty prop, JavaScope scope) {
		if(prop.isRosettaModelObject) {
			'''«scope.getIdentifierOrThrow(prop)».toBuilder()'''
		} else {
			'''«scope.getIdentifierOrThrow(prop)»'''
		}
	}
}