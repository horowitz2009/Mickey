package com.horowitz.mickey.model;

import java.util.List;

public class Protocol {

  private String name;
  private List _entries;
  
  
  public static class Entry {
    private String type;
    private String destinations;
  }
}
