package com.regnosys.rosetta.types;


public class RNumberType extends RBuiltinType {
  private final int rank;
  
  public static RNumberType getCommonType(final RNumberType t0, final RNumberType t1) {
    if (t0.rank < t1.rank) {
      return t1;
    }
    return t0;
  }
  
  public RNumberType(final String name, final int rank) {
    super(name);
    this.rank = rank;
  }
  
  @Override
  public int hashCode() {
    return 31 * super.hashCode() + this.rank;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    if (!super.equals(obj))
      return false;
    RNumberType other = (RNumberType) obj;
    if (other.rank != this.rank)
      return false;
    return true;
  }
  
  public int getRank() {
    return this.rank;
  }
}
