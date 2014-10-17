package com.horowitz.mickey.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class InitialMassLoader {

  public static void main(String[] args) {
    System.out.println("genereating contractors and missions");
    generateContractors();
    generateMissions();
    System.out.println("Done.");
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
    String[] contractors = new String[] { "bobby", "mahatma", "george", "otto", "sam", "alan", "wolfgang", "mizuki", "lucy", "giovanni", };

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
      mat.add(material.getName().toLowerCase());
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
