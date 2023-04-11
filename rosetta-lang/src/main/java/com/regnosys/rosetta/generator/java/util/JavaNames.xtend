package com.regnosys.rosetta.generator.java.util

import com.google.inject.Inject
import com.google.inject.Injector
import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.object.ExpandedType
import com.regnosys.rosetta.generator.util.RosettaAttributeExtensions
import com.regnosys.rosetta.rosetta.RosettaBasicType
import com.regnosys.rosetta.rosetta.RosettaCallableWithArgs
import com.regnosys.rosetta.rosetta.RosettaExternalFunction
import com.regnosys.rosetta.rosetta.RosettaModel
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RDataType
import com.regnosys.rosetta.types.REnumType
import com.regnosys.rosetta.types.RRecordType
import com.regnosys.rosetta.types.RType
import java.util.List
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.generator.java.types.JavaType
import com.regnosys.rosetta.utils.DottedPath
import com.regnosys.rosetta.generator.java.types.JavaClass
import com.regnosys.rosetta.generator.java.types.JavaReferenceType
import com.regnosys.rosetta.generator.java.types.JavaPrimitiveType
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.generator.java.types.JavaWildcardTypeArgument
import com.regnosys.rosetta.generator.java.types.JavaParametrizedType

class JavaNames {

	@Accessors(PUBLIC_GETTER)
	RosettaJavaPackages packages
	
	def JavaClass toImplType(JavaClass type) {
		new JavaClass(type.packageName, type.simpleName + "." + type.simpleName + "Impl")
	}
	def JavaClass toBuilderType(JavaClass type) {
		new JavaClass(type.packageName, type.simpleName + "." + type.simpleName + "Builder")
	}
	def JavaClass toBuilderImplType(JavaClass type) {
		new JavaClass(type.packageName, type.simpleName + "." + type.simpleName + "BuilderImpl")
	}

	def JavaType toListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return new JavaParametrizedType(List.toJavaType, type.toJavaType);
		} else
			return type.toJavaType
	}
	
	def JavaType toPolymorphicListOrSingleJavaType(RType type, boolean isMany) {
		if (isMany) {
			return type.toJavaType.toPolymorphicList
		} else
			return type.toJavaType
	}
	
	def JavaParametrizedType toPolymorphicList(JavaReferenceType t) {
		return new JavaParametrizedType(List.toJavaType, JavaWildcardTypeArgument.extendsBound(t));
	}
	
	def dispatch JavaReferenceType toReferenceType(JavaPrimitiveType type) {
		type.toReferenceType
	}
	def dispatch JavaReferenceType toReferenceType(JavaReferenceType type) {
		type
	}

	def JavaReferenceType toJavaType(ExpandedType type) {
		if (type.name == RosettaAttributeExtensions.METAFIELDS_CLASS_NAME || type.name == RosettaAttributeExtensions.META_AND_TEMPLATE_FIELDS_CLASS_NAME) {
			return new JavaClass(packages.basicMetafields, type.name)
		}
		if (type.metaType) {//TODO ExpandedType needs to store the underlying type for meta types if we want them to be anything other than strings
			return createForBasicType(RBuiltinType.STRING)
		}
		if (type.builtInType) {
			return JavaClassTranslator.toRType(type.name).toJavaType
		}
		new JavaClass(new RootPackage(type.model), type.name)
	}

	def JavaReferenceType toJavaType(RosettaCallableWithArgs func) {
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

	def JavaReferenceType toJavaType(RType rType) {
		switch (rType) {
			RBuiltinType:
				rType.createForBasicType
			REnumType:
				new JavaClass(modelRootPackage(rType.enumeration), rType.enumeration.name)
			RDataType:
				new JavaClass(modelRootPackage(rType.data), rType.data.name)
			RRecordType:
				rType.createForRecordType
		}
	}
	
	def JavaClass createMetaType(DottedPath parent, String meta) {
		return new JavaClass(parent, meta)
	}

	def JavaClass toMetaType(Attribute ctx, String name) {
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

	def JavaClass toMetaType(ExpandedAttribute type, String name) {
		if(type.type.isBuiltInType) {
			// built-in meta types are defined in metafield package
			return new JavaClass(packages.basicMetafields, name)
		}
		var parentPKG = new RootPackage(type.type.model)
		var metaPKG = parentPKG.metaField
		return new JavaClass(metaPKG, name)
	}

	def private RootPackage modelRootPackage(RosettaNamed namedType) {
		val rootElement = EcoreUtil2.getContainerOfType(namedType, RosettaRootElement)
		val model = rootElement.model
		if (model === null)
			// Faked attributes
			throw new IllegalArgumentException('''Can not compute package name for «namedType.eClass.name» «namedType.name». Element is not attached to a RosettaModel.''')
		return new RootPackage(model)
	}

	private def JavaReferenceType createForBasicType(RBuiltinType type) {
		return JavaClassTranslator.toJavaFullType(type).toReferenceType
	}
	private def JavaReferenceType createForRecordType(RRecordType type) {
		return JavaClassTranslator.toJavaFullType(type)
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
