package com.regnosys.rosetta.validation

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.HashMultimap
import com.google.common.collect.LinkedHashMultimap
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.expression.ListOperationExtensions
import com.regnosys.rosetta.generator.java.function.CardinalityProvider
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.BlueprintExtract
import com.regnosys.rosetta.rosetta.BlueprintFilter
import com.regnosys.rosetta.rosetta.RosettaBinaryOperation
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgsCall
import com.regnosys.rosetta.rosetta.RosettaContainsExpression
import com.regnosys.rosetta.rosetta.RosettaCountOperation
import com.regnosys.rosetta.rosetta.RosettaDisjointExpression
import com.regnosys.rosetta.rosetta.RosettaEnumSynonym
import com.regnosys.rosetta.rosetta.RosettaEnumValueReference
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaExternalRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaFeatureOwner
import com.regnosys.rosetta.rosetta.RosettaLiteral
import com.regnosys.rosetta.rosetta.RosettaMapPathValue
import com.regnosys.rosetta.rosetta.RosettaMapping
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaOnlyExistsExpression
import com.regnosys.rosetta.rosetta.RosettaSynonymBody
import com.regnosys.rosetta.rosetta.RosettaSynonymValueBase
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.RosettaTyped
import com.regnosys.rosetta.rosetta.RosettaTypedFeature
import com.regnosys.rosetta.rosetta.simple.Annotated
import com.regnosys.rosetta.rosetta.simple.Annotation
import com.regnosys.rosetta.rosetta.simple.AnnotationQualifier
import com.regnosys.rosetta.rosetta.simple.AssignOutputOperation
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Condition
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.FunctionDispatch
import com.regnosys.rosetta.rosetta.simple.ListLiteral
import com.regnosys.rosetta.rosetta.simple.ListOperation
import com.regnosys.rosetta.rosetta.simple.ListOperationKind
import com.regnosys.rosetta.rosetta.simple.Operation
import com.regnosys.rosetta.rosetta.simple.OutputOperation
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.services.RosettaGrammarAccess
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RErrorType
import com.regnosys.rosetta.types.RRecordType
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.types.RosettaExpectedTypeProvider
import com.regnosys.rosetta.types.RosettaOperators
import com.regnosys.rosetta.types.RosettaTypeCompatibility
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.utils.ExpressionHelper
import com.regnosys.rosetta.utils.RosettaConfigExtension
import com.regnosys.rosetta.validation.RosettaBlueprintTypeResolver.BlueprintUnresolvedTypeException
import java.lang.reflect.Method
import java.time.format.DateTimeFormatter
import java.util.List
import java.util.Set
import java.util.Stack
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import org.apache.log4j.Logger
import org.eclipse.emf.common.util.Diagnostic
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider
import org.eclipse.xtext.validation.AbstractDeclarativeValidator
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.FeatureBasedDiagnostic

import static com.regnosys.rosetta.rosetta.RosettaPackage.Literals.*
import static com.regnosys.rosetta.rosetta.simple.SimplePackage.Literals.*
import static com.regnosys.rosetta.validation.RosettaIssueCodes.*
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.*

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import static extension org.eclipse.emf.ecore.util.EcoreUtil.*

class RosettaSimpleValidator extends AbstractDeclarativeValidator {
	
	@Inject extension RosettaExtensions
	@Inject extension RosettaExpectedTypeProvider
	@Inject extension RosettaTypeProvider
	@Inject extension RosettaTypeCompatibility
	@Inject extension IQualifiedNameProvider
	@Inject extension ResourceDescriptionsProvider
	@Inject extension RosettaBlueprintTypeResolver
	@Inject extension RosettaFunctionExtensions
	@Inject extension RosettaOperators
	@Inject extension ListOperationExtensions
	@Inject ExpressionHelper exprHelper
	@Inject CardinalityProvider cardinality
	@Inject RosettaGrammarAccess grammar
	@Inject RosettaConfigExtension confExtensions
	
	static final Logger log = Logger.getLogger(RosettaValidator);
	
	protected override List<EPackage> getEPackages() {
		val result = newArrayList
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/Rosetta"));
		result.add(EPackage.Registry.INSTANCE.getEPackage("http://www.rosetta-model.com/RosettaSimple"));
		return result;
	}
	
	protected override MethodWrapper createMethodWrapper(AbstractDeclarativeValidator instanceToUse, Method method) {
		return new RosettaMethodWrapper(instanceToUse, method);
	}
	
	protected static class RosettaMethodWrapper extends MethodWrapper {
		protected new(AbstractDeclarativeValidator instance, Method m) {
			super(instance, m)
		}
		
		override void invoke(State state) {
			try {
				super.invoke(state);	
			}
			catch (Exception e) {
				val String message = "Unexpected validation failure running "+ method.name
				log.error(message,e);
				state.hasErrors = true;
				state.chain.add(createDiagnostic(message, state))
			}
		}
		
		def Diagnostic createDiagnostic(String message, State state) {
			new FeatureBasedDiagnostic(Diagnostic.ERROR, message, state.currentObject, null, -1, state.currentCheckType, null, null)
		}
	}
	
