package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.types.RType;
import java.util.Optional;

public class BindableType {
	public Optional<RType> type = Optional.empty();

	public boolean isBound() {
		return type.isPresent();
	}

	@Override
	public String toString() {
		return type.map(RType::toString).orElse("?");
	}
}
