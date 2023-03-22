
package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.model.lib.annotations.RosettaClass
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
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaClass
import com.regnosys.rosetta.generator.java.types.JavaParameterizedType
import com.regnosys.rosetta.utils.DottedPath
import com.regnosys.rosetta.types.RosettaTypeProvider

class ModelObjectGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension ModelObjectBuilderGenerator
	@Inject extension ImportManagerExtension
	@Inject RosettaTypeProvider typeProvider

	def generate(JavaNames javaNames, IFileSystemAccess2 fsa, Data data, String version) {
		fsa.generateFile(javaNames.packages.model.withForwardSlashes + '/' + data.name + '.java',
			generateRosettaClass(javaNames, data, version))
	}

	private def generateRosettaClass(JavaNames names, Data d, String version) {
		val scope = new JavaScope(names.packages.model)
		buildClass(names.packages.model, d.classBody(new JavaClass(names.packages.model.meta, d.name+'Meta'), names, version), scope)
	}
	
	def StringConcatenationClient classBody(Data d, JavaClass metaType, JavaNames names, String version) {
		classBody(d, metaType, names, version, Collections.emptyList)
	}

	def StringConcatenationClient classBody(Data d, JavaClass metaType, extension JavaNames names, String version, Collection<Object> interfaces) {
		val javaType = names.toJavaType(typeProvider.getRType(d)) as JavaClass
		'''
			«javadoc(d, version)»
			@«RosettaClass»
			public interface «javaType» extends «IF d.hasSuperType»«names.toJavaType(typeProvider.getRType(d.superType))»«ELSE»«RosettaModelObject»«ENDIF»«implementsClause(d, interfaces)» {
				«d.name» build();
				«javaType.toBuilderType» toBuilder();
				
				«FOR attribute : d.expandedAttributes»
					«javadoc(attribute.definition, attribute.docReferences, null)»
					«attribute.toJavaType(names)» get«attribute.name.toFirstUpper»();
				«ENDFOR»
				
				final static «metaType» metaData = new «metaType»();
				
				@Override
				default «RosettaMetaData»<? extends «d.name»> metaData() {
					return metaData;
				} 
						
				static «javaType.toBuilderType» builder() {
					return new «javaType.toBuilderImplType»();
				}
				
				default Class<? extends «d.name»> getType() {
					return «d.name».class;
				}
				«FOR pt :interfaces.filter(JavaParameterizedType).filter[baseType.simpleName=="ReferenceWithMeta" || baseType.simpleName=="FieldWithMeta"]»
				
					default Class<«pt.arguments.head»> getValueType() {
						return «pt.arguments.head».class;
					}
				«ENDFOR»
				
				«d.processMethod(names)»
				
				interface «javaType»Builder extends «d.name», «IF d.hasSuperType»«(names.toJavaType(typeProvider.getRType(d.superType)) as JavaClass).toBuilderType», «ENDIF»«RosettaModelObjectBuilder»«FOR inter:interfaces BEFORE ', ' SEPARATOR ', '»«buildify(inter)»«ENDFOR» {
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
							«javaType.toBuilderType» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name»);
							«IF attribute.hasMetas»«javaType.toBuilderType» set«attribute.name.toFirstUpper»Value(«attribute.toType(names, true)» «attribute.name»);«ENDIF»
						«ELSE»
							«javaType.toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name»);
							«javaType.toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name», int _idx);
							«IF attribute.hasMetas»«javaType.toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.toTypeSingle(names, true)» «attribute.name»);
							«javaType.toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.toTypeSingle(names, true)» «attribute.name», int _idx);«ENDIF»
							«IF !attribute.isOverriding»
							«javaType.toBuilderType» add«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name»);
							«javaType.toBuilderType» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name»);
							«IF attribute.hasMetas»«javaType.toBuilderType» add«attribute.name.toFirstUpper»Value(«attribute.toType(names, true)» «attribute.name»);
							«javaType.toBuilderType» set«attribute.name.toFirstUpper»Value(«attribute.toType(names, true)» «attribute.name»);«ENDIF»
							«ENDIF»
						«ENDIF»
					«ENDFOR»
					
					«d.builderProcessMethod(names)»
					
					«javaType.toBuilderType» prune();
				}
				
«««			This line reserves this name as a name SO any class imported with the smae name will automatically be fully qualified
				//«javaType.toImplType»
				class «javaType»Impl «IF d.hasSuperType»extends «(names.toJavaType(typeProvider.getRType(d.superType)) as JavaClass).toImplType» «ENDIF»implements «d.name» {
					«d.rosettaClass(names)»
					
					«d.boilerPlate(names)»
				}
				
				«d.builderClass(names)»
			}
		'''
	}
	
	
	def dispatch buildify(Object object) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
	def dispatch buildify(Class<?> clazz) {
		new JavaClass(DottedPath.splitOnDots(clazz.packageName), clazz.simpleName+"."+clazz.simpleName+"Builder")
	}
	def dispatch buildify(JavaParameterizedType clazz) {
		val builderType = new JavaClass(clazz.baseType.packageName, clazz.baseType.simpleName+"."+clazz.baseType.simpleName+"Builder")
		new JavaParameterizedType(builderType, clazz.arguments)
	}

	def boolean globalKeyRecursive(Data class1) {
		return class1.globalKey || (class1.superType !== null && class1.superType.globalKeyRecursive)
	}

	private def StringConcatenationClient rosettaClass(Data c, extension JavaNames names) {
		val expandedAttributes = c.expandedAttributes
		val javaType = names.toJavaType(typeProvider.getRType(c)) as JavaClass
		'''
		«FOR attribute : expandedAttributes»
			private final «attribute.toJavaType(names)» «attribute.name»;
		«ENDFOR»

		protected «javaType»Impl(«javaType.toBuilderType» builder) {
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
		@Override
		public «c.name» build() {
			return this;
		}

		@Override
		public «javaType.toBuilderType» toBuilder() {
			«javaType.toBuilderType» builder = builder();
			setBuilderFields(builder);
			return builder;
		}
		
		protected void setBuilderFields(«javaType.toBuilderType» builder) {
			«IF (c.hasSuperType)»
				super.setBuilderFields(builder);
			«ENDIF»
			«FOR attribute :expandedAttributes»
				«method(Optional, "ofNullable")»(get«attribute.name.toFirstUpper»()).ifPresent(builder::set«attribute.name.toFirstUpper»);
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