package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.types.AttributeMetaType
import com.regnosys.rosetta.generator.java.types.JavaPojoInterface
import com.regnosys.rosetta.generator.java.types.JavaPojoProperty
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil
import com.regnosys.rosetta.generator.java.types.RJavaEnum
import com.rosetta.model.lib.RosettaModelObject
import com.rosetta.model.lib.path.RosettaPath
import com.rosetta.model.lib.process.AttributeMeta
import com.rosetta.model.lib.process.BuilderProcessor
import com.rosetta.model.lib.process.Processor
import com.rosetta.util.ListEquals
import com.rosetta.util.types.JavaClass
import java.util.Collection
import java.util.Objects
import jakarta.inject.Inject
import org.eclipse.xtend2.lib.StringConcatenationClient

class ModelObjectBoilerPlate {

	@Inject extension ModelObjectBuilderGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension JavaTypeUtil

	val toBuilder = [JavaClass<?> t|t.simpleName + "Builder"]
	val identity = [JavaClass<?> t|t.simpleName]

	def StringConcatenationClient builderBoilerPlate(JavaPojoInterface javaType, boolean extended, JavaScope scope) {
		val properties = extended ? javaType.ownProperties : javaType.allProperties
		'''
			«javaType.contributeEquals(extended, properties, scope)»
			«javaType.contributeHashCode(extended, properties, scope)»
			«javaType.contributeToString(extended, properties, toBuilder, scope)»
		'''
	}
	
	def String javaAnnotation(JavaPojoProperty prop) {
		if (prop.type == REFERENCE) {
			return 'address'
		} else
			return prop.name
	}
	
	def String javaRuneAnnotation(JavaPojoProperty prop) {
		if (prop.type == REFERENCE) {
			return '@ref:scoped'
		} else
			return prop.serializedName
	}
	
	def boolean addRuneMetaAnnotation(JavaPojoProperty prop) {
		return prop.type == REFERENCE || 
			prop.type==META_FIELDS || 
			(javaRuneAnnotation(prop) == "@data" && prop.type.isValueRosettaModelObject)
	}
	
	def boolean isScopedReference(JavaPojoProperty prop) {
		return prop.attributeMetaTypes.contains(AttributeMetaType.SCOPED_REFERENCE)
	}
	
	def boolean isScopedKey(JavaPojoProperty prop) {
		return prop.attributeMetaTypes.contains(AttributeMetaType.SCOPED_KEY)
	}
	
	def StringConcatenationClient implementsClause(JavaPojoInterface javaType) {
		'''«FOR i : javaType.interfaces SEPARATOR ', '»«i»«ENDFOR»'''
	}

	def StringConcatenationClient boilerPlate(JavaPojoInterface javaType, boolean extended, JavaScope scope) {
		val properties = extended ? javaType.ownProperties : javaType.allProperties
		'''
			«javaType.contributeEquals(extended, properties, scope)»
			«javaType.contributeHashCode(extended, properties, scope)»
			«javaType.contributeToString(extended, properties, identity, scope)»
		'''
	}

	private def StringConcatenationClient contributeHashCode(JavaPojoProperty prop, JavaScope scope) {
		val id = scope.getIdentifierOrThrow(prop)
		val itemType = prop.type.itemType
		'''
			«IF itemType instanceof RJavaEnum»
				«IF prop.type.isList»
					_result = 31 * _result + («id» != null ? «id».stream().map(«Object»::getClass).map(«Class»::getName).mapToInt(«String»::hashCode).sum() : 0);
				«ELSE»
					_result = 31 * _result + («id» != null ? «id».getClass().getName().hashCode() : 0);
				«ENDIF»
			«ELSE»
				_result = 31 * _result + («id» != null ? «id».hashCode() : 0);
			«ENDIF»	
		'''
	}

	private def StringConcatenationClient contributeHashCode(JavaPojoInterface javaType, boolean extended, Collection<JavaPojoProperty> properties, JavaScope scope) {
		val methodScope = scope.methodScope("hashCode")
		'''
		@Override
		public int hashCode() {
			int _result = «IF extended»super.hashCode()«ELSE»0«ENDIF»;
			«FOR prop : properties»
				«prop.contributeHashCode(methodScope)»
			«ENDFOR»
			return _result;
		}
		
		'''
	}

	private def StringConcatenationClient contributeToString(JavaPojoInterface javaType, boolean extended, Collection<JavaPojoProperty> properties, (JavaClass<?>)=>String classNameFunc, JavaScope scope) {
		val methodScope = scope.methodScope("toString")
		'''
		@Override
		public «String» toString() {
			return "«classNameFunc.apply(javaType)» {" +
				«FOR prop : properties SEPARATOR ' ", " +'»
					"«prop.name»=" + this.«methodScope.getIdentifierOrThrow(prop)» +
				«ENDFOR»
			'}'«IF extended» + " " + super.toString()«ENDIF»;
		}
		'''
	}

	private def StringConcatenationClient contributeEquals(JavaPojoInterface javaType, boolean extended, Collection<JavaPojoProperty> properties, JavaScope scope) {
		val methodScope = scope.methodScope("equals")
		'''
		@Override
		public boolean equals(«Object» o) {
			if (this == o) return true;
			if (o == null || !(o instanceof «RosettaModelObject») || !getType().equals(((«RosettaModelObject»)o).getType())) return false;
			«IF extended»if (!super.equals(o)) return false;«ENDIF»
		
			«IF !properties.empty»«javaType» _that = getType().cast(o);«ENDIF»
		
			«FOR prop : properties»
				«prop.contributeToEquals(methodScope)»
			«ENDFOR»
			return true;
		}
		
		'''
	}

	private def StringConcatenationClient contributeToEquals(JavaPojoProperty prop, JavaScope scope) '''
	«IF prop.type.isList»
		if (!«ListEquals».listEquals(«scope.getIdentifierOrThrow(prop)», _that.«prop.getterName»())) return false;
	«ELSE»
		if (!«Objects».equals(«scope.getIdentifierOrThrow(prop)», _that.«prop.getterName»())) return false;
	«ENDIF»
	'''
	
	def StringConcatenationClient processMethod(JavaPojoInterface javaType) '''
		@Override
		default void process(«RosettaPath» path, «Processor» processor) {
			«FOR prop : javaType.allProperties»
				«IF prop.type.isRosettaModelObject»
					processRosetta(path.newSubPath("«prop.name»"), processor, «prop.type.itemType».class, «prop.getterName»()«prop.metaFlags»);
				«ELSE»
					processor.processBasic(path.newSubPath("«prop.name»"), «prop.type.itemType».class, «prop.getterName»(), this«prop.metaFlags»);
				«ENDIF»
			«ENDFOR»
		}
		
	'''
	
	def StringConcatenationClient builderProcessMethod(JavaPojoInterface javaType) '''
		@Override
		default void process(«RosettaPath» path, «BuilderProcessor» processor) {
			«FOR prop : javaType.allProperties»
				«IF prop.type.isRosettaModelObject»
					processRosetta(path.newSubPath("«prop.name»"), processor, «prop.toBuilderTypeSingle».class, «prop.getterName»()«prop.metaFlags»);
				«ELSE»
					processor.processBasic(path.newSubPath("«prop.name»"), «prop.type.itemType».class, «prop.getterName»(), this«prop.metaFlags»);
				«ENDIF»
			«ENDFOR»
		}
		
	'''

	private def StringConcatenationClient getMetaFlags(JavaPojoProperty prop) {
		if (prop.meta !== null) {
			''', «AttributeMeta».«prop.meta»'''
		}
	}
}
