package com.horowitz.mickey.data;

public class Home {

  private int        trainSlots;
  private int        intSlots;
  private int        depotSlots;
  private int        _freeFreight;
  private int        _freight;
  private int        _express;
  private Material[] _materials;

  public Home() {
    super();
    _materials = Material.createArray();
  }

  public Material[] getMaterials() {
    return _materials;
  }

  public int getTrainSlots() {
    return trainSlots;
  }

  public void setTrainSlots(int trainSlots) {
    this.trainSlots = trainSlots;
  }

  public int getIntSlots() {
    return intSlots;
  }

  public void setIntSlots(int intSlots) {
    this.intSlots = intSlots;
  }

  public int getDepotSlots() {
    return depotSlots;
  }

  public void setDepotSlots(int depotSlots) {
    this.depotSlots = depotSlots;
  }

  public int getFreeFreight() {
    return _freeFreight;
  }

  public void setFreeFreight(int freeFreight) {
    _freeFreight = freeFreight;
  }

  public int getFreight() {
    return _freight;
  }

  public void setFreight(int freight) {
    _freight = freight;
  }

  public int getExpress() {
    return _express;
  }

  public void setExpress(int express) {
    _express = express;
  }

  public void setMaterials(Material[] materials) {
    _materials = materials;
  }

  public static void main(String[] args) {
    Home home1 = new Home();
    home1.getMaterials()[Material.NAMES.BRICKS.ordinal()].setAmount(12);
    System.out.println(home1.getMaterials()[Material.NAMES.BRICKS.ordinal()]);
    Home home2 = new Home();
    home2.getMaterials()[Material.NAMES.BRICKS.ordinal()].setAmount(24);
    System.out.println(home2.getMaterials()[Material.NAMES.BRICKS.ordinal()]);
    System.out.println(home1.getMaterials()[Material.NAMES.BRICKS.ordinal()]);
  }

}