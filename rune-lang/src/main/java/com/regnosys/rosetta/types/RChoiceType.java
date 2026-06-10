package com.regnosys.rosetta.types;

import com.google.common.collect.Streams;
import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.model.lib.ModelSymbolId;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RChoiceType extends RType implements RObject {
	private final Choice choice;
	
	private ModelSymbolId symbolId = null;
	private List<RChoiceOption> ownOptions = null;
	private RDataType dataTypeView = null;
	
	private final ModelIdProvider modelIdProvider;
	private final RosettaTypeProvider typeProvider;
	private final RObjectFactory rObjectFactory;

	public RChoiceType(final Choice choice, final ModelIdProvider modelIdProvider, final RosettaTypeProvider typeProvider, final RObjectFactory rObjectFactory) {
		super();
		this.choice = choice;
		
		this.modelIdProvider = modelIdProvider;
		this.typeProvider = typeProvider;
		this.rObjectFactory = rObjectFactory;
	}
	
	@Deprecated // TODO: remove this and fully support choice types. See https://github.com/finos/rune-dsl/issues/797.
	public RDataType asRDataType() {
		if (dataTypeView == null) {
			dataTypeView = rObjectFactory.buildRDataType(choice);
		}
		return dataTypeView;
	}
	
	@Override
	public Choice getEObject() {
		return this.choice;
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		if (this.symbolId == null) {
			this.symbolId = modelIdProvider.getSymbolId(choice);;
		}
		return this.symbolId;
	}
	
	/**
	 * Get a list of the options defined in this choice type. This does not include options of any nested choice types.
	 */
	public List<RChoiceOption> getOwnOptions() {
		if (ownOptions == null) {
			this.ownOptions = choice.getOptions().stream().map(o -> new RChoiceOption(o, this, typeProvider)).collect(Collectors.toList());
		}
		return ownOptions;
	}
	
	/**
	 * Returns true if every leaf option type of this choice (recursively through nested choices) has a [metadata key].
	 * Returns false for an empty choice, an unresolved option, or any unkeyed leaf.
	 */
	public boolean hasImpliedKey() {
		return doHasImpliedKey(new HashSet<>());
	}
	private boolean doHasImpliedKey(Set<RChoiceType> visited) {
		if (!visited.add(this)) {
			return false; // cycle guard
		}
		List<RChoiceOption> options = getOwnOptions();
		if (options.isEmpty()) {
			return false;
		}
		for (RChoiceOption option : options) {
			RType optionRType = option.getType().getRType();
			if (optionRType instanceof RChoiceType) {
				if (!((RChoiceType) optionRType).doHasImpliedKey(visited)) {
					return false;
				}
			} else if (optionRType instanceof RDataType) {
				RDataType leaf = (RDataType) optionRType;
				// A leaf is keyed if it (or any of its supertypes) is annotated with [metadata key],
				// mirroring the reference-target check in AttributeValidator.
				boolean keyed = leaf.hasMetaAttribute("key")
						|| leaf.getAllSuperTypes().stream().anyMatch(st -> st.hasMetaAttribute("key"));
				if (!keyed) {
					return false;
				}
			} else {
				return false; // unresolved or unknown type
			}
		}
		return true;
	}

	/**
	 * Get a list of all options of this choice type, including all options of its nested choice types.
	 * 
	 * The list starts with the options of this choice type, recursively including nested options after their containing option.
	 */
	public List<RChoiceOption> getAllOptions() {
		return doGetAllOptions(new HashSet<>()).collect(Collectors.toList());
	}
	private Stream<RChoiceOption> doGetAllOptions(Set<RChoiceType> visited) {
		if (visited.add(this)) {
			return getOwnOptions().stream().flatMap(o -> {
				if (o.getType().getRType() instanceof RChoiceType) {
					RChoiceType nested = (RChoiceType) o.getType().getRType();
					return Streams.concat(Stream.of(o), nested.doGetAllOptions(visited));
				}
				return Stream.of(o);
			});
		}
		return Stream.empty();
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.choice == null) ? 0 : this.choice.hashCode());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RChoiceType other = (RChoiceType) object;
		return Objects.equals(this.choice, other.choice);
	}
}
