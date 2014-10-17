package com.horowitz.mickey;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.data.Material;
import com.horowitz.mickey.data.Mission;
import com.horowitz.mickey.data.Objective;
import com.horowitz.mickey.ocr.OCR;

public class MissionScanner {

  private OCR                          _ocr;

  private static final ImageComparator COMPARATOR  = new SimilarityImageComparator(0.04, 20);

  private List<ImageDataExt>           _objectives = new ArrayList<>();

  public MissionScanner() {
    super();
    _ocr = new OCR("masks.txt");// TODO
    try {
      // steam
      _objectives.add(new ImageDataExt(new ImageData("steamTrains.bmp", null, COMPARATOR, 0, 0), "steam", new Rectangle(-13, 53, 100, 22)));
      // diesel
      _objectives.add(new ImageDataExt(new ImageData("dieselTrains.bmp", null, COMPARATOR, 0, 0), "diesel", new Rectangle(-13, 53, 100, 22)));

      // electric
      // TODO

      // maglev
      // TODO
    } catch (IOException e) {
    }

    // Gold
    addObjectives("Gold");
    addObjectives("Wood");
    addObjectives("Nails");
    addObjectives("Bricks");
    addObjectives("Glass");
    addObjectives("Fuel");
    addObjectives("Steel");
    addObjectives("Gravel");
    addObjectives("U-235");
    addObjectives("Cement");
    addObjectives("Rubber");
    addObjectives("Carbon");
    addObjectives("Titanium");
    addObjectives("Marble");
    addObjectives("Wires");
    addObjectives("Plastics");
    addObjectives("Silicon");

  }

  private void addObjectives(String name) {
    try {
      _objectives.add(new ImageDataExt(new ImageData(name + "10.bmp", null, COMPARATOR, 0, 0), name, new Rectangle(59, -15, 160, 55)));
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

    try {
      _objectives.add(new ImageDataExt(new ImageData(name + "8.bmp", null, COMPARATOR, 0, 0), name, new Rectangle(20, -10, 140, 22)));
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public int[] scanMissionNumbers(BufferedImage contractorImage) {
    BufferedImage subimage = contractorImage.getSubimage(376, 388, 71, 21);
    OCR ocr = new OCR("masksMission.txt", 3, new Color(255, 255, 255), new Color(16, 161, 246));
    String res = ocr.scanImage(subimage);
    if (res.indexOf("/") > 0) {
      String[] ss = res.split("/");
      int[] numbers = new int[2];
      try {
        numbers[0] = Integer.parseInt(ss[0]);
        numbers[1] = Integer.parseInt(ss[1]);
        return numbers;
      } catch (NumberFormatException e) {
        // TODO handle OCR errors better
        e.printStackTrace();
      }
    }
    return null;
  }

  public void scanCurrentMission(BufferedImage contractorImage, Mission mission) {
    // this is the Objectives area without the description part
    BufferedImage objArea = contractorImage.getSubimage(456, 60 + 93, 288, 326 - 93);
    BufferedImage subimage = contractorImage.getSubimage(0, 0, objArea.getWidth(), objArea.getHeight());

   
    // VERSION 1 - let's scan everything -- NOPE. IT DOESN'T WORK
    if (false) {
      int yCovered = 0;
      List<Objective> objectives = new ArrayList<>();
    
      do {
        String res = null;
        Pixel p = null;
        ImageDataExt theIDE = null;
        for (ImageDataExt ide : _objectives) {
          OCR ocrRed = new OCR("masksMission.txt", 3, new Color(200, 1, 1), new Color(245, 245, 245));
          OCR ocrGray = new OCR("masksMission.txt", 3, new Color(103, 103, 103), new Color(245, 245, 245));
          p = ide._imageData.findImage(subimage);
          if (p != null) {
            // found a resource. now let's scan the numbers
            BufferedImage textImage = subimage.getSubimage(p.x + ide._correspondingArea.x, p.y + ide._correspondingArea.y,
                ide._correspondingArea.width, ide._correspondingArea.height);
            String resRed = ocrRed.scanImage(textImage);
            String resGray = ocrGray.scanImage(textImage);

            yCovered = p.y + ide._correspondingArea.y + ide._correspondingArea.height;
            res = resRed != null && resRed.length() > 0 ? resRed + resGray : resGray;
            theIDE = ide;
            break;
          }
        }
        if (res !=null) {
          //create objective and move forward
          Objective o = new Objective("d", theIDE._name);
          String[] ss = res.split("/");
          o.setCurrentAmount(Integer.parseInt(ss[0]));
          o.setNeededAmount(Integer.parseInt(ss[1]));
          objectives.add(o);
        }
        if (p != null && yCovered < subimage.getHeight()) {
          subimage = contractorImage.getSubimage(0, yCovered, objArea.getWidth(), objArea.getHeight());
        } else {
          //nothing found :(
          break;
        }
      } while (yCovered < subimage.getHeight());
      
      mission.setObjectives(objectives);
    }
    
    // VERSION 2 - knowing the number of the mission, let's scan smart

    
    
    for (Objective o : mission.getObjectives()) {
      String what = o.getMaterial();
      
      ImageDataExt theIDE = null;
      for (ImageDataExt ide : _objectives) {
        if (ide._name.equals(what)) {
          theIDE = ide;
          break;
        }
      }
      if (theIDE != null) {
        OCR ocrRed = new OCR("masksMission.txt", 3, new Color(200, 1, 1), new Color(245, 245, 245));
        OCR ocrGray = new OCR("masksMission.txt", 3, new Color(103, 103, 103), new Color(245, 245, 245));
        Pixel p = theIDE._imageData.findImage(subimage);
        if (p != null) {
          // found the resource. Now let's scan the numbers
          BufferedImage textImage = subimage.getSubimage(p.x + theIDE._correspondingArea.x, p.y + theIDE._correspondingArea.y,
              theIDE._correspondingArea.width, theIDE._correspondingArea.height);
          String resRed = ocrRed.scanImage(textImage);
          String resGray = ocrGray.scanImage(textImage);

          String res = resRed != null && resRed.length() > 0 ? resRed + resGray : resGray;
          if (res != null) {
            //set values to the existing objective
            String[] ss = res.split("/");
            o.setCurrentAmount(Integer.parseInt(ss[0]));
            o.setNeededAmount(Integer.parseInt(ss[1]));
          }
        }
      }//if
      
    }//for
  }


  public static void main(String[] args) {
    try {
      BufferedImage image = ImageIO.read(ImageManager.getImageURL("bobbymaterials.bmp"));
      MissionScanner scanner = new MissionScanner();
      int[] numbers = scanner.scanMissionNumbers(image);
      if (numbers != null) {
        int number = numbers[0];
        Mission[] readMissions = new DataStore().readMissions("Bobby");
        for (Mission m : readMissions) {
          if (m.getNumber() == number) {
            scanner.scanCurrentMission(image, m);
          }
        }
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private static class ImageDataExt {
    ImageData _imageData;
    String    _name;
    Rectangle _correspondingArea;

    public ImageDataExt(ImageData imageData, String name, Rectangle correspondingArea) {
      super();
      _imageData = imageData;
      _name = name;
      _correspondingArea = correspondingArea;
    }

  }

  public Mission scanCurrentMission(BufferedImage contractorImage) {
    // TODO Auto-generated method stub
    return null;
  }

}
