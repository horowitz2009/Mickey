package com.horowitz.mickey.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataStore {

  private Gson _gson = new GsonBuilder().setPrettyPrinting().create();

  public Contractor[] readContractors() throws IOException {
    String json = FileUtils.readFileToString(new File("data/contractors.json"));

    Contractor[] contractors = _gson.fromJson(json, Contractor[].class);

    return contractors;
  }

  public Contractor getContractor(String name) throws IOException {
    Contractor[] contractors = readContractors();
    for (Contractor c : contractors) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  public void saveContractor(Contractor contractor) throws IOException {
    Contractor[] contractors = readContractors();
    for (Contractor c : contractors) {
      if (c.getName().equals(contractor.getName())) {
        c.extract(contractor);
        break;
      }
    }
    writeContractors(contractors);
  }

  public void writeContractors(Contractor[] contractors) throws IOException {

    String json = _gson.toJson(contractors);

    FileUtils.writeStringToFile(new File("data/contractors.json"), json);
  }

  public Mission[] readMissions(String contractor) throws IOException {
    return readMissions(contractor, "");
  }

  public Mission[] readMissions(String contractor, String type) throws IOException {
    File file = new File("data/" + contractor + "_missions" + type + ".json");
    if (file.exists()) {
      String json = FileUtils.readFileToString(file);
    
      Mission[] missions = _gson.fromJson(json, Mission[].class);
    
      return missions;
    } else {
      return new Mission[0];
    }
  }

  public Mission getMission(String contractor, int number) throws IOException {
    Mission[] missions = readMissions(contractor);
    for (Mission mission : missions) {
      if (mission.getNumber() == number) {
        return mission;
      }
    }
    return null;
  }

  public Mission getCurrentMission(String contractor, int number) throws IOException {
    Mission[] missions = readCurrentMissions();
    for (Mission mission : missions) {
      if (mission.getContractor().equalsIgnoreCase(contractor)) {
        return mission;
      }
    }
    return null;
  }

  public void writeMissions(String contractor, Mission[] missions) throws IOException {

    String json = _gson.toJson(missions);

    FileUtils.writeStringToFile(new File("data/" + contractor + "_missions.json"), json);
  }

  public void writeMission(Mission newMission) throws IOException {
    Mission[] missions = readMissions(newMission.getContractor());
    for (Mission m : missions) {
      if (m.getContractor().equals(newMission.getContractor())) {
        m.setAny(newMission.isAny());
        m.setNumber(newMission.getNumber());
        m.setObjectives(newMission.getObjectives());
        break;
      }
    }
    writeMissions(newMission.getContractor(), missions);
  }

  public Mission[] readCurrentMissions() throws IOException {
    File file = new File("data/current_missions.json");
    if (file.exists()) {
      String json = FileUtils.readFileToString(file);

      Mission[] missions = _gson.fromJson(json, Mission[].class);

      return missions;
    } else {
      return null;
    }
  }

  public void mergeCurrentWithDB(Mission[] currentMissions) throws IOException {
    for (int i = 0; i < currentMissions.length; i++) {
      Mission cm = currentMissions[i];
      Mission mdb = getMission(cm.getContractor(), cm.getNumber());
      if (mdb != null)
        cm.mergeWithDB(mdb);
    }
  }
  
  
  
  public void writeCurrentMissions(Mission[] currentMissions) throws IOException {
    String json = _gson.toJson(currentMissions);

    FileUtils.writeStringToFile(new File("data/current_missions.json"), json);
  }

  public void writeCurrentMission(Mission currentMission) throws IOException {
    Mission[] missions = readCurrentMissions();
    boolean found = false;
    for (Mission mission : missions) {
      if (mission.getContractor().equals(currentMission.getContractor())) {
        mission.setAny(currentMission.isAny());
        mission.setNumber(currentMission.getNumber());
        mission.setObjectives(currentMission.getObjectives());
        found = true;
        break;
      }
    }
    if (!found) {
      //it's new
      missions = (Mission[]) ArrayUtils.add(missions, currentMission);
    }
    
    writeCurrentMissions(missions);
  }

  public List<String> getActiveContractorNames() throws IOException {
    List<String> active = new ArrayList<>();
    for (Contractor c : readContractors()) {
      if (c.isActive()) {
        active.add(c.getName());
      }
    }
    return active;
  }

}
