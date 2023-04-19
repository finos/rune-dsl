package com.regnosys.rosetta.types;

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

	public String getMessage() {
		return this.message;
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.message == null) ? 0 : this.message.hashCode());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RErrorType other = (RErrorType) obj;
		if (this.message == null) {
			if (other.message != null)
				return false;
		} else if (!this.message.equals(other.message))
			return false;
		return true;
	}
}
