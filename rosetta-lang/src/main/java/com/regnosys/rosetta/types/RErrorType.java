package com.regnosys.rosetta.types;

import java.util.Objects;

import com.rosetta.util.DottedPath;

// TODO: remove this type
public class RErrorType extends RType {
	private final String message;

	public RErrorType(final String message) {
		super();
		this.message = message;
	}

	@Override
	public String getName() {
		return this.message;
	}
	
	@Override
	public DottedPath getNamespace() {
		return DottedPath.of();
	}


	public String getMessage() {
		return this.message;
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.message == null) ? 0 : this.message.hashCode());
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) return false;
        if (this.getClass() != object.getClass()) return false;
        
		RErrorType other = (RErrorType) object;
		return Objects.equals(message, other.message);
	}
}
