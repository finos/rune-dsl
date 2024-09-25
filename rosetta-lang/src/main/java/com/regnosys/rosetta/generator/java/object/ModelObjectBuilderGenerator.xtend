package com.regnosys.rosetta.generator.java.object

import com.rosetta.model.lib.meta.Key
import com.rosetta.model.lib.process.BuilderMerger
import java.util.ArrayList
import java.util.function.Consumer
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.types.RDataType
import javax.inject.Inject
import com.rosetta.model.lib.annotations.RosettaAttribute
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.RosettaEcoreUtil

class ModelObjectBuilderGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension RosettaEcoreUtil
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil

	def StringConcatenationClient builderClass(RDataType t, JavaScope scope) {
		val javaType = t.toJavaType
		val superInterface = javaType.interfaces.head
		val builderScope = scope.classScope('''«javaType»BuilderImpl''')
		val attrs = t.javaAttributes
		val allAttrs = t.allJavaAttributes
		allAttrs.forEach[
			builderScope.createIdentifier(it, it.name.toFirstLower)
		]
		'''
		class «javaType»BuilderImpl«IF superInterface != ROSETTA_MODEL_OBJECT» extends «superInterface.toBuilderImplType» «ENDIF» implements «javaType.toBuilderType»«implementsClauseBuilder(t.EObject)» {
		
			«FOR attribute : attrs»
				protected «attribute.toBuilderType» «builderScope.getIdentifierOrThrow(attribute)»«IF attribute.isMulti» = new «ArrayList»<>()«ENDIF»;
			«ENDFOR»
		
			public «javaType»BuilderImpl() {
			}
		
			«attrs.builderGetters(builderScope)»
			«t.setters(builderScope)»
			
			@Override
			public «t.name» build() {
				return new «javaType.toImplType»(this);
			}
			
			@Override
			public «javaType.toBuilderType» toBuilder() {
				return this;
			}
		
			@SuppressWarnings("unchecked")
			@Override
			public «javaType.toBuilderType» prune() {
				«IF superInterface != ROSETTA_MODEL_OBJECT»super.prune();«ENDIF»
				«FOR attribute : attrs»
					«IF !attribute.isMulti && (attribute.RMetaAnnotatedType.RType instanceof RDataType || attribute.RMetaAnnotatedType.hasMeta)»
						if («builderScope.getIdentifierOrThrow(attribute)»!=null && !«builderScope.getIdentifierOrThrow(attribute)».prune().hasData()) «builderScope.getIdentifierOrThrow(attribute)» = null;
					«ELSEIF attribute.isMulti && attribute.RMetaAnnotatedType.RType instanceof RDataType || attribute.RMetaAnnotatedType.hasMeta»
						«builderScope.getIdentifierOrThrow(attribute)» = «builderScope.getIdentifierOrThrow(attribute)».stream().filter(b->b!=null).<«attribute.toBuilderTypeSingle»>map(b->b.prune()).filter(b->b.hasData()).collect(«Collectors».toList());
					«ENDIF»
				«ENDFOR»
				return this;
			}
			
			«attrs.hasData(superInterface != ROSETTA_MODEL_OBJECT)»
		
			«attrs.merge(t, superInterface != ROSETTA_MODEL_OBJECT)»
		
			«t.builderBoilerPlate(builderScope)»
		}
		'''
	}

	private def StringConcatenationClient merge(Iterable<RAttribute> attributes, RDataType type, boolean hasSuperType) {
		val builderName = type.toJavaType.toBuilderType
	'''
		@SuppressWarnings("unchecked")
		@Override
		public «builderName» merge(«RosettaModelObjectBuilder» other, «BuilderMerger» merger) {
			«IF hasSuperType»
				super.merge(other, merger);
				
			«ENDIF»
			«builderName» o = («builderName») other;
			
			«FOR a : attributes.filter[RMetaAnnotatedType.RType instanceof RDataType || RMetaAnnotatedType.hasMeta]»
				«val attributeName = a.name.toFirstUpper»
				«IF a.isMulti»
					merger.mergeRosetta(get«attributeName»(), o.get«attributeName»(), this::getOrCreate«attributeName»);
				«ELSE»
					merger.mergeRosetta(get«attributeName»(), o.get«attributeName»(), this::set«attributeName»);
				«ENDIF»
			«ENDFOR»
			
			«FOR a : attributes.filter[!(RMetaAnnotatedType.RType instanceof RDataType) && !RMetaAnnotatedType.hasMeta]»
				«val attributeName = a.name.toFirstUpper»
				«IF a.isMulti»
					merger.mergeBasic(get«attributeName»(), o.get«attributeName»(), («Consumer»<«a.toItemJavaType»>) this::add«attributeName»);
				«ELSE»
					merger.mergeBasic(get«attributeName»(), o.get«attributeName»(), this::set«attributeName»);
				«ENDIF»
			«ENDFOR»
			return this;
		}
	'''
	}

	private def StringConcatenationClient builderGetters(Iterable<RAttribute> attributes, JavaScope scope) '''
		«FOR attribute : attributes»
			@Override
			@«RosettaAttribute»("«attribute.javaAnnotation»")
			public «attribute.toBuilderTypeExt» get«attribute.name.toFirstUpper»() {
				return «scope.getIdentifierOrThrow(attribute)»;
			}

			«IF attribute.RMetaAnnotatedType.RType instanceof RDataType || attribute.RMetaAnnotatedType.hasMeta»
				«IF !attribute.isMulti»
					@Override
					public «attribute.toBuilderTypeSingle» getOrCreate«attribute.name.toFirstUpper»() {
						«attribute.toBuilderTypeSingle» result;
						if («scope.getIdentifierOrThrow(attribute)»!=null) {
							result = «scope.getIdentifierOrThrow(attribute)»;
						}
						else {
							result = «scope.getIdentifierOrThrow(attribute)» = «attribute.toMetaItemJavaType».builder();
							«IF !attribute.RMetaAnnotatedType.metaAttributes.filter[m|m.name=="location"].isEmpty»
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
									«attribute.toBuilderTypeSingle» new«attribute.name.toFirstUpper» = «attribute.toMetaItemJavaType».builder();
									«IF !attribute.RMetaAnnotatedType.metaAttributes.filter[m|m.name=="location"].isEmpty»
										new«attribute.name.toFirstUpper».getOrCreateMeta().addKey(«Key».builder().setScope("DOCUMENT"));
									«ENDIF»
									return new«attribute.name.toFirstUpper»;
								});
					}
					
				«ENDIF»
			«ENDIF»
		«ENDFOR»
	'''
	
	
	private def StringConcatenationClient setters(RDataType t, JavaScope scope)
		'''
		«FOR attribute : t.allJavaAttributes»
			«doSetter(t, attribute, scope)»
		«ENDFOR»
	'''
	
	private def StringConcatenationClient doSetter(RDataType t, RAttribute attribute, JavaScope scope) {
		val thisName = t.toJavaType.toBuilderType
		'''
		«IF attribute.isMulti»
			@Override
			public «thisName» add«attribute.name.toFirstUpper»(«attribute.toMetaItemJavaType» «scope.getIdentifierOrThrow(attribute)») {
				if («scope.getIdentifierOrThrow(attribute)»!=null) this.«scope.getIdentifierOrThrow(attribute)».add(«attribute.toBuilder(scope)»);
				return this;
			}
			
			@Override
			public «thisName» add«attribute.name.toFirstUpper»(«attribute.toMetaItemJavaType» «scope.getIdentifierOrThrow(attribute)», int _idx) {
				getIndex(this.«scope.getIdentifierOrThrow(attribute)», _idx, () -> «attribute.toBuilder(scope)»);
				return this;
			}
			«IF attribute.RMetaAnnotatedType.hasMeta»
			
			@Override
			public «thisName» add«attribute.name.toFirstUpper»Value(«attribute.toItemJavaType» «scope.getIdentifierOrThrow(attribute)») {
				this.getOrCreate«attribute.name.toFirstUpper»(-1).setValue(«scope.getIdentifierOrThrow(attribute)»«IF attribute.RMetaAnnotatedType.RType instanceof RDataType».toBuilder()«ENDIF»);
				return this;
			}

			@Override
			public «thisName» add«attribute.name.toFirstUpper»Value(«attribute.toItemJavaType» «scope.getIdentifierOrThrow(attribute)», int _idx) {
				this.getOrCreate«attribute.name.toFirstUpper»(_idx).setValue(«scope.getIdentifierOrThrow(attribute)»«IF attribute.RMetaAnnotatedType.RType instanceof RDataType».toBuilder()«ENDIF»);
				return this;
			}
			«ENDIF»
			@Override 
			public «thisName» add«attribute.name.toFirstUpper»(«attribute.toMetaJavaType» «scope.getIdentifierOrThrow(attribute)»s) {
				if («scope.getIdentifierOrThrow(attribute)»s != null) {
					for («attribute.toMetaItemJavaType» toAdd : «scope.getIdentifierOrThrow(attribute)»s) {
						this.«scope.getIdentifierOrThrow(attribute)».add(toAdd«IF needsBuilder(attribute)».toBuilder()«ENDIF»);
					}
				}
				return this;
			}
			
			@Override 
			@«RosettaAttribute»("«attribute.javaAnnotation»")
			public «thisName» set«attribute.name.toFirstUpper»(«attribute.toMetaJavaType» «scope.getIdentifierOrThrow(attribute)»s) {
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
			«IF attribute.RMetaAnnotatedType.hasMeta»
				
				@Override
				public «thisName» add«attribute.name.toFirstUpper»Value(«attribute.toJavaType» «scope.getIdentifierOrThrow(attribute)»s) {
					if («scope.getIdentifierOrThrow(attribute)»s != null) {
						for («attribute.toItemJavaType» toAdd : «scope.getIdentifierOrThrow(attribute)»s) {
							this.add«attribute.name.toFirstUpper»Value(toAdd);
						}
					}
					return this;
				}
				
				@Override
				public «thisName» set«attribute.name.toFirstUpper»Value(«attribute.toJavaType» «scope.getIdentifierOrThrow(attribute)»s) {
					this.«scope.getIdentifierOrThrow(attribute)».clear();
					if («scope.getIdentifierOrThrow(attribute)»s!=null) {
						«scope.getIdentifierOrThrow(attribute)»s.forEach(this::add«attribute.name.toFirstUpper»Value);
					}
					return this;
				}
			«ENDIF»
			
		«ELSE»
			@Override
			@«RosettaAttribute»("«attribute.javaAnnotation»")
			public «thisName» set«attribute.name.toFirstUpper»(«attribute.toMetaJavaType» «scope.getIdentifierOrThrow(attribute)») {
				this.«scope.getIdentifierOrThrow(attribute)» = «scope.getIdentifierOrThrow(attribute)»==null?null:«attribute.toBuilder(scope)»;
				return this;
			}
			«IF attribute.RMetaAnnotatedType.hasMeta»
				@Override
				public «thisName» set«attribute.name.toFirstUpper»Value(«attribute.toJavaType» «scope.getIdentifierOrThrow(attribute)») {
					this.getOrCreate«attribute.name.toFirstUpper»().setValue(«scope.getIdentifierOrThrow(attribute)»);
					return this;
				}
			«ENDIF»
		«ENDIF»
		'''
	}
	
	
	private def hasData(Iterable<RAttribute> attributes, boolean hasSuperType) '''
		@Override
		public boolean hasData() {
			«IF hasSuperType»if (super.hasData()) return true;«ENDIF»
			«FOR attribute:attributes.filter[name!="meta"]»
				«IF attribute.isMulti»
					«IF attribute.RMetaAnnotatedType.RType instanceof RDataType»
						if (get«attribute.name.toFirstUpper»()!=null && get«attribute.name.toFirstUpper»().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
					«ELSE»
						if (get«attribute.name.toFirstUpper»()!=null && !get«attribute.name.toFirstUpper»().isEmpty()) return true;
					«ENDIF»
				«ELSEIF attribute.RMetaAnnotatedType.RType instanceof RDataType»
					if (get«attribute.name.toFirstUpper»()!=null && get«attribute.name.toFirstUpper»().hasData()) return true;
				«ELSE»
					if (get«attribute.name.toFirstUpper»()!=null) return true;
				«ENDIF»
			«ENDFOR»
			return false;
		}
	'''

	private def StringConcatenationClient toBuilderType(RAttribute attribute) {
		if (attribute.isMulti) '''List<«attribute.toBuilderTypeSingle»>'''
		else '''«attribute.toBuilderTypeSingle»'''
	}

	private def StringConcatenationClient toBuilderTypeExt(RAttribute attribute) {
		if (attribute.isMulti) '''List<«IF attribute.RMetaAnnotatedType.RType instanceof RDataType || attribute.RMetaAnnotatedType.hasMeta»? extends «ENDIF»«attribute.toBuilderTypeSingle»>'''
		else '''«attribute.toBuilderTypeSingle»'''
	}

	def StringConcatenationClient toBuilderTypeSingle(RAttribute attribute) {
		if (attribute.RMetaAnnotatedType.hasMeta) {
			'''«attribute.toMetaItemJavaType.toBuilderType»'''
		} else {
			'''«attribute.toBuilderTypeUnderlying»'''
		}
	}
	
	private def StringConcatenationClient toBuilderTypeUnderlying(RAttribute attribute) {
		if (attribute.RMetaAnnotatedType.RType instanceof RDataType) '''«attribute.RMetaAnnotatedType.RType.name».«attribute.RMetaAnnotatedType.RType.name»Builder'''
		else '''«attribute.toMetaItemJavaType»'''
	}
	
		
	private def StringConcatenationClient toBuilder(RAttribute attribute, JavaScope scope) {
		if(needsBuilder(attribute)) {
			'''«scope.getIdentifierOrThrow(attribute)».toBuilder()'''
		} else {
			'''«scope.getIdentifierOrThrow(attribute)»'''
		}
	}
}