	@Check
	def void checkClassNameStartsWithCapital(Data classe) {
		if (!Character.isUpperCase(classe.name.charAt(0))) {
			warning("Type name should start with a capital", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}
	
	@Check
	def void checkFeatureCallFeature(RosettaFeatureCall fCall) {
		if (fCall.feature === null) {
			error("Attribute is missing after '->'", fCall, ROSETTA_FEATURE_CALL__FEATURE)
			return
		}
		if (fCall.onlyElement && fCall.receiver !== null && !fCall.receiver.eIsProxy && !fCall.feature.eIsProxy &&
			!(cardinality.isMulti(fCall.feature) || cardinality.isMulti(fCall.receiver))) {
			error("List only-element cannot be used for single cardinality expressions.", fCall, ROSETTA_FEATURE_CALL__FEATURE)
		}
	}
	
	@Check
	def void checkCallableCall(RosettaCallableCall cCall) {
		if (cCall.callable !== null && cCall.onlyElement && !cCall.callable.eIsProxy && !cardinality.isMulti(cCall.callable)) {
			error("List only-element cannot be used for single cardinality expressions.", cCall, ROSETTA_CALLABLE_CALL__CALLABLE)
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
		if (!annotationAttribute && !Character.isLowerCase(attribute.name.charAt(0))) {
			warning("Attribute name should start with a lower case", ROSETTA_NAMED__NAME, INVALID_CASE)
		}
	}

	@Check
	def void checkTypeExpectation(EObject owner) {
		if(!owner.eResource.errors.filter(XtextSyntaxDiagnostic).empty)
			return;
		owner.eClass.EAllReferences.filter[ROSETTA_EXPRESSION.isSuperTypeOf(it.EReferenceType)].filter[
			owner.eIsSet(it)
		].
			forEach [ ref |
				val referenceValue = owner.eGet(ref)
				if (ref.isMany) {
					(referenceValue as List<? extends EObject>).forEach [ it, i |
						val expectedType = owner.getExpectedType(ref, i)
						checkType(expectedType, it, owner, ref, i)
					]
				} else {
					val expectedType = owner.getExpectedType(ref)
					checkType(expectedType, referenceValue as EObject, owner, ref, INSIGNIFICANT_INDEX)
				}
			]
	}

	private def checkType(RType expectedType, EObject object, EObject owner, EReference ref, int index) {
		val actualType = object.RType
		if (actualType === null || actualType == RBuiltinType.ANY) {
			return
		}
		if (actualType instanceof RErrorType)
			error('''«actualType.name»''', owner, ref, index, TYPE_ERROR)
		else if (actualType == RBuiltinType.MISSING)
			error('''Couldn't infer actual type for '«getTokenText(findActualNodeFor(object))»'«»''', owner, ref, index,
				TYPE_ERROR)
		else if (expectedType instanceof RErrorType)
			error('''«expectedType.name»''', owner, ref, index, TYPE_ERROR)
		else if (expectedType !== null && expectedType != RBuiltinType.MISSING) {
			if (!actualType.isUseableAs(expectedType))
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
				
				attrFromClazzes.checkOverridingTypeAttributeMustHaveSameOrExtendedTypeAsParent(attrFromSuperClasses, name)
				attrFromClazzes.checkTypeAttributeMustHaveSameTypeAsParent(attrFromSuperClasses, name)
				attrFromClazzes.checkAttributeCardinalityMatchSuper(attrFromSuperClasses, name)
			}
		}
	}
	
	protected def void checkTypeAttributeMustHaveSameTypeAsParent(Iterable<Attribute> attrFromClazzes,
		Iterable<Attribute> attrFromSuperClasses, String name) {
		attrFromClazzes.filter[!override].forEach [ childAttr |
			attrFromSuperClasses.forEach [ parentAttr |
				if (childAttr.type !== parentAttr.type) {
					error('''Overriding attribute '«name»' with type («childAttr.type.name») must match the type of the attribute it overrides («parentAttr.type.name»)''',
						childAttr, ROSETTA_NAMED__NAME, DUPLICATE_ATTRIBUTE)					
				}
			]
		]
	}
	
	protected def void checkOverridingTypeAttributeMustHaveSameOrExtendedTypeAsParent(Iterable<Attribute> attrFromClazzes,
		Iterable<Attribute> attrFromSuperClasses, String name) {
		attrFromClazzes.filter[override].forEach [ childAttr |
			attrFromSuperClasses.forEach [ parentAttr |
				if ((childAttr.type instanceof Data && !(childAttr.type as Data).isChildOf(parentAttr.type)) ||
					!(childAttr.type instanceof Data && childAttr.type !== parentAttr.type )) {
					error('''Overriding attribute '«name»' must have a type that overrides its parent attribute type of «parentAttr.type.name»''',
						childAttr, ROSETTA_NAMED__NAME, DUPLICATE_ATTRIBUTE)
				}
			]
		]
	}
	
	protected def void checkAttributeCardinalityMatchSuper(Iterable<Attribute> attrFromClazzes, Iterable<Attribute> attrFromSuperClasses, String name) {
		attrFromClazzes.forEach [ childAttr |
			attrFromSuperClasses.forEach [ parentAttr |
				if (childAttr.card.inf !== parentAttr.card.inf || childAttr.card.sup !== parentAttr.card.sup || childAttr.card.isMany !== parentAttr.card.isMany) {
					error('''Overriding attribute '«name»' with cardinality («childAttr.cardinality») must match the cardinality of the attribute it overrides («parentAttr.cardinality»)''',
						childAttr, ROSETTA_NAMED__NAME, CARDINALITY_ERROR)
				}
			]
		]
	}
	
	protected def cardinality(Attribute attr)
		'''«attr.card.inf»..«IF attr.card.isMany»*«ELSE»«attr.card.sup»«ENDIF»'''
	
	
	private def attributeTypeNames(Iterable<Attribute> attrs) {
		return attrs.map[(eContainer as RosettaNamed).name].join(', ')
	}
	
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
	def checkChoiceRuleAttributesAreUnique(Condition choiceRule) {
		if(!choiceRule.isChoiceRuleCondition) {
			return
		}
		if(choiceRule.constraint !== null && choiceRule.constraint.attributes.size == 1) {
			error('''At least two attributes must be passed to a choice rule.''', choiceRule.constraint, CONSTRAINT__ATTRIBUTES)
			return
		}
		val name2attr = ArrayListMultimap.create
		choiceRule.constraint.attributes.forEach [
			name2attr.put(name, it)
		]
		for (value : choiceRule.constraint.attributes) {
			val attributeByName = name2attr.get(value.name)
			if (attributeByName.size > 1) {
				error('''Duplicate attribute '«value.name»'«»''', ROSETTA_NAMED__NAME, DUPLICATE_CHOICE_RULE_ATTRIBUTE)
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

	// TODO This probably should be made namespace aware
	@Check(FAST) // switch to NORMAL if it becomes slow
	def checkTypeNamesAreUnique(RosettaModel model) {
		val name2attr = HashMultimap.create
		model.elements.filter(RosettaNamed).filter[!(it instanceof FunctionDispatch)].forEach [ // TODO better FunctionDispatch handling
			name2attr.put(name, it)
		]
		val resources = getResourceDescriptions(model.eResource)
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
				val sameNamed = resources.getExportedObjects(toCheck.eClass(), qName, false).filter [
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
						error('''Set to constant type does not match type of field.''', element, ROSETTA_MAPPING__INSTANCES)
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
				return attributeRef.type
		} else if (container instanceof RosettaTyped) {
			 return container.type
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
	def checkFunctionCall(RosettaCallableWithArgsCall element) {
		val callerSize = element.args.size
		val callable = element.callable
		
		var implicitFirstArgument = implicitFirstArgument(element)
		val callableSize = switch callable {
			RosettaExternalFunction: callable.parameters.size
			Function: {
				callable.inputs.size
			}
			default: 0
		}
		if ((callerSize !== callableSize && implicitFirstArgument === null) || (implicitFirstArgument !== null && callerSize + 1 !== callableSize)) {
			error('''Invalid number of arguments. Expecting «callableSize» but passed «callerSize».''', element,
				ROSETTA_CALLABLE_WITH_ARGS_CALL__CALLABLE)
		} else {
			if (callable instanceof Function) {
				val skipFirstParam = if(implicitFirstArgument === null) 0 else 1
				element.args.indexed.forEach [ indexed |
					val callerArg = indexed.value
					val callerIdx = indexed.key
					val param = callable.inputs.get(callerIdx + skipFirstParam)
					checkType(param.type.RType, callerArg, element, ROSETTA_CALLABLE_WITH_ARGS_CALL__ARGS, callerIdx)
					if(!param.card.isMany && cardinality.isMulti(callerArg)) {
						error('''Expecting single cardinality for parameter '«param.name»'.''', element,
							ROSETTA_CALLABLE_WITH_ARGS_CALL__ARGS, callerIdx)
					}
				]
			}
		}
	}
	
	@Check
	def void checkMergeSynonymAttributeCardinality(Attribute attribute) {
		for (syn : attribute.synonyms) {
			val multi = attribute.card.isMany
			if (syn.body?.merge !== null && !multi) {
				error("Merge synonym can only be specified on an attribute with multiple cardinality.", syn.body, ROSETTA_SYNONYM_BODY__MERGE)
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
					error("Merge synonym can only be specified on an attribute with multiple cardinality.", syn.body, ROSETTA_SYNONYM_BODY__MERGE)
				}
			}
		}
	}

	@Check
	def void checkPatternAndFormat(RosettaExternalRegularAttribute attribute) {
		if (!isDateTime(attribute.attributeRef.RType)){
			for(s:attribute.externalSynonyms) {
				checkFormatNull(s.body)
				checkPatternValid(s.body)
			}
		}
		else {
			for(s:attribute.externalSynonyms) {
				checkFormatValid(s.body)
				checkPatternNull(s.body)
			}
		}
	}
	@Check
	def void checkPatternAndFormat(Attribute attribute) {
		if (!isDateTime(attribute.RType)){
			for(s:attribute.synonyms) {
				checkFormatNull(s.body)
				checkPatternValid(s.body)
			}
		}
		else {
			for(s:attribute.synonyms) {
				checkFormatValid(s.body)
				checkPatternNull(s.body)
			}
		}
	}
	
	def checkFormatNull(RosettaSynonymBody body) {
		if (body.format!==null) {
			error("Format can only be applied to date/time types", body, ROSETTA_SYNONYM_BODY__FORMAT)
		}
	}
	
	def checkFormatValid(RosettaSynonymBody body) {
		if (body.format!==null){
			try {
				DateTimeFormatter.ofPattern(body.format)
			} catch (IllegalArgumentException e) {
				error("Format must be a valid date/time format - "+e.message, body, ROSETTA_SYNONYM_BODY__FORMAT)
			}
		}
	}
	
	def checkPatternNull(RosettaSynonymBody body) {
		if (body.patternMatch!==null) {
			error("Pattern cannot be applied to date/time types", body, ROSETTA_SYNONYM_BODY__PATTERN_MATCH)
		}
	}
	
	def checkPatternValid(RosettaSynonymBody body) {
		if (body.patternMatch!==null) {
			try {
				Pattern.compile(body.patternMatch)
			} catch (PatternSyntaxException e) {
				error("Pattern to match must be a valid regular expression - "+e.message, body, ROSETTA_SYNONYM_BODY__PATTERN_MATCH)
			}
		}
	}
	
	
	private def isDateTime(RType rType) {
		#["date", "time", "zonedDateTime"].contains(rType.name)
	}
	
	@Check
	def void checkPatternOnEnum(RosettaEnumSynonym synonym) {
		if (synonym.patternMatch!==null) {
			try {
				Pattern.compile(synonym.patternMatch)
			} catch (PatternSyntaxException e) {
				error("Pattern to match must be a valid regular expression - "+e.message, synonym, ROSETTA_ENUM_SYNONYM__PATTERN_MATCH)
			}
		}
	}

	@Check
	def void checkNodeTypeGraph(RosettaBlueprint bp) {
		try {
			buildTypeGraph(bp.nodes, bp.output)
		} catch (BlueprintUnresolvedTypeException e) {
			error(e.message, e.source, e.getEStructuralFeature, e.code, e.issueData)
		}
	}
	
	@Check
	def void checkExtractCardinality(BlueprintExtract extract) {
		if (extract.repeatable) {
			val multi = cardinality.isMulti(extract.call)
			if (!multi) {
				error("Repeatable keyword must extract multiple cardinality", extract, BLUEPRINT_EXTRACT__REPEATABLE)
			}
		}
	}

	@Check
	def void checkBlueprintFilter(BlueprintFilter filter) {
		if (filter.filter!==null) {
			val exrType = filter.filter.RType
			if (exrType!==RBuiltinType.BOOLEAN) {
				error('''The expression for Filter must return a boolean the current expression returns «exrType.name»''', filter, BLUEPRINT_FILTER__FILTER, TYPE_ERROR)
			}

			val multi = cardinality.isMulti(filter.filter)
			if (multi) {
				error('''The expression for Filter must return a single value the current expression can return multiple values''', filter, BLUEPRINT_FILTER__FILTER)
			}

		}
		else if (filter.filterBP!==null) {
			val node = buildTypeGraph(filter.filterBP.blueprint.nodes, filter.filterBP.output)
			if (!checkBPNodeSingle(node, false)) {
				error('''The expression for Filter must return a single value but the rule «filter.filterBP.blueprint.name» can return multiple values''', filter, BLUEPRINT_FILTER__FILTER_BP)
			}
		}
	}

	@Check
	def void checkReport(RosettaBlueprintReport report) {
		if (report.reportType !== null) {
			checkReportDuplicateRules(report.reportType, newHashSet, newHashSet)
		}
	}

    /**
     * Recursively check all report attributes
     */
    private def void checkReportDuplicateRules(Data dataType, Set<RosettaBlueprint> rules, Set<Data> types) {
        val attrs = dataType.allAttributes
        attrs.forEach[attr|
            val ruleRef = attr.ruleReference
            if(ruleRef !== null) {
                val bp = ruleRef.reportingRule
                // check duplicates
                if (!rules.add(bp)) {
                    error("Duplicate reporting rule " + bp.name, ruleRef, ROSETTA_RULE_REFERENCE__REPORTING_RULE)
                }
            }
            // check nested report attributes types
            val attrType = attr.type
            if (attrType instanceof Data) {
                if (!types.contains(attrType)) {
                    attrType.checkReportDuplicateRules(rules, types)
                }
            }
        ]
    }

	/**
	 * Check all report attribute type and cardinality match the associated reporting rules
	 */
	@Check
	def void checkAttributeRuleReference(Attribute attr) {
		val ruleRef = attr.ruleReference
		if(ruleRef !== null) {
			val bp = ruleRef.reportingRule
			val node = buildTypeGraph(bp.nodes, bp.output)

			val attrExt = attr.toExpandedAttribute
			val attrSingle = attrExt.cardinalityIsSingleValue
			val ruleSingle = checkBPNodeSingle(node, false)

			// check cardinality
			if (attrSingle != ruleSingle) {
				val cardWarning = '''Cardinality mismatch - report field «attr.name» has «IF attrSingle»single«ELSE»multiple«ENDIF» cardinality ''' +
					'''whereas the reporting rule «bp.name» has «IF ruleSingle»single«ELSE»multiple«ENDIF» cardinality.'''
				warning(cardWarning, ruleRef, ROSETTA_RULE_REFERENCE__REPORTING_RULE)
			}
			// check type
			val bpType = node.output?.type?.name
			val bpGenericType = node.output?.genericName
			if (!node.repeatable && (bpType !== null || bpGenericType !== null) && (attr.type.name != bpType && !attr.type.name.equalsIgnoreCase(bpGenericType))) {
				val typeError = '''Type mismatch - report field «attr.name» has type «attr.type.name» ''' +
					'''whereas the reporting rule «bp.name» has type «IF bpType !== null»«bpType»«ELSEIF bpGenericType !== null»«bpGenericType»«ELSE»unknown«ENDIF».'''
				error(typeError, ruleRef, ROSETTA_RULE_REFERENCE__REPORTING_RULE)
			}

			// check basic type cardinality supported
			if (!attrSingle && (attrExt.builtInType || attrExt.enum)) {
				val unsupportedWarning = '''Report attributes with basic type («attr.type.name») and multiple cardinality is not supported.'''
				error(unsupportedWarning, attr, ROSETTA_NAMED__NAME)
			}
		}
	}

	def boolean checkBPNodeSingle(TypedBPNode node, boolean isAlreadyMultiple) {
		val multiple = switch(node.cardinality.get(0)) {
			case UNCHANGED: {isAlreadyMultiple}
			case EXPAND: {
				true
			}
			case REDUCE: {
				false
			}
		}

		if (node.next!==null) {
			return checkBPNodeSingle(node.next, multiple)
		}
		else {
			return !multiple
		}
	}

	@Check
	def checkExpressionCardinality(RosettaBinaryOperation binOp) {
		val leftCard = cardinality.isMulti(binOp.left)
		val rightCard = cardinality.isMulti(binOp.right)
		if (leftCard!=rightCard) {
			if (RosettaOperators.COMPARISON_OPS.contains(binOp.operator) || RosettaOperators.EQUALITY_OPS.contains(binOp.operator)) {
				if (binOp.cardOp===null) {
					warning('''Comparison operator «binOp.operator» should specify 'all' or 'any' when comparing a list to a single value''', binOp, ROSETTA_BINARY_OPERATION__OPERATOR)
				}
			}
		}
		else if (binOp.cardOp!==null) {
			warning('''«binOp.cardOp» is only aplicable when the sides have differing cardinality''', binOp, ROSETTA_BINARY_OPERATION__OPERATOR)
		}
	}

	@Check
	def checkBinaryParamsRightTypes(RosettaBinaryOperation binOp) {
		val resultType = binOp.RType
		if (resultType instanceof RErrorType) {
			error(resultType.message, binOp, ROSETTA_BINARY_OPERATION__OPERATOR)
		}
	}
	
	@Check
	def checkDisjointTypesMatch(RosettaDisjointExpression disjoint) {
		val leftType  = disjoint.container.RType
		val rightType = disjoint.disjoint.RType
		val typesMatch = leftType == rightType //arguable could support leftType.isUsablaAs || rightType.isUsableAs but the generated code doesn't support it
		if (!typesMatch) {
			error('''Disjoint must operate on lists of the same type''', disjoint, ROSETTA_DISJOINT_EXPRESSION__DISJOINT)
		}
	}
	
	@Check
	def checkContainsTypesMatch(RosettaContainsExpression disjoint) {
		val leftType  = disjoint.container.RType
		val rightType = disjoint.contained.RType
		val typesMatch = leftType == rightType //arguable could support leftType.isUsablaAs || rightType.isUsableAs but the generated code doesn't support it
		if (!typesMatch) {
			error('''contains must operate on lists of the same type''', disjoint, ROSETTA_DISJOINT_EXPRESSION__DISJOINT)
		}
	}

	@Check
	def checkFuncDispatchAttr(FunctionDispatch ele) {
		if (ele.attribute !== null && ele.attribute.type !== null && !ele.attribute.type.eIsProxy) {
			if (!(ele.attribute.type instanceof RosettaEnumeration)) {
				error('''Dispatching function may refer to an enumeration typed attributes only. Current type is «ele.attribute.type.name»''', ele,
					FUNCTION_DISPATCH__ATTRIBUTE)
			}
		}
	}
	
	@Check
	def checkData(Data ele) {
		val choiceRules = ele.conditions.filter[isChoiceRuleCondition].groupBy[it.constraint.oneOf]
		val onOfs = choiceRules.get(Boolean.TRUE)
		if (!onOfs.nullOrEmpty) {
			if (onOfs.size > 1) {
				onOfs.forEach [
					error('''Only a single 'one-of' constraint is allowed.''', it.constraint, null)
				]
			} else {
				if (!choiceRules.get(Boolean.FALSE).nullOrEmpty) {
					error('''Type «ele.name» has both choice condition and one-of condition.''', ROSETTA_NAMED__NAME,
						CLASS_WITH_CHOICE_RULE_AND_ONE_OF_RULE)
				}
			}
		}
	}
	
	@Check
	def checkAttribute(Attribute ele) {
		if (ele.type instanceof Data && !ele.type.eIsProxy) {
			if (ele.hasReferenceAnnotation && !(hasKeyedAnnotation(ele.type as Annotated) || (ele.type as Data).allSuperTypes.exists[hasKeyedAnnotation])) {
				//TODO turn to error if it's okay
				warning('''«ele.type.name» must be annotated with [metadata key] as reference annotation is used''',
					ROSETTA_TYPED__TYPE)
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
		if(structured.nullOrEmpty)
			return
		val mostUsedEnum = structured.max[$0.value.size <=> $1.value.size].key
		val toImplement = mostUsedEnum.allEnumValues.map[name].toSet
		enumsUsed.get(mostUsedEnum).forEach[
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
	def checkConstraintNotUsed(Function ele) {
		ele.conditions.filter[constraint !== null].forEach [ cond |
			error('''Constraints: 'one-of' and 'choice' are not supported inside function.''', cond, CONDITION__CONSTRAINT)
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
	def checkAsKeyUsage(AssignOutputOperation ele) {
		if (!ele.assignAsKey) {
			return
		}
		if(ele.path === null) {
			error(''''«grammar.assignOutputOperationAccess.assignAsKeyAsKeyKeyword_6_0.value»' can only be used when assigning an attribute. Example: "assign-output out -> attribute: value as-key"''', ele, ASSIGN_OUTPUT_OPERATION__ASSIGN_AS_KEY)
			return
		}
		val segments = ele.path?.asSegmentList(ele.path)
		val attr =  segments?.last?.attribute
		if(!attr.hasReferenceAnnotation) {
			error(''''«grammar.assignOutputOperationAccess.assignAsKeyAsKeyKeyword_6_0.value»' can only be used with attributes annotated with [metadata reference] annotation.''', segments?.last, SEGMENT__ATTRIBUTE)
		}
	}
	
	@Check
	def checkAsKeyUsage(OutputOperation ele) {
		if (!ele.assignAsKey) {
			return
		}
		if(ele.path === null) {
			error(''''«grammar.assignOutputOperationAccess.assignAsKeyAsKeyKeyword_6_0.value»' can only be used when assigning an attribute. Example: "set out -> attribute: value as-key"''', ele, OUTPUT_OPERATION__ASSIGN_AS_KEY)
			return
		}
		val segments = ele.path?.asSegmentList(ele.path)
		val attr =  segments?.last?.attribute
		if(!attr.hasReferenceAnnotation) {
			error(''''«grammar.assignOutputOperationAccess.assignAsKeyAsKeyKeyword_6_0.value»' can only be used with attributes annotated with [metadata reference] annotation.''', segments?.last, SEGMENT__ATTRIBUTE)
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
		if(!ele.path.nullOrEmpty) {
			val invalidChar = checkPathChars(ele.path)
			if (invalidChar !== null)
				error('''Character '«invalidChar.key»' is not allowed «IF invalidChar.value»as first symbol in a path segment.«ELSE»in paths. Use '->' to separate path segments.«ENDIF»''', ele, ROSETTA_MAP_PATH_VALUE__PATH)
		}
	}
	@Check
	def checkSynonyValuePath(RosettaSynonymValueBase ele) {
		if (!ele.path.nullOrEmpty) {
			val invalidChar = checkPathChars(ele.path)
			if (invalidChar !== null)
				error('''Character '«invalidChar.key»' is not allowed «IF invalidChar.value»as first symbol in a path segment.«ELSE»in paths. Use '->' to separate path segments.«ENDIF»''', ele, ROSETTA_SYNONYM_VALUE_BASE__PATH)
		}
	}
	
	@Check
	def checkCountOpArgument(RosettaCountOperation ele) {
		if (ele.argument !== null && !ele.argument.eIsProxy) {
			if (!cardinality.isMulti(ele.argument))
				error('''Count operation multiple cardinality argument.''', ele, ROSETTA_COUNT_OPERATION__ARGUMENT)
		}
	}
	
	@Check
	def checkFunctionPrefix(Function ele) {
		ele.annotations.forEach[a|
			val prefix = a.annotation.prefix
			if (prefix !== null && !ele.name.startsWith(prefix + "_")) {
				warning('''Function name «ele.name» must have prefix '«prefix»' followed by an underscore.''', ROSETTA_NAMED__NAME, INVALID_ELEMENT_NAME)
			}
		]
	}
	
	@Check
	def checkMetadataAnnotation(Annotated ele) {
		val metadatas = ele.metadataAnnotations
		metadatas.forEach[
			switch(it.attribute?.name) {
				case "key":
					if (!(ele instanceof Data)) {
						error('''[metadata key] annotation only allowed on a type.''', it, ANNOTATION_REF__ATTRIBUTE)
					} 
				case "id":
					if (!(ele instanceof Attribute)) {
						error('''[metadata id] annotation only allowed on an attribute.''', it, ANNOTATION_REF__ATTRIBUTE)
					}
				case "reference":
					if (!(ele instanceof Attribute)) {
						error('''[metadata reference] annotation only allowed on an attribute.''', it, ANNOTATION_REF__ATTRIBUTE)
					}
				case "scheme":
					if (!(ele instanceof Attribute || ele instanceof Data)) {
						error('''[metadata scheme] annotation only allowed on an attribute or a type.''', it, ANNOTATION_REF__ATTRIBUTE)
					}
				case "template":
					if (!(ele instanceof Data)) {
						error('''[metadata template] annotation only allowed on a type.''', it, ANNOTATION_REF__ATTRIBUTE)
					} else if (!metadatas.map[attribute?.name].contains("key")) {
						error('''Types with [metadata template] annotation must also specify the [metadata key] annotation.''', it, ANNOTATION_REF__ATTRIBUTE)
					}
				case "location":
					if (ele instanceof Attribute) {
						if (qualifiers.exists[qualName=="pointsTo"]) {
							error('''pointsTo qualifier belongs on the address not the location.''', it, ANNOTATION_REF__ATTRIBUTE)
						}
					} else {
						error('''[metadata location] annotation only allowed on an attribute.''', it, ANNOTATION_REF__ATTRIBUTE)
					}
				case "address":
					if (ele instanceof Attribute) {
						qualifiers.forEach[
							if (qualName=="pointsTo") {
								//check the qualPath has the address metadata
								switch qualPath {
									RosettaFeatureCall : { 
										val featCall = qualPath as RosettaFeatureCall
										switch att:featCall.feature {
											Attribute : checkForLocation(att, it)
										default : error('''Target of an address must be an attribute''', it, ANNOTATION_QUALIFIER__QUAL_PATH, TYPE_ERROR)
											
										}
									}
									default : error('''Target of an address must be an attribute''', it, ANNOTATION_QUALIFIER__QUAL_PATH, TYPE_ERROR)
								}
								val targetType = qualPath.RType
								val thisType = ele.RType
								if (!targetType.isUseableAs(thisType))
									error('''Expected address target type of '«thisType.name»' but was '«targetType?.name ?: 'null'»'«»''', it, ANNOTATION_QUALIFIER__QUAL_PATH, TYPE_ERROR)
								//Check it has
							}
						]
					} else {
						error('''[metadata address] annotation only allowed on an attribute.''', it, ANNOTATION_REF__ATTRIBUTE)
					}
				}
		]
	}
		
	def checkForLocation(Attribute attribute, AnnotationQualifier checked) {
		var locationFound = !attribute.metadataAnnotations.filter[it.attribute?.name=="location"].empty
		if (!locationFound) {
			error('''Target of address must be annotated with metadata location''', checked, ANNOTATION_QUALIFIER__QUAL_PATH)
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
		
		val annotationType = annotations.head.attribute.type
		val funcOutputType = func.output.type
		
		if (annotationType instanceof Data && funcOutputType instanceof Data) {
			val annotationDataType = annotationType as Data
			val funcOutputDataType = func.output.type as Data
			val funcOutputSuperTypeNames = funcOutputDataType.superType.allSuperTypes.map[name].toSet
			val annotationAttributeTypeNames = annotationDataType.attributes.map[type].map[name].toList
			
			if (annotationDataType.name !== funcOutputDataType.name
				&& !funcOutputSuperTypeNames.contains(annotationDataType.name) // annotation type is a super type of output type
				&& !annotationAttributeTypeNames.contains(funcOutputDataType.name) // annotation type is a parent of the output type (with a one-of condition)
			) {
				warning('''Invalid output type for creation annotation.  The output type must match the type specified in the annotation '«annotationDataType.name»' (or extend the annotation type, or be a sub-type as part of a one-of condition).''', func, FUNCTION__OUTPUT)
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
		val inputType = inputs.get(0).type
		if (inputType === null || inputType.eIsProxy) {
			error('''Invalid input type for qualification function.''', func, FUNCTION__INPUTS)
		} else if (!confExtensions.isRootEventOrProduct(inputType)) {
			warning('''Input type does not match qualification root type.''', func, FUNCTION__INPUTS)
		}
		
		if (RBuiltinType.BOOLEAN.name != func.output?.type?.name) {
	 		error('''Qualification functions must output a boolean.''', func, FUNCTION__OUTPUT)
		}
	}
	
	private def Pair<Character,Boolean> checkPathChars(String str) {
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
		val parents = e.args
				.map[onlyExistsParentType]
				.filter[it !== null]
				.toSet
		if (parents.size > 1) {
			error('''Only exists paths must have a common parent. Found types «parents.join(", ")».''', e, ROSETTA_ONLY_EXISTS_EXPRESSION__ARGS)
		}
	}
	
	@Check
	def checkListOperation(ListOperation o) {
		val receiver = o.receiver
		if (receiver !== null && !receiver.eIsProxy && !cardinality.isMulti(receiver)) {
			// previous step must be single cardinality except when it is a MAP following a ListOperation (such as REDUCE)
			val currentOperationIsMap = o.operationKind === ListOperationKind.MAP
			val previousOperationWasListOperation = receiver instanceof ListOperation
			val previousOperationWasOnlyElement = receiver instanceof RosettaCallableCall && (receiver as RosettaCallableCall).onlyElement
			if (!(currentOperationIsMap && (previousOperationWasListOperation || previousOperationWasOnlyElement))) {
				error('''List «o.operationKind.literal» cannot be used for single cardinality expressions.''', o, LIST_OPERATION__OPERATION_KIND)
			}
		}
		
		if (o.operationKind !== ListOperationKind.MAP && o.operationKind !== ListOperationKind.FILTER && o.operationKind !== ListOperationKind.FLATTEN)  {
			val previousOp = o.previousListOperation
			if (previousOp !== null && previousOp.isOutputListOfLists) {
				error('''List must be flattened before «o.operationKind» operation.''', o, LIST_OPERATION__OPERATION_KIND)
			}
		}
		
		switch (o.operationKind) {
			case FILTER: {
				checkBodyExists(o)
				checkOptionalNamedParameter(o)
				checkBodyType(o, RBuiltinType.BOOLEAN)
			}
			case MAP: {
				checkBodyExists(o)
				checkOptionalNamedParameter(o)
				if (o.isOutputListOfListOfLists) {
					error('''Each list item («o.firstOrImplicit.name») is already a list, mapping the item into a list of lists is not allowed. List map item expression must maintain existing cardinality (e.g. list to list), or reduce to single cardinality (e.g. list to single using expression such as count, sum etc).''', o, LIST_OPERATION__BODY)
				}
			}
			case FLATTEN: {
				checkBodyIsAbsent(o)
				checkNoParameters(o)
				if (!o.isItemMulti) {
					error('''List flatten only allowed for list of lists.''', o, LIST_OPERATION__OPERATION_KIND)
				}
			}
			case REDUCE: {
				checkBodyExists(o)
				checkNumberOfMandatoryNamedParameters(o, 2)
				if (o.inputRawType != o.bodyRawType) {
					error('''List reduce expression must evaluate to the same type as the input.  Found types «o.inputRawType» and «o.bodyRawType».''', o, LIST_OPERATION__BODY)
				}
				checkBodyIsSingleCardinality(o)
			}
			case SUM: {
				checkBodyIsAbsent(o)
				checkNoParameters(o)
				checkInputType(o, RBuiltinType.INT, RBuiltinType.NUMBER)
			}
			case JOIN: {
				// body is optional
				checkInputType(o, RBuiltinType.STRING)	
				checkBodyIsSingleCardinality(o)
				checkBodyExpressionTypeIsRosettaLiteral(o)
				checkBodyType(o, RBuiltinType.STRING)
			}
			case MIN,
			case MAX: {
				// body is optional
				checkOptionalNamedParameter(o)
				checkBodyIsSingleCardinality(o)
				checkBodyIsComparable(o)
				if (o.body === null) {
					checkInputIsComparable(o)
				}
			}
			case SORT: {
				// body is optional
				checkOptionalNamedParameter(o)
				checkBodyIsSingleCardinality(o)
				checkBodyIsComparable(o)
				if (o.body === null) {
					checkInputIsComparable(o)
				}
			}
			case REVERSE: {
				checkBodyIsAbsent(o)
				checkNoParameters(o)
			}
			case FIRST,
			case LAST: {
				checkBodyIsAbsent(o)
				checkNoParameters(o)
			}
			default: {
				// Do nothing
			}
		}
	}
	
	private def void checkNoParameters(ListOperation o) {
		if (o.parameters.size > 0) {
			error('''No item parameter allowed for list «o.operationKind.literal».''', o, LIST_OPERATION__PARAMETERS)
		}
	}
	
	private def void checkOptionalNamedParameter(ListOperation o) {
		if (o.parameters !== null && o.parameters.size !== 0 && o.parameters.size !== 1) {
			error('''List «o.operationKind.literal» must have 1 named parameter.''', o, LIST_OPERATION__PARAMETERS)
		}
	}
	
	private def void checkNumberOfMandatoryNamedParameters(ListOperation o, int max) {
		if (o.parameters === null || o.parameters.size !== max) {
			error('''List «o.operationKind.literal» must have «max» named parameter«IF max > 1»s«ENDIF».''', o, LIST_OPERATION__PARAMETERS)
		}
	}
	
	private def void checkInputType(ListOperation o, RType... type) {
		if (!type.contains(o.receiver.getRType)) {
			error('''List «o.operationKind.literal» input type must be a «type.map[name].join(" or ")».''', o, LIST_OPERATION__BODY)
		}
	}
	
	private def void checkInputIsComparable(ListOperation o) {
		val inputRType = o.receiver.getRType
		if (!inputRType.isComparable) {
			error('''List «o.operationKind.literal» only supports comparable types (string, int, string, date). Found type «inputRType.name».''', o, LIST_OPERATION__BODY)
		}
	}
	
	private def void checkBodyIsAbsent(ListOperation o) {
		if (o.body !== null) {
			error('''No expression allowed for list «o.operationKind.literal».''', o, LIST_OPERATION__OPERATION_KIND)
		}
	}
	
	private def void checkBodyExists(ListOperation o) {
		if (o.body === null) {
			error('''List «o.operationKind.literal» must have an expression specified within square brackets.''', o, LIST_OPERATION__OPERATION_KIND)
		}
	}
	
	private def void checkBodyIsSingleCardinality(ListOperation o) {
	 	if (o.body !== null && o.isBodyExpressionMulti) {
			error('''List «o.operationKind.literal» only supports single cardinality expressions.''', o, LIST_OPERATION__BODY)
		}
	}
	
	private def void checkBodyType(ListOperation o, RType type) {
		if (o.body !== null && o.body.getRType != type) {
			error('''List «o.operationKind.literal» expression must evaluate to a «type.name».''', o, LIST_OPERATION__BODY)
		}
	}
	
	private def void checkBodyIsComparable(ListOperation o) {
		if (o.body !== null) {
			val bodyRType = o.body.getRType
			if (!bodyRType.isComparable) {
				error('''List «o.operationKind.literal» only supports comparable types (string, int, string, date). Found type «bodyRType.name».''', o, LIST_OPERATION__BODY)
			}			
		}
	}
	
	private def void checkBodyExpressionTypeIsRosettaLiteral(ListOperation o) {
		if (o.body !== null && !(o.body instanceof RosettaLiteral)) {
			error('''List «o.operationKind.literal» does not allow expressions.''', o, LIST_OPERATION__BODY)
		}
	}

	
	private def boolean isComparable(RType rType) {
		switch (rType) {
			RBuiltinType,
			RRecordType:
				return true
			default:
				false
		}
	}
	
	@Check
	def checkAssignOutput(Operation o) {
		val expr = o?.expression	
		if (expr instanceof ListOperation) {
			if (expr !== null && expr.isOutputListOfLists) {
				error('''Assign expression contains a list of lists, use flatten to create a list.''', o, OPERATION__EXPRESSION)
			}
		}
	}
	
	@Check
	def checkOutputOperation(OutputOperation o) {
		val isList = o.path !== null ? o.pathAsSegmentList.last.attribute.isFeatureMulti : cardinality.isMulti(o.assignRoot)
		if (o.add && !isList) {
			error('''Add must be used with a list.''', o, OPERATION__ASSIGN_ROOT)
		}
		if (!o.add && isList) {
			info('''Set used with a list. Any existing list items will be overwritten.  Use Add to append items to existing list.''', o, OPERATION__ASSIGN_ROOT)
		}
		if (!isList && cardinality.isMulti(o.expression)) {
			error('''Cardinality mismatch - cannot assign list to a single value.''', o, OPERATION__ASSIGN_ROOT)
		}
	}
	
	private def isFeatureMulti(RosettaFeature feature) {
		switch (feature) {
			Attribute: feature.card.isMany
			default: throw new IllegalStateException('Unsupported type passed ' + feature?.eClass?.name)
		}
	}
	
	@Check
	def checkAlias(ShortcutDeclaration o) {
		val expr = o?.expression	
		if (expr instanceof ListOperation) {
			if (expr !== null && expr.isOutputListOfLists) {
				error('''Alias expression contains a list of lists, use flatten to create a list.''', o, SHORTCUT_DECLARATION__EXPRESSION)
			}
		}
	}
	
	private def getOnlyExistsParentType(RosettaExpression e) {
		switch (e) {
			RosettaFeatureCall: {
				val parentFeatureCall = e.receiver
				switch (parentFeatureCall) {
					RosettaFeatureCall: {
						val parentFeature = parentFeatureCall.feature 
						if (parentFeature instanceof RosettaTypedFeature) {
							return parentFeature.type.name
						}
					}
					RosettaCallableCall: {
						val parentCallable = parentFeatureCall.callable 
						if (parentCallable instanceof Attribute) {
							return parentCallable.type.name
						}
					}
					default: {
						log.warn("Only exists parent type unsupported " + parentFeatureCall)
						return null
					}
						
				}
			}
			default: {
				log.warn("Only exists expression type unsupported " + e)
				return null
			}
		}
	} 

/* 	
	@Inject TargetURIConverter converter
	@Inject IResourceDescriptionsProvider index
	@Inject IReferenceFinder refFinder
	
	@Check(EXPENSIVE)
	def checkNeverUsedModelElement(RosettaModel model) {
		model.elements
			.filter[it instanceof Data || it instanceof RosettaEnumeration || it instanceof Function]
			.forEach[ele |
				val refs = newHashSet
				val resSet = ele.eResource.resourceSet
				
				refFinder.findAllReferences(converter.fromIterable(#[ele.URI]), 
					[ targetURI, work | work.exec(resSet) ], 
					index.getResourceDescriptions(resSet), new IReferenceFinder.Acceptor() {
						override accept(IReferenceDescription description) {
							refs.add(description)
						}
		
						override accept(EObject source, URI sourceURI, EReference eReference, int index, EObject targetOrProxy, URI targetURI) {
							refs.add(new DefaultReferenceDescription(EcoreUtil2.getFragmentPathURI(source), targetURI, eReference, index, null))
						}
		
					}, 
					new NullProgressMonitor)
				
				if (refs.filter[filterEnumValueReferences(ele, it)].empty) {
					warning('''«(ele as RosettaNamed).name» is never used.''', ele, ROSETTA_NAMED__NAME)
				}
			]
	}
	
	// RosettaEnumeration are referenced by their own RosettaEnumValue, so exclude those to actually determine if the enum is unused
	private def filterEnumValueReferences(RosettaRootElement ele, IReferenceDescription ref) {
		if (ele instanceof RosettaEnumeration) {
			if (ref instanceof DefaultReferenceDescription) {
				val refType = ref?.EReference?.EContainingClass?.instanceClass
				if (refType !== null) {
					return !refType.isAssignableFrom(RosettaEnumValue)
				}
			}
		}
		return true
	}
*/
}