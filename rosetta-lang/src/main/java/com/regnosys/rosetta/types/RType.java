package com.regnosys.rosetta.types;

public abstract class RType {
  public abstract String getName();
  
  public boolean hasMeta() {
    return false;
  }
  
  @Override
  public String toString() {
    return this.getName();
  }
}
