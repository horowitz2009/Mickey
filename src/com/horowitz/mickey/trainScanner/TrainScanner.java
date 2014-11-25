package com.horowitz.mickey.trainScanner;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
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
import com.horowitz.mickey.common.MyImageIO;
import com.horowitz.mickey.data.Contractor;
import com.horowitz.mickey.data.DataStore;

public class TrainScanner {

  private ScreenScanner   _scanner;
  private ImageComparator _comparator;
  private MouseRobot      _mouse;
  private Logger          LOGGER;

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
          trains = scanIntTrains(x, y, xt, yt, hasScroller);
          // trains = scanIntTrains(x, y, xt, yt, false);
        }

      } else {
        System.err.println("can't find trains anchor");
      }

    } catch (RobotInterruptedException | IOException | AWTException e) {
      e.printStackTrace();
    }
    return trains;
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
      }
    }

    if (hasScroller) {
      // we have to continue
      yt = y + 126 + 120; // initial position
      lastTrain = trains.get(trains.size() - 1);
      while (!reachedEnd(x, y)) {
        // 1. click down arrow once
        _mouse.click(x + 719, y + 470);
        _mouse.delay(200, false);
        // 2. now find the last scanned train's position
        Rectangle expectedArea = new Rectangle(xt + 3 + 140, yEnd - 140, 528, 140);
        _scanner.writeImage(expectedArea, "expectedArea.png");
        Pixel pl = _comparator.findImage(lastTrain.getScanImage(), new Robot().createScreenCapture(expectedArea));
        if (pl != null) {
          pl.x += expectedArea.x;
          pl.y += expectedArea.y;
          //
          System.err.println("FOUND IT: ");
          // 3. look for the next train
          yt = pl.y - 12 + 60;
          _scanner.captureGame("game.png");
          if (yt + 57 <= yEnd) {
            Rectangle area = new Rectangle(xt + 669 - 9, yt - 5, 10, 25);
            _scanner.writeImage(area, "area.png");
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

  private void scanIntTrainsOLD2(int x, int y, int xt, int yt, boolean hasScroller) throws RobotInterruptedException, IOException, AWTException {
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
    while (yt <= yEnd) { // check 53 or 56 or 57?
      LOGGER.info("yt= " + yt + ", yEnd=" + yEnd);
      Rectangle area = new Rectangle(xt + 669 - 9, yt - 5, 10, 25);
      // Rectangle area2 = new Rectangle(xt + 169 - 9, yt - 5, 510, 45);
      // _scanner.writeImage(area2, "TrainAREA_"+(j)+".bmp");
      Pixel p = trCorner.findImage(area);
      if (p != null) {
        yt = p.y;
        LOGGER.info("p != null => yt= " + yt + ", yEnd=" + yEnd);
        if (yt + 50 <= yEnd) {
          LOGGER.info("Scanning train " + i);
          scanThisTrain(new Rectangle(xt + 3, yt, 666, 50), i++);
          _mouse.delay(200);
        } else {
          // LOGGER.info("DAMN WHY ??? Scanning train " + i);
          // scanThisTrain(new Rectangle(xt + 3, yt, 666, 50), i);
          // _mouse.delay(200);
          // LOGGER.info("DAMN " + i);
          _mouse.click(x + 719, y + 470);
          _mouse.delay(500);
          // //try again
          yt -= 40;
          continue;
        }
        if (hasScroller && yt + 60 > yEnd && !reachedEnd(x, y)) {
          LOGGER.info("hasScroller:" + hasScroller + ", reachedEnd:" + reachedEnd(x, y) + ", yt= " + yt + ", yEnd=" + yEnd);
          _scanner.captureGame("Train1_" + (j) + ".png");
          _mouse.click(x + 719, y + 470);
          _mouse.delay(500);
          // _mouse.click(x + 719, y+ 470);
          // _mouse.delay(500);
          _scanner.captureGame("Train2_" + (j++) + ".png");
          yt -= 75; // -75 + 60 = -15
        }

        yt += 57;

        LOGGER.info("finally yt= " + yt + ", yEnd=" + yEnd);
      }
      yt += 1;
    }
  }

  private void scanIntTrainsOLD(int x, int y, int xt, int yt, boolean hasScroller) throws RobotInterruptedException, IOException, AWTException {
    final ImageData trCorner = _scanner.generateImageData("int/trCorner.bmp");
    final ImageData brCorner = _scanner.generateImageData("int/brCorner.bmp");
    if (hasScroller) {
      LOGGER.info("scroller");
      _mouse.mouseMove(x + 718, y + 157);
      _mouse.click();
      _mouse.delay(1000);
      // train area is 357px high
      yt += 60; // first line is Add new Train offer
      Rectangle area = new Rectangle(xt + 666 - 9, yt - 1, 10, 61);
      Pixel p = trCorner.findImage(area);
      if (p != null) {
        // ok
        // scan first 5 trains
        // scanThisTrain(new Rectangle(p.x - 666 + 8, p.y, 666, 50), 1);
        Pixel lastOne = null;
        int i = 0;
        for (; i < 5; ++i) {
          area = new Rectangle(xt + 666 - 9, yt - 1, 10, 10);
          p = trCorner.findImage(area);
          if (p != null) {
            LOGGER.info("Scanning train " + (i + 1));
            lastOne = p;
            scanThisTrain(new Rectangle(xt, yt, 666, 50), i + 1);
            try {
              _mouse.delay(200);
            } catch (RobotInterruptedException e) {
            }
          }
          yt += 60;
        }
        BufferedImage lastTrain = null;
        if (lastOne != null) {
          // Good! We're moving well
          while (!reachedEnd(x, y)) {
            i++;
            yt = lastOne.y;
            _mouse.wheelDown(2);
            _mouse.delay(500);
            yt -= 12; // -72 + 60 = -12
            area = new Rectangle(xt + 666 - 9, yt - 5, 10, 20);
            p = trCorner.findImage(area);
            if (p != null) {
              LOGGER.info("Scanning train " + (i + 1));
              lastOne = p;
              yt = p.y;
              scanThisTrain(new Rectangle(xt, yt, 666, 50), i + 1);
              try {
                _mouse.delay(200);
              } catch (RobotInterruptedException e) {
              }
            }

          }

          // area = new Rectangle(lastOne.x - 1, lastOne.y + 50 - 13, 12, 14);
          // _scanner.writeImage(area, "lastTrainAREA.bmp");
          // p = brCorner.findImage(area);
          // if (p != null) {
          // LOGGER.info("found brCorner ");
          // //remember this train
          // area = new Rectangle(p.x - 52 + 8, p.y - 38 + 8, 52, 38);
          // lastTrain = new Robot().createScreenCapture(area);
          // _scanner.writeImage(area, "lastTrain.bmp");
          // }
        }
      }
    } else {
      LOGGER.info("no scroller");
      yt += 60;// skip first
      for (int i = 1; i < 6; ++i) {
        Rectangle area = new Rectangle(xt + 666 - 9, yt - 1, 10, 10);
        Pixel p = trCorner.findImage(area);
        if (p != null) {
          scanThisTrain(new Rectangle(xt, yt, 666, 50), i + 1);
          try {
            _mouse.delay(200);
          } catch (RobotInterruptedException e) {
          }
        }
        yt += 60;
      }

    }

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
    _scanner.writeImage(trainArea, "train" + number + ".bmp");
    Rectangle newArea = new Rectangle(trainArea.x + 148, trainArea.y + 1, 512, 24);
    _scanner.writeImage(newArea, "train" + number + "_scanThis.bmp");
    Robot robot = new Robot();
    Train train = new Train(robot.createScreenCapture(trainArea1), robot.createScreenCapture(newArea));

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
      _scanner.writeImage(infoArea, "trainInfo" + number + ".bmp");
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
          List<ContractorView> contractorViews = scanContractors(train, xStart, yLast, nn, gap, number);

          train.setContractorViews(contractorViews);
          train.setAdditionalInfoShort(train.getAdditionalInfo().getSubimage(1, 3, xStart - 2, 97));
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

  private List<ContractorView> scanContractors(Train train, int xStart, int y, long nn, int gap, int number) {
    List<ContractorView> result = new ArrayList<>();
    for (int i = 0; i < nn; ++i) {
      BufferedImage subimage = train.getAdditionalInfo().getSubimage(xStart + (i * (62 + gap)), y, 62, 77);

      try {
        MyImageIO.write(subimage, "PNG", new File("contractor" + number + "_" + (i + 1) + ".png"));
      } catch (IOException e) {
        e.printStackTrace();
      }

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
                ContractorView cv = new ContractorView(cname, image);
                result.add(cv);
                break;
              }
            }
          } catch (Exception e) {
            System.err.println("can't find " + filename);
          }
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    return result;
  }

  public void sendTrains(List<Train> trains) {
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
        LOGGER.info("Sending train " + i);
        // TODO sendThisTrain(new Rectangle(xt + 3, yt, 666, 50), i++);
        _mouse.delay(200);
        yt += 57;
      }
    }

    if (hasScroller) {
      // we have to continue
      yt = y + 126 + 120; // initial position
      lastTrain = trains.get(trains.size() - 1);
      while (!reachedEnd(x, y)) {
        // 1. click down arrow once
        _mouse.click(x + 719, y + 470);
        _mouse.delay(200, false);
        // 2. now find the last scanned train's position
        Rectangle expectedArea = new Rectangle(xt + 3 + 140, yEnd - 140, 528, 140);
        _scanner.writeImage(expectedArea, "expectedArea.png");
        Pixel pl = _comparator.findImage(lastTrain.getScanImage(), new Robot().createScreenCapture(expectedArea));
        if (pl != null) {
          pl.x += expectedArea.x;
          pl.y += expectedArea.y;
          //
          System.err.println("FOUND IT: ");
          // 3. look for the next train
          yt = pl.y - 12 + 60;
          _scanner.captureGame("game.png");
          if (yt + 57 <= yEnd) {
            Rectangle area = new Rectangle(xt + 669 - 9, yt - 5, 10, 25);
            _scanner.writeImage(area, "area.png");
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
  }

}
