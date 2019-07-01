package com.regnosys.rosetta.generator.java.object

import com.regnosys.rosetta.generator.java.object.ModelObjectBoilerPlate.TypeData
import com.regnosys.rosetta.generator.object.ExpandedAttribute
import com.regnosys.rosetta.rosetta.RosettaMetaType
import javax.inject.Inject

class ExternalHashcodeGenerator {
	@Inject extension ModelObjectBoilerPlate

	def processMethod(extension TypeData it) '''
		@Override
		public void process(RosettaPath path, Processor processor) {
			«IF hasSuperType»
				super.process(path, processor);
			«ENDIF»
			
			«FOR a : attributes.filter[!(isRosettaClass || hasMetas)]»
				«IF a.multiple»
					«a.name».stream().forEach(a->processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle».class, a, this«a.metaFlags»));
				«ELSE»
					processor.processBasic(path.newSubPath("«a.name»"), «a.toTypeSingle».class, «a.name», this«a.metaFlags»);
				«ENDIF»
			«ENDFOR»
			
			«FOR a : attributes.filter[isRosettaClass || hasMetas]»
				processRosetta(path.newSubPath("«a.name»"), processor, «a.toTypeSingle».class, «a.name»«a.metaFlags»);
			«ENDFOR»
		}
		
	'''
	
	def getMetaFlags(ExpandedAttribute attribute) {
		val result = new StringBuilder()
		if (attribute.type instanceof RosettaMetaType) {
			result.append(", AttributeMeta.IS_META")
		}
		result.toString
	}
	
}
