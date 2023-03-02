package com.regnosys.rosetta.types;

import com.regnosys.rosetta.rosetta.RosettaNamed;
import com.regnosys.rosetta.rosetta.RosettaRecordType;


public class RRecordType extends RAnnotateType {
  private final RosettaRecordType record;
  
  public RRecordType(final RosettaRecordType record) {
    super();
    this.record = record;
  }
  
  @Override
  public String getName() {
    return ((RosettaNamed) this.record).getName();
  }
  
  @Override
  public int hashCode() {
    return 31 * 1 + ((this.record== null) ? 0 : this.record.hashCode());
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RRecordType other = (RRecordType) obj;
    if (this.record == null) {
      if (other.record != null)
        return false;
    } else if (!this.record.equals(other.record))
      return false;
    return true;
  }
  
  public RosettaRecordType getRecord() {
    return this.record;
  }
}
