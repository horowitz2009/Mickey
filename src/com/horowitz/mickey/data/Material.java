package com.horowitz.mickey.data;

public class Material {

  public static enum NAMES {
    GOLD, WOOD, NAILS, BRICKS, GLASS, FUEL, STEEL, GRAVEL, U235, CEMENT, RUBBER, CARBON, TITANIUM, MARBLE, WIRES, PLASTICS, SILICON
  };

  public static Material[] createArray() {
    return new Material[] { new Material("Gold"), new Material("Wood"), new Material("Nails"), new Material("Bricks"), new Material("Glass"),
        new Material("Fuel"), new Material("Steel"), new Material("Gravel"), new Material("U-235"), new Material("Cement"), new Material("Rubber"),
        new Material("Carbon"), new Material("Titanium"), new Material("Marble"), new Material("Wires"), new Material("Plastics"),
        new Material("Silicon"), };
  }

  public static final Material[] ALL = createArray();

  private String                 _name;
  private int                    _amount;

  public Material(String name) {
    this(name, 0);
  }

  public Material(String name, int amount) {
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
