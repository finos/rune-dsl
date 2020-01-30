
package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Iterables
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.generator.object.ExpandedSynonym
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaClassSynonym
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaSynonymBase
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.types.RQualifiedType
import com.regnosys.rosetta.utils.RosettaConfigExtension
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaImportSet
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class ModelObjectGenerator {
	
	@Inject extension RosettaExtensions
	@Inject extension ModelObjectBoilerPlate
	@Inject extension RosettaConfigExtension
	@Inject extension ModelObjectBuilderGenerator
	@Inject extension ImportManagerExtension

	def generate(JavaNames javaNames, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version) {
		elements.filter(RosettaClass).forEach [ RosettaClass clazz |
			fsa.generateFile(javaNames.packages.model.directoryName + '/' + clazz.name + '.java', generateRosettaClass(javaNames, clazz, version))
			// TODO think about skipping the validation if lazz.attributes == 0 and provide an EmptyValidator
			fsa.generateFile(javaNames.packages.model.typeValidation.directoryName + '/' + clazz.name + 'Validator.java', validatorImpl(javaNames.packages, clazz))
			
			fsa.generateFile(javaNames.packages.model.existsValidation.directoryName + '/' + onlyExistsValidatorName(clazz) + '.java', onlyExistsValidator(javaNames.packages, clazz))
		]
	}

	private def static hasSuperType(RosettaClass c) {
		c.superType !== null
	}
		
	private def static hasStereotypes(RosettaClass c) {
		c?.stereotype !== null
	}
	
	private def hasAnySynonyms(RosettaClass c) {
		(c.synonyms !== null && !c.synonyms.empty) || (c.expandedAttributes.map[synonyms].flatten.size > 0)
	}
	
	private def static hasSynonymPath(RosettaSynonymBase p) {
		return hasSynonymPath(p.toRosettaExpandedSynonym)
	}
	
	private def static hasSynonymPath(ExpandedSynonym synonym) {
		synonym !== null && synonym.values.exists[path!==null]
	}

	private def  generateRosettaClass(JavaNames javaNames, RosettaClass c, String version) {
		
		val clazz = tracImports(classBody(javaNames,c,version))
		'''
			package «javaNames.packages.model.name»;
			
			«FOR imp : clazz.imports»
				import «imp»;
			«ENDFOR»

			«FOR imp : clazz.staticImports»
				import static «imp»;
			«ENDFOR»
			
			«imports(javaNames.packages, c)»
			
			«clazz.toString»
		'''
	}
	
	private def StringConcatenationClient classBody(JavaNames javaNames, RosettaClass c, String version) {
		'''
			«javadocWithVersion(c.definition, version)»
			«IF c.isRoot»
				@RosettaRoot
			«ENDIF»
			@RosettaClass
			«IF c.hasQualifiedAttribute»
				@RosettaQualified(attribute="«c.qualifiedAttribute»",qualifiedClass=«c.qualifiedClass».class)
			«ENDIF»
			«FOR stereotype : c.stereotype?.values?.map[it?.name]?:emptyList»
				@RosettaStereotype("«stereotype»")
			«ENDFOR»
			«contributeClassSynonyms(c.synonyms)»
			public «IF c.isAbstract »abstract «ENDIF»class «c.name» extends «IF c.hasSuperType »«c.superType.name»«ELSE»RosettaModelObject«ENDIF» «c.implementsClause»{
				«c.rosettaClass(javaNames)»
			
				«c.staticBuilderMethod»
			
				«c.builderClass(javaNames)»
			
				«c.boilerPlate(javaNames)»
			}
		'''
	}
	private  def imports(RosettaJavaPackages packages, RosettaClass c) '''
		«IF !c.hasSuperType »
			import «packages.defaultLib.name».RosettaModelObject;
		«ENDIF»
		import «packages.defaultLib.name».RosettaModelObjectBuilder;
		import «packages.defaultLib.name».meta.RosettaMetaData;
		import «packages.model.name».meta.«c.name»Meta;
		«IF c.allSuperTypes.map[expandedAttributes].flatten.exists[hasMetas]»
			import «packages.model.metaField.name».*;
			import com.rosetta.model.lib.meta.ReferenceWithMeta;
			import com.rosetta.model.lib.meta.FieldWithMeta;
		«ENDIF»
		import «packages.defaultLibAnnotations.name».RosettaClass;
		import com.rosetta.model.lib.path.RosettaPath;
		import com.rosetta.model.lib.process.*;

		«IF c.hasQualifiedAttribute»
			import «packages.defaultLibAnnotations.name».RosettaQualified;
		«ENDIF»
		«IF c.hasStereotypes»
			import «packages.defaultLibAnnotations.name».RosettaStereotype;
		«ENDIF»
		«IF c.hasAnySynonyms»
			import «packages.defaultLibAnnotations.name».RosettaSynonym;
		«ENDIF»
		«IF c.globalKeyRecursive»
			import «packages.model.metaField.name».MetaFields;
			import «packages.defaultLib.name».GlobalKey;
			import «packages.defaultLib.name».GlobalKeyBuilder;
		«ENDIF»
		«IF c.rosettaKeyValue»
			import «packages.defaultLib.name».RosettaKeyValue;
			import «packages.defaultLib.name».RosettaKeyValueBuilder;
		«ENDIF»
		«IF c.isRoot»
			import «packages.defaultLibAnnotations.name».RosettaRoot;
		«ENDIF»
		import «packages.defaultLib.name».qualify.Qualified;
		import com.rosetta.util.ListEquals;
		
		«FOR toImport : c.toJavaImportSet(packages)»
			import «toImport»;
		«ENDFOR»	'''
	
	def boolean globalKeyRecursive(RosettaClass class1) {
		return class1.globalKey || (class1.superType !== null && class1.superType.globalKeyRecursive)
	}

	def private hasQualifiedAttribute(RosettaClass c) {
		c.qualifiedAttribute !== null && c.qualifiedClass !== null
	}
	
	def private getQualifiedAttribute(RosettaClass c) {
		c.allSuperTypes.expandedAttributes.findFirst[qualified]?.name
	}
	
	def private getQualifiedClass(RosettaClass c) {
		val allExpandedAttributes = c.allSuperTypes.expandedAttributes
		if(!allExpandedAttributes.stream.anyMatch[qualified])
			return null
		
		val qualifiedClassType = allExpandedAttributes.findFirst[qualified].type.name
		var qualifiedRootClassName = switch qualifiedClassType { 
			case RQualifiedType.PRODUCT_TYPE.qualifiedType: c.findProductRootName
			case RQualifiedType.EVENT_TYPE.qualifiedType: c.findEventRootName
			default: throw new IllegalArgumentException("Unknown qualifiedType " + qualifiedClassType)
		}
		
		if(qualifiedRootClassName === null || qualifiedRootClassName.length == 0)
			throw new IllegalArgumentException("QualifiedType " + qualifiedClassType + " must have qualifiable root class")
			
		return qualifiedRootClassName
	}

	private def StringConcatenationClient rosettaClass(RosettaClass c, JavaNames names) '''
		«FOR attribute : c.expandedAttributes»
			private final «attribute.toJavaType(names)» «attribute.name»;
		«ENDFOR»
		private static «c.name»Meta metaData = new «c.name»Meta();

		«c.name»(«c.builderName» builder) {
			«IF hasSuperType(c)»
				super(builder);
			«ENDIF»
			«FOR attribute : c.expandedAttributes»
				this.«attribute.name» = «attribute.attributeFromBuilder»;
			«ENDFOR»
		}

		«FOR attribute : c.expandedAttributes»
			«javadoc(attribute.definition)»
			«contributeSynonyms(attribute.synonyms)»
			public final «attribute.toJavaType(names)» get«attribute.name.toFirstUpper»() {
				return «attribute.name»;
			}
			
		«ENDFOR»
		
		@Override
		public RosettaMetaData<? extends «c.name»> metaData() {
			return metaData;
		} 

		«IF !c.isAbstract»
			public «c.builderName» toBuilder() {
				«c.name»Builder builder = new «c.name»Builder();
				«FOR attribute : c.getAllSuperTypes.map[expandedAttributes].flatten»
					«IF attribute.cardinalityIsListValue»
						ofNullable(get«attribute.name.toFirstUpper»()).ifPresent(«attribute.name» -> «attribute.name».forEach(builder::add«attribute.name.toFirstUpper»));
					«ELSE»
						ofNullable(get«attribute.name.toFirstUpper»()).ifPresent(builder::set«attribute.name.toFirstUpper»);
					«ENDIF»
				«ENDFOR»
				return builder;
			}
		«ELSE»
			public abstract «c.builderName» toBuilder();
		«ENDIF»
	'''
	
	private def StringConcatenationClient toJavaType(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.isMultiple) '''«List»<«DataGenerator.toJavaTypeSingle(attribute, names)»>''' 
		else DataGenerator.toJavaTypeSingle(attribute, names)
	}

	
	
	private def contributeSynonyms(List<ExpandedSynonym> synonyms) '''		
		«FOR synonym : synonyms »
					«val maps = if (synonym.values.exists[v|v.maps>1]) ''', maps=«synonym.values.map[maps].join(",")»''' else ''»
					«FOR source : synonym.sources»
						«IF !synonym.hasSynonymPath»
							@RosettaSynonym(value="«synonym.values.map[name].join(",")»", source="«source.getName»"«maps»)
						«ELSE»
							«FOR value : synonym.values»
								@RosettaSynonym(value="«value.name»", source="«source.getName»", path="«value.path»"«maps»)
							«ENDFOR»
						«ENDIF»
					«ENDFOR»
				«ENDFOR»
	'''
	
	private def contributeClassSynonyms(List<RosettaClassSynonym> synonyms) '''		
		«FOR synonym : synonyms.filter[value!==null] »
			«val path = if (hasSynonymPath(synonym)) ''', path="«synonym.value.path»" ''' else ''»
			«val maps = if (synonym.value.maps > 0) ''', maps=«synonym.value.maps»''' else ''»
			
			«FOR source : synonym.sources»
				@RosettaSynonym(value="«synonym.value.name»", source="«source.getName»"«path»«maps»)
			«ENDFOR»
		«ENDFOR»
	'''

	private def staticBuilderMethod(RosettaClass c) '''
		«IF !c.isAbstract»
			public static «builderName(c)» builder() {
				return new «builderName(c)»();
			}
		«ENDIF»
	'''

	private def attributeFromBuilder(ExpandedAttribute attribute) {
		if(attribute.isRosettaClassOrData || attribute.hasMetas) {
			'''ofNullable(builder.get«attribute.name.toFirstUpper»()).map(«attribute.buildRosettaObject»).orElse(null)'''
		} else {
			'''builder.get«attribute.name.toFirstUpper»()'''
		} 
	}
	
	private def buildRosettaObject(ExpandedAttribute attribute) {		
		if(attribute.cardinalityIsListValue) {
			'''list -> list.stream().filter(Objects::nonNull).map(f->f.build()).filter(Objects::nonNull).collect(Collectors.toList())'''
		} else {
			'''f->f.build()'''
		}
	}

	private def validatorImpl(RosettaJavaPackages packages, RosettaClass c) '''
		package «packages.model.typeValidation.name»;
		
		import static «packages.defaultLibValidation.name».ValidatorHelper.checkCardinality;
		import static java.util.Collections.emptyList;
		import static java.util.stream.Collectors.joining;
		import static java.util.Optional.ofNullable;
		import static com.google.common.base.Strings.isNullOrEmpty;
		import static «packages.defaultLibValidation.name».ValidationResult.failure;
		import static «packages.defaultLibValidation.name».ValidationResult.success;
		

		import «packages.model.name».«c.name»;
		
		import com.google.common.collect.Lists;
		import «packages.defaultLibValidation.name».ComparisonResult;
		import «packages.defaultLibValidation.name».ValidationResult;
		import «packages.defaultLibValidation.name».ValidationResult.ValidationType;
		import «packages.defaultLibValidation.name».Validator;
		import «packages.defaultLib.name».path.RosettaPath;
		import «packages.defaultLib.name».RosettaModelObjectBuilder;
		
		
		public class «c.name»Validator implements Validator<«c.name»> {
		
			@Override
			public ValidationResult<«c.name»> validate(RosettaPath path, «c.name» o) {
				String error = 
					Lists.<ComparisonResult>newArrayList(
						«FOR attr : c.regularAndMaterialisedAttributes SEPARATOR ","»
						«IF attr.cardinalityIsListValue»
							«checkCardinalityList(attr)»
						«ELSE»
							«checkCardinalitySingle(attr)»
						«ENDIF»
						«ENDFOR»
					).stream().filter(res -> !res.get()).map(res -> res.getError()).collect(joining("; "));
				
				if (!isNullOrEmpty(error)) {
					return failure("«c.name»", ValidationType.MODEL_INSTANCE, o.getClass().getSimpleName(), path, "", error);
				}
				return success("«c.name»", ValidationType.MODEL_INSTANCE, o.getClass().getSimpleName(), path, "");
			}
			
			@Override
			public ValidationResult<«c.name»> validate(RosettaPath path, RosettaModelObjectBuilder b) {
				«c.name».«c.name»Builder o = («c.name».«c.name»Builder) b;
				String error = 
					Lists.<ComparisonResult>newArrayList(
						«FOR attr : c.regularAndMaterialisedAttributes SEPARATOR ","»
						«IF attr.cardinalityIsListValue»
							«checkCardinalityList(attr)»
						«ELSE»
							«checkCardinalitySingle(attr)»
						«ENDIF»
						«ENDFOR»
					).stream().filter(res -> !res.get()).map(res -> res.getError()).collect(joining("; "));
				
				if (!isNullOrEmpty(error)) {
					return failure("«c.name»", ValidationType.MODEL_INSTANCE, o.getClass().getSimpleName(), path, "", error);
				}
				return success("«c.name»", ValidationType.MODEL_INSTANCE, o.getClass().getSimpleName(), path, "");
			}
		
		}
	'''
	
	private def regularAndMaterialisedAttributes(extension RosettaClass it) {
		Iterables.concat(regularAttributes.expandedAttributesForList, materialiseAttributes)
	}
	
	private def checkCardinalitySingle(ExpandedAttribute attr)'''
		checkCardinality("«attr.name»", o.get«attr.name?.toFirstUpper»()!=null ? 1 : 0, «attr.inf», «attr.sup»)
	'''
	
	private def checkCardinalityList(ExpandedAttribute attr)'''
		checkCardinality("«attr.name»", o.get«attr.name?.toFirstUpper»()==null?0:o.get«attr.name?.toFirstUpper»().size(), «attr.inf», «attr.sup»)
	'''
	
	static def onlyExistsValidatorName(RosettaType c) {
		return c.name + 'OnlyExistsValidator'
	}
	
	private def onlyExistsValidator(RosettaJavaPackages packages, RosettaClass c) '''
		package «packages.model.existsValidation.name»;
		
		import «packages.defaultLibValidation.name».ExistenceChecker;
		import «packages.defaultLibValidation.name».ValidationResult;
		import «packages.defaultLibValidation.name».ValidationResult.ValidationType;
		import «packages.defaultLibValidation.name».ValidatorWithArg;
		import «packages.defaultLib.name».path.RosettaPath;
		import «packages.defaultLib.name».RosettaModelObjectBuilder;
		
		import «packages.model.name».«c.name»;
		
		import com.google.common.collect.ImmutableMap;
		
		import java.util.Map;
		import java.util.Set;
		import java.util.stream.Collectors;
		
		import static «packages.defaultLibValidation.name».ValidationResult.failure;
		import static «packages.defaultLibValidation.name».ValidationResult.success;
		
		public class «onlyExistsValidatorName(c)» implements ValidatorWithArg<«c.name», String> {
		
			@Override
			public ValidationResult<«c.name»> validate(RosettaPath path, «c.name» o, String field) {
				Map<String,Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
						«FOR attr : c.regularAttributes»
						.put("«attr.name»", ExistenceChecker.isSet(o.get«attr.name?.toFirstUpper»()))
						«ENDFOR»
						.build();
				// Exclude given method name
				Map<String, Boolean> otherFieldsExistenceMap = fieldExistenceMap.entrySet().stream()
						.filter(x -> !x.getKey().equals(field))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				// Find any other fields that are set
				Set<String> setFields = otherFieldsExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
						.keySet();
		
				if (!setFields.isEmpty()) {
					return failure("«c.name»", ValidationType.ONLY_EXISTS, o.getClass().getSimpleName(), path, "",
							String.format("[%s] is not the only field set. Other set fields: %s", field, setFields));
				}
				return success("«c.name»", ValidationType.ONLY_EXISTS, o.getClass().getSimpleName(), path, "");
			}
			
			@Override
			public ValidationResult<«c.name»> validate(RosettaPath path, RosettaModelObjectBuilder b, String field) {
				«c.name».«c.name»Builder o = («c.name».«c.name»Builder)b;
				Map<String,Boolean> fieldExistenceMap = ImmutableMap.<String, Boolean>builder()
						«FOR attr : c.regularAttributes»
						.put("«attr.name»", ExistenceChecker.isSet(o.get«attr.name?.toFirstUpper»()))
						«ENDFOR»
						.build();
				// Exclude given method name
				Map<String, Boolean> otherFieldsExistenceMap = fieldExistenceMap.entrySet().stream()
						.filter(x -> !x.getKey().equals(field))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				// Find any other fields that are set
				Set<String> setFields = otherFieldsExistenceMap.entrySet().stream()
						.filter(Map.Entry::getValue)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
						.keySet();
		
				if (!setFields.isEmpty()) {
					return failure("«c.name»", ValidationType.ONLY_EXISTS, o.getClass().getSimpleName(), path, "",
							String.format("[%s] is not the only field set. Other set fields: %s", field, setFields));
				}
				return success("«c.name»", ValidationType.ONLY_EXISTS, o.getClass().getSimpleName(), path, "");
			}
		}
	'''
}