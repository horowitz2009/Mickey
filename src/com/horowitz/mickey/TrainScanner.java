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
          int yt = y + 129;
          ImageData upArrow = _scanner.generateImageData("int/Up.bmp");
          boolean hasScroller = upArrow.findImage(new Rectangle(x + 697, y + 116, 43, 58)) != null;
          xt = x + (hasScroller ? 29 : 44);
          scanIntTrains(x, y, xt, yt, hasScroller);
        }

      }

    } catch (RobotInterruptedException | IOException | AWTException e) {
      e.printStackTrace();
    }

  }

  private void scanIntTrains(int x, int y, int xt, int yt, boolean hasScroller) throws RobotInterruptedException, IOException, AWTException {
    final ImageData trCorner = _scanner.generateImageData("int/trCorner.bmp");
    final ImageData brCorner = _scanner.generateImageData("int/brCorner.bmp");
    if (hasScroller) {
      LOGGER.info("scroller");
      _mouse.mouseMove(x + 718, y + 157);
      _mouse.click();
      _mouse.delay(1000);
      // train area is 357px high
      yt += 60; //first line is Add new Train offer
      Rectangle area = new Rectangle(xt + 666 - 9, yt - 1, 10, 61);
      Pixel p = trCorner.findImage(area);
      if (p != null) {
        // ok
        //scan first 5 trains
        //scanThisTrain(new Rectangle(p.x - 666 + 8, p.y, 666, 50), 1);
        Pixel lastOne = null;
        for (int i = 0; i < 5; ++i) {
          area = new Rectangle(xt + 666 - 9, yt - 1, 10, 10);
          p = trCorner.findImage(area);
          if (p != null) {
            LOGGER.info("Scanning train " + (i+1));
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
          //Good! We're moving well
          area = new Rectangle(lastOne.x - 1, lastOne.y + 50 - 13, 12, 14);
          _scanner.writeImage(area, "lastTrainAREA.bmp");
          p = brCorner.findImage(area);
          if (p != null) {
            LOGGER.info("found brCorner ");
            //remember this train
            area = new Rectangle(p.x - 52 + 8, p.y - 38 + 8, 52, 38);
            lastTrain = new Robot().createScreenCapture(area);
            _scanner.writeImage(area, "lastTrain.bmp");
          }
        }
      }
    } else {
      LOGGER.info("no scroller");
      yt += 60;//skip first
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

  private void scanThisTrain(Rectangle trainArea, int number) {
    _scanner.writeImage(trainArea, "train" + number + ".bmp");

    Rectangle newArea = new Rectangle(trainArea.x + 148, trainArea.y + 12, 512, 20);
    _scanner.writeImage(newArea, "train" + number + "_scanThis.bmp");
  }

}
