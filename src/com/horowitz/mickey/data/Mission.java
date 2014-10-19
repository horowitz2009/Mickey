package com.horowitz.mickey.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Mission {
  private List<Objective> _objectives;

  private int             _number;
  private String          _contractor;

  /**
   * if true means any of the objective if being done, then mission is considered accomplished. If false, then all objectives must be done.
   */
  private boolean         _any;

  public Mission() {
    super();
    _objectives = new ArrayList<Objective>();
  }

  public List<Objective> getObjectives() {
    return _objectives;
  }

  public void setObjectives(List<Objective> objectives) {
    _objectives = objectives;
  }

  public int getNumber() {
    return _number;
  }

  public void setNumber(int number) {
    _number = number;
  }

  public String getContractor() {
    return _contractor;
  }

  public void setContractor(String contractor) {
    _contractor = contractor;
  }

  public boolean isAny() {
    return _any;
  }

  public void setAny(boolean any) {
    _any = any;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
  }
  
  

}
