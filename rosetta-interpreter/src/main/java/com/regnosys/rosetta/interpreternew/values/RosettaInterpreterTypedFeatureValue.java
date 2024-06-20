package com.regnosys.rosetta.interpreternew.values;

import java.util.Objects;
import java.util.stream.Stream;

import com.regnosys.rosetta.rosetta.RosettaCardinality;
import com.regnosys.rosetta.rosetta.interpreter.RosettaInterpreterValue;

public class RosettaInterpreterTypedFeatureValue extends RosettaInterpreterBaseValue {
	
	public String name;
	public RosettaInterpreterValue value;
	public RosettaCardinality card;

	/**
	 * Constructor for data-type feature value.
	 *
	 * @param name 		name value
	 * @param value		value of feature
	 * @param card		cardinality value
	 */
	public RosettaInterpreterTypedFeatureValue(String name, RosettaInterpreterValue value, 
			RosettaCardinality card) {
		super();
		this.name = name;
		this.value = value;
		this.card = card;
	}
	
	/**
	 * Constructor for data-type feature value.
	 *
	 * @param name 		name value
	 */
	public RosettaInterpreterTypedFeatureValue(String name) {
		super();
		this.name = name;
		this.value = null;
		this.card = null;
	}
	
	/**
	 * Constructor for data-type feature value.
	 *
	 * @param name 		name value
	 * @param value		value of feature
	 */
	public RosettaInterpreterTypedFeatureValue(String name, RosettaInterpreterValue value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public RosettaInterpreterValue getValue() {
		return value;
	}
	
	public void setValue(RosettaInterpreterValue value) {
		this.value = value;
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
		return "RosettaInterpreterTypedFeatureValue [name=" + name + ", value=" + value 
				+ ", card=" + card + "]";
	}
}
