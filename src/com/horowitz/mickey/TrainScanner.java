package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;

public class TrainScanner {

  private ScreenScanner _scanner;
  private MouseRobot    _mouse;
  private Logger        LOGGER;

  public TrainScanner(ScreenScanner scanner, Logger logger) {
    super();
    _scanner = scanner;
    try {
      _mouse = new MouseRobot();
    } catch (AWTException e) {
    }
    LOGGER = logger;
  }

  public void analyzeIntTrains() {

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
        ImageData intTrainsTab = _scanner.generateImageData("int/int.bmp");
        if (intTrainsTab.findImage(new Rectangle(x + 94, y + 86, 66, 30)) != null) {
          // we're on the right track
          LOGGER.info("BINGO!");

          int xt = 0;
          int yt = y + 126;
          ImageData upArrow = _scanner.generateImageData("int/Up.bmp");
          boolean hasScroller = upArrow.findImage(new Rectangle(x + 697, y + 116, 43, 58)) != null;
          xt = x + (hasScroller ? 26 : 41);
          scanIntTrains(x, y, xt, yt, hasScroller);
        }

      }

    } catch (RobotInterruptedException | IOException | AWTException e) {
      e.printStackTrace();
    }

  }

  private void scanIntTrains(int x, int y, int xt, int yt, boolean hasScroller) throws RobotInterruptedException, IOException, AWTException {
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

  private void scanThisTrain(Rectangle trainArea, int number) {
    _scanner.writeImage(trainArea, "train" + number + ".bmp");

    Rectangle newArea = new Rectangle(trainArea.x + 148, trainArea.y + 12, 512, 20);
    _scanner.writeImage(newArea, "train" + number + "_scanThis.bmp");
  }

}
