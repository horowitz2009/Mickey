package com.horowitz.mickey.data;

public class Objective {
  public static enum Type {
    DELIVER, BUILD, SEND, SELL
  }

  private String    _type;
  /**
   * if type is deliver or build, the material is one of the 17 materials, including gold. Else it could be train type, specific locomotive, wagon or
   * train set.
   */
  private String _material;
  private int    _neededAmount;
  private int    _currentAmount;
  private int    _initialAmount;

  public Objective(String type, String material) {
    this(type, material, 0, 0, 0);
  }

  public Objective(String type, String material, int initialAmount, int neededAmount, int currentAmount) {
    super();
    _type = type;
    _material = material;
    _initialAmount = initialAmount;
    _neededAmount = neededAmount;
    _currentAmount = currentAmount;
  }

  public String getType() {
    return _type;
  }

  public void setType(String type) {
    _type = type;
  }
  
  public String getMaterial() {
    return _material;
  }

  public void setMaterial(String material) {
    _material = material;
  }

  public int getNeededAmount() {
    return _neededAmount;
  }

  public void setNeededAmount(int neededAmount) {
    _neededAmount = neededAmount;
  }

  public int getCurrentAmount() {
    return _currentAmount;
  }

  public void setCurrentAmount(int currentAmount) {
    _currentAmount = currentAmount;
  }

  public int getInitialAmount() {
    return _initialAmount;
  }

  public void setInitialAmount(int initialAmount) {
    _initialAmount = initialAmount;
  }

}
