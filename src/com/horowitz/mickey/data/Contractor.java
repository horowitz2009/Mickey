package com.horowitz.mickey.data;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Contractor {
  
  private String     _name;
  
  private Material[] _materials;
  
  private int        _currentMissionNumber;
  
  private Mission    _currentMission;
  
  private int        _endMissionNumber;

  
  
  public Contractor() {
    super();
  }

  public Contractor(String name) {
    this(name, null);
  }

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

  public Mission getCurrentMission() {
    return _currentMission;
  }

  public void setCurrentMission(Mission currentMission) {
    _currentMission = currentMission;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
  }
}