package com.regnosys.rosetta.validation

import com.regnosys.rosetta.types.RType
import java.util.Optional

class BindableType {
	public Optional<RType> type = Optional.empty
	
	def boolean isBound() {
		type.present
	}
	
	override String toString() { 
		type.map[toString].orElse("?")
	}
}
