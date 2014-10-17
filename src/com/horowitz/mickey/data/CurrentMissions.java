package com.horowitz.mickey.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CurrentMissions {

  private Mission[] _currentMissions;

  public CurrentMissions() {
    super();
    readCurrentMissions();
  }

  private void readCurrentMissions() {
    try {
      _currentMissions = new DataStore().readCurrentMissions();
    } catch (FileNotFoundException e) {
      _currentMissions = generateInitial();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Mission[] generateInitial() {
    List<Mission> currentMissions = new ArrayList<>();
    {
      Mission m = new Mission();
      m.setContractor("Bobby");
      m.setNumber(24);
      currentMissions.add(m);
    }
    {
      Mission m = new Mission();
      m.setContractor("Mahatma");
      m.setNumber(45);
      currentMissions.add(m);
    }
    {
      Mission m = new Mission();
      m.setContractor("George");
      m.setNumber(28);
      currentMissions.add(m);
    }

    return currentMissions.toArray(new Mission[0]);

  }

  public static void main(String[] args) {
    CurrentMissions currentMissions = new CurrentMissions();

  }

}
