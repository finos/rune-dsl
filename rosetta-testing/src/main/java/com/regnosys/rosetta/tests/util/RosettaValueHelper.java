package com.regnosys.rosetta.tests.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.regnosys.rosetta.interpreter.RosettaBooleanValue;
import com.regnosys.rosetta.interpreter.RosettaNumber;
import com.regnosys.rosetta.interpreter.RosettaNumberValue;
import com.regnosys.rosetta.interpreter.RosettaStringValue;
import com.regnosys.rosetta.interpreter.RosettaValue;

public class RosettaValueHelper {
	private <U> Optional<RosettaValue> tryCast(List<?> list, Class<U> clazz, Function<List<U>, RosettaValue> construct) {
		if (list.stream().allMatch(i -> clazz.isInstance(i))) {
			return Optional.of(construct.apply(list.stream().map(i -> clazz.cast(i)).collect(Collectors.toList())));
		}
		return Optional.empty();
	}
	public RosettaValue toValue(Object obj) {
		if (obj instanceof RosettaValue) {
			return (RosettaValue)obj;
		} else if (obj instanceof List<?>) {
			List<?> list = (List<?>)obj;
			if (list.isEmpty()) {
				return RosettaValue.empty();
			} else {
				return tryCast(list, Boolean.class, RosettaBooleanValue::new)
						.or(() -> tryCast(list, RosettaNumber.class, RosettaNumberValue::new))
						.or(() -> tryCast(list, Integer.class, ints -> new RosettaNumberValue(ints.stream().map(i -> RosettaNumber.valueOf(i)).collect(Collectors.toList()))))
						.or(() -> tryCast(list, String.class, RosettaStringValue::new))
						.orElseThrow(() -> new IllegalArgumentException("Unsupported Rosetta value " + obj));
			}
		} else {
			return toValue(List.of(obj));
		}
	}
}
