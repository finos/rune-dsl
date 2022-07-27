
package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.java.util.ParameterizedType
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RQualifiedType
import com.regnosys.rosetta.utils.RosettaConfigExtension
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.model.lib.annotations.RosettaClass
import com.rosetta.model.lib.annotations.RosettaQualified
import com.rosetta.model.lib.meta.RosettaMetaData
import java.util.Collection
import java.util.Collections
import java.util.List
import java.util.Objects
import java.util.Optional
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2

import static com.regnosys.rosetta.generator.java.util.ModelGeneratorUtil.*

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

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

			«FOR imp : classBody.getImports(javaNames.packages.model.name, javaNames.toJavaType(d).name)»
				import «imp»;
			«ENDFOR»

			«FOR imp : classBody.staticImports»
				import static «imp»;
			«ENDFOR»

			«classBody.toString»
		'''
	}
	
	def StringConcatenationClient classBody(Data d, JavaNames names, String version) {
		classBody(d, names, version, Collections.emptyList)
	}

	def StringConcatenationClient classBody(Data d, JavaNames names, String version, Collection<Object> interfaces) '''
		«javadoc(d, version)»
		@«RosettaClass»
		«IF d.hasQualifiedAttribute»
			@«RosettaQualified»(attribute="«d.qualifiedAttribute»",qualifiedClass=«names.toJavaType(d.getQualifiedClass).name».class)
		«ENDIF»
		public interface «names.toJavaType(d)» extends «IF d.hasSuperType»«names.toJavaType(d.superType)»«ELSE»«RosettaModelObject»«ENDIF»«implementsClause(d, interfaces)» {
			«FOR attribute : d.expandedAttributes»
				
				«javadoc(attribute.definition, attribute.docReferences, null)»
				«attribute.toJavaType(names)» get«attribute.name.toFirstUpper»();
			«ENDFOR»
			
			«val metaType = names.createJavaType(names.packages.model.meta, d.name+'Meta')»
			«metaType» metaData = new «metaType»();
			
			@Override
			default «RosettaMetaData»<? extends «d.name»> metaData() {
				return metaData;
			} 
					
			static «names.toJavaType(d).toBuilderType» builder() {
				return new «names.toJavaType(d).toBuilderImplType»();
			}
			
			default Class<? extends «d.name»> getType() {
				return «d.name».class;
			}
			«FOR pt :interfaces.filter(ParameterizedType).filter[type.simpleName=="ReferenceWithMeta" || type.simpleName=="FieldWithMeta"]»
			
				default Class<«pt.typeArgs.get(0).type»> getValueType() {
					return «pt.typeArgs.get(0).type».class;
				}
			«ENDFOR»
			
			«d.processMethod(names)»
			
			interface «names.toJavaType(d)»Builder extends «d.name», «IF d.hasSuperType»«names.toJavaType(d.superType).toBuilderType», «ENDIF»«RosettaModelObjectBuilder»«FOR inter:interfaces BEFORE ', ' SEPARATOR ', '»«buildify(inter)»«ENDFOR» {
«««				Get or create methods will create a builder instance of an object for you if it does not exist
				«FOR attribute : d.expandedAttributes»
					«IF attribute.isDataType || attribute.hasMetas»
						«IF attribute.cardinalityIsSingleValue»
							«attribute.toBuilderTypeSingle(names)» getOrCreate«attribute.name.toFirstUpper»();
							«attribute.toBuilderTypeSingle(names)» get«attribute.name.toFirstUpper»();
						«ELSE»
							«attribute.toBuilderTypeSingle(names)» getOrCreate«attribute.name.toFirstUpper»(int _index);
							«List»<? extends «attribute.toBuilderTypeSingle(names)»> get«attribute.name.toFirstUpper»();
						«ENDIF»
					«ENDIF»
				«ENDFOR»
				«FOR attribute : d.expandedAttributesPlus»
					«IF attribute.cardinalityIsSingleValue»
						«names.toJavaType(d).toBuilderType» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name»);
						«IF attribute.hasMetas»«names.toJavaType(d).toBuilderType» set«attribute.name.toFirstUpper»Value(«attribute.toType(names, true)» «attribute.name»);«ENDIF»
					«ELSE»
						«names.toJavaType(d).toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name»);
						«names.toJavaType(d).toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name», int _idx);
						«IF attribute.hasMetas»«names.toJavaType(d).toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.toTypeSingle(names, true)» «attribute.name»);
						«names.toJavaType(d).toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.toTypeSingle(names, true)» «attribute.name», int _idx);«ENDIF»
						«IF !attribute.isOverriding»
						«names.toJavaType(d).toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name»);
						«names.toJavaType(d).toBuilderType» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name»);
						«IF attribute.hasMetas»«names.toJavaType(d).toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.toType(names, true)» «attribute.name»);
						«names.toJavaType(d).toBuilderType» set«attribute.name.toFirstUpper»Value(«attribute.toType(names, true)» «attribute.name»);«ENDIF»
						«ENDIF»
					«ENDIF»
				«ENDFOR»
				
				«d.builderProcessMethod(names)»
				
				«names.toJavaType(d).toBuilderType» prune();
			}
			
