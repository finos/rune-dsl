package com.regnosys.rosetta.generator.java.object

import com.google.common.collect.Multimaps
import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension
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
import com.regnosys.rosetta.generator.java.types.JavaClass
import com.rosetta.model.lib.meta.BasicRosettaMetaData
import com.regnosys.rosetta.generator.java.types.JavaParametrizedType
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator
import com.regnosys.rosetta.rosetta.RosettaBuiltinType
import com.regnosys.rosetta.rosetta.TypeCall
import com.regnosys.rosetta.types.TypeSystem
import com.regnosys.rosetta.types.builtin.RBasicType
import com.regnosys.rosetta.types.builtin.RRecordType

class MetaFieldGenerator {
	@Inject extension ImportManagerExtension
	@Inject extension ModelObjectGenerator
	@Inject RosettaJavaPackages packages
	@Inject extension JavaTypeTranslator
	@Inject extension TypeSystem
	
	 
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
				metaFields("MetaFields", newArrayList(GlobalKeyFields), allMetaTypes.metaFieldTypes))
				
				fsa.generateFile('''«packages.basicMetafields.withForwardSlashes»/MetaAndTemplateFields.java''',
				metaFields("MetaAndTemplateFields", newArrayList(GlobalKeyFields, TemplateFields), allMetaTypes.metaAndTemplateFieldTypes))
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
				val targetModel = ref.type.model
				val targetPackage = new RootPackage(targetModel)
				
				if (ctx.cancelIndicator.canceled) {
					return
				}
				if (ref.type instanceof RosettaBuiltinType)
					fsa.generateFile('''«packages.basicMetafields.withForwardSlashes»/BasicReferenceWithMeta«ref.type.name.toFirstUpper».java''', basicReferenceWithMeta(ref))
				else
					fsa.generateFile('''«targetPackage.metaField.withForwardSlashes»/ReferenceWithMeta«ref.type.name.toFirstUpper».java''', referenceWithMeta(targetPackage, ref))
			}
			//find all the metaed types
			val metas =  nsc.value.flatMap[expandedAttributes].filter[hasMetas && !metas.exists[name=="reference" || name=="address"]].map[rosettaType].toSet
			for (meta:metas) {
				if (ctx.cancelIndicator.canceled) {
					return
				}
				val targetModel = meta.type.model
				val targetPackage = new RootPackage(targetModel)
				
				val metaType = meta.typeCallToRType
				// TODO: make consistent
				if(metaType instanceof RBasicType || metaType instanceof RRecordType) {
					fsa.generateFile('''«packages.basicMetafields.withForwardSlashes»/FieldWithMeta«metaType.toJavaReferenceType.simpleName».java''', fieldWithMeta(targetPackage, meta))
				} else {
					fsa.generateFile('''«targetPackage.metaField.withForwardSlashes»/FieldWithMeta«metaType.toJavaReferenceType.simpleName».java''', fieldWithMeta(targetPackage, meta))
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
		stringType.name = "string"
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
		externalKeyAttribute.typeCall = stringType;
		
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
		templateGlobalReferenceType.setName("templateGlobalReference")
		templateGlobalReferenceType.typeCall = stringType;
		
		val plusTypes = new ArrayList(utypes)
		plusTypes.add(templateGlobalReferenceType)
		val metaFieldTypes = plusTypes.metaFieldTypes
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
		«d.classBody(scope, new JavaClass(packages.basicMetafields, d.name+'Meta'), "1", interfaces)»
		
		class «name»Meta extends «BasicRosettaMetaData»<«name»>{
		
		}
		'''
		buildClass(packages.basicMetafields, body, scope)
	}

	def CharSequence fieldWithMeta(RootPackage root, TypeCall typeCall) {
		val rType = typeCall.typeCallToRType
		val javaType = rType.toJavaReferenceType
		
		val valueAttribute = SimpleFactory.eINSTANCE.createAttribute()
		valueAttribute.card = cardSingle
		valueAttribute.name = "value"
		valueAttribute.typeCall = typeCall
		
		val metaType = SimpleFactory.eINSTANCE.createData()
		metaType.setName("MetaFields")
		metaType.model = RosettaFactory.eINSTANCE.createRosettaModel
		metaType.model.name = packages.basicMetafields.withDots
		val metaAttribute = SimpleFactory.eINSTANCE.createAttribute()
		metaAttribute.setName("meta")
		metaAttribute.typeCall = metaType.toTypeCall
		metaAttribute.card = cardSingle
		
		val packageName= if (rType instanceof RBasicType || rType instanceof RRecordType) packages.basicMetafields else root.metaField
		
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = "FieldWithMeta" + javaType.simpleName
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = packageName.withDots
		d.attributes.addAll(#[
			valueAttribute, metaAttribute
		])
		
		val FWMType = new JavaParametrizedType(JavaClass.from(FieldWithMeta), typeCall.typeCallToRType.toJavaReferenceType)
		
		val scope = new JavaScope(packageName)
		
		val StringConcatenationClient body = '''
			«d.classBody(scope, new JavaClass(packageName, d.name + "Meta"), "1", #[GlobalKey, FWMType])»
			
			class FieldWithMeta«javaType.simpleName»Meta extends «BasicRosettaMetaData»<FieldWithMeta«javaType.simpleName»>{
			
			}
		'''
		
		buildClass(packageName, body, scope)
	}
	
	def referenceAttributes(TypeCall typeCall) {
		val valueAttribute = SimpleFactory.eINSTANCE.createAttribute()
		valueAttribute.card = cardSingle
		valueAttribute.name = "value"
		valueAttribute.typeCall = typeCall
		
		
		val globalRefAttribute = SimpleFactory.eINSTANCE.createAttribute()
		globalRefAttribute.setName("globalReference")
		globalRefAttribute.card = cardSingle
		globalRefAttribute.typeCall = stringType

		val externalRefAttribute = SimpleFactory.eINSTANCE.createAttribute()
		externalRefAttribute.setName("externalReference")
		externalRefAttribute.card = cardSingle
		externalRefAttribute.typeCall = stringType;
		
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
	
	def referenceWithMeta(RootPackage root, TypeCall typeCall) {
		
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = "ReferenceWithMeta" + typeCall.type.name.toFirstUpper
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = root.metaField.withDots
		d.attributes.addAll(referenceAttributes(typeCall))
		val refInterface = new JavaParametrizedType(JavaClass.from(ReferenceWithMeta), typeCall.typeCallToRType.toJavaReferenceType)
		
		val scope = new JavaScope(root.metaField)
		
		val StringConcatenationClient body = '''
			«d.classBody(scope, new JavaClass(root.metaField, d.name + "Meta"), "1", #[refInterface])»
			
			class ReferenceWithMeta«typeCall.type.name.toFirstUpper»Meta extends «BasicRosettaMetaData»<ReferenceWithMeta«typeCall.type.name.toFirstUpper»>{
			
			}
		'''
		
		buildClass(root.metaField, body, scope)
	}
	
	def basicReferenceWithMeta(TypeCall typeCall) {
		val Data d = SimpleFactory.eINSTANCE.createData;
		d.name = "BasicReferenceWithMeta" + typeCall.type.name.toFirstUpper
		d.model = RosettaFactory.eINSTANCE.createRosettaModel
		d.model.name = packages.basicMetafields.withDots
		d.attributes.addAll(referenceAttributes(typeCall))
		val refInterface = new JavaParametrizedType(JavaClass.from(ReferenceWithMeta), typeCall.typeCallToRType.toJavaReferenceType)
		
		val scope = new JavaScope(packages.basicMetafields)
		
		val StringConcatenationClient body = '''		
			«d.classBody(scope, new JavaClass(packages.basicMetafields, d.name + "Meta"), "1", #[refInterface])»
			
			class BasicReferenceWithMeta«typeCall.type.name.toFirstUpper»Meta extends «BasicRosettaMetaData»<BasicReferenceWithMeta«typeCall.type.name.toFirstUpper»>{
			
			}
		'''
		
		buildClass(packages.basicMetafields, body, scope)
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
