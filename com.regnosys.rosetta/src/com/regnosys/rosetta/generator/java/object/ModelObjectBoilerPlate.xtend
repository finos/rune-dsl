package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.RosettaClass
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.GlobalKey
import com.rosetta.model.lib.GlobalKeyBuilder
import com.rosetta.model.lib.RosettaKeyValue
import com.rosetta.model.lib.RosettaKeyValueBuilder
import com.rosetta.model.lib.qualify.Qualified
import com.rosetta.util.ListEquals
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class ModelObjectBoilerPlate {

	@Inject extension RosettaExtensions

	val toBuilder = [String s|s + 'Builder']
	val identity = [String s|s]

	def StringConcatenationClient boilerPlate(RosettaClass c, JavaNames names) '''
		«c.wrap.processMethod(names)»
		«c.wrap.boilerPlate»
	'''
	
	def StringConcatenationClient boilerPlate(Data d, JavaNames names) '''
		«d.wrap.processMethod(names)»
		«d.wrap.boilerPlate»
	'''


	def StringConcatenationClient builderBoilerPlate(RosettaClass c) {
		val wrap = c.wrap
		val attrs = wrap.attributes.filter[name != 'eventEffect'].toList
		'''
			«wrap.contributeEquals(attrs, toBuilder)»
			«wrap.contributeHashCode(attrs)»
			«wrap.contributeToString(toBuilder)»
		'''
	}

	def StringConcatenationClient builderBoilerPlate(Data c) {
		val wrap = c.wrap
		val attrs = wrap.attributes.filter[name != 'eventEffect'].toList
		'''
			«wrap.contributeEquals(attrs, toBuilder)»
			«wrap.contributeHashCode(attrs)»
			«wrap.contributeToString(toBuilder)»
		'''
	}
		

	def implementsClause(RosettaClass c) {
		implementsClause(c)[String s|s]
	}

	def StringConcatenationClient implementsClause(extension Data d) {
		val interfaces = newHashSet
		if(d.hasKeyedAnnotation)
			interfaces.add(GlobalKey)
		if(d.hasPartialKeyAnnotation)
			interfaces.add(RosettaKeyValue)
		if (interfaces.empty) null else '''implements «FOR i : interfaces SEPARATOR ','»«i»«ENDFOR»'''
	}
	
	def StringConcatenationClient implementsClauseBuilder(extension Data d) {
		val interfaces = <StringConcatenationClient>newArrayList
		if (d.hasKeyedAnnotation)
			interfaces.add('''«GlobalKeyBuilder»<«d.name»Builder>''')
		if (d.hasPartialKeyAnnotation)
			interfaces.add('''«RosettaKeyValueBuilder»<«d.name»Builder>''')
		if (d.name == "ContractualProduct" || d.name == "WorkflowEvent") {
			interfaces.add('''«Qualified»''')
		}
		if(interfaces.empty) null else ''' implements «FOR i : interfaces SEPARATOR ','»«i»«ENDFOR»'''
	}
	
	def implementsClause(extension RosettaClass it, (String)=>String nameFunc) {
		val interfaces = newHashSet
		
		if(globalKey)
			interfaces.add(nameFunc.apply('GlobalKey'))
			
		if(rosettaKeyValue)
			interfaces.add(nameFunc.apply('RosettaKeyValue'))
		
		if (interfaces.empty) '''''' else '''implements «interfaces.join(', ')» '''
	}
	
	
	def StringConcatenationClient toType(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.isMultiple) '''List<«attribute.toTypeSingle(names)»>''' 
		else attribute.toTypeSingle(names);
	}
	def StringConcatenationClient toTypeSingle(ExpandedAttribute attribute, JavaNames names) {
		if(!attribute.hasMetas) return '''«names.toJavaType(attribute.type)»'''
		val metaType = if (attribute.refIndex >= 0) {
				if (attribute.isRosettaClassOrData)
					'''ReferenceWithMeta«attribute.typeName.toFirstUpper»'''
				else
					'''BasicReferenceWithMeta«attribute.typeName.toFirstUpper»'''
			} else
				'''FieldWithMeta«attribute.typeName.toFirstUpper»'''

		return '''«names.toMetaType(attribute,metaType)»'''
	}

	private def StringConcatenationClient boilerPlate(TypeData c) {
		val attributesNoEventEffect = c.attributes.filter[name != 'eventEffect'].toList
		'''
			«c.contributeEquals(attributesNoEventEffect, identity)»
			«c.contributeHashCode(attributesNoEventEffect)»
			«c.contributeToString(identity)»
		'''
	} 

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
	private def contributeHashCode(TypeData c, List<ExpandedAttribute> attributes) '''
		@Override
		public int hashCode() {
			int _result = «c.contribtueSuperHashCode»;
			«FOR field : attributes»
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
	private def StringConcatenationClient contributeEquals(TypeData c, List<ExpandedAttribute> attributes, (String)=>String classNameFunc) '''
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			«IF c.hasSuperType»
				if (!super.equals(o)) return false;
			«ENDIF»
		
			«IF !attributes.empty»«classNameFunc.apply(c.name)» _that = («classNameFunc.apply(c.name)») o;«ENDIF»
		
			«FOR field : attributes»
				«field.contributeToEquals»
			«ENDFOR»
			return true;
		}
		
	'''

	private def StringConcatenationClient contributeToEquals(ExpandedAttribute a) '''
	«IF a.cardinalityIsListValue»
		if (!«ListEquals».listEquals(«a.name», _that.«a.name»)) return false;
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
	private def TypeData wrap(Data data) {
		return new TypeData(
			data.name,
			data.expandedAttributes,
			data.hasSuperType,
			true
		);
	}
	
	private def processMethod(extension TypeData it,  JavaNames names) '''
		@Override
		public void process(RosettaPath path, Processor processor) {
			«IF hasSuperType»
				super.process(path, processor);
			«ENDIF»
			
			«FOR a : attributes.filter[!(isRosettaClassOrData || hasMetas)]»
				«IF a.multiple»
					«a.name».stream().forEach(a->processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle(names)».class, a, this«a.metaFlags»));
				«ELSE»
					processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle(names)».class, «a.name», this«a.metaFlags»);
				«ENDIF»
			«ENDFOR»
			
			«FOR a : attributes.filter[isRosettaClassOrData || hasMetas]»
				processRosetta(path.newSubPath("«a.name»"), processor, «a.toTypeSingle(names)».class, «a.name»«a.metaFlags»);
			«ENDFOR»
		}
		
	'''
	
	private def getMetaFlags(ExpandedAttribute attribute) {
		val result = new StringBuilder()
		if (attribute.type.isMetaType) {
			result.append(", AttributeMeta.IS_META")
		}
		result.toString
	}
	// the eventEffect attribute should not contribute to the rosettaKeyValueHashCode. 
	// TODO: Have generic way of excluding attributes from the hash
	static def boolean isIncludedInRosettaKeyValueHashCode(ExpandedAttribute a) {
		return !( a.hasCalculation || a.isQualified || a.name == 'eventEffect' || a.name == 'globalKey')
	}

	@org.eclipse.xtend.lib.annotations.Data
	static class TypeData {
		val String name
		val List<ExpandedAttribute> attributes
		val boolean hasSuperType
		val boolean generateRosettaKeyValueHashCode
	}
}
