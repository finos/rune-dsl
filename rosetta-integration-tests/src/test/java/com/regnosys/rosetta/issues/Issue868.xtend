package com.regnosys.rosetta.issues

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith

import javax.inject.Inject

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import com.regnosys.rosetta.tests.util.ModelHelper

// Regression test for https://github.com/finos/rune-dsl/issues/844
@ExtendWith(InjectionExtension)
@InjectWith(RosettaTestInjectorProvider)
@TestInstance(Lifecycle.PER_CLASS)
class Issue868 {
	
	@Inject extension ModelHelper
		
	@Test
	def void singleSwitchBeforeOtherFunctionShouldParse() {
		'''
		choice AssetCriterium:
			AssetType
		
		type AssetType:
		
		func Foo:
			inputs:
				criterium AssetCriterium (1..1)
			output:
				result boolean (1..1)
			set result:
				criterium switch
					AssetType then empty
		
		func OtherFunc:
			inputs:
				inp int (1..1)
			output:
				outp int (1..1)
		'''.parseRosettaWithNoIssues
	}
}