package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.GlobalKey
import com.rosetta.model.lib.GlobalKeyBuilder
import com.rosetta.model.lib.Templatable
import com.rosetta.model.lib.Templatable.TemplatableBuilder
import com.rosetta.model.lib.qualify.Qualified
import com.rosetta.util.ListEquals
import java.util.List
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import java.util.Objects

class ModelObjectBoilerPlate {

	@Inject extension RosettaExtensions

	val toBuilder = [String s|s + 'Builder']
	val identity = [String s|s]

	def StringConcatenationClient boilerPlate(Data d, JavaNames names) '''
		«d.processMethod(names)»
		«d.boilerPlate»
	'''

	def StringConcatenationClient builderBoilerPlate(Data c, JavaNames names) {
		val attrs = c.expandedAttributes.filter[name != 'eventEffect'].toList
		'''
			«c.builderProcessMethod(names)»
			«c.contributeEquals(attrs, toBuilder)»
			«c.contributeHashCode(attrs)»
			«c.contributeToString(toBuilder)»
		'''
	}
	
	def StringConcatenationClient implementsClause(Data d) {
		val interfaces = newHashSet
		if(d.hasKeyedAnnotation)
			interfaces.add(GlobalKey)
		if(d.hasTemplateAnnotation)
			interfaces.add(Templatable)
		if (interfaces.empty) null else '''implements «FOR i : interfaces SEPARATOR ', '»«i»«ENDFOR»'''
	}
	
	def StringConcatenationClient implementsClauseBuilder(Data d) {
		val interfaces = <StringConcatenationClient>newArrayList
		if (d.hasKeyedAnnotation)
			interfaces.add('''«GlobalKeyBuilder»''')
		if(d.hasTemplateAnnotation)
			interfaces.add('''«TemplatableBuilder»''')
		if (d.name == "ContractualProduct" || d.name == "BusinessEvent") {
			interfaces.add('''«Qualified»''')
		}
		if(interfaces.empty) null else ''' implements «FOR i : interfaces SEPARATOR ', '»«i»«ENDFOR»'''
	}
	
	def StringConcatenationClient toType(ExpandedAttribute attribute, JavaNames names) {
		if (attribute.isMultiple) '''List<«attribute.toTypeSingle(names)»>''' 
		else attribute.toTypeSingle(names);
	}
	
	def StringConcatenationClient toTypeSingle(ExpandedAttribute attribute, JavaNames names) {
		if(!attribute.hasMetas) return '''«names.toJavaType(attribute.type)»'''
		val metaType = if (attribute.refIndex >= 0) {
				if (attribute.isRosettaType)
					'''ReferenceWithMeta«attribute.type.name.toFirstUpper»'''
				else
					'''BasicReferenceWithMeta«attribute.type.name.toFirstUpper»'''
			} else
				'''FieldWithMeta«attribute.type.name.toFirstUpper»'''

		return '''«names.toMetaType(attribute,metaType)»'''
	}

	private def StringConcatenationClient boilerPlate(Data c) {
		val attributesNoEventEffect = c.expandedAttributes.filter[name != 'eventEffect'].toList
		'''
			«c.contributeEquals(attributesNoEventEffect, identity)»
			«c.contributeHashCode(attributesNoEventEffect)»
			«c.contributeToString(identity)»
		'''
	} 

	private def contributeHashCode(ExpandedAttribute it) {
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
	private def contributeHashCode(Data c, List<ExpandedAttribute> attributes) '''
		@Override
		public int hashCode() {
			int _result = «c.contribtueSuperHashCode»;
			«FOR field : attributes.filter[!overriding]»
				«field.contributeHashCode»
			«ENDFOR»
			return _result;
		}
		
	'''

	private def contributeToString(Data c, (String)=>String classNameFunc) '''
		@Override
		public String toString() {
			return "«classNameFunc.apply(c.name)» {" +
				«FOR attribute : c.expandedAttributes.filter[!overriding].map[name] SEPARATOR ' ", " +'»
					"«attribute»=" + this.«attribute» +
				«ENDFOR»
			'}'«IF c.hasSuperType» + " " + super.toString()«ENDIF»;
		}
	'''

	// the eventEffect attribute should not contribute to the hashcode. The EventEffect must first take the hash from Event, 
	// but once stamped onto EventEffect, this will change the hash for Event. TODO: Have generic way of excluding attributes from the hash
	private def StringConcatenationClient contributeEquals(Data c, List<ExpandedAttribute> attributes, (String)=>String classNameFunc) '''
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			«IF c.hasSuperType»
				if (!super.equals(o)) return false;
			«ENDIF»
		
			«IF !attributes.empty»«classNameFunc.apply(c.name)» _that = («classNameFunc.apply(c.name)») o;«ENDIF»
		
			«FOR field : attributes.filter[!overriding]»
				«field.contributeToEquals»
			«ENDFOR»
			return true;
		}
		
	'''

	private def StringConcatenationClient contributeToEquals(ExpandedAttribute a) '''
	«IF a.cardinalityIsListValue»
		if (!«ListEquals».listEquals(«a.name», _that.«a.name»)) return false;
	«ELSE»
		if (!«Objects».equals(«a.name.toFirstLower», _that.«a.name.toFirstLower»)) return false;
	«ENDIF»
	'''

	private def contribtueSuperHashCode(Data c) {
		if(c.hasSuperType) 'super.hashCode()' else '0'
	}

	private def processMethod(Data c, JavaNames names) '''
		@Override
		public void process(RosettaPath path, Processor processor) {
			«IF c.hasSuperType»
				super.process(path, processor);
			«ENDIF»
			
			«FOR a : c.expandedAttributes.filter[!(isRosettaType || hasMetas)]»
				«IF a.multiple»
					«a.name».stream().forEach(a->processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle(names)».class, a, this«a.metaFlags»));
				«ELSE»
					processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle(names)».class, «a.name», this«a.metaFlags»);
				«ENDIF»
			«ENDFOR»
			
			«FOR a : c.expandedAttributes.filter[!overriding].filter[isRosettaType || hasMetas]»
				processRosetta(path.newSubPath("«a.name»"), processor, «a.toTypeSingle(names)».class, «a.name»«a.metaFlags»);
			«ENDFOR»
		}
		
	'''
	
	private def builderProcessMethod(Data c, JavaNames names) '''
		@Override
		public void process(RosettaPath path, BuilderProcessor processor) {
			«IF c.hasSuperType»
				super.process(path, processor);
			«ENDIF»
			
			«FOR a : c.expandedAttributes.filter[!overriding].filter[!(isRosettaType || hasMetas)]»
				processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle(names)».class, «a.name», this«a.metaFlags»);
			«ENDFOR»
			
			«FOR a : c.expandedAttributes.filter[!overriding].filter[isRosettaType || hasMetas]»
				processRosetta(path.newSubPath("«a.name»"), processor, «a.toTypeSingle(names)».class, «a.name»«a.metaFlags»);
			«ENDFOR»
		}
		
	'''

    private def getMetaFlags(ExpandedAttribute attribute) {
		val result = new StringBuilder()
		if (attribute.type.isMetaType) {
			result.append(", AttributeMeta.IS_META")
		}
		if (attribute.hasIdAnnotation) {
			result.append(", AttributeMeta.IS_GLOBAL_KEY_FIELD")
		}
		result.toString
	}
}
