package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.impl.RosettaFactoryImpl
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.RosettaModelObjectBuilder
import com.rosetta.util.BreadthFirstSearch
import java.util.Collection
import java.util.List
import java.util.Optional

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.rosetta.model.lib.meta.RosettaMetaData
import java.util.ArrayList
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.rosetta.model.lib.functions.MapperBuilder

class ModelObjectBuilderGenerator {
	
	@Inject extension ModelObjectBoilerPlate
	@Inject extension RosettaExtensions
	
	def builderName(RosettaType c) {
		return c.name + 'Builder';
	}
	
	def builderName(String typeName) {
		return typeName + 'Builder';
	}
	
	def builderSuperClass(RosettaClass clazz) {
		Optional.ofNullable(clazz.superType).map[builderName].orElse('RosettaModelObjectBuilder')
	}
	
	def StringConcatenationClient builderClass(Data c, JavaNames names) '''
		public static class «builderName(c)» extends «IF c.hasSuperType»«c.superType.builderName»«ELSE»«RosettaModelObjectBuilder»«ENDIF»{
		
			«FOR attribute : c.expandedAttributes»
				protected «attribute.toBuilderType» «attribute.name»;
			«ENDFOR»
		
			public «builderName(c)»() {
			}
					
			@Override
			public «RosettaMetaData»<? extends «c.name»> metaData() {
				return metaData;
			} 
		
			«c.expandedAttributes.builderGetters»
		
			«c.setters(names)»
		
			public «c.name» build() {
				return new «c.name»(this);
			}
		
			@Override
			public «builderName(c)» prune() {
				«IF c.hasSuperType»super.prune();«ENDIF»
				«FOR attribute : c.expandedAttributes»
					«IF !attribute.isMultiple && (attribute.isRosettaClassOrData || attribute.hasMetas)»
						if («attribute.name»!=null && !«attribute.name».prune().hasData()) «attribute.name» = null;
					«ELSEIF attribute.isMultiple && attribute.isRosettaClassOrData || attribute.hasMetas»
						if («attribute.name»!=null) «attribute.name» = «attribute.name».stream().filter(b->b!=null).map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
					«ENDIF»
				«ENDFOR»
				return this;
			}
			
			«c.expandedAttributes.hasData(c.hasSuperType)»
			
			«c.expandedAttributes.process(c.hasSuperType)»
		
			«c.builderBoilerPlate»
		}
	'''
	
	def StringConcatenationClient builderClass(RosettaClass c) '''
		public static «c.abstractModifier» class «builderName(c)» extends «c.builderSuperClass» «builderImplements(c)»{
		
			«FOR attribute : c.expandedAttributes»
				protected «attribute.toBuilderType» «attribute.name»;
			«ENDFOR»
		
			public «builderName(c)»() {
			}
					
			@Override
			public RosettaMetaData<? extends «c.name»> metaData() {
				return metaData;
			} 
		
			«c.expandedAttributes.builderGetters»
		
			«c.setters»
			««««ContractualProduct and event are the only objects that get qualified
			««««This could if necessary be replaced with code that finds all the quualifiaction rules
			««««and the qualification result fields and finds there common roots (current CP and EV)	
			«IF c.name=="ContractualProduct" || c.name=="Event"»
				«qualificationSetter(c)»
			«ENDIF»
			
			«IF !c.isAbstract»
				public «c.name» build() {
					return new «c.name»(this);
				}
			«ELSE»
				public abstract «c.name» build();
			«ENDIF»
		
			@Override
			public «builderName(c)» prune() {
				«IF c.superType!==null»super.prune();«ENDIF»
				«FOR attribute : c.expandedAttributes»
					«IF !attribute.isMultiple && (attribute.isRosettaClassOrData || attribute.hasMetas)»
						if («attribute.name»!=null && !«attribute.name».prune().hasData()) «attribute.name» = null;
					«ELSEIF attribute.isMultiple && attribute.isRosettaClassOrData || attribute.hasMetas»
						if («attribute.name»!=null) «attribute.name» = «attribute.name».stream().filter(b->b!=null).map(b->b.prune()).filter(b->b.hasData()).collect(Collectors.toList());
					«ENDIF»
				«ENDFOR»
				return this;
			}
			
			«c.expandedAttributes.hasData(c.superType!==null)»
			
			«c.expandedAttributes.process(c.superType!==null)»
		
			«c.builderBoilerPlate»
		}
	'''
	