«««			This line reserves this name as a name SO any class imported with the smae name will automatically be fully qualified
			//«names.toJavaType(d).toImplType»
			class «names.toJavaType(d)»Impl «IF d.hasSuperType»extends «names.toJavaType(d.superType).toImplType» «ENDIF»implements «d.name» {
				«d.rosettaClass(names)»
				
				«d.boilerPlate(names)»
			}
			
			«d.builderClass(names)»
		}
	'''
	
	
	def dispatch buildify(Object object) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	def dispatch buildify(Class<?> clazz) {
		new JavaType(clazz.name+"."+clazz.simpleName+"Builder")
	}
	def dispatch buildify(ParameterizedType clazz) {
		val builderType = new JavaType(clazz.type.name+"."+clazz.type.simpleName+"Builder")
		new ParameterizedType(builderType, clazz.typeArgs)
	}

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
		val expandedAttributes = c.expandedAttributes
		'''
		«FOR attribute : expandedAttributes»
			private final «attribute.toJavaType(names)» «attribute.name»;
		«ENDFOR»

		protected «names.toJavaType(c)»Impl(«names.toJavaType(c).toBuilderType» builder) {
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

		public «names.toJavaType(c).toBuilderType» toBuilder() {
			«names.toJavaType(c).toBuilderType» builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(«names.toJavaType(c).toBuilderType» builder) {
			«IF (c.hasSuperType)»
				super.setBuilderFields(builder);
			«ENDIF»
			«FOR attribute :expandedAttributes»
				«Optional.importMethod("ofNullable")»(get«attribute.name.toFirstUpper»()).ifPresent(builder::set«attribute.name.toFirstUpper»);
			«ENDFOR»
		}
		'''
	}

	private def StringConcatenationClient toJavaType(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.isMultiple) '''«List»<«IF attribute.dataType || attribute.hasMetas»? extends «ENDIF»«attribute.toJavaTypeSingle(names)»>'''
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
			if (attribute.cardinalityIsListValue)
				'''ofNullable(builder.get«attribute.name.toFirstUpper»()).filter(_l->!_l.isEmpty()).map(«attribute.buildRosettaObject»).orElse(null)'''
			else
				'''ofNullable(builder.get«attribute.name.toFirstUpper»()).map(«attribute.buildRosettaObject»).orElse(null)'''
		} else {
			if (attribute.cardinalityIsSingleValue)
				'''builder.get«attribute.name.toFirstUpper»()«IF attribute.needsBuilder».build()«ENDIF»'''
			else
				'''ofNullable(builder.get«attribute.name.toFirstUpper»()).filter(_l->!_l.isEmpty()).map(«ImmutableList»::copyOf).orElse(null)'''
		}
	}

	private def StringConcatenationClient buildRosettaObject(ExpandedAttribute attribute) {
		if(attribute.cardinalityIsListValue) {
			'''list -> list.stream().filter(«Objects»::nonNull).map(f->f.build()).filter(«Objects»::nonNull).collect(«ImmutableList».toImmutableList())'''
		} else {
			'''f->f.build()'''
		}
	}
	

}