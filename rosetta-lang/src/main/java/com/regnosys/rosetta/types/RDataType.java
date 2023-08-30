package com.regnosys.rosetta.types;

import java.util.Objects;

import com.regnosys.rosetta.rosetta.simple.Data;
import com.rosetta.model.lib.ModelSymbolId;
import com.rosetta.util.DottedPath;

public class RDataType extends RAnnotateType {
	private final Data data;
	private final ModelSymbolId symbolId;

	public RDataType(final Data data) {
		super();
		this.data = data;
		this.symbolId = new ModelSymbolId(
				DottedPath.splitOnDots(data.getModel().getName()),
				data.getName()
			);
	}
	
	@Override
	public ModelSymbolId getSymbolId() {
		return this.symbolId;
	}

	public Data getData() {
		return this.data;
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.data == null) ? 0 : this.data.hashCode());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RDataType other = (RDataType) object;
		return Objects.equals(this.data, other.data);
	}
}
