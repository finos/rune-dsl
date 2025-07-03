package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.types.RObjectFactory
import com.rosetta.model.lib.meta.BasicRosettaMetaData
import com.rosetta.util.types.generated.GeneratedJavaClass
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
		'''
			«metaJavaType.classBody(scope, new GeneratedJavaClass<Object>(metaJavaType.packageName, metaJavaType.simpleName + "Meta", Object), "1")»
			
			class «metaJavaType.simpleName»Meta extends «BasicRosettaMetaData»<«metaJavaType.simpleName»> {
			
			}
		'''
	}
	
	private def StringConcatenationClient referenceWithMeta(DottedPath root, RJavaReferenceWithMeta metaJavaType, RType valueType, JavaClassScope scope) {							
		'''
			«metaJavaType.classBody(scope, new GeneratedJavaClass<Object>(root.child("metaField"), metaJavaType.simpleName + "Meta", Object), "1")»
			
			class «metaJavaType.simpleName»Meta extends «BasicRosettaMetaData»<«metaJavaType.simpleName»> {
			
			}
		'''
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
