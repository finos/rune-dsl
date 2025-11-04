package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.utils.RosettaConfigExtension
import com.rosetta.model.lib.annotations.RosettaMeta
import com.rosetta.model.lib.qualify.QualifyFunctionFactory
import com.rosetta.model.lib.qualify.QualifyResult
import com.rosetta.model.lib.validation.Validator
import com.rosetta.model.lib.validation.ValidatorWithArg
import java.util.Arrays
import java.util.Collections
import java.util.List
import java.util.Set

import com.rosetta.model.lib.validation.ValidatorFactory
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil
import jakarta.inject.Inject
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.types.RObjectFactory

class ModelMetaGenerator extends RObjectJavaClassGenerator<RDataType, RGeneratedJavaClass<?>> {

	@Inject RosettaConfigExtension confExt
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension JavaTypeTranslator
	@Inject extension ModelGeneratorUtil
	@Inject extension RObjectFactory
	
	override protected streamObjects(RosettaModel model) {
		model.elements.stream.filter[it instanceof Data].map[it as Data].map[buildRDataType]
	}
	override protected createTypeRepresentation(RDataType t) {
		t.toJavaReferenceType.toJavaMetaDataClass
	}
	override protected generateClass(RDataType t, RGeneratedJavaClass<?> metaClass, String version, JavaClassScope scope) {
		val dataClass = t.toJavaType
		val validator = dataClass.toValidatorClass
		val typeFormatValidator = dataClass.toTypeFormatValidatorClass
		val onlyExistsValidator = dataClass.toOnlyExistsValidatorClass
		val context = t.EObject.eResource.resourceSet
		val qualifierFuncs = qualifyFuncs(t.EObject, context.resources.map[contents.head as RosettaModel].toSet)
		val conditionClasses = t.allSuperTypes.flatMap[it.EObject.conditions.map[toConditionJavaClass]]
		'''
			«emptyJavadocWithVersion(version)»
			@«RosettaMeta»(model=«dataClass».class)
			public «metaClass.asClassDeclaration» {
			
				@Override
				public «List»<«Validator»<? super «dataClass»>> dataRules(«ValidatorFactory» factory) {
					return «Arrays».asList(
						«FOR conditionClass : conditionClasses SEPARATOR ','»
							factory.<«conditionClass.instanceClass»>create(«conditionClass».class)
						«ENDFOR»
					);
				}
				
				@Override
				public «List»<«java.util.function.Function»<? super «dataClass», «QualifyResult»>> getQualifyFunctions(«QualifyFunctionFactory» factory) {
					«IF !qualifierFuncs.nullOrEmpty»
					return Arrays.asList(
						«FOR qf : qualifierFuncs SEPARATOR ','»
							factory.<«dataClass»>create(«qf.toFunctionJavaClass».class)
						«ENDFOR»
					);
					«ELSE»
					return «Collections».emptyList();
					«ENDIF»
				}
				
				@Override
				public «Validator»<? super «dataClass»> validator(«ValidatorFactory» factory) {
					return factory.<«dataClass»>create(«validator».class);
				}

				@Override
				public «Validator»<? super «dataClass»> typeFormatValidator(«ValidatorFactory» factory) {
					return factory.<«dataClass»>create(«typeFormatValidator».class);
				}

				@Deprecated
				@Override
				public «Validator»<? super «dataClass»> validator() {
					return new «validator»();
				}

				@Deprecated
				@Override
				public «Validator»<? super «dataClass»> typeFormatValidator() {
					return new «typeFormatValidator»();
				}
				
				@Override
				public «ValidatorWithArg»<? super «dataClass», «Set»<String>> onlyExistsValidator() {
					return new «onlyExistsValidator»();
				}
			}
		'''
	}
	
	private def Set<Function> qualifyFuncs(Data type, Set<RosettaModel> models) {
		// TODO: make sure this method doesn't need to go through all models in the resource set
		if(!confExt.isRootEventOrProduct(type)) {
			return emptySet
		}
		val funcs = models.flatMap[elements].filter(Function).toSet
		return funcs.filter[funcExt.isQualifierFunctionFor(it,type)].toSet
	}
	
}
