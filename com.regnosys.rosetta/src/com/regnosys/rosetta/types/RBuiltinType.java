package com.regnosys.rosetta.types;


public class RBuiltinType extends RAnnotateType {
  public static final RBuiltinType ANY = new RBuiltinType("any");
  
  public static final RBuiltinType BOOLEAN = new RBuiltinType("boolean");
  
  public static final RBuiltinType STRING = new RBuiltinType("string");
  
  public static final RNumberType INT = new RNumberType("int", 0);
  
  public static final RNumberType NUMBER = new RNumberType("number", 1);
  
  public static final RBuiltinType DATE = new RBuiltinType("date");
  
  public static final RBuiltinType DATE_TIME = new RBuiltinType("dateTime");
  
  public static final RBuiltinType ZONED_DATE_TIME = new RBuiltinType("zonedDateTime");
  
  public static final RBuiltinType TIME = new RBuiltinType("time");
  
  public static final RBuiltinType MISSING = new RBuiltinType("missing");
  
  public static final RBuiltinType VOID = new RBuiltinType("void");
  
  public static final RBuiltinType NOTHING = new RBuiltinType("nothing");
  
  private final String name;
  
  public RBuiltinType(final String name) {
    super();
    this.name = name;
  }
  
  @Override
  public int hashCode() {
    return 31 * 1 + ((this.name== null) ? 0 : this.name.hashCode());
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RBuiltinType other = (RBuiltinType) obj;
    if (this.name == null) {
      if (other.name != null)
        return false;
    } else if (!this.name.equals(other.name))
      return false;
    return true;
  }
  
  public String getName() {
    return this.name;
  }
}
