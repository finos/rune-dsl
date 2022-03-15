package com.regnosys.rosetta.types;

import com.regnosys.rosetta.rosetta.RosettaEnumeration;

public class REnumType extends RAnnotateType {
  private final RosettaEnumeration enumeration;
  
  public REnumType(final RosettaEnumeration enumeration) {
    super();
    this.enumeration = enumeration;
  }
  
  @Override
  public String getName() {
    return this.enumeration.getName();
  }
  
  @Override
  public int hashCode() {
    return 31 * 1 + ((this.enumeration== null) ? 0 : this.enumeration.hashCode());
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    REnumType other = (REnumType) obj;
    if (this.enumeration == null) {
      if (other.enumeration != null)
        return false;
    } else if (!this.enumeration.equals(other.enumeration))
      return false;
    return true;
  }
  
  public RosettaEnumeration getEnumeration() {
    return this.enumeration;
  }
}
