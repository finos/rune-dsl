package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService
import com.regnosys.rosetta.generator.java.statement.JavaEnhancedForLoop
import com.regnosys.rosetta.generator.java.statement.JavaIfThenStatement
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression
import com.regnosys.rosetta.generator.java.statement.builder.JavaIfThenElseBuilder
import com.regnosys.rosetta.generator.java.statement.builder.JavaThis
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.model.lib.annotations.RosettaAttribute
import com.rosetta.model.lib.annotations.RuneAttribute
import com.rosetta.model.lib.annotations.RuneMetaType
import com.rosetta.model.lib.meta.Key
import com.rosetta.model.lib.process.BuilderMerger
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaPrimitiveType
import com.rosetta.util.types.JavaType
import java.util.ArrayList
import java.util.Collections
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.rosetta.model.lib.annotations.RuneScopedAttributeReference
import com.rosetta.model.lib.annotations.RuneScopedAttributeKey

class ModelObjectBuilderGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil typeUtil
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
					«IF !prop.type.isList && prop.type.isRosettaModelObject»
						if («builderScope.getIdentifierOrThrow(prop)»!=null && !«builderScope.getIdentifierOrThrow(prop)».prune().hasData()) «builderScope.getIdentifierOrThrow(prop)» = null;
					«ELSEIF prop.type.isList && prop.type.isRosettaModelObject»
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
				
				«FOR prop : properties.filter[type.isRosettaModelObject]»
					«IF prop.type.isList»
						merger.mergeRosetta(«prop.getterName»(), o.«prop.getterName»(), this::«prop.getOrCreateName»);
					«ELSE»
						merger.mergeRosetta(«prop.getterName»(), o.«prop.getterName»(), this::set«prop.name.toFirstUpper»);
					«ENDIF»
				«ENDFOR»
				
				«FOR prop : properties.filter[!type.isRosettaModelObject]»
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
			@«RuneAttribute»("«prop.javaRuneAnnotation»")
			«IF prop.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
			«IF prop.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
			«IF prop.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
			public «prop.toBuilderTypeExt» «prop.getterName»() «field.completeAsReturn.toBlock»
			«IF prop.type.isRosettaModelObject»
				«IF !prop.type.isList»
					
					@Override
					public «prop.toBuilderTypeSingle» «prop.getOrCreateName»() {
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
				«ELSE»
					
					@Override
					public «prop.toBuilderTypeSingle» «prop.getOrCreateName»(int _index) {

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
			«IF !extended»«derivedIncompatibleGettersForProperty(field, prop, prop, scope)»«ENDIF»
		«ENDFOR»
	'''
	
	private def StringConcatenationClient derivedIncompatibleGettersForProperty(JavaExpression originalField, JavaPojoProperty originalProp, JavaPojoProperty prop, JavaScope scope) {
		val parent = prop.parentProperty
		if (parent === null) {
			return null
		} else if (parent.getterName == prop.getterName) {
			return derivedIncompatibleGettersForProperty(originalField, originalProp, parent, scope)
		}
		val getterScope = scope.methodScope(parent.getterName)
		'''
		
		@Override
		public «parent.toBuilderTypeExt» «parent.getterName»() «
			(if (parent.type.isList) {
				if (originalProp.type.isList) {
					originalField
						.addCoercions(parent.type, getterScope)
						.collapseToSingleExpression(scope)
						.mapExpression[
							val lambdaParam = new JavaVariable(getterScope.lambdaScope.createUniqueIdentifier(parent.type.itemType.simpleName.toFirstLower), parent.type.itemType)
							JavaExpression.from(
								'''«it».stream().map(«lambdaParam» -> «lambdaParam.toBuilder.toLambdaBody»).collect(«Collectors».toList())''',
								parent.toBuilderTypeExt
							)
						]
				} else {
					originalField
						.addCoercions(parent.type.itemType, getterScope)
						.mapExpression[
							if (it == JavaExpression.NULL) {
								JavaExpression.from('''«Collections».<«parent.toBuilderTypeSingle»>emptyList()''', parent.toBuilderTypeExt)
							} else {
								toBuilder.mapExpression[JavaExpression.from('''«Collections».singletonList(«it»)''', parent.toBuilderTypeExt)]
							}
						]
				}
			} else {
				originalField
					.addCoercions(parent.type, getterScope)
					.mapExpressionIfNotNull[toBuilder]
			}).completeAsReturn.toBlock»
		«IF parent.type.isRosettaModelObject»
			«IF !parent.type.isList»
				
				@Override
				public «parent.toBuilderTypeSingle» «parent.getOrCreateName»() «
					JavaExpression.from('''«originalProp.getOrCreateName»()''', originalProp.type.itemType)
						.addCoercions(parent.type.itemType, scope)
						.mapExpressionIfNotNull[toBuilder]
						.completeAsReturn
						.toBlock»
			«ELSE»
				
				@Override
				public «parent.toBuilderTypeSingle» «parent.getOrCreateName»(int _index) «
					JavaExpression.from('''«originalProp.getOrCreateName»(«IF originalProp.type.isList»_index«ENDIF»)''', originalProp.type.itemType)
						.addCoercions(parent.type.itemType, scope)
						.mapExpressionIfNotNull[toBuilder]
						.completeAsReturn
						.toBlock»
			«ENDIF»
		«ENDIF»
		«derivedIncompatibleGettersForProperty(originalField, originalProp, parent, scope)»
		'''
	}
	
	
	private def StringConcatenationClient setters(JavaPojoInterface javaType, JavaScope scope)
		'''
		«FOR prop : javaType.allProperties»
			
			«doSetter(javaType, prop, prop, scope)»
		«ENDFOR»
		'''
	
	private def StringConcatenationClient doSetter(JavaPojoInterface javaType, JavaPojoProperty mainProp, JavaPojoProperty currentProp, JavaScope scope) {
		val builderType = javaType.toBuilderType
		val mainPropType = mainProp.type
		val propType = currentProp.type
		val addMethodName = "add" + currentProp.name.toFirstUpper
		val addValueMethodName = addMethodName + "Value"
		val setMethodName = "set" + currentProp.name.toFirstUpper
		val setValueMethodName = "set" + currentProp.name.toFirstUpper + "Value"
		val isMainProp = mainProp == currentProp
		val field = scope.getIdentifierOrThrow(mainProp)
		val thisExpr = new JavaThis(builderType)
		'''
		«IF propType.isList»
			«val itemType = propType.itemType»
			«val mainItemType = mainPropType.itemType»
			«val addMethodScope = scope.methodScope(addMethodName)»
			«val addMethodArg = new JavaVariable(addMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower), itemType)»
			@Override
			«IF isMainProp»
				@«RosettaAttribute»("«currentProp.javaAnnotation»")
				@«RuneAttribute»("«currentProp.javaRuneAnnotation»")
				«IF currentProp.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
				«IF currentProp.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
				«IF currentProp.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
			«ENDIF»
			public «builderType» «addMethodName»(«itemType» «addMethodArg») «
				(if (isMainProp) {
					new JavaIfThenStatement(
						JavaExpression.from('''«addMethodArg» != null''', JavaPrimitiveType.BOOLEAN),
						JavaExpression.from('''this.«field».add(«addMethodArg.toBuilder»)''', JavaPrimitiveType.VOID)
							.completeAsExpressionStatement
					).append(thisExpr)
				} else {
					addMethodArg
						.addCoercions(mainItemType, false, addMethodScope)
						.collapseToSingleExpression(addMethodScope)
						.mapExpression[
							if (mainPropType.isList) {
								JavaExpression.from('''«addMethodName»(«it»)''', builderType)
							} else {
								JavaExpression.from('''«setMethodName»(«it»)''', builderType)
							}
						]
				}).completeAsReturn.toBlock
				»
			
			«val indexedAddMethodScope = scope.methodScope(addMethodName)»
			«val indexedAddMethodArg = new JavaVariable(indexedAddMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower), itemType)»
			@Override
			public «builderType» «addMethodName»(«itemType» «indexedAddMethodArg», int _idx) «
				(if (isMainProp) {
					JavaExpression.from('''getIndex(this.«field», _idx, () -> «indexedAddMethodArg.toBuilder»)''', JavaPrimitiveType.VOID)
						.completeAsExpressionStatement
						.append(thisExpr)
				} else {
					indexedAddMethodArg
						.addCoercions(mainItemType, false, addMethodScope)
						.collapseToSingleExpression(indexedAddMethodScope)
						.mapExpression[
							if (mainPropType.isList) {
								JavaExpression.from('''«addMethodName»(«it», _idx)''', builderType)
							} else {
								JavaExpression.from('''«setMethodName»(«it»)''', builderType)
							}
						]
				}).completeAsReturn.toBlock
				»
			«IF itemType instanceof RJavaWithMetaValue»
			«val valueType = itemType.valueType»
			«val mainValueType = mainItemType instanceof RJavaWithMetaValue ? mainItemType.valueType : mainItemType»
			
			«val addValueMethodScope = scope.methodScope(addValueMethodName)»
			«val addValueMethodArg = new JavaVariable(addValueMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower), valueType)»
			@Override
			public «builderType» «addValueMethodName»(«valueType» «addValueMethodArg») «
				(if (isMainProp) {
					JavaExpression.from('''this.«currentProp.getOrCreateName»(-1).setValue(«addValueMethodArg.toBuilder»)''', JavaPrimitiveType.VOID)
						.completeAsExpressionStatement
						.append(thisExpr)
				} else {
					addValueMethodArg
						.addCoercions(mainValueType, false, addValueMethodScope)
						.collapseToSingleExpression(addValueMethodScope)
						.mapExpression[
							if (mainPropType.isList) {
								JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«addValueMethodName»«ELSE»«addMethodName»«ENDIF»(«it»)''', builderType)
							} else {
								JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«setValueMethodName»«ELSE»«setMethodName»«ENDIF»(«it»)''', builderType)
							}
						]
				}).completeAsReturn.toBlock
				»

			«val indexedAddValueMethodScope = scope.methodScope(addValueMethodName)»
			«val indexedAddValueMethodArg = new JavaVariable(indexedAddValueMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower), valueType)»
			@Override
			public «builderType» «addValueMethodName»(«valueType» «addValueMethodArg», int _idx) «
				(if (isMainProp) {
					JavaExpression.from('''this.«currentProp.getOrCreateName»(_idx).setValue(«indexedAddValueMethodArg.toBuilder»)''', JavaPrimitiveType.VOID)
						.completeAsExpressionStatement
						.append(thisExpr)
				} else {
					indexedAddValueMethodArg
						.addCoercions(mainValueType, false, indexedAddValueMethodScope)
						.collapseToSingleExpression(indexedAddValueMethodScope)
						.mapExpression[
							if (mainPropType.isList) {
								JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«addValueMethodName»«ELSE»«addMethodName»«ENDIF»(«it», _idx)''', builderType)
							} else {
								JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«setValueMethodName»«ELSE»«setMethodName»«ENDIF»(«it»)''', builderType)
							}
						]
				}).completeAsReturn.toBlock
				»
			«ENDIF»
			
			«val addMultiMethodScope = scope.methodScope(addMethodName)»
			«val addMultiMethodArg = new JavaVariable(addMultiMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower + "s"), propType)»
			@Override 
			public «builderType» «addMethodName»(«propType» «addMultiMethodArg») «
				(if (isMainProp) {
					val forLoopId = addMultiMethodScope.createUniqueIdentifier("toAdd")
					val forLoopVar = new JavaVariable(forLoopId, itemType)
					new JavaIfThenStatement(
						JavaExpression.from('''«addMultiMethodArg» != null''', JavaPrimitiveType.BOOLEAN),
						new JavaEnhancedForLoop(true, itemType, forLoopId, addMultiMethodArg,
							JavaExpression.from('''this.«field».add(«forLoopVar.toBuilder»)''', JavaPrimitiveType.VOID)
								.completeAsExpressionStatement
						)
					).append(thisExpr)
				} else {
					addMultiMethodArg
						.addCoercions(mainPropType, false, addMultiMethodScope)
						.collapseToSingleExpression(addMultiMethodScope)
						.mapExpression[
							if (mainPropType.isList) {
								JavaExpression.from('''«addMethodName»(«it»)''', builderType)
							} else {
								JavaExpression.from('''«setMethodName»(«it»)''', builderType)
							}
						]
				}).completeAsReturn.toBlock
				»
			
			«val setMultiMethodScope = scope.methodScope(setMethodName)»
			«val setMultiMethodArg = new JavaVariable(setMultiMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower + "s"), propType)»
			@Override 
			«IF isMainProp»
			@«RuneAttribute»("«currentProp.javaRuneAnnotation»")
			«IF currentProp.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
			«IF currentProp.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
			«IF currentProp.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
			«ENDIF»
			public «builderType» «setMethodName»(«propType» «setMultiMethodArg») «
				(if (isMainProp) {
					new JavaIfThenElseBuilder(
						JavaExpression.from('''«setMultiMethodArg» == null''', JavaPrimitiveType.BOOLEAN),
						JavaExpression.from('''this.«field» = new «ArrayList»<>()''', JavaPrimitiveType.VOID),
						JavaExpression.from('''
							this.«field» = «setMultiMethodArg».stream()
								«IF propType.isRosettaModelObject».map(_a->_a.toBuilder())«ENDIF»
								.collect(«Collectors».toCollection(()->new ArrayList<>()))''', JavaPrimitiveType.VOID),
						typeUtil
					).completeAsExpressionStatement
						.append(thisExpr)
				} else {
					setMultiMethodArg
						.addCoercions(mainPropType, false, setMultiMethodScope)
						.collapseToSingleExpression(setMultiMethodScope)
						.mapExpression[
							JavaExpression.from('''«setMethodName»(«it»)''', builderType)
						]
				}).completeAsReturn.toBlock
				»
			«IF itemType instanceof RJavaWithMetaValue»
			«val valueType = itemType.valueType»
			«val mainValueType = mainItemType instanceof RJavaWithMetaValue ? mainItemType.valueType : mainItemType»
			
			«val addMultiValueMethodScope = scope.methodScope(addValueMethodName)»
			«val addMultiValueMethodArg = new JavaVariable(addMultiValueMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower + "s"), LIST.wrapExtends(valueType))»
			@Override
			public «builderType» «addValueMethodName»(«addMultiValueMethodArg.expressionType» «addMultiValueMethodArg») «
				(if (isMainProp) {
					val forLoopId = addMultiValueMethodScope.createUniqueIdentifier("toAdd")
					val forLoopVar = new JavaVariable(forLoopId, itemType)
					new JavaIfThenStatement(
						JavaExpression.from('''«addMultiValueMethodArg» != null''', JavaPrimitiveType.BOOLEAN),
						new JavaEnhancedForLoop(true, valueType, forLoopId, addMultiValueMethodArg,
							JavaExpression.from('''this.«addValueMethodName»(«forLoopVar»)''', JavaPrimitiveType.VOID)
								.completeAsExpressionStatement
						)
					).append(thisExpr)
				} else {
					addMultiValueMethodArg
						.addCoercions(mainPropType.isList ? LIST.wrapExtendsIfNotFinal(mainValueType) : mainValueType, false, addMultiValueMethodScope)
						.collapseToSingleExpression(addMultiValueMethodScope)
						.mapExpression[
							if (mainPropType.isList) {
								JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«addValueMethodName»«ELSE»«addMethodName»«ENDIF»(«it»)''', builderType)
							} else {
								JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«setValueMethodName»«ELSE»«setMethodName»«ENDIF»(«it»)''', builderType)
							}
						]
				}).completeAsReturn.toBlock
				»
			
			«val setMultiValueMethodScope = scope.methodScope(setValueMethodName)»
			«val setMultiValueMethodArg = new JavaVariable(setMultiValueMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower + "s"), LIST.wrapExtends(valueType))»
			@Override
			public «builderType» «setValueMethodName»(«setMultiValueMethodArg.expressionType» «setMultiValueMethodArg») «
				(if (isMainProp) {
					JavaExpression.from('''this.«field».clear()''', JavaPrimitiveType.VOID).completeAsExpressionStatement
						.append(new JavaIfThenStatement(
							JavaExpression.from('''«setMultiValueMethodArg» != null''', JavaPrimitiveType.BOOLEAN),
							JavaExpression.from('''«setMultiValueMethodArg».forEach(this::«addValueMethodName»)''', JavaPrimitiveType.VOID)
								.completeAsExpressionStatement
						)).append(thisExpr)
				} else {
					setMultiValueMethodArg
						.addCoercions(mainPropType.isList ? LIST.wrapExtendsIfNotFinal(mainValueType) : mainValueType, false, setMultiValueMethodScope)
						.collapseToSingleExpression(setMultiValueMethodScope)
						.mapExpression[
							JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«setValueMethodName»«ELSE»«setMethodName»«ENDIF»(«it»)''', builderType)
						]
				}).completeAsReturn.toBlock
				»
			«ENDIF»
		«ELSE»
			«val setMethodScope = scope.methodScope(setMethodName)»
			«val setMethodArg = new JavaVariable(setMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower), propType)»
			@Override
			«IF isMainProp»
			@«RosettaAttribute»("«currentProp.javaAnnotation»")
			@«RuneAttribute»("«currentProp.javaRuneAnnotation»")
			«IF currentProp.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
			«IF currentProp.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
			«IF currentProp.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
			«ENDIF»
			public «builderType» «setMethodName»(«propType» «setMethodArg») «
				(if (isMainProp) {
					JavaExpression.from('''this.«field» = «setMethodArg» == null ? null : «setMethodArg.toBuilder»''', JavaPrimitiveType.VOID)
						.completeAsExpressionStatement
						.append(thisExpr)
				} else {
					setMethodArg
						.addCoercions(mainPropType, false, setMethodScope)
						.collapseToSingleExpression(setMethodScope)
						.mapExpression[
							JavaExpression.from('''«setMethodName»(«it»)''', builderType)
						]
				}).completeAsReturn.toBlock
				»
			«IF propType instanceof RJavaWithMetaValue»
			«val valueType = propType.valueType»
			«val mainValueType = mainPropType instanceof RJavaWithMetaValue ? mainPropType.valueType : mainPropType»
			
			«val setValueMethodScope = scope.methodScope(setValueMethodName)»
			«val setValueMethodArg = new JavaVariable(setValueMethodScope.createUniqueIdentifier(currentProp.name.toFirstLower), valueType)»
			@Override
			public «builderType» «setValueMethodName»(«valueType» «setValueMethodArg») «
				(if (isMainProp) {
					JavaExpression.from('''this.«currentProp.getOrCreateName»().setValue(«setValueMethodArg»)''', JavaPrimitiveType.VOID)
						.completeAsExpressionStatement
						.append(thisExpr)
				} else {
					setValueMethodArg
						.addCoercions(mainValueType, false, setValueMethodScope)
						.collapseToSingleExpression(setValueMethodScope)
						.mapExpression[
							JavaExpression.from('''«IF mainPropType instanceof RJavaWithMetaValue»«setValueMethodName»«ELSE»«setMethodName»«ENDIF»(«it»)''', builderType)
						]
				}).completeAsReturn.toBlock
				»
			«ENDIF»
		«ENDIF»
		«IF currentProp.parentProperty !== null»
		
		«doSetter(javaType, mainProp, currentProp.parentProperty, scope)»
		«ENDIF»
		'''
	}
	
	private def hasData(Iterable<JavaPojoProperty> properties, boolean extended) '''
		@Override
		public boolean hasData() {
			«IF extended»if (super.hasData()) return true;«ENDIF»
			«FOR prop : properties.filter[name!="meta"]»
				«IF prop.type.isList»
					«IF prop.type.isValueRosettaModelObject»
						if («prop.getterName»()!=null && «prop.getterName»().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
					«ELSE»
						if («prop.getterName»()!=null && !«prop.getterName»().isEmpty()) return true;
					«ENDIF»
				«ELSEIF prop.type.isValueRosettaModelObject»
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
		if (prop.type.isList) (prop.type.isRosettaModelObject ? LIST.wrapExtends(prop.toBuilderTypeSingle) : LIST.wrap(prop.toBuilderTypeSingle))
		else prop.toBuilderTypeSingle
	}

	def JavaType toBuilderTypeSingle(JavaPojoProperty prop) {
		val itemType = prop.type.itemType
		if (prop.type.isRosettaModelObject) {
			(itemType as JavaClass<?>).toBuilderType
		} else {
			itemType
		}
	}
	
		
	private def JavaExpression toBuilder(JavaExpression expr) {
		val t = expr.expressionType
		if(t.isRosettaModelObject) {
			JavaExpression.from('''«expr».toBuilder()''', (t as JavaClass<?>).toBuilderType)
		} else {
			expr
		}
	}
}