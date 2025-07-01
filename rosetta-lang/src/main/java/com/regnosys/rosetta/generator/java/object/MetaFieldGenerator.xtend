package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.types.RObjectFactory
import com.rosetta.model.lib.meta.BasicRosettaMetaData
import com.rosetta.util.types.generated.GeneratedJavaClass
import jakarta.inject.Inject
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import com.regnosys.rosetta.generator.java.types.RJavaFieldWithMeta
import com.regnosys.rosetta.generator.java.types.RJavaReferenceWithMeta
import static extension org.eclipse.xtext.EcoreUtil2.*
import com.regnosys.rosetta.types.RType
import com.regnosys.rosetta.generator.java.scoping.JavaStatementScope
import com.rosetta.util.DottedPath

class MetaFieldGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension ModelObjectGenerator
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory

	 
	def void generate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ctx) {
		// moved from RosettaGenerator
		val model = resource.contents.filter(RosettaModel).head
		if((model?.name).nullOrEmpty){
			return
		}		
		
		//find all the reference types
		if (ctx.cancelIndicator.canceled) {
			return
		}
		for (attr : model.eAllOfType(Attribute).map[buildRAttribute].filter[RMetaAnnotatedType.hasMeta]) {
			val targetPackage = attr.RMetaAnnotatedType.RType.namespace
			val metaJt = attr.toForcedMetaItemJavaType

			if (ctx.cancelIndicator.canceled) {
				return
			}
			
			if (metaJt instanceof RJavaReferenceWithMeta) {
				fsa.generateFile('''«metaJt.canonicalName.withForwardSlashes».java''', referenceWithMeta(targetPackage, metaJt, attr.RMetaAnnotatedType.RType))
			} else if (metaJt instanceof RJavaFieldWithMeta) {
				fsa.generateFile('''«metaJt.canonicalName.withForwardSlashes».java''', fieldWithMeta(targetPackage, metaJt, attr.RMetaAnnotatedType.RType))
			} else {
				throw new UnsupportedOperationException("Invalid JavaType: " + metaJt)
			}
		}
	}

	private def CharSequence fieldWithMeta(DottedPath root, RJavaFieldWithMeta metaJavaType, RType valueType) {						
		val scope = new JavaStatementScope(metaJavaType.packageName)
		
		val StringConcatenationClient body = '''
			«metaJavaType.classBody(scope, new GeneratedJavaClass<Object>(metaJavaType.packageName, metaJavaType.simpleName + "Meta", Object), "1")»
			
			class «metaJavaType.simpleName»Meta extends «BasicRosettaMetaData»<«metaJavaType.simpleName»>{
			
			}
		'''
		
		buildClass(metaJavaType.packageName, body, scope)
	}
	
	private def referenceWithMeta(DottedPath root, RJavaReferenceWithMeta metaJavaType, RType valueType) {					
		val scope = new JavaStatementScope(root.child("metaField"))
		
		val StringConcatenationClient body = '''
			«metaJavaType.classBody(scope, new GeneratedJavaClass<Object>(root.child("metaField"), metaJavaType.simpleName + "Meta", Object), "1")»
			
			class «metaJavaType.simpleName»Meta extends «BasicRosettaMetaData»<«metaJavaType.simpleName»>{
			
			}
		'''
		
		buildClass(root.child("metaField"), body, scope)
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