	def builderImplements(RosettaClass c) {
		val implementsS = c.implementsClause[String s | '''«s.builderName»<«c.builderName»>''']
		
		
		if (c.name=="ContractualProduct" || c.name=="Event") {
			if (implementsS.length>0) '''«implementsS», Qualified'''
			else "implements Qualified"
		}
		else {
			implementsS
		}
	}
	
	def qualificationSetter(RosettaClass clazz) {
		val startAtt = RosettaFactoryImpl.eINSTANCE.createRosettaRegularAttribute
		startAtt.type = clazz
		val path = BreadthFirstSearch.search(startAtt, [att|att.getType.children], [att|att.type instanceof RosettaQualifiedType])
		if (path!==null) {
			'''
			public void setQualification(String qualification) {
				this«path.toSetter»
			}
			'''
		}
	}
	
	private def String toSetter(List<RosettaRegularAttribute> path) {
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

	private def process(List<ExpandedAttribute> attributes, boolean hasSuperType) '''
		@Override
		public void process(RosettaPath path, BuilderProcessor processor) {
			«IF hasSuperType»
				super.process(path, processor);
			«ENDIF»

			«FOR a : attributes.filter[!(isRosettaClassOrData || hasMetas)]»
				processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle».class, «a.name», this);
			«ENDFOR»
			
			«FOR a : attributes.filter[isRosettaClassOrData || hasMetas]»
				processRosetta(path.newSubPath("«a.name»"), processor, «a.toTypeSingle».class, «a.name»);
			«ENDFOR»
		}
	'''
	
	private def Collection<RosettaRegularAttribute> children(RosettaType c) {
		if (c instanceof RosettaClass) {
			c.regularAttributes
		}
		else {
			emptyList
		}
	}
	
	private def StringConcatenationClient builderGetters(List<ExpandedAttribute> attributes) '''
		«FOR attribute : attributes»
			
			public «attribute.toBuilderType» get«attribute.name.toFirstUpper»() {
				return «attribute.name»;
			}
			
			«IF attribute.isRosettaClassOrData || attribute.hasMetas»
				«IF !attribute.cardinalityIsListValue»
					public «attribute.toBuilderTypeSingle» getOrCreate«attribute.name.toFirstUpper»() {
						if («attribute.name»!=null) {
							return «attribute.name»;
						}
						else return «attribute.name» = new «attribute.toBuilderTypeSingle»();
					}
					
				«ELSE»
					public «attribute.toBuilderTypeSingle» getOrCreate«attribute.name.toFirstUpper»(int index) {
						if («attribute.name»==null) {
							this.«attribute.name» = new «JavaType.create(ArrayList.name)»<>();
						}
						return getIndex(«attribute.name», index, ()->new «attribute.toBuilderTypeSingle»());
					}
					
				«ENDIF»
			«ENDIF»
		«ENDFOR»
	'''
	
		
	private def setters(RosettaClass c) {
		var result = new StringBuilder(c.setters(c, false))
		var current = c.superType
		
		while (current !== null) {
			result.append(c.setters(current, true))
			current = current.superType
		}
		return result.toString
	}
		
	private def StringConcatenationClient setters(Data c, JavaNames names) {
		'''
		«FOR current : c.allSuperTypes»
		«c.setters(current, current != c, names)»
		«ENDFOR»
		'''
	}
		
	
	private def StringConcatenationClient setters(Data thisClass, Data clazz, boolean isSuper, JavaNames names) '''
		«FOR attribute : clazz.expandedAttributes»
			«IF attribute.cardinalityIsListValue»
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle(names)» «attribute.name») {
					if(this.«attribute.name» == null){
						this.«attribute.name» = new ArrayList<>();
					}
					this.«attribute.name».add(«attribute.toBuilder»);
					return this;
				}
				
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»(List<«attribute.toTypeSingle(names)»> «attribute.name»s) {
					if(this.«attribute.name» == null){
						this.«attribute.name» = new «ArrayList»<>();
					}
					for («attribute.toTypeSingle(names)» toAdd : «attribute.name»s) {
						this.«attribute.name».add(toAdd.toBuilder());
					}
					return this;
				}
				
				«IF attribute.isRosettaClassOrData»
					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Builder(«attribute.toBuilderTypeSingle(names)» «attribute.name») {
						if(this.«attribute.name» == null){
							this.«attribute.name» = new ArrayList<>();
							this.«attribute.name».add(«attribute.name»);
						} else {
							this.«attribute.name».add(«attribute.name»);
						}
						return this;
					}
					
				«ENDIF»
				
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» clear«attribute.name.toFirstUpper»() {
					this.«attribute.name» = null;
					return this;
				}
			«ELSE»
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»(«attribute.toType(names)» «attribute.name») {
					this.«attribute.name» = «attribute.toBuilder»;
					return this;
				}
				
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»(«MapperBuilder»<«attribute.toType(names)»> «attribute.name») {
					set«attribute.name.toFirstUpper»(«attribute.name».get());
					return this;
				}

				«IF attribute.isRosettaClassOrData»
					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Builder(«attribute.toBuilderType» «attribute.name») {
						this.«attribute.name» = «attribute.name»;
						return this;
					}
					
				«ENDIF»
			«ENDIF»
		«ENDFOR»
	'''
	
	private def setters(RosettaClass thisClass, RosettaClass clazz, boolean isSuper) '''
		«FOR attribute : clazz.expandedAttributes»
			«IF attribute.cardinalityIsListValue»
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»(«attribute.toTypeSingle» «attribute.name») {
					if(this.«attribute.name» == null){
						this.«attribute.name» = new ArrayList<>();
						this.«attribute.name».add(«attribute.toBuilder»);
					} else {
						this.«attribute.name».add(«attribute.toBuilder»);
					}
					return this;
				}
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»(List<«attribute.toTypeSingle()»> «attribute.name»s) {
					if(this.«attribute.name» == null){
						this.«attribute.name» = new ArrayList<>();
					}
					for («attribute.toTypeSingle()» toAdd : «attribute.name»s) {
						this.«attribute.name».add(toAdd«IF needsBuilder(attribute)».toBuilder()«ENDIF»);
					}
					return this;
				}
				«IF attribute.isRosettaClassOrData»
					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» add«attribute.name.toFirstUpper»Builder(«attribute.toBuilderTypeSingle» «attribute.name») {
						if(this.«attribute.name» == null){
							this.«attribute.name» = new ArrayList<>();
							this.«attribute.name».add(«attribute.name»);
						} else {
							this.«attribute.name».add(«attribute.name»);
						}
						return this;
					}
					
				«ENDIF»
				
				«IF isSuper»@Override «ENDIF»public «thisClass.builderName» clear«attribute.name.toFirstUpper»() {
					this.«attribute.name» = null;
					return this;
				}
			«ELSE»
				«IF isSuper || clazz.globalKey && attribute.name === 'globalKey'»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»(«attribute.toType» «attribute.name») {
					this.«attribute.name» = «attribute.toBuilder»;
					return this;
				}
				«IF attribute.isRosettaClassOrData»
					«IF isSuper»@Override «ENDIF»public «thisClass.builderName» set«attribute.name.toFirstUpper»Builder(«attribute.toBuilderType» «attribute.name») {
						this.«attribute.name» = «attribute.name»;
						return this;
					}
					
				«ENDIF»
			«ENDIF»
		«ENDFOR»
	'''
	
	
	private def hasData(List<ExpandedAttribute> attributes, boolean hasSuperType) '''
		@Override
		public boolean hasData() {
			«IF hasSuperType»if (super.hasData()) return true;«ENDIF»
			«FOR attribute:attributes»    
				«IF attribute.cardinalityIsListValue»
					«IF attribute.isRosettaClassOrData»
						if (get«attribute.name.toFirstUpper»()!=null && get«attribute.name.toFirstUpper»().stream().filter(Objects::nonNull).anyMatch(a->a.hasData())) return true;
					«ELSE»
						if (get«attribute.name.toFirstUpper»()!=null && !get«attribute.name.toFirstUpper»().isEmpty()) return true;
					«ENDIF»
				«ELSEIF attribute.isRosettaClassOrData»
					if (get«attribute.name.toFirstUpper»()!=null && get«attribute.name.toFirstUpper»().hasData()) return true;
				«ELSE»
					if (get«attribute.name.toFirstUpper»()!=null) return true;
				«ENDIF»
			«ENDFOR»
			return false;
		}
	'''

	
	private def abstractModifier(RosettaClass clazz) '''
		«IF clazz.isAbstract»abstract«ENDIF»
	'''
	
	
	private def toBuilderType(ExpandedAttribute attribute) {
		if (attribute.isMultiple) '''List<«attribute.toBuilderTypeSingle»>'''
		else attribute.toBuilderTypeSingle;
	}
	
	@Deprecated
	private def StringConcatenationClient toBuilderTypeSingle(ExpandedAttribute attribute) {
		if (attribute.hasMetas) {
			if (attribute.refIndex>=0) {
				if (attribute.isRosettaClassOrData)
					'''ReferenceWithMeta«attribute.typeName.toFirstUpper».ReferenceWithMeta«attribute.typeName.toFirstUpper»Builder'''
				else
					'''BasicReferenceWithMeta«attribute.typeName.toFirstUpper».BasicReferenceWithMeta«attribute.typeName.toFirstUpper»Builder'''
			}
			else {
				'''FieldWithMeta«attribute.typeName.toFirstUpper».FieldWithMeta«attribute.typeName.toFirstUpper»Builder'''
			}
		}
		else  {
			'''«attribute.toBuilderTypeUnderlying»'''
		}
	}
	
	private def StringConcatenationClient toBuilderTypeSingle(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.hasMetas) {
			val buildername = if (attribute.refIndex >= 0) {
					if (attribute.isRosettaClassOrData)
						'''ReferenceWithMeta«attribute.typeName.toFirstUpper».ReferenceWithMeta«attribute.typeName.toFirstUpper»Builder'''
					else
						'''BasicReferenceWithMeta«attribute.typeName.toFirstUpper».BasicReferenceWithMeta«attribute.typeName.toFirstUpper»Builder'''
				} else {
					'''FieldWithMeta«attribute.typeName.toFirstUpper».FieldWithMeta«attribute.typeName.toFirstUpper»Builder'''
				}
			'''«names.packages.model.javaType(buildername)»'''
		} else {
			'''«attribute.toBuilderTypeUnderlying»'''
		}
	}
	
	private def toBuilderTypeUnderlying(ExpandedAttribute attribute) {
		if (attribute.isRosettaClassOrData) '''«attribute.typeName».«attribute.typeName»Builder'''
		else attribute.typeName.toJavaType
	}
	
		
	private def toBuilder(ExpandedAttribute attribute) {
		if(needsBuilder(attribute)) {
			'''«attribute.name».toBuilder()'''
		} else {
			attribute.name
		}
	}
	private def needsBuilder(ExpandedAttribute attribute){
		attribute.isRosettaClassOrData || attribute.hasMetas
	}
}