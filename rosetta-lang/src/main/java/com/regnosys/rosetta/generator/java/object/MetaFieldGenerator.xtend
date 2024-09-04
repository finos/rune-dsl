package com.regnosys.rosetta.generator.java.object

import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.collect.Multimaps
import com.regnosys.rosetta.generator.java.JavaScope
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.impl.RosettaFactoryImpl
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.SimpleFactory
import com.regnosys.rosetta.scoping.RosettaScopeProvider
import com.rosetta.model.lib.GlobalKey
import com.rosetta.model.lib.meta.BasicRosettaMetaData
import com.rosetta.model.lib.meta.FieldWithMeta
import com.rosetta.model.lib.meta.GlobalKeyFields
import com.rosetta.model.lib.meta.MetaDataFields
import com.rosetta.model.lib.meta.ReferenceWithMeta
import com.rosetta.model.lib.meta.TemplateFields
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaParameterizedType
import com.rosetta.util.types.generated.GeneratedJavaClass
import java.util.ArrayList
import java.util.Collection
import java.util.List
import javax.inject.Inject
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

import com.regnosys.rosetta.types.RObjectFactory

class MetaFieldGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension ModelObjectGenerator
	@Inject RosettaJavaPackages packages
	@Inject extension JavaTypeTranslator
	@Inject extension RObjectFactory

	 
	def void generate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ctx) {
		// moved from RosettaGenerator
		val model = resource.contents.filter(RosettaModel).head
		if((model?.name).nullOrEmpty){
			return
		}
		
		
// TODO - This code is intended to only generate MetaFields.java once per name space. This however causes an issue when running with the incremental builder that deletes the file as a clean up and never re-generates it.
//		if (resource.resourceSet.adapterFactories.filter(MarkerAdapterFactory).findFirst[namespace == model.name] === null) {
//			try {
				val allModels = resource.resourceSet.resources.flatMap[contents].filter(RosettaModel).toList
				val allMetaTypes = allModels.flatMap[elements].filter(RosettaMetaType).toList
				fsa.generateFile('''«packages.basicMetafields.withForwardSlashes»/MetaFields.java''',
				metaFields("MetaFields", newArrayList(GlobalKeyFields), allMetaTypes.getMetaFieldTypes))
				
				fsa.generateFile('''«packages.basicMetafields.withForwardSlashes»/MetaAndTemplateFields.java''',
				metaFields("MetaAndTemplateFields", newArrayList(GlobalKeyFields, TemplateFields), allMetaTypes.getMetaAndTemplateFieldTypes))
//			} finally {
//				resource.resourceSet.adapterFactories.add(new MarkerAdapterFactory(model.name))
//			}
//		}
		
		val modelClasses = model.elements.filter [
			it instanceof Data
		]
		if (modelClasses.empty) {
			return
		}
		
		//find all the reference types
		val namespaceClasses = Multimaps.index(modelClasses, [c|c.model]).asMap
		for (nsc : namespaceClasses.entrySet) {
			if (ctx.cancelIndicator.canceled) {
				return
			}
			val refs = nsc.value.filter(Data).flatMap[buildRDataType.ownAttributes].filter [
				metaAnnotations.exists[name == "reference" || name == "address"]
			].toSet
			
			for (ref : refs) {
				val targetModel = ref.RType.namespace
				val targetPackage = new RootPackage(targetModel)
				val metaJt = ref.toMetaJavaType

				if (ctx.cancelIndicator.canceled) {
					return
				}
				fsa.generateFile('''«metaJt.canonicalName.withForwardSlashes».java''',
					referenceWithMeta(targetPackage, metaJt))
			}



			//find all the metaed types
			val metas =  nsc.value.filter(Data).flatMap[buildRDataType.ownAttributes].filter[
				!metaAnnotations.exists[name=="reference" || name=="address"]
			].toSet

			for (meta:metas) {
				val targetModel = meta.RType.namespace
				val targetPackage = new RootPackage(targetModel)
				val metaJt = meta.toMetaJavaType
				
				if (ctx.cancelIndicator.canceled) {
					return
				}
				fsa.generateFile('''«metaJt.canonicalName.withForwardSlashes».java''', fieldWithMeta(targetPackage, metaJt))
			}
		}
	}
	
	def toTypeCall(RosettaType t) {
		val typeCall = RosettaFactoryImpl.eINSTANCE.createTypeCall
		typeCall.type = t
		return typeCall
	}

	def getStringType() {
		val stringType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType
		stringType.name="string"
		stringType.model = RosettaFactory.eINSTANCE.createRosettaModel
		stringType.model.name = "com.rosetta.model.lib"
		return stringType.toTypeCall
	}
	
	def getCardSingle() {
		val cardSingle = RosettaFactory.eINSTANCE.createRosettaCardinality
		cardSingle.inf = 0
		cardSingle.sup = 1
		cardSingle
	}

	def List<Attribute> getMetaFieldTypes(Collection<RosettaMetaType> utypes) {
		val cardMult = RosettaFactory.eINSTANCE.createRosettaCardinality
		cardMult.inf = 0;
		cardMult.sup = 1000;
		cardMult.unbounded = true
		
		val globalKeyAttribute = SimpleFactory.eINSTANCE.createAttribute()
		globalKeyAttribute.setName("globalKey")
		globalKeyAttribute.card = cardSingle
		globalKeyAttribute.typeCall = stringType

		val externalKeyAttribute = SimpleFactory.eINSTANCE.createAttribute()
		externalKeyAttribute.setName("externalKey")
		externalKeyAttribute.card = cardSingle
		externalKeyAttribute.typeCall = stringType
		
		val keysType = SimpleFactory.eINSTANCE.createData()
		keysType.setName("Key")
		keysType.model = RosettaFactory.eINSTANCE.createRosettaModel
		keysType.model.name = "com.rosetta.model.lib.meta"
		val keysAttribute = SimpleFactory.eINSTANCE.createAttribute()
		keysAttribute.setName("key")
		keysAttribute.typeCall = keysType.toTypeCall
		keysAttribute.card = cardMult

		val filteredTypes = utypes.filter[t|t.name != "key" && t.name != "id" && t.name != "reference"].toSet;
		val result = filteredTypes.map[toAttribute].toList
		result.addAll(#[globalKeyAttribute, externalKeyAttribute, keysAttribute])
		return result
	}
	
	def toAttribute(RosettaMetaType type) {
		val newAttribute = SimpleFactory.eINSTANCE.createAttribute()
		newAttribute.card = cardSingle
		newAttribute.name = type.name
		newAttribute.typeCall = type.toTypeCall
		return newAttribute
	}

	def getMetaAndTemplateFieldTypes(Collection<RosettaMetaType> utypes) {
		val templateGlobalReferenceType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		templateGlobalReferenceType.name = "templateGlobalReference"
		templateGlobalReferenceType.typeCall = stringType
		
		val libModel = RosettaFactory.eINSTANCE.createRosettaModel
		libModel.name = RosettaScopeProvider.LIB_NAMESPACE
		templateGlobalReferenceType.model = libModel
		
		val plusTypes = new ArrayList(utypes)
		plusTypes.add(templateGlobalReferenceType)
		val metaFieldTypes = plusTypes.getMetaFieldTypes
		return metaFieldTypes
	}

	def metaFields(String name, Collection<Object> interfaces, Collection<Attribute> attributes) {
		if (attributes.exists[t|t.name == "scheme"]) {
			interfaces.add(MetaDataFields)
		}
		
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = name
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = packages.basicMetafields.withDots
		d.attributes.addAll(attributes)
		
		val scope = new JavaScope(packages.basicMetafields)
		
		val StringConcatenationClient body = '''		
		«d.buildRDataType.classBody(scope, new GeneratedJavaClass<Object>(packages.basicMetafields, d.name+'Meta', Object), "1", interfaces)»
		
		class «name»Meta extends «BasicRosettaMetaData»<«name»>{
		
		}
		'''
		buildClass(packages.basicMetafields, body, scope)
	}

	def CharSequence fieldWithMeta(RootPackage root, JavaClass<?> metaJavaType) {		
		val valueAttribute = SimpleFactory.eINSTANCE.createAttribute()
		valueAttribute.card = cardSingle
		valueAttribute.name = "value"
		// TODO
		// valueAttribute.typeCall = EcoreUtil2.copy(typeCall)
		
		val metaType = SimpleFactory.eINSTANCE.createData()
		metaType.setName("MetaFields")
		metaType.model = RosettaFactory.eINSTANCE.createRosettaModel
		metaType.model.name = packages.basicMetafields.withDots
		val metaAttribute = SimpleFactory.eINSTANCE.createAttribute()
		metaAttribute.setName("meta")
		metaAttribute.typeCall = metaType.toTypeCall
		metaAttribute.card = cardSingle
				
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = metaJavaType.simpleName
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = metaJavaType.packageName.withDots
		d.attributes.addAll(#[
			valueAttribute, metaAttribute
		])
		
		val FWMType = JavaParameterizedType.from(new TypeReference<FieldWithMeta<?>>() {}, metaJavaType)
		
		val scope = new JavaScope(metaJavaType.packageName)
		
		val StringConcatenationClient body = '''
			«d.buildRDataType.classBody(scope, new GeneratedJavaClass<Object>(metaJavaType.packageName, d.name + "Meta", Object), "1", #[GlobalKey, FWMType])»
			
			class «metaJavaType.simpleName»Meta extends «BasicRosettaMetaData»<«metaJavaType.simpleName»>{
			
			}
		'''
		
		buildClass(metaJavaType.packageName, body, scope)
	}
	
	def referenceAttributes() {
		// TODO
		val valueAttribute = SimpleFactory.eINSTANCE.createAttribute()
		valueAttribute.card = cardSingle
		valueAttribute.name = "value"
//		valueAttribute.typeCall = EcoreUtil2.copy(typeCall)
		
		
		val globalRefAttribute = SimpleFactory.eINSTANCE.createAttribute()
		globalRefAttribute.setName("globalReference")
		globalRefAttribute.card = cardSingle
		globalRefAttribute.typeCall = stringType

		val externalRefAttribute = SimpleFactory.eINSTANCE.createAttribute()
		externalRefAttribute.setName("externalReference")
		externalRefAttribute.card = cardSingle
		externalRefAttribute.typeCall = stringType
		
		val refType = SimpleFactory.eINSTANCE.createData()
		refType.setName("Reference")
		refType.model = RosettaFactory.eINSTANCE.createRosettaModel
		refType.model.name = "com.rosetta.model.lib.meta"
		val refAttribute = SimpleFactory.eINSTANCE.createAttribute()
		refAttribute.setName("reference")
		refAttribute.typeCall = refType.toTypeCall
		refAttribute.card = cardSingle
		 #[valueAttribute, globalRefAttribute, externalRefAttribute, refAttribute]
	}
	
	def referenceWithMeta(RootPackage root, JavaClass<?> metaJavaType) {		
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = metaJavaType.simpleName
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = metaJavaType.packageName.withDots
		d.attributes.addAll(referenceAttributes())
		val refInterface = JavaParameterizedType.from(new TypeReference<ReferenceWithMeta<?>>() {}, metaJavaType)
		
		val scope = new JavaScope(root.metaField)
		
		val StringConcatenationClient body = '''
			«d.buildRDataType.classBody(scope, new GeneratedJavaClass<Object>(root.metaField, d.name + "Meta", Object), "1", #[refInterface])»
			
			class «metaJavaType.simpleName»Meta extends «BasicRosettaMetaData»<«metaJavaType.simpleName»>{
			
			}
		'''
		
		buildClass(root.metaField, body, scope)
	}

	/** generate once per resource marker */
	static class MarkerAdapterFactory extends AdapterFactoryImpl {

		final String namespace

		new(String namespace) {
			this.namespace = namespace
		}

		def getNamespace() {
			namespace
		}
	}
}
