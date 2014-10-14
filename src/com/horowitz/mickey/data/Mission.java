package com.horowitz.mickey.data;

import java.util.List;

public class Mission {
  private List<Objective> _objectives;

  private int             _number;
  private String          _contractor;

  /**
   * if true means any of the objective if being done, then mission is considered accomplished. If false, then all objectives must be done.
   */
  private boolean         _any;
  
  

}
