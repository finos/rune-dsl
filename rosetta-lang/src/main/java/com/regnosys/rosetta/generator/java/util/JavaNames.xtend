package com.regnosys.rosetta.generator.java.util

import com.google.inject.Inject
import com.google.inject.Injector
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.object.ExpandedType
import com.regnosys.rosetta.generator.util.RosettaAttributeExtensions
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaCalculationType
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaEnumeration
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaQualifiedType
import com.regnosys.rosetta.rosetta.RosettaRecordType
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.RosettaType
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Data
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RRecordType
import com.regnosys.rosetta.types.RType
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.generator.java.types.JavaParameterizedType
import com.regnosys.rosetta.generator.java.types.JavaType
import com.regnosys.rosetta.utils.DottedPath
import com.regnosys.rosetta.generator.java.types.JavaClass
import com.regnosys.rosetta.generator.java.types.JavaReferenceType
import com.regnosys.rosetta.generator.java.types.JavaPrimitiveType

class JavaNames {

	@Accessors(PUBLIC_GETTER)
	RosettaJavaPackages packages

	def JavaType toListOrSingleJavaType(Attribute attribute) {
		if (attribute.card.isIsMany) {
			return new JavaParameterizedType(List.toJavaType, attribute.type.toJavaType.toReferenceType)
		} else
			return attribute.type.toJavaType
	}
	
	def dispatch JavaReferenceType toReferenceType(JavaPrimitiveType type) {
		type.toReferenceType
	}
	def dispatch JavaReferenceType toReferenceType(JavaReferenceType type) {
		type
	}

	def JavaType toJavaType(ExpandedType type) {
		if (type.name == RosettaAttributeExtensions.METAFIELDS_CLASS_NAME || type.name == RosettaAttributeExtensions.META_AND_TEMPLATE_FIELDS_CLASS_NAME) {
			return new JavaClass(packages.basicMetafields, type.name)
		}
		if (type.metaType) {//TODO ExpandedType needs to store the underlying type for meta types if we want them to be anything other than strings
			return createForBasicType("string")
		}
		if (type.builtInType) {
			return createForBasicType(type.name)
		}
		new JavaClass(new RootPackage(type.model), type.name)
	}

	def JavaType toJavaType(RosettaCallableWithArgs func) {
		switch (func) {
			Function:
				new JavaClass(modelRootPackage(func).functions, func.name)
			RosettaExternalFunction:
				new JavaClass(packages.defaultLibFunctions, func.name)
			default:
				throw new UnsupportedOperationException("Not implemented for type " + func?.class?.name)
		}
	}
	
	def JavaClass toJavaType(Class<?> c) {
		JavaClass.from(c)
	}

	def JavaType toJavaType(RosettaType type) {
		switch (type) {
			RosettaBasicType:
				createForBasicType(type.name)
			Data,
			RosettaEnumeration:
				new JavaClass(modelRootPackage(type), type.name)
			RosettaRecordType: {
				val canonicalName = DottedPath.splitOnDots(JavaClassTranslator.toJavaFullType(type.name))
				new JavaClass(canonicalName.parent, canonicalName.last)
			}
			RosettaCalculationType,
			RosettaQualifiedType:
				JavaClass.from(String)
			default:
				throw new UnsupportedOperationException("Not implemented for type " + type?.class?.name)
		}
	}

	def JavaType toJavaType(RType rType) {
		switch (rType) {
			RBuiltinType:
				rType.name.createForBasicType
			REnumType:
				rType.enumeration.toJavaType
			RDataType:
				rType.data.toJavaType
			RRecordType:
				(rType.record as RosettaType).toJavaType
			default:
				throw new UnsupportedOperationException("Not implemented for type " + rType?.name)
		}
	}
	
//	def createMetaType(String parent, String meta) {
//		MetaType.create(parent, meta)
//	}
//
	def toMetaType(Attribute ctx, String name) {
		var model = ctx.type.eContainer
		if (model instanceof RosettaModel) {
			var pkg = new RootPackage(model.name).metaField
			return new JavaClass(pkg, name)
		}
		
		if(model instanceof RosettaBasicType) {
			// built-in meta types are defined in metafield package
			return new JavaClass(packages.basicMetafields, name)
		}
//		var pkg = modelRootPackage(ctx).metaField 
//		createJavaType(pkg, name)
	}

//	def toMetaType(ExpandedAttribute type, String name) {
//		if(type.type.isBuiltInType) {
//			// built-in meta types are defined in metafield package
//			return new JavaClass(packages.basicMetafields, name)
//		}
//		var parentPKG = new RootPackage(type.type.model)
//		var metaParent = parentPKG.child(type.type.name)
//		
//		var metaPKG = parentPKG.metaField
//		var meta = metaPKG.child(name)
//		createMetaType(metaParent, meta)
//	}

	def private RootPackage modelRootPackage(RosettaNamed namedType) {
		val rootElement = EcoreUtil2.getContainerOfType(namedType, RosettaRootElement)
		val model = rootElement.model
		if (model === null)
			// Faked attributes
			throw new IllegalArgumentException('''Can not compute package name for «namedType.eClass.name» «namedType.name». Element is not attached to a RosettaModel.''')
		return new RootPackage(model)
	}

	private def JavaType createForBasicType(String typeName) {
		val canonicalName = DottedPath.splitOnDots(JavaClassTranslator.toJavaFullType(typeName))
		return new JavaClass(canonicalName.parent, canonicalName.last)
	}
	
	static def JavaNames createBasicFromPackages(RosettaJavaPackages packages) {
		val n = new JavaNames
		n.packages = packages
		n
	}

	static class Factory {
		@Inject Injector injector

		def create(RosettaModel model) {
			create(new RosettaJavaPackages(model))
		}

		def create(RosettaJavaPackages packages) {
			val result = new JavaNames
			injector.injectMembers(result)
			result.packages = packages
			return result
		}
	}
}
