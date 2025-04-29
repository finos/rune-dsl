package com.regnosys.rosetta.generator.java.object

import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.rosetta.model.lib.expression.ComparisonResult
import com.rosetta.model.lib.expression.ExpressionOperators
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.validation.ExistenceChecker
import com.rosetta.model.lib.validation.ValidationResult
import com.rosetta.model.lib.validation.ValidationResult.ValidationType
import com.rosetta.model.lib.validation.Validator
import com.rosetta.model.lib.validation.ValidatorWithArg
import java.util.Map
import java.util.Set
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.types.builtin.RStringType
import com.regnosys.rosetta.types.builtin.RNumberType
import com.regnosys.rosetta.types.builtin.RBuiltinTypeService
import java.util.Optional
import java.util.regex.Pattern
import org.apache.commons.text.StringEscapeUtils
import java.math.BigDecimal
import jakarta.inject.Inject
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import java.util.List
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RCardinality
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression

import com.regnosys.rosetta.generator.java.JavaIdentifierRepresentationService
import com.regnosys.rosetta.types.AliasHierarchy
import com.regnosys.rosetta.generator.GeneratedIdentifier
import com.google.common.collect.Streams
import java.util.ArrayList
import com.regnosys.rosetta.generator.java.statement.builder.JavaVariable
import com.regnosys.rosetta.generator.java.statement.JavaStatement
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue
import com.regnosys.rosetta.generator.java.statement.JavaBlock
import com.regnosys.rosetta.generator.java.statement.JavaForLoop
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement
import com.rosetta.util.types.JavaPrimitiveType
import com.regnosys.rosetta.generator.java.expression.InterpreterValueJavaConverter
import com.regnosys.rosetta.generator.java.expression.TypeCoercionService
import com.regnosys.rosetta.generator.java.statement.builder.JavaStatementBuilder
import com.rosetta.util.types.JavaType
import com.regnosys.rosetta.types.RAliasType
import com.regnosys.rosetta.generator.java.types.JavaConditionInterface

class ValidatorsGenerator {

	@Inject extension ImportManagerExtension
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem
	@Inject extension RBuiltinTypeService
	@Inject extension JavaTypeUtil
	@Inject extension JavaIdentifierRepresentationService
	@Inject extension InterpreterValueJavaConverter
	@Inject extension TypeCoercionService

	def generate(RootPackage root, IFileSystemAccess2 fsa, RDataType type, String version) {
		val javaType = type.toJavaReferenceType
		val attrs = type.allAttributes
		fsa.generateFile(javaType.toValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateClass(root, javaType, attrs, version))
		fsa.generateFile(javaType.toTypeFormatValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateTypeFormatValidator(root, javaType, attrs, version))
		fsa.generateFile(javaType.toOnlyExistsValidatorClass.canonicalName.withForwardSlashes + ".java",
			generateOnlyExistsValidator(root, javaType, attrs, version))
	}

	private def generateClass(RootPackage root, JavaPojoInterface javaType, Iterable<RAttribute> attributes, String version) {
		val scope = new JavaScope(root.typeValidation)
		buildClass(root.typeValidation, javaType.classBody(version, attributes), scope)
	}
	
	private def generateTypeFormatValidator(RootPackage root, JavaPojoInterface javaType, Iterable<RAttribute> attributes, String version) {
		val scope = new JavaScope(root.typeValidation)
		buildClass(root.typeValidation, javaType.typeFormatClassBody(version, attributes), scope)
	}

	private def generateOnlyExistsValidator(RootPackage root, JavaPojoInterface javaType, Iterable<RAttribute> attributes, String version) {
		val scope = new JavaScope(root.existsValidation)
		buildClass(root.existsValidation, javaType.onlyExistsClassBody(version, attributes), scope)
	}

