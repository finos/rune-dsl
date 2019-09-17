package com.regnosys.rosetta.generator.java.function

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions
import com.regnosys.rosetta.generator.java.util.JavaNames
import com.regnosys.rosetta.generator.util.RosettaFunctionExtensions
import com.regnosys.rosetta.rosetta.RosettaCallableCall
import com.regnosys.rosetta.rosetta.RosettaExpression
import com.regnosys.rosetta.rosetta.RosettaFeatureCall
import com.regnosys.rosetta.rosetta.RosettaMetaType
import com.regnosys.rosetta.rosetta.RosettaNamed
import com.regnosys.rosetta.rosetta.RosettaRegularAttribute
import com.regnosys.rosetta.rosetta.simple.Attribute
import com.regnosys.rosetta.rosetta.simple.Function
import com.regnosys.rosetta.rosetta.simple.ShortcutDeclaration
import com.regnosys.rosetta.types.RosettaTypeProvider
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.EcoreUtil2

import static extension com.regnosys.rosetta.generator.java.util.JavaClassTranslator.toJavaType
import com.regnosys.rosetta.rosetta.RosettaFeature

class ExpressionGeneratorWithBuilder {

	@Inject RosettaTypeProvider typeProvider
	@Inject ConvertableCardinalityProvider cardinalityProvider
	@Inject RosettaFunctionExtensions funcExt
	@Inject extension RosettaExtensions

	dispatch def StringConcatenationClient javaCode(RosettaExpression expr, Context ctx) {
		throw new UnsupportedOperationException('Not supported expression: ' + expr.eClass.name)
	}

	dispatch def StringConcatenationClient javaCode(RosettaFeatureCall expr, Context ctx) {
		var autoValue = true // if the attribute being referenced is WithMeta and we aren't accessing the meta fields then access the value by default
		if (expr.eContainer !== null && expr.eContainer instanceof RosettaFeatureCall &&
			(expr.eContainer as RosettaFeatureCall).feature instanceof RosettaMetaType) {
			autoValue = false;
		}
		val feature = expr.feature
		val StringConcatenationClient right = if (feature instanceof RosettaRegularAttribute)
				feature.attributeAccess(ctx, autoValue)
			else if (feature instanceof Attribute)
				feature.attributeAccess(ctx, autoValue)
			else
				throw new UnsupportedOperationException("Unsupported expression type of " + feature.class.simpleName)
		'''«expr.receiver.javaCode(ctx)».«right»(«IF cardinalityProvider.isMulti(feature)»0«ENDIF»)'''
	}

	dispatch def StringConcatenationClient javaCode(RosettaCallableCall expr, Context ctx) {
		val callee = expr.callable
		switch (callee) {
			Attribute: {
				'''«callee.name»'''
			}
			ShortcutDeclaration: {
				'''«callee.name»(«inputsAsArgs(callee)»)'''
			}
			RosettaNamed: {
				'''«callee.name»'''
			}
			default:
				throw new UnsupportedOperationException("Unsupported callable type of " + callee.class.simpleName)
		}
	}

	private def StringConcatenationClient attributeAccess(RosettaFeature feature, Context ctx, boolean autoVal) {
		'''getOrCreate«feature.name.toFirstUpper»'''
	}


	private def inputsAsArgs(ShortcutDeclaration alias) {
		val func = EcoreUtil2.getContainerOfType(alias, Function)
		funcExt.getInputs(func).join(', ')[name]
	}

	private dispatch def metaClass(RosettaRegularAttribute attribute) {
		if (attribute.metaTypes.exists[m|m.name == "reference"])
			"ReferenceWithMeta" + attribute.type.name.toJavaType.toFirstUpper
		else
			"FieldWithMeta" + attribute.type.name.toJavaType.toFirstUpper
	}

	private dispatch def metaClass(Attribute attribute) {
		if (attribute.annotations.exists[a|a.annotation?.name == "metadata" && a.attribute?.name == "reference"])
			"ReferenceWithMeta" + attribute.type.name.toJavaType.toFirstUpper
		else
			"FieldWithMeta" + attribute.type.name.toJavaType.toFirstUpper
	}

}

@Data
class Context {
	JavaNames names

	static def create(JavaNames names) {
		new Context(names)
	}
}
