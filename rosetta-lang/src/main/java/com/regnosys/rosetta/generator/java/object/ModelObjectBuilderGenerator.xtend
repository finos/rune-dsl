package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.meta.Key
import com.rosetta.model.lib.process.BuilderMerger
import java.util.ArrayList
import java.util.List
import java.util.function.Consumer
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.generator.java.types.JavaClass
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.generator.java.JavaScope

class ModelObjectBuilderGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension RosettaExtensions
	@Inject RosettaTypeProvider typeProvider

	def StringConcatenationClient builderClass(Data c, JavaScope scope, extension JavaNames names) {
		val javaType = names.toJavaType(typeProvider.getRType(c)) as JavaClass
		val builderScope = scope.classScope('''«javaType»BuilderImpl''')
		c.expandedAttributesPlus.forEach[
			builderScope.createIdentifier(it, it.name)
		]
		'''
		//«javaType.toBuilderImplType»
		class «javaType»BuilderImpl«IF c.hasSuperType» extends «(names.toJavaType(typeProvider.getRType(c.superType)) as JavaClass).toBuilderImplType» «ENDIF» implements «javaType.toBuilderType»«implementsClauseBuilder(c)» {
		
			«FOR attribute : c.expandedAttributes»
				protected «attribute.toBuilderType(names)» «builderScope.getIdentifierOrThrow(attribute)»«IF attribute.isMultiple» = new «ArrayList»<>()«ENDIF»;
			«ENDFOR»
		
			public «javaType»BuilderImpl() {
			}
		
			«c.expandedAttributes.builderGetters(builderScope, names)»
		
			«c.setters(builderScope, names)»
			
			@Override
			public «c.name» build() {
				return new «javaType.toImplType»(this);
			}
			
			@Override
			public «javaType.toBuilderType» toBuilder() {
				return this;
			}
		
			@SuppressWarnings("unchecked")
			@Override
			public «javaType.toBuilderType» prune() {
				«IF c.hasSuperType»super.prune();«ENDIF»
				«FOR attribute : c.expandedAttributes»
					«IF !attribute.isMultiple && (attribute.isDataType || attribute.hasMetas)»
						if («builderScope.getIdentifierOrThrow(attribute)»!=null && !«builderScope.getIdentifierOrThrow(attribute)».prune().hasData()) «builderScope.getIdentifierOrThrow(attribute)» = null;
					«ELSEIF attribute.isMultiple && attribute.isDataType || attribute.hasMetas»
						«builderScope.getIdentifierOrThrow(attribute)» = «builderScope.getIdentifierOrThrow(attribute)».stream().filter(b->b!=null).<«attribute.toBuilderTypeSingle(names)»>map(b->b.prune()).filter(b->b.hasData()).collect(«Collectors».toList());
					«ENDIF»
				«ENDFOR»
				return this;
			}
			
			«c.expandedAttributes.filter[!it.overriding].hasData(c.hasSuperType)»
		
			«c.expandedAttributes.filter[!it.overriding].merge(typeProvider.getRType(c), c.hasSuperType, names)»
		
			«c.builderBoilerPlate(builderScope, names)»
		}
		'''
	}

	private def StringConcatenationClient merge(Iterable<ExpandedAttribute> attributes, RType type, boolean hasSuperType, extension JavaNames names) {
		val builderName = (names.toJavaType(type) as JavaClass).toBuilderType
	'''
		@SuppressWarnings("unchecked")
		@Override
		public «builderName» merge(RosettaModelObjectBuilder other, «BuilderMerger» merger) {
			«IF hasSuperType»
				super.merge(other, merger);
				
			«ENDIF»
			«builderName» o = («builderName») other;
			
			«FOR a : attributes.filter[isDataType || hasMetas]»
				«val attributeName = a.name.toFirstUpper»
				«IF a.multiple»
					merger.mergeRosetta(get«attributeName»(), o.get«attributeName»(), this::getOrCreate«attributeName»);
				«ELSE»
					merger.mergeRosetta(get«attributeName»(), o.get«attributeName»(), this::set«attributeName»);
				«ENDIF»
			«ENDFOR»
			
			«FOR a : attributes.filter[!isDataType && !hasMetas]»
				«val attributeName = a.name.toFirstUpper»
				«IF a.multiple»
					merger.mergeBasic(get«attributeName»(), o.get«attributeName»(), («Consumer»<«names.toJavaType(a.type)»>) this::add«attributeName»);
				«ELSE»
					merger.mergeBasic(get«attributeName»(), o.get«attributeName»(), this::set«attributeName»);
				«ENDIF»
			«ENDFOR»
			return this;
		}
	'''
	}

	private def StringConcatenationClient builderGetters(Iterable<ExpandedAttribute> attributes, JavaScope scope, JavaNames names) '''
		«FOR attribute : attributes»
			@Override
			public «attribute.toBuilderTypeExt(names)» get«attribute.name.toFirstUpper»() {
				return «scope.getIdentifierOrThrow(attribute)»;
			}
			
			«IF attribute.isDataType || attribute.hasMetas»
				«IF !attribute.cardinalityIsListValue»
					@Override
					public «attribute.toBuilderTypeSingle(names)» getOrCreate«attribute.name.toFirstUpper»() {
						«attribute.toBuilderTypeSingle(names)» result;
						if («scope.getIdentifierOrThrow(attribute)»!=null) {
							result = «scope.getIdentifierOrThrow(attribute)»;
						}
						else {
							result = «scope.getIdentifierOrThrow(attribute)» = «attribute.toTypeSingle(names)».builder();
							«IF !attribute.metas.filter[m|m.name=="location"].isEmpty»
								result.getOrCreateMeta().toBuilder().addKey(«Key».builder().setScope("DOCUMENT"));
							«ENDIF»
						}
						
						return result;
					}
					
				«ELSE»
					public «attribute.toBuilderTypeSingle(names)» getOrCreate«attribute.name.toFirstUpper»(int _index) {

						if («scope.getIdentifierOrThrow(attribute)»==null) {
							this.«scope.getIdentifierOrThrow(attribute)» = new «ArrayList»<>();
						}
						«attribute.toBuilderTypeSingle(names)» result;
						return getIndex(«scope.getIdentifierOrThrow(attribute)», _index, () -> {
									«attribute.toBuilderTypeSingle(names)» new«attribute.name.toFirstUpper» = «attribute.toTypeSingle(names)».builder();
									«IF !attribute.metas.filter[m|m.name=="location"].isEmpty»
										new«attribute.name.toFirstUpper».getOrCreateMeta().addKey(«Key».builder().setScope("DOCUMENT"));
									«ENDIF»
									return new«attribute.name.toFirstUpper»;
								});
					}
					
				«ENDIF»
			«ENDIF»
		«ENDFOR»
	'''
	
	
	private def StringConcatenationClient setters(Data thisClass, JavaScope scope, JavaNames names)
		'''
		«FOR attribute : thisClass.expandedAttributesPlus»
			«doSetter(thisClass, attribute, scope, names)»
		«ENDFOR»
	'''
	
	private def StringConcatenationClient doSetter(RosettaType thisClass, ExpandedAttribute attribute, JavaScope scope, extension JavaNames names) {
		val thisName = (names.toJavaType(typeProvider.getRType(thisClass)) as JavaClass).toBuilderType
		'''
		«IF attribute.cardinalityIsListValue»
			@Override
			public «thisName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «scope.getIdentifierOrThrow(attribute)») {
				if («scope.getIdentifierOrThrow(attribute)»!=null) this.«scope.getIdentifierOrThrow(attribute)».add(«attribute.toBuilder(scope)»);
				return this;
			}
			
			@Override
			public «thisName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «scope.getIdentifierOrThrow(attribute)», int _idx) {
				getIndex(this.«scope.getIdentifierOrThrow(attribute)», _idx, () -> «attribute.toBuilder(scope)»);
				return this;
			}
			«IF attribute.hasMetas»
			
				@Override
				public «thisName» add«attribute.name.toFirstUpper»Value(«attribute.toTypeSingle(names, true)» «scope.getIdentifierOrThrow(attribute)») {
					this.getOrCreate«attribute.name.toFirstUpper»(-1).setValue(«scope.getIdentifierOrThrow(attribute)»«IF attribute.isDataType».toBuilder()«ENDIF»);
					return this;
				}
				
				@Override
				public «thisName» add«attribute.name.toFirstUpper»Value(«attribute.toTypeSingle(names, true)» «scope.getIdentifierOrThrow(attribute)», int _idx) {
					this.getOrCreate«attribute.name.toFirstUpper»(_idx).setValue(«scope.getIdentifierOrThrow(attribute)»«IF attribute.isDataType».toBuilder()«ENDIF»);
					return this;
				}
			«ENDIF»
			«IF !attribute.overriding»
				@Override 
				public «thisName» add«attribute.name.toFirstUpper»(«List»<? extends «attribute.toTypeSingle(names)»> «scope.getIdentifierOrThrow(attribute)»s) {
					if («scope.getIdentifierOrThrow(attribute)»s != null) {
						for («attribute.toTypeSingle(names)» toAdd : «scope.getIdentifierOrThrow(attribute)»s) {
							this.«scope.getIdentifierOrThrow(attribute)».add(toAdd«IF needsBuilder(attribute)».toBuilder()«ENDIF»);
						}
					}
					return this;
				}
				
				@Override 
				public «thisName» set«attribute.name.toFirstUpper»(«List»<? extends «attribute.toTypeSingle(names)»> «scope.getIdentifierOrThrow(attribute)»s) {
					if («scope.getIdentifierOrThrow(attribute)»s == null)  {
						this.«scope.getIdentifierOrThrow(attribute)» = new «ArrayList»<>();
					}
					else {
						this.«scope.getIdentifierOrThrow(attribute)» = «scope.getIdentifierOrThrow(attribute)»s.stream()
							«IF needsBuilder(attribute)».map(_a->_a.toBuilder())«ENDIF»
							.collect(«Collectors».toCollection(()->new ArrayList<>()));
					}
					return this;
				}
				«IF attribute.hasMetas»
					
					@Override
					public «thisName» add«attribute.name.toFirstUpper»Value(«List»<? extends «attribute.toTypeSingle(names, true)»> «scope.getIdentifierOrThrow(attribute)»s) {
						if («scope.getIdentifierOrThrow(attribute)»s != null) {
							for («attribute.toTypeSingle(names, true)» toAdd : «scope.getIdentifierOrThrow(attribute)»s) {
								this.add«attribute.name.toFirstUpper»Value(toAdd);
							}
						}
						return this;
					}
					
					@Override
					public «thisName» set«attribute.name.toFirstUpper»Value(«List»<? extends «attribute.toTypeSingle(names, true)»> «scope.getIdentifierOrThrow(attribute)»s) {
						this.«scope.getIdentifierOrThrow(attribute)».clear();
						if («scope.getIdentifierOrThrow(attribute)»s!=null) {
							«scope.getIdentifierOrThrow(attribute)»s.forEach(this::add«attribute.name.toFirstUpper»Value);
						}
						return this;
					}
				«ENDIF»
			«ENDIF»
			
		«ELSE»
			@Override
			public «thisName» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «scope.getIdentifierOrThrow(attribute)») {
				this.«scope.getIdentifierOrThrow(attribute)» = «scope.getIdentifierOrThrow(attribute)»==null?null:«attribute.toBuilder(scope)»;
				return this;
			}
			«IF attribute.hasMetas»
				
				@Override
				public «thisName» set«attribute.name.toFirstUpper»Value(«attribute.toType(names, true)» «scope.getIdentifierOrThrow(attribute)») {
					this.getOrCreate«attribute.name.toFirstUpper»().setValue(«scope.getIdentifierOrThrow(attribute)»«IF attribute.isDataType»«ENDIF»);
					return this;
				}
			«ENDIF»
		«ENDIF»
		'''
	}
	
	def boolean globalKey(RosettaType type) {
		switch (type) {
			Data: type.hasKeyedAnnotation
			default: false
		}
	}
	
	
	private def hasData(Iterable<ExpandedAttribute> attributes, boolean hasSuperType) '''
		@Override
		public boolean hasData() {
			«IF hasSuperType»if (super.hasData()) return true;«ENDIF»
			«FOR attribute:attributes.filter[name!="meta"]»
				«IF attribute.cardinalityIsListValue»
					«IF attribute.isDataType»
						if (get«attribute.name.toFirstUpper»()!=null && get«attribute.name.toFirstUpper»().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
					«ELSE»
						if (get«attribute.name.toFirstUpper»()!=null && !get«attribute.name.toFirstUpper»().isEmpty()) return true;
					«ENDIF»
				«ELSEIF attribute.isDataType»
					if (get«attribute.name.toFirstUpper»()!=null && get«attribute.name.toFirstUpper»().hasData()) return true;
				«ELSE»
					if (get«attribute.name.toFirstUpper»()!=null) return true;
				«ENDIF»
			«ENDFOR»
			return false;
		}
	'''

	private def StringConcatenationClient toBuilderType(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.isMultiple) '''List<«attribute.toBuilderTypeSingle(names)»>'''
		else '''«attribute.toBuilderTypeSingle(names)»'''
	}

	private def StringConcatenationClient toBuilderTypeExt(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.isMultiple) '''List<«IF attribute.dataType || attribute.hasMetas»? extends «ENDIF»«attribute.toBuilderTypeSingle(names)»>'''
		else '''«attribute.toBuilderTypeSingle(names)»'''
	}

	def StringConcatenationClient toBuilderTypeSingle(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.hasMetas) {
			val buildername = if (attribute.refIndex >= 0) {
					if (attribute.isDataType)
						'''ReferenceWithMeta«attribute.type.name.toFirstUpper».ReferenceWithMeta«attribute.type.name.toFirstUpper»Builder'''
					else
						'''BasicReferenceWithMeta«attribute.type.name.toFirstUpper».BasicReferenceWithMeta«attribute.type.name.toFirstUpper»Builder'''
				} else {
					'''FieldWithMeta«attribute.type.name.toFirstUpper».FieldWithMeta«attribute.type.name.toFirstUpper»Builder'''
				}
			'''«names.toMetaType(attribute, buildername)»'''
		} else {
			'''«attribute.toBuilderTypeUnderlying(names)»'''
		}
	}
	
	private def StringConcatenationClient toBuilderTypeUnderlying(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.isDataType) '''«attribute.type.name».«attribute.type.name»Builder'''
		else '''«names.toJavaType(attribute.type)»'''
	}
	
		
	private def StringConcatenationClient toBuilder(ExpandedAttribute attribute, JavaScope scope) {
		if(needsBuilder(attribute)) {
			'''«scope.getIdentifierOrThrow(attribute)».toBuilder()'''
		} else {
			'''«scope.getIdentifierOrThrow(attribute)»'''
		}
	}
}