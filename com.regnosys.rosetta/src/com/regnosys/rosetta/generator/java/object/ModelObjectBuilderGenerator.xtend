package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.util.BreadthFirstSearch
import java.util.ArrayList
import java.util.List
import java.util.Optional
import java.util.stream.Collectors
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import static extension com.regnosys.rosetta.generator.util.Util.*
import java.util.function.Consumer
import com.rosetta.model.lib.meta.Key

class ModelObjectBuilderGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension RosettaExtensions
	
	def implName(RosettaType c) {
		return c.name + "Impl";
	}
	def builderName(RosettaType c) {
		return c.name + 'Builder';
	}
	def builderImplName(RosettaType c) {
		return c.name + 'BuilderImpl';
	}
	
	def builderNameFull(RosettaType c) {
		return c.fullname + '.' + c.name + 'Builder';
	}
	
	def builderImplNameFull(RosettaType c) {
		return c.fullname + '.' + c.name + 'BuilderImpl';
	}
	
	def builderName(String typeName) {
		return typeName + 'Builder';
	}

	def StringConcatenationClient builderClass(Data c, JavaNames names) '''
		class «builderImplName(c)» «IF c.hasSuperType»extends «c.superType.builderImplNameFull» «ENDIF» implements «c.builderName»«implementsClauseBuilder(c)» {
		
			«FOR attribute : c.expandedAttributes.filter[!it.overriding]»
				protected «attribute.toBuilderType(names)» «attribute.name»;
			«ENDFOR»
		
			public «builderImplName(c)»() {
			}
		
			«c.expandedAttributes.filter[!it.overriding].builderGetters(names)»
		
			«c.setters(names)»
			«IF c.name=="ContractualProduct" || c.name=="BusinessEvent"»
				«qualificationSetter(c)»
			«ENDIF»
		
			public «c.name» build() {
				return new «c.implName»(this);
			}
			
			public «c.builderName» toBuilder() {
				return this;
			}
		
			@SuppressWarnings("unchecked")
			@Override
			public «builderName(c)» prune() {
				«IF c.hasSuperType»super.prune();«ENDIF»
				«FOR attribute : c.expandedAttributes»
					«IF !attribute.isMultiple && (attribute.isDataType || attribute.hasMetas)»
						if («attribute.name»!=null && !«attribute.name».prune().hasData()) «attribute.name» = null;
					«ELSEIF attribute.isMultiple && attribute.isDataType || attribute.hasMetas»
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
		@SuppressWarnings("unchecked")
		@Override
		public «builderName» merge(RosettaModelObjectBuilder other, BuilderMerger merger) {
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

	private def StringConcatenationClient builderGetters(Iterable<ExpandedAttribute> attributes, JavaNames names) '''
		«FOR attribute : attributes»
			public «attribute.toBuilderType(names)» get«attribute.name.toFirstUpper»() {
				return «attribute.name»;
			}
			
			«IF attribute.isDataType || attribute.hasMetas»
				«IF !attribute.cardinalityIsListValue»
					public «attribute.toBuilderTypeSingle(names)» getOrCreate«attribute.name.toFirstUpper»() {
						«attribute.toBuilderTypeSingle(names)» result;
						if («attribute.name»!=null) {
							result = «attribute.name»;
						}
						else {
							result = «attribute.name» = new «attribute.toBuilderTypeSingle(names)»();
							«IF !attribute.metas.filter[m|m.name=="location"].isEmpty»
								result.getOrCreateMeta().getOrCreateKeys().addKey(new «Key».KeyBuilder().setScope("DOCUMENT"));
							«ENDIF»
						}
						
						return result;
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
	
	
	private def StringConcatenationClient setters(RosettaType thisClass, JavaNames names)
		'''
		«FOR attribute : thisClass.expandedAttributes»
			«doSetter(thisClass, attribute, names)»
		«ENDFOR»
	'''
	
	private def StringConcatenationClient doSetter(RosettaType thisClass, ExpandedAttribute attribute, JavaNames names) {
		'''
		«IF attribute.cardinalityIsListValue»
			@Override
			public «thisClass.builderName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name») {
				if(this.«attribute.name» == null){
					this.«attribute.name» = new «ArrayList.name»<>();
				}
				this.«attribute.name».add(«attribute.toBuilder»);
				return this;
			}
			
			@Override
			public «thisClass.builderName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name», int _idx) {
				if(this.«attribute.name» == null){
					this.«attribute.name» = new «ArrayList.name»<>();
				}
				getIndex(this.«attribute.name», _idx, () -> «attribute.toBuilder»);
				this.«attribute.name».set(_idx, «attribute.toBuilder»);
				return this;
			}
			
			«IF !attribute.overriding»
				@Override 
				public «thisClass.builderName» add«attribute.name.toFirstUpper»(«List.name»<«attribute.toTypeSingle(names)»> «attribute.name»s) {
					if(this.«attribute.name» == null){
						this.«attribute.name» = new «ArrayList»<>();
					}
					for («attribute.toTypeSingle(names)» toAdd : «attribute.name»s) {
						this.«attribute.name».add(toAdd«IF needsBuilder(attribute)».toBuilder()«ENDIF»);
					}
					return this;
				}
				
				@Override 
				public «thisClass.builderName» set«attribute.name.toFirstUpper»(«List.name»<«attribute.toTypeSingle(names)»> «attribute.name»s) {
					if («attribute.name»s ==null) {
						this.«attribute.name» = new «ArrayList»<>();
					}
					else {
						this.«attribute.name» = «attribute.name»s.stream()
							«IF needsBuilder(attribute)».map(_a->_a.toBuilder())«ENDIF»
							.collect(«Collectors».toCollection(()->new ArrayList<>()));
					}
					return this;
				}
				
			«ENDIF»
«««			«IF attribute.isDataType || !attribute.metas.empty»
«««				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Builder(«attribute.toBuilderTypeSingle(names)» «attribute.name») {
«««					if(this.«attribute.name» == null){
«««						this.«attribute.name» = new «ArrayList»<>();
«««					}
«««					this.«attribute.name».add(«attribute.name»);
«««					return this;
«««				}
«««				
«««			«ENDIF»
«««			«IF attribute.isDataType && !attribute.metas.empty»
«««				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Ref(«attribute.toBuilderTypeUnderlying(names)» «attribute.name») {
«««					return add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)».builder().setValueBuilder(«attribute.name»).build());
«««				}
«««				
«««				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Ref(«attribute.toBuilderTypeUnderlying(names)» «attribute.name», int _idx) {
«««					return add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)».builder().setValueBuilder(«attribute.name»).build(), _idx);
«««				}
«««				
«««				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Ref(«List.name»<«attribute.type.name»> «attribute.name»s) {
«««					for («attribute.type.name» toAdd : «attribute.name»s) {
«««						add«attribute.name.toFirstUpper»Ref(toAdd);
«««					}
«««					return this;
«««				}
«««				
«««				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Ref(«attribute.type.name» «attribute.name») {
«««					if («attribute.name» != null) {
«««						return add«attribute.name.toFirstUpper»Ref(«attribute.name».toBuilder());
«««					}
«««					return this;
«««				}
«««				
«««			«ENDIF»
«««			«IF isSuper»@Override «ENDIF»public «thisClass.builderName» clear«attribute.name.toFirstUpper»() {
«««				this.«attribute.name» = null;
«««				return this;
«««			}
«««			
		«ELSE»
			@Override
			public «thisClass.builderName» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name») {
				if («attribute.name» != null) {
					this.«attribute.name» = «attribute.toBuilder»;
				}
				return this;
			}
«««
«««			«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Builder(«attribute.toBuilderType(names)» «attribute.name») {
«««				this.«attribute.name» = «attribute.name»;
«««				return this;
«««			}
«««
«««			«IF !attribute.metas.empty»
«««				«IF attribute.isDataType»
«««					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Ref(«attribute.toBuilderTypeUnderlying(names)» «attribute.name») {
«««						return set«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)».builder().setValueBuilder(«attribute.name»).build());
«««					}
«««					
«««					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Ref(«names.toJavaType(attribute.type)» «attribute.name») {
«««						if («attribute.name» != null) {
«««							return set«attribute.name.toFirstUpper»Ref(«attribute.name».toBuilder());
«««						}
«««						return this;
«««					}
«««					
«««				«ELSE»
«««					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Ref(«attribute.toBuilderTypeUnderlying(names)» «attribute.name») {
«««						return set«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)».builder().setValue(«attribute.name»).build());
«««					}
«««					
«««				«ENDIF»
«««			«ENDIF»
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

	private def StringConcatenationClient toBuilderTypeSingle(ExpandedAttribute attribute, JavaNames names) {
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
	
		
	private def toBuilder(ExpandedAttribute attribute) {
		if(needsBuilder(attribute)) {
			'''«attribute.name».toBuilder()'''
		} else {
			attribute.name
		}
	}
	private def needsBuilder(ExpandedAttribute attribute){
		attribute.isDataType || attribute.hasMetas
	}
}