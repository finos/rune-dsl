package com.regnosys.rosetta.blueprints.runner.data;

public class ClassIdentifier implements DataIdentifier {
	Class<?> c;

	public ClassIdentifier(Class<?> c) {
		super();
		this.c = c;
	}

	public Class<?> getC() {
		return c;
	}

	public void setC(Class<?> c) {
		this.c = c;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((c == null) ? 0 : c.hashCode());
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
		ClassIdentifier other = (ClassIdentifier) obj;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClassIdentifier [class=" + c.getSimpleName() + "]";
	}
	
	
}
