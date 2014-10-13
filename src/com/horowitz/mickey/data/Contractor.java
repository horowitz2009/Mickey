package com.horowitz.mickey.data;


public class Contractor {
  private String     _name;
  private Material[] _materials;
  private int        currentMissionNumber;

  public Contractor(String name, Material[] materials) {
    super();
    _name = name;
    _materials = materials;
  }

  public Material[] getMaterials() {
    return _materials;
  }

  public void setMaterials(Material[] materials) {
    _materials = materials;
  }

  public int getCurrentMissionNumber() {
    return currentMissionNumber;
  }

  public void setCurrentMissionNumber(int currentMissionNumber) {
    this.currentMissionNumber = currentMissionNumber;
  }

  public String getName() {
    return _name;
  }

}
