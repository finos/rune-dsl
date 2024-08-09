package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Multimaps
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.impl.RosettaFactoryImpl
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.SimpleFactory
import com.rosetta.model.lib.GlobalKey
import com.rosetta.model.lib.meta.GlobalKeyFields
import com.rosetta.model.lib.meta.MetaDataFields
import com.rosetta.model.lib.meta.ReferenceWithMeta
import com.rosetta.model.lib.meta.TemplateFields
import java.util.ArrayList
import java.util.Collection
import java.util.List
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*
import com.rosetta.model.lib.meta.FieldWithMeta
import org.eclipse.xtend2.lib.StringConcatenationClient
import com.regnosys.rosetta.generator.java.JavaScope
import com.rosetta.model.lib.meta.BasicRosettaMetaData
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.rosetta.TypeCall
import com.regnosys.rosetta.types.TypeSystem
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.scoping.RosettaScopeProvider
import com.rosetta.util.types.JavaClass
import com.rosetta.util.types.JavaParameterizedType
import javax.inject.Inject
import com.rosetta.util.types.generated.GeneratedJavaClass
import com.fasterxml.jackson.core.type.TypeReference
import com.regnosys.rosetta.config.RosettaGeneratorsConfiguration
import com.regnosys.rosetta.utils.ModelIdProvider

class MetaFieldGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension ModelObjectGenerator
	@Inject RosettaJavaPackages packages
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem
	@Inject RosettaGeneratorsConfiguration config
	@Inject extension ModelIdProvider
	
	 
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
		val namespaceClasses = Multimaps.index(modelClasses, [c|c.namespace]).asMap
		for (nsc: namespaceClasses.entrySet) {
			if (ctx.cancelIndicator.canceled) {
				return
			}
			val refs = nsc.value.filter(Data).flatMap[dataToType.expandedAttributes].filter[hasMetas && metas.exists[name=="reference" || name=="address"]].toSet
			
			for (ref:refs) {
				val targetModel = EcoreUtil2.getContainerOfType(ref.type.namespace, RosettaModel)
				if (targetModel.shouldGenerate) {
					val targetPackage = new RootPackage(targetModel.toDottedPath)
					val metaJt = ref.toMetaJavaType
					
					if (ctx.cancelIndicator.canceled) {
						return
					}
					fsa.generateFile('''«metaJt.canonicalName.withForwardSlashes».java''', referenceWithMeta(targetPackage, metaJt, ref.rosettaType))
				}
			}
			//find all the metaed types
			val metas =  nsc.value.filter(Data).flatMap[dataToType.expandedAttributes].filter[hasMetas && !metas.exists[name=="reference" || name=="address"]].toSet
			for (meta:metas) {
				val targetModel = EcoreUtil2.getContainerOfType(meta.type.namespace, RosettaModel)
				if (targetModel.shouldGenerate) {
					val targetPackage = new RootPackage(targetModel.toDottedPath)
					val metaJt = meta.toMetaJavaType
					
					if (ctx.cancelIndicator.canceled) {
						return
					}
					fsa.generateFile('''«metaJt.canonicalName.withForwardSlashes».java''', fieldWithMeta(targetPackage, metaJt, meta.rosettaType))
				}
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
		keysType.namespace = RosettaFactory.eINSTANCE.createRosettaModel
		keysType.namespace.name = "com.rosetta.model.lib.meta"
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
		templateGlobalReferenceType.namespace = libModel
		
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
		d.namespace = RosettaFactory.eINSTANCE.createRosettaModel
		d.namespace.name = packages.basicMetafields.withDots
		d.attributes.addAll(attributes)
		
		val scope = new JavaScope(packages.basicMetafields)
		
		val StringConcatenationClient body = '''		
		«d.dataToType.classBody(scope, new GeneratedJavaClass<Object>(packages.basicMetafields, d.name+'Meta', Object), "1", interfaces)»
		
		class «name»Meta extends «BasicRosettaMetaData»<«name»>{
		
		}
		'''
		buildClass(packages.basicMetafields, body, scope)
	}

	def CharSequence fieldWithMeta(RootPackage root, JavaClass<?> metaJavaType, TypeCall typeCall) {		
		val valueAttribute = SimpleFactory.eINSTANCE.createAttribute()
		valueAttribute.card = cardSingle
		valueAttribute.name = "value"
		valueAttribute.typeCall = EcoreUtil2.copy(typeCall)
		
		val metaType = SimpleFactory.eINSTANCE.createData()
		metaType.setName("MetaFields")
		metaType.namespace = RosettaFactory.eINSTANCE.createRosettaModel
		metaType.namespace.name = packages.basicMetafields.withDots
		val metaAttribute = SimpleFactory.eINSTANCE.createAttribute()
		metaAttribute.setName("meta")
		metaAttribute.typeCall = metaType.toTypeCall
		metaAttribute.card = cardSingle
				
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = metaJavaType.simpleName
		d.namespace = RosettaFactory.eINSTANCE.createRosettaModel
		d.namespace.name = metaJavaType.packageName.withDots
		d.attributes.addAll(#[
			valueAttribute, metaAttribute
		])
		
		val FWMType = JavaParameterizedType.from(new TypeReference<FieldWithMeta<?>>() {}, typeCall.typeCallToRType.toJavaReferenceType)
		
		val scope = new JavaScope(metaJavaType.packageName)
		
		val StringConcatenationClient body = '''
			«d.dataToType.classBody(scope, new GeneratedJavaClass<Object>(metaJavaType.packageName, d.name + "Meta", Object), "1", #[GlobalKey, FWMType])»
			
			class «metaJavaType.simpleName»Meta extends «BasicRosettaMetaData»<«metaJavaType.simpleName»>{
			
			}
		'''
		
		buildClass(metaJavaType.packageName, body, scope)
	}
	
	def referenceAttributes(TypeCall typeCall) {
		val valueAttribute = SimpleFactory.eINSTANCE.createAttribute()
		valueAttribute.card = cardSingle
		valueAttribute.name = "value"
		valueAttribute.typeCall = EcoreUtil2.copy(typeCall)
		
		
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
		refType.namespace = RosettaFactory.eINSTANCE.createRosettaModel
		refType.namespace.name = "com.rosetta.model.lib.meta"
		val refAttribute = SimpleFactory.eINSTANCE.createAttribute()
		refAttribute.setName("reference")
		refAttribute.typeCall = refType.toTypeCall
		refAttribute.card = cardSingle
		 #[valueAttribute, globalRefAttribute, externalRefAttribute, refAttribute]
	}
	
	def referenceWithMeta(RootPackage root, JavaClass<?> metaJavaType, TypeCall typeCall) {		
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = metaJavaType.simpleName
		d.namespace = RosettaFactory.eINSTANCE.createRosettaModel
		d.namespace.name = metaJavaType.packageName.withDots
		d.attributes.addAll(referenceAttributes(typeCall))
		val refInterface = JavaParameterizedType.from(new TypeReference<ReferenceWithMeta<?>>() {}, typeCall.typeCallToRType.toJavaReferenceType)
		
		val scope = new JavaScope(root.metaField)
		
		val StringConcatenationClient body = '''
			«d.dataToType.classBody(scope, new GeneratedJavaClass<Object>(root.metaField, d.name + "Meta", Object), "1", #[refInterface])»
			
			class «metaJavaType.simpleName»Meta extends «BasicRosettaMetaData»<«metaJavaType.simpleName»>{
			
			}
		'''
		
		buildClass(root.metaField, body, scope)
	}

	private def boolean shouldGenerate(RosettaModel model) {
		config.namespaceFilter.test(model.name) || model.overridden
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
