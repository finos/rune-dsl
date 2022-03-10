package com.regnosys.rosetta.types;


public class RQualifiedType extends RBuiltinType {
  public static final RQualifiedType PRODUCT_TYPE = new RQualifiedType("string", "productType");
  
  public static final RQualifiedType EVENT_TYPE = new RQualifiedType("string", "eventType");
  
  private final String qualifiedType;
  
  public RQualifiedType(final String name, final String qualifiedType) {
    super(name);
    this.qualifiedType = qualifiedType;
  }
  
  @Override
  public int hashCode() {
    return 31 * super.hashCode() + ((this.qualifiedType== null) ? 0 : this.qualifiedType.hashCode());
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
    RQualifiedType other = (RQualifiedType) obj;
    if (this.qualifiedType == null) {
      if (other.qualifiedType != null)
        return false;
    } else if (!this.qualifiedType.equals(other.qualifiedType))
      return false;
    return true;
  }
  
  public String getQualifiedType() {
    return this.qualifiedType;
  }
}
