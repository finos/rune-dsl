package com.regnosys.rosetta.tests.util

import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.rosetta.RosettaModel
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.eclipse.xtext.EcoreUtil2
import com.regnosys.rosetta.builtin.RosettaBuiltinsService
import java.util.ArrayList
import jakarta.inject.Inject

class ModelHelper {

	@Inject extension ParseHelper<RosettaModel>
	@Inject extension ValidationTestHelper
	@Inject RosettaBuiltinsService builtins


	public static val commonTestTypes = '''
		«getVersionInfo»
		
		type ReportableEvent:
		
		metaType scheme string
			
		body Authority ESMA
		body Authority CFTC
				
		corpus Regulation MiFIR
		corpus Regulation EMIR
		
		corpus CommissionDelegatedRegulation RTS_22
		
		segment article
		segment whereas
		segment annex
		segment section
		segment field
		
		synonym source FIX
		synonym source FpML
		synonym source DTCC
		synonym source ISO
		synonym source ISO_20022
		synonym source Bank_A
		synonym source Venue_B
	'''

	private def static getVersionInfo() {
		'''
			namespace "com.rosetta.test.model"
			version "test"
		'''
	}
	
	val rootpack = new RootPackage("com.rosetta.test.model")

	final def RootPackage rootPackage() {
		return rootpack
	}

	def parseRosetta(CharSequence model) {
		var m = model;
		if (!model.toString.trim.startsWith("namespace")) {
			m = versionInfo + "\n" + m
		}
		val resourceSet = testResourceSet()
		
		val parsed = m.parse(resourceSet)
		EcoreUtil2.resolveAll(parsed)
		return parsed;
	}

	def parseRosetta(CharSequence... models) {
		val resourceSet = testResourceSet()
		return new ArrayList(models
			.map[if (!it.toString.trim.startsWith("namespace")) versionInfo + "\n" + it else it]
			.map[it.parse(resourceSet)])
	}
	
	def parseRosettaWithNoErrors(CharSequence model) {
		val parsed = parseRosetta(model)
		parsed.assertNoErrors
		return parsed;
	}

	def parseRosettaWithNoErrors(CharSequence... models) {
		val parsed = parseRosetta(models)
		parsed.forEach[assertNoErrors]
		return parsed;
	}
	
	def parseRosettaWithNoIssues(CharSequence model) {
		val parsed = parseRosetta(model)
		parsed.assertNoIssues
		return parsed;
	}
	
	def parseRosettaWithNoIssues(CharSequence... models) {
		val parsed = parseRosetta(models)
		parsed.forEach[assertNoIssues]
		return parsed;
	}

	def combineAndParseRosetta(CharSequence... models) {
		var m = versionInfo.toString
		for (model : models) {
			m += "\n" + model
		}
		val resourceSet = testResourceSet()
		
		val parsed = m.parse(resourceSet)
		parsed.assertNoErrors
		return parsed;
	}
	
	def testResourceSet() {
		val resourceSet = parse(ModelHelper.commonTestTypes).eResource.resourceSet
		resourceSet.getResource(builtins.basicTypesURI, true)
		resourceSet.getResource(builtins.annotationsURI, true)
	
		return resourceSet;
	}
}
