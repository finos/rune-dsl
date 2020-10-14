package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.model.lib.meta.RosettaMetaData
import com.rosetta.util.BreadthFirstSearch
import java.util.ArrayList
import java.util.List
import java.util.Optional
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import static extension com.regnosys.rosetta.generator.util.Util.*

class ModelObjectBuilderGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension RosettaExtensions
	
	def builderName(RosettaType c) {
		return c.name + 'Builder';
	}
	
	def builderNameFull(RosettaType c) {
		return c.fullname + '.' + c.name + 'Builder';
	}
	
	def builderName(String typeName) {
		return typeName + 'Builder';
	}

	def StringConcatenationClient builderClass(Data c, JavaNames names) '''
		public static class «builderName(c)» extends «IF c.hasSuperType»«c.superType.builderNameFull»«ELSE»«RosettaModelObjectBuilder»«ENDIF»«implementsClauseBuilder(c)» {
		
			«FOR attribute : c.expandedAttributes.filter[!it.overriding]»
				protected «attribute.toBuilderType(names)» «attribute.name»;
			«ENDFOR»
		
			public «builderName(c)»() {
			}
					
			@Override
			public «RosettaMetaData»<? extends «c.name»> metaData() {
				return metaData;
			} 
		
			«c.expandedAttributes.filter[!it.overriding].builderGetters(names)»
		
			«c.setters(names)»
			«IF c.name=="ContractualProduct" || c.name=="BusinessEvent"»
				«qualificationSetter(c)»
			«ENDIF»
		
			public «c.name» build() {
				return new «c.name»(this);
			}
		
			@Override
			public «builderName(c)» prune() {
				«IF c.hasSuperType»super.prune();«ENDIF»
				«FOR attribute : c.expandedAttributes»
					«IF !attribute.isMultiple && (attribute.isRosettaType || attribute.hasMetas)»
						if («attribute.name»!=null && !«attribute.name».prune().hasData()) «attribute.name» = null;
					«ELSEIF attribute.isMultiple && attribute.isRosettaType || attribute.hasMetas»
						«attribute.name» = «Optional».ofNullable(«attribute.name»).map(l->l.stream().filter(b->b!=null).map(b->b.prune()).filter(b->b.hasData()).collect(«Collectors».toList())).filter(b->!b.isEmpty()).orElse(null);
					«ENDIF»
				«ENDFOR»
				return this;
			}
			
			«c.expandedAttributes.filter[!it.overriding].hasData(c.hasSuperType)»

			«c.expandedAttributes.filter[!it.overriding].merge(c, c.hasSuperType, names)»

			«c.builderBoilerPlate(names)»
		}
	'''

	private def qualificationSetter(Data clazz) {
		val attr =  BreadthFirstSearch.search(null as Attribute, [ att |
			if (att === null)
				clazz.attributes
			else
				att.type.eContents.filter(Attribute).toList
		], [att | att?.type instanceof RosettaQualifiedType])
		
		if (attr !== null) {
			'''
			public void setQualification(String qualification) {
				this«attr.pathToSetter»
			}
			'''
		}
	}
	
	private def String pathToSetter(List<Attribute> path) {
		val result = new StringBuilder
		for (var i=1;i<path.size-1;i++) {
			val att = path.get(i);
			result.append('''.getOrCreate«att.name.toFirstUpper»(«IF att.card.isIsMany»0«ENDIF»)''')
		}
		val last = path.last
		if (last.card.isIsMany) {
			result.append(".add"+last.name.toFirstUpper+"(qualification);")
		}
		else {
			result.append(".set"+last.name.toFirstUpper+"(qualification);")
		}
		result.toString()
	}

    private def StringConcatenationClient merge(Iterable<ExpandedAttribute> attributes, RosettaType type, boolean hasSuperType, JavaNames names) '''
		«val builderName = type.builderName»
		@Override
		public «builderName» merge(RosettaModelObjectBuilder b1, RosettaModelObjectBuilder b2, BuilderMerger merger) {
			«IF hasSuperType»
				super.merge(b1, b2, merger);
				
			«ENDIF»
			«builderName» m1 = («builderName») b1;
			«builderName» m2 = («builderName») b2;
			
			«FOR a : attributes.filter[isRosettaType || hasMetas]»
				«val attributeName = a.name.toFirstUpper»
				«IF a.multiple»
					merger.mergeRosetta(m1.get«attributeName»(), m2.get«attributeName»(), this::getOrCreate«attributeName», this::add«attributeName»Builder);
				«ELSE»
					merger.mergeRosetta(m1.get«attributeName»(), m2.get«attributeName»(), this::getOrCreate«attributeName», this::set«attributeName»Builder);
				«ENDIF»
			«ENDFOR»
			
			«FOR a : attributes.filter[!isRosettaType && !hasMetas]»
				«val attributeName = a.name.toFirstUpper»
				«IF a.multiple»
					merger.mergeBasic(m1.get«attributeName»(), m2.get«attributeName»(), (java.util.function.Consumer<«names.toJavaType(a.type)»>) this::add«attributeName»);
				«ELSE»
					merger.mergeBasic(m1.get«attributeName»(), m2.get«attributeName»(), this::set«attributeName»);
				«ENDIF»
			«ENDFOR»
			return this;
		}
	'''

	private def StringConcatenationClient builderGetters(Iterable<ExpandedAttribute> attributes, JavaNames names) '''
		«FOR attribute : attributes»
			public «attribute.toBuilderType(names)» get«attribute.name.toFirstUpper»() {
				return «attribute.name»;
			}
			
			«IF attribute.isRosettaType || attribute.hasMetas»
				«IF !attribute.cardinalityIsListValue»
					public «attribute.toBuilderTypeSingle(names)» getOrCreate«attribute.name.toFirstUpper»() {
						if («attribute.name»!=null) {
							return «attribute.name»;
						}
						else return «attribute.name» = new «attribute.toBuilderTypeSingle(names)»();
					}
					
				«ELSE»
					public «attribute.toBuilderTypeSingle(names)» getOrCreate«attribute.name.toFirstUpper»(int _index) {
						if («attribute.name»==null) {
							this.«attribute.name» = new «ArrayList»<>();
						}
						return getIndex(«attribute.name», _index, ()->new «attribute.toBuilderTypeSingle(names)»());
					}
					
				«ENDIF»
			«ENDIF»
		«ENDFOR»
	'''

	private def StringConcatenationClient setters(Data c, JavaNames names) {
		val attributesProcessed = newArrayList
		'''
		«FOR current : c.allSuperTypes»
		«c.setters(attributesProcessed, current, current != c, names)»
		«ENDFOR»
		'''
	}
	
	
	private def StringConcatenationClient setters(RosettaType thisClass, List<String> attributesProcessed, RosettaType clazz, boolean isSuper, JavaNames names)
		'''
		«FOR attribute : clazz.expandedAttributes.filter(a|!attributesProcessed.contains(a.name))»
			«doSetter(thisClass, isSuper, attribute, attributesProcessed, clazz, names)»
		«ENDFOR»
	'''
	
	private def StringConcatenationClient doSetter(RosettaType thisClass, boolean isSuper, ExpandedAttribute attribute, List<String> attributesProcessed, RosettaType clazz, JavaNames names) {
		attributesProcessed.add(attribute.name)
		'''
		«IF attribute.cardinalityIsListValue»
			«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name») {
				if(this.«attribute.name» == null){
					this.«attribute.name» = new «ArrayList.name»<>();
				}
				this.«attribute.name».add(«attribute.toBuilder»);
				return this;
			}
			
			«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name», int _idx) {
				if(this.«attribute.name» == null){
					this.«attribute.name» = new «ArrayList.name»<>();
				}
				getIndex(this.«attribute.name», _idx, () -> «attribute.toBuilder»);
				this.«attribute.name».set(_idx, «attribute.toBuilder»);
				return this;
			}
			
			«IF !attribute.overriding»«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»(«List.name»<«attribute.toTypeSingle(names)»> «attribute.name»s) {
				if(this.«attribute.name» == null){
					this.«attribute.name» = new «ArrayList»<>();
				}
				for («attribute.toTypeSingle(names)» toAdd : «attribute.name»s) {
					this.«attribute.name».add(toAdd«IF needsBuilder(attribute)».toBuilder()«ENDIF»);
				}
				return this;
			}
			
			«ENDIF»
			«IF attribute.isRosettaType || !attribute.metas.empty»
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Builder(«attribute.toBuilderTypeSingle(names)» «attribute.name») {
					if(this.«attribute.name» == null){
						this.«attribute.name» = new «ArrayList»<>();
					}
					this.«attribute.name».add(«attribute.name»);
					return this;
				}
				
			«ENDIF»
			«IF attribute.isRosettaType && !attribute.metas.empty»
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Ref(«attribute.toBuilderTypeUnderlying(names)» «attribute.name») {
					return add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)».builder().setValueBuilder(«attribute.name»).build());
				}
				
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Ref(«attribute.toBuilderTypeUnderlying(names)» «attribute.name», int _idx) {
					return add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)».builder().setValueBuilder(«attribute.name»).build(), _idx);
				}
				
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Ref(«List.name»<«attribute.type.name»> «attribute.name»s) {
					for («attribute.type.name» toAdd : «attribute.name»s) {
						add«attribute.name.toFirstUpper»Ref(toAdd);
					}
					return this;
				}
				
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Ref(«attribute.type.name» «attribute.name») {
					if («attribute.name» != null) {
						return add«attribute.name.toFirstUpper»Ref(«attribute.name».toBuilder());
					}
					return this;
				}
				
			«ENDIF»
			«IF isSuper»@Override «ENDIF»public «thisClass.builderName» clear«attribute.name.toFirstUpper»() {
				this.«attribute.name» = null;
				return this;
			}
			
		«ELSE»
			«IF isSuper || clazz.globalKey && attribute.name === 'globalKey'»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name») {
				if («attribute.name» != null) {
					this.«attribute.name» = «attribute.toBuilder»;
				}
				return this;
			}

			«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Builder(«attribute.toBuilderType(names)» «attribute.name») {
				this.«attribute.name» = «attribute.name»;
				return this;
			}

			«IF !attribute.metas.empty»
				«IF attribute.isRosettaType»
					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Ref(«attribute.toBuilderTypeUnderlying(names)» «attribute.name») {
						return set«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)».builder().setValueBuilder(«attribute.name»).build());
					}
					
					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Ref(«names.toJavaType(attribute.type)» «attribute.name») {
						if («attribute.name» != null) {
							return set«attribute.name.toFirstUpper»Ref(«attribute.name».toBuilder());
						}
						return this;
					}
					
				«ELSE»
					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Ref(«attribute.toBuilderTypeUnderlying(names)» «attribute.name») {
						return set«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)».builder().setValue(«attribute.name»).build());
					}
					
				«ENDIF»
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
			«FOR attribute:attributes»    
				«IF attribute.cardinalityIsListValue»
					«IF attribute.isRosettaType»
						if (get«attribute.name.toFirstUpper»()!=null && get«attribute.name.toFirstUpper»().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
					«ELSE»
						if (get«attribute.name.toFirstUpper»()!=null && !get«attribute.name.toFirstUpper»().isEmpty()) return true;
					«ENDIF»
				«ELSEIF attribute.isRosettaType»
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

	private def StringConcatenationClient toBuilderTypeSingle(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.hasMetas) {
			val buildername = if (attribute.refIndex >= 0) {
					if (attribute.isRosettaType)
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
		if (attribute.isRosettaType) '''«attribute.type.name».«attribute.type.name»Builder'''
		else '''«names.toJavaType(attribute.type)»'''
	}
	
		
	private def toBuilder(ExpandedAttribute attribute) {
		if(needsBuilder(attribute)) {
			'''«attribute.name».toBuilder()'''
		} else {
			attribute.name
		}
	}
	private def needsBuilder(ExpandedAttribute attribute){
		attribute.isRosettaType || attribute.hasMetas
	}
}