	def private StringConcatenationClient classBody(JavaPojoInterface javaType, String version, Iterable<RAttribute> attributes) '''
		public class «javaType.toValidatorClass» implements «Validator»<«javaType»> {
		
			private «List»<«ComparisonResult»> getComparisonResults(«javaType» o) {
				return «Lists».<«ComparisonResult»>newArrayList(
						«FOR attrCheck : attributes.map[checkCardinality(javaType, it)].filter[it !== null] SEPARATOR ", "»
							«attrCheck»
						«ENDFOR»
					);
			}

			@Override
			public «List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» path, «javaType» o) {
				return getComparisonResults(o)
					.stream()
					.map(res -> {
						if (!«method(Strings, "isNullOrEmpty")»(res.getError())) {
							return «method(ValidationResult, "failure")»("«javaType.rosettaName»", «ValidationResult.ValidationType».CARDINALITY, "«javaType.rosettaName»", path, "", res.getError());
						}
						return «method(ValidationResult, "success")»("«javaType.rosettaName»", «ValidationResult.ValidationType».CARDINALITY, "«javaType.rosettaName»", path, "");
					})
					.collect(«method(Collectors, "toList")»());
			}
		
		}
	'''
	
	def private StringConcatenationClient typeFormatClassBody(JavaPojoInterface javaType, String version, Iterable<RAttribute> attributes) {
		val validatorClass = javaType.toTypeFormatValidatorClass
		val packageScope = new JavaScope(validatorClass.packageName)
		val classScope = packageScope.classScope(validatorClass.simpleName)
		
		val pathId = classScope.createUniqueIdentifier("path")
		
		val aliasHierarchyPerAttribute = attributes.map[it->RMetaAnnotatedType.RType.computeAliasHierarchy].toMap([key], [value])
		val conditionDependencies = aliasHierarchyPerAttribute.values.flatMap[aliases].flatMap[conditions].map[toConditionJavaClass].toSet
		
		val runConditionsScope = classScope.methodScope("runConditions")
		val instanceId = runConditionsScope.createUniqueIdentifier("o")
		val instanceVar = new JavaVariable(instanceId, javaType)
		val resultsId = runConditionsScope.createUniqueIdentifier("results")
			
		'''
		public class «validatorClass» implements «Validator»<«javaType»> {
			«FOR dep : conditionDependencies»
				@«javax.inject.Inject»
				protected «dep» «classScope.createIdentifier(dep.toDependencyInstance, dep.simpleName.toFirstLower)»;
			«ENDFOR»
		
			private «List»<«ComparisonResult»> getComparisonResults(«javaType» o) {
				return «Lists».<«ComparisonResult»>newArrayList(
						«FOR attrCheck : attributes.flatMap[checkTypeFormat(javaType, it)] SEPARATOR ", "»
							«attrCheck»
						«ENDFOR»
					);
			}
			«IF !conditionDependencies.empty»
			
			private «List»<«ValidationResult»<?>> runConditions(«RosettaPath» «pathId», «javaType» «instanceId») {
				«List»<«ValidationResult»<?>> «resultsId» = new «ArrayList»();
				«FOR condCheck : attributes.map[checkTypeConditions(javaType, it, aliasHierarchyPerAttribute.get(it), pathId, instanceVar, resultsId, classScope)]»
				«condCheck.asStatementList»
				«ENDFOR»
				return «resultsId»;
			}
			«ENDIF»
		
			@Override
			public «List»<«ValidationResult»<?>> getValidationResults(«RosettaPath» «pathId», «javaType» o) {
				«IF conditionDependencies.empty»
				return getComparisonResults(o)
					.stream()
					.map(res -> {
						if (!«method(Strings, "isNullOrEmpty")»(res.getError())) {
							return «method(ValidationResult, "failure")»("«javaType.rosettaName»", «ValidationResult.ValidationType».TYPE_FORMAT, "«javaType.rosettaName»", «pathId», "", res.getError());
						}
						return «method(ValidationResult, "success")»("«javaType.rosettaName»", «ValidationResult.ValidationType».TYPE_FORMAT, "«javaType.rosettaName»", «pathId», "");
					})
					.collect(«method(Collectors, "toList")»());
				«ELSE»
				return «Streams».concat(getComparisonResults(o)
						.stream()
						.map(res -> {
							if (!«method(Strings, "isNullOrEmpty")»(res.getError())) {
								return «method(ValidationResult, "failure")»("«javaType.rosettaName»", «ValidationResult.ValidationType».TYPE_FORMAT, "«javaType.rosettaName»", «pathId», "", res.getError());
							}
							return «method(ValidationResult, "success")»("«javaType.rosettaName»", «ValidationResult.ValidationType».TYPE_FORMAT, "«javaType.rosettaName»", «pathId», "");
						}),
						runConditions(«pathId», o).stream()
					)
					.collect(«method(Collectors, "toList")»());
				«ENDIF»
			}
		
		}
		'''
	}

