package com.horowitz.mickey.data;

public class Material {
  private String _name;
  private int    _amount;

  public Material(String name, int amount) {
    super();
    _name = name;
    _amount = amount;
  }

  public int getAmount() {
    return _amount;
  }

  public void setAmount(int amount) {
    _amount = amount;
  }

  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return _name + ":" + _amount;
  }

}
