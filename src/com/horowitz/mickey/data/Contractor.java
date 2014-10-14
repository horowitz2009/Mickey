package com.horowitz.mickey.data;


public class Contractor {
  private String     _name;
  private Material[] _materials;
  private int        _currentMissionNumber;
  private int        _endMissionNumber;

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
    return _currentMissionNumber;
  }

  public int getEndMissionNumber() {
    return _endMissionNumber;
  }

  public void setEndMissionNumber(int endMissionNumber) {
    _endMissionNumber = endMissionNumber;
  }

  public void setCurrentMissionNumber(int currentMissionNumber) {
    this._currentMissionNumber = currentMissionNumber;
  }

  public String getName() {
    return _name;
  }

}
