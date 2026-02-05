package com.regnosys.rosetta.generator.java.object

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
import com.rosetta.model.lib.annotations.RuneIgnore
import com.rosetta.model.lib.meta.Key
import com.rosetta.model.lib.process.BuilderMerger
import com.rosetta.util.types.JavaPrimitiveType
import com.rosetta.util.types.JavaType
import java.util.ArrayList
import java.util.Collections
import java.util.function.Consumer
import java.util.stream.Collectors
import jakarta.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.rosetta.model.lib.annotations.RuneScopedAttributeReference
import com.rosetta.model.lib.annotations.RuneScopedAttributeKey
import com.regnosys.rosetta.generator.java.statement.builder.JavaLiteral
import com.rosetta.model.lib.annotations.RosettaIgnore
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import static com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType.*
import com.regnosys.rosetta.generator.java.types.JavaPojoPropertyOperationType
import com.regnosys.rosetta.generator.java.scoping.JavaMethodScope
import com.regnosys.rosetta.generator.java.types.JavaPojoBuilderInterface
import com.regnosys.rosetta.generator.java.types.JavaPojoBuilderImpl
import com.rosetta.model.lib.annotations.Accessor
import com.rosetta.model.lib.annotations.AccessorType
import com.rosetta.model.lib.annotations.Multi
import com.rosetta.model.lib.annotations.Required

class ModelObjectBuilderGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil typeUtil
	@Inject extension TypeCoercionService
	@Inject IShouldPrune shouldPrune

	def StringConcatenationClient builderClass(JavaPojoInterface javaType, JavaPojoBuilderImpl builderImplClass, JavaClassScope scope) {
		val superPojo = javaType.superPojo
		val extendSuperImpl = superPojo !== null && javaType.ownProperties.forall[parentProperty === null || parentProperty.type == type]
		val builderInterface = javaType.toBuilderInterface
		val properties = extendSuperImpl ? javaType.ownProperties : javaType.allProperties
		javaType.allProperties.forEach[
			scope.createIdentifier(it, it.name.toFirstLower)
		]
		'''
		«builderImplClass.asClassDeclaration» {
		
			«FOR prop : properties»
				protected «prop.toBuilderType» «scope.getIdentifierOrThrow(prop)»«IF prop.type.isList» = new «ArrayList»<>()«ENDIF»;
			«ENDFOR»
			«properties.builderGetters(extendSuperImpl, scope)»
			«javaType.setters(scope)»
			
			@Override
			public «javaType» build() {
				return new «javaType.toImplClass»(this);
			}
			
			@Override
			public «builderInterface» toBuilder() {
				return this;
			}
		
			@SuppressWarnings("unchecked")
			@Override
			public «builderInterface» prune() {
				«IF extendSuperImpl»super.prune();«ENDIF»
				«FOR prop : properties.filter[type.isRosettaModelObject]»
					«IF !prop.type.isList»
						«IF shouldPrune.shouldBePruned(javaType, prop)»
							if («scope.getIdentifierOrThrow(prop)»!=null && !«scope.getIdentifierOrThrow(prop)».prune().hasData()) «scope.getIdentifierOrThrow(prop)» = null;
						«ELSE»
							if («scope.getIdentifierOrThrow(prop)»!=null) «scope.getIdentifierOrThrow(prop)».prune();
						«ENDIF»
					«ELSE»
						«scope.getIdentifierOrThrow(prop)» = «scope.getIdentifierOrThrow(prop)».stream().filter(b->b!=null).<«prop.toBuilderTypeSingle»>map(b->b.prune())«IF shouldPrune.shouldBePruned(javaType, prop)».filter(b->b.hasData())«ENDIF».collect(«Collectors».toList());
					«ENDIF»
				«ENDFOR»
				return this;
			}
			
			«javaType.hasData(properties, extendSuperImpl, scope)»
		
			«properties.merge(builderInterface, extendSuperImpl, scope)»
		
			«javaType.builderBoilerPlate(extendSuperImpl, scope)»
		}
		'''
	}

	private def StringConcatenationClient merge(Iterable<JavaPojoProperty> properties, JavaPojoBuilderInterface builderType, boolean extended, JavaClassScope builderScope) {
		'''
			@SuppressWarnings("unchecked")
			@Override
			public «builderType» merge(«RosettaModelObjectBuilder» other, «BuilderMerger» merger) {
				«IF extended»
					super.merge(other, merger);
				«ENDIF»
				«builderType» o = («builderType») other;
				
				«FOR prop : properties.filter[type.isRosettaModelObject]»
					«val getter = prop.getOperationName(GET)»
					«IF prop.type.isList»
						merger.mergeRosetta(«getter»(), o.«getter»(), this::«prop.getOperationName(GET_OR_CREATE)»);
					«ELSE»
						merger.mergeRosetta(«getter»(), o.«getter»(), this::«prop.getOperationName(SET)»);
					«ENDIF»
				«ENDFOR»
				
				«FOR prop : properties.filter[!type.isRosettaModelObject]»
					«val getter = prop.getOperationName(GET)»
					«IF prop.type.isList»
						merger.mergeBasic(«getter»(), o.«getter»(), («Consumer»<«prop.type.itemType»>) this::«prop.getOperationName(ADD)»);
					«ELSE»
						merger.mergeBasic(«getter»(), o.«getter»(), this::«prop.getOperationName(SET)»);
					«ENDIF»
				«ENDFOR»
				return this;
			}
		'''
	}

	private def StringConcatenationClient builderGetters(Iterable<JavaPojoProperty> properties, boolean extended, JavaClassScope scope) {
		'''
		«FOR prop : properties»
			«val field = new JavaVariable(scope.getIdentifierOrThrow(prop), prop.type)»
			
			@Override
			@«RosettaAttribute»("«prop.javaAnnotation»")
			@«Accessor»(«AccessorType».GETTER)
			«IF prop.isRequired»@«Required»«ENDIF»
			«IF prop.type.isList»@«Multi»«ENDIF»
			@«RuneAttribute»("«prop.javaRuneAnnotation»")
			«IF prop.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
			«IF prop.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
			«IF prop.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
			public «prop.toBuilderTypeExt» «prop.getOperationName(GET)»() «field.completeAsReturn.toBlock»
			«IF prop.type.isRosettaModelObject»
				«val getOrCreateScope = scope.createMethodScope(prop.getOperationName(GET_OR_CREATE))»
				«IF !prop.type.isList»
					«val resultId = getOrCreateScope.bodyScope.createUniqueIdentifier("result")»
					
					@Override
					public «prop.toBuilderTypeSingle» «prop.getOperationName(GET_OR_CREATE)»() {
						«prop.toBuilderTypeSingle» «resultId»;
						if («field»!=null) {
							«resultId» = «field»;
						}
						else {
							«resultId» = «field» = «prop.type».builder();
							«IF prop.hasLocation»
								«resultId».getOrCreateMeta().toBuilder().addKey(«Key».builder().setScope("DOCUMENT"));
							«ENDIF»
						}
						
						return «resultId»;
					}
				«ELSE»
					«val indexId = getOrCreateScope.createUniqueIdentifier("index")»
					«val newObjectId = getOrCreateScope.bodyScope.lambdaScope.createUniqueIdentifier("new" + prop.name.toFirstUpper)»
					
					@Override
					public «prop.toBuilderTypeSingle» «prop.getOperationName(GET_OR_CREATE)»(int «indexId») {
						if («field»==null) {
							this.«field» = new «ArrayList»<>();
						}
						return getIndex(«field», «indexId», () -> {
									«prop.toBuilderTypeSingle» «newObjectId» = «prop.type.itemType».builder();
									«IF prop.hasLocation»
										«newObjectId».getOrCreateMeta().addKey(«Key».builder().setScope("DOCUMENT"));
									«ENDIF»
									return «newObjectId»;
								});
					}
				«ENDIF»
			«ENDIF»
			«IF !extended»«derivedIncompatibleGettersForProperty(field, prop, prop, scope)»«ENDIF»
		«ENDFOR»
		'''
	}
	
	private def StringConcatenationClient derivedIncompatibleGettersForProperty(JavaExpression originalField, JavaPojoProperty originalProp, JavaPojoProperty prop, JavaClassScope scope) {
		val parent = prop.parentProperty
		if (parent === null) {
			return null
		} else if (prop.getterOverridesParentGetter) {
			return derivedIncompatibleGettersForProperty(originalField, originalProp, parent, scope)
		}
		val getterName = parent.getOperationName(GET)
		val getterScope = scope.createMethodScope(getterName)
		val bodyScope = getterScope.bodyScope
		'''
		
		@Override
		@«RosettaIgnore»
		@«RuneIgnore»
		public «parent.toBuilderTypeExt» «getterName»() «
			(if (parent.type.isList) {
				if (originalProp.type.isList) {
					originalField
						.addCoercions(parent.type, bodyScope)
						.collapseToSingleExpression(bodyScope)
						.mapExpression[
							val lambdaParam = new JavaVariable(bodyScope.lambdaScope.createUniqueIdentifier(parent.type.itemType.simpleName.toFirstLower), parent.type.itemType)
							JavaExpression.from(
								'''«it».stream().map(«lambdaParam» -> «lambdaParam.toBuilder.toLambdaBody»).collect(«Collectors».toList())''',
								parent.toBuilderTypeExt
							)
						]
				} else {
					originalField
						.addCoercions(parent.type.itemType, bodyScope)
						.mapExpression[
							if (it == JavaLiteral.NULL) {
								JavaExpression.from('''«Collections».<«parent.toBuilderTypeSingle»>emptyList()''', parent.toBuilderTypeExt)
							} else {
								toBuilder.mapExpression[JavaExpression.from('''«Collections».singletonList(«it»)''', parent.toBuilderTypeExt)]
							}
						]
				}
			} else {
				originalField
					.addCoercions(parent.type, bodyScope)
					.mapExpressionIfNotNull[toBuilder]
			}).completeAsReturn.toBlock»
		«IF parent.type.isRosettaModelObject»
			«val getOrCreateName = parent.getOperationName(GET_OR_CREATE)»
			«val getOrCreateScope = scope.createMethodScope(getOrCreateName)»
			«IF !parent.type.isList»
				
				@Override
				public «parent.toBuilderTypeSingle» «getOrCreateName»() «
					JavaExpression.from('''«originalProp.getOperationName(GET_OR_CREATE)»()''', originalProp.type.itemType)
						.addCoercions(parent.type.itemType, getOrCreateScope.bodyScope)
						.mapExpressionIfNotNull[toBuilder]
						.completeAsReturn
						.toBlock»
			«ELSE»
				«val indexId = getOrCreateScope.createUniqueIdentifier("index")»
				
				@Override
				public «parent.toBuilderTypeSingle» «getOrCreateName»(int «indexId») «
					JavaExpression.from('''«originalProp.getOperationName(GET_OR_CREATE)»(«IF originalProp.type.isList»«indexId»«ENDIF»)''', originalProp.type.itemType)
						.addCoercions(parent.type.itemType, getOrCreateScope.bodyScope)
						.mapExpressionIfNotNull[toBuilder]
						.completeAsReturn
						.toBlock»
			«ENDIF»
		«ENDIF»
		«derivedIncompatibleGettersForProperty(originalField, originalProp, parent, scope)»
		'''
	}
	
	
	private def StringConcatenationClient setters(JavaPojoInterface javaType, JavaClassScope scope)
		'''
		«FOR prop : javaType.allProperties»
			
			«doSetter(javaType, prop, prop, scope)»
		«ENDFOR»
		'''
	// workaround to put a StringConcatenationClient inside a lambda without having Xtend convert it to a string.
	private def StringConcatenationClient scc(StringConcatenationClient arg) {
		arg
	}
	private def StringConcatenationClient setterMethod(JavaPojoProperty prop, JavaType mainBuilderType, JavaPojoPropertyOperationType operationType, JavaClassScope builderScope, (JavaMethodScope) => StringConcatenationClient computeParametersAndBody) {
		val opName = prop.getOperationName(operationType)
		val scope = builderScope.createMethodScope(opName)
		'''
		@Override
		public «mainBuilderType» «opName»«computeParametersAndBody.apply(scope)»
		'''
	}
	private def StringConcatenationClient doSetter(JavaPojoInterface javaType, JavaPojoProperty mainProp, JavaPojoProperty currentProp, JavaClassScope builderScope) {
		val builderType = javaType.toBuilderInterface
		val mainPropType = mainProp.type
		val propType = currentProp.type
		val isMainProp = mainProp == currentProp
		val field = builderScope.getIdentifierOrThrow(mainProp)
		val thisExpr = new JavaThis(builderType)
		'''
		«IF propType.isList»
			«val itemType = propType.itemType»
			«val mainItemType = mainPropType.itemType»
			«IF isMainProp»
				@«RosettaAttribute»("«currentProp.javaAnnotation»")
				@«Accessor»(«AccessorType».ADDER)
				«IF currentProp.isRequired»@«Required»«ENDIF»
				@«Multi»
				@«RuneAttribute»("«currentProp.javaRuneAnnotation»")
				«IF currentProp.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
				«IF currentProp.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
				«IF currentProp.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
			«ELSE»
				@«RosettaIgnore»
				@«RuneIgnore»
			«ENDIF»
			«setterMethod(currentProp, builderType, ADD, builderScope, [scope|
				val addMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower), itemType)
				scc('''(«itemType» «addMethodArg») «
					(if (isMainProp) {
						new JavaIfThenStatement(
							JavaExpression.from('''«addMethodArg» != null''', JavaPrimitiveType.BOOLEAN),
							JavaExpression.from('''this.«field».add(«addMethodArg.toBuilder»)''', JavaPrimitiveType.VOID)
								.completeAsExpressionStatement
						).append(thisExpr)
					} else {
						addMethodArg
							.addCoercions(mainItemType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								if (mainPropType.isList) {
									JavaExpression.from('''«mainProp.getOperationName(ADD)»(«it»)''', builderType)
								} else {
									JavaExpression.from('''«mainProp.getOperationName(SET)»(«it»)''', builderType)
								}
							]
					}).completeAsReturn.toBlock
				»''')])»
			
			«setterMethod(currentProp, builderType, ADD, builderScope, [scope|
				val indexedAddMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower), itemType)
				val indexId = scope.createUniqueIdentifier("idx")
				scc('''(«itemType» «indexedAddMethodArg», int «indexId») «
					(if (isMainProp) {
						JavaExpression.from('''getIndex(this.«field», «indexId», () -> «indexedAddMethodArg.toBuilder»)''', JavaPrimitiveType.VOID)
							.completeAsExpressionStatement
							.append(thisExpr)
					} else {
						indexedAddMethodArg
							.addCoercions(mainItemType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								if (mainPropType.isList) {
									JavaExpression.from('''«mainProp.getOperationName(ADD)»(«it», «indexId»)''', builderType)
								} else {
									JavaExpression.from('''«mainProp.getOperationName(SET)»(«it»)''', builderType)
								}
							]
					}).completeAsReturn.toBlock
					»''')
			])»
			«IF itemType instanceof RJavaWithMetaValue»
			«val valueType = itemType.valueType»
			«val mainValueType = mainItemType instanceof RJavaWithMetaValue ? mainItemType.valueType : mainItemType»
			
			«setterMethod(currentProp, builderType, ADD_VALUE, builderScope, [scope|
				val addValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower), valueType)
				scc('''(«valueType» «addValueMethodArg») «
					(if (isMainProp) {
						JavaExpression.from('''this.«currentProp.getOperationName(GET_OR_CREATE)»(-1).setValue(«addValueMethodArg.toBuilder»)''', JavaPrimitiveType.VOID)
							.completeAsExpressionStatement
							.append(thisExpr)
					} else {
						addValueMethodArg
							.addCoercions(mainValueType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								if (mainPropType.isList) {
									JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«mainProp.getOperationName(ADD_VALUE)»«ELSE»«mainProp.getOperationName(ADD)»«ENDIF»(«it»)''', builderType)
								} else {
									JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«mainProp.getOperationName(SET_VALUE)»«ELSE»«mainProp.getOperationName(SET)»«ENDIF»(«it»)''', builderType)
								}
							]
					}).completeAsReturn.toBlock
					»''')
			])»

			«setterMethod(currentProp, builderType, ADD_VALUE, builderScope, [scope|
				val indexedAddValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower), valueType)
				val indexId = scope.createUniqueIdentifier("idx")
				scc('''(«valueType» «indexedAddValueMethodArg», int «indexId») «
					(if (isMainProp) {
						JavaExpression.from('''this.«currentProp.getOperationName(GET_OR_CREATE)»(«indexId»).setValue(«indexedAddValueMethodArg.toBuilder»)''', JavaPrimitiveType.VOID)
							.completeAsExpressionStatement
							.append(thisExpr)
					} else {
						indexedAddValueMethodArg
							.addCoercions(mainValueType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								if (mainPropType.isList) {
									JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«mainProp.getOperationName(ADD_VALUE)»«ELSE»«mainProp.getOperationName(ADD)»«ENDIF»(«it», «indexId»)''', builderType)
								} else {
									JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«mainProp.getOperationName(SET_VALUE)»«ELSE»«mainProp.getOperationName(SET)»«ENDIF»(«it»)''', builderType)
								}
							]
					}).completeAsReturn.toBlock
					»''')
			])»
			«ENDIF»
			
			«setterMethod(currentProp, builderType, ADD, builderScope, [scope|
				val addMultiMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower + "s"), propType)
				scc('''(«propType» «addMultiMethodArg») «
					(if (isMainProp) {
						val forLoopId = scope.bodyScope.createUniqueIdentifier("toAdd")
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
							.addCoercions(mainPropType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								if (mainPropType.isList) {
									JavaExpression.from('''«mainProp.getOperationName(ADD)»(«it»)''', builderType)
								} else {
									JavaExpression.from('''«mainProp.getOperationName(SET)»(«it»)''', builderType)
								}
							]
					}).completeAsReturn.toBlock
				»''')])»
			
			«IF isMainProp»
				@«RosettaAttribute»("«currentProp.javaAnnotation»")
				@«Accessor»(«AccessorType».SETTER)
				«IF currentProp.isRequired»@«Required»«ENDIF»
				@«Multi»
				@«RuneAttribute»("«currentProp.javaRuneAnnotation»")
				«IF currentProp.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
				«IF currentProp.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
				«IF currentProp.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
			«ELSE»
				@«RosettaIgnore»
				@«RuneIgnore»
			«ENDIF»
			«setterMethod(currentProp, builderType, SET, builderScope, [scope|
				val setMultiMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower + "s"), propType)
				scc('''(«propType» «setMultiMethodArg») «
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
							.addCoercions(mainPropType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								JavaExpression.from('''«mainProp.getOperationName(SET)»(«it»)''', builderType)
							]
					}).completeAsReturn.toBlock
				»''')])»
			«IF itemType instanceof RJavaWithMetaValue»
			«val valueType = itemType.valueType»
			«val mainValueType = mainItemType instanceof RJavaWithMetaValue ? mainItemType.valueType : mainItemType»
			
			«setterMethod(currentProp, builderType, ADD_VALUE, builderScope, [scope|
				val addMultiValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower + "s"), LIST.wrapExtends(valueType))
				scc('''(«addMultiValueMethodArg.expressionType» «addMultiValueMethodArg») «
					(if (isMainProp) {
						val forLoopId = scope.bodyScope.createUniqueIdentifier("toAdd")
						val forLoopVar = new JavaVariable(forLoopId, itemType)
						new JavaIfThenStatement(
							JavaExpression.from('''«addMultiValueMethodArg» != null''', JavaPrimitiveType.BOOLEAN),
							new JavaEnhancedForLoop(true, valueType, forLoopId, addMultiValueMethodArg,
								JavaExpression.from('''this.«mainProp.getOperationName(ADD_VALUE)»(«forLoopVar»)''', JavaPrimitiveType.VOID)
									.completeAsExpressionStatement
							)
						).append(thisExpr)
					} else {
						addMultiValueMethodArg
							.addCoercions(mainPropType.isList ? LIST.wrapExtendsIfNotFinal(mainValueType) : mainValueType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								if (mainPropType.isList) {
									JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«mainProp.getOperationName(ADD_VALUE)»«ELSE»«mainProp.getOperationName(ADD)»«ENDIF»(«it»)''', builderType)
								} else {
									JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«mainProp.getOperationName(SET_VALUE)»«ELSE»«mainProp.getOperationName(SET)»«ENDIF»(«it»)''', builderType)
								}
							]
					}).completeAsReturn.toBlock
				»''')])»
			
			«setterMethod(currentProp, builderType, SET_VALUE, builderScope, [scope|
				val setMultiValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower + "s"), LIST.wrapExtends(valueType))
				scc('''(«setMultiValueMethodArg.expressionType» «setMultiValueMethodArg») «
					(if (isMainProp) {
						JavaExpression.from('''this.«field».clear()''', JavaPrimitiveType.VOID).completeAsExpressionStatement
							.append(new JavaIfThenStatement(
								JavaExpression.from('''«setMultiValueMethodArg» != null''', JavaPrimitiveType.BOOLEAN),
								JavaExpression.from('''«setMultiValueMethodArg».forEach(this::«mainProp.getOperationName(ADD_VALUE)»)''', JavaPrimitiveType.VOID)
									.completeAsExpressionStatement
							)).append(thisExpr)
					} else {
						setMultiValueMethodArg
							.addCoercions(mainPropType.isList ? LIST.wrapExtendsIfNotFinal(mainValueType) : mainValueType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								JavaExpression.from('''«IF mainItemType instanceof RJavaWithMetaValue»«mainProp.getOperationName(SET_VALUE)»«ELSE»«mainProp.getOperationName(SET)»«ENDIF»(«it»)''', builderType)
							]
					}).completeAsReturn.toBlock
				»''')])»
			«ENDIF»
		«ELSE»
			«IF isMainProp»
				@«RosettaAttribute»("«currentProp.javaAnnotation»")
				@«Accessor»(«AccessorType».SETTER)
				«IF currentProp.isRequired»@«Required»«ENDIF»
				@«RuneAttribute»("«currentProp.javaRuneAnnotation»")
				«IF currentProp.isScopedReference»@«RuneScopedAttributeReference»«ENDIF»
				«IF currentProp.isScopedKey»@«RuneScopedAttributeKey»«ENDIF»
				«IF currentProp.addRuneMetaAnnotation»@«RuneMetaType»«ENDIF»
			«ELSE»
				@«RosettaIgnore»
				@«RuneIgnore»
			«ENDIF»
			«setterMethod(currentProp, builderType, SET, builderScope, [scope|
				val setMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower), propType)
				scc('''(«propType» «setMethodArg») «
					(if (isMainProp) {
						JavaExpression.from('''this.«field» = «setMethodArg» == null ? null : «setMethodArg.toBuilder»''', JavaPrimitiveType.VOID)
							.completeAsExpressionStatement
							.append(thisExpr)
					} else {
						setMethodArg
							.addCoercions(mainPropType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								JavaExpression.from('''«mainProp.getOperationName(SET)»(«it»)''', builderType)
							]
					}).completeAsReturn.toBlock
				»''')])»
			«IF propType instanceof RJavaWithMetaValue»
			«val valueType = propType.valueType»
			«val mainValueType = mainPropType instanceof RJavaWithMetaValue ? mainPropType.valueType : mainPropType»
			
			«setterMethod(currentProp, builderType, SET_VALUE, builderScope, [scope|
				val setValueMethodArg = new JavaVariable(scope.createUniqueIdentifier(currentProp.name.toFirstLower), valueType)
				scc('''(«valueType» «setValueMethodArg») «
					(if (isMainProp) {
						JavaExpression.from('''this.«mainProp.getOperationName(GET_OR_CREATE)»().setValue(«setValueMethodArg»)''', JavaPrimitiveType.VOID)
							.completeAsExpressionStatement
							.append(thisExpr)
					} else {
						setValueMethodArg
							.addCoercions(mainValueType, false, scope.bodyScope)
							.collapseToSingleExpression(scope.bodyScope)
							.mapExpression[
								JavaExpression.from('''«IF mainPropType instanceof RJavaWithMetaValue»«mainProp.getOperationName(SET_VALUE)»«ELSE»«mainProp.getOperationName(SET)»«ENDIF»(«it»)''', builderType)
							]
					}).completeAsReturn.toBlock
				»''')])»
			«ENDIF»
		«ENDIF»
		«IF currentProp.parentProperty !== null»
		
		«doSetter(javaType, mainProp, currentProp.parentProperty, builderScope)»
		«ENDIF»
		'''
	}
	
	private def hasData(JavaPojoInterface type, Iterable<JavaPojoProperty> properties, boolean extended, JavaClassScope builderScope) {
		'''
		@Override
		public boolean hasData() {
			«IF extended»if (super.hasData()) return true;«ENDIF»
			«FOR prop : properties.filter[name!="meta"]»
				«val getter = prop.getOperationName(GET)»
				«IF prop.type.isList»
					«IF shouldPrune.mayBeEmpty(type, prop)»
						if («getter»()!=null && «getter»().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
					«ELSE»
						if («getter»()!=null && !«getter»().isEmpty()) return true;
					«ENDIF»
				«ELSEIF shouldPrune.mayBeEmpty(type, prop)»
					if («getter»()!=null && «getter»().hasData()) return true;
				«ELSE»
					if («getter»()!=null) return true;
				«ENDIF»
			«ENDFOR»
			return false;
		}
		'''
	}

	private def JavaType toBuilderType(JavaPojoProperty prop) {
		if (prop.type.isList) LIST.wrap(prop.toBuilderTypeSingle)
		else prop.toBuilderTypeSingle
	}

	private def JavaType toBuilderTypeExt(JavaPojoProperty prop) {
		if (prop.type.isList) (prop.type.isRosettaModelObject ? LIST.wrapExtends(prop.toBuilderTypeSingle) : LIST.wrap(prop.toBuilderTypeSingle))
		else prop.toBuilderTypeSingle
	}

	def JavaType toBuilderTypeSingle(JavaPojoProperty prop) {
		prop.type.itemType.toBuilder
	}
	
	// TODO: replace with coercions
	private def JavaExpression toBuilder(JavaExpression expr) {
		val t = expr.expressionType
		if(t.hasBuilderType) {
			JavaExpression.from('''«expr».toBuilder()''', t.toBuilder)
		} else {
			expr
		}
	}
}
