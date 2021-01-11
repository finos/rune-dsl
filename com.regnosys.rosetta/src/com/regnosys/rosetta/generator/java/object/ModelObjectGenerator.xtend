
package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RQualifiedType
import com.regnosys.rosetta.utils.RosettaConfigExtension
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.annotations.RosettaClass
import com.rosetta.model.lib.annotations.RosettaQualified
import com.rosetta.model.lib.meta.RosettaMetaData
import java.util.List
import java.util.Objects
import java.util.Optional
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import com.google.common.collect.ImmutableList

class ModelObjectGenerator {
	@Inject extension RosettaExtensions
	@Inject extension ModelObjectBoilerPlate
	@Inject extension ModelObjectBuilderGenerator
	@Inject extension ImportManagerExtension
	@Inject extension RosettaConfigExtension

	def generate(JavaNames javaNames, IFileSystemAccess2 fsa, Data data, String version) {
		fsa.generateFile(javaNames.packages.model.directoryName + '/' + data.name + '.java',
			generateRosettaClass(javaNames, data, version))
	}

	private def generateRosettaClass(JavaNames javaNames, Data d, String version) {
		val classBody = tracImports(d.classBody(javaNames, version))
		'''
			package «javaNames.packages.model.name»;

			«FOR imp : classBody.imports»
				import «imp»;
			«ENDFOR»

			import com.rosetta.model.lib.RosettaModelObjectBuilder;
			import com.rosetta.model.lib.path.RosettaPath;
			import com.rosetta.model.lib.process.BuilderMerger;
			import com.rosetta.model.lib.process.Processor;
			import com.rosetta.model.lib.process.AttributeMeta;

			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»

			«classBody.toString»
		'''
	}

	def StringConcatenationClient classBody(Data d, JavaNames names, String version) '''
		«javadocWithVersion(d.definition, version)»
		@«RosettaClass»
		«IF d.hasQualifiedAttribute»
			@«RosettaQualified»(attribute="«d.qualifiedAttribute»",qualifiedClass=«names.toJavaType(d.getQualifiedClass).name».class)
		«ENDIF»
		
		public interface «d.name» extends «IF d.hasSuperType»«names.toJavaType(d.superType).name»«ELSE»«RosettaModelObject»«ENDIF»«implementsClause(d)» {
			«d.name» build();
			«d.builderName» toBuilder();
			
			«FOR attribute : d.expandedAttributes»
			«javadoc(attribute.definition)»
			«attribute.toJavaType(names)» get«attribute.name.toFirstUpper»();
			«ENDFOR»
			«val metaType = names.createJavaType(names.packages.model.meta, d.name+'Meta')»
			final static «metaType» metaData = new «metaType»();
			
			@Override
			default «RosettaMetaData»<? extends «d.name»> metaData() {
				return metaData;
			} 
					
			static «d.builderImplName» newBuilder() {
				return new «d.builderImplName»();
			}
			
			default Class<«d.name»> getType() {
				return «d.name».class;
			}
				
			interface «d.builderName» extends «d.name», «IF d.hasSuperType»«d.superType.builderName», «ENDIF»RosettaModelObjectBuilder {
«««				Get or create methods will create a builder instence of an object for you if it does not exist
				«FOR attribute : d.expandedAttributes»
					«IF attribute.isDataType || attribute.hasMetas»
						«IF attribute.cardinalityIsSingleValue»
							«attribute.toJavaType(names)» getOrCreate«attribute.name.toFirstUpper»();
						«ELSE»
							«attribute.toJavaType(names)» getOrCreate«attribute.name.toFirstUpper»(int _index);
						«ENDIF»
					«ENDIF»
				«ENDFOR»
				
				«FOR attribute : d.expandedAttributes»
					«IF attribute.cardinalityIsSingleValue»
						«d.builderName» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name»);
					«ELSE»
						«d.builderName» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name»);
						«d.builderName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name»);
						«d.builderName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name», int _idx);
						«d.builderName» add«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name»);
					«ENDIF»
				«ENDFOR»
			}
		
			class «d.implName» «IF d.hasSuperType»extends «d.superType.implName» «ENDIF»implements «d.name» {
				«d.rosettaClass(names)»
				
				«d.boilerPlate(names)»
			}
			
			«d.builderClass(names)»
		}
	'''

	def boolean globalKeyRecursive(Data class1) {
		return class1.globalKey || (class1.superType !== null && class1.superType.globalKeyRecursive)
	}

