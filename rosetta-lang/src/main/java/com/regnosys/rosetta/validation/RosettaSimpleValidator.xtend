package com.regnosys.rosetta.validation

import com.google.common.collect.HashMultimap
import com.google.common.collect.LinkedHashMultimap
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.ExternalAnnotationSource
import com.regnosys.rosetta.rosetta.RosettaAttributeReference
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaDocReference
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaExternalRuleSource
import com.regnosys.rosetta.rosetta.RosettaExternalSynonymSource
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFeatureOwner
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.RosettaMapping
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaSynonymBody
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaTyped
import com.regnosys.rosetta.rosetta.expression.AsKeyOperation
import com.regnosys.rosetta.rosetta.expression.CanHandleListOfLists
import com.regnosys.rosetta.rosetta.expression.CardinalityModifier
import com.regnosys.rosetta.rosetta.expression.ClosureParameter
import com.regnosys.rosetta.rosetta.expression.ComparingFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.FilterOperation
import com.regnosys.rosetta.rosetta.expression.FlattenOperation
import com.regnosys.rosetta.rosetta.expression.HasGeneratedInput
import com.regnosys.rosetta.rosetta.expression.InlineFunction
import com.regnosys.rosetta.rosetta.expression.ListLiteral
import com.regnosys.rosetta.rosetta.expression.ListOperation
import com.regnosys.rosetta.rosetta.expression.MandatoryFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.MapOperation
import com.regnosys.rosetta.rosetta.expression.ModifiableBinaryOperation
import com.regnosys.rosetta.rosetta.expression.ReduceOperation
import com.regnosys.rosetta.rosetta.expression.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.expression.RosettaCountOperation
import com.regnosys.rosetta.rosetta.expression.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.expression.RosettaFunctionalOperation
import com.regnosys.rosetta.rosetta.expression.RosettaImplicitVariable
import com.regnosys.rosetta.rosetta.expression.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.expression.RosettaSymbolReference
import com.regnosys.rosetta.rosetta.expression.RosettaUnaryOperation
import com.regnosys.rosetta.rosetta.expression.SumOperation
import com.regnosys.rosetta.rosetta.expression.UnaryFunctionalOperation
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.Annotation
import com.regnosys.rosetta.rosetta.simple.AnnotationQualifier
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.rosetta.simple.Operation
import com.regnosys.rosetta.rosetta.simple.RosettaRuleReference
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.scoping.RosettaScopeProvider
import com.regnosys.rosetta.services.RosettaGrammarAccess
import com.regnosys.rosetta.types.RErrorType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaExpectedTypeProvider
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.regnosys.rosetta.utils.ExternalAnnotationUtil
import com.regnosys.rosetta.utils.ExternalAnnotationUtil.CollectRuleVisitor
import com.regnosys.rosetta.utils.ImplicitVariableUtil
import com.regnosys.rosetta.utils.RosettaConfigExtension
import java.time.format.DateTimeFormatter
import java.util.List
import java.util.Map
import java.util.Set
import java.util.Stack
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider
import org.eclipse.xtext.validation.Check

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.expression.ExpressionPackage.Literals.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.*

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import static extension org.eclipse.emf.ecore.util.EcoreUtil.*

import static extension com.regnosys.rosetta.validation.RosettaIssueCodes.*
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import com.regnosys.rosetta.rosetta.expression.RosettaExpression
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.rosetta.ParametrizedRosettaType
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.expression.ThenOperation
import org.eclipse.xtext.nodemodel.INode
import com.regnosys.rosetta.rosetta.expression.RosettaOperation
import com.regnosys.rosetta.types.CardinalityProvider
import com.regnosys.rosetta.rosetta.expression.ParseOperation
import com.regnosys.rosetta.rosetta.expression.ToStringOperation
import com.regnosys.rosetta.types.builtin.RBasicType
import com.regnosys.rosetta.types.REnumType
import java.util.Optional
import javax.inject.Inject
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression
import com.regnosys.rosetta.types.builtin.RRecordType
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair
import com.regnosys.rosetta.rosetta.RosettaRule
import com.regnosys.rosetta.rosetta.RosettaReport
import com.regnosys.rosetta.rosetta.simple.ChoiceOption
import com.regnosys.rosetta.rosetta.expression.DefaultOperation

// TODO: split expression validator
// TODO: type check type call arguments
class RosettaSimpleValidator extends AbstractDeclarativeRosettaValidator {

	@Inject extension RosettaExtensions
	@Inject extension RosettaExpectedTypeProvider
	@Inject extension RosettaTypeProvider
	@Inject extension IQualifiedNameProvider
	@Inject extension ResourceDescriptionsProvider
	@Inject extension RosettaFunctionExtensions
	@Inject ExpressionHelper exprHelper
	@Inject extension CardinalityProvider cardinality
	@Inject RosettaConfigExtension confExtensions
	@Inject extension ImplicitVariableUtil
	@Inject RosettaScopeProvider scopeProvider
	@Inject ExternalAnnotationUtil externalAnn
	@Inject extension RBuiltinTypeService
	@Inject extension TypeSystem
	@Inject extension RosettaGrammarAccess

	@Check
	def void ruleMustHaveInputTypeDeclared(RosettaRule rule) {
		if (rule.input === null) {
			error('''A rule must declare its input type: `rule «rule.name» from <input type>: ...`''', rule, ROSETTA_NAMED__NAME)
		}
		
		if (rule.expression.isOutputListOfLists) {
			error('''Assign expression contains a list of lists, use flatten to create a list.''', rule.expression,
				null)
		}
	}
	
	// @Compat
	@Check
	def void deprecatedMap(MapOperation op) {
		if (op.operator == "map") {
			warning("The `map` operator is deprecated. Use `extract` instead.", op, ROSETTA_OPERATION__OPERATOR, DEPRECATED_MAP)
		}
	}
	
	@Check
	def void unnecesarrySquareBracketsCheck(RosettaFunctionalOperation op) {
		if (op.hasSquareBrackets) {
			if (!op.areSquareBracketsMandatory) {
				warning('''Usage of brackets is unnecessary.''', op.function, null, REDUNDANT_SQUARE_BRACKETS)
			}
		}
	}
	
	def boolean hasSquareBrackets(RosettaFunctionalOperation op) {
		op.function !== null && findDirectKeyword(op.function, inlineFunctionAccess.leftSquareBracketKeyword_0_0_1) !== null
	}
	
	def boolean areSquareBracketsMandatory(RosettaFunctionalOperation op) {
		!(op instanceof ThenOperation) &&
		op.function !== null &&
		(
			!(op instanceof MandatoryFunctionalOperation)
			|| !op.function.parameters.empty
			|| op.containsNestedFunctionalOperation
			|| (op.eContainer instanceof RosettaOperation && !(op.eContainer instanceof ThenOperation))
		)
	}
	
	@Check
	def void mandatoryThenCheck(RosettaUnaryOperation op) {
		if (op.isThenMandatory) {
			val previousOperationIsThen =
				op.argument.generated
				&& op.eContainer instanceof InlineFunction
				&& op.eContainer.eContainer instanceof ThenOperation
			if (!previousOperationIsThen) {
				error('''Usage of `then` is mandatory.''', op, ROSETTA_OPERATION__OPERATOR, MANDATORY_THEN)
			}
		}
	}
	
	def boolean isThenMandatory(RosettaUnaryOperation op) {
		if (op instanceof ThenOperation) {
			return false
		}
		return op.argument instanceof MandatoryFunctionalOperation || 
			op.argument instanceof RosettaUnaryOperation && (op.argument as RosettaUnaryOperation).isThenMandatory
	}
	
