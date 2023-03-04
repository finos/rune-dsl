package com.regnosys.rosetta.types;

import com.regnosys.rosetta.rosetta.simple.Data;

public class RDataType extends RAnnotateType {
  private final Data data;
  
  public RDataType(final Data data) {
    super();
    this.data = data;
  }
  
  @Override
  public String getName() {
    return this.data.getName();
  }
  
  @Override
  public int hashCode() {
    return 31 * 1 + ((this.data== null) ? 0 : this.data.hashCode());
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RDataType other = (RDataType) obj;
    if (this.data == null) {
      if (other.data != null)
        return false;
    } else if (!this.data.equals(other.data))
      return false;
    return true;
  }
  
  public Data getData() {
    return this.data;
  }
}