	def private StringConcatenationClient onlyExistsClassBody(JavaPojoInterface javaType, String version, Iterable<RAttribute> attributes) {
		
		'''
		public class «javaType.toOnlyExistsValidatorClass» implements «ValidatorWithArg»<«javaType», «Set»<String>> {

			/* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			@Override
			public <T2 extends «javaType»> «ValidationResult»<«javaType»> validate(«RosettaPath» path, T2 o, «Set»<String> fields) {
				«Map»<String, Boolean> fieldExistenceMap = «ImmutableMap».<String, Boolean>builder()
						«FOR attr : attributes»
							«val prop = javaType.findProperty(attr.name)»
							«val propCode = prop.applyGetter(JavaExpression.from('''o''', javaType))»
							.put("«prop.name»", «ExistenceChecker».isSet((«prop.type») «propCode»))
						«ENDFOR»
						.build();
				
				// Find the fields that are set
				«Set»<String> setFields = fieldExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.map(Map.Entry::getKey)
						.collect(«Collectors».toSet());
				
				if (setFields.equals(fields)) {
					return «method(ValidationResult, "success")»("«javaType.rosettaName»", «ValidationType».ONLY_EXISTS, "«javaType.rosettaName»", path, "");
				}
				return «method(ValidationResult, "failure")»("«javaType.rosettaName»", «ValidationType».ONLY_EXISTS, "«javaType.rosettaName»", path, "",
						String.format("[%s] should only be set.  Set fields: %s", fields, setFields));
			}
		}
		'''
	}

	private def StringConcatenationClient checkCardinality(JavaPojoInterface javaType, RAttribute attr) {
		if (attr.cardinality == RCardinality.UNBOUNDED) {
			null
		} else {
			val prop = javaType.findProperty(attr.name)
			val propCode = prop.applyGetter(JavaExpression.from('''o''', javaType));
	        /* Casting is required to ensure types are output to ensure recompilation in Rosetta */
			'''
			«IF attr.isMulti»
				«method(ExpressionOperators, "checkCardinality")»("«attr.name»", («prop.type») «propCode» == null ? 0 : «propCode».size(), «attr.cardinality.min», «attr.cardinality.max.orElse(0)»)
			«ELSE»
				«method(ExpressionOperators, "checkCardinality")»("«attr.name»", («prop.type») «propCode» != null ? 1 : 0, «attr.cardinality.min», «attr.cardinality.max.orElse(0)»)
			«ENDIF»
			'''
		}
	}
		
	private def List<StringConcatenationClient> checkTypeFormat(JavaPojoInterface javaType, RAttribute attr) {
		val List<StringConcatenationClient> checks = newArrayList
		
		val t = attr.RMetaAnnotatedType.RType.stripFromTypeAliases
		if (t instanceof RStringType) {
			if (t != UNCONSTRAINED_STRING) {
				val min = t.interval.minBound
				val max = t.interval.max.optional
				val pattern = t.pattern.optionalPattern
								
				checks.add('''«method(ExpressionOperators, "checkString")»("«attr.name»", «javaType.getAttributeValue(attr)», «min», «max», «pattern»)''')
			}
		} else if (t instanceof RNumberType) {
			if (t != UNCONSTRAINED_NUMBER) {
				val digits = t.digits.optional
				val fractionalDigits = t.fractionalDigits.optional
				val min = t.interval.min.optionalBigDecimal
				val max = t.interval.max.optionalBigDecimal
				
				checks.add('''«method(ExpressionOperators, "checkNumber")»("«attr.name»", «javaType.getAttributeValue(attr)», «digits», «fractionalDigits», «min», «max»)''')
			}
		}
		
		return checks
	}
	