	@Check
	def void ambiguousFunctionalOperation(RosettaFunctionalOperation op) {
		if (op.function !== null) {
			if (op.isNestedFunctionalOperation) {
				val enclosingFunctionalOperation = EcoreUtil2.getContainerOfType(op, InlineFunction).eContainer as RosettaFunctionalOperation
				if (!enclosingFunctionalOperation.hasSquareBrackets) {
					error('''Ambiguous operation. Either use `then` or surround with square brackets to define a nested operation.''', op, ROSETTA_OPERATION__OPERATOR)
				}
			}
		}
	}
	
	def boolean isNestedFunctionalOperation(RosettaFunctionalOperation op) {
		val enclosingInlineFunction = EcoreUtil2.getContainerOfType(op, InlineFunction)
		if (enclosingInlineFunction !== null && !(enclosingInlineFunction.eContainer instanceof ThenOperation)) {
			if (op.getEnclosingCodeBlock === enclosingInlineFunction.getEnclosingCodeBlock) {
				return true
			}
		}
		return false
	}
	def boolean containsNestedFunctionalOperation(RosettaFunctionalOperation op) {
		if (op.function === null) {
			return false
		}
		val codeBlock = op.function.getEnclosingCodeBlock
		return EcoreUtil2.eAllOfType(op.function, RosettaFunctionalOperation)
			.filter[it.getEnclosingCodeBlock === codeBlock]
			.head !== null
	}
	
	/**
	 * Returns the object that directly encloses the given object with parentheses or brackets, or the first encountered root element.
	 */
	def EObject getEnclosingCodeBlock(EObject obj) {
		return getEnclosingCodeBlock(NodeModelUtils.findActualNodeFor(obj), obj)
	}
	private def EObject getEnclosingCodeBlock(INode node, EObject potentialCodeBlock) {
		if (potentialCodeBlock instanceof RosettaRootElement) {
			return potentialCodeBlock
		}
		val pairs = #{'(' -> ')', '[' -> ']', '{' -> '}'}
		val left = pairs.keySet.stream
			.map[potentialCodeBlock.findDirectKeyword(it)]
			.filter[it !== null]
			.findFirst.orElse(null)
		if (left !== null) {
			val right = potentialCodeBlock.findDirectKeyword(pairs.get(left.text))
			
			if (left.offset <= node.offset && (right === null || right.offset >= node.offset)) {
				return potentialCodeBlock
			}
		}
		return getEnclosingCodeBlock(node, potentialCodeBlock.eContainer)
	}

	@Check
	def void checkParametrizedType(ParametrizedRosettaType type) {
		val visited = newHashSet
		for (param: type.parameters) {
			if (!visited.add(param.name)) {
				error('''Duplicate parameter name `«param.name»`.''', param, ROSETTA_NAMED__NAME);
			}
		}
	}
	
	@Check
	def void checkAnnotationSource(ExternalAnnotationSource source) {
		val visited = newHashSet
		for (t : source.externalRefs) {
			if (!visited.add(t.typeRef)) {
				// @Compat Should be error, but would cause backward compatibility issues in some models
				warning('''Duplicate type `«t.typeRef.name»`.''', t, null);
			}
		}
	}

	@Check
	def void checkSynonymSource(RosettaExternalSynonymSource source) {
		for (t : source.externalClasses) {
			for (attr : t.regularAttributes) {
				if (attr.externalRuleReference !== null) {
					error('''You may not define rule references in a synonym source.''', attr.externalRuleReference, null);
				}
			}
		}
	}

	@Check
	def void checkRuleSource(RosettaReport report) {
		val visitor = new CollectRuleErrorVisitor

		report.reportType.collectRuleErrors(Optional.ofNullable(report.ruleSource), visitor)

		visitor.errorMap.entrySet.forEach [
			error(value, key, ROSETTA_EXTERNAL_REGULAR_ATTRIBUTE__ATTRIBUTE_REF);
		]
	}

	private def void collectRuleErrors(Data type, Optional<RosettaExternalRuleSource> source, CollectRuleErrorVisitor visitor) {
		externalAnn.collectAllRuleReferencesForType(source, type, visitor)

		type.allAttributes.forEach[attr |
			val attrType = attr.typeCall.type

			if (attrType instanceof Data) {
				if (!visitor.collectedTypes.contains(attrType)) {
					visitor.collectedTypes.add(attrType)
					attrType.collectRuleErrors(source, visitor)
				}
			}
		]
	}

	private static class CollectRuleErrorVisitor implements CollectRuleVisitor {

		final Map<RosettaFeature, RosettaRuleReference> ruleMap = newHashMap;
		final Map<RosettaExternalRegularAttribute, String> errorMap = newHashMap;
		final Set<Data> collectedTypes = newHashSet;

		override void add(RosettaFeature attr, RosettaRuleReference rule) {
			ruleMap.put(attr, rule);
		}

		override void add(RosettaExternalRegularAttribute extAttr, RosettaRuleReference rule) {
			val attr = extAttr.attributeRef
			if (!ruleMap.containsKey(attr)) {
				ruleMap.put(attr, rule);
			} else {
				errorMap.put(
					extAttr, '''There is already a mapping defined for `«attr.name»`. Try removing the mapping first with `- «attr.name»`.''')
			}
		}

		override void remove(RosettaExternalRegularAttribute extAttr) {
			val attr = extAttr.attributeRef
			if (ruleMap.containsKey(attr)) {
				ruleMap.remove(attr);
			} else {
				errorMap.put(
					extAttr, '''You cannot remove this mapping because `«attr.name»` did not have a mapping defined before.''')
			}
		}
	}

	@Check
	def void checkRuleSource(RosettaExternalRuleSource source) {
		if (source.superSources.size > 1) {
			error("A rule source may not extend more than one other rule source.", source,
				ROSETTA_EXTERNAL_RULE_SOURCE__SUPER_SOURCES, 1);
		}

		for (t : source.externalClasses) {
			t.externalClassSynonyms.forEach [
				error('''You may not define synonyms in a rule source.''', it, null);
			]

			for (attr : t.regularAttributes) {
				attr.externalSynonyms.forEach [
					error('''You may not define synonyms in a rule source.''', it, null);
				]
			}
		}

		errorKeyword("A rule source cannot define annotations for enums.", source,
			externalAnnotationSourceAccess.enumsKeyword_2_0)
	}

	// @Compat. In DRR, there is a segment `table` located after a `rationale`. This should never be the case, but to remain backwards compatible, we need to allow this.
	@Check
	def void deprecatedExtraneousSegment(RosettaDocReference docRef) {
		for (seg : docRef.extraneousSegments) {
			warning("Placing document segments after `rationale` is deprecated.", seg, null)
		}
	}

	@Check
	def void checkGeneratedInputInContextWithImplicitVariable(HasGeneratedInput e) {
		if (e.needsGeneratedInput && !e.implicitVariableExistsInContext) {
			error(
				"There is no implicit variable in this context. This operator needs an explicit input in this context.",
				e, null);
		}
	}

	@Check
	def void checkImplicitVariableReferenceInContextWithoutImplicitVariable(RosettaImplicitVariable e) {
		if (!e.implicitVariableExistsInContext) {
			error("There is no implicit variable in this context.", e, null);
		}
	}