	def private hasQualifiedAttribute(Data c) {
		c.qualifiedAttribute !== null && c.qualifiedClass !== null
	}

	def private getQualifiedAttribute(Data c) {
		c.allSuperTypes.flatMap[expandedAttributes].findFirst[qualified]?.name
	}

	def private getQualifiedClass(Data c) {
		val allExpandedAttributes = c.allSuperTypes.flatMap[expandedAttributes].toList
		if(!allExpandedAttributes.stream.anyMatch[qualified])
			return null

		val qualifiedClassType = allExpandedAttributes.findFirst[qualified].type.name
		var qualifiedRootClassType = switch qualifiedClassType {
			case RQualifiedType.PRODUCT_TYPE.qualifiedType: c.findProductRootName
			case RQualifiedType.EVENT_TYPE.qualifiedType: c.findEventRootName
			default: throw new IllegalArgumentException("Unknown qualifiedType " + qualifiedClassType)
		}

		if(qualifiedRootClassType === null)
			throw new IllegalArgumentException("QualifiedType " + qualifiedClassType + " must have qualifiable root class")

		return qualifiedRootClassType
	}

	private def StringConcatenationClient rosettaClass(Data c, JavaNames names) {
		val expandedAttributes = c.expandedAttributes.filter[!it.overriding]
		'''
		«FOR attribute : expandedAttributes»
			private final «attribute.toJavaType(names)» «attribute.name»;
		«ENDFOR»
		

		protected «c.implName»(«c.builderName» builder) {
			«IF c.hasSuperType»
				super(builder);
			«ENDIF»
			«FOR attribute : expandedAttributes»
				this.«attribute.name» = «attribute.attributeFromBuilder»;
			«ENDFOR»
		}

		«FOR attribute : expandedAttributes»
			@Override
			public «attribute.toJavaType(names)» get«attribute.name.toFirstUpper»() {
				return «attribute.name»;
			}
			
		«ENDFOR»
		
		public «c.name» build() {
			return this;
		}

		public «c.builderName» toBuilder() {
			«c.builderName» builder = newBuilder();
			«IF (c.hasSuperType)»
				super.setBuilderFields(builder);
			«ENDIF»
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(«c.builderName» builder) {
			«FOR attribute :expandedAttributes»
				«Optional.importMethod("ofNullable")»(get«attribute.name.toFirstUpper»()).ifPresent(builder::set«attribute.name.toFirstUpper»);
			«ENDFOR»
		}
		'''
	}

	private def StringConcatenationClient toJavaType(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.isMultiple) '''«List»<«attribute.toJavaTypeSingle(names)»>'''
		else attribute.toJavaTypeSingle(names)
	}

	static def StringConcatenationClient toJavaTypeSingle(ExpandedAttribute attribute, JavaNames names) {
		if (!attribute.hasMetas)
			return '''«names.toJavaType(attribute.type)»'''
		val name = if (attribute.refIndex >= 0) {
				if (attribute.isDataType)
					'''ReferenceWithMeta«attribute.type.name.toFirstUpper»'''
				else
					'''BasicReferenceWithMeta«attribute.type.name.toFirstUpper»'''
			} else
				'''FieldWithMeta«attribute.type.name.toFirstUpper»'''
		return '''«names.toMetaType(attribute, name)»'''
	}

	private def StringConcatenationClient attributeFromBuilder(ExpandedAttribute attribute) {
		if(attribute.isDataType || attribute.hasMetas) {
			'''ofNullable(builder.get«attribute.name.toFirstUpper»()).map(«attribute.buildRosettaObject»).orElse(null)'''
		} else {
			if (attribute.cardinalityIsSingleValue)
				'''builder.get«attribute.name.toFirstUpper»()«IF attribute.needsBuilder».build()«ENDIF»'''
			else
				'''builder.get«attribute.name.toFirstUpper»().stream()«IF attribute.needsBuilder».map(b->b.build)«ENDIF».collect(«ImmutableList».toImmutableList())'''
		}
	}

	private def StringConcatenationClient buildRosettaObject(ExpandedAttribute attribute) {
		if(attribute.cardinalityIsListValue) {
			'''list -> list.stream().filter(«Objects»::nonNull).map(f->f.build()).filter(«Objects»::nonNull).collect(«Collectors».toList())'''
		} else {
			'''f->f.build()'''
		}
	}
	

}