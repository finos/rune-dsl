package com.regnosys.rosetta.types

import org.eclipse.xtend.lib.annotations.Data

@Data
class BinaryOperation {
	String operator
	RBuiltinType left
	RBuiltinType right
}