	@Check
	def void checkClassNameStartsWithCapital(Data classe) {
		if (!Character.isUpperCase(classe.name.charAt(0))) {
			warning("Type name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkConditionName(Condition condition) {
		if (condition.name === null && !condition.isConstraintCondition) {
			warning("Condition name should be specified", ROSETTA_NAMED__NAME, INVALID_NAME)
		} else if (condition.name !== null && !Character.isUpperCase(condition.name.charAt(0))) {
			warning("Condition name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkFeatureCallFeature(RosettaFeatureCall fCall) {
		if (fCall.feature === null) {
			error("Attribute is missing after '->'", fCall, ROSETTA_FEATURE_CALL__FEATURE)
			return
		}
	}

	@Check
	def void checkFunctionNameStartsWithCapital(Function enumeration) {
		if (!Character.isUpperCase(enumeration.name.charAt(0))) {
			warning("Function name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkEnumerationNameStartsWithCapital(RosettaEnumeration enumeration) {
		if (!Character.isUpperCase(enumeration.name.charAt(0))) {
			warning("Enumeration name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkAttributeNameStartsWithLowerCase(Attribute attribute) {
		val annotationAttribute = attribute.eContainer instanceof Annotation
		val choiceOption = attribute instanceof ChoiceOption
		if (!choiceOption && !annotationAttribute && !Character.isLowerCase(attribute.name.charAt(0))) {
			warning("Attribute name should start with a lower case", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkTypeExpectation(EObject owner) {
		if (!owner.eResource.errors.filter(XtextSyntaxDiagnostic).empty)
			return;
		owner.eClass.EAllReferences.filter[ROSETTA_EXPRESSION.isSuperTypeOf(it.EReferenceType)].filter [
			owner.eIsSet(it)
		].
			forEach [ ref |
				val referenceValue = owner.eGet(ref)
				if (ref.isMany) {
					(referenceValue as List<? extends RosettaExpression>).forEach [ it, i |
						val expectedType = owner.getExpectedType(ref, i)
						checkType(expectedType, it, owner, ref, i)
					]
				} else {
					val expectedType = owner.getExpectedType(ref)
					checkType(expectedType, referenceValue as RosettaExpression, owner, ref, INSIGNIFICANT_INDEX)
				}
			]
	}

	private def checkType(RType expectedType, RosettaExpression expression, EObject owner, EReference ref, int index) {
		val actualType = expression.RType
		if (actualType === null) {
			return
		}
		if (actualType instanceof RErrorType)
			error('''«actualType.name»''', owner, ref, index, TYPE_ERROR)
		else if (actualType == MISSING) {
			val node = findActualNodeFor(expression)
			if (node !== null) {
				error('''Couldn't infer actual type for '«getTokenText(node)»'«»''', owner, ref, index,
					TYPE_ERROR)
			}
		} else if (expectedType instanceof RErrorType)
			error('''«expectedType.name»''', owner, ref, index, TYPE_ERROR)
		else if (expectedType !== null && expectedType != MISSING) {
			if (!actualType.isSubtypeOf(expectedType))
				error('''Expected type '«expectedType.name»' but was '«actualType?.name ?: 'null'»'«»''', owner, ref,
					index, TYPE_ERROR)
		}
	}

	@Check
	def checkAttributes(Data clazz) {
		val name2attr = HashMultimap.create
		clazz.allAttributes.forEach [
			name2attr.put(name, it)
		]
		for (name : clazz.attributes.map[name]) {
			val attrByName = name2attr.get(name)
			if (attrByName.size > 1) {
				val attrFromClazzes = attrByName.filter[eContainer == clazz]
				val attrFromSuperClasses = attrByName.filter[eContainer != clazz]

				attrFromClazzes.
					checkOverridingTypeAttributeMustHaveSameOrExtendedTypeAsParent(attrFromSuperClasses, name)
				attrFromClazzes.checkTypeAttributeMustHaveSameTypeAsParent(attrFromSuperClasses, name)
				attrFromClazzes.checkAttributeCardinalityMatchSuper(attrFromSuperClasses, name)
			}
		}
	}

	protected def void checkTypeAttributeMustHaveSameTypeAsParent(Iterable<Attribute> attrFromClazzes,
		Iterable<Attribute> attrFromSuperClasses, String name) {
		attrFromClazzes.filter[!override].forEach [ childAttr |
			val childAttrType = childAttr.RTypeOfSymbol
			attrFromSuperClasses.forEach [ parentAttr |
				val parentAttrType = parentAttr.RTypeOfSymbol
				if (childAttrType != parentAttrType) {
					error('''Overriding attribute '«name»' with type «childAttrType» must match the type of the attribute it overrides («parentAttrType»)''',
						childAttr, ROSETTA_NAMED__NAME, DUPLICATE_ATTRIBUTE)					
				}
			]
		]
	}

	protected def void checkOverridingTypeAttributeMustHaveSameOrExtendedTypeAsParent(
		Iterable<Attribute> attrFromClazzes, Iterable<Attribute> attrFromSuperClasses, String name) {
		attrFromClazzes.filter[override].forEach [ childAttr |
			attrFromSuperClasses.forEach [ parentAttr |
				if ((childAttr.typeCall.type instanceof Data && !(childAttr.typeCall.type as Data).isChildOf(parentAttr.typeCall.type)) ||
					!(childAttr.typeCall.type instanceof Data && childAttr.typeCall.type !== parentAttr.typeCall.type )) {
					error('''Overriding attribute '«name»' must have a type that overrides its parent attribute type of «parentAttr.typeCall.type.name»''',
						childAttr, ROSETTA_NAMED__NAME, DUPLICATE_ATTRIBUTE)
				}
			]
		]
	}

	protected def void checkAttributeCardinalityMatchSuper(Iterable<Attribute> attrFromClazzes,
		Iterable<Attribute> attrFromSuperClasses, String name) {
		attrFromClazzes.forEach [ childAttr |
			attrFromSuperClasses.forEach [ parentAttr |
				if (childAttr.card.inf !== parentAttr.card.inf || childAttr.card.sup !== parentAttr.card.sup ||
					childAttr.card.isMany !== parentAttr.card.isMany) {
					error('''Overriding attribute '«name»' with cardinality («childAttr.cardinality») must match the cardinality of the attribute it overrides («parentAttr.cardinality»)''',
						childAttr, ROSETTA_NAMED__NAME, CARDINALITY_ERROR)
				}
			]
		]
	}

	protected def cardinality(Attribute attr) '''«attr.card.inf»..«IF attr.card.isMany»*«ELSE»«attr.card.sup»«ENDIF»'''

	private def isChildOf(Data child, RosettaType parent) {
		return child.allSuperTypes.contains(parent)
	}

	@Check
	def checkEnumValuesAreUnique(RosettaEnumeration enumeration) {
		val name2attr = HashMultimap.create
		enumeration.allEnumValues.forEach [
			name2attr.put(name, it)
		]
		for (value : enumeration.enumValues) {
			val valuesByName = name2attr.get(value.name)
			if (valuesByName.size > 1) {
				error('''Duplicate enum value '«value.name»'«»''', value, ROSETTA_NAMED__NAME, DUPLICATE_ENUM_VALUE)
			}
		}
	}

	@Check
	def checkFeatureNamesAreUnique(RosettaFeatureOwner ele) {
		ele.features.groupBy[name].forEach [ k, v |
			if (v.size > 1) {
				v.forEach [
					error('''Duplicate feature "«k»"''', it, ROSETTA_NAMED__NAME)
				]
			}
		]
	}

	@Check
	def checkFunctionElementNamesAreUnique(Function ele) {
		(ele.inputs + ele.shortcuts + #[ele.output]).filterNull.groupBy[name].forEach [ k, v |
			if (v.size > 1) {
				v.forEach [
					error('''Duplicate feature "«k»"''', it, ROSETTA_NAMED__NAME)
				]
			}
		]
	}

	@Check
	def checkClosureParameterNamesAreUnique(ClosureParameter param) {
		val scope = scopeProvider.getScope(param.function.eContainer, ROSETTA_SYMBOL_REFERENCE__SYMBOL)
		val sameNamedElement = scope.getSingleElement(QualifiedName.create(param.name))
		if (sameNamedElement !== null) {
			error('''Duplicate name.''', param, null)
		}
	}

	// TODO This probably should be made namespace aware
	@Check(FAST) // switch to NORMAL if it becomes slow
	def checkTypeNamesAreUnique(RosettaModel model) {
		val name2attr = HashMultimap.create
		model.elements.filter(RosettaNamed).filter[!(it instanceof FunctionDispatch)].forEach [ // TODO better FunctionDispatch handling
			name2attr.put(name, it)
		]
		val resourceDescription = getResourceDescriptions(model.eResource)
		for (name : name2attr.keySet) {
			val valuesByName = name2attr.get(name)
			if (valuesByName.size > 1) {
				valuesByName.forEach [
					if (it.name !== null)
						error('''Duplicate element named '«name»'«»''', it, ROSETTA_NAMED__NAME, DUPLICATE_ELEMENT_NAME)
				]
			} else if (valuesByName.size == 1 && model.eResource.URI.isPlatformResource) {
				val EObject toCheck = valuesByName.get(0)
				val qName = toCheck.fullyQualifiedName
				val sameNamed = resourceDescription.getExportedObjects(toCheck.eClass(), qName, false).filter [
					isProjectLocal(model.eResource.URI, it.EObjectURI) && getEClass() !== FUNCTION_DISPATCH
				].map[EObjectURI]
				if (sameNamed.size > 1) {
					error('''Duplicate element named '«qName»' in «sameNamed.filter[toCheck.URI != it].join(', ',[it.lastSegment])»''',
						toCheck, ROSETTA_NAMED__NAME, DUPLICATE_ELEMENT_NAME)
				}
			}
		}
	}

	@Check
	def checkMappingSetToCase(RosettaMapping element) {
		if (element.instances.filter[^set !== null && when === null].size > 1) {
			error('''Only one set to with no when clause allowed.''', element, ROSETTA_MAPPING__INSTANCES)
		}
		if (element.instances.filter[^set !== null && when === null].size == 1) {
			val defaultInstance = element.instances.findFirst[^set !== null && when === null]
			val lastInstance = element.instances.last
			if (defaultInstance !== lastInstance) {
				error('''Set to without when case must be ordered last.''', element, ROSETTA_MAPPING__INSTANCES)
			}
		}

		val type = element.containerType

		if (type !== null) {
			if (type instanceof Data && !element.instances.filter[^set !== null].empty) {
				error('''Set to constant type does not match type of field.''', element, ROSETTA_MAPPING__INSTANCES)
			} else if (type instanceof RosettaEnumeration) {
				for (inst : element.instances.filter[^set !== null]) {
					if (!(inst.set instanceof RosettaEnumValueReference)) {
						error('''Set to constant type does not match type of field.''', element,
							ROSETTA_MAPPING__INSTANCES)
					} else {
						val setEnum = inst.set as RosettaEnumValueReference
						if (type.name != setEnum.enumeration.name) {
							error('''Set to constant type does not match type of field.''', element,
								ROSETTA_MAPPING__INSTANCES)
						}
					}
				}
			}
		}
	}

	def RosettaType getContainerType(RosettaMapping element) {
		val container = element.eContainer.eContainer.eContainer
		if (container instanceof RosettaExternalRegularAttribute) {
			val attributeRef = container.attributeRef
			if (attributeRef instanceof RosettaTyped)
				return attributeRef.typeCall.type
		} else if (container instanceof RosettaTyped) {
			 return container.typeCall.type
		}

		return null
	}

	@Check
	def checkMappingDefaultCase(RosettaMapping element) {
		if (element.instances.filter[^default].size > 1) {
			error('''Only one default case allowed.''', element, ROSETTA_MAPPING__INSTANCES)
		}
		if (element.instances.filter[^default].size == 1) {
			val defaultInstance = element.instances.findFirst[^default]
			val lastInstance = element.instances.last
			if (defaultInstance !== lastInstance) {
				error('''Default case must be ordered last.''', element, ROSETTA_MAPPING__INSTANCES)
			}
		}
	}

	@Check
	def checkSymbolReference(RosettaSymbolReference element) {
		val callable = element.symbol
		if (callable.isResolved) {
			if (callable instanceof RosettaCallableWithArgs) {
				val callerSize = element.args.size
	
				val callableSize = callable.numberOfParameters
				if (callerSize !== callableSize) {
					error('''Invalid number of arguments. Expecting «callableSize» but passed «callerSize».''', element,
						ROSETTA_SYMBOL_REFERENCE__SYMBOL)
				} else {
					if (callable instanceof Function) {
						element.args.indexed.forEach [ indexed |
							val callerArg = indexed.value
							val callerIdx = indexed.key
							val param = callable.inputs.get(callerIdx)
							checkType(param.typeCall.typeCallToRType, callerArg, element, ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, callerIdx)
							if(!param.card.isMany && cardinality.isMulti(callerArg)) {
								error('''Expecting single cardinality for parameter '«param.name»'.''', element,
									ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, callerIdx)
							}
						]
					} else if (callable instanceof RosettaRule) {
						if (callable.input !== null) {
							checkType(callable.input.typeCallToRType, element.args.head, element, ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, 0)
							if (cardinality.isMulti(element.args.head)) {
								error('''Expecting single cardinality for input to rule.''', element,
									ROSETTA_SYMBOL_REFERENCE__RAW_ARGS, 0)
							}
						}
					}
				}
			} else {
				if (callable instanceof Attribute) {
					if (callable.isOutput) {
						val implicitType = element.typeOfImplicitVariable
						val implicitFeatures = implicitType.allFeatures(callable)
						if (implicitFeatures.exists[name == callable.name]) {
							error(
								'''Ambiguous reference. `«callable.name»` may either refer to `«defaultImplicitVariable.name» -> «callable.name»` or to the output variable.''',
								element,
								ROSETTA_SYMBOL_REFERENCE__SYMBOL
							)
						}
					}
				}
				if (element.explicitArguments) {
					error(
						'''A variable may not be called.''',
						element,
						ROSETTA_SYMBOL_REFERENCE__EXPLICIT_ARGUMENTS
					)
				}
			}
		}
	}

	@Check
	def void checkMergeSynonymAttributeCardinality(Attribute attribute) {
		for (syn : attribute.synonyms) {
			val multi = attribute.card.isMany
			if (syn.body?.merge !== null && !multi) {
				error("Merge synonym can only be specified on an attribute with multiple cardinality.", syn.body,
					ROSETTA_SYNONYM_BODY__MERGE)
			}
		}
	}

	@Check
	def void checkMergeSynonymAttributeCardinality(RosettaExternalRegularAttribute attribute) {
		val att = attribute.attributeRef
		if (att instanceof Attribute) {
			for (syn : attribute.externalSynonyms) {
				val multi = att.card.isMany
				if (syn.body?.merge !== null && !multi) {
					error("Merge synonym can only be specified on an attribute with multiple cardinality.", syn.body,
						ROSETTA_SYNONYM_BODY__MERGE)
				}
			}
		}
	}

	@Check
	def void checkPatternAndFormat(RosettaExternalRegularAttribute attribute) {
		if (!isDateTime(attribute.attributeRef.RTypeOfFeature)){
			for(s:attribute.externalSynonyms) {
				checkFormatNull(s.body)
				checkPatternValid(s.body)
			}
		} else {
			for (s : attribute.externalSynonyms) {
				checkFormatValid(s.body)
				checkPatternNull(s.body)
			}
		}
	}

	@Check
	def void checkPatternAndFormat(Attribute attribute) {
		if (!isDateTime(attribute.RTypeOfSymbol)){
			for(s:attribute.synonyms) {
				checkFormatNull(s.body)
				checkPatternValid(s.body)
			}
		} else {
			for (s : attribute.synonyms) {
				checkFormatValid(s.body)
				checkPatternNull(s.body)
			}
		}
	}

	def checkFormatNull(RosettaSynonymBody body) {
		if (body.format !== null) {
			error("Format can only be applied to date/time types", body, ROSETTA_SYNONYM_BODY__FORMAT)
		}
	}

	def checkFormatValid(RosettaSynonymBody body) {
		if (body.format !== null) {
			try {
				DateTimeFormatter.ofPattern(body.format)
			} catch (IllegalArgumentException e) {
				error("Format must be a valid date/time format - " + e.message, body, ROSETTA_SYNONYM_BODY__FORMAT)
			}
		}
	}

	def checkPatternNull(RosettaSynonymBody body) {
		if (body.patternMatch !== null) {
			error("Pattern cannot be applied to date/time types", body, ROSETTA_SYNONYM_BODY__PATTERN_MATCH)
		}
	}

	def checkPatternValid(RosettaSynonymBody body) {
		if (body.patternMatch !== null) {
			try {
				Pattern.compile(body.patternMatch)
			} catch (PatternSyntaxException e) {
				error("Pattern to match must be a valid regular expression - " + e.message, body,
					ROSETTA_SYNONYM_BODY__PATTERN_MATCH)
			}
		}
	}

	private def isDateTime(RType rType) {
		#["date", "time", "zonedDateTime"].contains(rType?.name)
	}

	@Check
	def void checkPatternOnEnum(RosettaEnumSynonym synonym) {
		if (synonym.patternMatch !== null) {
			try {
				Pattern.compile(synonym.patternMatch)
			} catch (PatternSyntaxException e) {
				error("Pattern to match must be a valid regular expression - " + e.message, synonym,
					ROSETTA_ENUM_SYNONYM__PATTERN_MATCH)
			}
		}
	}

	/**
	 * Check all report attribute type and cardinality match the associated reporting rules
	 */
	@Check
	def void checkAttributeRuleReference(Attribute attr) {
		val ruleRef = attr.ruleReference
		if (ruleRef !== null) {
			val rule = ruleRef.reportingRule
			
			val attrExt = attr.toExpandedAttribute
			val attrSingle = attrExt.cardinalityIsSingleValue
			val attrType = attr.typeCall.typeCallToRType

			// check cardinality
			val ruleSingle = !rule.expression.isMulti
			if (attrSingle && !ruleSingle) {
				val cardWarning = '''Cardinality mismatch - report field «attr.name» has single cardinality ''' +
					'''whereas the reporting rule «rule.name» has multiple cardinality.'''
				error(cardWarning, ruleRef, ROSETTA_RULE_REFERENCE__REPORTING_RULE)
			} else if (!attrSingle && ruleSingle) {
				val cardWarning = '''Cardinality mismatch - report field «attr.name» has multiple cardinality ''' +
					'''whereas the reporting rule «rule.name» has single cardinality.'''
				warning(cardWarning, ruleRef, ROSETTA_RULE_REFERENCE__REPORTING_RULE)
			}
			
			// check type
			val ruleType = rule.expression.RType
			if (ruleType !== null && ruleType != MISSING && attrType !== null && attrType != MISSING && !ruleType.isSubtypeOf(attrType)) {
				val typeError = '''Type mismatch - report field «attr.name» has type «attrType.name» ''' +
					'''whereas the reporting rule «rule.name» has type «ruleType».'''
				error(typeError, ruleRef, ROSETTA_RULE_REFERENCE__REPORTING_RULE)
			}
		}
	}

	@Check
	def checkExpressionCardinality(ModifiableBinaryOperation binOp) {
		val leftCard = cardinality.isMulti(binOp.left)
		val rightCard = cardinality.isMulti(binOp.right)
		if (leftCard != rightCard) {
			if (binOp.cardMod === CardinalityModifier.NONE) {
				warning('''Comparison operator «binOp.operator» should specify 'all' or 'any' when comparing a list to a single value''',
					binOp, ROSETTA_OPERATION__OPERATOR)
			}
		} else if (binOp.cardMod !== CardinalityModifier.NONE) {
			warning('''«binOp.cardMod» is only aplicable when the sides have differing cardinality''', binOp,
				ROSETTA_OPERATION__OPERATOR)
		}
	}

	@Check
	def checkBinaryParamsRightTypes(RosettaBinaryOperation binOp) {
		val resultType = binOp.RType
		if (resultType instanceof RErrorType) {
			error(resultType.message, binOp, ROSETTA_OPERATION__OPERATOR)
		}
	}
	
	@Check
	def checkDefaultOperationMatchingCardinality(DefaultOperation defOp) {
		val leftCard = cardinality.isMulti(defOp.left)
		val rightCard = cardinality.isMulti(defOp.right)
		if (leftCard != rightCard) {
			val typeError = "Cardinality mismatch - default operator requires both sides to have matching cardinality"
			error(typeError, defOp, ROSETTA_OPERATION__OPERATOR)
		}
	}

	@Check
	def checkFuncDispatchAttr(FunctionDispatch ele) {
		if (ele.attribute.isResolved && ele.attribute.typeCall.isResolved) {
			if (!(ele.attribute.typeCall.type instanceof RosettaEnumeration)) {
				error('''Dispatching function may refer to an enumeration typed attributes only. Current type is «ele.attribute.typeCall.type.name»''', ele,
					FUNCTION_DISPATCH__ATTRIBUTE)
			}
		}
	}

	@Check
	def checkAttribute(Attribute ele) {
		if (ele.typeCall.type instanceof Data && ele.typeCall?.type.isResolved) {
			if (ele.hasReferenceAnnotation && !(hasKeyedAnnotation(ele.typeCall.type as Annotated) || (ele.typeCall.type as Data).allSuperTypes.exists[hasKeyedAnnotation])) {
				//TODO turn to error if it's okay
				warning('''«ele.typeCall.type.name» must be annotated with [metadata key] as reference annotation is used''',
					ROSETTA_TYPED__TYPE_CALL)
			}
		}
	}

	@Check
	def checkDispatch(Function ele) {
		if (ele instanceof FunctionDispatch)
			return
		val dispath = ele.dispatchingFunctions.toList
		if (dispath.empty)
			return
		val enumsUsed = LinkedHashMultimap.create
		dispath.forEach [
			val enumRef = it.value
			if (enumRef !== null && enumRef.enumeration !== null && enumRef.value !== null) {
				enumsUsed.put(enumRef.enumeration, enumRef.value.name -> it)
			}
		]
		val structured = enumsUsed.keys.map[it -> enumsUsed.get(it)].filter[it === null || value === null]
		if (structured.nullOrEmpty)
			return
		val mostUsedEnum = structured.max[$0.value.size <=> $1.value.size].key
		val toImplement = mostUsedEnum.allEnumValues.map[name].toSet
		enumsUsed.get(mostUsedEnum).forEach [
			toImplement.remove(it.key)
		]
		if (!toImplement.empty) {
			warning('''Missing implementation for «mostUsedEnum.name»: «toImplement.sort.join(', ')»''', ele,
				ROSETTA_NAMED__NAME)
		}
		structured.forEach [
			if (it.key != mostUsedEnum) {
				it.value.forEach [ entry |
					error('''Wrong «it.key.name» enumeration used. Expecting «mostUsedEnum.name».''', entry.value.value,
						ROSETTA_ENUM_VALUE_REFERENCE__ENUMERATION)
				]
			} else {
				it.value.groupBy[it.key].filter[enumVal, entries|entries.size > 1].forEach [ enumVal, entries |
					entries.forEach [
						error('''Dupplicate usage of «it.key» enumeration value.''', it.value.value,
							ROSETTA_ENUM_VALUE_REFERENCE__VALUE)
					]
				]
			}
		]
	}

	@Check
	def checkConditionDontUseOutput(Function ele) {
		ele.conditions.filter[!isPostCondition].forEach [ cond |
			val expr = cond.expression
			if (expr !== null) {
				val trace = new Stack
				val outRef = exprHelper.findOutputRef(expr, trace)
				if (!outRef.nullOrEmpty) {
					error('''
					output '«outRef.head.name»' or alias on output '«outRef.head.name»' not allowed in condition blocks.
					«IF !trace.isEmpty»
					«trace.join(' > ')» > «outRef.head.name»«ENDIF»''', expr, null)
				}
			}
		]
	}

	@Check
	def checkAssignAnAlias(Operation ele) {
		if (ele.path === null && ele.assignRoot instanceof ShortcutDeclaration)
			error('''An alias can not be assigned. Assign target must be an attribute.''', ele, OPERATION__ASSIGN_ROOT)
	}
	
	@Check
	def checkConstructorExpression(RosettaConstructorExpression ele) {
		val rType = ele.RType
		if (rType !== null) {
			val baseRType = rType.stripFromTypeAliases
			if (!(baseRType instanceof RDataType || baseRType instanceof RRecordType)) {
				error('''Cannot construct an instance of type `«rType.name»`.''', ele.typeCall, null)
			}
			
			val seenFeatures = newHashSet
			for (pair : ele.values) {
				val feature = pair.key
				val expr = pair.value
				if (!seenFeatures.add(feature)) {
					error('''Duplicate attribute `«feature.name»`.''', pair, CONSTRUCTOR_KEY_VALUE_PAIR__KEY)
				}
				checkType(feature.RTypeOfFeature, expr, pair, CONSTRUCTOR_KEY_VALUE_PAIR__VALUE, INSIGNIFICANT_INDEX)
				if(!cardinality.isFeatureMulti(feature) && cardinality.isMulti(expr)) {
					error('''Expecting single cardinality for attribute `«feature.name»`.''', pair,
						CONSTRUCTOR_KEY_VALUE_PAIR__VALUE)
				}
			}
			val absentAttributes = rType
				.allFeatures(ele)
				.filter[!seenFeatures.contains(it)]
			val requiredAbsentAttributes = absentAttributes
				.filter[!(it instanceof Attribute) || (it as Attribute).card.inf !== 0]
			if (ele.implicitEmpty) {
				if (!requiredAbsentAttributes.empty) {
					error('''Missing attributes «FOR attr : requiredAbsentAttributes SEPARATOR ', '»`«attr.name»`«ENDFOR».''', ele.typeCall, null)
				}
				if (absentAttributes.size === requiredAbsentAttributes.size) {
					error('''There are no optional attributes left.''', ele, ROSETTA_CONSTRUCTOR_EXPRESSION__IMPLICIT_EMPTY)
				}
			} else {
				if (!absentAttributes.empty) {
					error('''Missing attributes «FOR attr : absentAttributes SEPARATOR ', '»`«attr.name»`«ENDFOR».«IF requiredAbsentAttributes.empty» Perhaps you forgot a `...` at the end of the constructor?«ENDIF»''', ele.typeCall, null)
				}
			}
		}
	}

	@Check
	def checkListLiteral(ListLiteral ele) {
		val type = ele.RType
		if (type instanceof RErrorType) {
			error('''All collection elements must have the same super type but types were «type.message»''', ele, null)
		}
	}

	@Check
	def checkSynonyMapPath(RosettaMapPathValue ele) {
		if (!ele.path.nullOrEmpty) {
			val invalidChar = checkPathChars(ele.path)
			if (invalidChar !== null)
				error('''Character '«invalidChar.key»' is not allowed «IF invalidChar.value»as first symbol in a path segment.«ELSE»in paths. Use '->' to separate path segments.«ENDIF»''',
					ele, ROSETTA_MAP_PATH_VALUE__PATH)
		}
	}

	@Check
	def checkSynonyValuePath(RosettaSynonymValueBase ele) {
		if (!ele.path.nullOrEmpty) {
			val invalidChar = checkPathChars(ele.path)
			if (invalidChar !== null)
				error('''Character '«invalidChar.key»' is not allowed «IF invalidChar.value»as first symbol in a path segment.«ELSE»in paths. Use '->' to separate path segments.«ENDIF»''',
					ele, ROSETTA_SYNONYM_VALUE_BASE__PATH)
		}
	}

	@Check
	def checkCountOpArgument(RosettaCountOperation ele) {
		if (ele.argument.isResolved) {
			if (!cardinality.isMulti(ele.argument))
				error('''Count operation multiple cardinality argument.''', ele, ROSETTA_UNARY_OPERATION__ARGUMENT)
		}
	}
	
	@Check
	def checkParseOpArgument(ParseOperation ele) {
		val arg = ele.argument
		if (arg.isResolved) {
			if (cardinality.isMulti(arg)) {
				error('''The argument of «ele.operator» should be of singular cardinality.''', ele, ROSETTA_UNARY_OPERATION__ARGUMENT)
			}
			if (!arg.RType.isSubtypeOf(UNCONSTRAINED_STRING)) {
				error('''The argument of «ele.operator» should be a string.''', ele, ROSETTA_UNARY_OPERATION__ARGUMENT)
			}
		}
	}
	
	@Check
	def checkToStringOpArgument(ToStringOperation ele) {
		val arg = ele.argument
		if (arg.isResolved) {
			if (cardinality.isMulti(arg)) {
				error('''The argument of «ele.operator» should be of singular cardinality.''', ele, ROSETTA_UNARY_OPERATION__ARGUMENT)
			}
			val type = arg.RType.stripFromTypeAliases
			if (!(type instanceof RBasicType || type instanceof RRecordType || type instanceof REnumType)) {
				error('''The argument of «ele.operator» should be of a builtin type or an enum.''', ele, ROSETTA_UNARY_OPERATION__ARGUMENT)
			}
		}
	}

	@Check
	def checkFunctionPrefix(Function ele) {
		ele.annotations.forEach [ a |
			val prefix = a.annotation.prefix
			if (prefix !== null && !ele.name.startsWith(prefix + "_")) {
				warning('''Function name «ele.name» must have prefix '«prefix»' followed by an underscore.''',
					ROSETTA_NAMED__NAME, INVALID_ELEMENT_NAME)
			}
		]
	}

	@Check
	def checkMetadataAnnotation(Annotated ele) {
		val metadatas = ele.metadataAnnotations
		metadatas.forEach [
			switch (it.attribute?.name) {
				case "key":
					if (!(ele instanceof Data)) {
						error('''[metadata key] annotation only allowed on a type.''', it, ANNOTATION_REF__ATTRIBUTE)
					}
				case "id":
					if (!(ele instanceof Attribute)) {
						error('''[metadata id] annotation only allowed on an attribute.''', it,
							ANNOTATION_REF__ATTRIBUTE)
					}
				case "reference":
					if (!(ele instanceof Attribute)) {
						error('''[metadata reference] annotation only allowed on an attribute.''', it,
							ANNOTATION_REF__ATTRIBUTE)
					}
				case "scheme":
					if (!(ele instanceof Attribute || ele instanceof Data)) {
						error('''[metadata scheme] annotation only allowed on an attribute or a type.''', it,
							ANNOTATION_REF__ATTRIBUTE)
					}
				case "template":
					if (!(ele instanceof Data)) {
						error('''[metadata template] annotation only allowed on a type.''', it,
							ANNOTATION_REF__ATTRIBUTE)
					} else if (!metadatas.map[attribute?.name].contains("key")) {
						error('''Types with [metadata template] annotation must also specify the [metadata key] annotation.''',
							it, ANNOTATION_REF__ATTRIBUTE)
					}
				case "location":
					if (ele instanceof Attribute) {
						if (qualifiers.exists[qualName == "pointsTo"]) {
							error('''pointsTo qualifier belongs on the address not the location.''', it,
								ANNOTATION_REF__ATTRIBUTE)
						}
					} else {
						error('''[metadata location] annotation only allowed on an attribute.''', it,
							ANNOTATION_REF__ATTRIBUTE)
					}
				case "address":
					if (ele instanceof Attribute) {
						qualifiers.forEach [
							if (qualName == "pointsTo") {
								// check the qualPath has the address metadata
								switch qualPath {
									RosettaAttributeReference: {
										val attrRef = qualPath as RosettaAttributeReference
										if (attrRef.attribute.isResolved) {
											checkForLocation(attrRef.attribute, it)
											val targetType = attrRef.attribute.typeCall.typeCallToRType
											val thisType = ele.RTypeOfSymbol
											if (!targetType.isSubtypeOf(thisType))
												error('''Expected address target type of '«thisType.name»' but was '«targetType?.name ?: 'null'»'«»''', it, ANNOTATION_QUALIFIER__QUAL_PATH, TYPE_ERROR)
										}
									}
									default:
										error('''Target of an address must be an attribute''', it,
											ANNOTATION_QUALIFIER__QUAL_PATH, TYPE_ERROR)
								}
							}
						]
					} else {
						error('''[metadata address] annotation only allowed on an attribute.''', it,
							ANNOTATION_REF__ATTRIBUTE)
					}
			}
		]
	}

	def checkForLocation(Attribute attribute, AnnotationQualifier checked) {
		var locationFound = !attribute.metadataAnnotations.filter[it.attribute?.name == "location"].empty
		if (!locationFound) {
			error('''Target of address must be annotated with metadata location''', checked,
				ANNOTATION_QUALIFIER__QUAL_PATH)
		}
	}

	@Check
	def checkCreationAnnotation(Annotated ele) {
		val annotations = getCreationAnnotations(ele)
		if (annotations.empty) {
			return
		}
		if (!(ele instanceof Function)) {
			error('''Creation annotation only allowed on a function.''', ROSETTA_NAMED__NAME, INVALID_ELEMENT_NAME)
			return
		}
		if (annotations.size > 1) {
			error('''Only 1 creation annotation allowed.''', ROSETTA_NAMED__NAME, INVALID_ELEMENT_NAME)
			return
		}

		val func = ele as Function
		
		val annotationType = annotations.head.attribute.typeCall.type
		val funcOutputType = func.output.typeCall.type
		
		if (annotationType instanceof Data && funcOutputType instanceof Data) {
			val annotationDataType = annotationType as Data
			val funcOutputDataType = funcOutputType as Data
			
			val funcOutputSuperTypes = funcOutputDataType.superType.allSuperTypes.toSet
			val annotationAttributeTypes = annotationDataType.attributes.map[typeCall.type].toList
			
			if (annotationDataType != funcOutputDataType
				&& !funcOutputSuperTypes.contains(annotationDataType) // annotation type is a super type of output type
				&& !annotationAttributeTypes.contains(funcOutputDataType) // annotation type is a parent of the output type (with a one-of condition)
			) {
				warning('''Invalid output type for creation annotation.  The output type must match the type specified in the annotation '«annotationDataType.name»' (or extend the annotation type, or be a sub-type as part of a one-of condition).''',
					func, FUNCTION__OUTPUT)
			}
		}
	}

	@Check
	def checkQualificationAnnotation(Annotated ele) {
		val annotations = getQualifierAnnotations(ele)
		if (annotations.empty) {
			return
		}
		if (!(ele instanceof Function)) {
			error('''Qualification annotation only allowed on a function.''', ROSETTA_NAMED__NAME, INVALID_ELEMENT_NAME)
			return
		}

		val func = ele as Function

		if (annotations.size > 1) {
			error('''Only 1 qualification annotation allowed.''', ROSETTA_NAMED__NAME, INVALID_ELEMENT_NAME)
			return
		}

		val inputs = getInputs(func)
		if (inputs.nullOrEmpty || inputs.size !== 1) {
			error('''Qualification functions must have exactly 1 input.''', func, FUNCTION__INPUTS)
			return
		}
		val inputType = inputs.get(0).typeCall?.type
		if (!inputType.isResolved) {
			error('''Invalid input type for qualification function.''', func, FUNCTION__INPUTS)
		} else if (!confExtensions.isRootEventOrProduct(inputType)) {
			warning('''Input type does not match qualification root type.''', func, FUNCTION__INPUTS)
		}
		
		if (func.output?.typeCall?.typeCallToRType != BOOLEAN) {
	 		error('''Qualification functions must output a boolean.''', func, FUNCTION__OUTPUT)
		}
	}

	private def Pair<Character, Boolean> checkPathChars(String str) {
		val segments = str.split('->')
		for (segment : segments) {
			if (segment.length > 0) {
				if (!Character.isJavaIdentifierStart(segment.charAt(0))) {
					return segment.charAt(0) -> true
				}
				val notValid = segment.toCharArray.findFirst[it|!Character.isJavaIdentifierPart(it)]
				if (notValid !== null) {
					return notValid -> false
				}
			}
		}
	}

	@Check
	def checkOnlyExistsPathsHaveCommonParent(RosettaOnlyExistsExpression e) {
		val first = e.args.head
		val parent = exprHelper.getParentExpression(first)
		for (var i = 1; i < e.args.size; i++) {
			val other = e.args.get(i)
			val otherParent = exprHelper.getParentExpression(other)
			if ((parent === null) !== (otherParent === null) ||
				parent !== null && otherParent !== null && !EcoreUtil2.equals(parent, otherParent)) {
				if (otherParent !== null) {
					error('''Only exists paths must have a common parent.''', otherParent, null)
				} else {
					error('''Only exists paths must have a common parent.''', other, null)
				}
			}
		}
	}

	@Check
	def checkUnaryOperation(RosettaUnaryOperation e) {
		val receiver = e.argument
		if (e instanceof ListOperation && receiver.isResolved && !cardinality.isMulti(receiver)) {
			warning('''List «e.operator» operation cannot be used for single cardinality expressions.''', e,
				ROSETTA_OPERATION__OPERATOR)
		}

		if (!(e instanceof CanHandleListOfLists) && receiver !== null && receiver.isOutputListOfLists) {
			error('''List must be flattened before «e.operator» operation.''', e, ROSETTA_OPERATION__OPERATOR)
		}
	}

	@Check
	def checkInlineFunction(InlineFunction f) {
		if (f.body === null) {
			error('''Missing function body.''', f, null)
		}
	}

	@Check
	def checkMandatoryFunctionalOperation(MandatoryFunctionalOperation e) {
		checkBodyExists(e)
	}

	@Check
	def checkUnaryFunctionalOperation(UnaryFunctionalOperation e) {
		checkOptionalNamedParameter(e.function)
	}

	@Check
	def checkFilterOperation(FilterOperation o) {
		checkBodyType(o.function, BOOLEAN)
		checkBodyIsSingleCardinality(o.function)
	}

	@Check
	def checkMapOperation(MapOperation o) {
		if (o.isOutputListOfListOfLists) {
			error('''Each list item is already a list, mapping the item into a list of lists is not allowed. List map item expression must maintain existing cardinality (e.g. list to list), or reduce to single cardinality (e.g. list to single using expression such as count, sum etc).''',
				o, ROSETTA_FUNCTIONAL_OPERATION__FUNCTION)
		}
	}

	@Check
	def checkFlattenOperation(FlattenOperation o) {
		if (!o.argument.isOutputListOfLists) {
			error('''List flatten only allowed for list of lists.''', o, ROSETTA_OPERATION__OPERATOR)
		}
	}

	@Check
	def checkReduceOperation(ReduceOperation o) {
		checkNumberOfMandatoryNamedParameters(o.function, 2)
		if (o.argument.RType != o.function.body.RType) {
			error('''List reduce expression must evaluate to the same type as the input. Found types «o.argument.RType» and «o.function.body.RType».''', o, ROSETTA_FUNCTIONAL_OPERATION__FUNCTION)
		}
		checkBodyIsSingleCardinality(o.function)
	}

	@Check
	def checkNumberReducerOperation(SumOperation o) {
		checkInputType(o, UNCONSTRAINED_NUMBER)
	}

	@Check
	def checkComparingFunctionalOperation(ComparingFunctionalOperation o) {
		checkBodyIsSingleCardinality(o.function)
		checkBodyIsComparable(o)
		if (o.function === null) {
			checkInputIsComparable(o)
		}
	}

	@Check
	def checkAsKeyOperation(AsKeyOperation o) {
		val container = o.eContainer
		if (container instanceof Operation) {
			if (container.path === null) {
				error(''''«o.operator»' can only be used when assigning an attribute. Example: "set out -> attribute: value as-key"''',
					o, ROSETTA_OPERATION__OPERATOR)
				return
			}
			val segments = container.path.asSegmentList(container.path)
			val attr = segments?.last?.attribute
			if (!attr.hasReferenceAnnotation) {
				error(''''«o.operator»' can only be used with attributes annotated with [metadata reference] annotation.''',
					o, ROSETTA_OPERATION__OPERATOR)
			}
		} else if (container instanceof ConstructorKeyValuePair) {
			val attr = container.key
			if (!(attr instanceof Attribute) || !(attr as Attribute).hasReferenceAnnotation) {
				error(''''«o.operator»' can only be used with attributes annotated with [metadata reference] annotation.''',
					o, ROSETTA_OPERATION__OPERATOR)
			}
		} else {
			error(''''«o.operator»' may only be used in context of an attribute."''', o,
				ROSETTA_OPERATION__OPERATOR)
		}
	}

	@Check
	def checkImport(RosettaModel model) {

		var usedNames = model.eAllContents.flatMap[
			eCrossReferences.filter(RosettaRootElement).filter[isResolved].iterator
		].map[
			fullyQualifiedName
		].toList

		for (ns : model.imports) {
			if (ns.importedNamespace !== null) {
				val qn = QualifiedName.create(ns.importedNamespace.split('\\.'))
				val isUsed = if (qn.lastSegment.equals('*')) {
					usedNames.stream.anyMatch[startsWith(qn.skipLast(1)) && segmentCount === qn.segmentCount]
				} else {
					usedNames.contains(qn)
				}
				if (!isUsed) {
					warning('''Unused import «ns.importedNamespace»''', ns, IMPORT__IMPORTED_NAMESPACE, UNUSED_IMPORT)
				}
			}
		}
	}

	private def checkBodyExists(RosettaFunctionalOperation operation) {
		if (operation.function === null) {
			error('''Missing an expression.''', operation, ROSETTA_OPERATION__OPERATOR)
		}
	}

	private def void checkOptionalNamedParameter(InlineFunction ref) {
		if (ref !== null && ref.parameters !== null && ref.parameters.size !== 0 && ref.parameters.size !== 1) {
			error('''Function must have 1 named parameter.''', ref, INLINE_FUNCTION__PARAMETERS)
		}
	}

	private def void checkNumberOfMandatoryNamedParameters(InlineFunction ref, int max) {
		if (ref !== null && ref.parameters === null || ref.parameters.size !== max) {
			error('''Function must have «max» named parameter«IF max > 1»s«ENDIF».''', ref, INLINE_FUNCTION__PARAMETERS)
		}
	}
	
	private def void checkInputType(RosettaUnaryOperation o, RType type) {
		if (!o.argument.getRType.isSubtypeOf(type)) {
			error('''Input type must be a «type».''', o, ROSETTA_OPERATION__OPERATOR)
		}
	}

	private def void checkInputIsComparable(RosettaUnaryOperation o) {
		val inputRType = o.argument.getRType
		if (!inputRType.hasNaturalOrder) {
			error('''Operation «o.operator» only supports comparable types (string, int, string, date). Found type «inputRType.name».''', o, ROSETTA_OPERATION__OPERATOR)
		}
	}

	private def void checkBodyIsSingleCardinality(InlineFunction ref) {
		if (ref !== null && ref.isBodyExpressionMulti) {
			error('''Operation only supports single cardinality expressions.''', ref, null)
		}
	}

	private def void checkBodyType(InlineFunction ref, RType type) {
		val bodyType = ref?.body?.getRType
		if (ref !== null && bodyType !== null && bodyType != MISSING && bodyType != type) {
			error('''Expression must evaluate to a «type.name».''', ref, null)
		}
	}

	private def void checkBodyIsComparable(RosettaFunctionalOperation op) {
		val ref = op.function
		if (ref !== null) {
			val bodyRType = ref.body.getRType
			if (!bodyRType.hasNaturalOrder) {
				error('''Operation «op.operator» only supports comparable types (string, int, string, date). Found type «bodyRType.name».''', ref, null)
			}			
		}
	}

	@Check
	def checkOutputOperation(Operation o) {
		val expr = o?.expression
		if (expr !== null && expr.isOutputListOfLists) {
			error('''Assign expression contains a list of lists, use flatten to create a list.''', o,
				OPERATION__EXPRESSION)
		}
		val isList = cardinality.isSymbolMulti(o.path !== null
				? o.pathAsSegmentList.last.attribute
				: o.assignRoot)
		if (o.add && !isList) {
			error('''Add must be used with a list.''', o, OPERATION__ASSIGN_ROOT)
		}
		if (!o.add && isList) {
			info('''Set used with a list. Any existing list items will be overwritten.  Use Add to append items to existing list.''',
				o, OPERATION__ASSIGN_ROOT)
		}
		if (!isList && cardinality.isMulti(o.expression)) {
			error('''Cardinality mismatch - cannot assign list to a single value.''', o, OPERATION__ASSIGN_ROOT)
		}
	}

	@Check
	def checkAlias(ShortcutDeclaration o) {
		val expr = o?.expression
		if (expr !== null && expr.isOutputListOfLists) {
			error('''Alias expression contains a list of lists, use flatten to create a list.''', o,
				SHORTCUT_DECLARATION__EXPRESSION)
		}
	}
}
