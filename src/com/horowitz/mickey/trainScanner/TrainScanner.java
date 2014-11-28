package com.horowitz.mickey.trainScanner;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.horowitz.mickey.ImageComparator;
import com.horowitz.mickey.ImageData;
import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.MouseRobot;
import com.horowitz.mickey.Pixel;
import com.horowitz.mickey.RobotInterruptedException;
import com.horowitz.mickey.ScreenScanner;
import com.horowitz.mickey.data.DataStore;

public class TrainScanner {

  private ScreenScanner   _scanner;
  private ImageComparator _comparator;
  private MouseRobot      _mouse;
  Logger                  LOGGER;

  public TrainScanner(ScreenScanner scanner, Logger logger) {
    super();
    _scanner = scanner;
    _comparator = scanner.getComparator();
    try {
      _mouse = new MouseRobot();
    } catch (AWTException e) {
    }
    LOGGER = logger;
  }

  public List<Train> analyzeIntTrains() {
    List<Train> trains = null;
    try {
      int xx = (_scanner.getGameWidth() - 759) / 2;
      int yy = (_scanner.getGameHeight() - 550) / 2;
      xx += _scanner.getTopLeft().x;
      yy += _scanner.getTopLeft().y;

      _mouse.click(_scanner.getTopLeft().x + 56, _scanner.getTopLeft().y + 72);
      _mouse.delay(1300);
      _mouse.click(xx + 127, yy + 101);
      _mouse.delay(2300);
      _mouse.mouseMove(_scanner.getBottomRight());

      // make sure it is trains and it is international
      Pixel p = _scanner.getTrainsAnchor().findImage();
      if (p != null) {
        int x = p.x - 333;
        int y = p.y - 37;
        // ImageData intTrainsTab = _scanner.generateImageData("int/int.bmp");
        { // if (intTrainsTab.findImage(new Rectangle(x + 94, y + 86, 66, 30)) != null) {
          // we're on the right track
          LOGGER.info("Scanning int. trains...");

          int xt = 0;
          int yt = y + 126;
          ImageData upArrow = _scanner.generateImageData("int/Up.bmp");
          boolean hasScroller = upArrow.findImage(new Rectangle(x + 697, y + 116, 43, 58)) != null;
          xt = x + (hasScroller ? 26 : 41);
          // trains = scanIntTrains(x, y, xt, yt, hasScroller);
          trains = scanIntTrainsNEW(x, y, xt, yt, hasScroller);
          // trains = scanIntTrains(x, y, xt, yt, false);
        }

      } else {
        System.err.println("can't find trains anchor");
      }

    } catch (RobotInterruptedException | IOException | AWTException e) {
      LOGGER.severe("Scanning interrupted! The data is incomplete!!!");
      e.printStackTrace();
    }
    return trains;
  }

  private void pageDown(int x, int y, int page) {
    Pixel p = new Pixel(x + 719, y + 157);
    System.err.println("Page: " + page);
    LOGGER.info("Page: " + page);
    _mouse.mouseMove(p.x, p.y + (147 * (page - 1)));
    try {
      _mouse.delay(200);
    } catch (RobotInterruptedException e) {
    }
    _mouse.click();
  }

