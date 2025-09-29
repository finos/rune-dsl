package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.types.RObjectFactory
import com.rosetta.model.lib.meta.BasicRosettaMetaData
import jakarta.inject.Inject
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta
import static extension org.eclipse.xtext.EcoreUtil2.*
import com.regnosys.rosetta.types.RType
import com.rosetta.util.DottedPath
import com.regnosys.rosetta.generator.java.types.RJavaWithMetaValue
import com.regnosys.rosetta.generator.java.scoping.JavaClassScope
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.rosetta.util.types.JavaGenericTypeDeclaration
import com.rosetta.util.types.JavaParameterizedType
import java.util.stream.Stream
import com.regnosys.rosetta.types.RMetaAnnotatedType
import com.regnosys.rosetta.rosetta.expression.WithMetaOperation
import com.regnosys.rosetta.types.RosettaTypeProvider
import com.regnosys.rosetta.generator.java.JavaClassGenerator
import org.eclipse.emf.ecore.EObject

class MetaFieldGenerator extends JavaClassGenerator<RMetaAnnotatedType, RJavaWithMetaValue> {
	@Inject extension ModelObjectGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory
	@Inject extension RosettaTypeProvider

	override protected getSource(RMetaAnnotatedType object) {
		return null
	}
	override protected streamObjects(RosettaModel model) {
		streamObjects(model as EObject)
	}
	def Stream<RMetaAnnotatedType> streamObjects(EObject model) {
		Stream.concat(
			model.eAllOfType(Attribute).stream.map[buildRAttribute.RMetaAnnotatedType].filter[hasAttributeMeta],
			model.eAllOfType(WithMetaOperation).stream.map[RMetaAnnotatedType].filter[hasAttributeMeta]
		)
		.distinct
		.filter[toJavaReferenceType instanceof RJavaWithMetaValue]
	}
	override createTypeRepresentation(RMetaAnnotatedType t) {
		t.toJavaReferenceType as RJavaWithMetaValue
	}
	override generate(RMetaAnnotatedType t, RJavaWithMetaValue metaJt, String version, JavaClassScope scope) {
		val targetPackage = t.RType.namespace
		
		if (metaJt instanceof RJavaReferenceWithMeta) {
			referenceWithMeta(targetPackage, metaJt, t.RType, scope)
		} else if (metaJt instanceof RJavaFieldWithMeta) {
			fieldWithMeta(targetPackage, metaJt, t.RType, scope)
		} else {
			throw new UnsupportedOperationException("Invalid JavaType: " + metaJt)
		}
	}

	private def StringConcatenationClient fieldWithMeta(DottedPath root, RJavaFieldWithMeta metaJavaType, RType valueType, JavaClassScope scope) {								
		val dummyMetaClass = createAndRegisterDummyMetaClass(metaJavaType, scope)
		'''
			«metaJavaType.classBody(scope, dummyMetaClass, "1")»
			
			«dummyMetaClass.asClassDeclaration» {
			
			}
		'''
	}
	
	private def StringConcatenationClient referenceWithMeta(DottedPath root, RJavaReferenceWithMeta metaJavaType, RType valueType, JavaClassScope scope) {							
		val dummyMetaClass = createAndRegisterDummyMetaClass(metaJavaType, scope)
		'''
			«metaJavaType.classBody(scope, dummyMetaClass, "1")»
			
			«dummyMetaClass.asClassDeclaration» {
			
			}
		'''
	}
	
	private def RGeneratedJavaClass<?> createAndRegisterDummyMetaClass(RJavaWithMetaValue metaJavaType, JavaClassScope scope) {
		val interf = JavaParameterizedType.from(JavaGenericTypeDeclaration.from(BasicRosettaMetaData), metaJavaType)
		val dummyMetaClass = RGeneratedJavaClass.createWithSuperclass(metaJavaType.escapedPackageName, metaJavaType.simpleName + "Meta", interf)
		scope.fileScope.createIdentifier(dummyMetaClass, dummyMetaClass.simpleName)
		dummyMetaClass
	}

	/** generate once per resource marker */
	private static class MarkerAdapterFactory extends AdapterFactoryImpl {

		final String namespace

		new(String namespace) {
			this.namespace = namespace
		}

		def getNamespace() {
			namespace
		}
	}
}
