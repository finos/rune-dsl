package com.regnosys.rosetta.types;


public class RCalculationType extends RBuiltinType {
  public static final RCalculationType CALCULATION = new RCalculationType("string", "calculation");
  
  private final String calculationType;
  
  public RCalculationType(final String name, final String calculationType) {
    super(name);
    this.calculationType = calculationType;
  }
  
  @Override
  public int hashCode() {
    return 31 * super.hashCode() + ((this.calculationType== null) ? 0 : this.calculationType.hashCode());
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
    RCalculationType other = (RCalculationType) obj;
    if (this.calculationType == null) {
      if (other.calculationType != null)
        return false;
    } else if (!this.calculationType.equals(other.calculationType))
      return false;
    return true;
  }
  
  public String getCalculationType() {
    return this.calculationType;
  }
}
