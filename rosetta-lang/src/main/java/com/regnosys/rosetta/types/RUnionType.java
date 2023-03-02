package com.regnosys.rosetta.types;


public class RUnionType extends RType {
  private final RType from;
  
  private final RType to;
  
  private final String name;
  
  public RUnionType(final RType from, final RType to) {
    this.from = from;
    this.to = to;
    String _name = from.getName();
    String _plus = (_name + " or ");
    String _name_1 = to.getName();
    String _plus_1 = (_plus + _name_1);
    this.name = _plus_1;
  }
  
  @Override
  public String getName() {
    return this.name;
  }
  
  public String getToName() {
    String _xifexpression = null;
    if ((this.to instanceof RUnionType)) {
      _xifexpression = ((RUnionType)this.to).getToName();
    } else {
      _xifexpression = this.to.getName();
    }
    return _xifexpression;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.from== null) ? 0 : this.from.hashCode());
    result = prime * result + ((this.to== null) ? 0 : this.to.hashCode());
    return prime * result + ((this.name== null) ? 0 : this.name.hashCode());
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RUnionType other = (RUnionType) obj;
    if (this.from == null) {
      if (other.from != null)
        return false;
    } else if (!this.from.equals(other.from))
      return false;
    if (this.to == null) {
      if (other.to != null)
        return false;
    } else if (!this.to.equals(other.to))
      return false;
    if (this.name == null) {
      if (other.name != null)
        return false;
    } else if (!this.name.equals(other.name))
      return false;
    return true;
  }
  
  public RType getFrom() {
    return this.from;
  }
  
  public RType getTo() {
    return this.to;
  }
}
