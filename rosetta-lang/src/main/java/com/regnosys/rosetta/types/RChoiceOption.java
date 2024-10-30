package com.regnosys.rosetta.types;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.simple.ChoiceOption;

public class RChoiceOption implements RObject {
	
	private final ChoiceOption option;
	private RMetaAnnotatedType type = null;
	
	private final RChoiceType choiceType;
	
	private final RosettaTypeProvider typeProvider;
	
	public RChoiceOption(ChoiceOption option, RChoiceType choiceType, RosettaTypeProvider typeProvider) {
		this.option = option;
		this.choiceType = choiceType;
		
		this.typeProvider = typeProvider;
	}

	@Override
	public ChoiceOption getEObject() {
		return option;
	}

	public RMetaAnnotatedType getType() {
		if (type == null) {
			type = typeProvider.getRTypeOfSymbol(option);
		}
		return type;
	}
	
	public RChoiceType getChoiceType() {
		return choiceType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(option);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RChoiceOption other = (RChoiceOption) obj;
		return Objects.equals(option, other.option);
	}

	@Override
	public String toString() {
		return "RChoiceOption [type=" + type + "]";
	}
}
