package com.regnosys.rosetta.blueprints.runner.data;

public class RosettaIdentifier 
	implements DataIdentifier{
	
	//This field should be a rosetta identifier that identifies a field in an object
	//e.g. RTS_22_TransactionReport->price
	private String regRef;
	
	public RosettaIdentifier(String identifier) {
		this.regRef = identifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((regRef == null) ? 0 : regRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RosettaIdentifier other = (RosettaIdentifier) obj;
		if (regRef == null) {
			if (other.regRef != null)
				return false;
		} else if (!regRef.equals(other.regRef))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return regRef;
	}
	
		
}
