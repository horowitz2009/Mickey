package com.horowitz.mickey.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

public class ContractAnalysis {

  public ContractAnalysis() {
  }

  public static void main(String[] args) {
    Map<String, Map<String, Need>> map = new ContractAnalysis().collectCurrentNeeds();
    System.err.println(map);
    new ContractAnalysis().calcALLNeeds();
  }

  public void calcALLNeeds() {
    try {
      DataStore ds = new DataStore();
      Contractor[] contractors = ds.readContractors();
      Mission[] currentMissions = ds.readCurrentMissions();
      ds.mergeCurrentWithDB(currentMissions);

      for (int i = 0; i < contractors.length; i++) {
        Contractor contractor = contractors[i];

        // Set<String> sd = new HashSet<>();
        // Set<String> sde = new HashSet<>();
        // Set<String> sdem = new HashSet<>();
        // sd.add("George");
        // sd.add("Otto");
        //
        // if (sd.contains(contractor.getName())) {
        // contractor.setAccepts("SD");
        // }
        // sde.add("Bobby");
        // sde.add("Mahatma");
        // sde.add("Sam");
        // sde.add("Alan");//after 48 maglev
        // sde.add("Wolfgang");//after 12 maglev
        //
        // if (sde.contains(contractor.getName())) {
        // contractor.setAccepts("SDE");
        // }
        //
        // sdem.add("Mizuki");
        // sdem.add("Lucy");
        // sdem.add("Giovanni");
        // if (sdem.contains(contractor.getName())) {
        // contractor.setAccepts("SDEM");
        // }
        //
        //
        // Set<String> silicons = new HashSet<>();
        // silicons.add("Alan");
        // silicons.add("Wolfgang");
        // silicons.add("Mizuki");
        // silicons.add("Lucy");
        // silicons.add("Giovanni");
        // contractor.setScanMaterials2(silicons.contains(contractor.getName()));
        //
        // silicons.add("Bobby");
        // silicons.add("Mahatma");
        // silicons.add("George");
        // silicons.add("Otto");
        // silicons.add("Sam");
        // contractor.setActive(silicons.contains(contractor.getName()));

        // System.err.println();
        // System.err.println();
        // System.err.println();
        // System.err.println(contractor.getName());
        // System.err.println("=============================");
        Mission currentMission = null;
        for (Mission cm : currentMissions) {
          if (cm.getContractor().equalsIgnoreCase(contractor.getName())) {
            currentMission = cm;
            break;
          }
        }

        Mission[] missions = ds.readMissions(contractor.getName());
        Mission[] extraMissions = ds.readMissions(contractor.getName(), "_BEST");
//        if (currentMission == null) {
//          currentMission = extraMissions[0].copy();
//          ds.writeCurrentMission(currentMission);
//        }
        missions = (Mission[]) ArrayUtils.addAll(missions, extraMissions);

        Material[] materials = Material.createArray();

        Material[] initialMaterials = contractor.getMaterials();

        List<Material> matMoreList = new ArrayList<>();

        for (int j = 0; j < materials.length; j++) {
          Material material = materials[j];
          String matName = material.getName();
          Long S = 0l;
          Long Sadd = 0l;

          // you have everything - initial materials

          Material m = extractMat(matName, initialMaterials);
          // this is what contractor has at this point
          S = (m != null) ? m.getAmount() : 0l;

          // let's see the progress of the current mission
          Objective o = getObjective(currentMission, matName);
          if (o != null) {
            if (o.getType().equals("b")) {
              S = S - o.getNeededAmount();
              if (S < 0) {
                S = 0l;
                Sadd += (-S);
              }
            } else {
              S = S + o.getNeededAmount() - o.getCurrentAmount();
            }
          }
          
          int number = 999;
          if (currentMission != null) {
            number = currentMission.getNumber();
          }
          
          // now let's check the future missions
          for (int k = 0; k < missions.length; k++) {
            Mission mm = missions[k];
            if (mm.getNumber() > number) {
              o = getObjective(mm, matName);
              if (o != null) {
                if (o.getType().equals("b")) {
                  S = S - o.getNeededAmount();
                  if (S < 0) {
                    Sadd += (-S);
                    S = 0l;
                  }
                } else {
                  S += o.getNeededAmount();
                }
              }

            }
          }

          // System.err.println("S(" + matName + ") = " + S);
          // System.err.println("Sadd(" + matName + ") = " + Sadd);
          matMoreList.add(new Material(matName, Sadd));
        } // material

        contractor.setMaterialsMore(matMoreList.toArray(new Material[0]));

      } // contractor

      ds.writeContractors(contractors);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private Objective getObjective(Mission mission, String mat) {
    if (mission != null)
      for (Objective o : mission.getObjectives()) {
        if (o.getMaterial().equals(mat)) {
          return o;
        }
      }
    return null;
  }

  private Material extractMat(String matName, Material[] materials) {
    for (int i = 0; i < materials.length; i++) {
      if (materials[i].getName().equals(matName)) {
        return materials[i];
      }
    }
    return null;
  }

  public Map<String, Map<String, Need>> collectCurrentNeeds() {
    try {
      DataStore ds = new DataStore();
      Mission[] currentMissions = ds.readCurrentMissions();
      ds.mergeCurrentWithDB(currentMissions);

      List<Need> needs = new ArrayList<>();
      for (Mission mission : currentMissions) {
        List<Objective> objectives = mission.getObjectives();
        for (Objective objective : objectives) {
          long curr = objective.getCurrentAmount();
          long needed = objective.getNeededAmount();
          if (curr < needed) {
            needs.add(new Need(objective, mission.getContractor()));
          }
        }
      }

      // Contractor[] contractors = ds.readContractors();
      Material[] allMats = Material.createArray();
      Map<String, Map<String, Need>> map = new Hashtable<>();

      for (Material material : allMats) {
        for (Need need : needs) {
          if (material.getName().equals(need.getObjective().getMaterial())) {
            Map<String, Need> innerMap = map.get(material.getName());
            if (innerMap == null) {
              innerMap = new Hashtable<String, Need>();
              map.put(material.getName(), innerMap);
            }
            innerMap.put(need.getContractorName(), need);
          }
        }
      }

      String[] otherNeeds = Material.OTHER;

      for (String other : otherNeeds) {
        for (Need need : needs) {
          if (other.equals(need.getObjective().getMaterial())) {
            Map<String, Need> innerMap = map.get(other);
            if (innerMap == null) {
              innerMap = new Hashtable<String, Need>();
              map.put(other, innerMap);
            }
            innerMap.put(need.getContractorName(), need);
          }
        }
      }

      // map.get("Steel").get("Mizuki");
      // map.get("Silicon").isEmpty() is true => nobody needs silicon right now

      return map;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Map<String, Map<String, Need>> collectCurrentNeedsALL() {
    try {
      DataStore ds = new DataStore();
      Mission[] currentMissions = ds.readCurrentMissions();
      ds.mergeCurrentWithDB(currentMissions);

      List<Need> needs = new ArrayList<>();
      for (Mission mission : currentMissions) {
        List<Objective> objectives = mission.getObjectives();
        for (Objective objective : objectives) {
          long curr = objective.getCurrentAmount();
          long needed = objective.getNeededAmount();
          if (curr < needed) {
            needs.add(new Need(objective, mission.getContractor()));
          }
        }
      }

      // Contractor[] contractors = ds.readContractors();
      Material[] allMats = Material.createArray();
      Map<String, Map<String, Need>> map = new Hashtable<>();

      for (Material material : allMats) {
        Map<String, Need> innerMap = new Hashtable<String, Need>();
        map.put(material.getName(), innerMap);
        for (Need need : needs) {
          if (material.getName().equals(need.getObjective().getMaterial())) {
            innerMap.put(need.getContractorName(), need);
          }
        }
      }

      String[] otherNeeds = Material.OTHER;

      for (String other : otherNeeds) {
        for (Need need : needs) {
          if (other.equals(need.getObjective().getMaterial())) {
            Map<String, Need> innerMap = map.get(other);
            if (innerMap == null) {
              innerMap = new Hashtable<String, Need>();
              map.put(other, innerMap);
            }
            innerMap.put(need.getContractorName(), need);
          }
        }
      }

      // map.get("Steel").get("Mizuki");
      // map.get("Silicon").isEmpty() is true => nobody needs silicon right now

      return map;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
