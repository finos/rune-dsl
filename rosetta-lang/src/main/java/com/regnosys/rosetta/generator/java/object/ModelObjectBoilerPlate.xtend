package com.regnosys.rosetta.generator.java.object

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.rosetta.model.lib.GlobalKey
import com.rosetta.model.lib.GlobalKey.GlobalKeyBuilder
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.Templatable
import com.rosetta.model.lib.Templatable.TemplatableBuilder
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.process.AttributeMeta
import com.rosetta.model.lib.process.BuilderProcessor
import com.rosetta.model.lib.process.Processor
import com.rosetta.util.ListEquals
import java.util.Collection
import java.util.List
import java.util.Objects
import org.eclipse.xtend2.lib.StringConcatenationClient

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class ModelObjectBoilerPlate {

	@Inject extension RosettaExtensions
	@Inject extension ModelObjectBuilderGenerator

	val toBuilder = [String s|s + 'Builder']
	val identity = [String s|s]

	def StringConcatenationClient builderBoilerPlate(Data c, JavaNames names) {
		val attrs = c.expandedAttributes.toList
		'''
			«c.contributeEquals(attrs, [t|names.toJavaType(t).toBuilderType])»
			«c.contributeHashCode(attrs)»
			«c.contributeToString(toBuilder)»
		'''
	}
	
	def StringConcatenationClient implementsClause(Data d, Collection<Object> extraInterfaces) {
		val interfaces = newHashSet
		if(d.hasKeyedAnnotation)
			interfaces.add(GlobalKey)
		if(d.hasTemplateAnnotation)
			interfaces.add(Templatable)
		interfaces.addAll(extraInterfaces)
		if (interfaces.empty) null else ''', «FOR i : interfaces.sortBy[class.name] SEPARATOR ', '»«i»«ENDFOR»'''
	}
	
	def StringConcatenationClient implementsClauseBuilder(Data d) {
		val interfaces = <StringConcatenationClient>newArrayList
		if (d.hasKeyedAnnotation)
			interfaces.add('''«GlobalKeyBuilder»''')
		if(d.hasTemplateAnnotation)
			interfaces.add('''«TemplatableBuilder»''')
		if(interfaces.empty) null else ''', «FOR i : interfaces SEPARATOR ', '»«i»«ENDFOR»'''
	}
	def StringConcatenationClient toType(ExpandedAttribute attribute, JavaNames names) {
		toType(attribute, names, false)
	}
	def StringConcatenationClient toType(ExpandedAttribute attribute, JavaNames names, boolean underlying) {
		if (attribute.isMultiple) '''List<? extends «attribute.toTypeSingle(names, underlying)»>''' 
		else attribute.toTypeSingle(names, underlying);
	}
	def StringConcatenationClient toTypeSingle(ExpandedAttribute attribute, JavaNames names) {
		toTypeSingle(attribute, names, false)
	}
	def StringConcatenationClient toTypeSingle(ExpandedAttribute attribute, JavaNames names, boolean underlying) {
		if(!attribute.hasMetas || underlying) return '''«names.toJavaType(attribute.type)»'''
		val metaType = if (attribute.refIndex >= 0) {
				if (attribute.isDataType)
					'''ReferenceWithMeta«attribute.type.name.toFirstUpper»'''
				else
					'''BasicReferenceWithMeta«attribute.type.name.toFirstUpper»'''
			} else
				'''FieldWithMeta«attribute.type.name.toFirstUpper»'''

		return '''«names.toMetaType(attribute,metaType)»'''
	}

	def StringConcatenationClient boilerPlate(Data c, JavaNames names) {
		val attributes = c.expandedAttributes.toList
		'''
			«c.contributeEquals(attributes, [t|names.toJavaType(t)])»
			«c.contributeHashCode(attributes)»
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

	private def StringConcatenationClient contributeEquals(Data c, List<ExpandedAttribute> attributes, (Data)=>JavaType classNameFunc) '''
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof «RosettaModelObject») || !getType().equals(((RosettaModelObject)o).getType())) return false;
			«IF c.hasSuperType»
				if (!super.equals(o)) return false;
			«ENDIF»
		
			«IF !attributes.empty»«c.name.toFirstUpper» _that = getType().cast(o);«ENDIF»
		
			«FOR field : attributes.filter[!overriding]»
				«field.contributeToEquals»
			«ENDFOR»
			return true;
		}
		
	'''

	private def StringConcatenationClient contributeToEquals(ExpandedAttribute a) '''
	«IF a.cardinalityIsListValue»
		if (!«ListEquals».listEquals(«a.name», _that.get«a.name.toFirstUpper»())) return false;
	«ELSE»
		if (!«Objects».equals(«a.name», _that.get«a.name.toFirstUpper»())) return false;
	«ENDIF»
	'''

	private def contribtueSuperHashCode(Data c) {
		if(c.hasSuperType) 'super.hashCode()' else '0'
	}
	
	def StringConcatenationClient processMethod(Data c, JavaNames names) '''
		@Override
		default void process(«RosettaPath» path, «Processor» processor) {
			«IF c.hasSuperType»
				«names.toJavaType(c.superType).name».super.process(path, processor);
			«ENDIF»
			«FOR a : c.expandedAttributes.filter[!overriding].filter[!(isDataType || hasMetas)]»
				processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle(names)».class, get«a.name.toFirstUpper»(), this«a.metaFlags»);
			«ENDFOR»
			
			«FOR a : c.expandedAttributes.filter[!overriding].filter[isDataType || hasMetas]»
				processRosetta(path.newSubPath("«a.name»"), processor, «a.toTypeSingle(names)».class, get«a.name.toFirstUpper»()«a.metaFlags»);
			«ENDFOR»
		}
		
	'''
	
	def StringConcatenationClient builderProcessMethod(Data c, JavaNames names) '''
		@Override
		default void process(«RosettaPath» path, «BuilderProcessor» processor) {
			«IF c.hasSuperType»
				«names.toJavaType(c.superType).toBuilderType».super.process(path, processor);
			«ENDIF»
			
			«FOR a : c.expandedAttributes.filter[!overriding].filter[!(isDataType || hasMetas)]»
				processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle(names)».class, get«a.name.toFirstUpper»(), this«a.metaFlags»);
			«ENDFOR»
			
			«FOR a : c.expandedAttributes.filter[!overriding].filter[isDataType || hasMetas]»
				processRosetta(path.newSubPath("«a.name»"), processor, «a.toBuilderTypeSingle(names)».class, get«a.name.toFirstUpper»()«a.metaFlags»);
			«ENDFOR»
		}
		
	'''

	private def StringConcatenationClient getMetaFlags(ExpandedAttribute attribute) {
		if (attribute.type.isMetaType) {
			''', «AttributeMeta».META'''
		}
		else if (attribute.hasIdAnnotation) {
			''', «AttributeMeta».GLOBAL_KEY_FIELD'''
		}
	}
	
	def needsBuilder(ExpandedAttribute attribute){
		attribute.isDataType || attribute.hasMetas
	}
}