  private List<Train> scanIntTrains(int x, int y, int xt, int yt, boolean hasScroller) throws RobotInterruptedException, IOException, AWTException {
    List<Train> trains = new ArrayList<>();
    final ImageData trCorner = _scanner.generateImageData("int/trCorner.bmp");
    if (hasScroller) {
      _mouse.mouseMove(x + 718, y + 157);
      _mouse.click();
      _mouse.delay(1000);

    }
    // train area is 357px high. USE IT!

    int yEnd = yt + 357;
    yt += 60; // skip first slot
    int i = 1;
    int j = 1;
    Train lastTrain = null;
    // scan first 5 trains fast
    while (yt <= yEnd) {
      Rectangle area = new Rectangle(xt + 669 - 9, yt - 5, 10, 25);
      Pixel p = trCorner.findImage(area);
      if (p != null) {
        yt = p.y;
        LOGGER.info("Scanning train " + i);
        trains.add(scanThisTrain(new Rectangle(xt + 3, yt, 666, 50), i++));
        _mouse.delay(200);
        yt += 57;
      } else {
        // either fail or reached the last train before the end, so
        break;
      }
      System.err.println(yt);
    }

    if (hasScroller) {
      // we have to continue
      yt = y + 126 + 120; // initial position
      lastTrain = trains.get(trains.size() - 1);
      while (!reachedEnd(x, y)) {
        // 1. click down arrow once
        _mouse.click(x + 719, y + 470);
        _mouse.delay(200);
        // 2. now find the last scanned train's position
        Rectangle expectedArea = new Rectangle(xt + 3 + 140, yEnd - 140, 528, 140);
        // writeImage(expectedArea, "expectedArea.png");
        Pixel pl = _comparator.findImage(lastTrain.getScanImage(), new Robot().createScreenCapture(expectedArea));
        if (pl == null) {
          // failed to find the last train. Probably too animated.
          // let's try another approach
          expectedArea = new Rectangle(xt + 3 + 140 + 500, yEnd - 140, 28, 140);
          // writeImage(expectedArea, "expectedArea2.png");
          BufferedImage im = lastTrain.getFullImage();
          im = im.getSubimage(im.getWidth() - 19, 0, 19, 38);
          pl = _comparator.findImage(im, new Robot().createScreenCapture(expectedArea));
          if (pl != null) {
            pl.y += 12;
          }
        }
        if (pl != null) {
          pl.y += expectedArea.y;
          //
          System.err.println("FOUND IT: ");
          // 3. look for the next train
          yt = pl.y - 12 + 60;
          // _scanner.captureGame("game.png");
          if (yt + 57 <= yEnd) {
            Rectangle area = new Rectangle(xt + 669 - 9, yt - 5, 10, 25);
            // writeImage(area, "area.png");
            Pixel p = trCorner.findImage(area);
            if (p != null) {
              yt = p.y;
              LOGGER.info("Scanning train " + i);
              lastTrain = scanThisTrain(new Rectangle(xt + 3, yt, 666, 50), i++);
              trains.add(lastTrain);
              _mouse.delay(200);
              // yt += 57;
            }
          } else {
            // not the whole train
            // repeat the process
          }
        }
      }

    }
    return trains;
  }

  private void writeImage(Rectangle area, String filename) {

    _scanner.writeImage(area, filename);

  }

