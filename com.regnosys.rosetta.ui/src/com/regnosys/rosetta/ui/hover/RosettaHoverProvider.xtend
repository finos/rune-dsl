package com.regnosys.rosetta.ui.hover

import com.google.inject.Inject
import com.regnosys.rosetta.generator.java.function.CardinalityProvider
import com.regnosys.rosetta.rosetta.RosettaDefinable
import com.regnosys.rosetta.types.RBuiltinType
import com.regnosys.rosetta.types.RosettaTypeProvider
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider

class RosettaHoverProvider extends DefaultEObjectHoverProvider {
	
	@Inject extension RosettaTypeProvider
	@Inject extension CardinalityProvider
	override protected getHoverInfoAsHtml(EObject o) {
		if (!hasHover(o))
			return null;
		val type = o.RType
		val definition = if (o instanceof RosettaDefinable) o.definition
		val many = o.isMulti
		val documentation = o.documentation
		'''
			«getFirstLine(o)»
			«IF type !== null && type !== RBuiltinType.MISSING»
				<br />Type: <b>«type.name»</b> Cardinality: <b>«IF many»many«ELSE»one«ENDIF»</b>
			«ENDIF»
			«IF definition !== null»
				<p>«definition»</p>
			«ENDIF»
			«IF documentation !== null»
				<p>«documentation»</p>
			«ENDIF»
		'''
	}
}