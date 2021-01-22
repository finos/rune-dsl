package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Multimaps
import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.java.util.JavaType
import com.regnosys.rosetta.generator.java.util.ParameterizedType
import com.regnosys.rosetta.rosetta.RosettaFactory
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.impl.RosettaFactoryImpl
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.SimpleFactory
import com.rosetta.model.lib.GlobalKey
import com.rosetta.model.lib.meta.BasicRosettaMetaData
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

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.*
import static extension com.regnosys.rosetta.generator.util.RosettaAttributeExtensions.*

class MetaFieldGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension ModelObjectGenerator
	
	 
	def void generate(JavaNames names, Resource resource, IFileSystemAccess2 fsa, IGeneratorContext ctx) {
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
				fsa.generateFile('''«names.packages.basicMetafields.directoryName»/MetaFields.java''',
				metaFields(names, "MetaFields", newArrayList(GlobalKeyFields), allMetaTypes.metaFieldTypes))
				
				fsa.generateFile('''«names.packages.basicMetafields.directoryName»/MetaAndTemplateFields.java''',
				metaFields(names, "MetaAndTemplateFields", newArrayList(GlobalKeyFields, TemplateFields), allMetaTypes.metaAndTemplateFieldTypes))
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
			val refs = nsc.value.flatMap[expandedAttributes].filter[hasMetas && metas.exists[name=="reference" || name=="address"]].map[rosettaType].toSet
			
			for (ref:refs) {
				val targetModel = ref.model
				val targetPackage = new RosettaJavaPackages(targetModel)
				val newNames=JavaNames.createBasicFromPackages(targetPackage)
				
				if (ctx.cancelIndicator.canceled) {
					return
				}
				if (ref.isBuiltInType)
					fsa.generateFile('''«targetPackage.basicMetafields.directoryName»/BasicReferenceWithMeta«ref.name.toFirstUpper».java''', basicReferenceWithMeta(newNames, ref))
				else
					fsa.generateFile('''«targetPackage.model.metaField.directoryName»/ReferenceWithMeta«ref.name.toFirstUpper».java''', referenceWithMeta(newNames, ref))
			}
			//find all the metaed types
			val metas =  nsc.value.flatMap[expandedAttributes].filter[hasMetas && !metas.exists[name=="reference" || name=="address"]].map[rosettaType].toSet
			for (meta:metas) {
				if (ctx.cancelIndicator.canceled) {
					return
				}
				val targetModel = meta.model
				val targetPackage = new RosettaJavaPackages(targetModel)
				val newNames=JavaNames.createBasicFromPackages(targetPackage)
				
				if(meta.isBuiltInType) {
					fsa.generateFile('''«targetPackage.basicMetafields.directoryName»/FieldWithMeta«meta.name.toFirstUpper».java''', fieldWithMeta(newNames, meta))
				} else {
					fsa.generateFile('''«targetPackage.model.metaField.directoryName»/FieldWithMeta«meta.name.toFirstUpper».java''', fieldWithMeta(newNames, meta))
				}
			}
		}
	}

	def getStringType() {
		val stringType = RosettaFactoryImpl.eINSTANCE.createRosettaBasicType
		stringType.name="string"
		return stringType
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
		globalKeyAttribute.type = stringType

		val externalKeyAttribute = SimpleFactory.eINSTANCE.createAttribute()
		externalKeyAttribute.setName("externalKey")
		externalKeyAttribute.card = cardSingle
		externalKeyAttribute.type = stringType;
		
		val keysType = SimpleFactory.eINSTANCE.createData()
		keysType.setName("Key")
		keysType.model = RosettaFactory.eINSTANCE.createRosettaModel
		keysType.model.name = "com.rosetta.model.lib.meta"
		val keysAttribute = SimpleFactory.eINSTANCE.createAttribute()
		keysAttribute.setName("key")
		keysAttribute.type = keysType
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
		newAttribute.type = type.type
		return newAttribute
	}

	def getMetaAndTemplateFieldTypes(Collection<RosettaMetaType> utypes) {
		val templateGlobalReferenceType = RosettaFactoryImpl.eINSTANCE.createRosettaMetaType()
		templateGlobalReferenceType.setName("templateGlobalReference")
		templateGlobalReferenceType.type = stringType;
		
		val plusTypes = new ArrayList(utypes)
		plusTypes.add(templateGlobalReferenceType)
		val metaFieldTypes = plusTypes.metaFieldTypes
		return metaFieldTypes
	}

	def metaFields(JavaNames names, String name, Collection<Object> interfaces, Collection<Attribute> attributes) {
		if (attributes.exists[t|t.name == "scheme"]) {
			interfaces.add(MetaDataFields)
		}

		
		
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = name
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = "com.rosetta.model.metafields"
		d.attributes.addAll(attributes)
		val classBody = tracImports(d.classBody(names, "1", interfaces))
		classBody.addImport(BasicRosettaMetaData.name, BasicRosettaMetaData.simpleName)
		
		//val classBody = tracImports(metaBody(name, interfaces, metaFieldTypes))
		'''
		package «names.packages.basicMetafields.name»;

		«FOR imp : classBody.imports.filter[i| !i.endsWith(name+"Meta")]»
			import «imp»;
		«ENDFOR»
		«FOR imp : classBody.staticImports»
			import static «imp»;
		«ENDFOR»
		
		«classBody.toString»
		
		class «name»Meta extends BasicRosettaMetaData<«name»>{
		
		}
		'''
	}

	def CharSequence fieldWithMeta(JavaNames names, RosettaType type) {
		
		val valueAttribute = SimpleFactory.eINSTANCE.createAttribute()
		valueAttribute.card = cardSingle
		valueAttribute.name = "value"
		valueAttribute.type = type
		
		val metaType = SimpleFactory.eINSTANCE.createData()
		metaType.setName("MetaFields")
		metaType.model = RosettaFactory.eINSTANCE.createRosettaModel
		metaType.model.name = names.packages.basicMetafields.name
		val metaAttribute = SimpleFactory.eINSTANCE.createAttribute()
		metaAttribute.setName("meta")
		metaAttribute.type = metaType
		metaAttribute.card = cardSingle
		
		val packageName= if (type.isBuiltInType) names.packages.basicMetafields.name else names.packages.model.metaField.name
		
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = "FieldWithMeta"+type.name.toFirstUpper
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = packageName
		d.attributes.addAll(#[
			valueAttribute, metaAttribute
		])
		val classBody = tracImports(d.classBody(names, "1", #[GlobalKey]))
		classBody.addImport(BasicRosettaMetaData.name, BasicRosettaMetaData.simpleName)
		
		//val classBody = tracImports(metaBody(name, interfaces, metaFieldTypes))
		'''
		package «packageName»;

		«FOR imp : classBody.imports.filter[i| !i.endsWith("FieldWithMeta"+type.name.toFirstUpper+"Meta")]»
			import «imp»;
		«ENDFOR»
		«FOR imp : classBody.staticImports»
			import static «imp»;
		«ENDFOR»
		
		«classBody.toString»
		
		class FieldWithMeta«type.name.toFirstUpper»Meta extends BasicRosettaMetaData<FieldWithMeta«type.name.toFirstUpper»>{
		
		}
		'''
	}
	
	def referenceAttributes(RosettaType type) {
		val valueAttribute = SimpleFactory.eINSTANCE.createAttribute()
		valueAttribute.card = cardSingle
		valueAttribute.name = "value"
		valueAttribute.type = type
		
		
		val globalRefAttribute = SimpleFactory.eINSTANCE.createAttribute()
		globalRefAttribute.setName("globalReference")
		globalRefAttribute.card = cardSingle
		globalRefAttribute.type = stringType

		val externalRefAttribute = SimpleFactory.eINSTANCE.createAttribute()
		externalRefAttribute.setName("externalReference")
		externalRefAttribute.card = cardSingle
		externalRefAttribute.type = stringType;
		
		val refType = SimpleFactory.eINSTANCE.createData()
		refType.setName("Reference")
		refType.model = RosettaFactory.eINSTANCE.createRosettaModel
		refType.model.name = "com.rosetta.model.lib.meta"
		val refAttribute = SimpleFactory.eINSTANCE.createAttribute()
		refAttribute.setName("reference")
		refAttribute.type = refType
		refAttribute.card = cardSingle
		 #[valueAttribute, globalRefAttribute, externalRefAttribute, refAttribute]
	}
	
	def referenceWithMeta(JavaNames names, RosettaType type) {
		
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = "ReferenceWithMeta"+type.name.toFirstUpper
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = names.packages.model.metaField.name
		d.attributes.addAll(referenceAttributes(type))
		val refInterface = new ParameterizedType(new JavaType(ReferenceWithMeta.name), #[new ParameterizedType(names.toJavaType(type), #[])])
		val classBody = tracImports(d.classBody(names, "1", #[refInterface]))
		classBody.addImport(BasicRosettaMetaData.name, BasicRosettaMetaData.simpleName)
		
		//val classBody = tracImports(metaBody(name, interfaces, metaFieldTypes))
		'''
		package «names.packages.model.metaField.name»;

		«FOR imp : classBody.imports.filter[i| !i.endsWith("ReferenceWithMeta"+type.name.toFirstUpper+"Meta")]»
			import «imp»;
		«ENDFOR»
		«FOR imp : classBody.staticImports»
			import static «imp»;
		«ENDFOR»
		
		«classBody.toString»
		
		class ReferenceWithMeta«type.name.toFirstUpper»Meta extends BasicRosettaMetaData<ReferenceWithMeta«type.name.toFirstUpper»>{
		
		}
		'''
	}
	
	def basicReferenceWithMeta(JavaNames names, RosettaType type) {
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = "BasicReferenceWithMeta"+type.name.toFirstUpper
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = names.packages.basicMetafields.name
		d.attributes.addAll(referenceAttributes(type))
		val refInterface = new ParameterizedType(new JavaType(ReferenceWithMeta.name), #[new ParameterizedType(new JavaType(type.name.toJavaFullType), #[])])
		val classBody = tracImports(d.classBody(names, "1", #[refInterface]))
		classBody.addImport(BasicRosettaMetaData.name, BasicRosettaMetaData.simpleName)
		
		//val classBody = tracImports(metaBody(name, interfaces, metaFieldTypes))
		'''
		package «names.packages.basicMetafields.name»;

		«FOR imp : classBody.imports.filter[i| !i.endsWith("BasicReferenceWithMeta"+type.name.toFirstUpper+"Meta")]»
			import «imp»;
		«ENDFOR»
		«FOR imp : classBody.staticImports»
			import static «imp»;
		«ENDFOR»
		
		«classBody.toString»
		
		class BasicReferenceWithMeta«type.name.toFirstUpper»Meta extends BasicRosettaMetaData<BasicReferenceWithMeta«type.name.toFirstUpper»>{
		
		}
		'''
	}
	
	private def namespace(RosettaRootElement rc) {
		return rc.model.name
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
