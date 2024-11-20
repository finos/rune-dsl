package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.RosettaEcoreUtil
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.TypeSystem
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
import java.util.List
import java.util.Objects
import javax.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient

class ModelObjectBoilerPlate {

	@Inject extension RosettaEcoreUtil
	@Inject extension ModelObjectBuilderGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem

	val toBuilder = [String s|s + 'Builder']
	val identity = [String s|s]

	def StringConcatenationClient builderBoilerPlate(RDataType c, JavaScope scope) {
		val attrs = c.javaAttributes
		'''
			«c.contributeEquals(attrs, scope)»
			«c.contributeHashCode(attrs, scope)»
			«c.contributeToString(toBuilder, scope)»
		'''
	}
	
	def StringConcatenationClient implementsClause(RDataType d, List<Object> extraInterfaces) {
		val interfaces = newLinkedHashSet
		if(d.EObject.hasKeyedAnnotation)
			interfaces.add(GlobalKey)
		if(d.EObject.hasTemplateAnnotation)
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

	def StringConcatenationClient boilerPlate(RDataType t, JavaScope scope) {
		val attributes = t.javaAttributes
		'''
			«t.contributeEquals(attributes, scope)»
			«t.contributeHashCode(attributes, scope)»
			«t.contributeToString(identity, scope)»
		'''
	}

	private def StringConcatenationClient contributeHashCode(RAttribute attr, JavaScope scope) {
		val id = scope.getIdentifierOrThrow(attr)
		val rMetaAnnotatedType = attr.RMetaAnnotatedType
		'''
			«IF !rMetaAnnotatedType.hasMeta && rMetaAnnotatedType.RType instanceof REnumType»
				«IF attr.isMulti»
					_result = 31 * _result + («id» != null ? «id».stream().map(«Object»::getClass).map(«Class»::getName).mapToInt(«String»::hashCode).sum() : 0);
				«ELSE»
					_result = 31 * _result + («id» != null ? «id».getClass().getName().hashCode() : 0);
				«ENDIF»
			«ELSE»
				_result = 31 * _result + («id» != null ? «id».hashCode() : 0);
			«ENDIF»	
		'''
	}

	private def StringConcatenationClient contributeHashCode(RDataType c, Iterable<RAttribute> attributes, JavaScope scope) {
		val methodScope = scope.methodScope("hashCode")
		'''
		@Override
		public int hashCode() {
			int _result = «c.contribtueSuperHashCode»;
			«FOR field : attributes»
				«field.contributeHashCode(methodScope)»
			«ENDFOR»
			return _result;
		}
		
		'''
	}

	private def StringConcatenationClient contributeToString(RDataType t, (String)=>String classNameFunc, JavaScope scope) {
		val methodScope = scope.methodScope("toString")
		'''
		@Override
		public «String» toString() {
			return "«classNameFunc.apply(t.name)» {" +
				«FOR attribute : t.javaAttributes SEPARATOR ' ", " +'»
					"«attribute.name»=" + this.«methodScope.getIdentifierOrThrow(attribute)» +
				«ENDFOR»
			'}'«IF t.hasSuperDataType» + " " + super.toString()«ENDIF»;
		}
		'''
	}

	private def StringConcatenationClient contributeEquals(RDataType c, Iterable<RAttribute> attributes, JavaScope scope) {
		val methodScope = scope.methodScope("equals")
		'''
		@Override
		public boolean equals(«Object» o) {
			if (this == o) return true;
			if (o == null || !(o instanceof «RosettaModelObject») || !getType().equals(((«RosettaModelObject»)o).getType())) return false;
			«IF c.hasSuperDataType»
				if (!super.equals(o)) return false;
			«ENDIF»
		
			«IF !attributes.empty»«c.name.toFirstUpper» _that = getType().cast(o);«ENDIF»
		
			«FOR field : attributes»
				«field.contributeToEquals(methodScope)»
			«ENDFOR»
			return true;
		}
		
		'''
	}

	private def StringConcatenationClient contributeToEquals(RAttribute a, JavaScope scope) '''
	«IF a.isMulti»
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
			«FOR a : c.allJavaAttributes»
				«IF a.isRosettaModelObject»
					processRosetta(path.newSubPath("«a.name»"), processor, «a.toMetaItemJavaType».class, get«a.name.toFirstUpper»()«a.metaFlags»);
				«ELSE»
					processor.processBasic(path.newSubPath("«a.name»"), «a.toMetaItemJavaType».class, get«a.name.toFirstUpper»(), this«a.metaFlags»);
				«ENDIF»
			«ENDFOR»
		}
		
	'''
	
	def StringConcatenationClient builderProcessMethod(RDataType t) '''
		@Override
		default void process(«RosettaPath» path, «BuilderProcessor» processor) {
			«FOR a : t.allJavaAttributes»
				«IF a.isRosettaModelObject»
					processRosetta(path.newSubPath("«a.name»"), processor, «a.toBuilderTypeSingle».class, get«a.name.toFirstUpper»()«a.metaFlags»);
				«ELSE»
					processor.processBasic(path.newSubPath("«a.name»"), «a.toMetaItemJavaType».class, get«a.name.toFirstUpper»(), this«a.metaFlags»);
				«ENDIF»
			«ENDFOR»
		}
		
	'''

	private def StringConcatenationClient getMetaFlags(RAttribute attribute) {
		if (attribute.isMeta) {
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
}