  private boolean reachedEnd(int x, int y) {
    try {
      final ImageData scrollerEnded = _scanner.generateImageData("int/scrollerEnded.bmp");
      Rectangle area = new Rectangle(x + 703, y + 431, 32, 53);
      return scrollerEnded.findImage(area) != null;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private Train scanThisTrain(Rectangle trainArea, int number) throws AWTException, RobotInterruptedException {
    Rectangle trainArea1 = new Rectangle(trainArea.x + 148, trainArea.y, trainArea.width - 148, trainArea.height);
    String fullImageFilename = "data/int/train" + number + ".bmp";
    writeImage(trainArea, fullImageFilename);
    Rectangle newArea = new Rectangle(trainArea.x + 148, trainArea.y + 1, 512, 24);
    String scanImageFilename = "data/int/train" + number + "_scanThis.bmp";
    writeImage(newArea, scanImageFilename);
    Robot robot = new Robot();
    Train train = new Train(robot.createScreenCapture(trainArea1), robot.createScreenCapture(newArea));
    train.setFullImageFilename(fullImageFilename);
    train.setScanImageFilename(scanImageFilename);
    try {
      _mouse.delay(400, false);
    } catch (RobotInterruptedException e) {
    }

    // check whether is idle
    boolean isIdle = false;
    try {
      ImageData idle = _scanner.generateImageData("int/idle.bmp");
      isIdle = idle.findImage(new Rectangle(0, 0, 100, 46)) != null;
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    train.setIdle(isIdle);
    if (!isIdle) {
      _mouse.savePosition();
      _mouse.mouseMove(trainArea.x + 9, trainArea.y + 9);
      _mouse.delay(200);
      Rectangle infoArea = new Rectangle(trainArea.x, trainArea.y + trainArea.height, 590, 110);
      writeImage(infoArea, "trainInfo" + number + ".bmp");
      train.setAdditionalInfo(robot.createScreenCapture(infoArea));

      try {
        ImageData eastEnd = _scanner.generateImageData("int/eastEnd4b.bmp");
        Pixel p = null;
        // for (int i = 0; i < 3 && p == null; ++i) {
        p = eastEnd.findImage(train.getAdditionalInfo());
        // _mouse.delay(200);
        // }
        ImageData eastEnd2 = _scanner.generateImageData("int/eastEnd2.bmp");
        ImageData westEnd = _scanner.generateImageData("int/westEnd.bmp");
        ImageData westEnd2 = _scanner.generateImageData("int/westEnd2.bmp");
        System.err.println(number + ":" + p);
        if (p != null) {
          Pixel p2 = eastEnd2.findImage(train.getAdditionalInfo().getSubimage(p.x - 15, p.y, train.getAdditionalInfo().getWidth() - p.x + 15,
              train.getAdditionalInfo().getHeight()));
          Pixel pwest = westEnd.findImage(train.getAdditionalInfo().getSubimage(70, 0, 64, train.getAdditionalInfo().getHeight()));
          if (pwest == null) {
            pwest = westEnd2.findImage(train.getAdditionalInfo().getSubimage(70, 0, 64, train.getAdditionalInfo().getHeight()));
          }
          if (pwest == null) {
            pwest = new Pixel(0, 0);
          }
          int xStart = 70 + pwest.x;
          int xLast = p.x - 15 + p2.x + 4;
          int yLast = p2.y;
          int width = xLast - xStart;
          double n = (double) (width + 5) / 67;
          int nn = (int) Math.round(n);
          System.err.println(number + "  xStart: " + xStart + "; xLast: " + xLast + "; N: " + n + "    " + nn);
          int gap = 5;
          if (nn > 1) {
            gap = (width - nn * 62) / (nn - 1);
          } else {
            gap = (width - nn * 62);
          }
          System.err.println("" + gap);
          List<String> contractorNames = scanContractors(train, xStart, yLast, nn, gap, number);

          train.setContractorsBeenSent(contractorNames);
          train.setAdditionalInfoShort(train.getAdditionalInfo().getSubimage(1, 3, xStart - 2, 97));
          String shortFilename = "data/int/train" + number + "_info.bmp";
          _scanner.writeImage(train.getAdditionalInfoShort(), shortFilename);
          train.setAdditionalInfoShortFilename(shortFilename);
        } else {
          train.setIdle(true);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      _mouse.restorePosition();
    }
    return train;
  }

  private List<String> scanContractors(Train train, int xStart, int y, long nn, int gap, int number) {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < nn; ++i) {
      BufferedImage subimage = train.getAdditionalInfo().getSubimage(xStart + (i * (62 + gap)), y, 62, 77);

      // try {
      // MyImageIO.write(subimage, "PNG", new File("contractor" + number + "_" + (i + 1) + ".png"));
      // } catch (IOException e) {
      // e.printStackTrace();
      // }

      try {
        List<String> activeContractorNames = new DataStore().getActiveContractorNames();
        for (String cname : activeContractorNames) {
          String filename = "int/" + cname + "3.bmp";
          try {
            BufferedImage image = ImageIO.read(ImageManager.getImageURL(filename));
            if (image != null) {
              Pixel p = _comparator.findImage(image, subimage);
              if (p != null) {
                System.err.println("This is " + cname);
                result.add(cname);
                break;
              }
            }
          } catch (Exception e) {
            System.err.println("can't find " + filename);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

    return result;
  }

  public void sendTrains(List<Train> trains) {
    int x = 0;
    int y = 0;
    try {
      int xx = (_scanner.getGameWidth() - 759) / 2;
      int yy = (_scanner.getGameHeight() - 550) / 2;
      xx += _scanner.getTopLeft().x;
      yy += _scanner.getTopLeft().y;

      _mouse.click(_scanner.getTopLeft().x + 56, _scanner.getTopLeft().y + 72);
      _mouse.delay(1300, false);
      _mouse.click(xx + 127, yy + 101);
      _mouse.delay(2300, false);
      _mouse.mouseMove(_scanner.getBottomRight());

      // make sure it is trains and it is international
      Pixel p = _scanner.getTrainsAnchor().findImage();
      if (p != null) {
        x = p.x - 333;
        y = p.y - 37;
        // ImageData intTrainsTab = _scanner.generateImageData("int/int.bmp");
        { // if (intTrainsTab.findImage(new Rectangle(x + 94, y + 86, 66, 30)) != null) {
          // we're on the right track
          LOGGER.info("Sending int. trains...");

          int xt = 0;
          int yt = y + 126;
          ImageData upArrow = _scanner.generateImageData("int/Up.bmp");
          boolean hasScroller = upArrow.findImage(new Rectangle(x + 697, y + 116, 43, 58)) != null;
          xt = x + (hasScroller ? 26 : 41);
          sendIntTrains(trains, x, y, xt, yt, hasScroller);
        }

      } else {
        System.err.println("can't find trains anchor");
      }
    } catch (IOException | AWTException e) {
      LOGGER.severe("Ooops. Something went wrong!");
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      LOGGER.severe("Interrupted by user");
    } finally {
      if (x != 0)
        _mouse.click(x + 159, y + 524);
    }
  }

  private void sendIntTrains(List<Train> trains, int x, int y, int xt, int yt, boolean hasScroller) throws RobotInterruptedException, IOException,
      AWTException {
    final ImageData trCorner = _scanner.generateImageData("int/trCorner.bmp");
    if (hasScroller) {
      _mouse.mouseMove(x + 718, y + 157);
      _mouse.click();
      _mouse.delay(1000);

    }
    yt += 60; // skip first slot
    // for now all trains must be sent!
    for (int i = 0; i < trains.size(); ++i) {
      Rectangle area = new Rectangle(xt + 669 - 9, yt - 5, 10, 25);
      Pixel p = trCorner.findImage(area);
      if (p != null) {
        yt = p.y;
        LOGGER.info("Sending train " + i);
        Rectangle singleTrainArea = new Rectangle(xt + 3, yt, 666, 50);

        for (Train train : trains) {
          if (!train.getContractorsToSend().isEmpty()) { // not good. next version make it to handle such situations
            if (train.getSentTime() == 0 || System.currentTimeMillis() - train.getSentTime() > 4 * 60 * 60000) {
              Pixel p1 = _comparator.findImage(train.getScanImage(), new Robot().createScreenCapture(singleTrainArea));
              if (p1 != null) {
                // found it, then send it
                sendThisTrain(train, xt + 3, yt);
                break;
              }
            }
          }
        }

        _mouse.delay(200);
      }// if
    }// for
  }

  private void sendThisTrain(Train train, int x, int y) throws RobotInterruptedException, IOException, AWTException {
    _mouse.mouseMove(x + 72, y + 25);
    _mouse.delay(300);
    _mouse.mouseMove(x + 200, y + 25);
    _mouse.click();
    _mouse.delay(300);
    // it is expected a SendDialiof been opened
    int xx = (_scanner.getGameWidth() - 760) / 2;
    int yy = (_scanner.getGameHeight() - 550) / 2;
    xx += _scanner.getTopLeft().x;
    yy += _scanner.getTopLeft().y;

    ImageData selectAnchor = _scanner.generateImageData("int/Select.bmp");
    Pixel p = selectAnchor.findImage(new Rectangle(xx, yy, 200, 75));
    if (p != null) {
      // find and select contractors
      final Pixel tl = new Pixel(p.x - 35, p.y - 32);
      // Rectangle carea = new Rectangle(tl.x + 41, tl.y + 90, 678, 96);
      Rectangle carea = new Rectangle(tl.x + 42, tl.y + 72, 676, 20);
      for (String cname : train.getContractorsToSend()) {
        for (int page = 0; page < 3; page++) {
          BufferedImage cimage = new Robot().createScreenCapture(carea);
          writeImage(carea, "carea.bmp");
          BufferedImage contractorImage = ImageIO.read(ImageManager.getImageURL("int/" + cname + "_name.bmp"));
          Pixel cp = _comparator.findImage(contractorImage, cimage);
          if (cp != null) {
            // found the contractor
            _mouse.mouseMove(carea.x + cp.x + 20, carea.y + cp.y + 50);
            _mouse.delay(250);
            _mouse.click();
            _mouse.delay(750);
            break;
          } else {
            _mouse.delay(250);
            _mouse.click(tl.x + 739, tl.y + 131);
            _mouse.delay(750);
          }
        }
      }

      // TODO click send and choose 4h way
      _mouse.mouseMove(tl.x + 475, tl.y + 517);
      _mouse.delay(300);
      _mouse.click();
      _mouse.delay(700);
      _mouse.click(tl.x + 475, tl.y + 498);
      _mouse.delay(400);

      // at the end
      // train.setSentTime(System.currentTimeMillis());
    }

  }

  private List<Train> scanIntTrainsNEW(int x, int y, int xt, int yt, boolean hasScroller) throws RobotInterruptedException, IOException, AWTException {
    List<Train> trains = new ArrayList<>();
    if (hasScroller) {
      _mouse.mouseMove(x + 719, y + 157);// center of scrollball placed in the beginning
      _mouse.click();
      _mouse.delay(1000);
    }

    yt += 63; // skip first slot
    xt += 3;

    scanSlots(xt, yt, 1, trains);
    if (hasScroller) {
      pageDown(x, y, 2);
      _mouse.delay(2000);
      scanSlots(xt, yt, 2, trains);
      _mouse.delay(1000);

      pageDown(x, y, 3);
      _mouse.delay(2000);
      scanSlots(xt, yt, 3, trains);
      _mouse.delay(1000);

      LOGGER.info("DONE");
    }
    return trains;
  }

  private void scanSlots(int xt, int yt, int page, List<Train> trains) throws AWTException, IOException, RobotInterruptedException {
    for (int slot = 0; slot < 5; slot++) {
      _mouse.delay(400);
      Rectangle slotArea = new Rectangle(xt, yt + slot * 60, 666, 50);
      int number = slot + 1 + ((page - 1) * 5);
      String fullImageFilename = "data/int/train" + number + ".png";
      writeImage(slotArea, fullImageFilename);
      Rectangle newArea = new Rectangle(slotArea.x + 148, slotArea.y + 2, 512, 24);
      String scanImageFilename = "data/int/train" + number + "_scanThis.bmp";
      writeImage(newArea, scanImageFilename);
      Robot robot = new Robot();
      Train train = new Train(robot.createScreenCapture(slotArea), robot.createScreenCapture(newArea));
      train.setFullImageFilename(fullImageFilename);
      train.setScanImageFilename(scanImageFilename);

      Rectangle delArea = new Rectangle(slotArea.x, slotArea.y + 24, 50, 20);
      ImageData delData = _scanner.generateImageData("int/Del.bmp");
      if (delData.findImage(delArea) != null) {
        LOGGER.info("Found Del.bmp in slot " + slot);
        // scan additional info
        _mouse.savePosition();
        _mouse.mouseMove(slotArea.x + 9, slotArea.y + 9);
        _mouse.delay(800);
        Rectangle infoArea = new Rectangle(slotArea.x, slotArea.y + slotArea.height, 590, 110);
        String addInfoFilename = "trainInfo" + number + ".bmp";
        writeImage(infoArea, addInfoFilename);
        train.setAdditionalInfo(cutToEdge(robot.createScreenCapture(infoArea)));

        train.setAdditionalInfoFilename(addInfoFilename);

        //short info
        ImageData westEnd = _scanner.generateImageData("int/westEnd.bmp");
        ImageData westEnd2 = _scanner.generateImageData("int/westEnd2.bmp");
        Pixel pwest = westEnd.findImage(train.getAdditionalInfo().getSubimage(60, 0, 64, train.getAdditionalInfo().getHeight()));
        if (pwest == null) {
          pwest = westEnd2.findImage(train.getAdditionalInfo().getSubimage(60, 0, 64, train.getAdditionalInfo().getHeight()));
        } else {
          System.err.println("found westEnd");
        }
        if (pwest == null) {
          pwest = new Pixel(13, 0);
        } else {
          System.err.println("found westEnd2");
        }
        
        System.err.println(pwest);

        train.setAdditionalInfoShort(train.getAdditionalInfo().getSubimage(0, 0, 60 + pwest.x - 1, train.getAdditionalInfo().getHeight()));
        
        
        String shortFilename = "data/int/trainInfo" + number + "_short.png";
        _scanner.writeImage(train.getAdditionalInfoShort(), shortFilename);
        train.setAdditionalInfoShortFilename(shortFilename);
        train.setIdle(false);

        List<String> contractorNames = scanContractorsSimple(train);

        train.setContractorsBeenSent(contractorNames);

        _mouse.restorePosition();
      } else {
        train.setIdle(true);
      }
      trains.add(train);
    }
  }

  private BufferedImage cutToEdge(BufferedImage image) {
    BufferedImage result = image;
    try {
      BufferedImage edgeImage = ImageIO.read(ImageManager.getImageURL("int/brEdge.bmp"));
      Pixel p = _comparator.findImage(edgeImage, image);
      if (p != null) {
        System.err.println("found edge " + p);
        result = image.getSubimage(0, 0, p.x + edgeImage.getWidth(), p.y + edgeImage.getHeight());
      }
    } catch (IOException e) {
    }
    return result;
  }

  private List<String> scanContractorsSimple(Train train) {
    List<String> result = new ArrayList<>();

    if (train.getAdditionalInfo() != null) {
      BufferedImage subimage = train.getAdditionalInfo().getSubimage(60, 0, train.getAdditionalInfo().getWidth() - 60,
          train.getAdditionalInfo().getHeight());
      try {
        List<String> activeContractorNames = new DataStore().getActiveContractorNames();
        while (subimage.getWidth() >= 50) {
          boolean atLeastOneFound = false;
          for (String cname : activeContractorNames) {
            if (!result.contains(cname)) {
              String filename = "int/" + cname + "3.bmp";
              BufferedImage cimage = ImageIO.read(ImageManager.getImageURL(filename));
              if (cimage != null) {
                Pixel p = _comparator.findImage(cimage, subimage);
                if (p != null) {
                  atLeastOneFound = true;
                  System.err.println("Found " + cname);
                  subimage = subimage.getSubimage(cimage.getWidth() + 3, 0, subimage.getWidth() - cimage.getWidth() - 3, subimage.getHeight());
                  result.add(cname);
                  break;
                }// if
              }// if cimage != null
            }// if contains
          }// for
          if (!atLeastOneFound) {
            break;
          }
        } // while
      } catch (IOException e) {
        e.printStackTrace();
      }
    }// if
    return result;
  }

}
