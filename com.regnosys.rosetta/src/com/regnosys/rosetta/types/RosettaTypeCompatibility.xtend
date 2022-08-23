package com.regnosys.rosetta.types

import com.google.inject.Inject
import com.regnosys.rosetta.RosettaExtensions

class RosettaTypeCompatibility {

	@Inject extension RosettaExtensions

	def dispatch boolean isUseableAs(Void t0, Void t1) {
		true
	}
	
	def dispatch boolean isUseableAs(RDataType t0, RDataType t1) {
		t0.data.allSuperTypes.contains(t1.data)
	}

	def dispatch boolean isUseableAs(RNumberType t0, RNumberType t1) {
		t0.rank <= t1.rank
	}

	def dispatch boolean isUseableAs(RRecordType t0, RRecordType t1) {
		t0.name == t1.name
	}

	def dispatch boolean isUseableAs(RType t0, RType t1) {
		t0.class === t1.class
	}

	def dispatch boolean isUseableAs(RBuiltinType t0, RBuiltinType t1) {
		t0 == RBuiltinType.NOTHING || t0 == t1
	}
	
	def dispatch boolean isUseableAs(RBuiltinType t0, RType t1) {
		t0 == RBuiltinType.NOTHING
	}

	def dispatch boolean isUseableAs(RUnionType t0, RType t1) {
		t0.from.isUseableAs(t1) || t0.to.isUseableAs(t1)
	}

	def dispatch boolean isUseableAs(RType t0, RUnionType t1) {
		t1.from.isUseableAs(t0) || t1.to.isUseableAs(t0)
	}

	def dispatch boolean isUseableAs(RErrorType t0, RErrorType t1) {
		false
	}

	def dispatch boolean isUseableAs(REnumType t0, REnumType t1) {
		t0.enumeration === t1.enumeration || t0.enumeration.allSuperEnumerations.contains(t1.enumeration)
	}

}
