package com.regnosys.rosetta.types;

public abstract class RAnnotateType extends RType {
  private boolean meta = false;
  
  public void setWithMeta(final boolean meta) {
    this.meta = meta;
  }
  
  @Override
  public boolean hasMeta() {
    return this.meta;
  }
}
