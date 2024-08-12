package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.RosettaExtensions
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
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.rosetta.util.types.JavaType
import javax.inject.Inject
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.TypeSystem

class ModelObjectBoilerPlate {

	@Inject extension RosettaExtensions
	@Inject extension ModelObjectBuilderGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem

	val toBuilder = [String s|s + 'Builder']
	val identity = [String s|s]

	def StringConcatenationClient builderBoilerPlate(RDataType c, JavaScope scope) {
		val attrs = c.expandedAttributes.toList
		'''
			«c.contributeEquals(attrs, scope)»
			«c.contributeHashCode(attrs, scope)»
			«c.contributeToString(toBuilder, scope)»
		'''
	}
	
	def StringConcatenationClient implementsClause(RDataType d, Collection<Object> extraInterfaces) {
		val interfaces = newHashSet
		if(d.data.hasKeyedAnnotation)
			interfaces.add(GlobalKey)
		if(d.data.hasTemplateAnnotation)
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
	def JavaType toListOrSingleMetaType(ExpandedAttribute attribute) {
		if (attribute.isMultiple) attribute.toMetaOrRegularJavaType.toPolymorphicList
		else attribute.toMetaOrRegularJavaType;
	}

	def StringConcatenationClient boilerPlate(RDataType c, JavaScope scope) {
		val attributes = c.expandedAttributes.toList
		'''
			«c.contributeEquals(attributes, scope)»
			«c.contributeHashCode(attributes, scope)»
			«c.contributeToString(identity, scope)»
		'''
	}

	private def StringConcatenationClient contributeHashCode(ExpandedAttribute attr, JavaScope scope) {
		val id = scope.getIdentifierOrThrow(attr)
		'''
			«IF attr.enum»
				«IF attr.list»
					_result = 31 * _result + («id» != null ? «id».stream().map(Object::getClass).map(Class::getName).mapToInt(String::hashCode).sum() : 0);
				«ELSE»
					_result = 31 * _result + («id» != null ? «id».getClass().getName().hashCode() : 0);
				«ENDIF»
			«ELSE»
				_result = 31 * _result + («id» != null ? «id».hashCode() : 0);
			«ENDIF»	
		'''
	}

	private def StringConcatenationClient contributeHashCode(RDataType c, List<ExpandedAttribute> attributes, JavaScope scope) {
		val methodScope = scope.methodScope("hashCode")
		'''
		@Override
		public int hashCode() {
			int _result = «c.contribtueSuperHashCode»;
			«FOR field : attributes.filter[!overriding]»
				«field.contributeHashCode(methodScope)»
			«ENDFOR»
			return _result;
		}
		
		'''
	}

	private def StringConcatenationClient contributeToString(RDataType c, (String)=>String classNameFunc, JavaScope scope) {
		val methodScope = scope.methodScope("toString")
		'''
		@Override
		public String toString() {
			return "«classNameFunc.apply(c.name)» {" +
				«FOR attribute : c.expandedAttributes.filter[!overriding] SEPARATOR ' ", " +'»
					"«attribute.name»=" + this.«methodScope.getIdentifierOrThrow(attribute)» +
				«ENDFOR»
			'}'«IF c.hasSuperDataType» + " " + super.toString()«ENDIF»;
		}
		'''
	}

	private def StringConcatenationClient contributeEquals(RDataType c, List<ExpandedAttribute> attributes, JavaScope scope) {
		val methodScope = scope.methodScope("equals")
		'''
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || !(o instanceof «RosettaModelObject») || !getType().equals(((RosettaModelObject)o).getType())) return false;
			«IF c.hasSuperDataType»
				if (!super.equals(o)) return false;
			«ENDIF»
		
			«IF !attributes.empty»«c.name.toFirstUpper» _that = getType().cast(o);«ENDIF»
		
			«FOR field : attributes.filter[!overriding]»
				«field.contributeToEquals(methodScope)»
			«ENDFOR»
			return true;
		}
		
		'''
	}

	private def StringConcatenationClient contributeToEquals(ExpandedAttribute a, JavaScope scope) '''
	«IF a.cardinalityIsListValue»
		if (!«ListEquals».listEquals(«scope.getIdentifierOrThrow(a)», _that.get«a.name.toFirstUpper»())) return false;
	«ELSE»
		if (!«Objects».equals(«scope.getIdentifierOrThrow(a)», _that.get«a.name.toFirstUpper»())) return false;
	«ENDIF»
	'''

	private def contribtueSuperHashCode(RDataType c) {
		if(c.hasSuperDataType) 'super.hashCode()' else '0'
	}
	
	def StringConcatenationClient processMethod(RDataType c) '''
		@Override
		default void process(«RosettaPath» path, «Processor» processor) {
			«FOR a : c.expandedAttributesPlus.filter[!overriding]»
				«IF a.isDataType || a.hasMetas»
					processRosetta(path.newSubPath("«a.name»"), processor, «a.toMetaOrRegularJavaType».class, get«a.name.toFirstUpper»()«a.metaFlags»);
				«ELSE»
					processor.processBasic(path.newSubPath("«a.name»"), «a.toMetaOrRegularJavaType».class, get«a.name.toFirstUpper»(), this«a.metaFlags»);
				«ENDIF»
			«ENDFOR»
		}
		
	'''
	
	def StringConcatenationClient builderProcessMethod(RDataType c) '''
		@Override
		default void process(«RosettaPath» path, «BuilderProcessor» processor) {
			«FOR a : c.expandedAttributesPlus.filter[!overriding]»
				«IF a.isDataType || a.hasMetas»
					processRosetta(path.newSubPath("«a.name»"), processor, «a.toBuilderTypeSingle».class, get«a.name.toFirstUpper»()«a.metaFlags»);
				«ELSE»
					processor.processBasic(path.newSubPath("«a.name»"), «a.toMetaOrRegularJavaType».class, get«a.name.toFirstUpper»(), this«a.metaFlags»);
				«ENDIF»
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
	
	private def boolean hasSuperDataType(RDataType c) {
		val s = c.superType
		return s !== null && s.stripFromTypeAliases instanceof RDataType
	}
	
	def needsBuilder(ExpandedAttribute attribute){
		attribute.isDataType || attribute.hasMetas
	}
}
