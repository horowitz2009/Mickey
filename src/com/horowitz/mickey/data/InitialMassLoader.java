package com.horowitz.mickey.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class InitialMassLoader {

  public static void main(String[] args) {
    System.out.println("genereating contractors and missions");
    // generateContractors();
    // generateMissions();
    loadHomeMissions();

    System.out.println("Done.");
  }

  private static void loadHomeMissions() {
    try {
      DataStore ds = new DataStore();
      Home home = new Home();
      home.setMaterials(Material.createArray());
      ds.saveHome(home);

      List<Mission> newMissions = new ArrayList<>();

      int n = 100;
      Mission m;
      List<Objective> objectives;

      m = new Mission("Home", "International slot #13", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 8000000));
      objectives.add(new Objective("b", "Wires", 1040000));
      objectives.add(new Objective("b", "Silicon", 1040000));
      m.setObjectives(objectives);
      m.setSelected(true);
      newMissions.add(m);

      m = new Mission("Home", "International slot #14", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Nails", 1116000));
      objectives.add(new Objective("b", "U-235", 1044000));
      objectives.add(new Objective("b", "Carbon", 1044000));
      m.setObjectives(objectives);
      newMissions.add(m);

      m = new Mission("Home", "International slot #15", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Glass", 1200000));
      objectives.add(new Objective("b", "Titanium", 1120000));
      objectives.add(new Objective("b", "Plastics", 1120000));
      m.setObjectives(objectives);
      newMissions.add(m);

      m = new Mission("Home", "Second Maglev Rail", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Titanium", 524000));
      objectives.add(new Objective("b", "Wires", 476000));
      objectives.add(new Objective("b", "Silicon", 452000));
      m.setObjectives(objectives);
      m.setSelected(true);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #21", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Bricks", 1800000));
      objectives.add(new Objective("b", "Steel", 1480000));
      objectives.add(new Objective("b", "Cement", 880000));
      m.setSelected(true);
      m.setObjectives(objectives);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #23", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1800000));
      objectives.add(new Objective("b", "Gravel", 1480000));
      objectives.add(new Objective("b", "Cement", 880000));
      m.setObjectives(objectives);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #24", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1800000));
      objectives.add(new Objective("b", "Steel", 1480000));
      objectives.add(new Objective("b", "Rubber", 880000));
      m.setObjectives(objectives);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #25", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gravel", 1400000));
      objectives.add(new Objective("b", "Rubber", 1400000));
      objectives.add(new Objective("b", "Titanium", 800000));
      m.setObjectives(objectives);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #26", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Nails", 1560000));
      objectives.add(new Objective("b", "Cement", 1400000));
      objectives.add(new Objective("b", "Carbon", 1000000));
      m.setObjectives(objectives);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #27", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 3960000));
      objectives.add(new Objective("b", "Fuel", 1560000));
      objectives.add(new Objective("b", "U-235", 1320000));
      m.setObjectives(objectives);
      newMissions.add(m);

      m = new Mission("Home", "Local slot #28", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 16000000));
      objectives.add(new Objective("b", "Titanium", 200000));
      m.setObjectives(objectives);
      newMissions.add(m);

      // level 229
      m = new Mission("Home", "Local slot #29", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 15200000));
      objectives.add(new Objective("b", "Nails", 680000));
      m.setObjectives(objectives);
      newMissions.add(m);

      // level 243
      m = new Mission("Home", "Local slot #30", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Steel", 1200000));
      objectives.add(new Objective("b", "Rubber", 1160000));
      objectives.add(new Objective("b", "Wires", 1120000));
      m.setObjectives(objectives);
      newMissions.add(m);

      // level 257
      m = new Mission("Home", "Local slot #31", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Wood", 1600000));
      objectives.add(new Objective("b", "Nails", 1600000));
      objectives.add(new Objective("b", "Plastics", 1240000));
      m.setObjectives(objectives);
      newMissions.add(m);

      // level 222
      m = new Mission("Home", "International slot #16", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Bricks", 1200000));
      objectives.add(new Objective("b", "U-235", 1200000));
      objectives.add(new Objective("b", "Carbon", 1200000));
      m.setObjectives(objectives);
      newMissions.add(m);

      // level 237
      m = new Mission("Home", "International slot #17", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Fuel", 1320000));
      objectives.add(new Objective("b", "Titanium", 1240000));
      objectives.add(new Objective("b", "Marble", 1320000));
      m.setObjectives(objectives);
      newMissions.add(m);

      // level 252
      m = new Mission("Home", "International slot #18", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Glass", 1360000));
      objectives.add(new Objective("b", "Cement", 1280000));
      objectives.add(new Objective("b", "Rubber", 1200000));
      m.setObjectives(objectives);
      newMissions.add(m);

      m = new Mission("Home", "8 destinations", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gravel", 240000));
      objectives.add(new Objective("b", "Rubber", 200000));
      objectives.add(new Objective("b", "Carbon", 200000));
      m.setObjectives(objectives);
      newMissions.add(m);

      // second station
      m = new Mission("Home", "Second Station", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gravel", 660000));
      objectives.add(new Objective("b", "U-235", 710000));
      objectives.add(new Objective("b", "Cement", 649600));
      m.setSelected(true);
      m.setObjectives(objectives);
      newMissions.add(m);

      // third station
      m = new Mission("Home", "Third Station", ++n);
      objectives = new ArrayList<>();
      objectives.add(new Objective("b", "Gold", 6800000));
      objectives.add(new Objective("b", "Wood", 560000));
      objectives.add(new Objective("b", "Marble", 560000));
      m.setObjectives(objectives);
      newMissions.add(m);

      ds.writeMissions("Home", newMissions.toArray(new Mission[0]));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void readContractors() {
    try {
      Contractor[] readContractors = new DataStore().readContractors();
      for (Contractor contractor : readContractors) {
        System.out.println(contractor);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void generateContractors() {
    Contractor[] contractors = new Contractor[] { new Contractor("Bobby", Material.createArray()), new Contractor("Mahatma", Material.createArray()),
        new Contractor("George", Material.createArray()), new Contractor("Otto", Material.createArray()),
        new Contractor("Sam", Material.createArray()), new Contractor("Alan", Material.createArray()),
        new Contractor("Wolfgang", Material.createArray()), new Contractor("Mizuki", Material.createArray()),
        new Contractor("Lucy", Material.createArray()), new Contractor("Giovanni", Material.createArray()), };

    // try to save it to file
    try {
      new DataStore().writeContractors(contractors);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static void generateMissions() {
    String[] contractors = new String[] { "Bobby", "Mahatma", "George", "Otto", "Sam", "Alan", "Wolfgang", "Mizuki", "Lucy", "Giovanni", };

    try {
      for (String contractor : contractors) {
        Mission[] missions = readMissions(contractor);
        new DataStore().writeMissions(contractor, missions);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static Mission[] readMissions(String contractor) throws Exception {
    Set<String> mat = new HashSet<>();

    mat.add("xp");
    mat.add("money");
    mat.add("loco");
    mat.add("wagon");
    mat.add("steam");
    mat.add("diesel");
    mat.add("electric");
    mat.add("maglev");
    mat.add("ts");
    mat.add("tsany");
    Material[] ma = Material.createArray();
    for (Material material : ma) {
      mat.add(material.getName());
    }

    List<Mission> missions = new ArrayList<>();

    List<String> lines = FileUtils.readLines(new File("data/" + contractor + "_missions.txt"));
    for (String line : lines) {
      try {
        String[] s = line.split(",");
        Mission m = new Mission();
        m.setContractor(StringUtils.capitalize(contractor));
        m.setNumber(Integer.parseInt(s[0]));// 0 number
        String t = s[1].trim();// 1 type
        boolean any = false;
        if (t.length() > 1) {
          t = t.substring(0, 1);
          any = true;
        }
        m.setAny(any);

        if (t.equals("s") || t.equals("b") || t.equals("d")) {
          // ok
        } else {
          throw new Exception("ERROR! not valid type: " + contractor + " " + line);
        }
        if (s.length > 2) {

          for (int i = 2; i < s.length; i++) {
            if (i > 2 && t.equals("s"))
              t = "d";

            String[] what = s[i].trim().split(" ");

            String w = what[1];
            if (w.equals("money"))
              w = "gold";

            if (!mat.contains(w)) {
              throw new Exception("ERROR! not valid type: " + contractor + " " + line);
            }

            w = StringUtils.capitalize(w);
            if (w.equals("Ts")) {
              w = "TS";
            }
            if (w.equals("Xp")) {
              w = "XP";
            }

            if (w.equals("Tsany")) {
              w = "TSany";
            }

            Objective obj = new Objective(t, w);
            obj.setNeededAmount(Integer.parseInt(what[0]));
            m.getObjectives().add(obj);
          }
        }
        missions.add(m);
      } catch (Throwable t) {
        throw new Exception("ERROR reading line: " + contractor + " " + line);
      }
    }

    return missions.toArray(new Mission[0]);

  }

}
