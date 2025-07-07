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
import com.regnosys.rosetta.generator.java.RObjectJavaClassGenerator
import com.regnosys.rosetta.types.RAttribute
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass
import com.rosetta.util.types.JavaGenericTypeDeclaration
import com.rosetta.util.types.JavaParameterizedType

class MetaFieldGenerator extends RObjectJavaClassGenerator<RAttribute, RJavaWithMetaValue> {
	@Inject extension ModelObjectGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory

	override protected streamObjects(RosettaModel model) {
		model.eAllOfType(Attribute).stream.map[buildRAttribute].filter[RMetaAnnotatedType.hasMeta]
	}
	override protected createTypeRepresentation(RAttribute attr) {
		attr.toForcedMetaItemJavaType as RJavaWithMetaValue
	}
	override protected generate(RAttribute attr, RJavaWithMetaValue metaJt, String version, JavaClassScope scope) {
		val targetPackage = attr.RMetaAnnotatedType.RType.namespace
		
		if (metaJt instanceof RJavaReferenceWithMeta) {
			referenceWithMeta(targetPackage, metaJt, attr.RMetaAnnotatedType.RType, scope)
		} else if (metaJt instanceof RJavaFieldWithMeta) {
			fieldWithMeta(targetPackage, metaJt, attr.RMetaAnnotatedType.RType, scope)
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
