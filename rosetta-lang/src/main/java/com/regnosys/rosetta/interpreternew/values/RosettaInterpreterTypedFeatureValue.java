package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterTypedFeatureValue extends RosettaInterpreterBaseValue {
	
	public String name;
	public RosettaInterpreterBaseValue value;
	public RosettaCardinality card;

	public RosettaInterpreterTypedFeatureValue(String name, RosettaInterpreterBaseValue value,
			RosettaCardinality card) {
		super();
		this.name = name;
		this.value = value;
		this.card = card;
	}

	public String getName() {
		return name;
	}

	public RosettaInterpreterBaseValue getValue() {
		return value;
	}

	public RosettaCardinality getCard() {
		return card;
	}

	@Override
	public Stream<Object> toElementStream() {
		return Stream.of(name, value, card);
	}

	@Override
	public Stream<RosettaInterpreterValue> toValueStream() {
		return Stream.of(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(card, name, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RosettaInterpreterTypedFeatureValue other = (RosettaInterpreterTypedFeatureValue) obj;
		return Objects.equals(card, other.card) && Objects.equals(name, other.name)
				&& Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "RosettaInterpreterTypedFeatureValue [name=" + name + ", value=" + value + ", card=" + card + "]";
	}

}
