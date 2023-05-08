package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
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
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.TypeSystem

class ModelObjectBuilderGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension RosettaExtensions
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem

	def StringConcatenationClient builderClass(Data c, JavaScope scope) {
		val javaType = new RDataType(c).toJavaType
		val builderScope = scope.classScope('''«javaType»BuilderImpl''')
		c.expandedAttributesPlus.forEach[
			builderScope.createIdentifier(it, it.name)
		]
		'''
		//«javaType.toBuilderImplType»
		class «javaType»BuilderImpl«IF c.hasSuperType» extends «new RDataType(c.superType).toJavaType.toBuilderImplType» «ENDIF» implements «javaType.toBuilderType»«implementsClauseBuilder(c)» {
		
			«FOR attribute : c.expandedAttributes»
				protected «attribute.toBuilderType» «builderScope.getIdentifierOrThrow(attribute)»«IF attribute.isMultiple» = new «ArrayList»<>()«ENDIF»;
			«ENDFOR»
		
			public «javaType»BuilderImpl() {
			}
		
			«c.expandedAttributes.builderGetters(builderScope)»
		
			«c.setters(builderScope)»
			
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
						«builderScope.getIdentifierOrThrow(attribute)» = «builderScope.getIdentifierOrThrow(attribute)».stream().filter(b->b!=null).<«attribute.toBuilderTypeSingle»>map(b->b.prune()).filter(b->b.hasData()).collect(«Collectors».toList());
					«ENDIF»
				«ENDFOR»
				return this;
			}
			
			«c.expandedAttributes.filter[!it.overriding].hasData(c.hasSuperType)»
		
			«c.expandedAttributes.filter[!it.overriding].merge(new RDataType(c), c.hasSuperType)»
		
			«c.builderBoilerPlate(builderScope)»
		}
		'''
	}

	private def StringConcatenationClient merge(Iterable<ExpandedAttribute> attributes, RDataType type, boolean hasSuperType) {
		val builderName = type.toJavaType.toBuilderType
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
					merger.mergeBasic(get«attributeName»(), o.get«attributeName»(), («Consumer»<«a.rosettaType.typeCallToRType.toJavaReferenceType»>) this::add«attributeName»);
				«ELSE»
					merger.mergeBasic(get«attributeName»(), o.get«attributeName»(), this::set«attributeName»);
				«ENDIF»
			«ENDFOR»
			return this;
		}
	'''
	}

	private def StringConcatenationClient builderGetters(Iterable<ExpandedAttribute> attributes, JavaScope scope) '''
		«FOR attribute : attributes»
			@Override
			public «attribute.toBuilderTypeExt» get«attribute.name.toFirstUpper»() {
				return «scope.getIdentifierOrThrow(attribute)»;
			}
			
			«IF attribute.isDataType || attribute.hasMetas»
				«IF !attribute.cardinalityIsListValue»
					@Override
					public «attribute.toBuilderTypeSingle» getOrCreate«attribute.name.toFirstUpper»() {
						«attribute.toBuilderTypeSingle» result;
						if («scope.getIdentifierOrThrow(attribute)»!=null) {
							result = «scope.getIdentifierOrThrow(attribute)»;
						}
						else {
							result = «scope.getIdentifierOrThrow(attribute)» = «attribute.toMetaOrRegularJavaType».builder();
							«IF !attribute.metas.filter[m|m.name=="location"].isEmpty»
								result.getOrCreateMeta().toBuilder().addKey(«Key».builder().setScope("DOCUMENT"));
							«ENDIF»
						}
						
						return result;
					}
					
				«ELSE»
					public «attribute.toBuilderTypeSingle» getOrCreate«attribute.name.toFirstUpper»(int _index) {

						if («scope.getIdentifierOrThrow(attribute)»==null) {
							this.«scope.getIdentifierOrThrow(attribute)» = new «ArrayList»<>();
						}
						«attribute.toBuilderTypeSingle» result;
						return getIndex(«scope.getIdentifierOrThrow(attribute)», _index, () -> {
									«attribute.toBuilderTypeSingle» new«attribute.name.toFirstUpper» = «attribute.toMetaOrRegularJavaType».builder();
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
	
	
	private def StringConcatenationClient setters(Data thisClass, JavaScope scope)
		'''
		«FOR attribute : thisClass.expandedAttributesPlus»
			«doSetter(thisClass, attribute, scope)»
		«ENDFOR»
	'''
	
	private def StringConcatenationClient doSetter(Data thisClass, ExpandedAttribute attribute, JavaScope scope) {
		val thisName = new RDataType(thisClass).toJavaType.toBuilderType
		'''
		«IF attribute.cardinalityIsListValue»
			@Override
			public «thisName» add«attribute.name.toFirstUpper»(«attribute.toMetaOrRegularJavaType» «scope.getIdentifierOrThrow(attribute)») {
				if («scope.getIdentifierOrThrow(attribute)»!=null) this.«scope.getIdentifierOrThrow(attribute)».add(«attribute.toBuilder(scope)»);
				return this;
			}
			
			@Override
			public «thisName» add«attribute.name.toFirstUpper»(«attribute.toMetaOrRegularJavaType» «scope.getIdentifierOrThrow(attribute)», int _idx) {
				getIndex(this.«scope.getIdentifierOrThrow(attribute)», _idx, () -> «attribute.toBuilder(scope)»);
				return this;
			}
			«IF attribute.hasMetas»
			
				@Override
				public «thisName» add«attribute.name.toFirstUpper»Value(«attribute.rosettaType.typeCallToRType.toJavaType» «scope.getIdentifierOrThrow(attribute)») {
					this.getOrCreate«attribute.name.toFirstUpper»(-1).setValue(«scope.getIdentifierOrThrow(attribute)»«IF attribute.isDataType».toBuilder()«ENDIF»);
					return this;
				}
				
				@Override
				public «thisName» add«attribute.name.toFirstUpper»Value(«attribute.rosettaType.typeCallToRType.toJavaType» «scope.getIdentifierOrThrow(attribute)», int _idx) {
					this.getOrCreate«attribute.name.toFirstUpper»(_idx).setValue(«scope.getIdentifierOrThrow(attribute)»«IF attribute.isDataType».toBuilder()«ENDIF»);
					return this;
				}
			«ENDIF»
			«IF !attribute.overriding»
				@Override 
				public «thisName» add«attribute.name.toFirstUpper»(«List»<? extends «attribute.toMetaOrRegularJavaType»> «scope.getIdentifierOrThrow(attribute)»s) {
					if («scope.getIdentifierOrThrow(attribute)»s != null) {
						for («attribute.toMetaOrRegularJavaType» toAdd : «scope.getIdentifierOrThrow(attribute)»s) {
							this.«scope.getIdentifierOrThrow(attribute)».add(toAdd«IF needsBuilder(attribute)».toBuilder()«ENDIF»);
						}
					}
					return this;
				}
				
				@Override 
				public «thisName» set«attribute.name.toFirstUpper»(«List»<? extends «attribute.toMetaOrRegularJavaType»> «scope.getIdentifierOrThrow(attribute)»s) {
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
					public «thisName» add«attribute.name.toFirstUpper»Value(«List»<? extends «attribute.rosettaType.typeCallToRType.toJavaReferenceType»> «scope.getIdentifierOrThrow(attribute)»s) {
						if («scope.getIdentifierOrThrow(attribute)»s != null) {
							for («attribute.rosettaType.typeCallToRType.toJavaType» toAdd : «scope.getIdentifierOrThrow(attribute)»s) {
								this.add«attribute.name.toFirstUpper»Value(toAdd);
							}
						}
						return this;
					}
					
					@Override
					public «thisName» set«attribute.name.toFirstUpper»Value(«List»<? extends «attribute.rosettaType.typeCallToRType.toJavaReferenceType»> «scope.getIdentifierOrThrow(attribute)»s) {
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
			public «thisName» set«attribute.name.toFirstUpper»(«attribute.toListOrSingleMetaType» «scope.getIdentifierOrThrow(attribute)») {
				this.«scope.getIdentifierOrThrow(attribute)» = «scope.getIdentifierOrThrow(attribute)»==null?null:«attribute.toBuilder(scope)»;
				return this;
			}
			«IF attribute.hasMetas»
				
				@Override
				public «thisName» set«attribute.name.toFirstUpper»Value(«attribute.rosettaType.typeCallToRType.toPolymorphicListOrSingleJavaType(attribute.cardinalityIsListValue)» «scope.getIdentifierOrThrow(attribute)») {
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

	private def StringConcatenationClient toBuilderType(ExpandedAttribute attribute) {
		if (attribute.isMultiple) '''List<«attribute.toBuilderTypeSingle»>'''
		else '''«attribute.toBuilderTypeSingle»'''
	}

	private def StringConcatenationClient toBuilderTypeExt(ExpandedAttribute attribute) {
		if (attribute.isMultiple) '''List<«IF attribute.dataType || attribute.hasMetas»? extends «ENDIF»«attribute.toBuilderTypeSingle»>'''
		else '''«attribute.toBuilderTypeSingle»'''
	}

	def StringConcatenationClient toBuilderTypeSingle(ExpandedAttribute attribute) {
		if (attribute.hasMetas) {
			'''«attribute.toMetaJavaType.toBuilderType»'''
		} else {
			'''«attribute.toBuilderTypeUnderlying»'''
		}
	}
	
	private def StringConcatenationClient toBuilderTypeUnderlying(ExpandedAttribute attribute) {
		if (attribute.isDataType) '''«attribute.type.name».«attribute.type.name»Builder'''
		else '''«attribute.rosettaType.typeCallToRType.toJavaReferenceType»'''
	}
	
		
	private def StringConcatenationClient toBuilder(ExpandedAttribute attribute, JavaScope scope) {
		if(needsBuilder(attribute)) {
			'''«scope.getIdentifierOrThrow(attribute)».toBuilder()'''
		} else {
			'''«scope.getIdentifierOrThrow(attribute)»'''
		}
	}
}