package com.rosetta.model.lib.validation;

import com.rosetta.model.lib.path.RosettaPath;

public interface C extends Validator<Foo>{
	
	ValidationResult validate(RosettaPath path, Foo foo);

}
