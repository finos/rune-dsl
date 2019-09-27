package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaFeature
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.rosetta.util.ListEquals
import java.util.Collections
import java.util.List
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class ModelObjectBoilerPlate {

//	@Inject extension ExternalHashcodeGenerator

	val toBuilder = [String s|s + 'Builder']
	val identity = [String s|s]

	def boilerPlate(RosettaClass c) '''
		«c.wrap.processMethod»
		«c.wrap.boilerPlate»
	'''
	
	def StringConcatenationClient boilerPlate(com.regnosys.rosetta.rosetta.simple.Data d) '''
		«d.wrap.processMethod»
		«d.wrap.boilerPlate»
	'''

	def calculationResultBoilerPlate(String ownerName, List<? extends RosettaFeature> features) {
		features.wrapCalculationResult(ownerName).boilerPlate
	}

	def builderBoilerPlate(RosettaClass c) '''
		«c.wrap.contributeEquals(toBuilder)»
		«c.wrap.contributeHashCode»
		«c.wrap.contributeToString(toBuilder)»
	'''
	
	def StringConcatenationClient builderBoilerPlate(com.regnosys.rosetta.rosetta.simple.Data c) '''
		«c.wrap.contributeEquals(toBuilder)»
		«c.wrap.contributeHashCode»
		«c.wrap.contributeToString(toBuilder)»
	'''

	def implementsClause(RosettaClass c) {
		implementsClause(c)[String s|s]
	}

	def implementsClause(extension RosettaClass it, (String)=>String nameFunc) {
		val interfaces = newHashSet
		
		if(globalKey)
			interfaces.add(nameFunc.apply('GlobalKey'))
			
		if(rosettaKeyValue)
			interfaces.add(nameFunc.apply('RosettaKeyValue'))
		
		if (interfaces.empty) '''''' else '''implements «interfaces.join(', ')» '''
	}
	
	@Deprecated
	def toType(ExpandedAttribute attribute) {
		if (attribute.isMultiple) '''List<«attribute.toTypeSingle»>''' 
		else attribute.toTypeSingle;
	}
	
	def StringConcatenationClient toType(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.isMultiple) '''List<«attribute.toTypeSingle(names)»>''' 
		else attribute.toTypeSingle(names);
	}
	
	@Deprecated
	def toTypeSingle(ExpandedAttribute attribute) {
		if (!attribute.hasMetas) attribute.typeName.toJavaType
		else if (attribute.refIndex >= 0) {
			if (attribute.isRosettaClassOrData)
				'''ReferenceWithMeta«attribute.typeName.toFirstUpper»'''
			else
				'''BasicReferenceWithMeta«attribute.typeName.toFirstUpper»'''
		} else
			'''FieldWithMeta«attribute.typeName.toFirstUpper»'''
	}
	
	def StringConcatenationClient toTypeSingle(ExpandedAttribute attribute, JavaNames names) {
		if (!attribute.hasMetas) return '''«attribute.typeName.toJavaType»'''
		val metaType = if (attribute.refIndex >= 0) {
			if (attribute.isRosettaClassOrData)
				'''ReferenceWithMeta«attribute.typeName.toFirstUpper»'''
			else
				'''BasicReferenceWithMeta«attribute.typeName.toFirstUpper»'''
		} else
			'''FieldWithMeta«attribute.typeName.toFirstUpper»'''
			
		return '''«names.packages.metaField.javaType(metaType)»'''
	}

	private def StringConcatenationClient boilerPlate(TypeData c) '''
		«c.contributeEquals(identity)»
		«c.contributeHashCode»
		«c.contributeToString(identity)»
	'''

	private def contributeHashCode(extension ExpandedAttribute it) {
		'''
			«IF enum»
				«IF list»
					_result = 31 * _result + («name» != null ? «name».stream().map(Object::getClass).map(Class::getName).mapToInt(String::hashCode).sum() : 0);
				«ELSE»
					_result = 31 * _result + («name» != null ? «name».getClass().getName().hashCode() : 0);
				«ENDIF»
			«ELSE»
				_result = 31 * _result + («name» != null ? «name».hashCode() : 0);
			«ENDIF»	
		'''
	}

	// the eventEffect attribute should not contribute to the hashcode. The EventEffect must first take the hash from Event, 
	// but once stamped onto EventEffect, this will change the hash for Event. 
	private def contributeHashCode(TypeData c) '''
		@Override
		public int hashCode() {
			int _result = «c.contribtueSuperHashCode»;
			«FOR field : c.attributes.filter[name != 'eventEffect']»
				«field.contributeHashCode»
			«ENDFOR»
			return _result;
		}
		
	'''

	private def contributeToString(TypeData c, (String)=>String classNameFunc) '''
		@Override
		public String toString() {
			return "«classNameFunc.apply(c.name)» {" +
				«FOR attribute : c.attributes.map[name] SEPARATOR ' ", " +'»
					"«attribute»=" + this.«attribute» +
				«ENDFOR»
			'}'«IF c.hasSuperType» + " " + super.toString()«ENDIF»;
		}
	'''

	// the eventEffect attribute should not contribute to the hashcode. The EventEffect must first take the hash from Event, 
	// but once stamped onto EventEffect, this will change the hash for Event. TODO: Have generic way of excluding attributes from the hash
	private def StringConcatenationClient contributeEquals(TypeData c, (String)=>String classNameFunc) '''
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			«IF c.hasSuperType»
				if (!super.equals(o)) return false;
			«ENDIF»
		
			«classNameFunc.apply(c.name)» _that = («classNameFunc.apply(c.name)») o;
		
			«FOR field : c.attributes.filter[s | s.name != 'eventEffect']»
				«field.contributeToEquals»
			«ENDFOR»
			return true;
		}
		
	'''

	private def StringConcatenationClient contributeToEquals(ExpandedAttribute a) '''
	«IF a.cardinalityIsListValue»
		if (!«JavaType.create(ListEquals.name)».listEquals(«a.name», _that.«a.name»)) return false;
	«ELSE»
		if («a.name» != null ? !«a.name».equals(_that.«a.name») : _that.«a.name» != null) return false;
	«ENDIF»
	'''

	private def contribtueSuperHashCode(TypeData c) {
		if(c.hasSuperType) 'super.hashCode()' else '0'
	}

	private def TypeData wrap(RosettaClass rosettaClass) {
		return new TypeData(
			rosettaClass.name,
			rosettaClass.expandedAttributes,
			rosettaClass.superType !== null,
			true
		);
	}
	private def TypeData wrap(com.regnosys.rosetta.rosetta.simple.Data data) {
		return new TypeData(
			data.name,
			data.expandedAttributes,
			data.hasSuperType,
			true
		);
	}

	private def TypeData wrapCalculationResult(List<? extends RosettaFeature> features, String typeName) {
		return new TypeData(typeName, features.map [
			new ExpandedAttribute(null, getNameOrDefault, type, typeName, 0, 1, list, Collections.emptyList, null,
				false, it == RosettaEnumeration, false, Collections.emptyList)
		], false, false);
	}
	
	private def processMethod(extension TypeData it) '''
		@Override
		public void process(RosettaPath path, Processor processor) {
			«IF hasSuperType»
				super.process(path, processor);
			«ENDIF»
			
			«FOR a : attributes.filter[!(isRosettaClassOrData || hasMetas)]»
				«IF a.multiple»
					«a.name».stream().forEach(a->processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle».class, a, this«a.metaFlags»));
				«ELSE»
					processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle».class, «a.name», this«a.metaFlags»);
				«ENDIF»
			«ENDFOR»
			
			«FOR a : attributes.filter[isRosettaClassOrData || hasMetas]»
				processRosetta(path.newSubPath("«a.name»"), processor, «a.toTypeSingle».class, «a.name»«a.metaFlags»);
			«ENDFOR»
		}
		
	'''
	
	private def getMetaFlags(ExpandedAttribute attribute) {
		val result = new StringBuilder()
		if (attribute.type instanceof RosettaMetaType) {
			result.append(", AttributeMeta.IS_META")
		}
		result.toString
	}
	// the eventEffect attribute should not contribute to the rosettaKeyValueHashCode. 
	// TODO: Have generic way of excluding attributes from the hash
	static def boolean isIncludedInRosettaKeyValueHashCode(ExpandedAttribute a) {
		return !( a.hasCalculation || a.isQualified || a.name == 'eventEffect' || a.name == 'globalKey')
	}

	@Data
	static class TypeData {
		val String name
		val List<ExpandedAttribute> attributes
		val boolean hasSuperType
		val boolean generateRosettaKeyValueHashCode
	}
}