	private def JavaStatement checkTypeConditions(JavaPojoInterface javaType, RAttribute attr, AliasHierarchy hierarchy, GeneratedIdentifier pathId, JavaVariable instanceVar, GeneratedIdentifier resultsId, JavaScope scope) {
		if (!attr.isMulti) {
			var conditionCalls = JavaBlock.EMPTY
			for (alias : hierarchy.aliases) {
				for (condition : alias.conditions) {
					val conditionClass = condition.toConditionJavaClass
					val prop = javaType.findProperty(attr.name, conditionClass.instanceClass)
					conditionCalls = conditionCalls.append(
						addConditionValidationResultsCode(
							resultsId,
							'''«pathId».newSubPath("«attr.name»")''',
							prop.applyGetter(JavaExpression.from('''o''', javaType)),
							alias,
							conditionClass,
							scope
						)
					)
				}
			}
			return conditionCalls
		} else {
			if (!hierarchy.aliases.flatMap[conditions].empty) {
				val prop = javaType.findProperty(attr.name)
				val attrVarExpr = prop.applyGetter(JavaExpression.from('''o''', javaType))
				val forIndex = scope.createUniqueIdentifier("i")
				return attrVarExpr.declareAsVariable(true, attr.name, scope)
					.complete[attrVar|
						var forBody = JavaBlock.EMPTY
						for (alias : hierarchy.aliases) {
							for (condition : alias.conditions) {
								forBody = forBody.append(
									addConditionValidationResultsCode(
										resultsId,
										'''«pathId».newSubPath("«attr.name»").withIndex(«forIndex»)''',
										JavaExpression.from('''«attrVar».get(«forIndex»)''', attrVar.expressionType.itemType),
										alias,
										condition.toConditionJavaClass,
										scope
									)
								)
							}
						}
						new JavaForLoop(
							new JavaLocalVariableDeclarationStatement(false, JavaPrimitiveType.INT, forIndex, JavaExpression.from('''0''', JavaPrimitiveType.INT)),
							JavaExpression.from('''«forIndex» < «attrVar».size()''', JavaPrimitiveType.BOOLEAN),
							JavaExpression.from('''«forIndex»++''', JavaPrimitiveType.INT),
							forBody
						)
					]
			}
			return JavaBlock.EMPTY
		}
	}
	private def JavaStatement addConditionValidationResultsCode(GeneratedIdentifier resultsId, StringConcatenationClient pathCode, JavaExpression attributeItemCode, RAliasType alias, JavaConditionInterface conditionClass, JavaScope scope) {
		val conditionVar = scope.getIdentifierOrThrow(conditionClass.toDependencyInstance)
		val arguments = newArrayList
					
		arguments.add(JavaExpression.from(pathCode, JavaType.from(RosettaPath)))
		arguments.add(attributeItemCode
			.addCoercions(conditionClass.instanceClass, scope))
		conditionClass.parameters.forEach[param,type|
			arguments.add(
				alias.arguments.get(param).convertValueToJava
					.addCoercions(type, scope)
			)
		]
		
		return JavaStatementBuilder.invokeMethod(
			arguments,
			[args|JavaExpression.from('''«resultsId».addAll(«conditionVar».getValidationResults(«args»))''', JavaPrimitiveType.VOID)],
			scope
		).completeAsExpressionStatement
	}
	
	private def JavaExpression getAttributeValue(JavaPojoInterface javaType, RAttribute attr) {
		val prop = javaType.findProperty(attr.name)
		val propCode = prop.applyGetter(JavaExpression.from('''o''', javaType));
		val propType = propCode.expressionType
		val propItemType = propType.itemType
		if (propItemType instanceof RJavaWithMetaValue) {
			if (propType.isList) {
				JavaExpression.from('''«propCode».stream().map(«propItemType»::getValue).collect(«Collectors».toList())''', LIST.wrap(propItemType.valueType))
			} else {
				JavaExpression.from('''«propCode».getValue()''', propItemType.valueType)
			}
		} else {
			propCode
		}
	}
	private def StringConcatenationClient optional(Optional<? extends Object> v) {
		if (v.isPresent) {
			'''«method(Optional, "of")»(«v.get»)'''
		} else {
			'''«method(Optional, "empty")»()'''
		}
	}
	private def StringConcatenationClient optionalPattern(Optional<Pattern> v) {
		if (v.isPresent) {
			'''«method(Optional, "of")»(«Pattern».compile("«StringEscapeUtils.escapeJava(v.get.toString)»"))'''
		} else {
			'''«method(Optional, "empty")»()'''
		}
	}
	private def StringConcatenationClient optionalBigDecimal(Optional<BigDecimal> v) {
		if (v.isPresent) {
			'''«method(Optional, "of")»(new «BigDecimal»("«StringEscapeUtils.escapeJava(v.get.toString)»"))'''
		} else {
			'''«method(Optional, "empty")»()'''
		}
	}
}
